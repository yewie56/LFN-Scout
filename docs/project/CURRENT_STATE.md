# LFN-Scout CURRENT_STATE – Phase 3O/15

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `versionName 0.5.55`, `versionCode 213` unverändert.
**Letzter bestätigter GitHub TEST:** Phase 3O/14-FIX1 **GRÜN (Nutzerbestätigung)**. Keine Commit-ID neu verifiziert.
**Neuer Quellkandidat:** Phase 3O/15; GitHub TEST und Android-Gerätetest OFFEN.

## Implementiert in 3O/15

`AnalyzerSetupTransitionCoordinator` protokolliert erfolgreiche Rolling- und Direct-Startanforderungen vor dem asynchronen Android-Service-Start unter einem `inFlightServices`-Marker. Die Setup-Reservierung wird verweigert, solange mindestens ein Marker besteht. `Service.onDestroy()` räumt den jeweiligen Marker ab. Fehlgeschlagene Startanforderungen und Exceptions entfernen ihn ebenfalls. Entsprechende statische Checks und drei JUnit-Tests ergänzt. Ältere statische Guards akzeptieren jetzt die erweiterte `withTrackedServiceStart`-Schnittstelle.

## Grenzen

- Noch **kein** vollumfänglicher atomarer Recorder-/Service-/AudioRecord-Übergang. Ein Destroy-Callback garantiert nicht allein die Beendigung sämtlicher Coroutine-/AudioRecord-Cleanups; der Mikrofon-Arbiter und die übrigen Audio-Owner-Prüfungen bleiben notwendig.
- Bei erfolgreich akzeptiertem START ohne späteres Service-`onDestroy()` bleibt die Setup-Sperre vorsichtshalber gesetzt. Keine Zeitüberschreitung mit unsicherer Freigabe.
- `Plan.executable=false`, keine produktive Audio-/FFT-Umschaltung und kein vollständiger Hardware-Rollback.
- Neue JUnit-Tests enthalten, noch nicht via GitHub ausgeführt. Statische Regressionen sind keine Android-Kompilierung.

## Unverändert

Termux v4, Workflows/Signatur, Cloud, Gradle-Version. `supabase/storage_backup_before_v0526/` bleibt historisch auf GitHub `main` und ist bewusst nicht in den Quell-ZIPs; fehlende Dateien sind keine Löschanweisung. `docs/project/CURRENT_STATE.md` bei jedem Quell-ZIP aktualisieren.

## Nächster Schritt

Service-/Capture-Cleanup atomar quittieren, mögliche stale `inFlight`-Marker kontrolliert behandeln und danach erst einen vollständigen STOP-Reconfigure-START-Executor mit Rollback vorbereiten. Ausschließlich GitHub-TEST nötig, REGULAR nicht obligatorisch.
