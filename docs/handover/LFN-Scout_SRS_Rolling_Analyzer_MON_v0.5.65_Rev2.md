# LFN-Scout – Software Requirements Specification (SRS)
## Rolling-Audio, 8-kHz-Analyzer, MON und benannte JSON-Setups

**Dokumentstatus:** Überarbeiteter SRS-Entwurf, Revision 2; Speicherung vor Setupwechsel fachlich festgelegt; **keine Codeänderung**
**Ausgangsstand:** v0.5.65, Projekt-ZIP `LFN-Scout_v0.5.65_SETUP_STATUS_STOP_TEST_SOURCE.zip`
**Datum:** 2026-10-16
**Änderung Revision 2:** Vor jeder Aktivierung eines anderen benannten Setups alle aufgelaufenen Änderungen am bislang aktiven Setup dauerhaft und überprüfbar speichern; zusätzlich Speicherung beim Analyzer-Verlassen. Dies ersetzt die frühere Regel „nur beim Analyzer-Verlassen speichern“.
**Geltungsbereich:** Scout, Rolling-Audio, 8-kHz-Analyzer, MON und deren Wechselwirkungen. Der Direct-Analyzer wird nur an den Schnittstellen und bei bestehenden Sicherheitsregeln behandelt.
**Prioritäten:** MUSS = verbindliche funktionale Anforderung; SOLL = Qualitätsziel; OFFEN = technische oder fachliche Entscheidung erforderlich.
**Quellen:** Nutzerfestlegungen in der laufenden Abstimmung; bestehende Implementierung/Kommentare in oben genanntem ZIP; `DIRECTIVES.md`, `docs/project/CURRENT_STATE.md` und die unten aufgeführten Quellmodule. Die Beschreibung des **Soll-Zustands** ist keine Aussage, dass er bereits implementiert sei.

---

## 1. Ziel und Problemstellung

Die Rolling-Audioaufnahme soll kontinuierlich vergangene Audiodaten bereitstellen, damit unerwartete Ereignisse *nachträglich* als Aufnahme gesichert werden können. Der spezialisierte 8-kHz-Analyzer nutzt den bereits vorhandenen Rolling-PCM-Stream als Teilnehmer. STOP beendet die Analyzer-Verarbeitung, nicht automatisch den Rolling-Dienst. Ein anschließendes RUN soll denselben Stream wieder abonnieren können. Der Benutzer soll während Analyzer-STOP zwischen benannten versionierten Analyzer-JSON-Setups wechseln können, ohne die Rolling-Aufnahme oder deren Puffer zu beeinträchtigen. Parameter der Mithörfunktion MON müssen, soweit sie ausschließlich deren Signalverarbeitung betreffen, live änderbar sein.

**Reproduzierter Symptomhinweis:** Bei Analyzer STOP und weiter laufendem Rolling (grünes Mikrofon) erscheint beim Setupwechsel „STOP erforderlich; Aufnahme/MON/Vorschau beenden“. Das ist nach dem vereinbarten Soll-Zustand zu restriktiv; die sachliche Ursache und sämtliche nachgeschalteten Sicherheitsbedingungen sind vor der Korrektur nachzuweisen.

## 2. Begriffe und Eigentümerschaft

- **Scout-Setup:** Eigenständige Konfiguration des Scouts, insbesondere Rolling-Audio-Einstellungen und Betriebsdauer; nicht mit einem Analyzer-Setup gleichsetzen.
- **Rolling-Capture:** Kontinuierliche Aufnahme des Audioeingangs und Pflege eines zeitlich begrenzten, überschreibenden RAM-Puffers; keine Behauptung einer durchgehenden WAV-Datei auf Datenträger.
- **8-kHz-PCM-Stream:** Datenquelle, die der Rolling-Dienst auch für den spezialisierten 8-kHz-Analyzer bereitstellt.
- **Rolling-Puffer:** Rückblickend auslesbarer Verlauf; v0.5.65 setzt in `RollingAudioPolicy.kt` 30 Minuten bei 1.000 Samples/s als Kapazität an. Das ist von der maximalen Betriebsdauer zu unterscheiden.
- **Analyzer-Subscriber:** Verbraucher des Audio-Streams; seine FFT-/Darstellungsverarbeitung ist von dessen Capture-Lebenszyklus zu trennen.
- **MON:** Mithörzweig mit eigener Signalverarbeitung, Filterung/Gain und gegebenenfalls Wiedergabe/Aufzeichnung.
- **Analyzer-Setup:** Benannte, versionierte JSON-Datei mit Analyzer-/MON-Konfiguration; nicht identisch mit Scout-Setup.
- **STOP:** Analyzer liefert/aktualisiert keine laufende Analyse; bedeutet nicht automatisch Rolling STOP.
- **Audio-Owner:** Tatsächlicher Besitzer von Mikrofon/`AudioRecord`; von Analyse-Subscriber und Setup-Parameterbesitzer zu unterscheiden.

## 3. Nutzerszenarien

**UC-01 – Unerwartetes Ereignis während RUN.** Rolling arbeitet, ein Ereignis tritt auf, der Benutzer markiert oder speichert später einen rückwirkenden Audioclip, sofern noch im Puffer.

**UC-02 – Ereignis während STOP.** Analyzer wird gestoppt, Rolling läuft weiter. Der Benutzer startet Analyzer später neu und kann ein noch im Puffer vorhandenes Ereignis aus der STOP-Phase nachträglich sichern.

**UC-03 – Benanntes Setup umschalten.** Analyzer STOP, Rolling aktiv. Der Benutzer wählt ein anderes JSON-Setup. Zuerst werden sämtliche Änderungen des bisher aktiven Setups einschließlich noch offener Panel-Änderungen in **dessen eigene JSON-Datei dauerhaft gespeichert**. Nur wenn die Speicherung erfolgreich ist, wird das neue Setup überprüft und aktiviert. Die UI zeigt erst anschließend das tatsächlich aktive Setup. Rolling und Puffer bleiben unverändert. Scheitert die Speicherung, bleibt das bisherige Setup aktiv.

**UC-04 – MON live ändern.** Bei aktiver Wiedergabe werden z. B. Gain und MON-Filterwerte angepasst, ohne Rolling neu zu starten. Nur der Mithörzweig ändert sich, nicht der rückwirkend gespeicherte Rohpuffer.

**UC-05 – Analyzer verlassen.** Bestehender Dialog fragt, ob Rolling beendet oder weitergeführt werden soll. Die Auswahl wird respektiert; kein zusätzlicher Dialog.

**UC-06 – Setup mit inkompatiblen Erfassungsparametern.** Ein Analyzer-Setup versucht, Eingabegerät oder Capture-Abtastrate zu ändern. Ein normaler STOP-Setupwechsel darf nicht den aktiven Rolling-Capture unbemerkt umkonfigurieren. Der Konflikt wird konkret erklärt; kein Datenverlust.

## 4. Verbindliche funktionale Anforderungen

### 4.1 Rolling-Aufnahme und Datenerhalt

| ID | Priorität | Anforderung und Abnahmekriterium |
|---|---|---|
| ROL-001 | MUSS | Rolling-Aufnahme läuft unabhängig von RUN/STOP des 8-kHz-Analyzers. STOP darf den Rolling-Dienst nicht allein wegen Analyzer-STOP beenden. |
| ROL-002 | MUSS | Der Übergang RUN→STOP→RUN darf den Rolling-RAM-Puffer nicht löschen, neu initialisieren oder seine Zeitbasis zurücksetzen. |
| ROL-003 | MUSS | Der Benutzer kann rückwirkend Audiodaten aus einem Zeitabschnitt speichern, der vor STOP, während STOP oder nach Wiederanlauf lag, sofern er noch im Rolling-Fenster enthalten ist. |
| ROL-004 | MUSS | Ein Analyzer-Setupwechsel bei STOP lässt laufende Rolling-Aufnahme, PCM-Erfassung und historische Rolling-Daten unangetastet. |
| ROL-005 | MUSS | Beim Verlassen des Analyzers wird der **bereits vorhandene Dialog** genutzt, um „Rolling stoppen“ oder „Rolling weiterlaufen lassen“ zu wählen. Kein automatisches Abstellen und kein zusätzlicher Dialog. |
| ROL-006 | MUSS | Die Konfiguration der Rolling-Puffergröße und Betriebsdauer ist fachlich von FFT-Historie, Anzeigehistorie und MON-Puffern getrennt. |
| ROL-007 | MUSS | Die Rolling-Aufnahme darf durch eine MON-Gain-/Filteränderung keine nachträglich veränderten Audiodaten enthalten: MON-Verarbeitung erfolgt nach dem Abzweig vom Rolling-Rohdatenpfad. |
| ROL-008 | SOLL | Aufnahmezustand, verfügbare Pufferzeitspanne und gegebenenfalls ein begründeter Aufnahmelücken-/Neustartstatus sind diagnostizierbar. |

### 4.2 8-kHz-Analyzer und Stream-Abonnement

| ID | Priorität | Anforderung und Abnahmekriterium |
|---|---|---|
| ANA-001 | MUSS | Der 8-kHz-Analyzer bezieht PCM als Subscriber aus dem bestehenden Rolling-Stream, soweit dessen Betriebsmodus dies vorsieht; kein unnötiger zweiter Mikrofon-Owner. |
| ANA-002 | MUSS | Bei Analyzer-STOP beendet der Analyzer seine Stream-Verarbeitung bzw. meldet seinen Subscriber ab, ohne den Rolling-Dienst zu stoppen. |
| ANA-003 | MUSS | Bei Analyzer-RUN verbindet sich der Analyzer wieder mit dem Rolling-Stream und verwendet die dann aktive Analyzer-Konfiguration. |
| ANA-004 | MUSS | Ist Rolling beim START des 8-kHz-Analyzers ausgeschaltet, wird es gemäß dem bereits bestehenden und funktionierenden Startpfad aktiviert; kein neuer paralleler Startmechanismus. |
| ANA-005 | MUSS | Ein JSON-Setupwechsel bei STOP verändert nur die Analyzer-/MON-Laufzeitparameter und deren Zuordnung, nicht die Scout-Rolling-Konfiguration. |
| ANA-006 | MUSS | Eine Änderung reiner Subscriber-Parameter (FFT, Window, Decimation im Subscriber, Filterbank, Display) darf die grundlegende PCM-Erfassung nicht restartieren. |
| ANA-007 | MUSS | Widersprechen neue Setup-Parameter der laufenden Hardwarequelle oder Capture-Abtastrate, ist eine spezifische sichere Entscheidung nötig: unveränderte Quelle nutzen, inkompatiblen Wechsel ablehnen oder ausdrücklich separat vereinbarte Umschaltung. Kein stiller Neustart. |
| ANA-008 | SOLL | Streaming-Subscriber- und Pufferzustände werden beim Wechsel protokolliert, ohne PCM-Rohdaten, Geodaten oder Schlüssel zu loggen. |

### 4.3 Benannte Analyzer-JSON-Setups

| ID | Priorität | Anforderung und Abnahmekriterium |
|---|---|---|
| SET-001 | MUSS | Beliebig viele benannte, versionierte Analyzer-JSON-Setups im app-internen Speicher; Import/Export über Android-Dateiauswahl ohne pauschale Dateisystemfreigabe. |
| SET-002 | MUSS | Scrollbare Auswahl aller Setups sowie schneller Zugriff auf die drei zuletzt genutzten (MRU). |
| SET-003 | MUSS | Vollständiger benannter Setupwechsel nur bei tatsächlich gestopptem Analyzer; dabei ist ein **unabhängig laufender Rolling-Capture allein kein Sperrgrund**. |
| SET-004 | MUSS | Sicherheitsprüfung unterscheidet Capture-Owner, aktive Analyzer-Subscriber, MON-WAV, Direct-Capture, Vorschau, ausstehende Starts/Cleanups und tatsächliche Hardwareänderungen. Erforderliche Prüfungen bleiben wirksam. |
| SET-005 | MUSS | Blockierte Wechsel nennen die konkreten Zustände statt ausschließlich „STOP erforderlich; Aufnahme/MON/Vorschau beenden“. |
| SET-006 | MUSS | JSON-Schema, Wertebereiche, Kompatibilität und betroffene Parameter vor Übernahme validieren. Fehler dürfen weder vorhandenes Setup überschreiben noch Rolling verändern. |
| SET-007 | MUSS | Die Setup-Übernahme ist logisch atomar: vollständige Analyzer- und MON-Parameterübernahme oder Rücknahme auf verifizierten Vorzustand. |
| SET-008 | MUSS | Das **tatsächlich aktive** Setup wird erst nach erfolgreicher Übernahme als aktiv angezeigt; Auswahlmarkierung, Laufzeitwerte und beim nächsten RUN verwendete Werte stimmen überein. |
| SET-009 | MUSS | Parameteränderungen werden beim Verlassen des betreffenden Einstellungs-Panels automatisch in den aktiven Laufzeitstand übernommen, ohne jede einzelne Änderung zu bestätigen. |
| SET-010 | MUSS | Dauerhaftes Speichern des aktiven benannten Analyzer-JSON-Setups ist zwingend **vor jedem Wechsel auf ein anderes Setup sowie beim Verlassen des gesamten Analyzers** durchzuführen. Ein bloßer Panelwechsel löst keine JSON-Dateispeicherung aus. |
| SET-011 | MUSS | Vor Aktivierung von Setup B müssen **sämtliche aufgelaufenen Änderungen von Setup A**, einschließlich noch nicht aus einem offenen Panel übernommener gültiger Eingaben und zur Laufzeit geänderter MON-Parameter, A zugeordnet und in **A.json** dauerhaft geschrieben werden. Ein Schreib-/Validierungsfehler verhindert den Wechsel; A und seine Änderungen bleiben erhalten. Keine Speicherung in B.json. |
| SET-012 | MUSS | Bei Ablehnung oder Fehler bleibt das zuvor tatsächlich aktive Setup einschließlich UI-Zuordnung erhalten. MRU wird nur bei bestätigter Aktivierung aktualisiert. |
| SET-013 | MUSS | Der normale nächste START darf ein soeben erfolgreich gewechseltes JSON-Setup nicht durch alte Legacy-Preferences überschreiben. |
| SET-014 | MUSS | Bestehende helle, übersichtliche, logisch gruppierte Setup-Oberfläche und ein konsistentes Setup-System bleiben erhalten. |
| SET-015 | SOLL | Benutzerfreundliche Diagnosedaten unterscheiden „ausgewählt“, „übernommen“, „startwirksam“ und „dauerhaft gespeichert“. |
| SET-016 | MUSS | Reihenfolge: vor dem Wechsel alle offenen Panel-Werte übernehmen und validieren; A vollständig und robust als versionierte JSON-Datei speichern; **erst nach bestätigtem Erfolg** B laden und validieren, transaktional übernehmen, Readback prüfen, aktive Kennung/UI/MRU auf B ändern. |
| SET-017 | MUSS | Scheitert das Speichern von A, darf B nicht als aktiv oder startwirksam erscheinen. Die bislang aktive Kennung A, ihre Änderungen und deren laufende Wirkung bleiben erhalten; Fehlerursache ist sichtbar. |
| SET-018 | MUSS | Fehlgeschlagene B-Aktivierung nach erfolgreich gespeichertem A lässt A aktiv und stellt die Laufzeitkonfiguration von A wieder her; A.json bleibt mit den letzten gültigen Änderungen erhalten. Keine unbemerkte Teilübernahme. |
| SET-019 | MUSS | JSON-Speichern schützt die bisherige gültige Datei vor teilweise geschriebenen/trunkierten Inhalten (temporäre Datei, Prüfung, sicherer Ersatz soweit von Android-Dateisystem unterstützt). Erfolg erst nach bestätigtem Abschluss; fehlgeschlagener Schreibvorgang darf den alten Stand nicht stillschweigend zerstören. |
| SET-020 | MUSS | Die aktive Setup-ID ist die allein maßgebliche Zuordnung für Schreibziele und Anzeige; angeklickte Dropdown-/MRU-Zeile ist bis zur abgeschlossenen Aktivierung nur eine **Wechselanforderung**. |
| SET-021 | MUSS | Beim Analyzer-Verlassen wird das zuletzt tatsächlich aktive Setup einschließlich letzter Änderungen gespeichert, unabhängig davon, ob in der Sitzung Setups gewechselt wurden. Der vorhandene Rolling-Verlassen-Dialog bleibt bestehen. |

### 4.4 MON / Mithören

| ID | Priorität | Anforderung und Abnahmekriterium |
|---|---|---|
| MON-001 | MUSS | Parameter, die nur die MON-Verarbeitung betreffen (z. B. Gain und MON-Filter), lassen sich bei aktiver MON-Funktion **on the fly** ändern, ohne Analyzer STOP zu verlangen. |
| MON-002 | MUSS | MON-Liveänderungen erfordern keinen Rolling-Neustart und löschen keine Audiodaten. |
| MON-003 | MUSS | MON-Verstärkung/Filterung ändert nicht den ungefilterten rückwirkenden Rolling-Speicherpfad. |
| MON-004 | MUSS | Liveänderungen werden kohärent an gültigen Verarbeitungsgrenzen übernommen; ungültige Werte werden abgewiesen, ohne den letzten gültigen Parameterzustand zu beschädigen. |
| MON-005 | MUSS | Beim Wechsel benannter Analyzer-Setups kann eine tatsächlich aktive MON-WAV-Aufzeichnung weiterhin einen sicherheitsbegründeten Sperrgrund darstellen. Dies ist von einer bloßen laufenden Rolling-Aufnahme zu unterscheiden. |
| MON-006 | SOLL | Bei Liveänderungen von Gain/Filtern Knackser, Instabilität und Übergangsartefakte minimieren; diese Eigenschaft wird getestet. |

### 4.5 Scout-Setup und Zuständigkeitsgrenzen

| ID | Priorität | Anforderung und Abnahmekriterium |
|---|---|---|
| SCO-001 | MUSS | Scout- und Analyzer-Setups sind eigenständige Konfigurationen mit getrennten Speicherorten/Parameterräumen. |
| SCO-002 | MUSS | Rolling-Länge, maximale Rolling-Betriebsdauer, grundlegende Audioquelle und zugehörige Capture-Einstellungen werden nicht durch die Auswahl eines Analyzer-Setups verändert. |
| SCO-003 | MUSS | Eine bewusste Scout-Rolling-Konfigurationsänderung, die einen Capture-Neustart voraussetzt, muss separat nach geltenden Sicherheitsregeln behandelt werden. |
| SCO-004 | MUSS | Weder die Setup-Auswahl noch bloßes Analyzer-STOP erzeugt implizit einen neuen Rolling-Aufnahme- oder Puffermodus. |

## 5. Sicherheits- und Zustandsmatrix

Diese Matrix definiert **fachliche Wechselerlaubnis**, nicht eine Umgehung der nötigen transaktionalen Ressourcensicherung.

| Analyzer | Rolling | MON / weitere Aufnahmen | Analyzer-Setupwechsel | Fachliche Begründung |
|---|---|---|---|---|
| RUN | aktiv | beliebig | Nein | Analyzer verarbeitet gerade das bisherige Setup. |
| STOP | aktiv | MON aus, keine weiteren Konflikte | **Ja** | Rolling ist unabhängiger Stream-Produzent; keine Capture-Umstellung. |
| STOP | aus | MON aus, keine weiteren Konflikte | **Ja** | Keine aktive Verarbeitung. |
| STOP | aktiv | MON-Livewiedergabe aktiv | OFFEN hinsichtlich Komplettwechsel | Es ist zu klären, ob ein kompletter MON-Parametersatz sicher atomar umschaltbar ist; *einzelne* MON-Liveparameter sind erlaubt. |
| STOP | beliebig | MON-WAV/Direct-Capture aktiv | Nur nach nachgewiesener Ressourcenverträglichkeit, ansonsten Nein | Aufnahme darf nicht beschädigt werden. |
| STOP | beliebig | Preview aktiv | Nach separater Prüfung, sonst Nein | Kein undefinierter Vorschauzustand. |
| STOP | beliebig | Capture-Start/-Cleanup offen | Nein, falls relevante Ressource betroffen | Nebenläufigkeit sicher abfangen. |
| STOP | aktiv | Setup ändert Hardware-Capture | Nein im reinen JSON-Setupwechsel | Schutz des laufenden Rolling-Streams. |

## 6. Normativer Ablauf: Setupwechsel bei STOP und Rolling EIN

1. Benutzer fordert Setup B im Dropdown oder über MRU an; A bleibt das tatsächlich aktive Setup.
2. Wechselvoraussetzungen prüfen: Analyzer STOP und Subscriber ausgeklinkt, kein relevanter Lifecycle-/Hardwarekonflikt. Eine aktive Rolling-Aufnahme alleine ist kein Hinderungsgrund.
3. Noch offene gültige Eingaben aus dem aktuellen Einstellungs-Panel übernehmen; alle aufgelaufenen Analyzer- und MON-Laufzeitänderungen dem aktiven **Setup A** zuordnen. Ungültige Werte sichtbar melden, Setupwechsel nicht ausführen.
4. **Setup A vollständig und fehlertolerant in seine eigene versionierte JSON-Datei speichern; das bestätigte Ergebnis abwarten.** Bei Fehler A einschließlich ungespeicherter Änderungen aktiv lassen und keine B-Aktivierung beginnen.
5. Setup B laden und auf Schema, Wertebereiche, Parametereigentümerschaft und Hardwarekompatibilität prüfen. Kein Rolling-Owner-Neustart.
6. B in einer logisch atomaren oder kompensierbaren Transaktion als Analyzer-/MON-Laufzeitkonfiguration übernehmen; Readback und Startwirksamkeit prüfen. Bei Fehler den gespeicherten Zustand A wieder als aktiv herstellen; MRU und Aktivanzeige bleiben A.
7. **Erst nach bestätigter erfolgreicher B-Aktivierung** aktive Setup-ID und MRU aktualisieren; UI zeigt B.
8. Beim nächsten RUN übernimmt der Analyzer genau B und klinkt sich in den kontinuierlich weiterlaufenden Rolling-Stream ein.
9. Beim Verlassen des Analyzers erneut das dann aktive Setup speichern; vorhandenen Dialog „Rolling stoppen / weiterlaufen“ benutzen.

**Grenzfälle:** A und B identisch → kein eigentlicher Wechsel; bei „Save“ desselben Setups gelten die normalen Speicherregeln. Konkurrierende Wechselanforderungen müssen serialisiert werden. Bei validem gespeichertem A und fehlerhaftem B bleibt A.json unangetastet. Es findet niemals eine automatische Speicherung von A in B.json statt.

## 7. Feststellungen zur Implementierung v0.5.65 (Code-Audit-Basis, kein abgeschlossener Laufzeittest)

- **I-01:** `AnalyzerStoppedJsonControllerPort.kt`, `applyCombinedDraft()` lehnt ausdrücklich bei `safety.rollingRunning` ab und erzeugt die vom Nutzer beobachtete pauschale Fehlermeldung.
- **I-02:** `AnalyzerStoppedJsonControllerPort.status()` fasst `rollingRunning || directCapturing` unter `scoutRecording` zusammen. Damit kann Rolling an mehreren Stellen wie ein sperrendes Recording behandelt werden.
- **I-03:** `AnalyzerSetupSwitchTransaction.kt` lehnt mit `state.scoutRecording || state.monRecording` ab; weitere Audio-Owner- und Lifecycle-Prüfungen existieren, darunter `AnalyzerSetupAudioOwnerGate` und `AnalyzerSetupTransitionCoordinator`. Eine isolierte Änderung nur an I-01 reicht nicht als Sicherheitsnachweis.
- **I-04:** Der Port dokumentiert selbst, dass STOP-Drafts **nicht persistent und nicht garantiert startwirksam** sind. `AnalyzerSimpleSetupPanel.kt` weist nach Anwendung explizit darauf hin, dass der nächste Messstart derzeit ältere Voreinstellungen laden kann. Das verletzt SET-013 im Soll-Zustand.
- **I-05:** `AnalyzerSimpleSetupPanel.kt` ruft bei Setupwechsel `saveCurrent()` auf; dort wird `AnalyzerActiveSetupPersistence.saveOnAnalyzerExit()` verwendet. Die **Auslösung** einer Speicherung vor dem Wechsel ist gemäß Revision 2 fachlich richtig. Es muss aber geprüft werden, ob *alle* aufgelaufenen Änderungen enthalten sind, die korrekte A.json geschrieben wird, ob Schreiben robust und bestätigbar ist und ob bei Fehler kein Wechsel erfolgt. Der Methodenname allein belegt dies nicht.
- **I-06:** `RollingAudioPolicy.kt` setzt `ROLLING_DURATION_SECONDS = 30*60`, `TARGET_SAMPLE_RATE_HZ = 1000`; nicht mit 8-kHz-Capture-Eingang gleichsetzen.
- **I-07:** `AnalyzerModeCoordinator.kt` unterscheidet `ROLLING_8K` von direkten Modi; bei Wechsel in Nicht-Rolling-Modi wird Rolling ausdrücklich gestoppt und bisheriger Inhalt verworfen. Historische Direct-Analyzer-Lifecycle-Vorgaben in `DIRECTIVES.md` bleiben zu beachten, dürfen aber nicht versehentlich die vereinbarte 8-kHz-ROLLING-Betriebsart beschädigen.
- **I-08:** Die aus der Nutzerrückmeldung bekannte automatische Aktivierung von Rolling beim 8-kHz-Analyzer-Start ist bereits vorhanden und soll **nicht neu implementiert** werden.

**Auditgrenze:** Quellcode-Inspektion belegt Prüfpfade und dokumentierte Implementierungsabsichten. Ob ein konkreter Gerätezustand zusätzlich einen Lifecycle-Marker hält, erfordert Diagnoselog oder Gerätetest. Ein erweiterter Code-Audit der gesamten MON-/PCM-Aktualisierung und ihrer Nebenläufigkeit steht vor Implementierungsfreigabe aus.

## 8. Regressionstest- und Abnahmeplan

| Test-ID | Ausgangslage/Aktion | Erwartung |
|---|---|---|
| T-001 | 8-kHz-Analyzer RUN, Rolling EIN → STOP | Rolling bleibt EIN, Zeitachse/Puffer bleiben erhalten, Analyzer-Subscriber ist abgemeldet. |
| T-002 | STOP + Rolling EIN → JSON-Setup B wählen | Übernahme gelingt bei sonst konfliktfreiem Zustand; Rolling ohne Unterbrechung. |
| T-003 | STOP + Rolling AUS → Setup B wählen | Wechsel gelingt. |
| T-004 | Analyzer RUN → Setup B wählen | Wechsel sicher abgelehnt, A bleibt aktiv. |
| T-005 | STOP + Rolling EIN → Setup B → RUN | Analyzer verwendet startwirksam die Parameter von B, UI zeigt B, Rolling durchgehend. |
| T-006 | Ereignis vor STOP, dann STOP→RUN, anschließend rückwirkender Clip | Ereignis aus dem vorhandenen Rolling-Fenster wird korrekt gespeichert. |
| T-007 | Ereignis während STOP, danach Setupwechsel und RUN | Rückwirkender Clip aus der STOP-Phase erfolgreich. |
| T-008 | MON EIN, Gain/Filter live ändern | Änderung hörbar/messtechnisch wirksam, keine Rolling-Unterbrechung, Rohpuffer unverändert. |
| T-009 | Ungültiger MON-Filterwert live | Ablehnung; letzter gültiger Filter bleibt wirksam, kein Capture-Neustart. |
| T-010 | MON-WAV/Direct-Aufnahme oder relevante Preview blockiert | Konkrete Fehlerursache, kein unbeabsichtigter Setupwechsel oder Stoppen anderer Dienste. |
| T-011 | Ungültiges JSON, fehlendes Feld/Schemafehler | Keine Teilübernahme, vorheriges Setup und Puffer unverändert. |
| T-012 | Fehler nach Teilanwendung von Analyzer/MON | Vollständiger, geprüfter Rollback; aktive UI-Zuordnung bleibt A. |
| T-013 | A ändern → bei STOP B auswählen → später A erneut wählen | **Vor** B-Aktivierung wurden sämtliche Änderungen in A.json dauerhaft gesichert; nach Rückwechsel stimmen Laufzeitwerte und Datei mit A überein. |
| T-014 | Panel verlassen, ohne Setupwechsel oder Analyzer-Verlassen | Parameter automatisch übernommen, **noch kein JSON-Schreibzwang**. |
| T-015 | Analyzer verlassen, bestehender Dialog „Weiterlaufen“ | Rolling bleibt aktiv, Setup dauerhaft gespeichert. |
| T-016 | Analyzer verlassen, Dialog „Stoppen“ | Rolling stoppt entsprechend Benutzerwahl; Setup dauerhaft gespeichert. |
| T-017 | Setup B mit anderer Hardwarequelle/-Rate | Kein stiller Rolling-Neustart, keine Pufferlöschung; verständliche Blockierung. |
| T-018 | Wiederholte schnelle STOP/RUN/Setupwechsel und verzögerter Audio-Lifecycle | Keine Race Condition, keinen falschen Aktivstatus; Fail-closed bei tatsächlichen Konflikten. |
| T-019 | MRU-Setupwechsel erfolgreich bzw. abgelehnt | MRU ändert sich nur bei tatsächlichem Erfolg; aktive Auswahl stets wahr. |
| T-020 | Bestehende Legacy-/Direct-Analyzer-Regressionen | Keine Rückschritte beim bislang separat geregelten Direct-Capture-Restore. |
| T-021 | In A mehrere Panels sowie MON live ändern; vor Setupwechsel ein Panel noch offen | Alle gültigen neuen Werte in A.json, kein Wert in falschem Setup; B erst nach bestätigt erfolgreicher A-Speicherung aktiv. |
| T-022 | A.json-Schreibfehler (z. B. simuliertes I/O-Problem) und B auswählen | Wechsel abgelehnt, A aktiv, alle Änderungen erhalten, eindeutige Fehlermeldung; alte gültige A.json nicht beschädigt. |
| T-023 | A erfolgreich speichern, aber B-JSON ungültig bzw. B-Übernahme fehlschlägt | A bleibt aktiv und startwirksam, A.json enthält neue Änderungen, MRU unverändert; keine Teilübernahme von B. |
| T-024 | A ändern → B erfolgreich → B ändern → A erfolgreich → Analyzer verlassen | A.json und B.json enthalten jeweils nur ihre eigenen aktuellen Werte; B wird vor Rückwechsel, A beim Verlassen gespeichert. |
| T-025 | Zwei rasch aufeinanderfolgende Setupwünsche während Speicherung | Serialisierte Bearbeitung, niemals in falsche JSON-Datei geschrieben, Anzeige/ID/MRU konsistent. |
| T-026 | Schreiben abbrechen, temporäre/fehlerhafte Datei simulieren | Vorherige gültige JSON nicht abgeschnitten; kein fälschlich bestätigter Wechsel. |

**Nachweis je Test:** Testumgebung/Build, VersionCode, reproduzierbare Schritte, Soll/Ist, PASS/FAIL, gegebenenfalls gefilterter Logcat-Auszug ohne private Daten. Testcode muss tatsächlich ausgeführt werden; Quelltextprüfung allein ist kein Android-Geräte-PASS. Bestehende automatisierte Regressionen und integrierter Self-Test sind einzubeziehen.

## 9. Offene Entscheidungen vor der Codefreigabe

- **OFF-01 (ENTSCHIEDEN, 16.10.2026):** Vor Aktivierung von B **sämtliche Änderungen an A dauerhaft in A.json speichern**; erst bei Erfolg umschalten. Zusätzlich bei Analyzer-Verlassen speichern. Keine separate RAM-Dirty-Draft-Strategie über mehrere verlassene Setups erforderlich.
- **OFF-02 (hoch):** Ist ein vollständiger Analyzer-Setupwechsel bei eingeschaltetem MON, aber Analyzer STOP, zulässig? Einzelne MON-Liveänderungen sind verbindlich zulässig; ein kompletter atomarer Wechsel kann andere Risiken haben.
- **OFF-03 (hoch):** Exakte Parameterlisten „Capture-Hardware“, „Rolling“, „Analyzer-Subscriber“, „MON-Live“, „MON-Stop-only“ anhand aller aktuellen JSON-Felder erstellen und freigeben.
- **OFF-04 (mittel):** Was soll bei App-Prozessverlust *vor* dem nächsten Setupwechsel bzw. vor dem regulären Analyzer-Verlassen mit noch nicht gespeicherten Änderungen geschehen? Separate Crash-Recovery bleibt ungeklärt; die verbindliche Speicherung bei Setupwechsel und Analyzer-Verlassen ist davon unabhängig.
- **OFF-05 (mittel):** Definierte Behandlung inkompatibler Hardwareparameter, falls sie in historischen Analyzer-Setups vorhanden sind; niemals stillschweigend auf Rolling übertragen.
- **OFF-06 (mittel):** Verhalten bei erschöpfter Rolling-Pufferzeit, fehlenden Samples und Android-Aufnahmeunterbrechungen einschließlich Kennzeichnung unvollständiger Clips.

## 10. Vorgeschlagene Umsetzungsetappen (erst nach ausdrücklicher Freigabe)

1. **Quellcode-Audit vollständig:** Parameter-/Ressourcenmatrix, tatsächliche Gate-Abhängigkeiten, Audio-Subscriber-Lebenszyklus und Persistenzpfade belegen.
2. **Gezielte Sicherheitskorrektur:** Rolling-Capture als unabhängigen Producer von den echten Analyzer-Setup-Sperrgründen trennen, ohne allgemeine Capture-/MON-/Direct-Sicherheitskontrollen abzuschalten.
3. **Atomarer Setupwechsel:** inklusive **zwingender bestätigter Speicherung von A vor B-Aktivierung**, Startwirksamkeit, aktivem Namen, MRU und Rollback bei Fehlern.
4. **MON-Live-Parameter:** prüfen und erforderlichenfalls nur im MON-Verarbeitungszweig korrekt aktualisieren.
5. **Regression/Self-Test:** Tests T-001 bis T-026; vollständigen Android-Build und, wo erforderlich, echte Gerätetests separat ausweisen.
6. **Dokumentation/Release:** `DIRECTIVES.md`, `CHANGELOG.md`, `docs/project/CURRENT_STATE.md` und einschlägige SRS/SDS/Test-Dokumente konsistent aktualisieren. Vollständiges Projekt-ZIP mit Dateien direkt im ZIP-Wurzelverzeichnis, ohne zusätzlichen obersten Ordner und ohne private Daten; Versionsformat `Haupt.Neben.Patch`.

## 11. Änderungsstatus

**Nur dieses Requirements-Dokument wurde überarbeitet (Revision 2).** Bestehende Projektquellen, Tests, Projekt-ZIP und verbindliche Dokumente im Ausgangsarchiv wurden **nicht verändert**. Dieses SRS ist ein Diskussionsentwurf, noch kein freigegebener Implementierungsauftrag.

## 12. Referenzierte Dateien des Quellstandes

`DIRECTIVES.md`; `docs/project/CURRENT_STATE.md`; `app/src/main/java/de/brummbeobachter/app/AnalyzerStoppedJsonControllerPort.kt`; `AnalyzerSetupSwitchTransaction.kt`; `AnalyzerSetupAudioOwnerGate.kt`; `AnalyzerSetupTransitionCoordinator.kt`; `AnalyzerSimpleSetupPanel.kt`; `AnalyzerModeCoordinator.kt`; `RollingAudioPolicy.kt`; `RollingAudioService.kt`; `AnalyzerSessionGateway.kt`; `AnalyzerMonitor.kt` (jeweils relativ zum oben genannten App-Quellpfad, sofern nicht vollständig angegeben).

## 13. Revisionshistorie

| Revision | Datum | Inhalt |
|---|---|---|
| 1 | 2026-10-16 | Erstfassung, Speicherung ursprünglich nur beim Analyzer-Verlassen und OFF-01 ungeklärt. |
| 2 | 2026-10-16 | Verbindliche Entscheidung: alle Änderungen an A zwingend vor Aktivierung von B nach A.json speichern; Speicherung auch beim Analyzer-Verlassen. Sicherheits-, Fehler- und Regressionstests ergänzt; OFF-01 geschlossen. |
