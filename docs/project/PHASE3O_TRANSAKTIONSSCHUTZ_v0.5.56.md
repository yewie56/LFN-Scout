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
