# LFN-Scout – Analyzer Phase 2H: Highspeed-Impulsdaten vor der Decimation

Stand: 19.09.2026. **Entwicklungszwischenstand auf v0.4.85/125; kein Android-Release, kein Hardware-Nachweis.** Ergänzung zu `ANALYZER_PHASE2_AUDIO_STANDALONE.md` und dem Sollvertrag `analyzer_v0.7/`.

## 1. Entscheidung / Anforderungen

- **AN-IMP-001 (FIX):** Impulserkennung erhält tatsächliche PCM16-Mikrofondaten **vor** 8:1-Mittelung und **vor** Recorder-Gain. Die bestehende 1000-Hz-WAV-Strecke darf nicht umgestellt, ersetzt oder vom neuen Zweig aufgenommen werden.
- **AN-IMP-002 (FIX):** gemeinsamer, einziger `AudioRecord`-Owner; ein zusätzlicher selbständiger Highspeed-Mikrofonclient ist verboten.
- **AN-IMP-003 (ZIEL):** native 48000-Hz-Erfassung mit validierter Audioquelle/Route und sauberer Decimation für die bestehende 1000-Hz-Strecke. **Nicht implementiert**; die tatsächlich eingestellte Capture-Rate bleibt aktuell **8000 Hz** (125 µs pro Sample). Niemals digital hochgerechnete Werte als 48-kHz-Mikrofonsamples kennzeichnen.
- **AN-IMP-004 (FIX):** Decoder/Detektor im gemeinsamen `:analyzer-core`-Kotlin-Modul; Quelle/Rate/Gaps setzen den Zustand zurück. Bounded Queue darf statt der Capture-Thread-Blockierung Daten verlieren; solche Verluste sind sichtbar/diagnostizierbar.
- **AN-IMP-005 (GATE):** Gerätemessung, Validierung des Mikrofonfrequenzgangs, Android-`AudioTimestamp` und ein geeignetes Hochpass-/Hüllkurvenverfahren sind für physikalisch aussagekräftige Impulscharakterisierung erforderlich.

## 2. Tatsächlicher Code-Datenfluss

```text
RollingAudioService / AudioRecorder: ein vorhandener AudioRecord, mono PCM16, 8000 Hz
                       │
                       ├→ rawPcm.publish(input, count, 8000 Hz, gainDb=0)
                       │    (nur bei Subscriber; erster Sample-Zeitpunkt nur geschätzt)
                       │     → asynchroner HighspeedImpulseSession → HighspeedImpulseDetector
                       │       → optionale Event-Callbacks (kein PCM-Export)
                       │
                       └→ bisherige 8:1-Mittelung und recorderseitiger Gain
                             → bestehende 1000-Hz-WAV/Rolling-Verarbeitung
                             → bisheriger 1000-Hz-FFT-Hub
```

Der Highspeed-Abgriff läuft **nur bei laufendem Rolling oder bei direkter 120-s-Aufnahme** und nur mit explizitem `AnalyzerSessionGateway.startImpulseDetector { event -> ... }`. Bei inaktivem Rolling und ohne Aufnahme existiert weiterhin kein eigener Analyzer-Capture. Das Ereignis-Callback öffnet kein Panel und schreibt keine Dateien; UI/Service-Koordination folgt später. Die eigenständige Android-App kann denselben Core später mit eigenem Capture-Adapter verwenden.

## 3. Parameter / Semantik des implementierten Prototyps

| Parameter | Anfangswert | Bedeutung |
|---|---:|---|
| tatsächlicher Hardware-Capture | 8000 Hz | Bestandsmodus; 48 kHz noch kein Android-Capture |
| Detektor-Rate | 4000–192000 Hz | akzeptierter Sample-Rate-Vertrag; **synthetische** Prüfung bei 48 kHz |
| `minPeakPcm` | 1400 PCM | Mindest-Onset-Schwelle, **unkalibriert** (PCM16) |
| `thresholdOverNoise` | 6 | zusätzliche dynamische Schwelle über geglättetem Betragshintergrund |
| `releaseFraction` | 0,35 | niedrigere Rücksetzschwelle relativ zur aktuellen Onset-Schwelle |
| `releaseHoldMs` | 2 ms | Zeit unter Release-Schwelle vor Event-Abschluss |
| `refractoryMs` | 20 ms | Sperrzeit nach einem abgeschlossenen Event |
| Raw-Queue | 8 Blöcke | Drop-oldest statt AudioRecord-Blockierung |

Eventdaten: Peak in PCM16 und dBFS **vor Recorder-Gain**, geschätzter monotonic onset, globaler Sample-Index der zusammenhängenden Session, oberhalb der Schwelle geschätzte Dauer, diskrete Energie (PCM²) und Clip-Samplezahl. **Nicht enthalten:** absoluter Schalldruck, tatsächliche Druckanstiegszeit, validierte Bandbreite, absolute UTC-Zeitsynchronität, ein geeichtes Pegelmaß, Ereignisaufzeichnung. Der Algorithmus ist ein erster threshold-basierter Erkenner; ein tonales Signal kann je nach Parameter ebenso Trigger erzeugen. Für wissenschaftliche Aussagen sind zusätzliche Filter/Validierung nötig.

## 4. Zeitbasis, Drop/Route-Grenzen

`captureMonotonicNs` im Rohpfad ist aus `System.nanoTime()` nach `read` minus Blockdauer geschätzt; dies ist **nicht** Android `AudioTimestamp` bzw. eine vermessene Sample-Epochenzeit. Scheduling-/HAL-Jitter bleibt möglich. Eine neue Capture-Session markiert einen Sequenzsprung; Route-/Rate-/Queue-Gaps resetten den Detektor, um Impulse nicht künstlich über Lücken fortzuschreiben. Bei Queue-Überlast gibt es keine garantierte Vollständigkeit; `droppedFrames`/`discontinuityCount` müssen für die spätere GUI/Logs ausgewertet werden. Kein Roh-PCM wird durch diese Erweiterung gespeichert oder übertragen.

## 5. Testmatrix (separat von T-01…T-71)

| ID | Test | Lokaler Status |
|---|---|---|
| H-01 | echter 8-kHz-Fixture-Puls 0,5 ms über Blockgrenze, Schwelle/Dauer | PASS Kotlin-CLI |
| H-02 | 8:1-Mittelung reduziert Peak gegenüber raw, WAV-Policy weiterhin 1000 Hz | PASS Fixture / statisch |
| H-03 | Detektor verarbeitet 48-kHz-Fixture 0,5 ms | PASS Kotlin-CLI, **kein Hardwaretest** |
| H-04 | Negative PCM16-Übersteuerung −32768; Stille triggert nicht | PASS Kotlin-CLI |
| H-05 | Nicht blockierender PCM-Hub, Source-/Route-Reset, bisherige Phase-2-Tests | PASS Kotlin-CLI |
| H-06 | Android Studio/CI `testDebugUnitTest`, `assembleDebug`, reale 8000-/48000-Hz-Audio-Hardware | NOT RUN |
| H-07 | Gleichzeitige WAV/Analyzer-Aufnahme, 30-min-Rolling, Energie/Drop/Clip, Akku/Screen-off | NOT RUN |
| H-08 | Messkette: Frequenzgang, zuverlässige Impulsform, AudioTimestamp/Hardware-Latenz | NOT RUN |

Aufruf auf einem PC mit Kotlin-CLI: `bash tools/RUN_ANALYZER_HIGHSPEED_SELFTEST.sh`. Auf Android-Studio-/Gradle-Umgebung: `gradlew.bat testDebugUnitTest`, danach `gradlew.bat assembleDebug`. Ein PASS des Kotlin-CLI-Fixtures ersetzt **keinen** Android-/Gerätenachweis.

## 6. Folgearbeiten vor 48 kHz / Freigabe

1. Gerät/Audioquelle/Route bei 48 kHz prüfen; AudioRecord tatsächlich initialisieren, Start/Stop- und Routentests.
2. Bestehende WAV-Rate weiterhin 1000 Hz; bei 48 kHz **separaten validierten Anti-Alias-Filter** für 48:1-Decimation statt einfachem Mittelwert entwickeln und auf identische Aufnahme-Pegelsemantik prüfen.
3. Explizite Auswahleinstellung/Fallback; niemals still 8 kHz als 48 kHz ausgeben; Eingabegain/Frontend-/Mic-Hochpass dokumentieren.
4. Zeitbasis mit `AudioTimestamp` und Samplezähler verifizieren; Zeitkorrelation mehrerer Geräte gesondert.
5. Zusätzlich zum Peak: robuste bandbezogene Transientenerkennung/Ringing-Kontrolle, definierte Ereignisstatistik, Histo-Overlay und persistente Parameter/Reports; jeweils Regressionstests.

**Private Recovery-Daten:** Das vollständige Projekt enthält weiterhin Supabase-Dumps und 53 WAV-Backups und darf nicht unverändert öffentlich nach GitHub hochgeladen werden; `.gitignore` schützt nur reguläres Git-Tracking, nicht unbedachtes ZIP-Veröffentlichen.

## 7. Testnachtrag – Seismik-Contract-Tests (19.09.2026)

Der vom Nutzer durchgeführte `:app:testDebugUnitTest`-Lauf des ursprünglichen Phase-2H-Pakets ergab 112/114 erfolgreiche Tests, zwei veraltete Seismik-Contract-Tests. Die korrigierte Testversion prüft den tatsächlichen gemeinsamen Bitmap-Renderer und die `near_bitmap_*`-Gesten. Isolierte Kotlin-Ausführung des Testpaares: 6/6 PASS. **Erneuter kompletter Android-App-JUnit-Test, APK und Gerätetest bleiben offen.** Details und neue Testanleitung: [`SEISMIC_REGRESSION_PHASE2H_FIX.md`](SEISMIC_REGRESSION_PHASE2H_FIX.md).
