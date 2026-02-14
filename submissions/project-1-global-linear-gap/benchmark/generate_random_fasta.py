#!/usr/bin/env python3
"""Generate two random DNA FASTA files."""

import argparse
import random
from pathlib import Path


DNA = "ACGT"


def random_dna(length: int) -> str:
    # Uniform random DNA generator used for quick synthetic test data.
    return "".join(random.choice(DNA) for _ in range(length))


def write_fasta(path: Path, header: str, sequence: str) -> None:
    with path.open("w", encoding="utf-8") as handle:
        handle.write(f">{header}\n")
        handle.write(sequence + "\n")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate two random DNA FASTA files."
    )
    parser.add_argument("length", type=int, help="Sequence length (positive integer)")
    parser.add_argument("output1", help="Output FASTA path for sequence 1")
    parser.add_argument("output2", help="Output FASTA path for sequence 2")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.length <= 0:
        raise ValueError("length must be positive")

    out1 = Path(args.output1)
    out2 = Path(args.output2)
    # Create parent folders automatically so users can pass new output paths.
    out1.parent.mkdir(parents=True, exist_ok=True)
    out2.parent.mkdir(parents=True, exist_ok=True)

    write_fasta(out1, "seq1", random_dna(args.length))
    write_fasta(out2, "seq2", random_dna(args.length))
    print(f"Wrote random FASTA files: {out1} and {out2} (length={args.length})")


if __name__ == "__main__":
    main()
