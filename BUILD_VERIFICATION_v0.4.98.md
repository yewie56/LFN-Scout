# LFN-Scout v0.4.98 – Build-Verifikation

Stand: 20.09.2026. Baseline: vollständige v0.4.97. Version: 0.4.98 / code 139.

| Prüfung | Ergebnis | Aussage |
|---|---|---|
| Kotlin-Hosttest `RUN_ANALYZER_WATERFALL498_SELFTEST.sh` | PASS 6/6 | Aktuell, Leistungsmittel, Maximum, Flush/Reset und 250-ms-Timing |
| Statische Integrationsprüfung | PASS 13/13 | Persistenz, Setup, Default, Controller, PNG, Diagnose-Callback, Versionscode |
| Neue JUnit-Klasse `WaterfallAggregatorTest.kt` | HINZUGEFÜGT, Gradle NOT RUN | 5 Regressionstests; durch Android-Studio-/Windows-Gradle überprüfen |
| Altbestand `:analyzer-core:test` / `:app:testDebugUnitTest` | NOT RUN | Bereits vorher getesteter Stand ist keine Freigabe dieser Version |
| `assembleDebug` / Compose-Kompilierung | NOT RUN | In dieser Umgebung kein Android-SDK |
| Xiaomi: Moduswahl / PNG / Performance / Setup-Recall | NOT RUN | Muss am Endgerät geprüft werden |
| ZIP-CRC, aktuelles SHA256-Manifest, private Recovery-WAV-Dateien | wird vor Übergabe geprüft | Ergebnisse im externen Testbericht |

## Windows-Abnahme

1. Projekt-ZIP vollständig separat entpacken, `PREPARE_PROJECT.bat` starten. `./gradlew.bat :app:testDebugUnitTest --stacktrace` ausführen; bei PASS Android Studio Run ▶.
2. Im bisherigen Register **Anzeige** Wasserfall-Auswertung Aktuell/Mittelwert/Maximum wechseln, jeweils 120 s neu füllen. Bei lautem kurzen Testsignal Änderungen gegenüber dem Default prüfen. Der Moduswechsel darf weder rote Spektrum-Average-Kurve noch L50/L90 zurücksetzen.
3. Auswahl im Setup speichern, App-/Analyzer-Neustart und Setup-Laden prüfen; bei altem Setup ohne Modus erwartet Default Aktuell.
4. Manuell und automatisch PNG prüfen: sichtbarer Modus, echtes 120-s-Historienfenster und keine modusunterschiedlichen Zeilen in einem Bild. Panelwechsel, Rolling-Buffer/WAV und CPU bei FFT-Hop 10 gesondert prüfen.

**Diese Version ist Testkandidat, keine freigegebene Android-APK.** Die neue Settings-Navigation ist nur beschrieben, nicht umgesetzt.
