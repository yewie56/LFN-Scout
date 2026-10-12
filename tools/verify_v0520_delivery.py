#!/usr/bin/env python3
from pathlib import Path
import re, sys
root=Path(__file__).resolve().parents[1]
checks=[]
def chk(name, cond):
    checks.append((name,bool(cond)))

gradle=(root/'app/build.gradle.kts').read_text()
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text()
live=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
water=(root/'app/src/main/java/de/brummbeobachter/app/WaterfallGenerator.kt').read_text()
repo=(root/'app/src/main/java/de/brummbeobachter/app/LocalAudioStatisticsRepository.kt').read_text()
dialog=(root/'app/src/main/java/de/brummbeobachter/app/AudioCommentDialog.kt').read_text()
vortest=(root/'tools/run-vortest.ps1').read_text() if (root/'tools/run-vortest.ps1').exists() else ''

chk('versionName 0.5.20', 'versionName = "0.5.20"' in gradle)
chk('versionCode 167', 'versionCode = 167' in gradle)
chk('shared comment dialog exists', 'internal fun AudioCommentDialog(' in dialog)
chk('observer uses shared comment dialog', 'recordingId = currentPending.recordingId' in main and 'AudioCommentDialog(' in main)
chk('analyzer uses shared comment dialog', 'recordingId = "analyzer-${analyzerPendingClip.endEpochMs}"' in live)
chk('analyzer freezes audio before dialog', live.find('LocalAnalyzerAudioBuffer.take(') < live.find('pendingAudioClip = clip') and 'Keyboard/tap sounds therefore cannot enter this saved clip' in live)
chk('analyzer comment uses observer timeouts', 'audioNoCommentTimeoutSeconds = reminderSettings.audioNoCommentTimeoutSeconds' in main and 'audioCommentTimeoutSeconds = reminderSettings.audioCommentTimeoutSeconds' in main)
chk('analyzer comment stored in json', 'put("comment", comment?.take(AudioArchive.MAX_COMMENT_LENGTH)' in repo)
chk('analyzer save accepts comment', re.search(r'suspend fun saveAnalyzer\([\s\S]*?comment: String\?\)', repo) is not None)
chk('audio action info is two-line', '/${localSaveConfig.durationSeconds}s\\n${dailyCount}/${dailyLimit}' in live)
chk('observer main setup button removed', 'Text("Audio / Statistik: ${localSaveConfig.durationSeconds}' not in main)
chk('observer setup moved to settings', 'Text("Audio / Statistik · ${localSaveConfig.durationSeconds} s' in main)
chk('developer label in title row', 'developerModeEnabled: Boolean' in main and '"ENTWICKLERMODUS"' in main and 'modifier = Modifier.weight(1f)' in main)
# only one visible ImpulseDiagnosticCard invocation and it must be after Info section
imp_positions=[m.start() for m in re.finditer(r'ImpulseDiagnosticCard\(\)',main)]
info_pos=main.find('title = "Info"')
chk('highspeed diagnostic only once', len(imp_positions)==1)
chk('highspeed diagnostic is bottom', len(imp_positions)==1 and imp_positions[0] > info_pos)
chk('highspeed diagnostic developer-only', 'if (developerModeEnabled) {\n                Spacer(Modifier.height(FaceliftLayoutPolicy.SECTION_GAP_DP.dp))\n                ImpulseDiagnosticCard()' in main)
chk('android system back handler', 'BackHandler(enabled = showDebugLog || currentPanel != AppPanel.MAIN)' in main)
chk('navigation history stack', 'val panelHistory = remember { mutableListOf<AppPanel>() }' in main and 'fun navigateBack()' in main)
chk('main navigation resets history', 'panelHistory.clear()' in main)
chk('waterfall no -120 data floor', 'const val LEVEL_HARD_MIN_DB = -240.0' in water)
chk('waterfall keeps compensated FFT values', '20.0 * log10(max(amplitude, 1.0e-12))' in water)
chk('waterfall auto/manual caption correct', 'if (levelAuto) "automatisch" else "manuell"' in water)
chk('color normalization clamps only for rendering', 'value - minDb' in water and '.coerceIn(\n                                0.0,\n                                1.0' in water)
chk('ADB wait remains present', 'Warte auf genau ein freigegebenes Android-Geraet' in vortest)

failed=[n for n,v in checks if not v]
for i,(n,v) in enumerate(checks,1): print(f'{i:02d} {"PASS" if v else "FAIL"} {n}')
print(f'\n{len(checks)-len(failed)}/{len(checks)} PASS')
if failed:
    print('Failed:', ', '.join(failed)); sys.exit(1)
