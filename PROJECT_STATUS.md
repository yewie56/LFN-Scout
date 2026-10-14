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

## Phase 3O – Arbeitspaket 15 (Quellkandidat)
Arbeitspaket 14 wurde durch den Benutzer mit GitHub-TEST GRÜN bestätigt. Implementiert ist ein **nicht produktiv aufgerufener** expliziter STOP-Konfigurationsstart in `LiveAnalyzerController`, der keine gespeicherten Analyzer-Preferences nachlädt und einen aktuellen DSP-/Timing-Checkpoint verlangt. Der bisherige Startpfad und seine Persistenzsemantik bleiben bestehen. Statische Prüfungen 29/29 PASS; GitHub-JUnit/Android-Build und Gerätetest für Paket 15 sind noch offen. Eine vollständige Audio-Owner-/Service-Release-Garantie und Hardware-Rollback bestehen nicht; `Plan.executable=false`.

## Phase 3O – Arbeitspaket 14 (Quellkandidat; 14.10.2026)

Benutzerbestätigung Paket 13: GitHub-TEST **GRÜN**. Paket 14 enthält Controller-Schnittstellen-Audit, zwei JUnit-Fault-Injection-Fälle und einen statischen Sicherheitsguard. Kein produktiver Setupwechsel. Versionsstand Gradle 0.5.55/213 bleibt unverändert. GitHub-/Gerätetests für Paket 14 offen. Details: `docs/project/CURRENT_STATE.md`.

## 14.10.2026 – Phase 3O, Arbeitspaket 13 (Quellkandidat)

- Drei JUnit-Konkurrenztests mit echten Threads/Latches statt ausschließlich verschachtelter Aufrufe: laufende Setup-Lease gegen Rolling/Direct, zweite Setup-Anforderung und Freigabe nach fehlerhaftem Setup-Callback.
- Produktive Audio-Routinen und Setup-Executor unverändert (`Plan.executable=false`). Die Tests liefern keinen Nachweis für Android-Hardware oder asynchrone Service-Lebenszyklen; diese bleiben offen.
- Letzter vom Benutzer bestätigter GitHub-TEST: Arbeitspaket 12 GRÜN. JUnit/Android-Build für Paket 13 ausstehend.

### Phase 3O – Arbeitspaket 12

Arbeitspaket 11 GitHub-TEST GRÜN. Arbeitspaket 12 als Quellkandidat: isolierter Legacy-Reservierungspfad an Koordinator angeschlossen, drei JUnit-Regressionen ergänzt. GitHub-TEST offen, produktiver Setupwechsel gesperrt.

## Phase 3O – Arbeitspaket 11 (Quellkandidat)

Arbeitspaket 10: GitHub-TEST durch Benutzer bestätigt **GRÜN**. Arbeitspaket 11 integriert die MicCaptureArbiter-Tokenübernahme in dieselbe koordinierte Reservierungsentscheidung und ergänzt drei JUnit-Regressionen. Der produktive Audio-/FFT-Setupwechsel bleibt deaktiviert (`Plan.executable=false`); JUnit und Android-Build für Arbeitspaket 11 noch offen. Version `0.5.55` / `213` unverändert. Siehe `docs/project/CURRENT_STATE.md`.

## 2026-10-14 – Phase 3O / Arbeitspaket 10 (Quellkandidat)

Arbeitspaket 9 GitHub-TEST **GRÜN**. Legacy-Eintrittspunkte erhalten dieselben fail-closed Start-Blocker wie die modernen getrackten Pfade, ohne Lifecycle-Tracking vorzutäuschen. Drei neue JUnit-Regressionen; Paket 10 GitHub-TEST offen. `Plan.executable=false`; Gradle `0.5.55 / 213` unverändert.

## 2026-10-14 – Phase 3O, Arbeitspaket 9 (Quellkandidat)

Arbeitspaket 8: GitHub-TEST vom Benutzer **GRÜN** bestätigt. Paket 9 verhindert getrackte Rolling-/Direct-Startabgaben schon beim Belegen eines existierenden Arbiter-Tokens. Drei JUnit-Regressionen; tatsächliche Android-/GitHub-Ausführung ausstehend. Vorprüfung ist kein atomarer künftiger Hardware-Lease. Reale Setup-Umschaltung bleibt gesperrt (`Plan.executable=false`), Gradle `0.5.55 / 213` unverändert.

## 2026-10-14 – Phase 3O / Arbeitspaket 8 (Quellkandidat)

Letzter bestätigter GitHub-TEST: **Arbeitspaket 7 GRÜN**. Paket 8 verbindet die bestehende Setup-Reservierung während synchroner Blöcke mit dem exklusiven Mikrofon-Token des vorhandenen `MicCaptureArbiter`. Drei neue JUnit-Tests; Quellprüfungen und ZIP-Prüfung separat. GitHub-Test und Gerätetest Paket 8 **ausstehend**. Produktive Audio-/FFT-Umschaltung weiter gesperrt (`Plan.executable=false`); Gradle `0.5.55/213`, Termux v4, Cloud und Workflows unverändert. Es fehlen atomare Lifecycle-Sperren sämtlicher Komponenten sowie echter Controller/Recovery und Gerätetest.

## Phase 3O – Arbeitspaket 7 (Quellkandidat, 14.10.2026)

- Letzter durch Benutzer bestätigter GitHub-TEST: Arbeitspaket 6 **GRÜN**.
- Paket 7: Cross-Service-Start-Ausschluss unter vorhandenem Koordinator-Mutex, MON-Start-Schutz und drei neue JUnit-Regressionsfälle.
- Lokale Phase-3O-Quellprüfungen bestanden; GitHub-JUnit/Android-Build und Android-Gerätetest für Paket 7 ausstehend.
- Reale atomare Hardware-Exklusivität, Controller-Adapter und vollständiger Rollback weiterhin offen. `Plan.executable=false`. Gradle `0.5.55` / `213` unverändert.

## Phase 3O – Arbeitspaket 6 (Quellkandidat, 14.10.2026)
- Arbeitspaket 5 FIX: GitHub-TEST laut Benutzer **GRÜN**.
- Owner-geprüfte Setup-Reservierung im vorhandenen Koordinator; vier zusätzliche JUnit-Tests. GitHub-TEST Paket 6 offen.
- Keine produktive Umschaltung; Plan.executable=false; Version 0.5.55/213.

## Phase 3O – Arbeitspaket 5 FIX (14.10.2026)

Paket 4 GitHub-TEST GRÜN. Paket 5 GitHub-TEST ROT: 225 JUnit-Tests, ein Fehler `lostLeaseDoesNotClaimSafeStopped`. Vorabprüfung der Exklusivität im isolierten Protokollkern ergänzt; bei Verlust keine Controllerberührung und keine positive Rollback-Aussage. Fix-GitHub-TEST offen. Produktive Aktivierung bleibt gesperrt; 0.5.55/213 unverändert.

## Phase 3O – Arbeitspaket 5 (Quellkandidat)

Arbeitspaket 4: GitHub-TEST GRÜN laut Benutzer. Arbeitspaket 5: simulierte exklusiv gehaltene Audio-Besitzgrenze mit vier neuen JUnit-Regressionen. Keine produktive Freischaltung und keine Änderung an Recorder-, Cloud-, Workflow- oder Gradle-Schnittstellen. GitHub-TEST/JUnit und Gerätetest für Arbeitspaket 5 stehen aus.

## Phase 3O – Arbeitspaket 4 (14.10.2026, Quellkandidat)
GitHub-TEST Arbeitspaket 3 durch Benutzer GRÜN. Nicht angebundenes, testbares Transaktionsprotokoll mit Rollback und sechs JUnit-Regressionen ergänzt. Keine Freigabe, `Plan.executable=false`; Build/JUnit Paket 4 ausstehend. Nächste Schritte: exklusiver MIC-/Recorder-Lease, echter Controller-Adapter, Fault-Injection und UI-/Geräteabnahme.


## Phase 3O Arbeitspaket 3 – Kandidat

Arbeitspaket 2: GitHub-TEST GRÜN (Benutzer). Paket 3: eindeutige Capture-Generationskennung und sichere Abschlussverarbeitung; GitHub-/Gerätetest noch offen. Keine produktive Setup-Umschaltung.

## Phase 3O – Arbeitspaket 2 (14.10.2026)

**Letzter bestätigter GitHub-TEST:** Arbeitspaket 1 GRÜN (Nutzerbestätigung). **Neuer Quellkandidat:** Arbeitspaket 2, GitHub-TEST OFFEN. Interne Gate-Markerprüfung für Reservierung und Diagnose vereinheitlicht und unbekannte Services abgewiesen; drei JUnit-Regressionen ergänzt. Produktive Umschaltung weiterhin deaktiviert; kein Hardware-Rollback und kein Laufzeit-Gerätetest. Siehe `docs/project/CURRENT_STATE.md`.

## Phase 3O – Arbeitspaket 1 (14.10.2026)

**Letzter bestätigter GitHub-TEST:** Phase 3O/20 GRÜN (Benutzer). **Aktueller Quellkandidat:** Arbeitspaket 1; CI und Gerätetest offen. Gemeinsame ablehnende Diagnose von Koordinator-Markern und externen Audio-Besitzern in der bestehenden Setup-Transaktionsprüfung ergänzt. Keine produktive Umschaltung; Gradle 0.5.55 / 213 bleibt. Detailstand: `docs/project/CURRENT_STATE.md`.

## Phase 3O/20 (Quellkandidat; GitHub TEST offen)

Rein lesende, synchronisierte Sperrgrund-Diagnose des AnalyzerSetupTransitionCoordinator ergänzt. Keine Freigabe produktiver Audio-/FFT-Setupwechsel. Neue statische und JUnit-Regressionstests; letzter bestätigter TEST 3O/19 grün. Termux v4, Gradle 0.5.55/213, Cloud und Supabase-Backup-Policy unverändert.

# Phase 3O/19

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

## Phase 3O, Teilstufe 8 – 2026-10-14
Gemeinsamer STOP-Zustandscheckpoint; noch kein echter Audio-Setupwechsel, GitHub TEST offen. Recovery-Storage separat gesichert, nicht aus Git gelöscht. Details: `docs/project/CURRENT_STATE.md`.

## 14.10.2026 – Phase 3O/6 (Quellkandidat)
FFT-/Filterbank-Checkpoint bei STOP einschließlich validierter speicherfreier Wiederherstellung implementiert. Nicht als kompletter Audio-Rollback oder Setup-Switch interpretieren. GitHub-TEST Teilstufe 5: GRÜN (Nutzermeldung). Teilstufe 6: CI und Gerätetest OFFEN. Gradle 0.5.55/213.

Phase 3O/5: Optionaler speicherfreier FFT-/8-kHz-Filterbank-Startentwurf (`DspStartOverride`) eingebaut; komplette Audio-/Recorder-Transaktion, Rollback und UI bleiben gesperrt. Teilstufe 4 GitHub TEST laut Nutzer grün; Teilstufe 5 nicht gebaut.

## v0.5.56 – Phase 3O, Teilstufe 4 (Quellkandidat, 14.10.2026)

- `LiveAnalyzerController.setDsp` und `setFilterConfig`: optional `persist=false` für STOP-begrenzte, speicherfreie Controller-Änderungen; `persist=true` als unveränderter Standard für Legacy-Aufrufer. Kein Anschluss an Setup-UI oder Transaktionsport.
- `start(context)` lädt weiter aus SharedPreferences; vollständige Audio-/FFT-Umschaltung, Recorder-Lock und Rollback bleiben gesperrt. Lesender Phase-3N-Plan weiterhin `executable=false`.
- Neuer statischer Regressionstest `tools/verify_lfn056_phase3o_draft_setters.py`; GitHub TEST für diese Teilstufe noch offen. Vorherige Teilstufe 3: GitHub TEST laut Nutzer GRÜN.
- Gradle `0.5.55` / `213` unverändert; Termux-v4-Skript nicht geändert.

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
Quellkandidat: Rauschboden-Subtraktion korrigiert; Gradle/Android offen. Siehe docs/project/NOISE_FLOOR_SUBTRACTION_v05454.md.

## v0.5.45.3 – Rauschboden 10-s-Power-Average, einstellbare Dauer, Nullsignal-Schutz (Quellkandidat)
- Einmessdauer im Analyzer unter Spektrum: 10/30/60/120/300/600 s, Standard 10 s, persistent.
- Langdruck auf NOISE misst weiterhin das zeitliche lineare Power Average aller gueltigen FFT-Fenster.
- Wenn gemessene Leistung nahe oder unter Referenzleistung liegt (<= 1,25 x Referenz), wird der Originalwert durchgereicht statt auf Null geclippt. Die Werte sind dann NICHT rauschkompensiert und muessen als nicht sicher trennbar interpretiert werden; hoher SNR wird weiterhin in linearer Leistung subtrahiert.
- Physikalischer Hinweis: Wird ein Nutzsignal bei der Kalibration mit eingemessen, wird es als Rauschen behandelt. Zeitliche Mittelung allein behebt dies nicht.
- FAST, Roh-WAV, Cloud und bisherige CSV-Filterbank unveraendert; Rolling-Halbstundenpfad bleibt FFT-basiert.
- Gradle / Android-Build und Geraetetests offen.

## v0.5.45.2 – Quellkandidat
Rauschboden-Einmessung per zeitlichem linearem Power-Average, gültige und verworfene Fenster dokumentiert. Bisherigen JSON-Schema v1 beibehalten. Gradle/Android-Test noch offen. Der 8-kHz-Analyzer und 1-kHz-Rolling-FFT sind weiterhin nicht derselbe FFT-Rechenkern.

## v0.5.45.1 – Quellkandidat / Compilerfix
Windows-v0.5.45-Vortest: PREPARE PASS; app-Kotlin fehlgeschlagen wegen verwaistem `writeFilterPerformanceWindow`-Aufruf im äußeren `finally`. v0.5.45.1 entfernt nur diesen Aufruf. Vollständiger Android-Build und Geräte-Regression noch offen. CSV-FFT-/FAST-Architektur unverändert.

## v0.5.45 – Buildkandidat
FFT-basierte Rolling-Statistik, alte CSV-Filter inaktiv. Getrennter 1-kHz-Rolling-FFT-Rechenstrom statt vollständigem Sharing mit dem Live-8-kHz-Analyzer – Ziel noch offen. Android-Tests noch ausstehend. Details: docs/project/NOISE_FLOOR_FFT_ROLLING_v0545.md.

## v0.5.44 – Performancevergleich nur der Rolling-CSV-Filter
Neue separate CSV `RollingCsvFilterPerformance.csv` mit 60.000-Sample-Fenstern und P50/P95/P99 der sekündlichen Rechenkosten, 10/20 Bänder und alle 3 auswählbaren Methoden. Isolierter Kotlin-Hosttest PASS; Android-Gradle-/ADB-Test **noch offen**. v0.5.43 wurde vom Nutzer mit vollständigem Windows-Vortest PASS und 6/6 Android-Funktionstests PASS bestätigt. Analyzer bleibt unverändert. Siehe `docs/project/CSV_FILTER_PERFORMANCE_v0.5.44.md`.

## v0.5.43 – CSV-Filterverfahren (KORREKTUR)
Analyzer aus v0.5.41 wiederhergestellt; wählbare Filter jetzt ausschließlich in Rolling-Halbstunden-CSV. Kotlin-Hosttest PASS, Android-Build/ADB offen. Siehe `docs/project/CSV_FILTER_METHODS_CORRECTION_v0.5.43.md`.

**v0.5.42 Quellkandidat:** FAST-Wahl + Performance-Anzeige implementiert. Kotlin-Hostprüfung PASS; Android-Gradle-JUnit/APK/ADB nicht geprüft; letzte bestätigte Android-Version v0.5.39.

# Statusnachtrag v0.5.41 (12.10.2026)

Offline-Vergleich der drei Filterverfahren implementiert. `tools/filter_methods_comparison_v0541.py` und `FILTERVERGLEICH_STARTEN.bat` sind die Startpunkte. Lokal erzeugter Bericht: `docs/project/filter_comparison_evidence_v0541/VERGLEICHSBERICHT.md`. Python-Self-Test 18/18 PASS in der Bearbeitungsumgebung; **keine** Windows- oder Android-Build-/Gerätefreigabe. V0.5.40 Android-Build weiterhin offen; letzte belegte v0.5.39. Produktive Android-Filter bleiben unverändert.

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

## v0.5.37 / versionCode 184 – Source Candidate

Cloud-Halbstunden-Gesamtstatistik über bestehende Edge Function angebunden. Kein Datenbankschema geändert. Nur Gesamt-Leq/L50/L90 samt Zeitfenster, Frequenzbereich und Coverage/Qualität. Beständige schlüsselfreie Warteschlange und Wiederholversuch bei Rolling-Service-Start/Bucket-Abschluss hinzugefügt. Android-Build, Gerätetest und Cloud-End-to-End noch offen. Vier Impulskennwerte und subjektive Aggregatwerte lokal.

## v0.5.37 / versionCode 184 – Source Candidate

Cloud-Halbstunden-Gesamtstatistik über bestehende Edge Function angebunden. Kein Datenbankschema geändert. Nur Gesamt-Leq/L50/L90 samt Zeitfenster, Frequenzbereich und Coverage/Qualität. Beständige schlüsselfreie Warteschlange und Wiederholversuch bei Rolling-Service-Start/Bucket-Abschluss hinzugefügt. Android-Build, Gerätetest und Cloud-End-to-End noch offen. Vier Impulskennwerte und subjektive Aggregatwerte lokal.


## v0.5.36 – Quellkandidat
Zeitstempelvalidierung und Regressionstests für lokale Bewertungen implementiert. Android-Gradle-Build, Integration und Neustart auf Gerät **OFFEN**.
## v0.5.35 – Source Candidate
Lokale subjektive Beobachtungs-Aggregation in Halbstunden-Gesamt-CSV integriert. Bestehende Beobachtungen vor Update nicht rueckwirkend importiert. Nur statische Pruefung; Android-Build, Unit-Tests und Realbetrieb noch offen. Keine Cloud-Aenderung.

## Dokumentationsnachtrag v0.5.34 – Zurueckgestellte Aufgaben

`docs/project/DEFERRED_TASKS.md` wurde als zentrale Aufgabenliste aufgenommen. Bei jeder folgenden Version pflegen; Referenznummern bleiben stabil. Ausschliesslich Dokumentationsaenderung: App-Version v0.5.34 / versionCode 181 bleibt unveraendert; kein neuer Android-Build oder Geraetetest durch diesen Nachtrag.

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


## v0.5.32 – aktueller Testkandidat
Neue JUnit-Diagnoseprüfungen für die Rolling-Filterbank im Android-Testverzeichnis. Noch kein Gradle-Build oder Xiaomi-Gerätetest dieser Version nachgewiesen. Physikalischer Frequenzgang der einfachen Filter erster Ordnung ist weiterhin unkalibriert.

## v0.5.31 – Source Candidate
Implementiert: Rolling-Filterstatistik 10/20 Bänder, unabhängiger Bereich 0–100 Hz, optionaler CSV-Export, standardmäßig deaktiviert. Filter nur diagnostisch, vorläufiger DSP. Noch kein realer Gradle-/APK-/Gerätetest v0.5.31; SD-Speicher, Sekunden-CSV, Filter-Kalibration, Cloud-Halbstundenstatistik weiter offen.

## v0.5.30 – Source Candidate, nicht freigegeben

Auf Basis des nachweislich unter Windows gebauten/installierten v0.5.29 (11.10.2026) implementiert: PRE-GAIN-PCM als Eingang der Rolling-Halbstundenstatistik; optionaler öffentlicher CSV-Export pro abgeschlossenem :00/:30-Bucket, Standard EIN; geordneter PARTIAL-Abschluss. Bestehende Rollingbuffer-Länge 30 Minuten und Betriebsdauer-Standard 12 Stunden bleiben getrennt. **Build, Unit-Tests und Gerätetest v0.5.30 sind offen**. Der Pegelfilter aus v0.5.29 ist vorläufig und nicht kalibriert; CSV ist DIAGNOSTIC. Noch offen: Beobachtungsmittelwerte, Filterstatistik, Sekundenwerte, SD-Karte, persistente Recovery-Buckets und Supabase-Anbindung der Halbstundenwerte.

## v0.5.29 Source Candidate – keine Produktivfreigabe

Neu: Rolling-Halbstunden-Statistikkern mit konfigurierbaren Gesamtfrequenzgrenzen und Logging. Nicht persistent, nicht an Cloud angebunden; siehe `docs/project/ROLLING_HALF_HOUR_v0.5.29.md`. Windows-Gradle-/Gerätetest fehlt.

## v0.5.28 – Entwicklungsstand

Quellkandidat mit optionalem WAV-Filter-CSV-Export (siehe `docs/project/WAV_FILTER_CSV_v0.5.28.md`). Kontinuierliche Halbstundenstatistik und SD-Speicherort-Auswahl weiterhin OFFEN. Windows-Build/Android-Gerätetest von 0.5.28 NICHT nachgewiesen.

## v0.5.27 – Outbox-Fortsetzung (Source Candidate)

Observer- und Analyzer-Audiostatistik mit privater, schlüssel-freier persistenter Set-Outbox und Retry bei Upload-Ereignissen; noch keine autonome Wiederholung und kein 30-Minuten-Bucket. Android Build und Xiaomi-Test NICHT NACHGEWIESEN. Siehe `docs/project/ANDROID_CLOUD_V0.5.27.md`.

## v0.5.27 – Source Candidate (10.10.2026)

Teilimplementierung Android-Cloud-Anbindung; vollständige Abgrenzung: `docs/project/ANDROID_CLOUD_V0.5.27.md`. Android Build/Tests NOT RUN (Gradle-Distribution in Prüfumgebung nicht verfügbar). Kein Release-PASS.

# LFN-Scout – Projektstatus v0.5.26 (10.10.2026, nach Remote-Deployment/Validierung)

**Version:** `0.5.26` / `versionCode 173` / Paket `de.brummbeobachter.app`.

**Supabase:** additive Backend-/Schema-Erweiterung ist auf dem Remote-Projekt `kbejualnruiwtnmeytif` eingespielt. `audio_recordings` besitzt Location-/Origin-/Triggerkontext; neu sind `parameter_lookup`, `measurement_parameter_sets` (Overall + bis 40 Filter je WAV) und `half_hour_statistics` für unabhängige 30-Minuten-Leq/L50/L90-Zeitreihen. Etablierte Statistikparameter bleiben explizit benannt; `P01...P32` bilden den versionierten flexiblen Zusatz-/Reserve-Layer.

**Remote-Migrationen:** synchron bis einschließlich `20261010174500`. Zusätzlich zur Hauptmigration wurden `20261010_seed_parameter_lookup_v0526.sql`, `20261010163000_fix_half_hour_bucket_id_default_v0526.sql` und `20261010174500_normalize_parameter_lookup_semantic_keys_v0526.sql` angewendet. Die letzte Migration vereinheitlicht die Lookup-Namen mit DB/API, insbesondere `valid_epochs` statt `epoch_count` sowie `lmin_dbfs`, `lmax_dbfs`, `mean_level_dbfs`, `level_stddev_db`, `crest_factor_db`, `kurtosis_excess`.

**Edge Functions remote:** `upload_wav` v1.5.0, `submit_audio_statistics` v1.0.0 und `submit_half_hour_statistics` v1.0.0 sind deployed. `verify_jwt=false` bleibt passend zum vorhandenen Observer-Key-Verfahren.

**Nachgewiesene Backend-Tests:** Datenbank-Constraints für COMPLETE/PARTIAL, Coverage >1800, 30-Minuten-Dauer, :00/:30-Ausrichtung, Duplikat-Bucket sowie Overall/Filter 1/Filter 40/Filter 41 wurden real gegen Remote-Supabase geprüft. `submit_audio_statistics` wurde per HTTP für Overall und Filter 1 sowie auf Idempotenz getestet. `submit_half_hour_statistics` wurde per HTTP für einen COMPLETE-Bucket, Idempotenz und einen ungültigen 1900-s-Request getestet. Ein bestehender alter WAV-Uploadpfad funktioniert gegen `upload_wav` v1.5.0 weiterhin; ohne neue Clientfelder werden `capture_origin`/`trigger_type` erwartungsgemäß `LEGACY_UNKNOWN`, Standort kann auf `FALLBACK_OBSERVER_BASE` fallen.

**Backups:** Pre-Migrations-Dumps (`schema_before_v0526.sql`, `data_before_v0526.sql`) und Post-Migrations-Dumps (`schema_after_v0526.sql`, `data_after_v0526.sql`) sind vorhanden und wurden auf lesbaren PostgreSQL-Dump-Inhalt geprüft. Ein praktischer Restore auf ein komplett neues leeres Supabase-Projekt bleibt ausdrücklich zurückgestellt.

**Android-Status:** Android v0.5.26 ruft die neuen Statistik-Endpunkte weiterhin **nicht produktiv** auf und sendet die neuen WAV-Herkunftsfelder noch nicht. Für die nächste Version sind `capture_origin=OBSERVER|ANALYZER`, passende `trigger_type`-Werte, produktiver Statistik-Upload sowie die UI-Änderung `Supabase ok` → `Cloud ok` vorgesehen. Ein realer v0.5.26-Android-Gradle-Lauf `:app:testDebugUnitTest`/`assembleDebug`, APK-/ADB-Installation und Gerätetest sind in diesem Übergabestand **nicht nachgewiesen**. Daher darf v0.5.26 trotz erfolgreich validiertem Backend nicht als vollständig Android-getesteter Release bezeichnet werden.


---

# LFN-Scout – Projektstatus v0.5.25 (25.09.2026)

**Version:** `0.5.25` / `versionCode 172` / Paket `de.brummbeobachter.app`.

**Änderung:** Analyzer-Statistikbox oben rechts zeigt kompakt `Leq(min-max)`, `L50(min-max)`, `L90(min-max)` mit dynamischem Statistik-Frequenzbereich. Leq wird unbewertet über den vollständig gefüllten, konfigurierten Statistikzeitraum berechnet. Der Rahmen und die klickbare Reset-Fläche umfassen exakt die kompakte Drei-Zeilen-Box.

**Screendump-Kommentar:** manuelle Analyzer-Bilder rendern den optionalen Kommentar unten rechtsbündig ohne schwarzen Hintergrundkasten direkt in den eingefrorenen Wasserfall.

**Prüfstatus bei Erstellung:** Source Candidate; echter Windows-Gradle-Lauf `:app:testDebugUnitTest` + `assembleDebug`, APK-Erzeugung, ADB-Installation und Xiaomi-Test v0.5.25: **NOT RUN**.

---

## LFN-Scout – Projektstatus v0.5.24 (25.09.2026)

**Version:** `0.5.24` / `versionCode 171` / Paket `de.brummbeobachter.app`.

**Zweck:** reine Compiler-Reparatur auf Basis v0.5.23. Keine neue Android-Laufzeitfunktion und keine Änderung an Audio-/Supabase-/Retention-Architektur.

**Realer Befund v0.5.23:** PREPARE PASS; `:app:testDebugUnitTest` brach in `:app:compileDebugKotlin` ab. Fehler: fehlender `OutlinedTextField`-Import in `LiveAnalyzerScreen.kt` sowie nullable `ByteArray?`-Verwendung im Analyzer-Audio-Upload in `MainActivity.kt`.

**Korrektur v0.5.24:** Material3-Import ergänzt; `wavBytes` wird explizit auf `null || isEmpty()` geprüft, danach kann Kotlin den Wert im Upload-Zweig als `ByteArray` smart-casten.

**Prüfstatus bei Erstellung:** statische Reparaturprüfung und Paketintegrität PASS. Echter Windows-Gradle-Lauf `:app:testDebugUnitTest` + `assembleDebug`, APK-Erzeugung, ADB-Installation und Xiaomi-Test v0.5.24: **NOT RUN**. v0.5.24 darf bis dahin nur als **Source Candidate** gelten.

---

# LFN-Scout – Projektstatus v0.5.23 (25.09.2026)

**Version:** `0.5.23` / `versionCode 170` / Paket `de.brummbeobachter.app`.

**Analyzer-Audio:** lokale Analyzer-WAVs verwenden die gleiche Namenspolicy wie Beobachter-Audio und liegen direkt im bestehenden Hauptpfad `Download/LFN-Scout/`; kein zusätzliches Audio-/LocalSaves-Unterverzeichnis. Analyzer-WAVs werden im `AudioArchive` geführt, über denselben Supabase-WAV-Pfad übertragen und nach erfolgreichem Upload von derselben 24-h-Retention verwaltet. `origin=ANALYZER` bleibt als Metadatum erhalten. Statistik-JSON/Manifest liegen im selben Hauptpfad und werden lokal mit dem Archiveintrag verwaltet; Statistik-Upload nach Supabase bleibt bis zum passenden Schema ausgesetzt.

**Analyzer-Bild:** manueller Kamera-Langdruck friert zuerst den Bildzustand ein, öffnet danach einen optionalen Kommentardialog und rendert den Kommentar unten in den Wasserfall, bevor PNG/JPG endgültig geschrieben wird.

**Prüfstatus:** `verify_v0523_delivery.py` **21/21 PASS**; Kotlin-PSI **92 Dateien / 0 Syntaxfehler PASS**; Statistik-/Gain-Hosttest **PASS**; Wasserfall-Regressionscheck **PASS**. ZIP-Integrität wird vor Auslieferung geprüft. Reale Windows-Ausführung `VORTEST_INSTALL.bat`, ADB-Installation, Xiaomi-Funktionstest und realer Analyzer-Supabase-Upload v0.5.23: **NOT RUN**.

---

# LFN-Scout – Projektstatus v0.5.22 (25.09.2026)

**Version:** `0.5.22` / `versionCode 169` / Paket `de.brummbeobachter.app`.

**Zweck:** reine Windows-Vortest-Startkorrektur auf Basis v0.5.21. Der reale Nutzerlauf von v0.5.21 scheiterte noch vor PREPARE, weil `cmd.exe` die in `VORTEST.bat` eingebettete PowerShell-RegEx zur Versionsbestimmung falsch zerlegte.

**Korrektur:** BAT-Wrapper parsen die Version nicht mehr selbst. `VORTEST.bat` und `PREPARE_PROJECT.bat` haben versionsneutrale Kopfzeilen. `tools/run-vortest.ps1` und `tools/package-active.ps1` bleiben die einzigen für den Vortest relevanten Leser der `versionName` aus `app/build.gradle.kts` und verwenden sie für versionierte Ausgabedateien. Dadurch entfällt die problematische CMD-/PowerShell-Verschachtelung vollständig.

**Prüfstatus:** statische Skriptprüfung und ACTIVE-ZIP-Integrität PASS. Reale Windows-Ausführung/ADB-/Xiaomi-Installation v0.5.22: **NOT RUN**. Keine Android-Laufzeitänderung gegenüber v0.5.21/v0.5.20.

---

# LFN-Scout – Projektstatus v0.5.21 (25.09.2026)

**Version:** `0.5.21` / `versionCode 168` / Paket `de.brummbeobachter.app`.

**Zweck:** reine Build-/Vortest-Korrektur auf Basis v0.5.20. Der reale v0.5.20-Windows-Lauf bestätigte PREPARE, Android-Unit-Tests und `assembleDebug` als PASS. Der anschließende Fehler 51 war kein Android-Buildfehler, sondern ein Versionsversatz im Vortest: Paketierung erzeugte v0.5.20, die Prüfung suchte noch v0.5.19.

**Korrektur:** `run-vortest.ps1`, `VORTEST.bat` und PREPARE lesen `versionName` jetzt direkt aus `app/build.gradle.kts`. APK-/ACTIVE-/Lognamen und Prüfschritte verwenden damit dieselbe Version. Keine Android-Laufzeitänderung gegenüber v0.5.20.

**Prüfstatus:** statische Prüfung der Versionskopplung PASS; ACTIVE-ZIP CRC/Probe-Extraktion/Dateimanifest vor Auslieferung PASS. Reale Windows-Ausführung und ADB-/Xiaomi-Installation v0.5.21: **NOT RUN**.

---

# LFN-Scout – Projektstatus v0.5.20 (25.09.2026)

**Version:** `0.5.20` / `versionCode 167` / Paket `de.brummbeobachter.app`.

**Umfang:** Korrektur- und UI-Version auf Basis v0.5.19. Die korrekten L50/L90-Werte bleiben erhalten; die durch v0.5.19 entstandene unsichtbare WAV-Wasserfall-Darstellung wird separat behoben. Gain-referenzierte FFT-Bins werden nicht mehr vor der Farbabbildung bei -120 dBFS abgeschnitten.

**Audio-Kommentar:** Analyzer `SAVE AUDIO` friert den vollständigen Audioausschnitt zuerst ein und öffnet erst danach den gemeinsamen Kommentar-Dialog. Beobachter und Analyzer verwenden damit dieselbe Dialogkomponente und dieselben konfigurierten Audio-Timeouts. Analyzer-Kommentare werden lokal in Statistik-JSON und Manifest gespeichert. Tippen nach dem SAVE-Tap kann den bereits fixierten Audioausschnitt nicht mehr verlängern.

**UI:** Zusatzinfo neben Analyzer `SAVE AUDIO` zweizeilig. Beobachter-Audio-/Statistik-Setup aus dem Hauptpanel in Einstellungen → Audioaufnahme verschoben. `ENTWICKLERMODUS` bei aktivem Modus oben rechts auf Titelhöhe statt eigener Zeile. Highspeed-Impulsdiagnose nur im Entwicklermodus und ganz unten in Zusatzfunktionen. Appweite Regel bleibt: keine spontanen Zusatztexte in bestehende Mess-/Displayzeilen einschieben.

**Navigation:** Android-System-Zurück (Taste/Geste) geht über den tatsächlichen Panel-Verlauf zurück; auf der Hauptansicht bleibt das normale Android-Verhalten. Analyzer-interne Einstellungen werden weiterhin zuerst intern geschlossen.

**Prüfstatus:** `python tools/verify_v0520_delivery.py` **24/24 PASS**; `python tools/test_waterfall_v0520.py` **PASS**; Host-Statistiktest mit aktuellem `StatisticsWindowEngine`/`GainReferencePolicy` **PASS** (40 Bänder, +50-dB-Kompensation, L90/Leq, 10-Hz-Dominanz); Kotlin-PSI **92 Dateien / 0 Syntaxfehler PASS**. Gradle-Testversuch in der Erstellungsumgebung konnte nicht starten, weil die ACTIVE-Kopie kein `gradle-wrapper.jar` enthält (`GradleWrapperMain` fehlt); daher kein Android-/Compose-Buildnachweis. Windows-`VORTEST_INSTALL.bat`, ADB-Installation und Xiaomi-Test von v0.5.20: **NOT RUN**.

**Backend/Packaging:** Keine neuen Supabase-Tabellen/-Uploads. ADB-Warteschleife aus v0.5.19 bleibt bestehen. BASE `LFN-Scout_BASE_ASSETS_R1.zip` unverändert.

---

# LFN-Scout – Projektstatus v0.5.19 (25.09.2026)

**Version:** `0.5.19` / `versionCode 166` / Paket `de.brummbeobachter.app`.

**Umfang:** Korrektur-/Konsistenzversion fuer Analyzer-Save-UI, gemeinsame Gain-/L50-/L90-Referenzierung und komfortablere ADB-Installation. `SAVE-Setup` liegt in Analyzer-Einstellungen → Statistik; `SAVE AUDIO` ist ein 48-dp-Actionbutton unten neben festem Buffer-/Quotentext. Erfolgreicher lokaler Save meldet `Lokal gespeichert` als 3-s-Overlay nahe dem Button. Appweite Vorgabe bleibt: keine spontanen Zusatztexte in vorhandene Display-/Messzeilen.

**Gain/Statistik:** Recording-Gain wird nur dann zurueckgerechnet, wenn er tatsaechlich im analysierten PCM enthalten ist. +50 dB werden einmalig als -50 dB Pegelkorrektur referenziert. Der Analyzer-Lokalsave ist PRE-GAIN und darf nicht doppelt korrigiert werden. Bei geeigneter 1-kHz-WAV verwenden WAV-Anzeige und Statistik-JSON denselben `StatisticsWindowEngine.analyzeOverall()`-Pfad fuer L50/L90.

**Vortest:** `VORTEST_INSTALL.bat` wartet nach PASS von PREPARE, Unit-Tests, Build, APK und ACTIVE-Paket ohne Standard-Timeout auf genau ein autorisiertes ADB-Geraet. Keine/unauthorized/offline/mehrere Geraete fuehren zum sichtbaren Weiterwarten mit 2-s-Polling; Installation startet erst bei genau einem `device`.

**Pruefstatus:** Host-Kotlin-Selbsttest des gemeinsamen Gain-/Statistikpfads PASS. Versionsspezifische statische Pruefung **33/33 PASS**; ACTIVE-ZIP-Integritaet wird vor Auslieferung vollstaendig geprueft. Echter Windows-Gradle-Lauf, reale ADB-Warteschleife und Xiaomi-Funktionstest von v0.5.19: **NOT RUN**. Vorgänger v0.5.18.3 hatte real PREPARE/Unit-Tests/assembleDebug/ACTIVE-Paket PASS.

**Backend:** Keine neuen Supabase-Tabellen oder Uploadpfade in v0.5.19. BASE `LFN-Scout_BASE_ASSETS_R1.zip` bleibt unveraendert.

---

# LFN-Scout – Projektstatus v0.5.18.4 (25.09.2026)

**Version:** `0.5.18.4` / `versionCode 165` / Paket `de.brummbeobachter.app`.

**Zweck der Zwischenversion:** ausschliesslich Verbesserung der Windows-Vortest-/ADB-Automatisierung. Alle nativen Prozessmeldungen von STDOUT und STDERR werden waehrend der Ausfuehrung live auf dem Bildschirm dargestellt und gleichzeitig ins Vortest-Log geschrieben. Auch die nicht-prozessgebundenen Schritte APK-Pruefung, ACTIVE-ZIP, SHA-256, ADB-Geraetezaehlung und Endzusammenfassung erscheinen auf dem Screen.

**Befund aus dem realen v0.5.18.3-Nutzerlauf:** PREPARE PASS, Unit-Tests PASS, `assembleDebug` PASS, ACTIVE-Paket PASS. Zwei Kotlin-Warnungen wurden angezeigt, aber der Build war erfolgreich. `adb devices` zeigte nur die Kopfzeile `List of devices attached` und kein Geraet; deshalb korrekter Abbruch mit Fehlercode 62 vor der Installation. Damit ist der Build-/Paketierungsweg von v0.5.18.3 real bestaetigt, die ADB-Installation aber nicht.

**v0.5.18.4:** bei ADB-Fehlern klare Bildschirmdiagnose und immer kompakte Schrittzusammenfassung. Keine beabsichtigte Android-Laufzeitanderung gegenueber v0.5.18.3. BASE R1 bleibt unveraendert.

**Pruefstatus v0.5.18.4:** statische Skript-/Lieferpruefung PASS; echter Windows-/Gradle-/ADB-Lauf dieser neuen Skriptversion sowie Xiaomi-Funktionstest **NOT RUN**.

---
# LFN-Scout – Projektstatus v0.5.18.3 (25.09.2026)

**Version:** `0.5.18.3` / `versionCode 164` / Paket `de.brummbeobachter.app`.

**Zweck der Zwischenversion:** ausschließlich Korrektur der Windows-Vortest-/ADB-Automatisierung auf Basis v0.5.18.2. Der vom Nutzer gemeldete Abbruch erfolgte trotz korrekt gefundenem JDK 17, weil Windows PowerShell 5.1 die normale STDERR-Ausgabe von `java -version` in der bisherigen `2>&1`-Pipeline als `NativeCommandError` behandelte.

**Korrektur:** Native Prozesse werden in `tools/run-vortest.ps1` über `System.Diagnostics.Process` gestartet. STDOUT und STDERR werden getrennt protokolliert; allein der echte Prozess-Exitcode entscheidet über PASS/FAIL. Das gilt für PREPARE, Gradle, ACTIVE-Paketierung und ADB. Die ACTIVE/BASE-Aufteilung bleibt bestehen; `LFN-Scout_BASE_ASSETS_R1.zip` ist unverändert weiterzuverwenden.

**Prüfstatus:** Quell-/Skriptprüfung vorgesehen und in `BUILD_VERIFICATION_v0.5.18.3.md` dokumentiert. Reale Windows-PowerShell-/Gradle-Ausführung, ADB-Installation und Xiaomi-Test dieser Korrektur: **NOT RUN**, bis Nutzerlog vorliegt. Keine beabsichtigte Android-Laufzeitänderung gegenüber v0.5.18.2.

---
# LFN-Scout – Projektstatus v0.5.18.1 (25.09.2026)

**Version:** `0.5.18.1` / `versionCode 162` / Paket `de.brummbeobachter.app`.

**Zweck der Zwischenversion:** Build-/Vortest-Automatisierung auf unveränderter funktionaler Basis v0.5.18. `VORTEST.bat` fasst PREPARE, Android-Unit-Tests und Debug-APK-Build zu einem fehlersensitiven Ein-Klick-Ablauf zusammen. PREPARE kann dafür ohne Zwischenpausen aufgerufen werden. Ergebnislog und versionierte Debug-APK werden unter `Builds/` abgelegt.

**Prüfstatus:** statischer Automationsvertrag 12/12 PASS. Vollständiger Windows-Lauf von `VORTEST.bat`, Android-Gradle-Build und Xiaomi-Test **NOT RUN** in der Erstellungsumgebung. Die offenen v0.5.18-Audio-/Statistik-Gerätetests bleiben unverändert offen. Supabase wurde nicht geändert.

---
# LFN-Scout v0.5.18 – aktueller Quellstand (25.09.2026)

Implementiert im Quellcode: Navigation mit vier Icons, lokale WAV-/Statistik-Vorbereitung für Beobachter/Analyzer, konfigurierbare Dauer/Frequenz, 1-kHz-Audio-Ringbuffer mit PAUSE, dB-Gain-Kompensation und lokale JSON-Sidecars. Details `docs/project/LOCAL_AUDIO_STATS_v0.5.18.md`. Keine Änderungen in Supabase; Beobachter-Cloud-Upload unverändert. Die vollständige 8-kHz-FAST-Statistik, Impulse, Modulation und Supabase-Statistiktabellen bleiben geplant.

**Status:** Android-/Compose-Build NOT RUN; Xiaomi v0.5.18 NOT RUN; bestehender vom Nutzer bestätigter v0.5.17 Portrait-/Analyzerstand bleibt Rückfallpunkt. Host-/Statisch-Prüfungen siehe `BUILD_VERIFICATION_v0.5.18.md`. Keine Gerätefreigabe ohne Testplan.

---
# LFN-Scout v0.5.17 – Hochformat für gesamte App (24.09.2026)

**Aktueller Quellstand:** `versionName=0.5.17`, `versionCode=160`, Paket-ID `de.brummbeobachter.app`. Vollständiger v0.5.16-Projektstand bleibt als gesondertes Original-ZIP erhalten. Ab v0.5.17 erzwingen `MainActivity` und `ReminderAlarmActivity` das Hochformat. Drehung des Xiaomi darf den Analyzer weder ausblenden noch dessen Audio-/FFT-/Wasserfall-Zustand unterbrechen. Implementierung ausschließlich in Android-Manifest und Versionsmetadaten; keine Änderung an Kotlin, Audio, Rolling, Setup oder Export.

**Prüfstatus:** statische XML-/Quellvertragsprüfung vorgesehen; Android-SDK-/Gradle-Build und Xiaomi-Gerätetest NOT RUN. Gerätefreigabe erst nach realen Tests nach `docs/project/TESTPLAN_v0.5.17.md`. Für v0.5.16 wurde vom Nutzer der Export einer alten Setup-4-Datei, einer getrennten CURRENT-v2-Datei und von Setup 1 per Screenshot und `Setups4.zip` nachgewiesen; nicht als vollständige Migration-/Buildfreigabe interpretieren.

**Weiterhin offen:** Alle dokumentierten v0.5.16-Restpunkte einschließlich allgemeiner JSON-Import, Konfliktlösung, interner v2-Migration, 16:1-Pegelabweichung, schneller Impulserkennung, gesamter Android-/Xiaomi-Regressionssuite.

---
# LFN-Scout v0.5.16 – Migrations-/JSON-Exportkorrektur (24.09.2026)

**Quellstand:** versionName `0.5.16`, versionCode `159`, Android-Paket unverändert. Basis v0.5.15. **Statischer Testkandidat**, kein ausgeführter Android-/Compose-Build oder Xiaomi-Test.

**Diagnose:** Die vom Nutzer gelieferte alte `Setup_4_48k.json` hat u. a. `range=22000`, während das interne Setup 4 `range=200` besitzt. Der v0.5.15-Migrator erkennt den Konflikt und gibt `false` zurück; der Export wirft daraufhin eine `IllegalStateException`. Das ist eine aus Daten und Code belegte Konfliktkonstellation, kein geräteseitig nachgewiesener vollständiger Stacktrace.

**Korrektur:** Bei ausstehenden Migrationen wird der intakte Altname nie vom normalen Export überschrieben. Das aktuell intern gespeicherte Setup erhält einen unabhängigen `Setup_N_CURRENT_v2.json`-Namen. Der Export anderer Slots läuft weiter. Der Diagnose-ZIP enthält nun `Settings/setup_migration.json` (ausstehende Slots und konkrete Fehlermeldungen), und die GUI zeigt Fehlermeldungen einschließlich `exception.message`. Nach vollständig validiertem Import inkl. Preference-Readback wird die Altdatei wie zuvor gelöscht; danach kann der reguläre v2-Dateiname verwendet werden. Konflikte bleiben erhalten und erfordern eine spätere, ausdrücklich geprüfte Konfliktlösung. Keine stillschweigende Werteübernahme, kein automatisches Löschen bei Konflikt.

**Offen:** Echter vollständiger JSON-Import, interne Schema-v2-Migration, einheitlicher LIVE-/STOP-Parameterkatalog und Konflikt-UI. Ebenso keine Änderung an FFT, Audio/ROLLING, Analysepegel oder der Konfiguration des Nutzers. Eine v0.5.16-Installation passt Setup 4 **nicht automatisch** auf 8 kHz an.

**Windows-Prüfung:** `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`, danach Xiaomi-Regression nach `docs/project/TESTPLAN_v0.5.16.md`.

---

> **v0.5.15 (Implementierung, nicht geräteverifiziert):** Konservative Migration zugänglicher alter v1-Setup-JSONs vor dem v2-Export mit Readback und erst anschließender Löschung. Konflikte erhalten Altdateien und verhindern Export. Das interne Preference-Schema bleibt bestehen; kein allgemeiner JSON-Import. Android-/Xiaomi-/MediaStore-Test ausstehend. `docs/project/LEGACY_SETUP_MIGRATION_v0.5.15.md`.

## v0.5.14 (Quellstand; Android-Build/Device NOT RUN)
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

# LFN-Scout v0.5.13 – 5-s-Pop-up für Wasserfall- und FILTER-Meldungen (24.09.2026)

**Quellstand:** `versionName=0.5.13`, `versionCode=156`, Basis v0.5.12 mit wiederhergestelltem kumulativem README. Der alte permanente Wasserfall-/FILTER-Status unter der Anzeige entfällt; eine zweizeilige Meldung wird fünf Sekunden nach dem letzten Anzeigenwechsel unten rechts über dem *unveränderten* Wasserfall gezeigt. Kein Bildschirmhöhe-/Canvas-/PNG-/Signalpfad-Umbau. CAMERA- und ORIGINAL/FILTER-Buttons einschließlich Icon-Assets bleiben erhalten.

**Prüfstatus:** separater Text-JVM-Hosttest und statische Quellprüfung durchgeführt; siehe `BUILD_VERIFICATION_v0.5.13.md`. Android-/Compose-Build und Xiaomi **NOT RUN**. Geräteseitige Position, Anzeigedauer und Lesbarkeit sind erst nach `docs/project/TESTPLAN_v0.5.13.md` freizugeben.

**Offen:** 16:1-Pegelabweichung; echte FAST-Impulsereignis-Erkennung/Filter-Ringing; optionaler FILTER-Colorbar-Tap; noch offene Android-/Xiaomi-Abnahme und praktischer Supabase-Restore.

---

# LFN-Scout v0.5.12 – ORIGINAL-Icon und FILTER-Pegelautomatik (24.09.2026)

**Quellstand:** `0.5.12` / `versionCode 155`; Basis v0.5.11. Das ORIGINAL-Symbol wurde durch das **unveränderte, vom Nutzer hochgeladene PNG** ersetzt. FILTER behält fünf symmetrische Balken. FILTER-Langdruck startet unabhängig von ORIGINAL eine eigene dBFS-Pegelautomatik anhand der tatsächlichen Filterbandpegel. FILTER erhält separate persistente dBFS-Anzeigegrenzen sowie Erfassungsdauer und Pegelreserve, einschließlich Setup-Slots 1–3 und 8-kHz-Profil. Bildschirm und Bildexport besitzen getrennte FILTER- und ORIGINAL-Skalen. Keine Audio-/Rolling-/DSP-Änderung beabsichtigt.

**Prüfung:** separater Kotlin-JVM-Hosttest 10/10 PASS; statischer Quellvertrag 18/18 PASS; separater Renderer-Hosttest 5/5 PASS; Kotlin-PSI 110 Dateien/0 Syntaxfehler; alter Direkt-Core-Hosttest 33/33 PASS. Der anschließend angestoßene ältere v0.5.06-Selbsttest wurde wegen Zeitlimit beendet (**kein PASS**). Vollständiger Android-/Compose-Build und Xiaomi **NOT RUN**. Deshalb ausschließlich Quell-/Hosttestkandidat. Siehe `BUILD_VERIFICATION_v0.5.12.md`, `docs/project/FILTER_AUTORANGE_AND_ICONS_v0.5.12.md`, `docs/project/TESTPLAN_v0.5.12.md`.

**Offen:** FILTER-Colorbar antippen (optional, nicht implementiert); eigenständige Onset-/Ringing-Engine und physikalische FAST-Latenz; 16:1-Pegelverlust; praktische Xiaomi-/Android-Abnahme; externe Restore-Probe. **Nicht die ausstehenden Gerätefunktionen als PASS bewerten.**

---

# LFN-Scout v0.5.11 – Experimenteller FAST-Filterwasserfall (24.09.2026)

**Quellstand:** `0.5.11` / `versionCode 154`, Basis v0.5.10. Neues Produkt-Feature in Stufe A umgesetzt: konfigurierbare Filterbank auf bestehendem 8-kHz-PCM (2–40 Filter, Standard 10, unabhängiges FILTER MIN/MAX), zwei verbindliche originale 48-dp-Icons, vier Darstellungszustände, FILTER-LUT mit dBFS-Skala, transientabhängige Mischung im bisherigen Wasserfall und einheitlicher Bitmap-PNG/JPG-Pfad. Kein zweiter AudioRecord. Direkt-Analyzer zusätzlich 5-ms-Hop und wählbarer Glättungszeitmodus für 5 s. Einstellungen und Setup 1–4 erweitert.

**Prüfung:** JVM-FAST-Host 11/11 PASS; historischer Direkt-Host 33/33 frisch PASS; Kotlin-PSI-Syntax 185/0; alter Timingtest TIMEOUT. **Android-/Compose-Build und Xiaomi NOT RUN; Stand nur Quell- und DSP-Testkandidat**. Früherer v0.5.09-Windows-Build FAIL; v0.5.10-Windows-Abnahme wurde hier nicht belegt. Diese Stufe ist KEINE fertige frequenzaufgelöste Impulserkennung. Separate FAST-Zeilen ohne SLOW-Warm-up, Ereignis-Onset/Fusion, Ringing-Klassifikation, Kalibrierung und Tests aus LFN-IMP-001 bleiben offen. 16:1-Pegelverlust ebenfalls offen. Vollständige Details unter `docs/project/FILTERBANK_v0.5.11.md`.

---

# LFN-Scout v0.5.10 – Buildkorrektur für v0.5.09 (21.09.2026)

**Quellversion:** `0.5.10` / `versionCode 153`; Paket-ID unverändert. Nutzer-Windows-Build von **v0.5.09 ist nachweislich FAIL** bei `:app:compileDebugKotlin`, `LiveAnalyzerController.kt:534`, inkompatibler `contains`-Empfänger. `DirectAnalyzerConfig.permissibleHops` ist `Set<Int>`, `AnalyzerTiming.allowedFftHops` ist `IntArray`. Der gemeinsame bedingte Ausdruck `fftHop in if (...) set else intArray` kann vom Kotlin-Typsystem nicht als `contains` aufgelöst werden. Korrektur: explizites `in` in jedem einzelnen Zweig, für Direkt- bzw. Rollingmodus unveränderte erlaubte Werte. Keine weitere App-Funktion oder Audio-/Rolling-Logik absichtlich geändert.

**Nachweis:** vom Nutzer v0.5.09 FAIL. Die v0.5.10-Korrektur wird durch separat kompilierte Kotlin-Regression für gemischte Set/IntArray-Typen und Quellcode-Regression überprüft; Ergebnisse unter `BUILD_VERIFICATION_v0.5.10.md`. **Vollständiger Android-/Compose-Build und Xiaomi-Test der v0.5.10 bleiben NICHT AUSGEFÜHRT** (kein Android-SDK in der Erstellungsumgebung). Alle v0.5.09-Funktionen erst nach erfolgreichem Windows-Build und tatsächlichem Gerätetest freigeben. Bekannte Pegelabweichung 16:1 weiter offen.

---

# LFN-Scout v0.5.09 – Direkt-Analyzer-Erweiterung (21.09.2026)

**Version:** `0.5.09` / `versionCode 152` / App-ID `de.brummbeobachter.app`. Vollständige Quellbasis v0.5.08. Der vorherige 48-kHz-Direktmodus unterstützt nun auswählbare Eingangsrate/-quelle, 9 FFT-Größen, 9 Fenster, direkte Dezimierung 0–12, sieben Transformintervalle, α 0–1, 0–2 Peakmarker, optionalen DC-Abzug, Stay Awake und zwei unabhängige digitale Testtöne. Setup-Slot 4 und getrenntes Direktprofil enthalten die neuen Werte. Exklusive Mikrofon-/Rolling-Zustandsmaschine und das v0.5.08-Zeitband/Farbskala bleiben konzeptionell unverändert.

**Prüfung:** JVM-Host 33/33 PASS; alter Timingtest 6/6 PASS; Kotlin-PSI-Syntax 181 Dateien/0 Fehler. **Android-Gradle/Compose-Build und Xiaomi-Test NICHT AUSGEFÜHRT.** Geräteseitige 96-kHz-Verfügbarkeit nicht gesichert. Nur ganzzahlig teilbare Decimation-Kombinationen werden angeboten. Alte 16:1-Pegelabweichung **OFFEN**. Details `BUILD_VERIFICATION_v0.5.09.md`, `docs/project/DIRECT_ANALYZER_v0.5.09.md`, `docs/project/TESTPLAN_v0.5.09.md`.

---

# LFN-Scout v0.5.08 – Zeitachse und Kamerameldung (21.09.2026)

**Version:** `0.5.08` / `versionCode 151`; Quellbasis v0.5.07. Die Wasserfall-Sekundenachse ist durch das schwarze, mit der vorhandenen Farbskala kombinierte Echtzeitband (`dd.MM`, `HH:mm`, max. 5 Ticks) ersetzt. Spektrum und Wasserfall besitzen weiterhin identische x-Plotgrenzen. PNG/JPG nutzt dieselbe Zeitdatenquelle. Das manuelle Kamera-Pop-up wird jetzt in der Nähe des Buttons gezeigt.

**Prüfstatus:** Eigenständige Kotlin-JVM-Zeitachsenprüfung PASS. Android-/Compose-Build und Xiaomi für v0.5.08 **NOT RUN**. Der vorherige v0.5.06-Windows-Build schlug fehl; ein neuer v0.5.07-Erfolg ist nicht nachgewiesen. Die 16:1-Pegelabweichung bleibt offen. Details `BUILD_VERIFICATION_v0.5.08.md`, `docs/project/TESTPLAN_v0.5.08.md`.

---

# LFN-Scout v0.5.07 – Buildkorrektur (21.09.2026)

**Version:** `0.5.07`, `versionCode 150`, Paket-ID unverändert. Basis: v0.5.06 / 149. Der Nutzer meldete für v0.5.06 einen echten Windows-Gradle-FAIL in `:app:compileDebugKotlin`. Die bisherige Behauptung, v0.5.06 sei zur Android-Abnahme bereit, ist damit widerlegt. Letzter vom Nutzer praktisch geprüfter Referenzstand: v0.5.05 (Pop-up war dort noch fehlerhaft).

**Gezielt korrigiert:** `AnalyzerRawLfSession.resetMaxHold()` delegiert an den bereits vorhandenen, synchronisierten Core-Reset. In `LiveAnalyzerScreen.kt` fehlte nach der unteren Kamerasymbol-/MAX-CLR-Zeile eine schließende Klammer für die scrollende Column. Jetzt befindet sich das Popup im äußeren `BoxWithConstraints`-Viewport, und `cursorReadout()` / `makeWaterfall()` sind wieder echte Top-Level-Funktionen. Die nachfolgend gemeldeten Kotlin-Typ- und `Modifier.align`-Fehler waren wahrscheinlich Folgefehler des fehlenden Blockendes; Android-Compiler muss dies bestätigen. Kein beabsichtigter Eingriff in Rolling, Audio-Modi, WAV, Screenshot-Writer oder DSP-Berechnung.

**Nachweise:** Nutzer-Windows-Gradle v0.5.06 = FAIL (16 gemeldete Kotlin-Diagnosen). v0.5.07: lokaler Core-MaxHold-Selbsttest 8/8 PASS; Timing-Hosttest 6/6 PASS; statischer Buildfix-Vertrag PASS. **Android-/Compose-Gradle-Build NOT RUN (SDK fehlt); Xiaomi v0.5.07 NOT RUN.** Der v0.5.06-Quellvertrag war unzureichend: er prüfte den Aufruf, nicht die Existenz der Session-Methode, und nicht die tatsächliche UI-Klammerung. Die 16:1-Pegelabweichung bleibt offen.

**Nächster Schritt:** Auf Windows `PREPARE_PROJECT.bat`, danach `gradlew.bat :app:testDebugUnitTest --stacktrace` sowie `gradlew.bat assembleDebug --stacktrace`; erst bei `BUILD SUCCESSFUL` Xiaomi-Plan aus `docs/project/TESTPLAN_v0.5.07.md` durchführen. Die vier Setup-Slots bleiben persistent.

---

# LFN-Scout v0.5.06 – aktueller Quellstand (21.09.2026)

**Version:** `0.5.06`, `versionCode 149`, `applicationId de.brummbeobachter.app`. Vollständiger Quellstand aus v0.5.05. Die neue Version erweitert **nur** Spektrumdarstellung/-bedienung, manuelle Bild-Speicherrückmeldung und zugehörige persistente Einstellungen. Rolling, WAV, Audio-Umschaltung, Seismik und Backend nicht absichtlich geändert.

**Geräte-Referenz:** Nutzer meldete v0.5.05 auf seinem Xiaomi als getestet OK, korrigierte jedoch anschließend: das Pop-up bei langem Kameradruck fehlt. v0.5.05 bleibt damit *außerhalb dieses einen Prüfpunkts* der zuletzt bestätigte Geräte-Stand. Die zuvor gemeldete 16:1-Energie-/Pegelabweichung bleibt offen.

**In v0.5.06 implementiert (nicht gleichbedeutend mit geprüft auf Android):** Aktuell/Average/Maxhold im Spektrum unabhängig EIN/AUS, mit getrennten persistenten Einstellungen auch pro Setup-Slot (1–4). MAX/CLR erscheint ausschließlich bei sichtbarer Maxhold-Kurve; seine 48-dp-Bedienfläche ist nicht größer als die Kamera (48 dp). Der Reset verwendet den bereits vorhandenen DSP-MaxHold-Reset, erhält FFT-/Audio-Zeitbasis und Wasserfall. Sichtbarkeit gilt gleichermaßen für Bildschirm und PNG/JPG-Offscreen-Export. Manueller Bildexport meldet asynchron erst nach erfolgreichem Dateischreiben (oder nach Fehler) ein für ca. 2,2 s fest über dem scrollenden Diagramm positioniertes Pop-up; automatische Exporte lösen keine solche Meldung aus. Kamera-Kurztipp bleibt unabhängig.

**Lokal getestet:** Analyzer-v0.5.06-Core-MaxHold-Selbsttest **8/8 PASS**, bestehender v0.5.04-Hosttest **16/16 PASS**, v0.5.04.1-Zeitbasis **6/6 PASS**, v0.5.03-FIR-/Statistiktests **20/20 PASS**; statischer v0.5.06-Quellvertrag **21/21 PASS**. **NOT RUN:** vollständiger Android-/Compose-Gradle-Build und Xiaomi-Praxistest dieser Version. Die Quellprüfung beweist weder Laufzeitfunktion noch sichtbares Pop-up auf dem Gerät.

**Noch offen:** Die gemeldete 16:1-Pegelabweichung (Ursache nicht gesichert), praktischer Restore auf ein vollständig leeres Supabase-Projekt, Mithören, Superfast und Radiushistogramme. Die neuen drei Kurvenschalter/Pop-up sind **erst nach Gerätetest freizugeben**. Testplan `docs/project/TESTPLAN_v0.5.06.md`; technische Details `docs/project/ANALYZER_SPECTRUM_AND_CAMERA_v0.5.06.md`.

---

# LFN-Scout v0.5.05 – Aktueller Quellstand (21.09.2026)

**Implementiert:** Crash-Korrektur im Zeitbasis-/Snapshot-Pfad, wiederhergestellte PAUSE-Bedienung, bestätigungsorientiertes Rolling-Restore-Logging, Setup-JSON- und gefilterter Diagnose-Export. Basiert auf v0.5.04.2. App-ID `de.brummbeobachter.app`, versionCode 148.

**Tatsächlich getestet:** Host-Timingtest 6/6 mit JUnit-kompatiblem Teststub. **NOT RUN:** vollständiger Gradle-/Compose-Build für v0.5.05, Xiaomi-Regression, tatsächliche Stabilisierung 48 kHz. Der zuvor gelieferte Logcat belegte den `IllegalArgumentException`-Pfad; die neue Version ist noch nicht auf dem Gerät validiert. 8-kHz-Modi sind vom Nutzer auf v0.5.04.2 als funktionierend bestätigt.

**Offen:** 16:1-Pegelabweichung, PAUSE-Funktion am Gerät abnehmen, Export-/MediaStore-Update überprüfen; MAX/CLR und unabhängig schaltbare Akt/Average/Maxhold-Kurven später. Details `docs/project/TESTPLAN_v0.5.05.md`, `docs/project/ANALYZER_DATA_EXPORT_v0.5.05.md`.

---

# LFN-Scout v0.5.04.2 – Diagnoserelease (21.09.2026)

**Quellstand:** `versionName=0.5.04.2`, `versionCode=147`, Paket-ID unverändert. Aus v0.5.04.1 übernommen. **Keine Fehlerkorrektur der Audio-Zustandsmaschine:** Ergänzung ausschließlich um Diagnoseereignisse, keine beabsichtigte Änderung von Audio-Routing, STOP, Rolling oder FFT.

**Nutzerbeobachtung:** 8-kHz-Modi funktionieren (die frühere gegenteilige Aussage ist zurückgenommen). Im 48-kHz-Direktbetrieb treten sporadische Abstürze bei STOP + anschließendem Verlassen des Panels mit Rolling-Restore sowie beim erneuten 48-kHz-Start nach wiederhergestelltem Rolling auf. PAUSE-Taste fehlt. 16:1-Pegelabweichung weiterhin ungeklärt. Die Ursachen der Abstürze sind **nicht nachgewiesen**.

**Neu:** `AnalyzerAudioTrace.kt` schreibt mit Zeitstempel (über `DebugLogger`), Sitzungskennung (`sid`), Prozess-ID (`pid`) und Thread die Übergänge `AUDIO_DIAG_*` in `lfn_scout_debug.log` und die entsprechenden Ereignisse nach Android-Logcat unter `LFN-AUDIO`. Geloggt werden: Modus-Anforderung/Start, ursprünglicher Rolling-Zustand, Rolling-Stop/-Freigabe, 48-kHz-Service-Start/-Ergebnis, Mikrofon-Lease, tatsächliche Sample-Rate und Recording-State, PCM-/Analyzer-Heartbeat im 15-s-Intervall, STOP/RESTART, Panel-Verlassen, Restore, Capture-Fehler, Cancel und Cleanup. Keine PCM-Werte, GPS-Koordinaten, Zugangsschlüssel oder Benutzernamen. Der weiterhin vorhandene 8-kHz-Pfad wird nicht funktional verändert.

**Nicht nachgewiesen:** Vollständiger Android-/Compose-Build für dieses Paket, Xiaomi-Gerätetest, tatsächliche Crashursache und etwaige Stabilisierung. Der vom Nutzer vorgelegte Gradle-FAIL betraf v0.5.04; nicht als Ergebnis für diese Version umdeuten.

**Nächster Schritt:** `docs/project/AUDIO_DIAGNOSE_v0.5.04.2.md`, `docs/project/TESTPLAN_v0.5.04.2.md` befolgen, reproduzierbaren Crash und die letzten Diagnoseereignisse vor dem Absturz erfassen. Erst danach gezielte Korrektur.

---

# Projektstand v0.5.04.1 – Buildkorrektur vom 21.09.2026

**Tatsächlicher Quellstand:** `0.5.04.1`, `versionCode 146`, unveränderte Paket-ID `de.brummbeobachter.app`. Der v0.5.04-Windows-Lauf des Nutzers ist **FAIL**: zwei `AnalyzerTimingTest`-Fälle (`IllegalArgumentException`) und Kotlin-Kompilierfehler im Autorange-Callback. Beide Ursachen wurden im Quell- bzw. Testcode gezielt korrigiert. Die tatsächliche erneute Gradle-Kompilierung/Android-Integration und Xiaomi-Abnahme sind **NOT RUN**; eine APK wird nicht als erfolgreich gebaut bezeichnet. Einzelheiten in `BUILD_VERIFICATION_v0.5.04.1.md`.

**Umfang der Korrektur:** Nur Kotlin-Smart-Cast, Timing-JUnit-Tests und versionierte Dokumentation. 8-/48-kHz-Betriebsarten, STOP/Panel-Verlassen-Restore, Daten- und WAV-Verhalten sowie bislang vorhandene Analyzer-Funktionen bleiben im Produktionscode unverändert. Die 16:1-Pegelabweichung bleibt **OFFEN**. Die unabhängigen Spektrumkurven Aktuell/Average/Maxhold mit kompaktem MAX/CLR-Knopf sind **vorgemerkt, nicht implementiert**.

**Nächster Schritt:** Windows `PREPARE_PROJECT.bat` (bei neuem Ordner), `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`. Erst nach erfolgreichen Ergebnissen Xiaomi-Tests aus `docs/project/TESTPLAN_v0.5.04.md` durchführen.

---

# Projektstand v0.5.04 – 21.09.2026

**Tatsächlicher Quellstand:** LFN-Scout `0.5.04`, `versionCode 145`, `applicationId de.brummbeobachter.app`. Basierend auf dem vorliegenden v0.5.03-Quellprojekt. Dies ist ein **ungetesteter Android-Entwicklungskandidat**, keine auf dem Xiaomi freigegebene APK. Der Nutzer hat v0.5.02 vollständig abgenommen; v0.5.03 testete er weiter und meldete eine offene 16:1-Pegelabweichung.

| Funktion | Implementiert | Testnachweis |
|---|---|---|
| Temporärer 8-kHz-Rolling-Analyzer und exklusiver 48-kHz-Direkt-Analyzer | Quellcode | Static contract; Android-Start/STOP **NOT RUN** |
| STOP ohne Rolling-Restore; Restore erst STOP **und** Panel verlassen | Quellcode (`AnalyzerModeCoordinator`) | Static contract; Android-Zustandsmatrix **NOT RUN** |
| Rolling-Historie darf beim 48k-Übergang verworfen werden; vorheriges Rolling neu starten | Quellcode | Gerät **NOT RUN** |
| Eigenes 48-kHz-Profil/Setup 4, 0–22000 Hz, FFT8192/16384 | Quellcode | JVM-FFT-Test, Setup-Android **NOT RUN** |
| Digitales Testsignal 50/195 Hz, −100…−1 dBFS | Quellcode (nur Analyzer-Hub) | JVM-Core-Sinus-Test, App-GUI **NOT RUN** |
| 10–14400 s Wasserfall, max. 2400 Zeilen; Moduswechsel erhält Historie | Quellcode | JVM-Timingtest + Static contract; Gerät **NOT RUN** |
| Langer Kameradruck: Erfolg/Fehler nach Dateischreiben anzeigen | Quellcode | Static contract; PNG/JPG Gerät **NOT RUN** |
| 16:1-Pegel-/Energieabweichung | **NICHT BEHOBEN** | Nutzer meldete Fehler v0.5.03; Ursache offen |
| Automatische fortlaufende WAV-Dateiaufzeichnung in 8k JA-Modus | **NICHT IMPLEMENTIERT** | Der vorhandene Rolling-Dienst speichert RAM-Ring, nicht laufend WAV auf Disk |

**Lokale Prüfung:** Host-Analyzer `tools/RUN_ANALYZER_V0504_SELFTEST.sh`: 16/16 PASS; v0.5.03: 20/20 PASS; v0.5.02 (für erweitertes Historienlimit aktualisierter Vertrag): 16/16 PASS; alte Wasserfall-/Timing-Hosttests PASS. `tools/verify_v0504_source.py`: 20/20 statische Quellverträge PASS. **Kein Android-SDK/keine vollständige Gradle- bzw. Compose-Kompilierung und kein Xiaomi-Test in dieser Umgebung.** Ein eventueller lokaler `kotlinc`-Aufruf ohne Android-Classpath darf nicht als Android-Build zählen.

**Verbindlicher Einstieg:** `DIRECTIVES.md` → `BUILD_WORKFLOWS.md` → `CHANGELOG.md` → `docs/project/ANALYZER_v0.5.04.md` → `docs/project/TESTPLAN_v0.5.04.md` → tatsächliche Kotlin-Dateien. Für Build und Gerätetest siehe `BUILD_VERIFICATION_v0.5.04.md`.

**Zurückgestellt:** Mithören, Superfast, Spektrum-Max-Hold, Radiushistogramme, vollständiger Supabase-Restore auf neuem Projekt, vollständige WAV-Daueraufnahme im neuen Analyzer-JA-Modus. Das Projekt-ZIP enthält historisch mitgeführte private Supabase-Recovery-Daten; nicht öffentlich hochladen.

---

# Projektstand v0.5.03 – 20.09.2026

**Aktuelle Quelle:** v0.5.03 / `versionCode 144` / Paket-ID unverändert `de.brummbeobachter.app`. **Neuer Android-Build und Xiaomi-Gerätetest: NOT RUN**. Die v0.5.02-Gesamtgeräteabnahme wurde vom Nutzer als erfolgreich gemeldet. Das ist ein Nutzerbericht und kein CI-Log. Ältere v0.5.02-Statusabschnitte unten sind historisch.

| Neuer Bereich | Implementiert | Nachweis |
|---|---|---|
| Analyzer-Decimation 4:1 / 8:1 / 16:1 | Ja, FIR 129/129/513 Taps mit Cutoff 280/280/215 Hz; 8-kHz-Rohhub und WAV bleiben getrennt | JVM-DSP-FIR-Test; Gerät ausstehend |
| Dynamischer LF-Takt / FFT-Hop / Wasserfall | Ja, 2000 / 1000 / 500 Hz; Frequenzauflösung und Intervall aktualisiert | JVM-Timing-Test; UI-/Belastungstest ausstehend |
| Cursor-Readout | Ja, gleiche Höhe links/rechts | Nur Quellprüfung; Gerät ausstehend |
| Cursor bis Wasserfallende | Ja, Spektrum + Wasserfall, auch Offscreen-Export | Nur Quellprüfung; PNG/JPG-Gerät ausstehend |
| Namen/Beschreibungen für drei Slots | Ja, persistente Name-/Beschreibung-Keys; alte namenlose Slots ladbar | Nur Quellprüfung; Gerät ausstehend |
| Statistikband Min/Max | Ja, frei 0–200 Hz, Min < Max; Anzeigen und Spektrummarkierung synchron | Nur Quellprüfung; Gerät ausstehend |
| Statistikfenster / Average Time | Ja, 10–600 s und 0,1–60 s; separate gleitende Band-Leistungsmittelung; 5-s-Default; L50/L90 erst nach vollem Fenster | Nur Quellprüfung; reale Pegelvalidierung ausstehend |

**Abgrenzung:** Die neue Statistik-Average-Time ist das gleitende Leistungsmittel der Bandstatistik, **nicht** die rote FFT-Spektrummittelung; der 5-s-Default verändert gegenüber v0.5.02 die Berechnungsdefinition der L50/L90. Unter hohem FFT-Durchsatz ist die reale CPU-Last ungemessen; Perzentilsortierung höchstens einmal pro Sekunde. Die vorhandene 8:1-FIR ist unverändert parametriert. Die 16:1-FIR-Unterdrückung wurde an synthetischen Tönen getestet, nicht im realen Gerät kalibriert.

**Verbindlicher Einstieg:** `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/ANALYZER_v0.5.03.md` und `docs/project/TESTPLAN_v0.5.03.md`. Nicht implementiert bleiben u. a. Radiushistogramme, Mithören, Superfast, Spektrum-Max-Hold und der externe Supabase-Restore auf ein neues leeres Projekt.

---

# PROJECT_STATUS.md – LFN-Scout

**20.09.2026 · Aktueller Quellstand v0.5.02 / versionCode 143 · Entwicklungs-/Testkandidat, keine Android-/Xiaomi-Freigabe für v0.5.02.**

## 1. Nachweise getrennt nach Version

| Stand / Prüfung | Ergebnis | Nachweis und Abgrenzung |
|---|---|---|
| v0.5.00 Xiaomi | Nutzer-Praxistest PASS | Kein großer weißer Streifen; Bild wird gespeichert; Analyzer läuft bei Panelwechsel weiter. |
| v0.5.01 Xiaomi | Nutzer-Praxistest: Kamera, PNG/JPG, Setups, dBFS, Navigation und WAV/Rolling/Bewertung/Seismik PASS | Nutzer meldete fünf Testgruppen erfolgreich (Installation über v0.5.00 ohne Datenlöschen, zwei Autozyklen). **Korrektur:** Hz sitzt unmittelbar über einer Frequenzzahl: Achsen-Prüffall FAIL. Keine separat archivierte CI-Buildlogdatei. |
| v0.5.02 Core-Compile | HOST PASS | Vollständige `analyzer-core/src/main/kotlin` mit `kotlinc` kompiliert. |
| v0.5.02 neuer Core-Test | HOST PASS 15/15 | `tools/RUN_ANALYZER_V0502_SELFTEST.sh`: FFT 5, Hz-Mitten, freie Frequenzbereiche, Wasserfall-Ressourcenlimit, FFT-Größen, Window-Korrektur und Smoothing AUS. |
| Erhaltener Wasserfall-/Zeitbasis-Hosttest | HOST PASS | `tools/RUN_ANALYZER_WATERFALL498_SELFTEST.sh`, `tools/RUN_ANALYZER_TIMING495_SELFTEST.sh`. Betrifft isolierten Core, nicht App-UI. |
| v0.5.02 Android-/Compose-Build, `:app:testDebugUnitTest`, `assembleDebug` | **NOT RUN** | Kein Android SDK verfügbar. Es existiert keine gebaute v0.5.02-APK in diesem Paket. |
| v0.5.02 Xiaomi-End-to-End inklusive PNG/JPG/Audio | **NOT RUN** | Freigabe nach Build und `docs/project/TESTPLAN_v0.5.02.md`. |
| Supabase-Restore auf neuem leeren Projekt | **NOT RUN** | Unabhängig vom Analyzer weiter zurückgestellt. |

## 2. Tatsächlich implementiert v0.5.02 (gegenüber v0.5.01)

- Gemeinsame Bildschirm-/Export-Position von `Hz`: bei jedem Frequenzbereich zwischen dem **zweiten und dritten Frequenz-Tick**, nicht über einer Zahl; keine zusätzliche Zeile.
- Wasserfall-Fußzeile zeigt **Akt / MW / MAX**, auch im Offscreen-Bildexport.
- Die beiden `± Minimum`/`± Maximum`-Buttons entfallen; Textfelder bleiben direkt editierbar (Minus über Text-Tastatur).
- Wasserfall-Zeitdauer als **freie ganzzahlige Sekundeneingabe** (10–600 s, höchstens 2400 Zeilen), Default 120; Zeitachse, Zeilenkapazität, Auto-Bildzyklus und Setup-Persistenz werden aktualisiert. L50/L90-Statistikfenster bleibt unverändert bei 120 s.
- Schmale linke Wasserfall-Farbleiste: tatsächliche längste Tickzahl bestimmt die Breite; gemeinsame Plotränder im Live-UI. Für eine spätere rechte Histogrammspalte weiterhin **keine implementierten Histogramme**.
- FFT-Hop 5; FFT Size 2048/4096/8192; freie ganzzahlige Frequenz-Min-/Max-Werte innerhalb 0–200 Hz (Mindestspanne 5 Hz); Window Hann/Hamming/Blackman/Rechteck; Desired Transformation Interval ms mit FFT-Hop gekoppelt; exponentielles Smoothing EIN/AUS und freie Zeitkonstante/Faktor (α aus tatsächlich eingestelltem Hop).
- Präferenzen und Slots 1–3 um diese Werte erweitert; bisherige Defaults für Altinstallationen bleiben 0–100 Hz, FFT 4096/Hann, Hop 100, Zeit 120 s, τ 5 s.
- Nur Analyzer-Subscriber wird bei FFT-/Window-/Hop-/Smoothingwechsel neu initialisiert. Gemeinsamer 8-kHz-Mikrofon-/WAV-/Rolling-Pfad, Bewertung, Seismik und Backend sind unverändert. Erster neuer FFT-Warmup benötigt je nach FFT-Größe etwa 2,048/4,096/8,192 s.

## 3. Bekannte Grenzen / Abnahme

- Hohe CPU-Last bei FFT 8192 + Hop 5 möglich: **nicht** auf Xiaomi vermessen; kein garantierter FFT-Durchsatz. Nutzergerät/Recorder unter Last prüfen.
- 10–600 s ist kein garantiert bei jedem Zeilenintervall erreichbarer Bereich: 2400-Zeilen-Schutz verhindert z. B. 600 s bei 100 ms. Gültige freie Werte werden nicht auf feste Presets gerundet.
- Speicherung der Zeitdauer startet eine neue Wasserfallhistorie/Auto-Bild-Zeitbasis, aber keine neue Mikrofonaufnahme. FFT-Konfigurationswechsel startet Analyzer-FFT neu; kurzer Warmup, nicht als nahtloser FFT-Betrieb ausgeben.
- Frequenzgrenzen betreffen **Darstellung**, nicht FIR-Ausgang oder L50/L90-Band 2–25 Hz. Decimation bleibt **fest 8:1** und für v0.5.03 geplant.
- Spektrum-Max-Hold, Mithören, Superfast, Radiushistogramme, SPL-Kalibrierung und benannter Setup-Import weiterhin nicht implementiert.

## 4. Verbindliche Übergabe

`DIRECTIVES.md` → `BUILD_WORKFLOWS.md` → `CHANGELOG.md` → `docs/project/ANALYZER_v0.5.02.md` → `docs/project/TESTPLAN_v0.5.02.md` → aktuelle tatsächliche Kotlin-Dateien. Historische `docs/project/UEBERGABE_v0.5.00_2026-09-20.md` bleibt Referenz der Ausgangsbasis.

**Sicherheit:** Vollständiges Liefer-ZIP enthält historisch mitgeführte private Supabase-Recovery-Daten und WAV-Backups. Nicht öffentlich in GitHub hochladen; `.git/` darf nicht im ZIP stehen.

## v0.5.27 Zusatzstand 10.10.2026 – nicht final

Neue persistente Maximalzeit des Rolling-BETRIEBS (12 h Default, GUI 1–168 h) im Quellcode. Android-Gradle-Build und Gerätetest weiterhin NICHT nachgewiesen. Statistiksammlung aus Rolling-PCM, lokale CSV, SD-Karte/SAF und automatischer Hintergrund-Retry weiterhin offen. Verbindliche Anforderungen unter `docs/project/ROLLING_AND_STATISTICS_REQUIREMENTS_v0.5.27.md`.


## Phase 3O – dritte Teilstufe: Legacy-Grenzwertschutz (14.10.2026)

`AnalyzerRuntimeSetupSnapshot.capture` darf Filtergrenzen nicht mehr stillschweigend von `Double` nach `Int` abschneiden. `exactLegacyFilterHz` akzeptiert nur endliche, exakt ganzzahlige Werte im Int-Bereich; bei echten Bruchteilen bricht die *lesende* Erfassung mit einer klaren Fehlermeldung ab. Grund: Das aktuelle Legacy-JSON-Schema deklariert `filter_min_hz` und `filter_max_hz` als Integer. Dieser Guard dient der Integrität, **ist keine Unterstützung für Bruch-Hz**. Für eine vollwertige Übernahme muss die Schema-/Controller-Frage explizit geklärt werden. `AnalyzerSetupFormat` prüft Integerwerte jetzt über `BigDecimal.toBigIntegerExact()` statt verlustbehaftetem `Double`. Vier JUnit-Regressionsfälle hinzugefügt, deren Ausführung durch Gradle aussteht. Keine Änderung an Recordern, Audiopipeline, SharedPreferences, JSON-Dateien, Workflows oder Gradle-Version. `Plan.executable=false`, kein Adapter aktiv.

## 14.10.2026 – Phase 3O Teilstufe 7
Nur lesender STOP-Audio-/Timing-Checkpoint im Controller und CURRENT_STATE.md ergänzt. Letzter vom Nutzer als grün bestätigter GitHub-Test: 3O Teilstufe 5; 3O/6 und 3O/7 Buildstatus offen. JSON-Audio-Executor weiterhin gesperrt; kein Recorder-Rollback. Interne Gradle-Version unverändert.

## Phase 3O, Teilstufe 9 – Stand 14.10.2026
- Phase 3O/8 GitHub-TEST vom Nutzer als GRÜN gemeldet (Commit nicht übermittelt).
- Neue best-effort-Bereinigung bei lokalem Analyzer-Sessionstartfehler in `LiveAnalyzerController.start`; vollständiger Audio-/Recorder-Rollback und UI-Setupwechsel weiterhin gesperrt.
- Phase 3O/9 nur statisch geprüft, GitHub TEST und Gerätetest offen. Gradle 0.5.55/213.

## Phase 3O/13
MON-WAV-Start gegen zukünftige Setup-Reservierung koordiniert (noch nicht produktiv aktiviert). GitHub TEST und Gerätetest für 3O/13 offen; Phase 3O/12 vom Nutzer als grün gemeldet.

### Phase 3O/15 (Quellkandidat)
Rolling/Direct: erfolgreiche asynchrone Startanforderungen werden bis `onDestroy()` konservativ verfolgt. Keine Audio-Setup-Aktivierung, JUnit/Android TEST noch offen.
