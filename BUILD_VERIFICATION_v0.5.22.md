# BUILD_VERIFICATION v0.5.22

Datum: 25.09.2026

## Anlass

Realer v0.5.21-Nutzerlauf: `VORTEST_INSTALL.bat` scheiterte sofort mit CMD-Syntaxfehler an der inline in `VORTEST.bat` eingebetteten PowerShell-RegEx zur `versionName`-Ermittlung. PREPARE und Gradle wurden dabei nicht gestartet.

## Korrektur

- Inline-PowerShell-Versionsermittlung vollständig aus `VORTEST.bat` entfernt.
- `PREPARE_PROJECT.bat` ebenfalls versionsneutral gemacht; keine Abhängigkeit von einer geerbten `LFN_VERSION`-Umgebungsvariable.
- `tools/run-vortest.ps1` und `tools/package-active.ps1` lesen `versionName` weiterhin direkt aus `app/build.gradle.kts`.
- Version auf `0.5.22`, `versionCode 169` erhöht.

## Statische Prüfungen

- Kein `for /f` + inline PowerShell-RegEx mehr in `VORTEST.bat`: PASS.
- Kein `%LFN_VERSION%` in `VORTEST.bat`/`PREPARE_PROJECT.bat`: PASS.
- `run-vortest.ps1` liest `versionName` aus `app/build.gradle.kts`: PASS.
- `package-active.ps1` liest `versionName` aus `app/build.gradle.kts`: PASS.
- `versionName = "0.5.22"`, `versionCode = 169`: PASS.

## Noch offen

- Reale Windows-Ausführung `VORTEST_INSTALL.bat`: NOT RUN.
- ADB-Warteschleife/Installation: NOT RUN.
- Xiaomi-Funktionstest: NOT RUN.

Keine Android-Laufzeitänderung gegenüber v0.5.21/v0.5.20.
