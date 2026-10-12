#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v kotlinc >/dev/null || { echo 'kotlinc required' >&2; exit 2; }
command -v kotlin >/dev/null || { echo 'kotlin required' >&2; exit 2; }
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt tools/analyzer_live_selftest.kt -d "$tmp/live.jar"
kotlin -classpath "$tmp/live.jar" Analyzer_live_selftestKt
