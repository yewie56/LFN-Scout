# LFN-Scout v0.4.92 – Testkorrektur und Buildbericht

**Stand: 2026-09-20 · Quellcode-Korrekturstand · Android- und Xiaomi-Test: NOT RUN für v0.4.92.**

## Befund

Unter Windows schlug `:analyzer-core:test` in v0.4.91 fehl: 24 Tests, 1 Fehler. Die `NoSuchElementException` in `RawLfDecimatorTest.zeroInputProducesZeroAndNotPlausibleLevels()` entstand durch `.last()` auf einer leeren Spektrumsliste. Der Test lieferte 8.200 Samples bei 8 kHz; daraus entstehen nur etwa 1.000 LF-Samples. `LiveAnalyzerCore` emittiert erst nach mindestens 4.096 LF-Samples ein Spektrum.

## Korrektur

- `RawLfDecimatorTest.kt`: Prüft nun explizit, dass im Startup noch **kein** FFT-Ergebnis ausgegeben wird, füttert danach weitere 32.800 Rohsamples in **dieselbe** Filter- und FFT-Instanz, verifiziert eine erzeugte Spektrumsliste und die Nulleingang-Pegelgrenze.
- **Keine** Änderung an `RawLfDecimator.kt`, `LiveAnalyzerCore.kt`, Recorder, Rolling oder den Android-UI-Quellen.
- Version `0.4.92` / `versionCode 132`; Paketkennung unverändert.

## Verifikation

- Quelltextprüfung der korrigierten Testbedingung: PASS.
- Die vier Methoden von `RawLfDecimatorTest` mit dem produktiven Kotlin-Kern lokal durch Aufruf der tatsächlichen Testmethoden und minimalen JUnit-Assertion-Stubs ausgeführt: 4/4 PASS. Dies ist **kein** vollständiger Gradle-JUnit-Lauf. Die vorhandenen synthetischen Live491- und Phase2-Selbsttests: PASS. 
- `:analyzer-core:test`, `:app:testDebugUnitTest`, `assembleDebug` und Xiaomi-Gerätetest: **NOT RUN** auf Windows/Android; nachholen.
- `verify_v0492_live_analyzer.py`: statischer Integrations-/Dateierhaltungstest; Ergebnis siehe gesonderte Ausführung.
- ZIP-Integrität, Manifest-Prüfung und WAV-Backup-Vergleich mit v0.4.91: beim Verpacken geprüft.

## Testablauf Windows

Im **neuen** Projektordner zuerst `PREPARE_PROJECT.bat`, danach:

```powershell
.\gradlew.bat :analyzer-core:test --stacktrace
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Nach erfolgreich ausgeführten Tests und Build über Android Studio installieren. Erst dann prüfen, ob die Live-Kurve und der Wasserfall auf dem Xiaomi sichtbar sind. Das bislang unbestätigte Gerätethema von v0.4.90 ist durch diese reine Testkorrektur **nicht** gelöst oder bewiesen.

**Datenschutz:** Private Supabase-Recovery-Daten sind im Gesamtprojekt enthalten. Nicht unverändert öffentlich auf GitHub hochladen.
