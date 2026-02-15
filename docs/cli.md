# CLI Reference

The CLI entry point is:
```text
java -jar java/cli/target/bioseq-cli.jar
```

Available subcommands:
- `global_linear`
- `global_count`
- `global_affine`

## Common Input Modes
All alignment commands use exactly one of these modes:
- Direct input: `--seq1 <residues> --seq2 <residues>`
- FASTA files: `--fasta1 <path> --fasta2 <path>`

The matrix path is always required:
- `--matrix <path>`

## global_linear
Compute global alignment with a linear gap penalty.

Required:
- input mode (`--seq1/--seq2` or `--fasta1/--fasta2`)
- `--matrix <path>`
- `--gap <non-negative int>`

Optional:
- `--traceback` include aligned strings in output
- `--threads <positive int>` use wavefront parallel aligner when `> 1`
- `--wrap <positive int>` line wrap width for traceback output (default `60`)
- `--out <path>` write output to file

Output:
- Without `--traceback`: integer optimal cost
- With `--traceback`: cost and aligned sequences in FASTA-like format

Example (cost only):
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

Example (traceback, FASTA input, threads):
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/dna_example.txt --gap 2 --threads 4 --traceback --wrap 60
```

## global_count
Compute optimal linear-gap cost and number of co-optimal alignments.

Required:
- input mode (`--seq1/--seq2` or `--fasta1/--fasta2`)
- `--matrix <path>`
- `--gap <non-negative int>`

Optional:
- `--threads <positive int>` accepted as shared option
- `--out <path>` write output to file
- `--wrap <positive int>` accepted as shared option

Output:
- `cost: <int>`
- `count: <BigInteger>`

Example:
```bash
java -jar java/cli/target/bioseq-cli.jar global_count --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

## global_affine
Compute global alignment with affine gap penalties and count co-optimal alignments.

Required:
- input mode (`--seq1/--seq2` or `--fasta1/--fasta2`)
- `--matrix <path>`
- `--alpha <non-negative int>` gap opening penalty
- `--beta <non-negative int>` gap extension penalty

Optional:
- `--traceback` include aligned strings
- `--threads <positive int>` use wavefront affine aligner when `> 1`
- `--wrap <positive int>` line wrap width for traceback output (default `60`)
- `--out <path>` write output to file

Output:
- Without `--traceback`: cost and count
- With `--traceback`: cost, count, and aligned sequences

Example:
```bash
java -jar java/cli/target/bioseq-cli.jar global_affine --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --alpha 10 --beta 3 --threads 4 --traceback
```

## Help
Root help:
```bash
java -jar java/cli/target/bioseq-cli.jar --help
```

Subcommand help:
```bash
java -jar java/cli/target/bioseq-cli.jar global_affine --help
```
