# Bioinformatics Algorithms (Java)

Java implementations of core algorithms from **Algorithms in Bioinformatics**, organized as a clean multi-module Maven portfolio. The focus is on correctness, clarity, and reproducible experiments, with submissions packaged separately for course evaluation.

## Topics covered
- Pairwise global alignment (linear gap penalties)
- Counting optimal alignments
- Multiple sequence alignment (placeholders)
- Phylogenetic tree reconstruction (placeholders)
- Scoring matrices, FASTA I/O, and runtime analysis

## Quickstart
```bash
cd java
mvn test
```

## Submissions
Submission bundles live in `submissions/`, one folder per project. Each contains a brief instructor-facing README plus placeholders for the report and code package.

## Notes on scoring
Dynamic programming is implemented as **min-cost alignment**. Score matrices are treated as **distances**, and gap costs are **penalties** (lower is better).
