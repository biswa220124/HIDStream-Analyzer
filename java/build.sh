#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$ROOT_DIR/src"
OUT_DIR="$ROOT_DIR/out"

mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name "*.java" -print0 | xargs -0 javac -encoding UTF-8 -d "$OUT_DIR"

echo "Build complete: $OUT_DIR"
