# Getting Started

This page explains how to build and run the Java modules, with emphasis on reproducibility and minimal prerequisites. It will also describe where to find submission bundles and how they relate to the code.

## Outline
- Prerequisites (Java, Maven)
- Build and test
- Running the CLI
- Where results and submissions live

## Maven Wrapper
If Maven is not installed locally, you can generate the Maven Wrapper from the `java/` folder:
```bash
cd java
mvn -N wrapper:wrapper
```
This creates `mvnw`, `mvnw.cmd`, and the `.mvn/wrapper/` files needed to run:
```bash
cd java
.\mvnw.cmd -q test
```

## Running the tools
Java 21 is required for this project. Use the Maven Wrapper in `java/` to build:
```powershell
cd java
.\mvnw.cmd -q test
.\mvnw.cmd -q package
```
Runnable jars will be produced later in the `cli` module. For the current CLI contract and options, see `docs/cli.md`.
