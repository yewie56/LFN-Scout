# LFN-Scout v0.5.03 – Analyzer: verbindliche Implementierung

**Quelle:** tatsächliche Kotlin-Dateien unter `analyzer-core/src/main/kotlin/de/brummbeobachter/app/` und `app/src/main/java/de/brummbeobachter/app/`. Vorherige v0.5.02-Papiere sind historische Baseline.

## 1. FIR-Decimation

Der gemeinsame Roh-PCM-Hub liefert ausschließlich echte 8-kHz-Frames. `AnalyzerRawLfSession` besitzt als Subscriber eine unabhängige `RawLfDecimator`-Instanz. Die Auswahl 4:1 → 2 kHz (129 Taps, Cutoff 280 Hz), 8:1 → 1 kHz (129 Taps, Cutoff 280 Hz, bisheriger Standard), 16:1 → 0,5 kHz (513 Taps, Cutoff 215 Hz) verändert ausschließlich die Analyzer-Daten, **nicht** Aufnahme, Rolling, WAV oder Backend. Alle FIR-Koeffizienten werden auf DC-Gain 1 normalisiert. Die maximal angezeigte Frequenz bleibt 200 Hz. 16:1 verwendet gegenüber 8:1 eine längere Gruppenlaufzeit von 32 ms statt 8 ms (konzeptionell, bei 8 kHz). Keine SPL-Kalibrierung.

FFT-Größe 2048/4096/8192 und Hop in LF-Samples bleiben erhalten. Zeitintervall ist `hop * 1000 / LF-Rate` ms; gewünschtes Intervall wird auf den nächsten zulässigen Hop abgebildet. Wasserfall-Zeitbasis berücksichtigt die echte LF-Rate; ein 2000-ms-Zeilenintervall kann für 16:1 und großen Hop erforderlich sein. Maximal 2400 Zeilen, frei gewählte Dauer 10–600 s. FFT-Neukonfiguration beginnt eine neue Analyzer-Historie; WAV bleibt bestehen.

## 2. Cursor

Beide möglichen Textpositionen links/rechts verwenden dieselbe obere Text-Grundlinie; der Statistikrahmen darf die Schrift nicht verdecken. Gleiche Cursorfrequenz und x-Geometrie in Spektrum und Wasserfall. Die Linien werden in beiden Canvas bis zum letzten Bildpixel gezeichnet; Offscreen PNG/JPG erhält den zuletzt an den Controller übertragenen Cursor. Zwischen den getrennten Diagramm-Views kann ein kleiner layoutbedingter Zwischenraum verbleiben.

## 3. Setups

Lokale Slots 1–3 speichern frei editierbaren Namen (bis 80 Zeichen) und mehrzeilige Beschreibung (bis 1000), neben allen bisherigen DSP-/UI-Werten. Slots ohne neue Keys laden mit leerem Namen und Beschreibung; neue Werte nutzen Defaults. JSON-Import/-Export ist **nicht** implementiert.

## 4. Statistik

Statistik-Min 0–199 Hz, Max 1–200 Hz, Min < Max; Zeitfenster 10–600 s, Default 120; `Average Time` 0,1–60 s, Default 5, höchstens Fenster. Die Band-Leistungen werden pro gültiger FFT in linearer Domäne gemittelt (gleitendes Fenster), erst danach logarithmiert und für L50/L90 sortiert. Perzentile werden aus dem **vollen Statistikfenster** berechnet und höchstens einmal pro Sekunde aktualisiert. Der Default 5 s ist eine neue Statistikdefinition gegenüber den vorher unmittelbar pro FFT erfassten Bandpegeln. Der rote Spektrum-Mittelwert, Wasserfallhistorie und Auto-Bildzyklus bleiben unabhängig.

Bereichslabel oben rechts im Spektrum enthält die Frequenzgrenzen. Derselbe Bereich wird im Spektrum schattiert und im Offscreen-Export beschriftet; teils außerhalb der sichtbaren Frequenzachse liegende Statistikbänder werden nur im Schnittbereich schattiert. Beim Band-/Zeitwechsel beginnt die Statistikneuaufnahme.

## 5. Tests und Grenzen

`tools/RUN_ANALYZER_V0503_SELFTEST.sh`: FIR-Durchlass bei 50/195 Hz, Aliasunterdrückung, Blockgrenzen, dynamische Zeitbasis, Statistikvalidierung (20/20). v0.5.02 und erhaltene historische Host-Core-Tests separat ausführen. Compose/Android nicht auf dem Host kompiliert; v0.5.03-Xiaomi-Tests **NOT RUN**. CPU-Last bei großem Statistikfenster, 16:1, FFT 8192 oder Hop 5 auf dem Mobilgerät messen. Supabase-Restore und externe Kalibrierung offen.
