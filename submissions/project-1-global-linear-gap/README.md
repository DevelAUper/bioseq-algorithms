# Project 1: Global Alignment (Linear Gap)

This folder is a submission-ready package for Project 1. It is organized so tutors can run it quickly and students can trace exactly which source code was used.

## Purpose
- Run `global_linear` to get one optimal global alignment and its minimum cost.
- Run `global_count` to get the minimum cost and number of optimal alignments.
- Provide report, evaluation answers, runnable bundle, and source snapshot together.

## Folder layout
- `run/`: runnable scripts, jar, examples, and generated output.
- `code/`: source snapshot used to build the runnable jar.
- `report.md`: final project report.
- `project1_eval_answers.txt`: filled evaluation answers.
- `code_zip_PLACEHOLDER.txt`: replace with `code.zip` for final hand-in.

## Quick run (Windows)
1. Open `submissions/project-1-global-linear-gap/run/`.
2. Double-click `copy_jar_here.bat` (if you need to refresh `bioseq-cli.jar` from Maven output).
3. Double-click `RUN_GLOBAL_LINEAR.bat`.
4. Optional: double-click `RUN_GLOBAL_COUNT.bat`.
5. Outputs are written to `run/output/`.

## Quick run (Mac/Linux)
1. Open a terminal in `submissions/project-1-global-linear-gap/run/`.
2. Run:
```bash
chmod +x copy_jar_here.sh RUN_GLOBAL_LINEAR.sh RUN_GLOBAL_COUNT.sh
./copy_jar_here.sh
./RUN_GLOBAL_LINEAR.sh
./RUN_GLOBAL_COUNT.sh
```
3. Outputs are written to `run/output/`.

## Run with your own FASTA + matrix
From inside `submissions/project-1-global-linear-gap/run/`:

Global alignment with traceback:
```bash
java -jar bioseq-cli.jar global_linear --fasta1 /path/to/seq1.fa --fasta2 /path/to/seq2.fa --matrix /path/to/matrix.txt --gap 5 --traceback --wrap 60 --out output/my_linear_result.txt
```

Count optimal alignments:
```bash
java -jar bioseq-cli.jar global_count --fasta1 /path/to/seq1.fa --fasta2 /path/to/seq2.fa --matrix /path/to/matrix.txt --gap 5 --out output/my_count_result.txt
```

Notes:
- You can use `--seq1` and `--seq2` instead of FASTA files.
- Matrix values are treated as costs (distance-like), so lower is better.

## Output files
- `run/output/global_linear_example.txt`
- `run/output/global_count_example.txt`
- Optional: `run/dist/` if `build_windows_exe.bat` is used with `jpackage`.

## Troubleshooting
- `java not found`: install Java (JDK/JRE), reopen terminal/window, and rerun.
- `permission denied` on `.sh`:
```bash
chmod +x RUN_GLOBAL_LINEAR.sh RUN_GLOBAL_COUNT.sh copy_jar_here.sh
```

## Build and test commands (repo root)
```bash
cd java
./mvnw -q test
./mvnw -q -pl cli -am package
```

## Submission checklist
- `report.md` completed.
- `project1_eval_answers.txt` completed.
- `run/` works and writes outputs.
- `code/` matches the source used for the jar.
- `code_zip_PLACEHOLDER.txt` replaced by `code.zip` in final package.
