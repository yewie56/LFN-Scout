# BUILD_VERIFICATION – v0.5.03 / 144

| Check | Ergebnis | Abgrenzung |
|---|---|---|
| `bash tools/RUN_ANALYZER_V0503_SELFTEST.sh` | PASS 20/20 | JVM-FIR/Takttests, keine Android-Oberfläche |
| `bash tools/RUN_ANALYZER_V0502_SELFTEST.sh` | PASS 15/15 | Vorheriger Core-Vertrag |
| `bash tools/RUN_ANALYZER_WATERFALL498_SELFTEST.sh` | PASS 6/6 | Historischer Wasserfall-Core |
| `bash tools/RUN_ANALYZER_TIMING495_SELFTEST.sh` | PASS | Historische Zeitbasis/PCM-Hub |
| Quelltext-/Persistenzvertrag v0.5.03 | PASS 29/29 | Keine Compose-/Runtime-Prüfung |
| Android `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `assembleDebug` | NOT RUN | Android-SDK nicht vorhanden; keine APK erzeugt |
| Xiaomi v0.5.03 | NOT RUN | `docs/project/TESTPLAN_v0.5.03.md` |
| Supabase-Restore neues leeres Projekt | NOT RUN | Zurückgestellt |

Vollständiges Quell-ZIP mit historisch privatem Supabase-Recovery-Archiv – **nicht öffentlich veröffentlichen**.
