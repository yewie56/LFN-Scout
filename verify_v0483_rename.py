from pathlib import Path
import re, sys
root=Path(__file__).resolve().parent
errors=[]

def need(cond,msg):
    if not cond: errors.append(msg)

build=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
need('versionName = "0.4.83"' in build and 'versionCode = 123' in build,'version 0.4.83/123')
strings=(root/'app/src/main/res/values/strings.xml').read_text(encoding='utf-8')
need('<string name="app_name">LFN-Scout</string>' in strings,'app_name LFN-Scout')
workflow=(root/'.github/workflows/build-android.yml').read_text(encoding='utf-8')
need('Build LFN-Scout' in workflow,'workflow name')
need('LFN-Scout-v${VERSION}-debug.apk' in workflow,'APK name')
archive=(root/'app/src/main/java/de/brummbeobachter/app/AudioArchive.kt').read_text(encoding='utf-8')
seis=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
need('Download/LFN-Scout/' in archive,'AudioArchive public path')
need('Download/LFN-Scout/' in seis,'Seismic public path')
logger=(root/'app/src/main/java/de/brummbeobachter/app/DebugLogger.kt').read_text(encoding='utf-8')
need('LFN-Scout_Log_' in logger,'log filename')
# Internal package is the deliberate compatibility exception.
allowed_fragments=('package de.brummbeobachter.app','import de.brummbeobachter.app','applicationId = "de.brummbeobachter.app"','namespace = "de.brummbeobachter.app"')
for p in root.rglob('*'):
    if not p.is_file() or '.git' in p.parts or p.name == 'verify_v0483_rename.py':
        continue
    if p.suffix.lower() not in {'.kt','.kts','.xml','.md','.txt','.yml','.yaml','.bat','.ps1','.pro','.toml','.json','.gradle','.properties'}:
        continue
    try: text=p.read_text(encoding='utf-8')
    except Exception: continue
    for lineno,line in enumerate(text.splitlines(),1):
        if 'LFN-INvestigator' in line or 'BrummBeobachter' in line:
            if any(a in line for a in allowed_fragments):
                continue
            errors.append(f'legacy name: {p.relative_to(root)}:{lineno}: {line.strip()[:120]}')
need((root/'docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md').exists(),'renamed changes doc')
need((root/'docs/project/LFN-Scout_Geplante_Erweiterungen.md').exists(),'renamed planned doc')
if errors:
    print('FAIL')
    for e in errors[:80]: print(e)
    print(f'errors={len(errors)}')
    sys.exit(1)
print('PASS')
