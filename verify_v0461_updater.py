from pathlib import Path
import re, json, sys
root=Path(__file__).resolve().parent
errors=[]

def need(path):
    p=root/path
    if not p.exists(): errors.append(f"FEHLT: {path}")
    return p

app=need(Path('app/build.gradle.kts'))
if app.exists():
    t=app.read_text(encoding='utf-8')
    if 'versionName = "0.4.61"' not in t: errors.append('versionName != 0.4.61')
    if 'versionCode = 101' not in t: errors.append('versionCode != 101')
for p in [
    Path('Tools/WindowsUpdater/Install-LFN-IN-Updater.cmd'),
    Path('Tools/WindowsUpdater/Update-LFN-IN.cmd'),
    Path('Tools/WindowsUpdater/Update-LFN-IN.ps1'),
    Path('Tools/WindowsUpdater/settings.json'),
    Path('Tools/WindowsUpdater/README.md'),
    Path('Builds/debug/.gitkeep'),
]: need(p)
wf=need(Path('.github/workflows/build-android.yml'))
if wf.exists():
    w=wf.read_text(encoding='utf-8')
    for s in ['STORE_APK_IN_REPOSITORY', 'Builds/debug', 'git push origin HEAD:main', 'actions/upload-artifact@v4']:
        if s not in w: errors.append(f'Workflow fehlt: {s}')
readme=root/'Tools/WindowsUpdater/README.md'
if readme.exists():
    r=readme.read_text(encoding='utf-8')
    for s in ['Erstinstallation', 'Normaler Update-Ablauf', 'Dry-Run', 'Rollback', 'GitHub CLI', 'Variante A', 'Variante B']:
        if s not in r: errors.append(f'Updater-Anleitung fehlt Abschnitt/Begriff: {s}')
settings=root/'Tools/WindowsUpdater/settings.json'
if settings.exists():
    try:
        d=json.loads(settings.read_text(encoding='utf-8'))
        if d.get('repository')!='https://github.com/yewie56/LFN-IN.git': errors.append('Repository URL falsch')
    except Exception as e: errors.append(f'settings.json ungültig: {e}')
if errors:
    print('FAIL v0.4.61 updater verification')
    for e in errors: print('-',e)
    sys.exit(1)
print('PASS v0.4.61 updater verification')
