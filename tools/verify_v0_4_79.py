from pathlib import Path
root = Path(__file__).resolve().parents[1]
ui = (root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text(encoding='utf-8')
regional = (root/'app/src/main/java/de/brummbeobachter/app/SeismicRegionalPolicy.kt').read_text(encoding='utf-8')
wave = (root/'app/src/main/java/de/brummbeobachter/app/SeismicWaveformPolicy.kt').read_text(encoding='utf-8')
live = (root/'app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks = [
 ('version', 'versionName = "0.4.79"' in gradle and 'versionCode = 119' in gradle),
 ('master 6144x3456', 'MASTER_WIDTH_PX = 6144' in ui and 'MASTER_HEIGHT_PX = 3456' in ui),
 ('4x supersampling', 'EXPORT_SUPERSAMPLE = 4' in ui),
 ('1536 bins per row', 'HELICORDER_BINS_PER_ROW = 1536' in live),
 ('bin keeps first/last', 'val first: Float' in wave and 'val last: Float' in wave),
 ('bin keeps extrema order', 'val minBeforeMax: Boolean' in wave),
 ('regional captures edge samples', 'firstValue' in regional and 'lastValue' in regional),
 ('regional captures extrema positions', 'minIndex' in regional and 'maxIndex' in regional),
 ('scheli emits four values per bin', 'FloatArray(bins.size * 4)' in ui),
 ('scheli uses real extrema order', 'if (bin.minBeforeMax)' in ui),
]
failed=[]
for name, ok in checks:
    print(('[PASS] ' if ok else '[FAIL] ')+name)
    if not ok: failed.append(name)
if failed:
    raise SystemExit('FAILED: '+', '.join(failed))
print(f'PASS: {len(checks)}/{len(checks)}')
