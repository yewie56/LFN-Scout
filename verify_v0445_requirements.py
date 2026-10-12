
from pathlib import Path
root=Path(__file__).resolve().parent
pkg=root/"app/src/main/java/de/brummbeobachter/app"

main=(pkg/"MainActivity.kt").read_text(encoding="utf-8")
archive=(pkg/"AudioArchive.kt").read_text(encoding="utf-8")
debug=(pkg/"DebugLogger.kt").read_text(encoding="utf-8")
sup=(pkg/"SupabaseClient.kt").read_text(encoding="utf-8")
helper=(root/"supabase/functions/upload_wav/storage_filename.js").read_text(encoding="utf-8")
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")

assert 'versionName = "0.4.45"' in build and "versionCode = 85" in build
assert "MediaStore" in archive and ".Downloads" in archive, "Downloads/MediaStore storage missing"
assert "Download/LFN-Scout/" in archive, "public Downloads path missing"
assert "LEGACY_CLEANUP_DONE" in archive, "first-start legacy cleanup flag missing"
assert '"audio_recordings"' in archive, "legacy internal folder missing"
assert "deleteLegacyWavsOnce" in archive, "legacy one-time cleanup missing"
assert "MAX_LINES = 2000" in debug, "log history not restored to 2000 lines"
for event in [
    "AUDIO_STORAGE_INIT",
    "AUDIO_RECORD_REQUEST",
    "AUDIO_RECORD_STARTED",
    "AUDIO_INPUT_RESOLVED",
    "AUDIO_RECORD_FINISHED",
    "AUDIO_WAV_SAVED",
    "AUDIO_COMMENT_DIALOG_OPEN",
    "AUDIO_UPLOAD_START",
    "AUDIO_UPLOAD_OK",
    "AUDIO_UPLOAD_FAILED",
    "AUDIO_ARCHIVE_CLEANUP",
]:
    assert event in main + archive + sup, f"missing log event {event}"
assert "safeObserverId}/` +" in helper and "${safeObserverId}_${timestamp}" in helper, "observer id not in WAV filename"
print("v0.4.45 requirements PASS")
