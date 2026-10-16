# LFN-Scout – Gesamt-Software-Requirements-Specification (SRS)

**Basis:** v0.5.65 · **Stand:** 16.10.2026 · **Fassung:** Entwurf 01 · **Status:** Noch nicht fachlich freigegeben

> Dies ist die Gesamt-SRS der App, nicht nur die bisherige Teil-SRS zu Analyzer/Rolling/MON. Der Projektcode und das Original-ZIP wurden nicht verändert.

## Inhaltsübersicht

- 1. Geltungsbereich und Dokumentstatus
- 2. Produktziel, Nutzungsszenarien, Rollen
- 3. Scout: Beobachtung und Erinnerung
- 4. Scout-Konfiguration und Trennung der Setups
- 5. Rolling-Audio, Puffer und nachträgliche Sicherung
- 6. Analyzer Betriebsarten, Zustands- und Audioverwaltung
- 7. Analyzer-Signalverarbeitung und Messdarstellung
- 8. MON – Mithören und Live-Parameter
- 9. Analyzer-Setups, UI, Transaktion und Speicherung
- 10. Audioereignisse, WAV, Export und lokale Dateien
- 11. Akustische Statistik und Halbstunden-Buckets
- 12. Cloud, Supabase, Offline-Warteschlange und Datenschutz
- 13. Standort, Zeit, Synchronisation und Messkontext
- 14. Bedienoberfläche, Status und Zugänglichkeit
- 15. Qualität, Betrieb und Sicherheit
- 16. Regressionstests und integrierter Selbsttest
- 17. Traceability, Akzeptanzstatus und Priorisierung
- 18. Offene fachliche und technische Punkte
- 19. Quellen und mitgeltende Dokumente

## 1. Geltungsbereich und Dokumentstatus

Dieses Dokument beschreibt die **gesamte Android-Anwendung LFN-Scout** einschließlich Scout-Beobachtung, Audioaufzeichnung, Analyzer, Statistik, lokaler Datenhaltung, Cloud-Anbindung, Export, Bedienoberfläche, Build und Test. Es ist eine **konsolidierte Ausgangsfassung zur fachlichen Freigabe**, kein Nachweis, dass jede Anforderung in v0.5.65 erfüllt ist.

Die Schlüsselwörter **MUSS**, **SOLL** und **KANN** kennzeichnen Verbindlichkeit. Ein vorhandener Codepfad ist nicht automatisch hardwareverifiziert. Herkunftsmarkierungen: **[B]** aus Projektquellen dokumentierter Bestand; **[F]** in dieser Gesprächsfolge verbindlich entschieden; **[P]** geplante Erweiterung oder Prüfbedarf.

Quellbasis: vollständiges Quell-ZIP v0.5.65 (versionCode 223), insbesondere `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `docs/project/CURRENT_STATE.md`, Analyzer-SRD/SRS v0.7 und die in Abschnitt 19 genannten Fachdokumente. Neueste Entscheidungen aus diesem Gespräch haben für betroffene Anforderungen Vorrang vor älteren Projekttexten.

## 2. Produktziel, Nutzungsszenarien, Rollen

- **SYS-001 [B]** LFN-Scout MUSS subjektive Beobachtungen niederfrequenter Phänomene zeitlich erfassen und mit objektiven akustischen Messdaten in Beziehung setzen können.
- **SYS-002 [B]** Die App MUSS ohne permanente Internetverbindung grundlegende Beobachtungs-, Aufnahme- und Messfunktionen anbieten; Cloud-Synchronisation ist getrennt zu behandeln.
- **SYS-003 [B]** Die App MUSS zwischen dem Beobachtungs-/Scout-Bereich und dem spezialisierten Analyzer unterscheiden; der Benutzer darf zwischen beiden wechseln, ohne ungewollten Datenverlust.
- **SYS-004 [F]** Analyzer STOP ist vom Fortbestehen des Rolling-Audiostreams zu unterscheiden.
- **SYS-005 [P]** Für Messungen mit mehreren Teilnehmern sind reproduzierbare Zeiten, Statusdaten, Metadaten und kompatible Exportformate wesentlich.
- **SYS-006 [B]** Die sichtbare Produktbezeichnung lautet LFN-Scout; bestehende interne Verzeichnis-/Projektkennungen werden nicht allein wegen des sichtbaren Namens umbenannt.

## 3. Scout: Beobachtung und Erinnerung

- **OBS-001 [B]** Der Scout MUSS eine manuelle Intensitätsbewertung unterstützen, einschließlich des gesonderten Zustands »nicht beobachtet«.
- **OBS-002 [B]** Beobachtungen MÜSSEN einen Zeitstempel und die zum Ereignis verfügbaren Beobachter-/Messkontextdaten erhalten.
- **OBS-003 [B]** Erinnerungs- und Beobachtungseinstellungen MÜSSEN über das erste Scout-Einstellungs-Panel erreichbar sein.
- **OBS-004 [B]** Eine zusätzliche Einstellungs-Schaltfläche direkt an der Bewertungs-/Beobachtungsfunktion SOLL zu diesen Einstellungen führen.
- **OBS-005 [B]** Erinnerungsintervall, aktiv/inaktiv und einschlägige Scout-Optionen MÜSSEN nach App-Neustart entsprechend der vorhandenen Scout-Persistenz wiederhergestellt werden.
- **OBS-006 [P]** Unterscheidung zwischen fehlender Beobachtung, bewusster Null-Bewertung und technischem Nichtverfügbarsein ist im Datenmodell eindeutig zu dokumentieren und zu testen.
- **OBS-007 [P]** Die genauen Zeitfenster-/Timeout-Regeln, UI-Texte und alle Beobachtungsfelder sind anhand der bestehenden Implementierung in einen vollständigen Datenfeldkatalog zu übernehmen.

## 4. Scout-Konfiguration und Trennung der Setups

- **SCFG-001 [F]** Scout-Einstellungen und Analyzer-JSON-Setups MÜSSEN logisch getrennt bleiben.
- **SCFG-002 [B]** Die vorhandene Scout-Speicherung und Persistenz bleibt unverändert, solange nicht eine separat genehmigte Fachänderung erforderlich ist.
- **SCFG-003 [F]** Parameter der Rolling-Erfassung (z. B. Laufzeitgrenze und Aufnahmeeinstellungen) gehören zur Scout-/Rolling-Verantwortung, nicht zum Analyzer-JSON-Setup.
- **SCFG-004 [F]** Der Analyzer darf durch Laden eines seiner JSON-Setups weder Rolling-Grundparameter noch Scout-Beobachtungsparameter stillschweigend verändern.
- **SCFG-005 [P]** Konflikte zwischen geändertem Scout-Aufnahmegerät und bereits laufender Aufnahme bedürfen gesonderter Stop/Neustart- und Datenbewahrungsregeln.

## 5. Rolling-Audio, Puffer und nachträgliche Sicherung

- **ROL-001 [F]** Rolling MUSS bei Analyzer RUN → STOP unabhängig weiterlaufen.
- **ROL-002 [F]** Rolling MUSS bei Analyzer STOP → RUN unabhängig weiterlaufen.
- **ROL-003 [F]** Rolling MUSS während eines zulässigen Analyzer-JSON-Setupwechsels weiterlaufen, ohne Lücken im Aufnahmestrom oder Reset des Ringpuffers.
- **ROL-004 [F]** Der Benutzer MUSS unerwartete Ereignisse rückwirkend abspeichern können, solange deren Audiodaten im Rolling-Puffer vorhanden sind; dies gilt auch über STOP/RUN-Wechsel hinweg.
- **ROL-005 [F]** Beim *Verlassen des gesamten Analyzers* MUSS der bereits bestehende Dialog fragen, ob Rolling beendet oder fortgesetzt werden soll. Kein zweiter Dialog.
- **ROL-006 [B]** Für den spezialisierten 8-kHz-Analyzer wird ein bereits laufender Rolling-Stream mitbenutzt; falls Rolling noch nicht eingeschaltet ist, wird er beim Start des 8-kHz-Analyzers aktiviert. Diese bereits funktionierende Funktion ist zu erhalten.
- **ROL-007 [F]** Bei Analyzer STOP MUSS dessen Verarbeitung vom gemeinsamen Stream abgemeldet werden; bei RUN wieder angemeldet werden, ohne allein deshalb Rolling neu zu starten.
- **ROL-008 [B]** Pufferlänge und maximaler Rolling-Betriebszeitraum sind getrennte Größen. In der untersuchten Codebasis dokumentierte Defaults (z. B. 30-Minuten-Puffer, 12-Stunden-Betriebsdauer) sind implementierungsabhängig und bedürfen technischer Verifikation.
- **ROL-009 [F]** FFT-/Anzeige-/MON-Änderungen dürfen den unverarbeiteten Rolling-Puffer weder überschreiben noch filterseitig rückwirkend verändern.
- **ROL-010 [P]** Bei tatsächlichem Geräteverlust, Audiofehler, Android-Energiesparintervention oder Aufnahmekonflikt MUSS ein klarer Ausfallstatus geführt werden; Kontinuität darf nicht fälschlich behauptet werden.
- **ROL-011 [P]** Puffer-Überlauf, zeitliche Genauigkeit, Dateischnitt über Puffergrenzen und Aufbewahrung einzelner Audioschnipsel sind für Hardwaretests zu spezifizieren.
- **ROL-012 [B]** Der sichtbare Aufnahmeindikator muss den tatsächlichen Rolling-Zustand wiedergeben, nicht nur die angeforderte Schalterstellung.

## 6. Analyzer Betriebsarten, Zustands- und Audioverwaltung

- **AN-001 [B]** Analyzer MUSS die verfügbaren unterstützten Betriebsarten (einschließlich des Rolling-basierten 8-kHz-Modus und der dokumentierten Direktbetriebsarten) klar unterscheiden.
- **AN-002 [F]** Die Bedienzustände RUN, STOP, MON, Vorschau und Aufnahmen MÜSSEN intern separat erfasst und diagnostizierbar sein.
- **AN-003 [F]** Eine aktive Rolling-Aufnahme allein darf einen sonst sicheren Analyzer-Setupwechsel im Zustand STOP nicht verhindern.
- **AN-004 [F]** Die tatsächliche Beteiligung des Analyzers am Stream, eine laufende Direct-Aufnahme, MON-Eigentümerschaft oder noch ausstehende Ressourcenfreigabe MÜSSEN weiterhin sicherheitsgeprüft werden.
- **AN-005 [F]** Sicherheitsprüfungen dürfen nicht pauschal deaktiviert oder durch UI-Vermutungen ersetzt werden.
- **AN-006 [F]** Stopp/Start und Setupwechsel dürfen nicht versehentlich die gemeinsam genutzte 8-kHz-Audioquelle neu initialisieren.
- **AN-007 [P]** Geräte-/Sample-Rate-Wechsel, die tatsächlich die Rolling-Audioquelle betreffen, benötigen eine explizite Konfliktstrategie; bis zu deren Prüfung ist der Wechsel sicher abzulehnen.
- **AN-008 [B]** Der Analyzer besitzt unterschiedliche Pfade für Direct und Rolling; die Effektivparameter, Synchronisations- und Ressourcenregeln sind je Pfad im SDS zu dokumentieren.
- **AN-009 [B]** Hardware-/Mikrofonunterstützung, Android-Audio-Berechtigungen und Audio-Routing sind vor dem Start zu prüfen und im Fehlerfall anzuzeigen.

## 7. Analyzer-Signalverarbeitung und Messdarstellung

- **DSP-001 [B]** Der Analyzer MUSS Spektren und zeitaufgelöste Wasserfälle für niederfrequente Signale darstellen.
- **DSP-002 [B]** Wählbare, mit dem jeweiligen Audio-Modus kompatible FFT-Länge, FFT-Hop, Fensterung, Frequenzbereich und Anzeigeparameter sind im Setup abzubilden.
- **DSP-003 [B]** Signalpfade mit Decimation und digitaler Filterung MÜSSEN die tatsächlich wirksame Nyquist-Grenze und Aliasing-Schutzregeln berücksichtigen.
- **DSP-004 [B]** Spektrum/Wasserfall MÜSSEN vorhandene Funktionen wie Zoom/Pan, Glättung, Kurvenauswahl und Reset entsprechend den dokumentierten Bedienkontrakten bewahren.
- **DSP-005 [B]** Die vorhandenen schnellen Filterverfahren, Filterbank- und Vergleichsfunktionen sind mit ihren Parametern und Gültigkeitsgrenzen vollständig im Analyzer-Fachanhang nachzuführen.
- **DSP-006 [B]** Bereits dokumentierte Kennwertmethoden (u. a. Leq, L50, L90, Filter-/Impulskennwerte) dürfen durch Setupkorrekturen nicht stillschweigend verändert werden.
- **DSP-007 [B]** Messpegel und Anzeigeeinheiten MÜSSEN ihren tatsächlichen Bezug (z. B. dBFS versus kalibrierter dB SPL) erkennen lassen.
- **DSP-008 [P]** Alle Analyzer-Parameter benötigen einen versionierten Katalog aus Name, Typ, Einheit, Gültigkeitsbereich, Standardwert, Laufzeitänderbarkeit, Persistenzort und Wirkung auf Rolling/MON.
- **DSP-009 [P]** Messalgorithmen und Kalibrierung sind als eigenständige, prüfbare Anforderungen aus dem vorhandenen Analyzer-v0.7-Fachstand zu übernehmen, nicht frei zu ersetzen.

## 8. MON – Mithören und Live-Parameter

- **MON-001 [F]** Alle MON-Signalverarbeitungsparameter (z. B. Gain, Mithörfilter) MÜSSEN während des Betriebs *on the fly* änderbar sein.
- **MON-002 [F]** Eine MON-Parameteränderung darf keinen Analyzer-STOP verlangen.
- **MON-003 [F]** MON-Parameteränderungen dürfen keinen Neustart oder Unterbruch des Rolling-8-kHz-Audiostreams auslösen und keinen Rolling-Pufferinhalt verlieren.
- **MON-004 [F]** Filterung/Gain im Mithörzweig dürfen keine bereits erfassten Rohdaten im Rolling-Puffer verändern.
- **MON-005 [F]** MON-Live-Änderungen sind vom vollständigen benannten Analyzer-Setupwechsel zu unterscheiden.
- **MON-006 [P]** Technische Grenzfälle bei geänderten Filterkoeffizienten, Kanalbelegung, Gain-Sprüngen und Latenz sind im SDS als atomare Übernahme, Glättung oder Fehlerzustand festzulegen.
- **MON-007 [P]** Hardware-Routing-Parameter, die eine Audio-Neuinitialisierung erfordern würden, sind getrennt von reinen MON-DSP-Parametern zu kennzeichnen.
- **MON-008 [F]** Geänderte MON-Parameter MÜSSEN im tatsächlich aktiven benannten Analyzer-Setup korrekt erfasst und nach der jeweils fälligen Speicheroperation wiederhergestellt werden.

## 9. Analyzer-Setups, UI, Transaktion und Speicherung

- **SET-001 [F]** Jeder benannte Analyzer-Setupdatensatz MUSS in einer eigenen versionierten JSON-Datei im App-Speicher geführt werden.
- **SET-002 [F]** Beliebig viele benannte Setups sind über eine scrollbare Auswahl erreichbar; die drei zuletzt benutzten sind als MRU-Schnellwahl zugänglich.
- **SET-003 [F]** Die Setup-Oberfläche MUSS hell, übersichtlich und als *ein* maßgebliches Setup-System gestaltet werden; Legacy-Slots dürfen kein konkurrierendes Bedienkonzept bilden.
- **SET-004 [F]** Bei STOP MUSS das Umschalten auf ein anderes benanntes Setup möglich sein, wenn keine tatsächlich inkompatiblen Analyzer-/MON-/Audiooperationen aktiv sind. Rolling allein ist kein Sperrgrund.
- **SET-005 [F]** Beim Verlassen eines Einstellungs-Panels MÜSSEN die Änderungen ohne Einzelbestätigung in die aktive Laufzeitkonfiguration übernommen werden.
- **SET-006 [F]** **Vor** der Aktivierung eines anderen benannten Setups MÜSSEN alle aufgelaufenen Änderungen am bisher aktiven Setup in dessen eigener JSON-Datei dauerhaft gespeichert sein.
- **SET-007 [F]** Nur bei *erfolgreich abgeschlossenem Speichern* darf das neue Setup geladen und aktiviert werden. Bei Schreibfehler bleibt das bisherige Setup aktiv und sämtliche Änderungen bleiben erhalten.
- **SET-008 [F]** Beim Verlassen des gesamten Analyzers MUSS das zu diesem Zeitpunkt aktive Setup ebenfalls dauerhaft gespeichert werden.
- **SET-009 [F]** Schreiben der JSON-Datei MUSS gegen Teil-/Abbruchschreiben abgesichert sein; tatsächliche Dateisystemgarantien sind zu prüfen.
- **SET-010 [F]** Die aktive Setupbezeichnung darf erst nach erfolgreicher Transaktion aktualisiert werden; MRU ebenfalls erst danach.
- **SET-011 [F]** Das wirklich startwirksame Setup MUSS mit der Anzeige übereinstimmen. Eine bloß temporäre JSON-Entwurfsübernahme oder nachfolgendes Laden alter Legacy-Preferences ist kein erfolgreicher Setupwechsel.
- **SET-012 [B]** SAF-Import/-Export bleibt vorgesehen, ohne allgemeine Dateisystemberechtigung; Fehler-/Versionsprüfung und Namenskollisionen müssen definiert sein.
- **SET-013 [B]** Fallback beim nicht verfügbaren Aufnahmegerät muss konsistent im aktiven Setup und Laufzeitstatus erkennbar sein.
- **SET-014 [F]** Änderungen an MON, Zoom, angezeigten Kurven und weiteren laufenden Analyzerparametern MÜSSEN dem aktiven Setup zugeordnet sein, sofern sie Teil seines Schemas sind.
- **SET-015 [B]** In v0.5.65 ist der Setupwechsel laut `CURRENT_STATE.md` ausdrücklich noch nicht produktiv vollständig START-wirksam; dies ist ein offener Implementierungsdefekt, kein erfülltes Requirement.
- **SET-016 [P]** Schreibfehler, ungültiges JSON, fehlende Felder, Migration, Rücknahme und schnelle aufeinanderfolgende Auswahl sind atomar und deterministisch zu spezifizieren.

## 10. Audioereignisse, WAV, Export und lokale Dateien

- **REC-001 [B]** Manuelle und rückwirkende Audioereignisse MÜSSEN mit eindeutiger zeitlicher Zuordnung gesichert werden können.
- **REC-002 [B]** Gespeicherte WAVs und zugehörige Statistik-/Metadaten MÜSSEN konsistent verknüpft bleiben.
- **REC-003 [B]** Bestehende Exportpfade für WAV, CSV, Screenshots/PNG und weitere dokumentierte Dateien dürfen nicht regressieren.
- **REC-004 [B]** Herkunft des Audioereignisses (`capture_origin`) und Triggergrund (`trigger_type`, Metadaten) sind entsprechend dem bestehenden Cloud-Vertrag getrennt zu erfassen.
- **REC-005 [B]** TEST- und REGULAR-Installationen besitzen getrennte Speicher-/Paketkontexte gemäß verbindlichem Build-Workflow.
- **REC-006 [P]** Dateibenennung, Zeitzonen, Datum, Speicherplatzfehler, Android-Scoped-Storage und Dateikollisionen benötigen End-to-End-Testfälle.
- **REC-007 [P]** Jede bestätigte WAV-Speicherung muss belegbar vollständig sein; bei partiellen Schreibfehlern ist eine erkennbare Diagnose erforderlich.

## 11. Akustische Statistik und Halbstunden-Buckets

- **STAT-001 [B]** Exakt an :00 und :30 ausgerichtete 30-Minuten-Zeitfenster MÜSSEN entsprechend dem bestehenden Statistikvertrag behandelt werden.
- **STAT-002 [B]** Leq, L50, L90 sowie Zeitabdeckung, Messbereich, Qualität und Beobachter-/Standortbezug sind entsprechend den dokumentierten Datenmodellen auszugeben.
- **STAT-003 [B]** STAT-/Audio-Filterdaten umfassen eine Overall-Konfiguration und bis zu 40 Filterdatensätze je WAV im Backend-Vertrag.
- **STAT-004 [B]** Spezifische Impulskennwerte (`peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`) bleiben vorerst lokal und in der Halbstunden-CSV; keine vorgezogene Supabase-Übertragung.
- **STAT-005 [B]** Die vier Impulskennwerte erhalten bei späterer, ausdrücklich freigegebener Cloud-Erweiterung eigene benannte DB-Spalten statt Ersatz durch generische P01–P04.
- **STAT-006 [B]** Reservefelder P01–P32 dürfen etablierte benannte Statistikfelder nicht ersetzen.
- **STAT-007 [P]** Für alle Kennwerte sind Fensterung, Auswahl der gültigen Daten, Bewertungsreferenz, fehlende Daten und Nachrechenbarkeit durch Testsignale festzulegen.

## 12. Cloud, Supabase, Offline-Warteschlange und Datenschutz

- **CLD-001 [B]** Bestehende Backend-Funktionen, Tabellen und Legacy-Anfragen müssen kompatibel bleiben, insbesondere `submit_observation`.
- **CLD-002 [B]** Audio-/Mess-/Halbstundenstatistik-Schnittstellen werden nur mit passend versioniertem Schema und Migration genutzt.
- **CLD-003 [B]** Ausstehende Datensätze müssen offline persistent vorgehalten und bei Netzrückkehr sicher übertragen werden können.
- **CLD-004 [B]** Ein bestätigter Cloud-Erfolg führt bei akustischer Halbstundenstatistik zunächst zum persistenten Status `SENT_PENDING_DELETE` samt Bestätigungszeitpunkt; Löschung erst frühestens nach 24 Stunden.
- **CLD-005 [B]** Bis zur Löschfrist darf der bereits erfolgreich gesendete Datensatz nicht versehentlich erneut übertragen werden.
- **CLD-006 [B]** Fehlgeschlagene Sendungen müssen unterscheidbar, nachvollziehbar und wiederholbar bleiben.
- **CLD-007 [B]** Standort-/Benutzerkennungen, präzise Koordinaten und Uploads dürfen nur gemäß dokumentiertem Datenschutz-/Berechtigungskonzept behandelt werden.
- **CLD-008 [P]** Vollständiger Restore-Test auf ein leeres Supabase-Projekt mit anschließendem End-to-End-Wiederherstellungstest bleibt eine zurückgestellte Aufgabe.
- **CLD-009 [P]** API-Versionen, Authentifizierung, Tabellenfelder und Retention sind in einem separaten, vollständigen Schnittstellenkatalog zu verifizieren.

## 13. Standort, Zeit, Synchronisation und Messkontext

- **CTX-001 [B]** Beobachtungen und Aufnahmen tragen die dokumentierten Zeit- und Standortinformationen sowie einen Location-Status, falls GPS nicht verfügbar ist.
- **CTX-002 [B]** Ein fehlender Standort darf nicht heimlich durch erfundene Koordinaten ersetzt werden.
- **CTX-003 [P]** Zeitbasis, monotone Streamzeit, Wall-Clock-Zeit, Zeitzone und Zeitumstellungen müssen für rückwirkende Ereignisse eindeutig aufeinander abgebildet werden.
- **CTX-004 [P]** Mehrgeräte-Korrelation und externe Kontextquellen (z. B. Wetter) bleiben als Erweiterungen ausdrücklich vom belegten v0.5.65-Funktionsumfang getrennt.

## 14. Bedienoberfläche, Status und Zugänglichkeit

- **UI-001 [F]** Für die neue Analyzer-Setup-Bedienung gilt eine helle, nachvollziehbare Oberfläche mit eindeutiger aktiver Setupbezeichnung.
- **UI-002 [F]** Der Status des wirklich aktiven Setups und der Status der bloß zugeordneten JSON-Datei dürfen nicht verwechselt werden.
- **UI-003 [F]** Eine blockierte Umschaltung muss den konkreten Sperrgrund melden, nicht nur »STOP erforderlich; Aufnahme/MON/Vorschau beenden«.
- **UI-004 [B]** Bestehende Gesten für Spektrum, Wasserfall, Zoom, Reset und Export bleiben gemäß Analyzer-Bedienkontrakten erhalten.
- **UI-005 [B]** Bedienung muss zwischen Analyzer RUN/STOP und Rolling aktiv/inaktiv klar unterscheiden.
- **UI-006 [P]** Verschiedene Displaygrößen, Scrollbarkeit, Textlänge, Farbkontrast und Android-Berechtigungsdialoge sind in UI-Tests abzudecken.

## 15. Qualität, Betrieb und Sicherheit

- **NFR-001 [F]** Kein Datenverlust des Rolling-Puffers bei Analyzer STOP/RUN oder zulässigen JSON-Setupwechseln.
- **NFR-002 [F]** Kein stilles Verwerfen ungespeicherter Setupänderungen bei einem fehlgeschlagenen Wechsel.
- **NFR-003 [B]** Build-/Release-Versprechen müssen zwischen Quelltextprüfung, Kompilierung, Unit-Tests, Instrumentation und echtem Gerätetest unterscheiden.
- **NFR-004 [B]** Die Abwärtskompatibilität von Altsetups und bereits gespeicherten Messdateien ist zu erhalten beziehungsweise über explizite Migrationen zu gewährleisten.
- **NFR-005 [P]** Langzeitbetrieb, Audio-Latenz, CPU-/Akkubelastung, Speicher- und Netzlast brauchen messbare Akzeptanzschwellen.
- **NFR-006 [B]** Fehler sollen diagnostizierbar sein, ohne vertrauliche Nutzerdaten unnötig in Logs zu schreiben.
- **NFR-007 [B]** Versionen folgen dem Format Hauptversion.Nebenversion.Patch, mit monoton steigendem Android-versionCode.

## 16. Regressionstests und integrierter Selbsttest

- **TST-001 [F]** Setupwechsel bei STOP mit aktivem Rolling MUSS gelingen, sofern keine anderen Ressourcenkonflikte bestehen.
- **TST-002 [F]** RUN → STOP → Setupwechsel → RUN MUSS den Rolling-Stream samt zeitlicher Kontinuität und Ereignispuffer erhalten.
- **TST-003 [F]** Gespeichertes Rolling-Ereignis MUSS Daten vor und nach einem STOP/RUN-Übergang korrekt enthalten, wenn innerhalb der Pufferhistorie.
- **TST-004 [F]** Fehler beim Speichern von Setup A MUSS Wechsel zu B verhindern; A und seine Änderungen bleiben erhalten.
- **TST-005 [F]** Erfolg beim Speichern von A MUSS vor Aktivierung von B nachweisbar sein.
- **TST-006 [F]** START muss das wirklich gewählte Setup laden; Legacy-Preference-Rückfall ohne Kennzeichnung ist ein Testfehler.
- **TST-007 [F]** MRU-/aktive-Setup-Anzeige muss bei Erfolg, Fehler und schnellem mehrfachen Wechsel korrekt sein.
- **TST-008 [F]** MON-Gain und MON-Filter müssen während laufendem Analyzer ohne STOP und ohne Rolling-Neustart änderbar sein.
- **TST-009 [F]** MON-Filter dürfen Rolling-Rohdaten nicht verändern.
- **TST-010 [F]** Der bestehende Analyzer-Exit-Dialog muss »Rolling weiterlaufen« und »Rolling stoppen« korrekt handhaben.
- **TST-011 [F]** Rolling muss vor Start des 8-kHz-Analyzers automatisch eingeschaltet werden, wenn noch nicht aktiv.
- **TST-012 [B]** Bekannte frühere Fehler müssen in Regressionstests dauerhaft abgedeckt werden.
- **TST-013 [B]** Ein integrierter Selbsttest mit PASS/FAIL für Setup-Schema, Save/Load, Ressourcen-Gates und synthetische Audiosignale ist anzustreben.
- **TST-014 [P]** Hardwaretests sollen die echte Mikrofonroute, Audio-Fokus, Bildschirmwechsel, Hintergrundbetrieb, Berechtigungsentzug und Android-Lifecycle abdecken.
- **TST-015 [B]** TEST-Build und REGULAR-Build dürfen sich nicht gegenseitig Daten/Installationen überschreiben; Workflows und Artefakte sind getrennt zu prüfen.

## 17. Traceability, Akzeptanzstatus und Priorisierung

Jede einzelne Requirement-ID MUSS im nächsten Schritt um folgende Spalten ergänzt werden: **Quellenbeleg**, **Kotlin-Komponente**, **Prüfverfahren**, **Testergebnis**, **Implementierungsstatus** und **Freigabeentscheidung**. Besonders kritisch: `ROL-001..012`, `AN-002..007`, `MON-001..008`, `SET-004..016`, `TST-001..011`.

**Priorität 1:** Funktionierender Setupwechsel bei STOP und weiterlaufendem Rolling; tatsächliche startwirksame Aktivierung; transaktionale Speicherung; korrekte UI-Anzeige; keine Audio-Unterbrechung. **Priorität 2:** Verifizierbare MON-Live-Änderungen; Import/Export-/Persistenzvollständigkeit; längere Hardwaretests. **Priorität 3:** Geplante Ausbaustufen, neue Cloud-Felder und weitergehende Messmethoden.

Eine neue Quellversion darf erst nach expliziter Freigabe zur Codeänderung erstellt werden.

## 18. Offene fachliche und technische Punkte

1. Vollständiger *Ist*-Parameterkatalog aller Scout-, Analyzer-, MON- und Rolling-Felder mit Eigentümerschaft, Einheiten und Persistenzpfaden.
2. Exakte Regeln, welche Analyzer-Parameter während RUN und welche ausschließlich bei STOP geändert werden dürfen, getrennt nach Rolling/Direct.
3. Nachweis der Audio-Pipeline von der 8-kHz-Mikrofonaufnahme über Analyse-Zweig und 1-kHz-Rolling-Puffer einschließlich Sample-Zeiten.
4. Verhalten bei externem Mikrofonwechsel, Berechtigungsentzug und konkurrierenden Audio-Clients.
5. Vollständige Traceability aller vorhandenen Analyzer-v0.7-SRS-Anforderungen sowie historisch eingeführter Scout-Funktionen.
6. Vollständige Feld- und Schnittstelleninventur des Supabase-Backends; besonders noch nicht aktivierte Android-Uploads.
7. Freigabekriterien und realer Hardware-Nachweis auf dem Zielgerät.

Diese Punkte werden nicht als bereits erfüllte Eigenschaften ausgegeben.

## 19. Quellen und mitgeltende Dokumente

- `DIRECTIVES.md` – verbindliche Entwicklungsdirektiven.
- `BUILD_WORKFLOWS.md` – Build-/Freigabewege.
- `docs/project/CURRENT_STATE.md` – priorisierter, aktueller Implementierungsstand v0.5.65.
- `PROJECT_STATUS.md`, `CHANGELOG.md` – chronologischer Projektstatus.
- `docs/project/analyzer_v0.7/docs/project/01_SRD_LFN-Scout_Analyzer_v0.7.md` und `02_SRS_LFN-Scout_Analyzer_v0.7.md` – detaillierter Analyzer-Fachbestand.
- `docs/project/ROLLING_AND_STATISTICS_REQUIREMENTS_v0.5.27.md`, `ROLLING_HALF_HOUR_v0.5.29.md`, `ROLLING_FILTER_STATISTICS_v0.5.31.md`.
- `docs/project/SUPABASE_STATISTICS_V0.5.26.md`, `ANDROID_CLOUD_V0.5.27.md`, `LFN-Scout_SUPABASE_GESAMTPARAMETERKATALOG_v1.md`.
- `docs/project/PHASE3O_TRANSAKTIONSSCHUTZ_v0.5.56.md`, `PHASE3N_UMSCHALTPLAN_v0.5.56.md`.
- `docs/project/LFN-Scout_Geplante_Erweiterungen.md`, `DEFERRED_TASKS.md`.
- Weitere einzelne Dokumente unter `docs/project/` bleiben ergänzend gültig, soweit keine neuere verbindliche Entscheidung widerspricht.

**Hinweis zur Vollständigkeit:** Dieses Dokument ist eine *Gesamtstruktur mit konsolidierten zentralen Anforderungen*, noch kein zeilenweise gegen alle Kotlin-Funktionen und historischen Fachdokumente abgeglichener Endstand. Insbesondere der vollständige, ggf. mehrere hundert Felder umfassende Parameter- und Schnittstellenkatalog muss als Anhang eingebunden werden, bevor die SRS als vollständig und freigegeben bezeichnet werden darf.
