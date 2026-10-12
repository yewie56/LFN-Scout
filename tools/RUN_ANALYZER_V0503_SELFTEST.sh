#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
core=analyzer-core/src/main/kotlin/de/brummbeobachter/app
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
kotlinc "$core"/*.kt tools/analyzer_v0503_selftest.kt -include-runtime -d "$tmp/v0503.jar"
java -jar "$tmp/v0503.jar"
