#!/usr/bin/env bash
set -euo pipefail
# Build jar first (from repo root):
#   cd java
#   ./mvnw -q test
#   ./mvnw -q -pl cli -am package

HERE="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
cd "$HERE"
SRC_PRIMARY="../../../../java/cli/target/bioseq-cli.jar"
SRC_FALLBACK="../../../java/cli/target/bioseq-cli.jar"
DST="./bioseq-cli.jar"
ROOT_JAR_PRIMARY="../../../../bioseq-cli.jar"
ROOT_JAR_FALLBACK="../../../bioseq-cli.jar"

if [ -f "$SRC_PRIMARY" ]; then
  cp -f "$SRC_PRIMARY" "$DST"
  echo "Success: copied $SRC_PRIMARY to $DST"
  exit 0
fi

if [ -f "$SRC_FALLBACK" ]; then
  cp -f "$SRC_FALLBACK" "$DST"
  echo "Success: copied $SRC_FALLBACK to $DST"
  exit 0
fi

if [ -f "$ROOT_JAR_PRIMARY" ]; then
  cp -f "$ROOT_JAR_PRIMARY" "$DST"
  echo "Fallback success: copied repo-root bioseq-cli.jar to $DST"
  exit 0
fi

if [ -f "$ROOT_JAR_FALLBACK" ]; then
  cp -f "$ROOT_JAR_FALLBACK" "$DST"
  echo "Fallback success: copied repo-root bioseq-cli.jar to $DST"
  exit 0
fi

echo "Failed: could not find source jar."
echo "Checked:"
echo "  $SRC_PRIMARY"
echo "  $SRC_FALLBACK"
echo "Build the CLI jar first:"
echo "  cd java"
echo "  ./mvnw -q test"
echo "  ./mvnw -q -pl cli -am package"
exit 1
