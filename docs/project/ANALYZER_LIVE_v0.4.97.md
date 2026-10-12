# LFN-Scout v0.4.97 – Robuste Autorange und kompaktere Darstellung

Stand 20.09.2026. Basis: v0.4.96 (versionCode 137). Neuer Quellstand: versionName 0.4.97 / versionCode 138. Dieses Dokument beschreibt den Quellcode; Android- und Xiaomi-Abnahme noch offen.

## 1. Autorange (nur Display, keine Veränderung der Messkette)

- Auslösung weiterhin durch Tipp auf linke dBFS-Achse. Standarddauer 2 s; per Anzeige-Tab einstellbar. Sammeln nur gültiger Live-Spektren der gewählten Frequenz (ohne DC und die ≤−180-dBFS-Leerwerte).
- **Pro Spektrum:** Sortierte Pegel aller gültigen sichtbaren Bins; 5. Perzentil als robuster unterer, 95. Perzentil als robuster oberer Pegel. Bei weniger als 20 gültigen Bins werden deren echte Extremwerte genommen, um einen sehr schmalen LF-Bereich nicht künstlich zusammenzudrücken.
- **Über den Erfassungszeitraum:** 10. Perzentil der unteren Framewerte und 90. Perzentil der oberen Framewerte. Einzelne kurze Einbrüche/Spitzen werden so gedämpft. Die Reihenfolge ist entscheidend: keine Vermischung von Zeit- und Frequenzachse.
- Neue Grenzen: `floor(robustes Minimum − marginDb)` und `ceil(robustes Maximum + marginDb)`; Default `marginDb=5` (feste dB, NICHT Prozent). Unter-/Obergrenzen werden sofort gespeichert und beim Neustart geladen. Mindestanzeigespanne 10 dB. Bei ungültigem Signal keine Überschreibung.
- Das Verfahren zeigt **typische Pegel**, nicht die absoluten Extremwerte. Sehr schmale reale Peaks oder seltene Impulse können außerhalb der automatisch bestimmten Grenzen liegen. Manuelle Presets und bestehende Setup-Slots bleiben verfügbar.

## 2. Bildschirm / PNG

- Cursorreadout zeigt nur `x.x Hz · y.y dBFS` in Cyan mit Textschatten; keine Umrandung und kein Hintergrund. Ziehen bewegt den Cursor, Tipp auf den Text blendet ihn aus.
- L50/L90-Box oben rechts: zwei dicht gesetzte Zeilen, dünner roter ungefüllter Rahmen mit 6-dp-Eckenradius und rund 3 dp Padding um den Text; nicht länger feste 156×33 dp. Tipp setzt Statistik und Mittelwertbildung zurück, ohne die Wasserfallhistorie zu löschen.
- Farbskala links im Wasserfall: 27 → 18 dp, kleinere signierte Zahlen innerhalb der Farbfläche. Frequenzplot für Spektrum und Wasserfall wird gemeinsam von links 68 → 60 dp und rechts 15 → 12 dp verbreitert. Zeitachse und ihre Zahlenticks bleiben unverändert; Frequenz-Ticks bleiben pixelgenau ausgerichtet.
- Offscreen-PNG: linke Farbskala 63 → 42 px, gemeinsamer Frequenz-Plot links 155 → 140 px und rechts 955 → 970 px; schmale Innenbeschriftung und engere L50/L90-Rahmenbreite. Keine Änderungen an der PNG-Automatik.

## 3. Invarianten und Tests

- Keine Änderung an 8-kHz-Capture, 8:1-LF-FIR, FFT-Hop, Wasserfall-Zeitbasis, Rolling-Buffer, WAV-Recorder, Seismik oder Supabase-Recovery.
- `tools/AnalyzerDisplayRangeHostSelfTest.kt`: sieben gezielte Prüfungen, einschließlich isolierter Notches und zeitlicher Ausreißer. `analyzer-core/src/test/.../AnalyzerDisplayRangeTest.kt`: zwei neue JUnit-Regressionen; originalen Gradle-Test auf Windows ausführen.
- Statische Regression: `verify_v0497_analyzer_contract.py` inklusive Diagnose-Callback-Existenz, Bar-/Viewport-Mapping, Persistenz und Versionsdaten.
- Android-Gradle-/Compose-Kompilierung, Touch-Bedienung auf Xiaomi, realistische Autorange über 2 s, Layout bei 20/50/100/200 Hz, PNG-Export und CPU-Last noch zu testen. Keine APK-/Gerätefreigabe behaupten.
