# Analyzer Phase 1 – Verifikationsprotokoll

Datum: 19.09.2026. **HISTORISCHER PHASE-1-Teststand.** Seit Phase 2 liegt derselbe Core unter `analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt`. Das aktuelle Phase-2-Protokoll steht unter `docs/project/ANALYZER_PHASE2_AUDIO_STANDALONE.md`.

| Test | Status | Nachweis/Grenze |
|---|---|---|
| Reiner Kotlin-Core mit `kotlinc` kompilieren | PASS | `kotlinc …/LiveAnalyzerCore.kt -d core.jar` |
| Isolierter Testhost verwendet dieselbe Core-Klasse | PASS | `kotlin -classpath core.jar tools/analyzer_core_selftest.kts` |
| 10-Hz-Sinus -> korrekter FFT-Peak und ~-6 dBFS | PASS | deterministische 1000-Hz/1024-FFT-Fixture |
| Gleiches PCM in 1 Block / 37-Sample-Chunks | PASS | identische FFT-Folgen/Ergebnisse |
| Input-Clip, endliche Silence-Floor, MaxHold/Reset | PASS | deterministische Fixture |
| Android-JUnit-Datei als Teil von `testDebugUnitTest` | NOT RUN | Gradle-Distribution/Android-Toolchain hier nicht verfügbar |
| Bestehende App-Regressionstests | NOT RUN | nicht erneut ausgeführt |
| T-01…T-71 vollumfänglich | NOT RUN | isolierte DSP-Fixtures sind keine vollständigen Akzeptanztests |
| Android Build/CI und echter Hardwaretest | NOT RUN | kein APK-/Gerätenachweis |

## Lokaler, bewusst enger Host-Test

```bash
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt -d core.jar
kotlin -classpath core.jar tools/analyzer_core_selftest.kts
```

Die Host-Datei enthält eigene PCM-Testsignale, **keinen getrennt gepflegten Analyzer-Kern**.
