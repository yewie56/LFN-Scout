#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
core=analyzer-core/src/main/kotlin/de/brummbeobachter/app
folder=$(mktemp -d)
trap 'rm -rf "$folder"' EXIT
kotlinc "$core"/*.kt tools/analyzer_timing494_selftest.kt -include-runtime -d "$folder/selftest.jar"
java -jar "$folder/selftest.jar"
