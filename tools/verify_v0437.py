from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/de/brummbeobachter/app/MainActivity.kt").read_text(encoding="utf-8")
layout = (root / "app/src/main/java/de/brummbeobachter/app/AppPanel.kt").read_text(encoding="utf-8")
build = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
pause = (root / "app/src/main/java/de/brummbeobachter/app/PauseControlsPolicy.kt").read_text(encoding="utf-8")
prep = (root / "PREPARE_PROJECT.bat").read_text(encoding="utf-8")

audio_start = main.index('SectionCard(\n        title = "Audio aufzeichnen",')
audio_end = main.index('@Composable\nprivate fun RadiusHistogramSummary', audio_start)
audio = main[audio_start:audio_end]

settings_start = main.index('@Composable\nprivate fun ReminderSettingsScreen')
settings = main[settings_start:]

checks = [
    ("versionName 0.4.37", 'versionName = "0.4.37"' in build),
    ("versionCode 77", "versionCode = 77" in build),
    ("WAV info retained in main", '"WAV · 120 s · 1000 Hz · mono · 16 Bit"' in audio),
    ("Gain label retained in main", '"Verstärkung: +$gainDb dB"' in audio),
    ("Gain slider retained in main", "valueRange = 0f..50f" in audio),
    ("Audio today retained in main", '"Audio heute: "' in audio),
    ("CLIP appears beside gain via one Row", 'Arrangement.SpaceBetween' in audio and '"CLIP · $clippedSamples"' in audio),
    ("No standalone CLIP Samples block", '"CLIP · $clippedSamples Samples"' not in audio),
    ("Default Bereit suppressed", 'status != "Bereit"' in audio),
    ("Active source/input retained", "actualSourceLabel" in audio and "actualDeviceLabel" in audio),
    ("Recording time retained", '"Aufnahme: " +' in audio),
    ("Start button retained", '"Aufzeichnung starten"' in audio),
    ("Gain removed from settings", "valueRange = 0f..50f" not in settings and "onAudioGainChange" not in settings),
    ("AudioSource retained in settings", '"AudioSource"' in settings),
    ("Audio input retained in settings", '"Audioeingang"' in settings),
    ("Inputs refresh retained in settings", 'Text("Inputs aktualisieren")' in settings),
    ("Main compact gap defined", "MAIN_SECTION_GAP_DP = 2" in layout),
    ("Main compact card padding defined", "MAIN_CARD_PADDING_DP = 5" in layout),
    ("Main compact title gap defined", "MAIN_CARD_TITLE_GAP_DP = 1" in layout),
    ("Header subtitle preserved", '"Beobachten · bewerten · dokumentieren"' in main),
    ("Rating explanation preserved", '"0 = nichts wahrgenommen · 5 = sehr stark"' in main),
    ("Recent ratings preserved", '"Letzte Bewertungen"' in main),
    ("Verlauf/Rohdaten section preserved", '"Verlauf & Rohdaten"' in main),
    ("Pause row 1 unchanged", "listOf(1, 2, 4, 8)" in pause),
    ("Pause row 2 unchanged with 12h", "listOf(12, 24, 72, DISABLE)" in pause),
    ("Java17 preparation retained", 'set "JAVA_HOME=%JAVA17_HOME%"' in prep),
]

failed = []
for name, ok in checks:
    print(("PASS" if ok else "FAIL"), name)
    if not ok:
        failed.append(name)

print(f"TOTAL {len(checks)-len(failed)}/{len(checks)} PASS")
if failed:
    raise SystemExit("FAILED: " + "; ".join(failed))
