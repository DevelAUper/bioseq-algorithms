# Project 1: Global Alignment (Linear Gap)

This folder contains the submission bundle for the global alignment assignment.

## What the programs do
- `global_linear` computes one optimal global alignment and its total cost using a linear gap penalty.
- `global_count` computes the total cost and the number of optimal alignments under the same scoring model.

## How to run (once jars exist)
Examples use the CLI fat jar that will be built in `java/cli/target/`.

Global alignment (with traceback):
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2 --traceback
```

Count optimal alignments:
```bash
java -jar java/cli/target/bioseq-cli.jar global_count --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2
```

## Inputs
- FASTA files: `--fasta1` and `--fasta2` (first record in each file is used).
- Scoring matrix file: `--matrix` (distance-based).
- Gap cost: `--gap` (linear penalty).

## Outputs
- `global_linear`: prints the total cost; with `--traceback`, also prints aligned sequences.
- `global_count`: prints the total cost and the number of optimal alignments.

## Submission files
- `report.pdf` will be placed in this folder (see `report_PLACEHOLDER.txt`).
- `code.zip` will be placed in this folder (see `code_zip_PLACEHOLDER.txt`).
