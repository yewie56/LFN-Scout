from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
exp = (root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
ui = (root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks = [
 ('version', 'versionName = "0.4.78"' in gradle and 'versionCode = 118' in gradle),
 ('master 3072x1728', 'MASTER_WIDTH_PX = 3072' in ui and 'MASTER_HEIGHT_PX = 1728' in ui),
 ('single near renderer used by UI', 'SeismicPlotExporter.renderNearPlot(data)' in main),
 ('near bitmap displayed', 'nearPlotBitmap.asImageBitmap()' in main),
 ('renderer exposed', 'fun renderNearPlot(snapshot: SeismicLiveService.Snapshot): Bitmap' in exp),
 ('no downscale in renderer', 'Bitmap.createScaledBitmap(hiRes' not in exp),
 ('save can reuse rendered bitmap', 'nearPlotBitmap: Bitmap? = null' in exp and 'nearPlotBitmap ?: renderNearPlot(snapshot)' in exp),
 ('save button in both plot cards', main.count('Text("Plot speichern")') >= 2),
 ('times embedded by renderer', 'canvas.drawText(time' in exp),
]
failed=[]
for name,ok in checks:
    print(('[PASS] ' if ok else '[FAIL] ')+name)
    if not ok: failed.append(name)
if failed:
    raise SystemExit('FAILED: '+', '.join(failed))
print(f'PASS: {len(checks)}/{len(checks)}')
