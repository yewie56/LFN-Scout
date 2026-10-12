from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
checks=[]
def check(name, cond):
    checks.append((name,bool(cond)))
    print(('PASS' if cond else 'FAIL')+': '+name)
build=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
runner=(root/'tools/run-vortest.ps1').read_text(encoding='utf-8')
bat=(root/'VORTEST.bat').read_text(encoding='utf-8')
prep=(root/'PREPARE_PROJECT.bat').read_text(encoding='utf-8')
pack=(root/'tools/package-active.ps1').read_text(encoding='utf-8')
check('versionName 0.5.21', 'versionName = "0.5.21"' in build)
check('versionCode 168', 'versionCode = 168' in build)
check('runner no hardcoded 0.5.19', "$version = '0.5.19'" not in runner)
check('runner reads build.gradle.kts', "app\\build.gradle.kts" in runner and 'versionMatch' in runner)
check('VORTEST reads build.gradle.kts', 'LFN_VERSION' in bat and 'app\\build.gradle.kts' in bat)
check('VORTEST output uses LFN_VERSION', 'LFN-Scout_v%LFN_VERSION%-debug.apk' in bat and 'LFN-Scout_ACTIVE_v%LFN_VERSION%.zip' in bat)
check('PREPARE dynamic banner', 'LFN_VERSION' in prep and 'LFN-Scout v%LFN_VERSION% - Projekt vorbereiten' in prep)
check('package-active reads build.gradle.kts', 'versionMatch' in pack and 'app\\build.gradle.kts' in pack)
check('v0.5.21 build verification present', (root/'BUILD_VERIFICATION_v0.5.21.md').is_file())
failed=[n for n,c in checks if not c]
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} PASS')
raise SystemExit(1 if failed else 0)
