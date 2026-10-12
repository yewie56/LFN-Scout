from pathlib import Path
root=Path(__file__).resolve().parent
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text()
build=(root/'app/build.gradle.kts').read_text()
checks=[]
def ck(name, cond):
    checks.append((name, cond))
ck('version 0.4.82', 'versionName = "0.4.82"' in build and 'versionCode = 122' in build)
ck('recording dialog removed', 'title = { Text("Verbleibende Aufnahmezeit") }' not in main)
ck('transparent countdown overlay marker', 'AUDIO_RECORDING_TRANSPARENT_OVERLAY' in main)
ck('large remaining-seconds text', 'fontSize = 72.sp' in main)
ck('yellow abort button still present', 'Text("Aufnahme abbrechen")' in main and 'containerColor =\n                            MaterialTheme\n                                .colorScheme\n                                .secondary' in main)
ck('clip indicator still present', '"CLIP · $clippedSamples"' in main and '"CLIP: nein"' in main)
failed=[name for name,ok in checks if not ok]
for name,ok in checks:
    print(('PASS' if ok else 'FAIL')+': '+name)
raise SystemExit(1 if failed else 0)
