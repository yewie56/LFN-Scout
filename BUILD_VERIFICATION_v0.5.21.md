# BUILD_VERIFICATION v0.5.21

## Anlass
Realer Windows-Lauf von v0.5.20: PREPARE PASS, Unit-Tests PASS, `assembleDebug` PASS, APK PASS. `tools/package-active.ps1` erzeugte `LFN-Scout_ACTIVE_v0.5.20.zip`, der nachgelagerte Check suchte wegen fest verdrahtetem `$version = '0.5.19'` jedoch nach v0.5.19 und brach mit Fehlercode 51 ab.

## Korrektur
- `tools/run-vortest.ps1` liest `versionName` aus `app/build.gradle.kts`.
- `VORTEST.bat` liest dieselbe Version für Kopf- und Ergebnisanzeige.
- `PREPARE_PROJECT.bat` verwendet dieselbe Version im Kopf.
- `tools/package-active.ps1` hatte die dynamische Versionsableitung bereits korrekt und bleibt unverändert.

## Erwarteter Windows-Ablauf
PREPARE → Unit-Tests → assembleDebug → APK → ACTIVE-Paket → ACTIVE-Paketprüfung → ADB-Warteschleife → `adb install -r`.

## Status
Statische Versionskopplung und Paketintegrität: PASS. Reale Windows-/ADB-Ausführung v0.5.21: NOT RUN.
