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
OUT_FILE="$OUT_DIR/global_linear_example.txt"

if ! command -v java >/dev/null 2>&1; then
  echo "Java is not installed or not on PATH."
  echo "Install Java and try again."
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "Jar not found. Run ./copy_jar_here.sh or build the project."
  exit 1
fi

mkdir -p "$OUT_DIR"

java -jar "$JAR" global_linear \
  --fasta1 "./examples/seq1.fa" \
  --fasta2 "./examples/seq2.fa" \
  --matrix "./examples/dna_matrix.txt" \
  --gap 2 --traceback --wrap 60 \
  --out "$OUT_FILE"

echo "Done. Wrote: $OUT_FILE"
echo "Hint: custom input example:"
echo "  java -jar bioseq-cli.jar global_linear --fasta1 your1.fa --fasta2 your2.fa --matrix your_matrix.txt --gap 5 --traceback --out output/your_result.txt"
