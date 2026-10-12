from pathlib import Path
import re, sys
root = Path(__file__).resolve().parent
main = (root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
comment = (root/'app/src/main/java/de/brummbeobachter/app/CommentAutoSendPolicy.kt').read_text(encoding='utf-8')
settings = (root/'app/src/main/java/de/brummbeobachter/app/ReminderSettingsStore.kt').read_text(encoding='utf-8')
waterfall = (root/'app/src/main/java/de/brummbeobachter/app/WaterfallGenerator.kt').read_text(encoding='utf-8')
debug = (root/'app/src/main/java/de/brummbeobachter/app/DebugLogger.kt').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks = []
def ck(name, cond): checks.append((name, bool(cond)))
ck('version 0.4.55', 'versionName = "0.4.55"' in gradle)
ck('rating 10s untouched', 'RATING_NO_COMMENT_TIMEOUT_MS = 10_000L' in comment)
ck('rating 10s editing', 'RATING_COMMENT_TIMEOUT_MS = 10_000L' in comment)
ck('audio 60s untouched', 'AUDIO_NO_COMMENT_TIMEOUT_MS = 60_000L' in comment)
ck('audio 10s editing', 'AUDIO_COMMENT_TIMEOUT_MS = 10_000L' in comment)
ck('rating effect retriggers on comment', 'pendingComment\n    )' in main or 'pendingComment,' in main[main.find('LaunchedEffect(\n        pendingRating'):main.find('Surface(', main.find('LaunchedEffect(\n        pendingRating'))])
ck('rating phase starts on text edit not focus', '.onFocusChanged { focusState' not in main[main.find('Text("Kommentar (optional)")')-500:main.find('Text("Kommentar (optional)")')+500])
ck('audio untouched starts 60', 'audioCommentCountdown =\n                    60' in main or 'audioCommentCountdown = 60' in main)
ck('normal l50/l90 default 25Hz', 'DEFAULT_MAX_HZ = 25.0' in (root/'app/src/main/java/de/brummbeobachter/app/WaterfallStatsPolicy.kt').read_text(encoding='utf-8') if (root/'app/src/main/java/de/brummbeobachter/app/WaterfallStatsPolicy.kt').exists() else False)
ck('stats settings persisted', 'waterfallStatsMinHz' in settings and 'waterfallStatsMaxHz' in settings)
ck('generator accepts stats range', 'statsMinHz:' in waterfall and 'statsMaxHz:' in waterfall)
ck('L50/L90 power uses selected bins only', 'if (bin in firstL50L90Bin..lastL50L90Bin)' in waterfall and 'framePower += power' in waterfall)
ck('adaptive color range remains 2-140Hz', 'const val LEVEL_STATS_MIN_HZ = 2.0' in waterfall and 'const val LEVEL_STATS_MAX_HZ = 140.0' in waterfall)
ck('more auto refresh', 'currentPanel == AppPanel.MORE' in main and 'refreshAudioArchive()' in main)
ck('waterfall overwrite confirmation', 'waterfallOverwriteNames' in main and 'Bilddatei vorhanden' in main and 'Überschreiben' in main and 'existingOutputNames' in waterfall)
ck('log export', 'saveCopy' in debug and 'Logfile speichern' in main)
ck('normal visible Supabase wording removed', 'Rolling-Aufnahmen werden nicht zu Supabase übertragen' not in main and 'keine Supabase-Übertragung' not in main)
ck('cloud wording present', 'Cloud' in main)
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ' ' + name)
failed = [n for n,o in checks if not o]
print(f'RESULT {len(checks)-len(failed)}/{len(checks)} PASS')
sys.exit(1 if failed else 0)
