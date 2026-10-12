#!/usr/bin/env python3
"""v0.4.97 source-level regression checks; not an Android build or UI/device test."""
from pathlib import Path
r=Path(__file__).resolve().parent
read=lambda p:(r/p).read_text(encoding='utf-8')
ui=read('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
ctrl=read('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
scale=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerDisplayRange.kt')
tests=read('analyzer-core/src/test/kotlin/de/brummbeobachter/app/AnalyzerDisplayRangeTest.kt')
gradle=read('app/build.gradle.kts')
checks={
    'identity v0.4.97/138': 'versionCode = 138' in gradle and 'versionName = "0.4.97"' in gradle,
    'diagnostic callback defined': 'private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)' in ctrl and ctrl.count('::acceptDiagnostics')==3,
    'shared raw PCM no extra capture': 'AnalyzerSessionGateway.startRawLf' in ctrl and 'import android.media.AudioRecord' not in ui,
    'autorange two stages': 'frameLows.add' in scale and 'frameHighs.add' in scale and 'percentile(frameLows.sorted(), 0.10)' in scale and 'percentile(frameHighs.sorted(), 0.90)' in scale,
    'autorange rejects DC invalid': 'hz > 0.0' in scale and 'db > -180.0' in scale,
    'autorange fixed dB margin': 'low - marginDb' in scale and 'high + marginDb' in scale,
    'autorange persistent both limits': 'putInt("display_min_db", bounds.first)' in ctrl and 'putInt("display_max_db", bounds.second)' in ctrl,
    'robust narrow notch regression': 'isolatedSpectralDipsAndPeaksAreNotHardBounds' in tests,
    'robust time regression': 'shortOutlierFramesDoNotDominateTwoSecondCapture' in tests,
    'cursor without box': 'drawText(label,bx+1.dp.toPx(),baseline,paint)' in ui and 'drawRoundRect(RectF(bx,by' not in ui,
    'cursor drag and text off': 'detectDragGestures' in ui and 'cursorHz = null' in ui and 'cursorReadout(state,hz)' in ui,
    'stats 3dp intrinsic unfilled': '.clickable {' in ui and 'RoundedCornerShape(6.dp))\n                .clickable' in ui and '}.padding(3.dp)' in ui and '.width(156.dp).height(33.dp)' not in ui,
    'reset statistics and DSP average': 'levelHistory.clear()' in ctrl and 'session?.resetAveraging()' in ctrl,
    'bar 27 to 18dp': 'barRight=barLeft+18.dp.toPx()' in ui,
    'PNG bar 63 to 42px': 'val barL=10f;val barR=52f' in ctrl,
    'shared Hz plot margins': 'val plotLeftDp = 60.dp' in ui and 'val plotRightDp = 12.dp' in ui and ui.count('val left=plotLeftDp.toPx();val right=size.width-plotRightDp.toPx()')==1,
    'PNG plots share bounds': 'val left = 140f; val right = 970f;' in ctrl,
    'UI and PNG ticks': 'frequencyTicks(state.rangeHz)' in ui and 'AnalyzerDisplayTicks.db(state.minDisplayDb, state.maxDisplayDb)' in ui and 'AnalyzerDisplayTicks.db(s.minDisplayDb,s.maxDisplayDb)' in ctrl,
    'offscreen PNG and auto-PNG preserved': 'renderPng(s: Snapshot)' in ctrl and 'fun saveNow(' in ctrl,
}
fail=[]
for label,ok in checks.items():
    print(('PASS' if ok else 'FAIL')+' | '+label)
    if not ok:fail.append(label)
print(f'{len(checks)-len(fail)}/{len(checks)} static checks PASS')
if fail: raise SystemExit(1)
