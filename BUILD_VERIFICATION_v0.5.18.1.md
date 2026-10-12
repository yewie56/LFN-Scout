# Build-/Testnachweis v0.5.18.1 – automatisierter Windows-Vortest

**Stand:** 25.09.2026  
**Android-Version:** 0.5.18.1 / 162  
**Basis:** v0.5.18; keine beabsichtigte App-Laufzeitänderung außer Versionskennung.

| Prüfung | Ergebnis | Aussage |
|---|---|---|
| `python tools/verify_v05181_vortest.py` | **PASS 12/12** | Versionsdaten, PREPARE-no-pause, VORTEST-Reihenfolge, APK-Prüfung, Log/Result und Exitcode statisch vorhanden |
| Windows `VORTEST.bat` | **NOT RUN** | Windows/PowerShell/Android-SDK in dieser Erstellungsumgebung nicht verfügbar |
| `:app:testDebugUnitTest` | **NOT RUN** | Wird von `VORTEST.bat` automatisiert; realer Windows-Lauf ausstehend |
| `assembleDebug` | **NOT RUN** | Wird nur nach erfolgreichen Unit-Tests gestartet; reale APK ausstehend |
| Xiaomi-Gerätetest | **NOT RUN** | v0.5.18-Geräteabnahme bleibt erforderlich |

## Automationsablauf

`VORTEST.bat` startet `tools/run-vortest.ps1`. Dieses Skript ruft nacheinander auf:

1. `PREPARE_PROJECT.bat --no-pause`
2. `gradlew.bat :app:testDebugUnitTest --stacktrace`
3. `gradlew.bat assembleDebug --stacktrace`
4. Prüfung von `app\build\outputs\apk\debug\app-debug.apk`
5. Kopie nach `Builds\LFN-Scout_v0.5.18.1-debug.apk` und SHA-256 im Log

Fehlercodes: 10 PREPARE, 20 Unit-Test, 30 Assemble, 40 erwartete APK fehlt. Der nächste Schritt wird bei Fehler nicht ausgeführt.

**Keine Freigabebehauptung:** Der statische PASS beweist nur die vorhandene Automationslogik. Erst der Nutzerlauf auf Windows kann Gradle-/Compose-Kompilierung bestätigen.
