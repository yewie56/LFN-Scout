# LFN-Scout v0.5.56 – Phase 3D: MON-Übernahme (isoliert)

## Umfang
`AnalyzerMonitorSetupMigration.kt` ist eine neue, **nicht in die UI eingebundene** Klasse. Sie liest die bestehenden MON-SharedPreferences `lfn_analyzer_monitor_049` (14 Float-Felder, 2 Boolean-Felder). Nur tatsächlich vorhandene Werte werden in `parameters.monitor` übertragen. Es gibt weder Default-Injektion noch Wertebegrenzung (`coerceIn`). Fehlende und unbekannte Einstellungen werden nicht als gültige neue MON-Werte erfunden.

`preview(setup)` liefert ein vorgeschlagenes JSON-Dokument im Speicher, ohne es zu sichern oder zu aktivieren.

`backupAndSave(setupId, repository)` ist nur für eine **später ausdrücklich ausgelöste** Migration vorgesehen. Vor dem Schreiben ins v3-Setup wird die gesamte ursprüngliche MON-Preference-Map mit Zeitstempel und Ziel-ID unter `filesDir/analyzer_setups/backups/monitor/` als einmalige JSON-Sicherung abgelegt und anschließend gelesen/geprüft. Der alte Preferences-Speicher wird nicht verändert. Bereits vorhandene `parameters.monitor` werden nicht überschrieben.

## Abgrenzung
Keine UI, keine automatische Ausführung, kein Laden/Aktivieren des MON-DSP zur Laufzeit. Vor der Aktivierung müssen die Nutzerentscheidung, die Zuordnung MON-Daten ↔ Analyzer-Setup (globale bisherige MON-Werte) und die Behandlung eines unvollständigen Bestands geklärt bzw. implementiert werden. Die ursprüngliche `AnalyzerMonitor.start(context)`-Logik lädt weiterhin die alten Preferences und würde einen neuen Setup-Entwurf zur Laufzeit überschreiben. Die künftige Integration muss dies explizit beheben.

Statische Prüfungen unter `tools/verify_lfn056_phase3d.py`. Kein Android-Gradle-Build und kein Gerätetest in dieser Phase. `app/build.gradle.kts` bleibt auf 0.5.55 / versionCode 213. Termux v4 unberührt.
