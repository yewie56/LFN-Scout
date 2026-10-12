# Ergänzung v0.5.23 – Buildweg unverändert

Für v0.5.23 bleibt der Windows-Test-/Installationsweg unverändert:

```bat
VORTEST_INSTALL.bat
```

Die Versionsnummer wird weiterhin zentral aus `app/build.gradle.kts` von den PowerShell-Werkzeugen gelesen. BAT-Wrapper bleiben versionsneutral.

---

# Ergänzung v0.5.22 – BAT-Wrapper ohne Inline-PowerShell-Versionsparser

`VORTEST.bat` und `PREPARE_PROJECT.bat` dürfen die Projektversion nicht mit eingebetteten PowerShell-/RegEx-Ausdrücken innerhalb von CMD-`for /f` ermitteln. Der reale v0.5.21-Lauf zeigte, dass die verschachtelten Quotes/Metazeichen von `cmd.exe` bereits vor PowerShell syntaktisch zerlegt werden können.

Die BAT-Dateien bleiben daher versionsneutral. Die zentrale Versionsquelle ist weiter `app/build.gradle.kts`; `tools/run-vortest.ps1` und `tools/package-active.ps1` lesen `versionName` direkt dort und verwenden sie für Logs, APK- und ACTIVE-Dateinamen.

---

# Ergänzung v0.5.21 – Projektversion nicht mehr in Vortest-Skripten fest verdrahten

`VORTEST.bat`, `PREPARE_PROJECT.bat`, `tools/run-vortest.ps1` und `tools/package-active.ps1` verwenden die `versionName` aus `app/build.gradle.kts` als gemeinsame Versionsquelle. Damit dürfen APK-/ACTIVE-/Lognamen nicht mehr manuell auf eine alte Version festgelegt werden. Der reale v0.5.20-Lauf zeigte genau diesen Fehler: Paketierung v0.5.20, anschließende Prüfung v0.5.19.

---

# Ergänzung v0.5.20 – unveränderter VORTEST-/ADB-Weg

Für v0.5.20 bleibt der v0.5.19-Windows-Buildweg unverändert: `VORTEST_INSTALL.bat` führt PREPARE → Unit-Tests → `assembleDebug` → APK-/ACTIVE-Prüfung aus und wartet danach ohne Standard-Timeout auf genau ein autorisiertes ADB-Gerät. Keine Gerätefreigabe aus statischen Prüfungen ableiten.

---

# Ergaenzung v0.5.19 – ADB-Warteschleife nach erfolgreichem Vortest

`VORTEST_INSTALL.bat` fuehrt weiterhin PREPARE → `:app:testDebugUnitTest` → `assembleDebug` → APK-Pruefung → ACTIVE-Paketierung aus. Erst nach vollstaendigem PASS beginnt die ADB-Phase.

In der ADB-Phase wird alle 2 Sekunden `adb devices` geprueft. Bei keinem, `unauthorized`, `offline` oder mehreren angeschlossenen Geraeten bleibt der Vortest sichtbar in der Warteschleife. Erst bei genau einem autorisierten `device` wird `adb install -r` ausgefuehrt. Standardmaessig gibt es kein Timeout; manueller Abbruch ist mit Ctrl+C moeglich. Alle Meldungen bleiben live auf dem Bildschirm und parallel im Log.

Das erzeugte ACTIVE-Paket bleibt der buildfaehige, versionierte Quellstand. BASE R1 wird nur bei Aenderung ausgelagerter grosser Referenz-/Recovery-Artefakte neu erzeugt.

---

# Ergaenzung v0.5.18.4 – komplette Live-Ausgabe des Vortests

Ab v0.5.18.4 zeigt `VORTEST.bat`/`VORTEST_INSTALL.bat` alle Ausgaben von PREPARE, Unit-Tests, `assembleDebug`, ACTIVE-Paketierung und ADB **waehrend der Ausfuehrung live im Konsolenfenster** und schreibt dieselben Zeilen parallel in das Vortest-Log. Normale STDERR-Ausgabe bleibt erlaubt; nur der echte Prozess-Exitcode entscheidet ueber PASS/FAIL.

Nicht-prozessgebundene Ergebnisse wie APK-Pfad/Groesse/SHA-256, ACTIVE-ZIP-Pfad/Groesse/SHA-256 sowie ADB-Geraetezaehlung werden ebenfalls auf dem Screen angezeigt. Am Ende erscheint immer eine Zusammenfassung aller Schritte. Bei Fehlercode 62 werden konkrete Hinweise zum Anschluss/USB-Debugging angezeigt.

`VORTEST_INSTALL.bat` bleibt der bevorzugte Ein-Klick-Weg fuer Build + ACTIVE-Paket + Installation. BASE R1 wird nur bei Aenderung seiner ausgelagerten Inhalte neu erzeugt.

Vollstaendige Spezifikation: `docs/project/PROJECT_PACKAGING_v0.5.18.4.md`. Test: `docs/project/TESTPLAN_v0.5.18.4.md`.

---

# Ergänzung v0.5.18.3 – robuster nativer Prozessstart im Vortest

Ab v0.5.18.3 startet `tools/run-vortest.ps1` PREPARE, Gradle, ACTIVE-Paketierung und ADB über `System.Diagnostics.Process`. STDOUT und STDERR werden getrennt erfasst und protokolliert; der reale Prozess-Exitcode entscheidet über PASS/FAIL. Dadurch ist normale STDERR-Ausgabe wie `java -version` unter Windows PowerShell 5.1 kein falscher `NativeCommandError` mehr.

Der Ein-Klick-Ablauf bleibt: `VORTEST.bat` für Build/Paketierung, `VORTEST_INSTALL.bat` für denselben Ablauf plus ADB-Installation nach vollständigem PASS. ACTIVE/BASE-Trennung bleibt unverändert, BASE R1 wird wiederverwendet.

Vollständige Spezifikation: `docs/project/PROJECT_PACKAGING_v0.5.18.3.md`. Test: `docs/project/TESTPLAN_v0.5.18.3.md`.

---

# Ergänzung v0.5.18.2 – Ein-Klick-Vortest, ACTIVE-Paket und ADB-Install

Für den aktuellen Windows-Kommandozeilenweg gilt ab v0.5.18.2:

- `VORTEST.bat`: PREPARE → `:app:testDebugUnitTest` → `assembleDebug` → APK-Prüfung → automatisch geprüftes `Builds/LFN-Scout_ACTIVE_v0.5.18.2.zip` + SHA-256.
- `VORTEST_INSTALL.bat` oder `VORTEST.bat --install`: derselbe Ablauf und nur nach vollständigem PASS zusätzlich `adb install -r` auf genau ein autorisiertes Gerät. Bei 0, mehreren oder `unauthorized` Geräten keine Installation.
- Das ACTIVE-ZIP ist der normale Quellstand für Archivierung, ChatGPT-/Bibliotheksübergabe und nächste Entwicklung. Große selten geänderte Dateien liegen separat in `LFN-Scout_BASE_ASSETS_R1.zip`; `BASE_PACKAGE_REQUIRED.txt` nennt Revision und SHA-256. Das BASE-Paket ist für den Android-Build nicht erforderlich.
- Aus dem ACTIVE-ZIP ausgeschlossen werden insbesondere `.git`, `.gradle`, `.idea`, `.tools`, `Builds`, alle `build`-Verzeichnisse, `local.properties`, APK/AAB/Logs und die dokumentierten BASE-Pfade. Maschinenlokale JDK-Pfade werden aus der gepackten Kopie von `gradle.properties` entfernt.

Vollständige Spezifikation: `docs/project/PROJECT_PACKAGING_v0.5.18.2.md`. Test: `docs/project/TESTPLAN_v0.5.18.2.md`.

---
# v0.5.18.1 – automatisierter Windows-Vortest (25.09.2026)

Für einen frisch entpackten Windows-Projektstand ist der bevorzugte Vortest jetzt ein einzelner Aufruf:

```bat
VORTEST.bat
```

Er führt streng nacheinander aus:

```bat
PREPARE_PROJECT.bat --no-pause
gradlew.bat :app:testDebugUnitTest --stacktrace
gradlew.bat assembleDebug --stacktrace
```

Bei Fehler wird sofort abgebrochen. Unter `Builds/` entstehen ein zeitgestempeltes `VORTEST_v0.5.18.1_YYYYMMDD_HHMMSS.log`, `LAST_VORTEST_RESULT.txt` und bei PASS `LFN-Scout_v0.5.18.1-debug.apk`. `PREPARE_PROJECT.bat` bleibt bei normalem Doppelklick unverändert interaktiv; `--no-pause`/`/nopause` ist nur die neue Automationsoption. Der Xiaomi-Test nach `docs/project/TESTPLAN_v0.5.18.md` wird dadurch nicht ersetzt.

---
# v0.5.18 – Build-/Abnahmepflicht (25.09.2026)

Vor weiteren Änderungen v0.5.17 separat sichern. Windows: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`. Vollständiges Log prüfen. Danach Xiaomi-Regression gemäß `docs/project/TESTPLAN_v0.5.18.md`; erst dann v0.5.18 für Installation freigeben. Lokale Quell-/Host-Tests ersetzen keinen Android-/Compose-Build. Keine Supabase-Änderung in dieser Version. Vollständiges ZIP über ChatGPT-Bibliothek transferieren; Projekt enthält ggf. private Recovery-Daten.

---
> **v0.5.17 – Hochformat:** Windows: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`, danach Xiaomi-Rotationstest für beide Analyzer und Erinnerungsansicht nach `docs/project/TESTPLAN_v0.5.17.md`. Statischer XML-Test ist kein Android-Buildnachweis. Das gesondert gesicherte v0.5.16-ZIP nicht überschreiben.

> **v0.5.13 – Testfolge:** Neues Wasserfall-Pop-up ist ausschließlich eine Compose-Anzeigeänderung. Nach `PREPARE_PROJECT.bat` Windows-Gradle-Unit-Tests und APK-Build, anschließend Xiaomi-Sichtprüfung gemäß `docs/project/TESTPLAN_v0.5.13.md`. Die Kotlin-JVM-Format- und statischen Checks sind kein Android-/Compose-Typ-/Gerätetest.

> **v0.5.12 – Buildpflicht:** `PREPARE_PROJECT.bat` im frischen Windows-Projektordner, danach `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`. Erst danach Xiaomi-Abnahme nach `docs/project/TESTPLAN_v0.5.12.md`. Host-/statische Tests ersetzen keine Android-/Compose-Kompilierung. Das vom Nutzer gelieferte ORIGINAL-Icon ist ein unverändertes PNG in `drawable-nodpi`.

> v0.5.11: Vor Gerätefreigabe Windows Gradle `:app:testDebugUnitTest` sowie `assembleDebug` tatsächlich ausführen. Host-/PSI-PASS allein belegen keine Android-Kompilierung. Testplan `docs/project/TESTPLAN_v0.5.11.md` beachten.

> **v0.5.10 – Windows-Korrekturkandidat:** Nach `PREPARE_PROJECT.bat` im frischen Projektordner beide Befehle `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace` ausführen. Ein tatsächlicher v0.5.09-Gradle-Build ist fehlgeschlagen; Host-/Quelltests allein bestätigen keine Android-Compilation. Geräteplan `docs/project/TESTPLAN_v0.5.10.md`.

> **v0.5.09:** Buildwege unverändert. Auf Windows nach `PREPARE_PROJECT.bat` unbedingt `:app:testDebugUnitTest` sowie `assembleDebug` ausführen. JVM-Host- und PSI-Syntaxprüfungen allein beweisen keine Android-Kompilierung. Geräteabnahme nach `docs/project/TESTPLAN_v0.5.09.md`.

> **v0.5.08:** Bestehende Windows-/GitHub-Buildwege gelten unverändert. Nach `PREPARE_PROJECT.bat` sind `:app:testDebugUnitTest` und `assembleDebug` auf Windows verpflichtend; lokale reine JVM-Tests ersetzen Android-Compose-Kompilierung NICHT. `docs/project/TESTPLAN_v0.5.08.md` vor Gerätefreigabe verwenden.

> **v0.5.06:** Frisch entpacken → `PREPARE_PROJECT.bat` → `gradlew.bat :app:testDebugUnitTest --stacktrace` → `gradlew.bat assembleDebug --stacktrace` → Xiaomi-Gerätetest nach `docs/project/TESTPLAN_v0.5.06.md`. MAX/CLR, 3 Kurven, manuelles Kamera-Pop-up und Setup-/Bildexport im Gerätetest überprüfen; Host-/Static-Tests ersetzen keinen Android-Build.

> **v0.5.05:** Windows-Update: neues Projekt entpacken → `PREPARE_PROJECT.bat` → `gradlew.bat :app:testDebugUnitTest --stacktrace` → `gradlew.bat assembleDebug --stacktrace`. Gerätetest laut `docs/project/TESTPLAN_v0.5.05.md`. Export unter Download/LFN-Scout/Setups/ und /Diagnostics/. Der Host-Timingtest allein ist **kein** Android-Buildnachweis.

> **v0.5.04.2-Diagnose:** Für sporadische 48-kHz-/Rolling-Abstürze siehe `docs/project/AUDIO_DIAGNOSE_v0.5.04.2.md`. Zusätzlich zum gewöhnlichen App-Debuglog kann `adb logcat` vor der Reproduktion gestartet werden. Die Build-Wege selbst sind unverändert.

> **v0.5.04.1 – Korrekturstand:** Zuerst `PREPARE_PROJECT.bat` (neuer Projektordner), dann `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`. Der v0.5.04-Nutzerlauf war **FAIL** (zwei Timing-JUnit-Fälle, Kotlin-Smart-Cast); erfolgreiche Android-Tests für v0.5.04.1 sind noch nicht belegt. Änderungen betreffen keinen Buildweg.

> **v0.5.04 – Build-/Updateweg unverändert.** Bei neuer Windows-Entpackung zuerst `PREPARE_PROJECT.bat`, danach `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`. Quell-/Hostprüfung: `bash tools/RUN_ANALYZER_V0504_SELFTEST.sh`, `python tools/verify_v0504_source.py`. Der exklusive 48-kHz-Mikrofonwechsel, STOP+Panelverlassen-Restore, digitaler Test und PNG/JPG sind **erst auf dem Xiaomi anhand `docs/project/TESTPLAN_v0.5.04.md` zu verifizieren**. Kein Android-Buildnachweis im Lieferpaket. Das ZIP enthält private Recovery-Daten; nicht öffentlich committen.

> **v0.5.03:** Unveränderte Buildwege A/B/C. Vor Geräteabnahme nach `PREPARE_PROJECT.bat` `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace` ausführen; Ergebnis und Xiaomi-Testplan `docs/project/TESTPLAN_v0.5.03.md` protokollieren. Die lokale Core-Vorprüfung ist `bash tools/RUN_ANALYZER_V0503_SELFTEST.sh`. Kein aktueller Android-Buildnachweis im Lieferpaket.

> **v0.5.02-Testkandidat:** `PREPARE_PROJECT.bat` bei frischem Windows-Ordner, anschließend `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`. Für lokale Core-Vorprüfung mit installiertem `kotlinc`: `bash tools/RUN_ANALYZER_V0502_SELFTEST.sh`. Android-/Xiaomi-Test noch **NOT RUN**. Testplan: `docs/project/TESTPLAN_v0.5.02.md`. Bestehende Buildwege A/B/C bleiben unverändert.

# LFN-Scout v0.5.02 – verbindliche Build- und Update-Workflows

Dieses Dokument beschreibt den **verbindlich festgelegten Stand** für LFN-Scout v0.4.62. Es beschreibt alle aktuell unterstützten Buildwege und den bevorzugten Android-only-Ablauf mit möglichst wenigen Arbeitsschritten.

## 1. Überblick: drei Buildwege, ein Projekt

Es gibt **nur ein gemeinsames Quellprojekt**. Je nach Arbeitsumgebung wird derselbe Projektstand auf drei verschiedene Arten gebaut:

| Weg | Zweck | Gerät | Start | Ergebnis |
|---|---|---|---|---|
| **A – Android Studio** | vollständige lokale Entwicklung/Debugging | Windows-PC | Projekt in Android Studio öffnen und Build/Run starten | APK lokal auf dem PC |
| **B – Gradle-Kommandozeile** | reproduzierbarer lokaler Test/Build ohne Android-Studio-Oberfläche | Windows-PC | `gradlew.bat testDebugUnitTest` und `gradlew.bat assembleDebug` | APK lokal auf dem PC |
| **C – GitHub Actions** | bevorzugter Android-only-Weg | Android-Handy + GitHub-Cloud | GitSync `Push` | Tests + APK-Build auf GitHub |

**Wichtig:** Es gibt keinen Schalter in der App, der A, B oder C auswählt. Der verwendete Buildweg ergibt sich ausschließlich daraus, **wo und wie der Build gestartet wird**.

## 2. Projektstruktur und wichtige Dateien

Der vollständige Projektstand enthält unter anderem:

```text
LFN-IN/
├── .github/
│   └── workflows/
│       └── build-android.yml
├── app/
├── gradle/
├── Tools/
│   └── WindowsUpdater/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── PREPARE_PROJECT.bat
├── BUILD_WORKFLOWS.md
├── GITHUB_BUILD.md
├── UPDATER_ANLEITUNG.md
└── CHANGELOG.md
```

Ab v0.5.18.2 wird die Projektübergabe in **ACTIVE** und **BASE** getrennt. Das ACTIVE-ZIP enthält den vollständigen buildfähigen aktiven Quellstand und niemals einen `.git/`-Ordner. Große selten veränderte Referenz-/Recovery-Artefakte liegen im separat versionierten BASE-Paket; für den normalen Android-Build ist BASE nicht erforderlich.

## 3. Rolle von `PREPARE_PROJECT.bat`

`PREPARE_PROJECT.bat` ist **kein eigener Buildweg**. Die Datei ist eine Windows-Hilfe für die Buildwege A und B.

Sie erledigt insbesondere:

1. Java 17 suchen bzw. vorbereiten.
2. Java 17 nur für den Projekt-/Buildkontext verwenden; ein eventuell vorhandenes globales Java 8 bleibt unverändert.
3. Gradle 8.13 vorbereiten.
4. den Gradle Wrapper erzeugen/auffrischen.
5. `gradle.properties` mit **beiden** lokalen JDK-17-Eintraegen `org.gradle.java.home` (Gradle-Daemon) und `org.gradle.java.installations.paths` (`:analyzer-core`-Toolchain) konfigurieren.
6. Vorhandenes Java und den Compiler `javac` auf **Version 17** pruefen. Ein gefundenes Android SDK wird in `local.properties` eingetragen. In der Liefer-ZIP sind bewusst keine absoluten Windows-JDK-/SDK-Pfade enthalten.

### Wann muss `PREPARE_PROJECT.bat` ausgeführt werden?

- Beim **ersten lokalen Windows-Build** eines frisch entpackten Projekts: **ja, empfohlen**.
- Wenn Java/Gradle/Wrapper lokal bereits korrekt vorbereitet sind: **normalerweise nicht bei jedem Build nötig**.
- Beim Android-only-Weg C mit GitHub Actions: **nie erforderlich**.

---

# Buildweg A – Android Studio unter Windows

## 4. Voraussetzungen

Benötigt werden:

1. Windows-PC.
2. Android Studio.
3. aktuelles `LFN-Scout_ACTIVE_vX.Y.Z.zip`. Das passende BASE-Paket wird nur benötigt, wenn ausgelagerte Referenz-/Recovery-Inhalte gebraucht oder geändert werden.

## 5. Minimaler Ablauf A

### Erstmalig oder bei einem frisch entpackten Projekt

1. ZIP vollständig in einen eigenen Projektordner entpacken.
2. `PREPARE_PROJECT.bat` doppelklicken.
3. Auf `PASS: Projekt vorbereitet.` warten. Das Skript meldet ausdruecklich PASS fuer Gradle-Daemon **und** `analyzer-core`-Toolchain.
4. Android Studio starten.
5. **Open** wählen und den entpackten Projektordner öffnen; kein alter v0.4.85-Ordner.
6. Gradle-Synchronisation abwarten. Falls die IDE trotzdem ein abweichendes Gradle-JDK nutzt, siehe `docs/project/JDK17_ANDROID_STUDIO_v0.4.89.md`.
7. Für einen Build: **Build → Make Project** bzw. den entsprechenden Build-Befehl verwenden.
8. Für Installation/Debugging auf einem angeschlossenen Android-Gerät: Gerät auswählen und **Run** starten.

### Bei späteren Builds desselben vorbereiteten Projektordners

1. Android Studio öffnen.
2. Projekt öffnen.
3. Build oder Run starten.

`PREPARE_PROJECT.bat` muss nicht vor jedem Android-Studio-Build erneut laufen. **Achtung bei Git:** Nach der Windows-Vorbereitung enthaelt `gradle.properties` einen lokalen JDK-Pfad; diese lokalen Zeilen duerfen nicht in das gemeinsame Repository committed werden. Beim mobilen GitSync-Weg ist der ungeaenderte Projekt-ZIP-Stand massgeblich.

---

# Buildweg B – Gradle-Kommandozeile unter Windows

## 6. Zweck

Dieser Weg baut dasselbe Android-Projekt ohne Android-Studio-Oberfläche. Er eignet sich für reproduzierbare lokale Tests oder Automatisierung unter Windows.

## 7. Minimaler Ablauf B

### Erstmalig oder bei einem frisch entpackten Projekt

1. ZIP vollständig entpacken.
2. `PREPARE_PROJECT.bat` ausführen.
3. Danach im Projektordner PowerShell oder Eingabeaufforderung öffnen.
4. Unit-Tests ausfuehren (das App-Test-Task bindet `:analyzer-core:test` ein):

```bat
gradlew.bat testDebugUnitTest
```

5. Wenn die Tests erfolgreich sind, Debug-APK bauen:

```bat
gradlew.bat assembleDebug
```

Die normale Gradle-Ausgabe liegt anschließend unter:

```text
app\build\outputs\apk\debug\
```

### Bei späteren Builds desselben vorbereiteten Projektordners

Normalerweise reichen:

```bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

---

# Buildweg C – Android-only mit Work + GitSync + GitHub Actions

## 8. Ziel

Dieser Weg ist der **bevorzugte Standardweg**, wenn ausschließlich das Android-Handy verwendet werden soll.

Der Windows-PC bleibt vollständig ausgeschaltet.

```text
Android-Handy
  → ChatGPT Work
  → ACTIVE-Projekt-ZIP
  → Android-Dateimanager
  → lokales GitSync-Repository
  → GitSync Push
  → GitHub Actions
  → APK
  → Download auf dasselbe Handy
  → Installation
```

## 9. Einmalig auf dem Android-Handy installieren

Benötigt werden:

1. **ChatGPT-App** – für Work und die Entwicklungsaufträge.
2. **GitSync** – für Clone/Pull/Commit/Push des vollständigen Git-Repositories.
3. **Android-Dateimanager** – muss ZIP-Dateien entpacken sowie Ordner/Dateien löschen und kopieren können. Die Anzeige versteckter Dateien sollte einschaltbar sein, damit `.git/` sichtbar kontrolliert werden kann.
4. **Webbrowser** – zum Prüfen von GitHub Actions und zum Herunterladen der gebauten APK. Die offizielle GitHub-App ist optional und nicht erforderlich.

Android Studio, Java, Gradle, Termux und ein Windows-PC werden für Weg C **nicht benötigt**.

## 10. GitSync einmalig einrichten

1. GitSync öffnen.
2. Mit dem GitHub-Konto anmelden.
3. Repository `yewie56/LFN-IN` auswählen.
4. Repository auf das Handy klonen.
5. Branch `main` verwenden.
6. Automatische Synchronisation zunächst deaktiviert lassen; Pull/Commit/Push werden bewusst manuell ausgelöst.

Nach dem Clone existiert auf dem Handy ein lokaler Repository-Ordner. Dieses Dokument bezeichnet ihn als **R**.

Beispiel:

```text
R = /storage/emulated/0/Documents/LFN-IN/
```

Der tatsächliche Pfad hängt von GitSync und Android ab.

Der Ordner enthält unter anderem:

```text
R/
├── .git/
├── .github/
├── app/
├── gradle/
├── build.gradle.kts
└── ...
```

### `.git/` ist geschützt

`R/.git/` wird beim Clone von Git/GitSync erzeugt. Dieser Ordner:

- ist **nicht** Bestandteil der Projekt-ZIP,
- darf **niemals** gelöscht werden,
- darf **niemals** durch Inhalte einer ZIP überschrieben werden.

Er enthält Git-Historie, Branch-Zustand und Repository-Verknüpfung.

### `Builds/` ist ebenfalls geschützt

Bei der aktuell aktiven Variante B speichert GitHub Actions erfolgreiche APKs zusätzlich unter:

```text
R/Builds/debug/
```

Deshalb bleibt `R/Builds/` beim Einspielen einer neuen Quellcode-ZIP ebenfalls erhalten.

## 11. Neue Version in Work anfordern

### 11.1 Woher Work den aktuellen Projektstand nimmt

Bevor Work Änderungen erzeugt, muss Work den **aktuellen vollständigen Projektstand** kennen. Bevorzugt liest Work dazu das GitHub-Repository `yewie56/LFN-IN` über den verbundenen GitHub-Zugriff. Dieser Zugriff dient hier nur als **Quelle**; der eigentliche schreibende Push erfolgt später mit GitSync.

Falls Work in einer Sitzung keinen Zugriff auf das Repository hat, wird als Ausgangsbasis das zuletzt gültige ACTIVE-ZIP angehängt. Das in `BASE_PACKAGE_REQUIRED.txt` genannte BASE-Paket wird zusätzlich bereitgestellt, wenn die Aufgabe ausgelagerte Referenz-/Recovery-Inhalte betrifft. Es darf nicht aus einzelnen alten Codeausschnitten rekonstruiert werden.

### 11.2 Änderungsauftrag

In ChatGPT Work einen eindeutigen Auftrag geben, z. B.:

```text
Bearbeite LFN-Scout auf Basis des aktuellen Projekts LFN-IN.
Erstelle die nächste Version.

Änderungen:
- ...
- ...

Aktualisiere betroffene Tests, CHANGELOG und Dokumentation.
Liefere anschließend IMMER das buildfähige ACTIVE-ZIP der neuen Version.
Wenn ausgelagerte BASE-Inhalte geändert wurden, liefere zusätzlich eine neue BASE-Revision.
Keines der Pakete darf einen .git-Ordner enthalten.
```

**Verbindliche Ausgabe ab v0.5.18.2:** Work liefert immer den vollständigen buildfähigen ACTIVE-Quellstand, beispielsweise:

```text
LFN-Scout_ACTIVE_v0.5.18.2.zip
```

Die passende BASE-Revision bleibt unverändert, solange die ausgelagerten Pfade unverändert sind. Keine Patch-Datei und keine Sammlung einzelner geänderter Dateien als Hauptübergabe.

## 12. Neue ZIP auf das Handy herunterladen

1. Die von Work erzeugte ZIP herunterladen.
2. Sie liegt danach typischerweise unter `Download/`.
3. Die ZIP **noch nicht direkt in R entpacken**.

## 13. Vor dem Ersetzen des Projekts: GitSync Pull

1. GitSync öffnen.
2. Repository `LFN-IN` auswählen.
3. **Pull** ausführen.
4. Nur weiterarbeiten, wenn Pull ohne Konflikt abgeschlossen wurde.

Warum zuerst Pull? Dadurch ist der lokale Stand auf dem Handy auf dem aktuellen GitHub-Stand, einschließlich eventuell von GitHub Actions committeden APKs.

Bei einem Merge-/Pull-Konflikt: **nicht mit der ZIP fortfahren**, sondern den Konflikt zuerst klären.

## 14. ZIP in temporären Ordner entpacken

Die ZIP in einen separaten temporären Ordner entpacken. Dieses Dokument bezeichnet ihn als **T**.

Beispiel:

```text
Download/LFN-Scout_Android_v0.4.63.zip
        ↓ entpacken
T = Download/LFN-Scout_Android_v0.4.63/
```

In T müssen mindestens vorhanden sein:

```text
app/
.github/
gradle/
build.gradle.kts
settings.gradle.kts
gradlew
gradlew.bat
```

Fehlen wesentliche Projektbestandteile, darf T nicht nach R übernommen werden.

## 15. Alten Projektinhalt in R entfernen

Im Dateimanager den Repository-Ordner R öffnen.

**Erhalten:**

```text
R/.git/
R/Builds/
```

**Alle anderen Projektdateien und Projektordner aus R löschen.**

Beispiel vor dem Löschen:

```text
R/
├── .git/               ← ERHALTEN
├── Builds/             ← ERHALTEN
├── .github/            ← LÖSCHEN
├── app/                ← LÖSCHEN
├── gradle/             ← LÖSCHEN
├── Tools/              ← LÖSCHEN
├── build.gradle.kts    ← LÖSCHEN
├── settings.gradle.kts ← LÖSCHEN
└── ...                 ← LÖSCHEN
```

Danach verbleiben zunächst nur:

```text
R/
├── .git/
└── Builds/
```

Diese Löschoperation ist wichtig. Reines Darüberkopieren würde Dateien aus alten Versionen stehen lassen, die in der neuen Version absichtlich entfernt wurden.

## 16. Vollständigen neuen Projektinhalt von T nach R kopieren

Jetzt den **gesamten Inhalt von T** nach R kopieren.

Beispiel:

```text
T/app/               → R/app/
T/.github/           → R/.github/
T/gradle/            → R/gradle/
T/build.gradle.kts   → R/build.gradle.kts
T/settings.gradle.kts→ R/settings.gradle.kts
...
```

Danach enthält R:

```text
R/
├── .git/      ← vorhandener Git-Ordner, unverändert
├── Builds/    ← vorhandene APK-Historie, unverändert
├── .github/   ← neue Version aus T
├── app/       ← neue Version aus T
├── gradle/    ← neue Version aus T
└── ...        ← neue Version aus T
```

## 17. GitSync: Änderungen committen und pushen

1. GitSync öffnen.
2. Prüfen, dass GitSync die erwarteten Änderungen erkennt: Modified / Added / Deleted.
3. Alle gewünschten Änderungen für den Commit vormerken (**Stage all** bzw. äquivalente Funktion).
4. Commit-Nachricht eingeben:

```text
LFN-Scout vX.Y.Z
```

5. **Commit** ausführen.
6. **Push** ausführen.

Der Push nach `main` startet GitHub Actions automatisch. Ein zusätzlicher manueller Start des Workflows ist normalerweise nicht nötig.

## 18. Was GitHub Actions automatisch macht

Der Workflow `.github/workflows/build-android.yml` führt aus:

1. Repository auschecken.
2. Java 17 bereitstellen.
3. Gradle vorbereiten.
4. App-Version aus `app/build.gradle.kts` lesen.
5. `./gradlew testDebugUnitTest --stacktrace` ausführen.
6. Nur bei erfolgreichen Unit-Tests `./gradlew assembleDebug --stacktrace` ausführen.
7. APK umbenennen in:

```text
LFN-Scout-vX.Y.Z-debug.apk
```

8. APK als GitHub-Artifact hochladen.
9. Bei aktuell aktivierter Variante B die APK zusätzlich unter `Builds/debug/` in das Repository committen.

## 19. Buildstatus auf dem Handy prüfen

Im normalen Webbrowser öffnen:

```text
https://github.com/yewie56/LFN-IN
```

Dann **Actions** öffnen und den neuesten Lauf ansehen.

- gelb: läuft
- grün: erfolgreich
- rot: fehlgeschlagen

### Wenn der Build rot ist

1. Fehlgeschlagenen Workflow-Lauf öffnen.
2. Schritt mit rotem X öffnen.
3. relevanten Fehlertext kopieren bzw. Log herunterladen.
4. Fehler in Work einfügen.
5. Work soll eine korrigierte **vollständige Projekt-ZIP** der nächsten Version erzeugen.
6. Wieder ab Abschnitt 12 beginnen.

## 20. APK herunterladen und installieren

Wenn GitHub Actions grün ist, liegt die APK bei Variante B im Repository unter:

```text
Builds/debug/LFN-Scout-vX.Y.Z-debug.apk
```

Auf dem Handy:

1. Browser → `github.com/yewie56/LFN-IN`.
2. `Builds` öffnen.
3. `debug` öffnen.
4. gewünschte APK öffnen/herunterladen.
5. im Android-Dateimanager `Download/` öffnen.
6. APK antippen.
7. **Installieren** wählen.

Falls Android beim ersten Mal die Installation blockiert, für den verwendeten Browser oder Dateimanager einmalig **„Aus dieser Quelle installieren“** bzw. **„Unbekannte Apps installieren“** erlauben.

## 21. Nach erfolgreichem Build GitSync erneut synchronisieren

Weil GitHub Actions bei Variante B die APK selbst nach `Builds/debug/` committen kann, ist GitHub danach eventuell einen Commit vor dem lokalen Repository.

Deshalb nach erfolgreichem Build:

1. GitSync öffnen.
2. Repository `LFN-IN` wählen.
3. **Pull** ausführen.

Danach ist R wieder auf demselben Stand wie GitHub und enthält auch die neu gebaute APK.

---

# 22. Kompakter Standardablauf C

Nach der einmaligen Einrichtung besteht der normale Android-only-Zyklus aus diesen Arbeitsschritten:

1. Änderungswunsch in Work formulieren.
2. vollständige neue Projekt-ZIP aus Work herunterladen.
3. GitSync → **Pull**.
4. ZIP nach T entpacken.
5. in R alles außer `.git/` und `Builds/` löschen.
6. gesamten Inhalt von T nach R kopieren.
7. GitSync → **Stage all → Commit → Push**.
8. Browser → GitHub Actions prüfen.
9. bei grünem Build APK herunterladen und installieren.
10. GitSync → **Pull**, damit der lokale Stand wieder einschließlich APK aktuell ist.

Kein Windows-PC, kein Android Studio, kein Termux und keine lokalen Git-Kommandos sind für diesen Weg erforderlich.

---

# 23. Manuelles Starten von GitHub Actions

Normalerweise startet der Workflow durch GitSync `Push` automatisch.

Zusätzlich kann er manuell gestartet werden:

```text
GitHub → LFN-IN → Actions → Build LFN-Scout → Run workflow
```

Dieser manuelle Start ist nur nötig, wenn derselbe bereits auf GitHub vorhandene Commit erneut gebaut werden soll.

---

# 24. Aktuelle APK-Ablagestrategie

Aktuell ist **Variante B** aktiv:

```yaml
STORE_APK_IN_REPOSITORY: "true"
```

Damit gibt es zwei APK-Ausgaben:

1. GitHub-Artifact des Workflow-Laufs.
2. versionierte APK im Repository:

```text
Builds/debug/LFN-Scout-vX.Y.Z-debug.apk
```

Später kann auf Variante A gewechselt werden:

```yaml
STORE_APK_IN_REPOSITORY: "false"
```

Dann bleibt die APK nur als Artifact bzw. später als Release erhalten. Die Buildwege A, B und C bleiben davon unberührt.

---

# 25. Optionaler Windows-Updater als Fallback

Das Projekt enthält zusätzlich `Tools/WindowsUpdater/` und `UPDATER_ANLEITUNG.md`. Dieser Updater ist **nicht Bestandteil des normalen Android-only-Wegs C**. Er ist nur ein optionaler Windows-Fallback, falls später doch ein Windows-PC verwendet werden soll.

Er automatisiert dort im Wesentlichen:

```text
ACTIVE-Projekt-ZIP → lokales Git-Repository → Commit/Push → GitHub Actions → APK-Synchronisation
```

Für den ausschließlich mobilen Betrieb kann dieser Ordner ignoriert werden.

---

# 26. Verbindliche Regeln für neue Projektversionen

1. Jede neue LFN-Scout-Version wird als vollständiges buildfähiges **ACTIVE-ZIP** geliefert; eine neue BASE-Revision nur bei Änderung der ausgelagerten BASE-Inhalte.
2. Die ZIP enthält **keinen `.git/`-Ordner**.
3. `R/.git/` wird nur durch GitSync/Git verwaltet und niemals ersetzt.
4. `R/Builds/` bleibt beim Einspielen einer neuen Quellcode-ZIP erhalten.
5. Vor jeder lokalen Projektaktualisierung auf Android zuerst **GitSync Pull**.
6. Neue ZIP immer zunächst nach T entpacken, niemals direkt in R.
7. In R alte Projektdateien außerhalb `.git/` und `Builds/` entfernen, bevor T hineinkopiert wird.
8. Erst danach GitSync Commit/Push ausführen.
9. GitHub Actions baut nur nach erfolgreichen Unit-Tests weiter.
10. Eine neue APK wird erst installiert, wenn der GitHub-Actions-Lauf grün ist.


## Entwicklungsnachtrag Analyzer Phase 2 (19.09.2026; keine neue Freigabe)

Das Projekt enthält das neue Gradle-Modul `:analyzer-core`. Die bestehenden drei Buildwege bleiben unverändert. `gradlew.bat testDebugUnitTest` (Windows) bzw. `./gradlew testDebugUnitTest --stacktrace` (GitHub Actions) führt über die App-Task-Abhängigkeit auch dessen JUnit-Tests aus; `assembleDebug` bindet dieselbe Bibliothek ein. Ein unabhängiger Host-Selbsttest lässt sich bei installiertem Kotlin-CLI unter Linux/macOS mit `bash tools/RUN_ANALYZER_PHASE2_SELFTEST.sh` ausführen. In dieser Entwicklungsumgebung war kein Android-SDK vorhanden; daher ist kein vollständiger Android-Build und keine APK als geprüft ausgewiesen.


## Analyzer-Entwicklungsprüfungen Phase 2H (noch v0.4.85)

Auf einem PC mit Kotlin CLI können `bash tools/RUN_ANALYZER_PHASE2_SELFTEST.sh` und `bash tools/RUN_ANALYZER_HIGHSPEED_SELFTEST.sh` ausgeführt werden. Diese isolierten Host-Prüfungen sind **kein** Ersatz für die vorhandenen Android-Buildwege A/B/C mit `testDebugUnitTest` und `assembleDebug`; sie produzieren keine APK. Private Supabase-Recovery-Daten beim GitSync-/GitHub-Upload ausschließen (bestehende `.gitignore` erhalten). Siehe `docs/project/ANALYZER_PHASE2_HIGHSPEED.md`.

## v0.4.89 – JDK-Suche und Diagnose

Bei einem neuen Projektordner PREPARE ausführen; JDK17 Suche priorisiert projektlokales, Android Studio, installierte JDKs vor alten Projekt-Caches. Bei Entwicklermodus ist in Zusatzfunktionen die Highspeed-Diagnose sichtbar, nicht das fertige Analyzer-Panel. Die v0.4.86-APK wurde auf dem Xiaomi erfolgreich getestet; für v0.4.89 sind Tests noch offen.

## v0.4.89 – Gerätetest Analyse-Gain

`PREPARE_PROJECT.bat`, dann `:analyzer-core:test`, `:app:testDebugUnitTest`, `assembleDebug`. Entwicklerdiagnose unter Zusatzfunktionen: Gain 0–50 dB nur für Impulsdetektor; bestehende Aufnahme nicht betroffen. v0.4.89 Android-Build vor Ort noch offen.

## v0.4.89 – Manuelle Impuls-Empfindlichkeit

Nach PREPARE_PROJECT.bat Analyzer- und App-Tests, dann assembleDebug, anschließend Android Studio Run. Testablauf einschließlich Presets/Rauschmultiplikator siehe `docs/project/ANALYZER_PHASE2F_SCHWELLE_v0.4.89.md`.

## Stand v0.5.00

Reine Anpassung der Analyzer-Oberfläche. Alle Android-Studio-/Gradle-/GitHub-Buildwege und `PREPARE_PROJECT.bat` bleiben unverändert. Empfohlener lokaler Ersttest: `gradlew.bat :app:testDebugUnitTest --stacktrace`, danach Android Studio **Run**. Eine erfolgreiche Android-Kompilierung oder Geräteleistung dieser Version wird hier nicht vorausgesetzt.
