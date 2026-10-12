# LFN-Scout v0.5.18 – Lokaler Audio-/Statistik-Save (25.09.2026)

**Stand:** Implementiert als Android-Quellcode; **Android/Compose-Gradle-Build und Xiaomi-Abnahme noch NICHT nachgewiesen**. Der vom Nutzer für v0.5.17 bestätigte Portrait-/Analyzer-Betrieb ist kein Beleg für v0.5.18. `versionName=0.5.18`, `versionCode=161`, Paket-ID unverändert. Kein Schema-/Function-/Datenänderung in Supabase.

## 1. UI

Hauptnavigation mit eigenem Analyzer-Zugang, danach Zusatzfunktionen, Einstellungen, Seismik. Verwendete Vektorsymbole `activity`, `blocks`, `settings`, `waves` als lokale Android-Drawables. Alle übrigen Navigations- und Portraitvorgaben bleiben erhalten.

Beobachter-Audiokarte und Analyzer haben ein gemeinsames lokal persistiertes SAVE-Setup: Aufnahme-/Statistikfenster 30–600 s (**Default 120 s**; Grenze vorläufig), Gesamtstatistik MIN/MAX 1–200 Hz (**Default 2–25 Hz**, MIN < MAX); WAV immer 1.000 Hz, 16 Bit, Mono. Die Filterbank des 8-kHz-Analyzers liefert `FILTER MIN/MAX` und tatsächliche Bandzahl 2–40. In der Direkt-Betriebsart ohne verfügbare FAST-Filterbank werden lokal zehn Ersatz-Statistikbänder innerhalb des Gesamtbereichs gerechnet. Im Beobachter ohne Analyzer-Setup ebenso lokale zehn Ersatzbänder. Dies sind **NICHT** die historischen 8-kHz-FAST-Messwerte; im JSON explizit gekennzeichnet.

Analyzer-Button `SAVE AUDIO` ist nur bei RUN, mindestens eingestellter Dauer an zusammenhängendem 1-kHz-Rohpuffer, echtem (nicht TEST-)Signal und freier gemeinsamer Tagesquote aktiv. RUN/SAVE friert die letzten `duration_s` Sekunden ein. PAUSE friert den letzten Abschnitt im Moment des Pausierens ein; später SAVE bezieht sich genau darauf. Fehler beim Speichern ermöglichen erneut SAVE desselben PAUSE-Snapshots. Aktive FFT/Wasserfall laufen bei RUN/SAVE weiter. 600-s-Puffer auf maximal etwa 1,2 MB PCM (plus Clippingflags) begrenzt. Shared raw-hub Subscriber, **kein zweiter Mikrofonzugriff**. Quellenwechsel/Abbrüche leeren den gültigen Puffer. Ausgangs- und Eingangsrate getrennt protokolliert.

## 2. Pegel und lokale Berechnung

Die 1-kHz-WAV wird **nicht** durch den Analyzer-Anzeigegain verstärkt. Seine FFT-Pegel (Live/Average/MaxHold) werden um den tatsächlich angewandten Anzeigegain **einmalig in dB zurückgerechnet**; die FAST-Filter-Pegel aus dem Rohstrom werden nicht doppelt korrigiert. Beobachter-WAV enthält bei eingestelltem Gain die Aufzeichnungsverstärkung: auch beim bestehenden Rolling-captureNext-Zweig wird vor dem WAV-Schreiben der Gain genau einmal angewandt; statistische Eingangsamplituden werden mit `10^(-gainDb/20)` korrigiert (bei +50 dB also −50 dB in Pegeln). Bereits übersteuerte WAV-Daten können nicht zurückgerechnet werden und werden markiert. Alte historische 16:1-Pegelabweichung, nicht-validierter Mikrofon-Frequenzgang und dB-SPL-Kalibrierung sind damit **nicht behoben**.

`StatisticsWindowEngine` verarbeitet **genau die gespeicherten 1-kHz-WAV-Samples**. Gesamt- und bis zu 40 nominale Biquad-Filterstatistiken über ganze 1-s-Epochen: Leq, Min/Max, L1/L10/L50/L90/L95/L99, arithm. Pegelmittel, Std-Abweichung, Crest-Faktor und Exzess-Kurtosis. Spektral: 2048er-Hann-Fenster, 50% Überlappung, PSD-Mittel/Median/Max und dominante Binfrequenz (Roh-WAV-Pegel referenziert). Keine genormte Pegelmessung; keine validierte präzise Grenzfrequenz oder FAST-Gleichwertigkeit. Impuls-/Modulationsalgorithmus und originalgetreue FAST-Zeitleistenstatistik bleiben `NOT_IMPLEMENTED` bzw. `NOT_AVAILABLE`. Kein irreführender Nullwert.

## 3. Dateien und Cloud-Abgrenzung

Analyzer legt `Downloads/LFN-Scout/LocalSaves/LFN_<UUID>_1k.wav`, `_statistics.json`, `_manifest.json` ab; Manifest mit SHA-256 von WAV und JSON. Teilschreibfehler sollen alle drei Dateien zurückrollen. Beobachter behält die vorhandene WAV-Archivierung und bestehende Supabase-Übertragung **unverändert** und ergänzt dazu lokal `LFN_<recording_id>_statistics.json` als Sidecar mit echter Archiv-WAV-Kennung; KEIN Analyzer-Setup im Beobachter-Sidecar. Analyzer-Sidecar enthält wirksame LIVE-Einstellungen. Statistikberechnung erfolgt außerhalb der Echtzeit-Audio-Verarbeitung. Quotencheck/Markierung via DataStore-Transaktion für beide Panels; lokale Analyzer-Saves werden **nicht** an Supabase gesendet. Es gibt hier noch keine gemeinsame transaktionale WAV/JSON/Cloud-Outbox; vollständiger gemeinsamer Upload, separate FAST-Ereignisse, automatisierter Backup/Restore sind Folgeversionen.

**Datenschutz:** Das Projekt-ZIP kann private Supabase-Recovery-Daten enthalten; nicht öffentlich teilen, Secrets nicht ins Android-APK übernehmen. Zum Herunterladen im Browser vorzugsweise ChatGPT-Bibliothek nutzen.

## 4. Verifikation

JVM-Test des reinen Statistik-Kerns (40 Bänder, ±Gain, L90, Frequenzpeak); Kotlin-Host-Kompilation des Puffers gegen Stubs; statischer Quell-/XML-Test; ZIP-Integritätsprüfung. **Vollständiger Android-/Compose-Build mit Android SDK und Xiaomi-Test müssen auf Windows folgen.** Pflicht: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`, Gerätetest `docs/project/TESTPLAN_v0.5.18.md`. Den funktionierenden v0.5.17-Stand bis dahin separat behalten.
