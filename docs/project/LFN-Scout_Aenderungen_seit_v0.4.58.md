## v0.4.79 – höher aufgelöster, weniger reduzierter Nähe-Helicorder

- Master-Bitmap auf **6144×3456** und Render-Supersampling auf 4× erhöht.
- Helicorder-Verdichtung von 240 auf **1536 Bins je 30-Minuten-Zeile** erhöht.
- Jeder Bin bewahrt ersten/letzten Samplewert sowie Minimum und Maximum in realer zeitlicher Reihenfolge.
- Kontinuierlicher scheli-artiger Trace damit deutlich detailreicher; keine zusätzliche Frequenzfilterung.

## v0.4.74 – LFN-Scout, Landscape-Liveplot und kompakte Zeitachse

- Sichtbarer App-/Produktname: **LFN-Scout**.
- Verzeichnis- und ZIP-Namen bleiben unverändert **LFN-Scout**.
- Näheplot in der App nun **16:9 Landscape**, Zeitachse horizontal entlang der langen Seite.
- Uhrzeitlabels auf **4 sp** halbiert.
- Export bleibt 1536×864 px mit 2× Supersampling.
- Redundante Float-Konvertierungswarnungen im Exporter entfernt.

## v0.4.73 – 16:9-Näheplot mit 2× Supersampling
- PNG-Endformat der selbst erzeugten Seismikplots auf 1536 × 864 px vereinheitlicht.
- Interne Renderauflösung 3072 × 1728 px, danach Downsampling.
- Maßstrecke links vor den Uhrzeiten; Wert und Einheit (`40000 nm/s`) direkt an der Strecke.
- WBBU-nahe kompakte Kopfzeile und 5-Minuten-X-Achse.
- Speicherpfad unverändert `Download/LFN-Scout/`.

# LFN-Scout – Änderungen seit der hochgeladenen Version 0.4.58


## v0.4.72 – Näheplot an WBBU angenähert
- Klassischer 24-h-Helicorder bleibt bei 48 Halb­stundenzeilen, wird aber kompakter und dichter gerendert.
- Vertikale Intensitätslegende links vor der Zeitachse spart horizontale Plotfläche.
- X-Achse jetzt in 5-Minuten-Schritten wie beim WBBU-Vorbild.
- Gespeicherter Näheplot verwendet dasselbe Layout; Speicherziel ist wieder `Download/LFN-Scout/`.


## v0.4.71 – Seismik-Interaktion und PNG-Export

- Näheplots mit doppelter Wellenform-Strichstärke.
- Intensitätslegende als physikalische Maßstrecke analog zur WBBU-Referenz; Counts-Fallback eindeutig markiert.
- Pinch-Zoom/Pan für WBBU und Näheplots, Doppeltipp zum Reset.
- PNG-Export des vollständigen Plots mit Erfolgsmeldung; Ablage unter `Download/LFN-Scout/`.

Stand: 17.09.2026

Diese Datei beschreibt **ausschließlich die Änderungen zwischen der hier ursprünglich hochgeladenen Version `LFN-Scout_Android_v0.4.58.zip` und dem aktuellen Stand `LFN-Scout_Android_v0.4.69.zip`**.

Grundlage ist ein direkter Dateivergleich beider vollständigen Projekt-ZIPs.

## 1. Zusammenfassung

Zwischen v0.4.58 und v0.4.62 wurden:

- **19 neue Dateien** ergänzt,
- **17 vorhandene Dateien** geändert,
- **keine vorhandenen Dateien gelöscht**.

Die Hauptänderungen betreffen:

1. GitHub Actions als zusätzlicher Buildweg,
2. Umbenennung der sichtbaren App von **LFN-Scout** auf **LFN-Scout**,
3. Umbenennung des Projekts/Repositories auf **LFN-IN**,
4. Korrekturen und Stabilisierung der Unit-Tests für CI,
5. automatische APK-Ablage unter `Builds/debug/`,
6. Windows-Updater als optionaler Fallback,
7. vollständige Dokumentation der drei Buildwege,
8. Festlegung des Android-only-Workflows mit Work + vollständiger Projekt-ZIP + GitSync + GitHub Actions.

Wichtig: Die eigentliche App-Funktionalität wurde in v0.4.59–v0.4.62 **nicht grundlegend erweitert**. Der Schwerpunkt lag auf Build-, CI-, Namens-, Test- und Workflow-Infrastruktur.

---

## 2. Ausgangsstand v0.4.58

Die hochgeladene Version v0.4.58 enthielt bereits unter anderem:

- Android-Studio-Projekt,
- lokalen Gradle-Build,
- `PREPARE_PROJECT.bat`,
- bestehende Unit-Tests,
- bestehende Supabase-Anbindung,
- Seismik-Panel,
- Rolling-Buffer-/Audio-Funktionen,
- bestehende Build-/Selftest-Dokumentation.

Noch **nicht vorhanden** waren:

- `.github/workflows/build-android.yml`,
- GitHub-Actions-CI,
- versionierte APKs unter `Builds/debug/`,
- Windows-Updater,
- zentrale Buildworkflow-Dokumentation,
- der App-Name `LFN-Scout`,
- der Projektname `LFN-IN`.

---

# 3. Änderungen in v0.4.59

## 3.1 GitHub Actions ergänzt

Neu hinzugefügt:

```text
.github/workflows/build-android.yml
```

Damit wurde ein dritter Buildweg eingeführt.

Der Workflow kann:

- automatisch bei `push` starten,
- automatisch bei `pull_request` starten,
- manuell über `workflow_dispatch` gestartet werden.

Der Ablauf lautet:

```text
Checkout
→ Java 17
→ Gradle
→ testDebugUnitTest
→ assembleDebug
→ APK als GitHub-Artifact
```

## 3.2 GitHub-Build-Dokumentation ergänzt

Neu:

```text
GITHUB_BUILD.md
```

Darin wird der GitHub-Actions-Build beschrieben.

## 3.3 Neue Prüfdateien

Neu:

```text
BUILD_VERIFICATION_v0.4.59.txt
verify_v0459_requirements.py
```

## 3.4 Versionsstand

```text
versionName = "0.4.59"
versionCode = 99
```

## 3.5 App-Funktionalität

In v0.4.59 wurde keine neue eigentliche App-Funktion ergänzt.

Der Schwerpunkt war:

```text
GitHub Actions / CI / APK-Build
```

---

# 4. Änderungen in v0.4.60

## 4.1 Öffentlicher App-Name geändert

Der sichtbare App-Name wurde geändert von:

```text
LFN-Scout
```

zu:

```text
LFN-Scout
```

Betroffen ist unter anderem:

```text
app/src/main/res/values/strings.xml
```

Aktuell:

```xml
<string name="app_name">LFN-Scout</string>
```

## 4.2 Projekt-/Repository-Name geändert

Das Projekt heißt jetzt:

```text
LFN-IN
```

in:

```text
settings.gradle.kts
```

Aktuell:

```kotlin
rootProject.name = "LFN-IN"
```

## 4.3 Interne Android-Package-ID bewusst nicht geändert

Aus Kompatibilitätsgründen bleiben erhalten:

```text
namespace = "de.brummbeobachter.app"
applicationId = "de.brummbeobachter.app"
```

Damit wird keine unnötige Trennung von bereits vorhandenen Installationen und Daten erzeugt.

## 4.4 Sichtbare Texte im Quellcode umbenannt

Geändert wurden unter anderem:

### `MainActivity.kt`

```text
LFN-Scout
→ LFN-Scout
```

betroffen sind unter anderem:

- Hauptüberschrift,
- Info-Dialog,
- Export-/Dateinamen.

### `DebugLogger.kt`

vorher:

```text
LFN-Scout_Log_...
```

jetzt:

```text
LFN-Scout_Log_...
```

### `ReminderNotifier.kt`

vorher z. B.:

```text
LFN-Scout Wecker
LFN-Scout sehr auffällig
```

jetzt:

```text
LFN-Scout Wecker
LFN-Scout sehr auffällig
```

### `RollingAudioService.kt`

Notification-Titel:

```text
LFN-Scout
→ LFN-Scout
```

### `SeismicLiveService.kt`

HTTP-User-Agent:

```text
LFN-Scout/<Version>
→ LFN-Scout/<Version>
```

## 4.5 Testumgebung für `org.json` korrigiert

Neu in:

```text
app/build.gradle.kts
```

ist:

```kotlin
testImplementation("org.json:json:20250517")
```

Grund:

Die lokalen JVM-Unit-Tests liefen zuvor teilweise gegen Android-Stub-Klassen von `org.json`, wodurch mehrere Parser-Tests in GitHub Actions fehlschlugen.

## 4.6 Facelift-Test korrigiert

Geändert:

```text
app/src/test/java/de/brummbeobachter/app/FaceliftThemePolicyTest.kt
```

Die erwarteten kompakten Layoutwerte wurden an den tatsächlichen Produktionsstand angepasst:

vorher:

```text
SCREEN_PADDING_DP = 10
SECTION_GAP_DP    = 5
CARD_PADDING_DP   = 10
FIELD_GAP_DP      = 4
```

jetzt:

```text
SCREEN_PADDING_DP = 7
SECTION_GAP_DP    = 3
CARD_PADDING_DP   = 7
FIELD_GAP_DP      = 2
```

## 4.7 Widersprüchlichen Reminder-Test bereinigt

Geändert:

```text
ReminderScheduleGridPolicyTest.kt
```

Der alte widersprüchliche Test:

```text
30 Minuten bleiben relativ
```

wurde ersetzt durch:

```text
15 Minuten bleiben relativ
```

Die 30-Minuten-Regel bleibt separat als Rasterverhalten getestet.

## 4.8 GitHub-Workflow umbenannt

Workflow-Name:

```text
Build LFN-Scout
```

## 4.9 Testreports bei CI-Fehlern

Bei fehlgeschlagenen Unit-Tests wird der Testreport zusätzlich als GitHub-Artifact hochgeladen.

## 4.10 Versionsstand

```text
versionName = "0.4.60"
versionCode = 100
```

---

# 5. Änderungen in v0.4.61

## 5.1 Automatische APK-Ablage im Repository

Neu:

```text
Builds/debug/.gitkeep
```

Der GitHub-Workflow kann erfolgreiche APKs versioniert speichern:

```text
Builds/debug/LFN-Scout-vX.Y.Z-debug.apk
```

Aktuell ist diese Variante aktiviert über:

```yaml
STORE_APK_IN_REPOSITORY: "true"
```

Das normale GitHub-Artifact bleibt parallel bestehen.

## 5.2 Version wird im Workflow automatisch gelesen

Die Versionsnummer wird direkt aus:

```text
app/build.gradle.kts
```

ausgelesen.

Damit muss die Versionsnummer nicht zusätzlich im GitHub-Workflow manuell gepflegt werden.

## 5.3 Automatische APK-Benennung

Beispiel:

```text
LFN-Scout-v0.4.61-debug.apk
```

bzw. entsprechend der jeweiligen `versionName`.

## 5.4 Automatischer Commit der APK

Nach erfolgreichem Test und Build wird die APK bei Variante B durch GitHub Actions selbst committed.

Der Commit enthält:

```text
[skip ci]
```

um eine Build-Schleife zu vermeiden.

## 5.5 Windows-Updater ergänzt

Neu:

```text
Tools/WindowsUpdater/
```

mit:

```text
Install-LFN-IN-Updater.cmd
Install-LFN-IN-Updater.ps1
Update-LFN-IN.cmd
Update-LFN-IN.ps1
settings.json
README.md
```

Der Windows-Updater ist heute nur noch als **optionaler Fallback** vorgesehen.

Er unterstützt unter anderem:

- neue ZIP finden,
- Version erkennen,
- Git prüfen,
- GitHub CLI prüfen,
- Pull,
- sichere Dateiübernahme,
- `.git` schützen,
- `Builds` schützen,
- Dry-Run,
- Commit,
- Push,
- GitHub-Actions-Lauf beobachten,
- nach erfolgreichem Build wieder synchronisieren.

## 5.6 Ausführliche Updater-Anleitung

Neu:

```text
UPDATER_ANLEITUNG.md
```

## 5.7 Neue Prüfdateien

Neu:

```text
BUILD_VERIFICATION_v0.4.61.txt
verify_v0461_updater.py
```

## 5.8 Versionsstand

```text
versionName = "0.4.61"
versionCode = 101
```

---

# 6. Änderungen in v0.4.62

## 6.1 Zentrale Build- und Workflow-Dokumentation

Neu:

```text
BUILD_WORKFLOWS.md
```

Diese Datei ist jetzt die zentrale Beschreibung der drei unterstützten Buildwege.

---

## 6.2 Buildweg A – Android Studio

Weiterhin unterstützt:

```text
PREPARE_PROJECT.bat
→ Projekt in Android Studio öffnen
→ Gradle Sync
→ Build / Run
```

`PREPARE_PROJECT.bat` wurde auf den neuen Namen und Versionsstand aktualisiert:

```text
LFN-Scout v0.4.62
```

Die Datei ist eine **Windows-Vorbereitung**, kein eigener vierter Buildweg.

---

## 6.3 Buildweg B – lokaler Gradle-Build unter Windows

Weiterhin unterstützt:

```text
PREPARE_PROJECT.bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

Auch die Ausgaben von:

```text
gradlew
gradlew.bat
```

wurden von:

```text
LFN-Scout
```

auf:

```text
LFN-Scout
```

umgestellt.

---

## 6.4 Buildweg C – Android-only

Neu verbindlich dokumentiert wurde der bevorzugte mobile Ablauf:

```text
ChatGPT Work
→ vollständige Projekt-ZIP
→ ZIP auf Android herunterladen
→ GitSync Pull
→ ZIP in temporären Ordner entpacken
→ alten Projektinhalt im lokalen Repository ersetzen
→ .git erhalten
→ Builds erhalten
→ GitSync Stage
→ Commit
→ Push
→ GitHub Actions
→ Unit-Tests
→ APK-Build
→ APK herunterladen
→ auf demselben Handy installieren
```

Dabei ist ausdrücklich festgelegt:

- jede neue Version wird als **vollständige Projekt-ZIP** geliefert,
- die ZIP enthält **keinen `.git`-Ordner**,
- `.git/` wird ausschließlich von Git/GitSync erzeugt und verwaltet,
- `Builds/` wird beim Ersetzen des Projektinhalts geschützt,
- vor jeder neuen Version erfolgt zuerst ein `Pull`.

---

# 7. Neue Entwicklungsdirektiven seit v0.4.58

`DIRECTIVES.md` wurde erweitert.

Neu festgelegt wurden insbesondere:

```text
12. Jede neue LFN-Scout-Version als vollständige Projekt-ZIP liefern.
13. Projekt-ZIPs enthalten niemals .git/.
14. Android-only ist der bevorzugte mobile Buildweg:
    Work → vollständige Projekt-ZIP → GitSync → GitHub Actions → APK → Installation.
15. BUILD_WORKFLOWS.md ist die verbindliche Buildbeschreibung.
```

---

# 8. Neu hinzugefügte Dateien seit v0.4.58

Insgesamt wurden 19 Dateien ergänzt:

```text
.github/workflows/build-android.yml
BUILD_VERIFICATION_v0.4.59.txt
BUILD_VERIFICATION_v0.4.60.txt
BUILD_VERIFICATION_v0.4.61.txt
BUILD_VERIFICATION_v0.4.62.txt
BUILD_WORKFLOWS.md
Builds/debug/.gitkeep
GITHUB_BUILD.md
Tools/WindowsUpdater/Install-LFN-IN-Updater.cmd
Tools/WindowsUpdater/Install-LFN-IN-Updater.ps1
Tools/WindowsUpdater/README.md
Tools/WindowsUpdater/Update-LFN-IN.cmd
Tools/WindowsUpdater/Update-LFN-IN.ps1
Tools/WindowsUpdater/settings.json
UPDATER_ANLEITUNG.md
verify_v0459_requirements.py
verify_v0460_requirements.py
verify_v0461_updater.py
verify_v0462_workflows.py
```

---

# 9. Geänderte vorhandene Dateien seit v0.4.58

Geändert wurden 17 vorhandene Dateien:

```text
CHANGELOG.md
DIRECTIVES.md
PREPARE_PROJECT.bat
README.md
app/build.gradle.kts
app/src/main/java/de/brummbeobachter/app/DebugLogger.kt
app/src/main/java/de/brummbeobachter/app/MainActivity.kt
app/src/main/java/de/brummbeobachter/app/ReminderNotifier.kt
app/src/main/java/de/brummbeobachter/app/RollingAudioService.kt
app/src/main/java/de/brummbeobachter/app/SeismicLiveService.kt
app/src/main/res/values/strings.xml
app/src/test/java/de/brummbeobachter/app/FaceliftThemePolicyTest.kt
app/src/test/java/de/brummbeobachter/app/ReminderScheduleGridPolicyTest.kt
gradlew
gradlew.bat
settings.gradle.kts
tools/gradlew-local-template.bat
```

Seit v0.4.58 wurden im direkten ZIP-Vergleich **keine Dateien entfernt**.

---

# 10. Versionsentwicklung

```text
v0.4.58
LFN-Scout
Ausgangsversion
        ↓
v0.4.59
GitHub Actions ergänzt
        ↓
v0.4.60
LFN-Scout / LFN-IN
CI-Testkorrekturen
        ↓
v0.4.61
APK-Ablage im Repository
Windows-Updater
        ↓
v0.4.62
verbindliche drei Buildwege
Android-only-Workflow vollständig dokumentiert
```

Versionscodes:

```text
0.4.58 → 98
0.4.59 → 99
0.4.60 → 100
0.4.61 → 101
0.4.62 → 102
```

---

# 11. Was bewusst unverändert blieb

Seit v0.4.58 wurden in den Versionen v0.4.59–v0.4.62 **keine grundlegenden Änderungen an der eigentlichen Mess-/Beobachtungslogik vorgenommen**.

Bewusst unverändert blieben insbesondere:

```text
applicationId = "de.brummbeobachter.app"
namespace     = "de.brummbeobachter.app"
```

sowie die bestehende Supabase-Grundstruktur.

Der Schwerpunkt dieser vier Versionen war:

```text
Buildbarkeit
CI
Tests
Versionsverwaltung
Namensumstellung
APK-Bereitstellung
Android-only-Workflow
Dokumentation
```

---

# 12. Aktueller Stand nach v0.4.62

Das Projekt kann jetzt auf drei Wegen gebaut werden:

## A – Android Studio

```text
PREPARE_PROJECT.bat
→ Android Studio
→ Build / Run
```

## B – Windows-Kommandozeile

```text
PREPARE_PROJECT.bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

## C – Android-only

```text
Work
→ vollständige Projekt-ZIP
→ GitSync
→ GitHub Actions
→ APK
→ Installation auf Android
```

Für den normalen mobilen Betrieb ist **C der bevorzugte Weg**.

A und B bleiben vollständig erhalten.


# 8. Änderungen in v0.4.64

## 8.1 Seismik auf echte FDSN-Wellenformdaten umgestellt

Der bisherige feste GEOFON-PNG-Pfad wurde entfernt. Er lieferte bei Stationen wie `GE.STU` eine Stationskarte und kein Seismogramm.

Neu:
- GPS-basierte Suche nach nahen vertikalen `...Z`-Kanälen über `fdsnws-station`,
- Abruf der letzten 60 Minuten über `fdsnws-dataselect`,
- MiniSEED-2-Decodierung für Integer, Steim1 und Steim2,
- native Android-Darstellung als Hüllkurven-Seismogramm,
- Fallback auf weitere nahe Z-Kanäle, wenn ein Kandidat keine aktuellen Daten liefert,
- detailliertes Logging für Datenabruf, Timeout, HTTP-, Verfügbarkeits- und Parsingfehler.

Regressionstests sichern insbesondere ab, dass kein statisches Stationskarten-PNG mehr als erfolgreicher Seismikplot verwendet wird.


# 9. Änderungen in v0.4.65

## 9.1 EIDA-Routing und lokaler Tagesplot

Die Seismik-Funktion wurde nach dem v0.4.64-Test weiter gehärtet. Der dort verwendete zwingende GEOFON-Availability-Vorabtest lieferte für aktuelle Kandidaten wiederholt HTTP 404 und verhinderte dadurch den Wellenformabruf.

Neu:
- Stations-/Kanalsuche über den ORFEUS EIDA Federator,
- Bestimmung des zuständigen Datenknotens über EIDA Routing,
- direkter FDSN-DataSelect-Abruf ohne zwingenden Availability-Vortest,
- ausgewählter Tag als lokales Tagesfenster,
- heute: 00:00 lokale Zeit bis jetzt,
- vergangene Tage: 00:00 bis 24:00 lokale Zeit,
- Sommer-/Winterzeit wird über die System-Zeitzone korrekt berücksichtigt,
- Datumsnavigation mit ◀, Datumsauswähler, ▶ und „Heute“,
- Zukunftsdaten gesperrt,
- neue Routing-/Tagesfenster-Logs und Anzeige des verwendeten EIDA-Datenknotens.

Die MiniSEED-Decodierung verwendet für längere Tagesabrufe nun eine speichereffizientere zweipassige Strategie mit direkt dimensioniertem `IntArray`.

Regressionstests prüfen Tagesgrenzen, DST-Rückstellung, Zukunftssperre und das Parsen von EIDA-Routing-Antworten.


# 10. Änderungen in v0.4.66

## 10.1 Direkte Stationssuche und Probeabruf

Der Praxistest von v0.4.65 zeigte wiederholte Timeouts bereits beim europaweiten Kanalabruf über den EIDA-Federator (`SEISMIC_TIMEOUT phase=channel_list`). Die Tagesfenster- und Datumsnavigation funktionierten dabei korrekt.

Neu:
- schnelle direkte `fdsnws-station`-Abfrage zuerst am BGR-EIDA-Knoten und anschließend bei GEOFON,
- ORFEUS-Federator nur noch als Fallback, wenn direkte Quellen keine Z-Kanal-Kandidaten liefern,
- quellenübergreifende Deduplizierung und Sortierung nach Entfernung/Kanalpriorität,
- EIDA-Routing erst für wenige nahe Kandidaten,
- kurzer DataSelect-Probeabruf vor dem vollständigen Tagesdownload,
- bei langen Fenstern fünf Minuten Abstand zum aktuellen Fensterende, um typische Echtzeit-Latenzen zu berücksichtigen,
- zusätzliche Diagnose-Logs für Quellenwahl und Probeabruf.

Unverändert bleiben:
- heute: 00:00 lokale Zeit bis jetzt,
- vergangene Tage: 00:00 bis 24:00 lokale Zeit,
- ◀ / Datumsauswahl / ▶ / Heute,
- Zukunftssperre und DST-sichere lokale Tagesgrenzen.


# 11. Änderungen in v0.4.67

## 11.1 Steim-Decoder und Routing-Härtung

Der Praxistest von v0.4.66 zeigte erstmals erfolgreich geroutete und geladene GEOFON-MiniSEED-Daten für `GE.STU`, die jedoch beim Dekodieren mit Meldungen wie `Steim decoded 289/340 samples` verworfen wurden. Die Ursache lag in einer um ein Wort verschobenen Auswertung der 2-Bit-Steim-Kontrollnibbles.

Neu:
- Steim1/Steim2-Kontrollnibble gemäß SEED-/libmseed-Referenz korrigiert (`30 - 2 * wordIndex`),
- bestehende künstliche Steim-Testdaten auf korrekte Kontrollwortbelegung umgestellt,
- zusätzlicher Mehrframe-Steim1-Regressionstest,
- `SEISMIC_ROUTE_NONE` beendet die Prüfung des Kandidaten sofort, ohne anschließenden Federator-DataSelect-Versuch,
- Federator-Fallback bleibt nur für Kandidaten mit mindestens einer gültigen EIDA-Route erhalten,
- BGR-TLS-Fehler bleibt nicht-blockierend; keine unsichere Zertifikatsausnahme.

Unverändert bleiben die lokale Tagesdarstellung 00:00 bis jetzt bzw. 00:00 bis 24:00 für historische Tage und die Navigation ◀ / Datum / ▶ / Heute.

### v0.4.68 – regionales WBBU-Sonderverfahren und klassischer 24-h-Plot
- Klassischer 24-h-Seismikplot: 48 Zeilen à 30 Minuten, abwechselnd blau/rot.
- WBBU-Sonderverfahren ausschließlich innerhalb 30 km um Bürstadt und nur für den aktuellen Tag: aktuelles HLNUG-Tagesseismogramm.
- Für vergangene Tage und außerhalb der Sonderzone gilt weiterhin das Näheprinzip mit tatsächlich verfügbaren offenen EIDA/FDSN-Rohdaten.
- Valencia und andere Standorte: keine Sonderstation, rein dynamische Nähe-/Verfügbarkeitsauswahl.


# 11. Ergänzung v0.4.69 – WBBU-vergleichbare Seismik-Y-Skalierung

- 48 Halb­stundenzeilen und Blau/Rot-Helicorder bleiben erhalten.
- FDSN-Kanalmetadaten `Scale`, `ScaleFrequency`, `ScaleUnits` werden ausgewertet.
- Bei Geschwindigkeitseinheiten wird die Darstellung sensitivitätsbasiert in `nm/s` skaliert.
- WBBU-Referenz aus dem bereitgestellten Plot: 40000 nm/s ≈ 4,95 Zeilenabstände.
- Fehlende/unpassende Kalibrierung führt zu einem klar gekennzeichneten Counts-Fallback.
- Keine Behauptung einer vollständigen frequenzabhängigen Response-Deconvolution.

# 12. Ergänzung v0.4.70 – Compose-Build-Fix Helicorder

- Fehlenden `androidx.compose.foundation.layout.Box`-Import für die Zeitbeschriftung des klassischen Seismik-Helicorders ergänzt.
- Beschriftungsschleife der 48 Halb­stundenzeilen von `forEach` auf eine direkte Kotlin-`for`-Schleife umgestellt, damit `Box` und `Text` sicher im bestehenden `@Composable`-Kontext ausgeführt werden.
- Neuer Regressionstest `SeismicHelicorderComposeContractTest.kt` verhindert eine Wiederholung beider Compilerfehler.
- Keine funktionale Änderung an WBBU-Sonderlogik, Näheprinzip, Datenabruf, MiniSEED-Decoder, 30-Minuten-Zeilen, Blau/Rot-Darstellung oder sensitivitätsbasierter nm/s-Skalierung.


# 13. Ergänzung v0.4.77 – gemeinsamer Speicherweg und scheli-artiger Helicorder

- Der gemeinsame `Plot speichern`-Button liegt vor der WBBU/Nähe-Verzweigung und ist damit für beide Plotarten sichtbar.
- WBBU speichert bytegenau das aktuelle HLNUG-PNG; Näheplots verwenden weiterhin den eigenen PNG-Renderer.
- Der eigene Näheplot rendert nun pro 30-Minuten-Zeile eine zusammenhängende, peak-erhaltende Helicorder-Spur.
- Die scheli-nahe Grundgeometrie ist explizit festgelegt: 48 Zeilen, 1800 s pro Zeile, alternierende Rot/Blau-Spuren und eine gemeinsame feste Amplitudenskala.


# 14. Ergänzung v0.4.78 – gemeinsames Master-Bitmap für Helicorder

Der selbst erzeugte Nähe-Helicorder verwendet ab v0.4.78 nur noch einen Renderer. Dieser erzeugt ein Master-Bitmap mit 3072×1728 Pixeln. Uhrzeiten, X-Achse, Maßstab und Seismogrammspuren werden in dieses Bitmap gezeichnet. Die Android-Oberfläche skaliert und zoomt dieses Bild; beim Speichern wird dasselbe Bitmap verwendet. Dadurch können Anzeige und Export nicht mehr voneinander abweichen. WBBU behält sein originales HLNUG-PNG und besitzt einen eigenen sichtbaren Speicherbutton im Plot-Card.

## v0.4.81
- Seismik: gespeicherte Dateinamen enthalten nun zusätzlich die lokale Speicher-Uhrzeit bis auf Sekunden.
- Seismik: das jeweils bereits fertige WBBU- bzw. Näheplot-Bitmap wird vollständig um 90° gegen den Uhrzeigersinn gedreht; interne Beschriftungen werden nicht separat neu angeordnet.
- Entwicklermodus: ganzzahlige Näheplot-Messwertverstärkung 1–20, persistent, Standard 2. Die feste Y-Skala bleibt unverändert.
- Wasserfall: bisherige automatische Min/Max-Pegelbestimmung bleibt Standard. Optional kann im Entwicklermodus ein manueller Bereich über Min/Max-Referenzwerte bei 50 dB Gain gewählt werden; bei kleinerem Gain verschiebt sich der komplette Bereich um `Gain - 50 dB` ins Negative.
- Normale Audioaufnahme: während der laufenden 120-s-Aufnahme wird ein großer Restsekunden-Countdown mit direkt zugänglichem Abbruchbutton angezeigt. Rolling-Buffer bleibt davon unberührt.
