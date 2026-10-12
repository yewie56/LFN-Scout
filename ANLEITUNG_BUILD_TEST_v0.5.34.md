# LFN-Scout v0.5.34 — Build und Funktionstest (Windows 11)

1. Dieses ZIP direkt nach `C:\LFN534` entpacken, ohne zusätzliches Elternverzeichnis im ZIP.
2. Android-Smartphone über USB anschließen, entsperren und USB-Debugging erlauben.
3. `VORTEST_INSTALL.bat` ausführen. Anschließend `Builds\LAST_VORTEST_RESULT.txt` und aktuelles `Builds\VORTEST_*.log` auf sieben PASS-Schritte prüfen.
4. Auf dem Handy unter Audio-Statistik die kontinuierliche Halbstundenstatistik und Gesamt-CSV aktivieren. Rollingbuffer laufen lassen und mindestens eine Halbstundengrenze abwarten.
5. `Download/LFN-Scout/HalfHourStatistics_*.csv` öffnen. Vier neue Spalten prüfen: `peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`.
6. Optional: Sekunden-CSV desselben Fensters hochladen, um Energieanteile und Leq–L50 unabhängig nachzurechnen. Nur vollständige Sekundendaten geben eine vollständige Reproduktion.
7. Bestehende Rollingfilter-CSV, Analyzer-Aufrufen und Rollingbuffer-Fortsetzung kontrollieren (Regression).

**Prüfstatus:** Android-Build, JUnit und ADB-Gerätetest v0.5.34 sind hier NICHT ausgeführt. Keine kalibrierte Impulsmessung. Subjektive Ratings in Halbstunden-CSV weiterhin offen.
