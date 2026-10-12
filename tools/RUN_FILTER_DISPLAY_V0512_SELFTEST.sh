#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/FilterDisplayRange.kt tools/filter_display_range_v0512_selftest.kt -include-runtime -d "$tmp/filter-test.jar"
java -jar "$tmp/filter-test.jar"
