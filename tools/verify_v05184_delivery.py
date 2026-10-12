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
base_req = (root/'BASE_PACKAGE_REQUIRED.txt').read_text(encoding='utf-8')
workflow = (root/'BUILD_WORKFLOWS.md').read_text(encoding='utf-8')
status = (root/'PROJECT_STATUS.md').read_text(encoding='utf-8')
changelog = (root/'CHANGELOG.md').read_text(encoding='utf-8')

check('versionName 0.5.18.4', 'versionName = "0.5.18.4"' in build)
check('versionCode 165', 'versionCode = 165' in build)
check('prepare banner updated', 'LFN-Scout v0.5.18.4' in prepare)
check('vortest banner updated', 'LFN-Scout v0.5.18.4' in vortest)
check('vortest active path updated', 'LFN-Scout_ACTIVE_v0.5.18.4.zip' in vortest)
check('install convenience batch preserved', 'VORTEST.bat --install' in install_bat)
check('runner version updated', "$version = '0.5.18.4'" in runner)
check('runner uses ProcessStartInfo', 'System.Diagnostics.ProcessStartInfo' in runner)
check('runner redirects stdout and stderr', 'RedirectStandardOutput = $true' in runner and 'RedirectStandardError = $true' in runner)
check('runner live line async stdout', 'StandardOutput.ReadLineAsync()' in runner)
check('runner live line async stderr', 'StandardError.ReadLineAsync()' in runner)
check('runner no ReadToEnd buffering', 'ReadToEndAsync()' not in runner)
check('runner screen+log function', 'function Write-ScreenAndLog' in runner and 'Add-Content -LiteralPath $log' in runner)
check('runner writes every live line to screen+log', 'Write-ScreenAndLog ([string]$line)' in runner)
check('exit code remains authoritative', 'ExitCode = [int]$exitCode' in runner and '$run.ExitCode -ne 0' in runner)
check('runner summary exists', 'VORTEST-ZUSAMMENFASSUNG' in runner and 'GESAMTERGEBNIS' in runner)
check('summary on fail path', "Write-Summary 'FAIL'" in runner)
check('summary on pass path', "Write-Summary 'PASS'" in runner)
check('APK data on screen', "Write-ScreenAndLog ('APK: {0}'" in runner and "Write-ScreenAndLog ('SHA-256: {0}'" in runner)
check('ADB path shown', "Write-ScreenAndLog ('ADB: {0}'" in runner)
check('ADB device counters shown', 'Freigegebene ADB-Geraete' in runner and 'Nicht autorisiert' in runner and 'Offline:' in runner)
check('ADB zero-device guidance shown', 'USB-DATENKABEL' in runner and 'USB-Debugging einschalten' in runner)
check('exactly one device required', '$devices.Count -ne 1' in runner)
check('unauthorized rejected', 'FAIL_UNAUTHORIZED' in runner)
check('adb install -r retained', "@('-s', $serial, 'install', '-r', $apkTarget)" in runner)
check('active pack excludes base paths', all(x in pack for x in [
    'docs/project/analyzer_v0.7/assets/', 'docs/project/analyzer_v0.7/references/',
    'docs/project/handoff_assets/', 'docs/project/FILTERBANK_UI_REFERENCE/',
    'docs/reconstruction/', 'supabase/storage_backup/'
]))
check('base R1 unchanged', 'LFN-Scout_BASE_ASSETS_R1.zip' in base_req and re.search(r'SHA-256:\s*[0-9a-f]{64}', base_req))
check('workflow documents live output', 'v0.5.18.4' in workflow and 'live' in workflow.lower())
check('status documents real v0.5.18.3 pass', 'Projektstatus v0.5.18.4' in status and 'PREPARE PASS' in status and 'Fehlercode 62' in status)
check('changelog starts with v0.5.18.4', changelog.startswith('# v0.5.18.4'))
check('new testplan exists', (root/'docs'/'project'/'TESTPLAN_v0.5.18.4.md').exists())
check('new packaging doc exists', (root/'docs'/'project'/'PROJECT_PACKAGING_v0.5.18.4.md').exists())
check('new build verification exists', (root/'BUILD_VERIFICATION_v0.5.18.4.md').exists())

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} PASS')
if failed:
    raise SystemExit('FAILED: ' + '; '.join(failed))
