from pathlib import Path
root=Path(__file__).resolve().parents[1]
pkg=root/'app/src/main/java/de/brummbeobachter/app'
checks={
 'version': 'versionCode = 174' in (root/'app/build.gradle.kts').read_text(),
 'name': 'versionName = "0.5.27"' in (root/'app/build.gradle.kts').read_text(),
 'upload_origin': 'put("capture_origin", captureOrigin)' in (pkg/'SupabaseClient.kt').read_text(),
 'upload_trigger': 'put("trigger_type", triggerType)' in (pkg/'SupabaseClient.kt').read_text(),
 'api_stats': 'submit_audio_statistics' in (pkg/'SupabaseClient.kt').read_text(),
 'index_conversion': 'band.optInt("index", -1) + 1' in (pkg/'AudioStatisticsCloud.kt').read_text(),
 'metric_name': '"valid_epochs"' in (pkg/'AudioStatisticsCloud.kt').read_text(),
 'status': 'Lokal gespeichert · Cloud ok' in (pkg/'LiveAnalyzerScreen.kt').read_text(),
 'analyzer_hook': 'AudioStatisticsCloud.submitExisting(' in (pkg/'MainActivity.kt').read_text(),
}
for k,v in checks.items():print(('PASS' if v else 'FAIL'),k)
assert all(checks.values())
print('PASS static source checks only; no compile or device claim')
