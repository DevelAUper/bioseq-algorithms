# Project 1 Report: Global Alignment with Linear Gap Cost

**Authors:** `<Main student name>` and Eduardo  
**Date:** 17 Feb 2026

## Introduction
This project implements two tools for pairwise DNA/protein sequence analysis using a **global alignment** model with a **linear gap penalty**:

- `global_linear`: returns one optimal global alignment and its total cost.
- `global_count`: returns the same optimal cost and the number of optimal alignments.

The project uses **cost minimization** (distance-like scoring), so lower values are better.  
Inputs are two sequences (usually from FASTA), a substitution/cost matrix, and a gap value (`gap`).

The submission is packaged for two audiences:
- Tutors who want one-click execution (`run/` folder).
- Students/graders who want the exact source snapshot (`code/` folder and `code.zip`).

## Methods
### Core alignment model (minimize cost)
For two sequences `s1` and `s2`, dynamic programming (DP) is used.  
Each DP cell `(i, j)` represents the best (minimum) alignment cost for prefixes `s1[0..i)` and `s2[0..j)`.

The recurrence is:

\[
dp[i][j] = \min
\begin{cases}
dp[i-1][j-1] + \text{matrix.cost}(s1[i-1], s2[j-1]) \\
dp[i-1][j] + \text{gap} \\
dp[i][j-1] + \text{gap}
\end{cases}
\]

Plain-language meaning of the three options:
- **Diagonal:** align one symbol from each sequence (match or substitution).
- **Up:** align a symbol in `s1` to a gap (`-`) in `s2`.
- **Left:** align a symbol in `s2` to a gap (`-`) in `s1`.

### Backtracking in `global_linear`
After the DP cost table is filled, stored move directions are followed from the last cell back to `(0,0)`.  
This reconstructs one optimal alignment by appending characters/gaps and reversing at the end.

### Counting in `global_count`
A second DP table stores counts of optimal paths.  
At each cell, we first find the optimal cost, then **add counts from all predecessor moves that reach that same optimal cost**.  
This explicitly handles ties: if diagonal and left are both optimal, both counts are included.

### File formats
#### FASTA
Expected structure:
- Header line starts with `>`
- Sequence lines follow
- Multi-line sequence content is concatenated

Example (3 lines):
```text
>seq1
ACGTTGCA
```

#### Matrix file (Phylip-like)
Format:
1. First non-empty line: alphabet size `n`
2. Next `n` lines: one symbol, then `n` integer costs

Small DNA example (`A/C/G/T`, values 0/5/2):
```text
4
A 0 2 5 2
C 2 0 2 5
G 5 2 0 2
T 2 5 2 0
```

If an input sequence contains unknown symbols (not in the matrix alphabet), execution stops with an error message.

### Design choices (module structure)
The implementation is split into three modules to make review and grading straightforward:
- `core`: sequence objects, FASTA reading, matrix/alphabet parsing, and gap-cost utilities.
- `pairwise-alignment`: global DP algorithms for optimal cost/alignment and optimal-alignment counting.
- `cli`: command-line parsing and output formatting, with no algorithm logic.

This keeps biological/scoring logic separate from I/O and shell interaction, improving reproducibility and debugging.

## Tests
The source snapshot contains JUnit tests covering key correctness points:

- `OptimalAlignmentCounterTest`  
  Validates counting behavior for both tie cases (multiple optimal alignments) and unique-optimum cases.

- `ScoreMatrixTest`  
  Verifies matrix-file parsing and selected pairwise cost lookups.

- `FastaReaderTest`  
  Checks FASTA parsing behavior (including lowercase and multiline sequence input).

- `PairwiseSmokeTest`  
  Confirms the pairwise module test pipeline executes successfully in the build.

- `TextWrapTest`  
  Verifies CLI output wrapping behavior for readable traceback output formatting.

Together, these tests validate input parsing, cost lookup, DP-based outputs, and user-facing formatting.

## Experiments
Runtime artifacts are included in:
- `submissions/project-1-global-linear-gap/results/timings_project1.csv`
- `submissions/project-1-global-linear-gap/results/timings_project1_plot.png`

### Protocol
- Warmup: 2 runs on length 200 (ignored in reported results).
- Measured lengths: 500, 1000, 1500, 2000.
- Repetitions per length: 3.
- Reported metric: median runtime (plus minimum runtime).
- Plot uses **DP cells (`n*m`)** on x-axis and runtime (seconds) on y-axis.

### Exact measured values (from CSV)

| length | cells | runtime_seconds_median | runtime_seconds_min |
|---:|---:|---:|---:|
| 500 | 250000 | 0.324220399999831 | 0.3041327999999339 |
| 1000 | 1000000 | 0.31740630000058445 | 0.2933006999992358 |
| 1500 | 2250000 | 0.3608930999998847 | 0.34635960000014165 |
| 2000 | 4000000 | 0.38204450000012 | 0.3752588999996078 |

### Interpretation
- The overall trend rises with increasing DP cells, which is consistent with expected \(O(nm)\) behavior.
- Small fluctuations (for example, 500 vs 1000) are normal in JVM workflows due to startup/JIT and machine noise.
- Warmup plus median-of-3 reporting reduces first-run bias and makes the trend more stable.

## How to run
### A. One-click run (recommended for tutor)
Use the folder:
`submissions/project-1-global-linear-gap/run/`

- **Windows:** double-click:
  - `RUN_GLOBAL_LINEAR.bat`
  - `RUN_GLOBAL_COUNT.bat`
- **Mac/Linux:** run:
  - `./RUN_GLOBAL_LINEAR.sh`
  - `./RUN_GLOBAL_COUNT.sh`

Output files appear in:
`submissions/project-1-global-linear-gap/run/output/`

### B. Command-line run (reproducible)
From `submissions/project-1-global-linear-gap/`:

`global_linear` with traceback and custom output:
```bash
java -jar run/bioseq-cli.jar global_linear --fasta1 <file> --fasta2 <file> --matrix <file> --gap 5 --traceback --out <file>
```

`global_count`:
```bash
java -jar run/bioseq-cli.jar global_count --fasta1 <file> --fasta2 <file> --matrix <file> --gap 5 --out <file>
```

Example placeholders:
- `<file>` for FASTA inputs can be paths such as `run/examples/seq1.fa` and `run/examples/seq2.fa`
- matrix path can be `run/examples/dna_matrix.txt`

### Build from bundled source snapshot
From `submissions/project-1-global-linear-gap/code/`:

Windows:
```bat
mvnw.cmd -q test
mvnw.cmd -q -pl cli -am package
```

Mac/Linux:
```bash
./mvnw -q test
./mvnw -q -pl cli -am package
```

The runnable jar in `run/` was built from this source snapshot.

## Checklist for submission
- `run/` contains runnable scripts and `bioseq-cli.jar`.
- `run/output/` contains example output files after execution.
- `code/` contains source snapshot (`core`, `pairwise-alignment`, `cli`).
- `results/` contains `timings_project1.csv` and `timings_project1_plot.png`.
- `project1_eval_answers.txt` is filled with Q1/Q2/Q3 answers.
- `report.md` is present and ready for export to PDF if needed.
