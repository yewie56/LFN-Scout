#!/usr/bin/env python3
from pathlib import Path
p=Path(__file__).resolve().parents[1]
core=(p/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/FastFilterBank.kt').read_text()
sess=(p/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt').read_text()
gateway=(p/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text()
ctl=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
ui=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
settings=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt').read_text()
render=(p/'app/src/main/java/de/brummbeobachter/app/FilterWaterfallRenderer.kt').read_text()
cases={
 'source-based filter engine': 'class FastFilterBank' in core and 'sampleRateHz==8000' in core,
 'config bounds / dynamic bands': 'bands in 2..40' in core and 'maxHz <= 200.0' in core,
 'causal bandpass and fast envelope': 'class Biquad' in core and 'fastAlpha' in core,
 'only original recorder': 'AudioRecord(' not in core+sess+gateway+ctl,
 'PCM predecimation tap': sess.index('filterBank?.accept(frame.samples)') < sess.index('val down ='),
 'one subscription': 'private val subscription = hub.subscribe' in sess,
 'source discontinuity resets filters': 'filterBank?.reset()' in sess,
 'controller config and callback': 'onFilter = if (direct48k) null else ::acceptFilter' in ctl,
 'history aligns filters and original': 'filterHistory.addLast(pendingFilter' in ctl and 'filterHistory.removeFirst()' in ctl,
 'null-safe Java ArrayDeque sentinel': 'FastFilterBank.Levels(doubleArrayOf(),doubleArrayOf())' in ctl,
 'visual toggle independent of restart': 'fun setWaterfallVisibility' in ctl and 'waterfallRevision++ // same historical data, no restart' in ctl,
 'filter config STOP-only': 'check(!running && !direct48k)' in ctl,
 'matched UI and PNG palette': 'FilterWaterfallRenderer.colorAt' in ui and 'FilterWaterfallRenderer.colorAt' in ctl,
 'original inferno-like icon': 'if(original)' in ui and '0xFFFFE991' in ui,
 'filter exactly five bars': 'floatArrayOf(.40f,.72f,1.0f,.72f,.40f)' in ui,
 'both icon dimensions 48': 'Box(Modifier.size(48.dp).padding(1.dp)' in ui,
 'dynamic saved slots': 'setup_${slot}_filter_bands' in settings and 'setup_${slot}_filter_min_hz' in settings,
 '5 ms transform interval': 'intArrayOf(5, 10, 15' in (p/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/DirectAnalyzerConfig.kt').read_text(),
 'seconds-based direct smoothing': 'fun setDirectTimeSmoothing' in ctl and 'Glättungszeit in Sekunden' in settings,
 'latest version': 'versionCode = 154' in (p/'app/build.gradle.kts').read_text(),
 'explicit open validation': 'NOT RUN' in (p/'BUILD_VERIFICATION_v0.5.11.md').read_text(),
}
for name,good in cases.items():
 print(('PASS' if good else 'FAIL') + ': ' + name)
assert all(cases.values()), 'integration contract failure'
print(f'INTEGRATION CONTRACT PASS {len(cases)}/{len(cases)} (source-only, not Android compile)')
