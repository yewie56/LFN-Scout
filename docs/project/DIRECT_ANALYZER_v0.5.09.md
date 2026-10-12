# LFN-Scout – Direkt-Analyzer v0.5.09 (Entwicklungsstand, 21.09.2026)

## 1. Ziel und Versionsbezug

Direkt-Analyzer = bisher als „48-kHz-Analyzer“ bezeichneter, **exklusiver** PCM16-Mikrofoneingang ohne parallelen Rolling-Recorder. Produktmodus-Enum und historischer Setup-Export-Dateiname (`Setup_4_48k.json`) bleiben aus Kompatibilitätsgründen erhalten. `versionName=0.5.09`, `versionCode=152`, App-ID unverändert. Implementierung ist ein **Build-/Gerätetestkandidat**; Android/Compose-Build und Xiaomi-Test wurden in der Erstellungsumgebung nicht ausgeführt. Der vom Nutzer für v0.5.08 gemeldete Zwischenstand „OK soweit“ ist keine vollständige v0.5.09-Abnahme.

## 2. Tatsächlich implementierte Einstellungen

| Einstellung | Werte/Verhalten | Quelle/Persistenz |
|---|---|---|
| Audio Source | AUTO (globale Auswahl-Reihenfolge), UNPROCESSED (API ≥24), MIC, VOICE_RECOGNITION | priv. `lfn_scout_live_analyzer`, `direct_audio_source` + `mode_48_...` |
| Eingangsabtastrate | 8000, 11025, 16000, 22050, 44100, 48000, 96000 Hz | `direct_sample_rate` |
| FFT-Größe | 64,128,256,512,1024,2048,4096,8192,16384 (Radix-2) | `fft_size` |
| Decimation | Direkte Faktoren 0–12, 0 AUS, 1 = 1:1; 2–12 FIR + Downsampling | `decimation_factor` |
| Fenster | COSINE, HANN, HAMMING, BLACKMAN, BLACKMAN_HARRIS, FLAT_TOP, RECTANGULAR, TRIANGULAR, WELCH | `fft_window` |
| gewünschtes Transformationsintervall | 10,15,20,25,30,50,100 ms; Hop gerundet auf volle Samples. Die bestehende freie Eingabe wählt die nächstliegende der sieben Stufen | `fft_hop_samples` |
| Exponential Smoothing | α=0,00–1,00 in 0,01-Schritten; auf **lineare FFT-Leistung** | `direct_smoothing_alpha` |
| Peak Markers | 0,1,2 Peaks des aktuellen Spektrums, lokales Maximum, Abstand ≥3 FFT-Bins | `direct_peak_markers` |
| Stay Awake | Flag `FLAG_KEEP_SCREEN_ON` während Direktmessung bei angezeigtem Analyzer-Panel | `direct_stay_awake` |
| Subtract DC | EIN/AUS vor FFT | `direct_subtract_dc` |
| Testsignal 1 und 2 | Jeweils 1 Hz bis *unter Eingangs-Nyquist*, Frequenz (Float) und eigener EIN/AUS-Schalter; beide nur im **digitalen TEST-Modus**, nicht beim realen Mikrofon | `mode_48_direct_test1_hz`, `..._test2_hz`, `..._test1_on`, `..._test2_on` |

Alle Parameter werden zusätzlich in Setup 4 gespeichert. Der JSON-Export ist weiterhin eine **lesbare Kopie**, kein automatischer Import. Um die bisherigen 8-kHz-Modi nicht zu beeinflussen, sind Direktprofil- und Rollingprofile getrennt. Wegen des vorhandenen Mode-Enums bleibt der symbolische Name `DIRECT_48K` historisch erhalten, obwohl die Eingangsrate nun wählbar ist.

## 3. Zeitbasis, Nyquist und Speichergrenzen

- Tatsächliche Analyserate = Eingangsrate / effektiver Faktor. **Nicht ganzzahlig teilbare Kombinationen** (z. B. 11025 / 2 und 44100 / 8) werden im Menü ausgeblendet und bei direktem API-Aufruf abgewiesen. Keine implizite, frequenzverfälschende Rundung/Resampling.
- FFT-Achse verwendet die reale ganzzahlige Analyserate, Δf = Analyserate / FFT-Größe; höchste analysierte Frequenz = Analyserate/2. Der sichtbare Bereich wird entsprechend begrenzt, kann bei 96 kHz ohne Dezimierung bis 48 kHz reichen.
- Gewünschtes Transformationsintervall = 10–100 ms; Hop kann größer als FFT-Größe sein. Berechnung benutzt den jüngsten FFT-Ringabschnitt, nicht eine umetikettierte Abtastrate.
- Die vorhandene Wasserfall-Zeilen-Zeitbasis bleibt unabhängig vom FFT-Hop; höchstens 2400 Historienzeilen, maximal 14400 s mit zulässigen Zeilenintervallen. Bei ungültigen Kombinationen nicht heimlich eine unzulässige Historie anlegen.
- Die zuvor existente 8-kHz-Rolling-FIR (4/8/16) wird **nicht** durch den neuen Direkt-FIR ersetzt. Die gemeldete 16:1-Pegelabweichung bleibt als eigener Fehler offen.

## 4. Algorithmen und Referenzpegel

- Direkt-FIR: windowed sinc (Blackman), Grenzfrequenz 0,42 × Ausgangsrate; Koeffizienten auf Summe 1 normiert; PCM16 erst in Double normalisieren, dann kontinuierliche Blockverarbeitung mit Zustand.
- Neun FFT-Fenster: kohärenter Gain = Koeffizientensumme/FFT-Größe; Peak-Amplituden auf dBFS (nicht dB SPL). Flat-Top hat breitere Hauptkeule und Nebenspektren; FFT-Fenster beeinflussen Rauschleistungs-Darstellung, nicht nur Peak-Amplitude.
- α ist Aktualisierungsgewicht: `P_avg[n] = P_avg[n-1] + α * (P_inst[n] - P_avg[n-1])`. Bei α=0 bleibt der nach Start initialisierte Wert, bei α=1 folgt der aktuelle Wert unmittelbar.
- DC-Abzug optional nur im Direktmodus. Die historische LF-Berechnung zieht DC weiterhin ab.
- Peakmarker suchen lokale Maxima **ohne DC-Bin** innerhalb des sichtbaren Bereichs; Frequenz und dBFS im Live-Spektrum und PNG/JPG. Dieser Marker ist kein Ersatz für Maxhold und verändert den Wasserfall nicht.
- Testtöne summieren sich bei beiden EIN mit jeweils halber Amplitude, damit der gemeinsame Signalpegel begrenzt bleibt. Keine Ausgabe über Lautsprecher; nur an den Analyzer-Rohhub, nie an Rolling/WAV.

## 5. Betriebszustände und Änderungsschutz

- Bei STOP bleibt der gewählte Modus erhalten. Erst nach STOP + Verlassen wird der zuvor gesicherte Rolling-Zustand wiederhergestellt. Ohne STOP darf Panelwechsel den laufenden Analyzer nicht zurücksetzen.
- Audioquelle, Eingangsrate und Dezimierung sind **nur bei STOP** änderbar. Beim erneuten START wird der neue real unterstützte Mikrofoneingang exklusiv geöffnet. Bei Fehler eindeutige Diagnose (`lastError`); keinen falschen Wert als laufend anzeigen.
- FFT/Fenster/Glättung/DC und Marker können während einer Messung geändert werden; Änderungen der DSP-Zeitbasis erzeugen einen frischen Analyzer-Subscriber, aber keinen zweiten AudioRecord.
- Stay Awake hält den **sichtbaren Analyzer-Bildschirm** wach. Der bestehende Service-CPU-Wakelock für den Direktmitschnitt bleibt unverändert; ein systemweiter Bildschirm-Wachhaltemodus außerhalb dieses Panels ist nicht zugesagt.
- Bestehendes schwarzes Datum/Uhrzeit-Wasserfallband, maximal fünf Ticks, dBFS-Farbskala, gemeinsame x-Koordinaten, Kameralangdruck-Pop-up und Bildexport bleiben erhalten.

## 6. Prüfstatus

Lokaler JVM-Hosttest `tools/RUN_ANALYZER_V0509_SELFTEST.sh`: 33/33 PASS (Konfigurationslogik, alle 9 FFT-Größen, 9 Fenster mit kohärentem Pegel, DC, α, FIR 12:1, Peakmarker). Historischer Timingtest 6/6 PASS. Kotlin-PSI-Syntaxprüfung aller 181 `.kt`-Dateien: 0 Syntaxfehler. **Android-Gradle/Compose-Compiler, Xiaomi-Audioaufnahme und visuelle Marker-/GUI-Abnahme NICHT AUSGEFÜHRT.** Dazu `TESTPLAN_v0.5.09.md` verwenden; Einzelheiten `BUILD_VERIFICATION_v0.5.09.md`.
