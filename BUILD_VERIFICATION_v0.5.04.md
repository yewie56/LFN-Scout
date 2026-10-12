# Build-Verifikation v0.5.04 – 21.09.2026

| Prüfung | Ergebnis | Grenze |
|---|---|---|
| Analyzer-Core-Kompilierung + v0.5.04-Sinus-/Timingtest | PASS 16/16 | JVM-Host, kein Android/AudioRecord |
| Analyzer-FIR-/Decimationtest v0.5.03 | PASS 20/20 | Synthetisch; tatsächlicher 16:1-Energieverlust weiterhin OFFEN |
| Analyzer v0.5.02 Regression | PASS 16/16 | Historienschutz gemäß v0.5.04-Änderung angepasst |
| Historische Wasserfall-/Timing-Regressionsprüfungen | PASS | Historische Timing-Verträge nur auf zulässige LF-Kombinationen angewendet |
| Statischer App-Quellvertrag v0.5.04 | PASS 20/20 | Keine Android-Kotlin- oder Compose-Kompilierung |
| `:app:testDebugUnitTest`, `:app:compileDebugKotlin`, `assembleDebug` | **NOT RUN** | Android-SDK hier nicht vorhanden |
| Gerätetest Xiaomi / Audioquellen-/STOP-Restore / PNG/JPG | **NOT RUN** | Muss anhand Geräte-Testplan ausgeführt werden |

Windows-Rezept: `PREPARE_PROJECT.bat`, danach `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`. Die resultierende APK ist erst nach tatsächlichem erfolgreichen Build vorhanden. Testplan `docs/project/TESTPLAN_v0.5.04.md`. Warnings aus v0.5.03 nicht ungeprüft als korrigiert bezeichnen.
