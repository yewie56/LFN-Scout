from pathlib import Path
import sys
root=Path(__file__).resolve().parent
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
b=(root/'app/build.gradle.kts').read_text()
a=(root/'app/src/main/java/de/brummbeobachter/app/AudioArchive.kt').read_text()
pol=(root/'app/src/main/java/de/brummbeobachter/app/AudioArchiveCompatibilityPolicy.kt').read_text()
need('versionName = "0.4.84"' in b and 'versionCode = 124' in b,'version 0.4.84/124')
need('Download/LFN-Scout/' in pol,'current path')
need('Download/BrummBeobachter/' in pol,'legacy path')
need('findPublishedWavByName' in a,'MediaStore name fallback')
need('ALREADY_ABSENT' in a and 'shouldRemoveMetadataAfterDelete' in a,'idempotent missing-file cleanup')
need('AudioArchiveCompatibilitySelfTest.kt' in [p.name for p in root.iterdir()],'compatibility selftest')
print('PASS' if not errors else 'FAIL')
for e in errors: print(e)
sys.exit(1 if errors else 0)
