
from pathlib import Path
import re
root=Path(__file__).resolve().parent
main=(root/"app/src/main/java/de/brummbeobachter/app/MainActivity.kt").read_text(encoding="utf-8")
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")
readme=(root/"README.md").read_text(encoding="utf-8")

assert 'versionName = "0.4.42"' in build, "versionName 0.4.42 missing"
assert 'versionCode = 82' in build, "versionCode 82 missing"
assert '"App-Version: ${BuildConfig.VERSION_NAME}"' in main, "Systemstatus does not use BuildConfig.VERSION_NAME"
assert '"App-Version: 0.4.41"' not in main, "hard-coded system-status version remains"

cancel_pos=main.find('Text("Aufnahme abbrechen")')
assert cancel_pos >= 0, "cancel label missing"
block=main[max(0,cancel_pos-1500):cancel_pos+300]
assert re.search(r'containerColor\s*=\s*MaterialTheme\s*\.colorScheme\s*\.secondary', block), "cancel button is not yellow"
assert re.search(r'contentColor\s*=\s*MaterialTheme\s*\.colorScheme\s*\.onSecondary', block), "cancel button text contrast not set"

assert readme.startswith("# LFN-Scout Android v0.4.42"), "README version header stale"
for phrase in [
    "Bewertungen 0–5",
    "Audioaufnahme",
    "UNPROCESSED",
    "R0",
    "RX",
    "Edge Functions",
    "Entwickler-Modus",
    "Adaptive Icon",
    "Java 17",
    "PREPARE_PROJECT",
]:
    assert phrase in readme, f"README missing current topic: {phrase}"

print("v0.4.42 requirements PASS")
