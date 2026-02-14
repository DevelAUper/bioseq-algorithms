#!/usr/bin/env python3
"""Benchmark runtime of Java global linear alignment CLI."""

import csv
import random
import statistics
import subprocess
import sys
import time
from pathlib import Path


LENGTHS = [500, 1000, 1500, 2000]
WARMUP_LENGTH = 200
WARMUP_RUNS = 2
REPETITIONS = 3
DNA = "ACGT"


def random_dna(length: int) -> str:
    # Random DNA for synthetic benchmark inputs.
    return "".join(random.choice(DNA) for _ in range(length))


def run_global_linear(work_dir: Path, jar_rel: str, matrix_path: Path, seq1: str, seq2: str, label: str) -> float:
    # Call the same CLI command used by the submission scripts.
    cmd = [
        "java",
        "-jar",
        jar_rel,
        "global_linear",
        "--seq1",
        seq1,
        "--seq2",
        seq2,
        "--matrix",
        str(matrix_path),
        "--gap",
        "2",
    ]
    start = time.perf_counter()
    result = subprocess.run(cmd, cwd=work_dir, capture_output=True, text=True)
    elapsed = time.perf_counter() - start
    if result.returncode != 0:
        # Print stderr to keep failures actionable for tutors/students.
        print(f"Command failed for {label}:", file=sys.stderr)
        print(result.stderr.strip(), file=sys.stderr)
        raise SystemExit(result.returncode)
    return elapsed


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    work_dir = script_dir / "tmp_work"
    work_dir.mkdir(exist_ok=True)

    jar_rel = "../../run/bioseq-cli.jar"
    matrix_path = (script_dir / "../run/examples/dna_matrix.txt").resolve()
    csv_path = (script_dir / "../results/timings_project1.csv").resolve()
    png_path = (script_dir / "../results/timings_project1_plot.png").resolve()
    csv_path.parent.mkdir(parents=True, exist_ok=True)

    # Warmup runs reduce JVM/JIT first-run noise in measured points.
    warm_seq1 = random_dna(WARMUP_LENGTH)
    warm_seq2 = random_dna(WARMUP_LENGTH)
    for run_idx in range(WARMUP_RUNS):
        run_global_linear(
            work_dir,
            jar_rel,
            matrix_path,
            warm_seq1,
            warm_seq2,
            f"warmup run {run_idx + 1}",
        )

    rows = []
    for length in LENGTHS:
        seq1 = random_dna(length)
        seq2 = random_dna(length)
        # Repeat and summarize with median + min for robust reporting.
        runtimes = [
            run_global_linear(
                work_dir,
                jar_rel,
                matrix_path,
                seq1,
                seq2,
                f"length {length} run {idx + 1}",
            )
            for idx in range(REPETITIONS)
        ]
        cells = length * length
        median_runtime = statistics.median(runtimes)
        min_runtime = min(runtimes)
        rows.append((length, cells, median_runtime, min_runtime))

    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["length", "cells", "runtime_seconds_median", "runtime_seconds_min"])
        writer.writerows(rows)

    try:
        import matplotlib.pyplot as plt

        # Plot runtime against DP work size (n*m cells).
        x = [row[1] for row in rows]
        y = [row[2] for row in rows]
        plt.figure(figsize=(7, 4))
        plt.plot(x, y, marker="o")
        plt.xlabel("DP cells (n*m)")
        plt.ylabel("Runtime (seconds)")
        plt.title("Runtime of Global Linear Alignment (Java)")
        plt.grid(True, linestyle="--", alpha=0.4)
        plt.tight_layout()
        plt.savefig(png_path, dpi=150)
        plt.close()
    except ImportError:
        # CSV remains the primary artifact; plotting is optional.
        print("Matplotlib not installed. CSV was generated. You can plot manually.")

    print("Benchmark complete.")
    print(f"CSV: {csv_path}")
    if png_path.exists():
        print(f"Plot: {png_path}")


if __name__ == "__main__":
    main()
