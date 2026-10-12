# LFN-Scout v0.5.45.9 – 1/f-Instrumentierung

## Ziel und reproduzierter Befund
- Benutzerbefund: NOISE AUS zeigt über 30 Sekunden ein stabiles Spektrum.
- Mit NOISE 1/f zeitweilig kein verwertbares Eingangssignal, kurzzeitiges Spektrum und erneutes Verschwinden.
- Verdacht im Code: `NoisePowerModel.reference()` führt `fit()` in **jedem** Main-FFT-Fenster und **jedem** Rolling-FFT-Fenster erneut aus. Der Fit probiert 9 White-Floor-Kandidaten und 57 Alpha-Kandidaten mit mehreren Listen-/Logarithmusdurchläufen aus. Das ist ein untersuchungswürdiger Performance-Pfad, nicht bereits die bewiesene Ursache.

## Änderungen
- `NoiseFloorReference.kt`: getrennter zeitlicher Zähler für Main und Rolling, Anzahlen `frames`, `fits`, Mittel-/Maximaldauer des Fit, Mittel-/Maximaldauer der Verarbeitung; Gesamtzahl und vollständig wegsubtrahierte Bins/Fenster; ungültige Modellwerte; Modell-Fallbacks und Alter des letzten Aufrufs. Verwendung von `System.nanoTime()`; keine Dateispeicherung im PCM/FFT-Verarbeitungsweg.
- `LiveAnalyzerScreen.kt`: Im bestehenden `DIAG`-Dialog wird die Laufzeitdiagnose jede Sekunde aktualisiert und kann mit **Laufzeitzähler zurücksetzen** zurückgesetzt werden. Zusätzliche Anzeige von `PCM-Alter`, FFT-Zähler, Signalstatus.
- Keine Änderung des mathematischen 1/f-Modells, um den Fehler unverfälscht reproduzieren zu können. Ungültige Referenzbins werden als Originalwert belassen (Fail-safe).
- `app/build.gradle.kts`: Version 0.5.45.9, versionCode 202; GitHub-Testpaket nur mit `-PlfnGithubTestApp=true`, Windows-Build ohne Flag bleibt regulär. Manifest-Label für TEST separat.
- `.github/workflows/build-android.yml`: Wiederherstellung der inzwischen erfolgreichen GitHub-Buildwege TEST (Push) und REGULAR (manuell); Keystore per GitHub-Secrets und explizites Signieren mit `apksigner`; Signatur- und Paketprüfung. Secrets werden NICHT mitgeliefert.

## Reproduzierbarer Test auf dem Smartphone
1. GitHub-Push: automatischen TEST-Build grün prüfen. TEST-APK installieren.
2. NOISE AUS aktivieren. Messung starten, 30 Sekunden Spektrum beobachten.
3. `DIAG` öffnen, **Laufzeitzähler zurücksetzen**, NOISE 1/f einschalten. DIAG erneut öffnen; 30–60 Sekunden beobachten.
4. Main- und Rolling-Werte vor und nach dem Ausfall dokumentieren: `fit_avg`, `fit_max`, `total_max`, `age`, `cancelled`, `fallback`, `invalid_ref`; zusätzlich `PCM-Alter`, `FFT-Zähler`, `Status`.
5. Bei stark anwachsender Main-/Rolling-Fit-Laufzeit spricht dies für Rechenüberlast. Bei `cancelled` nahe 100% ohne stark erhöhte Laufzeit eher für Subtraktion/Signalstatus. Bei wachsendem `PCM-Alter` muss der tatsächliche Datenfluss separat geprüft werden.
6. Erst nach Diagnose gezielte Cache-/Performance-Korrektur vornehmen. Keine Rückschlüsse nur aus einem einzelnen Zähler.

## Regression/Build
- Repository-Tests: `./gradlew testDebugUnitTest` (durch GitHub Actions).
- `./gradlew assembleDebug -PlfnGithubTestApp=true` für TEST; regulärer `assembleDebug` für Windows.
- Automatische Signaturprüfung muss bei REGULAR den SHA-256-Wert `7e4342fb19302cbf2ca8eff4a2cfbb91a04a0bc571ff188980d15101fac6e987` bestätigen.
- Die Windows-.bat-Skripte und lokale Java-17-Konfiguration sind unverändert.
- Keine Android- oder Gradle-Ausführung in dieser Erstellungsumgebung möglich: tatsächlicher Compile-/Gerätetest erfolgt durch GitHub Actions und den Benutzer.

## Grenzen
- DIAG-Zähler sind **flüchtig** und werden nicht automatisch als CSV gespeichert; mittels **Diagnose kopieren** können sie in die Android-Zwischenablage übernommen und in eine Notiz eingefügt werden. Das ist bewusst ein manuell ausgelöster Export, um das Echtzeitverhalten während der Fehlersuche nicht durch Dateizugriffe zu verändern.
- Telemetrie misst Aufrufzeit unter Synchronisierung einschließlich Fit und Array-Verarbeitung; sie liefert keine CPU-Profilierung und ist keine vollständige AudioRecord.read-Diagnose.
- ZIP stammt aus v0.5.45.8 und enthält noch nicht automatisch zusätzliche, nur in Termux/GitHub ausgeführte Änderungen. Der dokumentierte TEST/REGULAR-Workflow wurde anhand des zuletzt in der Unterhaltung bestätigten erfolgreichen Workflows rekonstruiert. Vor dem Push ggf. mit `git diff` prüfen, um weitere zwischenzeitliche GitHub-Änderungen nicht zu überschreiben.
