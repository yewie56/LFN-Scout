#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/*.kt tools/analyzer_v0506_selftest.kt -include-runtime -d "$tmp/test.jar"
java -jar "$tmp/test.jar"
