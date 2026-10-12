# LFN-Scout v0.5.32 – Windows-11-Build und Test

## Status
Source Candidate; die v0.5.31 erfolgreich geprüfte APK ist **nicht** gleichzusetzen mit einem v0.5.32-Test. Diese Version ergänzt nur neue Diagnose-Regressionstests für die experimentelle Rolling-Filterbank. Die neuen Tests prüfen keine absolute Frequenzgangkalibrierung.

## Build / Installation
1. ZIP in neues Verzeichnis wie `C:\LFN532` entpacken (Dateien bereits direkt in ZIP-Wurzel).
2. Handy über USB verbinden, USB-Debugging erlauben.
3. `VORTEST_INSTALL.bat` doppelklicken. Alternativ ohne Gerät `VORTEST.bat`.
4. Prüfen: `Builds\LAST_VORTEST_RESULT.txt` und `Builds\VORTEST_*.log`.
5. Erwartet sind PREPARE, UNIT TESTS, ASSEMBLE DEBUG, APK, ACTIVE ZIP, ADB DEVICE und INSTALL jeweils PASS (ohne Handy nur erste fünf).
6. Bei FAIL Log hochladen; einen übersprungenen Schritt keinesfalls als PASS werten.

## Neue Regressionstests
`app/src/test/java/de/brummbeobachter/app/RollingFilterFrequencyValidationTest.kt` prüft:
- Frequenzbänder von 0 bis 100 Hz für 10 und 20 Filter
- synthetische Sinussignale mit 3, 7, 15, 35, 75, 95 Hz und endliche Pegel
- Verdoppelung der Eingangsamplitude ergibt annähernd +6,02 dB
- Stille, sehr kurze (ungültige) Epochen und keine erfundenen Daten

## Xiaomi-Gerätetest
1. Nach ADB-Installation die App starten. Einstellungen > Audio/Statistik lokal.
2. 30-Minuten-Statistik EIN, Filterstatistik EIN, 10 Filter, Bereich 0–100 Hz, CSV EIN.
3. Rollingbetrieb starten, Halbstundengrenze abwarten; auch Analyzer parallel starten und stoppen.
4. `Download/LFN-Scout/` auf `HalfHourStatistics_*.csv` und `HalfHourFilterStatistics_*.csv` prüfen.
5. Test mit 20 Filtern wiederholen und sicherstellen, dass die CSV 20 Filterzeilen erhält.
6. Statistik / Filterstatistik AUS: keine neuen Filterzeilen oder unnötige Filterberechnungen.
7. Ergebnis, CSV-Dateien und Log zur Analyse bereitstellen.

## Offene Aufgaben
Sekunden-Epochen-CSV, subjektive Beobachtungswerte in jeder Gesamt-CSV, genaue Filterdämpfung und Übergangsbereiche mit Soll-Toleranzen, SD-Zielwahl, Cloud-Halbstunden-Upload und automatischer Outbox-Retry. Die bereits bestehenden Filter sind erster Ordnung und werden durch neue Tests **nicht kalibriert**.
