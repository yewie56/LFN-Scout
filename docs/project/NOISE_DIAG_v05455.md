# Rauschdiagnose v0.5.45.5

## v0.5.45.5 – Diagnostik 1/f-Verdacht (Quellkandidat)
- Neuer DIAG-Dialog unmittelbar bei NOISE: fest-seedierter synthetischer Weißrauschtest über denselben LiveAnalyzerCore wie der Analyzer, 1000 Hz / 4096 / Hann, lineare Leistungsmittelung und lineare Regression des dB-Leistungsspektrums über log10(f) im Bereich 1–100 Hz. Toleranz ±2,5 dB/Dekade, kein physikalischer Kalibrationsnachweis.
- Gespeichertes Original-Rauschprofil: Steigung 1–100 Hz aus unkompensierten Profil-Power-Bins; Modell-Hinweis, keine gemessene Mikrofonsensitivität.
- Kein Eingriff in FAST, Roh-PCM/WAV, Cloud, Rolling-CSV, FFT-Produktivberechnung oder NOISE-Subtraktion.
- Grenze: DIAG zeigt die Referenz als **numerische Steigung**, nicht als zweite Spektralkurve; Original/Rest lassen sich über NOISE ein/aus vergleichen. Vollständige 3-Kurven-Überlagerung und automatische Roh-PCM-/Dezimationsvergleichsdiagnose sind noch offen.
- Teststatus: Kotlin-Hosttest ggf. separat dokumentiert; Gradle Android/JUnit/ADB ausstehend.

Testfolge: NOISE AUS, Kurzschluss, Referenz einmessen, DIAG ansehen, Weißrauschtest ausführen, NOISE EIN/AUS und Rest beobachten. Weißrauschtest simuliert nur digitale PCM-Daten. PASS belegt nicht das tatsächliche Rauschen der Android-Audiohardware.
