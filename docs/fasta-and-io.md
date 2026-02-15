# FASTA and I/O

This project uses FASTA for sequence input and small text files for score matrices. The parser behavior is deterministic and intentionally simple for reproducible CLI and GUI runs.

## FASTA Basics
A FASTA record has:
- one header line starting with `>`
- one or more sequence lines

Example:
```text
>seq1
ACGTACGT
```

## What `FastaReader` Does
`bioseq.core.io.FastaReader` reads only the first record from each input source.

Behavior:
- Header lines are detected by leading `>`.
- Multi-line sequences are concatenated in order.
- Parsing stops at the second header.
- Blank lines are ignored.
- Residues are normalized to uppercase.
- If no header is present, ID defaults to `seq`.

This matches CLI usage where each of `--fasta1` and `--fasta2` contributes one sequence.

## Direct Sequence Input
For direct command input (`--seq1`, `--seq2`) the project creates `Sequence` values via:
- `bioseq.core.model.Sequence.of(...)`

In GUI direct-input mode, whitespace is removed and residues are uppercased before alignment.

## Error Handling
Typical input errors include:
- missing FASTA file path
- empty FASTA input
- residues not supported by the selected score matrix alphabet

These are surfaced as clear CLI/GUI validation errors rather than silent fallbacks.

## Related Files
- Parser: `java/core/src/main/java/bioseq/core/io/FastaReader.java`
- Sequence model: `java/core/src/main/java/bioseq/core/model/Sequence.java`
- CLI input resolution: `java/cli/src/main/java/bioseq/cli/BioseqCli.java`
