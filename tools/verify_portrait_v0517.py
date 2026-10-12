#!/usr/bin/env python3
"""Statischer Regressionstest: App-Aktivitäten sind portrait-gesperrt."""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parents[1]
manifest = root / "app/src/main/AndroidManifest.xml"
ns = "{http://schemas.android.com/apk/res/android}"
app = ET.parse(manifest).getroot().find("application")
assert app is not None, "application fehlt"
activities = {activity.get(ns + "name"): activity for activity in app.findall("activity")}
for name in (".MainActivity", ".ReminderAlarmActivity"):
    assert name in activities, f"{name} fehlt"
    assert activities[name].get(ns + "screenOrientation") == "portrait", f"{name} ist nicht portrait-gesperrt"
    assert activities[name].get(ns + "configChanges") is None, "keine ungetesteten Lifecycle-Sonderfälle"
main = activities[".MainActivity"]
assert main.get(ns + "exported") == "true", "Launcher geändert"
assert any(f.find("action").get(ns + "name") == "android.intent.action.MAIN"
           for f in main.findall("intent-filter") if f.find("action") is not None), "Launcher-Intent fehlt"
for service in (".RollingAudioService", ".AnalyzerDirectAudioService"):
    assert any(s.get(ns + "name") == service for s in app.findall("service")), f"{service} fehlt"
text = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
assert re.search(r"versionCode\s*=\s*(?:160|161)\b", text)
assert re.search(r'"0\.5\.(?:17|18)"', text)
assert 'applicationId = "de.brummbeobachter.app"' in text
print("PASS: Main-/Reminder-Portrait, Launcher, Audiodienste, Paket-ID und Version v0.5.17/v0.5.18")
