"""Static highspeed integration assertions. No assertion about device/Android runtime."""
from pathlib import Path
import json
p=Path(__file__).resolve().parent
core=p/'analyzer-core/src/main/kotlin/de/brummbeobachter/app'
app=p/'app/src/main/java/de/brummbeobachter/app'
def read(path): return path.read_text(encoding='utf-8')
checks={
 'baseline 0.4.90/130': 'versionName = "0.4.90"' in read(p/'app/build.gradle.kts') and 'versionCode = 130' in read(p/'app/build.gradle.kts'),
 'unchanged app ID': 'applicationId = "de.brummbeobachter.app"' in read(p/'app/build.gradle.kts'),
 'legacy WAV policy 8k to 1k 120s': all(s in read(app/'AudioRecordingPolicy.kt') for s in ('SOURCE_SAMPLE_RATE_HZ = 8000','TARGET_SAMPLE_RATE_HZ = 1000','DURATION_SECONDS = 120')),
 'only shared raw hub': 'val rawPcm = AnalyzerPcmHub()' in read(app/'AnalyzerSessionGateway.kt'),
 'rolling pre-decimation raw publish': read(app/'RollingAudioService.kt').index('AnalyzerSessionGateway.rawPcm.publish(') < read(app/'RollingAudioService.kt').index('sum += input[i].toLong()'),
 'direct pre-decimation raw publish': read(app/'AudioRecorder.kt').index('AnalyzerSessionGateway.rawPcm.publish(') < read(app/'AudioRecorder.kt').index('groupSum += input[index].toLong()'),
 'pre-gain metadata 0 in both producers': all(', 0, firstSampleNs)' in read(app/f) for f in ('AudioRecorder.kt','RollingAudioService.kt')),
 'real sample-rate metadata in both producers': all('publish(input, count, sourceRate' in read(app/f) for f in ('AudioRecorder.kt','RollingAudioService.kt')),
 'standalone reusable JVM sources': all((core/f).is_file() for f in ('HighspeedImpulseDetector.kt','HighspeedImpulseSession.kt')),
 'new JVM and Junit regression': (p/'tools/analyzer_highspeed_selftest.kt').is_file() and (p/'analyzer-core/src/test/kotlin/de/brummbeobachter/app/HighspeedImpulseDetectorTest.kt').is_file(),
 'docs and manifest synchronised': (p/'docs/project/ANALYZER_PHASE2_HIGHSPEED.md').is_file() and json.loads(read(p/'docs/reconstruction/project_manifest.json'))['analyzer_development']['highspeed']['actual_android_48k_enabled'] is False,
 'private recovery WAV count preserved': len(list((p/'supabase/storage_backup').rglob('*.wav'))) == 53,
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL')+': '+name)
assert all(checks.values()), 'highspeed integration static verification failed'
print(f'PASS: {len(checks)}/{len(checks)} static checks. Device testing remains NOT RUN.')
