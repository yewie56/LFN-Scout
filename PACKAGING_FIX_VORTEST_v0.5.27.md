# LFN-Scout v0.5.27 – Korrektur Windows-Vortest ACTIVE-Paket

Stand: 2026-10-10. Basis: ROLLING_LIMIT_TEST_CANDIDATE, weiterhin versionCode 174.

## Befund (Windows-Vortest 19:27)

- PREPARE: PASS
- UNIT TESTS: PASS
- ASSEMBLE DEBUG: PASS
- APK CHECK: PASS; 14.971.237 Byte
- ACTIVE PACKAGE: FAIL beim Kopieren aus `supabase/storage_backup_before_v0526/...` in langen Staging-Pfad
- ADB-Installation: nicht ausgeführt (Folgefehler)

## Korrektur

`tools/package-active.ps1`: Ausschlusspräfix `supabase/storage_backup/` durch `supabase/storage_backup` ersetzen. Damit werden auch zeitgestempelte Recovery-Verzeichnisse wie `storage_backup_before_v0526` im *ACTIVE*-ZIP ausgeschlossen. Die Quelldaten werden weder gelöscht noch verändert; sie gehören nach der bestehenden BASE-/Recovery-Trennung nicht ins aktive Quellarchiv.

## Test unter Windows 11

1. Dieses ZIP in einen **neuen**, kurzen Ordner entpacken, z.B. `C:\LFN527`.
2. `VORTEST_INSTALL.bat` starten (mit USB-Debugging und verbundenem Handy), oder zuerst `VORTEST.bat`, wenn noch keine Installation gewünscht ist.
3. Prüfen: PREPARE, UNIT TESTS, ASSEMBLE DEBUG, APK CHECK und ACTIVE PACKAGE alle PASS.
4. Bei Installationswunsch zusätzlich ADB DEVICE und INSTALL PASS prüfen.
5. Falls weiterhin ein Dateipfadfehler erscheint, neues Log übermitteln.

## Grenzen

Die Korrektur behebt nur das Packaging. Statistik-/SD-Neufunktionen bleiben, soweit zuvor dokumentiert, offen. Ein PASS des korrigierten Skripts unter Windows ist noch nicht nachgewiesen.
