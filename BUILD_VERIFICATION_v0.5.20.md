# BUILD_VERIFICATION v0.5.20

**Version:** 0.5.20 / versionCode 167  
**Datum:** 25.09.2026

## Änderungen

- WAV-Wasserfall-Regressionsfix: gain-korrigierte FFT-Bins unter -120 dBFS werden nicht mehr vor der Color-Mapping-Stufe zusammengeclippt.
- L50/L90 bleiben auf dem gemeinsamen `StatisticsWindowEngine.analyzeOverall()`-Pfad.
- gemeinsamer Audiokommentar-Dialog für Beobachter und Analyzer; Analyzer fixiert den Clip vor Dialogöffnung und verwendet dieselben Audio-Timeouts.
- Analyzer-Kommentar wird in lokalem Statistik-JSON und Manifest gespeichert.
- Analyzer-Audioinfo zweizeilig; Beobachter-Setup in Einstellungen; Entwicklermodus-Hinweis in Titelzeile; Highspeed-Impulsdiagnose ganz unten.
- Android-System-Zurück mit Panel-Verlauf.
- keine Supabase-Schemaänderung; BASE R1 unverändert.

## In dieser Arbeitsumgebung ausgeführt

1. `python tools/verify_v0520_delivery.py` → **24/24 PASS**.
2. `python tools/test_waterfall_v0520.py` → **PASS**. Der Hosttest bestätigt, dass repräsentative gain-referenzierte Werte unter -120 dBFS getrennt bleiben und unterschiedliche Farbpositionen erhalten.
3. `tools/test_local_statistics_v0518.kt` gegen den aktuellen `StatisticsWindowEngine`/`GainReferencePolicy` kompiliert und ausgeführt → **PASS**: 40 Bänder, +50-dB-Gain-Kompensation, L90/Leq und 10-Hz-Dominanz. Die einzige dabei ausgegebene Warnung liegt im Host-Test selbst (`!!`), nicht im App-Quellcode.
4. Kotlin-PSI-Syntaxprüfung des gesamten App-Quellbaums → **92 Dateien / 0 Syntaxfehler PASS**. Dies ist eine Parserprüfung, kein Android-/Compose-Typcheck.
5. Quell-/Dokumentprüfung auf Version 0.5.20 / versionCode 167 und unveränderte ADB-Warteschleife.

## Gradle-Versuch

Ausgeführt wurde:

```text
./gradlew :app:testDebugUnitTest --stacktrace
```

Ergebnis in dieser Container-Arbeitsumgebung:

```text
Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
Caused by: java.lang.ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain
```

Damit wurde **kein Android-/Compose-Build** ausgeführt. Dieser Fehler ist ein fehlendes lokales Wrapper-Artefakt in der ACTIVE-Kopie, kein Nachweis eines Kotlin-/Android-Quellfehlers. Der Windows-Pfad `PREPARE_PROJECT.bat`/`VORTEST_INSTALL.bat` ist dafür weiterhin maßgeblich.

## Nicht ausgeführt

- Windows `PREPARE_PROJECT.bat`: **NOT RUN**,
- `gradlew.bat :app:testDebugUnitTest`: **NOT RUN**,
- `gradlew.bat assembleDebug`: **NOT RUN**,
- ADB-Installations-Warteschleife v0.5.20: **NOT RUN**,
- Xiaomi-Funktionstest: **NOT RUN**.

Diese Punkte dürfen nicht als PASS bezeichnet werden, bevor der reale Windows-/Gerätelauf vorliegt.
