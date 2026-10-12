# v0.4.29 Audio + Developer Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Echte 120-s-WAV-Aufnahme, Alarm-Lautstärke, +0…30-dB-Gain, Tageslimit, Entwicklermodus und nochmals kompakteres UI in v0.4.29.

**Architecture:** Reine Regeln liegen in kleinen Policy-Klassen und werden unit-testbar gehalten. Android-spezifische Audioaufnahme liegt in `AudioRecorder.kt`; Persistenz bleibt im bestehenden `ReminderSettingsStore`. UI bleibt in `MainActivity.kt`, Backend-Änderungen liegen unter `supabase/`.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, AudioRecord, DataStore, HttpURLConnection, Supabase Edge Functions.

**Spec:** `docs/superpowers/specs/2026-09-08-v0428-audio-developer-mode-design.md`

## Global Constraints
- v0.4.27 bleibt unverändert als Rückfallversion.
- Keine neue externe Android-Abhängigkeit.
- WAV-Ziel: 1000 Hz, mono, PCM16, 120 s.
- Gain 0…30 dB in 1-dB-Schritten.
- Tageslimit Standard 10; editierbar nur im Entwicklermodus.
- Passwort = dritte Vierergruppe des Schlüssels rückwärts.
- Kein expliziter Compose-`weight`-Import.
- bestehende Reminder-, Offline-, Radius- und Fullscreen-Logik nicht regressieren.

---

### Task 1: Reine Policies
- [ ] RED-Tests für Passwort, Gain, Limit und Layoutwerte.
- [ ] GREEN: `DeveloperModePolicy`, `AudioRecordingPolicy`, neue Layoutwerte.

### Task 2: Persistenz
- [ ] RED-Tests/Strukturprüfung für neue Settingsfelder.
- [ ] GREEN: Lautstärke, Gain, Tageslimit, Tageszähler, Entwicklermodus in DataStore.

### Task 3: WAV-Aufnahme
- [ ] RED-Tests für WAV-Header und Gain-Faktor.
- [ ] GREEN: `AudioRecorder.kt` mit AudioRecord 8000 Hz, Downsample 8:1, Gain, Clipping, WAV-Header.

### Task 4: Upload
- [ ] GREEN: App-Client `uploadWav`, Edge Function `upload_wav`, Config und Setup-Hinweis.

### Task 5: UI
- [ ] Alarm-Lautstärke-Slider in Einstellungen.
- [ ] Gain-Regler in Audio-Bereich.
- [ ] 15-s-Warn-Countdown.
- [ ] 120-s-Aufnahmestatus und Auto-Upload.
- [ ] Tageslimit-Anzeige.
- [ ] Entwicklermodus-Aktivierung/Deaktivierung und dev-only Felder.
- [ ] nochmals reduzierte vertikale Abstände.

### Task 6: Schlüsselstatus
- [ ] Migration status active/paused/revoked.
- [ ] Edge Functions prüfen Status.
- [ ] App erkennt `key_revoked` und deaktiviert Entwicklermodus.

### Task 7: Regression/Paket
- [ ] statische Regressionen.
- [ ] Gradle-Build versuchen.
- [ ] ZIP-Integrität.
- [ ] `LFN-Scout_Android_v0.4.29.zip`.
