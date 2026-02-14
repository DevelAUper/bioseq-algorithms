# Project 1 Source Bundle

This folder is a self-contained source snapshot used for Project 1 submission.

## Modules
- `core`: sequence model, FASTA parsing, scoring matrix parsing, alphabet validation, gap-cost utilities.
- `pairwise-alignment`: global linear-gap alignment and optimal-alignment counting implementations.
- `cli`: command-line entrypoints and output helpers.

## Build
Run from inside `submissions/project-1-global-linear-gap/code/`.

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

The runnable jar in `../run/` was built from this source tree.
