# LFN-IN Windows-Updater – genaue Anleitung

Diese Anleitung gehört zu **LFN-Scout v0.4.62**. Der Updater automatisiert den Weg von einer neuen, von ChatGPT gelieferten Projekt-ZIP bis zum GitHub-Build und – bei der derzeit gewählten **Variante B** – bis zur lokal verfügbaren APK unter `Builds\debug\`.

## 1. Was der Updater automatisch macht

Der Updater führt in dieser Reihenfolge aus:

1. Prüft, ob **Git** installiert ist.
2. Prüft, ob die **GitHub CLI (`gh`)** installiert und bei GitHub angemeldet ist.
3. Prüft das lokale Repository `C:\Projekte\LFN-IN`.
4. Falls es noch nicht existiert, wird `https://github.com/yewie56/LFN-IN.git` automatisch geklont.
5. Prüft, ob lokale, noch nicht gespeicherte Änderungen vorhanden sind. In diesem Fall bricht der Updater absichtlich ab.
6. Führt `git pull --ff-only` aus und bringt den lokalen Stand auf den aktuellen Stand von GitHub.
7. Sucht im Windows-Download-Ordner automatisch die neueste Datei nach dem Muster `LFN-Scout_Android_v*.zip`.
8. Entpackt die ZIP in ein temporäres Verzeichnis.
9. Prüft, ob die ZIP ein vollständiges Android-Projekt enthält.
10. Liest die `versionName` automatisch aus `app\build.gradle.kts`.
11. Kopiert den neuen Projektstand in das lokale Git-Repository.
12. Die Verzeichnisse `.git\` und `Builds\` werden dabei ausdrücklich geschützt.
13. Zeigt die geplanten Git-Änderungen an.
14. Fragt vor Commit und Push ausdrücklich: `Änderungen committen und zu GitHub pushen? [J/N]`.
15. Erst nach `J` wird ein Commit `LFN-Scout vX.Y.Z` erzeugt und zu GitHub gepusht.
16. Wartet auf den zu genau diesem Commit gehörenden GitHub-Actions-Lauf.
17. Zeigt den Build-Fortschritt über `gh run watch` an.
18. Bei erfolgreichem Build wird bei **Variante B** erneut `git pull` ausgeführt.
19. Dadurch erscheint die von GitHub gebaute APK lokal unter `C:\Projekte\LFN-IN\Builds\debug\LFN-Scout-vX.Y.Z-debug.apk`.

Der Updater ändert niemals selbst Android-Quellcode. Er übernimmt lediglich den Inhalt der neuen Projekt-ZIP, versioniert ihn mit Git und startet dadurch den vorhandenen GitHub-Actions-Build.

---

## 2. Erstinstallation

### 2.1 Voraussetzungen

Benötigt werden unter Windows:

- Windows PowerShell 5.1 oder neuer
- Git
- GitHub CLI (`gh`)
- ein GitHub-Konto mit Zugriff auf `yewie56/LFN-IN`

### 2.2 Git installieren

In PowerShell oder Windows Terminal:

```powershell
winget install --id Git.Git -e
```

Danach das Terminal schließen und neu öffnen.

Prüfen:

```powershell
git --version
```

### 2.3 GitHub CLI installieren

```powershell
winget install --id GitHub.cli -e
```

Prüfen:

```powershell
gh --version
```

### 2.4 Einmalig bei GitHub anmelden

```powershell
gh auth login
```

Empfohlene Auswahl:

1. `GitHub.com`
2. `HTTPS`
3. Anmeldung über den Browser

Danach prüfen:

```powershell
gh auth status
```

### 2.5 Updater installieren

Im entpackten Projekt zu folgendem Ordner gehen:

```text
Tools\WindowsUpdater\
```

Dann doppelklicken:

```text
Install-LFN-IN-Updater.cmd
```

Der Installer kopiert den Updater nach:

```text
%LOCALAPPDATA%\LFN-IN\Updater\
```

also typischerweise nach:

```text
C:\Users\<Benutzername>\AppData\Local\LFN-IN\Updater\
```

Außerdem wird auf dem Desktop eine Verknüpfung angelegt:

```text
LFN-IN Update
```

Die persönliche `settings.json` im installierten Updater-Verzeichnis bleibt bei späteren Neuinstallationen erhalten.

---

## 3. Normaler Update-Ablauf

Für jede neue Version ist der normale Ablauf danach nur noch:

1. Neue ZIP von ChatGPT herunterladen, z. B.:

```text
LFN-Scout_Android_v0.4.62.zip
```

2. Die ZIP **nicht manuell in das Git-Repository entpacken**. Sie darf einfach im Download-Ordner liegen bleiben.
3. Auf dem Desktop `LFN-IN Update` starten.
4. Der Updater findet automatisch die neueste passende ZIP.
5. Er zeigt die erkannte Version und alle geplanten Änderungen an.
6. Diese Liste prüfen.
7. Bei der Abfrage

```text
Änderungen committen und zu GitHub pushen? [J/N]
```

`J` eingeben.
8. Der Updater pusht die neue Version.
9. GitHub Actions startet automatisch.
10. Der Updater wartet auf Tests und Build.
11. Nach erfolgreichem Build wird bei Variante B der APK-Commit von GitHub automatisch per `git pull` geholt.
12. Die APK liegt anschließend lokal unter:

```text
C:\Projekte\LFN-IN\Builds\debug\LFN-Scout-vX.Y.Z-debug.apk
```

---

## 4. Was auf dem Bildschirm zu sehen ist

Typischer Ablauf:

```text
============================================================
 LFN-Scout - Windows Updater
============================================================

==> Voraussetzungen pruefen
[PASS] Git gefunden.
[PASS] GitHub CLI gefunden.
[PASS] GitHub CLI ist angemeldet.

==> Lokales Repository vorbereiten
[PASS] Repository ist sauber und aktuell (main).

==> Update-ZIP bestimmen
[PASS] ZIP: C:\Users\...\Downloads\LFN-Scout_Android_v0.4.62.zip
[PASS] Neue Version erkannt: 0.4.62

==> Projektdateien einspielen

Geplante Git-Aenderungen:
 M app/build.gradle.kts
 M CHANGELOG.md
 A ...

Aktueller Commit: ...
Neue Version:     0.4.62
Geschuetzt:       .git\ und Builds\

Aenderungen committen und zu GitHub pushen? [J/N]
```

Erst bei `J` werden Commit und Push ausgeführt.

---

## 5. Dry-Run

Ein Dry-Run testet das Einspielen der ZIP und zeigt die geplanten Änderungen, führt aber **keinen Commit und keinen Push** aus.

PowerShell:

```powershell
& "$env:LOCALAPPDATA\LFN-IN\Updater\Update-LFN-IN.ps1" -DryRun
```

Der Updater spielt die ZIP probeweise ein, zeigt `git status` und setzt das Repository danach wieder auf den Ausgangsstand zurück.

Eine bestimmte ZIP kann so getestet werden:

```powershell
& "$env:LOCALAPPDATA\LFN-IN\Updater\Update-LFN-IN.ps1" -ZipPath "C:\Users\<Name>\Downloads\LFN-Scout_Android_v0.4.62.zip" -DryRun
```

---

## 6. Eine bestimmte ZIP statt der neuesten verwenden

Standardmäßig wird die neueste Datei nach dem Muster

```text
LFN-Scout_Android_v*.zip
```

im Download-Ordner verwendet.

Eine konkrete ZIP kann übergeben werden:

```powershell
& "$env:LOCALAPPDATA\LFN-IN\Updater\Update-LFN-IN.ps1" -ZipPath "D:\Downloads\LFN-Scout_Android_v0.4.62.zip"
```

---

## 7. Sicherheitsmechanismen

Der Updater enthält bewusst mehrere Sperren:

### 7.1 `.git\` wird geschützt

Das lokale Git-Repository wird nicht aus der ZIP überschrieben. Dadurch bleiben Repository-Verbindung und Git-Historie erhalten.

### 7.2 `Builds\` wird geschützt

Bereits erzeugte APKs werden beim Einspielen einer neuen Quellcode-ZIP nicht gelöscht.

### 7.3 Lokale Änderungen stoppen den Updater

Wenn `git status` bereits Änderungen meldet, wird **nicht automatisch überschrieben**. Erst müssen diese Änderungen bewusst committed, verworfen oder anderweitig gesichert werden.

### 7.4 Nur Fast-Forward-Pull

Der Updater verwendet:

```text
git pull --ff-only
```

Dadurch werden keine automatischen Merge-Commits erzeugt.

### 7.5 Projekt-ZIP wird geprüft

Vor dem Kopieren müssen unter anderem vorhanden sein:

```text
app\build.gradle.kts
settings.gradle.kts
build.gradle.kts
gradlew
gradlew.bat
.github\workflows\build-android.yml
```

Fehlt etwas, wird abgebrochen.

### 7.6 Versionsnummer wird automatisch gelesen

Die Version wird aus

```text
app\build.gradle.kts
```

gelesen. Eine Versionsnummer muss nicht von Hand im Updater eingetragen werden.

---

## 8. Rollback

### 8.1 Vor Commit/Push

Wenn bei der Sicherheitsabfrage `N` eingegeben wird, führt der Updater automatisch einen Rollback auf den vorherigen Commit aus. Es wird nichts zu GitHub übertragen.

### 8.2 Nach einem bereits erfolgten Push

Ist eine Version bereits gepusht, sollte sie nicht durch Löschen der Git-Historie zurückgenommen werden. Stattdessen zunächst den fehlerhaften Commit identifizieren:

```powershell
cd C:\Projekte\LFN-IN
git log --oneline -10
```

Für einen sauberen Rücknahme-Commit:

```powershell
git revert <COMMIT-ID>
git push
```

Bei Unsicherheit vor `git revert` nicht experimentieren, sondern den Commit bzw. den GitHub-Actions-Fehler prüfen.

---

## 9. Wenn GitHub Actions fehlschlägt

Der Updater beendet sich dann mit einem Fehler und zeigt die Workflow-ID an. Die fehlgeschlagenen Logzeilen können direkt angezeigt werden mit:

```powershell
gh run view <RUN-ID> --repo yewie56/LFN-IN --log-failed
```

Oder im Browser:

```powershell
gh run view <RUN-ID> --repo yewie56/LFN-IN --web
```

Wichtig: Bei einem roten Build wird die fehlerhafte Version zwar als Quellcode-Commit auf GitHub vorhanden sein, aber es wird keine erfolgreiche APK als abgeschlossene Version betrachtet.

---

## 10. Variante B – derzeit aktiv

Derzeit ist im GitHub-Workflow eingestellt:

```yaml
env:
  STORE_APK_IN_REPOSITORY: "true"
```

Damit wird nach erfolgreichen Tests und erfolgreichem Debug-Build die APK zusätzlich in das Repository committed:

```text
Builds/debug/LFN-Scout-vX.Y.Z-debug.apk
```

Der Updater wartet auf den Build und führt danach noch einmal `git pull --ff-only` aus. Dadurch erscheint die APK automatisch auch im lokalen Projektverzeichnis.

Das normale GitHub-Artifact bleibt **zusätzlich** erhalten.

---

## 11. Variante A – späterer Wechsel möglich

Wenn APKs später nicht mehr im Git-Repository gespeichert werden sollen, wird in

```text
.github\workflows\build-android.yml
```

nur diese Einstellung geändert:

```yaml
STORE_APK_IN_REPOSITORY: "false"
```

Dann bleibt die APK als GitHub-Artifact verfügbar, wird aber nicht mehr nach `Builds\debug\` committed.

In `settings.json` des Windows-Updaters wird dann ebenfalls gesetzt:

```json
"storeApkInRepository": false
```

Ist zusätzlich

```json
"downloadArtifactWhenNotStored": true
```

gesetzt, lädt der Updater das erfolgreiche Artifact automatisch in das konfigurierte Verzeichnis herunter.

Damit ist der Updater bereits für beide Betriebsarten vorbereitet.

---

## 12. settings.json

Die Standardkonfiguration lautet:

```json
{
  "repository": "https://github.com/yewie56/LFN-IN.git",
  "repositorySlug": "yewie56/LFN-IN",
  "branch": "main",
  "workDirectory": "C:\\Projekte\\LFN-IN",
  "downloadsDirectory": "%USERPROFILE%\\Downloads",
  "zipPattern": "LFN-Scout_Android_v*.zip",
  "workflowFile": "build-android.yml",
  "waitForGithubBuild": true,
  "storeApkInRepository": true,
  "downloadArtifactWhenNotStored": true,
  "artifactDownloadDirectory": "%USERPROFILE%\\Downloads\\LFN-IN-Builds"
}
```

### `workDirectory`

Lokales Git-Repository. Standard:

```text
C:\Projekte\LFN-IN
```

### `downloadsDirectory`

Ordner, in dem nach der neuen ZIP gesucht wird.

### `zipPattern`

Dateimuster der von ChatGPT gelieferten Pakete.

### `waitForGithubBuild`

Bei `true` wartet der Updater auf GitHub Actions.

### `storeApkInRepository`

`true` = Variante B. Nach erfolgreichem Build holt der Updater die APK über `git pull` aus `Builds\debug\`.

`false` = Variante A. Die APK bleibt außerhalb der Git-Historie.

---

## 13. Typische Fehler

### Git fehlt

Meldung:

```text
Git fehlt.
```

Lösung:

```powershell
winget install --id Git.Git -e
```

### GitHub CLI fehlt

```powershell
winget install --id GitHub.cli -e
```

### GitHub CLI nicht angemeldet

```powershell
gh auth login
```

### `.git` fehlt im Arbeitsverzeichnis

Wenn `C:\Projekte\LFN-IN` existiert, aber kein Git-Repository ist, bricht der Updater bewusst ab. Den fremden Ordner sichern/umbenennen oder `workDirectory` in `settings.json` ändern. Ist das Zielverzeichnis ganz nicht vorhanden, klont der Updater das Repository automatisch.

### Repository enthält lokale Änderungen

Prüfen:

```powershell
cd C:\Projekte\LFN-IN
git status
```

Der Updater überschreibt solche Änderungen nicht automatisch.

### Keine ZIP gefunden

Prüfen, ob die Datei im Download-Ordner ungefähr so heißt:

```text
LFN-Scout_Android_v0.4.62.zip
```

Alternativ die ZIP mit `-ZipPath` explizit angeben.

---

## 14. Manuelle Kontrolle nach einem erfolgreichen Update

Repository-Stand:

```powershell
cd C:\Projekte\LFN-IN
git status
git log --oneline -5
```

Letzte GitHub-Actions-Läufe:

```powershell
gh run list --repo yewie56/LFN-IN --limit 5
```

APKs bei Variante B:

```powershell
Get-ChildItem C:\Projekte\LFN-IN\Builds\debug\*.apk
```

---

## 15. Empfohlener Arbeitsablauf

Für normale neue Versionen künftig nur noch:

```text
1. Neue LFN-Scout-ZIP herunterladen.
2. ZIP im Download-Ordner liegen lassen.
3. Desktop-Verknüpfung „LFN-IN Update“ starten.
4. Änderungsübersicht prüfen.
5. Mit J bestätigen.
6. GitHub-Tests und Build abwarten.
7. APK aus C:\Projekte\LFN-IN\Builds\debug\ verwenden.
```

Damit werden manuelles Entpacken in den Git-Ordner, manuelles `git add`, `git commit`, `git push`, das Warten im Browser und das nachträgliche Herunterladen der APK bei Variante B weitgehend überflüssig.
