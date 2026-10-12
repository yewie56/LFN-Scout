"""Static integration contract; does not replace real Windows/Android tests."""
from pathlib import Path
import sys
p=Path(__file__).resolve().parent
def read(f): return (p/f).read_text(encoding='utf-8')
checks={
 'version 0.4.89/129': 'versionName = "0.4.89"' in read('app/build.gradle.kts') and 'versionCode = 129' in read('app/build.gradle.kts'),
 'no reserved PowerShell HOME assignment': '$home' not in read('tools/configure-gradle-jdk17.ps1').lower(),
 'project JDK has first priority': read('tools/prepare-java17.ps1').index('Try-JavaHome $localCurrent') < read('tools/prepare-java17.ps1').index('Try-JavaHome $cached'),
 'IDE JDK before stale cache': read('tools/prepare-java17.ps1').index('Android-Studio-JBR') < read('tools/prepare-java17.ps1').index('Try-JavaHome $cached'),
 'diagnostic only in developer mode': 'if (developerModeEnabled) {\n                ImpulseDiagnosticCard()' in read('app/src/main/java/de/brummbeobachter/app/MainActivity.kt'),
 'raw pre-decimation PCM': 'AnalyzerSessionGateway.rawPcm.subscribe' in read('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt'),
 'real detector on raw PCM': 'AnalyzerSessionGateway.startImpulseDetector' in read('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt'),
 'session closed when panel leaves': 'DisposableEffect(enabled)' in read('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt') and 'telemetry.stop()' in read('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt'),
 'legacy 8k capture unchanged': '8_000' in read('app/src/main/java/de/brummbeobachter/app/AudioRecorder.kt') or '8000' in read('app/src/main/java/de/brummbeobachter/app/AudioRecorder.kt'),
}
for name,valid in checks.items(): print(('PASS' if valid else 'FAIL')+': '+name)
if not all(checks.values()): sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} static diagnostic checks (Android tests not run).')
