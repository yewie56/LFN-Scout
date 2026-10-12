from pathlib import Path
import re, sys

ROOT = Path(__file__).resolve().parents[1]
checks=[]

def text(rel): return (ROOT/rel).read_text(encoding='utf-8')
def check(name, ok):
    checks.append((name,bool(ok)))
    print(f"{'PASS' if ok else 'FAIL'}: {name}")

build=text('app/build.gradle.kts')
check('versionName 0.5.19', 'versionName = "0.5.19"' in build)
check('versionCode 166', re.search(r'versionCode\s*=\s*166\b', build))

screen=text('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
settings=text('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt')
check('SAVE-Setup is in analyzer settings', 'SAVE-Setup · Audio / lokale Statistik' in settings and 'onOpenLocalSaveSettings' in settings)
check('SAVE-Setup removed from analyzer main screen', 'SAVE-Setup' not in screen)
check('Save Audio uses 48-dp action button', 'SAVE AUDIO is a 48-dp action button' in screen and 'Box(Modifier.size(48.dp)' in screen)
check('Audio save feedback uses Popup', 'Popup(alignment=Alignment.TopStart' in screen and 'audioSaveStatus' in screen)
check('Local saved confirmation exists', '"Lokal gespeichert"' in screen)
check('Audio feedback auto-clears after 3 seconds', 'delay(3_000L)' in screen)
check('Audio buffer/quota info sits in bottom action row', 'availableSeconds' in screen and 'dailyCount' in screen and 'bottom action row' in screen)

policy=text('analyzer-core/src/main/kotlin/de/brummbeobachter/app/GainReferencePolicy.kt')
stats=text('analyzer-core/src/main/kotlin/de/brummbeobachter/app/StatisticsWindowEngine.kt')
water=text('app/src/main/java/de/brummbeobachter/app/WaterfallGenerator.kt')
level=text('app/src/main/java/de/brummbeobachter/app/WaterfallLevelPolicy.kt')
repo=text('app/src/main/java/de/brummbeobachter/app/LocalAudioStatisticsRepository.kt')
controller=text('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
check('Central gain policy exists', 'object GainReferencePolicy' in policy and 'measuredDb - gainDb' in policy)
check('Statistics exposes shared analyzeOverall', 'fun analyzeOverall' in stats and 'GainReferencePolicy.amplitudeFactor' in stats)
check('WAV display uses shared L50/L90 statistics path', 'StatisticsWindowEngine.analyzeOverall' in water and 'sharedLevels?.l50' in water and 'sharedLevels?.l90' in water)
check('WAV spectrum applies recording-gain compensation', water.count('GainReferencePolicy.compensateDb') >= 2)
check('Manual range no longer shifts with gain', 'delta' not in level.lower() and 'normalizeGainDb(gainDb)' in level)
check('Analyzer live FFT uses central gain policy', controller.count('GainReferencePolicy.compensateDb') >= 3)
check('Analyzer local save is explicitly PRE-GAIN', 'StatisticsWindowEngine.analyze(clip.pcm, 0.0, config)' in repo and 'applied_wav_gain_db' in repo)

buf=text('app/src/main/java/de/brummbeobachter/app/LocalAnalyzerAudioBuffer.kt')
rem=text('app/src/main/java/de/brummbeobachter/app/ReminderSettingsStore.kt')
check('Expression-body return warning fixed', 'private fun accept(frame: AnalyzerPcmHub.Frame): Unit {' in buf and 'return@synchronized' in buf)
check('Reminder unnecessary result!! removed in quota function', 'result!!' not in rem)

vortest=text('tools/run-vortest.ps1')
check('ADB wait loop exists', 'while ([string]::IsNullOrWhiteSpace($serial))' in vortest)
check('ADB polls every 2 seconds', 'Start-Sleep -Seconds 2' in vortest)
check('ADB unauthorized state is handled', 'unauthorized' in vortest and 'USB-Debugging zulassen' in vortest)
check('Multiple ADB devices do not auto-install', 'MEHRERE ADB-Geraete' in vortest or 'Mehrere' in vortest)
check('No fixed ADB wait timeout introduced', 'adbWaitTimeout' not in vortest and 'Wait-Process -Timeout' not in vortest)

for rel in [
    'docs/project/ANALYZER_AUDIO_SAVE_GAIN_v0.5.19.md',
    'docs/project/TESTPLAN_v0.5.19.md',
    'BUILD_VERIFICATION_v0.5.19.md',
    'README.md','CHANGELOG.md','PROJECT_STATUS.md','DIRECTIVES.md','BUILD_WORKFLOWS.md','BASE_PACKAGE_REQUIRED.txt']:
    check(f'document exists: {rel}', (ROOT/rel).is_file())

base=text('BASE_PACKAGE_REQUIRED.txt')
check('BASE R1 SHA retained', '3f662bd281689c67c00f7fb1720dbef38a3252d22b8a9e18eeed85071ed95d08' in base)

failed=[n for n,ok in checks if not ok]
print(f"\nRESULT: {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    print('FAILED:')
    for n in failed: print(' -',n)
    sys.exit(1)
