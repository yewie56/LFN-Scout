
from pathlib import Path
root=Path(__file__).resolve().parent
pkg=root/"app/src/main/java/de/brummbeobachter/app"
main=(pkg/"MainActivity.kt").read_text(encoding="utf-8")
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")

assert (pkg/"WaterfallGenerator.kt").exists(), "WaterfallGenerator.kt missing"
wf=(pkg/"WaterfallGenerator.kt").read_text(encoding="utf-8")
assert "FREQ_MIN_HZ = 0.0" in wf and "FREQ_MAX_HZ = 100.0" in wf
assert "LEVEL_STATS_MIN_HZ = 2.0" in wf and "LEVEL_STATS_MAX_HZ = 100.0" in wf
assert "PERCENTILE_LOW = 0.05" in wf and "PERCENTILE_HIGH = 0.995" in wf
assert "LEVEL_MARGIN_DB = 3.0" in wf and "MIN_LEVEL_SPAN_DB = 30.0" in wf
assert "LEVEL_HARD_MIN_DB = -120.0" in wf and "LEVEL_HARD_MAX_DB = 0.0" in wf
assert "infernoColor" in wf
assert "FFT_SIZE = 1024" in wf and "HOP_SIZE = 256" in wf
assert "Hann" in wf or "hann" in wf
assert "time increases downward" in wf or "timeDownward" in wf
assert "savePng" in wf and "MediaStore" in wf
assert "Wasserfall letzte Aufnahme" in main
assert "Wasserfallspektrum" in main
assert "WATERFALL_GENERATE_START" in main and "WATERFALL_GENERATE_OK" in main
assert 'versionName = "0.4.46"' in build and "versionCode = 86" in build
print("v0.4.46 waterfall requirements PASS")
