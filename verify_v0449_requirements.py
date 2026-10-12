from pathlib import Path
root=Path(__file__).resolve().parent
pkg=root/'app/src/main/java/de/brummbeobachter/app'
main=(pkg/'MainActivity.kt').read_text(encoding='utf-8')
store=(pkg/'ReminderSettingsStore.kt').read_text(encoding='utf-8')
wf=(pkg/'WaterfallGenerator.kt').read_text(encoding='utf-8')
build=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
fmt=pkg/'WaterfallOutputFormat.kt'

assert fmt.exists(), 'WaterfallOutputFormat.kt missing'
ft=fmt.read_text(encoding='utf-8')
assert 'PNG' in ft and 'JPG' in ft and 'PNG_AND_JPG' in ft
assert 'waterfallOutputFormat' in store
assert 'setWaterfallOutputFormat' in store
assert 'Wasserfall-Bildformat' in main
assert 'PNG + JPG' in main
assert 'saveImages' in wf
assert 'CompressFormat.JPEG' in wf or '.JPEG' in wf
assert '95' in wf
assert 'Wasserfall für diese Aufnahme' not in main
assert '.clickable' in main and 'onGenerateWaterfallForEntry' in main
assert 'detectTransformGestures' in main
assert 'onDoubleTap' in main
assert 'graphicsLayer' in main
assert 'PANEL_SWIPE_THRESHOLD_DP' in main
assert 'AppPanel.MAIN' in main and 'AppPanel.MORE' in main and 'AppPanel.SETTINGS' in main
assert 'onSwipeRight = { currentPanel = AppPanel.MORE }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.MAIN }' in main
assert 'onSwipeRight = { currentPanel = AppPanel.SETTINGS }' in main
assert 'onSwipeLeft = { currentPanel = AppPanel.MORE }' in main
assert 'versionName = "0.4.49"' in build and 'versionCode = 89' in build
print('v0.4.49 requirements PASS')
