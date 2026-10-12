# LFN-Scout v0.5.45.8 – vollständiger GitHub-Build ausschließlich vom Android-Handy

## Voraussetzungen

- Android-Handy, Internetzugang, freier Speicher (mindestens 1 GB empfohlen).
- GitHub-Konto. Verwende ein **privates** Repository: Der Projektquellstand enthält lokale Konfigurations- und Projektdateien, die vor Veröffentlichungen überprüft werden müssen.
- **Termux** aus F-Droid (nicht zwingend die veraltete Play-Store-Version). Keine Root-Rechte, kein PC, kein lokales Android Studio notwendig.
- ZIP `LFN-Scout_v0.5.45.8_GITHUB_ANDROID_KOMPLETT.zip` im Android-Ordner **Download** speichern.
- ZIP enthält vollständigen Projektquellcode mit `.github/workflows/build-android.yml` – Android-Dateimanager müssen versteckte `.github`-Ordner beibehalten.

## A – Einrichtung, nur einmal erforderlich

1. Installiere Termux über `https://f-droid.org/packages/com.termux/`, öffne Termux.
2. Führe folgende Befehle nacheinander aus:

   ```sh
   pkg update
   pkg install git gh unzip
   termux-setup-storage
   ```

3. Beim letzten Befehl die Android-Berechtigung für gemeinsamen Speicher bestätigen.
4. Registriere dich bei GitHub unter `https://github.com` bzw. melde dich an. Aktiviere nach Möglichkeit Zwei-Faktor-Authentifizierung.

## B – Projekt vom Handy nach GitHub hochladen

1. Lade das Projekt-ZIP in **Download** herunter.
2. Öffne Termux und führe die folgenden Befehle aus (Projektname `LFN-IN` entspricht der bisherigen Projektkonvention):

   ```sh
   mkdir -p ~/lfn-scout-0458
   cd ~/lfn-scout-0458
   unzip -q /sdcard/Download/LFN-Scout_v0.5.45.8_GITHUB_ANDROID_KOMPLETT.zip
   bash GITHUB_ANDROID_TERMUX_START.sh LFN-IN
   ```

3. Bei der GitHub-Anmeldung wird ein Browser-Gerätecode verlangt. Öffne `https://github.com/login/device`, trage den im Terminal angezeigten Code ein und bestätige die Freigabe. Danach nach Termux zurückkehren.
4. Das Skript erstellt ein **privates** Repository `LFN-IN` oder lädt in dein schon vorhandenes Repo hoch. **ACHTUNG:** Bei bereits existierendem Repo mit anderer Historie kann `git push` fehlschlagen. Nie `--force` verwenden; erst Rückfrage halten.
5. Öffne `https://github.com/DEIN-BENUTZERNAME/LFN-IN` im Android-Browser. Der Ordner `.github/workflows` muss vorhanden sein.

**Datenschutz:** Die `.gitignore` schließt lokale vollständige Cloud-Backups, private `.env`-Dateien, Keystores und den Ordner `Builds/` aus. Vor einem Upload in ein öffentliches Repository alle Inhalte und die Git-Historie zusätzlich auf Geheimnisse prüfen. Das ZIP selbst enthält Projektmaterial, aber die `.gitignore` schützt nur den Git-Upload, nicht das ZIP auf deinem Handy.

## C – GitHub Actions APK-Übersetzung

1. Öffne im Android-Browser die private Repo-Seite: `https://github.com/DEIN-BENUTZERNAME/LFN-IN`.
2. Öffne den Tab **Actions**; falls GitHub verlangt, die Workflows erst aktivieren, bestätige dies.
3. Wähle links **Build LFN-Scout**. Auf kleinen Handy-Displays ggf. Browser-Menü → **Desktopwebsite** einschalten.
4. Tippe auf **Run workflow**, Branch `main`, dann den grünen Start-Button. Auch ein `push` auf `main` kann den Workflow automatisch starten.
5. Öffne den neu gestarteten Lauf. Warte auf die grüne Erfolgsmeldung für **Test and build debug APK**.
6. Bei Fehlschlag: Öffne die rote Aktion; die erste Fehlermeldung aus `Run unit tests` oder `Build debug APK` protokollieren und hier im Chat hochladen. Eine rote Aktion ist **keine** installierbare APK.

### APK direkt auf Android herunterladen

**Variante A (empfohlen für Handy):** Nach erfolgreichem Workflow zu **Code → Builds → debug** gehen. Dort sollte `LFN-Scout-v0.5.45.8-debug.apk` stehen. Tippe den Dateinamen an, dann **Download raw file** (ggf. im Drei-Punkte-Menü). Die Datei ist eine `.apk`, kein ZIP.

**Variante B (Fallback):** Im Tab **Actions** den erfolgreichen Lauf öffnen und unten **Artifacts → LFN-Scout-v0.5.45.8-debug** herunterladen. Das Artifact kommt als ZIP: in Android entpacken, darin die `.apk` auswählen.

**Falls Variante A fehlt, obwohl Build grün:** GitHub Actions hat möglicherweise keine Schreibrechte für das Repo. Unter **Settings → Actions → General → Workflow permissions** `Read and write permissions` erlauben und neuen Workflow-Lauf starten, oder Variante B verwenden. In Organisations-Repositories kann dies administrativ eingeschränkt sein.

### APK installieren

1. Im Android-Dateimanager die heruntergeladene `.apk` antippen.
2. Wenn Android nach Berechtigung fragt: **Installation aus dieser Quelle zulassen** (nur für den von dir gerade heruntergeladenen GitHub-Build).
3. Installation bestätigen. Bei einer Signaturabweichung zur installierten Version **nicht sofort deinstallieren**: eine Deinstallation kann lokal gespeicherte Daten und NOISE-Referenzen löschen. Stattdessen zuerst Backup-/Signaturstrategie prüfen. GitHub-Debug-Builds können anders signiert sein als lokale Windows-Debug-Builds.
4. In der App über Versionsanzeige prüfen: `0.5.45.8`.

## D – Funktionsprüfung auf dem Handy

1. App öffnen, Analyzer aufrufen, NOISE auf AUS stellen. Eingang mit dem vorgesehenen **1-kΩ-Abschlusswiderstand** verbinden (nur bei geeignetem Anschluss und Bias-Beschaltung).
2. NOISE MESS: kontrollieren, ob bei niedriger Restleistung Wasserfall und Messung weiterlaufen.
3. NOISE 1/f: mindestens 2 Minuten laufen lassen. Meldung `Unter Rauschboden` ist legitim; die Fehlermeldung `Kein verwertbares LF-Nutzsignal` sollte bei kontinuierlich eintreffenden gültigen PCM-Daten nicht auftreten.
4. NOISE AUS: das ursprüngliche Spektrum muss wieder sichtbar sein.
5. DIAG öffnen und Weißrauschtest starten. Referenzsteigung der gültigen 1-kΩ-Messung wurde zuvor mit −19,72 dB/Dekade gemessen. Die ältere Messung mit −11,67 war fehlerhaft und wird nicht verwendet.
6. Prüfe die FAST-Auswertung und ob die bisherigen CSV-Filterbank-Einstellungen sichtbar, aber deaktiviert bleiben.

**Prüfergebnis übermitteln:** Screenshot des erfolgreichen GitHub-Actions-Laufs, bei Problemen Fehlerlog, Screenshots mit NOISE 1/f und ggf. DIAG. Ein erfolgreiches Actions-Build beweist keine messtechnische Kalibration.
