from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
checks = []

def check(name, ok):
    checks.append((name, bool(ok)))
    print(f"{'PASS' if ok else 'FAIL'}: {name}")

build = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
vortest = (ROOT / 'VORTEST.bat').read_text(encoding='utf-8')
prepare = (ROOT / 'PREPARE_PROJECT.bat').read_text(encoding='utf-8')
runner = (ROOT / 'tools/run-vortest.ps1').read_text(encoding='utf-8')
packer = (ROOT / 'tools/package-active.ps1').read_text(encoding='utf-8')

check('versionName 0.5.22', 'versionName = "0.5.22"' in build)
check('versionCode 169', 'versionCode = 169' in build)
check('VORTEST no inline PowerShell in for/f', not re.search(r'for\s+/f.*powershell', vortest, re.I))
check('VORTEST no LFN_VERSION dependency', 'LFN_VERSION' not in vortest)
check('PREPARE no LFN_VERSION dependency', 'LFN_VERSION' not in prepare)
check('runner reads build.gradle.kts', "app\\build.gradle.kts" in runner and 'versionName' in runner)
check('packer reads build.gradle.kts', "app\\build.gradle.kts" in packer and 'versionName' in packer)
check('VORTEST_INSTALL calls VORTEST --install', 'call VORTEST.bat --install' in (ROOT / 'VORTEST_INSTALL.bat').read_text(encoding='utf-8'))

failed = [name for name, ok in checks if not ok]
print(f"\n{len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    raise SystemExit(1)
