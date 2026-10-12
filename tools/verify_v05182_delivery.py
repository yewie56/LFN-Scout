from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
checks = []

def check(name, cond):
    checks.append((name, bool(cond)))

build = (root/'app'/'build.gradle.kts').read_text(encoding='utf-8')
prepare = (root/'PREPARE_PROJECT.bat').read_text(encoding='utf-8', errors='replace')
vortest = (root/'VORTEST.bat').read_text(encoding='utf-8', errors='replace')
install_bat = (root/'VORTEST_INSTALL.bat').read_text(encoding='utf-8', errors='replace')
runner = (root/'tools'/'run-vortest.ps1').read_text(encoding='utf-8')
pack = (root/'tools'/'package-active.ps1').read_text(encoding='utf-8')
basepack = (root/'tools'/'package-base-assets.ps1').read_text(encoding='utf-8')
base_req = (root/'BASE_PACKAGE_REQUIRED.txt').read_text(encoding='utf-8')
workflow = (root/'BUILD_WORKFLOWS.md').read_text(encoding='utf-8') if (root/'BUILD_WORKFLOWS.md').exists() else ''
directives = (root/'DIRECTIVES.md').read_text(encoding='utf-8') if (root/'DIRECTIVES.md').exists() else ''

check('versionName 0.5.18.2', 'versionName = "0.5.18.2"' in build)
check('versionCode 163', 'versionCode = 163' in build)
check('prepare keeps no-pause support', '--no-pause' in prepare and ':maybe_pause' in prepare)
check('vortest supports install option', '--install' in vortest and '-Install' in vortest)
check('install convenience batch exists', 'VORTEST.bat --install' in install_bat)
check('runner calls prepare', 'PREPARE_PROJECT.bat --no-pause' in runner)
check('runner unit tests before build', runner.find(':app:testDebugUnitTest --stacktrace') < runner.find('assembleDebug --stacktrace'))
check('runner checks expected apk', 'app\\build\\outputs\\apk\\debug\\app-debug.apk' in runner)
check('runner creates versioned apk', 'LFN-Scout_v{0}-debug.apk' in runner)
check('runner invokes active package after build', "'ACTIVE PACKAGE'" in runner and 'tools\\package-active.ps1' in runner)
check('runner requires exactly one adb device', '$devices.Count -ne 1' in runner)
check('runner rejects unauthorized device', 'unauthorized' in runner and "'FAIL_UNAUTHORIZED'" in runner)
check('runner installs via adb -r', 'install -r $apkTarget' in runner)
check('runner has adb failure codes', all(x in runner for x in [" 60 'ADB CHECK'", " 61 'ADB CHECK'", " 62 'ADB CHECK'", " 63 'ADB INSTALL'"]))
check('active pack excludes generated dirs', all(x in pack for x in ["'.git'", "'.gradle'", "'.idea'", "'.tools'", "'Builds'", "'build'"]))
check('active pack excludes machine local properties', "'local.properties'" in pack and 'org\\.gradle\\.java\\.home' in pack)
check('active pack excludes all six base roots', all(x in pack for x in [
    'docs/project/analyzer_v0.7/assets/',
    'docs/project/analyzer_v0.7/references/',
    'docs/project/handoff_assets/',
    'docs/project/FILTERBANK_UI_REFERENCE/',
    'docs/reconstruction/',
    'supabase/storage_backup/'
]))
check('active pack verifies by extraction', 'ExtractToDirectory' in pack and 'ACTIVE-ZIP-Verifikation' in pack)
check('active pack writes sha256', 'Get-FileHash' in pack and ".zip.sha256" not in pack and "$shaPath = $zipPath + '.sha256'" in pack)
check('base pack script has same six roots', all(x in basepack for x in [
    'docs/project/analyzer_v0.7/assets',
    'docs/project/analyzer_v0.7/references',
    'docs/project/handoff_assets',
    'docs/project/FILTERBANK_UI_REFERENCE',
    'docs/reconstruction',
    'supabase/storage_backup'
]))
check('base requirement names R1', 'LFN-Scout_BASE_ASSETS_R1.zip' in base_req)
check('base requirement has SHA-256', re.search(r'SHA-256:\s*[0-9a-f]{64}', base_req) is not None)
check('workflow documents v0.5.18.2', 'v0.5.18.2' in workflow and 'VORTEST_INSTALL.bat' in workflow and 'LFN-Scout_ACTIVE' in workflow)
check('directives document ACTIVE/BASE split', 'ACTIVE' in directives and 'BASE' in directives and 'BASE_PACKAGE_REQUIRED.txt' in directives)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} PASS')
raise SystemExit(1 if failed else 0)
