# Vor-Implementierungs-Gate: Ist-Architektur und Mapping-Formular

**ID:** LFN-AN-MAP · **Version:** 0.7 · **Status:** Ist-Mapping anhand der vollständigen Projekt-ZIP v0.4.85 ergänzt am 19.09.2026; Integrationsgates G1–G5 weiterhin OFFEN. Keine erfundenen Klassen-/Dateinamen übernehmen.

## 1. Unverzichtbare Eingangsdateien

- `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, vollständige `docs/project/` und ggf. aktuelle Tests/Selftests.
- Gradle-/Manifest-/Version-Konfiguration, App-/Repositoryname, Package-ID, Navigation und tatsächliche Module.
- Audioaufnahme-Implementierung: Capture-Owner, PCM-Format, Zeitstempel, Puffer, Zielcodec/-dateiformat, Input-Routing.
- Radiushistogramm: Erzeugung, semantische Definition, Datenzugang, Anzahl Historie, Einheiten/Zeiten.
- Persistenz und ggf. Supabase/Bewertungsstrukturen – **Bestandsschutz**.

## 2. Ist/Soll-Mapping (vor Codeänderung ausfüllen)

| Architekturrolle / Risiko | Aktuelle Ist-Datei/-Klasse/-API | Geplante Änderung | Datenmigration | Regressionstest | Status |
|---|---|---|---|---|---|
| Gemeinsamer Mikrofon-Capture | OFFEN | CaptureOwner-Fan-out | OFFEN | T-44/45/48 | NOT MAPPED |
| Bestehender Audio-Recorder | OFFEN | PCM-Consumer-Adapter | ggf. vermeiden | T-45/46/47 | NOT MAPPED |
| Timestamp-/Sampleformat | OFFEN | PcmBlock-Vertrag | OFFEN | T-44/47 | NOT MAPPED |
| Analyzer-Core/FFT | OFFEN | unabhängiger Kern | OFFEN | T-03…06 | NOT MAPPED |
| Panel-Host / Lifecycle | OFFEN | SessionCoordinator | keine UI-Datenmigration | T-49/51 | NOT MAPPED |
| PNG-Exportziel | OFFEN | Offscreen + async | ggf. Ordnerprofil | T-50/53/54 | NOT MAPPED |
| Histogramm-Repository | OFFEN | SnapshotProvider | read-only | T-55…58 | NOT MAPPED |
| Setups / Kalibrierung | OFFEN | Schema-Versionierung | v0.5-Migration | T-17…21 | NOT MAPPED |
| Bestandsbewertungen/Supabase | OFFEN | unverändert | keine | Bestandstests | NOT MAPPED |
| Build/CI und APK | OFFEN | unverändert soweit möglich | keine | offizieller Workflow | NOT MAPPED |

## 3. Go-/No-Go-Kriterien

- **G0 (Start):** Reale Eingangsdateien und Versionshash erfasst; aktueller Build und vorhandene Tests nachgewiesen. Scheitern/Fehlen dokumentieren statt PASS behaupten.
- **G1 (gemeinsamer Capture):** Ein Stream / zwei Verbraucher testbar. Wenn Bestands-Recorder nur eigenständige Hardware-API unterstützt: Adapterumbau explizit spezifizieren, nicht parallel capture erzwingen.
- **G2 (Panelwechsel):** Aktive Analyzer-Session überlebt internal navigation; sichtbares UI deaktiviert Rendering.
- **G3 (Save offscreen):** PNG entsteht ohne Analyzer-View aus eigenem Renderstate, mit aktuellen Daten und Metadaten.
- **G4 (Histogramme):** echte Quelle, Zeiten und Einheiten belegt; bei nicht zugänglicher Datenquelle Blocker dokumentieren.
- **G5 (Freigabe):** SRS, SDS, UI, Algorithmen, Tests, `docs/project/`, Changelog, komplette ZIP und APK-/Testreports synchronisiert.

## 4. Konflikt-Entscheidungsprotokoll

Für jeden Konflikt schriftlich erfassen: betroffene Requirement-ID; aktuelle API/Verhalten; Alternativen; gewählte Abhilfe; Risiken; Migration; Rollback; zuständiger Test; Ergebnis. Bei ungeklärten wesentlichen Konflikten nicht ungefragt andere LFN-Scout-Funktionen ändern.

**Wichtig:** Die Formblätter sind in diesem Übergabepaket **unbefüllt**. Sie dürfen erst anhand des aktuellen Quellcodes als erledigt markiert werden.

## 5. Prüfpunkte aus Abschluss-Audit v0.7

| Ist-Objekt | Reale Quellpfade / Klasse / Version / Vertrag | Status vor Sichtung |
|---|---|---|
| Capture-Owner und Mic-Berechtigung | AUSZUFÜLLEN (kein Klassenname erfunden) | OFFEN |
| PCM-Rate, Quelle, Kanäle, Puffer und Ownership | AUSZUFÜLLEN | OFFEN |
| Bestehender Audio-Recorder und Start-/Stop-Konflikt | AUSZUFÜLLEN | OFFEN |
| Interne Panel-Navigation/Lifecycle | AUSZUFÜLLEN | OFFEN |
| Radiushistogramm: ID, Radius-/Zeitachsen, Zeitstempel, persistente Quelle | AUSZUFÜLLEN | OFFEN |
| PNG-Ziel, freie Quota, SAF-/Storage-Rechte | AUSZUFÜLLEN | OFFEN |
| Settings-/Setup-Migration und zuletzt benutztes Setup | AUSZUFÜLLEN | OFFEN |
| Android-Build, CI, Regressiontests, Abhängigkeiten | AUSZUFÜLLEN | OFFEN |
| Letzte freigegebene App-/Produktnamen, Paket-ID, ZIP-Namenskonvention | AUSZUFÜLLEN | OFFEN |

**Kein GO**, solange die Mappingpflichten für gemeinsam genutzte Komponenten ungeprüft sind. Zusätzliche neue `docs/project/`-Dateien sind mit dem tatsächlichen Bestand abzustimmen; keine Blindüberschreibung offizieller Projektdokumentation.



---

## 6. Tatsächliches Ist-Mapping auf v0.4.85 (19.09.2026; geprüft gegen Quellcode)

**Baseline:** `LFN-Scout_Android_v0.4.85_Private_Recovery_Master_Docs_Expanded_2026-09-19.zip`. Es handelt sich um den vollständigen vorliegenden Projektstand, nicht einen remote verifizierten Git-Commit. Die bestehenden Verzeichnisse `docs/project/` und `docs/reconstruction/` wurden **nicht überschrieben**; diese v0.7-Quellen liegen im separaten Unterordner `docs/project/analyzer_v0.7/`. Sie sind ein Sollvertrag, nicht der implementierte Funktionsstand.

| Rolle | Tatsächlicher Quellcode / belegter Vertrag | Soll-/Integrationsarbeit | Teststatus |
|---|---|---|---|
| Live-Mikrofon / Rolling-Owner | `app/src/main/java/de/brummbeobachter/app/RollingAudioService.kt`: ein `AudioRecord` mit `AudioRecordingPolicy.SOURCE_SAMPLE_RATE_HZ=8000`, mono PCM16, `READ_BLOCKING`, Downsampling per 8er-Mittelwert, `audioGainDb` **vor** Übergabe an Rolling. `RollingAudioEngine.kt`: 1000-Hz-`ShortArray`-Ring, 1.800.000 Samples, `append()` und `captureNext()` unter Lock. | Rolling allein ist KEIN vertraglich kompatibler 48-kHz-Roh-PCM-Fan-out. Eine von Recorder, Rolling und Analyzer gemeinsam koordinierte Capture-Session auf tatsächlich verfügbarer Rate ist noch nicht implementiert. | G1 NO-GO; T-44–48, T-62 NOT RUN |
| Normale Audioaufnahme | `AudioRecorder.kt::recordTwoMinutes()`: nutzt `RollingAudioEngine.captureNext()` nur bei aktivem Rolling, ansonsten öffnet sie eigenständig `AudioRecord`; Ziel 1000 Hz/120 s WAV; Quellen AUTO/UNPROCESSED/MIC. | Bei Analyzer und normaler Aufnahme ohne Rolling wären sonst **zwei Mikrofoninstanzen** möglich; vor Analyzer-Einschaltung CaptureOwner/Lease priorisiert Recorder einführen. Gain-/Formatwandler ausschließlich recorderseitig. | G1 NO-GO; T-45–48, T-62 NOT RUN |
| PCM-Vertrag | `AudioRecordingPolicy.kt`: Eingang 8000 Hz, Ziel 1000 Hz; `RollingAudioEngine.Snapshot` hält start/end epoch ms, **kein pro-Block-monotonic timestamp, sequence oder Rohformat**. | Immutable `PcmBlock` inklusive monotonic timestamp, sample index/sequence, source/rate/clip/drop; kein stiller Rate-Switch. | G1 NO-GO; T-44/47 NOT RUN |
| Analyzer-Core | Neu: `LiveAnalyzerCore.kt`, reiner Kotlin-DSP-Kern, `ShortArray`-Eingang, Hann, DC-Subtract, radix-2-FFT, lineare Bins, dBFS, gleitendes Average, MaxHold und Clip-Flag. Parameter zur Laufzeit: 1000 Hz, 4096 FFT, 512 hop, 0–100 Hz. Der Kern **öffnet keinen Mikrofoneingang**. | Derzeit nur Kern-/Fixture-Implementierung. Blackman-Harris, 48-kHz-Profil/Decimation, Stats 2–25Hz, Kalibrierung, Monitor-Ausgabe und vollständige Setupverwaltung fehlen. | JVM-Kotlin-Kompilierung + isolierter DSP-Fixture-Selbsttest PASS; End-to-End T-01–71 NOT RUN |
| UI und Navigation | `AppPanel.kt::AppPanel` enthält `MAIN`, `SEISMIC`, `SETTINGS`, `MORE`; `MainActivity.kt` `currentPanel`/Compose-`when`; es existiert **kein** `ANALYZER`-Eintrag. | Unterpanel „Spektrum & Mithören“, Zustände, Gesten, Cursor, Settings erst nach G1. Eine View darf Engine nicht besitzen. | G2 NO-GO; T-49/51/68/69 NOT RUN |
| Vorhandene Wasserfallfunktion | `WaterfallGenerator.kt` rendert aus vorhandener **WAV-Datei**, `FFT_SIZE=1024`, `HOP_SIZE=256`, 0–140 Hz, Inferno, L50/L90. | Nicht als kontinuierliche Live-Engine missdeuten. Nur bewährte FFT-/Farb-Hilfen nach Reuse-Prüfung adaptieren, keine konkurrierende WAV-Aufnahme. | T-01–43 NOT RUN |
| Radiushistogramme | `RadiusHistogramResult.kt`: `RadiusHistogramBlock(startTime,endTime,ratingCounts,noResponseCount)`; `RadiusHistogramResult(radiusKm,centerSource,observerCount,observationCount,blocks,observations)`; `MainActivity.kt::RadiusHistogramSummary()` zeigt bis zu 4 `blocks` in 2 Spalten, `ratingCounts` per Index 0–5 und -1 als `noResponseCount`. Abfrage `SupabaseClient.getRadiusHistograms()` nach Bewertung (bei aktivierter Radiusanalyse). | Read-only Snapshot mit echten Zeiten, Kennung aus echten vorhandenen Feldern (aktuell **keine native Block-ID**), stale/empty status; 0/1/4 Anzeige nur aus verfügbaren Daten. Keine erfundenen Radiusachsen/Hz. | G4 NO-GO; T-55–58, T-61 NOT RUN |
| PNG/Public Storage | `SeismicPlotExporter.kt` und `WaterfallGenerator.kt`: bestehende Android MediaStore/Download-Exportpfade; `Download/LFN-Scout/` ist offizielles öffentliches Ziel. | Analyzer eigener Immutable-Offscreen-State/PNG-Renderer und begrenzte Save-Queue, ohne Compose-Sichtbarkeit und ohne WAV. Bestehende Exportnamen nicht ändern. | G3 NO-GO; T-50/53/54/63/66 NOT RUN |
| Setups/Persistenz | `ReminderSettingsStore.kt` (DataStore) für App-Einstellungen; kein bestehender `AnalyzerSetupStore`/Recall. | Eigene versionierte Analyzer-Setups kompatibel zur bestehenden App, Mithören beim Neustart AUS. | T-17–21/67 NOT RUN |
| Paketidentität/Build | `app/build.gradle.kts`: `applicationId`/`namespace=de.brummbeobachter.app`, `versionName=0.4.85`, `versionCode=125`, min/target/compile 23/36/36, Java 17; `gradle/wrapper/gradle-wrapper.properties`: 8.13; CI `.github/workflows/build-android.yml`. | Die interne ID bleibt erhalten. Hier liegt ein Entwicklungsstand **ohne versionsseitige Release-Änderung** vor. | Nur reiner Kotlin-Kern lokal kompiliert; Android-Gradle/CI/Gerät NOT RUN |
| Bestandsdaten/Backend | `supabase/functions/{submit_observation,upload_wav,get_recent_observations,get_radius_histograms,get_all_observations}` und `supabase/migrations/` vorhanden; private DB-/Storage-Recovery-Daten ebenfalls. | Neue Analyzer-Funktion darf keine neuen unangeforderten Uploads/Secrets einführen; Private Recovery Master NICHT öffentlich hochladen. | bestehende Regressionen in dieser Phase NOT RUN |

### 6.1 Echte Konflikte und verbindliche Stop-Regeln

1. **G1:** `RollingAudioEngine.append()` verarbeitet bereits verstärkte, auf 1000 Hz reduzierte Samples. Das ist weder kalibriertes Roh-PCM noch die 48-kHz-Referenz. Eine spätere Analyzer-Integration darf diese Quelle nicht als 48-kHz-Rohsignal deklarieren.
2. **G1:** Bei inaktivem Rolling erzeugt die bestehende normale Aufnahme einen eigenen `AudioRecord`. Vor einem gemeinsamen Live-Analyzer MUSS ein zentraler Owner die Rate/Route aushandeln oder den Konflikt explizit ablehnen; keinen zweiten Recorder parallel starten.
3. **G2/G3:** Der bestehende WAV-Wasserfall ist keine Live-Engine und besitzt keinen offscreen, ansichtsunabhängigen PNG-Scheduler. Für `Save aktiv` ist ein separater Zustand samt begrenzter Queue notwendig.
4. **G4:** Die echten Radiusblöcke haben `startTime/endTime`, sechs Werte für `ratingCounts` und `noResponseCount`; es gibt keine native GUID/ID pro Block. Jede eingeführte ID muss als deterministisch **abgeleitete** Kennung kenntlich sein.
5. **Privacy:** Der Private Recovery Master enthält Beobachtungsdaten und 53 WAVs. Niemals als öffentlicher Git-Commit freigeben. Eine veröffentlichbare Code-ZIP muss diese privaten Inhalte ausschließen.

### 6.2 Phase-1-Befund / Abnahme

- Reiner DSP-Core: **IMPLEMENTIERT, isoliert mit Kotlin-Compiler und deterministischen Fixture-Checks getestet**. Seine FFT-/dBFS-Messwerte sind nicht im Android-Gerät kalibriert.
- Gemeinsamer Capture-Owner, Live-Panel, Mithören, Histogramme, PNG-Automatik, Setups: **NICHT IMPLEMENTIERT**.
- `T-01…T-71`: **NICHT ALS VOLLSTÄNDIGE TESTS AUSGEFÜHRT**. Isolierte Kern-Prüfungen ersetzen diese Akzeptanztests nicht.
- Freigabe: **NO-GO** für einen produktiven Live-Analyzer. Bestehende v0.4.85-App-Funktionalität wurde in dieser Phase nicht neu gestartet oder auf einem Gerät getestet.
