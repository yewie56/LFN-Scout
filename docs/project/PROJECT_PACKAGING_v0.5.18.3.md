# LFN-Scout v0.5.18.3 – ACTIVE/BASE-Paketierung und korrigierter Vortest

**Stand:** 25.09.2026  
**Version:** 0.5.18.3 / versionCode 164  
**Basis:** v0.5.18.2. Keine beabsichtigte Änderung an Audio-, Statistik-, Analyzer-, UI- oder Supabase-Laufzeitfunktion.

## Korrektur gegenüber v0.5.18.2

Windows PowerShell 5.1 kann normale STDERR-Ausgabe nativer Programme bei einer `2>&1`-Pipeline als `NativeCommandError` behandeln. `java -version` schreibt seine Versionsinformation regulär auf STDERR. v0.5.18.2 konnte deshalb trotz erfolgreichem Java-Prozess unmittelbar nach PREPARE abbrechen.

v0.5.18.3 startet die nativen Prozesse über `System.Diagnostics.Process`, erfasst STDOUT und STDERR getrennt und entscheidet ausschließlich anhand des echten Prozess-Exitcodes. Das gilt für PREPARE, Gradle, die ACTIVE-Paketierung und ADB. STDERR allein ist ausdrücklich kein Fehlerkriterium.

## Vortestablauf

`VORTEST.bat`:

1. `PREPARE_PROJECT.bat --no-pause`
2. `gradlew.bat :app:testDebugUnitTest --stacktrace`
3. nur bei PASS: `gradlew.bat assembleDebug --stacktrace`
4. Debug-APK prüfen und versioniert nach `Builds/` kopieren
5. `LFN-Scout_ACTIVE_v0.5.18.3.zip` erzeugen
6. ACTIVE-ZIP vollständig verifizieren und SHA-256 erzeugen
7. Log und `LAST_VORTEST_RESULT.txt` schreiben

`VORTEST_INSTALL.bat` führt denselben Ablauf aus und installiert erst danach per `adb install -r`, sofern genau ein autorisiertes Gerät erkannt wird.

## ACTIVE/BASE

### ACTIVE – bei jeder Version neu

`LFN-Scout_ACTIVE_v0.5.18.3.zip`

Enthält den buildfähigen aktiven Quellstand, Tests, Tools, Gradle-Dateien, aktuelle textbasierte Projektdokumentation sowie Supabase-Schema/Functions/Config ohne ausgelagertes Storage-Backup.

### BASE – unverändert weiterverwenden

`LFN-Scout_BASE_ASSETS_R1.zip`

SHA-256 laut `BASE_PACKAGE_REQUIRED.txt`. Enthält die großen selten veränderten Referenz-/Recovery-Artefakte. Für den normalen Android-Build nicht erforderlich.
