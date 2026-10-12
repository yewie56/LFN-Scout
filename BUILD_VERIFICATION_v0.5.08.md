# BUILD_VERIFICATION v0.5.08 – 21.09.2026

## Quellbasis und Umfang

Basis: vollständiger tatsächlicher v0.5.07-Projektstand, Paket-ID unverändert `de.brummbeobachter.app`. Neue Fassung v0.5.08 / versionCode 151. Die Sekundenachse neben dem Wasserfall wurde durch eine echte Kalenderzeitachse ersetzt. Eine Zeitmarke je tatsächlich empfangener Zeile, Auswahl von 1/2/5/10/20/30/60 Minuten, höchstens fünf gleichzeitige Ticks. Der bestehende Farbmaßstab bleibt erhalten, der Spektrum- und Wasserfall-Plotstart bleibt geometrisch unverändert (54 dp GUI bzw. x=124 beim Bitmap-Export). Kamera-Meldung durch ein am Button verankertes Popup statt Top-Center-Anzeige.

## Tatsächliche Prüfungen

- Separate Kotlin-JVM-Prüfung von `WaterfallTimeAxis.kt` ausgeführt: **PASS** (sechs Intervallwerte, maximal fünf chronologische Ticks, tatsächliche Zeilenpositionen, Minuten-/Stundenbindung, Pause-Lücke, leere Historie). Die drei Testmethoden in `WaterfallTimeAxisTest.kt` wurden zusätzlich unter Kotlin mit einem lokalen JUnit-API-Stub ausgeführt: **PASS 3/3** (kein Gradle-Ersatz).
- `tools/verify_v0508_time_axis.py`: statische Quellprüfung **PASS 13/13** (Zeittabellen-Kopplung, unveränderte Frequenzplot-Koordinaten, vollständige paarweise History-/Timestamp-Bereinigung, Kamera-Popup).
- Vollständiger Android-/Compose-Build `:app:testDebugUnitTest` bzw. `assembleDebug`: **NOT RUN** – kein Android-SDK in dieser Laufzeit. Insbesondere UI-Typprüfung durch echten Compiler noch offen.
- Xiaomi-Gerätetest: **NOT RUN**. Ein bisheriger Nutzer-Windows-Build von v0.5.06 war FAIL; v0.5.07-Gerätestatus nicht neu nachgewiesen.
- 16:1-Pegelabweichung unverändert **OFFEN**. Kein Eingriff in DSP, Recorder, Rolling, WAV, Supabase.

## Windows-Prüfung (verpflichtend)

```powershell
.\PREPARE_PROJECT.bat
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Nur `BUILD SUCCESSFUL` beider Befehle und bestandene Xiaomi-Prüfungen begründen die Gerätefreigabe. Testplan `docs/project/TESTPLAN_v0.5.08.md`.
