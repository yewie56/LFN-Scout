# BUILD_VERIFICATION v0.5.10 – 21.09.2026

## Anlass / nachweislicher Fehler

Nutzer-Windows-Build v0.5.09, ` .\gradlew.bat :app:testDebugUnitTest --stacktrace `: `:app:compileDebugKotlin FAILED`, `LiveAnalyzerController.kt:534:24`, `None of the following candidates is applicable` (fehlender passender `contains`-Empfänger). Dieser reale FAIL übersteuert die früheren bloßen Host-/statischen PASS-Ergebnisse.

## Korrektur

`DirectAnalyzerConfig.permissibleHops` = `Set<Int>`; `AnalyzerTiming.allowedFftHops` = `IntArray`. Der bedingte Ausdruck im rechten Operanden von `in` hat keinen einheitlichen `contains`-Empfänger. Explizite getrennte Zweigprüfung:

```kotlin
require(if (direct48k) fftHop in DirectAnalyzerConfig.permissibleHops else fftHop in AnalyzerTiming.allowedFftHops)
```

Alle zulässigen Hop-Werte bleiben unverändert. App-`versionName=0.5.10`, `versionCode=153`. Keine beabsichtigten Änderungen an Audio-Source, Rolling, WAV, FFT-Verarbeitung, Testsignalen oder UI.

## Verifizierungsstatus

| Test | Tatsächlicher Befund | Aussagegrenze |
|---|---|---|
| `tools/RUN_ANALYZER_V0510_BUILD_FIX_SELFTEST.sh` | **PASS 4/4** Kotlin-JVM-Membership (mit `kotlinc` kompiliert und ausgeführt) | Isoliertes Kotlin-Reproduktionsmuster, **kein** Android-Controller-Compile |
| `tools/verify_v0510_kotlin_membership.py` | **PASS 7/7** | Statischer Abgleich realer Typen und reparierter Controller-Ausdruck |
| `tools/verify_v0510_direct_analyzer.py` | **PASS 25/25** | v0.5.09-Direkt-Analyzer-Quellvertrag mit angepasster Versionsprüfung, keine Laufzeitprüfung |
| `tools/RUN_ANALYZER_V0509_SELFTEST.sh` | **PASS 33/33** | Kotlin-JVM-Hosttest des DSP-Kerns, kein Android-Service/Compose |
| Alter Timing-Hosttest v0.5.04.1 | **NICHT ERNEUT VOLLSTÄNDIG ABGESCHLOSSEN** | Frühere v0.5.09-Ergebnisse 6/6 bleiben historisch; hier nicht als frisches PASS behauptet |
| Windows-Nutzerbuild v0.5.09 | **FAIL** | Belegter Kotlin-Typfehler in `setTiming` |
| Gradle `:app:testDebugUnitTest` und `assembleDebug` v0.5.10 | **NICHT AUSGEFÜHRT** | Android-SDK in der Erstellungsumgebung nicht verfügbar |
| Xiaomi v0.5.10 | **NICHT AUSGEFÜHRT** | Keine Geräte- oder Audioquellen-Freigabe |

Dies ist ein Buildkorrektur-Kandidat, keine bestätigte APK. Die neue Reparatur ersetzt keinen vollständigen Android-Build.

## Nächste Schritte

1. Frisch entpacken / `PREPARE_PROJECT.bat`.
2. ` .\gradlew.bat :app:testDebugUnitTest --stacktrace `.
3. ` .\gradlew.bat assembleDebug --stacktrace `.
4. Bei erfolgreichem Build: `docs/project/TESTPLAN_v0.5.10.md` abarbeiten; 16:1-Pegelabweichung separat offen.
