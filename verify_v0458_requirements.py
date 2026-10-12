from pathlib import Path
root=Path('.')
checks=[]
def ck(name, ok):
    checks.append((name, bool(ok)))
    print(('PASS' if ok else 'FAIL')+': '+name)

gradle=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
app_panel=(root/'app/src/main/java/de/brummbeobachter/app/AppPanel.kt').read_text(encoding='utf-8')
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
service_path=root/'app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt'
service=service_path.read_text(encoding='utf-8') if service_path.exists() else ''
readme=(root/'README.md').read_text(encoding='utf-8')
changelog=(root/'CHANGELOG.md').read_text(encoding='utf-8')
prepare=(root/'PREPARE_PROJECT.bat').read_text(encoding='utf-8')

app_panel_test=(root/'app/src/test/java/de/brummbeobachter/app/AppPanelTest.kt').read_text(encoding='utf-8')
seismic_policy_test=(root/'app/src/test/java/de/brummbeobachter/app/SeismicStationPolicyTest.kt').read_text(encoding='utf-8')
waterfall_busy_test=(root/'app/src/test/java/de/brummbeobachter/app/WaterfallBusyIndicatorContractTest.kt').read_text(encoding='utf-8')

ck('version 0.4.58', 'versionName = "0.4.58"' in gradle and 'versionCode = 98' in gradle)
ck('SEISMIC panel enum', 'SEISMIC' in app_panel)
ck('seismic service exists', service_path.exists())
ck('GEOFON station service', 'geofon.gfz.de/fdsnws/station/1/query' in service)
ck('GEOFON live vertical PNG', 'geofon.gfz.de/waveform/data/' in service)
ck('GPS nearest station distance', 'haversineKm' in service and 'latitude' in service and 'longitude' in service)
ck('network timeouts', 'connectTimeout' in service and 'readTimeout' in service)
ck('timeout log', 'SEISMIC_TIMEOUT' in service)
ck('unavailable log', 'SEISMIC_UNAVAILABLE' in service)
ck('http error log', 'SEISMIC_HTTP_ERROR' in service)
ck('success log', 'SEISMIC_IMAGE_OK' in service and 'SEISMIC_STATION_SELECTED' in service)
ck('seismic screen', 'private fun SeismicScreen(' in main and 'Seismik' in main)
ck('seismic refresh', 'onRefresh' in main and 'SEISMIC_LOAD_START' in service)
ck('seismic source label', 'GEOFON' in main and 'vertikal' in main.lower())
ck('waterfall per-record busy id', 'generatingWaterfallRecordingId' in main)
ck('four panels regression', 'AppPanel.SEISMIC' in app_panel_test and 'exactly_four_panels_exist' in app_panel_test)
ck('seismic station policy tests', 'station_text_is_sorted_by_distance_from_gps' in seismic_policy_test and 'malformed_station_lines_are_skipped' in seismic_policy_test)
ck('waterfall busy regression test', 'generatingWaterfallRecordingId == entry.recordingId' in waterfall_busy_test)
ck('README 0.4.58', readme.startswith('# LFN-Scout Android v0.4.58'))
ck('CHANGELOG 0.4.58', '## 0.4.58' in changelog)
ck('PREPARE_PROJECT 0.4.58', 'LFN-Scout v0.4.58 - Projekt vorbereiten' in prepare)

fails=[n for n,ok in checks if not ok]
print(f'\n{len(checks)-len(fails)}/{len(checks)} PASS')
raise SystemExit(1 if fails else 0)
