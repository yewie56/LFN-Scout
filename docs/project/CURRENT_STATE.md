# LFN-Scout CURRENT_STATE – Phase 3O/14

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `versionName 0.5.55`, `versionCode 213` unverändert.
**Letzter bestätigter GitHub TEST:** Phase 3O/13 **GRÜN (Nutzerbestätigung)**. Keine zugehörige Commit-ID neu verifiziert.
**Neuer Quellkandidat:** Phase 3O/14; GitHub TEST und Android-Gerätetest **OFFEN**.

## Implementiert

- Phase 3O/13: MON-WAV-Startschutz über `AnalyzerSetupTransitionCoordinator`.
- Phase 3O/14: Rolling-/Direct-START-Anforderungen verwenden `withServiceStart` und werden bei aktiver Setup-Reservierung abgelehnt. Zwei zusätzliche JUnit-Tests.
- Alle bisherigen Quellen/Regressionen aus 3A–3O/13 enthalten.

## Kritische Grenze

`startForegroundService()` startet asynchron. Ein vor Beginn einer Setup-Reservierung angenommener START-Request ist damit **nicht** atomar bis zur tatsächlichen Mikrofonaufnahme abgesichert. Noch keine vollständige Koordination aller Audio-Session-/Recorder-Übergänge; kein produktiver Transaktions-Executor, kein vollständiger Audio-/Recorder-Rollback. `Plan.executable=false`. Diese Änderungen alleine erlauben **keine** Audio-/FFT-Setup-Aktivierung.

## Unverändert

Termux v4, Workflows/Signatur, Cloud-Schema, Gradle-Version und historische Backup-Daten bleiben unangetastet. `supabase/storage_backup_before_v0526` verbleibt in GitHub `main` und wird aus der Quell-ZIP ausgeschlossen; dessen Fehlen ist keine Löschanweisung. Für Quellkandidaten nur GitHub-TEST notwendig, REGULAR bei Bedarf.

## Nächster Schritt

Asynchrone Service-START-Zustände und Session-Start unter einer tatsächlich durchgehenden Reservierung koordinieren, inklusive Race-/Fehlertests. Erst danach speicherfreie STOP/START-Übernahme und vollständigen Rollback freigeben.

## Fortsetzung

Vor Änderungen `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md` und Phase-3-Dokumentation beachten. Immer CURRENT_STATE aktualisieren und vollständige Quell-ZIP ohne Wurzel-Unterordner liefern.
