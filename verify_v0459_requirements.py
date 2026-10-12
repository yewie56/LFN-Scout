from pathlib import Path

root = Path(__file__).resolve().parent

def require(cond, msg):
    if not cond:
        raise AssertionError(msg)
    print(f"PASS: {msg}")

build = (root / 'app' / 'build.gradle.kts').read_text(encoding='utf-8')
require('versionName = "0.4.59"' in build, 'versionName 0.4.59')
require('versionCode = 99' in build, 'versionCode 99')

workflow_path = root / '.github' / 'workflows' / 'build-android.yml'
require(workflow_path.exists(), 'GitHub Actions workflow exists')
workflow = workflow_path.read_text(encoding='utf-8')
for needle, label in [
    ('workflow_dispatch:', 'manual workflow start'),
    ('push:', 'push trigger'),
    ('pull_request:', 'pull request trigger'),
    ('actions/checkout@v4', 'checkout action'),
    ('actions/setup-java@v4', 'Java setup action'),
    ('java-version: \'17\'', 'Java 17'),
    ('./gradlew testDebugUnitTest', 'unit tests in CI'),
    ('./gradlew assembleDebug', 'debug APK build in CI'),
    ('actions/upload-artifact@v4', 'APK artifact upload'),
    ('app/build/outputs/apk/debug/*.apk', 'debug APK artifact path'),
]:
    require(needle in workflow, label)

require((root / 'GITHUB_BUILD.md').exists(), 'GitHub build documentation exists')
readme = (root / 'README.md').read_text(encoding='utf-8')
require('# LFN-Scout Android v0.4.59' in readme, 'README identifies v0.4.59')
require('GitHub Actions' in readme, 'README mentions GitHub Actions')
changelog = (root / 'CHANGELOG.md').read_text(encoding='utf-8')
require('## 0.4.59' in changelog, 'CHANGELOG contains v0.4.59')
print('ALL v0.4.59 STATIC CHECKS PASSED')
