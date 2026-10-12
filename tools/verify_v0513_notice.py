from pathlib import Path
import hashlib
root=Path(__file__).resolve().parents[1]
old=Path('/mnt/data/LFN-Scout_Android_v0.5.12')
s=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
b=(old/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
checks={
 '5sec effect with display-only key': 'LaunchedEffect(waterfallNoticeKey)' in s and 'delay(5_000L)' in s,
 'not affected by live row count': 'state.rows.size' not in s[s.index('val waterfallNoticeKey'):s.index(').joinToString("|")')],
 'not affected by live waterfall revision': 'state.waterfallRevision' not in s[s.index('val waterfallNoticeKey'):s.index(').joinToString("|")')],
 'direct filter controls inactive on direct': 'if (!state.direct48k && state.showFilterWaterfall) state.filterBandCount else 0' in s,
 'bottom-right align inside waterfall box': 'Modifier.align(Alignment.BottomEnd)' in s and '.padding(end=plotRightDp + 4.dp, bottom=plotBottomDp + 4.dp)' in s,
 'exactly two popup text lines': 'Text(notice.first,' in s and 'Text(notice.second,' in s,
 'font heights preserved': 'color=white, fontSize=10.sp, lineHeight=11.sp' in s and 'fontSize=9.sp, lineHeight=10.sp' in s,
 'static watermark status removed': 'Text("Wasserfall $modeShort' not in s,
 'static filter status removed': 'FILTER: Band-RMS in dBFS (Magenta–Weiß)' not in s,
 'static progress status removed': 'FILTER-Autorange: ${(state.filterAutoRangeProgress' not in s,
 'image generator exactly unchanged': s[s.index('private fun makeWaterfall('):s.index('/** Two fixed 48-dp icons.')] == b[b.index('private fun makeWaterfall('):b.index('/** Two fixed 48-dp icons.')],
 'filter renderer unchanged': (root/'app/src/main/java/de/brummbeobachter/app/FilterWaterfallRenderer.kt').read_bytes() == (old/'app/src/main/java/de/brummbeobachter/app/FilterWaterfallRenderer.kt').read_bytes(),
 'time axis unchanged': (root/'app/src/main/java/de/brummbeobachter/app/WaterfallTimeAxis.kt').read_bytes() == (old/'app/src/main/java/de/brummbeobachter/app/WaterfallTimeAxis.kt').read_bytes(),
 'original icon unchanged': (root/'app/src/main/res/drawable-nodpi/original_waterfall_icon.png').read_bytes() == (old/'app/src/main/res/drawable-nodpi/original_waterfall_icon.png').read_bytes(),
 'filter five bars and toggles preserved': 'floatArrayOf(.40f,.72f,1.0f,.72f,.40f)' in s and 'LiveAnalyzerController.setWaterfallVisibility' in s,
 'camera popup preserved': 'if (saveMessage.isNotEmpty()) {' in s and 'delay(2200)' in s,
 'version and code': 'versionCode = 156' in (root/'app/build.gradle.kts').read_text() and 'versionName = "0.5.13"' in (root/'app/build.gradle.kts').read_text(),
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL')+': '+name)
assert all(checks.values()), 'static checks failed'
print(f'WATERFALL_NOTICE_STATIC PASS {sum(checks.values())}/{len(checks)}')
