"""LFN-Scout v0.4.89 portable JDK/Android-SDK setup contract. Static only.

Does not execute Windows PowerShell or claim Android Studio Sync success.
"""
from pathlib import Path
import json
import re
import sys

ROOT = Path(__file__).resolve().parent
errors = []

def check(name: str, condition: bool):
    print(('PASS' if condition else 'FAIL') + ': ' + name)
    if not condition:
        errors.append(name)

def src(name):
    return (ROOT / name).read_text(encoding='utf-8')

app = src('app/build.gradle.kts')
prep = src('PREPARE_PROJECT.bat')
jdk = src('tools/configure-gradle-jdk17.ps1')
finder = src('tools/prepare-java17.ps1')
sdk = src('tools/configure-android-sdk.ps1')
props = src('gradle.properties')
check('version 0.4.89 / code 129', 'versionName = "0.4.89"' in app and 'versionCode = 129' in app)
check('stable Android applicationId', 'applicationId = "de.brummbeobachter.app"' in app)
check('analyzer Kotlin JVM 17 toolchain retained', 'jvmToolchain(17)' in src('analyzer-core/build.gradle.kts'))
check('prepare runs JDK, daemon+toolchain config before Gradle',
    prep.index('prepare-java17.ps1') < prep.index('configure-gradle-jdk17.ps1') < prep.index('gradlew.bat --version'))
check('prepare configures Android SDK on this machine',
    prep.index('configure-android-sdk.ps1') < prep.index('gradlew.bat --version'))
check('JDK candidate is a complete JDK', 'javac.exe' in finder and 'javac.exe' in jdk)
check('JDK executable version checked', 'java\\.version' in jdk and 'javac\\s+17' in jdk)
check('daemon and toolchain properties both configured',
    'org.gradle.java.home=' in jdk and 'org.gradle.java.installations.paths=' in jdk)
check('project properties preserve unrelated settings and use forward slashes',
    'Where-Object' in jdk and "$jdkHome.Replace('\\','/')" in jdk)
check('idempotent JDK configuration checks duplicate paths',
    'matching.Count -ne 1' in jdk)
# The verifier must pass both BEFORE PREPARE (portable ZIP) and AFTER PREPARE
# (local, uncommitted JDK/SDK paths configured for this exact Windows PC).
cache = ROOT / '.tools/java17.path'
if cache.exists():
    jdk_home = cache.read_text(encoding='utf-8').splitlines()[0].strip().replace('\\', '/')
    jdk_home = jdk_home.rstrip('/')
    expected_keys = ('org.gradle.java.home', 'org.gradle.java.installations.paths')
    prop_lines = props.splitlines()
    jdk_props_ok = bool(jdk_home) and all(
        prop_lines.count(key + '=' + jdk_home) == 1 for key in expected_keys
    ) and all(sum(1 for line in prop_lines if line.startswith(key + '=')) == 1
              for key in expected_keys)
    check('prepared local JDK configuration matches .tools/java17.path', jdk_props_ok)
else:
    check('portable ZIP has no machine-specific JDK or SDK paths',
        'org.gradle.java.home=' not in props
        and 'org.gradle.java.installations.paths=' not in props
        and not (ROOT / 'local.properties').exists())
check('existing Gradle launcher and CI entrypoint retained',
    'call "%GRADLE_EXE%" %*' in src('gradlew.bat')
    and 'testDebugUnitTest' in src('.github/workflows/build-android.yml')
    and 'assembleDebug' in src('.github/workflows/build-android.yml'))
check('Android SDK setup is local and optional when missing',
    'local.properties' in sdk and 'ANDROID_HOME' in sdk and 'Android Studio' in sdk)
check('documentation updated',
    (ROOT / 'docs/project/JDK17_ANDROID_STUDIO_v0.4.86.md').is_file()
    and '0.4.89' in src('CHANGELOG.md') and '0.4.89' in src('BUILD_WORKFLOWS.md'))
check('reconstruction manifests updated',
    json.loads(src('docs/reconstruction/project_manifest.json'))['version_code'] == 129
    and json.loads(src('docs/reconstruction/build_environment.json'))['version'] == '0.4.89')
if errors:
    print('FAIL: ' + ', '.join(errors))
    sys.exit(1)
print('PASS: 15/15 static JDK/SDK setup checks; Windows IDE Sync still NOT RUN.')
