# LFN-Scout v0.5.40 – Windows 11 Build und Filterbankreport

1. Vollständiges Projekt-ZIP direkt nach `C:\LFN540` entpacken (ohne zusätzliche Verzeichnisebene).
2. `FILTERBANK_REPORT_STARTEN.bat` starten. Benötigt Python 3, ausschließlich Standardbibliothek. Ergebnis in `FilterbankReport_v0.5.40\VALIDIERUNGSBERICHT.md`. Die SVG-Dateien lassen sich mit Firefox öffnen; CSV ist semikolongetrennt und mit Excel kompatibel.
3. Handy via USB verbinden, USB-Debugging freigeben und `VORTEST_INSTALL.bat` ausführen.
4. `Builds\LAST_VORTEST_RESULT.txt`, VORTEST-Log und `FilterbankReport_v0.5.40\VALIDIERUNGSBERICHT.md` hochladen.

Parameter für alternative Konfiguration:

`py -3 tools\filterbank_validation_report.py --min-hz 10 --max-hz 60 --seconds 8 --output FilterbankReport_10_60`

Beachte: Die Python-Simulation ist getrennt von Android-Gradle-Tests; sie bildet den aktuell implementierten Kotlin-Filteralgorithmus nach. Es findet **keine** Messung des Mikrofonfrequenzgangs oder realer akustischer Übersprechdämpfung statt. Die Filter der ersten Ordnung sind breitbandig; nominale Grenzen stellen keine scharfe spektrale Trennung dar. Weder App-Bildschirm noch Cloud-API wurden geändert. Die tatsächliche 24-h-Bereinigung und der Cloud-Tabellenabgleich sind weiterhin offen.
