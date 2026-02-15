#!/usr/bin/env python3
"""
Benchmark wavefront parallelism for global linear alignment.

This script is intended to be run from the repository root:
    python scripts/benchmark_parallelism.py

Workflow:
1. Build the CLI jar (skipping tests).
2. Run two warmup alignments (ignored in final metrics).
3. Measure runtime for multiple sequence lengths and thread counts.
4. Save results to CSV and (if matplotlib is available) produce two plots.
"""

from __future__ import annotations

import csv
import random
import statistics
import subprocess
import sys
import time
from pathlib import Path
from typing import Dict, List


THREAD_COUNTS = [1, 2, 4, 8]
SEQUENCE_LENGTHS = [5000, 10000, 15000]
WARMUP_LENGTH = 1000
WARMUP_RUNS = 2
REPEATS = 3
GAP_PENALTY = 2
DNA_ALPHABET = "ACGT"
CSV_COLUMNS = ["length", "threads", "median_seconds", "speedup"]


def repo_root() -> Path:
    return Path(__file__).resolve().parents[1]


def build_cli_jar(root: Path) -> Path:
    """Build the CLI jar and return the path to the shaded jar."""
    java_dir = root / "java"
    if sys.platform.startswith("win"):
        mvnw = java_dir / "mvnw.cmd"
        cmd = [str(mvnw), "-q", "-pl", "cli", "-am", "package", "-DskipTests"]
    else:
        mvnw = java_dir / "mvnw"
        cmd = [str(mvnw), "-q", "-pl", "cli", "-am", "package", "-DskipTests"]

    print("Building CLI jar...")
    subprocess.run(cmd, cwd=java_dir, check=True)

    target_dir = root / "java" / "cli" / "target"
    preferred = target_dir / "bioseq-cli.jar"
    if preferred.exists():
        return preferred

    # Fallback in case naming changes in Maven configuration.
    candidates = sorted(target_dir.glob("*.jar"))
    filtered = [
        p
        for p in candidates
        if "original-" not in p.name and not p.name.endswith("-sources.jar") and not p.name.endswith("-javadoc.jar")
    ]
    if not filtered:
        raise FileNotFoundError(f"Could not find built CLI jar in {target_dir}")
    return filtered[0]


def random_dna(length: int, rng: random.Random) -> str:
    """Generate a random DNA sequence of the requested length."""
    return "".join(rng.choice(DNA_ALPHABET) for _ in range(length))


def run_alignment(
    root: Path,
    jar_path: Path,
    matrix_path: Path,
    seq1: str,
    seq2: str,
    threads: int,
) -> float:
    """Run one CLI alignment and return wall-clock runtime in seconds."""
    cmd = [
        "java",
        "-jar",
        str(jar_path),
        "global_linear",
        "--seq1",
        seq1,
        "--seq2",
        seq2,
        "--matrix",
        str(matrix_path),
        "--gap",
        str(GAP_PENALTY),
        "--threads",
        str(threads),
    ]

    start = time.perf_counter()
    completed = subprocess.run(
        cmd,
        cwd=root,
        capture_output=True,
        text=True,
    )
    elapsed = time.perf_counter() - start

    if completed.returncode != 0:
        stdout_tail = completed.stdout[-800:] if completed.stdout else ""
        stderr_tail = completed.stderr[-800:] if completed.stderr else ""
        raise RuntimeError(
            "global_linear command failed.\n"
            f"Return code: {completed.returncode}\n"
            f"STDOUT (tail):\n{stdout_tail}\n"
            f"STDERR (tail):\n{stderr_tail}"
        )

    return elapsed


def warmup_runs(root: Path, jar_path: Path, matrix_path: Path) -> None:
    """Run a couple of short alignments to warm up JVM/class loading/JIT."""
    print("Running warmup (ignored in analysis)...")
    warmup_threads = [1, max(THREAD_COUNTS)]
    rng = random.Random(2026)
    for run_idx in range(WARMUP_RUNS):
        threads = warmup_threads[run_idx % len(warmup_threads)]
        seq1 = random_dna(WARMUP_LENGTH, rng)
        seq2 = random_dna(WARMUP_LENGTH, rng)
        _ = run_alignment(root, jar_path, matrix_path, seq1, seq2, threads)


def write_csv(csv_path: Path, rows: List[Dict[str, float]]) -> None:
    csv_path.parent.mkdir(parents=True, exist_ok=True)
    with csv_path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=CSV_COLUMNS)
        writer.writeheader()
        for row in rows:
            writer.writerow(
                {
                    "length": int(row["length"]),
                    "threads": int(row["threads"]),
                    "median_seconds": f"{row['median_seconds']:.6f}",
                    "speedup": f"{row['speedup']:.6f}",
                }
            )


def make_plots(rows: List[Dict[str, float]], speedup_png: Path, runtime_png: Path) -> bool:
    """Generate speedup/runtime plots. Returns False when matplotlib is unavailable."""
    try:
        import matplotlib.pyplot as plt
    except Exception as exc:  # noqa: BLE001
        print(f"matplotlib is unavailable ({exc}); skipping plot generation.")
        return False

    speedup_png.parent.mkdir(parents=True, exist_ok=True)

    # Group points by sequence length.
    grouped: Dict[int, List[Dict[str, float]]] = {}
    for row in rows:
        grouped.setdefault(int(row["length"]), []).append(row)
    for length_rows in grouped.values():
        length_rows.sort(key=lambda r: int(r["threads"]))

    plt.style.use("seaborn-v0_8-whitegrid")

    # Plot 1: speedup curves with ideal linear reference.
    fig, ax = plt.subplots(figsize=(8.5, 5.5))
    for length in sorted(grouped):
        x = [int(r["threads"]) for r in grouped[length]]
        y = [float(r["speedup"]) for r in grouped[length]]
        ax.plot(x, y, marker="o", linewidth=2, label=f"Length {length}")

    ideal_x = THREAD_COUNTS
    ideal_y = THREAD_COUNTS
    ax.plot(ideal_x, ideal_y, linestyle="--", color="black", linewidth=1.5, label="Ideal linear speedup")
    ax.set_title("Wavefront Parallelism Speedup (Global Linear Alignment)")
    ax.set_xlabel("Threads")
    ax.set_ylabel("Speedup")
    ax.set_xticks(THREAD_COUNTS)
    ax.legend()
    ax.grid(True, linestyle="--", alpha=0.6)
    fig.tight_layout()
    fig.savefig(speedup_png, dpi=200)
    plt.close(fig)

    # Plot 2: runtime curves.
    fig, ax = plt.subplots(figsize=(8.5, 5.5))
    for length in sorted(grouped):
        x = [int(r["threads"]) for r in grouped[length]]
        y = [float(r["median_seconds"]) for r in grouped[length]]
        ax.plot(x, y, marker="o", linewidth=2, label=f"Length {length}")

    ax.set_title("Runtime vs Thread Count (Global Linear Alignment)")
    ax.set_xlabel("Threads")
    ax.set_ylabel("Runtime (seconds)")
    ax.set_xticks(THREAD_COUNTS)
    ax.legend()
    ax.grid(True, linestyle="--", alpha=0.6)
    fig.tight_layout()
    fig.savefig(runtime_png, dpi=200)
    plt.close(fig)
    return True


def print_summary(rows: List[Dict[str, float]]) -> None:
    print("\nBenchmark summary (median of 3 runs):")
    print(f"{'length':>8} {'threads':>8} {'median_seconds':>16} {'speedup':>10}")
    for row in sorted(rows, key=lambda r: (int(r["length"]), int(r["threads"]))):
        print(
            f"{int(row['length']):>8} "
            f"{int(row['threads']):>8} "
            f"{float(row['median_seconds']):>16.6f} "
            f"{float(row['speedup']):>10.3f}"
        )


def main() -> None:
    root = repo_root()
    matrix_path = root / "data" / "matrices" / "dna_example.txt"
    if not matrix_path.exists():
        raise FileNotFoundError(f"Missing matrix file: {matrix_path}")

    jar_path = build_cli_jar(root)
    print(f"Using CLI jar: {jar_path}")

    warmup_runs(root, jar_path, matrix_path)

    measurements: List[Dict[str, float]] = []
    for length in SEQUENCE_LENGTHS:
        for threads in THREAD_COUNTS:
            print(f"Benchmarking length={length}, threads={threads} ...")
            # Fixed seed per (length, threads) makes runs reproducible.
            rng = random.Random(length * 1000 + threads)
            seq1 = random_dna(length, rng)
            seq2 = random_dna(length, rng)

            runtimes = []
            for _ in range(REPEATS):
                runtimes.append(run_alignment(root, jar_path, matrix_path, seq1, seq2, threads))
            median_seconds = statistics.median(runtimes)
            measurements.append(
                {
                    "length": float(length),
                    "threads": float(threads),
                    "median_seconds": float(median_seconds),
                }
            )

    baseline_by_length: Dict[int, float] = {}
    for row in measurements:
        if int(row["threads"]) == 1:
            baseline_by_length[int(row["length"])] = float(row["median_seconds"])

    rows_with_speedup: List[Dict[str, float]] = []
    for row in measurements:
        length = int(row["length"])
        baseline = baseline_by_length[length]
        speedup = baseline / float(row["median_seconds"])
        row_with_speedup = dict(row)
        row_with_speedup["speedup"] = speedup
        rows_with_speedup.append(row_with_speedup)

    csv_path = root / "results" / "parallelism_analysis.csv"
    speedup_png = root / "results" / "parallelism_speedup.png"
    runtime_png = root / "results" / "parallelism_runtime.png"

    write_csv(csv_path, rows_with_speedup)
    plotted = make_plots(rows_with_speedup, speedup_png, runtime_png)
    print_summary(rows_with_speedup)

    print(f"\nCSV written to: {csv_path}")
    if plotted:
        print(f"Speedup plot written to: {speedup_png}")
        print(f"Runtime plot written to: {runtime_png}")
    else:
        print("Plots were not generated (matplotlib unavailable).")


if __name__ == "__main__":
    main()
