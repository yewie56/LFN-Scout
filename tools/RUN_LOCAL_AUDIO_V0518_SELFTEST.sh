#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python tools/verify_v0518_local_audio.py
python tools/verify_portrait_v0517.py
TEMP_JAR="$(mktemp --suffix=.jar)"
trap 'rm -f "$TEMP_JAR"' EXIT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/StatisticsWindowEngine.kt \
  tools/test_local_statistics_v0518.kt -include-runtime -d "$TEMP_JAR"
java -jar "$TEMP_JAR"
