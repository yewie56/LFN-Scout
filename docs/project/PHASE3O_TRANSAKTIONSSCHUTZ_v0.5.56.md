## Phase 3O/11 – Audio-Owner-Abweisung (2026-10-14)

- Nutzer bestätigt Phase 3O/10 GitHub TEST **GRÜN**. Phase 3O/11 GitHub TEST und Gerätetest offen.
- `AnalyzerSetupAudioOwnerGate.kt` liest ausschließlich `MicCaptureArbiter.owner`, `RollingAudioEngine.isRunning`, `AnalyzerDirectAudioService.isCapturing` und `AnalyzerMonitorRecording.isActive()`.
- `AnalyzerSetupSwitchTransaction.execute` verweigert den Vorgang bereits bei einem dieser aktiven Zustände; auch ein Fehler beim Abfragen führt zur Ablehnung. Keine Recorder-Start-/Stop-Aktion, kein Preference-/JSON-Schreiben.
- **Keine Atomarität:** Die getrennten Service-/Recorder-Übergänge verwenden bisher kein gemeinsames Transaktions-Lock. Ein scheinbar freier Zustand ist ausdrücklich **keine** Erlaubnis zur Übernahme. `Plan.executable=false`, keine Adapter- oder UI-Freigabe.
- Statische Regression `tools/verify_lfn056_phase3o_owner_gate.py` (9 Prüfungen); Kotlin CI und dynamische Rennen-/Gerätetests offen.
- Historisches Storage-Backup bleibt in GitHub `main`, nicht in der Quell-ZIP. Gradle `0.5.55/213` und Termux v4 unverändert.


## Phase 3O/10 – STOP-Konfiguration bei fehlgeschlagenem Session-Aufbau (14.10.2026)

- Nutzerbestätigung: **Phase 3O/9 GitHub TEST GRÜN**; Phase 3O/10 GitHub TEST noch offen.
- `LiveAnalyzerController.start()` erfasst vor dem Laden der Präferenzen einen gemeinsamen STOP-Checkpoint. Falls `LocalAnalyzerAudioBuffer.start()` oder `newSession()` einen Fehler auslösen, wird nach Best-effort-Cleanup die zuvor erfasste DSP-/Audio-/Timing-Konfiguration wiederhergestellt. Rücknahme findet nur unter Controller-Lock bei `running=false` statt, ohne `SharedPreferences`- oder JSON-Schreibzugriff. Fehler in der Rücknahme werden dem ursprünglichen Fehler als suppressed hinzugefügt.
- **Grenzen:** Kein allgemeiner Rollback bei Fehlern im früheren Pref-/Konfigurations- und Validierungsabschnitt; keine Recorder-/Service-/Hardware-Rücknahme und keine Freigabe der Setup-Umschaltung. Die getrennten Aufnahmeeigentümer (Scout, MON, Rolling) sind noch nicht unter einem gemeinsamen Lock verbunden.
- Statische Regression: `tools/verify_lfn056_phase3o_start_rollback.py`; Kotlin-/Android- und Fault-Injection-Test offen.
- GitHub `main` behält das historische `supabase/storage_backup_before_v0526`; Entwicklungs-ZIPs enthalten es nicht. Bei fehlenden Dateien in der ZIP **nicht löschen**. Termux v4, Gradle 0.5.55/213, Workflows und Cloud unverändert.

# Phase 3O – Teilstufe: Transaktionsschutz, noch keine Audio-/FFT-Aktivierung

## Befund aus dem vollständigen Phase-3N-Quellstand

`LiveAnalyzerController.start(context)` liest Einstellungen erneut aus `lfn_scout_live_analyzer` SharedPreferences. Bestehende Setter `setDirectCapture`, `setDirectDecimation`, `setDsp`, `setTiming`, `setFilterConfig`, `setHistorySeconds` und weitere schreiben Einstellungen persistent. STOP/SET/START wäre daher keine speicherfreie Übernahme und könnte trotz vermeintlichen Rollbacks persistente Konfiguration verändern. `stop()` schließt die Analyzer-Session und den lokalen Buffer; die Mikrofon-/Rolling-/Recording-Hoheit liegt außerhalb des Analyzer-Controllers. Eine Kontrolle nur anhand von `LiveAnalyzerController.Snapshot.running` verhindert keine Aufnahme-Race-Conditions.

## Tatsächlich neu

`AnalyzerSetupSwitchTransaction.kt`: unabhängige fail-closed STOPPED-Transaktionsgrenze mit Adaptervertrag für ein mit Recorder-Übergängen *gemeinsames* Lock, getrennte Scout-/MON-Aufnahmesperren, MON-/Display-Sperre, unmittelbar erneuten Saved-/Live-Fingerprintvergleich und erneute Legacy-Preflight-Validierung. Kein Adapter angeschlossen. STOP-pflichtige Werte und Anzeigeänderungen werden absichtlich abgewiesen. Die Schnittstelle enthält Checkpoint-/Restore-Hooks als späteren Vertrag, sie werden nicht als implementierter Rollback ausgegeben. Bereits bestehender Anzeige-Draft-/Preview-Pfad unverändert.

**Wichtig:** Diese Teilstufe führt ausdrücklich **keine** Audio-/FFT-Übernahme durch. `Plan.executable` bleibt `false`. Keine STOP-/START-, Persistenz- oder Rollback-Aktion, keine UI-Aktivierung, keine Cloud-/Recorder-Änderung. Dies ist eine begrenzte Teilphase, nicht die abgeschlossene Phase 3O.

## Freigabesperren / nächste Integration

1. Audio-Owner/Scout-Rolling/MON-Aufnahme und Übergänge unter einem gemeinsamen Lock oder gesichertem Service-Protokoll koordinieren, echte Zustände erneut lesen.
2. Controller-Konfiguration einschließlich FFT, Filterbank, Direct-Mode und Mode-Wechsel vollständig und *ohne* SharedPreferences-Write injizierbar machen; Start darf während Setup-Apply nicht aus Preferences rückladen.
3. Ganze Legacy-Konfiguration einschließlich typisierter Werte, Kombinationsgrenzen (direkte Sample-Rates/Decimation, FFT-Hops, Filterbank und History) validieren; fehlende/null Werte nicht ergänzen.
4. Tiefen Runtime-Checkpoint, fehlschlagbare Reinitialisierung, tatsächliches Rollback mit gesonderter Statusmeldung bei Rollback-Fehler und verifizierten readback-Werten erstellen. Kein automatischer Neustart ohne ausdrückliche Freigabe.
5. JVM-Unit- und Gerätetests: Aufnahme-Races, stale state, Präzision, ungültige Werte, Rollback-/Rollback-Fehler, unveränderte Preferences und JSON, 8/48-kHz-Wechsel, Filterstabilität, no-op, Prozessneustart. Vor Freigabe GitHub TEST und Android-Gerätetest ausführen.

## Prüfgrenze

`tools/verify_lfn056_phase3o.py` prüft ausschließlich die statischen Sperrverträge. `tools/verify_lfn056_phase3n.py` prüft die unveränderte lesende Planung. Keine Kotlin-Kompilation, kein eigener GitHub-Build oder Gerätetest für diese Lieferung behauptet.

## Fortsetzung 3O – Präzisions- und Vollständigkeitsschutz (zweite Teilstufe)

Der bisherige numerische Vergleich mittels `Number.toDouble()` ist in `AnalyzerSetupSwitchPreflight` und `AnalyzerRuntimeSetupSnapshot.compare` durch `AnalyzerSetupExactComparison.same` mit `BigDecimal` ersetzt. Der Vergleich erhält Ganzzahlpräzision und erlaubt numerisch gleiche Schreibweisen (z. B. 10 und 10.0). Zahlen werden nicht mit Strings gleichgesetzt. Zusätzliche Laufzeitschlüssel, die im gespeicherten Legacy-Objekt fehlen, werden in der Preflight als nicht vollständig vergleichbar gemeldet; dadurch gilt ein partieller JSON-Entwurf nicht als vollständig aktiviertes Audio-/FFT-Setup. `Unchanged` verlangt beidseitig gleiche Schlüssel und Werte, einschließlich Null-Status.

`AnalyzerSetupExactComparisonTest.kt` ergänzt vier **noch nicht lokal ausgeführte** JUnit-Tests. Quellprüfungen werden getrennt protokolliert. Diese Ergänzung ist keine Freigabe eines Audio-Adapters und implementiert insbesondere **keine tatsächliche speicherfreie Übernahme, keinen Rollback und keinen Neustart**. Bei partiellen Setups kann die strengere Vorprüfung absichtlich auch einen No-op verweigern. Außerdem bleibt die Umwandlung der Filtergrenzen zu `Int` in `AnalyzerRuntimeSetupSnapshot.capture` eine bekannte Präzisionslücke, die vor Audio-Aktivierung zu beheben ist.


## Phase 3O – dritte Teilstufe: Legacy-Grenzwertschutz (14.10.2026)

`AnalyzerRuntimeSetupSnapshot.capture` darf Filtergrenzen nicht mehr stillschweigend von `Double` nach `Int` abschneiden. `exactLegacyFilterHz` akzeptiert nur endliche, exakt ganzzahlige Werte im Int-Bereich; bei echten Bruchteilen bricht die *lesende* Erfassung mit einer klaren Fehlermeldung ab. Grund: Das aktuelle Legacy-JSON-Schema deklariert `filter_min_hz` und `filter_max_hz` als Integer. Dieser Guard dient der Integrität, **ist keine Unterstützung für Bruch-Hz**. Für eine vollwertige Übernahme muss die Schema-/Controller-Frage explizit geklärt werden. `AnalyzerSetupFormat` prüft Integerwerte jetzt über `BigDecimal.toBigIntegerExact()` statt verlustbehaftetem `Double`. Vier JUnit-Regressionsfälle hinzugefügt, deren Ausführung durch Gradle aussteht. Keine Änderung an Recordern, Audiopipeline, SharedPreferences, JSON-Dateien, Workflows oder Gradle-Version. `Plan.executable=false`, kein Adapter aktiv.

## Phase 3O – vierte Teilstufe: STOP-gebundene speicherfreie DSP-Setter (14.10.2026)

`LiveAnalyzerController.setDsp` und `setFilterConfig` erhalten jeweils das optionale Argument `persist: Boolean = true`. Damit bleibt der bisherige Aufrufpfad kompatibel und schreibt wie zuvor Legacy-SharedPreferences. Nur bei ausdrücklich gesetztem `persist=false` werden keine Preferences geschrieben. Der FFT-Draft-Pfad setzt STOP unter dem Controller-Lock voraus; die Filtergeometrie war bereits auf STOP und den 8-kHz-Modus beschränkt. Filter-Validierung über `FastFilterBank.Config` und FFT-Grenzprüfung bleiben vorhanden.

**Grenze:** Dies sind zwei interne speicherfreie Laufzeit-Setter, kein freigegebener Audio-/FFT-Setup-Switch. `start(context)` liest weiterhin die persistente Konfiguration erneut und überschreibt dadurch diese Draft-Werte. Es wurde kein STOP-/START-/Rollback-Adapter angeschlossen, der Transaktionsplan bleibt nicht ausführbar. Ohne den Recorder-Owner gemeinsam genutztes Lock und tiefe Zustands-Rücknahme darf der neue Weg nicht von der Setup-UI als vollständiger Wechsel aufgerufen werden. Die bisherige Aufnahmepipeline und der Direct-48k-Service bleiben unverändert.

Neue statische Regression `tools/verify_lfn056_phase3o_draft_setters.py` prüft die persist-Guards, STOP-Schranken, Plan-Sperre und Versionskonstanz; sie kann Kotlin-Kompilation, dynamische Rollbacks oder Audio-Hardwareverhalten nicht beweisen. GitHub TEST und Smartphone-Funktionstest für diese Teilstufe stehen aus.

## Teilstufe 5 – Startübernahme eng begrenzter DSP-Werte
`start(context, dspOverride: DspStartOverride? = null)` liest zunächst den Legacy-Preferences-Snapshot wie bisher und überschreibt vor `newSession()` ausschließlich FFT-Größe, Fenster, Smoothing und optional alle drei Filterbankwerte (nur im 8-kHz-Modus). Dies ist ein **speicherfreier, auf den aktuellen Start begrenzter** API-Pfad, kein JSON-Setup-Adapter. Bei `null` bleibt das historische Verhalten. Die Validierung ist im Controller-Lock und prüft FFT-Whitelist, endliche Smoothing-Zeit, Filter-Vollständigkeit und bestehende `FastFilterBank.Config`-Bedingungen. Kein Codec-/Audio-Moduswechsel, keine Recorder-Sperre und kein Rollback; die Funktion darf nicht an die JSON-Setup-UI angebunden werden. Der lesende Plan bleibt nicht ausführbar. Es bleibt möglich, dass spätere Initialisierung anderer Timing-Werte einen Entwurf unbrauchbar macht; kombinierte Konfigurationsvalidierung bleibt vor Freigabe nötig.

## Teilstufe 6 – STOP-DSP-Checkpoint (14.10.2026)

Nach vom Nutzer gemeldetem grünem GitHub-TEST der Teilstufe 5 wurden im `LiveAnalyzerController` die Methoden `captureStoppedDspCheckpoint()` und `restoreStoppedDspCheckpoint(checkpoint)` ergänzt. Beide sind durch den vorhandenen Controller-Lock geschützt und nur im Zustand STOP erlaubt. Der unveränderliche Checkpoint erfasst ausschließlich FFT-Größe, Fenster, Smoothing und im 8-kHz-Modus Filtergrenzen/-bandanzahl sowie die Betriebsart als Restore-Schutz. Die Rücknahme prüft den Modus und **alle** DSP-Werte, bevor Felder überschrieben werden. Es findet kein SharedPreferences-Write, Session-Neustart oder Recorder-Zugriff statt.

**Grenzen:** Checkpoint/Rücknahme betreffen nur Controller-DSP-Felder, nicht sämtliche Audio-Timing-/Direct-Mode-/Recorder-/Service-Zustände; kein transaktionaler Gesamtswitch, keine Wiederherstellung externer PCM-Subscriptions, keine Garantie nach Prozessabsturz. `start()` liest weiterhin Legacy-Preferences vor Übernahme des optionalen DSP-Overrides. Der Phase-3N-Plan bleibt `executable=false`; UI-/Transaktionsadapter nicht angeschlossen. Ein vorbestehender oder neu entstandener Fehler beim echten Session-Start kann durch diesen kleinen Checkpoint allein nicht zuverlässig rückgängig gemacht werden. Keine Annahme eines grünen Build-/Gerätetests für Teilstufe 6.

Neue statische Regression: `tools/verify_lfn056_phase3o_checkpoint.py`. GitHub-TEST und vollständige dynamische Rollback-Tests bleiben offen.

## Teilstufe 7 – lesender STOP-Audio-/Timing-Checkpoint (14.10.2026)

Erweitert die Checkpoint-Abdeckung über DSP hinaus, **ohne** ein ungetestetes Wiederherstellen vorzutäuschen. `StoppedAudioTimingCheckpoint` enthält Direct-Modus und -Eingangsrate, Direct-Smoothing/Marker/DC/Wakelock/Zeitsmoothing, Decimation, FFT-Hop, effektives Wasserfallintervall, Historienlänge und Analyse-Gain. `captureStoppedAudioTimingCheckpoint()` verlangt STOP; `isStoppedAudioTimingCheckpointCurrent()` vergleicht alle Felder unter demselben Lock, liefert bei RUN `false`. Beide Funktionen sind lesend, ändern keine Persistenz und greifen nicht auf Recorder/Audio-Session zu. Es gibt ausdrücklich noch **keine Restore-Methode für diese zusätzlichen Werte**, da Service-/Recorder-Koordination und konsistente Validierung fehlen. Die vorhandene DSP-Rücknahme bleibt unverändert. Statische Regression `tools/verify_lfn056_phase3o_audio_timing_checkpoint.py`; Kotlin-Build und Geräteprüfung ausstehend. `CURRENT_STATE.md` erstmals als verpflichtende aktuelle Übergabedatei angelegt.

## Teilstufe 9 – Fehlerbereinigung beim Analyzer-Sessionstart (14.10.2026)

`LiveAnalyzerController.start` setzt `running=true` nun erst **nach** erfolgreichem `LocalAnalyzerAudioBuffer.start()` und `newSession()`. Fehler bei diesen beiden Schritten führen zur best-effort-Bereinigung von Session und lokalem Buffer, `session=null`, `running=false`, `paused=false` und anschließendem Weiterwerfen des ursprünglichen Fehlers. Die Änderung ist bewusst auf die Session-Initialisierung begrenzt. Es findet kein Preference-Write statt und keine automatische Änderung am Recorder oder am Mikrofon-Service. Bereits geladene interne Controller-Konfigurationsfelder sowie Vorgänge außerhalb des abgesicherten Blocks werden NICHT zurückgerollt. Insbesondere ist dies kein atomarer, externer Audio-Rollback und gibt den Setup-Executor nicht frei (`Plan.executable=false`).

Neue statische Regression: `tools/verify_lfn056_phase3o_start_failure.py`; ein echter injizierter Session-Startfehler und Hardwaretests stehen aus.
