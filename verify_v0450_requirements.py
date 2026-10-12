from pathlib import Path
root=Path(__file__).resolve().parent
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
build=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
archive=(root/'app/src/main/java/de/brummbeobachter/app/AudioArchive.kt').read_text(encoding='utf-8')
assert 'versionCode = 90' in build and 'versionName = "0.4.50"' in build
assert 'Wasserfall neueste Aufnahme' not in main
assert 'onGenerateWaterfall: () -> Unit' not in main
assert 'title = "Bewertung · 0 = nichts wahrgenommen · 5 = sehr stark"' in main
assert '"0 = nichts wahrgenommen · 5 = sehr stark"' not in main.replace('title = "Bewertung · 0 = nichts wahrgenommen · 5 = sehr stark"','')
assert 'reminderStatus +\n                            " · Modus: "' in main
# Correct, non-cyclic swipe directions in invocation order SETTINGS, MORE, MAIN:
assert 'onSwipeLeft = { },\n                onSwipeRight = { currentPanel = AppPanel.MORE }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.SETTINGS },\n                onSwipeRight = { currentPanel = AppPanel.MAIN }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.MORE },\n                onSwipeRight = { }' in main
# All entries shown; AudioArchive newest-first.
assert 'audioEntries\n                        .forEach' in main
assert '.sortedByDescending' in archive and 'entry.recordedAt' in archive
# v0.4.49 features retained
assert 'WaterfallOutputFormat.PNG_AND_JPG' in main
assert 'detectTransformGestures' in main
assert 'detectTapGestures' in main
assert 'onGenerateWaterfallForEntry' in main
print('v0.4.50 requirements PASS')
