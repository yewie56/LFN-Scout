"""Static integration gates for v0.4.89. This does not replace Gradle or device tests."""
from pathlib import Path
import json,sys,hashlib
p=Path(__file__).resolve().parent
def read(x): return (p/x).read_text(encoding='utf-8')
gradle=read('app/build.gradle.kts')
ui=read('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt')
session=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/HighspeedImpulseSession.kt')
det=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/HighspeedImpulseDetector.kt')
settings=read('analyzer-core/src/main/kotlin/de/brummbeobachter/app/ImpulseSensitivity.kt')
refs=['app/src/main/java/de/brummbeobachter/app/AudioRecorder.kt','app/src/main/java/de/brummbeobachter/app/RollingAudioService.kt','app/src/main/java/de/brummbeobachter/app/AudioRecordingPolicy.kt','app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt','tools/prepare-java17.ps1','tools/configure-gradle-jdk17.ps1']
checks={
 'version 0.4.89 / 129': 'versionName = "0.4.89"' in gradle and 'versionCode = 129' in gradle,
 'app identity unchanged': 'applicationId = "de.brummbeobachter.app"' in gradle,
 'JDK-17 toolchain and module preserved': 'jvmToolchain(17)' in read('analyzer-core/build.gradle.kts'),
 'no second microphone': 'AudioRecord(' not in ui and 'AudioRecord(' not in session,
 'raw PCM consumers only': 'AnalyzerSessionGateway.rawPcm.subscribe' in ui and 'AnalyzerSessionGateway.startImpulseDetector' in ui,
 'immutable per-frame sensitivity': 'val settings = sensitivity' in session and 'ImpulseSensitivity' in session,
 'fixed threshold mapped before capture gain': 'settings.fixedThresholdRawPcm()' in session,
 'adaptive factor adjustable': 'detector?.thresholdOverNoise = settings.noiseMultiplier' in session,
 'adaptive noise remains enabled': 'thresholdOverNoise * noiseAbs' in det,
 'preset options complete': all(f'{name}("' in settings for name in ('ROBUST','NORMAL','SENSITIVE')),
 'three sliders': all(t in ui for t in ('valueRange = 0f..50f','valueRange = 50f..3000f','valueRange = 1.5f..12f')),
 'settings persisted app-private': 'getSharedPreferences(' in ui and 'MODE_PRIVATE' in ui and 'putInt("min_peak_pcm"' in ui,
 'raw and adaptive threshold telemetry': all(t in ui for t in ('snapshot.noiseRaw','snapshot.adaptiveThresholdRaw','snapshot.thresholdRaw','snapshot.fixedThresholdRaw')),
 'clipboard no external export': 'ClipData.newPlainText' in ui and 'appendLine("Standort' not in ui and 'appendLine("Quelle' not in ui and 'http://' not in ui and 'https://' not in ui,
 'unit tests and selftest extended': 'quietSeriesFiveEventsThenSilence' in read('analyzer-core/src/test/kotlin/de/brummbeobachter/app/HighspeedImpulseDetectorTest.kt') and 'configurable sensitive session' in read('tools/analyzer_highspeed_selftest.kt'),
 'new docs and changelog': (p/'docs/project/ANALYZER_PHASE2F_SCHWELLE_v0.4.89.md').exists() and '## v0.4.89' in read('CHANGELOG.md'),
 'no .git included': not list(p.rglob('.git')),
 '53 recovery WAV files': len(list((p/'supabase/storage_backup').rglob('*.wav')))==53,
 'Recovery WAV exact bytes': sum(f.stat().st_size for f in (p/'supabase/storage_backup').rglob('*.wav'))==12722332,
}
for k,v in checks.items():print(('PASS' if v else 'FAIL')+': '+k)
if not all(checks.values()):sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} v0.4.89 integration checks. Android build/device NOT RUN.')
