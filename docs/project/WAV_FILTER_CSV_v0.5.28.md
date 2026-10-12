# LFN-Scout 0.5.28: Optionaler WAV-Filter-CSV-Export

## Implementierung
- In **Audio / Statistik lokal** neue persistente Checkbox **WAV-Filterstatistik als CSV exportieren (je WAV)**, Standard AUS.
- Gilt für Observer- und Analyzer-WAV-Auswertungen aus `StatisticsWindowEngine`, bis zu 40 Filterbänder gemäß deren konkreter Auswertungskonfiguration.
- Pro gespeicherter WAV ein eigenes, im Android-Download-Ordner direkt zugängliches CSV `Download/LFN-Scout/WavImpulseFilterStatistics_<recording_id>.csv`. Kein privater App-Speicher.
- UTF-8, Semikolon-Trennzeichen, quoted CSV fields; 1 Zeile pro Filter, Frequenzgrenzen und alle benannten statistischen Metriken.
- Keine zusätzliche Audioberechnung; es wird das bereits erzeugte Statistik-JSON verwendet.
- CSV-Ausgabefehler werden geloggt, dürfen die WAV-Speicherung und Cloud-Übertragung nicht abbrechen.
- Keine fortlaufende Sammeldatei, sondern eine Datei je Aufnahme, zur Vermeidung teurer vollständiger Datei-Rewrites und zur leichten Zuordnung.
- Die Bezeichnung „Impulsfilter“ in der CSV-Option bezieht sich auf die vorhandene WAV-Filterstatistik. Eine eigenständige Impulsereignis-Erkennung ist damit **nicht** implementiert (`impulses.status=NOT_IMPLEMENTED`).

## Noch nicht umgesetzt
- Die kontinuierliche, Rolling-gebundene 30-Minuten-Statistik mit Leq/L50/L90, subjektivem Mittelwert, 10/20 Filterbändern, Epochen-CSV und lokalen Exportoptionen.
- SD-Karten-Zielauswahl getrennt für WAV und Statistik.
- Unabhängiger periodischer Retry-Worker für Cloud-Statistik.

## Prüfanweisung
1. `VORTEST.bat` unter Windows: PREPARE / Unit-Tests / assembleDebug / APK / ACTIVE ZIP müssen jeweils PASS sein.
2. `VORTEST_INSTALL.bat` mit genau einem autorisierten Android-Gerät.
3. In Audio / Statistik lokal CSV-Option deaktivieren, WAV als Observer und Analyzer speichern: Es wird keine neue CSV angelegt.
4. CSV-Option aktivieren, erneut je eine Aufnahme speichern. Im Dateienmanager `Download/LFN-Scout/` suchen: `WavImpulseFilterStatistics_<recording_id>.csv`.
5. CSV in Excel importieren: Trennzeichen Semikolon, Anzahl der Datenzeilen muss der Zahl der ausgewerteten Filter entsprechen; Index beginnt bei 1.
6. Ohne Filter/bei Speicherfehler darf die WAV-Speicherung nicht abbrechen. Supabase-Funktion unverändert prüfen.
7. Einstellung nach App-Neustart prüfen; danach wieder ausschalten und sicherstellen, dass keine neue CSV entsteht.
8. Alte Setups/Einstellungen müssen weiterhin geladen werden.

Android-/Gerätetest für v0.5.28 bislang **nicht nachgewiesen**.
