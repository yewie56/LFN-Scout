# LFN-Scout v0.5.35 – Build- und Gerätetest

1. Projekt ZIP nach C:\LFN535 entpacken.
2. Android via USB/ADB verbinden. `VORTEST_INSTALL.bat` starten.
3. `Builds/LAST_VORTEST_RESULT.txt` und Log hochladen.
4. Auf dem Handy Rollingbuffer, 30-Minuten-Statistik und lokalen Gesamt-CSV-Export einschalten.
5. In einem laufenden Halbstundenfenster mindestens eine Bewertung `0` und eine Bewertung `4` absenden. Optional eine automatische Nichtantwort -1 durch Erinnerungs-Timeout abwarten.
6. Nach HH:00/HH:30 `Download/LFN-Scout/HalfHourStatistics_*.csv` kontrollieren: `observation_count=2`, `observation_mean=2`, `observation_min=0`, `observation_max=4`. -1 zaehlt separat; Werte koennen bei anderen Beobachtungen abweichen.
7. CSV hochladen. Bei `0` Beobachtungen bleiben mean/min/max LEER, nicht 0.

**Einschraenkung:** Ereignisse von vor der Installation werden nicht rueckwirkend erfasst. Die lokalen Daten werden max. 45 Tage / 4000 Ereignisse vorgehalten. Keine Datenbank-/Cloud-Aenderung. Android-Build noch nicht erfolgt.
