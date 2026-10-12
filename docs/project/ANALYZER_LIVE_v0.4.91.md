# LFN-Scout v0.4.91 – Live-Signalreparatur und UI-Abgleich

**Status:** Testkandidat; JVM-Kotlin-Selbsttest PASS. Android-Gradle/APK/Xiaomi v0.4.91 NOT RUN.

## 1. Anlass und Erkenntnisstand

Der Nutzer hat v0.4.90 erfolgreich auf einem Xiaomi gestartet: Wasserfall-Historie 235/235, Cursor −240 dBFS, L50/L90 −220,3 dBFS, keine sichtbare Kurve. Solche Zahlen entstehen aus dem numerischen FFT-Minimum; sie sind **kein gemessener SPL-Wert**. Der konkrete Verlustpunkt zwischen 8-kHz-Mikrofon, 8:1-Rundung und 1-kHz-PCM war ohne Live-Stufenmesswerte nicht endgültig bewiesen. Die feste UI-Untergrenze −110 dBFS verdeckte außerdem sehr kleine, aber reale Signale.

## 2. Implementierter neuer Datenweg

`RollingAudioService.kt` bzw. `AudioRecorder.kt` publizieren bereits 8-kHz-Roh-PCM, vor Recorder-Gain/Downsampling, in `AnalyzerSessionGateway.rawPcm`. Die neue LF-Session `AnalyzerRawLfSession.kt` abonniert **diese vorhandene Quelle**, prüft tatsächliche Rate 8000 Hz und Kontinuität, verarbeitet mit `RawLfDecimator.kt` (129 Tap Hamming-FIR, 280 Hz, Faktor 8) und liefert normierte Double-Samples an `LiveAnalyzerCore.acceptNormalized()`. Im Kern bleiben FFT 4096, Hop 512, Hann und Frequenzobergrenze 200 Hz. Auf die 8-kHz-Rohdaten wirkt nur ein unabhängiger einstellbarer *Analyzer-Gain* (0–50 dB, Startwert 0), **nicht** auf Recorder, WAV und getrennte Impulsdiagnose. Der angezeigte dBFS-Pegel ist unkalibriert und bei Gain > 0 entsprechend angehoben; kein physikalischer SPL.

Die bisherige `AnalyzerLiveSession` / `AnalyzerSessionGateway.pcm` und der bestehende 8:1-WAV-Pfad bleiben für Legacy-Zwecke unverändert. Es wird **kein zusätzliches AudioRecord** geöffnet. Bei Quellen-/Sequenzwechsel wird LF-FIR/FFT neu initialisiert. Der Hochfrequenzpfad bis 20 kHz bleibt späterer Entwicklungsgegenstand.

## 3. Diagnose und Gültigkeit

Im Panel „Diagnose“ werden gemeldete Quelle/Rate, letzter Roh-PCM-Peak und RMS (PCM16), LF-Peak/RMS (vor Analyzer-Gain), Rohblöcke, FFT-Anzahl, Datenalter, Drops, Resets, Fehler und ungültige Quellraten angezeigt. Bei ausbleibenden PCM-Blöcken >3 s wird ein Unterbrechungsstatus angezeigt. Eine FFT nur auf numerischem Boden (alle Bins ≤−180 dBFS) wird nicht als gültiges Nutzsignal übernommen: keine neue Wasserfallzeile/Statistik, kein neuer PNG-Export.

Die Anzeigen sind Diagnosen, **keine akustische Kalibrierung**. Die alte FFT-Mindestgrenze −240 dBFS bleibt intern als numerische Schranke, wird aber nicht mehr als Messwert ausgegeben.

## 4. Oberfläche / aktuelle Grenzen

Das mitgelieferte Nutzerbild „Mithören EIN/AUS“ ist visuelle Referenz; das Panel ist kompakter, dunkel, Kopf/Status oben, Live-Spektrum über klassischem Inferno-Wasserfall. Für den klassischen Wasserfall gilt wie in der Vorlage: **Zeit horizontal (−120 … 0 s), Frequenz vertikal (0 … einstellbar 20/50/100/200 Hz)**. C09 zeigt sechs Settings-Register; bislang funktionieren LF-Bereich, Analysegain, Auto-PNG und die drei Setup-Slots (nur Bereich/Auto-PNG). Mithören ist explizit AUS/nicht implementiert; Superfast ist weiterhin getrennte Impulsdiagnose; kein Impuls-Wasserfall. Histogrammspalte rechts und 20-kHz-Modus bleiben offen. Screenshots benutzen denselben reellen FFT-Zustand; noch kein pixelidentischer Compose-/PNG-Look verifiziert.

## 5. Nachweise und lokale Windows-Tests

- `tools/RUN_ANALYZER_LIVE491_SELFTEST.sh`: 47/180 Hz im 8-kHz-Rohsignal, sehr kleine PCM-Amplitude, 2500-Hz-Alias, Nulleingang, Roh-Hub → LF-Spektrum: PASS.
- Alte statische v0.4.90-Prüfung ist historisch; neue `verify_v0491_live_analyzer.py` kontrolliert den 8-kHz-Rohpfad und die WAV-Unveränderlichkeit.
- Windows: `PREPARE_PROJECT.bat` → `gradlew.bat :analyzer-core:test --stacktrace` → `gradlew.bat :app:testDebugUnitTest --stacktrace` → `gradlew.bat assembleDebug --stacktrace`. **Bis diese Läufe erfolgen: NOT RUN.**
- Xiaomi: Rolling einschalten, Analyzer starten, 4–5 s Warmup; einen 30–100-Hz-Ton bei normaler Lautstärke wiedergeben, Kurve und Wasserfall prüfen. Signaldiagnose auf Rohpeak/RMS, LF-RMS und Stale/Resets prüfen. Danach bei leisem Geräusch +0 / +20 dB vergleichen. Rolling/WAV/Seismik/Bewertungen regressionsprüfen; PNG/Auto über Panelwechsel prüfen. Keine künstlichen Signale in der Anzeige.

**Weiterhin offen:** Geräte-/Mikrofonfrequenzgang, Kalibrierung, Android-Hintergrund-/Screen-off-Verhalten, komplette T-01…T-71-Abnahme, vollständige visuelle Freigabe.
