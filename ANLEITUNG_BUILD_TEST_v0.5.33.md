# LFN-Scout v0.5.33 – Windows 11 Build und Gerätetest

## 1. Installation

1. Gesamtes ZIP nach `C:\LFN533` entpacken, **ohne zusätzliches Unterverzeichnis**.
2. Telefon per USB verbinden und USB-Debugging bestätigen.
3. `VORTEST_INSTALL.bat` aufrufen.
4. `Builds\LAST_VORTEST_RESULT.txt` und `Builds\VORTEST_*.log` prüfen und zur Auswertung hochladen.

Ein PASS bestätigt Unit-Tests, Debug-Build, Paketierung und ADB-Installation; keinen Live-Gerätetest.

## 2. Neues in 0.5.33

* Einstellungsdialog `Audio / Statistik lokal`: `Sekundenwerte-CSV` schaltbar, Standard AUS.
* Voraussetzung: `30-Minuten-Statistik aktivieren` EIN. Weitere Filterspalten nur bei `Rolling-Filterstatistik aktivieren` EIN.
* Der Sekunden-Export nimmt PRE-GAIN-Pegel aus dem 1-s-Statistikpfad.
* `Download/LFN-Scout/HalfHourEpochs_<bucket_start_ms>.csv`: ein Datensatz je gültiger Sekunde, Gesamt- und bei aktivierter Filterstatistik 10/20 Bandspalten.
* Speichern in RAM, Snapshot alle etwa 300 gültigen Sekunden bzw. am Bucket-Ende; kein Schreiben pro Sekunde.
* Die Filter bleiben unkalibriert. Keine neuen Cloud-Requests.
* Angepasster sichtbarer Text: Cloud statt Supabase. Interne Backendbezeichner bleiben kompatibel.

## 3. Gerätetest

1. App öffnen, 30-Minuten-Statistik EIN, Rolling-Filterstatistik EIN (10 Bänder), Sekundenwerte-CSV EIN.
2. Speichern, Rollingbuffer neu starten (Einstellungen werden bei Start übernommen).
3. Mindestens 6 min aufzeichnen und prüfen, ob `HalfHourEpochs_*.csv` im Download-Ordner liegt.
4. Über eine HH:00/HH:30-Grenze laufen lassen; neue Gesamt-, Filter- und Sekunden-CSV prüfen.
5. `HalfHourEpochs_*.csv` zusammen mit `HalfHourStatistics_*.csv` und `HalfHourFilterStatistics_*.csv` hochladen.
6. Sekunden-CSV AUS, Rollingbuffer neu starten, prüfen, dass keine neue Sekunden-Datei entsteht.
7. Analyzer öffnen/schließen und prüfen, dass Rollingbuffer weiterläuft.

## 4. Offen

* Subjektive Beobachtungsaggregate aus lokaler verlässlicher Quelle: **nicht implementiert**.
* Exakte Filterfrequenzgangkalibration und Soll-Ist-Report: offen.
* SD-Karten-Ziele, Cloud-Halbstunden-Upload und Retry: offen.

Status: Source Candidate. Android-Gradle-, ADB- und Gerätetests für v0.5.33 noch nicht ausgeführt.
