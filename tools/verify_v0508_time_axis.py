#!/usr/bin/env python3
"""Version-local structural safeguards; does not replace Android/Compose compilation."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ctrl = (ROOT / 'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text(encoding='utf-8')
ui = (ROOT / 'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text(encoding='utf-8')
time = (ROOT / 'app/src/main/java/de/brummbeobachter/app/WaterfallTimeAxis.kt').read_text(encoding='utf-8')
gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
checks = {
    'versionName/code': 'versionName = "0.5.08"' in gradle and 'versionCode = 151' in gradle,
    'paired rows': 'historyEpochMillis.addLast(System.currentTimeMillis())' in ctrl and
        'history.removeFirst(); historyEpochMillis.removeFirst()' in ctrl,
    'all history resets paired': len(re.findall(r'\bhistory\.clear\(\)', ctrl)) ==
        len(re.findall(r'\bhistoryEpochMillis\.clear\(\)', ctrl)),
    'snapshot owns timestamps': 'val rowEpochMillis: List<Long>' in ctrl and 'history.toList(), historyEpochMillis.toList(),' in ctrl,
    'both renderers use the same clock ticks': 'WaterfallTimeAxis.ticks(s.rowEpochMillis' in ctrl and
        'WaterfallTimeAxis.ticks(state.rowEpochMillis' in ui,
    'both old seconds axes removed': 'for(step in 0..4)' not in ui and 'for(step in 0..4)' not in ctrl,
    'spectrum/waterfall common x-origin retained': 'val plotLeftDp = 54.dp' in ui and
        'val left = 124f; val right = 970f' in ctrl and 'val left=plotLeftDp.toPx();val right=size.width-plotRightDp.toPx()' in ui,
    'colorbar remains drawn': 'val barRight=barLeft+labelWidth+2.dp.toPx()' in ui and
        'drawRect(barL,y,barR' in ctrl,
    'timeband fits original left margin': 'val timeBandR = left - 5.dp.toPx()' in ui and 'val timeBandR=left-8f' in ctrl,
    'real row anchor + 5 max': 'candidates.add(Tick(0f, newestFirst.first()))' in time and 'candidates.size == 5' in time,
    'pause gaps guarded': 'gap > maxAdjacentMs' in time,
    'popup anchored at camera': 'Popup(alignment=Alignment.TopStart' in ui and 'Modifier.align(Alignment.TopCenter).padding(top=64.dp)' not in ui,
    'revision documentation': (ROOT/'docs/project/TESTPLAN_v0.5.08.md').exists() and
        (ROOT/'BUILD_VERIFICATION_v0.5.08.md').exists(),
}
for name, ok in checks.items():
    print(f'{"PASS" if ok else "FAIL"} {name}')
if not all(checks.values()):
    raise SystemExit(1)
print(f'PASS {len(checks)}/{len(checks)} static checks (NOT Android build)')
