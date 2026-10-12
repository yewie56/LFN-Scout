# HISTORISCH: nur v0.4.90, bewusst keine aktuelle Freigabe für v0.4.91.
"""Static integration gate for v0.4.90; not a substitute for Android/phone tests."""
from pathlib import Path
import hashlib
import json
import sys
root = Path(__file__).resolve().parent
r = lambda p: (root / p).read_text(encoding='utf-8')
g = r('app/build.gradle.kts')
main = r('app/src/main/java/de/brummbeobachter/app/MainActivity.kt')
ui = r('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
ctl = r('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
gw = r('app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt')
old = r('app/src/main/java/de/brummbeobachter/app/ImpulseDiagnosticCard.kt')
doc = r('docs/project/ANALYZER_LIVE_v0.4.90.md')
checks = {
    'version 0.4.90 / 130 / stable identity': 'versionName = "0.4.90"' in g and 'versionCode = 130' in g and 'applicationId = "de.brummbeobachter.app"' in g,
    'existing recorder WAV rate 1000 and source 8000': all(x in r('app/src/main/java/de/brummbeobachter/app/AudioRecordingPolicy.kt') for x in ('SOURCE_SAMPLE_RATE_HZ = 8000', 'TARGET_SAMPLE_RATE_HZ = 1000')),
    'real new panel routed through extras': 'currentPanel == AppPanel.ANALYZER' in main and 'onOpenAnalyzer = { currentPanel = AppPanel.ANALYZER }' in main and 'AppPanel.ANALYZER' in r('app/src/test/java/de/brummbeobachter/app/AppPanelTest.kt'),
    'shared PCM only / no second microphone': 'AnalyzerSessionGateway.start(::accept)' in ctl and 'AudioRecord(' not in ctl and 'AudioRecord(' not in ui,
    'LF 20/50/100/200 1k FFT 4096': 'intArrayOf(20, 50, 100, 200)' in ctl and 'FFT_SIZE = 4096' in ctl and 'RATE_HZ = 1000' in ctl and 'maxFrequencyHz = maxFrequencyHz' in gw,
    'bounded real spectrum waterfall': 'history.addLast(s.liveDbFs.copyOf())' in ctl and 'while (history.size > HISTORY_ROWS)' in ctl and 'makeWaterfall(rows, state.frequencyHz, range)' in ui,
    'UI shows actual state/no dummy generated image': 'trace(state.live' in ui and 'trace(state.average' in ui and 'state.l50DbFs' in ui,
    'PNG independent from composable': 'fun saveNow()' in ctl and 'private fun renderPng(' in ctl and 'MediaStore.Downloads.EXTERNAL_CONTENT_URI' in ctl and 'getExternalFilesDir' in ctl,
    'auto PNG only when full waterfall': 'history.size >= HISTORY_ROWS' in ctl and '120_000_000_000L' in ctl,
    'save only PNG, no WAV or cloud': 'Bitmap.CompressFormat.PNG' in ctl and not any(t in ctl for t in ('WavPcm16', 'SupabaseClient', '.wav', 'uploadWav')),
    'setup slots and explicit unfinished placeholders': 'listOf("1","2","3")' in ui and 'noch nicht implementiert' in ui and 'Mithören: später' in ui,
    'previous impulse developer path left intact': 'ImpulseDiagnosticCard()' in main and 'AnalyzerSessionGateway.rawPcm.subscribe' in old,
    'updated project documentation': '0.4.90' in r('README.md') and 'v0.4.90' in r('CHANGELOG.md') and 'NOT RUN' in doc,
    'same 13 original accepted concept images': len(list((root/'docs/project/analyzer_v0.7/assets/concepts').glob('C*.png'))) == 13,
    '53 private recovery WAV files unchanged': len(list((root/'supabase/storage_backup').rglob('*.wav'))) == 53 and sum(f.stat().st_size for f in (root/'supabase/storage_backup').rglob('*.wav')) == 12_722_332,
    'no git or machine-local config in delivery': not list(root.rglob('.git')) and not (root/'local.properties').exists() and not (root/'.tools/java17.path').exists(),
}
for label, passed in checks.items(): print(('PASS' if passed else 'FAIL') + ': ' + label)
if not all(checks.values()): sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} static v0.4.90 checks; Android / Xiaomi NOT RUN')
