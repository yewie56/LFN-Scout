# LFN-Scout v0.4.87 – Highspeed-Diagnose / Java 17

## Ist-Stand
Die Diagnosekarte ist **nur bei aktiviertem Entwicklermodus** in „Zusatzfunktionen“ sichtbar. START abonniert zwei asynchrone Verbraucher des bestehenden unverstärkten Roh-PCM-Hubs: einen Rohblock-Monitor und den gemeinsamen `HighspeedImpulseSession`. STOP/Verlassen schließt beide Abonnements. **Kein weiterer `AudioRecord`, keine WAV-/PNG-Speicherung.**

Die Karte zeigt tatsächliche PCM-Abtastrate (heute 8 kHz), Quelle, seit Start größten Rohsample-Peak, Ereigniszähler, letzten Ereignispeak und Dauer sowie Block-, Drop-, Reset- und Fehlerzähler. Bei ausgeschaltetem Rolling-Buffer und ohne Aufnahme wird auf vorhandene PCM-Daten gewartet. Neue Detektion bei 48 kHz ist ausschließlich synthetisch im Core getestet: **Hardware-48-kHz-Capture fehlt weiterhin.** Die Diagnose ist kein geeichtes Schalldruckmessgerät.

## Windows-Buildvorbereitung
`PREPARE_PROJECT.bat` prüft ein vollständiges JDK 17. Suchreihenfolge: Projektlokales JDK → Android-Studio-JBR → von IDE/Gradle verwaltete JDKs → systemweit installierte JDKs → gültiges JAVA_HOME → älterer Cache → frühere Projekte → notfalls lokaler Download. Für Gradle-Daemon und Toolchain werden `org.gradle.java.home` und `org.gradle.java.installations.paths` nur lokal gesetzt. Die PowerShell-Variable heißt `$jdkHome`, nicht `$home`. Globale Java- und IDE-Einstellungen bleiben unverändert.

## Testablauf auf Windows und Xiaomi
1. Neue ZIP in neuen Ordner entpacken; `PREPARE_PROJECT.bat` ausführen.
2. `python verify_v0487_jdk.py`, `python verify_v0487_diagnostic.py`, `python verify_analyzer_highspeed.py`, `python verify_reconstruction.py` ausführen.
3. `gradlew.bat :analyzer-core:test`, `gradlew.bat :app:testDebugUnitTest`, `gradlew.bat assembleDebug` ausführen.
4. In Android Studio die neue Version installieren, Bestandsaufnahme (Rolling, WAV, Wasserfall) auf Funktion prüfen.
5. Entwicklermodus aktivieren, „Zusatzfunktionen“ → Highspeed-Impulsdiagnose → START; Rolling-Buffer starten (oder normale Aufnahme, nicht beides). Auf 8000 Hz und steigende Rohblöcke achten. Leichte reproduzierbare Testimpulse nahe dem Mikrofon erzeugen **ohne auf das Gerät einzuschlagen**. Peaks/Ereigniszahl beobachten. STOP/Panelwechsel testen.
6. Drop-/Fehlerzähler und App-Diagnoselog dokumentieren; Gerätetest kann nur vom Nutzer als PASS bestätigt werden.

**Offene Gates:** Android-Kompilierung und reale Xiaomi-Messung dieser Version; genauer Pegelabgleich und Impuls-Bandbreite; 48-kHz-Mikrofonumschaltung; vollständiges Analyzer-Panel und Standalone-App; Offscreen-PNG.
