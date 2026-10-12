#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
kotlinc tools/lfn0510_membership_selftest.kt -include-runtime -d "$tmp/membership.jar"
java -jar "$tmp/membership.jar"
python3 tools/verify_v0510_kotlin_membership.py
python3 tools/verify_v0510_direct_analyzer.py
