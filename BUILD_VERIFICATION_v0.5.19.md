# BUILD_VERIFICATION v0.5.19

**Version:** 0.5.19 / versionCode 166  
**Datum:** 25.09.2026

## Belegter Vorgängerstatus

Der reale Nutzerlauf von v0.5.18.3 bestätigte PREPARE, `:app:testDebugUnitTest`, `assembleDebug` und ACTIVE-Paketierung als PASS. Die damalige Installation wurde nicht ausgeführt, weil `adb devices` kein Gerät meldete. v0.5.18.4 änderte daraufhin nur die Live-Ausgabe/ADB-Diagnose; ein realer v0.5.18.4-Lauf wurde hier nicht nachgewiesen.

## Änderungen v0.5.19

- Analyzer-Save-UI in untere Actionleiste verschoben; 48-dp-Audio-Symbol, fester Puffer-/Quotentext daneben, 3-s-Popup `Lokal gespeichert`.
- Lokale SAVE-Einstellungen aus der Hauptanzeige nach Einstellungen → Statistik verschoben.
- zentrale `GainReferencePolicy`; WAV-Spektrum/Spektrogramm um tatsächlich enthaltenen Recording-Gain korrigiert.
- 1-kHz-WAV L50/L90 verwenden denselben `StatisticsWindowEngine.analyzeOverall()`-Pfad wie die lokale Statistik-JSON.
- manuelle Wasserfall-Skala auf gain-korrigierten Bezug umgestellt.
- Analyzer-Live-FFT verwendet dieselbe zentrale Gain-Regel; PRE-GAIN-Save-WAV bleibt ohne Doppelabzug.
- `VORTEST_INSTALL.bat` wartet nach erfolgreichem Build/Paketieren auf genau ein autorisiertes ADB-Gerät und pollt alle 2 s.
- zwei bekannte Kotlin-Warnungen aus v0.5.18.3 bereinigt.

## In dieser Arbeitsumgebung ausgeführt

- `StatisticsWindowEngine.kt` + `GainReferencePolicy.kt` mit `kotlinc` kompiliert und ein Host-Selbsttest ausgeführt: gemeinsamer Overall-Pfad liefert identische L50/L90/Leq-Werte wie `analyze()` für denselben +50-dB-Testdatensatz: PASS.
- versionsspezifische statische Liefer-/Vertragsprüfung `python tools/verify_v0519_delivery.py`: **33/33 PASS**.
- ZIP-Integritätsprüfung des ausgelieferten ACTIVE-Pakets: **PASS** (vollständiges `ZipFile.testzip()` und Probe-Extraktion vor Auslieferung).

## Nicht ausgeführt

- echter Windows-`PREPARE_PROJECT.bat` für v0.5.19: NOT RUN,
- Android/Compose-Gradle-Build v0.5.19: NOT RUN,
- ADB-Installations-Warteschleife auf Windows: NOT RUN,
- Xiaomi-UI-/Gain-/Audio-Regression: NOT RUN.

Keine dieser offenen Prüfungen darf als PASS aus statischen Prüfungen abgeleitet werden.
