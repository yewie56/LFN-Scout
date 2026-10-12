from pathlib import Path

root = Path('.')
checks = []
def ck(name, cond): checks.append((name, bool(cond)))

build = (root/'app/build.gradle.kts').read_text(encoding='utf-8')
engine = (root/'app/src/main/java/de/brummbeobachter/app/RollingAudioEngine.kt').read_text(encoding='utf-8')
main = (root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
changelog = (root/'CHANGELOG.md').read_text(encoding='utf-8')
readme = (root/'README.md').read_text(encoding='utf-8')

cancel_test = root/'app/src/test/java/de/brummbeobachter/app/RollingAudioEngineCancellationTest.kt'
rating_test = root/'app/src/test/java/de/brummbeobachter/app/AudioCurrentRatingNamingContractTest.kt'

ck('version 0.4.54', 'versionCode = 94' in build and 'versionName = "0.4.54"' in build)
ck('capture cleanup is unconditional', 'finally {' in engine and 'if (capture === localCapture)' in engine and 'capture = null' in engine[engine.index('finally {'):])
ck('rolling cancellation regression test exists', cancel_test.is_file())
ck('latest recentItems state is retained for audio', 'val latestAudioRecentItems =\n        rememberUpdatedState(\n            recentItems\n        )' in main)
ck('recording-start observation snapshot exists', 'val observationsAtRecordingStart =\n                    latestAudioRecentItems.value' in main)
ck('local filename uses recording-start observation snapshot', 'observations =\n                                observationsAtRecordingStart' in main)
ck('current-rating naming regression test exists', rating_test.is_file())
ck('changelog documents cancellation fix', 'Rolling-Audiostream nicht verfügbar' in changelog and '0.4.54' in changelog)
ck('README documents no backend changes', 'Keine Supabase-, Schema-, Migration- oder Edge-Function-Änderung' in readme)
ck('legacy backend priority remains deferred', 'wav_present' in readme and 'wav_path' in readme)

failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'{len(checks)-len(failed)}/{len(checks)} PASS')
raise SystemExit(1 if failed else 0)
