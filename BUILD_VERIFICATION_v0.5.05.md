# Build-/Teststatus v0.5.05 (21.09.2026)

Basis: tatsächlicher v0.5.04.2-Quellstand. Fix aus dem vom Nutzer gelieferten AndroidRuntime-Stacktrace (`AnalyzerTiming.lfRateHz` über Snapshot beim Reopen).

- HOST: `tools/RUN_ANALYZER_V05041_TIMING_SELFTEST.sh`: **6/6 PASS**, mit testweisem JUnit-Stub; neuer Regressionstest `restoredIdleModeNeverCrashesOnOldWidebandFactor` enthalten.
- Statischer Quellvertrag `tools/verify_v0505_source.py`: **21/21 PASS**. `AnalyzerInspectionExport.kt` separat mit selbst erstellten Android-/JSON-Teststubs per `kotlinc` syntaktisch kompiliert; **kein Android-SDK- oder App-Buildnachweis**.
- Android-/Compose-Gradle: **NOT RUN** (hier kein Android SDK).
- Xiaomi: **NOT RUN** für v0.5.05.
- Export- und Rolling-Zustandsintegration: **NOT RUN auf Android**. Statische Quellprüfung ist kein Geräte- oder Kompilierungsnachweis.
- Bekannte offene Störung: 16:1 Energieverlust; keine Korrektur behauptet.

Erforderliche Windows-Befehle:

```powershell
.\PREPARE_PROJECT.bat
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Xiaomi gemäß `docs/project/TESTPLAN_v0.5.05.md`.
