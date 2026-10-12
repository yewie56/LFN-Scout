# LFN-Scout v0.4.99 – einspaltiges Analyzer-Einstellpanel

**Basis:** v0.4.98, versionCode 139. **Neu:** v0.4.99, versionCode 140. Vollständiger Android-Studio-/Xiaomi-Test offen.

## Navigation

In der Hauptansicht führt `⚙ Einstellungen` oder das Dreipunktmenü zur eigenständigen Einstellansicht. Oben befindet sich die aktive Setup-Auswahl (drei bestehende lokale Plätze) mit `Laden`, `Speichern`, `+ Neu` (nur erster freier Platz). Darunter liegen die acht aufrufbaren Unterpanels Spektrum, Audio, Anzeige, Statistik, Automatiken, Kalibrierung, Setups und Diagnose. Ein Zurück-Link im Unterpanel führt zur Übersicht; der Zurückpfeil der Einstellansicht führt zum Live-Analyzer. **Keine** zweite Mikrofoninstanz oder zweite Recorderstrecke.

## Funktionale Zuordnung

- **Spektrum:** 20/50/100/200 Hz, FFT-Hop 10/50/100/200/500/1000 LF-Samples, Mittel- und Rauschboden-Kurven; FFT 4096 / 1 kHz / Hann als Status. Spektrum-Max-Hold bleibt ausdrücklich offen.
- **Audio:** Analysegain 0–50 dB und tatsächliche gemeinsame Audioquelle; Mithören/Audio-Ausgangsauswahl derzeit nicht implementiert.
- **Anzeige:** separate Texteingaben für **Minimum dBFS und Maximum dBFS**, Button zum Prüfen/Übernehmen/Speichern, Presets −120…−60, −120…−40 und −120…0; Wasserfall-Zeilenintervall, Wasserfallmodus Aktuell/Mittelwert/Maximum. Eingabe nur für Ganzzahlen zulässig: `min∈[-240,40]`, `max∈[-230,50]`, `max−min≥10`. Grenzwerte verwenden die bestehende `setDisplayRange`-Methode; keine Änderung des physikalischen Audiosignals. Autorange-Einstellungen unter Automatiken; Tipp auf Spektrumachse bleibt.
- **Statistik:** 120-s-L50/L90 (2–25 Hz), gemeinsamer Reset mit roter Average-Kurve. Dauer derzeit fest.
- **Automatiken:** Autorange-Dauer 1/2/3/5/10 s, Reserve ±0/3/5/10/15 dB und Auto-PNG.
- **Kalibrierung:** derzeit unkalibriertes dBFS, spätere Profil- und Referenzkalibrierung informativ.
- **Setups:** oben 3 Slots speichern Bereich, FFT-Hop, Wasserfallintervall/-modus, Min/Max dBFS, Analysegain, Kurven, Autorange und Auto-PNG. Kein bestehender Slot wird durch `Neu` überschrieben. Benannte JSON-Setups/Import/Export noch nicht implementiert.
- **Diagnose:** tatsächliche PCM-Pegel, FFT, Fehler/Drops, Alter, PNG-Dateiname, Stop.

## PNG-Speicherort

Der tatsächliche Exportcode in `LiveAnalyzerController.export` speichert ab API 29 (Android 10) über `MediaStore.Downloads` unter `Environment.DIRECTORY_DOWNLOADS + "/LFN-Scout"` mit dem Dateinamen `LFN-Scout_Analyzer_yyyy-MM-dd_HH-mm-ss_SSS.png`.

**Xiaomi / aktuelles Android:** Dateimanager → interner Speicher → **Download** (ggf. „Downloads“) → **LFN-Scout**. Manuelles PNG wird über `📷 PNG` ausgelöst. Auto-PNG wird nach der ersten vollständigen 120-s-Wasserfallhistorie und danach höchstens pro 120-s-Zyklus erzeugt, sofern eingeschaltet; Schreibfehler erscheinen als `PNG-Fehler: ...` im Analyzer-Status.

Auf Android 9 oder älter speichert der unveränderte Fallback app-spezifisch unter `Android/data/de.brummbeobachter.app/files/Pictures/Analyzer/` statt im allgemein zugänglichen Download-Ordner. Für diesen Fallback ist kein direkter Browserzugriff garantiert.

## Nicht ausgeführt / Validierung

- Android-Compose-Kompilierung (`:app:testDebugUnitTest`) nur mit vollständigem Android SDK möglich. Auf Xiaomi kontrollieren: Rücknavigation, alle acht Kategorien, eigene Min-/Max-Zahlen speichern und nach Restart überprüfen, Setup-Slot laden sowie manuelles und Auto-PNG über Datei-App.
- Bestehende Audio- und WAV-Dateien/Supabase-Recovery-Daten dürfen nicht geändert werden.
