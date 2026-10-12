# LFN-Scout v0.5.05 – Windows- und Xiaomi-Abnahme (noch nicht durchgeführt)

## A – Build und Unit-Tests

- [ ] Frisch entpacken, `PREPARE_PROJECT.bat` ausführen.
- [ ] `gradlew.bat :app:testDebugUnitTest --stacktrace` -> BUILD SUCCESSFUL, alle Tests PASS.
- [ ] `gradlew.bat assembleDebug --stacktrace` -> BUILD SUCCESSFUL. APK installieren ohne Datenlöschen.

## B – Kritischer 48-kHz-Absturz

- [ ] Rolling EIN → 48 kHz → 20 s messen → STOP → Panel verlassen; Rolling läuft erneut.
- [ ] App darf nicht abstürzen, insbesondere nicht beim erneuten Öffnen des Analyzer-Panels.
- [ ] 48-kHz-Zyklus ≥10-mal wiederholen. Nach jeder Runde ggf. Logcat; keine `IllegalArgumentException` in `AnalyzerTiming.lfRateHz`.
- [ ] `RESTORE_RESULT restored=true` vor `RESTORE_DONE`; korrekte `sid`.
- [ ] Rolling AUS → 48 kHz → STOP + Panel verlassen → Rolling bleibt AUS.
- [ ] 8-kHz-Modus sowohl mit Rolling EIN als auch AUS; START, STOP, Panelwechsel unverändert.

## C – PAUSE/STOP

- [ ] RUN → PAUSE: Wasserfall-/Spektrumhistorie bleibt sichtbar; kein Rolling-Restore.
- [ ] PAUSE → RUN: Historie wird weitergeführt.
- [ ] RUN → STOP: Messung AUS, Betriebsart unverändert. Nur STOP + Panel verlassen löst Restore aus.

## D – Setup- und Diagnoseexport

- [ ] Vorhandene Slots nach Update im Download/LFN-Scout/Setups/ prüfen; slot 4 nach erstem 48-kHz-Start.
- [ ] Setup-Name, Beschreibung, FFT, Wasserfall, Statistik, Bildformat/Qualität speichern; JSON neu lesen (tatsächlich passender Wert, valides JSON).
- [ ] Wiederholtes Speichern aktualisiert Setup-Kopie, ohne beliebige Dateiduplikate zu erzeugen.
- [ ] Diagnose-ZIP manuell exportieren, entpacken, JSON-Dateien, README und AUDIO_DIAG prüfen.
- [ ] Keine Supabase-Secrets, Teilnehmerdaten, GPS/Beobachtungen oder Audio-Rohdaten im Diagnose-ZIP.
- [ ] Bei Exportschreibfehler bleiben die internen Setups unverändert.

## E – Regression und offen

- [ ] v0.5.04-Testplan (Audio, FFT, 8/48 kHz, Testsignale, PNG/JPG, Rolling, WAV, Bewertung, Seismik) erneut durchlaufen.
- [ ] 16:1-Energieabweichung mit 50/195 Hz separat prüfen: **NICHT als behoben markieren**.
- [ ] Akt/Average/Maxhold einzeln und MAX/CLR: **noch nicht implementiert**.
