# LFN-Scout v0.5.41 – Vergleich Legacy-IIR / Butterworth4 / FFT

Erstellt: 2026-10-11T22:05:54

**Offline-Self-Test: 18/18 PASS, 0 FAIL.** Einzelheiten: `SELFTEST.csv`. Dies ist **kein** Android-Build, kein Mikrofonversuch und keine physikalische Kalibrierung.

## Aufbau und Normierung

- Abtastrate **1000 Hz**, identische nominale Bandgrenzen für 10 und 20 Bänder, jeweils **0–100 Hz und 10–60 Hz**. Die FFT misst fensterweise **quadratische Bandenergie**, nicht einen kontinuierlichen gefilterten Sample-Strom.
- Legacy-IIR: unveränderte HP/LP-Rekursion erster Ordnung aus `RollingFilterHalfHourStatistics.kt`, HP für untere Grenze 0 Hz **weggelassen**, LP bleibt aktiv. Zustände anfangs null.
- Butterworth4: bei unterer Grenze >0 Hz **Gesamtordnung vier** (`butter(2, bandpass)` -> vier Pole), SOS; bei 0 Hz **4. Ordnung Tiefpass**, kein 0-Hz-Hochpass. Kein Zero-Phase-`filtfilt`, sondern kausal.
- FFT-Hann: Hann-Fenster **4096 Samples = 4.096 s**, Hop **1024 Samples = 1.024 s**, binäre Bin-Zuordnung `low <= f < high`, Frequenzauflösung **0.244141 Hz**, keine Partial-Bin-Korrektur. FFT-Band-RMS basiert auf einseitigem Leistungsspektrum (DC/Nyquist einfach, innere Bins doppelt), normiert mit Fensterenergie. Gerade an Bandgrenzen: Leakage und Bin-Lage beeinflussen den Wert.
- `Frequency_response.csv`: relative **Amplitude** (Sinus, idealisierte Nullzustand-/stationäre Betrachtung). FFT: begrenzte Fenster-Projektion, nicht LTI-Frequenzgang. `Band_metrics.csv`: Maximum und zusammenhängendes Intervall oberhalb Peak/√2 aus einem **0,05-Hz-Raster** (nicht notwendigerweise die nominalen Grenzen). `lower_truncated`/`upper_truncated`: abgeschnittene Messgrenze; für FFT sind die -3-dB-Grenzen nur effektive Schätzgrößen.
- `Coupling_matrix.csv`: Eingangs-Sinustöne in der Mitte jedes nominalen Bandes, in jedem Ausgabeband gegen **denselben Eingangs-RMS** gemessen; kein vertikales Summieren von dB-Werten. Heatmaps zeigen Dämpfung in dB. FFT-Energie ist nicht gleichbedeutend mit kausalem Übersprechen.
- `Amplitude_linearity.csv`: Pegel 0,03, 0,06 und 0,12 Spitzenamplitude, Sinuston Bandmitte; nur *numerische* Linearitätskontrolle. `Impulse_and_settling.csv`: Einschwingzeit bis 90 % des gemessenen stationären 1-s-RMS, Ausschwingen bis unter 10 %. Der Sample-Impuls bei 1 s ist nur bei beiden IIR-Methoden eine echte Filterimpulsantwort. FFT-Impulsbreite bezeichnet stattdessen eine Fenster-Lokalisation; FFT-Gruppenlaufzeit ist als Filtereigenschaft **nicht definiert**.
- Tieffrequenz 0,5–2 Hz: eigene **120-s-Simulation** mit Sinus-Burst von 10 bis 100 s; nicht mit einer kurzen 6-s-Simulation als eingeschwungen bewerten. Sonstige Bursts: 4 bis 16 s, Gesamtdauer 24 s. Kein weiterer Gain vor Messung (PRE_GAIN-Konzept).

## Ausgaben

`Frequency_response.csv`, `Band_metrics.csv`, `Coupling_matrix.csv`, `Amplitude_linearity.csv`, `Impulse_and_settling.csv`, `SELFTEST.csv`, zusätzliche `Response_*.svg`, `Coupling_*.svg`, `Impulse_*.svg`.

## Grenzen und Risiken

1. Keine Aussage zur Mikrofonkalibrierung, Gebäudeakustik, absolutem dB SPL oder Android-Performance. FFT mit langen Fenstern verwischt zeitlich; ein stationär gutes Frequenzband muss impulsförmige Signale nicht gut zeitlich lokalisieren.
2. SOS-Butterworths haben Übergänge/Ringing. 0-Hz-Tiefpass und endliche Beobachtungszeit gesondert betrachten. Gruppenlaufzeit bei IIR frequenzabhängig; sie wird hier **nicht quantitativ geschätzt**, um keine Scheingenauigkeit zu behaupten.
3. Die SVG-Impulse zeigen absolute Sample-Amplitude in dB relativ zu 1. Alle dB-Angaben sind digitale Modellwerte, **nicht dB SPL**.
4. Kein automatischer Sieger: Auswahl nach Ziel **Trennschärfe vs. Latenz vs. Impulstreue**, Leistungs-/Gerätetest ist ein eigener späterer Auftrag.
5. Der Bericht ist ein offline wiederholbarer Quellvergleich, kein Beleg für einen Android-Gradle-Test oder Cloud-Übertragung.
