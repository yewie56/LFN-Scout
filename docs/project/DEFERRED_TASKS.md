### Phase 3O – nach Arbeitspaket 13

- Hardware-End-to-End-Tests, asynchrone Service-Start/Stop-Races, tatsächlicher Controller-Adapter und Fehler-Rollback bleiben offen. Keine Freischaltung von `Plan.executable` ohne diese Nachweise.

### Phase 3O – Weiter offen nach Arbeitspaket 12

- End-to-End-Atomarität der asynchronen Service-/Audio-Starts, Controller-Adapter, Hardware-Rollback und Android-Gerätetests. `Plan.executable=false` bis zur vollständigen Verifikation beibehalten.

## Phase 3O – weiterhin offen nach Arbeitspaket 11

- Gemeinsame atomare Koordination sämtlicher realer asynchroner Audio-Start-/Stop-Pfade auch über Service-Lebenszyklen nachweisen.
- Produktiven Controller-Adapter STOP–Reconfigure–START integrieren, transaktionalen Rollback einschließlich Hardware-Fehlerinjektion absichern und Android-Gerätetests durchführen.
- Erst danach Freischaltentscheidung für `Plan.executable` treffen.

## Phase 3O / Arbeitspaket 10 – weiterhin offen

- [ ] Vollständig atomare Koordination der tatsächlichen Audio-Hardware aller Analyzer-/MON-/Scout-/Rolling-/Direct-Pfade einschließlich asynchronem Cleanup nachweisen. Die Legacy-Start-Helper sind nur ablehnende synchrone Prüfungen.
- [ ] Produktiver Controller-Adapter, Hardware-STOP–Reconfigure–START, Rollback/Fault-Injection und Android-Gerätetest vor Freischaltung nachziehen. `Plan.executable=false` bis dahin.
- [ ] Paket 10 GitHub-JUnit/Android-Build prüfen.

## Phase 3O / Arbeitspaket 9 – offen

- [ ] Gesamte Audio-Hardware-/Lifecycle-Sperrung auch bei asynchroner Aufnahmefreigabe und allen Analyzer-/MON-/Scout-/Rolling-/Direct-Pfaden nachweisen. Das neue Owner-Snapshot vor dem getrackten Service-Start ist keine atomare Reservierung.
- [ ] Produktiven Controller-Adapter STOP–Reconfigure–START, harte Rollback-Validierung und Fehler-Injektion implementieren; `Plan.executable=false` beibehalten.
- [ ] Paket 9 im GitHub-TEST (JUnit + Android-Build) und später auf Android-Hardware validieren.

## Phase 3O / Arbeitspaket 8 – weiterhin offen

- [ ] Gemeinsame atomare Absicherung aller realen Analyzer-/MON-/Scout-/Rolling-/Direct-Start- und Stop-Lebenszyklen; das bestehende MIC-Token allein schützt nicht alle laufenden Ressourcen und ist kein vollständiger Hardware-Lease über die Setuptransaktion.
- [ ] Sicherer STOP–Reconfigure–START-Controller-Adapter mit unveränderter JSON-/Preference-Persistenz und umfassendem Rollback bei Hardwarefehlern.
- [ ] GitHub-JUnit/Android-Build und anschließend Android-Gerätetest für Paket 8; vor echter Setup-Freigabe Fault-Injection und Konkurrenztests.
- [ ] `Plan.executable=false` bis zur ausdrücklichen Freigabe beibehalten.

## Phase 3O / Arbeitspaket 7 – verbleibende Aufgaben

- Die konservative Rolling-/Direct-/MON-Startbarriere ersetzt keinen hardwareweiten atomaren Mikrofon-Lease; auch Analyzer-/Scout-/sonstige Start-/Stop-Pfade und tatsächliche Ressourcenübergänge noch integrieren.
- Produktiven Controller-Adapter, STOP–Reconfigure–START, Rollback bei STOP-/Release-/START-Fehlern, UI-Integration und Android-Gerätetests nachziehen. `Plan.executable=false` bis zur ausdrücklichen Freigabe.

## Phase 3O/AP6 – weiter offen
- Gemeinsame atomare Reservierung sämtlicher Analyzer-/MON-/Scout-/Rolling-/Direct-Audio-Starts und Stopps, inklusive Hardware-Owner. Owner-Probe ist nur ein ablehnender Vorcheck.
- Produktiven preference-freien Controlleradapter erst mit Fehlersimulation, Rollback-Verifikation und Gerätetest integrieren; `Plan.executable=false` bis dahin beibehalten.

## Phase 3O – nach Arbeitspaket 5 FIX

- [ ] GitHub-TEST für Paket 5 FIX; Gerätevalidierung steht weiterhin aus.
- [ ] Echte gemeinsame Audio-Exklusivreservierung und Controller-Adapter inklusive Rollback und Fault-Injection.
- [ ] Setup-Executor erst nach vollständiger Absicherung freigeben (derzeit `Plan.executable=false`).

## Nach Phase 3O/Arbeitspaket 5 weiterhin offen

- Gemeinsame reale Exklusivsperre, die alle Audio-/Recorder-Start- und Stop-Pfade atomar einschließt.
- Preference-freier produktiver Controller-Adapter für STOP–Reconfigure–START und Rollback.
- Ausführliche Fehler-Injektion, Race-Tests und echte Android-Gerätevalidierung vor Freigabe.

## Offen nach Phase 3O, Arbeitspaket 4
- Isoliertes Transaktionsprotokoll vorhanden, jedoch absichtlich nicht produktiv angeschlossen.
- Atomaren MIC-/Recorder-Lease und speicherfreien realen Controller-Adapter bereitstellen; Rollback/STOP-Eigentum am Gerät validieren.
- UI erst nach erfolgreicher Fehler-Injektion und Gerätetests anbinden/freigeben; `Plan.executable=false`.


## Offen nach Phase 3O, Arbeitspaket 3

- MIC-Arbiter-Exklusivität der gesamten Transaktion nachweisen und integrieren.
- STOP–Reconfigure–START, vollständiger Rollback und Fehler-Injektion umsetzen.
- Geräte- und End-to-End-Tests vor jeder Produktivfreigabe durchführen.

# Phase 3O – Arbeitspaket 2: offene Folgepakete (14.10.2026)

Interne Gate-Markerprüfung ist jetzt in Diagnose und Reservierung vereinheitlicht. Offen: vollständige Audio-Owner-/Capture-Generationen-Absicherung einschließlich externem Mic-Arbiter; STOP–Reconfigure–START; vollständiger Recovery-Pfad mit Fault-Injection; UI und Gerätetests. `Plan.executable=false`; ein leeres Gate ist keine Hardware-Freigabe. GitHub-TEST Arbeitspaket 1 GRÜN (Benutzer), Arbeitspaket 2 OFFEN. Bereits früher zurückgestellte R-/O-/A-Aufgaben bleiben bestehen.

# Phase 3O – Arbeitspaket 1: Restarbeiten (14.10.2026)

Die Diagnosen sind jetzt in einer ablehnenden Prüfung kombiniert, aber nicht atomar. Offen bleiben die vier Restpakete aus `CURRENT_STATE.md`: Audio-Eigentümer-Lebenszyklen absichern, exklusiven STOP–Reconfigure–START-Transaktionskern implementieren, vollständigen Rollback mit Fehler-Injektion testen sowie UI-/Geräteabnahme. **Keine Freigabe aufgrund leerer Sperrgründe.** Historische aufgeschobene Aufgaben folgen unverändert.

# Phase 3O/19 – weiterhin offen

- `AnalyzerSetupTransitionCoordinator.snapshotGate()` ergänzt: synchronisierte, unverändernd lesbare Kopien von Start-, Destroy- und Capture-Job-Markern; zwei JUnit-Tests und statischer Verifikator.
- Keine automatische Entsperrung und keine produktive Audio-/FFT-Umschaltung. Letzter durch Nutzer bestätigter GitHub TEST: 3O/18 GRÜN, 3O/19 offen.

# Phase 3O/14 – Rolling-/Direct-Startschutz (14.10.2026)

- GitHub TEST Phase 3O/13: **GRÜN laut Nutzer**; Phase 3O/14 noch nicht durch GitHub/Android bestätigt.
- `AnalyzerSetupTransitionCoordinator.withServiceStart` serialisiert die synchronen START-Anforderungen von `RollingAudioService.requestStart` und `AnalyzerDirectAudioService.requestStart` mit einer zukünftigen Setup-Reservierung. Bei aktiver Reservierung wird der Request ohne Android-Serviceaufruf mit `false` abgewiesen. MON-Schutz der Phase 3O/13 bleibt bestehen.
- Zwei neue JUnit-Regressionen: Ablehnung eines konkurrierenden Service-Starts sowie Wiederverwendbarkeit nach Start-Exception.
- **Nicht atomar über den gesamten Service-Lebenszyklus:** `startForegroundService` kehrt vor dem asynchronen AudioRecord-Start zurück. Ein zuvor angenommener Request kann später noch eine Session starten. Bereits aktive oder noch startende Services werden nicht von dieser Koordinationsstelle gestoppt oder reserviert. Eine produktive Umschaltung ist daher weiterhin verboten (`Plan.executable=false`).
- Keine Änderung an Recorder-STOP-Routinen, Cloud, GitHub-Workflow, Gradle `0.5.55/213`, Termux v4 oder Backup-Dateien. Storage-Backup auf GitHub `main` belassen, aus Quell-ZIP ausschließen.

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

## Phase 3O/6 – offener Integrationsrest
STOP-DSP-Checkpoint vorhanden, aber Gesamtkonfiguration (Sampling, Direct, Timing, Recorder) nicht abgedeckt. Noch erforderlich: Audio-/Recorder-Eigentümer synchronisieren, vollständigen Laufzeit-Checkpoint, echter fehlergetesteter Session-Rollback, 8-/48-kHz-Wechsel, JUnit-/Gerätetests und explizite UI-Freigabe. Plan bleibt nur lesend.

## Phase 3O – nach Teilstufe 5
Speicherfreier FFT-/Filter-Startentwurf vorhanden; unvollständige Audiofelder, Recorder-Lock, Modewechsel, tiefer Checkpoint, Rollback, UI-Freigabe und Gerätetests sind weiterhin offen. Teilstufe 4 GitHub TEST laut Nutzer grün.

## Phase 3O – offene Integration nach Teilstufe 4 (14.10.2026)

Zwei speicherfreie STOP-Setter (`setDsp`, `setFilterConfig`) vorhanden; vollständige speicherfreie `start()`-Injektion, 8-/48-kHz-Mode-Switch, gemeinsamer Recorder-State-Lock, Checkpoint/Rollback, Setup-UI-Integration sowie Kotlin-/Geräte-Regressionen sind **offen**. Kein Audio-Setup freigegeben. Teilstufe 3 CI vom Nutzer grün bestätigt; Teilstufe 4 CI offen. Bestehende Kennungen R/O/A bleiben unverändert.

## v0.5.56 – Phase 3N (Entwicklung, nicht freigegeben)

Lesender Audio-/FFT-Ablaufplan mit Zustands-Fingerprints, Sperrgründen und späterer STOP-/Rollback-Schrittfolge. Keine ausführbare Umschaltung; GitHub-Build/Gerätetest der Phase offen.

## v0.5.56 – Phase 3M (Entwicklung, nicht freigegeben)

Lesende Audio-/FFT-Umschaltprüfung mit Änderungs-/STOP-/nicht-unterstützt-/null-Kategorisierung; keine tatsächliche Audio-/FFT-Aktivierung. GitHub-Android-Build und Gerätetest offen; MON-/Anzeige-Vorschau aus vorherigen Phasen erhalten.

## v0.5.56 – Phase 3J (Entwicklung, nicht freigegeben)

Analyzer-Anzeige-Vorschau mit 12 Feldern und expliziter Wiederherstellung ergänzt; JSON/SharedPreferences werden dabei nicht geschrieben. Vollständige Setup-Aktivierung, Prozessrestart-Verhalten, UI-Panels und Android-Gerätetest weiterhin offen. Bestehende R-/O-/A-Kennungen unverändert.

## v0.5.44 – Offene Abnahme
- Reale v0.5.44 Android-JUnit-/Build-/ADB-Tests; Smartphone-Messung für 3 Methoden × 10/20 Bänder, jeweils mit gleicher Eingabe und mehreren Durchläufen.
- MediaStore-Append, CSV-Zeilenanzahl, Performance-Ausreißer, CPU-/Akku-Leistung und Langzeitverhalten überprüfen. Die Performance-CSV ist kein physikalischer Messwert und kein Gesamt-CPU-Profil.
- Ursprüngliche R01–R07, O01–O08, A01–A05, 24-h-Retention und lokale Impulskennwerte bleiben bestehen.

## v0.5.43: CSV-Filter-Methodenkennzeichnung und Speed
Offen: Methodennamen revisionssicher pro CSV-Export dokumentieren; echten Android-Performancevergleich 10/20 Bänder (P50/P95/P99, Energieverbrauch) durchführen; Regression beider unveränderten Analyzer-Wasserfälle.

# Nachtrag v0.5.41 (12.10.2026)

**O09:** Der geplante Offline-Dreifachvergleich ist mit v0.5.41 als Quellwerkzeug und reproduzierbarer Bericht umgesetzt; Android-Integration/produktive Filterentscheidung weiter ausdrücklich **offen**. **O05/A05:** Physikalische Kalibrierung und spätere Geräteprüfung weiterhin offen. **O08:** 18/18 Offline-Self-Tests PASS; Windows Gradle/ADB/Langzeit offen. **O03:** Reale 24-h-Löschung, Offline-Retry und direkte Cloud-Tabellenkontrolle offen. R01–R07, O01–O08, A01–A05 bleiben mit ihrer bisherigen Bedeutung erhalten.

# LFN-Scout – Liste der zurückgestellten Aufgaben

**Aktueller Entwicklungsstand: v0.5.40** (Python-Report 330/330 PASS; Android-Build noch offen). Letzter nachgewiesener Android-Build: v0.5.39 PASS. Cloud-Queue-Gerätetest v0.5.38 PASS für Übernahme/Aufbewahrung; tatsächliche 24-h-Löschung noch offen.

Ich habe die bisherigen Projektentscheidungen und zurückgestellten Aufgaben zusammengestellt. Dabei unterscheide ich zwischen ausdrücklich zurückgestellten Funktionen und offenen Entwicklungsarbeiten.

## 1. Ausdrücklich zurückgestellte Aufgaben

| Nr. | Aufgabe | Vereinbarter Umfang |
|---|---|---|
| **R01** | Cloud-Erweiterung für Impulskennwerte | Vier explizite Spalten in `half_hour_statistics` ergänzen und Werte übertragen. Jetzt ausdrücklich zurückgestellt. |
| **R02** | GUI-Facelift | Moderne, übersichtliche Einstellungen mit Kategorien und gut zugänglichem Gain-Regler; bisherige Mockup-Grafik als Orientierung. |
| **R03** | Cloud-Restore-Test | Vollständige Wiederherstellung auf einem neuen, leeren Supabase-Projekt und anschließender End-to-End-Test. |
| **R04** | Backend-Sicherheit | Prüfen, welche technischen URLs, Schlüssel und Konfigurationsinformationen in APK, Logs und Fehlermeldungen sichtbar sind. |
| **R05** | API-Proxy | Später gegebenenfalls Cloudflare Worker oder vergleichbare Proxy-Lösung zur Abschirmung der Backend-Schnittstellen. |
| **R06** | Verständlichere WAV-Dateinamen | Namen für lokale Aufnahmen und Cloud-Dateien lesbarer gestalten. |
| **R07** | Teilnehmerkeys mit Ablaufdatum | Gültigkeit im Key kodieren; neuester Key exklusiv aktiv; Berechtigung für 1-kHz- bzw. 8-kHz-Analyzer im Key. |

## 2. Noch offene Funktionen der Messwerterfassung

| Nr. | Funktion | Aktueller Stand |
|---|---|---|
| **O01** | Subjektive Bewertungen in Halbstunden-CSV | v0.5.35: lokaler CSV-Standardfall mit 0 und 4 auf Gerät bestätigt. v0.5.36: zusätzliche Zeitstempel-/Grenzfall-Regressionstests implementiert, Windows-/Geräte-Sonderfalltests offen. Bereits vor v0.5.35 erzeugte Beobachtungen sind nicht rückwirkend lokal verfügbar. |
| **O02** | Cloud-Upload der akustischen Halbstunden-Gesamtstatistik | Noch umzusetzen; ohne zusätzliche Impulskennwerte und ohne aggregierte Bewertungen. |
| **O03** | Cloud-Retry und Nachlieferung | Unterbrochene Übertragungen zuverlässig nachholen, ohne Duplikate zu erzeugen. |
| **O04** | SD-Karten-Unterstützung | WAV- und CSV-Speicherort unabhängig auswählbar, einschließlich Android-SAF-Unterstützung. |
| **O05** | Filterkalibrierung | Frequenzgänge, Trennschärfe, Pegelabweichungen und Filterübersprechen quantitativ prüfen. |
| **O06** | Erweiterte Impulsdiagnose | Die vier Kennwerte sind lokal vorgesehen; weitergehende Ereigniserkennung, Zeitlokalisierung und Klassifikation noch offen. |
| **O07** | Parameter und Einstellungen | Sinnvolle zusätzliche Konfigurationsparameter einschließlich Speicherung und Validierung. |
| **O08** | Weitere Regressionstests und Self-Tests | Besonders Langzeitbetrieb, Rollingbuffer, Speicherfehler, CSV-Konsistenz und Wiederanlauf prüfen. |

## 3. Zurückgestellte Analyzer-Erweiterungen

Diese Punkte stammen aus der längerfristigen Planung des spezialisierten Analyzers.

| Nr. | Funktion | Beschreibung |
|---|---|---|
| **A01** | Mithören | Optionaler Monitoring-Pfad mit eigener Verstärkung und gegebenenfalls Bandpass. |
| **A02** | SUPERFAST | Zusätzliche besonders schnelle Analyse-/Darstellungsmöglichkeiten. |
| **A03** | Automatische Aufzeichnungen | Erweiterte Trigger, automatische WAV-/PNG-Speicherung und Diagnose. |
| **A04** | Erweiterte Impulsalgorithmen | Verschiedene Verfahren vergleichen; Filter-Ringing und Parameterwirkung berücksichtigen. |
| **A05** | Weiterführende Filteranalyse | Mehr Filterkonfigurationen beziehungsweise alternative Verfahren, soweit messtechnisch sinnvoll. |

Einzelne Grundlagen dieser Analyzer-Funktionen können bereits vorhanden sein. Die Tabelle bezeichnet deshalb den noch ausstehenden Erweiterungsumfang, nicht zwangsläufig eine vollständig fehlende Funktion.

## 4. Verbindliche Regeln für spätere Versionen

Diese Punkte sind keine zurückgestellten Aufgaben, sondern gelten bereits:

- **Cloud statt Supabase:** In sichtbaren Menüs, Meldungen und Einstellungen ausschließlich „Cloud“. Interne technische Bezeichner bleiben kompatibel.
- **Keine unnötigen Datenbankänderungen:** Die Cloud-Struktur möglichst stabil halten.
- **Vier Impulskennwerte vorerst ausschließlich lokal:** `peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`.
- **Später vier explizite Spalten:** Für diese Kennwerte keine vorläufige Festlegung auf `P01–P04`.
- **Filterstatistik unabhängig schaltbar:** Standard AUS; 10 oder 20 Bänder mit eigenem Frequenzbereich.
- **Regressionstests und integrierte Self-Tests:** Bei Erweiterungen möglichst gleich mitentwickeln.
- **Bestehende Funktionen erhalten:** Keine stillschweigenden Funktionsverluste.

## 5. Meine vorgeschlagene Bearbeitungsreihenfolge

| Priorität | Referenz | Aufgabe |
|---|---|---|
| 1 | O01 | Subjektive Bewertungen lokal vollständig integrieren |
| 2 | O05 | Filterbank messtechnisch validieren |
| 3 | O02/O03 | Akustische Halbstunden-Gesamtstatistik zuverlässig an Cloud übertragen |
| 4 | O04 | SD-Karten-Unterstützung |
| 5 | R03 | Vollständigen Cloud-Restore-Test durchführen |
| 6 | R02 | GUI-Facelift |
| Später | R01 | Vier Impulskennwerte als explizite Cloud-Spalten ergänzen |

**Insgesamt sind hier 7 ausdrücklich zurückgestellte Aufgaben, 8 offene Mess- und Infrastrukturaufgaben sowie 5 Analyzer-Erweiterungen erfasst.**

Die Kennzeichnung **R01–R07, O01–O08 und A01–A05** können wir künftig als dauerhafte Referenzen für die Projektplanung verwenden. Diese Zusammenstellung ist aus den bisherigen Projektentscheidungen abgeleitet; eine erneute vollständige Prüfung aller aktuellen Projektdateien steht noch aus.

## v0.5.37 – Statusaktualisierung

- **O02:** Cloud-Upload der akustischen Gesamt-Halbstundenstatistik im Quellstand implementiert; Windows-Build und Cloud-Gerätetest offen. Keine DB-Migration.
- **O03:** Lokaler persistenter Puffer, Retry bei Start/abgeschlossenem Bucket und idempotente bucket_id im Quellstand. Zeitgesteuerte unabhängige Hintergrund-Nachlieferung weiterhin offen; Langzeittest ausstehend.
- **R01:** Vier Impulskennwerte bleiben ausschließlich lokal; vier explizite Cloud-Spalten erst später.
- **O01:** Lokal bereits im v0.5.36-Standard-/Nichtantwort-CSV-Test bestätigt; Cloud-Aggregation weiter ausgeschlossen.

## v0.5.37 – Statusaktualisierung

- **O02:** Cloud-Upload der akustischen Gesamt-Halbstundenstatistik im Quellstand implementiert; Windows-Build und Cloud-Gerätetest offen. Keine DB-Migration.
- **O03:** Lokaler persistenter Puffer, Retry bei Start/abgeschlossenem Bucket und idempotente bucket_id im Quellstand. Zeitgesteuerte unabhängige Hintergrund-Nachlieferung weiterhin offen; Langzeittest ausstehend.
- **R01:** Vier Impulskennwerte bleiben ausschließlich lokal; vier explizite Cloud-Spalten erst später.
- **O01:** Lokal bereits im v0.5.36-Standard-/Nichtantwort-CSV-Test bestätigt; Cloud-Aggregation weiter ausgeschlossen.

## v0.5.37 – 24-h-Retention
- **O03:** `SENT_PENDING_DELETE` und frühestens 24 h nach bestätigtem Upload bereinigen: im Quellstand implementiert; Android-Build und realer Retention-Gerätetest noch offen. GUI-Statusanzeige nicht implementiert (später prüfen).

## v0.5.38 – Statusaktualisierung
- **O02:** v0.5.37 Windows-Build PASS, geräteseitige Cloud-Erfolgsbestätigung und lokales CSV vorhanden. Cloud-Datenbankdatensatz inhaltlich noch direkt zu vergleichen.
- **O03:** 24-h-Retention und keine Wiederholung nach Prozess-Neustart am Gerät bestätigt; tatsächliche Löschung nach 24 h und Offline-Retry offen. In v0.5.38 präzisere Diagnosezähler und Löschfrist, noch kein eigener Hintergrundjob oder GUI.
- **O08:** Zusätzliche Regression zur Aufbewahrungsfrist; Langzeit-Gerätetest offen.

## v0.5.39 – Aufgabenstatus
- **O05:** Quantitativer Soll-Ist-Vergleich der digitalen, unkalibrierten Filterbank als JVM-Regression ergänzt; Ausführung auf Windows noch offen. Physikalische Kalibrierung, Bandtrennschärfe und Mikrofonfrequenzgang weiter offen.
- **O03:** v0.5.38 Windows/ADB und Geräte-Queue-Status PASS. Fristablauf-Löschung, Offline-Retry und Cloud-Datenabgleich offen.
- **O08:** Software-Frequenzgang-Regression mit 10/20 Bändern ergänzt; noch nicht Windows-getestet.
- **R01:** vier Impulskennwerte weiterhin nur lokal.

- **O05 v0.5.40:** Python-Offline-Validierungsreport mit Frequenzgang/Übersprechmatrix und Soll-Ist-CSV implementiert; 330/330 simulierte Prüfungen PASS. Android-Build/Gerätetest und reale physikalische Kalibrierung weiter offen.

## Übergabeaktualisierung v0.5.40 (11.10.2026)

- **O01:** Bewertungsaggregation mit 0/4 und 2/4/-1 im Geräte-CSV nachgewiesen, weitere Grenzfälle offen.
- **O02:** bestehender Gesamt-Halbstunden-Cloud-Upload laut Android-App-Log erfolgreich; direkter Tabellen-Abgleich bleibt offen (ältere Tabelle oben ist ein historischer Planungsstand).
- **O03:** 24-h-Retention über Prozessneustart und Upgrade auf v0.5.38 nachgewiesen; vollständiger Zeitablauf und Offline-Retry offen.
- **O05:** v0.5.39 Windows-Unit-Tests PASS; v0.5.40 Python-Report 330/330 PASS; reale akustische Kalibrierung/Trennschärfe offen.
- **O09 (neu):** eigenständiger Vergleichsreport Legacy-Filter/Butterworth 4. Ordnung/FFT-Bandenergie inklusive Frequenzgang, Übersprechen, Einschwing-/Impulsantworten und PASS/FAIL für v0.5.41 **geplant, noch nicht implementiert**.
- **Build:** v0.5.39 Gradle, APK, ADB PASS; v0.5.40 Android Gradle/APK/ADB noch offen.
- Details in `docs/project/HANDOVER_v0.5.40_2026-10-11.md`.

## v0.5.42 ergänzte offene Aufgaben
- **O09:** Methodenvergleich auf echtem Xiaomi mit gleichen Testsignalen, 10/20/40 Bändern: Filter-CPU-Mittel/Maximum sowie P50/P95/P99, Thermal, Akku, Dropouts; v0.5.42 nur Quell- und Hosttest.
- **O10:** Physikalische Amplituden-/Bandnormierung aller 3 Verfahren und Impuls-Zeitauflösung unabhängig validieren, FFT-Hann-Fenstereinfluss getrennt ausweisen.
- **O11:** Methodenkennung in lokalen Messmetadaten/CSV (rückwärtskompatibel) ergänzen, damit Messwerte nicht ohne Filterkontext verglichen werden; noch nicht implementiert.

### v0.5.56 Phase 3F – Entwicklungsnotiz (keine Freigabe)
- Der reine JSON-Setup-Browser ist in der Analyzer-Setup-Seite sichtbar; aktiv bleibt die bisherige Slot-Speicherung.
- Noch offen: JSON-Setup-Aktivierung, tatsächliche MRU-Schnellzugriffe, Änderungen nur auf explizites Speichern persistieren, MON-/Analyzer-Synchronisation, acht Analyzer-Panels und sieben Scout-Panels, SAF-Import-/Export sowie automatisierte und Gerätetests.

## Phase 3O – 14.10.2026 (Teilfortschritt, nicht freigegeben)
- **O11:** Sichere Audio-/FFT-JSON-Übernahme: `AnalyzerSetupSwitchTransaction.kt` als fail-closed STOPPED-Guard/Adaptervertrag ergänzt. **Kein Controller-Adapter, keine aktive Setter-/STOP-/START-/Rollback-Funktion**; Phase 3N weiterhin lesend und `Plan.executable=false`. Erforderlich: gemeinsame Recorder-Synchronisation, speicherfreie Konfigurationsinjektion, wirklicher Checkpoint/Restore und JVM-/Android-/Gerätetests. Details: `PHASE3O_TRANSAKTIONSSCHUTZ_v0.5.56.md`.

## Phase 3O – zweite Teilstufe (14.10.2026)
- **O11:** Präziser Zahl- und vollständiger Keyset-Vergleich als Vorbedingung ergänzt. Tatsächlicher Controller-Apply, gemeinsame Recorder-Zustandssperre, sichere Reinitialisierung und Rollback weiterhin **offen**. Vier neue JUnit-Fälle vorhanden, noch nicht mit Android-Gradle ausgeführt. Filtergrenzen-Double→Int im Runtime-Snapshot bleibt offen.


## Phase 3O – dritte Teilstufe: Legacy-Grenzwertschutz (14.10.2026)

`AnalyzerRuntimeSetupSnapshot.capture` darf Filtergrenzen nicht mehr stillschweigend von `Double` nach `Int` abschneiden. `exactLegacyFilterHz` akzeptiert nur endliche, exakt ganzzahlige Werte im Int-Bereich; bei echten Bruchteilen bricht die *lesende* Erfassung mit einer klaren Fehlermeldung ab. Grund: Das aktuelle Legacy-JSON-Schema deklariert `filter_min_hz` und `filter_max_hz` als Integer. Dieser Guard dient der Integrität, **ist keine Unterstützung für Bruch-Hz**. Für eine vollwertige Übernahme muss die Schema-/Controller-Frage explizit geklärt werden. `AnalyzerSetupFormat` prüft Integerwerte jetzt über `BigDecimal.toBigIntegerExact()` statt verlustbehaftetem `Double`. Vier JUnit-Regressionsfälle hinzugefügt, deren Ausführung durch Gradle aussteht. Keine Änderung an Recordern, Audiopipeline, SharedPreferences, JSON-Dateien, Workflows oder Gradle-Version. `Plan.executable=false`, kein Adapter aktiv.

## 14.10.2026 – Offener Phase-3O-Folgeumfang
3O/7 dokumentiert einen **lesenden** STOP-Audio-/Timing-Checkpoint mit Stale-Kontrolle; vollständiger Restore/STOP/START-Executor, gemeinsames Aufnahme-Lock, Android-Geräte- und Fehler/Rollback-Regressionstests bleiben offen. Pflichtübergabe `docs/project/CURRENT_STATE.md` bei jeder Projekt-ZIP aktualisieren. R01–R07, O01–O08 und A01–A05 unverändert.

- [ ] Phase 3O: Recorder- und Hardware-Zustandstransaktion, STOP/START/Rollback, dynamische Tests. Der atomare Controller-Checkpoint aus Teilstufe 8 reicht hierfür noch nicht aus.
- [ ] Historischen Backupordner erst nach unabhängiger Sicherungsprüfung und separater Freigabe aus Git-Tracking entfernen.

### Phase 3O/9 – Fehlerrücknahme nach Sessionstart
- Initialisierungsfehler des lokalen Analyzers räumen jetzt Session/Buffer best-effort auf (Quellkandidat 3O/9). Noch offen: JVM-/Geräte-Fault-Injection, Startfehler vor/nach der Sessionerstellung, readback-Prüfung nach Restore, Transaktion mit dem tatsächlichen Recorder-/Service-Owner und Hardware-Modewechsel. `Plan.executable` bleibt false.
- Historisches `supabase/storage_backup_before_v0526` bleibt in GitHub `main` versioniert, wird aber in Projekt-ZIPs nicht mehr mitgeliefert. Keine Git-Löschung oder Änderung an Termux v4.

### Phase 3O/12 – verbleibende Aufgaben
- [ ] Gemeinsamer Übergangs-/Sperrvertrag für **alle** Recorder inklusive MON, Service-Start/Stop, Analyzer-STOP/START und Fehlerrollback; MicCaptureArbiter-Reservierung alleine genügt nicht.
- [ ] Dynamische Tests der vollständigen Übergänge und Geräteprüfung sowie UI-Freigabe erst nach nachgewiesener Sicherheit.

### Phase 3O/13 Nachtrag
- Weiter offen: MON-Setup-Gate mit vollständig atomaren Rolling-/Direct-/Audio-Service-Transitions und produktivem Executor zusammenführen; dynamische Android-Tests und echter Rollback.
