# LFN-Scout – Live-Analyzer v0.4.94: Timing und Achsen

**Status:** Quellcode/Testkandidat auf v0.4.93, visuelle Gerätefreigabe offen. **Vorrang:** Aktuelle, vom Benutzer am 20.09.2026 geforderte Anzeigeänderungen präzisieren ältere Konzeptbilder und die v0.7-Sollunterlagen (deren Bildbeschriftungen nicht normativ sind).

## 1. Ausgangslage

Der Benutzer hat für v0.4.93 die sieben praktischen Schritte (Rolling, gelbe Live-Kurve, farbiger Wasserfall, Geräuschreaktion, 100/200 Hz, Pause, Panelwechsel) als erledigt bestätigt. Beanstandet: geringe Rate, falsche Wasserfalllaufrichtung, Frequenzachse ohne Ticks und `Hz` am letzten Tick, fehlende dBFS-Achse und dBFS-Ticks.

## 2. Tatsächlich geänderte Implementierung

| Gegenstand | Wert/Verhalten | Quelle |
|---|---|---|
| Mikrofon, WAV | 8000 Hz Capture, WAV weiter bestehender 1000-Hz-Pfad | AudioRecorder / RollingAudioService unverändert |
| Analyzer-FIR | Existing `RawLfDecimator`, 8:1 auf 1000 Hz mit Double-Ausgang | AnalyzerRawLfSession |
| FFT | 4096, Hann, ca. 0,244 Hz Bins | LiveAnalyzerCore |
| **FFT-Hop** | 50, 100, 200, 500, 1000 LF-Samples; Default **100** | AnalyzerTiming, SharedPreferences `fft_hop_samples` |
| **Wasserfall-Hop** | 1, 2, 5, 10 FFT-Spektren; Default **5** | SharedPreferences `waterfall_hop_spectra` |
| Wasserfall | Erstes/neuestes FFT-Spektrum oben, ältere nach unten, lineare Frequenz horizontal | LiveAnalyzerScreen.makeWaterfall / Controller.renderPng |
| Historie | `ceil(120000/(FFT-Hop * Wasserfall-Hop))` Zeilen | AnalyzerTiming.historyRows |
| Statistik | `ceil(120000/FFT-Hop)` Einträge für 2–25-Hz-L50/L90 | AnalyzerTiming.statisticFrames |
| Zeichenabfrage | 100 ms bzw. 50 ms bei FFT-Hop 50; unabhängig vom FFT-Hop | LiveAnalyzerScreen LaunchedEffect |
| UI/PNG | dBFS-Y-Ticks Spektrum, Hz-X-Ticks Spektrum und Wasserfall, dBFS-Farbskala-Ticks | LiveAnalyzerScreen / LiveAnalyzerController |

**Default-Rechenbeispiel:** 1000/100=10 neue Spektren/s (Soll); alle 5 Spektren eine Wasserfallzeile → 2 Zeilen/s, 240 Zeilen = 120 s. Die 4096 Sample-Fensterlänge bleibt ca. 4,1 s und ist nicht mit dem Hop gleichzusetzen. `Hz` steht im Achsentitel, keine Einheit am letzten Tick. Numerische dBFS-Achsen sind unkalibriert; keine dB SPL.

FFT-Hop-Änderung startet **nur den Analyzer-Subscriber** neu, mit ~4 s Warmup. Reiner Wasserfall-Hop-Wechsel lässt die FFT-/Mikrofon-Session stehen, verwirft aber Historie und Statistik, um Zeiten nicht zu vermischen. Setups 1–3 sichern beide Hops neben Bereich und Auto-PNG.

Die Anzeige rastert maximal 480 Zeilen hoch, auch wenn die vollständige 120-s-Historie bei kleinen Hops mehr Spektralzeilen enthält. Dabei werden nur echte vorhandene Zeilen anhand ihres Alters ausgewählt (keine erfundenen Zwischenwerte); das PNG kann die volle Zeilenzahl verwenden.

## 3. Abnahme / bekannte Grenzen

- Isolierter Kotlin-Test: `bash tools/RUN_ANALYZER_TIMING494_SELFTEST.sh` prüft alle 20 Hop-Kombinationen, FFT-Kadenz und synthetischen Roh-PCM-Hub. Vorhandener LF-Selbsttest bleibt erhalten.
- Statischer Quellvertragscheck: `python verify_v0494_analyzer_contract.py` (nur Quelltext, **kein** Android-/Compose-Build).
- Noch auszuführen auf Windows: `:app:testDebugUnitTest`, `assembleDebug`; Xiaomi: Spektrum und dBFS-Ticks, 0–100/200 Hz mit einzelnem Hz-Achsentitel, Wasserfall mit Zeit 0 oben/−120 unten, Farb-dBFS-Ticks, Pause, PNG-Export sowie automatische PNGs nach 120 s, 10-Hz-FFT-Zielrate und Drops/Callback-Fehler prüfen.
- Default 10 Hz ist eine **Sollrate**. Rendering, Mikrofonsystem und thermische Drosselung können sie vermindern. Kein zweiter Mikrofon-Owner, keine Änderung am Recorder, kein Mithören/Impuls-Wasserfall oder 48-kHz-Mode.
