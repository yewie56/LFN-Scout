# LFN-Scout v0.4.91 – Fehleranalyse, Build- und Testbericht

**Stand:** 20.09.2026 · **Status:** neuer Android-Quellstand/Testkandidat, noch keine auf Windows gebaute oder auf Xiaomi geprüfte v0.4.91-APK.

## 1. Befund aus v0.4.90

Der Nutzer hat die App auf dem Xiaomi gestartet. Screenshot: Wasserfall 235/235, keine Kurven, Cursor −240 dBFS, L50/L90 etwa −220,3 dBFS. Das entspricht dem numerischen FFT-Boden und **nicht** einem gemessenen Schalldruckpegel. Aus der bisherigen Anzeige allein ist der tatsächliche Verlustpunkt nicht beweisbar. Es gibt mindestens zwei Schwachpunkte: zusätzliche Ganzzahl-Rundung im 8:1-Recorderzweig und eine starre Grafik-Untergrenze von −110 dBFS, die sehr schwache reale Kurven verdeckt. Die bereits mit der Impulsdiagnose erfolgreich geprüften 8-kHz-Roh-PCM-Daten werden nun separat ausgewertet; eine neue Stufendiagnose macht die Frage auf dem Gerät prüfbar.

## 2. Änderungen gegenüber v0.4.90

- Version: `versionName = "0.4.91"`, `versionCode = 131`. Die App-Paketkennung bleibt `de.brummbeobachter.app`.
- Separater **Analyzer-ONLY**-Pfad: `AnalyzerSessionGateway.rawPcm` → `AnalyzerRawLfSession` → 129-Tap-Hamming-FIR mit 280 Hz Grenzfrequenz → 1 kHz normierte **Double**-Samples → bisherige FFT 4096/512. Der 200-Hz-Pegel wird durch den FIR rechnerisch um etwa 0,27 dB gedämpft; das ist keine vollständige Mikrofonkalibrierung. Kein zusätzlicher Mic-Client.
- Eigener Analyzer-Gain 0–50 dB (Standard 0), persistent, getrennt vom WAV-/Impulsgain. Neue Signaldiagnose: tatsächlich gemeldete Rate/Quelle, Rohpeak/RMS, LF-Peak/RMS vor Analyzer-Gain, FFT-Zähler, Datenalter, Drops, Resets, Callback-Fehler.
- Numerische FFT-Untergrenze wird **nicht** als gültige Pegelmessung verwendet; bei allen Bins ≤−180 dBFS keine neue gültige Wasserfallzeile/Statistik/PNG. PCM-Ausfall >3 s wird sichtbar gemeldet.
- Kompaktere dunkle Analyzer-Oberfläche nach bereitgestelltem Nutzerbild. Im klassischen Wasserfall nun **Zeit horizontal und Frequenz vertikal**; Spektrum gelb/rot/grau, 2–25-Hz-Band, Diagnose, PNG-Leiste, sechs Register. Noch nicht implementierte Audio-/Impulsfunktionen werden nicht als betriebsbereit ausgegeben.
- Der bestehende 8-kHz→1-kHz-120-s-WAV-Recorder, Rolling-Buffer, Impulsdiagnose, Seismik und Supabase-Code bleiben unverändert.

## 3. Lokal tatsächlich ausgeführte Prüfungen

| Prüfung | Resultat |
|---|---|
| `kotlinc` aller `:analyzer-core`-Produktivquellen | PASS |
| `tools/RUN_ANALYZER_LIVE491_SELFTEST.sh` | PASS: schwaches 47-Hz-Signal und 180-Hz-Signal, 2.500-Hz-Alias, Nulleingang, Roh-PCM-Hub bis FFT |
| `tools/RUN_ANALYZER_PHASE2_SELFTEST.sh` | PASS: alter 10-Hz-FFT-Pfad, PCM-Fan-out, Ownership |
| `tools/RUN_ANALYZER_LIVE_SELFTEST.sh` (v0.4.90-Basistest) | PASS |
| `verify_v0491_live_analyzer.py` | PASS: 16/16 statische Architektur-/Datenerhalt-Prüfungen |
| Rekonstruktionsmanifest und ZIP-Integrität | beim Verpacken geprüft; Ergebnis siehe Manifestprüfung |
| `:analyzer-core:test`, `:app:testDebugUnitTest`, `assembleDebug` | **NOT RUN** in der Erstellungsumgebung (kein Android-SDK) |
| Xiaomi, visuelle Layoutfreigabe, echte Mikrofontests | **NOT RUN** für v0.4.91 |
| Automatischer PNG-Speicherzyklus/Panelwechsel, 120-s-WAV-Regression, 2–25-Hz-Kalibrierung | **NOT RUN** für v0.4.91 |

Ein erfolgreicher synthetischer Test beweist noch nicht, dass genau der v0.4.90-Gerätefehler auf dem Xiaomi beseitigt ist. Er zeigt den durchgängigen neuen Datenpfad und stellt messbare Stufenwerte für den Gerätetest bereit.

## 4. Windows-11-Test in Reihenfolge

1. ZIP in einen **neuen** Ordner `LFN-Scout_Android_v0.4.91` entpacken.
2. `PREPARE_PROJECT.bat` ausführen, auf `PASS: Projekt vorbereitet.` warten.
3. `gradlew.bat :analyzer-core:test --stacktrace`.
4. `gradlew.bat :app:testDebugUnitTest --stacktrace`.
5. `gradlew.bat assembleDebug --stacktrace`.
6. Bei allen PASS Android Studio Run/Install auf Xiaomi. Alte Installations-/App-Daten möglichst vorab sichern; Debug-Signatur muss kompatibel sein.

## 5. Xiaomi-Gerätetest – zuerst Datenfluss, dann Bild

1. Rolling-Buffer aktivieren; Analyzer starten; ca. 5 s warten. **Diagnose** öffnen. Rohquelle muss 8.000 Hz anzeigen, Rohpeak und RMS sollten sich mit einem hörbaren Testton oder Sprechen ändern; LF-Peak/RMS müssen auf tieffrequente Signale reagieren. Bei stabilem Eingang sollen die Live-Kurven und der klassische Wasserfall erscheinen.
2. Wenn Rohpeak >0, LF-RMS aber 0: Fehler in Filter-/Source-Datenweg. Wenn LF-RMS >0, aber Live ohne Kurve: FFT-/Validierungs-/Skalenproblem. Wenn kein Rohblock eintrifft: Rolling-/Recorder-/Hub-Zustand, Drops und Alter prüfen. Genau diese Werte als Screenshot schicken.
3. Anzeige 20/50/100/200 Hz, Gelb/Rot/Grau, Cursor, Pause, Wasserfallvergrößerung, ggf. Analyzer-Gain 0 und 20 dB prüfen. Gain nicht als absolute SPL-Kalibrierung interpretieren.
4. Manuell PNG, Auto-PNG nach kompletter Historie (~120 s) und nach Panelwechsel testen. Der Wert „PNG-Auftrag angenommen“ ist noch keine bestätigte erfolgreich gespeicherte Datei – im Downloadordner nachsehen.
5. Bestandsregression: 120-s-WAV aufnehmen, bisherigen WAV-Wasserfall erstellen, Rolling fortsetzen; Bewertungen und Seismik prüfen. Keine automatische Bestätigung ohne Tests.

**Zurückgestellt:** Impuls-Wasserfall/-Analyse, Mithören mit Audiowiedergabe, 48-kHz-/20-kHz-Breitband, Standalone-APK, volle Histogrammspalte und komplette formale T-01…T-71-Abnahme.

**Datenschutz:** Die vollständige ZIP enthält private Supabase-Recovery-Daten einschließlich 53 WAVs. Nicht unverändert öffentlich in GitHub hochladen.
