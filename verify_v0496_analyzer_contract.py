#!/usr/bin/env python3
"""Static integration guard. Does NOT replace Android/Gradle UI compilation or device testing."""
from pathlib import Path
import re
root=Path(__file__).resolve().parent
read=lambda name: (root/name).read_text(encoding='utf-8')
ctrl=read('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
ui=read('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
core=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt')
sess=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt')
scale=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerDisplayRange.kt')
gradle=read('app/build.gradle.kts')
checks={
 'identity code 137': 'versionCode = 137' in gradle and 'versionName = "0.4.96"' in gradle,
 'callback real declaration and 3 registrations': 'private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)' in ctrl and ctrl.count('::acceptDiagnostics') == 3,
 'no new microphone source': 'import android.media.AudioRecord' not in ctrl and 'import android.media.AudioRecord' not in ui and 'AnalyzerSessionGateway.startRawLf' in ctrl,
 'autorange wall-clock and sample aggregation': 'System.nanoTime() - autoRangeStartNs' in ctrl and 'collector.accept(s, range)' in ctrl and 'acceptedFrames' in scale,
 'autorange exactly fixed dB not percentage': 'low - marginDb' in scale and 'high + marginDb' in scale and 'percent' not in ctrl.lower(),
 'autorange prefs save and restore': 'putInt("display_min_db", bounds.first)' in ctrl and 'prefs.getInt("display_min_db", -120)' in ctrl,
 'autorange no usable signal handling': 'if (!running || paused || !validSignal)' in ctrl and 'Autorange abgebrochen' in ctrl,
 'reset both statistics and DSP average': 'levelHistory.clear()' in ctrl and 'session?.resetAveraging()' in ctrl and 'fun resetAveraging()' in core and 'fun resetAveraging()' in sess,
 'reset no waveform history clear': 'history.clear()' not in ctrl[ctrl.index('fun resetBandStatisticsAndAverage()'):ctrl.index('fun setTraceVisibility(')],
 'cursor tap, drag and hide via readout': 'detectTapGestures' in ui and 'detectDragGestures' in ui and 'cursorHz = null' in ui,
 'rounded red empty stats frame': '.border(BorderStroke(1.dp,Color(0xFFE34949)),RoundedCornerShape(6.dp))' in ui,
 'waterfall title/explanation gone': '"Klassischer Wasserfall"' not in ui and 'Zeit (s) ↓  ·' not in ui and '"Vergrößern"' not in ui,
 'same Hz viewport for both canvases': ui.count('val left=plotLeftDp.toPx();val right=size.width-plotRightDp.toPx()') == 1 and 'val left=plotLeftDp.toPx(); val right=size.width-plotRightDp.toPx()' in ui,
 'legend left, numbers inside': 'val barLeft=3.dp.toPx();val barRight=barLeft+27.dp.toPx()' in ui and 'barLeft+1.dp.toPx()' in ui,
 'user selects duration & fixed margin': 'setAutoRangeSettings(context,seconds,state.autoRangeMarginDb)' in ui and 'setAutoRangeSettings(context,state.autoRangeDurationSec,db)' in ui,
 'setup preserves custom min AND max': 'setup_${slot}_display_min' in ui and 'setup_${slot}_display_max' in ui,
 'png left legend and common range': 'val barL=12f;val barR=75f' in ctrl and 'AnalyzerDisplayTicks.db(s.minDisplayDb,s.maxDisplayDb)' in ctrl,
}
failed=[]
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL')+' | '+name)
    if not ok:failed.append(name)
print(f'{len(checks)-len(failed)}/{len(checks)} static checks PASS')
if failed:raise SystemExit(1)
