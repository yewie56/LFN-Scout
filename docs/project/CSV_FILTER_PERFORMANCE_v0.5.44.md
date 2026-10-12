# LFN-Scout v0.5.44 – CSV-Filter-Performance

## Geltungsbereich
Nur `RollingFilterHalfHourStatistics` für die lokale CSV-Dokumentation. **Keine Änderungen** an Original-FFT, FAST-/Impuls-Analyzer, beiden Wasserfällen, WAV-CSV, Cloud, Statistik-CSV-Schema oder Halbstunden-Buckets. Das Signalflussdiagramm aus v0.5.43 bleibt maßgeblich.

## Messung
Beim eingeschalteten Rolling-CSV-Filter werden jeweils nach 60.000 übergebenen 1-kHz-PCM-Samples die CPU-Ausführungszeiten als separates `Download/LFN-Scout/RollingCsvFilterPerformance.csv` gespeichert. Der letzte Teilblock wird bei STOP zusätzlich ausgegeben. Jede Zeile enthält Fensterbeginn/-ende, Filtermethode, Bandzahl 10/20, Sample-Rate, Sampleanzahl, mittlere/Maximalzeit pro Sample (µs), größte Sekunden-Filter-Rechenzeit inklusive FFT (µs), P50/P95/P99 dieser sekündlichen Rechenzeiten (µs), Sekundenanzahl, Gesamtzeit (µs) und Echtzeitanteil (%). Die Ausgabe ist eine **Performance-Diagnose**, kein Kalibrierungsnachweis und keine systemweite CPU-/Akku-Messung. Die `System.nanoTime()`-Instrumentierung verursacht Overhead. Zeitliche Spitzen im Android-Scheduler können erheblich schwanken; mehrere Durchläufe messen.

### Vergleichsplan
Jedes der Verfahren Legacy-IIR, Butterworth HP2+LP2 und FFT-Hann bei 10 und 20 Bändern auf demselben Smartphone jeweils mindestens einige Minuten laufen lassen. Möglichst gleiches Eingangssignal und identische Android-Einstellungen benutzen. `mean_us_per_sample`, `p95_epoch_us`, `p99_epoch_us` sowie `realtime_percent` vergleichen. FFT wird als fensterbasierte Bandenergie interpretiert, nicht als kontinuierlicher Durchlassfilter. Das Umschalten erfordert einen neuen Rolling-Start. Die bestehende CSV der Pegelmesswerte wird **nicht** verändert.

### Bisherige Prüfungen
- Isolierte Kotlin-Hostkompilierung: PASS.
- Synthetische 35-Hz-Hostprobe: 3 Verfahren × 10/20 Bänder = 6/6 PASS; Samplezählung, Performance-Metadaten, Reset und P50≤P95≤P99 geprüft.
- Android-Gradle / Android-JUnit / ADB / Langzeittest v0.5.44: **OFFEN**. `VORTEST_INSTALL.bat` auf Windows ausführen. App-Regression (beide Wasserfälle unverändert) nochmals prüfen.
- MediaStore-Append `wa` ist geräteabhängig und muss praktisch geprüft werden. Bei Fehlern `ROLLING_FILTER_PERF_CSV_ERROR` im Debug-Log beachten; niemals einen Export als bestätigt markieren, bevor Datei und Zeilen geprüft sind.

**Versionsbasis:** v0.5.43: Windows-Build, JUnit, APK, ACTIVE, ADB und sechs Funktionstests auf dem Gerät vom Nutzer mit PASS gemeldet.
