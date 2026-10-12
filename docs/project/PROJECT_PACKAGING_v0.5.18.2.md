# LFN-Scout v0.5.18.2 – Vortest, ACTIVE-/BASE-Pakete und ADB-Installation

**Stand:** 25.09.2026  
**Version:** 0.5.18.2 / versionCode 163  
**Basis:** v0.5.18.1. Keine beabsichtigte Änderung an Audio-, Statistik-, Analyzer-, UI- oder Supabase-Laufzeitfunktion.

## 1. Ziel

Der Windows-Vortest erzeugt nach vollständigem PASS automatisch einen archivierungs- und ChatGPT-übergabefertigen aktiven Quellstand. Auf Wunsch wird anschließend die gebaute Debug-APK per ADB auf genau ein angeschlossenes und autorisiertes Android-Gerät installiert.

## 2. Standardablauf

Doppelklick auf `VORTEST.bat`:

1. `PREPARE_PROJECT.bat --no-pause`
2. `gradlew.bat :app:testDebugUnitTest --stacktrace`
3. nur bei PASS: `gradlew.bat assembleDebug --stacktrace`
4. erwartete Debug-APK prüfen und versioniert nach `Builds/` kopieren
5. `LFN-Scout_ACTIVE_v0.5.18.2.zip` erzeugen
6. ACTIVE-ZIP vollständig entpacken/verifizieren und SHA-256 erzeugen
7. Log und `LAST_VORTEST_RESULT.txt` schreiben

Ein Fehler beendet die Kette sofort. Ein fehlgeschlagener Vortest darf kein offizielles PASS-ACTIVE-Paket vortäuschen.

## 3. Installation auf dem Xiaomi / Android-Gerät

Doppelklick auf `VORTEST_INSTALL.bat` oder Aufruf `VORTEST.bat --install` führt nach den obigen PASS-Schritten zusätzlich aus:

1. `adb.exe` über PATH, Android-SDK-Umgebungsvariablen, `local.properties` oder den üblichen Windows-SDK-Pfad suchen.
2. `adb devices` auswerten.
3. Bei `unauthorized`: Abbruch.
4. Bei 0 oder mehr als 1 freigegebenen Geräten: Abbruch.
5. Bei genau einem Gerät: `adb -s <serial> install -r Builds\LFN-Scout_v0.5.18.2-debug.apk`.

`-r` aktualisiert eine bereits installierte App bei kompatibler Signatur/Paket-ID und soll deren App-Daten erhalten. Eine Signaturabweichung oder ein Installationsfehler führt zu FAIL; es erfolgt keine automatische Deinstallation.

Es gibt bewusst keinen PUSH-ONLY-Standardweg: Zum realen App-Test ist eine Installation erforderlich.

## 4. Zweiteilige Projektübergabe

### ACTIVE-Paket – bei jeder Version neu

`LFN-Scout_ACTIVE_v0.5.18.2.zip`

Enthält den buildfähigen aktiven Quellstand, Gradle/Wrapper, Tests, Tools, aktuelle textbasierte Projektdokumentation, Supabase-Schema/Functions/Config ohne Storage-Backup und die Referenz auf das BASE-Paket.

Nicht enthalten sind generierte/machinenspezifische Inhalte (`Builds`, `**/build`, `.gradle`, `.idea`, `.tools`, `.git`, `local.properties`, APK/AAB/Logs) sowie die unten ausgelagerten BASE-Pfade. Vor dem ZIP wird ein eventuell von PREPARE ergänzter lokaler JDK-Pfad aus der gepackten Kopie von `gradle.properties` entfernt.

### BASE-Paket – nur bei Änderung neu

`LFN-Scout_BASE_ASSETS_R1.zip`

Ausgelagerte Pfade:

- `docs/project/analyzer_v0.7/assets/`
- `docs/project/analyzer_v0.7/references/`
- `docs/project/handoff_assets/`
- `docs/project/FILTERBANK_UI_REFERENCE/`
- `docs/reconstruction/`
- `supabase/storage_backup/`

Das BASE-Paket ist nicht zum normalen Android-Build erforderlich. Es enthält u. a. große UI-/Analyzer-Referenzen, historische Rekonstruktionsunterlagen und private Supabase-Storage-Recovery-Daten. Nicht öffentlich verteilen.

Die aktuelle Zuordnung steht in `BASE_PACKAGE_REQUIRED.txt` einschließlich SHA-256. Erst wenn einer der ausgelagerten Inhalte geändert wird, wird eine neue BASE-Revision (`R2`, `R3`, ...) erzeugt.

## 5. Ergebnisdateien nach PASS

Unter `Builds/`:

- `LFN-Scout_v0.5.18.2-debug.apk`
- `LFN-Scout_ACTIVE_v0.5.18.2.zip`
- `LFN-Scout_ACTIVE_v0.5.18.2.zip.sha256`
- `VORTEST_v0.5.18.2_<Zeit>.log`
- `LAST_VORTEST_RESULT.txt`

Das BASE-Paket liegt separat im Archiv-/Übergabebestand und wird nicht bei jedem Vortest neu erzeugt.

## 6. Fehlercodes

| Code | Bedeutung |
|---:|---|
| 10 | PREPARE fehlgeschlagen |
| 20 | Unit-Tests fehlgeschlagen |
| 30 | `assembleDebug` fehlgeschlagen |
| 40 | erwartete Debug-APK fehlt |
| 50 | ACTIVE-Paket-Skript fehlgeschlagen |
| 51 | ACTIVE-ZIP/SHA nach Paketierung fehlt |
| 60 | `adb.exe` nicht gefunden |
| 61 | Gerät nicht für USB-Debugging autorisiert |
| 62 | nicht genau ein freigegebenes ADB-Gerät |
| 63 | `adb install -r` fehlgeschlagen |

## 7. Statusgrenzen

Die statische Prüfung der Skripte beweist nur deren Struktur. In der ChatGPT-Erstellungsumgebung wurden Windows PowerShell, Android SDK, Gradle-Build und ADB-Installation nicht ausgeführt. Erst ein tatsächlicher Windows-Lauf mit `Status: PASS` bestätigt Build/Paketierung; die Funktion der App auf dem Xiaomi erfordert weiterhin den passenden Gerätetest.
