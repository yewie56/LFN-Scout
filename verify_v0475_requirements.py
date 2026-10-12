from pathlib import Path
import sys

root = Path(__file__).resolve().parent
main_path = root / 'app/src/main/java/de/brummbeobachter/app/MainActivity.kt'
gradle_path = root / 'app/build.gradle.kts'
main = main_path.read_text(encoding='utf-8')
gradle = gradle_path.read_text(encoding='utf-8')

checks = []
def ck(name, cond):
    checks.append((name, bool(cond)))

ck('versionName 0.4.75', 'versionName = "0.4.75"' in gradle)
ck('versionCode 115', 'versionCode = 115' in gradle)

# Lightweight Kotlin brace scanner ignoring comments and strings.
stack = []
i = 0
line = 1
state = 'code'
extra_close = None
while i < len(main):
    c = main[i]
    n = main[i+1] if i + 1 < len(main) else ''
    if state == 'code':
        if c == '/' and n == '/':
            state = 'line'; i += 2; continue
        if c == '/' and n == '*':
            state = 'block'; i += 2; continue
        if main[i:i+3] == '"""':
            state = 'triple'; i += 3; continue
        if c == '"':
            state = 'string'; i += 1; continue
        if c == "'":
            state = 'char'; i += 1; continue
        if c == '{':
            stack.append(line)
        elif c == '}':
            if not stack:
                extra_close = line
                break
            stack.pop()
    elif state == 'line':
        if c == '\n': state = 'code'
    elif state == 'block':
        if c == '*' and n == '/': state = 'code'; i += 2; continue
    elif state == 'string':
        if c == '\\': i += 2; continue
        if c == '"': state = 'code'
    elif state == 'triple':
        if main[i:i+3] == '"""': state = 'code'; i += 3; continue
    elif state == 'char':
        if c == '\\': i += 2; continue
        if c == "'": state = 'code'
    if c == '\n':
        line += 1
    i += 1

ck('Kotlin braces balanced', extra_close is None and not stack)
ck('PanelHeader remains top-level after SeismicScreen', '\n}\n\n@Composable\nprivate fun PanelHeader(' in main)
ck('MoreFunctionsScreen present', '\n@Composable\nprivate fun MoreFunctionsScreen(' in main)
ck('LFN-Scout visible name retained', 'LFN-Scout' in main or 'LFN-Scout' in (root/'app/src/main/res/values/strings.xml').read_text(encoding='utf-8'))
ck('Landscape live plot retained', '.aspectRatio(SeismicPlotUiPolicy.LIVE_ASPECT_RATIO)' in main)
ck('Plot save retained', 'Text("Plot speichern")' in main)

fails = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL'), name)
print(f'{len(checks)-len(fails)}/{len(checks)} PASS')
sys.exit(1 if fails else 0)
