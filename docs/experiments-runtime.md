# Runtime Experiments

This page describes the baseline runtime experiment setup used for pairwise global alignment performance tracking.

## Goal
Measure how runtime scales with sequence length for the current Java implementations under controlled settings.

## Benchmark Setup
- Algorithm: `global_linear` (sequential baseline, `--threads 1`)
- Alphabet: DNA (`A/C/G/T`)
- Matrix: `data/matrices/dna_example.txt`
- Gap penalty: `--gap 2`
- Sequence lengths: `1000, 2000, 3000, 4000, 5000`
- Repetitions per length: 3 (report median runtime)

This baseline setup is separate from the wavefront parallelism study documented in `docs/parallelism-analysis.md`.

## Suggested Execution Flow
1. Build CLI jar:
```bash
cd java
./mvnw -q -pl cli -am package -DskipTests
```

Windows PowerShell equivalent:
```powershell
cd java
.\mvnw.cmd -q -pl cli -am package -DskipTests
```

2. Run your runtime script/commands and save outputs to:
- `results/timings/` for CSV/raw logs
- `results/plots/` for generated figures

## Metrics to Record
- Sequence length
- Runtime in seconds (or milliseconds)
- Repeat index
- Median runtime per length
- Environment metadata (CPU, OS, Java version)

## Reporting Guidance
- Use median rather than mean to reduce outlier impact.
- Clearly separate algorithm time from one-time JVM startup costs.
- Keep matrix, gap model, and input generator constant across runs.

## Current Repository Outputs
- Parallelism CSV/plots are in `results/parallelism_analysis.csv`, `results/parallelism_speedup.png`, and `results/parallelism_runtime.png`.
- General runtime placeholders are documented in `results/timings/README.md` and `results/plots/README.md`.
