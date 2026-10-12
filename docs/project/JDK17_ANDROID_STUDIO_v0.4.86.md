# LFN-Scout v0.4.86 – Java 17 und Android Studio vorbereiten

**Status:** Neue Windows-Vorbereitung implementiert; tatsächlicher Android-Studio-Sync und v0.4.86-APK-Build müssen auf dem Windows-PC bestätigt werden. Der erfolgreiche v0.4.85-Build ist nur die Ausgangsregression.

## 1. Fehlerursache und Ziel

Android Studio konnte im v0.4.85-Projekt die Java-17-Toolchain für `:analyzer-core:compileJava` nicht finden (`Undefined Toolchain Download Repositories`). PowerShell startete dagegen über den speziellen `gradlew.bat` mit einem aus einem älteren Projekt entdeckten JDK 17 und konnte erfolgreich kompilieren.

`JAVA_HOME` eines gestarteten Batch-Prozesses gilt nicht automatisch für die bereits gestartete Android-Studio-IDE. Deshalb konfiguriert `PREPARE_PROJECT.bat` nun **beide** Gradle-Eigenschaften:

- `org.gradle.java.home`: JDK des Gradle-Daemons.
- `org.gradle.java.installations.paths`: expliziter JDK-17-Suchpfad für die Java-/Kotlin-Toolchain im separaten Modul `:analyzer-core`.

Dies erfolgt **nur** in den lokalen Dateien des entpackten Projekts; globale Java-Umgebungsvariablen und persönliche IDE-Einstellungen bleiben unverändert. Es wird ein vollständiges JDK (`java.exe` **und** `javac.exe`) geprüft; bei Bedarf wird Temurin 17 in `.tools` bezogen.

## 2. Ablauf auf deinem Windows-11-PC

1. ZIP `LFN-Scout_Android_v0.4.86.zip` in einen **neuen** Ordner unter `AndroidStudioProjects` entpacken, etwa `LFN-Scout_Android_v0.4.86`. ZIP nicht über einen laufenden oder geänderten Projektordner kopieren.
2. `PREPARE_PROJECT.bat` im eigentlichen Projektordner doppelklicken (oder in PowerShell ` .\PREPARE_PROJECT.bat`). Warte auf `PASS: Projekt vorbereitet.`. Das Skript sucht JDK 17 und SDK, schreibt die beiden JDK-Eigenschaften und stellt den Gradle-8.13-Wrapper wieder her.
3. Android Studio: **File → Open**, den **neuen** Projektordner wählen. `File → Sync Project with Gradle Files` und den Sync abwarten.
4. Im Projektordner PowerShell öffnen. Zuerst `python .\verify_v0486_jdk.py`, danach `python .\verify_analyzer_highspeed.py`, dann ` .\gradlew.bat :analyzer-core:test --stacktrace`, ` .\gradlew.bat :app:testDebugUnitTest --stacktrace`, zuletzt ` .\gradlew.bat assembleDebug --stacktrace` ausführen. Nur bei erfolgreichen Tests weiterbauen.
5. Danach das Xiaomi in Android Studio wählen und über **Run ▶** installieren. Eine Debug-APK kann nur bei passender Signatur über eine bestehende Installation installiert werden. **Vor einem eventuellen Deinstallieren App-Daten sichern.**

**Hinweis zur PowerShell:** Die Leerstelle vor einigen Befehlen dient nur dem Textlayout; die Befehle beginnen direkt mit `.`.

## 3. Prüfen, was PREPARE geschrieben hat

Unter `gradle.properties` stehen danach genau einmal (Windows-Pfad nur beispielhaft):

```properties
org.gradle.java.home=C:/Users/user/AndroidStudioProjects/BrummBeobachter_Android_v0.4.31/.tools/jdk17/current
org.gradle.java.installations.paths=C:/Users/user/AndroidStudioProjects/BrummBeobachter_Android_v0.4.31/.tools/jdk17/current
```

`local.properties` erhält den **aktuellen lokalen** SDK-Pfad. Beide Dateien sind lokale Konfigurationsdateien; insbesondere die `org.gradle.java.home`- und `org.gradle.java.installations.paths`-Zeilen dürfen nicht mit einer fremden Windows-Installation in andere Projektpakete kopiert werden. `.tools/java17.path` wird ebenfalls lokal erzeugt und ist durch `.gitignore` aus Git ausgeschlossen. **Vor Git Commit/Push unter Windows:** Die beiden durch PREPARE erzeugten JDK-Zeilen in `gradle.properties` nicht mitcommitten (diese Datei ist im bestehenden Repository versioniert). Für den Android-only-Weg die ungeänderte Liefer-ZIP verwenden.

## 4. Falls Android Studio weiterhin denselben Fehler meldet

1. Prüfen, ob `PREPARE_PROJECT.bat` `PASS: Projekt vorbereitet.` gemeldet hat. Falls nicht, den ersten konkreten Fehler mitteilen.
2. Android Studio: `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK`. Prüfen, welches JDK eingestellt ist. Wird weiterhin ein anderes JDK verwendet, dort das lokal gefundene JDK 17 eintragen oder auswählen. Dies ist ein **Fallback**, kein notwendiger Standardschritt.
3. Erneut synchronisieren. Falls die Fehlermeldung bleibt, den vollständig aufgeklappten Fehler mitsamt den beiden JDK-Eigenschaften (Pfade nur, keine Zugangsdaten) mitteilen. Nicht pauschal Gradle-/Android-Plugin aktualisieren, nicht `ignoreFailures` setzen.
4. Wird SDK Platform 36 vermisst, in Android Studio `Tools → SDK Manager` installieren. Dieser Fehler ist vom JDK-Toolchain-Problem getrennt.

## 5. Unveränderte Funktionen und Tests

Die App-ID `de.brummbeobachter.app` sowie das 8-kHz-Capture, die 1-kHz-WAV-Aufnahme, Seismik-Code, Bewertung, Supabase und der Highspeed-Detektor wurden nicht verändert. Der Core kann weiterhin später in einer Standalone-App verwendet werden. Eine v0.4.86-APK darf erst nach dem bestätigten Build als verfügbar gelten.

**Privates Projekt-ZIP:** Enthält weiterhin vertrauliche Supabase-Recovery-Dumps und 53 WAV-Backups. Nicht vollständig öffentlich weitergeben oder auf GitHub hochladen.
