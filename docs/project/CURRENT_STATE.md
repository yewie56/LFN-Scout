# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 1 (nach Teilstufe 20)

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Phase 3O/20 **GRÜN**.
**Neuer Quellkandidat:** Phase 3O, Arbeitspaket 1. GitHub-TEST / Android-Gerätetest **OFFEN**.

## Inhalt dieses Arbeitspakets

- `AnalyzerSetupTransitionCoordinator.diagnoseSafety()` führt die bisherigen
  internen Sperrgründe mit der bestehenden Audio-Eigentümerabfrage zusammen.
  Probe-Ausfälle ergeben `audio_owner_probe_failed` statt einer scheinbaren Freigabe.
- `AnalyzerSetupSwitchTransaction.execute()` verweigert den bisherigen
  *nicht ausführbaren* Pfad zusätzlich bei internen Service-/Cleanup-Sperren;
  Fehler der Port-Statusabfrage werden nun ebenfalls kontrolliert abgewiesen.
- Drei dynamische JUnit-Testfälle hinzugefügt und zwei statische Verifikatoren
  angepasst bzw. ergänzt. Kein neuer Recorder-Lifecycle und keine zusätzlichen
  JSON-/Preference-Schreibzugriffe.
- `SafetyDiagnosis.executionAuthorized=false` und `Plan.executable=false` bleiben
  bindend. Eine leere Sperrliste ist KEIN Freigabe-Nachweis; zwischen den
  Beobachtungen kann ein Android-Service seinen Zustand ändern (TOCTOU).

## Restplan – vier größere Pakete nach diesem Paket

1. **Gemeinsames Start-/Stop-Protokoll:** alle Aufnahmebesitzer und
   asynchronen Capture-Finalisierungen auf echte Reservierungsgrenzen prüfen;
   keine unbekannten Zustände als frei behandeln.
2. **Transaktionskern:** atomare STOP–Reconfigure–START-State-Machine über den
   bestehenden Controller, ausschließlich bei nachweislich exklusivem Besitz.
3. **Rollback und Fehler-Injektion:** Checkpoints, vollständiges Recovery,
   sichere Endzustände, fault-injection- und Race-Regressionen.
4. **UI-Anbindung und Stabilisierung:** Setup-Auswahl/Vorschau/Save-Dialoge
   integrieren, Android-Gerätetests durchführen, Freischaltung erst nach Abnahme.

## Prüfstatus

Quellbasierte Phase-3O-Regressionen: 17/20 Skripte innerhalb der lokalen 5-s-Grenze erfolgreich; drei Skripte (`verify_lfn056_phase3o.py`, `verify_lfn056_phase3o_14_fix_return.py`, `verify_lfn056_phase3o_async_start_tracking.py`) überschritten die Grenze und bleiben **nicht abschließend geprüft**. Die beiden unmittelbar betroffenen Sicherheits-Verifikatoren (`owner_gate`, `safety_diagnosis`) sind erfolgreich. Java/Kotlin-JUnit, Android-Build und Hardware-Gerätetest wurden lokal **nicht** ausgeführt; GitHub-TEST ausstehend.

## Historischer Stand

# LFN-Scout CURRENT_STATE – Phase 3O/20

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `versionName 0.5.55`, `versionCode 213` unverändert.
**Letzter bestätigter GitHub TEST:** Phase 3O/19 **GRÜN (Nutzerbestätigung)**. Keine Commit-ID neu verifiziert.
**Neuer Quellkandidat:** Phase 3O/20; GitHub TEST und Android-Gerätetest OFFEN.

## Implementiert in 3O/20

`AnalyzerSetupTransitionCoordinator.assessGate()` stellt unter einem gemeinsamen Lock eine **nur diagnostische** Entscheidung mit stabilen Sperrgründen bereit (`setup_reserved`, `service_in_flight:<service>`, `capture_cleanup_pending:<service>`, optional verwaiste Destroy-Marker). `coordinatorMarkersClear=true` bedeutet **nur**, dass die internen Marker leer sind; es ist **keine Ausführungsfreigabe** und prüft weder externe Recorder noch Hardware/Mikrofon-Arbiter. Keine automatische Freigabe, kein UI-Aufruf, kein Executor; `Plan.executable=false`. Zwei JUnit-Tests und statische Regression ergänzt. **GitHub TEST 3O/20 offen.**

## Implementiert in 3O/19

Der `AnalyzerSetupTransitionCoordinator` stellt einen synchronisierten, **rein lesenden** `GateSnapshot` mit Setup-Reservierung, laufenden Service-Starts, Destroy-Markern und ausstehenden Capture-Cleanups bereit. Die Sets werden kopiert; Diagnostik kann damit keine Koordinatorzustände verändern oder ausstehende Marker löschen. Zwei JUnit-Regressionsfälle sowie `tools/verify_lfn056_phase3o_gate_snapshot.py` hinzugefügt. Dies ist noch keine UI-Anzeige, kein automatischer Timeout und keine Freigabe einer Transaktion. GitHub TEST von 3O/19 offen.

## Implementiert in 3O/18

Doppelte bzw. verspätete `serviceDestroyed()`-Callbacks erzeugen keine veralteten `destroyedServices`-Marker mehr, wenn weder Start noch Capture-Job ausstehen. Doppelte `captureJobFinished()`-Callbacks dürfen keine andere Sitzung abschließen. Zwei neue JUnit-Tests und ein zusätzlicher statischer Regressionstest. Der ältere MON-Quelltest wurde auf die strengere `captureJobsPending`-Abfrage aktualisiert. GitHub-TEST von 3O/18 offen. **Kein ausführbarer Setupwechsel.**

## Implementiert in 3O/17

`withTrackedServiceStart` verweigert jetzt einen zweiten START für denselben Service, solange dessen vorheriger `inFlightServices`-Marker oder ein Capture-Cleanup besteht. Damit kann eine doppelt angenommene asynchrone Anfrage nicht denselben Set-Eintrag teilen und anschließend verfrüht freigeben. Drei JUnit-Regressionen für doppelte Anforderung, ausstehenden Cleanup und ordnungsgemäßen Neustart nach Freigabe ergänzt. Diese konservative Sperre ändert das Verhalten bei wiederholten START-Anfragen; die Android-Integration muss im TEST und am Gerät überprüft werden. **Kein ausführbarer Setupwechsel.**

## Vorheriger Stand 3O/16

Die asynchronen Rolling-/Direct-Capture-Jobs werden vor dem Launch als ausstehend vorgemerkt. `onDestroy()` kennzeichnet nur das Ende des Service-Lifecycles; erst das zugehörige `Job.invokeOnCompletion` bestätigt den Abschluss des Coroutine-`finally` und damit den Capture-Cleanup. Die Startvormerkung wird nur entfernt, wenn beide Bedingungen erfüllt sind. Ein Setup-Reservierungsversuch scheitert auch bei noch ausstehendem Capture-Job. Drei JUnit-Regressionen und ein neuer Quelltest ergänzt. **Kein ausführbarer Setupwechsel.**

## Vorheriger Stand 3O/15

`AnalyzerSetupTransitionCoordinator` protokolliert erfolgreiche Rolling- und Direct-Startanforderungen vor dem asynchronen Android-Service-Start unter einem `inFlightServices`-Marker. Die Setup-Reservierung wird verweigert, solange mindestens ein Marker besteht. `Service.onDestroy()` kennzeichnet den Service als zerstört; der Marker wird seit 3O/16 erst nach Coroutine-Jobende entfernt. Fehlgeschlagene Startanforderungen und Exceptions entfernen ihn ebenfalls. Entsprechende statische Checks und drei JUnit-Tests ergänzt. Ältere statische Guards akzeptieren jetzt die erweiterte `withTrackedServiceStart`-Schnittstelle.

## Grenzen

- Noch **kein** vollumfänglicher atomarer Recorder-/Service-/AudioRecord-Übergang. Der Jobabschluss erfasst den synchronen Cleanup im Capture-`finally`, nicht zwingend alle fremden asynchronen Folgearbeiten; der Mikrofon-Arbiter und die übrigen Audio-Owner-Prüfungen bleiben notwendig.
- Bei erfolgreich akzeptiertem START ohne späteres Service-`onDestroy()` bleibt die Setup-Sperre vorsichtshalber gesetzt. Keine Zeitüberschreitung mit unsicherer Freigabe.
- `Plan.executable=false`, keine produktive Audio-/FFT-Umschaltung und kein vollständiger Hardware-Rollback.
- Neue JUnit-Tests enthalten, noch nicht via GitHub ausgeführt. Statische Regressionen sind keine Android-Kompilierung.

## Unverändert

Termux v4, Workflows/Signatur, Cloud, Gradle-Version. `supabase/storage_backup_before_v0526/` bleibt historisch auf GitHub `main` und ist bewusst nicht in den Quell-ZIPs; fehlende Dateien sind keine Löschanweisung. `docs/project/CURRENT_STATE.md` bei jedem Quell-ZIP aktualisieren.

## Nächster Schritt

Weitere Audio-/Recorder-Ressourcen inklusive Fremd-Coroutinen und stale `inFlight`-Marker absichern und danach erst einen vollständigen STOP-Reconfigure-START-Executor mit Rollback vorbereiten. Ausschließlich GitHub-TEST nötig, REGULAR nicht obligatorisch.

## Prüfhistorie 3O/16

Phase-3O-Quelltests PASS. Ältere Phase-3F-/3J-Quelltests schlagen wegen später hinzugefügter Setup-UI-/Session-Funktionen fehl (3F 3 von 12 Checks, 3J 2 von 18); diese Tests wurden nicht als PASS ausgegeben. GitHub TEST von 3O/16 GRÜN (Nutzerbestätigung); GitHub TEST von 3O/17 und 3O/18 GRÜN (Nutzerbestätigung).

Historie: Phase 3O/14 führte gemeinsame Rolling-/Direct-Startschranken ein; Phase 3O/14-FIX1 korrigierte den Kotlin-Return-Fehler.
