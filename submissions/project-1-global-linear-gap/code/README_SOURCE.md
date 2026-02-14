# Project 1 Source Bundle

This `code/` folder is a self-contained source snapshot for Project 1.

## Module structure
- `core`: shared data structures and utilities.
- `pairwise-alignment`: global alignment implementations.
- `cli`: command-line interface.

## How to build
From inside `submissions/project-1-global-linear-gap/code/`:

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

The runnable jar in `../run/` was built from this source.
