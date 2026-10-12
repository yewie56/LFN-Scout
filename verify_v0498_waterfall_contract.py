#!/usr/bin/env python3
"""Static contract for v0.4.98 (NOT a replacement for Android/Gradle)."""
from pathlib import Path
p=Path(__file__).resolve().parent
ct=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text(encoding='utf8')
ui=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text(encoding='utf8')
agg=(p/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/WaterfallAggregator.kt').read_text(encoding='utf8')
version=(p/'app/build.gradle.kts').read_text(encoding='utf8')
checks={
 'Exactly three waterfall modes': all(x in agg for x in ['CURRENT(', 'AVERAGE(', 'MAXIMUM(']),
 'Linear power average': '10.0.pow(db / 10.0)' in agg and '10.0 * log10' in agg,
 'Binwise max': 'if (db > maximum[i])' in agg,
 'Default current persistent': 'getString("waterfall_mode", WaterfallMode.CURRENT.name)' in ct,
 'Controller per FFT aggregation': 'waterfallAccumulator.accept(s.liveDbFs)' in ct,
 'One row when due': 'if (dueRow)' in ct and 'waterfallAccumulator.take(waterfallMode)' in ct,
 'Mode change clears waterfall history': 'fun setWaterfallMode(' in ct and 'history.clear()' in ct,
 'Setup saves mode': 'putString("setup_${slot}_water_mode",state.waterfallMode.name)' in ui,
 'Setup loads mode': 'getString("setup_${slot}_water_mode", WaterfallMode.CURRENT.name)' in ui,
 'PNG declares mode': 'Wasserfall ${s.waterfallMode.label}' in ct,
 'Spectrum max hold not shown': 'curve(state.maxHold' not in ui and 'curve(state.maximum' not in ui,
 'Version 0.4.98/139': 'versionCode = 139' in version and 'versionName = "0.4.98"' in version,
 'Diagnostics callback retained': 'private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)' in ct,
}
for name, valid in checks.items(): print(('PASS' if valid else 'FAIL')+': '+name)
print(f'STATIC_CHECKS {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
