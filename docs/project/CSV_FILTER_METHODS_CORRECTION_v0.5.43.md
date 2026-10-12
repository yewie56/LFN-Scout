# LFN-Scout v0.5.43 – Korrektur CSV-Filterauswahl (12.10.2026)

## Verbindlicher Signalfluss
Die v0.5.42-Auswahl im FAST-/Analyzer-Filterwasserfall war eine Fehlzuordnung und ist **vollständig zurückgenommen**. Die betroffenen Analyzer-Quellen wurden bytegleich aus v0.5.41 wiederhergestellt: `LiveAnalyzerController`, `LiveAnalyzerSettingsPanel`, `AnalyzerInspectionExport`, `AnalyzerLegacySetupMigration`, `AnalyzerRawLfSession`, `FastFilterBank` sowie `FastFilterBankTest`. Der Original-FFT-Analyzer und **beide Analyzer-Wasserfälle** bleiben unverändert.

Die Wahl Legacy-IIR / Butterworth HP2+LP2 / FFT-Hann ist ausschließlich in `RollingFilterHalfHourStatistics.kt` wirksam, nicht im Analyzer, nicht in der WAV-eigenen Filter-CSV und nicht im Cloud-Pfad. Menü: `Audio / Statistik lokal` → Rolling-Filterstatistik aktivieren → CSV-Filterverfahren wählen → speichern → Rolling-Messung neu starten.

- Legacy-IIR: bisherige einpolige HP/LP-Kaskade für Rolling-Band-CSV; unveränderter Standard.
- Butterworth4: getrennte Butterworth-Hoch- und Tiefpässe 2. Ordnung (bei DC nur LP2), nicht kalibriert.
- FFT-Hann: 1024-Punkte Radix-2-FFT eines 1-s-Signalblocks bei 1 kHz mit Hann-Fenster, einseitig und auf Fensterenergie normalisiert; 1-s zeitliche Aggregation; **kein kontinuierlicher Filter**.
- Rolling-Filter: 0–100 Hz, 10 oder 20 Bänder als Standard; freies MIN/MAX im vorhandenen Bereich. Filterstatistik standardmäßig AUS.
- CSV-Spalten, Dateinamen, Epochengrenzen, Halbstundenlogik und Cloud unverändert. Die CSV-Datei enthält noch **keine explizite Methodenspalte**; zum objektiven Vergleich den verwendeten Setupwert / Debug-Log separat dokumentieren. Dies ist eine offene Verbesserungsaufgabe.
- Methoden unterscheiden sich im Pegel auch ohne Programmfehler. Keine automatische physikalische Vergleichbarkeit. Bandkanten/DC/Fensterleckage und transiente Signale gesondert kalibrieren.

## Performance
Die Rolling-CSV-Verarbeitung misst mit `System.nanoTime()` die Rechenzeit pro übergebenem PCM-Sample einschließlich Abtast-Messaufwand sowie den einmal je Sekunde ausgeführten FFT-Rechenanteil. `performance()` liefert Samples, mittlere Mikrosekunden/Sample, maximales Sample-Intervall und den geschätzten Echtzeitanteil. Das ist **keine** Smartphone-Gesamt-CPU- oder Batterie-Messung; FFT-Spitzen der 1-s-Blockberechnung sind nicht mit dem einzelnen Sample-Maximum gleichzusetzen. Vergleiche auf gleicher Android-Hardware mit identischem PCM und gleicher Bandanzahl sind noch offen. Die vorhandene Rolling-Debugausgabe protokolliert die Methode je 30-min-Bucket; Performance-Daten sind derzeit intern abfragbar, noch keine Live-GUI.

## Tests / Grenzen
- Isolierter `kotlinc`-Build des Statistik-Filters: PASS.
- Synthetischer 35-Hz-Test, 6 s, alle drei Methoden: PASS, endliche Pegel, jeweils 10 CSV-Bandzeilen; Legacy-Maximum im 20–30-Hz-Band durch starke Überlappung, neue Verfahren im 30–40-Hz-Band. Kein Beweis für Kalibrierung.
- Abgleich der 7 Analyzer-Dateien mit v0.5.41: bytegleich wiederhergestellt.
- Android-Gradle-Build / Android-JUnit / Gerätetest: **NICHT BESTÄTIGT**. Unter Windows 11 `VORTEST_INSTALL.bat` ausführen. CSV-Export mit 10/20 Bändern, Pause/STOP, Halbstundenwechsel, Auswahlpersistenz und gleichzeitigen Analyzer-Anzeigen am Gerät verifizieren.

## Aufgaben / Korrekturen
Die zusätzliche Statistik-Auswahl betrifft **nicht** den FFT/FAST-Wasserfall. Die alte v0.5.42-Grafik und `FILTER_METHODS_RUNTIME_v0.5.42.md` sind historisch/falsch zugeordnet und dürfen nicht als aktuelle Architektur verstanden werden. Korrekte Architektur: `docs/project/LFN_Scout_Signalfluss_KORRIGIERT_v0.5.43.svg`. Alle Cloud-Queue-/24-h-Retention-Aufgaben und lokal-only-Impulskennwerte bleiben zurückgestellt bzw. unverändert.
