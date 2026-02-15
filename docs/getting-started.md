# Getting Started

This page shows how to build, test, and run the current BioSeq toolkit from a clean checkout.

## Prerequisites
- Java 21 or newer
- Maven 3.9+ or the included Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Build
Unix/macOS:
```bash
cd java
./mvnw -q package -DskipTests
```

Windows PowerShell:
```powershell
cd java
.\mvnw.cmd -q package -DskipTests
```

## Run Tests
Unix/macOS:
```bash
cd java
./mvnw test
```

Windows PowerShell:
```powershell
cd java
.\mvnw.cmd test
```

## Run the CLI
Show help:
```bash
java -jar java/cli/target/bioseq-cli.jar --help
```

Linear global alignment:
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

Affine global alignment:
```bash
java -jar java/cli/target/bioseq-cli.jar global_affine --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --alpha 10 --beta 3
```

## Run the GUI
```bash
java -jar java/gui/target/bioseq-gui.jar
```

## Module Overview
- `java/core`: shared data structures, FASTA I/O, alphabet/matrix parsing, gap models.
- `java/pairwise-alignment`: global linear/affine aligners, counting, and wavefront parallel variants.
- `java/multiple-alignment`: center-star heuristic, profile matrix, sum-of-pairs scoring.
- `java/phylogeny`: distance matrix utilities and tree builders (UPGMA, Neighbor-Joining).
- `java/cli`: picocli command-line tools.
- `java/gui`: Swing desktop application.

## Next Reading
- CLI details: `docs/cli.md`
- Input formats: `docs/fasta-and-io.md`, `docs/scoring-matrices.md`
- Algorithms: `docs/pairwise-global-linear.md`, `docs/counting-optimal-alignments.md`
