
from pathlib import Path
root=Path(__file__).resolve().parent
pkg=root/"app/src/main/java/de/brummbeobachter/app"

policy=pkg/"AudioFileNamingPolicy.kt"
assert policy.exists(), "AudioFileNamingPolicy.kt missing"
pt=policy.read_text(encoding="utf-8")

assert "MAX_RATING_AGE_MINUTES = 30L" in pt
assert "fun selectRatingTag" in pt
assert "Duration.between" in pt
assert 'return "RX"' in pt
assert "exactly 30 minutes counts" in pt
assert "fun buildLocalWavFileName" in pt
assert "observerId" in pt and "gainDb" in pt and "audioSource" in pt
assert 'DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")' in pt

main=(pkg/"MainActivity.kt").read_text(encoding="utf-8")
recent=(pkg/"RecentObservation.kt").read_text(encoding="utf-8")
archive=(pkg/"AudioArchive.kt").read_text(encoding="utf-8")
wf=(pkg/"WaterfallGenerator.kt").read_text(encoding="utf-8")
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")

assert "observerId" in recent and "RecentObservationsResult" in recent
assert "result.observerId" in main
assert "localWavFileName" in main
assert "targetDisplayName" in archive
assert "publishWav(" in archive
assert "sourceFileName.substringBeforeLast" in wf
assert "_L50" in wf and "_L90" in wf
assert "FREQ_MAX_HZ.roundToInt()" in wf or "FREQ_MAX_HZ.toInt()" in wf
assert "_WF${" in wf
assert 'versionName = "0.4.48"' in build and "versionCode = 88" in build
print("v0.4.48 requirements PASS")
