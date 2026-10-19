# Ergänzung v0.5.67 – Release-Gates

Alle neuen und geänderten Dateien vor ZIP-Auslieferung auf Git-Whitespace-Konformität prüfen; nach einem fehlgeschlagenen Build neuen Build erst nach Ursachenbehebung anfordern. Statische Prüfungen niemals als erfolgreiche Android-Kompilierung deklarieren.

# Übergabestatus v0.5.40 – 11.10.2026 (Dokumentationsnachtrag, Android unverändert)

**Aktueller Quellstand:** v0.5.40 / versionCode 187. **Letzter nachgewiesener Android-Windows-Build:** v0.5.39 PASS. **v0.5.40 Android-Build offen.** Offline-Python-Filterbankreport unter Windows 330/330 PASS (Nutzerbericht 11.10.2026 23:48:52). Python-Startfehler und Quoting-Fehler des BAT-Starters sind in dieser Übergabekopie korrigiert. 24-h-Cloud-Queue/Neustart unter v0.5.37/.38 geräteseitig teilweise bestätigt, tatsächliche fristgerechte Löschung und Cloud-Datenbankabgleich offen. **v0.5.41 mit Legacy-/Butterworth-/FFT-Vergleich geplant, noch nicht implementiert.** Vollständige Übergabe: `docs/project/HANDOVER_v0.5.40_2026-10-11.md`.

## v0.5.40 / versionCode 187 – Offline-Filterbankreport (Quellkandidat)
- Neu: `FILTERBANK_REPORT_STARTEN.bat` und `tools/filterbank_validation_report.py` (Python 3, Standardbibliothek). Analytische Antworten, simulierte Filterantwort je Band, Soll-Ist-CSV, Frequenzgang-SVG und Übersprechmatrizen für 10/20 Bänder. Standard 0–100 Hz, konfigurierbare Grenzen, zusätzliche Testfrequenzen nahe 0 Hz und bis 120 Hz.
- Lokale Report-Ausführung: 330/330 numerische Vergleiche PASS bei ±0,35 dB, 6 Sekunden synthetischer Sinus und Auswertung des letzten 1-s-Fensters. Keine zusätzliche Android-Messfunktion, keine Mikrofonkalibration und keine Änderung an Cloud/Datenbankschema/24-h-Retention.
- v0.5.39 Windows `testDebugUnitTest`, `assembleDebug`, APK, ACTIVE-ZIP und ADB-Installation am 11.10.2026 23:32:56 PASS (hochgeladene Protokolle). v0.5.40 Windows-Build/Gerätetest OFFEN.
- Validierungsdokumentation: `ANLEITUNG_BUILD_TEST_v0.5.40.md`. O05 bleibt für physikalische Charakterisierung offen.

## v0.5.39 / versionCode 186 – quantitative Filterbank-Softwarevalidierung (Quellkandidat)
- JUnit-Regressionen vergleichen die 1-s-Pegel pro Band mit der **analytischen diskreten Übertragungsfunktion** der implementierten Hochpass-/Tiefpass-Kaskade. Testvektoren: 2, 5, 10, 15, 35, 75 und 95 Hz für 10/20 Bänder, zusätzlich 10–60 Hz mit beiden Bandzahlen.
- Eigene Grenze der Validierung: Nachweis des DSP gegen die aktuelle mathematische Implementierung, **keine** akustische Kalibrierung, keine spektral scharfen Bandpassfilter, keine Messmikrofon-Validierung.
- Die vorhandene Filterbank/GUI/CSV/Cloud und 24-h-Retention bleiben unverändert. Kein Cloud-Schema- oder API-Eingriff.
- v0.5.38: Windows Gradle Unit/APK/ADB PASS am 11.10.2026 23:20, Gerätelog v0.5.38: `pending_upload=0 retained_sent=1 deleted_expired=0 unreadable=0` PASS. 24-h-Löschung, Cloud-Tabellenvergleich und Offline-Retry weiterhin offen.
- **v0.5.39:** statische Quellprüfung möglich, Windows-Gradle-/ADB- und Gerätetest **noch offen**.

## v0.5.38 / versionCode 185 – präziser Cloud-Warteschlangenstatus (Quellkandidat)
- Lokale Queue-Diagnose trennt `pending_upload` von `retained_sent`; `pending` in `ROLLING_CLOUD_SYNC/RETRY` bleibt aus Kompatibilitätsgründen Gesamtzahl aller lokalen Dateien (auch gesendet).
- Neue Diagnosewerte: `deleted_expired`, `other_observer`, `unreadable`, `next_delete_at_ms` (frühester bestätigter Löschzeitpunkt, Unix-Millisekunden; 0 falls keiner).
- Bestätigte Datensätze werden weiterhin frühestens 24 h nach Cloud-Bestätigung beim nächsten Queue-Durchlauf gelöscht; weder erneute Übertragung noch Cloud-Schemaänderung.
- Gerätetest v0.5.37 am 11.10.2026: Gradle/Unit/APK/ADB PASS, Cloud-Sendebestätigung + Aufbewahrung + Nicht-Neusendung über Android-Prozessneustart PASS. Reale 24-h-Bereinigung sowie direkter Abgleich Cloud-Tabelleninhalt noch offen.
- Android-Build v0.5.38 noch nicht ausgeführt.

## Ergänzung v0.5.37 – 24-h-Cloud-Warteschlangen-Aufbewahrung
Erfolgreich bestätigte Bucket-Uploads lokal als `SENT_PENDING_DELETE` samt `cloud_confirmed_at_ms` persistent markieren; erst mindestens 24 h nach Bestätigung beim nächsten Durchlauf löschen. Keine erneute reguläre Übertragung. Alte PENDING-Einträge weiterhin wiederholen. Keine Cloud-Migration, kein Schlüssel auf Disk. Status derzeit nur Diagnose-Log, keine GUI.


## v0.5.37 – Cloud-Halbstunden-Gesamtstatistik
Ohne Datenbankmigration und nur über bestehenden Endpunkt. Nur Leq/L50/L90 + Abdeckung, Status, Frequenzbereich, Zeitfenster und App-Version. Vier Impulskennwerte, Bewertungen, Filter- und Epochstatistiken vorerst nicht übertragen. Schlüssel nicht persistent in Retry-Queue speichern. `docs/project/DEFERRED_TASKS.md` weiter pflegen.


## v0.5.37 – Cloud-Halbstunden-Gesamtstatistik
Ohne Datenbankmigration und nur über bestehenden Endpunkt. Nur Leq/L50/L90 + Abdeckung, Status, Frequenzbereich, Zeitfenster und App-Version. Vier Impulskennwerte, Bewertungen, Filter- und Epochstatistiken vorerst nicht übertragen. Schlüssel nicht persistent in Retry-Queue speichern. `docs/project/DEFERRED_TASKS.md` weiter pflegen.


## v0.5.36 – lokale Beobachtungs-Regression
Bewertung 0–5 nach tatsächlicher responseTime, -1 nach scheduledTime. Ungültige Zeitstempel nicht erraten. Zeitfenster halboffen; IDs deduplizieren. Pflicht: Regressionstests plus Gerätetest Neustart und Nichtantwort. Cloud unverändert.

## Zusatz v0.5.35 – Lokale subjektive Halbstundenwerte

Nur lokal erfasste Beobachtungen nach Installation von v0.5.35 in `HalfHourStatistics_*.csv` aggregieren. `0` ist gueltig, `-1` zaehlt als Nichtantwort und nicht zum Mittelwert. Zeitbasis bei Antwort = response_time, bei Nichtantwort = scheduled_time. Halb-offenes Zeitfenster; Ereignis-ID deduplizieren. Keine Cloud-Schema-/API-Aenderung und keine Uebertragung dieser Aggregatdaten. `docs/project/DEFERRED_TASKS.md` bei jeder Version aktualisieren.

## Verbindliche Aufgabenpflege – DEFERRED_TASKS.md (Dokumentationsnachtrag v0.5.34)

- `docs/project/DEFERRED_TASKS.md` ist die zentrale, dauerhaft mitzufuehrende Liste zurueckgestellter und offener Projektaufgaben.
- Vor **jeder folgenden Version** die Liste lesen, gegen Entscheidungen und den tatsaechlichen Implementierungs- und Teststand abgleichen und aktualisieren.
- Referenzen `R01–R07`, `O01–O08`, `A01–A05` stabil halten; neue Aufgaben mit eindeutigen neuen Kennungen anhaengen, abgeschlossene Aufgaben mit Version und Pruefnachweis kennzeichnen statt kommentarlos loeschen.
- `PROJECT_STATUS.md` und `CHANGELOG.md` sollen relevante Statusaenderungen konsistent enthalten; nicht getestete Funktionen nicht als erfolgreich getestet bezeichnen.
- Der dokumentierte Anfangsstand ist eine Zusammenstellung bisheriger Entscheidungen, **keine** nachtraegliche vollstaendige Code-/Datenbankpruefung.

# Ergänzung v0.5.34
Impulsdiagnose nur diagnostisch aus PRE-GAIN 1-s-Werten, nicht als physikalisch validierten Impulsdetektor bezeichnen. Subjektive Bewertungen erst nach vollständig nachgewiesenem lokalem Ereignispfad aggregieren. Sichtbare Texte weiter „Cloud“, technische IDs unverändert.

# Verbindlicher Zusatz ab v0.5.33
Nutzerseitig sichtbare UI-, Status-, Menü-, Fehler- und Hilfetexte verwenden **Cloud** statt **Supabase**. Interne API-/Datenbank-/Migrationsnamen und URLs nicht umbenennen.
1-s-Epoch-CSV optional AUS, PRE-GAIN, 300-s-Batch, öffentlich unter Downloads, keine Sekundenschreibzugriffe und kein Cloud-Upload.
Subjektive Beobachtungsaggregation nur mit nachweislich vollständiger lokaler Datenquelle als implementiert deklarieren.

---
# Ergänzung v0.5.26 – Supabase-Statistik und 30-Minuten-Buckets

- Bestehende Supabase-Tabellen/Legacy-Requests bleiben rückwärtskompatibel; nur additive Migrationen.
- `observer_id` bleibt zentrale Teilnehmer-ID; keine parallele `user_id`.
- Neue Statistikdatensätze tragen immer Observer- und Location-Kontext; `comment` ist einheitlich optional vorgesehen.
- Bisher etablierte Statistikparameter (u. a. Leq/L50/L90) behalten feste, benannte Felder. `P01...P32` nur für flexible Zusatzparameter mit versioniertem Lookup.
- Audio-Statistik: 1 Overall-Datensatz plus 0..40 Filterdatensätze pro WAV.
- Unabhängige Zeitreihe: exakt ausgerichtete 30-Minuten-Buckets mit `leq_dbfs`, `l50_dbfs`, `l90_dbfs`, Frequenzbereich, Coverage/Qualität und Geolocation.
- `audio_source=UNP/MIC` bleibt Mikrofonquelle; Herkunft (`OBSERVER`/`ANALYZER`) separat als `capture_origin`; Trigger separat als `trigger_type`.
- Android-Statistikupload bleibt in v0.5.26 noch deaktiviert, bis Remote-Migration/Edge-Functions und Legacy-Kompatibilität real getestet sind.

---

# Ergänzung v0.5.23 – gemeinsamer Audio-Hauptpfad und Bildkommentar

- Analyzer-Audio darf für den Übergang **kein neues Audio-Unterverzeichnis** anlegen. Es muss direkt denselben bestehenden Hauptpfad wie Beobachter-Audio verwenden, damit Retry und 24-h-Retention unverändert greifen.
- Herkunft von Audio (`OBSERVER`/`ANALYZER`) als Metadatum führen, nicht durch getrennte Verzeichnisbäume erzwingen.
- Analyzer-WAVs sollen denselben Upload-/Retry-/Retention-Codepfad wie Beobachter-WAVs nutzen. Statistik-JSON erst nach Einrichtung eines passenden Supabase-Schemas hochladen.
- Bei manuellem Analyzer-Bildsave muss der Bildzustand vor Öffnen des Kommentardialogs fixiert werden; Kommentar erst danach erfassen und vor finalem Speichern unten in den Wasserfall rendern.
- Appweite UI-Regel bleibt: keine spontanen Zusatztexte in bestehende Display-/Messzeilen einschieben.

---

# Ergänzung v0.5.22 – keine komplexen PowerShell-Ausdrücke in CMD-`for /f`

Für Windows-BAT-Wrapper keine verschachtelten Inline-PowerShell-RegEx-Ausdrücke mit Quotes/Klammern in `for /f (...)` verwenden. v0.5.21 zeigte einen realen CMD-Syntaxfehler noch vor dem PowerShell-Start. BAT-Wrapper bleiben möglichst dünn und versionsneutral; Versionslogik liegt in `.ps1` und liest `app/build.gradle.kts`.

---

# Zusatzdirektive v0.5.20 – Wasserfall, Kommentar, Navigation und kompakte Beobachter-UI

- WAV-Wasserfall: gain-korrigierte Spektralwerte nicht vor der Farbumrechnung auf einen Display-Floor wie -120 dBFS klemmen. Numerische Messwerte erhalten; Begrenzung erst bei der Color-Mapping-Normierung.
- Analyzer `SAVE AUDIO`: Audiofenster/Snapshot immer **vor** Öffnen des Kommentardialogs vollständig fixieren. Bedien-/Tippgeräusche nach dem SAVE-Tap dürfen nicht in den gespeicherten Clip gelangen.
- Beobachter und Analyzer verwenden denselben Audiokommentar-Dialog und dieselben konfigurierten Audio-Timeouts; keine getrennten Timeout-Implementierungen.
- Analyzer-Kommentar dem lokalen Recording zuordnen und in dessen Statistik-/Manifestdaten speichern.
- Analyzer-Zusatzinfo neben `SAVE AUDIO` zweizeilig, ohne neue Displayzeile.
- Beobachter-Hauptpanel ohne separaten Audio-/Statistik-Setup-Button; Konfiguration in Einstellungen → Audioaufnahme.
- `ENTWICKLERMODUS` bei aktivem Modus auf Titelhöhe oben rechts, nie als eigene vertikale Zeile; bei AUS unsichtbar.
- `Highspeed-Impulsdiagnose` nur im Entwicklermodus und als letzter Eintrag der Zusatzfunktionen.
- Android-System-Zurück geht auf das zuvor besuchte Panel zurück. Laufender Analyzer darf durch reinen Panel-Zurückwechsel nicht automatisch gestoppt werden.

---

# Zusatzdirektive v0.5.19 – Save-UI, Gain-Bezug und ADB-Warten

- Analyzer `SAVE-Setup` fuer lokale Audio-/Statistik-Saves gehoert in Analyzer-Einstellungen → Statistik, nicht in die Hauptanzeige.
- `SAVE AUDIO` gehoert unten in die Actionleiste als 48-dp-Actionbutton im Format des Kamerabuttons; der feste Audio-Buffer-/Quotentext steht direkt daneben.
- Kurzzeitmeldungen wie `Lokal gespeichert` erscheinen als Overlay/Popup nahe der ausloesenden Aktion und verschwinden nach 3 s. Grundsaetzlich keine spontanen Zusatz-/Status-/Fehlertexte in bestehende Display-, Messwert- oder Beschriftungszeilen einschieben oder dadurch Layout verschieben.
- Digitalen Gain nur kompensieren, wenn er im analysierten PCM tatsaechlich enthalten ist; genau einmal. PRE-GAIN-Audio nicht erneut korrigieren.
- Gleiche WAV + gleicher Statistikbereich + gleicher Gain-Bezug muessen fuer L50/L90 in Anzeige und Statistik-JSON denselben Rechenpfad verwenden.
- `VORTEST_INSTALL.bat` soll nach komplettem Build-PASS auf genau ein autorisiertes ADB-Geraet warten und ohne Standard-Timeout alle 2 s neu pruefen; bei mehreren/unauthorized/offline nicht automatisch installieren.

---

# Zusatzdirektive v0.5.18.4 – Vortest-Bildschirmausgabe

- Saemtliche Meldungen des Windows-Vortests muessen live auf dem Bildschirm erscheinen und gleichzeitig vollstaendig ins Log geschrieben werden.
- Das gilt fuer STDOUT und STDERR aller nativen Schritte sowie fuer APK-/ZIP-/SHA-/ADB-Pruefergebnisse und Fehlermeldungen.
- Keine reine Log-Ausgabe und keine absichtliche Pufferung bis zum Ende eines lang laufenden Schritts.
- PASS/FAIL richtet sich bei nativen Prozessen ausschliesslich nach dem realen Exitcode, nicht danach, ob ein Programm STDERR verwendet.
- Am Ende immer eine sichtbare Zusammenfassung der einzelnen Schritte.

---

# Ergänzung ab v0.5.18.1 – Windows-Vortest

- Für Windows-Zwischenprüfungen den vollständigen Standardablauf bevorzugt über `VORTEST.bat` starten.
- `VORTEST.bat` muss PREPARE → `:app:testDebugUnitTest` → `assembleDebug` in dieser Reihenfolge ausführen und beim ersten Fehler abbrechen.
- Ein PASS darf nur ausgegeben werden, wenn die erwartete Debug-APK tatsächlich existiert; Log und Kurzstatus müssen erhalten bleiben.
- `PREPARE_PROJECT.bat --no-pause` dient nur der Automatisierung. Der bisherige interaktive PREPARE-Aufruf bleibt unterstützt.
- Ein erfolgreicher Vortest ist Android-Buildnachweis, aber kein Xiaomi-Gerätefunktionsnachweis; Geräteteststatus weiterhin getrennt dokumentieren.

---
# v0.5.18 – verbindlicher lokaler SAVE-Umfang (25.09.2026)

Eigener Analyzer-Hauptbutton, Reihenfolge/Icons exakt `activity`, `blocks`, `settings`, `waves`; Portraitvorgabe bleibt. Beobachter/Analyzer gemeinsame lokale Statistikfunktion und Quote. WAV 1kHz/16bit/Mono Default; Dauer konfigurierbar 30–600s (vorläufig), 120s Default; Statistikbereich 2–25Hz Default unabhängig vom Filterbank-Setup (2–40 Bänder). Für Analyzer RUN/PAUSE ein zusammenhängender Vor-Gain-Ringpuffer; Clip/Snapshot nicht mit altem Rolling-Audio vermischen. Gain in betroffenen Pegeln einmalig abziehen, bei Clipping nicht als gültig ausgeben. Beobachter sendet weiterhin allein altes Supabase-Schema; neue Statistiken zunächst lokal, keine neue Backend-Tabellen-/Key-/Uploadänderung. Teststatus der v0.5.18 **nicht** aus v0.5.17 ableiten. `docs/project/LOCAL_AUDIO_STATS_v0.5.18.md`.

---
# LFN-Scout v0.5.17 – verbindliche Hochformat-Sperre (24.09.2026)

**Gesamte App einschließlich Erinnerungsansicht stets im Portraitmodus.** `MainActivity` und `ReminderAlarmActivity` sind im `AndroidManifest.xml` mit `android:screenOrientation="portrait"` festgelegt. Drehen des Geräts in die Horizontale ist **kein** STOP-, PAUSE-, Panelwechsel- oder Audio-Restore-Ereignis. Bei aktivem Analyzer müssen Bildschirm, gewähltes Panel, FFT/Wasserfall, Aufnahme und Messzustand erhalten bleiben. Keine Änderung an `AudioRecord`, Rolling- oder Direkt-Analyzer-Lifecycle.

**Status:** Quelländerung und statischer Manifest-Regressionstest; Android-Gradle-Build und Xiaomi-Rotationstest für v0.5.17 noch NICHT ausgeführt. Geräteregression `docs/project/TESTPLAN_v0.5.17.md`; `BUILD_VERIFICATION_v0.5.17.md`. Die dokumentierte Exportprüfung von v0.5.16 gilt nur für den konkret gelieferten Konfliktfall; weitere Abnahme nicht unterstellen.

---
# LFN-Scout v0.5.16 – Migrations-/JSON-Exportkorrektur (24.09.2026)

**Quellstand:** versionName `0.5.16`, versionCode `159`, Android-Paket unverändert. Basis v0.5.15. **Statischer Testkandidat**, kein ausgeführter Android-/Compose-Build oder Xiaomi-Test.

**Diagnose:** Die vom Nutzer gelieferte alte `Setup_4_48k.json` hat u. a. `range=22000`, während das interne Setup 4 `range=200` besitzt. Der v0.5.15-Migrator erkennt den Konflikt und gibt `false` zurück; der Export wirft daraufhin eine `IllegalStateException`. Das ist eine aus Daten und Code belegte Konfliktkonstellation, kein geräteseitig nachgewiesener vollständiger Stacktrace.

**Korrektur:** Bei ausstehenden Migrationen wird der intakte Altname nie vom normalen Export überschrieben. Das aktuell intern gespeicherte Setup erhält einen unabhängigen `Setup_N_CURRENT_v2.json`-Namen. Der Export anderer Slots läuft weiter. Der Diagnose-ZIP enthält nun `Settings/setup_migration.json` (ausstehende Slots und konkrete Fehlermeldungen), und die GUI zeigt Fehlermeldungen einschließlich `exception.message`. Nach vollständig validiertem Import inkl. Preference-Readback wird die Altdatei wie zuvor gelöscht; danach kann der reguläre v2-Dateiname verwendet werden. Konflikte bleiben erhalten und erfordern eine spätere, ausdrücklich geprüfte Konfliktlösung. Keine stillschweigende Werteübernahme, kein automatisches Löschen bei Konflikt.

**Offen:** Echter vollständiger JSON-Import, interne Schema-v2-Migration, einheitlicher LIVE-/STOP-Parameterkatalog und Konflikt-UI. Ebenso keine Änderung an FFT, Audio/ROLLING, Analysepegel oder der Konfiguration des Nutzers. Eine v0.5.16-Installation passt Setup 4 **nicht automatisch** auf 8 kHz an.

**Windows-Prüfung:** `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`, danach Xiaomi-Regression nach `docs/project/TESTPLAN_v0.5.16.md`.

---

> **Ab v0.5.15:** Alte v1-JSON-Setups beim Start vor Export einmalig, konfliktbewusst importieren, commit + readback prüfen und ausschließlich danach löschen. Bei Konflikt/Fehler Altdatei behalten und Export sperren. Bereits existierende v2-Dateien nicht löschen. Dokument: `docs/project/LEGACY_SETUP_MIGRATION_v0.5.15.md`.

> **Ab v0.5.14:** Beide Analyzer zeigen dieselben neun Gruppen, nicht unterstützte Parameter bleiben ausgegraut. JSON v2 exportiert in beiden Slots dieselben Parameter, vorhandene Werte, fehlende Altwerte als `null` samt `provenance`, ohne Defaults zu erfinden. Der JSON-Export ist weiterhin read-only und kein Import. Interner Audio-/DSP-Pfad bleibt unverändert. Siehe `docs/project/UNIFIED_ANALYZER_SETTINGS_v0.5.14.md`.

> **Verbindlicher UI-Stand ab v0.5.13:** Die bisher fest unter dem Wasserfall platzierten Zeilen `Wasserfall …`, `FILTER …` und `FILTER-Autorange …` dürfen nicht permanent angezeigt werden. Nur bei Darstellungswechsel fünf Sekunden lang als zweizeiliges 10-/9-sp-Overlay unten rechts im Wasserfall; laufende FFT/Zeilen-Refreshs starten das Popup NICHT neu. Wasserfall-Bitmap, Farbbalken, Zeitachse, Frequenzflucht und Bildexport dürfen durch das Overlay nicht verändert werden. ORIGINAL-/FILTER-/Kamera-Buttons einschließlich verbindlicher Icons bleiben unverändert. `docs/project/ANALYZER_WATERFALL_NOTICE_v0.5.13.md`.

> **Verbindlicher UI-Asset-Stand ab v0.5.12:** ORIGINAL muss das unveränderte PNG `docs/project/FILTERBANK_UI_REFERENCE/ORIGINAL_ICON_NUTZER_2026-09-24.png` verwenden; produktives Android-Asset `app/src/main/res/drawable-nodpi/original_waterfall_icon.png`. Das ältere Beispiel `ORIGINAL_FILTER_BINDEND.png` gilt für ORIGINAL **nicht mehr**. FILTER bleibt FÜNF symmetrische Magenta/Rosa/Weiß-Balken, unabhängig von 2–40 Filterbändern. Beide 48 dp und mit unveränderten Kurz-Tipp-Schaltern; FILTER-Langdruck löst die unabhängige Filter-dBFS-Autorange aus. Keine nicht getestete Funktion als Geräte-PASS melden. Details: `docs/project/FILTER_AUTORANGE_AND_ICONS_v0.5.12.md`.

> **Historisch, für ORIGINAL durch v0.5.12 ersetzt:** zwei 48-dp-Buttons ORIGINAL (damaliges Inferno-Mini-Spektrogramm), FILTER (fünf symmetrische Magenta/Rosa/Weiß-Balken), siehe `docs/project/FILTERBANK_UI_REFERENCE/ORIGINAL_FILTER_BINDEND.png`. Filterbandzahl 2–40 (Standard 10), eigene MIN/MAX, kein dritter Actionbutton. FAST-Filterbank-Stand v0.5.11 bleibt experimentell; vollständige Impulserkennung nicht als implementiert behaupten.

> **Buildkorrektur v0.5.10:** Nutzer-Windows-Gradle v0.5.09 = FAIL wegen Kotlin-Typfehler in `setTiming`: gemeinsame `in if (...)`-Expression über `Set<Int>` und `IntArray` ist nicht typprüfbar. Separate `in`-Zweige verwenden. Dieser Fehler darf erst nach echtem Android-/Compose-Build als behoben bestätigt werden. Bestehende 8-kHz-/Rolling-Zustände und v0.5.09-Direktfunktionen unverändert; 16:1-Pegelabweichung offen. Siehe `BUILD_VERIFICATION_v0.5.10.md`.

> **Verbindlicher Zusatz v0.5.09:** Direkt-Analyzer (historisch DIRECT_48K) unterstützt wählbare Quelle und Eingangsrate 8–96 kHz, FFT 64–16384, neun Fenster, dezimalen Glättungsfaktor α, Peakmarker, optionalen DC-Abzug und digitale Testtöne. Decimation 0 = AUS, 1 = 1:1, 2–12 = direkter Faktor; nicht ganzzahlig teilbare Konfigurationen abweisen statt falsch umrechnen. Audioquelle/Rate/Faktor nur bei STOP ändern. STOP und anschließendes Verlassen für Rolling-Restore beibehalten; alte 8-kHz-/WAV-Pfade nicht ändern. Kein Android-/Geräte-PASS ohne echte Builds/Tests. Details `docs/project/DIRECT_ANALYZER_v0.5.09.md`.

> **Ergänzung v0.5.08:** Wasserfallzeitachse benutzt reale Zeilenzeitstempel (`dd.MM` / `HH:mm`), höchstens fünf sichtbare Ticks und nur 1/2/5/10/20/30/60-minütige Intervalle. Spektrum- und Wasserfall-x-Grenzen müssen unverändert exakt fluchten; schmale dBFS-Farbskala bleibt bestehen. Das Kamera-Pop-up ist am Symbol zu verankern. Android-Build und Gerätetest erst nach realem Nachweis PASS erklären. 16:1-Pegelabweichung offen.

> **Buildkorrektur v0.5.07:** Der Nutzer-Windows-Build v0.5.06 ist FAIL. Jeder im Controller aufgerufene Subscriber-API-Endpunkt muss im realen Session-Typ deklariert sein; bei Compose-UI-Änderungen sind die Scroll-/Viewport-Grenzen und Top-Level-Helper syntaktisch zu prüfen. Statische Textprüfungen und Host-Core-Tests sind kein Ersatz für `:app:compileDebugKotlin`/Android-Build. Vor Xiaomi-Abnahme muss der Windows-Gradle-Build für v0.5.07 PASS sein.

> **Verbindlicher Zusatz v0.5.06:** Spektrumkurven Aktuell, Average und Maxhold unabhängig schalten und in Setup-Slots 1–4 sowie separaten Betriebsprofilen persistieren. MAX/CLR ausschließlich bei sichtbarem Maxhold; gesamte Bedienfläche höchstens Kamera (48 dp). MAX CLR setzt nicht Wasserfall, FFT, Audio, L50/L90 oder Mittelwert zurück. Manuellen Kamera-Langdruck erst nach tatsächlichem Dateiabschluss über ein sichtbares, nicht mitscrollendes Pop-up bestätigen; automatischer Bildexport darf kein manuelles Pop-up auslösen. STOP/Panel-Restore unverändert. `docs/project/TESTPLAN_v0.5.06.md` vor Freigabe abarbeiten.

> **Verbindlicher Zusatz v0.5.05:** Crash-Fix nach 48-kHz-STOP + Panel verlassen: 8-kHz-LF-Controllerparameter vor UI-Rekomposition synchron wiederherstellen. `RESTORE_DONE` nur bei bestätigtem Rolling; STOP allein ändert den gewählten Modus nicht. PAUSE getrennt von STOP. Interne Analyzer-SharedPreferences bleiben führend; öffentliche JSON-Dateien und Diagnose-ZIPs sind ausschließlich lesbare, nicht automatisch eingelesene Kopien. Keine privaten Beobachtungs-, Zugangsdaten- oder Rohdateien in Downloads exportieren. `docs/project/TESTPLAN_v0.5.05.md` vor Freigabe abarbeiten.

> **Zusatz v0.5.04.2 (Diagnose):** Der Nutzer bestätigt, dass die 8-kHz-Modi funktionieren. Sporadische Abstürze konzentrieren sich auf den 48-kHz-/Rolling-Übergang; kein unbelegter 8-kHz-Fehlerfix. STOP darf Betriebsart nicht zurücksetzen, Rolling-Restore ausschließlich nach STOP und Verlassen. Audiologging `AUDIO_DIAG_*` und `LFN-AUDIO`: keine PCM-Rohdaten, Ortsdaten oder Schlüssel; 15-s-Heartbeats begrenzen die Lograte. Vor Änderungen am Audio-Lifecycle zuerst Logs aus `docs/project/AUDIO_DIAGNOSE_v0.5.04.2.md` auswerten. Fehlende PAUSE-Taste und 16:1-Energieabweichung bleiben offen.

> **Buildkorrektur v0.5.04.1:** Der erste Nutzer-Windows-Gradle-Lauf v0.5.04 ist FAIL; behobene Smart-Cast-Stelle und an LF-/48-kHz-Zeitbasen angepasste Tests sind vor jeder neuen Funktionsentwicklung mit echten Gradle-Tests zu verifizieren. v0.5.04.1 darf erst nach neuem Android-Build und Xiaomi-Test freigegeben werden; 16:1-Pegelabweichung bleibt offen. Aktuell geplante Spektrumkurven Aktuell/Average/Maxhold + maximal kameragroßer MAX/CLR-Knopf sind noch NICHT implementiert.

> **Verbindlicher Zusatz v0.5.04 (gegenüber historischen Direktiven):** Die neue 48-kHz-Betriebsart darf die vorhandene 8-kHz-Aufnahme nicht parallel offen halten. Vorherigen `rollingBufferEnabled`-Zustand sichern; STOP alleine darf Rolling **nicht** wiederherstellen. Erst gestoppter Analyzer **und** Panelverlassen führen Restore aus; bei vorigem Rolling EIN neue RAM-Pufferung beginnen. Eine laufende Analyzer-Messung läuft bei Panelwechsel weiter. Ältere Resetforderung bei Akt/MW/MAX ist supersediert: jetzt Historie erhalten und Wechsel markieren. Rolling schreibt keine automatische durchgehende WAV-Datei auf Disk, dies nicht als implementiert darstellen. Tests gemäß `docs/project/TESTPLAN_v0.5.04.md`. Die 16:1-Pegelabweichung bleibt offen.

> **Zusatz v0.5.03:** Decimation ausschließlich am Analyzer-Subscriber (4/8/16:1), gemeinsamer 8-kHz-Mikrofon-/WAV-Pfad unverändert. FFT-Zeitbasis über wirkliche LF-Rate, nicht fest 1 Sample = 1 ms; unabhängige Statistik-Zeit/Frequenz und Mittelungsdefinition beachten. Alte lokale Setups müssen ohne Name/Beschreibung und neue Parameter ladbar bleiben. Android-Build nur nach tatsächlicher Gradle-Ausführung als PASS markieren. Siehe `docs/project/ANALYZER_v0.5.03.md`.

> **Zusatz v0.5.02 (verbindlich):** Neue Analyzer-Einstellungen werden in `lfn_scout_live_analyzer` sowie allen drei lokalen Setup-Slots persistiert. Feste Mikrofonquelle 8 kHz / FIR-Decimation 8:1 und WAV-Recorder nicht verändern; freie Wasserfallzeit ist vom konstanten 120-s-L50/L90-Statistikfenster zu unterscheiden. `Hz` muss geometrisch zwischen **zwei direkt benachbarten beschrifteten Ticks** stehen, Bildschirm und Export identisch. Ein Android-Build gilt ausschließlich nach wirklicher Gradle-Ausführung als PASS. Siehe `docs/project/ANALYZER_v0.5.02.md`.

# LFN-IN / LFN-Scout – Entwicklungsdirektiven

1. Vollständige lauffähige Dateien liefern, keine Diff-Fragmente.
2. Bestehende Funktionen bei Erweiterungen erhalten.
3. Persistente Einstellungen verwenden, wo sinnvoll.
4. Bereits behobene Fehler als Regressionstest berücksichtigen.
5. Selbsttest/PASS-FAIL vorsehen, sobald mehrere Subsysteme vorhanden sind.
6. Supabase-Service-/Secret-Keys niemals in die Android-App einbauen.
7. Teilnehmer authentisieren sich ausschließlich mit ihrem LFN-Scout-Schlüssel.
8. `rating = -1` bedeutet Nichtantwort; `0` bedeutet echte Beobachtung "nichts wahrgenommen".
9. Standort bei Beobachtung: volle verfügbare Genauigkeit; ohne Berechtigung `0 / 0`.
10. WAV-Dateiname enthält `R0` bis `R5`.
11. `rating = -1` erzeugt keine WAV-Datei.
12. Jede neue LFN-Scout-Version als vollständigen **aktiven** Quellstand (`LFN-Scout_ACTIVE_vX.Y.Z.zip`) liefern; keine Patch-/Diff-Ausgabe als Hauptübergabe. Große selten veränderte Referenz-/Recovery-Artefakte werden ab v0.5.18.2 separat als versioniertes `LFN-Scout_BASE_ASSETS_Rn.zip` geführt.
13. Projekt-ZIPs enthalten niemals `.git/`; `.git/` wird ausschließlich von Git/GitSync im lokalen Repository verwaltet.
14. Android-only bleibt ein unterstützter mobiler Buildweg: Work → ACTIVE-Projekt-ZIP → GitSync → GitHub Actions → APK → Installation. Das BASE-Paket ist für den normalen Build nicht erforderlich.
15. Die verbindliche Bedien- und Buildbeschreibung steht in `BUILD_WORKFLOWS.md`; Änderungen am Buildprozess müssen dort mitgepflegt werden.

16. Jede neue Projektlieferung inklusive Entwicklungs-/Korrekturstand erhaelt `versionName`, fortlaufenden `versionCode`, konsistenten Projektordner/ZIP-Namen und aktualisierte Dokumentation.
17. Bei neuem Windows-Projektordner `PREPARE_PROJECT.bat` zuerst ausfuehren. JDK 17 fuer Gradle-Daemon **und** `:analyzer-core`-Toolchain projektlokal konfigurieren, ohne globale Java-/IDE-Einstellungen zu aendern. Private JDK-/SDK-Pfade nicht ins Liefer-ZIP aufnehmen.

## Ergänzung v0.5.18.2 – ACTIVE/BASE und Vortest-Ausgabe

- Ein offizielles ACTIVE-ZIP wird automatisiert erst nach erfolgreichem Windows-Vortest erzeugt; es muss ZIP-verifiziert und mit SHA-256 versehen werden.
- `BASE_PACKAGE_REQUIRED.txt` im ACTIVE-Stand nennt die benötigte BASE-Revision und SHA-256. BASE wird nur neu erzeugt, wenn einer der ausgelagerten Pfade geändert wurde.
- Das ACTIVE-Paket muss ohne BASE Android-buildfähig sein. BASE enthält nur nicht buildkritische große Referenz-/Rekonstruktions-/Recovery-Artefakte.
- Maschinenlokale Dateien/Pfade und Buildprodukte dürfen nicht in das ACTIVE-Paket gelangen.
- Optionale ADB-Installation erst nach vollständigem PASS und nur bei genau einem autorisierten Zielgerät; kein automatisches Deinstallieren.

## Namenskonvention ab v0.4.74

- Sichtbarer App-/Produktname: **LFN-Scout**.
- Bestehende Projekt-, Verzeichnis-, ZIP- und Build-Pfadnamen mit **LFN-Scout** werden nicht umbenannt.
## README-Synchronisation
- Bei jeder neuen Version werden die Änderungen aus dem `CHANGELOG.md` zusätzlich in der `README.md` unter `Neu in vX.Y.Z` mitgeführt.
- Versionskopf der `README.md` muss immer dem aktuellen `versionName` entsprechen.
- **Kumulativer README-Stil:** Jede Version erhält direkt oben einen eigenen Abschnitt `# LFN-Scout vX.Y.Z` und `## Neu in vX.Y.Z` mit konkreten Änderungen, Querverweisen und getrenntem Build-/Geräte-Prüfstatus. Frühere Versionsabschnitte vollständig erhalten und eindeutig als historisch kennzeichnen; keinen neuen Release nur als Einzeiler vor veraltete `AKTUELL`-Meldungen setzen. Reine README-Korrekturen erhöhen die App-Version nicht.



## Rekonstruktionsdokumentation

- `docs/reconstruction/PROJECT_STATE_CURRENT.md` muss bei funktionalen/architektonischen Änderungen mitgeführt werden.
- Betroffene SRS/SDS-Subsystemdokumente und maschinenlesbare Manifeste sind bei jeder Version zu aktualisieren.
- `file_manifest.sha256` wird erst nach allen Änderungen neu erzeugt.
- Historische Quellen dürfen aktuellen Code nicht überschreiben; bei Konflikten gilt die Quellenpriorität aus `00_RECONSTRUCTION_INDEX.md`.


## Analyzer v0.7 – Entwicklungsvertrag

Vor Änderungen an Recorder, Rolling, UI, PNG oder Histogrammen `docs/project/analyzer_v0.7/README.md`, `docs/project/analyzer_v0.7/docs/project/18_ABSCHLUSS_AUDIT_v0.7.md` und das mit realen Klassen ausgefüllte `docs/project/analyzer_v0.7/docs/project/16_Repo_Mapping_und_Gates_v0.7.md` lesen. Keine doppelten Mikrofon-Owner; `Save aktiv` bedeutet ausschließlich PNG; alle bestehenden Funktionen/Tests erhalten. Teststatus muss belegbar sein.

## Ergänzung v0.4.94 – Analyzer-Zeitbasis und Visualisierung

- 8-kHz-Capture/8:1-FIR-Decimation, 1-kHz-LF-Abtastrate, FFT-Größe und FFT-Hop nicht verwechseln. FFT-Hop bestimmt nur das Aktualisierungsintervall; bestehender Recorder-/WAV-Zweig bleibt unabhängig.
- FFT-Hop und Wasserfall-Hop getrennt, persistent und mit echten 120-s-Historien-/Statistikfenstern behandeln. Konfiguration auf neue Zeitbasis umrechnen und Übergang dokumentieren.
- Spektrum: linke dBFS-Achse mit Haupt-Ticks/Zahlen/Gitter und lineare Frequenzachse mit Ticks. Wasserfall: neueste Zeitzeile oben, alte wandern abwärts; Frequenz horizontal mit Ticks, dBFS-Farbskala mit Tick/Zahl. `Hz` ausschließlich separat als Achseneinheit, nie an den letzten Frequenz-Tick hängen. Dieselben Konventionen im Offscreen-PNG.
- Die UI-Sollrate ist keine garantierte Geräteleistung; Android-Build und Xiaomi-Gerätetest müssen gesondert nachgewiesen werden. Vor weiteren Änderungen Ausgangsstand v0.4.93 (Xiaomi 7/7 Benutzerprüfung) und `docs/project/ANALYZER_LIVE_v0.4.94.md` lesen.

## Ergänzung v0.4.95 – Anzeige / Zeitbasis

- Anzeigepreset −120…−60 dBFS (Default; oberer Wert −60), weitere optionale Bereiche −120…−40 und −120…0. Dieselbe Skala und Ticks für GUI/PNG und Farbleiste. Kein kalibrierter dB-SPL-Pegel.
- Im Normalbild `LFN-Scout · 0–X Hz`, eine Statuszeile direkt darunter; L50(2–25) und L90(2–25) oben rechts IM Spektrum; separate Kurvenlegende und redundante Überschriften entfallen. Rauschboden default AUS.
- FFT-Hop 10 als optionale Hochgeschwindigkeitskonfiguration; Wasserfall mindestens mit echter Spektrum-Kadenz, unabhängig von der FFT-Zählung auf 120 s. Erreichte Android-FPS/CPU-Last gesondert auf Gerät testen.

## Ergänzung v0.4.95.1 – Diagnose-Callback als Build-Vertrag

- Für jeden `AnalyzerSessionGateway.startRawLf(::accept, ::acceptDiagnostics, ...)`-Aufruf muss die Controller-Methode `acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)` tatsächlich deklariert sein und die Diagnose persistent im Controller aktualisieren. Verifier prüfen Referenz **und Deklaration**, nicht nur den Aufruftext.
- Nach einem UI-Umbau muss `:app:compileDebugKotlin` bzw. `:app:testDebugUnitTest` erfolgreich auf Android/Windows kompiliert werden, bevor eine Android-Build-Freigabe behauptet wird.

## Ergänzung v0.4.96 – Live-Analyzer Autorange / Darstellung

- dBFS-Achse als Autorange-Touchziel, Start nur bei gültigem Live-Signal. Standard 2 s / feste ±5 dB, persistente frei bestimmte Unter- UND Obergrenze; leere Signale ändern keine gespeicherten Grenzen.
- Tipp auf rot umrandete L50/L90 setzt sowohl 120-s-Statistik als auch FFT-Average zurück, ohne den Wasserfall oder PCM/WAV zu beeinflussen.
- Beide Frequenzachsen nutzen identische linke/rechte Plotränder. Wasserfall-dBFS-Farbskala links, Zahlen innerhalb; Zeitzahlen bleiben links am Plot. Cursor folgt Finger, Readout an oberer Spitze und Tipp zum Ausschalten.
- Kein Android-Build als PASS deklarieren, sofern nicht tatsächlich mit Android-SDK kompiliert.

## Ergänzung v0.4.97 – robustes Autorange und Textflächen

- Autorange darf einzelne tiefe schmale Spektrumeinbrüche oder kurzzeitige Pegelausreißer nicht als absolute Display-Min/Max übernehmen. Robustes Frequenz-/Zeit-Perzentilverfahren separat testen; feste ±5 dB (einstellbar) statt Prozentmargin bleibt. Manuelles Preset und beide persistenten Grenzwerte erhalten.
- Cursor-Hz/dBFS als ungerahmter Text; L50/L90 in intrinsisch schmaler, ungefüllter, roter abgerundeter Resetbox mit 3 dp Padding. Keine unnötig großen Touch-Zonen.
- Farbskala links um ~1/3 schmaler; Werte darin lesbar; die gewonnene Breite für beide gemeinsam ausgerichteten Frequenzplots und das PNG verwenden. Keine Audio-, FFT- oder WAV-Regressionsänderung im Layoutrelease.

## Ergänzung v0.4.98 – Wasserfall-Auswertung

- Wasserfallmodi `Aktuell` (Default), `Mittelwert` (lineare Leistungsaggregation je Frequenzbin) und `Maximum` (binweise Maxima) ausschließlich über die aktuell angesammelten FFT-Spektren pro Zeilenintervall. Keine dB-arithmetische Mittelung; keine ungewollte Änderung der separaten roten Average-Kurve.
- Moduswechsel: komplette Wasserfallhistorie + halbfertige Zeile zurücksetzen, neuen 120-s-PNG-Zyklus starten; Statistik/FFT/Recorder/WAV unverändert. Auswahl persistent und in allen drei Setup-Slots.
- Spektrum-Max-Hold nicht in das Hauptbild einführen, bis vom Nutzer separat freigegeben. Neuordnung der Analyzer-Settings nach `docs/project/ANALYZER_SETTINGS_PANEL_VORSCHLAG_v0.4.98.md` ist **Vorschlag**, kein implementierter Bereich.

## Ergänzung v0.4.99 – Einstellpanel und manuelle dBFS-Grenzen

- Einspaltige Analyzer-Navigation mit acht Kategorien; bisherige aktive Controls nicht ausblenden oder in funktionslose Platzhalter verwandeln. Nicht implementierte Funktionen als Information, nicht als aktive Auswahl zeigen.
- `Anzeige`: zwei separat editierbare ganzzahlige Unter-/Obergrenzen, validiert gemäß `LiveAnalyzerController.setDisplayRange`, inklusive persistentem Speichern und Setup-Recall. Autorange und drei Presets bleiben gleichzeitig verfügbar.
- Das lokale Setup über der Navigation darf nur bestehende Plätze 1–3 überschreiben, wenn `Speichern` bewusst ausgelöst wird; `Neu` sucht einen freien Platz ohne implizites Überschreiben. Letzte Setup-Auswahl separat persistieren. PNG-Pfad ohne irreführende Annahmen über Android-Versionen dokumentieren.

## Ergänzung v0.5.00 – Analyzer-Viewport und Bedienelemente

- Analyzer-Hauptbild muss bei kurzer Inhaltsliste den verfügbaren Bildschirm dunkel ausfüllen; verbleibende Höhe vorrangig Spektrum und Wasserfall zuweisen, bei kleinen Displays scrollen statt Diagramm/Ticks abzuschneiden.
- Die nicht realisierten Karten `Mithören`/`Superfast` und die redundante Karte `Anzeige` gehören **nicht** in das Live-Hauptbild. Keine zweite Settings-/Diagnose-Aktionsleiste; Zugriff über Hauptmenü → Settings-Kategorien. PNG-Manual/Auto muss bedienbar bleiben.
- Entfernte Mittel-/Rausch-Schalter müssen über `Einstellungen → Spektrum` weiter erreichbar sein. Audio, DSP, WAV, Rating, Supabase, PNG-Automatik unverändert; Layout erst nach Build-/Xiaomi-Test freigeben.

## Übergabe-Verbindlichkeit (Dokumentationsrevision 20.09.2026)

Bei Chat-/Projektwechsel `PROJECT_STATUS.md` und `docs/project/UEBERGABE_v0.5.00_2026-09-20.md` vor jeder Änderung lesen. Ein HANDOFF **ohne Änderung der App** behält v0.5.00/141, um keine ungetestete Schein-Softwareversion zu erzeugen; bei der nächsten tatsächlichen Programmänderung fortlaufend versionieren. Für Testergebnisse zwischen Kotlin-Host, statischem Verifier, Android-Gradle und Xiaomi strikt trennen. Bekannte Layoutdefekte erst nach neuem Gerätescreenshot als behoben markieren.

## Ergänzung v0.5.01 – neues Bedien-/Exportverhalten (übersteuert nur die Hz-Zeilenposition aus v0.4.94)

- Hz bleibt eigenständige Einheit, wird **nicht** an die letzte Tickzahl angehängt, steht nun jedoch **auf derselben Zeile zwischen zwei Frequenzzahlen**. Live-UI und Offscreen-Export folgen demselben Layout.
- Ein Kamerasymbol: kurzer Tap = Auto-Bild EIN/AUS; langer Druck = ein manuelles Bild unabhängig von Auto; ein langer Druck darf nicht zusätzlich umschalten. Ein rotes X kennzeichnet AUS; kein separates Auto-EIN/AUS-Wort.
- Bildformat PNG/JPG und JPG-Qualität pro Setup persistieren; bestehende Download-Pfade beibehalten. Neue Angaben im Code sind erst nach Android-/Gerätetest als getestet zu melden.
- Kopf-Rückpfeil und Android-Back navigieren jeweils eine Kategorieebene zurück. Beim Autorange keine laufende dBFS-Feldbearbeitung überschreiben.
## Ergänzung v0.5.18.3 – Native Prozesse im Windows-Vortest

- Normale STDERR-Ausgabe nativer Programme ist kein Fehlerkriterium. PREPARE, Gradle, Paketierung und ADB werden im automatisierten Vortest anhand des echten Prozess-Exitcodes bewertet.
- Ein offizielles ACTIVE-ZIP wird weiterhin nur nach erfolgreichem Vortest erzeugt. BASE R1 bleibt unverändert, solange dessen Inhalte nicht geändert werden.
- `VORTEST_INSTALL.bat` installiert nur nach vollständigem PASS und nur auf genau ein autorisiertes ADB-Gerät.


## Build-Verifikation ab v0.5.24

- Statische Quell-, PSI-, Host- oder Paketprüfungen ersetzen keinen echten Android-/Compose-Build.
- Eine Version darf nur dann als build-verifiziert/fertig bezeichnet werden, wenn mindestens `:app:testDebugUnitTest` und `assembleDebug` erfolgreich durchgelaufen sind.
- Ist der reale Gradle-Build in der Erstellungsumgebung nicht möglich, wird das Paket ausdrücklich als **Source Candidate – Build noch nicht verifiziert** gekennzeichnet.
- Compilerfehler aus einem realen Vortest haben Vorrang vor statischen PASS-Prüfungen und werden vor Funktionsausweitungen behoben.


## Dauerhafte Testdirektive ab v0.5.75
Ein jederzeit über die App erreichbarer automatisierter System- und Regressionstest ist dauerhafter Projektbestandteil. Neue Features und Fehlerbehebungen erweitern seine Testabdeckung; Tests dürfen nicht stillschweigend entfernt werden. PASS, FAIL und SKIP unterscheiden; keine Schein-PASS für ungeprüfte Hardware-, Audio- oder Steuerpfade. Diagnose- und Exportpaket ohne Nutzdatenänderung. Die v0.5.75-Erststufe ist eine passive Log-Auswertung; vollautomatische echte Bedienabläufe sind noch offen. Details: `docs/project/PERSISTENT_SYSTEMTEST_v0.5.75.md`.
