# LFN-Scout v0.5.04.1 – Buildkorrektur und Prüfstatus

## 1. Tatsächlich vorliegender Fehlernachweis

Der vom Nutzer bereitgestellte Windows-Lauf `gradlew.bat :app:testDebugUnitTest --stacktrace` für v0.5.04 scheiterte bei `:analyzer-core:test` (40 Tests, 2 FAILED: `independentDecimationAndFftHop`, `waterfallTimebaseIndependentOfFftHop`; `IllegalArgumentException`) sowie bei `:app:compileDebugKotlin` (`LiveAnalyzerController.kt:545–546`: Smart-Cast von `savedRange` in mutierender Closure unmöglich). **v0.5.04: Windows-Gradle FAIL.**

## 2. Korrektur v0.5.04.1

- Autorange-Ergebnis einmal als `val result = collector.result()` lesen, anschließend `savedRange = result`, nur unveränderliches `result` auf `.first`/`.second` prüfen. Autorange-Verhalten soll unverändert bleiben.
- Historische JUnit-Schleifen auf die zulässigen 1-kHz-FFT-Hops 5/10/50/100/200/500/1000 einschränken. Neue 48-kHz-Tests für die `allowedWidebandHops` 480/960/2400/4800 mit der tatsächlichen Abtastrate 48000 Hz. Dies behebt die veraltete Testannahme; der Produktionstiming-Code ist unverändert.
- Version `0.5.04.1` / Code `146`, unveränderte Android-Paket-ID. Keine Änderungen am Rolling-/48-kHz-Audiopfad und am WAV-Verhalten.

## 3. Prüfmatrix

| Prüfung | Status |
|---|---|
| Eingegangener Nutzer-Windows-Build v0.5.04 | **FAIL** (siehe oben) |
| v0.5.04.1 Host v0.5.04 / v0.5.03 / v0.5.02 | **PASS 16/16, 20/20, 16/16** (Kotlin/JVM ohne Android SDK) |
| Korrigierte `AnalyzerTimingTest`-Methoden mit testlokalem JUnit-Stub | **HOST PASS 5/5**; ersetzt keinen Gradle-JUnit-Lauf |
| v0.5.04.1 statischer Quellcodevertrag | **PASS 20/20**; keine Compose-Kompilierung |
| v0.5.04.1 `:app:testDebugUnitTest` / `:app:compileDebugKotlin` / `assembleDebug` | **NOT RUN** in dieser Umgebung, Android-SDK fehlt |
| Xiaomi Audio-Modi, STOP+Verlassen, Export | **NOT RUN** für v0.5.04.1 |
| 16:1-Pegelabweichung | **OFFEN** |

## 4. Nächste Schritte Windows

Im **neu entpackten** v0.5.04.1-Projekt `PREPARE_PROJECT.bat` ausführen und dann:

```powershell
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Bei Erfolg die APK per In-place-Update installieren und anschließend `docs/project/TESTPLAN_v0.5.04.md` einschließlich Rolling-Zustandsmatrix abarbeiten. Keine Gerätefreigabe allein aufgrund der Hosttests.
