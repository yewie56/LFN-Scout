"""Static integration checks (NOT a substitute for Gradle or device testing)."""
from pathlib import Path
import json
import sys

r = Path(__file__).resolve().parent
checks = []

def check(label, passed):
    checks.append((label, bool(passed)))
    print(f"{'PASS' if passed else 'FAIL'}: {label}")

def source(relative):
    return (r / relative).read_text(encoding='utf-8')

core = 'analyzer-core/src/main/kotlin/de/brummbeobachter/app/'
app = 'app/src/main/java/de/brummbeobachter/app/'
check('actual v0.4.90/130 baseline', 'versionName = "0.4.90"' in source('app/build.gradle.kts') and 'versionCode = 130' in source('app/build.gradle.kts'))
check('unchanged applicationId', 'applicationId = "de.brummbeobachter.app"' in source('app/build.gradle.kts'))
check('one shared JVM library wired', 'include(":app", ":analyzer-core")' in source('settings.gradle.kts') and 'implementation(project(":analyzer-core"))' in source('app/build.gradle.kts'))
check('single canonical analyzer core', (r / (core + 'LiveAnalyzerCore.kt')).is_file() and not (r / (app + 'LiveAnalyzerCore.kt')).exists())
check('capture arbiter used by both recorder paths', 'MicCaptureArbiter.tryAcquire("rolling")' in source(app + 'RollingAudioService.kt') and 'MicCaptureArbiter.tryAcquire("direct_recording")' in source(app + 'AudioRecorder.kt'))
check('PCM fan-out used by both recorder paths', 'AnalyzerSessionGateway.pcm.publish(' in source(app + 'RollingAudioService.kt') and 'AnalyzerSessionGateway.pcm.publish(' in source(app + 'AudioRecorder.kt'))
check('existing WAV format invariant', 'SOURCE_SAMPLE_RATE_HZ = 8000' in source(app + 'AudioRecordingPolicy.kt') and 'TARGET_SAMPLE_RATE_HZ = 1000' in source(app + 'AudioRecordingPolicy.kt') and 'DURATION_SECONDS = 120' in source(app + 'AudioRecordingPolicy.kt'))
check('legacy app test command retained', './gradlew testDebugUnitTest --stacktrace' in source('.github/workflows/build-android.yml') and 'dependsOn(":analyzer-core:test")' in source('app/build.gradle.kts'))
check('no .git in project tree', not any(p.name == '.git' for p in r.rglob('*')))
manifest = json.loads(source('docs/reconstruction/project_manifest.json'))
check('manifest and new official docs synced', manifest.get('analyzer_development', {}).get('phase') == 2 and (r / 'docs/project/ANALYZER_PHASE2_AUDIO_STANDALONE.md').is_file())
check('private recovery files retained', len(list((r / 'supabase/storage_backup').rglob('*.wav'))) == 53)
if not all(p for _, p in checks):
    sys.exit('Phase 2 static verification FAILED')
print(f'PASS: {len(checks)}/{len(checks)} static integration checks (no Android runtime implied)')
