# Rolling-Halbstundenstatistik v0.5.29 – Teilimplementierung

## Implementiert
- Bestehenden Rollingbuffer-PCM-Stream bei 1 kHz ohne zweiten Recorder als Quelle verwendet.
- Globaler Schalter (Standard EIN) und eigenständige editierbare Grenzen, Standard 2–20 Hz, in Audio/Statistik lokal; SharedPreferences persistent.
- Kontinuierliche RAM-Aggregation von 1-s-Epochen auf festen 30-min-Grenzen; Leq energetisch und L50/L90 aus den Sekundenpegeln.
- RAM-Resultate derzeit nur als `ROLLING_HALF_HOUR_RAM_RESULT` in den Diagnose-Logs.
- Vorläufiger 1.-Ordnung-Hoch-/Tiefpass; **nicht norm- oder kalibrierungsvalidiert**.

## Nicht implementiert / keinesfalls als PASS werten
- Persistente Halbstundenstatistik, Recoverability, Cloud-Upload, sichere CSV, Beobachtungsmittelwerte, Filterbank (10/20), Sekundenexport, SD-Kartenziele.
- Aktive Einstellungen während eines laufenden Rollingbetriebs ohne Neustart übernehmen.
- Beenden eines angefangenen Halbstundenfensters beim Stop und Neustart; bislang wird nur beim Uhrzeit-Bucketwechsel ein Logresultat erzeugt.
- Robuste Datums-/Zeitkorrektur und tatsächliche AudioTimestamp-Messung; verwendet geschätzte Systemzeit aus kumulierten Samples.
- Gain-Kompensation und Filterverifikation durch Referenzsignale.

## Regressionstests
`RollingHalfHourStatisticsTest.kt`: Bucketwechsel, unvollständige Epochen und ungültige Bandgrenzen. Android-Unit-Testlauf steht noch aus.

## Tests nach Windows-Build
1. `VORTEST.bat` und `VORTEST_INSTALL.bat` nutzen. Nur tatsächlich gelaufene Schritte als PASS markieren.
2. Rolling einschalten; unter Audio/Statistik lokal Schalter und 2–20 Hz kontrollieren.
3. Über eine Halbstundengrenze messen und Debuglog `ROLLING_HALF_HOUR_RAM_RESULT` prüfen.
4. Analyzer zeitweise öffnen/schließen; sicherstellen, dass Rolling weiterläuft.
5. Schalter ausschalten und neuen Rollinglauf starten: keine neuen Halbstunden-Resultate.
6. Alt-Funktionen wie WAV-Speichern, Cloud-Uploads, Audio-Beurteilung regressiv testen.

**Dies ist bewusst kein fertiges v0.5.29-Produktrelease.**
