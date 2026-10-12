#!/usr/bin/env python3
from pathlib import Path
import hashlib
p=Path(__file__).resolve().parents[1]
s=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
c=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
r=(p/'app/src/main/java/de/brummbeobachter/app/FilterWaterfallRenderer.kt').read_text()
u=(p/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt').read_text()
icon=p/'app/src/main/res/drawable-nodpi/original_waterfall_icon.png'
ref=p/'docs/project/FILTERBANK_UI_REFERENCE/ORIGINAL_ICON_NUTZER_2026-09-24.png'
checks={
 'unmodified embedded user ORIGINAL PNG': icon.read_bytes()==ref.read_bytes() and icon.read_bytes().startswith(b'\x89PNG'),
 'ORIGINAL image displayed via drawable': 'painterResource(R.drawable.original_waterfall_icon)' in s,
 'FILTER five fixed symmetric bars': 'floatArrayOf(.40f,.72f,1.0f,.72f,.40f)' in s,
 'button remains exactly 48 dp': 'Box(Modifier.size(48.dp).padding(1.dp)' in s,
 'separate click and longpress': 'detectTapGestures(onTap={ onClick() }, onLongPress={ onLongPress() })' in s,
 'FILTER invokes autorange on longpress': 'LiveAnalyzerController.startFilterAutoRange()' in s,
 'auto source is actual filter RMS not spectrum': 'collector.accept(level.absoluteDbFs)' in c,
 'automatik does not alter original collector': 'filterAutoRange=FilterDisplayRange(filterAutoRangeMarginDb)' in c and 'autoRange = AnalyzerDisplayRange(autoRangeMarginDb)' in c,
 'separate filter display keys persisted': '.putInt("filter_display_min_db",result.first)' in c and '.putInt("filter_display_max_db",result.second)' in c,
 'filter scale saved in setups': 'setup_${slot}_filter_display_min' in u and 'setup_${slot}_filter_auto_duration' in u,
 'original and filter display independent in renderer': 'if(!original) return filterColor(db[b],filterMinDb,filterMaxDb)' in r,
 'mixed mode keeps original overlay trigger': 'val alpha=((excess[b]-3.0)/15.0)' in r,
 'screen/PNG both call renderer': 'FilterWaterfallRenderer.colorAt' in c and 'FilterWaterfallRenderer.colorAt' in s,
 'filter-only colorbar uses independent scale': 'val legendMin=if(filterOnly) state.filterDisplayMinDb' in s,
 'filter-only PNG colorbar independent': 'val barMin=if(filterOnly) s.filterDisplayMinDb' in c,
 'mode profiles transport settings': '"filter_autorange_duration_sec", "filter_autorange_margin_db"' in c,
 'lifecycle cancellation': 'filterAutoRange=null;filterAutoRangeStartNs=0L' in c,
 'correct version': 'versionCode = 155' in (p/'app/build.gradle.kts').read_text() and 'versionName = "0.5.12"' in (p/'app/build.gradle.kts').read_text(),
}
for label,valid in checks.items(): print(('PASS' if valid else 'FAIL')+': '+label)
print('STATIC CONTRACT',sum(checks.values()),'/',len(checks))
assert all(checks.values())
print('ORIGINAL_ICON_SHA256',hashlib.sha256(icon.read_bytes()).hexdigest())
