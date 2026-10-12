# LFN-Scout – Seismik-Regressionstests: Korrektur zum Analyzer-Phase-2H-Paket

**Stand:** 19.09.2026 · **Basis:** LFN-Scout 0.4.85 / versionCode 125 · **Art:** Test- und Dokumentationskorrektur, **keine neue App-Version**.

## 1. Anlass / gemeldeter Fehler

Der Nutzer führte auf Windows 11 aus:

```powershell
.\gradlew.bat :app:testDebugUnitTest --stacktrace
```

Ergebnis im **Originalpaket**: `114 tests completed, 2 failed`, nämlich:

- `SeismicHelicorderComposeContractTest.helicorder_time_labels_use_composable_safe_for_loop` (Zeile 29)
- `SeismicPlotInteractionContractTest.both_plot_types_are_zoomable_and_resettable` (Zeile 28)

`verify_analyzer_highspeed.py`: 12/12 PASS; `:analyzer-core:test`: BUILD SUCCESSFUL. Die zwei fehlerhaften App-Tests sind von der Highspeed-Implementierung unabhängig.

## 2. Technische Ursache und aktuelle Verträge

Seit v0.4.78 rendert der Näheplot alle Helicorder-Zeilen einschließlich ihrer Uhrzeiten in `SeismicPlotExporter.renderNearPlot(snapshot)` (Android-Canvas); die Compose-UI zeigt dessen `nearPlotBitmap` an und gibt dasselbe Bitmap an `SeismicPlotExporter.save(...)`. Der alte Test erwartete fälschlich `for (row in data.helicorderRows)` in `MainActivity.kt` und einen Box-Import. Das ist nach der Umstellung auf den gemeinsamen Bitmap-Renderer kein aktueller Vertrag mehr.

Der WBBU-Plot verwendet `wbbu_zoom` / `wbbu_double_tap_reset`. Der Näheplot verwendet `near_bitmap_zoom` / `near_bitmap_double_tap_reset`; der zweite Test erwartete noch den älteren Schlüssel `near_double_tap_reset`. Beide tatsächlichen Handler enthalten `detectTransformGestures`, Zoom-Clamp und Pan-Änderung sowie `detectTapGestures(onDoubleTap ...)`, `MIN_ZOOM` und `Offset.Zero`.

## 3. Geänderte Regressionstests

- `SeismicHelicorderComposeContractTest`: kontrolliert nun Uhrzeitformatierung und `canvas.drawText(time, ...)` im Exporter und die gemeinsame Bitmap-Referenz zwischen Anzeige und PNG-Speicherung. Der ehemalige Compose-Loop darf nicht wieder eingeführt werden.
- `SeismicPlotInteractionContractTest`: prüft die zwei tatsächlichen Bitmap-Gestenbezeichner **einschließlich** Gestenbehandlung, Clamp, Pan und Zoom-/Pan-Reset. Übrige Kontrollen für Strichstärke, Legende, Export und Speichermeldung bleiben bestehen.
- **Kein** `ignoreFailures`, kein auskommentierter Test, keine Änderung an `MainActivity.kt`, `SeismicPlotExporter.kt`, PCM, WAV, Supabase oder Analyzer-Algorithmik.

## 4. Nachweise und Grenzen

- Das aktualisierte Testpaar wurde mit dem lokalen `kotlinc` und einem isolierten JUnit-kompatiblen Assertions-/Annotations-Stub kompiliert und als sechs Testmethoden ausgeführt: **6/6 PASS**. Dies ist eine echte Ausführung der enthaltenen Assertions auf den unveränderten Produktionsquelldateien, aber kein Android-Gradle-Test.
- `verify_analyzer_highspeed.py` und `verify_v0485_reconstruction.py` müssen erneut laufen; Status im separat mitgelieferten Testbericht.
- **Noch offen:** vollständiger erneuter `:app:testDebugUnitTest`-Lauf, Android-Build, Gerätetest und tatsächliche 48-kHz-Mikrofonerfassung. Der frühere Befund `112/114` gilt nur für das Originalpaket; er darf nicht als Testergebnis dieses korrigierten Pakets fortgeschrieben werden.

## 5. Benutzer-Testschritte unter Windows 11 (PowerShell)

Im **entpackten neuen** Projektordner nach `PREPARE_PROJECT.bat`:

```powershell
python .\verify_analyzer_highspeed.py
python .\verify_v0485_reconstruction.py
.\gradlew.bat :analyzer-core:test --stacktrace
.\gradlew.bat :app:testDebugUnitTest --stacktrace
# Nur falls alle Tests erfolgreich:
.\gradlew.bat assembleDebug --stacktrace
```

Erwartung für die App-Tests: `BUILD SUCCESSFUL`, **0 fehlgeschlagene Tests**. Bei Fehlern bitte die relevanten Meldungen aus `app/build/reports/tests/testDebugUnitTest/index.html` beifügen. Keine APK als freigegeben behandeln, solange die Tests fehlschlagen.

**Sicherheit:** Die private Projekt-ZIP enthält Supabase-Recovery-Dumps und 53 WAV-Backups. Nicht unverändert öffentlich veröffentlichen; `.gitignore` ist kein Schutz für hochgeladene ZIP-Archive.
