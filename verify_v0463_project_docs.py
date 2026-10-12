from pathlib import Path
import re

root = Path(__file__).resolve().parent
checks = []

def check(name, cond):
    checks.append((name, bool(cond)))

build = (root / 'app/build.gradle.kts').read_text(encoding='utf-8')
check('versionName 0.4.63', 'versionName = "0.4.63"' in build)
check('versionCode 103', 'versionCode = 103' in build)
check('docs/project exists', (root / 'docs/project').is_dir())
check('project README exists', (root / 'docs/project/README.md').is_file())
check('changes MD exists', (root / 'docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md').is_file())
check('planned extensions MD exists', (root / 'docs/project/LFN-Scout_Geplante_Erweiterungen.md').is_file())
check('superpowers plans retained', (root / 'docs/superpowers/plans').is_dir())
check('superpowers specs retained', (root / 'docs/superpowers/specs').is_dir())
check('.git absent from package tree', not any(p.name == '.git' for p in root.rglob('*')))

for name, ok in checks:
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")

failed = [name for name, ok in checks if not ok]
if failed:
    raise SystemExit('FAILED: ' + ', '.join(failed))
print(f'PASS: {len(checks)}/{len(checks)} checks')
