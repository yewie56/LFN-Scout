from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
errors = []

def require(path, needle=None):
    p = ROOT / path
    if not p.exists():
        errors.append(f"FEHLT: {path}")
        return ""
    text = p.read_text(encoding="utf-8", errors="replace")
    if needle is not None and needle not in text:
        errors.append(f"FEHLT IN {path}: {needle}")
    return text

app = require("app/build.gradle.kts")
if 'versionName = "0.4.62"' not in app:
    errors.append("versionName ist nicht 0.4.62")
if 'versionCode = 102' not in app:
    errors.append("versionCode ist nicht 102")

prep = require("PREPARE_PROJECT.bat")
if "LFN-Scout v0.4.62 - Projekt vorbereiten" not in prep:
    errors.append("PREPARE_PROJECT.bat zeigt nicht v0.4.62")

workflow = require(".github/workflows/build-android.yml")
for needle in [
    "name: Build LFN-Scout",
    "workflow_dispatch:",
    "push:",
    "pull_request:",
    'STORE_APK_IN_REPOSITORY: "true"',
    "./gradlew testDebugUnitTest --stacktrace",
    "./gradlew assembleDebug --stacktrace",
    "Builds/debug",
]:
    if needle not in workflow:
        errors.append(f"Workflow-Anforderung fehlt: {needle}")

doc = require("BUILD_WORKFLOWS.md")
for needle in [
    "Buildweg A – Android Studio unter Windows",
    "Buildweg B – Gradle-Kommandozeile unter Windows",
    "Buildweg C – Android-only mit Work + GitSync + GitHub Actions",
    "PREPARE_PROJECT.bat",
    "ChatGPT-App",
    "GitSync",
    "Android-Dateimanager",
    "Webbrowser",
    "R/.git/",
    "R/Builds/",
    "GitSync → **Pull**",
    "Stage all",
    "LFN-Scout-vX.Y.Z-debug.apk",
]:
    if needle not in doc:
        errors.append(f"BUILD_WORKFLOWS.md unvollständig: {needle}")

if errors:
    print("FAIL v0.4.62 workflow verification")
    for e in errors:
        print("-", e)
    sys.exit(1)

print("PASS v0.4.62 workflow verification")
