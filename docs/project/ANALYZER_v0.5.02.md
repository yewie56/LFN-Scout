# LFN-Scout v0.5.02 – Implementierung des Live-Analyzers

**Source of truth:** tatsächliche Kotlin-Dateien in `analyzer-core` und `app/src/main/java/de/brummbeobachter/app`. Diese Ergänzung gilt vor älteren Analyzer-v0.7-Konzeptentwürfen. **Status:** implementiert, Host-Core geprüft; Android-Build/Xiaomi noch nicht ausgeführt.

## A. Anzeige/Bedienung

1. **Hz:** `AnalyzerTiming.frequencyTicks(min,max)` berechnet Achsenticks, `unitFrequencyHz` gibt das arithmetische Mittel zwischen Tick 2 und 3 zurück. Beide Canvas-Achsen und Offscreen-Bitmap verwenden genau dieselbe Positionsregel. Keine zweite Zeile. Gerätetest bei 20/50/100/200 Hz erforderlich, insbesondere letzte Tickzahl ohne Überdeckung.
2. **Wasserfall-Fußzeile:** `Akt`, `MW`, `MAX` zeigen ausschließlich den aktiven Aggregationsmodus. Offscreen-Text enthält denselben Kurzcode plus Langbezeichnung. Keine Spektrum-Max-Hold-Kurve implementiert.
3. **dBFS-Grenzen:** keine `±`-Sonderbuttons mehr. Minimum/Maximum bleiben direkte Textfelder inklusive Minuszeichen und Übernehmen/Presets/Autorange. Gültigkeitsbereich −240…40 bzw. −230…50, Differenz mindestens 10 dB. KeyboardType.Text ermöglicht Eingabe negativer Werte ohne separaten Vorzeichenbutton; Xiaomi-Tastaturtest erforderlich.
4. **Wasserfallzeit:** freie **ganzzahlige** Eingabe 10–600 s, Standard 120 s. Aus Sicherheitsgründen maximal 2400 historische Zeilen (`seconds * 1000 / effectiveWaterfallIntervalMs <= 2400`), daher nicht jede Kombination der vollen 600 s mit 100 ms möglich. Übernahme leert ausschließlich Wasserfallhistorie/Teilzeile, aktualisiert Achsen und startet Auto-Bildzyklus neu. L50/L90 bleiben mit 120-s-Bandstatistik unabhängig.
5. **Farbskala:** linke Farbleiste misst die breiteste aktuell sichtbare negative/positive Tickzahl plus kleinen Innenabstand. Spektrum und Wasserfall verwenden gemeinsame 54-dp/12-dp-Plotränder; weitere rechte Histogrammanzeigen bleiben zurückgestellt. Im Offscreen-Bild gleiche dynamische Farbleistenlogik.

## B. Signalverarbeitung

- **Rohsignal unverändert:** gemeinsames 8-kHz-PCM → bestehender FIR / Decimation 8:1 → 1-kHz-Analyzer; keine zusätzlichen Mikrofone, keine Änderung der WAV-Verarbeitung. `maxFrequencyHz` des DSP bleibt 200 Hz. Min/Max schneiden nur die grafische Darstellung aus diesem vorhandenen Spektrum heraus.
- **FFT Size:** 2048/4096/8192 (Default 4096). Δf = 1000/N Hz; Warmup und Integrationszeit N/1000 s. Bei neuen Einstellungen nur Analyzer-Subscriber neu starten; WAV bleibt aktiv.
- **FFT-Hop:** 5/10/50/100/200/500/1000 Samples (Default 100). Wegen LF-Rate 1000 Hz ist die Zahl in Samples numerisch gleich Transformationsintervall in Millisekunden. Beide Eingaben sind gekoppelt; es wird **kein** davon unabhängiges Intervall gespeichert. 5 Samples = theoretisch 200 FFT/s; CPU-Last nicht auf Xiaomi getestet.
- **Window:** Hann (Default), Hamming, Blackman, Rechteck; pro Funktion `coherentGain = sum(window)/N` wird bei jeder FFT zur tonalen Amplitudenkorrektur verwendet. Der bisherige DC-Abzug bleibt bestehen.
- **Exponential Smoothing:** Glättung linearer Leistung wie bisher. EIN mit Zeitkonstante `τ` (Default 5 s, 0,01–60 s), `α = 1−exp(−hop/(1000 τ))`; AUS verwendet α=1 und folgt aktueller Spektralleistung. Im Panel wahlweise τ oder α editieren; α=1 setzt AUS. Die bestehende rote Kurve kann wie bisher separat angezeigt/ausgeblendet werden. Keine neue unabhängige zweite Glättungsstufe.

## C. Persistenz und Kompatibilität

Neue SharedPreferences: `min_frequency_hz` (Int 0), `history_seconds` (Int 120), `fft_size` (Int 4096), `fft_window` (String HANN), `smoothing_enabled` (Boolean true), `smoothing_time_sec` (Float 5.0). Die bisherige `range_hz` trägt jetzt den frei gewählten oberen Integer-Grenzwert. Sämtliche Felder in den drei `setup_{slot}_...`-Datensätzen enthalten. Alt-Slots ohne neue Keys erhalten Defaultwerte. `image_format` PNG/JPG sowie JPG-Qualität unverändert.

**Nicht implementiert:** frei wählbare Decimation/FIR, Spektrum-Max-Hold, Audio-Mithören, Superfast, Radiushistogramme, SPL-Kalibrierung, Setup-JSON-Import. Dazu keine Testfreigabe.

## D. Testgrenzen

`tools/RUN_ANALYZER_V0502_SELFTEST.sh`: 15/15 Core PASS. Bestehende Wasserfall-/Zeitbasis-Hosttests PASS. Diese prüfen **nicht** Compose, reale UI-Platzierung, Dateispeicherung, Mobilgerät-CPU oder Regression bei gleichzeitiger Aufnahme. Für diese Punkte gilt `TESTPLAN_v0.5.02.md`.
