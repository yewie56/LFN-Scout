# Build-/Teststatus v0.5.06 – 21.09.2026

**Basis:** vollständig vorhandener und vom Nutzer (bis auf fehlendes Kamera-Pop-up) geprüfter v0.5.05-Quellstand; Paket-ID unverändert, versionCode 149.

| Prüfung | Ergebnis | Reichweite |
|---|---|---|
| `tools/RUN_ANALYZER_V0506_SELFTEST.sh` | PASS 8/8 | Kotlin-DSP-MaxHold: Retention, CLR, Average-/FFT-Erhalt |
| `tools/RUN_ANALYZER_V0504_SELFTEST.sh` | PASS 16/16 | 48-kHz-FFT, Signal, Wasserfallzeit |
| `tools/RUN_ANALYZER_V05041_TIMING_SELFTEST.sh` | PASS 6/6 | 8-/48-kHz-Zeitbasis mit JUnit-kompatiblem Stub |
| `tools/RUN_ANALYZER_V0503_SELFTEST.sh` | PASS 20/20 | FIR, Statistik, Timing (synthetische Töne) |
| `tools/verify_v0506_source.py` | PASS 21/21 | Statische Quelltextverträge für Kamera/3 Kurven/Setup/Export |
| Vollständiger Android-/Compose-Build | **NOT RUN** | Kein Android SDK in dieser Umgebung |
| Xiaomi v0.5.06 | **NOT RUN** | Testplan erforderlich |

Host- und Static-Tests beweisen keinen Android-Build oder visuelle Funktion. Die 16:1-Energieabweichung ist weiterhin offen. Manuelles Pop-up muss auf dem Gerät sichtbar bestätigt werden.

Windows:

```powershell
.\PREPARE_PROJECT.bat
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Geräteplan: `docs/project/TESTPLAN_v0.5.06.md`.
