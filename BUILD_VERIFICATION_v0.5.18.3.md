# BUILD_VERIFICATION v0.5.18.3

**Version:** 0.5.18.3 / versionCode 164  
**Datum:** 25.09.2026

## Anlass

Beim ersten realen Windows-Lauf von v0.5.18.2 fand `PREPARE_PROJECT.bat` korrekt ein JDK 17 und `java -version` lieferte `openjdk version "17.0.20.1" ...`. Der PowerShell-Runner brach dennoch mit `NativeCommandError` ab. Ursache war nicht Java selbst, sondern die Kombination aus `$ErrorActionPreference = 'Stop'` und der Umleitung nativer STDERR-Ausgabe über `2>&1`.

## Korrektur

- `tools/run-vortest.ps1` nutzt für native Prozesse jetzt `System.Diagnostics.Process` mit getrenntem STDOUT/STDERR.
- Erfolg/Fehler wird ausschließlich über `Process.ExitCode` entschieden.
- Die Korrektur gilt zentral für PREPARE, Gradle, `package-active.ps1` und ADB.
- Ausgaben auf STDERR werden protokolliert, führen bei Exitcode 0 aber nicht zum Abbruch.
- Asynchrones Lesen von STDOUT/STDERR verhindert Pipe-Deadlocks bei großen Gradle-Ausgaben.
- Keine beabsichtigte Änderung am Android-Laufzeitcode.

## Statische Prüfungen

Die versionsspezifische Prüfung `python tools/verify_v05183_delivery.py` kontrolliert insbesondere:

- versionName/versionCode,
- Nutzung von `System.Diagnostics.Process`,
- getrennte Erfassung von STDOUT/STDERR,
- Exitcode-basierte Entscheidung,
- Abwesenheit der fehlerauslösenden `& $command 2>&1 | Tee-Object`-Konstruktion im Runner,
- Reihenfolge PREPARE → Unit-Test → assembleDebug → ACTIVE → ADB,
- bestehende Ein-Gerät-/Unauthorized-Schutzlogik,
- unveränderte BASE-R1-Referenz.

## Nicht ausgeführt

- Windows PowerShell 5.1: NOT RUN in dieser Erstellungsumgebung.
- Android SDK/Gradle-Build: NOT RUN.
- ADB-Installation: NOT RUN.
- Xiaomi-Funktionstest: NOT RUN.

Ein realer PASS darf erst nach dem Nutzerlauf gemäß `docs/project/TESTPLAN_v0.5.18.3.md` eingetragen werden.

## Ergebnis der statischen versionsspezifischen Prüfung

`python tools/verify_v05183_delivery.py`: **25/25 PASS**.

Dies bestätigt nur den Quell-/Automationsvertrag. Der reale Windows-Lauf bleibt bis zur Nutzerprobe NOT RUN.
