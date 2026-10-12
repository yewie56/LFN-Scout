from pathlib import Path
root=Path(__file__).resolve().parent
errors=[]
def ck(name, cond):
    if not cond: errors.append(name)
app=(root/'app/build.gradle.kts').read_text()
ui=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text()
svc=(root/'app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt').read_text()
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text()
exp=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text()
ck('versionName 0.4.80', 'versionName = "0.4.80"' in app)
ck('versionCode 120', 'versionCode = 120' in app)
ck('near plot gain constant', 'const val NEAR_PLOT_VALUE_GAIN = 2.0' in ui)
ck('3072 bins', 'private const val HELICORDER_BINS_PER_ROW = 3072' in svc)
ck('valueScale multiplied', 'valueScale = (velocityScale ?: 1.0) * SeismicPlotUiPolicy.NEAR_PLOT_VALUE_GAIN' in svc)
ck('ui mentions Messwerte ×2', 'Messwerte ×2' in main)
ck('ui mentions 3072 Bins/Zeile', '3072 Bins/Zeile' in main)
ck('exporter meta mentions Messwerte ×2', 'Messwerte ×2' in exp)
print('PASS' if not errors else 'FAIL')
for e in errors: print('MISSING:', e)
raise SystemExit(0 if not errors else 1)
