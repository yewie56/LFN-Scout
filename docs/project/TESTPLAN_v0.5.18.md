# Testplan LFN-Scout v0.5.18 – Audio-/Statistik-Integration

**Alle Schritte initial NOT RUN, sofern kein konkret protokollierter Testbeleg.** Die früher vom Nutzer bestätigte Portrait- und Analyzer-Funktion der v0.5.17 nicht pauschal übertragen.

## A. Build / Regression auf Windows

1. v0.5.17-ZIP sichern; neues v0.5.18-ZIP frisch entpacken. `PREPARE_PROJECT.bat` ausführen.
2. `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace` ausführen, vollständiges Log sichern.
3. `python tools/verify_v0518_local_audio.py`, `python tools/verify_portrait_v0517.py`. Kein APK-PASS ohne Gradle-Abschluss.
4. Probeweise lokale Audio-JVM-Tests durchführen und SHA-256 von ZIP/WAV/JSON prüfen.

## B. Xiaomi – Analyzer

1. Main-Buttons und genau `activity`, `blocks`, `settings`, `waves` in Reihenfolge Analyzer, Zusatzfunktionen, Einstellungen, Seismik kontrollieren; Portraitdrehung bleibt wirksam.
2. SAVE-Setup: Dauer 30–600 s, MIN < MAX <= 200 Hz, Standard 120 s / 2–25 Hz; Werte nach Neustart behalten.
3. RUN unter 120 s: SAVE AUDIO deaktiviert. Ab 120 s freigeben; während RUN speichern und FFT/Wasserfall auf Unterbrechungsfreiheit prüfen. WAV 1kHz/16bit/mono, genau 120 s; JSON/Manifest/Hash vergleichen.
4. RUN mindestens 120 s, PAUSE, einige Sekunden warten, SAVE AUDIO: Dateiende muss dem PAUSE-Zeitpunkt entsprechen. Fehler simulieren und Snapshot-Wiederholung prüfen; RUN setzt neuen Snapshot frei.
5. Quelle, Gain und Modus wechseln: Fenster füllt sich neu; keine Vermischung alter PCM-Abschnitte. Direkt-Modus und 8-kHz-Modus separat. TEST-Modus kann nicht SAVE auslösen.
6. +50 dB Analyzer-Gain: FFT-Pegel inkl. Wasserfall und Statistik um -50 dB bezogen auf vorverstärktes Signal (soweit tatsächlicher DSP-Gain angewandt). Roh-WAV bleibt vorgain; FAST-Pegel werden nicht ein zweites Mal korrigiert.
7. Filterbank 2/10/20/40, getrennte FILTER-MIN/MAX (ggf. außerhalb STAT-MIN/MAX); JSON enthält genau aktive Bandzahl mit nominalen Grenzen, lokal berechnete Werte ausdrücklich nicht als 8-kHz-FAST ausgewiesen.

## C. Xiaomi – Beobachter und Supabase-Regression

1. Aufzeichnung mit eingestellter Dauer starten, 1-kHz-WAV sowie lokales JSON nach Abschluss prüfen. Im JSON **kein** `analyzer_setup`-Objekt.
2. Aufzeichnungs-Gain +50 dB: Leq/L10/L90 und PSD gegen Gain=0-Signal vergleichen; Übersteuerung als ungültig markieren.
3. Bereits funktionierenden Beobachter-Supabase-Upload mit bestehendem Datenvertrag/Kommentar prüfen. Neue Statistik darf nicht unbemerkt im Cloud-Payload auftauchen.
4. Gemeinsames Tageslimit 10: Beobachter und Analyzer abwechselnd speichern, 11. Save deaktiviert; parallele Save-Aktionen und Tageswechsel einschließlich Fehlerfall prüfen.
5. Offline-Betrieb, knappes Speichervolumen und verweigerte Speicherberechtigung: nachvollziehbarer Fehler und keine beschädigten halbfertigen Analyzer-Dateien. Protokoll mit PASS/FAIL.
6. STOP/Panelwechsel/Audio-Restore, Setup1/4-Exportkonflikt, PNG/JPG, Filterbuttons und Erinnerungs-Portrait als Regression testen.

## D. Explizit nicht Bestandteil

Kein Statistik-Supabase-Upload, keine FAST-Rohchronik, keine validierte Impuls-/Modulationsstatistik, keine Kalibrierfreigabe, kein vollständiger Managed-Restore. Prüfkriterien dafür bleiben OPEN.
