# LFN-Scout v0.4.85 – Analyzer Phase 2H: Highspeed-Test- und Lieferbericht

**Datum:** 2026-09-19. **Quellbasis:** `LFN-Scout_v0.4.85_Analyzer_v0.7_Phase2_Integration.zip`. **Status:** Entwicklungszwischenstand, keine neue Releaseversion und keine APK.

## Geänderte / neue Implementierung

- `analyzer-core/src/main/kotlin/de/brummbeobachter/app/HighspeedImpulseDetector.kt` (neu): pre-gain PCM16 Event-Detektor mit konfigurierbaren Schwellwerten und Zeitparametern, samplebasierten Ergebnisdaten.
- `.../HighspeedImpulseSession.kt` (neu): asynchroner Frame-Konsument des gemeinsamen `AnalyzerPcmHub`; Rate-/Quellen-/Gap-Reset, Drop-/Fehlerzähler.
- `.../AnalyzerPcmHub.kt`: unabhängige Frame-Kopie pro Subscriber und explizite Unterbrechungsmarkierung.
- `app/.../AnalyzerSessionGateway.kt`: gesonderter `rawPcm`-Hub und optionale `startImpulseDetector`-Fabrik.
- `app/.../RollingAudioService.kt` und `AudioRecorder.kt`: Roh-PCM-Veröffentlichung bei Bedarf direkt nach `AudioRecord.read`, **vor** 8:1-Mittelung/Gain; Markierung neuer Capture-Sessions; bisheriger Recorderzweig beibehalten.
- `tools/analyzer_highspeed_selftest.kt`, `RUN_ANALYZER_HIGHSPEED_SELFTEST.sh`, JUnit-Quelltest und `verify_analyzer_highspeed.py`; offizielle Dokumentation, Changelog, README, Buildworkflow/Rekonstruktionsmanifest aktualisiert.

## Tatsächlich ausgeführte Prüfungen

| Prüfung | Ergebnis | Aussage |
|---|---|---|
| Kotlin-CLI `RUN_ANALYZER_HIGHSPEED_SELFTEST.sh` | PASS | 0,5-ms-8-kHz-Impuls über Blockgrenze; alte 1-kHz-Mittelung schwächt Peak ab; 48-kHz-PCM-Fixture; -32768-Clip; Stille; Async-Fan-out und Routen-Reset |
| Kotlin-CLI `RUN_ANALYZER_PHASE2_SELFTEST.sh` | PASS | ursprünglicher FFT-, Hub-, Überlast-, Reset- und MicOwner-Test |
| `python verify_analyzer_highspeed.py` | PASS 12/12 | tatsächliche Hook-Stellen vor Mittelung, 8k→1k-WAV-Konstanten, Manifest, private Daten |
| `python verify_analyzer_phase2.py` | PASS 11/11 | vorherige Phase-2-Statik bleibt erhalten |
| `python verify_v0485_reconstruction.py` | PASS | 53 WAV, 12.722.332 Byte, v0.4.85/125 |
| Kotlin JUnit `:analyzer-core:test`, Android-JUnit | NOT RUN | Tests im Projekt vorhanden; kein Gradle/Android-SDK in dieser Laufzeit |
| Android-APK `assembleDebug` / GitHub Actions | NOT RUN | keine neue APK / keine Laufzeitfreigabe |
| Xiaomi-Gerät 8k/48k, realer Schallimpuls, Kalibrierung/AudioTimestamp | NOT RUN | kein Hardware-/physikalischer Messnachweis |
| UI, automatischer Capture bei ausgeschaltetem Rolling, PNG/Mithören/Standalone-APK | NOT IMPLEMENTED | unveränderte Folgeaufgaben |

**48-kHz-Klarstellung:** Der Detektor verarbeitet synthetische 48-kHz-Testdaten, der tatsächliche Android-`AudioRecord` bleibt wie bisher auf **8000 Hz**. Das neue Rohsignal ist **nicht** nachträglich hochgerechnet. `captureMonotonicNs` ist die aus dem Read-Zeitpunkt geschätzte Blockbeginnzeit, kein präziser Android-Hardwarezeitstempel. Absolute dB-SPL, Infraschall unter gerätebedingten Hochpassgrenzen und echte Impulsbreiten sind nicht belegt.

**Daten-/Release-Sicherheit:** Paket enthält private Supabase-Recovery-Dateien und 53 WAV-Backups. Nicht unverändert öffentlich veröffentlichen. `applicationId = de.brummbeobachter.app`, v0.4.85/125 und WAV 1000 Hz / 120 s bleiben unverändert. ZIP- und SHA-Manifest-Prüfung siehe abschließende Integritätsausgabe.
