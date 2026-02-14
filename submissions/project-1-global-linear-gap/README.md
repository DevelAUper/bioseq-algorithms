# Project 1: Global Alignment (Linear Gap)

This folder is prepared so anyone can run Project 1 without coding.

## Quick run (Windows)
1. Open `submissions/project-1-global-linear-gap/run/`.
2. Double-click `copy_jar_here.bat` (copies `bioseq-cli.jar` from the Maven build output).
3. Double-click `RUN_GLOBAL_LINEAR.bat`.
4. Results are written to `run/output/`.
5. You can also double-click `RUN_GLOBAL_COUNT.bat`.

## Quick run (Mac/Linux)
1. Open a terminal in `submissions/project-1-global-linear-gap/run/`.
2. Run:
```bash
chmod +x copy_jar_here.sh RUN_GLOBAL_LINEAR.sh RUN_GLOBAL_COUNT.sh
./copy_jar_here.sh
./RUN_GLOBAL_LINEAR.sh
```
3. Results are written to `run/output/`.
4. You can also run `./RUN_GLOBAL_COUNT.sh`.

## Run with your own FASTA and matrix files
From inside `submissions/project-1-global-linear-gap/run/`:

Global linear alignment with traceback:
```bash
java -jar bioseq-cli.jar global_linear \
  --fasta1 /path/to/seq1.fa \
  --fasta2 /path/to/seq2.fa \
  --matrix /path/to/matrix.txt \
  --gap 5 --traceback --wrap 60 \
  --out output/my_linear_result.txt
```

Count optimal alignments:
```bash
java -jar bioseq-cli.jar global_count \
  --fasta1 /path/to/seq1.fa \
  --fasta2 /path/to/seq2.fa \
  --matrix /path/to/matrix.txt \
  --gap 5 \
  --out output/my_count_result.txt
```

Notes:
- You can also use `--seq1` and `--seq2` instead of FASTA files.
- The matrix is a distance/cost matrix (lower is better; total cost is minimized).

## What output files are produced
- `run/output/global_linear_example.txt`: default output from `RUN_GLOBAL_LINEAR`.
- `run/output/global_count_example.txt`: default output from `RUN_GLOBAL_COUNT`.
- Optional: if you run `build_windows_exe.bat` and `jpackage` is available, an app-image is created in `run/dist/`.

## Troubleshooting
- `java not found`:
  Install Java (JRE/JDK 21+ recommended), then reopen terminal/Explorer and run again.
- `permission denied` on `.sh`:
  Run exactly:
```bash
chmod +x RUN_GLOBAL_LINEAR.sh RUN_GLOBAL_COUNT.sh copy_jar_here.sh
```

## Submission placeholders
- `report_PLACEHOLDER.txt`: keep until `report.pdf` is ready.
- `code_zip_PLACEHOLDER.txt`: keep until `code.zip` is ready.

## How the jar was built and tested
From the repo root:
```bash
cd java
./mvnw -q test
./mvnw -q -pl cli -am package
```
