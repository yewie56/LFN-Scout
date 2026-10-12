from pathlib import Path

root = Path(__file__).resolve().parent
errors = []

def need(path, text):
    data = (root / path).read_text(encoding='utf-8')
    if text not in data:
        errors.append(f"{path}: fehlt {text!r}")

need('app/build.gradle.kts', 'versionCode = 100')
need('app/build.gradle.kts', 'versionName = "0.4.60"')
need('app/build.gradle.kts', 'testImplementation("org.json:json:')
need('app/src/main/res/values/strings.xml', '<string name="app_name">LFN-Scout</string>')
need('.github/workflows/build-android.yml', 'name: Build LFN-Scout')
need('.github/workflows/build-android.yml', 'LFN-Scout-v0.4.60-debug.apk')
need('.github/workflows/build-android.yml', 'Upload unit test report on failure')
need('app/src/test/java/de/brummbeobachter/app/FaceliftThemePolicyTest.kt', 'assertEquals(7, FaceliftLayoutPolicy.SCREEN_PADDING_DP)')
need('app/src/test/java/de/brummbeobachter/app/ReminderScheduleGridPolicyTest.kt', 'fun interval_30_minutes_uses_grid()')

reminder = (root / 'app/src/test/java/de/brummbeobachter/app/ReminderScheduleGridPolicyTest.kt').read_text(encoding='utf-8')
if 'fun interval_30_minutes_remains_relative()' in reminder:
    errors.append('ReminderScheduleGridPolicyTest: widersprüchlicher alter 30-Minuten-Test noch vorhanden')

if errors:
    print('FAIL v0.4.60 requirements')
    for e in errors:
        print('-', e)
    raise SystemExit(1)
print('PASS v0.4.60 requirements')
