# Projektübergabe LFN-Scout — technischer Stand v0.5.00

**Datum:** 20.09.2026 · **Quellbasis:** `LFN-Scout_Android_v0.5.00.zip` · **App:** `versionName=0.5.00`, `versionCode=141` · **Status:** Testkandidat, keine Gerätefreigabe.

## 0. Benutzung dieses Dokuments in einem neuen Chat

1. Diese Datei, `PROJECT_STATUS.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md` sowie `docs/reconstruction/PROJECT_STATE_CURRENT.md` **vor jeder Änderung** lesen. Den echten Code und die offiziellen Dokumente in `docs/project/` als Grundlage verwenden; ältere Versionsabschnitte sind historisch.
2. Unveränderte Produktivquellen aus **v0.5.00** beibehalten. Dieses HANDOFF-Archiv aktualisiert **Dokumentation und den zuvor veralteten Rekonstruktionsprüfer**, aber **keine** Audio-, Android-UI-, DSP-, Recorder-, Backend- oder WAV-Produktivfunktion und **keine** App-Versionskennung. Es ist keine neue App-Version.
3. Für die **nächste tatsächlich neue App-Lieferung** Versionsnummer und versionCode erhöhen, vollständiges ZIP liefern, alle betroffenen Dokumente + Hashmanifest aktualisieren. Status nie aus vorheriger Version ableiten. Keine eigenständig gestarteten Mikrofone.
4. Ein Build auf dem Gerät/PC ist nicht automatisch bewiesen, wenn Kotlin-Host- oder statische Tests hier erfolgreich laufen. Erst `:app:testDebugUnitTest`, `assembleDebug` sowie Xiaomi-Regressionsprüfung durchführen.
5. **Privat:** Die ZIP enthält Supabase-Schema/Daten-/Konfigurations-Recovery und 53 WAVs. Nicht öffentlich hochladen; insbesondere GitHub-Sync nur nach dem Ignorier-/Secrets-Audit. Keine Service-Role-Secrets in den Android-Client einbauen.

## 1. Projektidentität, Architektur und Invarianten

| Eigenschaft | Iststand / Nachweis |
|---|---|
| Name | LFN-Scout; ältere interne Package-/DataStore-Namen dienen der Kompatibilität. |
| Android | `applicationId`/`namespace` `de.brummbeobachter.app`, minSdk 23, targetSdk/compileSdk 36, JVM/JDK 17 (`app/build.gradle.kts`). |
| Build | Ein Projekt; Windows: `PREPARE_PROJECT.bat` → Android Studio oder `gradlew.bat`; mobil über GitSync/GitHub Actions. `.git/`, `.tools/`, `local.properties` nicht ins ZIP. |
| Backend | Supabase Edge Functions; Rating 0 = echte Antwort „nichts wahrgenommen“, −1 = automatische Nichtantwort. Standort nur gemäß Berechtigung, ggf. 0/0. Rolling-WAV nicht in Supabase hochladen. |
| Aufnahme | Existierende normale 120-s-WAV und optionaler Rolling-RAM-Puffer. Recorder-Ausgabe 1 kHz, mono PCM16; gemeinsamer 8-kHz-Roh-Capture. Bestehender Recorder-Gain und separater Analyzer-Gain nicht verwechseln. |
| Analyzer | `:analyzer-core` für plattformunabhängige LF-FFT/PCM/Zeilenaggregation; Android-Adapter, Controller und Compose-UI im `:app`. Shared PCM, **keine zweite Mikrofoninstanz**. |
| Live-Eingang | Roh-PCM 8 kHz, UNPROCESSED auf dem gezeigten Xiaomi angefordert; Analyzer-seitiger 129-Tap-FIR und Decimation 8:1 zu 1 kHz mit Double-PCM. UNPROCESSED allein beweist keine vollständige Abschaltung herstellerseitiger DSP. |
| Backend-Recovery | Schema-/Daten-/Rollen-/Auth-/Storage-Snapshots und 53 lokale Storage-WAVs; Restore auf neues leeres Supabase-Projekt **nicht** praktisch E2E-geprüft. |

## 2. Versionschronik und letzte nachweisliche Funktion

| Stand | Veränderung bzw. Nachweis | Bewertung |
|---|---|---|
| v0.4.89 | Highspeed-Impulsdiagnose, Schwellwert/Noise-Faktor/Presets; Testmeldungen über Xiaomi und frühere WAV-Strecke. | Nicht als neuer Impuls-Wasserfall zu verstehen. |
| v0.4.90 | Erstes klassisches Analyzer-Panel. Screenshot: volle Historie, Kurven fehlen, −240 dBFS; Design abgelehnt. | **Gerätefehler belegt.** |
| v0.4.91–v0.4.93 | Separater 8-kHz-Roh-PCM→FIR→LF-FFT-Weg; Teststartup- und Controller-Getter korrigiert. | Nutzer markierte **7/7 Xiaomi-Funktionsschritte für v0.4.93 erledigt**: Rolling, Spektrum, Wasserfall, Reaktion, 100/200 Hz, Pause, Panelwechsel. Keine Kalibrier-/PNG-Freigabe. |
| v0.4.94 | FFT-/Wasserfall-Hop getrennt, Ticks und Zeitrichtung; Nutzer zeigte reales farbiges Spektrum, Wasserfall und Roh-/LF-Diagnose ohne Drops/Resets/Fehler. | **Auf Xiaomi sichtbar und vom Nutzer als grundsätzlich funktionierend beschrieben**; kein genereller APK-Testnachweis. |
| v0.4.95–v0.4.95.1 | Hop 10, 120-s-Zeitbasis, kompakte Header, Default −120…−60 dBFS; Hotfix für fehlendes `acceptDiagnostics`. | v0.4.95-Kotlin-Compilefehler wurde vom Nutzer gemeldet; Hotfix lokal statisch geprüft. |
| v0.4.96–v0.4.97 | Autorange, Statistikreset, Cursor, Achsenlayout; Perzentile und schmalere Farbskala. Nutzer-Screenshot v0.4.96 mit realem Plot. | Teilfunktionen optisch sichtbar, jedoch weder Autorange-Persistenz noch Volltest nachgewiesen. |
| v0.4.98 | Wasserfallmodi Aktuell/Mittelwert/Maximum pro Zeilenintervall. | Implementiert, synthetische Tests, **Modusvergleich am Xiaomi nicht belegt**. |
| **v0.4.99** | Acht einspaltige Settings-Unterpanels + frei editierbare, persistente dBFS-Min/Max-Werte. Nutzer-Screenshots mit live Spektrum/Wasserfall, Auto EIN, weißem Streifen unten. | **Letzter durch Screenshot belegter Gerätelauf**. Neue Settings, PNG-Datei und Setup-Recall nicht separat nachgewiesen. |
| **v0.5.00** | Dunkler `BoxWithConstraints(fillMaxSize())`-Viewport, adaptive Plothöhen, untere Karten/Redundanzen entfernt. | **Neuester Quellstand, Android-Build und Xiaomi-Screenshot OFFEN.** Weißer Streifen NICHT als behoben bestätigen. |

**Warnung:** Historische `docs/project/analyzer_v0.7/`-Bilder und 0.4.90/0.4.91-Dokumente sind Konzept/Entwicklungshistorie. Das Bild `C13_Mithoeren_EIN_AUS_FINAL.png` betrifft den Designzustand, ist keine Garantie, dass Mithören realisiert ist. Die am 20.09. erzeugte Einstellungsillustration kann UI-/Versions-/Messangaben enthalten, die nicht aus dem Android-Code stammen.

## 3. Tatsächlich vorhandene Live-Analyzer-Funktionen (implementiert ≠ gerätegetestet)

- `LiveAnalyzerController.kt`, `LiveAnalyzerScreen.kt`, `LiveAnalyzerSettingsPanel.kt`, `AnalyzerSessionGateway.kt`, `AnalyzerRawLfSession.kt`, `RawLfDecimator.kt`, `LiveAnalyzerCore.kt`, `WaterfallAggregator.kt` (Quellpfade über das Manifest prüfen).
- Lineare LF-Bereiche 0–20/50/100/200 Hz; FFT 4096 bei 1 kHz (≈0,244 Hz Raster), Hann, Eingangs-Gain separat 0–50 dB; FFT-Hop 10/50/100/200/500/1000 Samples. Hop ist **Fenster-Vorschub**, nicht Decimation und keine Mittelung von 10 Samples. Bei Hop 10 rechnerisch 100 FFT/s, nicht 100 UI-Bilder/s; UI-Polling 50/100 ms.
- Spektrum: gelb aktuell, rot zeitlich gemittelt; geschätzter Rauschboden optional, Default AUS; **kein Spektrum-Max-Hold**. L50(2–25)/L90(2–25) oben rechts, gemeinsame Reset-Fläche auch für die Average-Kurve. Diese dBFS-Werte sind relativ und nicht SPL-kalibriert.
- Manuelle Min-/Max-dBFS-Grenzen (zwei freie ganzzahlige Felder, Prüfung `max-min>=10`), Presets und Autorange per Tipp auf linke dBFS-Achse (2 s, ±5 dB fest, beide einstellbar). Autorange verwendet Frequenzperzentile 5/95 und Zeitperzentile 10/90; neue Grenzen persistent. Der voreingestellte Bereich ist −120 bis −60 dBFS (nicht die automatisch oder manuell zuletzt gespeicherten Werte überschreiben).
- Ziehbarer Cursor, Readout ohne Rahmen, Tipp auf Readout schaltet ihn aus; kompakter rot umrandeter L50/L90-Reset (3 dp Innenrand). dBFS-/Hz-Achsenticks; Wasserfallzeit neu oben→alt unten; Farbskala links, schmal und mit innenliegenden dBFS-Werten; gleiche Frequenz-Plotränder für Spektrum/Wasserfall. UI- und Offscreen-PNG folgen derselben Konvention.
- Wasserfall: Standard 120 s, Beispiel 480 Zeilen × 250 ms, Intervall wählbar; Modi `Aktuell` (Default), `Mittelwert` (lineare FFT-Leistungen binweise innerhalb der Zeile, dann dB), `Maximum` (binweises Maximum im Zeilenintervall). Moduswechsel setzt **nur** Wasserfallhistorie/Zeilenaggregation und Auto-PNG-Zyklus zurück. Starke FFT-Überlappung begrenzt Impuls-Zeitauflösung auf das ca. 4,096-s-Fenster.
- Offscreen-PNG manuell `📷 PNG`; Auto-PNG ab voll gefüllten 120 s und danach pro weiterem Zyklus, sofern eingeschaltet. Android 10+ via MediaStore unter `Download/LFN-Scout/LFN-Scout_Analyzer_yyyy-MM-dd_HH-mm-ss_SSS.png`; Android 9− app-spezifisches `.../files/Pictures/Analyzer/`. **Dateischreib- und Panelwechseltest offen.** Auto-PNG ist keine WAV-Aufnahme.
- Settings seit v0.4.99: Spektrum, Audio, Anzeige, Statistik, Automatiken, Kalibrierung, Setups, Diagnose. Drei lokale persistente Setup-Slots mit Laden/Speichern/Neu; keine benannten JSON-Imports. Mithören und Kalibrierungsprofil sind lediglich als später vorgesehen bezeichnet.
- v0.5.00-Hauptbild entfernt die Karten Mithören/Superfast/Anzeige und unten Einstellungen/Diagnose sowie permanente Hinweiszeile. Settings über ⋮; Diagnose als Unterpanel. PNG/Auto bleiben unten. Höhe auf Basis des Viewports, bei wenig Platz Scroll-Fallback.

## 4. Nicht mit Gerätetest zu verwechseln: konkrete Testhistorie

- Nutzerprotokoll v0.4.90: `:app:testDebugUnitTest` **BUILD SUCCESSFUL**, aber Gerät ohne Live-Kurven (beides kann gleichzeitig gelten).
- Nutzerprotokoll v0.4.91: `:analyzer-core:test` **FAILED**, Nullsignaltest `.last()` vor erstem FFT-Fenster; v0.4.92 testseitig korrigiert.
- Nutzerprotokoll v0.4.92: `:app:compileDebugKotlin` **FAILED** (`spectrumCallbackErrors`); v0.4.93 Getternamen korrigiert.
- Nutzerprotokoll v0.4.95: `:app:compileDebugKotlin` **FAILED** (`acceptDiagnostics`); v0.4.95.1 Methode wiederhergestellt.
- v0.4.97 lokale JVM-Reflection-Tests 35/35 und statischer Vertrag 19/19 PASS; **kein** originaler Android-/Compose-Gradle-Compile.
- v0.4.98 Wasserfall-Selbsttest 6/6; v0.4.99 statischer Vertrag 16/16; v0.5.00 statisch 20/20 und Wasserfall-Selbsttest 6/6; der v0.5.00-LF-Selbsttest wurde damals begonnen, **ohne belegten Abschluss**. Diese Ergebnisse sind lokal, nicht auf dem Xiaomi.
- Für **v0.5.00** keine dokumentierte erfolgreiche `:app:testDebugUnitTest`-/`assembleDebug`-Ausführung auf Windows, keine APK-/Gerätefreigabe, kein Beleg, dass weißer Bereich wirklich verschwunden ist.

## 5. Offene Defekte, Fragen und technische Risiken

| Priorität | Status | Sachverhalt / notwendiger Nachweis |
|---|---|---|
| P0 | OFFEN | v0.5.00 unter Windows/Android Studio kompilieren, App-Tests laufen lassen und am Xiaomi installieren. Keinen Teststatus aus v0.4.99 erben. |
| P0 | OFFEN | Weißer Streifen nach neuer Viewport-Implementierung tatsächlich weg? Beide Diagramme füllen die Fläche, Ticks nicht abgeschnitten, kleine Displays scrollen. |
| P1 | OFFEN | Echter PNG-Export manuell/Auto nach 120 s, Speicherort, Dateiname, Fehler, 2×120-s-Zyklen und Offscreen-Fortführung beim internen Panelwechsel; Berechtigungen/Dateimanager prüfen. |
| P1 | OFFEN | Wasserfallmodi vergleichen, Werte physikalisch plausibel, Moduswechsel setzt nur Historie/PNG; CPU/FPS bei FFT-Hop 10, Wärmelast und Rolling/WAV gleichzeitig messen. |
| P1 | OFFEN | Einstell-Kategorien auf Xiaomi: Lesbarkeit/Scrollen/Rücknavigation, manueller dBFS-Min/Max und Presets, Autorange persistiert, drei Setup-Slots ohne Überschreiben, Neustart-Wiederherstellung. |
| P1 | OFFEN | L50/L90-Reset/5-s-Average, Cursor Ziehen/Ausschalten, Rauschboden default AUS, UNPROCESSED-Routing bei Quellenwechsel; Pegel/2–25 Hz nicht absolut kalibriert. |
| P1 | OFFEN | Regression: 120-s-WAV, 30-min-Rolling, Rating 0 versus −1, Offline-Retry, Seismik, Supabase-Integrität auch nach längerem Analyzerbetrieb. |
| P1 | OFFEN | Live-Analyzer ohne laufenden Rolling-Buffer: Ausgangsbedingung/Startbutton prüfen; Screen-off, App-Hintergrund, Telefonate/USB-Mikrofon, Audio-Unterbrechung nicht freigegeben. |
| P1 | OFFEN | Supabase-Backup E2E auf **neuem leeren** Testprojekt inklusive Auth/Storage/Functions/Client; private Recovery nicht public committen. |
| P2 | OFFEN | Der ältere `verify_reconstruction.py` war auf 0.4.91 fest verdrahtet; im HANDOFF-Archiv ist dies korrigiert. Vollständige weitere historische Doku kann veraltete Überschriften enthalten; bei Widersprüchen Code/`PROJECT_STATUS.md` bevorzugen. |

## 6. Zurückgestellt / nicht implementiert

- Mithören mit Ausgangsroute/Kopfhörer, Monitoring-Gain, optionalem Monitoring-Bandpass und ggf. WAV-Monitoring; Aufnahme- und Clippingkonflikte vor Implementierung testen.
- Eigener Superfast-/Impuls-Wasserfall und erweiterte Impulsanalyse; bisherige Highspeed-Diagnose bleibt separater Entwicklerpfad.
- Spektrum-Max-Hold/Peak-Kurve (ausdrücklich **noch nicht** implementieren; später gewünscht). Rechte Spalte mit letztem oder 4 realen Radiushistogrammen; Datenquelle/Einheiten/Lifecycle vor UI-Zusage klären.
- Echte SPL-Kalibrierung inkl. Mikrofon-/Frequenzprofile, Gerätereferenz 94 dB/1 kHz, Rauschboden-Kalibrierung; bis dahin ausschließlich dBFS uncal.
- 48-kHz-Mikrofon-Capture/Breitbandanzeige bis 20 kHz nur nach Hardware-Routing-/CPU-/Testgate. Keine neue Mikrofoninstanz. Eigenständige Standalone-APK mit gemeinsamem Core/UI später.
- Benannte JSON-Setups, Import/Export, zusätzliche FFT-Fenster/FFT-Größen, flexible Statistikbands und Historien, optionale Trigger/Schnittstellen und 72-h-/Wochen-Monitoring aus alten v0.7-/Projektplänen: keines davon als gegenwärtige App-Funktion ausgeben.
- Größere InversionAnalyzer-Integration (eigenes Inversionspanel/KIT-Mast) erst im gesonderten Projektzweig; historische Version-5.0-Roadmap nicht mit dem technischen Patchstring 0.5.00 verwechseln.
- Nur teilweise bereits vorbereitet: GitHub APK-Artefakte, Windows-Updater, umfangreiche Projekt-/Chat-Rekonstruktion; aktuelle Funktionsfähigkeit nicht aus Dokumenten ableiten.

## 7. Nächste Arbeitsschritte in der richtigen Reihenfolge

1. VOLLSTÄNDIGES Projekt in neuen Ordner entpacken; `PREPARE_PROJECT.bat` und Windows JDK-17-Prüfung durchführen. Archiv nur in privater Umgebung verwenden.
2. `gradlew.bat :app:testDebugUnitTest --stacktrace` ausführen (Projektkonfiguration zieht Coretests hinzu), danach `gradlew.bat assembleDebug --stacktrace`. Fehlende Referenzen, Compose-Fehler oder Warnungen dokumentieren; **keine** stillen Workarounds per `ignoreFailures`.
3. Via Android Studio Run auf Xiaomi; Screenshot des v0.5.00-Hauptbilds, sichtbare Diagrammhöhe, White-Space, Menü/Settings und Ticks bewerten. Bei Fehler erst reproduzierbaren Codebefund, dann neue Versionsnummer.
4. 10–15 Minuten Live-Messung mit Rolling, FFT-Hop 10/100, zwei Wasserfallmodi und PNG manuell; für Auto-PNG länger als 240 s. Prüfen, ob alle PNGs im genannten Ordner erscheinen, ggf. auch bei Panelwechsel.
5. Setups, dBFS-Autorange mit kurzem negativen Dip, L50/L90-Reset, Cursor, User-Rating und 120-s-WAV gegen Regression testen. Testergebnisse PASS/FAIL/NOT RUN in `PROJECT_STATUS.md` protokollieren.
6. Erst nach reproduzierbarer Gerätefreigabe geplante Mithör-/Histogramm-/Kalibrier-/Superfast-Funktionen priorisieren. Die nächste Code-Version nicht ohne begründeten Nutzerauftrag erweitern.

## 8. Quellenpfade und maßgebliche Dokumente

- `PROJECT_STATUS.md` – komprimierte, aktuelle Statusmatrix; `CHANGELOG.md` – historisierte Änderungen.
- `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `GITHUB_BUILD.md`, `PREPARE_PROJECT.bat` – verbindliche Entwicklung und Build.
- `docs/reconstruction/00_RECONSTRUCTION_INDEX.md`, `PROJECT_STATE_CURRENT.md`, `03_SYSTEM_ARCHITECTURE.md`, `06_AUDIO_SYSTEM.md`, `07_WATERFALL_AND_SPECTRUM.md`, `11_SETTINGS_AND_PERSISTENCE.md`, `15_TEST_AND_VALIDATION.md`, `16_KNOWN_ISSUES_AND_LIMITATIONS.md`, `21_SUPABASE_DISASTER_RECOVERY.md` – Gesamtsystem.
- `docs/project/ANALYZER_LAYOUT_v0.5.00.md`, `ANALYZER_SETTINGS_PANEL_v0.4.99.md`, `ANALYZER_LIVE_v0.4.98.md`, `ANALYZER_LIVE_v0.4.97.md`, `ANALYZER_LIVE_v0.4.96.md` – neuester Analyzer-Iststand und historische Änderungsbasis.
- `docs/project/analyzer_v0.7/` inklusive `assets/concepts/BILDREGISTER_v0.7.md` und `C13_Mithoeren_EIN_AUS_FINAL.png` – normative/fachliche Altübergabe, **mit aktueller Implementierung vergleichen**. `docs/superpowers/` enthält nur interne Entwicklungspläne.
- `BUILD_VERIFICATION_v0.5.00.md` und ältere Berichte liefern lokale Testnachweise/Defizite; nicht als aktuelle APK-Freigabe umdeuten.

**Bei Unklarheiten:** Der neue Chat soll den Code prüfen und den fehlenden Nachweis ausdrücklich als offen kennzeichnen, nicht aus früheren Konzeptscreenshots oder alten Testergebnissen erraten.

## 9. Eingebettete visuelle Belege

- `handoff_assets/Xiaomi_v0.4.99_weisser_Bereich.jpg`: reales letztes Gerätebild als reproduzierbare Layout-Referenz; dient **nicht** als Bestätigung der Korrektur in v0.5.00.
- `handoff_assets/Einstellpanel_Konzept_2026-09-20.png`: stilistisches Konzept der einspaltigen Settings. Abweichende „v1.2.0“-Beschriftung und ausgedachte Preview-/Messwerte sind **nicht** Teil des Produktstands; ausschließlich Navigation und Anordnung illustriert.
- `analyzer_v0.7/assets/concepts/C13_Mithoeren_EIN_AUS_FINAL.png`: bisherige Analyzer-Stilreferenz mit Mithören an/aus; Funktion Mithören selbst bleibt zurückgestellt.
