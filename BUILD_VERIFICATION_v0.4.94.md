# LFN-Scout v0.4.94 – Test- und Buildbericht

**Version:** 0.4.94 / versionCode 134. Stand: 20.09.2026. **Testkandidat: kein nachgewiesener vollständiger Android-Build.**

## 1. Grundlage

Quellstand v0.4.93 aus vollständigem Projekt-ZIP. Nutzer meldete v0.4.93 Xiaomi-Funktionstest mit 7/7 erledigten Schritten (kein messtechnischer oder visueller Freigabebericht). Die bisherigen bekannten Mängel in v0.4.90/91/92 wurden nicht als aktuelle v0.4.94-Testergebnisse übernommen.

## 2. Änderungen

Siehe `docs/project/ANALYZER_LIVE_v0.4.94.md` und `CHANGELOG.md`. In produktiver Audioerfassung, Recorder, Rolling, WAV, Supabase, Impulsdiagnose, Seismik keine Quelländerung. App-Modifikationen ausschließlich LiveAnalyzerScreen, LiveAnalyzerController und AnalyzerSessionGateway; wiederverwendbarer Kern um AnalyzerTiming und AnalyzerRawLfSession-Hop ergänzt.

## 3. Prüfumfang und Grenzen

- `bash tools/RUN_ANALYZER_TIMING494_SELFTEST.sh`: plattformunabhängiger Kotlin-Hosttest für 20 Zeitbasis-Kombinationen, FFT 100 vs 500 LF-Samples, simulierten gemeinsamen Roh-Hub.
- `bash tools/RUN_ANALYZER_LIVE491_SELFTEST.sh`: bestehender LF-Signal-, Anti-Alias- und Hub-Regressionshosttest.
- `python verify_v0494_analyzer_contract.py`: rein statischer Quellenvertrag. **Kein Kotlin/Android-Compile-Beweis.**
- Standalone-`LiveAnalyzerController.kt` inklusive `AnalyzerSessionGateway.kt` und sämtlichen Core-Quellen: `kotlinc` gegen ausdrücklich lokale minimale Android-Teststubs **PASS** (dient als zusätzliche Schnittstellen-/Kotlin-Typprüfung, **nicht** als realer Android-/Compose-Build).
- Kotlin-PSI-Parser: `LiveAnalyzerScreen.kt`, `LiveAnalyzerController.kt`, `AnalyzerTiming.kt` syntaktisch erfolgreich geparst (kein Android-/Compose-Typcheck).
- `:analyzer-core:test`: **NOT RUN** im Android-Gradle (zusätzlicher JUnit-Test hinterlegt).
- `:app:testDebugUnitTest`, `assembleDebug`, Android-Studio-Sync, APK: **NOT RUN** mangels Android-SDK.
- Xiaomi-Bildrate, Wasserfallrichtung/Labels, PNG, 120-s-Historie bei unterschiedlichen Hops, Panelwechsel: **NOT RUN für v0.4.94**.

## 4. Geräteabnahme

1. `PREPARE_PROJECT.bat` einmal im frischen Windows-Projektordner; `gradlew.bat :app:testDebugUnitTest --stacktrace` (bindet Analyzer-Core ein), danach `gradlew.bat assembleDebug --stacktrace`; alternativ Android Studio Run für reinen Gerätetest ohne Unit-Test-Garantie.
2. Rolling einschalten, Analyzer öffnen, 4–5 s FFT-Warmup, 0–100-Hz-Spektrum mit links −140…0-dBFS-Ticks und unten Frequenz-Ticks; `Hz` steht **separat im Achsentitel**. Auf 200 Hz schalten, Skalierung prüfen.
3. Wasserfall: unmittelbar neue Zeilen oben, ältere wandern nach unten, Zeit 0 oben/−120 unten, dBFS-Farbleiste mit Ticks und Frequenzachse unten mit Ticks. Screenshot und PNG mit derselben Orientierung vergleichen.
4. FFT-Hop 100 (Soll 10/s), dann 500 (2/s), zurück 100. Jeweils warmup abwarten; Drops/Fehler prüfen. Wasserfall-Hop 5 und 1 vergleichen. Auto-PNG ab voller Historie und danach frühestens nach 120 s prüfen.
5. Pause/Resume, interner Panelwechsel, WAV/Rating/Seismik-Regression. Bei CPU-/Thermikproblemen 200/500 Hop bevorzugen.

**Datenschutz:** Projekt-ZIP enthält private Supabase-Recovery-Artefakte; nie ungefiltert öffentlich bereitstellen.

## 5. Paketprüfung

SHA-256-Dateimanifest aus den finalen Quelldateien rekonstruiert (das Manifest selbst ist ausgeschlossen). 53/53 private Recovery-WAVs im Vergleich zur Ausgangsversion v0.4.93 bytegleich. ZIP-CRC und Datei-Liste nach Packen geprüft.
