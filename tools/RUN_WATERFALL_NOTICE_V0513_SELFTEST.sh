#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
kotlinc app/src/main/java/de/brummbeobachter/app/AnalyzerWaterfallNotice.kt tools/waterfall_notice_v0513_selftest.kt -include-runtime -d "$tmp/waterfall-notice.jar"
java -jar "$tmp/waterfall-notice.jar"
