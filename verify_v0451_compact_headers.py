from pathlib import Path
root=Path(__file__).resolve().parent
main=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
assert 'CompactInlineHeader(' in main, 'CompactInlineHeader helper missing'
assert 'title = "Bewertung"' in main, 'Bewertung title not separated'
assert 'detail = "0 = nichts wahrgenommen · 5 = sehr stark"' in main, 'Bewertung detail missing'
assert 'title = "Audio aufzeichnen"' in main, 'Audio title missing'
assert 'detail = "WAV · 120 s · 1000 Hz · mono · 16 Bit"' in main, 'Audio detail missing'
assert 'maxLines = 1' in main, 'No-wrap guard missing'
assert 'softWrap = false' in main, 'softWrap=false missing'
assert 'fontSize = 9.sp' in main or 'fontSize = 8.sp' in main, 'Small detail typography missing'
assert 'versionName = "0.4.51"' in (root/'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'versionCode = 91' in (root/'app/build.gradle.kts').read_text(encoding='utf-8')

# Regression checks retained from v0.4.50
assert 'Wasserfall neueste Aufnahme' not in main
assert 'Wasserfall für diese Aufnahme' not in main
assert 'onSwipeLeft = { },\n                onSwipeRight = { currentPanel = AppPanel.MORE }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.SETTINGS },\n                onSwipeRight = { currentPanel = AppPanel.MAIN }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.MORE },\n                onSwipeRight = { }' in main
assert 'WaterfallOutputFormat.PNG_AND_JPG' in main
assert 'detectTransformGestures' in main
assert 'detectTapGestures' in main

print('v0.4.51 compact headers + regressions PASS')
