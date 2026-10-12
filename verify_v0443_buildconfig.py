
from pathlib import Path
root=Path(__file__).resolve().parent
build=(root/"app/build.gradle.kts").read_text(encoding="utf-8")
main=(root/"app/src/main/java/de/brummbeobachter/app/MainActivity.kt").read_text(encoding="utf-8")

assert 'BuildConfig.VERSION_NAME' in main, "dynamic version usage missing"
assert 'buildConfig = true' in build, "BuildConfig generation not enabled"
assert 'versionName = "0.4.43"' in build, "versionName not 0.4.43"
assert 'versionCode = 83' in build, "versionCode not 83"
print("v0.4.43 BuildConfig verification PASS")
