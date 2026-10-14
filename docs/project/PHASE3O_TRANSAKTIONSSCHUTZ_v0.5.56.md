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
