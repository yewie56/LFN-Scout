# LFN-Scout CURRENT_STATE – Phase 3O/12

**Stand:** 14.10.2026. Entwicklungsziel `0.5.56`; Gradle bleibt `0.5.55`, versionCode `213`.
**Letzter bestätigter GitHub TEST:** Phase 3O/11 **GRÜN (Nutzermeldung)**. Commit-ID nicht erneut überprüft.
**Neu erstellter Quellkandidat:** Phase 3O/12; GitHub TEST und Android-Gerätetest **offen**.

## Neuer Umfang
`AnalyzerSetupMicrophoneReservation.withReservation` kann den bestehenden MicCaptureArbiter für die Dauer eines synchronen Blocks exklusiv reservieren. Der Token wird auch bei Ausnahmen freigegeben; bei bestehendem Owner wird der Block gar nicht ausgeführt. Drei neue JVM-JUnit-Tests decken Konkurrenz, Belegtheit und Ausnahme ab. **Die Reservierung ist noch nicht an den Setup-Executor angeschlossen.**

## Unverändert / Sicherheitsgrenzen
- Phase 3N `Plan.executable=false` – kein produktiver STOP/START-Setupwechsel.
- Keine Synchronisierung mit MON-Aufnahmestarts oder sämtlichen Serviceübergängen; noch kein sicherer Gesamt-Rollback.
- Termux v4 unverändert; Workflows, Signatur, Cloud, Recorder und `.gitignore` unverändert.
- Historisches `supabase/storage_backup_before_v0526` bleibt auf GitHub `main`, **nicht** im Quell-ZIP. Fehlen darf **nicht** als Löschanweisung gelten.
- Quellkandidat enthält volle Quellen und Regressionstests. Statische Tests != GitHub-/Gerätetest.

## Nächster Schritt
Gemeinsame atomare Aufnahme-/Audio-Service-Übergänge entwerfen und integrieren, einschließlich MON und Startup-Races; danach überprüfter speicherfreier STOP/START, Rollback und UI-Anbindung.

## Verbindliche Fortsetzung
Vor Änderungen `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md` und Phase-3-Dokumente lesen; `CURRENT_STATE.md` bei jeder Lieferung aktualisieren. Nur GitHub TEST zunächst nötig; REGULAR bei Bedarf.
