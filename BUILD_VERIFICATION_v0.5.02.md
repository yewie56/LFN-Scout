# BUILD_VERIFICATION v0.5.02 – 20.09.2026

**App/Quellstand:** v0.5.02 / versionCode 143 / `de.brummbeobachter.app`.

| Check | Ergebnis | Durchführung |
|---|---|---|
| `kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/*.kt` | PASS | Isolierter JVM-/Core-Compile, kein Compose. |
| `bash tools/RUN_ANALYZER_V0502_SELFTEST.sh` | PASS 15/15 | Neue Timing-, Hz-, Fenster-, FFT-, Smoothing- und Zeitdauer-Regressionen. |
| `bash tools/RUN_ANALYZER_WATERFALL498_SELFTEST.sh` | PASS 6/6 | Erhaltener Wasserfall-Core. |
| `bash tools/RUN_ANALYZER_TIMING495_SELFTEST.sh` | PASS | Erhaltener Core-Zeitbasis- und PCM-FIR-Test. |
| `python3 verify_v0502_analyzer.py` | PASS 20/20 | Statischer Quelltext-/Persistenzvertrag, ersetzt ältere versionsgebundene Verifier für diese Version. |
| `python3 verify_reconstruction.py` | PASS | SHA-256-Manifest für 623 Projektdateien neu erzeugt; Quellen-/Versionsinvarianten geprüft. |
| `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `assembleDebug` | NOT RUN | Android-SDK in Entwicklungsumgebung nicht vorhanden; keine App-Kompilierung. |
| Xiaomi inkl. Belastungstest FFT 8192/Hop 5 und PNG/JPG | NOT RUN | Siehe `docs/project/TESTPLAN_v0.5.02.md`. |
| Restore neues Supabase-Projekt | NOT RUN | Zurückgestellt. |

**Wichtig:** Diese Entwicklungslieferung ist ein Quell-ZIP, keine freigegebene APK. Das Paket enthält historisch private Recovery-Daten; nur privat verwenden.
