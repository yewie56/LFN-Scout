# LFN-Scout v0.5.30 – Windows 11 Build & Test

1. ZIP in kurzen neuen Pfad entpacken, etwa `C:\LFN530`. Alle Dateien liegen ohne zusätzlichen Top-Ordner im ZIP.
2. USB-Debugging auf dem Handy aktivieren und USB-Kabel verbinden.
3. `VORTEST_INSTALL.bat` ausführen. Es führt PREPARE, `:app:testDebugUnitTest`, `assembleDebug`, APK-Prüfung, ACTIVE-ZIP und ADB-Installation aus.
4. Bei FAIL bitte `Builds/LAST_VORTEST_RESULT.txt` sowie `Builds/VORTEST_v0.5.30_*.log` bereitstellen.
5. Unter „Audio / Statistik lokal“ die Statistik EIN und „Halbstunden-Gesamtwerte als CSV“ EIN lassen. Standard Gesamtband 2–20 Hz.
6. Rollingbuffer vor einer :00/:30-Grenze starten; danach im öffentlichen Ordner `Download/LFN-Scout/` nach `HalfHourStatistics_<Millis>.csv` suchen.
7. CSV prüfen: eine Kopfzeile, eine Ergebniszeile, plausible Bucketgrenzen, nicht mehr als 1800 Epochen; Kennzeichen `PRE_GAIN_PCM_DIAGNOSTIC`.
8. Einen zweiten Lauf mit CSV AUS testen: keine neue Datei, Rollingbuffer und WAV unverändert.
9. Gain 0 und Gain 50 getrennt testen, bei möglichst gleichem Eingangssignal. Die Statistik sollte nicht allein wegen der digitalen Verstärkung 50 dB höher liegen. Die Filterwerte sind noch NICHT kalibriert.
10. Rollinganalyzer starten/beenden: Rollingbuffer und Halbstundenstatistik sollen weiterlaufen. Erzeugte Logs auf `ROLLING_HALF_HOUR_RESULT`, `ROLLING_HALF_HOUR_CSV_OK` bzw. `_ERROR` untersuchen.
11. Rollingbuffer kontrolliert stoppen: Für ein angebrochenes Fenster soll PARTIAL geschrieben werden. Bei abruptem Systemabsturz ist diese Sicherung noch nicht garantiert.

**Grenze:** Erfolgreiches Build/Install ≠ wissenschaftlich validierte Pegelmessung. Statistik-Cloud-Upload und Beobachtungsmittelwerte fehlen weiterhin.
