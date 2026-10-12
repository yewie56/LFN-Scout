# SDS – LFN-Scout Live-Spektrum-Analyzer

**Dokument-ID:** LFN-AN-SDS · **Version:** 0.7 · **Stand:** 19.09.2026 · **Status:** Architekturentwurf, **nicht** gegen den aktuellen Quellcode abgeglichen und nicht implementiert.  
**Normative Grundlagen:** `01_SRD_LFN-Scout_Analyzer_v0.7.md`, `02_SRS_LFN-Scout_Analyzer_v0.7.md`, `references/Funktionsreferenz_v0.4.md`, `04_UI_Gesten_v0.7.md`, `05_Algorithmen_Kalibrierung_v0.7.md`.

## 1. Architekturziele und Nichtziele

1. Neues **Unterpanel** in der vorhandenen App; keine zweite **Produktions**-APK, keine Änderung der LFN-Scout-Package-ID. Eine schlanke separate Android-**Testhülle** für denselben Kern ist zulässig. Bestehende Panels und die Semantik ihrer separaten Aufzeichnungsfunktionen bleiben erhalten; erforderliche interne Capture-Adapteränderungen sind zu testen.
2. Kontinuierliche **Live-Analyse**: Spektrum/Wasserfall auch bei Mithören AUS. Die aktuelle Mikrofonsignalkette ist grundsätzlich die einzige PCM-Quelle. **Kein WAV/PCM/Audio-Replay und kein kontinuierliches CSV-Logging.**
3. Klare Grenze: Messdaten dürfen weder durch Mithörfilter noch Nutzer-/Auto-Gain noch Noise-Anzeigekompensation verändert werden.
4. UI-/Datei-/Datenbankarbeit darf Audio-Callbacks nicht blockieren; auf schwächeren Geräten zuerst Renderfrequenz senken und Drops offen anzeigen.
5. Alte Klassen-/Paketnamen sind **nicht zu unterstellen**. Die nachfolgenden Bezeichner sind Architekturrollen und Interfaces, keine behaupteten Ist-Klassen.

## 2. Datenfluss und gesicherte Abzweigung

```text
       Android Live-Mikrofon / kompatibler gemeinsamer Capture-Service
             │ PCM, monotoner Capture-Zeitstempel, Geräte-/Routen-ID
             ▼
       [InputObserver: CLIP/Drops/Route] ──► Warnstatus
             │
             ├──────────────────────────────┐
             ▼                              ▼
       [MeasurementEngine]            [MonitorEngine]
       DC / Anti-Alias / Decimation    Bandpass ► User-Gain
       Fenster ► FFT ► PSD/RMS         ► Auto-Abregelung/Limiter
             │                         ► Mute/Fade ► AudioTrack
             ├─► Rohspektrum                 (nur Kopfhörer)
             ├─► optional Micro-Cal
             ├─► Average / MaxHold
             ├─► Bandpegel ► L50 / L90
             └─► Rohsample-Peak ► Superfast
                       │
                       ├─► [DisplayAdapter] ◄─ Noise-Referenz + optionaler
                       │    (nur Anzeige)      linearer Leistungsabzug
                       │              │
                       └─► [Renderer: Spektrum + Wasserfall + Overlay]
                                      ├─► Anzeige/Freeze/Cursor
                                      └─► [AutoPNG / manuelles PNG]
                            [CSV-Exporter] ◄─ definierter Roh- oder
                                               kalibrierter Snapshot
```

**Invarianten:** Messzweig hat keinen schreibbaren Verweis auf Monitor-Gain/-Filterzustand; `NoiseDisplayAdapter` darf weder die L50/L90-Pipeline noch Original-Average überschreiben. Visualisierung darf einen eingefrorenen Snapshot bearbeiten, aber Capture und Monitor laufen bei Anzeige-Pause weiter. Eingang- und Ausgangsclip getrennt führen. Ein vermeintlicher 0-Hz-Anzeigebeginn sagt nichts über Mikrofonhardware aus.

## 3. Modulverantwortungen und Vertragsvorschlag

| Architekturrolle (Platzhalter) | Eingabe | Ergebnis/Zustand | Darf ausdrücklich nicht |
|---|---|---|---|
| `AnalyzerPanelHost` | Panel-Navigation/Lifecycle | Öffnen, Sichtbarkeit, Session-Ende | andere Panels mutieren |
| `AudioInputAdapter` | vorhandener PCM-Stream oder Android-Live-API | PCM-Blöcke mit `sequence`, `monotonicNs`, effektiver Rate/Route | Audiodateien erzeugen; 2. Capture ohne Prüfung |
| `InputQualityObserver` | unveränderte PCM-Blöcke | `INPUT_CLIP`, Drops, effektive Route | Eingangsclip als behoben markieren |
| `SpectrumPipeline` | PCM, Analysekonfiguration | frequenzbezogene normierte PSD, RMS, Effektiv-Δf, Zeitbezug | Monitor-Ausgabewerte lesen |
| `CalibrationEngine` | Spektrum, gültiges Profil | Roh-/relativ korrigierte bzw. freigegebene SPL-Werte mit Gültigkeitsmaske | außerhalb Gültigkeit extrapolieren |
| `RollingStatistics` | bandbegrenzte gültige Pegel/Zeitgewichte | L50/L90 und Fortschritt | Noise-Abzug/Mithörgain verwenden |
| `PeakSampleObserver` | jeder PCM-Block **vor** Decimation | Sample-Max, Zeit, Reset | Werte als True Peak deklarieren |
| `MonitorFilter` | PCM, fMin/fMax, User-Gain | gefilterte Samples | auf Messpuffer schreiben |
| `MonitorSafety` | post-filter/post-gain Peak, Route | Auto-Absenkung, Limiter/Mute, Alarm | Mic-Gain/FFT ändern |
| `NoiseReferenceRepository` | Referenzdatei/Setup-ID | validierte PSD inkl. Metadaten | bei Missmatch still resamplen |
| `NoiseDisplayAdapter` | original PSD/Referenz | optional kompensierte Display-PSD + Gültigkeitsmaske | Originalwerte überschreiben |
| `AnalyzerStateModel` | Parameter, Gesten, Runtime-Status | immutable UI-Frames / Setup-Dirty | Audio-/Speicher-I/O im UI-Thread |
| `AnalyzerRenderer` | UI-Frames, Bands, ggf. Freeze | zwei Plotflächen + kompakte Zusatzkarten | Audio verarbeiten |
| `PngScheduleCoordinator` | monotone Wasserfall-Vollzyklen + `saveActive` | **maximal ein** Bildauftrag je vollständig neuem Zyklus | PCM/WAV/CSV-Dauerprotokoll erzeugen |
| `SnapshotExporter` | gerenderter Snapshot + Metadaten | PNG; CSV nur manuell | Audio persistieren |
| `AnalyzerSetupRepository` | Setup-ID, Version, Einstellungen | atomare lokale Speicherung/Recall/Import/Export | Monitor/Autosave beim Recall aktivieren |
| `AnalyzerDiagnostics` | Statusereignisse und synthetische Fixtures | PASS/FAIL, Drops, Performance | Live-Referenzgerätetest ersetzen |

**Interface-Festlegung:** Abstrakte Schnittstellen und Immutable-Events sind bindend als Verhaltensvertrag, tatsächliche Kotlin-Dateinamen und konkrete Dependency-Injection erst nach Repo-Inspektion bestimmen. Nicht ohne Bedarf das bestehende Buildsystem oder die Navigation neu schreiben.

## 4. Immutable Datenobjekte

```text
PcmBlock(sequence, captureMonotonicNs, sampleRateHz, routeKey,
         floatSamples, frameCount, inputClipFlags)
SpectrumFrame(captureStartNs, captureEndNs, frequencyHz[], rawPsd[],
              rawLevelDbfs[], correctedLevel[], correctedUnit,
              validMask[], inputClipFlags, configurationHash)
WaterfallRow(monotonicNs, calibratedOrRawDisplay[], validMask[], colorScaleId)
StatisticState(windowStartNs, windowEndNs, bandMinHz, bandMaxHz,
               l50, l90, unit, validity, fillFraction)
MonitoringState(enabled, route, userGainDb, attenuationDb, effectiveGainDb,
                outputClipRisk, outputClipCount, mutedReason)
DisplayState(liveOrFrozen, expandedWaterfall, cursorPositionHz,
             selectedCurve, overlays, saveActive, pauseStatus)
```

`floatSamples` ist flüchtig und besitzt einen **klaren Owner/Lebenszyklus**. Für Tests können PCM-Fixtures aus Ressourcen zugespielt werden, ohne in der App eine Audio-Aufzeichnungsfunktion einzubauen.

## 5. Zeit und Nebenläufigkeit

- Ein einmaliger Eingangserwerb oder sichere gemeinsame Nutzung des im Repo bereits existierenden Captures; bei Rate-/Routenwechsel atomarer Konfigurations-Switch und Löschung inkonsistenter Anzeigehistorie.
- Kritischer Capture-/Monitorpfad auf audiofähigen Threads; Analyse mit bounded queue und kontrollierter Last; Renderer erhält neueste komplette Frames; PNG-I/O asynchron auf Dateithread.
- Zeitbasis: monotone Capturezeit für L50/L90, Average, Wasserfall und Save-Intervalle; UTC für Dateinamen/Metadaten. Beim Zeitwechsel dürfen Intervalle nicht springen.
- `Pause` friert einen **vollständigen Anzeigezustand** (Spektralwerte, Wasserfall, L50/L90-Ausgabe) ein. Cursor lässt sich über die eingefrorenen Werte verschieben. Monitor läuft optional weiter; dessen Clipwarnung kann als Live-Sicherheitsindikator unabhängig sichtbar bleiben und muss als solcher beschriftet sein.
- Touch-Gesten konkurrieren nicht mit UI-Paint: Eingaben verändern nur UI-/Monitor-Konfigurationszustand. Änderungen am IIR-Filter erfordern stabilen Koeffizientenwechsel (Crossfade oder zulässige Glättung).

## 6. Zustandsautomaten

| Zustand | Ereignis | Folgezustand | Wirkung |
|---|---|---|---|
| `LIVE_OFF` | Panel aktiv + Eingabe freigegeben | `LIVE_RUNNING` | flüchtige Blöcke verarbeiten |
| `LIVE_RUNNING` | Pause | `DISPLAY_FROZEN` | UI-Bild eingefroren, Cursor bedienbar |
| `DISPLAY_FROZEN` | Fortsetzen | `LIVE_RUNNING` | neuester Live-Frame, Save-Wasserfallzyklus neu starten |
| beliebig | Capture-/Routenfehler | `LIVE_ERROR` | sichtbar, Monitor Mute, keine Scheindaten |
| `MONITOR_OFF` | Tap + sichere Route | `MONITOR_ACTIVE` | kurzer Ramp-in, Mute bis Filter validiert |
| `MONITOR_ACTIVE` | Tap / Route verloren / Audiofokus weg | `MONITOR_OFF/MUTED` | sofort/kurz kontrolliert Mute |
| `SAVE_OFF` | explizit aktivieren | `SAVE_FILLING` | Zähler seit Aktivierung 0 |
| `SAVE_FILLING` | kompletter gültiger Zyklus | `SAVE_ACTIVE_CYCLIC` | 1. PNG, Zyklus neu |
| `SAVE_ACTIVE_CYCLIC` | weiterer kompletter Zyklus | unverändert | genau ein weiteres PNG |
| `SAVE_...` | Pause / Zeitspanne ändern | `SAVE_SUSPENDED/REFILL` | keine Duplikate; neuer Vollzyklus |

**Sichere voreingestellte Entscheidung:** nach App-Start/Recall sind Monitor und tatsächliches Autosave OFF; eine im Setup gespeicherte Save-Präferenz allein startet keine Dateioperation. Vor einer anderen Produktentscheidung explizite Freigabe und Testplanänderung.

## 7. Persistenz und Dateiformate

- Mehrere Setup-JSONs oder ein Repository mit gleicher Semantik, UUID, `schemaVersion`, `revision`, `dirty`, `lastUsedSetupId`; erst validieren, dann atomar ersetzen.
- Eigene Kalibrierprofile mit Messkettenbindung und Gültigkeitsband; Noise-Profile mit passender Rate/FFT/Fenster/PSD-Deklaration; beide via stabiler ID referenzieren, niemals ohne Kompatibilitätsprüfung übernehmen.
- PNG in App-Ordner/SAF mit eindeutiger Datei und sidecar-Metadaten oder eingebetteten Feldern; Speicherung gilt als abgeschlossen erst nach erfolgreichem Write+Close/atomarem Rename. Absturz darf kein korruptes gültig benanntes PNG hinterlassen.
- CSV nur **manuell**, eindeutig ausgewiesener `raw`/`calibrated`-Modus, Frequenzen/Einheiten/valid mask, niemals unbenannter Wechsel auf noise-korrigierte Daten.
- Keine PCM-Audiodaten auf Platte oder in Cloud; begrenzte RAM-Historie sowie datenschutzbewusste Logs ohne Mic-Payload.

## 8. Fehler-/Fallback-Matrix

| Ereignis | Standardreaktion | Abnahmekriterium |
|---|---|---|
| `UNPROCESSED` nicht verfügbar | kompatiblen Fallback zeigen, nicht still behaupten | gewählte und tatsächliche Quelle sichtbar |
| Mic/USB-Route wechselt | SPL und Noise-Kompatibilität neu prüfen | keine ungültigen SPL-Zahlen |
| Headset entfernt/unklare Route | Mithören unmittelbar Mute/AUS | kein ungewolltes Lautsprecher-Mithören |
| `INPUT_CLIP` | dauerhaft für betroffenes Datenfenster markieren | keine automatische Eingangsänderung |
| Mitgehörtes Signal Clip-Risiko | ausschließlich Ausgangs-Gain absenken, limitieren | Messwerte unverändert |
| L50/L90-Band nicht unterstützt | `ungültig/nicht kalibriert` mit Bereichsangabe | keine erfundenen Messwerte |
| Referenz unpassend / fehlt | Noise-Abzug aus, Warnung, Rohspektrum bleibt | alte Datei unverändert |
| Speicherziel voll/gesperrt | Save-Fehler und ggf. Save OFF; Live bleibt | kein Loop von Dateiaufträgen |
| hohe Last | UI-FPS reduzieren, Drop offen melden | Audio/Monitor sicher |

## 9. Integration in vorhandenen App-Code (Gate)

Vor Umsetzung sind **tatsächliche** Ausgangsdateien festzuhalten: `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/`, Gradle/SDK, vorhandene UI/Audio-/Persistenzklassen, Supabase- und Bewertungspfade, Tests und GitHub Actions. Die Integrationsanweisung enthält ein auszufüllendes Mapping-Formular. `docs/superpowers/` ist **nicht** die verbindliche fachliche Projektdokumentation. Bis dahin sind die Namen in §3 lediglich Entwurfsbezeichnungen. Kein Versprechen eines Builds oder bestätigter Klassen-Reuse.

## 10. Implementierungsreihenfolge und Meilensteine

1. Repo-Inventory + Tests/Build unverändert grün; UI-Unterpanel-Shell mit Fake-Frames.
2. Gemeinsamer Live-Capture + Spectrum/Waterfall/Ringbuffer + lineare Skalen + Pause.
3. Independent Monitor + Route + Filter-Overlay + Clip-/Abregelung + Gesten.
4. Kalibrierung und Noise-Profile + Kompatibilitäts-/PSD-Prüfung.
5. Average/MaxHold/Cursor, L50/L90, Superfast, PNG-Zyklus und CSV-Snapshot.
6. Mehrfach-Setups/Recall + vollständige Migration + UI-Ausbau.
7. Regression, Audio-Gerätevalidierung und Build entsprechend **aktuellem** `BUILD_WORKFLOWS.md`, vollständiger Doku-Abgleich/Changelog.

**Status:** Architektur spezifiziert, keine implementierte Änderung, keine gemessene Performance oder Mikrofonbandbreite.


## 11. Verbindliche Erweiterungsarchitektur v0.6 – Capture, Besitzer und Panelwechsel

Diese Sektion erweitert den v0.6-Entwurf (§1–10); bestehende Namen sind Rollen, **keine verifizierten Klassen im LFN-Scout-Code**.

### 11.1 Ein Audio-Capture, mehrere Abnehmer

```text
Android Mic / USB ──► [CaptureOwner: EINE aktive Eingangskonfiguration]
                            │ format, seq, tMonotonic, route, clipFlags
                            ▼
                      [PCM Fan-out / bounded ring]
                            │
                ┌───────────┴─────────────┐
                │                         │
                ▼                         ▼
       AnalyzerMeasurement           ExistingRecorderAdapter
          │                              │
          ├── live FFT/PSD               └── bestehender Datei-Recorder
          ├── Wasserfall                     Original-PCM bzw. dokumentierte
          ├── Superfast                      normierte Umwandlung
          ├── L50/L90
          └── MonitoringBranch ─► nur Kopfhörer, Gain/AGC isoliert
```

- CaptureOwner hat **allein** Start/Stop/Route-Konfigurationsrecht. Jede aktive Verbrauchersession hält ein Lease: `AnalyzerSession`, `RecorderSession`, ggf. weitere bereits vorhandene Nutzer. `release()` des Panels kann einen Capture mit offenen Leases nicht beenden.
- Rohsamples im Capture-Puffer sind unveränderliche, zeitgestempelte Blöcke. Abnehmer erhalten eigene Read-Cursors; Backpressure verwirft nur den betroffenen Abnehmerauftrag mit `DROPPED_FRAMES`, niemals mutiert sie bereits an einen anderen Abnehmer zugestellte Recorder-Samples.
- Ein bestehender Recorder kann bislang eine eigene Datei-/Capture-Kopplung besitzen: **Ist-Adapter im aktuellen Repo ermitteln**, dann auf gemeinsame PCM-Quelle umstellen. Container/Codec/Dateinamens-/Zeit-/Cloud-Semantik bleibt unverändert; Migration benötigt Regressionstest.
- Vorrang bei Konflikt: eine laufende Datei-Aufnahme darf nicht durch ein Analyzer-Setup die Eingangsrate/Route verlieren. Analyzer stellt bei Bedarf **seinen eigenen Analysepfad** mit geprüfter Anti-Alias-/Resampling-Stufe um. Nicht konvertierbarer Konflikt → UI-Fehler und explizite Entscheidung; keine falsche 2–25-Hz-Gültigkeit.
- Externe Aufnahme-Apps liegen außerhalb dieser appinternen Fan-out-Garantie. Android kann bei mehreren Apps einen Capture muten; das wird nicht durch einen zweiten AudioRecord innerhalb von LFN-Scout „gelöst“.

### 11.2 Session- und Renderer-Lebenszyklus

```text
LFN-Scout Haupt-App (sichtbar)
  ├── AnalyzerSessionCoordinator [engineStatus, saveActive, pause, consumers]
  │      ├── SpectrumEngine + StatisticEngine + WaterfallHistory (RAM)
  │      ├── HistogramSnapshotStore (Read-only, ≤4 aktuelle abgeschlossene)
  │      └── PngScheduleCoordinator → SnapshotAssembler → OffscreenRenderer → AsyncWriter
  ├── AnalyzerPanel [subscribe visible frames; onHide unsubscribe Paint ONLY]
  ├── OtherPanel [eigenes UI; Audioaufnahme via denselben PCM Fan-out]
  └── TestHostAdapter (nur separate Testhülle)
```

`onHide()` trennt ausschließlich View-/Paint-Subscriber. Wenn Analyzer-Sitzung aktiv (insbesondere Save), werden Ringpuffer, Statistik- und Wasserfall-Writes weitergeführt. `onShow()` liest den neuesten immutable `AnalyzerFrame`. Render-FPS im versteckten Panel = 0; PNG-Offscreen-Rendering erfolgt nur zum Save-Ereignis. Eine Anzeige-Pause friert die Bildschirmansicht ein und stoppt **Auto-PNG**, nicht PCM-/Recorder-/Monitoringpfad. Ein interner Panelwechsel ist keine Anzeige-Pause.

### 11.3 Zustands- und Bedarfshaltervertrag

| Capture-Consumer | Analyzer sichtbar | Analyzer versteckt | Recorder aktiv | Freigabe |
|---|---|---|---|---|
| Analyzer-Session aktiv | Engine + optional Render | Engine, Render AUS | unverändert | ausschließlich explizites Analyzer-Stoppen/App-Session-Ende |
| `Save aktiv` | Engine + periodisches PNG | Engine + periodisches Offscreen-PNG | unverändert | Save AUS beendet nur PNG-Bedarf, nicht ggf. Analyzer-Session |
| Recorder aktiv | PCM-Recorder | PCM-Recorder | `RecorderSession` Lease | Recorder-Stop; Analyzer läuft weiter wenn aktiv |
| Mithören aktiv | Monitorbranch | Monitorbranch nach Nutzerwahl | Recorder unangetastet | Tap AUS / sichere Route weg / Session-Ende |

**App-Hintergrund:** Bis zum Android-Service-Gate keine Aussage „läuft bei Bildschirm aus“. Der appinterne Panelwechsel während sichtbarer App funktioniert ohne zwingend zusätzlichen Foreground Service; für fortgesetzte App-Hintergrund-Erfassung müssen Startzeitpunkt, Mikrofon-FGS-Typ und Berechtigungen entsprechend Android-API geprüft werden (siehe `12_Quellen_Referenzen_v0.7.md`).

### 11.4 Offscreen-PNG-Zeitbasis und Snapshot-Transaktion

1. Beim expliziten Einschalten `Save aktiv`: monotones Zeitfenster `T_wf` neu beginnen und Wasserfallhistory für die PNG-Vollperiode füllen.
2. Bei genau `T_wf` gültiger Live-Historie: `snapshotId` sowie `captureIntervalStart/End` atomar festhalten, zum selben Zeitpunkt die letzten maximal vier **vollständigen** Histogramm-Snapshots kopieren und eine bildunabhängige `RenderState`-Kopie bilden.
3. Ein Offscreen-Renderer rendert die gewählte Analyzer-Hauptbildvariante in definierter Ausgabeauflösung. Es wird **nie** `Activity.takeScreenshot()`/sichtbarer fremder View abgegriffen.
4. Writer schreibt PNG + Metadaten zunächst temporär, finalisiert atomar soweit vom Ziel/SAF unterstützt; meldet `SUCCESS`, `WRITE_FAILED`, `SKIPPED_QUEUE_FULL` mit ID und Zeit.
5. Nach dem vollständigen nächsten `T_wf` folgt genau ein weiterer Snapshot. Kein Nachholen vieler identischer Bilder bei Dateistau. Live-PCM darf niemals wegen Dateischreiben warten.
6. Bei Anzeige-Pause Save aussetzen und nach Resume eine neue vollständige gültige Periode abwarten. Bei internem Panelwechsel unverändert weiterlaufen.

### 11.5 Histogramm-Read-Adapter

Siehe `14_Histogramm_Integration_v0.7.md` für `HistogramSnapshot`-Vertrag. Alle Daten stammen aus **dem bestehenden Histogramm-Repository/Publisher**, nicht aus UI-Widgets; tatsächliche Einheit, Radius-Bins, Zeit-/Ortsbezug und Aktualisierungslogik sind vor Implementierung zu mappen. `lastFour()` ist eine unveränderliche Kopie, neueste zuerst. Bei fehlen­den Datensätzen zeigen UI und PNG denselben **„keine Daten“/„veraltet“**-Status. Das Scrollen des Wasserfalls verändert die Histogrammzuordnung nicht.

### 11.6 Simulierbare Testhülle

Die Standalone-Testhülle enthält weder zweite Produktionsdatenbank noch zweite Build-Wahrheit. Sie injiziert `PcmSource`, `HistogramSource`, `FakeMonotonicClock`, `SnapshotSink`; benutzt ansonsten denselben DSP-/Save-/Setup-Code wie das reale Unterpanel. Entwicklung: Tests mit Fixtures → gemeinsamer Capture/Recorder in LFN-Scout → Panels/PNG/Histogramme auf echter App. **Keine Zusage paralleler Fremd-App-Captures.**

## 12. Zusätzlicher technischer Integrationsvertrag v0.7

1. **Core-Identität:** Dieselbe versionierte Analyzer-Core-Implementierung wird von Testhost und LFN-Scout eingebunden. Keine aus den Mockups abgeleiteten parallelen DSP-Berechnungen im UI-Layer.
2. **PCM-Fan-out:** Ein Capture-Owner liefert sequenznummerierte, monotone zeitgestempelte PCM-Blöcke mit Gerätekennung, effektivem AudioSource, Rate, Kanälen, Drop-/Clipindikator. Bestehender Recorder und neuer Analyzer erhalten **unveränderte** PCM-Samples bzw. wohldefinierte kompatible Kopien. Jede Adaptertransformation ist versioniert und protokolliert.
3. **Ressourcenvertrag:** Mehrere Consumer dürfen die Erfassung nicht durch UI-Lifecycle-Events beenden; Besitzer-/Lease-Regeln müssen eventuelle Race Conditions beim Start/Stop auflösen. Keine blockierende Datei-I/O im Audio-Callback.
4. **PNG-Transaktion:** Ein immutable `AnalyzerRenderSnapshot` enthält Frequenzachse, Spektrum, Average, Cursor, L50/L90, Wasserfallmatrix samt Zeitachsen/-Einheiten, kalibrierungs- und referenzabhängige Labels, Mithör-Overlay, tatsächliche Save-/Input-Clip-/Monitor-Statusanzeige und gegebenenfalls vier Histogramm-Snapshots. Die PNG-Erzeugung nutzt denselben Layout-/Rendervertrag auch ohne sichtbares Analyzer-Panel; bei unvollständigem Datensatz entsprechende Zustandsmarkierungen.
5. **Memory-Gate:** Historie und Ringpuffer für Wasserfallzeit dürfen bei erhöhtem FFT-/Superfast-Lastprofil nicht unbegrenzt wachsen. Samples pro Consumer, Queue-Policy, max. Speicherbedarf und Ausfallverhalten auf Referenzhandy nachweisen.
6. **Source-Konflikte:** Bei inkompatibler Recorder-Rate/-Quelle keine spontane Unterbrechung eines aktiven Recorders. Entweder Analyzer-only-Neuberechnung aus kompatibler gemeinsamer Quelle oder mit verständlichem Fehler und Nachweis. `UNPROCESSED` ist **präferiert, nicht garantiert**.
7. **Android-Lebenszyklus:** „Internes Panel unsichtbar“ ist nicht gleich „App im Hintergrund“ oder „Bildschirm aus“. Nur die erste Betriebsart ist hier zugesagt; andere Betriebsarten sind gesonderte, geräte-/SDK-spezifisch zu prüfende Erweiterungen.
8. **Histogrammprovider:** Nur kopierte, immutable Datenobjekte mit eindeutiger ID/Generation, Radius-/Zeitdefinition und Zustand. View-Koordinaten oder Screenshots eines anderen Panels sind keine Datenschnittstelle.

Vor Codeeingriff ist das echte Ist-/Soll-Mapping in `16_Repo_Mapping_und_Gates_v0.7.md` auszufüllen. Alle oben verwendeten Typbezeichnungen sind **Designbeispiele**, keine Behauptung über bestehende Klassen.

