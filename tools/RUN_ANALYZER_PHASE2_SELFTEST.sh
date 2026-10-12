#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v kotlinc >/dev/null || { echo 'Kotlin CLI compiler (kotlinc) required' >&2; exit 2; }
command -v kotlin >/dev/null || { echo 'Kotlin CLI runner (kotlin) required' >&2; exit 2; }
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/*.kt app/src/main/java/de/brummbeobachter/app/MicCaptureArbiter.kt tools/analyzer_phase2_selftest.kt -d "$tmp/test.jar"
kotlin -classpath "$tmp/test.jar" Analyzer_phase2_selftestKt
