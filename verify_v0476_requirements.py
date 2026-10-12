from pathlib import Path
root=Path(__file__).resolve().parent
exp=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
gradle=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks={
 'versionName 0.4.76':'versionName = "0.4.76"' in gradle,
 'versionCode 116':'versionCode = 116' in gradle,
 'raw WBBU png saver':'savePngBytes(' in exp,
 'WBBU uses source bytes':'snapshot.sourceImageBytes' in exp and 'savePngBytes(context.applicationContext, snapshot.sourceImageBytes, fileName)' in exp,
 'near plot renderer retained':'renderNearPlot(snapshot)' in exp,
 'common save button':'Text("Plot speichern")' in main,
 'success toast':'Plot erfolgreich gespeichert' in main,
 'LFN-Scout path':'Download/LFN-Scout/' in exp,
}
failed=[]
for name,ok in checks.items():
 print(('PASS' if ok else 'FAIL'), name)
 if not ok: failed.append(name)
print(f"RESULT {len(checks)-len(failed)}/{len(checks)} PASS")
raise SystemExit(1 if failed else 0)
