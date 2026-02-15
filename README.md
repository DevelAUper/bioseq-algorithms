# BioSeq Algorithms
A modular Java toolkit for biological sequence analysis — pairwise alignment, multiple alignment, phylogenetic tree reconstruction, and more.

Badges: `[Java 21]` `[Maven]` `[License: MIT]`

## Overview
BioSeq Algorithms is a multi-module Java project for core methods in computational biology. It includes exact dynamic-programming aligners, counting methods, heuristic multiple alignment, phylogeny builders, and both CLI and GUI interfaces. The codebase is designed for reproducible experiments and clear separation of architecture by module.

## Features
- Global pairwise alignment with linear gap cost (Needleman-Wunsch)
- Global pairwise alignment with affine gap cost (Gotoh-style three-layer DP)
- Optimal alignment counting (number of co-optimal alignments)
- Wavefront parallel DP (anti-diagonal CPU parallelism)
- Multiple sequence alignment (center-star heuristic with sum-of-pairs scoring)
- Phylogenetic tree reconstruction (UPGMA and Neighbor-Joining)
- Profile matrix computation
- Swing GUI for interactive alignment
- CLI with picocli (`global_linear`, `global_count`, `global_affine`)
- FASTA I/O and Phylip-like score matrix parsing

## Project Structure
```text
java/
  core/               - Sequence, FASTA I/O, scoring matrices, gap costs
  pairwise-alignment/ - Linear and affine global aligners, counting, wavefront parallel
  multiple-alignment/ - Center-star heuristic, profile matrices, sum-of-pairs
  phylogeny/          - UPGMA, Neighbor-Joining, distance matrices, Newick trees
  cli/                - Command-line interface
  gui/                - Swing graphical interface
```

## Quick Start
Prerequisites:
- Java 21+
- Maven (or use the included Maven Wrapper)

Build (Unix/macOS):
```bash
cd java
./mvnw -q package -DskipTests
```

Build (Windows PowerShell):
```powershell
cd java
.\mvnw.cmd -q package -DskipTests
```

Run CLI:
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --help
```

Run GUI:
```bash
java -jar java/gui/target/bioseq-gui.jar
```

Run tests (Unix/macOS):
```bash
cd java
./mvnw test
```

Run tests (Windows PowerShell):
```powershell
cd java
.\mvnw.cmd test
```

## CLI Usage
`global_linear` (cost only):
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

`global_linear` with traceback and threads:
```bash
java -jar java/cli/target/bioseq-cli.jar global_linear --fasta1 data/fasta/a.fa --fasta2 data/fasta/b.fa --matrix data/matrices/dna_example.txt --gap 2 --threads 4 --traceback --wrap 60
```

`global_count`:
```bash
java -jar java/cli/target/bioseq-cli.jar global_count --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --gap 2
```

`global_affine`:
```bash
java -jar java/cli/target/bioseq-cli.jar global_affine --seq1 ACGT --seq2 AGT --matrix data/matrices/dna_example.txt --alpha 10 --beta 3 --threads 4
```

## GUI
The Swing GUI supports interactive global alignment with linear or affine gap models. You can provide sequences directly or load them from FASTA files, choose a score matrix, configure gap penalties and thread count, and view cost/count/traceback output in one window.

## Input Formats
FASTA (first record is used per file):
```text
>seq1
ACGTACGT
```

Phylip-like score matrix format:
```text
4
A 0 2 5 2
C 2 0 2 5
G 5 2 0 2
T 2 5 2 0
```

## Testing
The project includes 43+ JUnit 5 tests across core, pairwise alignment, multiple alignment, phylogeny, CLI, and parallel implementations.

## Documentation
Documentation lives in `docs/`. Start with `docs/index.md`.

## Benchmarks
Runtime and parallelism outputs are stored under `results/`, including CSV and plots for wavefront speedup analysis and runtime trends.

## Course Context
This project was developed for the course **Algorithms in Bioinformatics** at Aarhus University. It covers a five-project progression from pairwise alignment foundations to multiple alignment, phylogeny, and performance analysis.

## License
MIT

## Authors
AA  
Eduardo Iglesias
