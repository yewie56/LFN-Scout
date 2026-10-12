# Integration und Übergabe an das bestehende LFN-Scout-Projekt

**ID:** LFN-AN-INT · **Version:** 0.7 · **Status:** konkrete Arbeitsanweisung mit offenem Repo-Mapping, **keine implementierte Änderung**.

## 1. Ziel / Auftrag

Das bestehende Android-Projekt LFN-Scout um ein neues, eigenständiges **Unterpanel „Spektrum & Mithören“** erweitern. Es handelt sich um einen **kontinuierlichen Live-Analyzer** wie die funktionale Spectroid-Referenz und ausdrücklich **nicht** um eine WAV-/PCM-Aufnahmefunktion. Gleicher Package-Identifier, gleiche Bestandsinstallation, gleiche vorhandene Daten, Bewertungen, Audio-/Supabase-Funktionen anderer Panels. Kernanforderungen sind in `01_SRD`/`02_SRS` und 146 Parameter in `references/Funktionsreferenz_v0.4.md` definiert.

## 2. Verbindliche Arbeitsgrundlagen / Priorität

1. **Zuerst** am tatsächlichen Projektstand lesen: `BUILD_WORKFLOWS.md`, `DIRECTIVES.md`, `CHANGELOG.md`, gesamte `docs/project/` und alle relevanten Gradle-, Kotlin-, Test- und Workflow-Dateien. Laufende oder spätere Versionen können anders strukturiert sein. `docs/superpowers/` enthält interne Entwurfs-/Taskpläne und ist **nicht** die offizielle kumulative Projektbeschreibung.
2. Dieses v0.6-Paket ist die **fachliche Soll-Quelle für das neue Analyzer-Panel**. Historische v0.3-Dateien unter `references/legacy` sind nur nachvollziehbares Archiv, nicht aktueller Arbeitsauftrag. Die Funktionsreferenz v0.4 bleibt als vollständige Parametertabelle Bestandteil.
3. Bei Konflikt zwischen bestehender App und der neuen Panel-Spezifikation **nicht still abändern**: betroffenen Vertrag, kompatible Lösung, erforderliche Migration und Test im Entscheidungsprotokoll dokumentieren.
4. **Vor jedem Codeeingriff** tatsächliche Basisversion/Commit und Komponenten-Mapping unten ausfüllen. Die mitgelieferte Datei ist kein APK- oder Quellcode-Update.

## 3. Pflicht-Inventory und Mappingformular

| Zu prüfen | Tatsächliche vorhandene Datei/Klasse/Commit (ausfüllen) | Entscheidung |
|---|---|---|
| Basisversion, Git-Commit, Gradle/AGP/Kotlin/SDK | **OFFEN** | erst nach Sichtung |
| Panel-Navigation und Lifecycle | **OFFEN** | bestehenden Host wiederverwenden |
| Mic-Permissions und Capture-/Audio-Pipeline | **OFFEN** | einen gemeinsamen Eingang sicherstellen |
| PCM-Sampleformat, Rate, Zeitstempel, Ringbuffer | **OFFEN** | Adapter implementieren, wenn kompatibel |
| bestehende Plot-/Renderer-Komponenten | **OFFEN** | reuse statt paralleles Plotframework, falls möglich |
| bestehende Setups/Prefs/Migration | **OFFEN** | vorhandenes System integrieren statt Duplikat |
| Kalibrierdateien/Messbasis, vorhandene Testsignale | **OFFEN** | Kompatibilität prüfen |
| vorhandene Audio-Aufnahme anderer Panels | **OFFEN** | semantisch nicht verändern/entfernen; Capture intern ggf. Adapter-Refactoring |
| Bewertungen/Events/Supabase/GitHub-Protokolle | **OFFEN** | Schemas unverändert lassen |
| `BUILD_WORKFLOWS.md` + `PREPARE_PROJECT.bat` + CI | **OFFEN** | realen aktuellen Buildweg beibehalten |
| bestehende Regressionstests und Selftests | **OFFEN** | vor/mit Erweiterung durchführen |

**Repo-Gate:** Ohne ausgefülltes Mapping keine Aussagen wie „Klasse X geändert“, „bestehender Capture wiederverwendet“ oder „Build erfolgreich“.

## 4. Dateistruktur als Soll-Vorschlag, nicht behaupteter Ist-Stand

```text
docs/project/analyzer/
    01_SRD_LFN-Scout_Analyzer_v0.7.md
    02_SRS_LFN-Scout_Analyzer_v0.7.md
    03_SDS_Architektur_v0.7.md
    04_UI_Gesten_v0.7.md
    05_Algorithmen_Kalibrierung_v0.7.md
    06_Test_Validierung_v0.7.md
    07_Integration_Uebergabe_v0.7.md
    08_Traceability_v0.7.md
    09_Entscheidungen_Offene_Punkte_v0.7.md
    10_Bedienung_Kurz_v0.7.md
    11_Parameterreferenz_v0.4.md (unverändert, verbindlich)
```

Falls aktuelle Projektdirektiven eine andere Unterstruktur verlangen, diese nachvollziehbar anpassen und Crosslinks aktualisieren. Das Übergabepaket selbst kann flach unter `docs/project/` liegen; **nicht** `docs/superpowers/` für verbindliche Dokumente verwenden. `README.md` und `CHANGELOG.md` im Projekt referenzieren das Analyzer-Dokumentenset, ohne bestehende Inhalte zu löschen.

## 5. Ursprüngliche Schrittfolge (durch den **erweiterten verbindlichen Plan in §10** präzisiert)

| Schritt | Liefergegenstand | Exit-Test |
|---|---|---|
| I0 | Repo-/Dokumentenstand dokumentiert; Bestands-Tests und Android-Studio-/CI-Build | bestehender Stand bleibt grün |
| I1 | Panel-Navigation, Portrait-UI, simuliertes Spektrum/Wasserfall, Settings als schmale Scrollseite | T-01, T-24/25 |
| I2 | tatsächlicher flüchtiger gemeinsamer Mic-Capture, FFT/PSD, Waterfall-Ringbuffer, Linear-/Y-Skalen | T-03…06 |
| I3 | separater Mithör-Filter/Output/Headset-Routing, Clip/Abregelung, Tap/Swipe/Bandgesten | T-09…14, T-22, T-28/29/43 |
| I4 | Kalibrierung + Gerätemetadaten; Noise-Referenz-Repo/Kompatibilität + optionaler Anzeigebezug | T-07/08, T-36…38 |
| I5 | Gelb/Rot/Grau, Cursor/Pause, L50/L90, Superfast, Max Sample, Save-Automatik | T-26/27, T-31…35, T-39…42 |
| I6 | vollständige Setup-Recall/Migration/SAF, UI-Validierung, vollständige Metadaten | T-02, T-17…21, T-41 |
| I7 | harte Regression/QA auf echtem Gerät, offizieller Buildworkflow, Changelog/Bedienung | T-01…59, Gates G0…G5 |

## 6. Explizite Nichtänderungen

- Keine neue Package-ID, kein Ersatz einer bestehenden App/Installation, keine stille Entfernung alter Features.
- Keine Änderungen an Bewertungsschema, Synchronisations-, Supabase- oder GitHub-Archiven ohne gesondert dokumentierte Entscheidung.
- **Keine neue PCM-/WAV-Aufzeichnung oder Audio-Replay** im neuen Panel. `Save aktiv` steht nur für PNG. Ein Mikrofonzugriff via Android `AudioRecord` schreibt per se noch keine Datei.
- Kein automatischer Mikrofon-Eingangsgain durch mithörseitige AGC; keine Referenz-Subtraktion in L50/L90 oder Mithören.
- Kein unvalidierter SPL-/Infraschall-Claim und kein ungewolltes Mithören via Lautsprecher.

## 7. Konfigurations-/Datenmigrationsstrategie

Alte Analyzer-Setups (falls bereits vorhanden) per `schemaVersion` migrieren; neue Felder erhalten die Defaults aus der Parameterreferenz. Kalibrier-/Noise-ID werden aufgelöst oder sichtbar deaktiviert. `lastUsedSetupId` nur nach erfolgreichem Laden aktualisieren. Auf Rollback muss der alte gültige Datenstand verfügbar bleiben. Testfixturen für v1/v2/v3 und korrupte JSON-Dateien vorsehen. **Mithören und tatsächliches Auto-Save starten nach Recall AUS.** Keine alte API-/DB-Migration ohne echten Bedarf.

## 8. Projektdokumentationspflicht

Bei jeder implementierten Änderung synchron aktualisieren: betroffene SRS/SDS/UI-/Algorithmus-/Bedien-/Teststellen, `docs/project/`-Index, `CHANGELOG.md`, ggf. `BUILD_WORKFLOWS.md` bei Buildänderung. Zu jedem Bug Regressionstest und dokumentierter PASS/FAIL-Selbsttest. Code und Tests nach den bestehenden Direktiven; vollständige Projekt-ZIP nur nach realem offiziellen Build und Prüfung ausgeben.

## 9. Übergabe-Checkliste

- [ ] aktuellen vollständigen LFN-Scout-Repo-Stand eingesammelt, identifiziert, Schutzkopie vorhanden
- [ ] offizielle Projektdokumente vollständig gelesen, Konflikte dokumentiert
- [ ] tatsächliches Modulmapping/PCM-Vertrag und Audio-Routing geklärt
- [ ] SRS-/CFG-Verträglichkeit gegen aktuellen Stand geprüft, 146 CFG-Zeilen übernommen
- [ ] reale Hardware/Software für Audio/2–25 Hz/SPL ausgewählt
- [ ] I0–I10 implementiert und Bestandsfunktionen regressionsgetestet
- [ ] PASS/FAIL-Reports, Logs und Changelog abgelegt
- [ ] offizieller Buildworkflow ausgeführt, APK/Dateihashes und Geräteprüfung nachgewiesen

**Aktueller Handoff-Status:** Fachpaket erstellt. Alles in dieser Checkliste zunächst offen, bis tatsächlicher Quellcode und Tests vorliegen.


## 10. Verbindlicher erweiterter Integrationsauftrag v0.6

**Ergänzt** die bisherigen Schritte I0–I7; nicht als nachträglich bewiesene Implementierung missverstehen.

| Schritt | Arbeit am aktuellen Repo | Exit-Gate |
|---|---|---|
| I0 | Vollständige ZIP/Commit sichern; `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/`, aktuelle Recorder-/Histogramm-/Audio-Architektur, Datenbankschemata und Tests lesen. | Ist-/Soll-Mapping in `16_Repo_Mapping_und_Gates_v0.7.md` ausgefüllt; Bestandsbuild/Tests nachgewiesen. |
| I1 | DSP-Kern als wiederverwendbares Modul; kleine Standalone-**Testhülle** mit Fake-PCM, Fake-Histogrammen und virtueller Zeit. | T-59 Unit-Anteil; kein eigenständiger Produktionszweig. |
| I2 | CaptureOwner/Fan-out mit Aufnahme-Adapter verbinden; Recorder-Originalformat/-daten schützen. | T-44…T-48 mit echter vorhandener Aufnahme. |
| I3 | AnalyzerSession außerhalb des View-Lifecycles, sichtbare/unsichtbare Zustände, Paint nur bei Sichtbarkeit. | T-49/T-51. |
| I4 | Snapshot-Assembler + Offscreen-Renderer + async PNG-Warteschlange; Save weiterhin allein PNG. | T-39…42/T-50/T-53/54. |
| I5 | Histogramm-Publisher-Adapter, Histos AUS/1/4, rechter responsive Strip, Metadaten. | T-55…58. |
| I6 | Bestehende v0.6-Funktionen (Mithören, DSP, Kalibrierung, Cursor, Statistik, Setups) vollständig einbinden. | alle bestehenden T-01…T-43. |
| I7 | Gerätelaufzeit parallel Aufnahme/Save/Histo; Memory/CPU/Audio-Routing und Fehlerrückwege. | G0…G6; vollständiges PASS/FAIL-Protokoll. |
| I8 | Projekt-Dokumentationsabgleich, `docs/project/`-Index, Migration, Changelog, vollständiger offizieller Build. | Doku ohne unmarkierte Widersprüche; Build-/APK-Nachweis. |
| I9 | Regression gegen Bewertungen, Seismik, Supabase, Recorder und alle anderen vorhandenen Panels. | Bestands-Regression PASS, keine stillen Verhaltensänderungen. |
| I10 | Freigabe der Integration unter klarer Zielversion und Hardwarematrix; offene Gerätegrenzen dokumentieren. | vollständige Projekt-ZIP, referenzierbare Prüfnachweise; ohne PASS keine Fertigmeldung. |

**Merge-Regel:** Dieses Paket beim Einbau vorzugsweise unter `docs/project/analyzer/` ablegen; die bisherigen offiziellen Projektdokumente nicht überschreiben. Für jede tatsächlich geänderte Klasse einen Mapping-Eintrag, Migrationsnotiz und Regressionstest. `docs/superpowers/` bleibt internen temporären Arbeitsplänen vorbehalten.

**Nicht verwechseln:** Eine interne gemeinsame Mikrofon-PCM-Quelle ist keine neue Audioaufzeichnung im Analyzer. Der bestehende Recorder darf die Quelle gemeinsam nutzen und weiterhin Dateien erzeugen. Ein neues Analyzer-Recording-UI ist ausdrücklich ausgeschlossen.

## 11. Ergänzende Übergabe-Pflichten v0.7

1. `assets/concepts/BILDREGISTER_v0.7.md` lesen. Die 13 enthaltenen Konzeptbilder auf Leitbild/Referenz/Historie prüfen und **keine** ausdrücklich verworfenen Bilder verwenden. Historische Begriffe/Skalen in Bildern dürfen Produktentscheidungen nicht überschreiben.
2. Echte LFN-Scout-Gesamtquellen vor der ersten Codeänderung lesen: `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/`, Audio-/Recorder-/Histogrammcode, Settings/Setups/CI. Quellstand (`commit`/ZIP-Hash) in Mappingdokument festhalten. Vorhandene `docs/project/` nicht pauschal überschreiben: gezielt mit dieser modulspezifischen Ergänzung zusammenführen.
3. Gemeinsame Audioerfassung und PCM-Owner zuerst gegen den bestehenden Recorder abgleichen, bevor ein zusätzlicher `AudioRecord` eingeführt wird. Keine PCM- oder Kalibrier-Änderungen an bestehenden Aufnahmeprodukten ohne eigene Tests.
4. Core mit kleinem Testhost aufbauen, zugleich reale LFN-Scout-Adapter bereitstellen. Bestehenden Histogramm-Datenvertrag und Export-Path per Ist-/Soll-Mapping identifizieren.
5. `Save aktiv` über internen Panelwechsel testen, einschließlich Offscreen-Renderer, Kalender-/Monotonzeitbasis, atomarem Speichern und Fehlerdiagnose. App-Hintergrund/Screen-off sind ein separates Freigabegate.
6. Testnachweise T-01…T-71 inklusive dokumentierter Bestandsregressionen erzeugen, bei Fehlern Regressionstests hinzufügen. Jede Änderung an Defaults/Schnittstellen/Quellen aktualisiert SRS, SDS, Parameterschema, Testmatrix und CHANGELOG.
7. Lieferumfang der Implementierung: vollständiges aktualisiertes Projekt-ZIP, offizielle Doku in `docs/project/`, Build-/Test-Bericht und reale ausführbare APK erst nach erfolgreichem Build. `docs/superpowers/` darf nur projektinterne Pläne enthalten.

