# LFN-Scout v0.5.00 – Test-/Buildbericht

**Basis:** v0.4.99 (Code 140), **Lieferung:** v0.5.00 (Code 141). Reine Anpassung des Android-Analyzer-Hauptbilds; Produktidentität unverändert.

## Umsetzung

- Vollständiger dunkler Viewport über `BoxWithConstraints(fillMaxSize())`, scrollbarer Inhalt ebenfalls `fillMaxSize()`; adaptive Spektrum-/Wasserfallhöhe 53/47 % des verfügbaren Plot-Budgets. Auf kleinen Displays weiterhin Scroll-Fallback.
- Untere Karten Mithören, Superfast, Anzeige entfernt. Beide Schalter Mittelwert/Rauschboden bleiben in Einstellungen → Spektrum; Diagnose in Einstellungen → Diagnose.
- Unten nur manueller PNG-Export und Auto EIN/AUS, permanente Hinweiszeile entfernt. Ansonsten unveränderte Audio-/FFT-/WAV-/Export-Pfade.

## Verifikation

- Statischer UI-/API-Vertrag: `python verify_v0500_analyzer_layout.py`: **20/20 PASS**. Dies prüft nur die Quelltextverträge, nicht die Android-Compose-Lauffähigkeit.
- Isolierter Kotlin-Wasserfall-Selbsttest: `WATERFALL v0.4.98 SELFTEST PASS 6/6` (bestehender DSP unverändert). Der zusätzliche LF-Selbsttest wurde begonnen, aber in der verfügbaren Laufzeit nicht abgeschlossen; **kein PASS** dafür.
- Android-Gradle `:app:testDebugUnitTest`, `assembleDebug` und Xiaomi-Layout-/PNG-Gerätetest: **NICHT DURCHGEFÜHRT** (kein Android-SDK). Auf dem Xiaomi insbesondere prüfen: kein weißer Streifen, Diagrammhöhe, Scrollbarkeit bei Einstellungswechsel, PNG, Schalter im Settings-Unterpanel, Recorder-Rolling unverändert.
- ZIP-CRC, Hashmanifest, Versionskennung, Herkunft und Schutz der 53 Supabase-Recovery-WAVs werden bei der Erstellung verifiziert. Private Wiederherstellungsdaten nicht öffentlich veröffentlichen.

## Ersttest unter Windows / Android Studio

1. ZIP in einen **neuen** Projektordner entpacken; `PREPARE_PROJECT.bat` ausführen.
2. `gradlew.bat :app:testDebugUnitTest --stacktrace` ausführen; nach `BUILD SUCCESSFUL` in Android Studio mit Run installieren.
3. Analyzer starten, Vollbild/weißen Bereich beurteilen; bei aktivem Rolling-Buffer Spektrum und Wasserfall, PNG/Auto, ⋮ → Einstellungen → Spektrum (beide Kurvenoptionen) und Diagnose überprüfen.

**Freigabestatus:** Testkandidat, kein geprüfter Android-/Xiaomi-Release.
