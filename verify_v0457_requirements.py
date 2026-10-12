from pathlib import Path
import sys

root = Path(__file__).resolve().parent
main = (root / "app/src/main/java/de/brummbeobachter/app/MainActivity.kt").read_text(encoding="utf-8")
settings = (root / "app/src/main/java/de/brummbeobachter/app/ReminderSettingsStore.kt").read_text(encoding="utf-8")
policy = (root / "app/src/main/java/de/brummbeobachter/app/CommentAutoSendPolicy.kt").read_text(encoding="utf-8")
water = (root / "app/src/main/java/de/brummbeobachter/app/WaterfallGenerator.kt").read_text(encoding="utf-8")
timep = (root / "app/src/main/java/de/brummbeobachter/app/WaterfallTimeAxisPolicy.kt").read_text(encoding="utf-8")
debug = (root / "app/src/main/java/de/brummbeobachter/app/DebugLogger.kt").read_text(encoding="utf-8")
service = (root / "app/src/main/java/de/brummbeobachter/app/RollingAudioService.kt").read_text(encoding="utf-8")
gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
readme = (root / "README.md").read_text(encoding="utf-8")
changelog = (root / "CHANGELOG.md").read_text(encoding="utf-8")
prepare = (root / "PREPARE_PROJECT.bat").read_text(encoding="utf-8")

checks=[]
def ck(name, cond):
    checks.append((name, bool(cond)))

# v0.4.56 regression baseline
ck("version 0.4.57", 'versionName = "0.4.57"' in gradle and 'versionCode = 97' in gradle)
ck("rating defaults 5/5", 'DEFAULT_RATING_NO_COMMENT_TIMEOUT_SECONDS = 5' in policy and 'DEFAULT_RATING_COMMENT_TIMEOUT_SECONDS = 5' in policy)
ck("audio defaults 60/10", 'DEFAULT_AUDIO_NO_COMMENT_TIMEOUT_SECONDS = 60' in policy and 'DEFAULT_AUDIO_COMMENT_TIMEOUT_SECONDS = 10' in policy)
ck("timeout range 1-300", 'MIN_TIMEOUT_SECONDS = 1' in policy and 'MAX_TIMEOUT_SECONDS = 300' in policy)
ck("timeouts persisted", all(k in settings for k in ['rating_no_comment_timeout_seconds','rating_comment_timeout_seconds','audio_no_comment_timeout_seconds','audio_comment_timeout_seconds','setCommentTimeouts']))
ck("rating uses settings", 'audioSettings.ratingCommentTimeoutSeconds' in main and 'audioSettings.ratingNoCommentTimeoutSeconds' in main)
ck("audio uses settings", 'settings.audioCommentTimeoutSeconds' in main and 'settings.audioNoCommentTimeoutSeconds' in main)
ck("developer timeout editor", 'Kommentar-Timeouts [s]' in main and 'Kommentar-Timeouts speichern' in main)
ck("debug direct save", 'Text("Protokoll speichern")' in main and 'onSave: () -> Unit' in main)
ck("normal location hides precision", 'reminderSettings.developerModeEnabled && location.accuracyMeters != null' in main)
ck("normal log sanitizes precision", 'accuracy_m=hidden' in debug)
ck("waterfall HH:mm:ss", 'HH:mm:ss' in timep)
ck("waterfall five labels", 'LABEL_COUNT = 5' in timep and 'WaterfallTimeAxisPolicy.labels' in water)
ck("rolling save is snapshot", 'RollingAudioEngine.snapshot()' in main)
ck("only explicit rolling disable stops service", main.count('RollingAudioService.requestStop(appContext)') == 1)
ck("rolling service warning cleaned", 'recorder?.recordingState' not in service and 'activeRecorder.stop()' in service)
ck("cloud user wording", 'Cloud' in main)

# v0.4.57 rolling status regression
ck("README 0.4.57", readme.startswith('# LFN-Scout Android v0.4.57'))
ck("CHANGELOG 0.4.57", '## 0.4.57' in changelog)
ck("PREPARE_PROJECT 0.4.57", 'LFN-Scout v0.4.57 - Projekt vorbereiten' in prepare)
ck("parent rolling seconds state", 'var rollingBufferedSeconds by remember' in main)
ck("parent rolling ticker", 'LaunchedEffect(reminderSettings.rollingBufferEnabled)' in main and 'rollingBufferedSeconds = RollingAudioEngine.bufferedSeconds()' in main)
ck("one second refresh", 'delay(1_000)' in main)
ck("MoreFunctions receives live parent value", 'rollingBufferedSeconds = rollingBufferedSeconds' in main)
ck("no screen-local liveRollingSeconds ticker", 'var liveRollingSeconds by remember' not in main)
ck("display uses rollingBufferedSeconds", 'RollingAudioPolicy.bufferStatusText(rollingBufferedSeconds)' in main)

for name, ok in checks:
    print(("PASS" if ok else "FAIL") + ": " + name)
failed=[name for name,ok in checks if not ok]
print(f"\nRESULT {len(checks)-len(failed)}/{len(checks)} PASS")
sys.exit(1 if failed else 0)
