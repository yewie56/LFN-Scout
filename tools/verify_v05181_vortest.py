from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
checks = []

def check(name, cond):
    checks.append((name, bool(cond)))

build = (root / 'app' / 'build.gradle.kts').read_text(encoding='utf-8')
prepare = (root / 'PREPARE_PROJECT.bat').read_text(encoding='utf-8', errors='replace')
vortest = (root / 'VORTEST.bat').read_text(encoding='utf-8', errors='replace')
ps = (root / 'tools' / 'run-vortest.ps1').read_text(encoding='utf-8')

check('versionName 0.5.18.1', 'versionName = "0.5.18.1"' in build)
check('versionCode 162', 'versionCode = 162' in build)
check('prepare supports no-pause', '--no-pause' in prepare and ':maybe_pause' in prepare)
check('prepare legacy interactive pause retained', re.search(r'(?m)^pause\s*$', prepare) is not None)
check('vortest calls prepare', 'PREPARE_PROJECT.bat --no-pause' in ps)
check('vortest runs app unit tests', ':app:testDebugUnitTest --stacktrace' in ps)
check('vortest runs debug build', 'assembleDebug --stacktrace' in ps)
check('vortest verifies expected apk', 'app\\build\\outputs\\apk\\debug\\app-debug.apk' in ps)
check('vortest copies versioned apk', 'LFN-Scout_v0.5.18.1-debug.apk' in ps)
check('vortest writes result file', 'LAST_VORTEST_RESULT.txt' in ps)
check('vortest creates timestamped log', 'VORTEST_v0.5.18.1_' in ps)
check('batch returns automation exit code', 'exit /b %VORTEST_RC%' in vortest)

failed = [n for n, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'RESULT: {len(checks)-len(failed)}/{len(checks)} PASS')
raise SystemExit(1 if failed else 0)
