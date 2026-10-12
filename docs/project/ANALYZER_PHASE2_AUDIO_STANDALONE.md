# LFN-Scout Analyzer v0.7 – Phase 2: Shared Capture und Standalone-Vorbereitung

**Stand:** 19.09.2026 · **Basis:** Benutzer-ZIP `LFN-Scout_v0.4.85_Analyzer_v0.7_Phase1_Integration.zip` · **App-Version:** v0.4.85 / Code 125 (keine neue freigegebene APK).  
**Status:** Codeintegration und isolierte JVM-Tests; Android-Gradle-Build, Gerätetest und normative Gesamtvalidierung T-01…T-71 noch **OFFEN**.

## 1. Implementierter Ist-Zustand

- `:analyzer-core` ist ein separat eingebundenes **Kotlin-JVM-Library-Modul**. Es enthält den *unveränderten* `LiveAnalyzerCore` aus Phase 1 mit gleicher Package-ID, sodass App-Referenzen und die später getrennte Standalone-App dieselbe FFT-Implementierung verwenden können. Die bisherigen FFT-JUnit-Tests wurden ins Modul verschoben.
- `AnalyzerPcmHub` verteilen *Kopien* von PCM16-Blöcken asynchron über begrenzte Queues; langsame Verbraucher verlieren die ältesten Blöcke. Die Lücken werden über fortlaufende Block-Sequenzen erkannt. Fehler beim Callback beenden den Capture-Thread nicht; Dropped-/Error-Zähler sind abfragbar. Keine Datei/Cloud/AudioRecord-Aktionen im Library-Code.
- `AnalyzerLiveSession` nimmt Blöcke aus einem Hub entgegen und liefert Spektren. Quell-, Geräte- oder Gainwechsel sowie Sequenzlücken initialisieren die Analyse neu, statt fehlende PCM-Samples unbemerkt als Kontinuität zu behandeln.
- `MicCaptureArbiter`: Rolling-Service und direkter 120-s-Recorder erwerben vor ihrem `AudioRecord`-Start exklusiv einen Token. Ein zweiter Hardware-Capture wird verhindert. Der Rolling-Service wartet bei belegtem Mikrofon über den vorhandenen 5-s-Neustartpfad. Rolling-Recorder bleibt bei aktivem Rolling am bereits vorhandenen `RollingAudioEngine.captureNext()`.
- `AnalyzerSessionGateway.pcm`: ein App-Prozess-Hub. `RollingAudioService` und `AudioRecorder` veröffentlichen, **nur bei aktivem Subscriber**, post-Gain-PCM16-Blöcke mit 1000 Hz nach der bisherigen 8:1-Mittelung; die WAV-Verarbeitung selbst bleibt unverändert.
- Testabdeckung: neuer App-`MicCaptureArbiterTest.kt`, `:analyzer-core`-`AnalyzerPcmHubTest.kt`, bisheriger `LiveAnalyzerCoreTest.kt` und eigenständiger Kotlin-CLI-Host `tools/RUN_ANALYZER_PHASE2_SELFTEST.sh`.
- Der bestehende GitHub-Workflow ruft weiterhin `./gradlew testDebugUnitTest --stacktrace` auf; die App-Testaufgabe hängt nun von `:analyzer-core:test` ab. **CI-Build nicht hier durchgeführt.**

## 2. Architektur- und Datenvertrag

```text
 RollingAudioService: AudioRecord(8 kHz PCM16 mono) ──→ 8:1/Gain ──→ RollingAudioEngine → 120-s WAV bei Aktivierung
                         │                              └→ AnalyzerSessionGateway.pcm (1000 Hz)
                         └─ MicCaptureArbiter (exclusive)

 AudioRecorder (nur wenn Rolling nicht aktiv): AudioRecord(8 kHz mono) ──→ 8:1/Gain ──→ normale 120-s WAV
                         │                                               └→ AnalyzerSessionGateway.pcm (1000 Hz)
                         └─ MicCaptureArbiter (exclusive)

                    AnalyzerSessionGateway.pcm (bounded/async)
                               ↓
                   :analyzer-core / AnalyzerLiveSession
                               ↓
                         LiveAnalyzerCore (FFT)
                               ↓
                  künftiger Panel-/PNG-/UI-Adapter

 Standalone-App (später): eigener EINZIGER Android-PCM-Capture-Adapter → gleicher Library-Hub/Core/UI-Baustein
```

**Begrenzungen des Messsignals:** Bei beiden bestehenden Quellen wird ausschließlich **nach** der vorhandenen digitalen Verstärkung publiziert. Anzeige in dBFS ist daher Gain-abhängig und nicht unkalibriert als dB SPL zu deklarieren. Die bisherige 8-kHz→1-kHz-Mittelung ist kein scharfes Anti-Alias-Filter; ein späterer alternativer 8/48-kHz-Rohdatenzweig benötigt eigene Spezifikation, Anti-Alias-Prüfung und Recorder-Regressionen. Der veröffentlichte Zielstrom ist momentan 1000 Hz, nicht 48000 Hz. Hochgenaue Zeitstempel sind noch nicht mit dem Android-`AudioTimestamp`-Mechanismus abgeglichen; `captureMonotonicNs` ist nur der Publikationszeitpunkt, keine Sample-Epochenzeit.

**Verfügbarkeit:** In Phase 2 gibt es bewusst **noch keinen automatisch gestarteten Analyzer-Capture**, wenn sowohl Rolling als auch die normale Aufnahme aus sind. Der spätere Session-Coordinator muss einen Analyzer-Bedarf selbstständig über **denselben** Owner/Service anmelden. Das Android-Panel, eigenständige Standalone-APK, Offscreen-PNG, Mithören, Radiushistogramme, Persistenz/Setups und Android-Hintergrundlauf wurden nicht implementiert.

## 3. Entscheidung DEC-22 – Standalone-Kern verbindlich

`analyzer-core` darf weder Android-Abhängigkeiten noch App-/Supabase-/Recorder-spezifische Speicher- oder UI-Operationen enthalten. Scout und eine spätere eigenständige Android-App binden dasselbe Modul ein; deren Capture- und Android-UI-Hosts sind austauschbar. Standalone erhält später eigene `applicationId` und Datenhaltung, während die bestehende Scout-ID `de.brummbeobachter.app` unverändert bleibt. Wiederverwendbare UI-Bausteine und Setup-Schemata sind noch **separate Folgearbeit**; hier ist lediglich der wiederverwendbare DSP-/Session-/PCM-Kern erreicht.

## 4. Qualitätsgates und Tests

| Test / Gate | Status | Aussage |
|---|---|---|
| Kotlin-CLI Kompilierung `:analyzer-core` | PASS | Library-Quellen gegen lokalen kotlinc kompiliert. |
| Kotlin-CLI Kompilierung `MicCaptureArbiter` | PASS | Plattformunabhängiger Owner-Token kompiliert. |
| JVM-Host `RUN_ANALYZER_PHASE2_SELFTEST.sh` | PASS | 10 Hz/-6 dBFS, PCM-Kopie, Queue-Überlast, Source-Wechsel/Reset, Capture-Exklusivität. |
| JUnit `LiveAnalyzerCoreTest`, `AnalyzerPcmHubTest`, `MicCaptureArbiterTest` | NOT RUN | Neue/verschobene Gradle-Tests vorhanden, mangels Android-SDK/Gradle in dieser Umgebung nicht ausgeführt. |
| `:app:testDebugUnitTest`, `:analyzer-core:test`, `assembleDebug` | NOT RUN | Kein Android-SDK im Ausführungscontainer; keine APK erzeugt. |
| G1: Recorder + Analyzer auf realem Gerät | OPEN | Aufnahme mit/ohne Rolling, Start-/Stop-Rennen, Ein-/Ausgabegeräte und CLIP im Hardwaretest prüfen. |
| G2–G5 und T-01…T-71 | OPEN | UI/Panelwechsel, PNG, Histos, vollständige Normfunktionen und Regressionen folgen. |

Bei Fehlern im Echtbetrieb zuerst `ROLLING_WAIT_MIC`, `ROLLING_RECORD_FAILED`, `ROLLING_STARTED` und die Service-Lifecycle-Logs prüfen. Die App enthält noch keine sichtbare PCM-/Session-Diagnoseansicht. Keine Testfälle stillschweigend als PASS markieren.

## 5. Build- und Lieferweg

Windows: `PREPARE_PROJECT.bat` (nur wenn Tools fehlen), danach `gradlew.bat testDebugUnitTest`, dann `gradlew.bat assembleDebug`; CLI-JVM-Selbsttest zusätzlich nur mit Kotlin-CLI. GitHub Actions behält den bisherigen Trigger und führt durch die Task-Abhängigkeit die Library-Tests mit aus. Eine APK gehört erst nach grünem CI-/Android-Build zur Lieferung.

**Quell- und Integritätsbasis:** ZIP v0.4.85 Phase 1; `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/analyzer_v0.7/` und `docs/reconstruction/`. Baseline- und Liefer-ZIP-Hashes sowie betroffene Dateien stehen im separaten `PHASE2_TESTBERICHT.md` und im aktualisierten `file_manifest.sha256`. Private Supabase-Recovery-Dateien bleiben nur im privaten Gesamt-ZIP und unterliegen weiterhin `.gitignore`.
