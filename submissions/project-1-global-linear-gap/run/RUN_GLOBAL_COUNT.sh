#!/usr/bin/env bash
set -euo pipefail
# Build jar (from repo root) if needed:
#   cd java
#   ./mvnw -q test
#   ./mvnw -q -pl cli -am package

HERE="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$HERE"
JAR="./bioseq-cli.jar"
OUT_DIR="./output"
OUT_FILE="$OUT_DIR/global_count_example.txt"

# Guard: Java must be on PATH.
if ! command -v java >/dev/null 2>&1; then
  echo "Java is not installed or not on PATH."
  echo "Install Java and try again."
  exit 1
fi

# Guard: expected jar must exist in run/ folder.
if [ ! -f "$JAR" ]; then
  echo "Jar not found. Run ./copy_jar_here.sh or build the project."
  exit 1
fi

# Ensure output folder exists before writing result file.
mkdir -p "$OUT_DIR"

java -jar "$JAR" global_count \
  --fasta1 "./examples/seq1.fa" \
  --fasta2 "./examples/seq2.fa" \
  --matrix "./examples/dna_matrix.txt" \
  --gap 2 \
  --out "$OUT_FILE"

echo "Done. Wrote: $OUT_FILE"
echo "Hint: custom input example:"
echo "  java -jar bioseq-cli.jar global_count --fasta1 your1.fa --fasta2 your2.fa --matrix your_matrix.txt --gap 5 --out output/your_count.txt"
