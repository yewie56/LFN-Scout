# LFN-Scout v0.4.96 – Test- und Buildbericht

Datum 20.09.2026. Baseline: vollständiges Projekt `LFN-Scout_Android_v0.4.95.1.zip` (versionCode 136). Neuer Stand: versionName **0.4.96**, versionCode **137**, unveränderte Installations-ID `de.brummbeobachter.app`.

## Geänderter Funktionsumfang

1. Autorange durch Berühren der linken Spektrum-dBFS-Achse: 2 s (einstellbar), feste Reserve je Seite ±5 dB (einstellbar), gültige FFT-Bins ohne DC/Leersignal, beide ermittelten Grenzen persistent. Manuelle Presets und Setup-Slots 1–3 bewahren auch individuelle Grenzen.
2. L50/L90 mit dünnem roten, ungefüllten Rahmen mit runden Ecken oben rechts; Berühren setzt 120-s-Statistik und exponentielle FFT-Mittelung zurück, ohne die Wasserfallhistorie oder WAV/PCM zu löschen.
3. Per Finger verschiebbarer Cursor. Readout an oberer Spitze, automatische Links-/Rechtsplatzierung; Berühren des Readouts schaltet den Cursor ab. Keine zusätzliche Cursorzeile.
4. Dünnere Spektralkurven, größeres Spektrum; Wasserfall-Titel und redundante Zeile entfernt. Farbskala links mit Zahlen **in** der Leiste; Frequenzachsen beider Plots in Flucht, eigenständige Hz-Einheit; Offscreen-PNG entsprechend.

## Ausgeführte Prüfungen

| Prüfung | Ergebnis | Einschränkung |
|---|---|---|
| `kotlinc` für alle `:analyzer-core`-Hauptdateien | PASS | JVM-Compiler; kein Android-SDK |
| Host-Selbsttest `tools/AnalyzerDisplayRangeHostSelfTest.kt` | **5/5 PASS** | Rechenregeln und Average-Reset |
| 33 JVM-Testmethoden aus den sechs `analyzer-core/src/test/...`-Klassen | **33/33 PASS** | Ausgeführt durch temporären lokalen JUnit-kompatiblen Reflection-Runner (nicht den originalen Gradle/JUnit-Runner); Original-Testquellen unverändert, außer 5 neue Tests |
| `python verify_v0496_analyzer_contract.py` | **17/17 PASS** | Statische Quelltextkontrollen; kein Ersatz für Android-Compile |
| Kotlin-Quellen auf offensichtliche Syntaxdiagnosen gesichtet | Keine `expecting`-/`unexpected tokens`-Meldungen | Mangels Android/Compose-Classpath viele erwartete `unresolved reference`-Meldungen; daraus folgt **kein** erfolgreicher Android-Compile |
| Android `:app:testDebugUnitTest` / `assembleDebug` | **NOT RUN** | Android-SDK/Gradle-Abhängigkeiten in Laufzeit nicht vorhanden |
| Xiaomi-Gerätetest: Autorange, Cursor, Reset, PNG und App-Rest | **NOT RUN** | Nur Nutzergerät kann UI-/FPS- und Speicherverhalten nachweisen |
| Vollständige Projekt-ZIP | **PASS** | ZIP-CRC aller 591 Dateien; 590 SHA-256-Manifestdateien verifiziert; keine `.git/`-Einträge; 53 WAV-Dateien binär identisch zur Ausgangsversion; 13 Konzept-PNGs vorhanden |

## Windows-/Xiaomi-Abnahme (noch auszuführen)

1. ZIP in eigenen neuen Ordner entpacken, `PREPARE_PROJECT.bat` starten. `gradlew.bat :app:testDebugUnitTest --stacktrace`, anschließend Android Studio Run ▶; auf **BUILD SUCCESSFUL** achten.
2. Rolling-Buffer starten und Live-Analyzer öffnen; prüfen, ob Live-Kurve/Wasserfall wie in v0.4.94 laufen und die gemeinsamen Hz-Achsen passen.
3. Linke dBFS-Achse antippen: nach ca. 2 s neue Grenzen beobachten, prüfen, ob `−5 dB` bzw. `+5 dB` auf das gemessene Min/Max angewendet wurden; Analyzer schließen/öffnen und Grenzen auf Persistenz prüfen. Erfassungsdauer/Reserve verändern und erneut testen. Test ohne Live-Signal darf die Grenzen nicht verändern.
4. Roten Rahmen antippen: L50/L90 zunächst `–`, Mittelwert startet neu; Wasserfall-/Rolling-Puffer laufen weiter. Cursor setzen, ziehen, durch Tipp auf Readout ausschalten.
5. Manuelle PNG und Auto-PNG prüfen: Zahlen im Farbbalken links, Spektrum-/Wasserfall-Hz-Ticks exakt vertikal in Flucht, Zeitzahlen unverändert. Speichereinstellungen und drei Setup-Slots testen.

## Datenschutz / Auslieferung

Die Voll-ZIP enthält dieselben privaten Supabase-Recovery-Daten wie die Baseline; **nicht öffentlich auf GitHub stellen**. `.git/` darf nicht im ZIP enthalten sein. Vor finaler Lieferung ZIP-Integrität, SHA-Manifest und unveränderte WAVs prüfen. Dieser Bericht behauptet keine erfolgreiche Android-/Gerätefreigabe.
