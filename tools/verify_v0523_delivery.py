from pathlib import Path
import re, sys
ROOT = Path(__file__).resolve().parents[1]
checks=[]
def check(name, ok):
    checks.append((name, bool(ok)))
    print(('PASS' if ok else 'FAIL') + ': ' + name)

def txt(rel): return (ROOT/rel).read_text(encoding='utf-8')

build=txt('app/build.gradle.kts')
archive=txt('app/src/main/java/de/brummbeobachter/app/AudioArchive.kt')
repo=txt('app/src/main/java/de/brummbeobachter/app/LocalAudioStatisticsRepository.kt')
screen=txt('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
controller=txt('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
main=txt('app/src/main/java/de/brummbeobachter/app/MainActivity.kt')
naming=txt('app/src/main/java/de/brummbeobachter/app/AudioFileNamingPolicy.kt')

check('versionName 0.5.23', 'versionName = "0.5.23"' in build)
check('versionCode 170', 'versionCode = 170' in build)
check('Analyzer uses shared AudioFileNamingPolicy', 'AudioFileNamingPolicy.buildLocalWavFileName' in repo)
check('Analyzer file name truthfully uses PRE-GAIN G0', 'gainDb = 0' in repo and 'The stored analyzer WAV is PRE-GAIN' in repo)
check('Analyzer archive uses root AudioArchive path', 'fun createAnalyzer(' in archive and 'publishWav(appContext, wavFile, targetDisplayName)' in archive)
check('Analyzer JSONs use root publishDownloadFile', 'publishDownloadFile(appContext, statTemp, statisticsName' in archive and 'publishDownloadFile(appContext, manifestTemp, manifestName' in archive)
# saveAnalyzer body itself must no longer publish to LocalSaves
sa = repo.split('suspend fun saveAnalyzer',1)[1].split('suspend fun saveObserverSidecar',1)[0]
check('Analyzer save does not use LocalSaves', 'LocalSaves' not in sa)
check('Archive origin metadata exists', 'val origin: String = "OBSERVER"' in archive and 'origin = "ANALYZER"' in archive)
check('Analyzer sidecars retained in archive metadata', 'manifestJsonName' in archive and 'companionJsonName = statisticsName' in archive)
check('24h cleanup deletes analyzer sidecars too', 'val companionDeleted = deleteCompanionJson' in archive and 'val manifestDeleted = deleteManifestJson' in archive)
check('Analyzer WAV enters shared Supabase upload path', 'onAnalyzerAudioArchived' in screen and 'supabaseClient.uploadWav(' in main and 'ANALYZER_AUDIO_UPLOAD_OK' in main)
check('Analyzer upload success marks shared archive uploaded', 'AudioArchive.markUploaded(appContext, archived.recordingId' in main)
check('Shared cleanup called after analyzer upload', 'AudioArchive.cleanup(appContext)' in main)
check('Statistics upload remains unconfigured', 'supabase_statistics_upload", "NOT_CONFIGURED"' in repo)
check('Stored analyzer files are reusable by existing waterfall path', 'AudioArchive.list(' in main and 'WaterfallGenerator' in main)
check('Image is frozen before comment dialog', 'captureManualImage()' in screen and 'pendingImageSnapshot = captured' in screen)
check('Comment dialog exists for manual image', 'Kommentar zum Analyzer-Bild' in screen and 'Kommentar (optional)' in screen)
check('Image comment is passed only after dialog', 'saveCapturedImage(snap, imageComment)' in screen)
check('Image comment is rendered into waterfall bitmap', 'Kommentar: $line' in controller and 'boxBottom = wfBottom - 10f' in controller)
check('Automatic image export remains comment-free by default', 'private fun export(snap: Snapshot, manual: Boolean = false, comment: String? = null)' in controller)
check('No runtime version hardcoding regression in VORTEST wrapper', 'versionName' not in txt('VORTEST.bat'))

failed=[n for n,o in checks if not o]
print(f'RESULT {len(checks)-len(failed)}/{len(checks)} PASS')
if failed: sys.exit(1)
