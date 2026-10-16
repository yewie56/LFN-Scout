# v0.5.66 – gezielter STOP/Rolling-Fix (Quellkandidat)

## Änderungen
- `AnalyzerStoppedJsonControllerPort`: Rolling ist kein `scoutRecording`; Fremd-Mikrofon-Eigentümer weiterhin verboten.
- `AnalyzerSetupSwitchTransaction`: Nur nach einem übereinstimmenden Rolling-Owner werden die exakt Rolling-bezogenen Service-/Mikrofonmarker ausgefiltert; alle übrigen Guards bleiben bestehen.
- `AnalyzerSimpleSetupPanel`: Speichern vor Umschalten zwingend erfolgreich, auch bei `Result.success(false)`; keine MRU-Aktualisierung vor positiver STOP-Übernahme.
- `AnalyzerNamedSetupStart`: Beim 8-kHz-RUN aktives JSON erneut transaktional in STOP übernehmen und mit bestehendem `startFromStoppedConfiguration` ohne Legacy-Prefs starten.
- `AnalyzerModeCoordinator`: Nur ROLLING_8K nutzt den benannten Startpfad; Direct/Test weiterhin unverändert.
- `LFN_GITHUB_TERMUX_v11.sh`: bytegleich zur hochgeladenen aktiven Version.

## Sicherheitsgrenzen und bekannte offene Punkte
- Rolling wird nicht beendet, neu gestartet oder gelöscht, aber eine echte kontinuierliche WAV-/Ringpuffer-Verifikation auf Android steht aus.
- Der Einstieg verwendet die vorhandenen Synchronisationsmechanismen. Deren vollständige atomare Serialisierung mit allen Android-Audio-Service-Zustandswechseln ist nicht nachgewiesen.
- Bei `repo.select()`-Fehler ist ein persistierter früherer Setup-Index möglich, obwohl ein neuer STOP-Entwurf im Controller steht. Ein abschließender Fehler-Injektionstest und ggf. weiterer Transaktionsfix ist vor Freigabe erforderlich.
- Der vorhandene Dialog beim Analyzer-Verlassen wurde nicht bearbeitet. Live-MON-DSP wurde nicht umgebaut.
- Kein Gradle- oder APK-Test in dieser Umgebung; keine behauptete PASS-Freigabe für T01–T20.

## Prüfung
- `python tools/verify_v0566_setup_switch.py` – nur statische Struktur-/Vertragsprüfungen.
- Noch auszuführen: `gradlew.bat testDebugUnitTest`, `gradlew.bat assembleDebug`, integrierter Android-Self-Test, Gerätetests T01–T20 nach `SETUP_FIX_TESTPLAN.md`.

## Nachtrag: Compile-Fix aus TEST-Build

GitHub Actions Run `38922482903` meldete `Unresolved reference applyCombinedDraft` in `AnalyzerNamedSetupStart.kt`. `applyCombinedDraft` ist im Companion Object von `AnalyzerStoppedJsonControllerPort` deklariert; Aufruf wurde auf `AnalyzerStoppedJsonControllerPort.applyCombinedDraft(...)` berichtigt. GitHub-Neubuild und Android-Gerätetest stehen noch aus.
