# LFN-Scout v0.5.11 – Geräte- und Buildtest

**Noch nicht auf Windows/Android/Xiaomi ausgeführt.** Ergebnisse nach Durchlauf mit Datum, Gerät und Screenshots eintragen.

1. Projekt frisch entpacken, `PREPARE_PROJECT.bat`, dann `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`; ggf. alle Kotlin-Fehler beilegen. Status NOT RUN.
2. 8-kHz-Anzeige wie vorher mit ORIGINAL EIN, FILTER AUS; Kontroll-Farbskala Inferno, Zeitband, Cursor, Flucht, Colorbar; Datei manuell/automatisch. NOT RUN.
3. Filter-Buttons exakt 48 dp gegenüber Kamera, ORIGINAL = Inferno-Mini-Spektrogramm; FILTER = **fünf symmetrische** Magenta/Weiß-Balken (ungeachtet N), kein separates Impulsicon. NOT RUN.
4. Vier Modi ORIGINAL, FILTER, BEIDE, KEINES; Sichtbarkeit ändert laufenden Recorder nicht. NOT RUN.
5. FILTER MIN/MAX 10/60 und N=10/20/40; nach STOP wählen, START, prüfe Filter-Signalverteilung bei digitalen 35/50-Hz-Tönen und Impulsburst. Kein unkalibrierter absoluten Pegel. NOT RUN.
6. 5 ms Hop des Direkt-Analyzers (z. B. 8 kHz, 8:1), STOP/START und Setup-4-Roundtrip; Glättung auf Zeitkonstante 5 s umschalten, Werte speichern und laden. NOT RUN.
7. JPG und PNG jeweils in FILTER-allein und BEIDE; Original-Spektrum bleibt frequenzrichtig, Bild und Gerät gleiche gewählte LUT, Zeitachse und Colorbar. NOT RUN.
8. Long-run und Wechsel zwischen internen Panels bei laufendem Analyzer; STOP und anschließend Panel verlassen; Rolling nur an vorgesehenem Zeitpunkt zurücksetzen. NOT RUN.
9. Altes Setup ohne Filterfelder laden, kein Crash. 8-kHz-Direkt-Modus und 48 kHz ohne ungewollte Audioänderung testen. NOT RUN.
10. Test der echten Impulslatenz und Fehlalarme erst nach Implementierung von LFN-IMP-001 Stufe B. NOT IMPLEMENTED.

**Keine Gerätefreigabe ohne tatsächliches BUILD SUCCESSFUL und Gerätemessungen.**
