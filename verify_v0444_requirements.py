
from pathlib import Path
root=Path(__file__).resolve().parent
pkg=root/"app/src/main/java/de/brummbeobachter/app"
main=(pkg/"MainActivity.kt").read_text(encoding="utf-8")
audio=(pkg/"AudioRecorder.kt").read_text(encoding="utf-8")
sup=(pkg/"SupabaseClient.kt").read_text(encoding="utf-8")
edge=(root/"supabase/functions/upload_wav/index.ts").read_text(encoding="utf-8")
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")

assert (pkg/"AudioArchive.kt").exists(), "AudioArchive.kt missing"
archive=(pkg/"AudioArchive.kt").read_text(encoding="utf-8")
assert '"audio_recordings"' in archive, "archive directory missing"
assert "RETENTION_MS" in archive and "24L * 60L * 60L * 1000L" in archive, "24h retention missing"
assert "uploadedAtEpochMs" in archive, "successful upload timestamp missing"
assert "cleanup" in archive and "uploadedAtEpochMs" in archive, "cleanup policy missing"
assert "AudioArchive" in audio and ".directory(" in audio and '"audio_pending"' not in audio, "recorder not using archive directory"

assert "audioComment" in main, "audio comment state missing"
assert "10" in main and "60" in main, "10s/60s timeout policy missing"
assert "Automatischer Versand in" in main, "visible countdown missing"
assert "Kommentar zur Audioaufnahme" in main, "audio comment dialog missing"
assert "Audio-Aufzeichnungen" in main, "archive UI missing"
assert "Lokales Aufzeichnungsverzeichnis" in main, "directory path display missing"
assert "Erneut senden" in main, "retry UI missing"

assert "comment: String?" in sup, "upload comment parameter missing"
assert '"comment"' in sup, "upload comment JSON missing"
assert "comment" in edge and "2000" in edge, "Edge comment validation missing"
assert "comment:" in edge, "Edge metadata comment insert missing"

assert (root/"supabase/migrations/20260909_add_audio_recording_comment.sql").exists(), "audio comment migration missing"
assert 'versionName = "0.4.44"' in build and "versionCode = 84" in build, "version not 0.4.44/84"
print("v0.4.44 requirement verification PASS")
