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

check('versionName 0.5.18.3', 'versionName = "0.5.18.3"' in build)
check('versionCode 164', 'versionCode = 164' in build)
check('prepare banner updated', 'LFN-Scout v0.5.18.3' in prepare)
check('prepare keeps no-pause support', '--no-pause' in prepare and ':maybe_pause' in prepare)
check('vortest updated to 0.5.18.3', 'LFN-Scout v0.5.18.3' in vortest and 'LFN-Scout_ACTIVE_v0.5.18.3.zip' in vortest)
check('install convenience batch preserved', 'VORTEST.bat --install' in install_bat)
check('runner uses ProcessStartInfo', 'System.Diagnostics.ProcessStartInfo' in runner and 'System.Diagnostics.Process' in runner)
check('runner captures stdout and stderr separately', 'RedirectStandardOutput = $true' in runner and 'RedirectStandardError = $true' in runner)
check('runner uses actual ExitCode', 'ExitCode = [int]$exitCode' in runner and '$run.ExitCode -ne 0' in runner)
check('runner no old native tee pipeline', '& $command 2>&1 | Tee-Object' not in runner)
check('runner documents stderr semantics', 'STDERR ist nicht automatisch ein Fehler' in runner)
check('runner async output read', 'ReadToEndAsync()' in runner)
check('runner order prepare tests build', runner.find("'PREPARE_PROJECT'") < runner.find("'UNIT TESTS'") < runner.find("'ASSEMBLE DEBUG'"))
check('runner active package after build', runner.find("'ASSEMBLE DEBUG'") < runner.find("'ACTIVE PACKAGE'"))
check('runner requires exactly one adb device', '$devices.Count -ne 1' in runner)
check('runner rejects unauthorized adb', 'unauthorized' in runner and 'FAIL_UNAUTHORIZED' in runner)
check('runner installs with adb -r', "@('-s', $serial, 'install', '-r', $apkTarget)" in runner)
check('runner version 0.5.18.3', "$version = '0.5.18.3'" in runner)
check('active pack still excludes base paths', all(x in pack for x in [
    'docs/project/analyzer_v0.7/assets/',
    'docs/project/analyzer_v0.7/references/',
    'docs/project/handoff_assets/',
    'docs/project/FILTERBANK_UI_REFERENCE/',
    'docs/reconstruction/',
    'supabase/storage_backup/'
]))
check('base R1 reference unchanged', 'LFN-Scout_BASE_ASSETS_R1.zip' in base_req and re.search(r'SHA-256:\s*[0-9a-f]{64}', base_req))
check('workflow documents 0.5.18.3 fix', 'v0.5.18.3' in workflow and 'System.Diagnostics.Process' in workflow)
check('status documents 0.5.18.3', 'Projektstatus v0.5.18.3' in status and 'versionCode 164' in status)
check('changelog documents NativeCommandError fix', changelog.startswith('# v0.5.18.3') and 'NativeCommandError' in changelog)
check('new testplan exists', (root/'docs'/'project'/'TESTPLAN_v0.5.18.3.md').exists())
check('new build verification exists', (root/'BUILD_VERIFICATION_v0.5.18.3.md').exists())

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} PASS')
if failed:
    raise SystemExit('FAILED: ' + '; '.join(failed))
