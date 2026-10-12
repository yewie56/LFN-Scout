#!/usr/bin/env python3
"""Targeted Kotlin type/membership regression; NOT an Android compiler."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
c=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text(encoding='utf-8')
d=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/DirectAnalyzerConfig.kt').read_text(encoding='utf-8')
a=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text(encoding='utf-8')
g=(r/'app/build.gradle.kts').read_text(encoding='utf-8')
checks={
  'separate set and array membership checks': 'require(if (direct48k) fftHop in DirectAnalyzerConfig.permissibleHops else fftHop in AnalyzerTiming.allowedFftHops)' in c,
  'old unresolved mixed-type membership absent': 'fftHop in if (direct48k)' not in c,
  'direct set type confirmed': 'val permissibleHops: Set<Int>' in d,
  'legacy int array type confirmed': 'val allowedFftHops = intArrayOf(' in a,
  'other DSP membership remains int arrays': 'require(size in if (direct48k) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes)' in c,
  'new version 0.5.10 / 153': 'versionCode = 153' in g and 'versionName = "0.5.10"' in g,
  'new device plan exists': (r/'docs/project/TESTPLAN_v0.5.10.md').is_file(),
}
for name,pass_ in checks.items(): print(f"{'PASS' if pass_ else 'FAIL'}: {name}")
print(f"STATIC MEMBERSHIP CONTRACT {sum(checks.values())}/{len(checks)} (NOT Android build)")
if not all(checks.values()): raise SystemExit(1)
