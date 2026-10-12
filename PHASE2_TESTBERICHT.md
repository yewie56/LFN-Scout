# LFN-Scout v0.4.85 – Analyzer v0.7, Phase 2: Test- und Übergabebericht

**Datum:** 19.09.2026. **Art:** Entwicklungszwischenstand auf dem vom Nutzer hochgeladenen v0.4.85-Projekt, **kein Android-Release / keine neu gebaute APK**. App `versionName=0.4.85`, `versionCode=125`, `applicationId=de.brummbeobachter.app` unverändert.

## Eingangsartefakt

`LFN-Scout_v0.4.85_Analyzer_v0.7_Phase1_Integration.zip`  
SHA-256: `38116b3fb44dd3fecd8012658b6f3b161fd4dba13476b1ca50dda46e29d486da`.

## Prüfungen

| Prüfung | Ergebnis | Tatsächlicher Nachweis / Grenze |
|---|---|---|
| Kotlin CLI, `:analyzer-core` (alle 3 Produktionsquellen) | PASS | `kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/*.kt -d ...` |
| Kotlin CLI, `MicCaptureArbiter.kt` | PASS | Kompiliert ohne Android-Libs |
| Original Phase-1-Selbsttest | PASS | Gleicher DSP-Kern im neuen Library-Pfad, 10-Hz-Referenz, Chunking, Clip, Reset |
| Phase-2-Kotlin-JVM-Host | PASS | `bash tools/RUN_ANALYZER_PHASE2_SELFTEST.sh`: 10 Hz / ca. −6 dBFS, defensive PCM-Kopie, nicht blockierender Fan-out, 30 verworfene Überlastblöcke, FFT-Session-Reset bei Quellenwechsel, exklusive Owner-Tokens |
| Statischer Phase-2-Integrationscheck | PASS | `python verify_analyzer_phase2.py`: 11/11, bestätigt Quellenkopplung, Versionsidentität, WAV-Format und Recovery-Snapshot |
| Vorhandene v0.4.85-Rekonstruktionsprüfung | PASS | `python verify_v0485_reconstruction.py`: 53 WAV / 12.722.332 Bytes |
| Manifest-JSON | PASS | JSON syntaktisch validiert |
| JUnit `:analyzer-core:test`, App-JUnit `testDebugUnitTest` | **NOT RUN** | Kein Android-SDK/Gradle in dieser Umgebung; Tests als Quellen ergänzt/verschoben, nicht als bestanden behauptet |
| Android `assembleDebug`, GitHub Actions, APK | **NOT RUN** | Keine APK erzeugt / keine Build- oder Update-Freigabe |
| Realgerät: 120-s-Recorder mit/ohne Rolling, Start/Stop-Rennen, CLIP, Audio-Route | **NOT RUN** | G1 bleibt offen, realen MIC/UNPROCESSED-Pfad testen |
| UI/Panelwechsel, Offscreen-PNG, Mithören, Radiushistogramme, Standalone-APK | **NOT IMPLEMENTED** | G2–G5 und vollständige T-01…T-71 offen |

Das historische `verify_v0484_requirements.py` wurde probeweise ausgeführt und meldet einen *erwarteten Versions-Pin-Fehler*: es verlangt ausdrücklich 0.4.84/124, während die aktuelle Basis 0.4.85/125 ist. Das ist **kein** Test für die aktuelle Version und wird nicht als bestandene Prüfung ausgegeben. Der aktuelle v0.4.85-Verifier bestand.

## Änderungsumfang

- Eigenes wiederverwendbares Kotlin-JVM-Gradle-Modul; ein DSP-/Session-Kern für LFN-Scout und zukünftigen Standalone-Host.
- Bestehende Aufnahmewege erhalten `MicCaptureArbiter` und post-Gain-PCM-Fan-out, ohne neue Mikrofoninstanz oder neues WAV-Dateiformat.
- Rolling-Coroutine-Abbruch erkennt jetzt den tatsächlich laufenden Job; normale Beendigung wird nicht als Hardwarefehler protokolliert.
- Library- und App-Tests, vollständige zusätzliche offizielle Projektdokumentation, aktualisierter Rekonstruktionsstand und Datei-Manifest.

## Nächste Freigabegates

1. Android Studio/GitHub: `gradlew[.bat] testDebugUnitTest` (inkl. `:analyzer-core:test`), danach `assembleDebug`.
2. Ein echtes Gerät: Mikrofonstart/Wechsel, 120-s-WAV und 30-min-Rolling auf Samplezahl, Gain, CLIP, Unterbrechungen und konkurrierende Nutzer prüfen.
3. Analyzer-Bedarf bei deaktiviertem Rolling und ohne WAV-Aufnahme über einen einzigen CaptureOwner implementieren; danach Panel/PNG/Histo/Mithören integrieren.
4. Erst nach vollständiger Testmatrix und dokumentiertem Build eine neue App-Version und APK freigeben.

**Wichtige Einschränkung:** Phase 2 stellt eine funktionierende Architektur zum freiwilligen Abonnieren vorhandener Audio-Streams bereit, aber noch keinen dauerhaft automatisch laufenden Analyzer. Keine weitere Version aus v0.4.63 übernehmen.
