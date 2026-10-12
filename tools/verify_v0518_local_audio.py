#!/usr/bin/env python3
"""Static source/regression contract only; not an Android compile or device test."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
app=root/'app/src/main/java/de/brummbeobachter/app'
get=lambda f:(app/f).read_text(encoding='utf-8')
main=get('MainActivity.kt'); screen=get('LiveAnalyzerScreen.kt'); controller=get('LiveAnalyzerController.kt')
rec=get('AudioRecorder.kt'); stats=get('LocalAudioStatisticsRepository.kt')
config=get('LocalAudioSaveSettings.kt'); buffer=get('LocalAnalyzerAudioBuffer.kt')
engine=(root/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/StatisticsWindowEngine.kt').read_text()
version=(root/'app/build.gradle.kts').read_text()
assert re.search(r'versionCode\s*=\s*161\b',version) and 'versionName = "0.5.18"' in version
assert 'applicationId = "de.brummbeobachter.app"' in version
ns='{http://schemas.android.com/apk/res/android}'
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml').getroot()
activities=manifest.find('application').findall('activity')
for name in ['.MainActivity','.ReminderAlarmActivity']:
    assert any(x.attrib.get(ns+'name')==name and x.attrib.get(ns+'screenOrientation')=='portrait' for x in activities)
for icon in ['activity','blocks','settings','waves']:
    ET.parse(root/f'app/src/main/res/drawable/nav_{icon}.xml')
order=['Triple("Analyzer", R.drawable.nav_activity','Triple("Zusatzfunktionen", R.drawable.nav_blocks','Triple("Einstellungen", R.drawable.nav_settings','Triple("Seismik", R.drawable.nav_waves']
assert all(k in main for k in order) and list(map(main.index,order))==sorted(map(main.index,order))
assert 'onOpenAnalyzer = { currentPanel = AppPanel.ANALYZER }' in main
assert 'durationSeconds = saveConfig.durationSeconds' in main and 'LocalAudioStatisticsRepository.saveObserverSidecar(' in main
assert 'LocalAudioSaveSettings' in config and 'DEFAULT_SECONDS = 120' in config and 'MAX_SECONDS = 600' in config
assert 'durationSeconds' in rec and 'RollingAudioEngine.captureNext(' in rec
assert 'val recordingGain = AudioRecordingPolicy.gainFactor(gainDb)' in rec
assert 'LocalAnalyzerAudioBuffer.freeze(' in controller and 'LocalAnalyzerAudioBuffer.start()' in controller
assert 'received.liveDbFs.map { it - correction }' in controller
assert 'LocalAnalyzerAudioBuffer.take(' in screen and 'SAVE AUDIO' in screen and 'dailyCount < dailyLimit' in screen
assert 'frozen' in buffer and 'frame.sequence' in buffer and 'previousEndNs' in buffer
assert 'filterBands in 2..40' in engine and '10.0.pow(-gainDb / 20.0)' in engine
assert 'saveAnalyzer(' in stats and 'saveObserverSidecar(' in stats
assert '"OBSERVER"' in stats and '"ANALYZER"' in stats
assert '"NOT_SCHEDULED_LOCAL_ONLY"' in stats and '"NOT_IMPLEMENTED"' in stats
assert 'if (origin == "ANALYZER" && analyzerSetup != null)' in stats
assert (root/'docs/project/LOCAL_AUDIO_STATS_v0.5.18.md').exists()
assert (root/'docs/project/TESTPLAN_v0.5.18.md').exists()
print('PASS: v0.5.18 static navigation, vectors, portrait, buffer, quota, local WAV+stats, +Gain, 40 bands, docs, unchanged cloud contract intention')
