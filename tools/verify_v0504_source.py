#!/usr/bin/env python3
"""Static source-contract checks, NOT a build/device or runtime state-machine test."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
app=root/'app/src/main/java/de/brummbeobachter/app'
read=lambda n:(app/n).read_text(encoding='utf-8')
mode=read('AnalyzerModeCoordinator.kt')
ctrl=read('LiveAnalyzerController.kt')
ui=read('LiveAnalyzerScreen.kt')
settings=read('LiveAnalyzerSettingsPanel.kt')
service=read('AnalyzerDirectAudioService.kt')
test=read('AnalyzerTestSignal.kt')
main=read('MainActivity.kt')
checks={
    'versionName 0.5.04.1': 'versionName = "0.5.04.1"' in (root/'app/build.gradle.kts').read_text(),
    'versionCode 146': 'versionCode = 146' in (root/'app/build.gradle.kts').read_text(),
    'same applicationId': 'applicationId = "de.brummbeobachter.app"' in (root/'app/build.gradle.kts').read_text(),
    'direct mic service declared': 'android:name=".AnalyzerDirectAudioService"' in (root/'app/src/main/AndroidManifest.xml').read_text(),
    'capture exclusive 48k': 'tryAcquire("analyzer_48k")' in service and 'AudioRecord(source, 48000' in service,
    'direct sends only analyzer raw hub': 'rawPcm.publish(' in service and 'RollingAudioEngine' not in service,
    'mode starts save original Rolling pref': 'originalRolling = store.settingsFlow.first().rollingBufferEnabled' in mode,
    '48k mode disables Rolling': 'store.setRollingBufferEnabled(false)' in mode and 'RollingAudioService.requestStop(context)' in mode,
    'STOP never restores': 'fun stopMeasurement(context: Context)' in mode and 'LiveAnalyzerController.stop()' in mode.split('fun stopMeasurement(context: Context)')[1].split('suspend fun restartMeasurement')[0] and 'restore(' not in mode.split('fun stopMeasurement(context: Context)')[1].split('suspend fun restartMeasurement')[0],
    'restore only after STOP and leave': 'if (LiveAnalyzerController.snapshot().running) return false' in mode and 'suspend fun finishStopped' in mode,
    '48k stopped then rolling restore': 'LiveAnalyzerController.configureMode(context, false)' in mode and 'store.setRollingBufferEnabled(originalRolling)' in mode,
    'back while running does not restore': 'if (state.running) { onBack(); return }' in ui,
    'recovery hook': 'recoverAfterProcessDeath(appContext)' in main,
    'wideband defaults 0–22k FFT8192': 'putInt("mode_48_range_hz", 22000)' in ctrl and 'putInt("mode_48_fft_size", 8192)' in ctrl,
    'separate 48k setup slot': 'setup_4_name' in ctrl and '(if (state.direct48k) 4..4 else 1..3)' in settings,
    'image confirmation asynchronous': 'status = "Bild wird gespeichert …"' in ctrl and 'Bild gespeichert:' in ui,
    'test generator never routes WAV': 'rawPcm.publish(' in test and 'RollingAudioEngine' not in test and 'AudioRecorder' not in test,
    'test banner displayed': 'TESTSIGNAL · KEINE MESSUNG' in ui and 'TESTSIGNAL · KEINE MESSUNG' in ctrl,
    'history preserved on mode switch': 'modeBoundaryRows = if (history.isEmpty()) null else 0' in ctrl and 'fun setWaterfallMode' in ctrl,
    'history max 14400': 'MAX_HISTORY_SECONDS = 14400' in (root/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text(),
}
for name,passed in checks.items(): print(('PASS' if passed else 'FAIL') + ': ' + name)
print(f'STATIC SOURCE CONTRACT: {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
