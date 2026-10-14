# LFN-Scout CURRENT_STATE – Phase 3O/16

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `versionName 0.5.55`, `versionCode 213` unverändert.
**Letzter bestätigter GitHub TEST:** Phase 3O/15 **GRÜN (Nutzerbestätigung)**. Keine Commit-ID neu verifiziert.
**Neuer Quellkandidat:** Phase 3O/16; GitHub TEST und Android-Gerätetest OFFEN.

## Implementiert in 3O/16

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

Phase-3O-Quelltests PASS. Ältere Phase-3F-/3J-Quelltests schlagen wegen später hinzugefügter Setup-UI-/Session-Funktionen fehl (3F 3 von 12 Checks, 3J 2 von 18); diese Tests wurden nicht als PASS ausgegeben. GitHub TEST von 3O/16 noch offen.

Historie: Phase 3O/14 führte gemeinsame Rolling-/Direct-Startschranken ein; Phase 3O/14-FIX1 korrigierte den Kotlin-Return-Fehler.
