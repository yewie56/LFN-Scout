# LFN-Scout v0.5.31 – Windows-11-Build und Test

## Status
Source Candidate, kein in dieser Umgebung nachgewiesener Android-/Gerätetest. VersionCode 178.

## Build unter Windows 11
1. ZIP nach `C:\LFN531` entpacken (Dateien direkt im Verzeichnis).
2. Bei verbundenem, entsperrtem Android-Gerät mit aktiviertem USB-Debugging `VORTEST_INSTALL.bat` starten.
3. `Builds\LAST_VORTEST_RESULT.txt` und die neue `VORTEST_v0.5.31_*.log` prüfen; nur tatsächliche PASS-Werte gelten.
4. Falls kein Gerät angeschlossen ist, zunächst `VORTEST.bat` nutzen.

## Gerätetest
1. In „Audio / Statistik lokal“ den Hauptschalter „30-Minuten-Statistik aktivieren“ einschalten.
2. Gesamtstatistik MIN=2, MAX=20 unverändert lassen.
3. „Rolling-Filterstatistik aktivieren“ einschalten; FILTER MIN=0, FILTER MAX=100, Filterbänder=10; CSV-Schalter aktiv lassen; speichern.
4. Rollingbuffer starten und bis zur nächsten Uhrzeitgrenze :00 oder :30 laufen lassen.
5. Unter `Download/LFN-Scout/` die Datei `HalfHourFilterStatistics_<bucketStart>.csv` suchen. Es müssen 10 Datenzeilen plus Kopfzeile sein, 0–10 Hz bis 90–100 Hz.
6. Mit 20 Bändern wiederholen; jeweils 5 Hz breit, 20 Datenzeilen.
7. Filterstatistik ausschalten und wieder über eine Halbstundengrenze laufen lassen: keine neue Filter-CSV.
8. Gleichzeitig Rolling-Analyzer betreiben: keine doppelte Audioerfassung oder doppelten CSV-Zeilen.
9. Rollingbetrieb manuell stoppen: unvollständiger Bucket als PARTIAL, sofern gültige Epochen vorliegen.
10. Bestehende WAV-Aufnahme, WAV-Filter-CSV, Rating und Cloud-Upload auf Regressionen prüfen.
11. Testprotokoll und beide CSV-Dateien hochladen.

## Bekannte Grenzen
- First-order Hoch-/Tiefpass ist vorläufig; der untere Filter 0–10 Hz beinhaltet in dieser Diagnosefassung auch DC.
- Exakte Filterübertragungsfunktion und Pegelrichtigkeit noch nicht gegen Kalibriersignale validiert.
- Keine Filterdaten zu Supabase; keine SD-Kartenwahl oder Sekunden-CSV in dieser Version.
- Halbstd.-CSV und Gesamtstatistik sind getrennt ein-/ausschaltbar; Filterbank standardmäßig AUS.
