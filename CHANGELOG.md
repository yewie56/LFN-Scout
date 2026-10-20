## v0.5.93 (2026-10-20)

- Produktionsquittungen für veröffentlichte Rolling-WAV und abgeschlossene 1800-Sekunden-Halbstunden-CSV, angebunden an die bestehenden Systemtest-IDs 3 und 5. Keine PASS-Promotion aus isolierten Fixtures.
- Versionscode 251.

## v0.5.92 (2026-10-20) – Integrierte Auditpfade (Android-Build ausstehend)
- Produktive Rolling-WAV-Aufnahme: blockweise Rücklese-/SHA256-Bestätigung; defekte Dateien werden nicht als Erfolg gemeldet.
- Unabhängige FFT-Sekunden-Bitmap für Overall-Halbstundenabschluss, Abweichungen explizit geloggt.
- 51 Python-Tests + isolierte Kotlin-JVM-Smoke-Prüfung PASS; Android-Gesamtbuild wegen fehlender Gradle-Wrapper-JAR nicht ausführbar.


## v0.5.91 – Fortsetzung 20.10.2026 (kein neuer Release)

- Filterbank-FFT-Sekundenbuchführung: negative/ungültige Werte, doppelte, verspätete und frühere Halbstunden werden ignoriert; Bitmap und Zeitmarke werden beim Bucketabschluss zurückgesetzt.
- Host-JVM-Runtime-Prüfung sowie 3 neue Python-Vertragstests hinzugefügt.
- Geräteseitige Tests 2/3/5 bleiben unverifiziert; kein Android-APK-Build.

## Arbeitsstand 20.10.2026 – Priorität 2/3/5 (ohne Versionssprung)
- Rolling-Future-Captures gegen Generationswechsel und STOP/Neustart gesichert; zu lange Anforderungen abgewiesen.
- Negative FFT-Zeitstempel werden vor Bucket-Anlage ignoriert.
- Regression: 40/40 PASS, Rolling-Kotlin isoliert kompiliert; Android-APK nicht verifiziert.

## 2026-10-20 – konsolidierte Fortsetzung 20× weiter (v0.5.91 unverändert)

- Produktive WAV-Archivveröffentlichung: Byteanzahl-/SHA-256-Rückleseprüfung vor und nach MediaStore-Freigabe (auch Legacy-Dateiweg).
- Produktiver FFT-Sekunden-CSV-Export: unabhängiges Parsen der tatsächlichen Zeitstempel, Erkennung von Duplicate/Missing/Out-of-Bucket und Persistenz-Readback.
- 42 Python-Regressionstests PASS, isolierte Kotlin-JVM-Tests 7/7 PASS; Gradle/App-Build und Android-Gerätetest weiterhin blockiert bzw. ausstehend.
- Drei priorisierte Freigabefälle 2/3/5 bleiben mangels vollständiger Gerätebelege SKIP. Details: `PRUEFBERICHT_20X_PRIORITAET_2_3_5_2026-10-20.md`.

## 2026-10-20 – Konsolidierte Datenintegritätsprüfung (unversionierter Arbeitsstand)

- Freigabekriterium auf **2 Sample-Kontinuität**, **3 produktive Rolling-Schreibkontinuität** und **5 Statistikabdeckung** beschränkt. Absturz- und Setup-Tests bleiben diagnostisch erhalten und sind nicht mehr verpflichtende Gate-IDs.
- Produktiver Halbstunden-CSV-Export liest den geschriebenen Inhalt durch MediaStore zurück, vergleicht die gesamte CSV mit den Sollbytes (UTF-8), prüft die Freigabe des PENDING-Flags und protokolliert Abweichungen als ERROR statt fälschlich OK.
- Keine automatische Aufwertung der drei technischen SKIP-Ergebnisse: der echte Archiv-WAV-Writer und die physikalische Vollständigkeit der Rohsamples sind noch nicht unabhängig nachgewiesen.
- Kein Android-Build verifiziert: Gradle 8.13 nicht lokal verfügbar. Noch kein APK-Testkandidat.

## v0.5.91 / versionCode 249 – MON-WAV-Dateirückleseprüfung (unfreigegebener Testkandidat)
- Vor MON-Archivierung jetzt unabhängiger CRC32-Rücklesevergleich der tatsächlich gespeicherten PCM-Nutzdaten mit den zuvor angenommenen und an den Writer übergebenen Blöcken.
- Vier reale JVM-Tests: intakte Datei, manipuliertes Byte, vertauschte Blöcke, abgeschnittene Datei.
- Bestehende fünf Android-SKIP-Fälle bleiben ungelöst; die Änderung betrifft MON und beweist weder Rolling-Langzeit-Schreibkontinuität noch physikalisch lückenfreie Aufnahme.

## v0.5.88 / versionCode 246 – Regressionstest-Vertragsabgleich (Android-Testkandidat)
- Veralteten MON-Setup-Rollback-Vertragstest auf repository.restoreSelection(previousActiveId, previousSelection.recent) umgestellt. Der Test fordert jetzt ausdrücklich MRU-Wiederherstellung.
- Enthält den bis zur vorherigen Arbeitsrunde vorliegenden MON-Setup-A/B/A-Prüfpfad und die PCM-Schreibprüfsummen-Erweiterung.
- Lokale Vertrags-Regression: 20/20 PASS. Android-Build und neuer Gerätetest hier nicht bestätigt.
- Letzter belegter Smartphone-Test v0.5.87: 50 PASS, 0 FAIL, 5 SKIP. Fünf SKIP-Fälle weiterhin nicht als gelöst nachgewiesen.

## v0.5.87 / versionCode 245 – Setup-Repository-Rollback und Testintegrität (unfreigegebener Android-Testkandidat)
- Integrierte Änderungen aus sechs Entwicklungsrunden: Validierungsanpassung des Statistik-Leerfenster-Tests, isolierte Repository-A/B/A-Tests mit Wiedereinlesen, Schreib-/Index-Rollback auch aus null-aktiver Auswahl, Schutz der produktiven Setup-Dateien mit SHA-256-Integritätsprüfung und Absicherung des MON-Setupwechsels.
- **Noch nicht implementiert/verifiziert:** vollständiger aktiver Controller-/MON-Setup-A/B/A-Systemtest sowie produktive PCM-/WAV-Lücken- und Statistik-End-to-End-Prüfung.
- Letzter Gerätestand v0.5.86: 43 PASS, 1 FAIL, 5 SKIP. Dieser neue Quellstand wurde nicht auf Android getestet.

## v0.5.83 – MON-Endzustand strenger abgesichert (Testkandidat, Android-Test offen)
- `live_mon_state_restored` setzt nun zusätzlich finalen Analyzer-RUN-Zustand und intakte Rolling-Servicegeneration sowie unveränderte Start-/Stop-Zähler voraus. Keine SKIP→PASS-Umetikettierung.
- Android-Laufzeittest und 10 SKIP offen.

## v0.5.79 – größerer Systemtest-Ausbau (QUELLKANDIDAT, kein freigegebenes Release)
- Bestehender autonomer STOP/RUN- und Rolling-Kontinuitätstest aus 0.5.77 bleibt erhalten.
- Erweiterte isolierte Repository-End-to-End-Prüfungen: A/B-Setup-Erstellung, MRU-Wechsel, MON-Unterscheidung, Filterbank 10/20, ungültiger Import; zusätzlich atomarer B-Save mit Repository-Neuöffnung, A-Isolation, MRU-Erhalt, Ablehnung ungültiger Schreibvorgänge ohne Beschädigung sowie Ablehnung fehlender Setup-ID ohne Indexänderung.
- Alle Fixture-Vorgänge finden nur in einem zufälligen Cache-Testverzeichnis statt, nicht in produktiven Setup-Dateien.
- KEINE Behauptung einer vollständig automatischen produktiven Setup-/Filterbank-UI-Umschaltung. Diese sowie PCM-Blocklücken- und Aufzeichnungsbelege bleiben offen und werden SKIP.
- Buildblocker: Hier kein Android-SDK und keine Gradle-8.13-Distribution; Android-Compile/Gradle-JVM-Tests **nicht** ausgeführt. Kein Smartphone-Release ohne Build-Nachweis.

# v0.5.78 / versionCode 236 – isolierte Mehrgruppen-Systemtests (Quellkandidat; Android-Build offen)

- Neues `IsolatedSetupSystemTest`: A/B-Anlage in getrenntem Cache-Repository, echter A→B→A-Wechsel mit MRU/Active-ID-Prüfung, Repository-Neuöffnung, MON-Parameter- und Filterbank-JSON-Roundtrips, ungültiger Import, vollständige Fixture-Bereinigung.
- Keinerlei Zugriff auf produktive Setup-Dateien für diese Fixture-Tests. Keine Live-Veränderung von MON oder Filterbank für die Roundtrip-Prüfungen.
- Der bestehende automatische Live-Test prüft zusätzlich die Filterbank-Konfiguration vor und nach drei echten Analyzer-STOP/RUN-Zyklen.
- Ergebnisbericht führt die isolierten Tests und Live-/Log-Tests zusammen; unveränderter ZIP-Export.
- Kritische Restlücken dokumentiert in `docs/project/SYSTEMTEST_SCOPE_v0.5.78.md`.
- Android-/Gradle-Build und JUnit-Tests konnten hier nicht ausgeführt werden (Gradle-8.13-Download ohne Netzwerk). **Keine Build-Freigabe**.

## v0.5.77 (versionCode 235) — safe automated live regression (candidate)

- **AutonomousRollingSystemTest**: from the Diagnostics screen, execute three real STOP/RUN cycles using the existing controller, but only if Analyzer is RUN in ROLLING_8K and Rolling was started earlier in Scout. Never stop/restart/clear Scout Rolling or modify user setups.
- Assert Rolling still owns the microphone and remains active after every STOP/RUN; assert MON enabled state and parameter snapshot unchanged. Add Scout-owned monotonic Rolling starts/stops/generation/appended counters and compare before/after to catch invisible resets or restarts. Best-effort restore Analyzer RUN also on failure/cancellation.
- Wait 33 seconds for multiple real PCM heartbeats; evaluate only the current tagged test window, never merge historic sessions.
- Save completed JSON/CSV report in private app storage; export the *same completed results* rather than re-running tests during export.
- Unsafe/unimplemented real setup-switch, filter mutation, statistics and block-perfect PCM verification remain SKIP; cannot claim full unattended coverage yet.
- Android/Gradle compilation and device runs not verified in this environment. Retains existing Termux-v11 script unchanged.

# v0.5.76 / versionCode 234 – Systemtest: Rolling-Sitzungen sauber trennen (Quellkandidat)

- F75-01: `rolling_counter_monotonic` wertet nur die letzte explizit protokollierte `AUDIO_DIAG_ROLLING_RECORDING_STARTED`-Sitzung aus. Reguläre Samplezähler-Neustarts einer neuen Rolling-Sitzung sind kein FAIL mehr.
- Andere Log-Replay-Prüfungen ebenfalls auf diese jüngste Sitzung begrenzt, um historische Fehler nicht dem aktuellen Test zuzuordnen.
- Sitzungsanzahl und Startposition werden in Ergebnisdetails ausgewiesen; `checkedLogLines` entspricht der ausgewerteten Teilmenge.
- JVM-Regressionstests: unabhängige Sitzungen, echter Rücksprung innerhalb der letzten Sitzung, historische Neustartfehler.
- Test bleibt read-only; keine automatische Bedienung, keine zusätzliche Audio- oder Setup-Manipulation. SKIP-Regeln bleiben erhalten.
- Eine echte abgeschlossene Testlauf-Grenze sowie PCM-Blocksequenz-/Schreiblückenprüfung sind weiterhin offen.

# v0.5.75 / versionCode 233 – permanentes Systemtest-Framework (erste Stufe, Quellkandidat)
- Read-only Ereignislog-Auswertung in `PersistentSystemTest.kt` mit 12 PASS/FAIL/SKIP-Prüfungen, CSV/JSON.
- Im Analyzer-Diagnosepanel dauerhaft verfügbare Schaltflächen für Start und ZIP-Export.
- Export über bestehenden MediaStore-Diagnosepfad mit bereinigtem App-Log.
- Drei JVM-Regressionsfälle für Erkennung von Restart, Zählerrückgang und SKIP bei fehlender Evidenz.
- Keine simulierten UI-Bedienungen oder Controller-STOP/RUN-Sequenzen; diese sind explizit als SKIP ausgewiesen und bleiben als nächste Ausbaustufe offen.
- Keine Änderungen am Audio-/Rolling-/MON-/Setup-Steuerpfad.
- Android-Build und Gerätetest müssen noch erfolgen.

# v0.5.74 / versionCode 232 – benannter Setupwechsel bei MON EIN (Quellkandidat)

- F73-01: Der STOP-Panel-Setupwechsel speichert zunächst das aktive benannte JSON samt seinen unveränderten MON-Parametern; erst danach wird die laufende MON-Wiedergabe für die streng gesicherte Setup-Transaktion kurz angehalten und nach Erfolg oder Fehler mit dem zuvor gewählten EIN/AUS- und SHIFT-Zustand wieder aufgenommen. Bei Erfolg stammen MON-Parameter aus dem neuen Setup, bei Fehlern werden alte Analyzer-/MON-Parameter soweit möglich zurückgenommen.
- Die Sicherheitsprüfung im STOP-Adapter ist **nicht** abgeschwächt; Rolling-PCM wird weder gestoppt noch neu initialisiert. Aktive MON-WAV-Aufzeichnungen bleiben aus Datensicherheitsgründen für Setupwechsel gesperrt, statt implizit beendet zu werden.
- Setupwechsel bei bereits laufendem Analyzer bleibt weiterhin gesperrt; Öffnen der Setupseite stoppt die Analyzer-Auswertung automatisch. Bei fehlgeschlagener Speicherung gelten weiterhin Wiederholen/Trotzdem wechseln/Abbrechen.
- Offene Prüfungen: vollständiger Android/Kotlin-Build, echtes MON+Rolling+Setupwechsel-Geräteszenario und Fehlerfall-/Rollback-Tests. Keine stabile Freigabe.

# v0.5.73 / versionCode 231 – Recovery des Analyzer-Wiederanlaufs (Quellkandidat)

- F72-02: Reproduzierter Crash vom 17.10.2026, 10:09:47, `IllegalStateException` bei `AnalyzerNamedSetupStart.start()` nach temporärem Filterbank-STOP mit aktivem MON. Der Wiederanlauf in ROLLING_8K lädt deshalb nicht nochmals das aktive benannte JSON, sondern startet per unmittelbar aufgenommenem STOP-Konfigurationscheckpoint (`startFromStoppedConfiguration`). Dadurch bleibt auch die zuvor übernommene Filterbankkonfiguration unverändert; Rolling-Audioerfassung und MON werden dabei nicht gezielt angehalten.
- Für automatische UND manuelle RUN-Aufrufe über `AnalyzerModeCoordinator.restartMeasurement()` werden synchrone Start-Exceptions protokolliert und als Fehlerstatus zurückgegeben, nicht mehr ungefangen in die Compose-Coroutine geworfen. Direkt-/Testmodus-Aufrufe bleiben funktional weitgehend unverändert.
- Fehler bei tatsächlicher Setup-Aktivierung bleiben strikt gesperrt; bestehende Sicherheitsprüfung für neues JSON nicht abgeschwächt.
- Offen: Reproduzierter Geräte-Test mit MON + Rolling, Filterbank-Persistenz, normaler Setupwechsel, Kotlin/Android-GitHub-Build sowie expliziter Test für den unwahrscheinlichen Start-Fehler nach teilweiser Ressourcenbelegung. Dies ist KEINE freigegebene stabile Version.

# v0.5.72 / versionCode 230 – MON-Erhalt bei Setup-STOP und Filterbank-Automatik (Quellkandidat)

- F71-01: Der bisherige MON-Auto-STOP bei `state.running=false` wird ausschließlich während des kontrollierten, vorübergehenden Analyzer-STOP für Setups/Filterbank ausgesetzt. MON-EIN/AUS und aktuelle MON-Parameter bleiben erhalten; der normale finale Analyzer-STOP behält die bisherige MON-Abschaltung. Kein Eingriff in Rolling-Erfassung oder PCM-Puffer.
- F71-02: Beim Öffnen der Filterbank aus RUN Analyzer-Auswertung einmalig STOP, mehrere Filterwerte bei STOP übernehmen, beim Verlassen nur falls zuvor RUN automatisch RUN. Direkter Wechsel zwischen Setups und Filterbank benötigt keinen unnötigen Zwischen-Neustart; vorheriges STOP/PAUSE-Verhalten bleibt berücksichtigt.
- Längere Analyzer-Pausen führen weiterhin zu Analyzer-Statistiklücken. Statistik-Abdeckungs-CSV, sichere Audioquellenwechsel und andere STOP-pflichtige Parameter sind nicht Bestandteil dieser gezielten Fehlerkorrektur.
- Android-Build und Gerätetests noch nicht bestätigt; die Tests dieses Kandidaten sind gesondert dokumentiert.

# v0.5.71 / versionCode 229 – MON-Bereich und automatische Setup-Pause (Quellkandidat)

- Eigenständige MON-&-Audio-Navigationskarte; bisherige MON-Bedienelemente werden in diesem Bereich eingeblendet, die bisherigen Steueraufrufe bleiben unverändert.
- Bei Öffnen von „Setups“ aus RUN: nur Analyzer STOP, Rolling nicht anfassen; bei Rückkehr zu den Bereichen automatisch RUN, sofern vorher RUN. Vorherige PAUSE wird wiederhergestellt, vorheriges STOP bleibt STOP.
- Hinweis auf Statistik-Lücke während Analyzer-STOP. Parameterabhängiges STOP/RUN für weitere DSP-Parameter, Rolling-Audioquellenwechsel sowie Statistik-Abdeckungs-CSV sind noch ausdrücklich offen; siehe `docs/project/ANALYZER_SETUP_AUTO_STOP_V0571.md`.
- 12 statische Quelltextverträge PASS. Android/Kotlin-Build konnte lokal nicht gestartet werden, da Gradle-8.13-Distribution fehlt und Netzwerkzugriff nicht verfügbar ist; Hardwaretest offen.

# v0.5.70 / versionCode 228 – gruppierte, helle Analyzer-Einstellungsübersicht

- Einstellungsnavigation optisch heller und in vier logische Gruppen geordnet; neun vorhandene Unterpanels bleiben samt Bedienelementen und Controller-Aufrufen vollständig erhalten.
- Keine Änderung an Audio, Rolling, FFT, MON-Controller oder JSON-Setup-Persistenz. Weitergehende Vereinheitlichung der Panelinhalte ausdrücklich noch offen.
- Statik-Regression: `tools/verify_v0570_settings_navigation.py`; Android-Build, Self-Test und Gerätetest noch offen.
- Details: `docs/project/ANALYZER_SETTINGS_NAVIGATION_V0570.md`.

# v0.5.69 / versionCode 227 – Setupstatus, RUN-Sperre, FFT-Warm-up (16.10.2026)

- Die obere Analyzer-Setup-Anzeige verfolgt die aktive JSON-Zuordnung während einer geöffneten Analyzer-Oberfläche und aktualisiert den Namen nach erfolgreichem Wechsel.
- Ein Setupwechsel bei Analyzer RUN öffnet vor jeder Speicherung/Aktivierung den Dialog „Analyzer zuerst stoppen“; Rolling bleibt unberührt. Vorhandene strengere MON-/Audio-Ressourcenprüfungen bleiben bestehen.
- Während regulären FFT-Warm-ups wird die irreführende Warnung „Kein verwertbares Eingangssignal“ unterdrückt. Die vorhandene Diagnose für spätere Signalfehler bleibt erhalten.
- Statische Regressionen: `tools/verify_v0569_setup_status.py`. Gradle-/Android-Self-Test und Hardwareprüfung sind noch offen.

# v0.5.68 / versionCode 226 – MON-Anzeige nach benanntem Setupwechsel (16.10.2026)

- Im Analyzer-Refresh werden MON-Center und MON-Gain jetzt aus dem aktuellen MON-Controller synchronisiert; unter aktivem Touch-Drag bleibt die lokale Bedienung vorrangig.
- Bestehende JSON- und Legacy-Speicherlogik unverändert. Regression `tools/verify_v0568_mon_setup.py`; Hardware- und Kotlin-Build offen. Siehe `docs/project/MON_SETUP_SYNC_V0568.md`.

# v0.5.67 / versionCode 225 – Analyzer-Verlassen und MON-Persistenz (16.10.2026)

- Beide Analyzer-Verlassen-Aktionen sichern aktuelle Analyzer- und MON-Werte vor Audio-Teardown und Navigation in die aktive benannte JSON-Datei. Bei Schreibfehler Warn-Popup mit Wiederholen, Trotzdem verlassen und Abbrechen; eine ausdrückliche Entscheidung kann den Verlassen-Vorgang fortsetzen.
- Weiterlaufen: Analyzer, MON und Rolling im Hintergrund weiter aktiv; nur die Scout-Oberfläche wird angezeigt.
- Stoppen: einmaliger, geordneter Ablauf ohne verschachtelte Exit-Coroutine. Rolling wird ausschließlich beendet, wenn es vor Analyzer-Start nicht aktiv war; beide asynchronen Wiederherstellungsrichtungen werden bestätigt.
- Setupwechsel: JSON wird nur bei Änderungen geschrieben; bei Fehlschlag Warn-Popup mit Erneut speichern, Trotzdem wechseln (Änderungen verwerfen), Abbrechen (beibehalten).
- Regression: 17/17 statische Verträge PASS. Gradle/JUnit und Android-Gerät-Self-Test noch offen; keine REGULAR-Freigabe.
- Details: `docs/project/EXIT_FIX_V0567_IMPLEMENTATION.md`.

- Build-Korrektur nach GitHub Actions Run 38922482903: Companion-Object-Funktion `AnalyzerStoppedJsonControllerPort.applyCombinedDraft` korrekt statisch aufgerufen; vorher unerreichbarer Instanz-Aufruf verursachte `Unresolved reference`. Android-Neubuild noch ausstehend.

# v0.5.66 / versionCode 224 – Analyzer-Setupwechsel bei Rolling (16.10.2026)

- 8-kHz-Rolling-Aufnahme ist bei STOP kein pauschaler Sperrgrund für die Analyzer-JSON-Entwurfsübernahme mehr; die erwartete Mikrofon-Eigentümerschaft `rolling` wird ausdrücklich geprüft. Fremde Besitzer, Direct-Audio, MON-Aufnahme, aktive MON-Ausgabe und Preview bleiben gesperrt. Sicherheitsmarker für Rolling werden nur bei nachgewiesenem Rolling-Eigentümer selektiv ausgeklammert.
- STOP des Analyzers gibt dessen PCM-Subscription frei, ohne Rolling-Engine/Ringpuffer zu stoppen (bestehendes Verhalten). 8-kHz-RUN wendet das aktive benannte JSON erneut an und startet über den vorhandenen präferenzfreien STOP-Konfigurations-Checkpoint. Andere Modi behalten den Legacy-Start.
- Vor dem Wechsel wird das vorige JSON per bestehendem AtomicFile-Verfahren gesichert. `Result.success(false)` zählt nicht mehr als erfolgreicher Save. MRU/aktive ID wird erst nach positiver STOP-Übernahme geändert.
- Termux-Skript `LFN_GITHUB_TERMUX_v11.sh` unverändert übernommen; ältere separat hochgeladene Versionen nicht aufgenommen. Keine Neugestaltung der Setup-Panels.
- Lokaler Quelltext-Vertragstest `tools/verify_v0566_setup_switch.py`: zunächst 17/17 PASS; nach abschließender Änderung erneut prüfen. **Gradle/JUnit, integrierter Android-Self-Test, APK-Build, Hardware-/Rolling-Kontinuitätstests nicht durchgeführt.** Dies ist ein **nicht gerätevalidierter Quellkandidat**, keine bestätigte APK-Freigabe.
- Rest-Risiken: Audio-Service-Lifecycle und Mic-Arbiter sind nicht auf demselben Lock wie Controller-STOP serialisiert; Hardwarekontinuität sowie persistenter MRU-/JSON-Rollback bei Index-Schreibfehler müssen in Android-Integrationstests nachgewiesen werden.

---

# v0.5.65 / versionCode 223 – Setup-Auswahl und Status sichtbar (16.10.2026)

- Analyzer zeigt die **zugeordnete** benannte JSON-Setup-Datei dauerhaft oberhalb der eigentlichen Anzeige; Status ist bewusst nicht als bestätigter Messstart gekennzeichnet.
- In Einstellungen/Setups ist die aktuelle Zuordnung ausgeschrieben; ein Tippen auf einen anderen Namen führt nur bei STOP den bereits vorhandenen geprüften STOP-Entwurfswechsel aus. Keine zusätzliche Laden-Schaltfläche.
- Anlegen ist ausdrücklich als „Speichern unter neuem Namen“ bezeichnet; vorhandene Persistenz beim Verlassen des Analyzers bleibt erhalten. Statusmeldungen unterscheiden die bestätigte STOP-Übernahme vom nicht verifizierten START-Laden.
- Keine Änderung an Audio-Pipeline, REGULAR-Paketkennung, Cloud, Signierung oder Workflow. START lädt weiterhin zum Teil Legacy-Prefs; vollständige aktive Audio-Setup-Umschaltung ist nicht implementiert.
- Prüfung: lokale statische Verträge und ZIP-Integrität; Android-/GitHub-Kompilierung und Hardwaretest offen.

---

# LFN-Scout v0.5.64

## Neu in v0.5.64

- Helles, vereinfachtes Panel für benannte JSON-Analyzer-Setups mit MRU-Anzeige.
- Automatisches atomisches Speichern der aktuell ausgewählten Analyzer- und MON-JSON-Daten beim Verlassen des gesamten Analyzer-Screens, nicht beim Panelwechsel.
- Legacy-Setup-Leiste aus der normalen UI entfernt, intern vorerst erhalten.
- **Grenze:** Vollständig persistenter Setupwechsel beim Messstart und automatische Übernahme aller bisher bestätigungspflichtigen Parameter noch ausstehend. Keine Android-Gradle-/Xiaomi-Testfreigabe.
- Verbindlicher Stand: `docs/project/CURRENT_STATE.md`.

---

## 0.5.63 – 15.10.2026

- Behebt den im GitHub-TEST-Run 38749515984 verbliebenen Kotlin-Kompilierungsfehler in `LiveAnalyzerController.kt:1318`: getrennte `in`-Prüfungen für `Set<Int>` (Direct-Mode) und `IntArray` (Legacy-Mode).
- `versionName` 0.5.63, `versionCode` 221. Vollständiger Projektstand ohne zusätzliche Termux-Abhängigkeiten.
- ZIP-/Quelltextprüfung erfolgreich; erneuter GitHub-Build noch ausstehend.

# v0.5.62 / versionCode 220 – Kotlin-Kompilierungsfehler AP31 korrigiert (15.10.2026)

- Grundlage: vollständig hochgeladenes v0.5.61-Paket, Phase 3O AP31. GitHub-TEST-Fehlerlog Run 38746929506 ausgewertet.
- `AnalyzerSetupSwitchPreflight.kt`: `when`-Bedingung für `requiresStop` und `stoppedDraftFields` in gültige Kotlin-Mehrfachbedingung umgestellt.
- `LiveAnalyzerController.kt`: expliziter `Unit`-Rückgabetyp für rekursive STOP-JSON-Rollback-Funktion; bedingte `in`-Prüfungen auf FFT-Größe (zwei Stellen) und Hop (eine Stelle) geklammert.
- Versionswechsel `0.5.61/219` → `0.5.62/220`; TEST- und REGULAR-Paketkennungen, Signier- und Buildverfahren bleiben unverändert.
- Prüfung: Vollständige ZIP-Struktur und alle gezielten Quelltextkorrekturen lokal verifiziert. Ein Android-Gradle-Build und eine APK wurden **nicht** durchgeführt/erzeugt. GitHub-TEST und Hardwaretest offen.
- Kein Setup-Facelift-Funktionsausbau; produktive vollständige Setup-Aktivierung weiterhin nicht freigegeben (`Plan.executable=false`).

---

## 15.10.2026 – AP31 STOP-JSON-Adapter (Entwicklungsstand)

Controllerseitige STOP-JSON-Entwurfsübernahme, strikte Vollständigkeits- und Werteprüfung, Rücknahme und Port-Anbindung ergänzt. Noch **kein** dauerhaftes Laden mit START/MON; keine freigegebene TEST-Version. Siehe `docs/project/CURRENT_STATE.md`.

# Phase 3O – AP31 (ENTWICKLUNGSSTAND, KEIN FREIGEGEBENES RELEASE) – 15.10.2026

- Grundlage: AP30 v0.5.60/218. Geänderter Gradle-Quellkandidat 0.5.61/219.
- AnalyzerJsonSetupBrowser: echtes Erstellen einer versionierten JSON-Datei aus der aktuellen Controller-Momentaufnahme plus MON, SAF-Dokumentauswahl für Import/Export, Umbenennen, Löschen mit internem Backup. Import vergibt bewusst neue UUID und überschreibt keine Setups; Dateigröße begrenzt auf 2 MiB. Alte 1–4-Slots bleiben unverändert.
- Nicht erledigt: Transaktionales Anwenden der gesamten Analyzer-/Audio-/FFT-Parameter auf den laufenden Controller, Ersetzung der Slot-Steuerung, vollständige Parametererfassung inklusive geräteabhängiger Quellen und abgesicherte Rücknahme. `Plan.executable=false` bleibt absichtlich; kein Scheinerfolg.
- Validierung: Quelltext-/Paket-Struktur geprüft; Android-Gradle-Build und Hardwaretest NICHT ausgeführt. Nicht als lauffähige TEST-APK bezeichnen. Vor Hardwareeinsatz GitHub-Unit-Tests und vollständigen APK-Build erforderlich.

---

# Phase 3O – AP30: Kotlin-Testkompilierung korrigiert (15.10.2026)

- Ausgangslage: AP29 GitHub-TEST ROT (`38665752366`): `SeismicPlotInteractionContractTest.kt:62:49 Unresolved reference 'folder'` bei `:app:compileDebugUnitTestKotlin`. Die Test-Assertion enthielt eine nicht escapte Kotlin-Stringinterpolation `$folder`.
- Gezielter Fix: Im reinen Test-Quellcode das `$` als `\$folder` escaped, damit die Assertion weiterhin exakt den Quelltext `"Download/$folder/"` von `PublicDataDirectory` prüft. Der Produktivcode und die Pfadtrennung bleiben unverändert.
- Versionsstand `0.5.60 / 218` für nächstes TEST-Update; `Plan.executable=false` unverändert. REGULAR wird nicht gebaut oder installiert.
- Verifikation: Quelltext-Assertion und ZIP-Struktur statisch geprüft. Gradle-Unit-Tests, GitHub-Build und Android-Hardwaretest OFFEN.

---

# Phase 3O – AP29: Seismogramm-Exportvertrag nach TEST-Speichertrennung (15.10.2026)

- Ausgangslage: AP28 GitHub-TEST ROT: `SeismicPlotInteractionContractTest.exporter_writes_full_near_plot_not_only_zoom_viewport`, 260/261 Tests bestanden. Der Vertrag erwartete den festen Legacy-Pfad `Download/LFN-Scout/`, während der reale Export seit AP28 über `PublicDataDirectory.relativePath` den passenden TEST-/REGULAR-Pfad wählt.
- Korrektur: Nur den veralteten Unit-Test auf die zentrale Pfadbindung umgestellt; neue Regression gegen versehentliches Zusammenlegen der TEST-/REGULAR-Ausgabe. Seismogramm-Renderer, Audio, Setup-Speicher, GitHub-Workflow und Supabase bleiben unverändert.
- Gradle `0.5.59` / `217` für installierbares TEST-Update; `Plan.executable=false` bleibt unverändert.
- Prüfstand: lokale Struktur- und ZIP-Prüfung, GitHub-Unit-Tests, Gradle-Build und Android-Hardwaretest **OFFEN**. Keine TEST-APK vor erfolgreichem GitHub-Build.

---

# Phase 3O – AP28: TEST-Speichertrennung (Quellkandidat, 15.10.2026)

- Nutzervorgabe: In der Android-Dateimanageransicht **Downloads/lfn-scout-test/**. Technisch wird der von Android bereitgestellte öffentliche Downloads-Ordner (`Environment.DIRECTORY_DOWNLOADS`, MediaStore `Download/`) verwendet; damit kein zusätzliches paralleles `Downloads`-Verzeichnis entsteht.
- Nur `applicationId` mit `.githubdebug` verwendet `lfn-scout-test`; REGULAR bleibt `LFN-Scout`. Es wird weder automatisch migriert noch eine REGULAR-Datei kopiert. Legacy-Lookups der TEST-App werden auf das neue Verzeichnis eingeschränkt.
- Modifizierte Bereiche: Audioarchiv/WAV, Statistiken/CSV, Wasserfallbilder, Diagnose, Export/Importordner, Setup-Dateien. App-interne Android-Daten werden weiterhin durch getrennte Paketkennungen isoliert.
- Gradle: `0.5.58` / `216`; Workflow, Signierung, Supabase, Mic/FFT und `Plan.executable=false` unverändert.
- Prüfstatus: ZIP-/statische Pfadprüfung lokal. Android-Gradle und Gerätemessung noch offen.

---

## Phase 3O/AP25 – Setup-Facelift (Source Candidate, 15.10.2026)

- Analyzer: Setup-Verwaltung sichtbar hervorgehoben; ausgewählter Platz, Speicherstatus und Setup-Name; direkter Einstieg und Setup-Kategorie zuerst.
- Nur UI/Text, keine Änderungen an Audio/FFT/WAV/Persistenz/Cloud/Workflow/Termux. Gradle-Version unverändert.
- Android-Build und neuer Hardwaretest stehen aus.

# Phase 3O – Arbeitspaket 24 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 23 **GRÜN**.
**Änderung:** Bei einem bereits quarantänisierten Mikrofon (`MicCaptureArbiter.uncertainReleaseOwner != null`) verweigern Rolling- und Direct-Service nun auch neue Android-`onStartCommand`-Starts *vor* Foreground-Aktivierung und Capture-Job-Registrierung. Rolling führt seinen vorhandenen STOP-Cleanup aus; Direct beendet den Service. Normale Starts bleiben unverändert. Neuer statischer Regressionstest prüft die Reihenfolge.
**Grenzen:** Keine positive Bestätigung physischer Hardwarefreigabe; keine automatische Entsperrung. Controller-Adapter, Rollback und Gerätetest weiter offen, `Plan.executable=false`.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert; historisches Storage-Backup nur in GitHub.

---

## Phase 3O – Arbeitspaket 23 (15.10.2026)

- Rolling-Autorestart endet nach fehlgeschlagener AudioRecord-Freigabe mit gesperrtem Mikrofon-Token; Diagnose-Trace und Logging statt weiterer Restart-Versuche.
- Statischer Regressionstest für Gate-Position und Schleifenabbruch. Produktive Setup-Umschaltung weiterhin deaktiviert.

### Phase 3O – Arbeitspaket 22
- Rolling-/Direct-Service verweigern erneuten Capture-Launch bis der alte, auch bereits abgebrochene Job vollständig abgeschlossen ist. STOP lässt die Job-Referenz bis zum Coroutine-Ende stehen. Neuer statischer Regressionstest. Keine produktive Setupfreigabe.

# Phase 3O – Arbeitspaket 21 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 20 **GRÜN**.
**Änderung:** In `RollingAudioService` und `AnalyzerDirectAudioService` wird ein bereits erfolgreich initialisiertes `AudioRecord`-Objekt vor dem optionalen `setPreferredDevice()` in der lokal durch das `finally` bereinigten Recorder-Referenz registriert. Wirft die Gerätezuweisung eine Ausnahme, läuft die bestehende `finally`-Freigabe über das korrekte Objekt. Bei einer `release()`-Ausnahme wird wie seit AP19 die Mikrofon-Lease quarantänisiert. Neuer statischer Regressionstest erzwingt die Reihenfolge.
**Grenzen:** Keine atomare Android-Hardwarefreigabebestätigung, kein produktiver Setupwechsel/Hardware-Rollback, kein Gerätetest. `Plan.executable=false` bleibt unverändert.
**Tests:** Lokale statische Phase-3O-Prüfer; JUnit/Android-Build für AP21 erst durch GitHub-TEST zu bestätigen.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert; historisches Backup nur in GitHub.

---

# Phase 3O – Arbeitspaket 20 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 19 **GRÜN**.
**Änderung:** Bei der AudioRecord-Quellenauswahl wird ein `release()`-Fehler eines verworfenen Kandidaten in `AudioRecorder`, `RollingAudioService` und `AnalyzerDirectAudioService` nun konservativ behandelt: bereits gehaltenes Mikrofon-Token als unsicher markieren und den Auswahlversuch abbrechen. Dadurch kein Start einer weiteren AudioRecord-Instanz nach ungewisser Freigabe im selben Prozess. Erfolgreiche Kandidatenfreigaben bleiben unverändert. Regressionstest auf Quellcodeebene hinzugefügt.
**Grenzen:** Keine verifizierte physische Freigabe durch Android, keine automatische Entsperrung, kein Hardware-Rollback und kein produktiver Setupwechsel. `Plan.executable=false`.
**Tests:** Statische Prüfungen lokal; JUnit/Android-GitHub-Build für Arbeitspaket 20 ausstehend, kein Gerätetest.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert. Historisches Storage-Backup ausschließlich auf GitHub.

---

# Phase 3O – Arbeitspaket 19 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 18 **GRÜN**.
**Änderung:** Wenn `AudioRecord.release()` in Direct, Rolling oder Scout-Recorder eine Ausnahme auslöst, markiert der bereits gehaltene `MicCaptureArbiter.Token` die Hardwarefreigabe als unsicher. `close()` gibt diesen Token dann nicht frei; weitere Capture-Starts bleiben innerhalb desselben Prozesses blockiert. Erfolgreiche normale Freigaben folgen unverändert dem bisherigen Ablauf. Der Status `uncertainReleaseOwner` ist diagnostisch abrufbar. Zwei zusätzliche JUnit-Fälle prüfen die Quarantäne und den Normalfall.
**Grenzen:** Android bietet durch die Rückkehr von `release()` keinen separaten Hardware-Freigabenachweis. Fehler bei `stop()` und andere nicht erkannte Hardware-Fehler sind damit nicht vollständig erfasst; nach einem Release-Fehler kann ein App-Prozessneustart erforderlich sein. Keine automatische Entsperrung ohne Hardware-Nachweis; keine produktive Setup-Transaktion. `Plan.executable=false`.
**Prüfstatus:** Statische Tests lokal; neue JUnit-Fälle und Android-Build erst im GitHub-TEST zu bestätigen, kein Gerätetest.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert; historisches Storage-Backup nur auf GitHub.

---

# Phase 3O – Arbeitspaket 18 (Quellkandidat)

**Stand:** 14.10.2026; Zielversion `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 17 **GRÜN**.
**Änderung:** Rein lesende STOP-Grenzdiagnose berücksichtigt jetzt zusätzlich die Abonnentenzahl des gemeinsamen Roh-PCM-Hubs (`AnalyzerSessionGateway.rawPcm.activeSubscribers`). Positive Abonnentenzahlen, negative unplausible Werte und Ausnahmen liefern getrennte Sperrgründe. Die Produktionsabfrage übergibt die bestehende Hub-Beobachtung; drei neue JUnit-Regressionsfälle sichern alle drei Fehlerklassen ab. Kein Stopp, kein Hardware-Release, keine Mutation.
**Sicherheitsgrenze:** Der Abonnentenzähler ist keine atomare Hardware-Release-Bestätigung. `executionAuthorized=false`, `Plan.executable=false`. Controlleradapter, echte AudioRecord-Freigabebestätigung, Rollback und Android-Gerätetest bleiben offen.
**Tests:** Statische Quellprüfungen lokal; JUnit und vollständiger Android-Build für Paket 18 erst im GitHub-TEST zu bestätigen.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert; Storage-Backup nur auf GitHub.

---

# Phase 3O – Arbeitspaket 17 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213`.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 16 **GRÜN**.
**Änderung:** `AnalyzerSetupTransitionCoordinator.diagnoseStoppedBoundary()` vereinigt die bisherigen Owner-/Lifecycle-Blocker mit der lokalen STOP-Diagnose des `LiveAnalyzerController`. Separate Gründe für laufenden Analyzer, vorhandene Session und PCM-Subscription. Probe-Ausfälle werden fail-closed gekennzeichnet. `diagnoseCurrentStoppedBoundary()` ist ein rein lesender Produktionsdiagnose-Einstieg ohne Aufrufer im produktiven Umschaltpfad. Drei neue JUnit-Regressionen.
**Grenze:** Die Beobachtungen sind zeitlich nicht atomar, weder Hardware-Release noch Android-Service-Stopp sind dadurch bestätigt; `executionAuthorized=false` und `Plan.executable=false` bleiben bestehen. Kein realer Controller-Adapter, kein Gerätetest.
**Tests:** Statische Quellprüfungen lokal; JUnit-Ausführung/Android-GitHub-Build für Paket 17 durch Nutzer noch ausstehend.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert. Historisches Storage-Backup nur auf GitHub.

---

## Phase 3O – Arbeitspaket 16 (14.10.2026)

- Lokale STOP-Verifikationsdiagnose für Analyzer-Session und PCM-Puffersubscription, ohne Hardware-Freigabebehauptung.
- Kein produktiver Umschaltpfad aktiviert; Android- und JUnit-Test ausstehend.

## Phase 3O / Arbeitspaket 15 – nicht freigegebener Quellkandidat (14.10.2026)
- `LiveAnalyzerController`: neuer opt-in Start mit vorher geprüftem STOP-Konfigurationscheckpoint ohne Preference-Reload. Bestehender Start unverändert delegiert an den Legacy-Pfad; keine produktive Anbindung.
- Neuer statischer Regressionstest für Abgrenzung Legacy-/Checkpoint-Start; Hardware-Lease, Controller-STOP und echter Rollback bleiben ausstehend.
- `Plan.executable=false`; Gradle `0.5.55 / 213`, Termux v4 und geschützte Konfigurationen unverändert.

## 14.10.2026 – Phase 3O, Arbeitspaket 14 (Quellkandidat)

- Controller-Audit: preference-lesender `LiveAnalyzerController.start`, unzureichend verifizierter Ressourcen-STOP und Checkpoints ohne echte Audio-Owner als Freigabeblocker dokumentiert.
- Zwei neue JUnit-Fehlerpfade: Lease-Verlust nach STOP und wiederholter START-Fehler einschließlich fehlgeschlagenem Rollback.
- Neuer statischer Architektur-Guard; keine produktive Controller-Verkabelung oder Änderung des Executors (`Plan.executable=false`). Letzter GitHub-TEST Paket 13 GRÜN.

## 14.10.2026 – Phase 3O, Arbeitspaket 13 (Quellkandidat)

- Drei JUnit-Konkurrenztests mit echten Threads/Latches statt ausschließlich verschachtelter Aufrufe: laufende Setup-Lease gegen Rolling/Direct, zweite Setup-Anforderung und Freigabe nach fehlerhaftem Setup-Callback.
- Produktive Audio-Routinen und Setup-Executor unverändert (`Plan.executable=false`). Die Tests liefern keinen Nachweis für Android-Hardware oder asynchrone Service-Lebenszyklen; diese bleiben offen.
- Letzter vom Benutzer bestätigter GitHub-TEST: Arbeitspaket 12 GRÜN. JUnit/Android-Build für Paket 13 ausstehend.

### Phase 3O – Arbeitspaket 12 (Quellkandidat)

- Legacy-Mikrofonreservierungshelfer an zentrale Setup-Reservierung angebunden; drei JUnit-Regressionen und statischer Prüfer. Keine Freigabe des Setup-Executors.

## 0.5.56 – Phase 3O, Arbeitspaket 11 (Quellkandidat, nicht freigegeben)

- Vereinheitlichte Koordinator- und MicCaptureArbiter-Reservierung in einem synchronisierten Zulassungsschritt; kein nachgelagerter Lease-Erwerb mehr.
- Fail-closed bei konkurrierendem Mikrofonbesitzer und token-sicheres Cleanup auch bei Ausnahmen.
- Drei JUnit-Regressionen, statischer Prüfer und Aktualisierung der bisherigen statischen Strukturprüfungen; produktiver Executor unverändert deaktiviert.

## 2026-10-14 – Phase 3O, Arbeitspaket 10 (Quellkandidat)
- Legacy MON-/Service-Start-Helper verweigern bei vorhandenen Koordinator-Markern, aktiver MON-Aufnahme und belegtem Mikrofon. Drei neue JUnit-Regressionen; neue asynchrone Starts müssen den getrackten Startpfad verwenden.
- Arbeitspaket 9 GitHub-TEST vom Benutzer GRÜN. Keine produktive Setup-Freigabe oder Version-/Workflow-/Cloud-Änderungen.

## 2026-10-14 – Phase 3O, Arbeitspaket 9 (Quellkandidat)
- Rolling-/Direct-Startabgabe unter bestehendem Koordinator jetzt zusätzlich bei belegtem `MicCaptureArbiter` abgewiesen; keine Markeranlage oder Android-Service-Einreichung bei Vorabkonflikt.
- Drei JUnit-Regressionstests plus statische Quellenprüfung ergänzt. Keine Aktivierung des Setup-Executors (`Plan.executable=false`), keine Gradle-/Cloud-/Workflow-/Recorder-Änderung.
- GitHub-TEST für Arbeitspaket 8 durch Benutzer **GRÜN** bestätigt; Arbeitspaket 9 offen.

## 2026-10-14 – Phase 3O, Arbeitspaket 8 (Quellkandidat)
- Bestehende synchrone Setup-Reservierung mit tatsächlichem exklusivem `MicCaptureArbiter`-Token gekoppelt; fail-closed bei belegtem Token und exception-sichere Freigabe.
- Drei JUnit-Regressionstests und statischen Lease-Verifikator ergänzt. Keine produktive Setup-Umschaltung; `Plan.executable=false`, Gradle `0.5.55`/`213` unverändert.
- GitHub-TEST Arbeitspaket 7 durch Benutzer GRÜN, Arbeitspaket 8 offen.

## 2026-10-14 – Phase 3O, Arbeitspaket 7 (Quellkandidat)
- Rolling-/Direct-Startanforderungen über die vorhandene Koordinator-Sperre auch gegen andere ausstehende Service-Lifecycles und Capture-Cleanup abgesichert.
- MON-Start bei ausstehenden Service-Lifecycles abgelehnt; aktives MON verhindert neuen getrackten Rolling-/Direct-Start.
- Drei neue JUnit-Regressionstests. Kein produktiver Setupwechsel; `Plan.executable=false`, Gradle `0.5.55`/`213` unverändert.
- GitHub-TEST Arbeitspaket 6 vom Benutzer GRÜN, Arbeitspaket 7 offen.

## 2026-10-14 – Phase 3O, Arbeitspaket 6 (Quellkandidat)
- Bestehende Setup-Reservierung weist jetzt zusätzlich erkannte Audio-Besitzer und fehlerhafte Besitzerabfragen fail-closed ab.
- Vier JUnit-Regressionsfälle für Probe, Reservierung und Freigabe. Keine Produktivfreigabe (`Plan.executable=false`); Version 0.5.55/213 unverändert.

## 14.10.2026 – Phase 3O, Arbeitspaket 5 FIX (nach GitHub-TEST ROT)
- JUnit 225 Tests, 1 Fehler (`AnalyzerSetupTransitionProtocolTest.lostLeaseDoesNotClaimSafeStopped`).
- Ungültige ExclusiveBoundary wird vor dem ersten Controllerzugriff ohne positive Rollback-Bestätigung abgewiesen.
- Eigenen Regressionstest für ungültigen Lease vor Checkpoint ergänzt; vorhandenen Fehlerfall beibehalten.
- `Plan.executable=false`, Gradle 0.5.55/213, Recorder/Cloud/Workflows unverändert. GitHub-TEST des FIX offen.

## Phase 3O / Arbeitspaket 5 (Quellkandidat, 14.10.2026)

- Isolierten Transaktionskern um testbare ExclusiveBoundary ergänzt; keine produktive Controller-/Recorder-Verbindung.
- Vier JUnit-Regressionstests für abgewiesene, gehaltene und verlorene Eigentümerschaft.
- `Plan.executable=false`, versionName 0.5.55 / versionCode 213 und historische Backup-Regel unverändert.

## 2026-10-14 – v0.5.56 Phase 3O, Arbeitspaket 4 (Quellkandidat)
- Isoliertes STOP–Reconfigure–START-Protokoll als testbare Zustandsfolge ergänzt, ohne produktiven Adapter.
- Rollback-Reihenfolge mit Originalfehlererhalt, Recovery-Fehlerliste und verifiziertem Safe-Stop; sechs JUnit-Fälle.
- Produktive Umschaltung bleibt gesperrt (`Plan.executable=false`), Gradle 0.5.55/213 unverändert. GitHub-TEST Paket 3 vom Benutzer GRÜN, Paket 4 offen.


## Phase 3O – Arbeitspaket 3 (Quellkandidat, 14.10.2026)

- Generationstoken für Rolling-/Direct-Capture-Job-Abschlüsse; verhindert Freigabe durch veraltete Callback-Generationen.
- Zwei JUnit-Regressionsfälle; Setup-Executor deaktiviert, Gradle-Version unverändert.

## v0.5.56 – Phase 3O, Arbeitspaket 2 (14.10.2026, Quellkandidat)

- Interne Sicherheitsentscheidung für Gate-Diagnose und Setup-Reservierung vereinheitlicht; verwaiste Lifecycle-Marker sperren nun beide Pfade.
- Unbekannte Service-Bezeichner und bereits markierte Destroy-Zustände verhindern einen getrackten Start.
- Drei zusätzliche JUnit-Regressionen und ein statischer Konsistenztest. Keine Recorder-/Cloud-/Workflow-Änderung, Executor bleibt gesperrt, Gradle 0.5.55/213 unverändert.
- Arbeitspaket 1: GitHub-TEST GRÜN laut Benutzer; Arbeitspaket 2: GitHub-TEST OFFEN.

## v0.5.56 – Phase 3O, Arbeitspaket 1 (14.10.2026, Quellkandidat)

- Ausschließlich ablehnende Sicherheitsdiagnose führt interne Setup-/Service-/Cleanup-Marker mit Audio-Owner-Status zusammen; Ausfall der Owner-Abfrage führt zur Sperre.
- Bisherige `AnalyzerSetupSwitchTransaction` berücksichtigt diese Sperren und behandelt Statuslesefehler konservativ.
- Drei zusätzliche JUnit-Regressionen, neuer statischer Prüfer und Anpassung des bestehenden Owner-Guards an die integrierte Diagnose.
- Kein produktiver Setupwechsel; `Plan.executable=false`; Gradle `0.5.55` / `213` und Termux v4 unverändert. GitHub-TEST ausstehend; 3O/20 laut Benutzer GRÜN.

## Phase 3O/20 (Quellkandidat; GitHub TEST offen)

Rein lesende, synchronisierte Sperrgrund-Diagnose des AnalyzerSetupTransitionCoordinator ergänzt. Keine Freigabe produktiver Audio-/FFT-Setupwechsel. Neue statische und JUnit-Regressionstests; letzter bestätigter TEST 3O/19 grün. Termux v4, Gradle 0.5.55/213, Cloud und Supabase-Backup-Policy unverändert.

# Phase 3O/19 – read-only Gate-Diagnose

- `AnalyzerSetupTransitionCoordinator.snapshotGate()` ergänzt: synchronisierte, unverändernd lesbare Kopien von Start-, Destroy- und Capture-Job-Markern; zwei JUnit-Tests und statischer Verifikator.
- Keine automatische Entsperrung und keine produktive Audio-/FFT-Umschaltung. Letzter durch Nutzer bestätigter GitHub TEST: 3O/18 GRÜN, 3O/19 offen.

## Phase 3O/18 (14.10.2026, Quellkandidat; GitHub TEST offen)

- Koordinator ignoriert verspätete/doppelte Lifecycle- und Job-Ende-Callbacks ohne gültige aktive Vormerkung; zwei JUnit-Tests und Quelltest.
- Historischer MON-Quelltest an bestehende Capture-Cleanup-Sperrbedingung angepasst.
- Letzter bestätigter GitHub-TEST: 3O/17 GRÜN (Nutzerangabe). Setup-Executor weiterhin deaktiviert.

## Phase 3O/17 – Doppelstartschutz (Quellkandidat, GitHub TEST offen)

- Gleichzeitige/wiederholte Rolling-/Direct-START-Anforderung desselben Services wird bei offenem Lifecycle-/Capture-Cleanup-Marker konservativ abgewiesen.
- Drei JUnit-Regressionstests ergänzt. `Plan.executable=false`; Gradle 0.5.55/213; Termux v4 und historische Supabase-Dateien unverändert.
- Letzter bestätigter GitHub TEST: Phase 3O/16 GRÜN (Nutzerbestätigung).

## 14.10.2026 – Entwicklungsstand v0.5.56, Phase 3O/16

Rolling/Direct: Service-`onDestroy` und Coroutine-Capture-Jobende müssen beide eintreten, bevor die Startvormerkung freigegeben wird. Drei JUnit-Tests plus statischer Test. Produktive Umschaltung bleibt gesperrt. GitHub-TEST 3O/15 durch Nutzer grün; 3O/16 noch nicht bestätigt. Zwei historische Quellprüfungen 3F/3J melden alte Erwartungsabweichungen.

## Phase 3O/14-FIX1 · 2026-10-14

- GitHub TEST Phase 3O/14 ROT: `RollingAudioService.kt:395:13 'return' is prohibited here`.
- Entfernt das unzulässige unqualifizierte `return` innerhalb von `AnalyzerSetupTransitionCoordinator.withServiceStart`-Lambda; Rückgabewert bleibt `Boolean`.
- Keine Änderung an Gradle-Version, Workflows, Termux v4, Cloud, Recordern oder Produktivfreigabe. GitHub TEST des Fixes offen.

# Phase 3O/14 – Rolling-/Direct-Startschutz (14.10.2026)

- GitHub TEST Phase 3O/13: **GRÜN laut Nutzer**; Phase 3O/14 noch nicht durch GitHub/Android bestätigt.
- `AnalyzerSetupTransitionCoordinator.withServiceStart` serialisiert die synchronen START-Anforderungen von `RollingAudioService.requestStart` und `AnalyzerDirectAudioService.requestStart` mit einer zukünftigen Setup-Reservierung. Bei aktiver Reservierung wird der Request ohne Android-Serviceaufruf mit `false` abgewiesen. MON-Schutz der Phase 3O/13 bleibt bestehen.
- Zwei neue JUnit-Regressionen: Ablehnung eines konkurrierenden Service-Starts sowie Wiederverwendbarkeit nach Start-Exception.
- **Nicht atomar über den gesamten Service-Lebenszyklus:** `startForegroundService` kehrt vor dem asynchronen AudioRecord-Start zurück. Ein zuvor angenommener Request kann später noch eine Session starten. Bereits aktive oder noch startende Services werden nicht von dieser Koordinationsstelle gestoppt oder reserviert. Eine produktive Umschaltung ist daher weiterhin verboten (`Plan.executable=false`).
- Keine Änderung an Recorder-STOP-Routinen, Cloud, GitHub-Workflow, Gradle `0.5.55/213`, Termux v4 oder Backup-Dateien. Storage-Backup auf GitHub `main` belassen, aus Quell-ZIP ausschließen.

## Phase 3O/12 – MicCaptureArbiter-Reservierung (14.10.2026)

- Nutzer bestätigt GitHub TEST für Phase 3O/11 GRÜN; Teilstufe 12 ist noch **nicht** GitHub-getestet.
- `AnalyzerSetupMicrophoneReservation.withReservation` reserviert für einen synchronen Block exklusiv den bestehenden Mikrofon-Arbiter und gibt den Token per `use` auch bei Ausnahme frei. Konkurrenz mit Rolling und Direct/Scout AudioRecord wird für diesen Block vermieden.
- Drei neue dynamische JUnit-Regressionstests: Konkurrenz über Threads, Ablehnung bei bestehendem Owner, Ausnahmefreigabe. Noch nicht auf GitHub ausgeführt.
- **Nicht atomar für MON-/Service-/Recorder-Übergänge insgesamt**, nicht in produktiven Setup-Executor integriert; keine freigegebene Audio-/FFT-Umschaltung und `Plan.executable=false`. Keine Umkonfiguration/Recorderänderung.
- Gradle 0.5.55/213 und Termux v4 unverändert, Backup `supabase/storage_backup_before_v0526` weiterhin auf GitHub main, nicht in Projekt-ZIPs.

## Phase 3O/11 – Audio-Owner-Abweisung (2026-10-14)

- Nutzer bestätigt Phase 3O/10 GitHub TEST **GRÜN**. Phase 3O/11 GitHub TEST und Gerätetest offen.
- `AnalyzerSetupAudioOwnerGate.kt` liest ausschließlich `MicCaptureArbiter.owner`, `RollingAudioEngine.isRunning`, `AnalyzerDirectAudioService.isCapturing` und `AnalyzerMonitorRecording.isActive()`.
- `AnalyzerSetupSwitchTransaction.execute` verweigert den Vorgang bereits bei einem dieser aktiven Zustände; auch ein Fehler beim Abfragen führt zur Ablehnung. Keine Recorder-Start-/Stop-Aktion, kein Preference-/JSON-Schreiben.
- **Keine Atomarität:** Die getrennten Service-/Recorder-Übergänge verwenden bisher kein gemeinsames Transaktions-Lock. Ein scheinbar freier Zustand ist ausdrücklich **keine** Erlaubnis zur Übernahme. `Plan.executable=false`, keine Adapter- oder UI-Freigabe.
- Statische Regression `tools/verify_lfn056_phase3o_owner_gate.py` (9 Prüfungen); Kotlin CI und dynamische Rennen-/Gerätetests offen.
- Historisches Storage-Backup bleibt in GitHub `main`, nicht in der Quell-ZIP. Gradle `0.5.55/213` und Termux v4 unverändert.


## Phase 3O/10 – STOP-Konfiguration bei fehlgeschlagenem Session-Aufbau (14.10.2026)

- Nutzerbestätigung: **Phase 3O/9 GitHub TEST GRÜN**; Phase 3O/10 GitHub TEST noch offen.
- `LiveAnalyzerController.start()` erfasst vor dem Laden der Präferenzen einen gemeinsamen STOP-Checkpoint. Falls `LocalAnalyzerAudioBuffer.start()` oder `newSession()` einen Fehler auslösen, wird nach Best-effort-Cleanup die zuvor erfasste DSP-/Audio-/Timing-Konfiguration wiederhergestellt. Rücknahme findet nur unter Controller-Lock bei `running=false` statt, ohne `SharedPreferences`- oder JSON-Schreibzugriff. Fehler in der Rücknahme werden dem ursprünglichen Fehler als suppressed hinzugefügt.
- **Grenzen:** Kein allgemeiner Rollback bei Fehlern im früheren Pref-/Konfigurations- und Validierungsabschnitt; keine Recorder-/Service-/Hardware-Rücknahme und keine Freigabe der Setup-Umschaltung. Die getrennten Aufnahmeeigentümer (Scout, MON, Rolling) sind noch nicht unter einem gemeinsamen Lock verbunden.
- Statische Regression: `tools/verify_lfn056_phase3o_start_rollback.py`; Kotlin-/Android- und Fault-Injection-Test offen.
- GitHub `main` behält das historische `supabase/storage_backup_before_v0526`; Entwicklungs-ZIPs enthalten es nicht. Bei fehlenden Dateien in der ZIP **nicht löschen**. Termux v4, Gradle 0.5.55/213, Workflows und Cloud unverändert.

## v0.5.56 Entwicklung – Phase 3O, Teilstufe 8 (2026-10-14; GitHub TEST offen)
- Gemeinsamer atomarer STOP-Checkpoint für DSP und Audio/Timing und Stale-Prüfung.
- `CURRENT_STATE.md` aktualisiert. Historisches Recovery-Storage im separaten Archiv gesichert und aus Quell-ZIP ausgeklammert (nicht aus Git gelöscht).

## Phase 3O – Teilstufe 6 (Quellkandidat, 14.10.2026)
- STOP-gebundener `StoppedDspCheckpoint` mit capture/restore für FFT und (8-kHz) Filterbank; zunächst vollständige Validierung, dann Laufzeitzuweisung, ohne Preferences-Schreibzugriff.
- Keine Audio-/Recorder-Rücknahme, keine automatische Aktivierung, keine UI-Anbindung; Phase-3N-Plan bleibt gesperrt.
- Nutzer meldet GitHub TEST Teilstufe 5 GRÜN; Teilstufe 6 GitHub-/Gerätetest offen.

## Phase 3O – Teilstufe 5 (Entwicklungskandidat, 14.10.2026)
- `LiveAnalyzerController.start` akzeptiert optional `DspStartOverride`, um FFT und optional 8-kHz-Filterbank **nach** dem Preferences-Lesen speicherfrei für genau diesen Start zu übernehmen.
- Vollständigkeits-, Finite-, FFT- und Filterbank-Validierung unter dem Controller-Lock vor Überschreiben der DSP-Werte; keine Setup-UI und keine automatische Aktivierung.
- Die bestehende Standard-Startsignatur bleibt durch optionalen Parameter kompatibel. Recorder-/Audio-Modus-/Persistenz- und Rollback-Integration bleiben offen. Kein vollständiger Setupwechsel.
- Statische Quellregression ergänzt. GitHub TEST und Gerätetest Teilstufe 5 offen; Teilstufe 4 laut Nutzerbericht grün.

## v0.5.56 – Phase 3O, Teilstufe 4 (Quellkandidat, 14.10.2026)

- `LiveAnalyzerController.setDsp` und `setFilterConfig`: optional `persist=false` für STOP-begrenzte, speicherfreie Controller-Änderungen; `persist=true` als unveränderter Standard für Legacy-Aufrufer. Kein Anschluss an Setup-UI oder Transaktionsport.
- `start(context)` lädt weiter aus SharedPreferences; vollständige Audio-/FFT-Umschaltung, Recorder-Lock und Rollback bleiben gesperrt. Lesender Phase-3N-Plan weiterhin `executable=false`.
- Neuer statischer Regressionstest `tools/verify_lfn056_phase3o_draft_setters.py`; GitHub TEST für diese Teilstufe noch offen. Vorherige Teilstufe 3: GitHub TEST laut Nutzer GRÜN.
- Gradle `0.5.55` / `213` unverändert; Termux-v4-Skript nicht geändert.

## v0.5.55 / versionCode 213 (2026-10-13)

- MON-Dateiname: auf ganze Zahlen gerundete FP/FB/G-Parameter; numerische JSON-Werte auf zwei Nachkommastellen.
- MULTI-Beschriftung `...`; Untertaste 1 testet zwei Schlüsselpräfixe via lesenden Supabase-Endpunkt.
- MON setzt erweiterten Wasserfall zurück und zeigt MON-Status oben rechts im Wasserfall.
- Rückfrage beim Verlassen (Ja Hintergrund / Nein stoppen / Abbrechen); Default Nein.
- Einzelheiten und Grenzen: `ANLEITUNG_TEST_v0.5.55.md`.
- Kein Android-Buildnachweis für diese Quellversion.

## v0.5.54 (versionCode 212) – Adaptive Tasten, dynamisches Ⓛ, MON-Metadaten und Gainbar (2026-10-13)

- Acht Tasten passen ohne horizontales Scrollen in die tatsächliche Breite: maximale Breite 48 dp; Rest wird proportional pro Taste auf acht gleiche Breiten verteilt, 2-dp-Abstand; gleiche Tastenhöhe 48 dp. MULTI-Popup-Tasten erhalten exakt dieselben Außenmaße.
- Long-Press-Markierungen zustandsabhängig: Kamera nur bei gültigem Signal, REC nur bei aktivem Mithören ohne laufende MON-Aufnahme, MON nur bei aktivem Mithören, FILTER nur bei laufender ungepauster Messung mit Signal, NOISE nur bei ungepauster Messung ohne Referenzeinmessung; die Tastengesten prüfen dieselben Bedingungen. ORIGINAL/DIAG/MULTI ohne Ⓛ.
- Mithör-WAV-Dateiname enthält Aufnahme-Startzeit, MON-Filterposition, Filterbreite und Gain. Gleichnamige JSON-Datei enthält diese drei numerisch im `mon_audio`-Block samt zeitgestempelter Parameteränderungen (`changes`). Die MON-Abtastrate im WAV-Companion ist 48 kHz. Beide Dateien gehen bei freigegebener Cloud-Übertragung über den bestehenden Archivpfad.
- Gain-Leiste links am Spektrum: `+120 dB`, aktuelle Verstärkung und `−60 dB` in drei vertikalen Segmenten, alle Textlabels gegen den Uhrzeigersinn um 90° gedreht. Buchstabenhöhe wird automatisch auf die tatsächliche Balkenbreite begrenzt, Textlänge auf die Segmenthöhe.
- GitHub-/Windows-Buildkonfiguration und Originalicons unverändert. Testbeschreibung: `ANLEITUNG_TEST_v0.5.54.md`.
- Validierung: Kotlin-PSI-Syntax PASS; statischer Regressionstest 14/14 PASS; Android-Gesamtbuild mangels verfügbarer Gradle-Distribution noch offen.

## v0.5.53 (versionCode 211) – Tasten, MULTI und MON-WAV

- Untere Aktionsleiste: Kamera, REC, MON, Original-Wasserfall, Filter-Wasserfall, NOISE, DIAG, MULTI. Bestehende Piktogramme beibehalten, alle Haupttasten und die fünf MULTI-Tasten 48 × 48 dp.
- Einheitliche Rahmen für Kamera und NOISE; dezente eingekreiste L-Kennzeichnung für Kamera, REC, MON, Filter-Wasserfall und NOISE.
- MULTI öffnet ein temporäres, nicht-destruktives Popup mit fünf gleich großen vertikalen Tasten (1–5), ohne Funktion; Auswahl schließt die Leiste.
- REC Long Press bei aktivem MON: Aufzeichnung der tatsächlich an AudioTrack übergebenen 48-kHz-Mono-PCM-Samples nach Filter, SHIFT, Gain, Kompressor, Limiter und Fade; kurzer REC-Druck stoppt und archiviert lokal.
- REC kurzer Druck ohne laufende MON-Aufnahme bewahrt die bisherige Rolling-Buffer-Funktion.
- Setup-Option "MON-Aufnahme nur lokal speichern" ist standardmäßig aktiviert. Ist sie deaktiviert, wird nach der lokalen Speicherung über den bestehenden Analyzer-Cloud-Aufruf übertragen.
- Mithör-WAV speichert im bestehenden AudioArchive mit origin=MON und rollingBuffer=false, ohne den Rolling-Tageszähler zu verändern. Langlaufende Speicherung erfolgt außerhalb des Audio-Ausgabe-Threads.
- MAX-HOLD-Reset bleibt funktional erhalten, wurde wegen der festgelegten Acht-Tasten-Anordnung in die Einstellungen verlegt.

**Prüfgrenze:** Kotlin-PSI-Syntaxprüfung bestanden; Android-Gradle-Gesamtbuild und tatsächlicher Smartphone-Test noch ausstehend. Cloud-Upload nutzt den existierenden Uploadpfad; ein eigenständiger automatischer Retry-Test für MON steht noch aus.

## v0.5.52 (versionCode 210) – MON Touch-Latenz, +120 dB, Setup-Gruppen (2026-10-13)

- MON-Gain von −60 bis +120 dB erweitert, persistentes Gain und Fade weiterhin vorhanden. Anzeige-Skalierung und vertikale Swipe-Empfindlichkeit aktualisiert.
- AudioTrack.WRITE_BLOCKING aus der gemeinsamen Verarbeitungssperre herausgenommen; Bluetooth-Output blockiert dadurch nicht mehr die Audio-Konfiguration.
- Während Swipe keine SharedPreferences-Schreibvorgänge. Gain und Bandpassmitte beim Loslassen/Abbruch persistieren.
- Kompressor und Limiter im Setup durch eigene klar sichtbare Überschriften und gruppierte Parameter getrennt.
- Kompressor und Limiter erhalten einmal pro Audioblock ermittelte Bypass-Flags; bei ausgeschaltetem Zustand entfallen die rechenintensiven Hüllkurven-/Regelberechnungen. Digitaler Schutzclip bleibt.
- **Validierung:** Kotlin-Parser PASS; statische Regressionstests PASS; vollständiger Android-Build und Smartphone-/Bluetooth-Test noch ausstehend.

## v0.5.51 (versionCode 209) – MON Dynamics, Touch & Spectrum UI (2026-10-13)

- MON: Persistierender Gain wird per 1,5-s-Fade-in (im Setup 0,2–5 s) eingeblendet.
- MON: Einstellbarer Kompressor (Threshold/Ratio/Attack/Release/Knee/Makeup) und einstellbarer Limiter (Ceiling/Attack/Release), getrennt schaltbar, persistent.
- Ausgang bleibt mit separatem digitalen Sicherheitsclip von -6 dBFS versehen; dies garantiert **keine** sichere Kopfhörerlautstärke.
- DIAG meldet Ausgangspeak, Kompressorstatus, Limiterstatus, Limiter-Eingriffe und Gain Reduction.
- Spektrum: Gain-Bar mit links um 90° gedrehter Beschriftung im schmalen Balken links der dB-Achse; daneben schmaler Ausgangsmesser nach Dynamikbearbeitung, beide auf Spektrumhöhe begrenzt.
- Wasserfall-Touch bei MON: Kein Zoom; horizontale Geste Mittenfrequenz, vertikale Geste Gain; während Drag werden Spektralwerte aus einem Snapshot angezeigt, bis Touch endet. Filter- und Pegelanzeigen aktualisieren weiter.
- Keine Änderungen an Windows-Buildwegen und GitHub-Buildworkflow.
- Offline geprüft: Kotlin-Parser; Android-Build und Audio-Gerätetest ausstehend.

## v0.5.50 (versionCode 208) – Bluetooth-Mithören und Gain
- 48-kHz-Mono-AudioTrack-Ausgabe mit linearer Interpolation der bandbegrenzten Monitoring-PCM-Eingangsdaten.
- Mithör-Gain −60 bis +100 dB per vertikaler Wischgeste im Wasserfall und persistentem Setupwert. Sanftes Fade-in 1,5 s, digitaler Softlimiter mit −20 dBFS Spitzengrenze.
- DIAG: tatsächliche Ausgaberoute, Ausgabestatus, Input/Output-Rate, geschriebene PCM-Samples, Limiter-Zählung und Ausgabefehler.
- 440-Hz-Ausgangstest mit festem niedrigem Pegel, unabhängig von Mikrofon, Bandpass, SHIFT und Gain.
- FFT, WAV, Cloud, Windows-Build und GitHub-Workflow unverändert.
- Android-Build und Gerätetest stehen aus. Siehe ANLEITUNG_TEST_v0.5.50.md.

## v0.5.49 (versionCode 207) – AUDIO-REC, DIAG, Mithören, kompaktes Routing
- AUDIO-REC: roter Kreis; Buffersekunden / konfigurierter Clipdauer und lokal gespeicherte Aufnahmen / Tageslimit in der 48-dp-Taste. Nur Uhr bei gespeichert + ausstehender Cloud; Haken nach bestätigtem Upload derselben WAV. Archivstatus für noch offene Übertragung wird nachgelesen.
- DIAG: Rahmen, Größe und Rundung an untere Tasten angepasst. NOISE-Zustände bleiben vollständig innerhalb der eigenen Taste.
- MON: Kopfhörersymbol 50 % der Tastenbreite. Tippen für Mithören, langer Druck für additive Frequenzverschiebung. Fade-in, persistenter Pegel / Mittenfrequenz, steilflankiger separater Biquad-Bandpass, parametrierbare Bandbreite und SHIFT-Frequenz. Gesten im Wasserfall, Anzeige im Spektrum.
- Obere Kurzstatusanzeige LIVE/PAUSE/STOP zusammen mit tatsächlich von der PCM-Quelle gemeldetem Mikrofon (bei STOP nur gewählte Quelle mit Sternchen). Keine zusätzliche Statuszeile.
- Separate neue PCM-Monitoring-Subscription und AudioTrack; kein zweiter AudioRecord, keine Änderung der eigentlichen FFT/WAV-Verarbeitung oder Cloud-Schemata.
- Kotlin-PSI-Parser 5/5 PASS, lokale Kotlin-Policy-Prüfungen 7/7 PASS; Android/AudioTrack-Gerätetest **offen** (kein lokaler Gradle-8.13-Download möglich).
- Windows-/GitHub-Builddateien unverändert. Siehe ANLEITUNG_TEST_v0.5.49.md.

## v0.5.48 (versionCode 206) – Kompakte NOISE- und AUDIO-Tasten
- NOISE: Keine eigene Statusmeldung mehr neben der Taste oder in der globalen Statuszeile. AUS/MESS/1/f, Einmessfortschritt und Fehlerindikator erscheinen nur innerhalb der 52-dp-Taste.
- AUDIO: Feste 48 × 48 dp wie die übrigen unteren Aktionen, roter Aufnahmepunkt und vollständig interne zweizeilige Beschriftung. Status SICHERT/OK/FEHLER/BEREIT/WARTEN nur in der Taste; bisheriges Popup und externer Sekunden-/Tageszähler entfallen.
- Sonstige Analyzer-, Rauschreferenz-, Audioaufnahme- und Cloud-Funktionen bleiben unverändert.
- Windows-Build- und GitHub-Workflow-Konfigurationen unverändert.

## v0.5.47 – Diagnose gemeinsamer NOISE-MESS-/1/f-Ueberkompensation
- Original-Live-FFT-Leistung, Referenzleistung und Restleistung je 5/10/20/50/100 Hz als dB-Werte vor bzw. nach der Subtraktion dokumentiert.
- Separate Main-/Rolling-Diagnose, Anzahl momentan unterdrueckter Bins und Leistungs-Verhaeltnis Referenz zu Live.
- Einmal pro Sekunde aktualisierte Proben statt Protokollierung jedes FFT-Fensters; Anzeige und Zwischenablage im DIAG-Fenster.
- Vier reine Regressionstests fuer Leistung-zu-dB und Verhaeltnis-Sonderfaelle.
- Keine Aenderung von Subtraktionsformel, bestehendem Rauschprofil, 1/f-Cache, FAST, Cloud, Windows-Build oder GitHub-Signierung.
- versionCode 205; versionName 0.5.47. Voller Android-/Geraetetest noch ausstehend.

## v0.5.46 – 1/f-Cache und Laufzeitdiagnose
- Einheitliche Versionierung mit maximal zwei Punkten, Android versionCode 204.
- Wiederverwendung der berechneten 1/f-Rauschleistungsreferenz fuer gleiche Referenzarray-Identitaet, Abtastrate und FFT-Groesse; getrennte Caches fuer Main-FFT und Rolling-FFT.
- Automatische Cache-Invalidierung bei Referenzladen, Beginn einer Einmessung, Abschluss einer neuen Einmessung und Wechsel des Frequenzrasters.
- Im DIAG-Text neue Zaehler cache_hit/cache_miss je Pipeline, vorhandene PCM-Alter- und FFT-Zeitstempel bleiben getrennt sichtbar.
- Die eigentliche Fit-Formel sowie FAST, Cloud, Windows-Build und GitHub-REGULAR/TEST-Signierung bleiben unveraendert.
- Drei JUnit-Regressionen fuer Cache-Wiederverwendung, Invalidierung und unbrauchbare Referenz.
- Status: Kotlin-Cache-Quelldatei erfolgreich kompiliert; Android-Gradle/Github-Tests und Geraeteverhalten noch nicht verifiziert.

## v0.5.45.9.1 – GitHub-Regressionskorrektur
- `AnalyzerSignalValidity`: ein Original-FFT-Puffer mit NaN oder +/-Infinity wird konsequent als NO_INPUT erkannt. Finite Werte am technischen Anzeigeuntergrund -180 dBFS bleiben weiterhin gueltiger Eingang; unter NOISE-Kompensation BELOW_NOISE_FLOOR.
- Regression: Fehler `invalidNumbersDoNotCreateFakeInput` aus GitHub-Actions-Run 38291009441 gezielt adressiert; zusaetzliche Tests fuer gemischt finite/nichtfinite Originalbins und reinen Anzeigeuntergrund.
- Die 1/f-Instrumentierung aus v0.5.45.9 bleibt bestehen. Laufzeitmessungen auf Android ausstehend.
- versionCode 203 fuer regulaeres Update; Windows-Build, GitHub-REGULAR-Signierung, TEST-Paket und CI-JDK-Uebersteuerung unveraendert.
- Teststatus: Quellpruefung; Android-Gradle-Test und Geraetetest sind noch offen.

## v0.5.45.8 – 12.10.2026 – Android-GitHub-Build + Signalstatus-Regression (Quellkandidat)
- `AnalyzerSignalValidity`: gültige Original-FFT-Daten im Bereich der technischen Anzeigeuntergrenze sind kein Audioausfall. Nur leere/nichtfinite FFT-Puffer führen zu `NO_INPUT`; Quellenalter und Audioaufnahmefehler bleiben unabhängig zu prüfen.
- Regressionstests für wechselnden 1/f-Restpegel sowie durchgehend unter Rauschboden liegende FFT.
- Version `0.5.45.8` / versionCode 201.
- Handy-Publishing per Termux + GitHub CLI, privates Repo und existierender `.github/workflows/build-android.yml`; `ANLEITUNG_GITHUB_ANDROID_v0.5.45.8.md`.
- Zusätzliche `.gitignore`-Ausschlüsse für Backups/Geheimnisse.
- FAST, Cloud, Rauschmodell, FFT-Frequenzbänder und CSV-Filterbank-Einstellungen unverändert.
- Vollständiger GitHub-Actions-Build / Android-Geräteprüfung noch OFFEN.

## v0.5.45.7 – Statuskorrektur bei vollständig kompensierter Rauschleistung (Quellkandidat)
- Fehlerbehebung: `LiveAnalyzerController.accept()` klassifiziert FFT-Eingangsgültigkeit anhand der **unkompensierten** gain-korrigierten FFT-Werte, nicht der bereits NOISE-subtrahierten Werte.
- Ist der ursprüngliche FFT-Eingang vorhanden, aber die komplette Restleistung kompensiert, bleibt das Analyzer-Fenster gültig; Status "Unter Rauschboden / kein auflösbares LF-Nutzsignal · Eingang aktiv". Geschichte und Statistiken werden nicht allein deswegen abgebrochen.
- Bei tatsächlich fehlenden/nicht numerischen Original-FFT-Werten bleibt `NO_INPUT` und bisheriges Fehlerverhalten erhalten.
- Neue `AnalyzerSignalValidity`-Policy und fünf JUnit-Regressionen. Kotlin-Hostprüfung 5/5 PASS; Android-Build/ADB noch offen.
- Keine Änderungen an NOISE-MESS-/1/f-Fit, FAST, gespeicherten Referenzen, Cloud oder deaktivierten CSV-Filterbank-Einstellungen. Referenzabschluss weiterhin 1 kΩ, Fehlmessung −11,67 dB/Dekade ausgeschlossen.
- Testprozedur: `ANLEITUNG_TEST_v0.5.45.7.md`.

## v0.5.45.6 – NOISE AUS / MESSPROFIL / 1/f-MODELL (Quellkandidat)
- Abschlusswiderstand nach Benutzerkorrektur **1 kΩ**; erste Steigung −11,67 dB/Dekade verworfen; gültige Referenz −19,72 dB/Dekade.
- Modell `P_white+K/f^alpha` auf gespeicherte Referenz gefittet und nur 1–100 Hz verwendet; unveränderte Messreferenz außerhalb. Keine pauschale Signal-EQ.
- NOISE kurz schaltet zyklisch AUS → MESSPROFIL → 1/f-MODELL → AUS; Langdruck startet unverändert die zeitlich gemittelte Referenz-Einmessung.
- Persistenter Modus, rückwärtskompatible `enabled`-Einstellung, Tests für Modellverhalten, vorhandene FAST/CSV-Filter nicht verändert.
- Android-Gradle/ADB noch nicht in dieser Umgebung nachgewiesen. Exakte Prüfschritte: `ANLEITUNG_TEST_v0.5.45.6.md`.

## v0.5.45.5 – Diagnostik 1/f-Verdacht (Quellkandidat)
- Neuer DIAG-Dialog unmittelbar bei NOISE: fest-seedierter synthetischer Weißrauschtest über denselben LiveAnalyzerCore wie der Analyzer, 1000 Hz / 4096 / Hann, lineare Leistungsmittelung und lineare Regression des dB-Leistungsspektrums über log10(f) im Bereich 1–100 Hz. Toleranz ±2,5 dB/Dekade, kein physikalischer Kalibrationsnachweis.
- Gespeichertes Original-Rauschprofil: Steigung 1–100 Hz aus unkompensierten Profil-Power-Bins; Modell-Hinweis, keine gemessene Mikrofonsensitivität.
- Kein Eingriff in FAST, Roh-PCM/WAV, Cloud, Rolling-CSV, FFT-Produktivberechnung oder NOISE-Subtraktion.
- Grenze: DIAG zeigt die Referenz als **numerische Steigung**, nicht als zweite Spektralkurve; Original/Rest lassen sich über NOISE ein/aus vergleichen. Vollständige 3-Kurven-Überlagerung und automatische Roh-PCM-/Dezimationsvergleichsdiagnose sind noch offen.
- Teststatus: Kotlin-Hosttest ggf. separat dokumentiert; Gradle Android/JUnit/ADB ausstehend.

## v0.5.45.4
- Rauschboden: echte lineare Leistungssubtraktion ohne 1,25-Schwelle und ohne Original-Fallback; Nullrest intern linear 0; neuer Regressionstest.
- dB-Anzeige-Platzhalter -180 dBFS; nicht als Messwert interpretieren.

## v0.5.45.3 – Rauschboden 10-s-Power-Average, einstellbare Dauer, Nullsignal-Schutz (Quellkandidat)
- Einmessdauer im Analyzer unter Spektrum: 10/30/60/120/300/600 s, Standard 10 s, persistent.
- Langdruck auf NOISE misst weiterhin das zeitliche lineare Power Average aller gueltigen FFT-Fenster.
- Wenn gemessene Leistung nahe oder unter Referenzleistung liegt (<= 1,25 x Referenz), wird der Originalwert durchgereicht statt auf Null geclippt. Die Werte sind dann NICHT rauschkompensiert und muessen als nicht sicher trennbar interpretiert werden; hoher SNR wird weiterhin in linearer Leistung subtrahiert.
- Physikalischer Hinweis: Wird ein Nutzsignal bei der Kalibration mit eingemessen, wird es als Rauschen behandelt. Zeitliche Mittelung allein behebt dies nicht.
- FAST, Roh-WAV, Cloud und bisherige CSV-Filterbank unveraendert; Rolling-Halbstundenpfad bleibt FFT-basiert.
- Gradle / Android-Build und Geraetetests offen.

## v0.5.45.2 – Zeitliches FFT-Rauschboden-Power-Average
- Einmessung mittelt unkompensierte instantane FFT-Leistungen je Bin arithmetisch über alle gültigen Fenster.
- Ungültige FFT-Fenster werden vollständig verworfen, Zähler im Profil dokumentiert.
- Keine Änderung an FAST, PCM-WAV, Cloud oder stillgelegter CSV-Filterbank.
- Regressionstests NoisePowerAverageTest ergänzt; Android-Gradle/ADB noch offen.

## v0.5.45.1 – 12.10.2026 – Windows-Kotlin-Buildfix (Quellkandidat)
- Korrigiert die durch den Benutzer-Windows-Vortest v0.5.45 belegte Compilerfehlermeldung `Unresolved reference writeFilterPerformanceWindow` in `RollingAudioService.kt:309`.
- Entfernt ausschließlich den außerhalb seines Gültigkeitsbereichs stehenden Aufruf im äußeren `finally`-Block; die frühere CSV-Filterbank und ihre deaktivierte GUI bleiben erhalten.
- versionName 0.5.45.1 / versionCode 194. Keine Änderung an FFT-/FAST-DSP, CSV-Feldern oder Cloud.
- Statische Regression für Referenz-/Scopefehler PASS; vollständiger Windows-Gradle/JUnit/APK/ADB-Vortest offen.

## v0.5.45 – 12.10.2026 – FFT-basierte Rolling-CSV-Statistik (Buildkandidat)
- Neue 1-kHz-Rolling-FFT für gemeinsame L-Werte und FFT-Banddaten; alte PCM-CSV-Filterbank nicht ausgeführt.
- Alte Filterbank GUI-Steuerungen sichtbar, aber deaktiviert; Einstellungen gespeichert.
- Rauschbodenprofil optional erweitert um separat eingemessene Rolling-FFT-Bandreferenz; FAST unverändert.
- **Offen:** Tatsächlich gemeinsamen 8-kHz-Analyzer-FFT-Strom verwenden, Android-Build/Tests. Siehe docs/project/NOISE_FLOOR_FFT_ROLLING_v0545.md.

## v0.5.44 – 12.10.2026 – CSV-Filter-Performance (Source Candidate)
- Nur Rolling-CSV-Filter: 60.000-Sample-Performancefenster mit Mittel-/Maximalzeit, Sekunden-Spitze, P50/P95/P99, Echtzeitanteil; gesonderte Download-CSV; Restfenster bei STOP.
- Regressionstests für Legacy/Butterworth/FFT sowie 10/20 Bänder ergänzt; Sample-Zähler und Reset abgesichert.
- Original-FFT/FAST und beide Wasserfälle bleiben unverändert. Keine Cloud-/Mess-CSV-Schemaänderung.
- Kotlin-Hostkompilierung und 6/6 synthetische Hostproben PASS; Android-Gradle/-ADB v0.5.44 noch offen.

## v0.5.43 (2026-10-12) – CSV-Filterauswahl und Architekturkorrektur


Die v0.5.42-Auswahl im FAST-/Analyzer-Filterwasserfall war eine Fehlzuordnung und ist **vollständig zurückgenommen**. Die betroffenen Analyzer-Quellen wurden bytegleich aus v0.5.41 wiederhergestellt: `LiveAnalyzerController`, `LiveAnalyzerSettingsPanel`, `AnalyzerInspectionExport`, `AnalyzerLegacySetupMigration`, `AnalyzerRawLfSession`, `FastFilterBank` sowie `FastFilterBankTest`. Der Original-FFT-Analyzer und **beide Analyzer-Wasserfälle** bleiben unverändert.

Die Wahl Legacy-IIR / Butterworth HP2+LP2 / FFT-Hann ist ausschließlich in `RollingFilterHalfHourStatistics.kt` wirksam, nicht im Analyzer, nicht in der WAV-eigenen Filter-CSV und nicht im Cloud-Pfad. Menü: `Audio / Statistik lokal` → Rolling-Filterstatistik aktivieren → CSV-Filterverfahren wählen → speichern → Rolling-Messung neu starten.

- Legacy-IIR: bisherige einpolige HP/LP-Kaskade für Rolling-Band-CSV; unveränderter Standard.
- Butterworth4: getrennte Butterworth-Hoch- und Tiefpässe 2. Ordnung (bei DC nur LP2), nicht kalibriert.
- FFT-Hann: 1024-Punkte Radix-2-FFT eines 1-s-Signalblocks bei 1 kHz mit Hann-Fenster, einseitig und auf Fensterenergie normalisiert; 1-s zeitliche Aggregation; **kein kontinuierlicher Filter**.
- Rolling-Filter: 0–100 Hz, 10 oder 20 Bänder als Standard; freies MIN/MAX im vorhandenen Bereich. Filterstatistik standardmäßig AUS.
- CSV-Spalten, Dateinamen, Epochengrenzen, Halbstundenlogik und Cloud unverändert. Die CSV-Datei enthält noch **keine explizite Methodenspalte**; zum objektiven Vergleich den verwendeten Setupwert / Debug-Log separat dokumentieren. Dies ist eine offene Verbesserungsaufgabe.
- Methoden unterscheiden sich im Pegel auch ohne Programmfehler. Keine automatische physikalische Vergleichbarkeit. Bandkanten/DC/Fensterleckage und transiente Signale gesondert kalibrieren.


## v0.5.42 (Quellkandidat)
- FAST Filtermethoden auswählbar bei STOP: Legacy-Biquad (Default), Butterworth HP2+LP2, FFT-Hann (4096/512). Persistenz und Setup-Slots. CPU-Performance-Messung pro Audioblock mit Echtzeitanteil. Keine Cloud-Schema- oder Uploadänderung.
- Kotlin-Hostprobe PASS; Android Gradle/Unit/APK/ADB OFFEN. Siehe `docs/project/FILTER_METHODS_RUNTIME_v0.5.42.md`.

## v0.5.41 / versionCode 188 – Offline-Filtervergleich (12.10.2026)

- Neues eigenständiges Python-3.11-Werkzeug `tools/filter_methods_comparison_v0541.py` für Legacy-IIR, Butterworth4 (kausale SOS) und Hann-FFT-Bandenergie, je 10/20 Bänder in 0–100 und 10–60 Hz.
- CSV/SVG, Bandmaxima, -3-dB-Bandbreiten, Übersprechmatrizen, Amplitudenlinearität, Sample-Impulsantworten, 1-s-Ein-/Ausschwingdiagnose, separate 120-s-Tieffrequenzsimulation.
- Für FFT nur Fensterlokalisierung statt unechter IIR-Impulsantwort; 0-Hz-Band korrekt als Tiefpass modelliert. Toleranzbasierte Offline-Self-Tests.
- **Keine Änderungen an Android-DSP, Rollingbuffer, CSV-Schema oder Cloud.** Android-Build/ADB v0.5.41 noch nicht nachgewiesen.

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

## v0.5.37 – Nachtrag: 24-h-Retention der Cloud-Outbox (Quellkandidat)
Bestätigte Datensätze verbleiben mit gespeichertem Zeitstempel als `SENT_PENDING_DELETE` bis mindestens 24 h nach Upload; keine erneute reguläre Übertragung. Die Warteschlange ist nicht in der GUI sichtbar; Diagnose-Log `ROLLING_CLOUD_QUEUE_STATUS`. Windows-/Gerätetest offen.

## v0.5.37 / versionCode 184 – Cloud-Halbstunden-Gesamtstatistik (Source Candidate)

- Bestehende Edge Function `submit_half_hour_statistics` v1.0.0 ohne Schema-/SQL-Änderungen angebunden.
- Nur akustische Gesamtstatistik übermittelt: Leq/L50/L90, 30-Minuten-Zeitraum, 2–20-Hz-Konfiguration (oder eingestellter Bereich), Abdeckung, gültige Epochen und Qualitätsstatus.
- Stabil aus Teilnehmer-Fingerprint + Zeitfenster + Bereich gebildete UUID für idempotente Wiederholungen. Teilnehmer-Schlüssel niemals im persistenten Puffer.
- Dauerhafter Pending-Puffer in App-Dateien; erneuter Versuch bei Service-Start und jedem neuen fertigen Bucket; begrenzte Batchgröße; Schutz vor Weitergabe an einen anderen Teilnehmer-Schlüssel.
- Bewusst **nicht** übertragen: vier lokale Impulskennwerte, subjektive Bewertungsaggregate, Filterband- oder Sekundenstatistiken.
- Build/Unit-Tests und echter Backend-E2E-Test noch ausstehend.

## v0.5.37 / versionCode 184 – Cloud-Halbstunden-Gesamtstatistik (Source Candidate)

- Bestehende Edge Function `submit_half_hour_statistics` v1.0.0 ohne Schema-/SQL-Änderungen angebunden.
- Nur akustische Gesamtstatistik übermittelt: Leq/L50/L90, 30-Minuten-Zeitraum, 2–20-Hz-Konfiguration (oder eingestellter Bereich), Abdeckung, gültige Epochen und Qualitätsstatus.
- Stabil aus Teilnehmer-Fingerprint + Zeitfenster + Bereich gebildete UUID für idempotente Wiederholungen. Teilnehmer-Schlüssel niemals im persistenten Puffer.
- Dauerhafter Pending-Puffer in App-Dateien; erneuter Versuch bei Service-Start und jedem neuen fertigen Bucket; begrenzte Batchgröße; Schutz vor Weitergabe an einen anderen Teilnehmer-Schlüssel.
- Bewusst **nicht** übertragen: vier lokale Impulskennwerte, subjektive Bewertungsaggregate, Filterband- oder Sekundenstatistiken.
- Build/Unit-Tests und echter Backend-E2E-Test noch ausstehend.


## v0.5.36 – Regression Beobachtungs-Zeitstempel
# LFN-Scout v0.5.36 – Regression Bewertungskopplung

- `LocalHalfHourObservations.fromPayload` isoliert die Zeitstempelentscheidung und prüft ID, Wertebereich und ISO-Zeitstempel vor der Speicherung.
- Antwort 0–5 verwendet ausschließlich `responseTime`, automatische Nichtantwort -1 ausschließlich `scheduledTime`.
- Ungültige Zeitstempel und fehlende Antwortzeit werden mit Diagnosemeldung verworfen, statt stillschweigend einer falschen Halbstunde zugeordnet.
- Halbstundenaggregation bleibt im Intervall `[start, end)`; gleiche Ereignis-ID wird nur einmal berücksichtigt.
- Neue JVM-Regressionstests: Grenzzeitpunkte, Zeitverschiebungen, -1, Nullbewertung, Duplikate und ungültige Ereignisse.
- Persistenz ist weiterhin über Android SharedPreferences vorhanden, aber ein Neustart-/Prozessabbruch-Gerätetest ist **nicht** durch die reinen JVM-Tests ersetzt.
- Kein neues Cloud-Schema, keine Änderung an `submit_observation`, keine Übertragung aggregierter Bewertungen oder Impulskennwerte.
- Bestehende Filterbank- und Sekundenwertberechnung unverändert.

## v0.5.35 – lokale Bewertungsstatistik (Source Candidate)
- Neuer lokaler, begrenzter Ereignisindex ohne Zugangsdaten, Standort oder Kommentar. Manuelle Bewertungen und automatisch erfasste Nichtantworten werden auf Ereignis-ID-Basis persistiert.
- Halbstunden-Gesamt-CSV enthaelt observation_mean/count/min/max/unavailable_count. Keine vorhandenen Cloud-APIs oder Datenbankschemas veraendert.
- JVM-Regressionstests fuer Zeitgrenzen, 0/-1, Deduplikation und leeres Fenster hinzugefuegt; Ausfuehrung und Android-Geraetetest ausstehend.

## Dokumentationsnachtrag zu v0.5.34 – ohne neue App-Version

- `docs/project/DEFERRED_TASKS.md` als zentrale Liste offener und zurueckgestellter Aufgaben hinzugefuegt.
- Pflege bei jeder neuen Version in `DIRECTIVES.md` verbindlich festgelegt und in `docs/project/README.md` verlinkt.
- Keine Kotlin-/App-/Cloud-Aenderung, kein erneuter Build oder Geraetetest beansprucht.

## v0.5.34 / versionCode 181 – Impulsdiagnose (Quellkandidat, 2026-10-11)
- Rollingbuffer-Halbstunden-Gesamt-CSV erhält vier neue optionale Spalten: `peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`.
- Grundlage sind ausschließlich die bereits ermittelten gültigen PRE-GAIN-1-s-Epochen. Keine zusätzlichen Audiofilter oder sekündlichen Flash-Schreibvorgänge.
- Top-1/Top-10 sind Energieanteile, keine automatische Klassifizierung als externe Impulse. Die Filter bleiben unkalibriert.
- Neue Unit-Tests für konstante Pegel, Einzelspitze und Halbstunden-Zuordnung ergänzt. **Android-Gradle-/Gerätetests dieser Version offen.**
- Subjektive Bewertungen sind weiterhin **nicht** in die Halbstunden-Gesamt-CSV integriert: Die lokale, vollständig belastbare Bewertungsquelle muss zuerst nachgewiesen/angebunden werden. Keine Cloud-Änderung.
- GUI-Facelift, SD-Ziele und Cloud-Halbstunden-Upload bleiben zurückgestellt/offen.



## v0.5.33 (2026-10-11) – Source Candidate
- versionCode 180; zusätzliche separat abschaltbare 1-s-Epoch-CSV aus Rollingbuffer-Halbstundenstatistik; default AUS.
- 300-Epochen-RAM-Snapshots, MediaStore Download/LFN-Scout, Gesamt + 10/20 optionale Filterspalten, PRE-GAIN.
- Sichtbare Cloud-Bezeichnung statt Supabase im lokalen Statistikdialog; interne Serverkennung unangetastet.
- Keine Behauptung einer fertigen Beobachtungsaggregation: ist weiterhin offen.
- Windows-Gradle-/ADB- und Xiaomi-Liveprüfung noch ausstehend.


## v0.5.32 / versionCode 179 — Filter-Diagnose-Regression
- Neue reproduzierbare Filterbank-JUnit-Tests für 3/7/15/35/75/95 Hz, Amplitudenskalierung, Nulldaten, Epochenvalidierung und 20-Band-Grenzen.
- Bestehender Audio-DSP, CSV-Export und UI bleiben funktional unverändert.
- Dies ist ein Testkandidat, **keine** Filterkalibrierung und **kein** Nachweis erfolgreicher Android-Tests.
- Noch offen: Sekunden-CSV, Beobachtungsintegration, SD-Zielauswahl, Cloud-Halbstunden-Upload und messgenaue Filtervalidierung.

## v0.5.31 / versionCode 178
- Optionale kontinuierliche 10-/20-Band-Filterstatistik aus Rollingbuffer-Pre-Gain-PCM, Standard AUS.
- Eigene Frequenzgrenzen Default 0–100 Hz, lokale CSV, eigene GUI-Einstellungen.
- Drei gezielte Unit-Tests ergänzt. Build und Gerätetest stehen aus.

## v0.5.30 / versionCode 177 – Source Candidate (11.10.2026)

- Rolling-Halbstunden-Gesamtstatistik verarbeitet den decimierten PCM-Wert **vor** digitaler Verstärkung und vor Rollingbuffer-Clipping (keine künstliche Pegelanhebung um den eingestellten Gain).
- Neue persistente Einstellung „Halbstunden-Gesamtwerte als CSV (Downloads)“, Standard EIN.
- Abgeschlossene Halbstunden werden optional außerhalb der Audio-Schleife über MediaStore als einzelne, öffentlich zugängliche CSV-Dateien `Download/LFN-Scout/HalfHourStatistics_<bucket_start_ms>.csv` abgelegt.
- Auch beim geordneten Capture-Ende wird ein PARTIAL-Bucket abgeschlossen. Dateiname pro Bucket stabil; vorhandene Datei wird statt zusätzlicher Datei überschrieben.
- Keine sekündlichen Flash-Schreibvorgänge; diagnostische Kennzeichnung `PRE_GAIN_PCM_DIAGNOSTIC`.
- Noch **nicht implementiert**: subjektiver Beobachtungsmittelwert in CSV; kontinuierliche 10/20-Filterbank, Sekunden-CSV, SD-Ordnerauswahl, Cloud-Halbstunden-Upload und 5-minütige Recovery-Checkpoints.
- Android-Build/Tests/Gerätetest v0.5.30 noch nicht ausgeführt. v0.5.29 Build/ADB PASS laut hochgeladenem Nutzerprotokoll.

## v0.5.29 – Rolling-Halbstunden-Kern (Source Candidate)

- Optionaler RAM-Statistikpfad direkt aus Rolling-PCM (ohne zweiten AudioRecord), 1-s-Epochen und feste :00/:30-Buckets.
- Einstellbare Overall-Grenzen Default 2–20 Hz, globale Aktivierung Default EIN.
- Einfache vorläufige HP/LP-Filterkette; nur Diagnose-Logging der Resultate, noch keine CSV/Cloud/Persistenz.
- Details und Einschränkungen: `docs/project/ROLLING_HALF_HOUR_v0.5.29.md`.
- Gradle-/Android-/Gerätetest dieser Version: noch nicht nachgewiesen.

## v0.5.28 – optionaler WAV-Filter-CSV-Export (Source Candidate)

- Persistente CSV-Export-Checkbox, Standard AUS, für WAV-bezogene Filterstatistiken.
- Pro WAV eigene Datei unter `Download/LFN-Scout/`; Observer und Analyzer.
- Keine Änderung an Rollingbuffer, Cloud-Schema oder WAV-/Statistikberechnung.
- Tests/Android-Build für diesen neuen Stand noch nicht ausgeführt.

## v0.5.27 – Cloud-Outbox Fortsetzung (Source Candidate)

- Observer-Sidecar-Statistik in dieselbe Statistik-API/Outbox eingebunden.
- Persistente separate Statistiksets mit erneuter Übertragung nach erfolglosen HTTP-POSTs; keine gespeicherten Observer-Klartextkeys.
- Offene Punkte und Teststatus unter `docs/project/ANDROID_CLOUD_V0.5.27.md`.

## v0.5.27 – 10.10.2026 (Source Candidate)

- WAV-Metadaten `capture_origin`, `trigger_type` für manuelle Pfade.
- Analyzer-Overall-/Filterstatistik nach WAV-Erfolg an neuen Endpoint, lokale 0-basierte Indizes nach 1-basierter API abgebildet.
- Status `Cloud ok`; bekannte fehlende Retry-/Half-hour-/Observer-Anbindung in `docs/project/ANDROID_CLOUD_V0.5.27.md`.
- Gradle-/APK-/Gerätetest: NOT RUN / nicht nachgewiesen.

# 0.5.26 – 2026-10-08 / Backend-Abschluss 2026-10-10

- Additive Supabase-Statistikarchitektur ergänzt; bestehende Legacy-Tabellen und Requests bleiben erhalten.
- Hauptmigration `20261008_add_measurement_statistics_v0526.sql`: Audio-Geolocation/Origin/Trigger, `parameter_lookup`, `measurement_parameter_sets`, `half_hour_statistics`.
- Seed-Migration `20261010_seed_parameter_lookup_v0526.sql`: 64 Lookup-Slots (32 Overall, 32 Filter).
- Korrekturmigration `20261010163000_fix_half_hour_bucket_id_default_v0526.sql`: `half_hour_statistics.bucket_id` erhält `gen_random_uuid()` als Default.
- Korrekturmigration `20261010174500_normalize_parameter_lookup_semantic_keys_v0526.sql`: Lookup-Semantik auf die kanonischen DB/API-Namen vereinheitlicht (`lmin_dbfs`, `lmax_dbfs`, `mean_level_dbfs`, `level_stddev_db`, `crest_factor_db`, `kurtosis_excess`, `valid_epochs`).
- Etablierte Statistikfelder bleiben explizit benannt; `P01...P32` bilden einen zusätzlichen versionierten Parameterlayer.
- Audio-Statistik unterstützt eine Overall-Zeile plus 0..40 Filterzeilen je `recording_id`.
- Neue unabhängige 30-Minuten-Zeitreihe für Leq/L50/L90 mit :00/:30-Raster, Frequenzbereich, Coverage, Qualität, Geolocation und Kommentar.
- `upload_wav` Backend v1.5.0 deployed: optionale Live-Geolocation, `capture_origin`, `trigger_type`, `trigger_metadata`; fehlende neue Felder bleiben Legacy-kompatibel.
- Neue Edge Functions `submit_audio_statistics` v1.0.0 und `submit_half_hour_statistics` v1.0.0 deployed.
- Remote-DB/Edge-Function-Validierung am 10.10.2026: zentrale DB-Constraints, Overall/Filter-Grenzen, HTTP-POSTs, Idempotenz und 30-Minuten-Negativtest PASS; alter WAV-Upload gegen neues Backend PASS.
- Pre-/Post-Migrations-Dumps vorhanden; Restore in ein komplett neues leeres Supabase-Projekt bleibt zurückgestellt.
- Android sendet neue Statistikdaten und `capture_origin`/`trigger_type` noch nicht produktiv. Folgeversion soll die Android-Anbindung aktivieren und die Analyzer-Meldung `Supabase ok` in `Cloud ok` ändern.
- Android-Gradle-Build `:app:testDebugUnitTest`/`assembleDebug`, APK/ADB und Gerätetest v0.5.26 sind im aktuellen Übergabestand weiterhin **nicht nachgewiesen**.


# LFN-Scout – Changelog

## 0.5.25 – 2026-09-25

- Analyzer-Statistikbox oben rechts zeigt nun kompakt drei dynamische Bandwerte im vereinbarten Format `Leq(min-max)`, `L50(min-max)`, `L90(min-max)`; z. B. `Leq(2-25)`. Die separate Überschriftszeile entfällt, damit die Box trotz drittem Wert praktisch nicht höher wird. Rahmen und komplette sensitive Reset-Fläche folgen automatisch der kompakten Boxgröße.
- Live-Analyzer berechnet `Leq` unbewertet über den vollständig gefüllten konfigurierten Statistikzeitraum aus der linearen Bandenergie; Default bleibt 2–25 Hz / 120 s, beides weiterhin einstellbar. L50/L90 behalten ihren bestehenden Percentilpfad.
- Manueller Analyzer-Screendump: Kommentar wird unten rechtsbündig direkt in den eingefrorenen Wasserfall geschrieben. Der bisherige schwarze/transparente Hintergrundkasten entfällt; nur ein kleiner Textschatten dient der Lesbarkeit.
- Keine Änderung an Audio-Save-/Supabase-/24-h-Retention-Logik.
- VersionName 0.5.25 / VersionCode 172.

**Prüfstatus bei Erstellung:** Source Candidate. Statische/Host-/Paketprüfungen PASS; echter Android-Gradle-Build und ADB/Xiaomi-Test noch NOT RUN.

## 0.5.24 – 2026-09-25

- Reine Compiler-Reparatur von v0.5.23; keine neue Laufzeitfunktion.
- Fehlenden Material3-Import `OutlinedTextField` in `LiveAnalyzerScreen.kt` ergänzt.
- Nullable-WAV-Handling im Analyzer-Supabase-Pfad in `MainActivity.kt` korrigiert (`ByteArray?` wird vor Upload explizit auf `null`/leer geprüft und danach smart-gecastet).
- Ursache waren vier reale Kotlin-Compilerfehler aus dem Windows-Vortest v0.5.23.
- Versionsregel verschärft: Quell-/Syntaxprüfungen allein gelten nicht als Android-Buildnachweis. Erst erfolgreicher `:app:testDebugUnitTest` plus `assembleDebug` bestätigt eine Build-Version.
- VersionName 0.5.24 / VersionCode 171.

# v0.5.23 – 25.09.2026 – Analyzer-Audio in gemeinsamen Archivpfad + Bildkommentar

- Analyzer-`SAVE AUDIO` nutzt jetzt die gleiche WAV-Dateinamens-Nomenklatur wie das Beobachterpanel (`AudioFileNamingPolicy`). Da der gespeicherte Analyzer-Puffer PRE-GAIN ist, wird im Dateinamen korrekt `G0` verwendet; der Analyzer-Anzeigegain bleibt im Statistik-JSON dokumentiert.
- Analyzer-WAV, Statistik-JSON und Manifest werden direkt in den bestehenden Hauptpfad `Download/LFN-Scout/` geschrieben. Es wird ausdrücklich **kein** neues `audio/`- oder `LocalSaves/`-Unterverzeichnis für Analyzer-Saves erzeugt.
- Analyzer-WAV wird in `AudioArchive` registriert und über denselben `SupabaseClient.uploadWav`-Pfad wie Beobachter-Audio hochgeladen. Nach erfolgreichem Upload greift dieselbe 24-h-Retention. Statistik-JSON/Manifest bleiben lokal; ein Statistik-Supabase-Schema ist weiterhin nicht eingerichtet.
- `AudioArchive` kennzeichnet die Herkunft als `origin=ANALYZER|OBSERVER`; Herkunft wird nicht durch einen Sonderpfad kodiert. Zugehörige Analyzer-JSONs werden beim 24-h-Cleanup zusammen mit WAV und Archivmetadaten entfernt.
- Manuelles Analyzer-Bild (Kamera-Langdruck): aktueller Analyzer-/Wasserfallzustand wird vor Öffnen des Kommentardialogs eingefroren. Optionaler Kommentar wird vor dem eigentlichen Dateischreiben unten als dunkles Overlay direkt in den Wasserfall gerendert. `Ohne Kommentar` und `Abbrechen` bleiben möglich.
- Keine Supabase-Schemaänderung. BASE R1 unverändert.

**Prüfstatus:** statische Quell-/Paketprüfung vorgesehen; realer Windows-Gradle-/ADB-/Xiaomi-Test v0.5.23 bei Erstellung noch NOT RUN.

---

# v0.5.22 – 25.09.2026 – CMD-Syntaxfehler im VORTEST-Start behoben

- Reale Windows-Ausführung von v0.5.21 brach sofort in `VORTEST.bat` ab: Die inline in `for /f (...)` eingebettete PowerShell-RegEx zur Ermittlung von `versionName` enthielt CMD-relevante Zeichen/Quotes und wurde von `cmd.exe` bereits vor dem PowerShell-Start syntaktisch zerlegt.
- Die Versionsauswertung wurde deshalb vollständig aus den BAT-Wrappern entfernt. `VORTEST.bat` und `PREPARE_PROJECT.bat` zeigen nur noch versionsneutrale Kopfzeilen und enthalten keine Inline-PowerShell-Ausdrücke mehr.
- Die tatsächliche Projektversion bleibt zentral in `app/build.gradle.kts`. `tools/run-vortest.ps1` und `tools/package-active.ps1` lesen sie dort direkt und erzeugen weiterhin korrekte versionierte Log-, APK- und ACTIVE-Dateinamen.
- Keine Änderung an Android-Laufzeit, Analyzer, Audio, Statistik, UI oder Supabase gegenüber v0.5.21/v0.5.20. BASE R1 unverändert.

**Prüfstatus v0.5.22:** statische BAT-/Versionskopplungsprüfung PASS; ACTIVE-Paketintegrität PASS. Reale Windows-Ausführung `VORTEST_INSTALL.bat`, ADB und Xiaomi-Installation v0.5.22 noch NOT RUN.

---

# v0.5.21 – 25.09.2026 – Vortest-Versionserkennung und ACTIVE-Paketprüfung korrigiert

- Reale Windows-Ausführung von v0.5.20 zeigte: PREPARE, Unit-Tests und `assembleDebug` waren PASS; APK wurde erfolgreich erzeugt.
- Fehlerursache für Fehlercode 51: `tools/run-vortest.ps1` und `VORTEST.bat` waren noch auf `0.5.19` fest verdrahtet, während `tools/package-active.ps1` korrekt `versionName=0.5.20` aus `app/build.gradle.kts` las. Dadurch wurde `LFN-Scout_ACTIVE_v0.5.20.zip` erzeugt, die anschließende Prüfung suchte aber fälschlich nach `LFN-Scout_ACTIVE_v0.5.19.zip(.sha256)`.
- `run-vortest.ps1`, `VORTEST.bat` und der PREPARE-Kopf lesen die Version nun aus `app/build.gradle.kts`; keine manuelle Versionspflege dieser Skripte mehr nötig.
- APK-, Log- und ACTIVE-Dateinamen verwenden dieselbe ermittelte Version. Dadurch kann derselbe Versionsversatz künftig nicht erneut entstehen.
- Keine Änderung an Android-Laufzeit, Analyzer, Audio, Statistik, UI oder Supabase gegenüber v0.5.20. BASE R1 unverändert.
- Reale v0.5.20-Nutzerprobe bestätigt Android-Buildfähigkeit: Unit-Tests PASS und `assembleDebug` PASS. ADB-Installation wurde wegen des nachgelagerten Packaging-Checks noch nicht erreicht.

**Prüfstatus v0.5.21:** Quell-/Skriptprüfung und Paketintegrität PASS. Reale Windows-Ausführung `VORTEST_INSTALL.bat` und Xiaomi-Installation dieser Korrektur noch NOT RUN.

---

# v0.5.20 – 25.09.2026 – Wasserfall-Regressionsfix, gemeinsamer Audiokommentar und kompaktere UI

- Regression aus v0.5.19 behoben: Der in Zusatzfunktionen erzeugte WAV-Wasserfall darf gain-korrigierte FFT-Bins unter -120 dBFS nicht mehr vor der Farbumrechnung auf -120 dBFS zusammenquetschen. Numerischer Sicherheitsbereich erweitert; erst die Farbnormierung begrenzt auf 0…1. L50/L90 bleiben auf dem gemeinsamen Statistikpfad.
- Wasserfall-Kopfzeile kennzeichnet die Pegelskala jetzt korrekt als `automatisch` oder `manuell`.
- Analyzer `SAVE AUDIO`: kompletter Audioausschnitt wird zuerst eingefroren/kopiert, erst danach öffnet derselbe Kommentar-Dialog wie im Beobachter-Workflow. Damit können Tastatur-/Tippgeräusche nicht mehr in den ausgewählten Clip gelangen.
- Analyzer-Kommentar verwendet dieselben konfigurierten Audio-Timeouts wie der Beobachter und wird im lokalen Statistik-JSON sowie Manifest gespeichert.
- Zusatzinfo neben `SAVE AUDIO` in der unteren Actionleiste ist zweizeilig, damit weniger horizontaler Platz belegt wird.
- Beobachterpanel: separater Audio-/Statistik-Setup-Button entfernt; dieselbe lokale SAVE-Konfiguration liegt nun unter Einstellungen → Audioaufnahme.
- Beobachterpanel: `ENTWICKLERMODUS` belegt keine eigene Zeile mehr, sondern steht bei aktivem Modus oben rechts auf Höhe von `LFN-Scout`.
- Zusatzfunktionen: `Highspeed-Impulsdiagnose (Test)` bleibt Entwicklerfunktion und steht, wenn sichtbar, ganz unten.
- Android-System-Zurück (Taste/Geste) verwendet jetzt einen Panel-Verlauf. Im Analyzer bleiben dessen interner Einstellungen-Zurückweg und die bestehende RUN/STOP-Semantik erhalten.
- ADB-Warteschleife aus v0.5.19 bleibt unverändert. Keine Supabase-Schemaänderung; BASE R1 unverändert.

**Prüfstatus:** statische v0.5.20-Lieferprüfung **24/24 PASS**; Host-Regressionscheck für die Wasserfall-Farbwerte **PASS**; Host-Statistiktest **PASS** (40 Bänder, +50-dB-Kompensation, L90/Leq, 10-Hz-Dominanz); Kotlin-PSI **92 Dateien / 0 Syntaxfehler PASS**. Ein direkter Gradle-Aufruf in dieser Arbeitsumgebung konnte nicht starten, weil `gradle-wrapper.jar` hier nicht vorhanden ist (`GradleWrapperMain` fehlt). Windows-`VORTEST_INSTALL.bat`, Android-/Compose-Build und Xiaomi-Funktionstest v0.5.20 daher weiterhin **NOT RUN**.

---

# v0.5.19 – 25.09.2026 – Analyzer-Save-UI, Gain-Konsistenz und ADB-Warteschleife

- `SAVE-Setup` fuer lokale Analyzer-Audio-/Statistik-Saves nach Einstellungen → Statistik verschoben.
- `SAVE AUDIO` als 48-dp-Actionbutton unten in der Analyzer-Actionleiste; Audio-Buffer-/Quoteninfo steht direkt daneben.
- Erfolgsinfo `Lokal gespeichert` erscheint 3 Sekunden als Overlay nahe dem Actionbutton; keine spontane Statuszeile im Display.
- Zentrale `GainReferencePolicy`: tatsaechlich im PCM enthaltener digitaler Gain wird bei Pegel-/Spektralwerten exakt einmal kompensiert. +50 dB Gain bedeutet -50 dB Rueckreferenzierung.
- Analyzer-Save-Puffer ist PRE-GAIN und wird deshalb nicht nochmals um den Analyzer-Gain korrigiert.
- WAV-L50/L90 fuer 1-kHz-Ganzsekundenaufnahmen verwenden denselben Statistikpfad wie die lokale JSON-Statistik; Regressionstest auf identische Ergebnisse aufgenommen.
- `VORTEST_INSTALL.bat` wartet nach erfolgreichem Vortest/Paketieren auf genau ein autorisiertes ADB-Geraet, pollt alle 2 s und installiert erst dann.
- Bekannte Kotlin-Warnung in `LocalAnalyzerAudioBuffer.kt` (Expression-Body-Return) sowie unnoetiges `!!` in `ReminderSettingsStore.kt` bereinigt.
- Keine Supabase-Schema-/Uploadaenderung; BASE R1 unveraendert.

---

# v0.5.18.4 – 25.09.2026 – Live-Bildschirmausgabe und ADB-Diagnose im Vortest

- Alle Meldungen nativer Vortestschritte werden nun zeilenweise live auf dem Bildschirm ausgegeben und gleichzeitig in das Log geschrieben; STDOUT und STDERR werden beide angezeigt.
- `tools/run-vortest.ps1` liest die Prozessausgaben asynchron zeilenweise statt sie bis zum Prozessende zu puffern. Der echte Exitcode bleibt alleinige PASS/FAIL-Entscheidung.
- Auch APK-Pfad, Groesse, SHA-256, ACTIVE-ZIP-Daten, ADB-Pfad, Anzahl freigegebener/nicht autorisierter/offline Geraete und Installationsausgabe erscheinen auf dem Screen.
- Immer sichtbare Schlusszusammenfassung fuer PREPARE, Unit-Tests, assembleDebug, APK, ACTIVE-ZIP, ADB-Geraet und Installation.
- Bei 0 bzw. nicht genau einem freigegebenen ADB-Geraet erklaert der Vortest die noetigen USB-/USB-Debugging-Schritte vor Fehlercode 62.
- Reale v0.5.18.3-Nutzerprobe: PREPARE, Unit-Tests, assembleDebug und ACTIVE-Paket PASS; ADB-Installation nicht ausgefuehrt, da kein Geraet in `adb devices` erschien.
- Keine beabsichtigte Android-Laufzeitanderung; ACTIVE/BASE-Aufteilung unveraendert, BASE R1 weiterverwenden.

---

# v0.5.18.3 – 25.09.2026 – Korrektur NativeCommandError im Windows-Vortest

- Reale Nutzerprobe von v0.5.18.2 zeigte: JDK 17 wurde korrekt gefunden und `java -version` lief, dennoch brach PowerShell wegen `NativeCommandError` ab.
- Ursache: Windows PowerShell 5.1 kann reguläre native STDERR-Ausgabe bei `2>&1` zusammen mit `$ErrorActionPreference = 'Stop'` als Fehler behandeln.
- `tools/run-vortest.ps1` startet native Prozesse nun über `System.Diagnostics.Process`, erfasst STDOUT/STDERR getrennt und wertet ausschließlich den echten Prozess-Exitcode als PASS/FAIL aus.
- Korrektur gilt für PREPARE, Gradle, ACTIVE-Paketierung und ADB; ADB-Schutz auf genau ein autorisiertes Gerät bleibt bestehen.
- ACTIVE/BASE-Aufteilung unverändert; BASE R1 wird weiterverwendet.
- Keine beabsichtigte Android-Laufzeitänderung. Windows-/Gradle-/ADB-/Xiaomi-Test dieser Korrektur zunächst NOT RUN.

---

# v0.5.18.2 – 25.09.2026 – ACTIVE/BASE-Projektteilung und optionaler ADB-Install

- Zwischenversion auf Basis v0.5.18.1; keine beabsichtigte Änderung an Audio-, Statistik-, Analyzer-, UI- oder Supabase-Laufzeitfunktion.
- Erfolgreicher `VORTEST.bat` erzeugt nun zusätzlich automatisch `Builds/LFN-Scout_ACTIVE_v0.5.18.2.zip`, verifiziert es durch vollständiges Entpacken und schreibt SHA-256.
- Neue zweiteilige Projektübergabe: ACTIVE enthält buildfähigen Quellcode/Tests/aktuelle Textdokumentation; `LFN-Scout_BASE_ASSETS_R1.zip` enthält große, selten geänderte Referenz-/Rekonstruktions-/Storage-Recovery-Artefakte. `BASE_PACKAGE_REQUIRED.txt` bindet Revision und SHA-256.
- `VORTEST_INSTALL.bat` bzw. `VORTEST.bat --install` installiert die gebaute APK nach vollständigem PASS per `adb install -r`, aber nur bei genau einem autorisierten ADB-Gerät. Kein automatisches Deinstallieren und kein Push-only-Standardweg.
- Maschinenlokale/generierte Inhalte (`local.properties`, lokale JDK-Pfade, `.tools`, `.gradle`, `Builds`, `**/build`, APK/AAB/Logs) werden aus dem ACTIVE-ZIP ausgeschlossen.
- Neuer Test-/Statusstand: `BUILD_VERIFICATION_v0.5.18.2.md`, `docs/project/TESTPLAN_v0.5.18.2.md`, `docs/project/PROJECT_PACKAGING_v0.5.18.2.md`. Windows-/Gradle-/ADB-/Xiaomi-Lauf in der Erstellungsumgebung weiterhin **NOT RUN**.

---
# v0.5.18.1 – 25.09.2026 – automatisierter Windows-Vortest

- Zwischenversion auf Basis v0.5.18; keine beabsichtigte Änderung an Audio-, Statistik-, Analyzer-, UI- oder Supabase-Laufzeitfunktion.
- Neue `VORTEST.bat`: ein Aufruf führt nacheinander `PREPARE_PROJECT.bat --no-pause`, `gradlew.bat :app:testDebugUnitTest --stacktrace` und nur bei Erfolg `gradlew.bat assembleDebug --stacktrace` aus.
- `PREPARE_PROJECT.bat` unterstützt zusätzlich `--no-pause` bzw. `/nopause`; der bisherige Doppelklickbetrieb bleibt interaktiv mit Pause erhalten.
- `tools/run-vortest.ps1` schreibt ein zeitgestempeltes vollständiges Log unter `Builds/`, erzeugt `Builds/LAST_VORTEST_RESULT.txt`, prüft die Debug-APK und kopiert sie bei PASS als `Builds/LFN-Scout_v0.5.18.1-debug.apk`.
- Fehlercodes: 10 PREPARE, 20 Unit-Tests, 30 assembleDebug, 40 APK fehlt. Nach einem Fehler wird nicht mit dem nächsten Buildschritt fortgefahren.
- Statischer Automationsvertrag `python tools/verify_v05181_vortest.py`: 12/12 PASS. Tatsächlicher Windows-PowerShell-/Gradle-Lauf in der Erstellungsumgebung **NOT RUN**; Android-/Xiaomi-Abnahme von v0.5.18-Funktionen bleibt separat erforderlich.

---
# v0.5.18 – 25.09.2026 – lokaler Audio-/Statistik-Save (Quellkandidat)

- Hauptnavigation: eigenständiger Analyzer-Button mit `activity`; danach `blocks` Zusatzfunktionen, `settings` Einstellungen und `waves` Seismik. Vorhandene Portrait-Vorgabe bleibt bestehen.
- Gemeinsames SAVE-Setup im Beobachter/Analyzer: 30–600 s (Default 120 s), Gesamtstatistik 1–200 Hz konfigurierbar (Default 2–25 Hz), WAV standardmäßig 1 kHz/16 Bit/mono.
- Analyzer-Save nur bei vollständigem 1-kHz-Audiofenster, RUN oder beim PAUSE eingefrorenem Fenster und freier gemeinsamer Tagesquote. Kein zweiter AudioRecord; lokale WAV/JSON/Manifest-Dateien inkl. SHA-256.
- Beobachter erstellt zusätzlich lokalen JSON-Sidecar zu bestehender WAV; bisheriger Supabase-Datenvertrag und Upload bleiben unverändert. LIVE-Analyzer-Setup nur in Analyzer-Sidecar.
- Rolling-Beobachteraufnahme: bisheriger Roh-PCM-Pfad wendet nun wie die Direktaufnahme den konfigurierten WAV-Gain einmalig auf die exportierte Aufnahme an; Roh-Rolling-Ring bleibt unverändert.
- Einmalige +Gain-Kompensation für Analyzer-FFT-Darstellung/Wasserfall und 1-kHz-Statistiken; Filterstatistik bis 40 nominale lokale Biquad-Bänder. Originalgetreue 8-kHz-FAST-Statistik, Impulse, Modulation und SPL-Kalibrierung weiterhin offen.
- Quellstand; Host-/statische Checks separat in `BUILD_VERIFICATION_v0.5.18.md` dokumentieren. Android-Gradle-Build und Xiaomi-Regression **NOT RUN**. Projekt-original v0.5.17 unverändert aufbewahren.

---
# LFN-Scout v0.5.17 – 24.09.2026 – App überall im Portraitmodus

- `MainActivity` und `ReminderAlarmActivity`: `android:screenOrientation="portrait"` in `app/src/main/AndroidManifest.xml`. Neigung des Geräts in die Horizontale darf weder Landscape-UI noch Analyzer-Panelverlust auslösen.
- Keine Änderung der Kotlin-Quellen, FFT, Audioaufzeichnung, Rolling/Direkt-Audio, Speicherdaten, Setups oder Exportpfade. Versionsnummer v0.5.17 / 160 für getrennte Installations- und Testverfolgung; gleiche Paket-ID.
- Statischer Manifest-Regressionstest `tools/verify_portrait_v0517.py` und Geräteschritte unter `docs/project/TESTPLAN_v0.5.17.md`. Android-Build und Xiaomi-Rotationstest für diese Version noch NICHT durchgeführt.
- v0.5.16: Nutzer lieferte nach erfolgreicher Exportmeldung `Setups4.zip` mit alter Setup-4-Datei, getrenntem CURRENT-v2 und Setup 1. Das bestätigt nur den konkreten Konflikt-Export, keine pauschale Gerätefreigabe.

---
# LFN-Scout v0.5.16 – Migrations-/JSON-Exportkorrektur (24.09.2026)

**Quellstand:** versionName `0.5.16`, versionCode `159`, Android-Paket unverändert. Basis v0.5.15. **Statischer Testkandidat**, kein ausgeführter Android-/Compose-Build oder Xiaomi-Test.

**Diagnose:** Die vom Nutzer gelieferte alte `Setup_4_48k.json` hat u. a. `range=22000`, während das interne Setup 4 `range=200` besitzt. Der v0.5.15-Migrator erkennt den Konflikt und gibt `false` zurück; der Export wirft daraufhin eine `IllegalStateException`. Das ist eine aus Daten und Code belegte Konfliktkonstellation, kein geräteseitig nachgewiesener vollständiger Stacktrace.

**Korrektur:** Bei ausstehenden Migrationen wird der intakte Altname nie vom normalen Export überschrieben. Das aktuell intern gespeicherte Setup erhält einen unabhängigen `Setup_N_CURRENT_v2.json`-Namen. Der Export anderer Slots läuft weiter. Der Diagnose-ZIP enthält nun `Settings/setup_migration.json` (ausstehende Slots und konkrete Fehlermeldungen), und die GUI zeigt Fehlermeldungen einschließlich `exception.message`. Nach vollständig validiertem Import inkl. Preference-Readback wird die Altdatei wie zuvor gelöscht; danach kann der reguläre v2-Dateiname verwendet werden. Konflikte bleiben erhalten und erfordern eine spätere, ausdrücklich geprüfte Konfliktlösung. Keine stillschweigende Werteübernahme, kein automatisches Löschen bei Konflikt.

**Offen:** Echter vollständiger JSON-Import, interne Schema-v2-Migration, einheitlicher LIVE-/STOP-Parameterkatalog und Konflikt-UI. Ebenso keine Änderung an FFT, Audio/ROLLING, Analysepegel oder der Konfiguration des Nutzers. Eine v0.5.16-Installation passt Setup 4 **nicht automatisch** auf 8 kHz an.

**Windows-Prüfung:** `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`, danach Xiaomi-Regression nach `docs/project/TESTPLAN_v0.5.16.md`.

---

## v0.5.15 – Einmaliger Import alter JSON-Setups

- Neuer `AnalyzerLegacySetupMigration`: v1-Exportdateien aus bekannten Downloads werden vor v2-Export je Slot gelesen, validiert, in vorhandene Preferences übernommen, rückgelesen und erst danach gelöscht.
- Konflikt mit gespeicherten Werten: keine stille Überschreibung; alte Datei bleibt erhalten; Export wird zum Schutz blockiert. Bereits vorhandene v2-Dateien bleiben erhalten.
- Interne v2-Speichermigration und allgemeiner Import beliebiger JSON-Dateien bleiben offen. Android-/Gerätetest ausstehend.

## v0.5.14 – Einheitliche Sicht auf Analyzer-Parameter und JSON v2 (Testkandidat)

# Analyzer: gemeinsame Einstellungsoberfläche und JSON-Projektion v0.5.14

## Status
Implementiert (Quellcode; Android/Compose-Build und Gerätetest NICHT ausgeführt):
- Neun vorhandene Kategorien bleiben in beiden Modi in identischer Reihenfolge erhalten.
- In der 8-kHz-Ansicht erscheinen die nicht separat unterstützten Audiooptionen und Peakmarker als nicht anklickbare graue Werte. In Direkt ist die komplette Filterbank als ausgegraute Einzelparameter sichtbar statt als einziger Hinweis.
- Bei Direkt sind Audioquelle, Eingangsrate und Decimation sichtbar STOP-pflichtig; die bestehende Laufzeitprüfung bleibt erhalten.
- Gemeinsame JSON-v2-Projektion: gleiche flache `settings`-Schlüssel und gleiche `groups`-Struktur, `availability` je Schlüssel sowie `provenance`; fehlende alte Slot-Werte bleiben `null` statt erfundene Standardwerte zu erhalten. 8k-Festwerte gelten in der Oberfläche; aus alten Slots ungesicherte Festwerte werden im JSON nicht erfunden.
- Nach dem App-Update wird vorhandener Setup-Export einmalig neu geschrieben. Gespeicherte Slots und mode_8/mode_48 bleiben unverändert.

## Absichtlich nicht implementiert / weiterhin offen
- **Kein JSON-Import**, keine Migration der alten Slots und keine Änderung des internen Preferences-Schemas. `read_only_copy=true` bleibt verbindlich.
- Laufzeitvorschriften werden noch nicht vollständig aus einem einzigen zentralen Parameterkatalog abgeleitet (der neue Export hat eine zentrale Key-/Gruppenliste); keine Änderung der DSP- und Audio-Laufzeit.
- Eine einheitliche Glättungszeit-Eingabemaske im Direktmodus ist noch offen; Zeitmodus und angezeigter Wert bestehen.
- Die 8-kHz-Testsignale sind weiterhin in ihrer bestehenden TEST-Betriebsart; direkte Testton-Keys ersetzen diesen Pfad nicht.
- `availability` beschreibt die Funktion des Modus, nicht Geräte-Hardwarefähigkeit oder Laufzeitpolicy. `provenance=MISSING_IN_SAVED_SLOT` ist bewusst weder FALSE noch Default.
- Veränderter Wasserfallmodus betrifft neue Zeilen; die Historie wird nicht rückwirkend neu berechnet.

## Abnahmeplan
1. Frisch entpacken, Windows `PREPARE_PROJECT.bat` (SDK-Pfad konfigurieren).
2. `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`.
3. Xiaomi: 8k und Direkt Einstellungen öffnen; Gruppen/ausgegraute Werte/Speichern/STOP prüfen.
4. Alten Setup-1/4-Slot ohne Speichern exportieren: gleiche Schlüsselmengen, fehlende Werte `null`, nach Speichern vorhandene Werte. Beim Speichern eines Direkt-Setups 100-ms-Wasserfallintervall prüfen.
5. Rollback/STOP/Panelverlassen, Buttons/Filterautoranges, PNG/JPG und Diagnoseexport regressionsprüfen.

Kein PASS für Android-Build, Emulator oder Xiaomi ohne tatsächlichen Lauf. 16:1-Pegelabweichung und vollständige Impulserkennung bleiben offen.

## v0.5.13 – 24.09.2026 – Transiente Wasserfall-Zustandsmeldung

- Bisher dauerhaft angezeigte Wasserfall- und FILTER-Infozeilen unter dem Wasserfall entfernt. Wechsel von Darstellung, Modus und relevanten Filter-/Pegelparametern löst eine zweizeilige Overlay-Meldung für 5 s unten rechts im Wasserfall-Viewport aus; Schriftgrößen der früheren Zeilen 10/9 sp bleiben erhalten.
- Anzeige-Zeitgeber wird ausschließlich durch Änderungen des stabilen Darstellungszustands (nicht neue Messzeilen/FFT-Snapshots) neu gestartet. Keine Änderung an Canvas-/Bitmap-Renderer, Zeitachse, Farbbalken, PNG/JPG, Audio/FFT, Filterbank oder den freigegebenen Buttons. Kamera-Pop-up unabhängig.
- JVM-Format-Test, statischer Integrationsvertrag und gerätespezifische Abnahme separat dokumentiert in `BUILD_VERIFICATION_v0.5.13.md` und `docs/project/TESTPLAN_v0.5.13.md`. Vollständiger Android-/Compose-Build und Xiaomi **NOT RUN**; 16:1-Pegelabweichung weiterhin offen.

## v0.5.12 – 24.09.2026 – ORIGINAL-Icon und separate FILTER-Autorange

- Originalwasserfall-Actionbutton zeigt unverändert die vom Nutzer hochgeladene 200×205-PNG, statt des bisherigen synthetischen LUT-Symbols. FILTER: fünf symmetrische Magenta/Rosa/Weiß-Balken, beide bei 48 dp.
- FILTER-Langdruck startet separate Pegelautomatik (echte digitale Filter-RMS-Daten; max. gültiger Impulspegel plus Reserve, zeitrobuster Grundpegel). FILTER-dBFS MIN/MAX, Erfassungsdauer und Reserve unabhängig vom Original, persistent in 8-kHz-Profil und Setups 1–3. Live und PNG/JPG teilen Filter-LUT und jeweiligen Skalenbereich. Keine neue AudioRecord-Instanz; alter Direkt-Analyzer unverändert.
- Host-FILTER 10/10, Integrationsvertrag 18/18, Renderer-Host 5/5, Kotlin-PSI 110/0, Direkt-Host 33/33 PASS; alter Maxhold-Hostlauf TIMEOUT; Android/Compose und Xiaomi NOT RUN. FILTER-Colorbar-Tap bleibt optional/offen. Siehe `docs/project/FILTER_AUTORANGE_AND_ICONS_v0.5.12.md` und Testplan.

- Dokumentationsrevision ohne Änderung an App-Version, Code oder Teststatus: `README.md` wieder kumulativ im bisherigen `# LFN-Scout v…` / `## Neu in v…`-Stil bis einschließlich v0.5.12 fortgeführt; ältere Versionshinweise eindeutig als historisch markiert. README-Direktive präzisiert.

## v0.5.11 – 24.09.2026 – Experimenteller FAST-Filterwasserfall, Direkt-5-ms-Hop

- Stufe A: 8-kHz-Rohdatenteilung innerhalb bestehender Analyzer-Session, 2–40 Filterbänder (Standard 10), unabhängige FILTER MIN/MAX, zwei verbindlich festgelegte 48-dp-Buttons, Filter-dBFS-LUT Magenta–Weiß, dynamisches transientabhängiges Overlay, gekoppelte Historie und PNG/JPG.
- Direktmodus: zusätzlicher 5-ms-Hop, zuschaltbare Glättung über Sekunden-Zeitkonstante (5 s) statt bisherigem festen α.
- Neuer Hosttest FAST 11/11 PASS, bisheriger Direkt-Core 33/33 PASS, PSI-Syntax 185/0; vollständiger Android-Build und Xiaomi NOT RUN. Filterreaktion/Latenz und Impulsfusion **nicht validiert/noch nicht implementiert**. Alte 16:1-Pegelabweichung weiter offen.
- Details `docs/project/FILTERBANK_v0.5.11.md`, `docs/project/TESTPLAN_v0.5.11.md`, `BUILD_VERIFICATION_v0.5.11.md`.

# LFN-Scout v0.5.10 – 21.09.2026 – Android-Kotlin-Buildkorrektur

- Nutzernachweis: Windows-Gradle `:app:testDebugUnitTest` v0.5.09 **FAIL** in `:app:compileDebugKotlin` (`LiveAnalyzerController.kt:534`).
- `setTiming`: `fftHop in if (direct48k) Set<Int> else IntArray` durch zwei typsichere Zweigprüfungen ersetzt; zulässige Werte unverändert.
- `versionName=0.5.10`, `versionCode=153`, App-ID unverändert; weitere v0.5.09-Direkt-Analyzer-Funktionen unverändert. Quell-/Hosttests sind kein Android-Buildnachweis. 16:1-Pegelabweichung offen.

---

# LFN-Scout v0.5.09 – 21.09.2026 – Direkt-Analyzer 8–96 kHz, erweiterte FFT/Filter/Einstellungen

- Exklusive PCM16-Quelle konfigurierbar: AUTO, UNPROCESSED, MIC, VOICE_RECOGNITION; Sampling Rate 8/11,025/16/22,05/44,1/48/96 kHz. Tatsächliche Hardwareunterstützung/AudioRecord.sampleRate prüfen und Fehler statt stiller Fehlkonfiguration anzeigen.
- FFT-Größen 64–16384 (9 Radix-2-Größen), 9 Fenster inklusive Cosine, Blackman-Harris, Flat Top, Triangular und Welch mit kohärenter Pegelkorrektur; FFT-Hop 10/15/20/25/30/50/100 ms, unabhängig von FFT-Größe. Direkter Faktor 0–12 (0 AUS; 1=1:1), FIR nur bei ganzzahlig teilbarer Rate.
- Direktes Glättungsgewicht α=0–1, optionaler DC-Abzug, 0–2 Peakmarker im Live-Spektrum und PNG/JPG, Stay Awake bei sichtbarem Direkt-Panel. Zwei unabhängig schaltbare digitale Testtöne mit eigenen Frequenzen in Hz (nur TEST-Modus).
- Neues Direktprofil + Setup-Slot 4, JSON-Export als lesbare Kopie; alte 8-kHz-/WAV-/Rollingpfade nicht absichtlich geändert. Crash-Recovery anhand des gespeicherten gewählten Modus statt anhand Dezimierungsfaktor 1.
- JVM-Core-Hosttest 33/33; Timing 6/6; PSI-Syntax 181 Dateien/0 Fehler. **Android/Compose-Build und Xiaomi NICHT AUSGEFÜHRT**. 16:1-Pegelabweichung OFFEN. Siehe `BUILD_VERIFICATION_v0.5.09.md` und `docs/project/TESTPLAN_v0.5.09.md`.

---

# LFN-Scout v0.5.08 – Zeitachse und Kamerameldung (21.09.2026)

**Version:** `0.5.08` / `versionCode 151`; Quellbasis v0.5.07. Die Wasserfall-Sekundenachse ist durch das schwarze, mit der vorhandenen Farbskala kombinierte Echtzeitband (`dd.MM`, `HH:mm`, max. 5 Ticks) ersetzt. Spektrum und Wasserfall besitzen weiterhin identische x-Plotgrenzen. PNG/JPG nutzt dieselbe Zeitdatenquelle. Das manuelle Kamera-Pop-up wird jetzt in der Nähe des Buttons gezeigt.

**Prüfstatus:** Eigenständige Kotlin-JVM-Zeitachsenprüfung PASS. Android-/Compose-Build und Xiaomi für v0.5.08 **NOT RUN**. Der vorherige v0.5.06-Windows-Build schlug fehl; ein neuer v0.5.07-Erfolg ist nicht nachgewiesen. Die 16:1-Pegelabweichung bleibt offen. Details `BUILD_VERIFICATION_v0.5.08.md`, `docs/project/TESTPLAN_v0.5.08.md`.

---

# LFN-Scout v0.5.07 – 21.09.2026 – Android-Buildkorrektur

- Windows-Build v0.5.06 ist nachweislich fehlgeschlagen: `resetMaxHold()` fehlte auf `AnalyzerRawLfSession`, zudem fehlte die UI-Blockschließung in `LiveAnalyzerScreen.kt`. Hierdurch waren `cursorReadout`, `makeWaterfall`, `Modifier.align` und Typinferenz fehlerhaft.
- Subscriber-Weiterleitung `fun resetMaxHold() = core.resetMaxHold()` ergänzt; schließende Klammer vor dem nicht scrollenden Kamera-Pop-up eingefügt. Kein Design-/Funktionswechsel beabsichtigt.
- Version `0.5.07`, `versionCode 150`; neuer Struktur-/Delegations-Regressionstest `tools/verify_v0507_buildfix.py`; ergänzter Geräte-Testplan. Host-Core- und Timingtests PASS; Android-Build/Xiaomi noch ausstehend. 16:1-Pegelabweichung offen.

# LFN-Scout v0.5.06 – 21.09.2026 – Spektrumkurven, MAX/CLR und Kamera-Pop-up

- Separat schaltbare Spektrumkurven Aktuell (gelb), Average (rot) und Maxhold (türkis), mit weiter separat schaltbarem grauen Rauschboden. Defaults wie v0.5.05: Aktuell/Avg EIN, MAX AUS. Spektrum-Optionen und Setup-Slots 1–4 erhalten neue persistente Keys mit Alt-Slot-Defaults; 8-/48-kHz-Modusprofile führen sie getrennt.
- Maxhold-Daten werden bereits vom Core ermittelt; neu in die Live-/PNG-/JPG-Darstellung übernommen. Bei sichtbarem MAX erscheint neben der Kamera ein zweizeiliger MAX/CLR-Knopf in **48 dp**, nicht größer als die Kamerafläche. Klarsetzen setzt nur Maxhold zurück, nicht Average, Wasserfall, FFT-Ring, Rolling oder WAV.
- Kamera-Langdruck: manueller Export trägt eigene Abschluss-Ereignisnummer und Meldung; erst nach tatsächlichem Erfolg/Fehler wird ein gut sichtbares feststehendes Overlaid-Pop-up ca. 2,2 s gezeigt. Bisherige kurze Statuszeile unten entfällt. Automatikexport und altes `status`-Feld lösen kein falsches manuelles Pop-up aus. Kurztipp schaltet weiterhin nur die Automatik um.
- Nutzer bestätigte v0.5.05 im Gerätetest bis auf das nachgemeldete fehlende Pop-up. Android-/Compose-Build und Xiaomi-Test v0.5.06 **NOT RUN**; neue Host-/Static-Verträge siehe `BUILD_VERIFICATION_v0.5.06.md`. Die 16:1-Pegelabweichung bleibt offen.

# LFN-Scout v0.5.05 – 21.09.2026 – Zeitbasis-Korrektur, PAUSE und Datenexport

- **Versionsregel:** keine zusätzliche vierte Versionskomponente erforderlich; v0.5.05 / versionCode 148 ist die nächste normale Version nach 0.5.04.2 / 147.
- **Crash-Fix im Quellcode:** Nach 48 kHz → STOP → Panel verlassen wurde `direct48k=false` gesetzt, aber der im Controller verbliebene Faktor 1 bei einer UI-Neuberechnung an `AnalyzerTiming.lfRateHz()` übergeben. Synchrones Zurückladen der 8-kHz-Controllerwerte; Snapshot-Sample-Rate mit explizitem Fallback für ungültige inaktive Zwischenzustände. JUnit-Regression `restoredIdleModeNeverCrashesOnOldWidebandFactor` (HOST PASS; noch kein Android-/Xiaomi-Nachweis).
- Rolling-Restore protokolliert `RESTORE_RESULT`, wartet bei zuvor aktivem Rolling auf echte Engine/Owner-Bestätigung; `RESTORE_DONE` wird erst bei bestätigter Wiederherstellung geschrieben. Bei Timeout bleibt das Panel offen, die Wiederherstellung ist offen; keine falsche Erfolgsbestätigung.
- **PAUSE** wieder als separate Schaltfläche neben RUN/STOP vorhanden; `setPaused` unterbricht Spektrum/Wasserfall-Fortschreibung, ohne den Rolling-Betriebsmodus zu ändern. STOP + Panel verlassen hat weiterhin eigene Semantik.
- **Lesbarer Export:** vorhandene Setups 1–4 werden nach Update und beim Speichern als JSON-Kopien unter `Download/LFN-Scout/Setups/` bereitgestellt; separate Schaltflächen unter Einstellungen → Setups/Diagnose. Diagnose-ZIP enthält nur explizit freigegebene Analyzer-/Test-/Impuls-Einstellungsfelder und `AUDIO_DIAG_*`-Zeilen. Keine Beobachtungsdaten, Zugangsschlüssel oder vollständiger privater Dateidump. Vorhandene Setups bleiben intern führend, JSON-Import nicht implementiert.
- **Nicht als behoben behaupten:** Pegelabweichung 16:1; Android-/Compose-Build und Xiaomi-Abnahme dieses Standes; MAX/CLR- und Kurvensteuerung weiterhin geplant. Die vom Nutzer bestätigten 8-kHz-Modi wurden nicht absichtlich funktional verändert.

## v0.5.04.2 – 21.09.2026 – Zusätzliche Audio-/Rolling-Diagnose

- Neuer `AnalyzerAudioTrace` mit Sitzungskennung, Prozess-/Threadkennung und persistenten `AUDIO_DIAG_*`-Ereignissen sowie `LFN-AUDIO`-Logcat-Tag.
- Diagnosepunkte in `AnalyzerModeCoordinator`, `AnalyzerDirectAudioService`, `RollingAudioService` und `LiveAnalyzerController`; PCM-Heartbeats nur alle ca. 15 s. Keine absichtliche Änderung der Zustandsübergänge oder des DSP.
- Nutzer korrigiert die frühere Fehlermeldung: **8-kHz-Modi funktionieren**. Weiter offen: sporadische 48-kHz-/Rolling-Übergangsabstürze, fehlende PAUSE-Taste, 16:1-Pegelabweichung.
- Version `0.5.04.2 / 147`; Android-Build, Xiaomi-Test und Crashursache **noch nicht nachgewiesen**. Diagnose- und Testanleitung unter `docs/project/AUDIO_DIAGNOSE_v0.5.04.2.md` und `TESTPLAN_v0.5.04.2.md`.

# v0.5.04.1 – 21.09.2026 – Buildkorrektur für v0.5.04

- Nutzer-Windows-Build v0.5.04: `:app:compileDebugKotlin` scheiterte an nicht zulässigem Smart-Cast einer in einer Closure veränderlichen `savedRange` (`LiveAnalyzerController.kt:545–546`). Ergebnis einmal in eine unveränderliche lokale Variable übernommen; keine beabsichtigte Änderung des Autorange-Verhaltens.
- `AnalyzerTimingTest` hatte alte 1-kHz-Testschleifen auf die inzwischen erweiterten globalen FFT-Hops (einschließlich 48-kHz-Hops) angewandt; zwei Tests scheiterten deshalb an `IllegalArgumentException`. Alte 1-kHz-Prüfungen auf passende LF-Hops begrenzt, expliziten 48-kHz-Zeitbasistest ergänzt. Produktive DSP-/Rolling-/Audio-State-Machine unverändert.
- `versionName=0.5.04.1`, `versionCode=146`; Paket-ID bleibt unverändert. **Windows-Gradle-Neulauf und Xiaomi-Abnahme noch offen**. Der 16:1-Pegelverlust bleibt offen. Siehe `BUILD_VERIFICATION_v0.5.04.1.md`.

## v0.5.04 – 21.09.2026 – Analyzer-Sitzung / 48 kHz / Testquelle / Langzeitwasserfall

- Analyzer-Startauswahl: 8-kHz-Rolling oder exklusiver 48-kHz-Direkteingang, zusätzlich synthetische Testquelle 8/48 kHz. Ausgangs-Rolling-Einstellung sichern, bei 48 kHz vorübergehend deaktivieren; **STOP lässt die Betriebsart unverändert**, Restore **erst** nach STOP und Verlassen des Panels. Bei zuvor aktivem Rolling neuer RAM-Ring; alte ungesicherte Daten dürfen verloren gehen.
- Separates 48-kHz-Profil/Setup 4 mit Default 0–22000 Hz, FFT8192, Hop4800, 120 s Wasserfall; 16384 FFT-Punkte auswählbar, 1:1 ohne Analyzer-FIR. 8-kHz-Setup-Slots 1–3 bleiben erhalten.
- Digitaler 50-/195-Hz-Generator mit frei einstellbarem Pegel/Frequenz, optional Dual-Tone ausschließlich im Analyzer-Hub, sichtbare TESTSIGNAL-Kennzeichnung. Keine akustische Kalibrierung.
- Wasserfallzeit frei 10–14400 s (3600/7200 s möglich), dynamisches Zeilenintervall mit max. 2400 Zeilen; für 48 kHz speicherbegrenzte Frequenzverdichtung. Akt/MW/MAX-Wechsel erhält alte Zeilen mit Trennmarkierung statt Reset.
- Kameralangdruck: Status des vorherigen Exports vor neuer Speicheraufgabe entwerten, nach Erfolg oder Fehler ca. 2 s Rückmeldung; TESTSIGNAL auch im Export.
- JVM-Core-Hosttest 16/16, statischer App-Vertrag 20/20. Android-/Compose-Build und Xiaomi v0.5.04 **NOT RUN**. Der Nutzer meldete v0.5.03 einen 16:1-Energieverlust: **weiter offen**. Rolling ist RAM-Aufzeichnung, **keine neu implementierte WAV-Dauerdatei**. Gerätetest: `docs/project/TESTPLAN_v0.5.04.md`.

## v0.5.03 – 20.09.2026 – Decimation, Cursor, benannte Setups, Statistik

- Analyzer-only FIR-Decimation 4/8/16 mit 2/1/0,5 kHz LF-Abtastrate; Anti-Alias-Filter faktorabhängig. FFT-Hop/Transformation/Glättung/Wasserfallzeit auf tatsächliche LF-Rate angepasst. Shared 8-kHz-Rohhub, WAV und Rolling-Produktion unverändert.
- Cursor-Readout links/rechts gleiche Höhe; Linie im Wasserfall bis zur Unterkante verlängert, Export zeichnet dieselbe Frequenzposition.
- Drei Setups mit Name/Beschreibung inklusive Alt-Slot-Fallback. Frei editierbares Statistikband, unabhängiges Zeitfenster und 5-s-Leistungs-Average; L50/L90-Labels, Spektrum-Markierung und Bildexport angepasst.
- `versionName=0.5.03`, `versionCode=144`, unveränderte Paketkennung. Neuer FIR-/Timing-Hosttest 20/20 sowie historische Hosttests. Android-/Compose-Build und Xiaomi **NICHT AUSGEFÜHRT**. v0.5.02 wurde durch den Nutzer auf dem Gerät als bestanden bestätigt; Einzelheiten: `docs/project/TESTPLAN_v0.5.03.md`.


## v0.5.02 – 2026-09-20 – Analyzer: freie Einstellungen und Anzeige-Korrekturen

- Hz-Mitte zwischen tatsächlichen Tickpaaren in beiden Diagrammen und Export; Wasserfall-Fußtext Akt/MW/MAX; ±-Buttons für dBFS-Min/Max entfernt; Farbbalken links dynamisch nach längster Tickzahl verschmälert.
- Freie Wasserfall-Sekundeneingabe, 10–600 s mit 2400-Zeilen-Schutz; persistente Historie, dynamische Zeitachse, automatische Bildzyklus-Zeitbasis und Setup-Slots angepasst. L50/L90 bleiben 120 s.
- FFT-Hop 5, Size 2048/4096/8192, Frequenzanzeige Min/Max 0–200 Hz mit mindestens 5 Hz Spannweite, vier Fenster und exponentielles Smoothing (AUS/EIN, Zeitkonstante / α). Transformationsintervall entspricht bei 1 kHz dem Hop in ms. Shared-PCM und WAV unverändert.
- `versionName=0.5.02`, `versionCode=143`, `applicationId` weiterhin `de.brummbeobachter.app`. Host-Core kompiliert und neue 15/15 Tests PASS; Android-/Compose-Build und Xiaomi v0.5.02 **NOT RUN**. Details: `docs/project/ANALYZER_v0.5.02.md`, `TESTPLAN_v0.5.02.md`.
- Nutzer-Abnahme v0.5.01 nachgetragen: fünf Testgruppen PASS bis auf die später ausdrücklich als FAIL gemeldete Hz-Positionierung; letzter verifizierter Gerätelauf bleibt v0.5.01 (nicht v0.5.02).

## v0.5.01 – 2026-09-20 – Analyzer-Bedienung, Kamera, PNG/JPG

- Kurzer Kameratipp schaltet automatische Speicherung, rotes X bei AUS; langer Druck erstellt ein manuelles Bild unabhängig vom Automatikzustand. Der separate Auto EIN/AUS-Text entfällt.
- PNG/JPG-Format und JPEG-Qualität in Automatiken; Dateiendung/MIME/Kompression korrekt, Auswahl persistent und in den drei Setup-Slots.
- Hz zwischen Zahlen auf derselben Frequenzachsenzeile im Live-UI und Bildexport; Rücknavigation jeweils nur eine Ebene, auch über Android-Back.
- Kontrastreiche manuelle Min-/Max-dBFS-Felder; Autorange-/Preset-Übernahme in nicht fokussierte Felder, während aktive Eingabe geschützt bleibt.
- v0.5.01 / versionCode 142, Package-ID weiterhin `de.brummbeobachter.app`. Audio-/FFT-/WAV-/Supabase-Produktivpfade unverändert. Statische Tests 20/20 + 20/20; Kotlin-Wasserfall 6/6, Range 7/7. Android-SDK-Build und Xiaomi v0.5.01 **NICHT GETESTET**. Stufen B/C bleiben offen. Siehe `docs/project/ANALYZER_CONTROLS_v0.5.01.md`.

## Dokumentationsübergabe 2026-09-20 – App v0.5.00 bleibt versionCode 141

- Die letzte verfügbare vollständige Quellversion v0.5.00 übernommen; **kein** Produktiv-Feature, keine Änderung von Android-Code, FFT/PCM/WAV, Backend, privaten Recovery-Dateien oder App-Versionscode.
- `PROJECT_STATUS.md`, ausführliche `docs/project/UEBERGABE_v0.5.00_2026-09-20.md` und `docs/project/TESTPLAN_v0.5.00_HANDOFF.md` hinzugefügt; Rekonstruktionsstand, Indizes, bekannte Probleme und Prüfmatrix synchronisiert. Quellen nach aktualisiertem SHA-256-Manifest prüfbar.
- Den seit v0.4.91 veralteten Prüfvertrag in `verify_reconstruction.py` an den aktuellen `project_manifest.json`-Versionsstand angepasst; ursprüngliches v0.5.00-Paket schlug aus diesem Grund `FAIL version invariant` / `source priority missing` fehl. Diese Änderung betrifft nur den Verifier.
- Letzte Nutzerprüfung: 7/7 Xiaomi-Schritte v0.4.93, Bilder vom laufenden Analyzer zuletzt in v0.4.99. v0.5.00-Layoutkorrektur ist implementiert, aber **Android-Build und Xiaomi-Test weiter NOT RUN**. Kein Nachweis für manuelles/Auto-PNG oder Settings-Persistenz auf dem Gerät.

## v0.5.00 – 2026-09-20 – Analyzer nutzt gesamte Höhe, überflüssige Karten entfernt

- Live-Analyzer-Root füllt mit dunklem Hintergrund die gesamte verfügbare Viewport-Höhe (`BoxWithConstraints` / `fillMaxSize`); die helle Restfläche unterhalb des Hauptbilds wird nicht mehr durch eine inhaltsgroße Column freigelassen.
- Diagrammhöhen abhängig von der tatsächlich verfügbaren Höhe statt starrer 285/220 dp; Spektrum und Wasserfall teilen den nach kompakter Kopf-/Fußzeile verfügbaren Platz. Mindesthöhen mit Scroll-Fallback für kleinere Displays; erweiterten Wasserfallmodus erhalten.
- Die drei unteren Karten `Mithören` (geplant), `Superfast` (geplant) und `Anzeige` (redundant) werden aus dem Hauptbild entfernt. Spektrum-Schalter `Mittelwert` und `Rauschboden` liegen weiterhin funktional unter **Einstellungen → Spektrum**; Diagnose unter **Einstellungen → Diagnose**.
- Untere Aktionszeile enthält ausschließlich **PNG** und **Auto EIN/AUS**. Die beiden zweiten Einstellungs-/Diagnose-Buttons sowie die permanente technische Hinweiszeile entfallen. Ein Fehlerhinweis verweist auf das Diagnose-Unterpanel.
- Unverändert: Audioaufnahme/8-kHz-Roh-PCM, FIR, FFT, Gain, FFT-/Wasserfall-Hop, Wasserfallmodi, Auto-/manueller PNG-Export, alle dBFS-Grenzen und Setup-Persistenz, L50/L90-Reset. Android-Build und Xiaomi-Layout-Test sind für diese Version offen; siehe `BUILD_VERIFICATION_v0.5.00.md`.
- `versionName = 0.5.00`, `versionCode = 141`, Paketkennung unverändert.

## v0.4.99 – 2026-09-20 – Einspaltiges Einstellpanel und manuelle dBFS-Grenzen

- Horizontales Registerband durch acht einspaltige Kategorien ersetzt. Aktives Setup oben: vorhandene 3 lokale Plätze, Laden/Speichern/Neu (freier Platz). Auswahl wird gemerkt.
- Bestehende tatsächlich funktionsfähige Parameter erhalten und thematisch Spektrum, Audio, Anzeige, Statistik, Automatiken, Kalibrierung, Setups und Diagnose zugeordnet. Im Bereich Anzeige zwei frei editierbare dBFS-Grenzen mit Eingabevalidierung und unmittelbarer persistenter Übernahme; Presets und Autorange bleiben bestehen.
- Setup-Slots speichern jetzt auch Analysegain und Autorange-Dauer/-Reserve (zusätzlich zu bestehenden Einstellungen); kein neues zweites Mikrofon, keine neuen WAV-, DSP- oder Backend-Änderungen. Nicht implementierte Funktionen nur als Information.
- PNG-Ort für Android 10+ `Download/LFN-Scout/`, für ältere Geräte app-spezifisches Pictures/Analyzer; manuelles PNG/Auto-PNG unverändert. PNG-Ausgabe selbst noch nicht auf Xiaomi geprüft.
- 0.4.99 / versionCode 140. Vollständiger Android-Build und Gerätetest offen; Code-/Manifestprüfung gesondert dokumentiert.

## v0.4.98 – 2026-09-20 – Wasserfall: Aktuell / Mittelwert / Maximum

- Separat wählbarer Wasserfallmodus (Default Aktuell, neu Mittelwert linearer FFT-Leistungen pro Zeilenintervall oder binweises Maximum). Zeitbasis und FFT-Hop bleiben getrennt. Auswahl in bestehenden Anzeige-Einstellungen sowie persistent in lokalen Preferences und Setup-Slots 1–3.
- Moduswechsel beginnt eine neue reine 120-s-Wasserfallhistorie; bestehende Audioaufnahme, rote Spektrum-Mittelwertkurve, L50/L90 und FFT bleiben unangetastet. PNG trägt Modusbezeichnung. Kein Spektrum-Max-Hold in der UI.
- Neue JUnit- und Host-Regressionen für alle drei Modi, leere Zeilen, Zeitbasis und Reset; Vorschlag für Einstell-Unterpanels unter `docs/project/ANALYZER_SETTINGS_PANEL_VORSCHLAG_v0.4.98.md`. Keine Umgestaltung der Register ohne Nutzerfreigabe.
- `versionName=0.4.98`, `versionCode=139`. Android-Build/Xiaomi-Test noch offen.

## v0.4.97 – 2026-09-20 – Robuste Autorange und platzsparende Analyzer-Ansicht

- Autorange berücksichtigt nun pro Spektrum das 5./95. Perzentil der gültigen Frequenzbins (bei weniger als 20 Bins die vorhandenen Extremwerte) und danach das 10./90. Perzentil über die gesammelten Spektren. Kurze negative Einbrüche bzw. schmale Einzelspitzen beeinflussen die Anzeigegrenzen dadurch weniger stark; weiterhin feste, einstellbare ±5 dB Reserve und standardmäßig 2 s. Nur gültige Werte, DC und Leersignal ausgeschlossen; persistiert dieselben Unter-/Obergrenzen. Keine Änderung des Messsignals.
- Cursor-Readout ohne Umrandung und ohne Hintergrund; Abtippen des Texts schaltet Cursor weiterhin aus. L50-/L90-Resetbox ohne Füllung, mit intrinsischer Breite/Höhe, roter abgerundeter 1-dp-Kontur und 3 dp Innenabstand. Reset von Statistik und FFT-Average bleibt unverändert.
- Farbskala links am Wasserfall von 27 auf 18 dp verengt, Beschriftung entsprechend kompakter. Gemeinsame Frequenz-Plotränder in Spektrum und Wasserfall von 68/15 auf 60/12 dp geändert; Offscreen-PNG: Farbleiste 63→42 px, Plotränder 155/955→140/970 px. Zeitachse, Wasserfall-Historie und 8-kHz-/1-kHz-PCM/WAV-Pfad unverändert.
- `versionName=0.4.97`, `versionCode=138`, `applicationId` unverändert. Isolierter `kotlinc`-Hosttest: 7/7 PASS; statische Integrationsprüfung 19/19 PASS; Android-SDK-Build und Xiaomi-Gerätetest noch offen. Einzelheiten: `docs/project/ANALYZER_LIVE_v0.4.97.md` und `BUILD_VERIFICATION_v0.4.97.md`.

## v0.4.96 – 2026-09-20 – Autorange und Analyzer-Hauptbild

- Tipp auf Spektrum-dBFS-Achse startet ein zeitbasiertes Autorange: Standard 2 s, fest ±5 dB; beide Parameter im Register Anzeige auswählbar. Gültige FFT-Min/Max ohne DC/Leersignal; automatische untere und obere dBFS-Grenze wird unmittelbar persistent gespeichert und beim Neustart geladen; manuelle Presets und Setups 1–3 unterstützen beide individuellen Grenzen.
- Kompakter roter, ungefüllter, abgerundeter L50/L90-Rahmen startet Statistik und FFT-Average gemeinsam neu, ohne die Wasserfallhistorie oder die 4096-Sample-FFT zu löschen.
- Cursor per Fingergeste verschiebbar; an oberer Cursorspitze positioniertes Hz/dBFS-Readout nach links/rechts; Tipp auf Readout schaltet Cursor ab. Kurven etwas dünner, Spektrum erhält mehr Höhe.
- Wasserfall-Titel/Vergrößern/Normal/Achsenerklärungszeile entfallen; Zoom durch Tipp auf den Wasserfall bleibt. Farbskala links neben dem Wasserfall, dBFS-Werte beginnen innerhalb der Farbleiste; Hz-Achsen beider Diagramme verwenden dieselben Plotränder. Dynamische dBFS-Ticks und entsprechende Offscreen-PNG-Anpassung.
- `versionName=0.4.96`, `versionCode=137`, `applicationId` unverändert. Host-Selbsttest und statische Vertragsprüfung PASS; vollständiger Android-Build, UI und Xiaomi noch **NICHT** verifiziert. Detail: `docs/project/ANALYZER_LIVE_v0.4.96.md`.

## v0.4.95.1 – 2026-09-20 – Build-Hotfix: fehlenden Diagnose-Callback wiederhergestellt

- Nutzer-Build der v0.4.95 scheiterte an drei `Unresolved reference 'acceptDiagnostics'` in `LiveAnalyzerController.kt` (Zeilen 110/136/174). Die drei Übergaben waren vorhanden, aber die private Callback-Methode war beim UI-Umbau entfallen.
- `private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)` wiederhergestellt. Sie nimmt Diagnosewerte unter dem bestehenden Controller-Lock entgegen, sofern der Analyzer läuft. Keine Änderung an PCM-Fan-out, FIR/FFT, Wasserfall-Timing, WAV, Rolling-Buffer oder den v0.4.95-Anzeigevorgaben.
- Regression `verify_v04951_diagnostics_contract.py` prüft jetzt die drei Callback-Übergaben **und die vorhandene Methodendeklaration mit passendem Typ**, damit die statische Prüfung diesen Fehler künftig erkennt.
- `versionName = "0.4.95.1"`, `versionCode = 136`, `applicationId` unverändert. Autorange durch Tipp auf die dBFS-Achse (2 s / einstellbare ±5 dB / persistente Grenzen) bleibt für v0.4.96 geplant und wurde in diesem Build-Hotfix **nicht** implementiert.
- Der Nutzer hat den fehlgeschlagenen Windows-/Android-Kompilierlauf für v0.4.95 vorgelegt. Vollständiger Android-Build und Xiaomi-Test für v0.4.95.1 noch offen.

## v0.4.95 – 2026-09-20 – kompakter Live-Analyzer und zeitbasierter Wasserfall

- Arbeits-Preset **−60 bis −120 dBFS** (oben −60, unten −120) für Spektrum und Wasserfall samt Farbleiste/PNG; Anzeige-Presets −40…−120 und 0…−120 optional; keine SPL-Kalibrierung.
- FFT-Hop zusätzlich 10 LF-Samples (rechnerisch 100 FFT/s; nicht garantierte Bildrate). Default 100 bleibt. Wasserfall-Intervall zeitbasiert 100/200/250/500/1000 ms, Default 250 ms: 480 Zeilen = 120 s. Bei FFT-Hops 500/1000 ms wird ein zu kurzes Wasserfallintervall automatisch auf die reale Spektrumkadenz angehoben. 1-kHz-FIR, bestehender Recorder und WAV unverändert.
- Haupttitel `LFN-Scout · 0–100 Hz`, darunter einmalig die Statuszeile `Live · 8 kHz → 1 kHz FIR · dBFS · Gain +19 dB uncal` (Gain ist beispielhaft und zeigt den tatsächlichen Wert). Doppelte Spektrum-/Gain-Überschrift und Kurvenlegende entfernt; L50(2–25)/L90(2–25) oben rechts im Diagramm. Mehr Höhe fürs Spektrum. Rauschboden-Kurve Default AUS; im GUI umschaltbar, Zustand und Anzeigebereich persistent und in Setups 1–3 gespeichert.
- UI-/PNG-dBFS-Achsen und Farbskala verwenden denselben gewählten Anzeigebereich. Auto-PNG weiterhin erst nach vollen 120 Sekunden und frühestens nach jedem weiteren 120-s-Zyklus.
- Der Nutzer bestätigte für v0.4.94 sichtbares Spektrum/Wasserfall und vorliegende Diagnose mit 0 Drops/Resets/Fehlern; dies gilt nicht als v0.4.95-Gerätetest. Android-Gradle-/Compose-Build und Xiaomi-Visual-Abnahme dieser Version offen.

## v0.4.94 – 2026-09-20 – Live-Analyzer: FFT-Hop, Wasserfall-Historie und vollständige Achsen

- Nutzerergebnis v0.4.93: 7/7 Xiaomi-Funktionsschritte abgehakt (Rolling, Live-Kurve, Wasserfall, Reaktion, Bereich 100/200, Pause, Panelwechsel). Diese Bestätigung ist kein Kalibrier-, PNG- oder Langzeittest.
- `AnalyzerRawLfSession` reicht einen **einstellbaren FFT-Hop** an `LiveAnalyzerCore` weiter. LF-Abtastrate bleibt 1000 Hz, FFT-Größe 4096; vorhandener 8-kHz-Rohdaten-FIR und die separate WAV-Strecke bleiben unverändert. Standard FFT-Hop 100 LF-Samples (Soll 10 FFT/s), Auswahl 50/100/200/500/1000.
- Separater Wasserfall-Hop 1/2/5/10 FFT-Spektren, Standard 5; Berechnung der Zeilenkapazität für ca. 120 Sekunden aus beiden Hops, Statistikpuffer separat nach FFT-Hop (L50/L90 weiter ~120 s). Lokale Preferences und drei Setup-Slots um beide Hops ergänzt.
- UI-Polling 500 ms → 100 ms (bei Hop 50: 50 ms); tatsächliche Render-/FFT-Rate geräte- und lastabhängig, keine garantierten FPS. Wasserfall-Zeilen werden nur bei deren eigenem Takt gerastert und das Bitmap zwischen Zeilen wiederverwendet.
- Korrekte Wasserfall-Ausrichtung auf **Zeit vertikal, neueste Zeile oben, ältere nach unten**, Frequenz horizontal; dBFS-Spektrumachse sowie Wasserfall-Farbskala haben sichtbare Zahlenticks; Spektrum und Wasserfall haben Frequenz-Ticks. Einheit `Hz` getrennt als Achsentitel, nicht am letzten Tick. Offscreen-PNG entsprechend umgestellt.
- Keine neuen Mikrofon-Owner, kein 48-kHz-Capture, Mithören/Histogramme/Impuls-Wasserfall weiter offen. `versionName=0.4.94`, `versionCode=134`, `applicationId` unverändert.
- Host-Kotlin-Cadence-/Hub-Selbsttest und statische Achsen-/Vertragsprüfung durchgeführt. Android-Studio-Gradle-Build, visuelle Gerätefreigabe, PNG-Automatik und 120-s-Langzeittest **NOT RUN**. Siehe `BUILD_VERIFICATION_v0.4.94.md`.

## v0.4.93 – 2026-09-20 – Android-Kompilierung: Fehlerzähler-Schnittstelle korrigiert

- Nutzer-Build `:app:testDebugUnitTest` für v0.4.92 brach bei `:app:compileDebugKotlin` mit `Unresolved reference 'spectrumCallbackErrors'` in `LiveAnalyzerController.kt:202` ab. In der tatsächlich verwendeten `AnalyzerRawLfSession` heißt die öffentliche Eigenschaft `callbackErrors`; `spectrumCallbackErrors` gehört zur älteren `AnalyzerLiveSession`.
- Controller greift nun auf `session?.callbackErrors` zu; Diagnoseanzeige und `Snapshot.callbackErrors` bleiben unverändert. Keine Änderung am PCM-Capture, FIR, FFT, WAV, Rolling, UI-Layout oder Supabase.
- Statische Regression `verify_v0493_live_analyzer_api.py` prüft Controller→Session-Vertrag und verhindert die Rückkehr des falschen Member-Namens. Die älteren versionsgebundenen Verifier bleiben als historische Nachweise erhalten.
- `versionName=0.4.93`, `versionCode=133`, Paketkennung unverändert. Kein lokaler Android-SDK-Build/Xiaomi-Test verfügbar; Windows-Gradle- und Xiaomi-Live-Test stehen aus. Siehe `BUILD_VERIFICATION_v0.4.93.md`.

## v0.4.92 – 2026-09-20 – Regressionstest: FFT-Startup bei Nulleingang

- `versionName=0.4.92`, `versionCode=132`, Paketkennung unverändert.
- `RawLfDecimatorTest.zeroInputProducesZeroAndNotPlausibleLevels`: zuvor 8.200 Rohsamples lieferten nach 8:1-Decimation nicht die benötigten 4.096 LF-Samples. Testet nun explizit den noch leeren Startup-Zustand und danach mit fortgesetzten Rohsamples ein vollständiges Nullsignal-FFT-Fenster.
- Keine produktive DSP-, Audio-, Recorder-, UI- oder Backend-Änderung. Die Visualisierung und der reale Xiaomi-Signalweg bleiben separat zu prüfen.
- Vollständiger Windows-Android-Gradle-Test und Geräteprüfung für v0.4.92 noch NOT RUN; siehe `BUILD_VERIFICATION_v0.4.92.md`.

## v0.4.91 – 2026-09-20 – Live-PCM-Reparatur, Signaldiagnose und UI-Abgleich

- Android-Update von v0.4.90/130 auf v0.4.91/131; Paketkennung unverändert.
- Roh-PCM-Fan-out (8000 Hz, keine neue Mikrofoninstanz) statt abgerundetem Recorder-1-kHz-Signal für Live-Analyzer; eigenständige 129-Tap-FIR-LF-Decimation und Double-FFT-Eingabe, ohne die bestehende WAV-Erstellung zu verändern.
- Diagnose für Roh-/LF-RMS und Peak, Quellenrate, letzte Daten, Drops, Resets, Callback-Fehler; ungültige/stale FFT-Werte nicht als valide Statistik/PNG veröffentlichen.
- UI-Annäherung an vom Nutzer bereitgestelltes dunkles Konzept: Zeit horizontal im Wasserfall, Frequenz vertikal, kompakte Status-/Bedienkarten und klare Nicht-Verfügbarkeitskennzeichnung für Mithören.
- Synthetische JVM-Prüfungen: schwaches 47 Hz + 180 Hz, Aliasunterdrückung bei 2500 Hz, nullsignalerkennung, Hub-Integration PASS. Kein Android-Gradle- oder Xiaomi-Test für v0.4.91 behauptet.
- `docs/project/ANALYZER_LIVE_v0.4.91.md` und `LFN-Scout_v0.4.91_TESTBERICHT.md` dokumentieren Risiken und offenen Testumfang.

## v0.4.90 – 2026-09-20 – Klassischer Live-Analyzer (Integrations-/Testkandidat)

- Erstmals echtes Live-Spektrum und klassischen Wasserfall als Android-Panel, Anzeige 0–20/50/100/200 Hz; gelb/rot/grau, Cursor, Pause, vergrößerter Wasserfall und relative 2–25-Hz-L50/L90.
- Reuse des bisherigen `:analyzer-core`, gemeinsamer 1-kHz-PCM-Hub, kein konkurrierender Mic-Owner; Controller über internen Panelwechsel stabil.
- Manuelles/offscreen periodisches PNG und drei lokale Setup-Slots. Nicht implementierte Settings-Register explizit als offen gekennzeichnet.
- Bestehende v0.4.89-Impulsdiagnose unverändert; Impuls-Wasserfall zurückgestellt. Keine Änderung an WAV-/Backend-/Recovery-Daten.
- 0.4.90/130. Reiner Kotlin-FFT-Selbsttest synthetisch erfolgreich; vollständiger Android-Build und Gerätetest offen. `docs/project/ANALYZER_LIVE_v0.4.90.md`.

## v0.4.89 – 2026-09-19 – Manuelle Impulsschwelle und Rauschabstand

- `versionName` 0.4.89 / `versionCode` 129, bestehende Android-Paketkennung unverändert.
- Diagnose: Mindestschwelle vor Gain 50–3000 PCM, adaptiver Faktor 1,5–12; robuste/mittlere/empfindliche Presets und weiterhin unabhängiger Gain 0–50 dB. Persistenz über private Android-Preferences.
- Session übergibt unveränderliche Konfiguration pro PCM-Block; Rauschschwelle bleibt adaptiv, Rohdaten/WAV/Recorder unverändert. Live-Telemetrie für Block-/Maxpeak, Rauschbasis, festen/adaptiven/effektiven Schwellwert; Kopie des Testprotokolls in die Zwischenablage.
- Neue JVM-Regressionen für leise Impulse, Presets, Rauschwirkung, fünf Ereignisse mit Ruhephase, Grenzwerte. Kompletter Android-Build und Gerätetest müssen lokal erfolgen.

## v0.4.88 – 2026-09-19 – Separater Gain für Highspeed-Impulsdiagnose

- `versionName` 0.4.88 / `versionCode` 128; Anwendungspaket-ID unverändert.
- Einstellbarer digitaler Diagnose-Gain 0–50 dB in 1-dB-Stufen, Startwert 0 dB. Gain beeinflusst den festen Impulsschwellenanteil im Roh-PCM-Äquivalent, nicht die Rohdaten oder WAV-Aufnahmen. Adaptiver Noise-Floor bleibt unverändert relativ zur Rohsignalstärke.
- Anzeige Rohpeak, mathematisch verstärkter Analysepeak ohne 16-Bit-Clipping, sowie effektive Roh-PCM-Schwelle. 8-kHz-Capture bleibt unverändert; keine 48-kHz-Hardwarefreigabe.
- Zusätzliche Regressionstests für leise Impulse, adaptives Rauschen und veränderbare Schwellen. Der eigentliche Spektrum-/Wasserfall-Analyzer ist weiterhin nicht als UI angebunden; diese Gain-Funktion bezieht sich derzeit nur auf die Impulsdiagnose.
- Die v0.4.87-Gerätetests ergaben 8 kHz, 5/5 laute Impulse und 0 weitere Ereignisse in anschließender Ruhephase; leise Impulse bisher offen.

## v0.4.87 – 2026-09-19 – JDK-Suche und Highspeed-Diagnose

- `versionName` 0.4.87 / `versionCode` 127. Produktname und `applicationId` unverändert.
- `configure-gradle-jdk17.ps1`: reservierte PowerShell-Variable `$home` durch `$jdkHome` ersetzt. Projektlokale, IDE- und installierte JDKs nun vor altem Projektcache priorisiert. Keine globalen Variablen verändert.
- Entwicklerdiagnose in Zusatzfunktionen: expliziter Start/Stopp des Roh-PCM-Detektors, Anzeige tatsächlicher Capture-Rate, Rohpeak, Ereignissen und Überlast-/Fehlerzählern; kein neuer Mikrofon-Owner.
- Statische Verifier und offizielle Projekt-/Rekonstruktionsdokumentation aktualisiert. Android-Build und Gerätetest von **v0.4.87 noch nicht nachgewiesen**.

## v0.4.86 – 2026-09-19 – Buildvorbereitung: Android-Studio-JDK-17 / Analyzer-Core

- `versionName` 0.4.86 und `versionCode` 126. Paket-ID bleibt `de.brummbeobachter.app` (Update-Kompatibilitaet).
- `PREPARE_PROJECT.bat` findet vollstaendiges JDK 17 und konfiguriert vor Gradle-Start sowohl `org.gradle.java.home` als auch `org.gradle.java.installations.paths`; letztere Eigenschaft macht das JDK fuer die Kotlin/Java-Toolchain von `:analyzer-core` auffindbar.
- Ortsabhaengige `.tools/java17.path`- und `local.properties`-Dateien sowie die fremdrechnerbezogene `org.gradle.java.home`-Zeile aus der Liefer-ZIP entfernt; die lokalen Einstellungen werden durch PREPARE bzw. Android Studio neu angelegt.
- `tools/configure-android-sdk.ps1` erzeugt bei erkanntem SDK die projektlokale `local.properties`; `verify_v0486_jdk.py` und aktualisierte Rekonstruktions- und Highspeed-Verifier; keine Aussetzung bestehender Tests.
- Windows-11-Nutzerergebnis der **vorherigen v0.4.85**: Analyzer-Core-/App-Tests und `assembleDebug` erfolgreich. v0.4.86: statische Tests dokumentiert; Android-Studio-Sync und Windows-Build noch zu bestaetigen.
- Keine Aenderungen an Audio-Engine, Impulsdetektor, Seismik-Produktivcode, WAV-Format oder Supabase-Daten.

# Changelog

## Entwicklungsnachtrag 2026-09-19 – Seismik-Regressionskorrektur Phase 2H (weiterhin v0.4.85)

- Zwei veraltete App-JUnit-Vertragstests auf den seit v0.4.78 aktiven gemeinsamen Bitmap-Renderer und die tatsächlichen `near_bitmap_*`-Gesten aktualisiert. Die Tests prüfen nun echte Label-/Anzeige-/Export- und Zoom-/Reset-Codepfade statt historische Bezeichner.
- Nutzerlauf des vorherigen Pakets: 114 App-Tests, 2 Fehler. Aktualisiertes Testpaar isoliert mit Kotlin ausgeführt: 6/6 PASS; **vollständiger Android-Gradle-Test, APK und Gerätetest für dieses korrigierte Paket weiterhin offen**.
- Kein Produktivcode, Audioformat, Supabase-Datenbestand, App-ID, `versionName` oder `versionCode` geändert. Siehe `docs/project/SEISMIC_REGRESSION_PHASE2H_FIX.md`.

## Entwicklungsnachtrag 2026-09-19 – Analyzer Phase 2H Highspeed (noch v0.4.85)

- Neuer `rawPcm`-Fan-out vor Recorder-Gain und Decimation aus beiden realen AudioRecord-Bestandswegen (8 kHz); bisheriger 1-kHz-WAV-/FFT-Pfad erhalten.
- Im gemeinsamen JVM-Modul: `HighspeedImpulseDetector` und `HighspeedImpulseSession`, bounded Queue, Source-/Rate-/Gap-Reset; kein neuer Mic-Owner.
- Synthetic-JVM-Tests für 0,5-ms-Pulse bei 8/48 kHz, Blockgrenze, Clipping, Stille und Queue-Handling: PASS. **Keine 48-kHz-Hardware-Erfassung**, kein Android-Build/Panel/PNG/Release.


## Entwicklungsnachtrag 2026-09-19 – Analyzer Phase 2 (noch v0.4.85, kein Android-Release)

- DSP-Core in neues Kotlin-JVM-Modul `:analyzer-core` ausgelagert; originaler FFT-Algorithmus unverändert.
- Bounded asynchroner PCM-Hub und Live-Session mit Source-/Gain-/Gap-Reset für eine spätere Standalone-App.
- In-Process-Mikrofon-Owner für Rolling-/direkte Aufnahme sowie PCM-Publikation nach bestehender 8:1-Mittelung und Gain hinzugefügt.
- Bisherige WAV-, Cloud- und UI-Verträge nicht umgebaut. App-JUnit und Core-JUnit ergänzt; CLI-Host getestet, Android-/Gerätetest offen.
- Dokumentation, Module/CI-Testabhängigkeit und Manifest synchronisiert. Details `docs/project/ANALYZER_PHASE2_AUDIO_STANDALONE.md`.


## v0.4.85
- `versionName` auf `0.4.85`, `versionCode` auf `125` erhöht.
- Supabase-Katastrophensicherung in den Projektstand integriert: Anwendungsschema, Nutzdaten-Dump, Auth-/Storage-Schema, Rollen-Dumps, Projektkonfiguration und Secret-Namensliste.
- Remote-Storage vollständig nach Dateianzahl gesichert: **53 WAV-Dateien**, davon 51 unter `brumm-audio` und 2 unter dem historischen Bereich `brummbeobachter-wav`; lokale Gesamtgröße 12.722.332 Byte.
- Fünf produktive Edge Functions mit lokal vorhandenen Quellen abgeglichen: `submit_observation`, `upload_wav`, `get_recent_observations`, `get_radius_histograms`, `get_all_observations`.
- Rekonstruktions- und Recovery-Dokumentation um Supabase-Disaster-Recovery, Restore-Reihenfolge, Sicherheitsregeln und Verifikationsstatus ergänzt.
- Lokale Build-/Tool-Caches (`node_modules`, `.gradle`, `.idea`, `app/build`, `supabase/.temp`) aus dem Release-ZIP entfernt; reproduzierbare Paketdefinitionen bleiben erhalten.

## v0.4.84
- `versionName` auf `0.4.84`, `versionCode` auf `124` erhöht.
- Audioarchiv-Legacy-Kompatibilität für den früheren öffentlichen Ordner `Download/BrummBeobachter/` ergänzt.
- Veraltete/stale `contentUri`-Einträge erhalten einen Dateinamen-Fallback über MediaStore für `Download/LFN-Scout/` und den historischen Ordner.
- 24-h-Cleanup behandelt eine bereits fehlende WAV als erfolgreichen Zielzustand und entfernt anschließend auch die Metadaten; echte Löschfehler bleiben retry-fähig.
- Neue Aufnahmen werden weiterhin ausschließlich nach `Download/LFN-Scout/` geschrieben.
- Regressionstest `AudioArchiveCompatibilitySelfTest.kt` ergänzt.

## v0.4.83
- Produkt-, Projekt-, ZIP-, Export-, Log- und Dokumentname auf **LFN-Scout** vereinheitlicht.
- Projektordner/ZIP: `LFN-Scout_Android_v0.4.83` bzw. `LFN-Scout_Android_v0.4.83.zip`.
- Öffentlicher Android-Speicherpfad auf `Download/LFN-Scout/` geändert. Alte Dateien unter früheren Ordnernamen werden nicht gelöscht.
- GitHub-Workflow, APK-/Artifact-Namen und Buildtexte auf `LFN-Scout` umgestellt.
- UI-/Theme-/interne Symbolnamen mit dem alten Produktnamen wurden auf `LFNScout...` umgestellt.
- **Kompatibilitätsausnahme:** Android `applicationId`/`namespace` und Kotlin-Package bleiben `de.brummbeobachter.app`, damit v0.4.83 als Update über bestehende Installationen installiert werden kann.

## v0.4.82
- `versionName` auf `0.4.82`, `versionCode` auf `122` erhöht.
- Der große Restsekunden-Countdown der normalen Audioaufnahme ist kein modaler `AlertDialog` mehr.
- Die Restsekundenzahl wird transparent über die bestehende Audio-Aufnahmekarte gelegt.
- Die CLIP-Anzeige bleibt während der Aufnahme sichtbar.
- Die vorhandene gelb hinterlegte Taste `Aufnahme abbrechen` bleibt sichtbar und erreichbar.
- ROLL/Rolling-Aufnahmen bleiben unverändert und erhalten weiterhin kein solches Aufnahme-Overlay.

## v0.4.81
- `versionName` auf `0.4.81`, `versionCode` auf `121` erhöht.
- Seismik-Dateinamen enthalten zusätzlich die lokale Speicher-Uhrzeit bis auf Sekunden.
- Fertige WBBU- und Näheplot-Bitmaps werden vollständig um 90° nach links gedreht; die Diagramminhalte selbst werden nicht neu angeordnet.
- Näheplot-Messwertverstärkung im Entwicklermodus ganzzahlig 1–20 einstellbar und persistent; Standard 2.
- Wasserfall-Automatik bleibt Standard; optional manuelle Min/Max-Referenz bei 50 dB Gain mit gemeinsamer negativer Verschiebung bei kleinerem Gain.
- Normale Audioaufnahme zeigt einen großen Restsekunden-Countdown mit direkt zugänglichem `AUFNAHME ABBRECHEN`; Rolling-Buffer bleibt davon unberührt.

## v0.4.80
- `versionName` auf `0.4.80`, `versionCode` auf `120` erhöht.
- Nähe-Helicorder weiter verdichtet: `HELICORDER_BINS_PER_ROW` von `1536` auf `3072` erhöht.
- Plot-Messwerte für den selbst erzeugten Näheplot probeweise mit Faktor `2.0` verstärkt, ohne Änderung der festen WBBU-Referenzskala.
- Master-Bitmap `6144 × 3456` beibehalten; Kurven dürfen dadurch deutlich stärker über benachbarte Zeilen hinausreichen.
- UI-/Exporttexte auf `Messwerte ×2` und `3072 Bins/Zeile` aktualisiert.

## v0.4.79 - 2026-09-17

- Nähe-Helicorder deutlich detailreicher: interne Master-Auflösung von 3072×1728 auf **6144×3456** erhöht.
- Supersampling des gemeinsamen Bitmap-Renderers von 2× auf **4×** erhöht; Anzeige und PNG-Export bleiben identisch.
- Verdichtung pro 30-Minuten-Zeile von 240 auf **1536 Bins** erhöht.
- Jeder Bin behält jetzt **ersten Wert, Minimum, Maximum und letzten Wert**; Minimum und Maximum werden in ihrer tatsächlichen zeitlichen Reihenfolge gezeichnet.
- Der scheli-artige kontinuierliche Trace verwendet damit bis zu vier zeitlich geordnete Stützpunkte pro Bin statt nur zwei Extrema.
- **Kein zusätzlicher Frequenzfilter** eingeführt; die Änderung betrifft ausschließlich Auflösung und zeitliche Darstellungsverdichtung.
- Neue Regression-/Selbsttests `SeismicDetailReductionSelfTest.kt`, `SeismicBinDetailSelfTest.kt` und `tools/verify_v0_4_79.py`.
- `versionName` auf `0.4.79`, `versionCode` auf `119` erhöht.

## v0.4.78 - 2026-09-17

- Näheplot auf einen einzigen Bitmap-Renderer umgestellt: Anzeige und Export verwenden dasselbe Master-Bitmap.
- Master-Auflösung auf 3072×1728 festgelegt; keine anschließende Pflicht-Reduktion auf 1536×864.
- Uhrzeiten aller 48 Halb-Stunden-Zeilen werden direkt in das Bitmap gerendert.
- X-Achse 0/5/10/15/20/25/30 min und Amplitudenmaßstab sind ebenfalls Teil des Bitmap.
- WBBU- und Näheplot-Card enthalten jeweils einen eigenen, direkt sichtbaren **Plot speichern**-Button.
- WBBU-Speicherung bleibt Original-PNG; Näheplot-Speicherung verwendet das bereits in der App dargestellte Master-Bitmap.
- `versionName` auf `0.4.78`, `versionCode` auf `118` erhöht.
- Regression-Verifier `verify_v0478_requirements.py` / `tools/verify_v0_4_78.py` ergänzt.

## v0.4.77 - 2026-09-17

- WBBU- und Näheplot verwenden jetzt denselben, immer sichtbaren gemeinsamen Button **Plot speichern**; der Button liegt bewusst vor der WBBU/Nähe-Verzweigung.
- WBBU speichert weiterhin das originale HLNUG-PNG, der Näheplot weiterhin den selbst erzeugten Helicorder.
- Näheplot-Rendering auf ein **scheli-artiges kontinuierliches Helicorder-Prinzip** umgestellt: 48 Zeilen × 1800 s, rot/blau alternierend, gemeinsame physikalische Skalierung und zusammenhängende peak-erhaltende Spur statt isolierter Min/Max-Balken.
- Zoom/Pan/Doppeltipp-Reset und Speicherziel `Download/LFN-Scout/` bleiben für beide Plotarten erhalten.
- Neuer `SeismicScheliPolicyTest` und Verifier `tools/verify_v0_4_77.py`.
- `versionName` auf `0.4.77`, `versionCode` auf `117` erhöht.

## v0.4.76 - 2026-09-17

- Regression-Fix für das Speichern des WBBU-Seismikplots.
- WBBU wird jetzt direkt als originale HLNUG-PNG-Bytefolge gespeichert; unnötiges Bitmap-Dekodieren und erneutes PNG-Komprimieren entfällt.
- Der selbst erzeugte Nähe-Helicorder bleibt unverändert erhalten und wird weiterhin über `SeismicPlotExporter.renderNearPlot()` exportiert.
- Gemeinsamer Button `Plot speichern` und Erfolgsmeldung bleiben für WBBU und Näheplot erhalten.
- Speicherziel bleibt `Download/LFN-Scout/`.
- Neuer Regressionstest `verify_v0476_requirements.py` prüft beide Exportpfade.
- `versionName` auf `0.4.76`, `versionCode` auf `116` erhöht.

## v0.4.75 - 2026-09-17

- Reiner Build-Fix für v0.4.74.
- Fehlende schließende Klammer am Ende von `SeismicScreen` ergänzt.
- Behebt die Compiler-Kaskade mit u. a. `Unresolved reference`, `private is not applicable to local function` und `Expecting '}'`.
- Neuer Regressionstest `verify_v0475_requirements.py` prüft Versionsstand, ausgeglichene Klammern und die Top-Level-Grenze vor `PanelHeader`.
- Keine funktionale Änderung an sichtbarem App-Namen `LFN-Scout`, Landscape-Seismikplot, Zoom, PNG-Export, WBBU-Sonderlogik oder Näheprinzip.
- `versionName` auf `0.4.75`, `versionCode` auf `115` erhöht.

## v0.4.74 - 2026-09-17

- Sichtbarer App-/Produktname ab dieser Version: **LFN-Scout**.
- Bestehende Projekt-, Verzeichnis- und ZIP-Namen bleiben absichtlich **LFN-Scout**.
- Sichtbarer Näheplot jetzt fest im Landscape-Seitenverhältnis **16:9**; die Zeitachse läuft entlang der langen horizontalen Strecke.
- Uhrzeitbeschriftungen links von 8 sp auf **4 sp** halbiert und über die Plot-Höhe verteilt.
- Intensitätsmaßstrecke bleibt ganz links mit vertikalem `40000 nm/s` direkt an der Strecke.
- Export bleibt 1536×864 px (intern 3072×1728 mit Downsampling).
- Redundante `toFloat()`-Aufrufe in `SeismicPlotExporter.kt` entfernt.
- Regression: `verify_v0474_requirements.py` prüft Namenswechsel, Landscape-Liveplot, kompakte Zeitlabels und Export-Warnungsbereinigung.

# Changelog

## v0.4.73 - 2026-09-17
- Näheplot-Export auf WBBU-nahes 16:9-Endformat 1536×864 umgestellt.
- Rendern intern mit 2× Auflösung (3072×1728), danach hochwertiges Downsampling auf 1536×864.
- Intensitätsmaßstrecke ganz links vor den Uhrzeiten; Beschriftung direkt parallel an der Strecke inklusive Einheit (`40000 nm/s`).
- WBBU-nahe kompakte Kopfzeile und X-Achse 0/5/10/15/20/25/30 min.
- Speicherziel bleibt `Download/LFN-Scout/`.
- Keine Änderung an Datenabruf, MiniSEED-Decodierung, Regional-/WBBU-Logik oder physikalischer Skalierung.

# LFN-Scout – Changelog

## v0.4.72 – 2026-09-17
- Näheplot optisch deutlich an WBBU angenähert: kompaktere 48 Halb­stundenzeilen, weniger Leerraum und dichteres Min/Max-Rendering.
- Linke Anordnung jetzt platzsparend: vertikale Intensitätsbeschriftung und Maßstrecke ganz links, danach Uhrzeiten, dann Plot.
- X-Achse WBBU-näher mit 5-Minuten-Markierungen von 0 bis 30 min.
- PNG-Exporter verwendet dieselbe kompakte WBBU-nahe Darstellung.
- Seismik-PNGs werden wieder im bestehenden `Download/LFN-Scout/` gespeichert.
- `versionName` auf `0.4.72`, `versionCode` auf `112` erhöht.

# Changelog

## v0.4.71 - 2026-09-17

- Seismik-Näheplots: Strichstärke der Wellenform gegenüber v0.4.70 von 0,8 px auf 1,6 px verdoppelt.
- Näheplots erhalten eine sichtbare Intensitäts-/Amplitudenlegende als Maßstrecke. Bei kalibrierten Geschwindigkeitsdaten entspricht die Strecke der WBBU-Referenz `40000 nm/s`; im Counts-Fallback wird die Counts-Skala kenntlich gemacht.
- WBBU-Originalplot und selbst erzeugte Näheplots sind per Zwei-Finger-Pinch bis 5x zoombar und im Zoom verschiebbar; Doppeltipp setzt auf 1x zurück.
- Neuer Button `Plot speichern`: WBBU speichert das originale HLNUG-PNG, Näheplots werden als vollständiger Helicorder inklusive Zeitlabels und Intensitätslegende gerendert.
- Exportziel: `Download/LFN-Scout/Seismik/`; Dateinamen enthalten Datum, Netzwerk/Station/Kanal.
- Nach erfolgreichem Export erscheint eine sichtbare Erfolgsmeldung und ein Toast; neue Logs `SEISMIC_PLOT_SAVE_OK` / `SEISMIC_PLOT_SAVE_FAILED`.
- Neue Policy/Selbsttest `SeismicPlotUiPolicy.kt` / `SeismicPlotUiSelfTest.kt` und Anforderungsverifier `verify_v0471_requirements.py`.
- `versionName` auf `0.4.71`, `versionCode` auf `111` erhöht.

## v0.4.70 - 2026-09-17

- Build-Fix für den neuen Seismik-Helicorder aus v0.4.69.
- Fehlenden Compose-Import `androidx.compose.foundation.layout.Box` in `MainActivity.kt` ergänzt.
- Zeitbeschriftung der 48 Helicorder-Zeilen von `data.helicorderRows.forEach { ... }` auf eine direkte `for (row in data.helicorderRows)`-Schleife umgestellt, damit `Box`/`Text` sicher im vorhandenen `@Composable`-Kontext ausgeführt werden.
- Neuer Regressionstest `SeismicHelicorderComposeContractTest.kt` prüft Import und Schleifenform.
- Keine Änderung an Seismik-Datenabruf, WBBU-Sonderlogik, 30-Minuten-Zeilen, Blau/Rot-Darstellung oder nm/s-Skalierung.
- `versionName` auf `0.4.70`, `versionCode` auf `110` erhöht.

## v0.4.69 - 2026-09-17

- Seismik-Näheplots erhalten eine feste WBBU-Referenz-Y-Skalierung statt Tages-Autoskalierung, sofern eine verwertbare FDSN-Geschwindigkeitssensitivität vorhanden ist.
- Aus dem bereitgestellten WBBU-Plot übernommen: `40000 nm/s` entsprechen ca. `4,95` Zeilenabständen; daraus folgt ca. `8080 nm/s` pro 30-Minuten-Zeilenabstand.
- FDSN `level=channel` wird jetzt vollständig hinsichtlich `Scale`, `ScaleFrequency` und `ScaleUnits` ausgewertet. Für `M/S` bzw. `NM/S` werden Counts sensitivitätsbasiert in `nm/s` umgerechnet.
- Beschleunigungs- oder unbekannte Einheiten werden nicht fälschlich als Geschwindigkeit interpretiert; in diesem Fall bleibt ein klar gekennzeichneter Counts-Fallback aktiv.
- Eigener Helicorder wird in einem gemeinsamen Canvas gezeichnet, sodass starke Ausschläge wie beim klassischen WBBU-Plot über benachbarte Zeilen reichen können.
- Anzeige nennt Sensitivität, Referenzfrequenz und weist darauf hin, dass die Skalierung keine vollständige frequenzabhängige Response-Deconvolution darstellt.
- Neue Logs `SEISMIC_CALIBRATION_OK` und `SEISMIC_CALIBRATION_FALLBACK`.
- Neuer Regressionstest `SeismicAmplitudeSelfTest.kt`.
- `versionName` auf `0.4.69`, `versionCode` auf `109` erhöht.

## v0.4.68 - 2026-09-17

- Seismik: klassischer 24-h-Helicorder mit 48 Zeilen à 30 Minuten; Zeilen alternieren blau/rot.
- Für den heutigen Tag bleiben zukünftige Halb­stundenzeilen leer; vergangene Tage zeigen 00:00–24:00 lokale Zeit.
- Regionale Sonderlogik ausschließlich im Radius von 30 km um HS.WBBU Bürstadt (49.62294, 8.49384).
- Innerhalb der Sonderzone wird für heute zuerst das laufend aktualisierte HLNUG-24-h-Seismogramm `HS.WBBU.png` verwendet; bei Fehler automatische Rückkehr zum allgemeinen FDSN/EIDA-Näheprinzip.
- Für vergangene Tage in der Sonderzone sowie für alle Orte außerhalb der Zone gilt ausschließlich das Näheprinzip: nächste tatsächlich verfügbare offene Station mit vertikalem Z-Kanal.
- Valencia und andere entfernte Standorte haben keine fest codierte Station; Auswahl erfolgt dynamisch nach Entfernung und Verfügbarkeit.
- Neuer Regressionstest `SeismicRegionalHelicorderSelfTest.kt` für Sonderzonen-Grenze, Valencia-Näheprinzip, 48 Halb­stundenzeilen, Blau/Rot-Wechsel und leere Zukunftszeilen.

## 0.4.67 – 2026-09-17

- MiniSEED-2 Steim1/Steim2-Decodierung korrigiert: die 2-Bit-Steim-Kontrollnibbles werden jetzt SEED-/libmseed-konform mit `30 - 2 * wordIndex` aus dem Kontrollwort gelesen. In v0.4.66 war die Zuordnung um ein Datenwort verschoben und führte bei realen GEOFON-Daten zu Fehlern wie `Steim decoded 289/340 samples`.
- Die bisherigen Steim-Selbsttests wurden auf die korrekte Kontrollwortbelegung berichtigt; zusätzlich wurde ein Mehrframe-Steim1-Regressionstest ergänzt.
- Kandidaten ohne EIDA-Route (`SEISMIC_ROUTE_NONE`) werden jetzt sofort übersprungen. Ein aussichtsloser DataSelect-Probeabruf am Federator erfolgt für solche Streams nicht mehr.
- Für Streams mit gültiger Route bleibt der Federator als zweiter DataSelect-Fallback erhalten.
- Der BGR-Zertifikatsfehler bleibt ein nicht-blockierender Metadatenquellenfehler; innerhalb eines Ladevorgangs wird BGR weiterhin nur einmal versucht, danach kann GEOFON übernehmen. Keine unsichere Zertifikatsausnahme wurde eingebaut.
- Datumsnavigation und lokale Tagesfenster aus v0.4.65/v0.4.66 unverändert beibehalten.
- `versionName` auf `0.4.67`, `versionCode` auf `107` erhöht.

## 0.4.66 – 2026-09-17

- Seismik-Stations-/Kanalsuche beschleunigt: direkte FDSN-Station-Abfragen bei BGR und GEOFON vor dem ORFEUS-Federator.
- Der in v0.4.65 beobachtete Timeout bei `phase=channel_list` des europaweiten Federators blockiert den Seismik-Plot damit nicht mehr als primärer Suchweg.
- Der ORFEUS-Federator bleibt als Fallback erhalten, wenn beide direkten Metadatenquellen keine Kandidaten liefern.
- EIDA-Routing wird erst für maximal 12 nahe vertikale Z-Kanal-Kandidaten verwendet.
- Vor dem vollständigen Tagesdownload wird pro Route ein kurzer MiniSEED/DataSelect-Probeabruf (maximal 120 s) ausgeführt. Bei langen Fenstern endet die Probe fünf Minuten vor dem Fensterende, um Echtzeit-Verzögerungen nicht fälschlich als fehlende Daten zu bewerten.
- Direkte Kandidaten werden quellenübergreifend dedupliziert und weiterhin nach Entfernung, Kanalpriorität (HHZ/BHZ/EHZ/SHZ/LHZ) und Samplerate sortiert.
- Neue Logs für direkte Quellen, Fallback und Probeabruf ergänzt.
- Datumsnavigation und lokale Tagesgrenzen aus v0.4.65 unverändert beibehalten.
- `versionName` auf `0.4.66`, `versionCode` auf `106` erhöht.

## 0.4.65 – 2026-09-17

- Seismik von reinem GEOFON-Direktzugriff auf ORFEUS EIDA erweitert: Kanalmetadaten über den EIDA Federator, Datenknoten über EIDA Routing, Wellenformdaten anschließend über den gerouteten FDSN-DataSelect-Endpunkt.
- Der in v0.4.64 fehlschlagende zwingende `fdsnws-availability`-Vortest wurde entfernt.
- Für den gewählten Tag wird immer ein lokales Tagesfenster verwendet: heute 00:00 bis jetzt, vergangene Tage 00:00 bis 24:00.
- Tagesgrenzen sind `ZoneId`-/DST-sicher; der Herbst-Zeitwechsel mit 25-Stunden-Tag ist durch Regressionstest abgedeckt.
- Seismik-Panel um Datumsnavigation **◀ / Datumsauswahl / ▶ / Heute** erweitert; Zukunftsdaten sind nicht auswählbar.
- Neue Logs `SEISMIC_DATE_SELECTED`, `SEISMIC_WINDOW`, `SEISMIC_ROUTE_OK`, `SEISMIC_ROUTE_NONE`; ausgewählter EIDA-Datenknoten wird angezeigt.
- MiniSEED-Decoder für lange Tagesabrufe speichereffizienter gemacht: zweipassige Record-Erfassung mit direktem `IntArray` statt boxed Sample-Liste.
- Neue Tests: `SeismicDayPolicyTest.kt`, `EidaRoutingPolicyTest.kt`, `SeismicDayRoutingSelfTest.kt`.
- `versionName` 0.4.65 / `versionCode` 105.

## 0.4.64 – 2026-09-17

- Seismik-Fehler aus v0.4.58–v0.4.63 behoben: der feste GEOFON-PNG-Pfad wurde entfernt, weil er eine Stationskarte statt eines Seismogramms lieferte.
- Seismik lädt jetzt echte vertikale Wellenformdaten über die offiziellen GEOFON-FDSN-Dienste `fdsnws-station`, `fdsnws-availability` und `fdsnws-dataselect`.
- GPS-basierte Suche nach nahen aktiven `...Z`-Kanälen; bevorzugt werden HHZ, BHZ, EHZ, SHZ und LHZ.
- MiniSEED-2-Decodierung ohne zusätzliche Laufzeitbibliothek für Integer-, Steim1- und Steim2-Daten ergänzt.
- Nativer 60-Minuten-Seismogramm-Plot in Jetpack Compose; keine externe PNG-Grafik mehr.
- Neue/präzisierte Logs: `SEISMIC_CHANNELS_OK`, `SEISMIC_CHANNEL_SELECTED`, `SEISMIC_AVAILABILITY_OK`, `SEISMIC_WAVEFORM_DOWNLOAD_START`, `SEISMIC_WAVEFORM_OK`, `SEISMIC_NO_CURRENT_DATA`, `SEISMIC_MINISEED_PARSE_ERROR`, `SEISMIC_PLOT_OK` sowie bestehende Timeout/HTTP/Unavailable-Logs.
- Regression-/Selbsttest `SeismicWaveformSelfTest.kt` und JVM-Test `SeismicWaveformPolicyTest.kt` ergänzt.
- `versionName` 0.4.64 / `versionCode` 104.

## 0.4.63 – 2026-09-17

- Neuer Ordner `docs/project/` für offizielle, dauerhaft mitgeführte Projektdokumentation.
- `docs/superpowers/` bleibt unverändert für interne aufgabenspezifische Entwicklungspläne und Design-Spezifikationen.
- `docs/project/LFN-Scout_Aenderungen_seit_v0.4.58.md` aufgenommen.
- `docs/project/LFN-Scout_Geplante_Erweiterungen.md` aufgenommen.
- `docs/project/README.md` dokumentiert die Trennung zwischen offizieller Projektdokumentation und `docs/superpowers/`.
- Keine Änderung an App-Logik, Supabase, Android-Package-ID oder Buildwegen.
- `versionName` 0.4.63 / `versionCode` 103.

## 0.4.62 – 2026-09-17

- Neue zentrale Dokumentation `BUILD_WORKFLOWS.md` mit dem verbindlich festgelegten Build- und Updateprozess.
- Buildweg A: Android Studio unter Windows präzise beschrieben.
- Buildweg B: lokaler Gradle-Kommandozeilenbuild unter Windows präzise beschrieben.
- Buildweg C: Android-only mit ChatGPT Work, vollständiger Projekt-ZIP, GitSync und GitHub Actions präzise beschrieben.
- `PREPARE_PROJECT.bat` als Windows-Vorbereitung für A/B dokumentiert; für GitHub Actions nicht erforderlich.
- Geschützte Ordner `.git/` und `Builds/` sowie exakte Reihenfolge Pull → temporär entpacken → alten Projektinhalt ersetzen → Commit → Push → CI → APK → Pull festgelegt.
- Erforderliche Android-Apps und APK-Installation dokumentiert.
- Keine Änderung an App-Logik, Supabase oder Android-Package-ID.
- `versionName` 0.4.62 / `versionCode` 102.

## 0.4.61 – 2026-09-17

- Windows-Updater-Paket `Tools/WindowsUpdater/` mit Installer, CMD-Starter, PowerShell-Automation und persistenter `settings.json` ergänzt.
- Ausführliche deutsche Updater-Anleitung als `UPDATER_ANLEITUNG.md` und im Updater-Ordner integriert.
- Updater automatisiert ZIP-Auswahl, Versionsprüfung, Repository-Synchronisation, sichere Dateiübernahme, Änderungsanzeige, Commit, Push, GitHub-Actions-Wartephase und anschließendes APK-Synchronisieren.
- Dry-Run und Rollback vor Commit/Push eingebaut.
- `.git/` und `Builds/` werden beim Einspielen neuer ZIPs geschützt.
- GitHub Actions liest die App-Version nun automatisch aus `app/build.gradle.kts`.
- Variante B aktiviert: erfolgreiche Debug-APKs werden zusätzlich versioniert nach `Builds/debug/` committed.
- GitHub-Artifact bleibt parallel bestehen; späterer Wechsel auf Variante A ist über `STORE_APK_IN_REPOSITORY` vorbereitet.
- `versionName` 0.4.61 / `versionCode` 101.

## 0.4.60 – 2026-09-17

- Öffentlicher App-Name auf **LFN-Scout** geändert; Repository-/Projektname ist **LFN-IN**.
- `versionName` 0.4.60 / `versionCode` 100.
- GitHub-Workflow heißt jetzt **Build LFN-Scout**.
- Debug-Artefakt/APK erhält den Namen `LFN-Scout-v0.4.60-debug`.
- CI lädt bei fehlgeschlagenen Unit-Tests zusätzlich den Testreport als Artifact hoch.
- JVM-Unit-Tests erhalten eine echte `org.json`-Implementierung; Androids lokale Stub-Klassen verursachten zuvor mehrere Parser-Testfehler.
- Veralteten Facelift-Layout-Test an die aktuell verwendeten kompakten Layoutwerte angepasst.
- Widersprüchlichen alten 30-Minuten-Reminder-Test in einen 15-Minuten-Relativtest überführt; 30 Minuten folgen der bestehenden 15/45-Minuten-Rasterlogik.
- Android `applicationId`/Namespace sowie interne Klassen- und bestehende Download-Pfade bleiben zur Kompatibilität unverändert.

## 0.4.59 – 2026-09-14

- GitHub-Actions-Build als dritter Build-Weg ergänzt.
- Manueller Start über `workflow_dispatch` möglich.
- Automatischer CI-Lauf bei Push und Pull Request.
- CI verwendet Java 17 und den vorhandenen Gradle-8.13-Bootstrap.
- `testDebugUnitTest` wird vor `assembleDebug` ausgeführt.
- Erzeugte Debug-APK wird als GitHub-Artifact hochgeladen.
- Neue Anleitung `GITHUB_BUILD.md` für Bedienung am PC und Handy.
- Keine Änderung der App-Funktionalität oder des Supabase-Backends.
- VersionName 0.4.59 / VersionCode 99.

# Changelog

## v0.4.71 - 2026-09-17

- Seismik-Näheplots: Strichstärke der Wellenform gegenüber v0.4.70 von 0,8 px auf 1,6 px verdoppelt.
- Näheplots erhalten eine sichtbare Intensitäts-/Amplitudenlegende als Maßstrecke. Bei kalibrierten Geschwindigkeitsdaten entspricht die Strecke der WBBU-Referenz `40000 nm/s`; im Counts-Fallback wird die Counts-Skala kenntlich gemacht.
- WBBU-Originalplot und selbst erzeugte Näheplots sind per Zwei-Finger-Pinch bis 5x zoombar und im Zoom verschiebbar; Doppeltipp setzt auf 1x zurück.
- Neuer Button `Plot speichern`: WBBU speichert das originale HLNUG-PNG, Näheplots werden als vollständiger Helicorder inklusive Zeitlabels und Intensitätslegende gerendert.
- Exportziel: `Download/LFN-Scout/Seismik/`; Dateinamen enthalten Datum, Netzwerk/Station/Kanal.
- Nach erfolgreichem Export erscheint eine sichtbare Erfolgsmeldung und ein Toast; neue Logs `SEISMIC_PLOT_SAVE_OK` / `SEISMIC_PLOT_SAVE_FAILED`.
- Neue Policy/Selbsttest `SeismicPlotUiPolicy.kt` / `SeismicPlotUiSelfTest.kt` und Anforderungsverifier `verify_v0471_requirements.py`.
- `versionName` auf `0.4.71`, `versionCode` auf `111` erhöht.

## 0.4.58 – 2026-09-14

- Neues Panel **Seismik** mit GPS-basierter Auswahl einer nahen verfügbaren GEOFON-Station.
- Abruf und Darstellung des aktuellen 24-h-Live-Seismogramms der breitbandigen Vertikalkomponente.
- Bei nicht erreichbarem Liveplot werden bis zu fünf nahe Stationskandidaten geprüft.
- Neue Debug-Events: `SEISMIC_LOAD_START`, `SEISMIC_STATIONS_OK`, `SEISMIC_STATION_SELECTED`, `SEISMIC_IMAGE_OK`, `SEISMIC_TIMEOUT`, `SEISMIC_UNAVAILABLE`, `SEISMIC_HTTP_ERROR`, `SEISMIC_PARSE_ERROR`, `SEISMIC_IMAGE_FAILED`, `SEISMIC_FAILED`.
- Wasserfall-UI korrigiert: "Wasserfall wird erzeugt ..." erscheint nur noch bei der tatsächlich bearbeiteten Aufnahme.
- `PREPARE_PROJECT.bat` auf 0.4.58 aktualisiert.

## 0.4.57 – 2026-09-14

- Rolling-Buffer-Zeitanzeige in „Zusatzfunktionen“ wird jetzt auf App-Ebene zyklisch jede Sekunde aktualisiert.
- Die Anzeige liest die tatsächlich im `RollingAudioEngine` gepufferte Dauer und bleibt damit auch nach einer normalen 120-s-Aufnahme aktuell.
- Der bisherige lokale Screen-Ticker wurde entfernt, damit Panel-/Dialog-Lebenszyklen die Anzeige nicht mehr einfrieren können.
- `PREPARE_PROJECT.bat` zeigt jetzt die aktuelle Projektversion 0.4.57.
- Keine Änderung am funktionierenden Rolling-Audiostream, an Supabase, Schema, Migrationen oder Edge Functions.

## 0.4.56 – 2026-09-13

- Kommentar-Timeouts im Entwicklermodus einstellbar und persistent; sie gelten auch im Normalmodus. Defaults: Bewertung 5/5 s, Audio 60/10 s.
- Debug-Protokoll kann direkt im Debug-Bildschirm gespeichert werden.
- Standortpräzision in Metern nur im Entwicklermodus sichtbar; normale Logexporte blenden sie aus.
- Wasserfall-Zeitachse zeigt lokale Uhrzeit im Format HH:mm:ss; fünf Zeitmarken, auch bei 30-min-Rolling.
- Rolling-Buffer bleibt bei aktivem Schalter kontinuierlich; Snapshot, Gain-/Quellen-/Input-Änderungen stoppen oder resetten ihn nicht.
- Kotlin-Warnung in RollingAudioService bereinigt.

## 0.4.55 – 2026-09-13

- Logfile kann aus Zusatzfunktionen nach `Download/LFN-Scout/` gespeichert werden; im Normalmodus werden technische Supabase-Begriffe als „Cloud“ ausgegeben.
- Benutzeroberfläche verwendet „Cloud“ statt „Supabase“; technische Backend-Namen bleiben intern erhalten.
- L50/L90 werden im Normalmodus fest über 2–25 Hz berechnet.
- Im Entwicklermodus ist der L50/L90-Frequenzbereich persistent einstellbar.
- Audioarchiv wird beim Wechsel zu Zusatzfunktionen automatisch aktualisiert; manueller Refresh nur noch im Entwicklermodus.
- Wasserfallbild-Speicherung prüft vorhandene PNG/JPG-Dateien und fragt vor dem Überschreiben.
- Kommentar-Timeout Bewertung: 10 s bis zur ersten Eingabe, danach 10 s nach jeder letzten Textänderung.
- Kommentar-Timeout Audioaufnahme: 60 s bis zur ersten Eingabe, danach 10 s nach jeder letzten Textänderung.

 LFN-Scout Android

## 0.4.54 – 2026-09-13

- K1-Audio-Regression behoben: Wird eine normale 120-s-Aufnahme bei aktivem Rolling-Buffer abgebrochen, räumt `RollingAudioEngine.captureNext()` den exklusiven Future-Capture-Slot jetzt in `finally` auf. Nachfolgende Aufnahmen blockieren nicht mehr mit `Rolling-Audiostream nicht verfügbar`.
- Lokale Audio-Dateinamensbewertung verwendet jetzt einen Snapshot der neuesten `recentItems` am tatsächlichen Aufnahmebeginn nach dem Countdown. Damit wird ein während des Countdowns abgegebenes Rating nicht mehr durch eine veraltete Compose-Closure übersehen.
- Regressionstests `RollingAudioEngineCancellationTest` und `AudioCurrentRatingNamingContractTest` ergänzt.
- Keine Supabase-, Schema-, Migration- oder Edge-Function-Änderung; `wav_present` / `wav_path` bleibt zurückgestellte Backend-Priorität 1.

## 0.4.53 – 2026-09-11

- Rolling-Buffer-Kontinuität ausdrücklich abgesichert: nach einmaliger Füllphase enthält der Ringpuffer dauerhaft die jeweils letzten 30 Minuten.
- Sichern bleibt ein nicht-destruktiver Snapshot; der laufende Ringpuffer wird weder geleert noch neu gestartet.
- Statusanzeige und Foreground-Service-Benachrichtigung zeigen bei vollem Puffer `letzte 30:00 min`.
- Regressionstest für Füllphase, Vollzustand und nicht-destruktives Snapshot-Verhalten ergänzt.
- Keine Supabase-Änderung.

## 0.4.52 – 2026-09-11

- 30-min Rolling-Audiobuffer als optionales Foreground-Service-Subsystem ergänzt.
- RAM-Ringpuffer, Speichern auch bei Teilfüllung, lokale WAV + gleichnamige JSON mit Kommentar.
- Rolling-Dateien strikt lokal: kein Supabase-Upload, keine 24-h-Bereinigung.
- Gemeinsame Audio-Liste, ROLL-Kennzeichnung, Wasserfall per Antippen und Löschen per langem Druck mit Bestätigung.
- Normale 120-s-Aufnahmen teilen bei aktivem Rolling-Buffer den laufenden AudioRecord-Stream.
- Persistenter Einstellschalter und Boot-/Wiederanlauflogik ergänzt.

## 0.4.51 – 2026-09-10

- Hauptpanel weiter verdichtet.
- `Bewertung` + Erklärung jetzt typografisch getrennt, aber in genau einer Zeile.
- Erklärungstext 8 sp, kein Wrap (`maxLines = 1`, `softWrap = false`).
- `Audio aufzeichnen` + WAV-Parameter jetzt typografisch getrennt, aber in genau einer Zeile.
- WAV-Parameter 8 sp, kein Wrap (`maxLines = 1`, `softWrap = false`).
- Keine Backend-/Supabase-Änderung.


## 0.4.50 – 2026-09-10

- Panel-Wischrichtungen korrigiert und weiterhin nicht zyklisch.
- „Wasserfall neueste Aufnahme“ entfernt; neueste Aufnahme wird als erster Listeneintrag bedient.
- „Bewertung · 0 = nichts wahrgenommen · 5 = sehr stark“ in einer Zeile.
- Erinnerungsstatus und Modus in einer Zeile.
- Keine Supabase-Änderung.


## 0.4.49 – 2026-09-10

- Wasserfall-Vorschau mit Zwei-Finger-Pinch-Zoom (1x bis 5x).
- Vergrößertes Wasserfallbild kann verschoben werden; Doppeltipp setzt auf Gesamtansicht zurück.
- Der Button `Wasserfall für diese Aufnahme` wurde entfernt.
- Ein Tipp auf die Dateiinfo einer lokalen Aufnahme öffnet/erzeugt direkt deren Wasserfall.
- `Erneut senden` bleibt als separater Button für nicht erfolgreich übertragene WAVs erhalten.
- Wasserfall-Bildformat ist in den Einstellungen persistent wählbar: PNG, JPG oder PNG + JPG.
- JPG wird mit Qualität 95 gespeichert.
- PNG und JPG verwenden denselben Dateistamm; nur die Erweiterung unterscheidet sich.
- Nicht-zyklischer Panelwechsel per horizontalem Wisch nach der festgelegten Bedienlogik: MAIN rechts -> MORE; MORE links -> MAIN, rechts -> SETTINGS; SETTINGS links -> MORE.
- Bestehende Buttons zur Panelnavigation bleiben erhalten.
- Keine Supabase-Migration und kein Edge-Function-Deployment erforderlich.


## 0.4.48 – 2026-09-10

- Lokale WAV-Dateien erhalten sprechende Dateinamen mit Beobachter-ID,
  Aufnahme-Startzeit, Gain, Audioquelle und Bewertung.
- Bewertung nur, wenn die letzte gültige Bewertung 0–5 beim Aufnahmestart
  höchstens 30 Minuten alt ist; genau 30:00 Minuten zählt noch.
- Sonst wird `RX` verwendet.
- `observer_id` aus `get_recent_observations` wird jetzt im Client ausgewertet
  und lokal im KeyStore mitgespeichert.
- Bei neu gespeichertem Teilnehmerschlüssel wird eine alte Observer-ID
  verworfen.
- Wasserfall-PNG übernimmt exakt den WAV-Stammnamen.
- PNG ergänzt nur `_L50..._L90..._WF<MaxFreq>.png`.
- `WF<MaxFreq>` wird dynamisch aus `FREQ_MAX_HZ` erzeugt.
- Keine neue Supabase-Migration und kein Edge-Function-Deployment erforderlich.


## 0.4.47 – 2026-09-10

- Wasserfallfunktion von 0–100 Hz auf 0–140 Hz erweitert.
- Adaptive Pegelachse jetzt aus 2–140 Hz.
- Einblendung von L50 und L90 im Wasserfallbild.
- Einblendung des gespeicherten Kommentars im Wasserfallbild.
- PNG-Dateiname enthält jetzt Bewertung/Gain aus dem WAV-Namen sowie L50/L90.
- Wasserfall nicht nur für die letzte Aufnahme, sondern für jede lokal verfügbare Aufnahme.
- Anzeige in `Audio-Aufzeichnungen` jetzt für alle lokalen Aufnahmen statt nur für 20.
- Vorschau-Dialog zeigt zusätzlich Kommentar, L50/L90 und finalen PNG-Dateinamen.
- Keine neuen Supabase-Migrationen oder Edge-Function-Änderungen erforderlich.


## 0.4.46 – 2026-09-10

- Neue Belohnungsfunktion `Wasserfall letzte Aufnahme`.
- Lokale STFT/FFT-Auswertung der neuesten vorhandenen WAV-Datei.
- Fallendes Wasserfallspektrum: Zeit von oben nach unten.
- Frequenzdarstellung linear von 0 bis 100 Hz.
- Colormap Inferno.
- Automatische robuste Pegeladaption aus 2 bis 100 Hz:
  P5 - 3 dB bis P99,5 + 3 dB, mindestens 30 dB Dynamik,
  harte Grenzen -120 bis 0 dBFS.
- Tatsächlich verwendete dBFS-Grenzen werden im Bild angezeigt.
- Vorschau direkt in der App.
- PNG-Speicherung unter `Download/LFN-Scout/`.
- Zusätzliche Debug-Logs für Wasserfall-Erzeugung und PNG-Speicherung.
- Keine Supabase-Änderung gegenüber v0.4.45 erforderlich.


## 0.4.45 – 2026-09-09

- Testweise sichtbare lokale WAV-Speicherung unter `Download/LFN-Scout/` via MediaStore.
- Alte WAV-Dateien aus dem bisherigen internen Ordner `files/audio_recordings` werden beim ersten Start genau einmal gelöscht.
- Ein permanentes SharedPreferences-Migrationsflag verhindert spätere Wiederholungen dieser Löschung.
- Debug-Log-Historie von 200 auf 2000 Zeilen erhöht.
- Ausführliches Logging für Audio-Speicher, Aufnahme, Eingangswahl, Kommentar, Upload, Retry und 24-h-Bereinigung ergänzt.
- Supabase-Dateinamen enthalten die Observer-ID jetzt auch im Dateinamen:
  `BB001/BB001_2026-09-09_01-23-45_G30_UNP_R3.wav`.
- `upload_wav` auf Version 1.4.0 erhöht.
- Datenbankschema gegenüber v0.4.44 unverändert.


## 0.4.44 – 2026-09-09

- Nach jeder erfolgreichen Audioaufnahme erscheint ein optionaler Kommentardialog.
- Ohne Interaktion: automatischer Versand nach 10 s ohne Kommentar.
- Nach erster Nutzung des Kommentarfelds: sichtbarer 60-s-Inaktivitäts-Countdown; jede Texteingabe startet ihn neu.
- Manueller Sofortversand mit oder ohne Kommentar.
- Audio-Kommentar maximal 2000 Zeichen und Speicherung in `audio_recordings.comment`.
- Neues lokales Verzeichnis `files/audio_recordings`.
- Erfolgreich gesendete WAVs bleiben 24 h nach dem erfolgreichen Upload lokal erhalten.
- Nicht erfolgreich gesendete WAVs werden nicht automatisch gelöscht.
- Zu jeder lokalen WAV wird eine JSON-Metadatei mit Uploadstatus und Kommentar geführt.
- Unter „Zusatzfunktionen -> Audio-Aufzeichnungen“ werden tatsächlicher Pfad, lokale Aufnahmen und Uploadstatus angezeigt.
- „Pfad kopieren“, „Aktualisieren“ und „Erneut senden“ für nicht gesendete WAVs ergänzt.
- `upload_wav` auf Version 1.3.0 erweitert.
- Neue SQL-Migration `20260909_add_audio_recording_comment.sql`.
- WAV-Namensschema und 30-Minuten-Rating-Regel unverändert.


## 0.4.43 – 2026-09-09

- Build-Fehler aus v0.4.42 behoben: `BuildConfig` wird im App-Modul jetzt explizit erzeugt (`buildConfig = true`).
- Dadurch funktionieren alle drei Verwendungen von `BuildConfig.VERSION_NAME` in Beobachtungsmetadaten, sichtbarer Versionsanzeige und Systemstatus.
- README auf v0.4.43 nachgezogen.
- Keine Änderungen an Supabase, Audioaufnahme oder WAV-Namenslogik.


## 0.4.42 – 2026-09-09

- Systemstatus verwendet die App-Version jetzt dynamisch aus `BuildConfig.VERSION_NAME`.
- Auch sichtbare Versionsanzeige und Beobachtungsmetadaten verwenden die Build-Version statt einer hart codierten Versionsnummer.
- `Aufnahme abbrechen` ist während einer laufenden Aufnahme chromgelb hinterlegt.
- `README.md` vollständig auf den aktuellen Projektstand gebracht.
- Keine Änderungen an Supabase-Schema oder Edge Functions.


## 0.4.41 – 2026-09-09

- Neues LFN-Scout-App-Icon integriert.
- Adaptive Icon für Android 8+.
- Round Icon integriert.
- Monochrome/Themed-Icon-Variante integriert.
- Legacy-PNGs für mdpi, hdpi, xhdpi, xxhdpi und xxxhdpi integriert.
- `AndroidManifest.xml` verwendet jetzt `@mipmap/ic_launcher` und `@mipmap/ic_launcher_round`.
- Keine Änderungen an Audio-, Bewertungs- oder Supabase-Funktionalität.


## 0.4.40 – 2026-09-09

- Lesbare Dateinamen für neue WAV-Dateien im privaten Supabase-Storage.
- Format: `BB001/2026-09-09_01-23-45_G30_UNP_R3.wav`.
- Bei MIC wird `_MIC_` statt `_UNP_` verwendet.
- Letzte gültige Bewertung 0…5 wird als `R0`…`R5` angehängt, wenn sie beim Aufnahmestart höchstens 30 Minuten alt war.
- Falls keine gültige Bewertung innerhalb von 30 Minuten existiert, wird `RX` verwendet.
- Die Bewertungszuordnung erfolgt serverseitig in `upload_wav`; die App sendet keine Bewertung für den Dateinamen.
- `recorded_at` wird jetzt beim tatsächlichen Start der 120-s-Aufnahme erfasst und beim Upload unverändert mitgegeben.
- Die tatsächliche AudioSource (`UNP` oder `MIC`) wird beim Upload mitgegeben.
- UUID `recording_id` bleibt als technischer Datenbankschlüssel erhalten, erscheint aber nicht mehr im Storage-Dateinamen.
- Bestehende UUID-Dateien im Storage bleiben unverändert.
- Keine SQL-Migration; nur `upload_wav` neu deployen.
- Hinweis für späteren Sammeldownload ergänzt: bevorzugt portables `rclone` über Supabase S3.


## 0.4.39 – 2026-09-09

- Buildfix in `AudioRecorder.kt`: redundanten Elvis-Operator bei `usedSourceMode` entfernt.
- Ursache: Nach der vorherigen Nullprüfung mit sofortigem Return ist `usedSourceMode` für Kotlin bereits sicher non-null; `?:` war deshalb immer wirkungslos und wurde vom Compiler beanstandet.
- Funktionalität des AudioSource-Fallbacks bleibt unverändert.
- Alle UI-Änderungen aus v0.4.38 bleiben erhalten.


## 0.4.38 – 2026-09-09

- Buildfix für v0.4.37: veralteten Parameter `onAudioGainChange` am Aufruf von `ReminderSettingsScreen(...)` entfernt.
- Ursache: Der Gain-Regler war korrekt aus dem Einstellungsfenster entfernt worden, aber der frühere Callback wurde am Call-Site versehentlich stehen gelassen.
- Gain 0…+50 dB bleibt ausschließlich im Hauptfenster.
- `Audio heute: n / Limit`, CLIP rechts neben Verstärkung, kompakte Pausenbuttons und alle übrigen v0.4.37-Änderungen bleiben unverändert.


## 0.4.37 – 2026-09-09

- Gain-Regler 0…+50 dB wieder ins Hauptfenster zurückgebracht.
- `Audio heute: n / Limit` wieder dauerhaft im Hauptfenster.
- Gain aus den Audio-Einstellungen entfernt; dort bleiben nur AudioSource, physischer Eingang und `Inputs aktualisieren`.
- CLIP-Anzeige rechts neben `Verstärkung: +x dB` in derselben Zeile, um vertikalen Platz zu sparen.
- Separaten CLIP-Block unter der Aufnahme entfernt.
- Standardstatus `Bereit` wird nicht mehr als eigene Zeile angezeigt.
- Hauptpanel nutzt kleinere, nur dort wirksame Karten-Innenabstände und Abschnittsabstände.
- Kopfzeile inklusive Untertitel bleibt unverändert.
- Bewertungsbereich bleibt strukturell unverändert.
- `WAV · 120 s · 1000 Hz · mono · 16 Bit` bleibt im Hauptfenster.
- Verlauf/Rohdaten bzw. die bisherigen Verlaufsbereiche bleiben erhalten.
- Pausierung aus v0.4.36 bleibt unverändert: genau zwei kompakte Zeilen inklusive 12 h und `Ganz aus`.


## 0.4.36 – 2026-09-09

- Alle Audio-Einstellungen aus dem Hauptpanel in das Einstellungsfenster verschoben.
- Neue Einstellungssektion `Audioaufnahme` mit:
  - AudioSource (`Auto · UNPROCESSED bevorzugt`, `UNPROCESSED`, `MIC`)
  - physischem Audioeingang
  - `Inputs aktualisieren`
  - Gain 0…+50 dB
- Hauptpanel zeigt bei Audio nur noch das Nötigste: Start/Abbruch, Aufnahmezeit, CLIP, aktiven Input/Source-Status und Ergebnisstatus.
- Normale Anzeige `Audio heute n / Limit` aus dem Hauptpanel entfernt; `Tageslimit erreicht` bleibt nur im Fehler-/Grenzfall sichtbar.
- Pausierungsbuttons deutlich kompakter gestaltet.
- Pausenoption `12 h` ergänzt.
- Alle Pausenoptionen inklusive `Ganz aus` liegen jetzt in genau zwei Buttonzeilen:
  - Zeile 1: 1 h · 2 h · 4 h · 8 h
  - Zeile 2: 12 h · 24 h · 72 h · Ganz aus
- Java-17-/Gradle-Vorbereitung aus v0.4.34 und Audio-Routing aus v0.4.35 bleiben erhalten.


## 0.4.35 – 2026-09-09

- Audio-Input-Selector ergänzt: alle von Android gemeldeten Eingabegeräte werden über `AudioManager.GET_DEVICES_INPUTS` angeboten.
- Physischer Eingang: `Auto` oder konkretes internes/USB/Headset/Bluetooth-/Line-In-Gerät.
- AudioSource-Selector ergänzt: `Auto · UNPROCESSED bevorzugt`, `UNPROCESSED`, `MIC`.
- Standard bleibt `Auto`; dabei wird zuerst `UNPROCESSED` versucht und nur bei Nichtverfügbarkeit auf `MIC` zurückgefallen.
- Bei erzwungenem `UNPROCESSED` gibt es bewusst keinen MIC-Fallback; stattdessen klare Fehlermeldung.
- Gewählter physischer Eingang wird über `AudioRecord.setPreferredDevice(...)` angefordert.
- Tatsächlich aufgelöste AudioSource und geroutetes Eingabegerät werden während der Aufnahme angezeigt.
- `Inputs aktualisieren` liest die aktuell verfügbaren Eingänge neu ein.
- Nicht mehr vorhandene gespeicherte Geräte fallen automatisch auf `Auto` zurück.
- AudioSource und Geräte-ID werden persistent in DataStore gespeichert.
- Gain 0…+50 dB, Live-CLIP und Java-17/Gradle-Vorbereitung aus v0.4.34 bleiben unverändert.


## 0.4.34 – 2026-09-09

- Bugfix in `PREPARE_PROJECT.bat`: nach dem Finden von Java 17 wird `JAVA_HOME` für die laufende BAT-Sitzung auf dieses JDK gesetzt.
- `PATH` wird für die laufende BAT-Sitzung um `%JAVA_HOME%\bin` ergänzt.
- Vor dem direkten Bootstrap-Gradle-Lauf wird `java.exe -version` explizit geprüft.
- Durch `setlocal` bleibt das globale Java 8 unverändert.
- Android-Studio-Buildweg bleibt unverändert: `gradle.properties` erhält weiterhin `org.gradle.java.home` mit dem gefundenen Java-17-Pfad.
- Java-17-Suche, Wiederverwendung vorhandener JDKs, Wrapper-Bootstrap-Fix und Download-Fortschrittsanzeige bleiben erhalten.


## 0.4.33 – 2026-09-09

- Bugfix in `PREPARE_PROJECT.bat`: `gradlew.bat` wird nicht mehr zum Erzeugen des Wrappers verwendet, während diese Datei selbst überschrieben wird.
- Zuerst wird mit `gradlew.bat --version` nur der Gradle-8.13-Bootstrap sichergestellt.
- Der eigentliche `wrapper`-Task läuft danach direkt über `%GRADLE_USER_HOME%\brummbeobachter-bootstrap\gradle-8.13\bin\gradle.bat`.
- Erst nach erfolgreicher Wrapper-Erzeugung wird der projektlokale Java-17-Launcher wieder nach `gradlew.bat` kopiert.
- Java-17-Suche, Wiederverwendung vorhandener JDKs und Download-Fortschrittsanzeige bleiben unverändert.


## 0.4.32 – 2026-09-09

- Bugfix der Java-17-Erkennung unter Windows PowerShell 5.1.
- `java.exe -version` / `-XshowSettings` wird jetzt über `System.Diagnostics.Process` ausgeführt.
- stdout und stderr werden explizit eingelesen; normale Java-Ausgabe auf stderr wird nicht mehr als PowerShell-Fehler behandelt.
- Dadurch werden vorhandene Java-17-Installationen und heruntergeladenes Temurin 17 korrekt erkannt.
- Ausgabe von `EnsureSuccessStatusCode()` wird unterdrückt; keine lange HTTP-Objektausgabe mehr.
- Suchreihenfolge und Download-Fortschrittsanzeige bleiben unverändert.


## 0.4.31 – 2026-09-09

- Bugfix in `tools/prepare-java17.ps1`: Funktionsparameter `$Home` in `$JdkHome` umbenannt.
- Ursache: PowerShell behandelt `$Home` und die eingebaute schreibgeschützte Variable `$HOME` identisch.
- Java-17-Suchlogik aus v0.4.30 bleibt unverändert.
- Audiofunktionen aus v0.4.29/v0.4.30 bleiben unverändert.


## 0.4.30 – 2026-09-09

- Projektvorbereitung sucht Java 17 zuerst lokal und in vorhandenen Android-Studio-Projekten.
- Bereits vorhandenes JDK 17 wird nur referenziert; kein Kopieren nötig.
- Suche zusätzlich in Android-Studio-/Gradle-JDK-Verzeichnissen (`.jdks`, `.gradle/jdks`) und üblichen Installationspfaden.
- Nur wenn kein geeignetes Java 17 gefunden wird, wird Temurin 17 heruntergeladen.
- Fallback-Download zeigt Fortschrittsbalken, Prozent, geladene MB und Gesamtgröße.
- Gefundener Java-17-Pfad wird in `.tools/java17.path` zwischengespeichert.
- `gradle.properties` erhält automatisch `org.gradle.java.home` mit dem gefundenen JDK-17-Pfad.
- Globales Java 8 bleibt unverändert.
- Audiofunktionen aus v0.4.29 unverändert: 0…+50 dB Gain und Live-CLIP-Anzeige.


## 0.4.29 – 2026-09-08

- Audio-Gain von 0…+30 dB auf 0…+50 dB erweitert, weiterhin 1-dB-Schritte.
- Live-CLIP-Anzeige während der 120-s-Aufnahme.
- CLIP-Anzeige bleibt nach erstem Übersteuerungsereignis bis Aufnahmeende gesetzt.
- Laufender und abschließender Clip-Zähler.
- `upload_wav` akzeptiert Gain bis +50 dB.
- Java-17-Projektintegration über festen Pfad `.tools/jdk17/current`.
- `PREPARE_PROJECT.bat` schreibt `org.gradle.java.home` projektlokal mit absolutem Pfad.
- Wrapper-Erzeugung überschreibt den Java-17-Launcher nicht mehr dauerhaft; der Launcher wird danach automatisch wiederhergestellt.
- Globales Java 8 bleibt unverändert.
- Compose-Warnungen aktualisiert: `HorizontalDivider` und Lambda-Overload von `LinearProgressIndicator`.
- unnötiger Safe-Call in `AudioRecorder.kt` bereinigt.
- versionName 0.4.29 / versionCode 69.


## 0.4.28 – 2026-09-08

- Echte Audioaufnahme im Hauptpanel.
- 15-s-Countdown mit Warnhinweis vor Aufnahmebeginn.
- Aufnahmeformat: 120 s, 1000 Hz, mono, PCM 16 Bit.
- Android-Aufnahme intern mit 8000 Hz, anschließend 8:1 auf 1000 Hz reduziert.
- Einstellbare Audioverstärkung 0…+30 dB in 1-dB-Schritten.
- Clipping-Erkennung und Anzeige.
- Automatischer Upload nach Aufnahmeende an `upload_wav`.
- Fehlgeschlagene Uploads lassen die WAV lokal unter `files/audio_pending` liegen.
- Audio-Tageslimit Standard 10; editierbar nur im Entwicklermodus.
- Persistenter Entwicklermodus; Abschalten ohne Passwort möglich.
- Entwicklerpasswort = dritte Vierergruppe des Teilnehmerschlüssels rückwärts.
- Wecklautstärke 0…100 %; wird auf Android STREAM_ALARM angewendet.
- Server-Schlüsselstatus active / paused / revoked.
- Bei erkanntem `key_revoked` wird der Entwicklermodus deaktiviert.
- Zusatzfunktionen Rohdaten/Debug/Systemdetails werden im Normalmodus ausgeblendet.
- Vertikale Abstände gegenüber v0.4.27 nochmals reduziert:
  Bildschirm 7 dp, Abschnitt 3 dp, Karte 7 dp, Feld 2 dp.
- In-Process-Tonpakete und Fullscreen-Timeout-Fix bleiben erhalten.
- versionName 0.4.28 / versionCode 68.


## 0.4.27 – 2026-09-08

- Neues Farbschema:
  - Primärfarbe dunkleres Türkis `#1F8F8A`
  - Akzentfarbe sattes Chromegelb `#E0B000`
- Hauptbuttons und aktive Systemelemente verwenden Türkis.
- Ausgewählte Bewertung wird chromegelb hervorgehoben.
- Vertikale Abstände in allen drei Panels deutlich reduziert:
  - Bildschirmrand 10 dp
  - Abschnittsabstand 5 dp
  - Karteninnenabstand 10 dp
  - Feldabstand 4 dp
- Bewertungsbuttons kompakter auf 54 dp Höhe.
- Kommentarfeld kompakter (1–3 Zeilen).
- Drei-Panel-Facelift aus v0.4.26 unverändert erhalten.
- In-Process-Tonpakete aus v0.4.25 unverändert erhalten.
- Fullscreen-Timeout-Fix aus v0.4.24 unverändert erhalten.
- versionName 0.4.27 / versionCode 67.


## 0.4.26 – 2026-09-08

- Das zuvor als separate v0.4.25-Facelift-Ausgabe vorbereitete Drei-Panel-Facelift
  ist jetzt die reguläre App-Version 0.4.26.
- Hauptpanel, Einstellungen (`⚙`) und Zusatzfunktionen (`⋮`) bleiben unverändert.
- Pausensteuerung bleibt im Hauptpanel.
- Radius-Histogramme bleiben kompakt als 2×2-Raster.
- Audio-Karte bleibt sichtbar; Aufnahme/Upload ist weiterhin nicht aktiviert.
- In-Process-Tonpakete aus v0.4.25 bleiben erhalten.
- Fullscreen-Timeout-Fix aus v0.4.24 bleibt erhalten.
- versionName 0.4.26 / versionCode 66.


## 0.4.25 Facelift – 2026-09-08

- Android-Version bleibt bewusst 0.4.25; ursprüngliche v0.4.25 bleibt als separate Rückfall-ZIP erhalten.
- Oberfläche in drei Panels gegliedert:
  - Hauptpanel
  - Einstellungen über `⚙`
  - Zusatzfunktionen über `⋮`
- Pausensteuerung 1/2/4/8/24/72 h und „Ganz aus“ ins Hauptpanel verschoben.
- Teilnehmerschlüssel ins Einstellungspanel verschoben.
- CSV/XLSX und Debug-Protokoll ins Zusatzfunktionen-Panel verschoben.
- Radius-Histogramme kompakt als 2×2-Raster.
- Audio-Aufnahmebereich im Hauptpanel sichtbar:
  WAV 120 s / 1000 Hz / mono; Schaltfläche bewusst deaktiviert, weil Android-Aufnahme/Upload noch nicht implementiert ist.
- Bestehende 5-s-Kommentar-Auto-Sendelogik beibehalten.
- In-Process-Tonpakete aus v0.4.25 unverändert beibehalten.
- Fullscreen-Timeout-Fix aus v0.4.24 unverändert beibehalten.


## 0.4.25 – 2026-09-07

- Experimentelle Alternative für die drei Erinnerungs-Tonpakete.
- v0.4.24 bleibt unverändert als Rückfallversion erhalten.
- Paket 1 wird weiterhin beim eigentlichen Reminder sofort gestartet.
- Paket 2 und Paket 3 werden nicht mehr als eigene AlarmManager-Weckereignisse geplant.
- Die beiden Folgetöne werden innerhalb des laufenden App-Prozesses zeitgesteuert.
- Zeitpunkte bleiben dynamisch aus dem Antwortfenster berechnet:
  - Paket 1 = 0 %
  - Paket 2 = 1/3
  - Paket 3 = 2/3
  - Nichtantwort-Timeout = 100 %
- Beispiel bei 5 Minuten: 0 s / 100 s / 200 s / Timeout 300 s.
- Beim Start des neuen Schemas werden mögliche alte PendingIntents für Paket 2/3 entfernt.
- Bewertung, Pause oder Timeout annullieren die noch ausstehenden In-Process-Tonpakete.
- Fullscreen-Timeout-Fix aus v0.4.24 bleibt erhalten.
- VersionName 0.4.25 / VersionCode 65.

## 0.4.24 – 2026-09-07

- Zweiter Fix für die nicht verschwindende Fullscreen-/Sperrbildschirmansicht.
- Bisheriger Broadcast-only-Schließweg war auf dem Gerät nicht zuverlässig.
- Neue zentrale `ReminderFullscreenController`-Instanz mit schwacher Referenz auf die sichtbare `ReminderAlarmActivity`.
- Activity registriert sich beim Öffnen und deregistriert sich in `onDestroy()`.
- Beim Antwortfenster-Timeout wird die sichtbare Activity direkt im App-Prozess geschlossen.
- Der eigentliche `finishAndRemoveTask()`-Aufruf läuft auf dem Main-Thread.
- Der bisherige Broadcast bleibt zusätzlich als Fallback bestehen.
- Notification, Ton und Vibration werden weiterhin vor dem Schließen gestoppt.
- `onDestroy()` bleibt ohne globales Alert-Stop, damit Tonpakete 2/3 nicht regressieren.
- VersionName 0.4.24 / VersionCode 64.

## 0.4.23 – 2026-09-04

- Automatisches Senden nach Auswahl eines Ratings ergänzt.
- Timeout 1: Rating gewählt und Kommentarfeld nicht angefasst → nach 5 Sekunden automatisch senden.
- Timeout 2: Kommentarfeld angeklickt/editiert → neuer 5-Sekunden-Timeout; danach Rating plus aktueller Kommentar automatisch senden.
- Kommentarinteraktion startet den zweiten Timeout nur einmal; weiteres Tippen verlängert ihn nicht fortlaufend.
- Manueller Button `Bewertung senden` sendet weiterhin sofort.
- `Abbrechen` verwirft Rating/Kommentar und beendet den Auto-Sende-Zustand.
- Beide Zeiten zentral in `CommentAutoSendPolicy` definiert.
- Keine Änderung an Supabase-Schema oder Edge Functions erforderlich.
- VersionName 0.4.23 / VersionCode 63.

## 0.4.22 – 2026-09-03

- Optionaler Kommentar zu Ratings 0..5.
- Ablauf geändert: Rating wählen → Kommentar optional → Bewertung senden.
- Kommentar maximal 2000 Zeichen.
- Automatische Nichtantwort `rating=-1` bleibt ohne Kommentar.
- Kommentar wird im persistenten Offline-Puffer mitgeführt.
- Supabase-Migration für `observations.comment` enthalten.
- Aktualisierte `submit_observation` Edge Function enthalten.
- Neue Edge Function `get_all_observations`.
- Vollständiger eigener Datenabruf paginiert (500 Datensätze pro Seite).
- Lokaler Export aller eigenen Beobachtungen als CSV oder XLSX.
- CSV: UTF-8 BOM + Semikolon für gute Excel-Kompatibilität unter deutscher Locale.
- XLSX wird ohne zusätzliche Android-Bibliothek direkt erzeugt.
- Export enthält UTC- und lokale Zeitspalten.
- VersionName 0.4.22 / VersionCode 62.

## 0.4.21 – 2026-09-03

- Bugfix: Sperrbildschirm-/Fullscreen-Erinnerung wird nach Antwortfenster-Timeout geschlossen.
- Timeout löscht weiterhin Ton, Vibration und Notification.
- Zusätzlich sendet der Timeout ein app-internes Schließsignal.
- `ReminderAlarmActivity` empfängt das Signal und beendet sich mit `finish()`.
- Normales `onDestroy()` bleibt weiterhin ohne globales Alert-Stop, damit Tonpakete 2/3 nicht regressieren.
- Neue Logs:
  - `FULL_SCREEN_CLOSE_REQUESTED`
  - `FULL_SCREEN_ACTIVITY_CLOSE_SIGNAL`
- Keine Änderung an Histogrammen, Radiuslogik, Offline-Retry oder Rohdaten.
- VersionName 0.4.21 / VersionCode 61.

## 0.4.20 – 2026-09-03

- Radius-Histogramme jetzt als echte horizontale Balken.
- Je 30-Minuten-Block eigene Normierung auf den höchsten Wert des Blocks.
- Der höchste Rating-Balken eines Blocks entspricht 100 % Balkenlänge.
- Alle anderen Ratings werden proportional dazu dargestellt.
- `rating = -1` bleibt separat und nimmt nicht an der Normierung teil.
- Vier 30-Minuten-Blöcke bleiben untereinander angeordnet.
- Keine Änderung an Backend, Radiuslogik oder Rohdaten.
- VersionName 0.4.20 / VersionCode 60.

## 0.4.19 – 2026-09-02

- Compiler-Bugfix in `LFNScoutScreen`.
- `radiusAnalysisEnabled`, `radiusHistogramStatus` und
  `radiusHistogramResult` werden jetzt explizit als Parameter übergeben.
- Direkter, ungültiger Zugriff von `LFNScoutScreen` auf
  `reminderSettings` entfernt.
- Keine funktionale Änderung an Radiuslogik, Edge Functions,
  Histogrammen oder Rohdaten.
- VersionName 0.4.19 / VersionCode 59.

## 0.4.18 – 2026-09-02

- Radius-Edge-Function liefert zusätzlich die Einzelbeobachtungen im Radius.
- Stabile `observer_id` wird pro Beobachtung übertragen.
- Rohfelder: rating, scheduled_time, response_time, latitude, longitude.
- Android-Result-Modell `RadiusRawObservation` ergänzt.
- Parser für Rohbeobachtungen ergänzt.
- Sichtbare Histogramm-Oberfläche bleibt unverändert.
- Gespeicherte GPS-Koordinaten werden unverändert zurückgegeben.
- Bei fehlendem GPS bleiben Rohkoordinaten 0.0/0.0.
- Basisstandort dient nur intern als Fallback für die Radiusentscheidung.
- Zugangsschlüssel/key_hash werden nicht ausgegeben.
- VersionName 0.4.18 / VersionCode 58.

## 0.4.17 – 2026-09-02

- Optionale 2h-Radius-Auswertung nach erfolgreicher Bewertung.
- Option standardmäßig AUS.
- Konfigurierbarer Radius, Default 600 km.
- Mittelpunkt: aktuelle GPS-Position, sonst Basisstandort.
- Vier feste 30-Minuten-Histogramme für Ratings 0..5.
- `-1` separat als Anzahl Nichtantworten.
- Anzeige von Radius, Standortquelle, Beobachterzahl und Beobachtungszahl.
- Neue Edge Function `get_radius_histograms`.
- Edge Function liefert ausschließlich aggregierte Ergebnisse.
- Fremde GPS-Koordinaten und Observer-IDs werden nicht an die App ausgegeben.
- Basisstandort-Fallback serverseitig.
- Persistente Radius-Einstellungen und Debug-Logging ergänzt.
- VersionName 0.4.17 / VersionCode 57.

## 0.4.16 – 2026-09-02

- Zeitraster `xx:15 / xx:45` greift jetzt ab einschließlich 30 Minuten.
- 15-Minuten-Intervall bleibt relativ.
- Persistenter Offline-Puffer erhält zusätzlichen 2-Stunden-WorkManager-Retry.
- 2-Stunden-Retry läuft nur solange der Puffer nicht leer ist.
- NetworkType.CONNECTED für den periodischen Retry.
- Bei nicht retrybaren Übertragungsfehlern sichtbare Android-Warnung:
  `Dauerhafte Störung der Datenübertragung`.
- Typische HTTP 400/401/403 werden weiterhin nicht neu gepuffert.
- Warn- und 2h-Retry-Logging ergänzt.
- VersionName 0.4.16 / VersionCode 56.

## 0.4.15 – 2026-09-02

- Persistenter lokaler Offline-Sendepuffer für Ratings `-1..5`.
- Netzwerkfehler sowie HTTP 408/429/5xx werden gepuffert.
- Permanente HTTP-4xx-Fehler werden nicht automatisch gepuffert.
- Originale `event_id` bleibt beim Retry erhalten.
- Duplikate im Puffer werden anhand `event_id` verhindert.
- Puffer wird erst nach bestätigtem Server-Erfolg gelöscht.
- Teilnehmer-Key wird nicht im Puffer gespeichert.
- Retry beim App-Start, Exact-Reminder und WorkManager-Reminder.
- Debug-Logging für Queue und Retry ergänzt.
- VersionName 0.4.15 / VersionCode 55.

## 0.4.14 – 2026-09-02

- Für Erinnerungsintervalle >30 min neues Zeitraster `xx:15 / xx:45`.
- Nominaler Termin `Basiszeit + Intervall` wird auf den nächstgelegenen
  30-Minuten-Slot mit +15-Minuten-Versatz gerundet.
- Bei Gleichstand wird der spätere Slot gewählt.
- Intervalle <=30 min bleiben relativ und unverändert.
- Pausenende bleibt ein exakter Termin und wird nicht gerastert.
- Debug-Logging `REMINDER_NEXT_TIME_POLICY` ergänzt.
- VersionName 0.4.14 / VersionCode 54.

## 0.4.13 – 2026-09-02

- Automatische Nichtantwort nach Ablauf des Antwortfensters.
- `rating=-1`, `response_time=null`, `wav_present=false`.
- Keine WAV-Datei für automatische Nichtantwort.
- Notification/Ton/Vibration werden beim Timeout beendet.
- Bewertung/Pause/Ausschalten annullieren den Timeout; bloßes Öffnen der App nicht.
- Timeout gilt für ALARM und VERY_NOTICEABLE.
- Timeout-/Submit-Logging ergänzt.
- Netzwerkfehler werden geloggt; Offline-Retry bleibt offen.
- VersionName 0.4.13 / VersionCode 53.

## 0.4.12 – 2026-09-02

- Root Cause aus Gerätestest behoben: `FULLSCREEN_DESTROY` rief `ReminderAlertController.stop()` auf und löschte dadurch Paket 2/3.
- `onDestroy()` der Full-Screen-Activity gibt jetzt nur Activity-/WakeLock-Ressourcen frei.
- Tonpakete werden nur noch durch echte Aktionen wie Bewertung/Pause oder Reminder-Neuplanung abgebrochen.
- Mehrfaches Öffnen/Zerstören der Full-Screen-Activity beeinflusst die Paketplanung nicht mehr.
- VersionName 0.4.12 / VersionCode 52.

## 0.4.11 – 2026-09-02

- Compilerfehler aus v0.4.10 behoben:
  `Unresolved reference 'playSingleAlarmPackage'`.
- Fehlende Einzelpaket-Wiedergabefunktion ergänzt.
- Jedes Paket spielt den gewählten Ton genau einmal.
- Paket-Vibration läuft einmal und nicht wiederholt.
- Noch ausstehende Tonpakete bleiben weiterhin durch den Generation-Guard abbrechbar.
- VersionName 0.4.11 / VersionCode 51.

## 0.4.10 – 2026-09-02

- Fehlerbehebung: drei Tonpakete hängen nicht mehr von der Full-Screen-Activity ab.
- Wecker-Modus plant Paket 1 direkt beim Reminder und zwei Alarm-Paket-Broadcasts bei 1/3 und 2/3 des Antwortfensters.
- Full-Screen-Activity ist nur noch Bedienoberfläche und startet keinen Ton mehr.
- Alarm-Notification verwendet neuen tonlosen Kanal `bb_urgent_alarm_packages_v1`, damit Paket 1 nicht doppelt ertönt.
- Vibration im Wecker-Modus erfolgt nur noch zusammen mit den drei Tonpaketen.
- Bewertung/Pause/Reminder-Neuplanung cancelt ausstehende Tonpakete.
- Neue Debug-Ereignisse `ALARM_TONE_PACKAGE_SCHEDULED` und `ALARM_TONE_PACKAGE_RECEIVED`.


## 0.4.10 – 2026-09-02

- Wecker-Modus von dauerhafter Tonwiederholung auf drei einzelne Tonpakete umgestellt.
- Jedes Tonpaket spielt den ausgewählten Ton genau einmal (`isLooping=false`).
- Pakete bei 0 %, 33 1/3 % und 66 2/3 % des Antwortfensters.
- Bei 9 Minuten Antwortfenster: 0:00, 3:00 und 6:00 Minuten.
- Vibration je Paket einmalig statt dauerhaft wiederholt.
- Bewertung/Pause invalidiert über den bestehenden Generation-Guard alle späteren Pakete.
- Debug-Ereignisse `ALARM_TONE_PACKAGE_STARTED` und `ALARM_TONE_PACKAGES_CANCELLED`.
- Unit-Test für die Paketverteilung ergänzt.
- VersionName 0.4.10 / VersionCode 49.

## 0.4.8 – 2026-09-02

- Direkter Reminder-Tonselbsttest ergänzt.
- Button `Ton jetzt abspielen` verwendet den gespeicherten Erinnerungston.
- Button `Ton stoppen` beendet den Selbsttest sofort.
- Der aktuelle Tonname wird ausdrücklich angezeigt.
- Der Selbsttest umgeht WorkManager, Exact Alarm und Notification vollständig.
- Keine Vibration beim direkten Tonselbsttest.
- Sichtbare Laufzeitergebnisse `PASS`, `FAIL` und `Ton gestoppt`.
- Persistentes Logging mit `SOUND_SELFTEST_STARTED`,
  `SOUND_SELFTEST_STOPPED` und `SOUND_SELFTEST_FAILED`.
- Der vorhandene Generation-Guard verhindert weiterhin ein Wiederanlaufen
  nach einem gleichzeitigen Stoppen.
- Der Selbsttest wird vor Tonwahl, Notification-Test und beim Schließen der
  Erinnerungseinstellungen beendet.
- Unit-Test für PASS/FAIL-Anzeigen und stabile Log-Ereignisnamen ergänzt.
- VersionName 0.4.8 / VersionCode 48.

## 0.4.7 – 2026-09-02

- Persistente Option `App nach Aktion automatisch in den Hintergrund`.
- Standardwert AUS.
- Optionaler Hintergrundwechsel nach erfolgreicher Bewertung.
- Optionaler Hintergrundwechsel nach Pause 1/2/4/8/24/72 h.
- Optionaler Hintergrundwechsel nach `Ganz ausschalten`.
- Kein automatischer Hintergrundwechsel bei Übertragungsfehlern.
- Kein automatischer Hintergrundwechsel nach `Erinnerungen einschalten`.
- App wird mit `moveTaskToBack(true)` nur in den Hintergrund verschoben.
- Debug-Logging für Einstellung und Auslöser ergänzt.
- VersionName 0.4.7 / VersionCode 47.


## Dokumentationsrevision 2026-09-19 – unveränderter Code v0.4.85

- Vorhandene Rekonstruktionsdokumente mit drei vom Nutzer gelieferten Chatverläufen vertieft; Chat-Traceability, 31 Rückstellungen samt Quellenstatus und Methodenhinweise ergänzt.
- Technische Invarianten, offene Funktionen und E2E-Abnahmeplan ausdrücklich voneinander abgegrenzt.
- Keine Änderung der Android-Quellen, Buildkonfiguration, Backend-Daten oder App-Versionsnummer. Kein erneuter Gradle- oder Restore-Erfolgsnachweis.


## Entwicklungsnachtrag 2026-09-19 – Analyzer v0.7, Phase 1 (keine neue Release-Version)

- Normative Analyzer-Übergabe v0.7 vollständig aufgenommen, ohne bestehende Root-/Projekt-MDs zu überschreiben.
- Ist-/Soll-Mapping gegen reale v0.4.85-Quellen ausgefüllt, CaptureOwner-Konflikte und Gates dokumentiert.
- Reinen FFT-/dBFS-DSP-Kern samt isolierten Tests/Host-Fixures ergänzt. Kein neues Nutzerpanel, keine Recorder-Umbauten, kein neuer Audiopfad, kein PNG-Autosave und keine Freigabe behauptet.

## 2026-10-10 – v0.5.27 Rolling-Betriebsdauer Source Candidate (zusätzlicher Zwischenstand)

- DataStore-Einstellung `rollingMaxOperationHours`, Default 12, zulässig 1–168 Stunden.
- Audio-Einstellungen: Eingabefeld und Speichern der maximalen Rolling-BETRIEBSDAUER.
- Service prüft Zeitablauf und stoppt Rolling sowie gekoppelten `ROLLING_8K`-Analyzer; 30-min-Ringkapazität bleibt unverändert.
- `ANLEITUNG_BUILD_TEST_v0.5.27.md` und `docs/project/ROLLING_AND_STATISTICS_REQUIREMENTS_v0.5.27.md` ergänzt.
- **Kein Android-Build-PASS:** Gradle-8.13-Distribution nicht verfügbar (`UnknownHostException: services.gradle.org`). Offene Statistik-/CSV-/SD-Funktionen sind nur spezifiziert, nicht implementiert.

## Entwicklungskandidat v0.5.56 – Phase 3O Teilstufe (14.10.2026)
- Fail-closed Transaktionsschutz als isolierter, nicht angeschlossener Adaptervertrag ergänzt. Stop-required Audio-/FFT-Felder werden noch **nicht** aktiviert; reine Phase-3N-Anzeige bleibt lesend.
- Statische Regression `tools/verify_lfn056_phase3o.py` und Dokumentation ergänzt. Keine Änderung an Audio-Hardware, Prefs, Cloud, Termux-v4-Skript oder Workflows. Gradle bleibt `0.5.55` / `213`.

### v0.5.56 – Phase 3O, zweite Teilstufe (Quellkandidat, nicht Release)
- Neue `AnalyzerSetupExactComparison.kt` für numerisch präzise und vollständige JSON-Statusvergleiche; Preflight und Runtime-Vergleich verwenden sie.
- Transaktions-No-op nur bei vollständiger Werte- und Schlüsselgleichheit; zusätzliche Live-Schlüssel werden als unvergleichbar ausgewiesen.
- Vier JUnit-Tests ergänzt (nicht lokal ausgeführt). Kein Audio-/FFT-Apply freigegeben; Gradle weiterhin `0.5.55` / `213`.


## Phase 3O – dritte Teilstufe: Legacy-Grenzwertschutz (14.10.2026)

`AnalyzerRuntimeSetupSnapshot.capture` darf Filtergrenzen nicht mehr stillschweigend von `Double` nach `Int` abschneiden. `exactLegacyFilterHz` akzeptiert nur endliche, exakt ganzzahlige Werte im Int-Bereich; bei echten Bruchteilen bricht die *lesende* Erfassung mit einer klaren Fehlermeldung ab. Grund: Das aktuelle Legacy-JSON-Schema deklariert `filter_min_hz` und `filter_max_hz` als Integer. Dieser Guard dient der Integrität, **ist keine Unterstützung für Bruch-Hz**. Für eine vollwertige Übernahme muss die Schema-/Controller-Frage explizit geklärt werden. `AnalyzerSetupFormat` prüft Integerwerte jetzt über `BigDecimal.toBigIntegerExact()` statt verlustbehaftetem `Double`. Vier JUnit-Regressionsfälle hinzugefügt, deren Ausführung durch Gradle aussteht. Keine Änderung an Recordern, Audiopipeline, SharedPreferences, JSON-Dateien, Workflows oder Gradle-Version. `Plan.executable=false`, kein Adapter aktiv.

## v0.5.56 – Phase 3O / Teilstufe 7 (Quellkandidat, noch kein Release)
- Nur lesender Audio-/Timing-STOP-Checkpoint mit 12 Feldern und Stale-Readback im Controller ergänzt; keine Aufnahme-/Service-/Hardwareumschaltung.
- `docs/project/CURRENT_STATE.md` als fortzuführende verbindliche Übergabe eingeführt.
- Regression `tools/verify_lfn056_phase3o_audio_timing_checkpoint.py`, kein neuer Kotlin-/GitHub-/Gerätetest behauptet. Gradle 0.5.55 / 213 unverändert.

## v0.5.56 – Phase 3O/9 Quellkandidat (14.10.2026)
- `LiveAnalyzerController.start`: `running=true` erst nach erfolgreicher lokaler Buffer-/Subscriber-Initialisierung. Bei Initialisierungsfehlern best-effort-Session-/Buffer-Cleanup, STOP-Status und ursprünglichen Fehler weitergeben.
- Keine Freigabe der JSON-Audio-/FFT-Umschaltung, kein Recorder-/Mikrofon-/Preferences-Rollback. `docs/project/CURRENT_STATE.md` aktualisiert; neue statische Regression.
- Gradle unverändert 0.5.55/213. GitHub-TEST für Teilstufe 8 laut Nutzer grün; Teilstufe 9 noch nicht gebaut.

## v0.5.56 Entwicklung – Phase 3O/13 (Quellkandidat)
- MON-WAV-Start und zukünftige synchrone Setup-Reservierung über `AnalyzerSetupTransitionCoordinator` gegeneinander koordiniert. Drei JUnit-Tests; weitere Recorder-Übergänge und Executor-Anbindung offen. Gradle 0.5.55/213 unverändert.

### Phase 3O/15 – Asynchrone Service-Starts konservativ erfassen (Quellkandidat)
- Nach erfolgreicher Rolling-/Direct-`startForegroundService`-Anforderung hält der Coordinator einen `inFlight`-Marker bis `Service.onDestroy`. Setup-Reservierung wird bei Marker verweigert.
- Fehlgeschlagene/ausnahmewerfende Startanforderungen geben den Marker frei; doppelte Startanforderungen desselben Service bleiben mengenbasiert erfasst.
- Drei ergänzende JUnit-Tests und statischer Lifecycle-Test. Keine Freigabe des Setup-Executors.
- Sicherheitsgrenze: Lebenszyklus-Freigabe ist noch kein atomarer Nachweis, dass alle Capture-Coroutinen vollständig beendet sind. Falls Android den Start nie zustellt, bleibt die Marker-Sperre konservativ bestehen.

## Phase 3O / AP26 – TEST-Installations-/Versionskorrektur (15.10.2026)

- Nutzer meldet: AP25 TEST-APK lässt sich über bestehende TEST-Installation nicht installieren; die Versionsnummer blieb 0.5.55/213.
- Gradle `defaultConfig` nach ausdrücklicher Beanstandung des fehlenden Hochzählens auf `versionName = "0.5.56"`, `versionCode = 214` angehoben. Diese Änderung betrifft APK-Versionierung von TEST **und** REGULAR bei einem künftigen Build; es wird keine REGULAR-APK installiert.
- Separater TEST-Paketname (`.githubdebug`), unveränderte Workflows und Signaturwege. Keine Änderungen an Audio-, Setup- und Cloud-Implementierung gegenüber AP25.
- Android kann auch APKs mit gleicher versionCode aktualisieren; der genaue Installationsfehler ist noch nicht diagnostiziert. Bei `INSTALL_FAILED_UPDATE_INCOMPATIBLE` Signatur prüfen und nicht blind Daten löschen/deinstallieren.
- Quellkandidat: Android/Gradle-Test und Hardwareinstallation noch offen. AP25-Android-Prüfung ist nicht als erfolgreich zu bewerten.

## Phase 3O / AP27 – Berechtigungsdialog (15.10.2026)
- Startdialog für Mikrofon- und GPS-/Standortfreigabe bei fehlenden Android-Laufzeitrechten, mit Auswahl „Später“.
- Keine Änderung an bestehender Audioverarbeitung oder Standortermittlung.
- TEST/REGULAR Gradle-Version angehoben auf 0.5.57 / 215; REGULAR nicht gebaut oder installiert.


### AP31 – kombinierte STOP-Entwürfe (Entwicklungsstand)
- Analyzer und MON lassen sich gemeinsam als nicht persistenter JSON-STOP-Entwurf anwenden.
- Bei MON-Fehler werden beide Parametergruppen zurückgesetzt und verglichen.
- Produktive dauerhafte JSON-Aktivierung bleibt ausdrücklich offen.


## v0.5.89 / versionCode 247 (Testkandidat)
- Unabhängiger JSON/CSV-Systemtestexport-Validator mit echtem FAIL/SKIP-Freigabegate.
- Sechs neue Validator-Regressionstests; bestehender Android-Testpfad unverändert.
- Noch keine Android-Buildfreigabe; produktive fünf SKIP-Fälle weiterhin offen.
