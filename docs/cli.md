# CLI Contract (Project 1)

## Overview
This project defines two command-line programs for min-cost global alignment with a linear gap penalty:
- `global_linear`: computes one optimal alignment and its total cost
- `global_count`: counts the number of optimal alignments (same scoring model)

## Inputs
You can provide sequences directly or via FASTA files:
- Direct sequences: `--seq1 <string>` and `--seq2 <string>`
- FASTA inputs: `--fasta1 <path>` and `--fasta2 <path>`
  - Only the first FASTA record from each file is used.

Required options:
- `--matrix <path>`: distance-based scoring matrix file
- `--gap <int>`: linear gap penalty (non-negative integer)

Optional options:
- `--traceback`: include alignment strings in output (when applicable)
- `--wrap <int>`: wrap alignment lines to this width
- `--out <path>`: write output to a file instead of stdout
- `--threads <int>`: future extension for parallelism (currently may be ignored)

## Output formats
`global_linear` (min-cost alignment)
- Always prints total cost as an integer.
- If `--traceback` is set, also prints aligned sequences on separate lines.

`global_count` (number of optimal alignments)
- Prints total cost and the count of optimal alignments.

## Examples
Windows PowerShell:
```powershell
# direct sequences
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACTG --seq2 AC-G --matrix data/matrices/example.txt --gap 2 --traceback

# FASTA inputs
java -jar java/cli/target/bioseq-cli.jar global_count --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2
```

bash:
```bash
# direct sequences
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACTG --seq2 ACG --matrix data/matrices/example.txt --gap 2 --traceback

# FASTA inputs
java -jar java/cli/target/bioseq-cli.jar global_count --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/example.txt --gap 2
```
