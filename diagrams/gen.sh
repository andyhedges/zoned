#!/usr/bin/env bash
set -euo pipefail

INPUT="${1:-recursive-resolution.puml}"
OUTDIR="${2:-out}"

missing=0

check() {
  local cmd="$1"
  local name="$2"
  local install="$3"

  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing: $name"
    echo "Install with: $install"
    echo
    missing=1
  fi
}

check plantuml "PlantUML" "brew install plantuml   or   sudo apt-get update && sudo apt-get install -y plantuml"
check dot "Graphviz (dot)" "brew install graphviz   or   sudo apt-get update && sudo apt-get install -y graphviz"
check java "Java (runtime)" "brew install openjdk   or   sudo apt-get update && sudo apt-get install -y default-jre"

if [ "$missing" -eq 1 ]; then
  echo "Cannot render diagrams until the missing tools are installed."
  exit 1
fi

if [ ! -f "$INPUT" ]; then
  echo "Input file not found: $INPUT" >&2
  echo "Usage: $0 [input.puml] [outdir]" >&2
  exit 2
fi

mkdir -p "$OUTDIR"

echo "Checking PlantUML syntax: $INPUT"
if ! plantuml -checkonly "$INPUT"; then
  echo "PlantUML syntax error in: $INPUT" >&2
  exit 3
fi

echo "Rendering: $INPUT -> $OUTDIR (svg + png)"
plantuml -tsvg -tpng -o "$OUTDIR" "$INPUT"

echo "Done. Outputs in: $OUTDIR/"
