# BUILD_VERIFICATION v0.5.09 – 21.09.2026

## Artefakt und Quellbasis

Basis ist die vollständig vorliegende ZIP `LFN-Scout_Android_v0.5.08.zip`. Neue Quellversion `0.5.09` / `versionCode 152`; unverändertes `applicationId de.brummbeobachter.app`. Keine Änderungen am 8-kHz-Rolling-Recorder, WAV-Recorder, Rating, Backend oder der Seismik beabsichtigt. Zeitband und Farbskala aus v0.5.08 bleiben erhalten.

## Tatsächlich ausgeführte Prüfungen

| Test | Status | Reichweite |
|---|---|---|
| `tools/RUN_ANALYZER_V0509_SELFTEST.sh` | **PASS 33/33** | JVM-Hosttest: Direktparameter, FFT 64–16384, alle Fenster, DC, α, FIR12:1, Peakmarker |
| `tools/RUN_ANALYZER_V05041_TIMING_SELFTEST.sh` | **PASS 6/6** | Alte LF-/48-kHz-Zeitbasis und ungültiger Hop 33 mit alter LF-Rate weiterhin abgewiesen |
| `tools/RUN_ANALYZER_V0506_SELFTEST.sh` | **PASS 8/8**, vor abschließender Timing-Validierung | Maxhold-Regression (keine beabsichtigte Änderung an Maxhold) |
| `tools/RUN_ANALYZER_V0504_SELFTEST.sh` | **PASS 16/16**, vor abschließender Timing-Validierung | Grundlegende FFT-/Analysetests |
| Kotlin PSI Syntax aller Dateien | **PASS 181 Dateien / 0 Syntaxfehler** | Syntax, **keine** Android-Typprüfung |
| `tools/verify_v0509_direct_analyzer.py` | **PASS 25/25** | Nur Quellvertrag, keine Compose-Kompilierung |
| Historischer v0.5.03-FIR-Test in dieser Sitzung | **TIMEOUT**, kein neues PASS | Ergebnis historisch vorhanden, aber nicht für v0.5.09 neu bestätigt |
| `:app:testDebugUnitTest` / `assembleDebug` | **NOT RUN** | Android-SDK in dieser Laufzeit nicht verfügbar |
| Xiaomi Praxistest | **NOT RUN** | Keine Aussagen zu realen 96 kHz oder Audio Source-Kompatibilität |

Diese Prüfergebnisse sind nicht gleichbedeutend mit einem erfolgreich erstellten APK. Bei einer ersten Windows-Compile-Diagnose muss vor einer Geräteabnahme eine gesonderte Korrektur erfolgen.

## Grenzen und offener Punkt

Nicht ganzzahlig teilbare Eingangsraten/Dezimierungsfaktoren sind bewusst ausgeblendet. Real unterstützte Audioquellen/raten müssen je Gerät geprüft werden. Der bereits vom Nutzer gemeldete 16:1-Pegelverlust bleibt **OFFEN**. Testanleitung unter `docs/project/TESTPLAN_v0.5.09.md`.
