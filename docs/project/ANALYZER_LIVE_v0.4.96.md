# LFN-Scout v0.4.96 – Live-Analyzer: Autorange und Hauptbild

Stand: 20.09.2026. Baseline: v0.4.95.1 (Diagnose-Callback-Hotfix). Dieses Dokument beschreibt implementierten Quellcode, **nicht** eine bereits erfolgreich auf Android oder dem Xiaomi getestete Freigabe.

## 1. Anwenderbedienung

- **Tipp auf die linke dBFS-Achse des Spektrums:** Startet Autorange (nur mit laufendem, gültigem, nicht pausiertem Signal). Der Controller sammelt für standardmäßig 2 s alle gültigen FFT-Pegel von >0 bis zur eingestellten LF-Obergrenze (20/50/100/200 Hz). Der DC-Bin und die numerische Leersignalgrenze ≤−180 dBFS zählen nicht. Neue Untergrenze `floor(Minimum − 5 dB)`; neue Obergrenze `ceil(Maximum + 5 dB)`; Mindestspanne 10 dB, zulässige Displaygrenzen −240…+50 dBFS. Fester Zuschlag **in dB**, keine Prozentberechnung. Fehlende Werte und Abbruch bei fehlender Datenquelle lassen bisherige Grenzen unverändert.
- **Einstellungsregister Anzeige:** Erfassungsdauer 1/2/3/5/10 s; Reserve je Seite 0/3/5/10/15 dB, Standard 2 s / ±5 dB. Interne Validierung erlaubt 1–30 s und 0–30 dB. Die abgeschlossene Autorange aktualisiert Spektrum, Wasserfall und Farbskala, legt beide Grenzen in `lfn_scout_live_analyzer` ab und stellt sie beim nächsten Analyzer-Start wieder her. Manuelle Presets (−120…−60/−40/0 dBFS) überschreiben die dynamischen Grenzen; Setup-Slots 1–3 sichern und laden **beide** Grenzen.
- **L50(2–25)/L90(2–25):** Werte in einem dünnen, roten, ungefüllten Rahmen mit gerundeten Ecken am oberen rechten Spektrumrand. Tipp darauf löscht den Statistikpuffer und setzt den exponentiellen 5-s-Mittelwert des FFT-Kerns zurück. Der FFT-Ring, die Wasserfallhistorie, PCM-Quelle, WAV-Aufnahme und der Analyzer-Gain bleiben intakt. Statistikwerte stehen erneut zur Verfügung, wenn ihr 120-s-Fenster gefüllt ist.
- **Cursor:** Tipp im Spektrum setzt ihn; Ziehen verschiebt ihn. Das Readout `x.x Hz · y.y dBFS` sitzt an der oberen Cursorspitze links/rechts nach Platz (rechts liegender Cursor → nach links und unterhalb der Statistikbox). Tipp auf das Readout schaltet ihn aus. Es gibt keine separate Cursor-Zeile.
- **Darstellung:** Spektralkurve dünner, Spektrum höher, redundante Titel „Klassischer Wasserfall“, „Vergrößern“, „Normal“ und die Achsenerklärungszeile sind entfernt. Tippen auf den Wasserfall schaltet weiterhin dessen Zoom um. Die Wasserfall-Farbskala steht **links**, ihre dBFS-Zahlen **in** der Farbfläche. Die Zeitmarkierungen bleiben links am Wasserfall. Für Spektrum und Wasserfall gelten dieselben `plotLeftDp`/`plotRightDp`-Konstanten und identische Hz-Tickpositionen. Frequenz-Einheit `Hz` steht jeweils getrennt unter der Achse. Dynamische dBFS-Ticks enthalten die wirklichen Autorange-Endpunkte. Das Offscreen-PNG folgt denselben Ausrichtungs-/Farbleistenregeln.

## 2. Technische Abgrenzung

- Neuer plattformunabhängiger Baustein `AnalyzerDisplayRange`/`AnalyzerDisplayTicks` in `:analyzer-core` für Autorange/Skalierung. `LiveAnalyzerCore.resetAveraging()` setzt nur den IIR-Average zurück, ohne den 4096-Sample-Ring und die FFT-Taktung zu verlieren.
- Der Controller bleibt alleiniger App-lifetime-Abonnent von `AnalyzerSessionGateway.rawPcm` (vorhandene 8-kHz-Quelle → 1-kHz-FIR). Es wird kein zweites AudioRecord geöffnet, kein WAV-/Rolling-/Supabase-Datenweg verändert. Impuls-Wasserfall, Mithören, Kalibrierung und Radiushistogramme sind nicht Teil dieses Releases.
- Der Parser für bestehende Display-Prefs akzeptiert nun gespeicherte individuelle Unter-/Obergrenzen statt sie beim nächsten Start auf das alte Preset zurückzusetzen.

## 3. Tests / Freigabestatus

Host-`kotlinc` plus gezielter Selbsttest: 5/5 PASS (feste ±5 dB, ungültiges Signal, einstellbare Reserve/Frequenzbegrenzung, dB-Ticks, Mittelwert-Reset ohne erneutes FFT-Warmup). Statische Verifier: 17/17 PASS inklusive v0.4.95.1-Diagnose-Callback-Vertrag. Android-Gradle-Compile, vollständige Gerätetests, UI-Gesten/Auto-PNG und Aufnahmeregressionen **NOT RUN** in diesem Container. Benutzer: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, Android Studio Run. Danach Autorange, Reset, Cursor, Geräte- und PNG-Layout überprüfen.
