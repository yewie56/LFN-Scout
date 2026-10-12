# LFN-Scout Analyzer – kumulative Gesamtdokumentation v0.7

**Status: Dokumentations-/Übergabestand, keine implementierte App.** Maßgeblich sind die Einzeldateien im Paket; dieses Dokument ist eine vollständig zusammengeführte Lesefassung. Originalbilder sind über die verlinkten Dateien im selben ZIP erreichbar.



---

# QUELLDOKUMENT: `docs/project/00_INDEX.md`

# Dokumentenindex – LFN-Scout Analyzer v0.7

**Start:** [`../../README.md`](docs/project/../../README.md), dann [`18_ABSCHLUSS_AUDIT_v0.7.md`](docs/project/18_ABSCHLUSS_AUDIT_v0.7.md) und [`../../assets/concepts/BILDREGISTER_v0.7.md`](docs/project/../../assets/concepts/BILDREGISTER_v0.7.md). Alle Dokumente beschreiben geplante Funktionen, keine implementierte App.

01. [SRD – Systemanforderungen](docs/project/01_SRD_LFN-Scout_Analyzer_v0.7.md)
02. [SRS – Softwareanforderungen](docs/project/02_SRS_LFN-Scout_Analyzer_v0.7.md)
03. [SDS – Architektur](docs/project/03_SDS_Architektur_v0.7.md)
04. [UI-/Gestenkonzept](docs/project/04_UI_Gesten_v0.7.md)
05. [Algorithmen/Kalibrierung](docs/project/05_Algorithmen_Kalibrierung_v0.7.md)
06. [Test-/Validierungsplan](docs/project/06_Test_Validierung_v0.7.md)
07. [Integration/Übergabe](docs/project/07_Integration_Uebergabe_v0.7.md)
08. [Traceability/IDs](docs/project/08_Traceability_v0.7.md)
09. [Entscheidungen/offene Punkte](docs/project/09_Entscheidungen_Offene_Punkte_v0.7.md)
10. [Kurzbedienung](docs/project/10_Bedienung_Kurz_v0.7.md)
11. [Changelog/Herkunft](docs/project/11_CHANGELOG_Herkunft_v0.7.md)
12. [Quellen/Referenzen](docs/project/12_Quellen_Referenzen_v0.7.md)
13. [Parameterergänzungen](docs/project/13_Parameter_Ergaenzungen_v0.7.md)
14. [Radiushistogramme](docs/project/14_Histogramm_Integration_v0.7.md)
15. [Offscreen-PNG/Panelwechsel](docs/project/15_PNG_Hintergrund_Panelwechsel_v0.7.md)
16. [Repo-Mapping/Gates](docs/project/16_Repo_Mapping_und_Gates_v0.7.md)
17. [Standalone-Testhost](docs/project/17_Standalone_Testhost_v0.7.md)
18. [Abschluss-Audit/Restgates](docs/project/18_ABSCHLUSS_AUDIT_v0.7.md)

**Normative Parameter:** [`Funktionsreferenz v0.4`](docs/project/../../references/Funktionsreferenz_v0.4.md) + [`13_Parameter_Ergaenzungen_v0.7.md`](docs/project/13_Parameter_Ergaenzungen_v0.7.md).  
**Visuelle Referenzen:** [`Bildregister mit Freigabestatus`](docs/project/../../assets/concepts/BILDREGISTER_v0.7.md) · [`D01 Histogramm-Schema (nicht freigegeben)`](docs/project/../../assets/diagrams/D01_Histogrammspalte_SOLL_Schema.png).  
**Originalbezug:** [`Spectroid-Anleitung v10.5`](docs/project/../../references/Spectroid_Anleitung_v10_5.docx), [`Spectroid-Screenshots`](docs/project/../../references/Spectroid-Screens.zip).  
**Beispieldaten:** [`analyzer_setup_example_v0.6.json`](docs/project/../../schemas/analyzer_setup_example_v0.6.json), weitere JSONs unter `schemas/`.



---

# QUELLDOKUMENT: `docs/project/01_SRD_LFN-Scout_Analyzer_v0.7.md`

# SRD – LFN-Scout: Spektrum, Wasserfall, Kalibrierung und Mithören

**Dokument-ID:** LFN-AN-SRD · **Version:** 0.7 (konsolidierter Entwurf) · **Stand:** 19.09.2026  
**Dokumentart:** System Requirements Document (übergeordnete Systemanforderungen)  
**Zugehöriges Detaildokument:** `02_SRS_LFN-Scout_Analyzer_v0.7.md`  
**Gegenstand:** neues, in LFN-Scout integriertes Analyzer-Unterpanel; **keine Implementierung und kein Nachweis eines erfolgreichen Builds**.

## 1. Zweck, Quellen und Geltungsbereich

Das Unterpanel **„Spektrum & Mithören“** erweitert LFN-Scout um einen eigenständig entwickelten, **kontinuierlich arbeitenden Spectroid-ähnlichen Echtzeit-Spektrumanalysator**. Es ist **keine Audioaufnahme- oder Recording-Funktion**. Die Android-Mikrofonerfassung liefert nur flüchtige Live-PCM-Blöcke für Anzeige und optionales Mithören; aus dem neuen Unterpanel werden keine Mikrofon- oder Mithör-WAV-Dateien erzeugt. Funktionsumfang und Bedienfälle leiten sich aus `references/Spectroid_Anleitung_v10_5.docx` ab. Die vorhandene Anleitung und die vom Nutzer bereitgestellten Screenshots dienen als **funktionale Referenz**, nicht als zu übernehmender Programmcode, Markenname oder UI-Grafikbestand. Die neue Bedienoberfläche muss zur bestehenden LFN-Scout-Oberfläche passen. Andere Panels, Bewertungen, Audiofunktionen und Datenflüsse dürfen nicht stillschweigend verändert oder entfernt werden.

Für die Integration gelten die vorhandenen Projektartefakte `BUILD_WORKFLOWS.md`, `DIRECTIVES.md`, `CHANGELOG.md`, `docs/project/` und der tatsächlich aktuelle Quellcode als maßgebliche Quellen. Vor der Implementierung sind sie gegen den aktuellen Projekt-ZIP-/Repositorystand zu prüfen. Eine zuletzt berichtete Versionsangabe darf nicht mit dem aktuellen tatsächlichen Repo-Stand verwechselt werden; der aktuelle Quellcode wurde für dieses Paket **nicht geprüft**. Der interne Android-Bezeichner `de.brummbeobachter.app` bleibt aus Update-Kompatibilitätsgründen unverändert.

## 2. Begriffe

| Begriff | Bedeutung |
|---|---|
| Spektrum | Frequenzdarstellung des Eingangssignals, standardmäßig 0–100 Hz linear. |
| Wasserfall | Kontinuierlich aktualisierte Zeit-Frequenz-Darstellung; begrenzte Historie nur im flüchtigen Arbeitsspeicher. |
| Live-Eingangssignal | Flüchtige PCM-Blöcke aus dem Android-Mikrofoneingang vor Kalibrierung, Mithörfilterung und Mithörverstärkung; keine persistente Audioaufzeichnung. |
| dBFS | Digitaler Pegel bezogen auf die Vollaussteuerung des Digitalsignals. |
| dB SPL / dB(Z) | Absoluter Schalldruckpegel, nur bei geeigneter kalibrierter Messkette. |
| Kalibrierdatei | Messkettenbezogene Frequenzkorrektur, optional mit absolutem Referenzpunkt. |
| Mithören | Echtzeit-Ausgabe eines gefilterten, einstellbar verstärkten Audiozweigs auf Kopfhörer/kompatiblen Audioausgang. |
| Clipping | Übersteuerung der Eingangs- oder Ausgangssignalkette; getrennt anzuzeigen. |
| Bandpass | Frequenzbereich `[f_min, f_max]` des Mithörfilters; Anzeige als dezente Überlagerung in beiden Diagrammen. |
| Setup / Messprofil | Ein benannter, versionierter, dauerhaft speicherbarer Satz sämtlicher relevanter Analyzer- und Mithörparameter, der jederzeit über **Recall/Laden** wieder aufgerufen werden kann. |
| Aktives Setup | Zuletzt erfolgreich geladenes oder gespeichertes Setup; nicht gespeicherte Änderungen werden gesondert markiert. |
| Automatische Mithör-Abregelung | Ausschließlich im Ausgabepfad arbeitende Verstärkungsreduzierung zur Vermeidung digitaler Übersteuerung des Mithörsignals. |

## 3. Systemziel und Systemgrenze

**Ziel:** Menschen sollen wiederkehrende niederfrequente Signale **in Echtzeit** erkennen, im kontinuierlichen Spektrum und Wasserfall beobachten und bei Bedarf mithören können, ohne dass die Mithörverarbeitung die Anzeige verfälscht. Das System unterstützt orientierende Untersuchungen von Brummtönen und technischen tieffrequenten Geräuschen; es liefert **keinen automatischen Nachweis einer bestimmten Quelle und keine medizinische Diagnose**.

**Im Umfang (verbindlich):** Live-Eingangswahl und -status; kontinuierliche FFT-Analyse, Zoom/Pan und fortlaufender Wasserfall; lineare Frequenzachse; dBFS und optionale Kalibrierung; Peak-/Max-Hold-Funktionen; mehrere abrufbare Setups; gemeinsamer, in beiden Diagrammen bedienbarer Mithörbandpass; Verstärkung, Clip-Erkennung, ausschließlich mithörseitige Abregelung; Audio-Routing/Sicherheitslogik; Testsignal; automatisch nach Wasserfallzyklen oder manuell erzeugte **PNG-Bilder** und ausschließlich manuell erzeugte aktuelle Spektraldaten als CSV; Diagnosestatus und reproduzierbare Tests. **Außerhalb des Umfangs:** Audio-Dateiaufzeichnung, WAV-Export, Wiedergabe alter Audiodaten und automatisches Dauer-Logging. Bestehende LFN-Scout-Funktionen müssen nachweislich erhalten bleiben; ein interner Adapter zur gemeinsamen Audioerfassung ist für die parallele Analyzer-/Recorder-Nutzung ausdrücklich zulässig, sofern die Recorder-Semantik unverändert bleibt.

**Nicht als vorhanden zu unterstellen:** Mikrofon- oder Ausgangscharakteristik eines konkreten Smartphones, eine kalibrierbare Frequenzantwort unterhalb des gerätespezifischen Übertragungsbereichs, absolute SPL-Messbarkeit ohne Referenz, konstante Bluetooth-Latenz, bereits implementierte Unterstützung für jede denkbare USB-Soundkarte, Erkennung eines konkreten Brummton-Verursachers oder Quellrichtung allein aus einem Mikrofon.

## 4. Systemanforderungen

Jede Anforderung besitzt eine dauerhafte ID. **MUSS** ist verbindlich; **SOLL** ist ein Qualitätsziel; **KANN** ist Erweiterung. Detail- und Prüfkriterien stehen im SRS.

### 4.1 Integration und Bedienung

| ID | Priorität | Anforderung |
|---|---|---|
| SYS-INT-001 | MUSS | Das neue Unterpanel ist über die bestehende LFN-Scout-Navigation erreichbar, ohne eine zweite App zu installieren. |
| SYS-INT-002 | MUSS | Identität, Einstellungen, Bewertungen, Zeitsystem, bekannte Datenflüsse und laufende andere Panels bleiben kompatibel; keine stille Funktionsentfernung. |
| SYS-INT-003 | MUSS | Start/Stopp der **Live-Analyse**, Pausieren/Fortsetzen **nur der Anzeige**, Eingang, Konfiguration, Kalibrierstatus, Mithörstatus und Clipstatus sind verständlich sichtbar; keine Aufnahmeschaltfläche. |
| SYS-INT-004 | MUSS | Alle für einen Versuch relevanten Einstellungen sind persistierbar, exportierbar und auf einen benannten Messprofilstand rückführbar. |
| SYS-INT-005 | SOLL | Einsteiger-Preset „Brummton 0–100 Hz“ und ein optionaler Preset „Technische Geräusche 0–200 Hz“ werden angeboten. |
| SYS-INT-006 | MUSS | Der Nutzer kann **beliebig mehrere benannte Setups** erstellen, sichern, laden (Recall), unter neuem Namen speichern, überschreiben (mit Rückfrage), duplizieren und löschen (mit Rückfrage). Die Anzahl darf nicht durch einen künstlich kleinen festen Grenzwert eingeschränkt sein; praktische Speichergrenzen bleiben möglich. |
| SYS-INT-007 | MUSS | Das zuletzt erfolgreich benutzte Setup wird beim nächsten Öffnen vorausgewählt; sämtliche zugehörigen Analyzer-, Anzeige-, Mithör- und Kalibriereinstellungen werden wiederhergestellt, der Mithörausgang startet **immer AUS**. |
| SYS-INT-008 | MUSS | Setups haben stabile IDs, Schemainformation, Geräte-/Kalibrierbezug und Prüfregeln. Ungültige, fehlende oder inkompatible Setups werden ohne Verlust des zuletzt gültigen Zustands behandelt; Laden während laufender Messung erfolgt kontrolliert. |

### 4.2 Mess- und Darstellungsfunktionen

| ID | Priorität | Anforderung |
|---|---|---|
| SYS-MES-001 | MUSS | Spektrum und Wasserfall werden **laufend und parallel aus dem Live-Eingang** aktualisiert und zeigen den nicht mithörverstärkten Messpfad. |
| SYS-MES-002 | MUSS | Verstellbare FFT-/Decimation-/Fenster-/Intervall-/Glättungsparameter; lineare Achse und fester Standardbereich 0–100 Hz. |
| SYS-MES-003 | MUSS | Optional 0–200 Hz, frei definierbarer Zoombereich, Verschieben, Skalen, Max Hold, Peak Marker und rücksetzbare Messspuren. |
| SYS-MES-004 | MUSS | Korrekte Zeit-/Frequenzzuordnung, dBFS-Kennzeichnung, Gleichanteilunterdrückung und klar markierte Eingangsübersteuerung. |
| SYS-MES-005 | MUSS | PNG-Bilder können ausdrücklich manuell und – wenn `Save aktiv` geschaltet ist – automatisch nach dem ersten vollständigen Wasserfall und anschließend je Wasserfallzeit gesichert werden. CSV bleibt ein manueller Spektral-Snapshot. Keine WAV/PCM- oder kontinuierliche CSV-Audioaufzeichnung. |
| SYS-MES-006 | SOLL | Optionaler Testsignalgenerator und reproduzierbare Selbsttests für Anzeige, FFT, Kalibrierung und Audio-Routing. |

### 4.3 Optionale Mikrofonkalibrierung

| ID | Priorität | Anforderung |
|---|---|---|
| SYS-CAL-001 | MUSS | Der Nutzer kann eine **optionale, gerätespezifische** Kalibrierdatei laden, prüfen, aktivieren und deaktivieren. |
| SYS-CAL-002 | MUSS | Frequenzgangkorrektur und **absolute Pegelreferenz** sind getrennte Sachverhalte: Nur bei verifizierter absoluter Referenz sind dB SPL bzw. dB(Z) zulässig. |
| SYS-CAL-003 | MUSS | Nicht unterstützte Bereiche, fehlende Kalibrierpunkte und Unterschiede zwischen Eingabegerät/Audioquelle/Abtastrate sind erkennbar und werden nicht still extrapoliert. |
| SYS-CAL-004 | MUSS | Mithörlautstärke und Mithörfilter dürfen Kalibrierdaten und unveränderte Messwerte nicht beeinflussen. |
| SYS-CAL-005 | SOLL | Mehrere benannte Kalibrierprofile und Import/Export mit Versions-/Integritätsprüfung. |

### 4.4 Mithören, Bandpass und Pegelschutz

| ID | Priorität | Anforderung |
|---|---|---|
| SYS-MON-001 | MUSS | Eigener, durch Benutzer einschaltbarer Mithörzweig: Eingang → Bandpass → Nutzer-Gain → automatischer Pegelschutz → Audioausgang. |
| SYS-MON-002 | MUSS | Der Mithörbandpass wird **in Spektrum und Wasserfall identisch und dezent** visualisiert; seine beiden Grenzfrequenzen sind direkt im Bild editierbar. |
| SYS-MON-003 | MUSS | Das gesamte Band lässt sich durch Ziehen verschieben (Panning), ohne seine Bandbreite zu ändern; Grenzfrequenzen zusätzlich numerisch editierbar. |
| SYS-MON-004 | MUSS | Gain ist einstellbar; Clipping/Clip-Nähe werden getrennt für Eingang und Mithörausgang erkannt und angezeigt. |
| SYS-MON-005 | MUSS | Die Mithörautomatik reduziert bei drohender Ausgangsübersteuerung selbsttätig den effektiven Gain; Eingangs-Clipping wird **nicht fälschlich als behoben** dargestellt. |
| SYS-MON-006 | MUSS | Bei Start, Stop, Ausgangswechsel, Kopfhörertrennung, Audio-Fokusverlust oder Fehlersituation besteht ein sicherer Stumm-/Abregelpfad. |
| SYS-MON-007 | MUSS | Mithören startet standardmäßig **Aus** und darf nicht unbemerkt den eingebauten Lautsprecher verwenden; die tatsächliche Ausgaberoute ist sichtbar. |
| SYS-MON-008 | SOLL | Ein-/Ausgabe-Latenz und Fehlerzähler sind sichtbar; verdrahtete bzw. USB-Audioausgabe wird unterstützt, soweit vom Gerät angeboten. |
| SYS-MON-009 | MUSS | Das Analyzer-Unterpanel zeichnet weder Eingang noch Mithörausgabe als WAV/PCM-Dateien auf; der Mithörzweig ist ausschließlich eine **Live-Ausgabe**. |
| SYS-MON-010 | MUSS | **Die automatische Abregelung wirkt ausschließlich im Mithörzweig.** Sie darf Live-Eingangs-PCM, Android-Eingangsverstärkung, Messfilter, Analysepegel, Kalibrierung, Spektrum, Wasserfall und PNG-/CSV-Momentaufnahmen weder ändern noch über einen Rückkopplungspfad beeinflussen. |
| SYS-MON-011 | MUSS | Input-Clipping ist eine **unabhängige Warnung** für die Messdatenqualität, keine Regelgröße zur Korrektur der Mikrofonaufnahme. Output-Peak/Clip-Risiko steuert nur den Mithör-Autogain bzw. -Limiter; ein bereits verzerrtes Eingangssignal bleibt als solches gekennzeichnet. |
| SYS-MON-012 | MUSS | Beim Laden/Wechseln eines Setups werden Mithörbandpass, gewünschter Benutzer-Gain und Regelparameter übernommen, nicht jedoch flüchtiger Auto-Absenkungszustand oder eine zuvor aktive Wiedergabe; letzteres erfordert eine neue Nutzeraktion. |

### 4.5 Datenschutz, Robustheit und Tests

| ID | Priorität | Anforderung |
|---|---|---|
| SYS-OPS-001 | MUSS | Die Mikrofonberechtigung wird erklärt; das neue Panel persistiert **kein Live-Audio** und lädt weder flüchtiges PCM noch Kalibrierdateien oder Momentaufnahmen ungefragt hoch. |
| SYS-OPS-002 | MUSS | Bei Ressourcenmangel erhält Echtzeit-Audio Vorrang vor Bildschirm-Neuzeichnung, nicht umgekehrt. |
| SYS-OPS-003 | MUSS | Jede neu eingeführte Funktion besitzt Akzeptanz- und Regressionstests einschließlich Grenz-/Fehlersituationen. |
| SYS-OPS-004 | MUSS | Bestehende Build-/CI-/Selftest-/Changelog-/Projekt-Dokumentationsregeln gelten für das Unterpanel weiter. |
| SYS-OPS-005 | MUSS | Die App meldet aktiv, wenn Messung, Kalibrierung oder Mithören wegen Geräteeinschränkungen nicht belastbar sind. |

## 5. Systemkontext und Datenwege

```text
Android-Mikrofon / USB-Eingang
        │  PCM + Zeitstempel + Geräte-/Routenstatus
        ▼
   Fortlaufende Live-Mikrofonerfassung (flüchtiges PCM)
        ├──► Analysezweig (Rohsignal / DC-Korrektur / FFT / Decimation)
        │       ├──► unkalibrierter dBFS-Datensatz
        │       ├──► optionale Kalibrierkorrektur ► validierter SPL-Datensatz
        │       └──► Live-Spektrum + scrollender Wasserfall + Status
        │              └──► manuelles PNG/CSV; automatisch PNG je Wasserfallzyklus
        │
        └──► Mithörzweig (eigenständig)
                Bandpass ► Benutzer-Gain ► AGC/Clip-Schutz ► AudioTrack
                       ▲                                  │
                       └──── gemeinsames UI f_min/f_max ─┘

LFN-Scout: Navigation / Messprofile / lokal dokumentierte Events / optionale
          projektübliche Schnittstellen (nur nach Kompatibilitätsprüfung)
```

**Wesentliche Invariante:** Das Ändern des Mithörbandpasses, des Nutzer-Gains **oder des automatisch abgesenkten Gains** verändert **keine** bereits berechneten oder künftigen Messspektren, Wasserfallwerte, Kalibrierwerte, flüchtige Eingangs-PCM-Blöcke oder Bewertungsdaten. Die Automatik existiert allein im Pfad zum Mithörausgang; sie greift insbesondere **nicht** auf die Android-Eingangslautstärke oder eine Mikrofon-AGC zu. Optional kann ein späterer, ausdrücklich aktivierbarer **Analysebandpass** unabhängig ergänzt werden; er ist nicht mit dem Mithörbandpass zu verwechseln.

**Clip-Zuordnung:** `INPUT_CLIP` wird *vor* Mithörfilter/Gain/Abregelung protokolliert und weist auf potenziell verzerrte Eingangsdaten hin. Die Mithörautomatik reagiert auf `OUTPUT_CLIP_RISK` bzw. den berechneten Post-Filter-/Post-Gain-Peak; sie kann keinen Eingangsclip rückgängig machen.

## 6. Betriebs- und Störszenarien

1. **Erststart:** Berechtigung erteilen → Eingabegerät wählen → Brummton-Preset → Spektrum/Wasserfall prüfen; Mithören bleibt aus.
2. **Live-Beobachtung:** Band im Plot auswählen und hin-/herschieben → Zielton im kontinuierlichen Wasserfall verfolgen → optional Gain erhöhen → Ausgangs-Clip-Schutz beobachtet den Mithörzweig.
3. **Kalibrierte Messung:** Datei importieren → Profil passt zu Gerät/Quelle/Rate → absolute Referenz vorhanden → Anzeige als dB SPL zulässig, sonst Frequenzkorrektur mit Relativ-/dBFS-Kennzeichnung.
4. **Kopfhörer abziehen:** Mithören sofort stummschalten; keine automatische Ausgabe über Lautsprecher; Status und Hinweis anzeigen.
5. **Input-Clipping:** Eingangsspur und Messdaten kennzeichnen; automatisches Ausgangs-Abregeln bleibt wirksam, darf das Eingangssignal aber nicht als korrigiert ausweisen.
6. **Rechenlast:** Audio-Puffer und Zeitstempel erhalten, Bildrate kontrolliert reduzieren, unvollständige Wasserfallzeilen als Lücken kennzeichnen.
7. **Rückkehr in App:** Zuletzt gültiges benanntes Setup und dessen Konfiguration wiederherstellen; aus Sicherheitsgründen **nicht** automatisch Mithören aktivieren. Ungesicherte Parameteränderungen werden als solche angezeigt.
8. **Messreihe/Lokalisierung:** gleiche Geräte, Profile, Zeitbasis und Kalibrierung; Orts-/Zeitinformation optional durch ausdrücklich aktivierte LFN-Scout-Funktionen.
9. **Setup-Recall:** Setup-Liste öffnen → Namen/Metadaten wählen → „Laden“ → kompatibles Setup aktiviert sich in Spektrum, Wasserfall, Kalibrierzuordnung und Mithörparametern; bei inkompatibler Eingangskonfiguration wird der Live-Stream kontrolliert neu initialisiert oder der Wechsel blockiert. Es entstehen **keine Audiosegmente oder Aufnahmedateien**.
10. **Setup-Verwaltung:** „Speichern“ aktualisiert das aktive Setup nur nach eindeutiger Aktion; „Speichern unter“ legt ein weiteres Setup an. „Löschen“ erfordert Bestätigung. Kein unbemerkter Verlust bisheriger Setups.

## 7. Festlegungen und offene Prüfungen vor Coding

| Entscheidung | Festlegung für Entwurf v0.3 |
|---|---|
| Darstellung | Primär 0–100 Hz **linear**, alternativ 0–200 Hz; weitere Bereiche frei zoombar. |
| Gerät | Android-Smartphone, internes Mikrofon und kompatibler externer Audioeingang. |
| Audioquelle | `UNPROCESSED` wenn gemeldet/erprobt; sonst `VOICE_RECOGNITION` als deklarierter Fallback; Gerätefähigkeit prüfen. |
| Analyse-Preset | 48 kHz / 8192 FFT / 3 Decimations / Blackman-Harris / 100 ms / Smoothing 0,3 **als Startprofil**, keine universell garantierte optimale Einstellung. |
| Bandpass | Voreinstellung 20–80 Hz **Entwurfswert**, Nutzer kann Grenzen einstellen; gültige Werte werden durch tatsächlich verfügbare Audioübertragung und Nyquist bestimmt. |
| Mithören | Standard Aus; nur nach bestätigtem kompatiblem Ausgabegerät und ausdrücklichem Start. |
| Abregelung | **Nur Mithörausgabe**: nur Output-Risiko/Pegel regelt den effektiven Mithör-Gain; Eingangsübersteuerung wird unabhängig markiert. |
| Setups | Mehrere benannte, versionierte Setups mit Save, Save As, Recall, Duplikat, Löschen, Import/Export, „zuletzt verwendet“; Monitor-Aktivstatus niemals wieder einschalten. |
| Kalibrierformat | definiertes JSON-Profil und optional einlesbare CSV-Frequenzpunkte; Details im SRS. |
| Projektintegration | Vor Implementierung Repository-/Dokumentabgleich; vorhandenen Android-Live-Eingang wenn kompatibel **teilen**, keinen konkurrierenden Mikrofonzugriff öffnen; bestehende LFN-Scout-Aufnahmefunktionen weder erweitern noch entfernen. |
| Validierung | Auf realem Smartphone Live-Aktualisierungsrate, Eingangsfrequenzgang, Analyseauflösung, Filterstabilität, Mithörlatenz und Verzicht auf neue Audio-Dateien prüfen. |

### 7.1 Aufruf und Persistenz mehrerer Setups

- **Setup-Auswahl:** Dropdown/Liste im Analyzer-Unterpanel mit dem Namen des aktiven Setups, Such-/Auswahlmöglichkeit, „Neu“, „Laden/Recall“, „Speichern“, „Speichern unter“, „Duplizieren“, „Löschen“ sowie optionalem Import/Export. Der aktuelle Zustand „ungespeicherte Änderungen“ ist erkennbar.
- **Umfang:** Audio-Eingang/Quelle/Abtastrate; FFT/Decimation/Fenster/Intervall/Glättung; Spektrums-/Wasserfall-Skalen, Zoom und Hold; Kalibrier-ID/Aktivstatus; Mithörband/Gain/Abregeloption und gewünschte Kopfhörerroute; PNG-Autosave- und manuellen PNG-/CSV-Exportpräferenzen. **Nicht im Setup:** Audio-Rohdaten, flüchtige Live-Puffer, laufende Auto-Gain-Absenkung oder automatische Wiedergabeaktivierung.
- **Robustheit:** stabile Setup-ID unabhängig vom Anzeigenamen, versioniertes Dateischema und Migration, lokale atomare Speicherung, bestätigungspflichtiges Überschreiben/Löschen und validierter Import. Fehlendes Mikrofon oder Kalibrierprofil wird angezeigt; es gibt keinen stillen Geräteaustausch bei als vergleichbar deklarierten Messreihen.
- **Startverhalten:** letztes gültiges Setup öffnen; wenn ungültig/gelöscht, erkennbarer Fallback auf definiertes Standardsetup; Mithören bleibt AUS.

## 8. Traceability und Freigabe

**Zusätzliche Abnahmebedingung:** Der Live-Analyzer funktioniert ohne Speicherung von PCM/WAV; Test T-23 prüft auch bei langem Live-Betrieb, dass keine neue Audio-Datei angelegt wird. Die Wasserfallhistorie ist ein begrenzter RAM-Ringpuffer: ein bloßer Panelwechsel darf ihn bei aktiver Analyzer-Sitzung nicht löschen; erst die ausdrückliche Beendigung der Sitzung bzw. ein App-Prozessneustart kann ihn zurücksetzen, soweit er nicht als PNG exportiert wurde.

Die System-IDs verweisen auf die Detail-IDs des SRS. Freigabe erst, wenn alle als **MUSS** markierten Anforderungen durch Tests mit synthetischen Signalen, mindestens einem realen Android-Gerät und mindestens einem Audio-Routenwechsel nachgewiesen sind. Die Dokumentversion 0.3 bezeichnet **einen Spezifikationsentwurf**, keine implementierte App-Version.

## 8.1 Änderungsstand v0.3 gegenüber v0.2

- Kernfunktion **kontinuierliche Live-Analyse wie Spectroid**, nicht Audio-Recording. Begriffe „Aufnahme“ in Bedienung und Betrieb durch „Live-Erfassung/-Analyse“ präzisiert.
- WAV-/PCM-Dateiaufzeichnung und Dauer-Logging explizit aus dem neuen Unterpanel ausgeschlossen; **manuelles PNG/CSV sowie aktivierbarer automatischer PNG-Zyklus** und Setup-Speicherung bleiben erhalten.
- Mithörzweig einschließlich Auto-Abregelung und Clip-Schutz bleibt vollständig vom Live-Messzweig getrennt.
- Alle weiteren bestehenden Anforderungen zu FFT, Waterfall, Kalibrierung, Bandgesten, Mithören und mehreren Setups bleiben erhalten.

## 9. Primärquellen für technische Plattformgrenzen

- Android: [Audioquelle `UNPROCESSED` und Fallback](https://developer.android.com/media/platform/mediarecorder) (Unterstützung geräteabhängig).
- Android: [`MediaRecorder.AudioSource`](https://developer.android.com/reference/android/media/MediaRecorder.AudioSource).
- Android: [`AudioRecord`](https://developer.android.com/reference/android/media/AudioRecord) (Eingangsformat, Route, Buffer).
- Android: [`AudioTrack`](https://developer.android.com/reference/android/media/AudioTrack) (Ausgaberoute, Buffer, Performance).
- Android: [Foreground-Service-Typ `microphone`](https://developer.android.com/develop/background-work/services/fgs/service-types) (falls Hintergrundbetrieb implementiert wird).
- Nutzerseitige Funktionsreferenz: `references/Spectroid_Anleitung_v10_5.docx` (lokale Arbeitsgrundlage).


## 10. Konsolidierte Systemergänzungen v0.6 (ersetzt entgegenstehende ältere Bild-/UI-Aussagen)

| ID | P | Verbindliche Systemanforderung |
|---|---|---|
| SYS-UX-001 | MUSS | Hauptansicht im Portrait zeigt **immer Spektrum und Wasserfall**; keine permanente Vielzahl von Einstellwerkzeugen. Separate schmale, einspaltige und scrollbare Einstellansicht. |
| SYS-UX-002 | MUSS | Kurzer Tap auf freien Wasserfall blendet **nur untere Zusatzkarten** aus bzw. wieder ein; Spektrum, Wasserfall, Pause, Status und Einstellzugang bleiben sichtbar. |
| SYS-UX-003 | MUSS | Spektrums-Fadenkreuz besteht aus X- und Y-Linie, Schnittpunkt und Frequenz-/Pegel-Readout; im angehaltenen **Anzeigebild** weiterhin beweglich. |
| SYS-UX-004 | MUSS | Live-Kurve gelb, unabhängig zuschaltbarer Average rot, gespeicherte Rauschbodenreferenz optional grau (initial AUS). |
| SYS-UX-005 | MUSS | Standard linear 0–100 Hz; optionale 0–200 Hz sowie numerische Frequenz- und Pegel-Min-/Max-Skalen. Wasserfall enthält `Inferno`. |
| SYS-UX-006 | MUSS | Mithörfeld unten links: Tippen EIN/AUS, vertikales Wischen ändert ausschließlich Benutzer-Gain; numerische Bandbreite in Hz im Spektrum-Setup, verschiebbare Bandmitte in Spektrum **und** Wasserfall. |
| SYS-UX-007 | MUSS | `MONITOR_CLIP`/Auto-Absenkung und getrennt `INPUT_CLIP` sind im Hauptbild sichtbar. Ausgangs-Auto-Absenkung ist ausschließlich im Mithörzweig. |
| SYS-STAT-001 | MUSS | Unabhängiger Bandpegel L50/L90, initial 2–25 Hz über rollende 120 s; kurze horizontale Linien nur im betroffenen Frequenzabschnitt und numerische Werte, separat abschaltbar. |
| SYS-STAT-002 | MUSS | `Superfast` liest echte Eingangssample-Spitzen unabhängig vom normalen Renderintervall; `Maximum Sample` besitzt Zeitstempel und Reset und beansprucht keine ungeprüfte True-Peak-/SPL-Gültigkeit. |
| SYS-NOISE-001 | MUSS | Rauschboden-Referenz messen, benannt speichern, laden; graue Kurve und lineare Leistungskompensation unabhängig schalten; nur Anzeigezweig, nicht Rohmessung/Statistik/Mithören. |
| SYS-SAVE-001 | MUSS | `Save aktiv` ist ausschließlich die optionale **PNG-Bildautomatik**: erste Speicherung nach erstmalig komplett gefülltem Wasserfall, danach genau einmal pro voller Wasserfallzeit. Keine Audioaufnahme. |
| SYS-SET-001 | MUSS | Alle 146 Parameter-IDs der `references/Funktionsreferenz_v0.4.md` bleiben im Datenmodell und Testnachweis auffindbar. Feste Kern-/Anzeigeparameter sind ggf. read-only. Mehrere Setups/Recall; Mithören beginnt nach Recall immer AUS. |

### Verbindlichkeitsregel

Diese Version ist fachlich konsolidiert. Die separate Parameterreferenz v0.4 gehört als vollständiger Bestandteil zum Paket. Bei Konflikt innerhalb älterer historischer Textstellen gelten die explizit vorgenannten v0.6-Anforderungen und das v0.6-SRS; historische v0.3-Dateien im `references/legacy/` sind ausschließlich Archiv. Alle Zahlen ohne Nutzerfestlegung bleiben als **Vorschlagswerte** bis zur Gerätevalidierung markiert. Kein Quellcode wurde geprüft oder verändert.


## 11. Zusätzliche Systemanforderungen v0.6 – panelunabhängiger Livebetrieb und Bestandsintegration

**Verbindlichkeit:** Die folgenden Anforderungen ergänzen die vollständig erhaltenen Kapitel 1–10 und ersetzen bei Widerspruch ältere Annahmen über panelgebundenen Capture, PNGs nur aus dem sichtbaren View oder völlig getrennte Mikrofon-Clients. „Hinter einem anderen Panel“ meint zunächst dieselbe **im Vordergrund geöffnete** App, nicht automatisch Bildschirm-aus/App-im-Hintergrund.

| ID | Prio | Systemanforderung | Abnahme |
|---|---|---|---|
| SYS-AUDSH-001 | MUSS | Analyzer und die **bestehende** Audioaufnahme innerhalb LFN-Scout nutzen für dasselbe physische Eingangssignal **eine koordinierte Capture-Instanz / einen gemeinsamen PCM-Verteiler**. Keine konkurrierenden unkoordinierten Android-Mikrofon-Captures. | T-44, T-45 |
| SYS-AUDSH-002 | MUSS | Der bestehende Recorder erhält unveränderte, zeitlich korrekt zugeordnete PCM-Daten; Filter/Gain/AGC/Rauschbodenabzug des Analyzers beeinflussen dessen Daten und Format nicht. | T-45, T-46 |
| SYS-AUDSH-003 | MUSS | Parallelbetrieb Analyzer + Recorder ist ein überprüfter Betriebsfall. Konfigurationskonflikte führen zu einer erklärten Wahl oder kontrolliertem Fallback statt stillen Änderungen oder verdeckten Lücken. | T-47, T-48 |
| SYS-LIFE-001 | MUSS | Bei Wechsel zu einem anderen **LFN-Scout-Panel** läuft eine ausdrücklich aktive Analyzer-Sitzung weiter; insbesondere bei `Save aktiv` laufen FFT, Wasserfallhistorie, L50/L90, Superfast und Speicherzeitsteuerung weiter. | T-49, T-50 |
| SYS-LIFE-002 | MUSS | Ein Panelwechsel darf weder Analyzer-Pause noch Save-Abschaltung auslösen. Rechen- und Renderlebenszyklus sind voneinander getrennt; unsichtbare UI wird nicht fortlaufend neu gezeichnet. | T-49, T-51 |
| SYS-LIFE-003 | MUSS | App-Verlassen/Bildschirm-aus sind getrennt zu spezifizieren, abhängig von aktueller Android-Ziel-API, Berechtigungen und Dienstmodell; ohne freigegebene Hintergrundfunktion keine unbemerkte Garantie. | T-52 |
| SYS-PNG-001 | MUSS | Automatische PNGs entstehen aus einem **panelunabhängigen Daten-Snapshot und Offscreen-Renderer**, nicht aus dem gerade sichtbaren Activity/View. | T-50, T-53 |
| SYS-PNG-002 | MUSS | Erstes PNG nach vollständiger Wasserfallzeit ab Aktivierung/Neufüllung, danach je neuer voller Wasserfallzeit; Dateifehler/Drops sichtbar, Mikrofonerfassung ungestört. | T-39, T-50, T-54 |
| SYS-HIST-001 | MUSS | Der Analyzer kann die letzte oder die letzten vier **vorhandenen Radiushistogramme** als Miniaturen **untereinander rechts neben dem Wasserfall** darstellen; AUS/1/4 auswählbar. | T-55, T-56 |
| SYS-HIST-002 | MUSS | Beide Panels beziehen Histogrammdaten aus einer strukturierten Quelle, nicht aus dem gerenderten Bild oder dem aktuellen Sichtbarkeitszustand des Histogramm-Panels. | T-55, T-57 |
| SYS-HIST-003 | MUSS | Zeitstempel, Einheit, Datenquelle, Veraltet-/Fehlt-Status und Zuordnung zur jeweiligen PNG-Ausgabe bleiben erkennbar; ohne verfügbares Histogramm keine erfundenen Werte. | T-56, T-58 |
| SYS-PROT-001 | MUSS | Eine eigenständig startbare **Testhülle** darf denselben Analyzer-Kern prüfen; sie ist kein gesondertes Produktionsprodukt und darf keine eigenständige konkurrierende Audioarchitektur etablieren. | T-59 |

**Produktabgrenzung:** Im Analyzer wird weiterhin kein Audio gespeichert. Die bereits bestehende Audioaufnahme in LFN-Scout ist eine *separate* Bestandsfunktion und darf gleichzeitig einen gemeinsamen PCM-Stream konsumieren. `Save aktiv` bleibt ausschließlich Bildspeicherung.

## 12. Abgeschlossene Übergabe und Quellenrang v0.7

**SYS-HND-001 (FIX)**: Ein einziges in sich geschlossenes Übergabepaket MUSS fachliche Anforderungen, Algorithmen, UI-/Bildstatus, Integrations-, Test- und Qualitätsnachweise gemeinsam enthalten. Fachlich gilt dieses Paket für das neue Analyzer-Unterpanel; es ersetzt **keine** bereits vorhandenen, noch nicht geprüften Gesamtprojekt-Anforderungen.

**SYS-HND-002 (FIX)**: Für die spätere UI-Implementierung gilt zuerst die textuelle UI-Spezifikation dieses Pakets und danach der Leitbild-Asset `C13`; ältere Entwürfe sind als Referenz oder historisch markiert. Ein Bildtext darf eine ausdrückliche Anforderung nicht überschreiben.

**SYS-HND-003 (FIX)**: Die Funktion muss den IST-Audio- und Histogrammzugang des laufenden LFN-Scout-Kerns berücksichtigen; ein vollständig isolierter und später umzubauender Standalone-Clone ist **nicht** vorgesehen.

**SYS-HND-004 (FIX)**: Die 146 ursprünglichen Parameter plus alle 23 v0.6-Ergänzungen werden unverändert übernommen. Ergänzungen in v0.7 dienen ausschließlich zur Schließung von Gaps; stille Entfernung oder Umdeutung ist nicht zulässig.




---

# QUELLDOKUMENT: `docs/project/02_SRS_LFN-Scout_Analyzer_v0.7.md`

# SRS – LFN-Scout: Spektrum, Wasserfall, Kalibrierung und Mithören

**Dokument-ID:** LFN-AN-SRS · **Version:** 0.7 (konsolidierter Entwurf) · **Stand:** 19.09.2026  
**Dokumentart:** Software Requirements Specification (detaillierte, prüfbare Softwareanforderungen)  
**Übergeordnet:** `01_SRD_LFN-Scout_Analyzer_v0.7.md` · **Implementierungsstand:** nicht implementiert/nicht getestet.  
**Verbindliche Lesart:** Jede Kennung bleibt über spätere Versionen stabil; `MUSS` = Abnahmekriterium, `SOLL` = Qualitätsziel, `KANN` = spätere Erweiterung.

## 1. Zweck, Ausgangslage, Abgrenzung

Dieses Dokument spezifiziert ein **eigenständiges, kontinuierlich arbeitendes Spectroid-ähnliches Echtzeit-Analyzer-Unterpanel „Spektrum & Mithören“ in LFN-Scout**. Sein Zweck ist die fortlaufende Anzeige von Live-Spektrum und scrollendem Wasserfall, nicht die Aufzeichnung oder Wiedergabe von Audio-Dateien. Die funktionale Grundlage ist die Spectroid-Anleitung Version 10.5 als Arbeitsreferenz sowie dessen Ergänzungen zur Kalibrierdatei und zum Mithören. Die Anleitung beschreibt einen Standardbereich 0–100 Hz linear, optional 0–200 Hz, FFT, Decimations, Blackman-Harris, Zeitauflösung/Glättung, Max Hold, Peak Marker, Subtract DC, Wasserfall und dBFS. Die Variante 48 kHz / 8192 bins / 3 Decimations ist **ein Startprofil für das konkret gezeigte Gerät, kein für jedes Smartphone optimaler Garantiewert**. Die Anleitung enthält an einigen Stellen mechanisch fortgeschriebene, widersprüchliche Alternativangaben zur FFT-Größe; als fachlich konsistenter Entwurfswert gilt hier **8192, Fallback 4096**, mit Anzeige der tatsächlich erreichten Δf. Der Satz `UNPROCESSED` bedeutet nicht, dass die gesamte Hardware garantiert bis 0 Hz linear arbeitet.

Das neue Modul wird in den tatsächlich aktuellen LFN-Scout-Code integriert. **Vor dem Coding** sind bestehender Audio-Capture/Buffer, vorhandene Android-UI-Navigation, Mess-/Bewertungsdaten, Kalibrier-/Setupmodelle, Build-, CI-, Tests und `docs/project/` zu prüfen. Der aktuelle App-Quellcode liegt diesem Paket nicht zugrunde; Versions- und Klassenangaben sind vor Umsetzung im Repository festzustellen. Sichtbarer App-Name **LFN-Scout**; interne Android-ID **`de.brummbeobachter.app`** bleibt unverändert. Die alte App und ihre Daten dürfen nicht durch eine neue Package-ID getrennt werden.

**Kein Bestandteil dieser Abnahme:** Audioaufzeichnung/WAV-Export, dauerhafte PCM-Speicherung, automatisiertes Dauer-Logging oder Replay; medizinische Diagnostik, präzise Infraschall-Schallpegelmessung ohne validierte Hardware/Referenz, bestimmtes Quellobjekt automatisch bestimmen, hörbare Wiedergabe von Frequenzen unterhalb der Kopfhörer-/Hörgrenze garantieren, bereits übersteuerte Mikrofonwerte „heilen“.

## 2. Daten- und Anzeigeprinzipien

1. **Live-Eingang ist Single Source of Truth:** Ein kontinuierlich gelesener, **flüchtiger** PCM-Stream mit Geräte-ID, Quelle, Rate und Zeitstempel speist beide Zweige. Androids Klassenname `AudioRecord` bezeichnet hier nur den Mikrofon-Live-Eingang und verpflichtet **nicht** zum Schreiben einer Aufnahme-Datei. Eine vorhandene LFN-Scout-Eingangskette darf nur dann geteilt werden, wenn ihr Verhalten für den Analyzer kompatibel ist; keine konkurrierende zweite Mikrofoninstanz.
2. **Live-Messpfad:** `flüchtiges PCM → optional DC-Korrektur → Analysefilter/Decimation → Window/FFT → dBFS/optional Kalibrierung → laufende Spektrum-/Wasserfallanzeige`. PNG bei ausdrücklicher Nutzeraktion **oder** bei aktivierter automatischer Bildspeicherung nach vollständigem Wasserfallzyklus; CSV **nur** als manueller aktueller Spektralstand. Nur eindeutig dokumentierte Messfilter sind erlaubt.
3. **Mithörpfad:** `PCM → Mithör-Bandpass → Benutzer-Gain → Clip-Vorbeugung/Abregelung → Ausgabe`; **kein Rückpfad in den Messzweig**.
4. **Beide Darstellungen** nutzen dieselbe Bandpass-Konfiguration, zeigen sie aber als **Overlay**, ohne die zugrunde liegenden Messwerte zu beschneiden.
5. **Zwei verschiedene Clipping-Ereignisse:** `INPUT_CLIP` (Mikrofon/A-D) und `OUTPUT_CLIP_RISK/OUTPUT_CLIP` (Mithörsignal/AudioTrack). Die **automatische Abregelung liegt ausschließlich im Mithörzweig** und reagiert auf dessen Ausgangsrisiko. `INPUT_CLIP` kennzeichnet unabhängig die Messdaten; ein vorher übersteuertes Signal bleibt verzerrt.
6. **Relative und absolute Pegel:** dBFS bleibt immer verfügbar. Frequenzkorrektur allein ergibt **keine** absolute SPL-Angabe. dB SPL/dB(Z) nur für geeignete Referenz, Gerät und gültige Frequenzen.

## 3. Bedienoberfläche und Zustandsmodell

### 3.1 Verbindliche Portrait-Anordnung (kein exaktes Pixel-Layout)

```text
┌──────────────────────────────────────────┐
│ LFN-Scout / Analyzer     [Pause] [⚙]     │
│ Status: Input / Monitor-Clip / Save      │
├──────────────────────────────────────────┤
│ SPEKTRUM: Gelb Live / Rot Average       │
│   + ggf. graue Rauschbodenreferenz       │
│   + Fadenkreuz (Hz und Pegel)            │
│   + kurze L50/L90-Markierungen (2–25 Hz)│
│   + dezentes Mithörband-Overlay          │
├──────────────────────────────────────────┤
│ WASSERFALL: Inferno, Band-Overlay        │
│   Tap ⇄ nur untere Zusatzkarten weg     │
│         (Spektrum bleibt sichtbar)       │
├──────────────────────────────────────────┤
│ [Mithören AUS/EIN + Gain-Wisch]          │
│ [L50/L90] [Superfast/Maximum Sample]     │
└──────────────────────────────────────────┘
```

Die Darstellung muss im Hochformat funktionieren. Bandbreite in Hz und andere Parameter stehen **im separaten scrollbaren Einstellpanel**. Nur Pause und Einstieg in Einstellungen sind ständig als Schaltflächen sichtbar. Berührungsprioritäten und Zustände sind in `04_UI_Gesten_v0.7.md` normiert. Die im Paket beigefügten AI-Entwurfsbilder sind **nicht pixelverbindlich**.

### 3.2 Zustände

`LIVE_OFF → LIVE_RUNNING → DISPLAY_FROZEN → LIVE_RUNNING → LIVE_OFF`. Die Anzeige-Pause friert **nur die Darstellung**; der flüchtige Mikrofon-Stream, die Analyse und gegebenenfalls Mithörsicherheit arbeiten weiter. `LIVE_OFF` stoppt nur die Analyzer-Nutzung des Eingangs unter Berücksichtigung bestehender LFN-Scout-Nutzer. Mithören besitzt einen **orthogonalen** Zustand: `MONITOR_OFF`, `MONITOR_ARMED`, `MONITOR_ACTIVE`, `MONITOR_MUTED`, `MONITOR_ERROR`. Es gibt keinen Recording-/Replay-/Aufnahmepause-Zustand.

## 4. Detaillierte funktionale Anforderungen

Jede Tabelle enthält **ID · Priorität · prüfbares Sollverhalten**. Die Akzeptanztests am Ende referenzieren die IDs.

### 4.1 Integration, Rechte, Persistenz

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-INT-001 | MUSS | Ein LFN-Scout-Navigationspunkt öffnet das Analyzer-Unterpanel; Zurück/Panelwechsel ohne App-Absturz und ohne Änderung bestehender Bewertungen. |
| AN-INT-002 | MUSS | Der Analyzer nutzt, wo technisch kompatibel, den vorhandenen **Live-Mikrofonstream** mit begrenztem RAM-Ringpuffer; ein zweiter konkurrierender Mikrofonzugriff ist verboten. Ein neuer Audio-Dateirecorder ist **nicht** Teil dieses Unterpanels. |
| AN-INT-003 | MUSS | Die interne Android-ID `de.brummbeobachter.app` wird nicht geändert; bestehende Daten können nach einem Update weiterhin geöffnet werden. |
| AN-INT-004 | MUSS | **Mehrere benannte, versionierte Setups** können erstellt, gespeichert, unter neuem Namen gespeichert, per **Recall** geladen, dupliziert und gelöscht werden; das zuletzt **erfolgreich** benutzte Setup wird beim nächsten Start vorausgewählt. Details und Tests: AN-SET-001–019. |
| AN-INT-005 | MUSS | Setup enthält Zoom, Wasserfall, Mikrofon/Audioquelle, Kalibrier-ID, Filtergrenzen und Benutzer-Gain sowie alle prüfrelevanten Analyseparameter; **Mithören bleibt nach Laden/Neustart AUS**, auch wenn es zuletzt EIN war. Flüchtiger Abregelwert wird nicht persistiert. |
| AN-INT-006 | MUSS | Änderungen an Eingang oder Rate im Live-Betrieb werden nach Warnung kontrolliert neu initialisiert oder bis Live-Stopp blockiert. Keine stille Mischung verschiedener Eingangskonfigurationen in der Anzeige; es werden **keine Aufnahmesegmente** erzeugt. |
| AN-INT-007 | MUSS | Androids Mikrofonberechtigung `RECORD_AUDIO` wird nur bei Bedarf angefragt; sie ist für den flüchtigen Live-Eingang notwendig und bedeutet **keine Datei-Aufzeichnung**. Ablehnung führt zu eindeutiger Erklärung und stabiler UI. |
| AN-INT-008 | MUSS | Das Unterpanel erzeugt keine PCM-/WAV-Audiodateien und lädt weder flüchtige Live-Samples noch Kalibrierprofile oder PNG-/CSV-Momentaufnahmen ungefragt hoch; bestehende andere LFN-Scout-Datenflüsse bleiben unverändert. |
| AN-INT-009 | MUSS | Fehler beim Laden eines Profils/der Kalibrierung führen zu sauberem Fallback und sichtbarer Warnung; zuletzt gültige Konfiguration bleibt erhalten. |
| AN-INT-010 | SOLL | Profilein-/ausfuhr über Android Storage Access Framework, ohne allgemeine Vollzugriffsrechte auf das Dateisystem. |

### 4.1.1 Mehrere Setups: Speichern, Laden (Recall) und Verwaltung

Die Funktion ist Teil desselben LFN-Scout-Unterpanels, nicht ein zweites, konkurrierendes Konfigurationssystem. Bereits vorhandene projektweite Setup-Routinen sind nach Quellcodeprüfung kompatibel wiederzuverwenden. Ein **Setup** ist die gespeicherte Konfiguration; Ein **Live-Lauf** ist die aktuelle flüchtige Analysesitzung, keine Audio-Dateiaufnahme. Änderungen im Setup dürfen früher exportierte PNG-/CSV-Momentaufnahmen und deren Metadaten nicht nachträglich umschreiben.

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-SET-001 | MUSS | Ein sichtbares Setup-Dropdown bzw. eine Auswahlliste zeigt alle gespeicherten Setups; das aktive Setup trägt eine eindeutige Kennzeichnung. Ein Eintrag lässt sich gezielt wählen und über **„Laden/Recall“** übernehmen. |
| AN-SET-002 | MUSS | „Neu“ erzeugt einen benannten Setup-Entwurf auf Basis eines auswählbaren Default- oder aktuellen Setups; „Speichern unter“ legt ein **weiteres** Setup an, ohne das bisherige zu ersetzen. |
| AN-SET-003 | MUSS | „Speichern“ schreibt explizit den aktuellen Zustand in das ausgewählte Setup; beim Ersetzen bereits gespeicherter Werte ist ein sichtbarer, nachvollziehbarer Überschreibvorgang erforderlich. „Duplizieren“ erzeugt eine neue, unabhängige Setup-ID. |
| AN-SET-004 | MUSS | „Löschen“ ist nur nach bestätigter Auswahl möglich und löscht ausschließlich das gewählte Setup; bei Löschung des aktiven Setups wird ein gültiges Default-/Ersatzsetup aktiviert. |
| AN-SET-005 | MUSS | Die Setup-Zahl ist nicht auf eine kleine feste Anzahl begrenzt. Anzeigename ist nicht die technische Identität: Jedes Setup besitzt eine unveränderliche `setupId`, `schemaVersion`, Zeitstempel und einen `revision`-Stand; doppelte Namen werden eindeutig behandelt. |
| AN-SET-006 | MUSS | Zu speichern sind: Eingangsgerät, Audioquelle, Rate, FFT, Decimation, Fenster, Transform-Intervall, Glättung, DC-Option, Spektrums-/Wasserfall-Grenzen, Achsenmodus, Palette, Historie, Peak-/Hold-Einstellungen, Kalibrier-ID/-Aktivierung, Mithörband, Nutzer-Gain, Auto-Abregel-Konfiguration, bevorzugte Ausgaberoute und Exportoptionen. |
| AN-SET-007 | MUSS | **Nicht** in ein Setup übernommen werden: Roh-PCM, Ringpuffer, bereits erzeugte Messdaten, flüchtiger `autoAttenuationDb`-Reglerzustand, aktuelle Clipzähler sowie „Mithören läuft“. Beim Recall gilt zwingend `monitorActive=false`, bis der Nutzer ausdrücklich startet. |
| AN-SET-008 | MUSS | Das zuletzt erfolgreich geladene oder gespeicherte Setup wird als `lastUsedSetupId` gesichert und bei erneutem Öffnen geladen. Existiert es nicht mehr oder ist es unbrauchbar, erfolgt ein gekennzeichneter Fallback auf das Default-Setup. |
| AN-SET-009 | MUSS | Eine Änderung seit dem letzten Speichern markiert das Setup sichtbar als „geändert“; beim Laden eines anderen Setups bzw. Schließen werden Speichern/Verwerfen/Abbrechen angeboten. Kein unbemerkter Datenverlust. |
| AN-SET-010 | MUSS | JSON-Setups sind schema-validiert, mit expliziten Defaults und dokumentierter Migration. Ein unbekanntes neueres Schema wird nicht still fehlinterpretiert oder überschrieben; Fehlercode und Warnung erscheinen. |
| AN-SET-011 | MUSS | Persistenz ist atomar (z. B. temporäre Datei + sichere Ersetzung); Absturz, voller Speicher oder abgebrochener Schreibvorgang darf ein vorher gültiges Setup nicht zerstören. |
| AN-SET-012 | MUSS | Beim Laden werden Parameterbereiche sowie referenzierte Audio-/Kalibrierressourcen geprüft. Bei fehlendem Eingang oder unpassender Kalibrierung erfolgen Warnung und ausdrückliche Neuauswahl; ein unbekannter oder fehlender Kalibrierverweis schaltet kalibrierten SPL-Modus sicher ab. |
| AN-SET-013 | MUSS | Ein Setup-Wechsel während laufender Live-Analyse mit Eingangs-/Ratenänderung wird bis zum Live-Stopp verweigert oder vollzieht nach sichtbarer Warnung eine kontrollierte Stream-Neuinitialisierung. Ein reiner Anzeige- oder Mithörparameterwechsel darf den Live-Messpfad nicht rückwirkend ändern. |
| AN-SET-014 | MUSS | Import und Export einzelner oder mehrerer Setups erfolgen lokal über Android Storage Access Framework, ohne allgemeine Speichervollrechte; importierte IDs/Namen werden geprüft und Konflikte dem Nutzer angezeigt. |
| AN-SET-015 | MUSS | Setup-Liste und `lastUsedSetupId` müssen auch nach App-/Prozessneustart korrekt sein; Speichern und Recall bleiben offline nutzbar und sind unabhängig von Supabase. |
| AN-SET-016 | MUSS | Jeder explizite PNG-/CSV-Snapshot hält `setupId`, `revision` und einen unveränderlichen Schnappschuss der **tatsächlich aktiven** Parameter fest; spätere Setup-Änderungen verfälschen exportierte Momentaufnahmen nicht. |
| AN-SET-017 | MUSS | Beim Laden eines Setups werden gespeicherte Mithörbandgrenzen, Nutzer-Gain und Reglerkonfiguration übernommen, aber tatsächlicher Ausgabepegel, Auto-Absenkungszustand und Output-Clipstatus initialisiert und Mithören stumm/AUS gehalten. |
| AN-SET-018 | MUSS | Bereitgestellte Startsetups „Brummton 0–100 Hz“ und „Technische Geräusche 0–200 Hz“ lassen sich abrufen; werkseitige Vorlagen bleiben unveränderbar, Anpassungen werden als benutzerdefinierte Kopien gespeichert. |
| AN-SET-019 | SOLL | Setup-Auswahl zeigt Name, Änderungsdatum, Kurzbeschreibung und Eignung für gewählten Eingang/Kalibrierung; Such- und Sortierfunktion erleichtert größere Setup-Bestände. |

**Setup-Bedienfolge:** `Setup wählen → Recall → Validierung → ggf. Warnung/Live-Stream-Neuinitialisierung → Aktivierung → lastUsedSetupId speichern`. `Speichern unter` erzeugt dagegen immer eine neue ID. Das Laden darf **nicht** still eine Ausgabe aktivieren oder eine alte automatische Mithörabsenkung reaktivieren.

### 4.2 Audioeingang und Routing

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-IN-001 | MUSS | Erkennung nutzbarer Audio-Eingabegeräte einschließlich internem Mikrofon und kompatibler USB-Audioeingänge, sofern angeschlossen. |
| AN-IN-002 | MUSS | `UNPROCESSED` wird bevorzugt, **wenn das Gerät die Unterstützung meldet und der Eingang initialisiert werden kann**; andernfalls `VOICE_RECOGNITION` als sichtbar bezeichneten Fallback anbieten. Andere Quellen nur bewusst auswählbar. |
| AN-IN-003 | MUSS | Gewünschte und tatsächlich aktive Audioquelle, physischer Eingang, Kanalzahl, Sampling Rate und PCM-Format werden angezeigt/gespeichert. |
| AN-IN-004 | MUSS | Standard-**Live-Eingang** 48 000 Hz mono, bevorzugt PCM_FLOAT, andernfalls PCM_16BIT; tatsächliche Geräteunterstützung prüfen statt Annahmen. Es werden keine PCM-Dateien geschrieben. |
| AN-IN-005 | MUSS | Nutzer kann mindestens 44,1/48 kHz wählen, **soweit real unterstützt**; ungültige Kombination wird abgewiesen und protokolliert. |
| AN-IN-006 | MUSS | Zeitstempel haben eine monotone interne Basis, die für Archiv/Export eindeutig UTC zugeordnet wird; Pufferüberläufe erzeugen markierte Datenlücken. |
| AN-IN-007 | MUSS | Bei Mikrofontrennung, fehlender Berechtigung, unterbrochener **Live-Erfassung** und Routenwechsel sicher stoppen oder kontrolliert rekonfigurieren, ohne falsche Anzeigekontinuität zu suggerieren. |
| AN-IN-008 | MUSS | Rohdaten-Clip-Erkennung vor aller benutzerseitigen Verstärkung; Probe nahe Full Scale/PCM-Sättigung zählt als Ereignis mit Zeitstempel und Dauer. |
| AN-IN-009 | SOLL | Einfache Geräte-Diagnoseseite mit tatsächlicher Rate, Signal-RMS/Peak, erkannter Latenz, Pufferaussetzern und `UNPROCESSED`-Support. |

### 4.3 Frequenzanalyse und Signalverarbeitung

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-DSP-001 | MUSS | FFT-Größen mindestens 2048/4096/8192/16384, soweit Leistungsbudget zulässt; Startprofil 8192, Fallback 4096. Jede Änderung aktualisiert die angezeigte reale Frequenzauflösung. |
| AN-DSP-002 | MUSS | Decimations mindestens 0–5 oder geräteabhängige sichere Teilmenge, mit geeignetem Anti-Aliasing vor jeder Reduktion; nicht unterstützte Kombinationen blockieren. |
| AN-DSP-003 | MUSS | Effektive Analyserate, FFT-Länge, `Δf = f_eff/N` und tatsächlich abgedeckter Nyquist-Bereich werden errechnet und angezeigt. **Die genaue Decimation-Definition wird an der implementierten Kette validiert.** |
| AN-DSP-004 | MUSS | Wählbare Fensterfunktion mindestens Blackman-Harris (Preset), Hann und Rechteck; korrekte Fenster-Normierung für angezeigte Einzeltöne bzw. integrierte Pegel. |
| AN-DSP-005 | MUSS | Desired Transform Interval einstellbar mit 50/100/250/500 ms und nutzerdefiniert im physikalisch möglichen Bereich; falls Analysefenster länger ist, wird ein gleitendes Fenster genutzt oder das tatsächliche Intervall kenntlich gemacht. |
| AN-DSP-006 | MUSS | Exponential Smoothing einstellbar 0–1, Preset 0,3; mathematische Definition und Zeitverhalten dokumentieren, `0` und `1` konsistent behandeln. |
| AN-DSP-007 | MUSS | `Subtract DC` getrennt Ein/Aus; entfernt numerischen Gleichanteil, aber ist keine Behauptung einer bis 0 Hz messbaren Mikrofonkette. |
| AN-DSP-008 | MUSS | Spektrum beruht auf **Messpfad vor Mithörfilter und Mithör-Gain**; Test mit Gain/Bandänderungen muss binweise gleiche Messpegel ergeben (innerhalb deterministischer Toleranz). |
| AN-DSP-009 | MUSS | FFT-Bins, dBFS-Umrechnung und Wasserfall nutzen denselben dokumentierten Pegelbezug. `−∞` bei Nulleingang wird per definierter Display-Untergrenze, nicht als realer Messwert, dargestellt. |
| AN-DSP-010 | MUSS | Anzeige von mehr als einer Auflösungsstufe ist möglich, ohne Aliasing in tieferfrequenten Analysefenstern zu erzeugen; 0–100 Hz behält brauchbare Frequenzauflösung. |
| AN-DSP-011 | MUSS | Analyseverzug (Fenster-/Pufferlatenz) wird für die Konfiguration berechnet oder experimentell ermittelt; keine als echtzeitnah missverständliche Zeitachse. |
| AN-DSP-012 | SOLL | Frequenz-/Leistungstabellen können als relative Bandstatistik für spätere LFN-Scout-Auswertung bereitgestellt werden; die Pegelband-Definition bleibt dokumentiert. |

**Rechenbeispiel, nicht als Ist-Wert zu missverstehen:** Falls eine Decimation-Stufe exakt eine Halbierung bedeutet, liefern 48 kHz, drei Stufen und N=8192 rechnerisch `f_eff = 6000 Hz`, `Δf ≈ 0,732 Hz`. Implementiert die Pipeline einen anderen Decimationsfaktor, ist der tatsächliche Wert maßgeblich. Anti-Alias-Filter müssen Frequenzverlust und Übergangsbereich berücksichtigen.

### 4.4 Spektrum und Wasserfall

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-UI-001 | MUSS | Parallele Sicht auf Spektrum und Wasserfall, einstellbare Aufteilung (z. B. 1/3:2/3), Hoch-/Querformat ohne Datenverlust. |
| AN-UI-002 | MUSS | Frequenzachse linear (Standard) oder optional logarithmisch; unabhängiges Zoomen/Panning der Anzeigefrequenz ohne Änderung des Mithörbandes. |
| AN-UI-003 | MUSS | Presets 0–100 Hz und 0–200 Hz; benutzerdefinierte Grenzen innerhalb des real verfügbaren Analysebereichs; Anzeige von Hz und dBFS bzw. korrekt freigegebenem dB SPL. |
| AN-UI-004 | MUSS | dB-Minimum/-Maximum, automatische oder feste Pegelskala und geeignete beschriftete Achsen einstellbar. |
| AN-UI-005 | MUSS | Wasserfall scrollt zeitlich, zeigt konfigurierbare Historie (mindestens 120 s), stabile Frequenzlinien und zeitliche Ereignisse; Lücken sichtbar markieren. |
| AN-UI-006 | MUSS | Mindestens drei kontrasttaugliche Farbpaletten, Startwert frei wählbar; Palettenbereich und Waterfall-Pegelbereich separat einstellbar. |
| AN-UI-007 | MUSS | Max Hold Ein/Aus, Reset; optional Average mit dokumentierter Mittelungszeit, getrennt von Exponential Smoothing. |
| AN-UI-008 | MUSS | Peak Marker Ein/Aus zeigt bei Aktivierung Frequenz und korrekten Pegel; Zielfrequenz kann manuell markiert werden. |
| AN-UI-009 | MUSS | Die **Live-Analyse** läuft bei bloßer Anzeige-Pause weiter; Pause/Run-Zustand oben sichtbar, PNG-/CSV-Momentaufnahme mit Zeitstempel. Ein „Recording“-Status oder eine Aufnahmeschaltfläche wird nicht angezeigt. |
| AN-UI-010 | MUSS | Hohe UI-Last darf keine regelmäßigen Capture-Drops verursachen; reduzierte Bildrate oder Waterfall-Aktualisierung wird angezeigt. |
| AN-UI-011 | SOLL | Touch-Gesten und Band-Editiermodus werden über Hilfetext, Eingabefelder und alternativ bedienbare Buttons erklärt. |

### 4.5 Kalibrierungsdatei und Pegeldarstellung

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-CAL-001 | MUSS | Kalibrierung kann unabhängig von **Live-Analyse** und Mithören Ein/Aus und mit benanntem Profil gewählt werden. |
| AN-CAL-002 | MUSS | JSON-Kalibrierdatei mit `schemaVersion`, `profileId`, Geräte-/Quellenkennung, `points[]` in Hz/dB, Referenzangaben, Erstell-/Prüfdatum, validiertem Gültigkeitsbereich und optional Prüfsumme. |
| AN-CAL-003 | MUSS | CSV-Import mindestens `frequency_hz,correction_db`; absolute Referenz kann in einem separaten, expliziten Kalibrierdialog ergänzt werden, **niemals aus einer bloßen CSV angenommen**. |
| AN-CAL-004 | MUSS | Korrekturpunkte müssen endliche Werte mit monoton aufsteigender Frequenz und hinreichend vielen Punkten aufweisen; Dubletten/NaN/ungültiger Bereich werden mit verständlicher Meldung abgewiesen. |
| AN-CAL-005 | MUSS | Zwischen gültigen Stützstellen lineare Interpolation in `log(f)` für `f>0` bzw. eine dokumentierte Alternative. **Keine stille Extrapolation** unterhalb/oberhalb der Stützstellen. |
| AN-CAL-006 | MUSS | Nur kompatible Eingabekette (Gerät, Mikrofonweg, Quelle, Rate oder im Profil ausdrücklich freigegebene Alternativen) darf kalibriert angezeigt werden. Routenwechsel entwertet den SPL-Status bis zur erneuten Prüfung. |
| AN-CAL-007 | MUSS | Ohne absolute Referenz: Darstellung als **frequenzkorrigierter relativer Pegel**, ausdrücklich **nicht dB SPL**; unkalibrierte Roh-dBFS-Anzeige weiterhin umschaltbar. |
| AN-CAL-008 | MUSS | Absolute Referenz (z. B. dokumentierte Kalibrator-Messung 94 dB SPL bei 1 kHz) enthält erwarteten SPL, tatsächlich gemessenen RMS-dBFS-Referenzwert, Messmethode und Referenzfrequenz. Nur danach dB SPL bzw. dB(Z) anbieten. |
| AN-CAL-009 | MUSS | Physikalisch ungeeignete/ungeprüfte Bereiche werden schraffiert/gekennzeichnet; keine irreführende absolute SPL-Ausgabe unterhalb des validierten Mikrofonbereichs. |
| AN-CAL-010 | MUSS | Rohwert, angewandter Korrekturwert, Kalibrier-ID und ausgegebener Wert lassen sich für Testsignale reproduzieren und in Exportmetadaten zuordnen. |
| AN-CAL-011 | MUSS | Eine Änderung der Mithörlautstärke oder des Mithörbandes verändert niemals den Kalibriermodus oder die Kalibrierwerte. |
| AN-CAL-012 | SOLL | Nutzer kann Kalibrierkurve visualisieren, aktiv/inaktiv vergleichen und getrennte Profile pro Mikrofon bzw. USB-Interface verwalten. |

#### 4.5.1 Beispiel für ein Kalibrierprofil (Dateischema, Beispielwerte nicht als echte Messung verwenden)

```json
{
  "schemaVersion": 1,
  "profileId": "beispiel-internes-mikrofon",
  "device": {"manufacturer": "BEISPIEL", "model": "BEISPIEL", "route": "BUILTIN_MIC", "audioSource": "UNPROCESSED", "sampleRateHz": 48000},
  "correctionDefinition": "db_to_add_to_raw_spectrum",
  "points": [
    {"frequencyHz": 20, "correctionDb": 4.0},
    {"frequencyHz": 50, "correctionDb": 1.5},
    {"frequencyHz": 100, "correctionDb": 0.5},
    {"frequencyHz": 1000, "correctionDb": 0.0}
  ],
  "validFrequencyHz": [20, 1000],
  "absoluteReference": null,
  "createdUtc": "2026-09-19T00:00:00Z",
  "notes": "SIMULIERTE DATEN: NICHT FÜR ECHTE SCHALLDRUCKWERTE"
}
```

Die absolute Umrechnung **muss den verwendeten Pegeltyp beachten**: Ein Tonlinienpegel mit korrekt korrigierter Fensteramplitude ist nicht automatisch derselbe Messwert wie über ein Frequenzband integrierter RMS/PSD-Pegel. Für einen gültigen, passenden Referenzpegel `L_ref_raw(f_ref)` mit bekannte SPL `L_ref_spl` und `C(f)` in dB lautet die schematische Korrektur `K = L_ref_spl − L_ref_raw(f_ref) − C(f_ref)` und `L_out(f) = L_raw(f) + C(f) + K`; die Software muss diese Formel an die **ausgewiesene Messgröße** und ihr FFT-Normierungsverfahren anpassen. `L_ref_raw` darf nicht aus bloßer FFT-Höhe bei unbekannter Normierung geschätzt werden. Frequenzgewichte: Standard Z/ungewichtet; A/C erst nach separater korrekter Implementierung und eindeutiger Kennzeichnung. Ein Mikrofonfrequenzgang kompensiert **keine** unbekannte Hardware-Hochpassfilterung oder unterbrochene Datenkette.

### 4.6 Mithör-Bandpass – Parameter, Overlay, Gesten

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-BP-001 | MUSS | Ein gemeinsamer Zustandswert `(f_min, f_max)` steuert **ausschließlich** den Mithörbandpass und beide Overlays. |
| AN-BP-002 | MUSS | Startwert 20–80 Hz als konfigurierbarer Vorschlag; Nutzer kann `f_min` und `f_max` direkt numerisch eingeben. |
| AN-BP-003 | MUSS | Gültigkeitsregel: `f_min > 0`, `f_min < f_max`, Mindestbandbreite konfigurierbar (Anfangswert 2 Hz), `f_max` kleiner als ein sicherer Anteil der tatsächlich genutzten Nyquist-Frequenz; Grenzen auf unterstützten Bereich begrenzen und anzeigen. |
| AN-BP-004 | MUSS | Die Grenzen erscheinen als zwei dezente, klar unterscheidbare Linien in **Spektrum und Wasserfall**; leicht transparente Bandfläche verdeckt keine wichtigen Intensitätswerte. |
| AN-BP-005 | MUSS | Modus **„Band bearbeiten“**: linke/rechte Griffzone verschiebt einzeln `f_min` bzw. `f_max`; Trefferzonen auch auf kleinen Displays brauchbar (mind. ca. 44–48 dp). |
| AN-BP-006 | MUSS | Ziehen innerhalb des Bandes verschiebt **beide Grenzen mit gleichem Δf** (Panning), die Bandbreite bleibt konstant; Clamping an Grenzen verschiebt das Band als Ganzes. |
| AN-BP-007 | MUSS | Änderungen in einem Diagramm werden unmittelbar im anderen Diagramm und in numerischen Feldern gespiegelt, ohne Schleifen oder Werteflackern. |
| AN-BP-008 | MUSS | Außerhalb des Bearbeitungsmodus bleiben die normalen Grafik-Gesten für Frequenzzoom und -pan aktiv; normale Waterfall-Zeitnavigation darf durch Bandziehen nicht blockiert werden. |
| AN-BP-009 | MUSS | Eingabefelder und Plus/Minus-Feinsteuerung funktionieren als Alternative zu Gesten; für schmale Bänder mindestens 0,1-Hz-Schritte, falls DSP und Eingabegerät dies sinnvoll auflösen. |
| AN-BP-010 | MUSS | Filterneuberechnung beim Dragging erfolgt geglättet/entkoppelt; ohne hörbare Sprünge/Knackser durch abrupten Koeffizientenwechsel. |
| AN-BP-011 | MUSS | Filtertyp mindestens stabiler IIR-Bandpass, Entwurfsvorschlag kaskadierter Butterworth-Hoch-/Tiefpässe je 4. Ordnung; tatsächliche Durchlass-/Sperrkurve wird getestet und dokumentiert. |
| AN-BP-012 | MUSS | Der Overlay zeigt Bandmitte und Grenzen auf Wunsch; Hilfetext erklärt, dass das Band **nur Mithören** beeinflusst und Spektrum/Waterfall nicht herausfiltert. |
| AN-BP-013 | SOLL | Optional „Band an Peak ausrichten“ ohne Änderung der Bandbreite, optional invertierbare Drag-Richtung und getrennte Analyse-Bandpass-Erweiterung in späterer Version. |

**Unterscheidung der Gesten:** Ein-Finger-Drag am linken/rechten Griff = Grenze ändern; Ein-Finger-Drag der Bandfläche im aktiven Editiermodus = Gesamtband verschieben; Pinch außerhalb des Bandes = **Anzeigefrequenzen zoomen**. Bei Überschneidungen hat die explizit gewählte Bearbeitungsart Vorrang. Zwei-Finger-Drag kann als Anzeige-Pan angeboten werden, darf aber nicht versehentlich den Mithörbandpass verändern. Zeitnavigation im Wasserfall wird getrennt gehandhabt.

### 4.7 Mithören, Gain, Clip-Erkennung und automatische Abregelung

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-MON-001 | MUSS | Mithören ist beim ersten Start, nach App-Neustart und nach einem nicht bestätigten Routenwechsel AUS; aktiver Zustand muss eindeutig sichtbar sein. |
| AN-MON-002 | MUSS | Aktivieren erst nach Ausgabegeräteprüfung; eingebauter Lautsprecher ist für Live-Mikrofon-Monitoring standardmäßig gesperrt, um Rückkopplungen zu vermeiden. |
| AN-MON-003 | MUSS | Verdrahtete Kopfhörer/geeignetes USB-Audioausgabegerät auswählbar, soweit unterstützt; tatsächliche Route statt nur gewünschter Route anzeigen. |
| AN-MON-004 | MUSS | Benutzer-Gain einstellbar, Entwurfsbereich −60 bis +24 dB, Default 0 dB; Maximalwert und sichere Voreinstellung in der UI dokumentiert. |
| AN-MON-005 | MUSS | Effektiver Gain = Nutzer-Gain + automatisches Attenuations-Gain; beide Werte getrennt angezeigt, Automatik überschreitet **nie** den vom Nutzer gesetzten maximalen Gain. |
| AN-MON-006 | MUSS | Ausgangs-Peak wird **nach Bandpass und Verstärkung, vor PCM-Ausgabe** gemessen; bei Clip-Nähe beginnt sofort Abregelung, bevor Samples Full Scale überschreiten. |
| AN-MON-007 | MUSS | Sicherheits-Startwerte: Ziel-Peak höchstens ca. −6 dBFS, Vorwarnung/Regelbeginn ca. −6 dBFS, harter Limiter knapp unter 0 dBFS; Werte dokumentiert und per Test validiert, nicht als Schutz vor schädlichem Schalldruck behauptet. |
| AN-MON-008 | MUSS | Angriff schnell genug, um auf Kurzblöcke zu reagieren (Entwurfsziel ≤20 ms plus Lookahead/Peak-Limiter); Absenkung bis stumm; Rückkehr nur langsam (Entwurfswert ca. 3 s) und nicht über Nutzer-Gain. |
| AN-MON-009 | MUSS | Output-Clip-Risiko, tatsächliches Clipping, automatischer Reduktionsbetrag und ggf. Clip-Zähler sind getrennt sichtbar und timestampiert. |
| AN-MON-010 | MUSS | `INPUT_CLIP` bleibt als nicht korrigierbarer Messfehler markiert, selbst wenn der Ausgang unauffällig klingt. |
| AN-MON-011 | MUSS | Start/Stop, Kopfhörerabzug, Route zu Lautsprecher, Audio-Fokusverlust, Telefonanruf, Buffer-Fehler oder App-Fehler lösen sofort sicheres Mute/Fade aus. |
| AN-MON-012 | MUSS | Auch bei sehr hoher Nutzerverstärkung darf ein interner numerischer Überlauf/NaN nicht zu unkontrollierter PCM-Ausgabe führen; bei Fehler Mute und Warnung. |
| AN-MON-013 | MUSS | Keine Rückeinspeisung aus AudioTrack in die Messung; bei Ausgangsgain- oder Bandwechsel bleibt dBFS-Messung identisch. |
| AN-MON-014 | MUSS | Ausgangspegel ist **kein** kalibrierter Kopfhörer-SPL; Anzeige beschränkt sich auf digitalen Ausgangspegel, Gain und Schutzstatus. |
| AN-MON-015 | SOLL | Nutzer kann optional einen sicheren Start-Pegel, Attack/Release in Experteneinstellungen und eine Maximalverstärkung unterhalb +24 dB vorgeben. |
| AN-MON-016 | SOLL | Erkennbare Verzögerung und Aussetzer dokumentieren; bei Bluetooth klarer Hinweis auf routeabhängige Latenz/Verarbeitung. |
| AN-MON-017 | MUSS | Die automatische Abregelung, Gain-Hüllkurve und der Limiter bearbeiten **ausschließlich die Samples des Mithörzweigs nach dessen Abzweigung vom Eingang**. Änderungen am Eingangsgain/Android-Mikrofonmodus oder am Analysezweig als Folge des Mithörpegels sind verboten. |
| AN-MON-018 | MUSS | Bei aktivem Mithören, Gain-Änderung, Bandverschiebung und automatischer Absenkung bleiben flüchtige Eingangs-PCM-Blöcke, FFT, Kalibrierwerte, Spektrum, Wasserfall und PNG-/CSV-Momentaufnahmen bei identischem Eingangssignal bit- bzw. numerisch innerhalb definierter Toleranz identisch. |
| AN-MON-019 | MUSS | `INPUT_CLIP` wird **vor** jeder Mithörverarbeitung erkannt und nur als Datenqualitätswarnung ausgewiesen. Der Regler steuert ausschließlich bei *Mithör-Ausgangsrisiko* ab; eine sinkende Ausgabe darf die Eingangswarnung niemals zurücksetzen. |
| AN-MON-020 | MUSS | Der flüchtige automatisch abgesenkte Gain startet beim Einschalten/Recall definiert neu, wird nicht in Setups gespeichert und darf beim Rückregeln den gespeicherten Benutzer-Gain nicht überschreiten. |

**Grenze und Isolation der Automatik:** Die Auto-Abregelung arbeitet **nur im Mithörausgang**, nicht im Mikrofon-, Analyse- oder Kalibrierpfad. Sie kann ausschließlich ihren digitalen Ausgang vor der PCM-Grenze halten; sie verhindert weder die physische Lautstärkeentscheidung des Nutzers noch ein bereits am Mikrofon/ADC verzerrtes Signal. Ein `INPUT_CLIP` bleibt auch bei leise klingendem Mithören bestehen. Hohe Verstärkung und tiefe Frequenzen können überraschende Kopfhörerleistung verlangen. Deshalb vorsichtiger Start und keine Auto-Aufregelung über den Nutzerwert.

### 4.8 Testsignal, PNG-/CSV-Momentaufnahmen und Schnittstellen zu LFN-Scout

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-EXP-001 | MUSS | Manueller PNG-Export und separat aktivierbarer automatischer PNG-Speicherzyklus nach erster vollständiger Wasserfallfüllung und anschließend je Wasserfallzeit; jeweils mit Datum, Uhrzeit, Setup, Gerät, Kalibrier- und Clipstatus. Keine Audiodatei. |
| AN-EXP-002 | MUSS | Nutzerinitiierter CSV-Export des **aktuellen Spektralwertestands** mit `frequency_hz`, `level`, `level_unit`, Zeitpunkt und Kalibrierstatus; kein kontinuierliches Messdatenlogging und keine unmarkierte Mischung aus dBFS/dB SPL. |
| AN-EXP-003 | MUSS | Für dieses Analyzer-Unterpanel gibt es **keine Audio-Aufzeichnung** des Eingangssignals: weder WAV/PCM-Dateierzeugung noch Aufnahme-Timer, Hintergrund-Dauerlogging oder Audio-Replay. Vorhandene separate Aufnahmefunktionen anderer LFN-Scout-Module werden nicht verändert. |
| AN-EXP-004 | MUSS | Das verarbeitete Mithörsignal wird **nur in Echtzeit ausgegeben**; es gibt weder Aufnahme noch WAV-/PCM-Export des Mithörzweigs. |
| AN-EXP-005 | MUSS | Testsignalgenerator mit mindestens einstellbarem Sinus (z. B. 10/20/47/50/100 Hz) und Pegel in dBFS; kein unbeabsichtigter Ausgang über Lautsprecher und keine Vermischung mit dem Live-Mikrofonsignal. |
| AN-EXP-006 | MUSS | Jeder **manuelle/automatische PNG- bzw. manuelle CSV-Snapshot** enthält `settingsSchemaVersion`, `setupId`, `setupRevision`, unveränderlichen aktiven Setup-Schnappschuss, `appVersion`, Zeitstempel, passende `deviceId/route` (datenschutzgeeignet), `raw/calibrated`, `monitorBand`, `monitorGain`, `clipStatus`. |
| AN-EXP-007 | MUSS | Vorhandene LFN-Scout-Bewertungen/Events können nur über definierte Schnittstellen mit dem **Zeitpunkt einer Live-Messung bzw. eines expliziten Snapshots** verknüpft werden; keine unbemerkte Änderung bestehender Supabase-/GitHub-Schemas. |
| AN-EXP-008 | SOLL | Live-Analyse, PNG-/CSV-Momentaufnahmen und Setups sind ohne Internet nutzbar; Versand/Synchronisation nur als eigenständiger, ausdrücklich aktivierter Vorgang. |

### 4.9 Fehlerbehandlung, Performance und Qualität

| ID | P | Anforderung und Akzeptanzbedingung |
|---|---|---|
| AN-NFR-001 | MUSS | Im Vordergrund auf dem festgelegten Referenzgerät mindestens 10 Minuten **fortlaufende Live-Analyse** mit FFT, Wasserfall und optionalem Mithören ohne verlorene Audioblöcke; Grenzgerät(e) und Testergebnis dokumentieren. |
| AN-NFR-002 | MUSS | Live-Eingang und -Mithörausgabe haben Priorität gegenüber UI; bei Last wird Renderfrequenz reduziert und Auslassung sichtbar, statt flüchtige Live-Audiopuffer unkontrolliert zu verwerfen. |
| AN-NFR-003 | MUSS | Fehler im Audio-/Kalibrier-/DSP-/Exportpfad werden mit maschinenlesbarem Code protokolliert und benutzerverständlich erklärt. Keine stillen Exceptions. |
| AN-NFR-004 | MUSS | Tests für jedes zuvor behobene Problem werden als Regressionstest bewahrt; automatische PASS/FAIL-Selbsttests sind vor Freigabe ausführbar. |
| AN-NFR-005 | MUSS | Einstellungen sind versioniert mit definierten Defaults, GUI, Speichern, Laden, Migration und dokumentiertem Änderungsprotokoll. |
| AN-NFR-006 | MUSS | Buildverfahren aus dem aktuellen LFN-Scout-Projekt bleibt erhalten, einschließlich Android Studio, vorhandenen Build-Skripten/CI und Testreports. |
| AN-NFR-007 | MUSS | Das Analyzer-Unterpanel arbeitet bei **geöffneter LFN-Scout-App unabhängig vom gerade sichtbaren internen Panel**. Bei `Save aktiv` laufen Verarbeitung und PNG-Zyklus bei internem Panelwechsel weiter. App-im-Hintergrund/Bildschirm-aus sind davon getrennte, nur nach expliziter Freigabe und Android-Dienst-/Berechtigungsprüfung unterstützte Betriebsfälle; daraus folgt keine neue Analyzer-Recording-Funktion. |
| AN-NFR-008 | MUSS | Kein Claim „Infraschall gemessen“/„kein externer Schall vorhanden“ ohne validierte Übertragungsfunktion, Kalibrierung und Messmethodik. |
| AN-NFR-009 | SOLL | Anzeige reagiert auf Band-Drag visuell spätestens im nächsten Renderzyklus; auf Referenzgerät Ziel 10–20 Hz Renderfrequenz, ohne Capture zu gefährden. |

## 5. Abnahme- und Regressionstests

Alle Tests sind automatisiert, soweit sinnvoll, sonst manuell auf mindestens einem echten Android-Gerät durchzuführen. Ein Emulator allein reicht für Mikrofonfrequenzgang, Audio-Routing, Monitoring und Clip-Schutz nicht.

| Test-ID | Abgedeckte Anforderungen | Verfahren / PASS-Bedingung |
|---|---|---|
| T-01 | AN-INT-001–003 | Unterpanel öffnen/schließen, App aktualisieren; vorhandene LFN-Scout-Panels/Bestandsdaten weiter benutzbar. |
| T-02 | AN-INT-004–006, AN-SET-001–019, AN-NFR-005 | Zwei Setups mit unterschiedlichen FFT/Band/Gain/Kalibriereinstellungen sichern und abwechselnd per Recall laden; Neustart lädt das zuletzt gültige Setup und lässt Mithören AUS; ältere Version wird sicher migriert. |
| T-03 | AN-IN-001–007 | Internes/USB-Gerät wählen, UNPROCESSED-Support prüfen, Quelle/Rate ändern, Input entfernen; tatsächliche Route und Fehler korrekt. |
| T-04 | AN-DSP-001–005 | Synthetische Sinustöne 10/47/50/100 Hz bei bekannten dBFS-Werten; Frequenzfehler ≤ 1 effektives FFT-Bin; Analyse-Δf stimmt mit Konfiguration. |
| T-05 | AN-DSP-002, AN-DSP-004, AN-DSP-009 | Zwei Sinustöne mit ungleichen Pegeln und breitbandigem Rauschen; Anti-Alias, Fenster-Normierung, PSD-/Band-RMS-Rechnung dokumentiert und gegen offline Referenz verglichen. |
| T-06 | AN-UI-001–010 | Linear/Log, Zoom/Pan, 0–100/200, Peak, Max Hold/Reset, Wasserfall-Zeitstempel und Pause; Bildschirme bleiben lesbar. |
| T-07 | AN-CAL-001–012 | Profil mit `C(50 Hz)=+6 dB`: freigegebene Frequenz zeigt exakt +6 dB Unterschied zum unkorri­gierten relativen Wert (Toleranz ≤0,1 dB); außerhalb Bereich kein vorgetäuschter SPL. |
| T-08 | AN-CAL-006–009 | Profil passt nicht zu Gerät/Quelle/Rate, CSV ohne absolute Referenz, ungültige Stützpunkte; SPL-Modus gesperrt, Fehler gut sichtbar. |
| T-09 | AN-BP-001–009 | Band 20–80 Hz, linke Grenze nach 30, rechte nach 90, Gesamtband +10 verschieben: Endzustand 40–100 Hz in **beiden** Overlays, Bandbreite konstant. |
| T-10 | AN-BP-010–012 | Während Live-Audio Grenzen verschieben; kein NaN/unstabiler IIR, keine starken Umschaltknackser; 47-Hz-Durchlass und entfernte Frequenz testbar abgeschwächt. |
| T-11 | AN-DSP-008, AN-MON-013 | Eine identische **synthetische Live-PCM-Testfolge** wird dem DSP im Test eingespeist; Mithörfilter und Gain variieren. Spektrum und Wasserfall bleiben innerhalb festgelegter numerischer Toleranz gleich. Kein Audio-Dateireplay in der App erforderlich. |
| T-12 | AN-IN-008, AN-MON-006–010 | Eingangssample 0,999/Clip; getrennte INPUT- und OUTPUT-Indikatoren. Ausgangspeaks nach Abregelung/Limiter unter definierter PCM-Grenze, Eingang bleibt als verzerrt markiert. |
| T-13 | AN-MON-001–003, AN-MON-011 | Kopfhörer ziehen, Route zu Lautsprecher, Fokus/Telefonereignis und Neustart: Mute, kein automatisches Lautsprecher-Monitoring. |
| T-14 | AN-MON-004–008 | Gain +0→+24 dB und synthetischer Peak: effektiver Gain sinkt automatisch, kehrt langsam zurück und überschreitet nicht Nutzerwert. |
| T-15 | AN-EXP-001–008 | Manuell PNG/CSV und bei eingeschaltetem Save automatisch PNG exportieren; Zeitpunkt, Einheiten, Kalibrier-ID, Band/Gain und Clipstatus rekonstruierbar; **kein WAV/PCM-Export und kein Audio-Upload** aus dem neuen Analyzer. |
| T-16 | AN-NFR-001–004 | Zehnminütiger Last-/Dauerlauf; automatisierter Selbsttest und Regressionstest-Report PASS/FAIL verfügbar. |
| T-17 | AN-SET-001–005, AN-SET-018–019 | Mehrere Namen/IDs anlegen, Speichern unter, Recall, Duplizieren, Konfliktnamen, Löschbestätigung und Werkspreset-Kopie; keine anderen Setups verändert. |
| T-18 | AN-SET-006–009, AN-SET-017 | Setups mit verschiedenem Zoom, Waterfall, FFT, Kalibrierung, Band und Gain wechseln; alle Parameter reproduziert, Dirty-State/Warnung korrekt, Mithören bleibt AUS und Auto-Attenuation wird nicht restauriert. |
| T-19 | AN-SET-010–012, AN-SET-014–015 | JSON-Import mit altem/neuem/ungültigem Schema, Duplikat-ID, fehlender Kalibrierung, vollem Speicher und unterbrochenem Schreiben; letztes gültiges Setup bleibt erhalten. |
| T-20 | AN-SET-008, AN-SET-013, AN-SET-016 | Wiederanlauf mit `lastUsedSetupId`; Wechsel des Live-Eingangs bei inkompatibler Rate nur kontrolliert nach Warnung; PNG-/CSV-Snapshot enthält unveränderlichen Setup-Stand. |
| T-21 | AN-SET-014–016 | Offline-Import/-Export über SAF; auch nach App-Update und Neustart sind alle gespeicherten Setups und Metadaten vorhanden. |
| T-22 | AN-DSP-008, AN-CAL-011, AN-MON-006–010, AN-MON-017–020 | Identische Live-PCM-Testfolge bei Mithörgain 0/+24 dB und automatischer Absenkung: Nur Ausgangspuffer/Regler ändern sich, **flüchtiges Eingangs-PCM, Spektrum, Wasserfall, Kalibrier-/Messwerte und PNG-/CSV-Snapshots nicht**. `INPUT_CLIP` bleibt markiert; Recall lässt Mithören AUS. |

| T-23 | SYS-MES-001, SYS-MON-009, AN-EXP-003–004, AN-NFR-001 | Live-Analyzer mit Wasserfall/Mithören 10 Minuten betreiben, Anzeige einfrieren und fortsetzen, intern zu anderem Panel wechseln und neu öffnen; dabei bei aktiver Analyzer-Sitzung Historie erhalten. Separat App-Prozessneustart testen. **Keine neue WAV-/PCM-Datei im Analyzer, kein Aufnahme-UI und kein Replay**; flüchtige Historie ist nach App-Neustart nicht als Audiodatei verfügbar. Bestehende Datei-Aufnahmefunktionen bleiben semantisch unverändert und können einen gemeinsamen PCM-Eingang verwenden. |

### 5.1 Zusätzliche Negativ- und Grenztests

- `f_min = f_max`, `f_min=0`, `f_max > Nyquist`, `NaN`, ungültige Filterkoeffizienten, extrem schmale Bänder, rasches Grenzflächen-Dragging und gleichzeitiges Display-Zoom: Filter bleibt stabil oder weist Eingabe kontrolliert zurück.
- USB-Mikrofon während aktivem SPL-Modus entfernen/ersetzen: SPL-Status sofort ungültig, keine stille Profilübernahme.
- Nutzer-Gain auf Maximalwert, Eingang übersteuert: Warnung `INPUT_CLIP` bleibt erhalten; Output wird dennoch sicher begrenzt; **keine automatische Änderung der Eingangsverstärkung oder Mess-/Kalibrierwerte**.
- Setup wird bei aktivem Mithören gewechselt: Stummschaltung vor Neukonfiguration, neue Band-/Gain-Werte laden; weder aktive Wiedergabe noch flüchtige Auto-Absenkung wiederherstellen.
- Waterfall-Historie läuft über Ringbuffergrenze: älteste Zeilen korrekt entfernt, kein Speicherwachstum ohne Grenze.
- Fehlende Mikrofonberechtigung, inkompatible Sampling Rate, Android-Audio-Fokusverlust, Benachrichtigung/Sperrbildschirm, App-Rotation und Hintergrundwechsel: keine falsche Weiterbetriebsmeldung.
- Wiedergabegerät ohne hörbare Übertragung von 10–20 Hz: keine künstliche Verfügbarkeits-/Hörbarkeitsbehauptung.

## 6. Vorschlag für Architekturmodule (Designleitlinie; vor Coding gegen Repo prüfen)

| Modul / Interface | Verantwortung | Nicht verantwortlich für |
|---|---|---|
| `AudioInputController` | Device Enumeration, `AudioRecord`, Timesync, PCM-Ringbuffer, Input-Clip | UI-Zeichnung, SPL-Freigabe |
| `MeasurementEngine` | DC, Anti-Alias, Decimation, FFT, Normalisierung, Statistik | Mithör-Gain |
| `CalibrationRepository` | Profile, Validierung, Gerätebindung, SPL-Freigabe | AudioTrack |
| `MonitorEngine` | Bandpass, User-/Auto-Gain, Limiter, Mute | Korrektur der Messspektren |
| `AudioOutputController` | `AudioTrack`, Audio-Fokus, Route, Headset-Events, Puffer | Speicherung von Messkalibrierung |
| `AnalyzerViewModel` | UI-State, gemeinsam gebundene `f_min/f_max`, Gesten, Presets und Auswahl des aktiven Setups | zeitkritischer DSP im UI-Thread |
| `AnalyzerSetupRepository` | Benannte Setups, lokale atomare JSON-Persistenz, Recall, Import/Export, `lastUsedSetupId`, Migration | Audio-Capture, AudioTrack oder automatisches Mithören |
| `AnalyzerRenderer` | Spectrum/Waterfall/Overlay/Marker/PNG | PCM-Capture |
| `AnalyzerSnapshotExporter` | Auf Nutzeraktion aktuelle Spektraldaten (CSV) und Darstellung (PNG), sowie bei `Save aktiv` zeitgesteuert ausschließlich PNG, mit Zeit/Setup/Kalibrier-Metadaten | PCM-/WAV-Aufzeichnung, Audio-/CSV-Dauerlogging, unaufgeforderte Cloud-Synchronisation |
| `AnalyzerSelfTest` | synthetische PCM-Fixtures, PASS/FAIL, Geräteselbsttest | Ersatz der realen Mikrofonvalidierung |

**Threading:** Ein flüchtiger Live-Audioeingang, ein eigener verarbeitender Analysepfad und ein zeitkritischer Mithörausgabepfad; grafische Daten werden entkoppelt über begrenzte Queues transportiert. Keine Dateioperation, Netzwerkzugriff oder Layout-Rechnung im Audio-Callback. Änderung von Bandpasskoeffizienten atomar bzw. geglättet. Audio-Eingangs- und Ausgangssample-Rate getrennt berücksichtigen (Resampling nur falls erforderlich und dokumentiert).

**Sicherheit:** Mit dem vorhandenen Mikrofonpfad schallgekoppeltes Mithören darf nicht versehentlich in Lautsprecherbetrieb wechseln. Ein hard-limitierter **digitaler** PCM-Ausgang ist kein Gehörschutz-Garant. Bei sicherheitsrelevanten Fehlern stummschalten und verständlich melden.

## 7. Versionierte Konfigurations- und Referenzdateien

Normativ sind `schemas/analyzer_setup_example.json`, `schemas/microphone_calibration_example.json`, `schemas/noise_reference_example.json` und die vollständig referenzierte Parameterliste `references/Funktionsreferenz_v0.4.md`. Ein gespeichertes Setup referenziert Kalibrier-/Noise-Ressourcen über stabile IDs; diese Dateien bleiben **eigenständig**. Gespeicherter `monitor.userGainDb` ist zulässig; flüchtiger Auto-Gain, `monitor.active`, Sample-/Clipzähler und laufende PCM-Daten sind nicht zu speichern. Alle Dateien sind beim Import gegen ein versioniertes Schema zu prüfen, atomar zu schreiben und bei Kompatibilitätskonflikt nicht still zu aktivieren.

## 8. Traceability SRD → SRS → Abnahme

| Systemgruppe | Detailanforderungen | Primäre Tests |
|---|---|---|
| SYS-INT-001–008 | AN-INT-001–010, AN-SET-001–019, AN-UI-001 | T-01, T-02, T-06, T-17–21 |
| SYS-MES-001–006 | AN-IN-001–009, AN-DSP-001–012, AN-UI-001–011, AN-EXP-001–006 | T-03–06, T-15 |
| SYS-CAL-001–005 | AN-CAL-001–012 | T-07, T-08 |
| SYS-MON-001–012 | AN-BP-001–013, AN-MON-001–020 | T-09–14, T-22 |
| SYS-OPS-001–005 | AN-INT-007–010, AN-EXP-007–008, AN-NFR-001–009 | T-01, T-15, T-16 |

## 9. Verbindliche Klärungen beim Übergang zur Implementierung

1. **Ist-Live-Audioarchitektur:** Welche vorhandenen LFN-Scout-Komponenten liefern einen kompatiblen Mikrofon-Live-Stream und die Event-Zeitbasis? Vorhandene separate Datei-Aufnahmefunktionen des Gesamtprojekts bleiben semantisch erhalten; ihre Eingangs-Adapter dürfen für den gemeinsamen PCM-Fan-out geprüft umgebaut werden. Antwort aus Quellcode, nicht aus Namensannahmen.
2. **Validiertes Android-Gerät:** Welche interne/USB-Eingabe unterstützt `UNPROCESSED`, welche Frequenzantwort und welche effektive Abtastrate? Nicht durch Simulator ersetzen.
3. **Kalibrierreferenz:** Gibt es für eine konkrete Eingangskette eine nachweisbare Referenz (z. B. geeigneter 94-dB/1-kHz-Kalibrator), einen zulässigen Gültigkeitsbereich und eine dokumentierte FFT/RMS-Messgröße? Sonst nur relative Korrektur.
4. **Filter/Bandgrenzen:** tatsächliche Durchlasskurve und hörbare Qualität von Kopfhörern bei sehr tiefen Frequenzen; 20–80 Hz ist eine Vorbelegung, keine technische Grenze.
5. **Audio-Routing:** reales Verhalten beim Entfernen von Kopfhörern und bei Bluetooth/USB-Wechsel auf dem Zielgerät; stumm bei unbekannter Route.
6. **Performance:** mit 8192 und 3 Decimations messen; 4096 als dokumentierte Fallback-Konfiguration testen. Kein blindes Kopieren unbestätigter Hz/bin-Werte.
7. **Projektkonformität:** erst `docs/project/`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, bestehende Tests und aktuellsten Source-Code abgleichen; dann SDS, Code, Test und Changelog gemeinsam fortschreiben.



## 10. Zusätzliche und präzisierte Detailanforderungen v0.6

Alle vorhandenen `AN-...`-IDs aus v0.3 bleiben erhalten. Die neuen Anforderungen schließen insbesondere die Lücke zwischen v0.3 und der Parameterreferenz v0.4. Die 146 `CFG-...`-IDs der vollständig beigefügten Funktionsreferenz sind normative Parametereigenschaften (Default, Einheit, Gruppe, Schalter, Validierung, Persistenz) und gelten zusätzlich zu diesen `AN`-IDs.

| ID | P | Konkrete prüfbare Anforderung |
|---|---|---|
| AN-UX-001 | MUSS | Portrait-Hauptbild behält Spektrum **und** Wasserfall permanent bei; keine überladene Einstellleiste. |
| AN-UX-002 | MUSS | Einstellpanel ist separat, schmal und einspaltig scrollbar; Gruppen entsprechen CFG-SP, AU, MO, DI, ST, CA, NF, SV, SE, EX. |
| AN-UX-003 | MUSS | Tap auf freien Wasserfall blendet **nur** untere Karten weg; zweiter Tap stellt sie wieder her. Pause und Einstellungen bleiben in beiden Ansichten erreichbar. |
| AN-UX-004 | MUSS | Frequenzachse startet linear 0–100 Hz; Nutzer kann Frequenz-Min/Max und Pegel-Min/Max numerisch einstellen. Logarithmische Darstellung ist optional und muss 0 Hz ausschließen. |
| AN-UX-005 | MUSS | Aktuelle Spektralkurve gelb; Average rot, separat zuschaltbar; Rauschbodenreferenz grau, separat zuschaltbar und initial AUS. |
| AN-UX-006 | MUSS | Fadenkreuzcursor besteht aus horizontaler und vertikaler Linie, Schnittpunkt und Readout mit Frequenz, Pegel und korrekter Einheit. Er folgt bei horizontalem Drag ausgewählter Kurve. |
| AN-UX-007 | MUSS | Fadenkreuz kann auch bei eingefrorenem Anzeigebild bewegt werden; dessen numerische Werte werden ausschließlich aus dem eingefrorenen Spektralsnapshot bestimmt. |
| AN-UX-008 | MUSS | Bei Kurvenwahl „Beide“ zeigt Cursor zwei eindeutig farb- oder textzugeordnete Y-Werte für die gleiche Frequenz; kein irreführender einzelner Y-Schnittpunkt. |
| AN-UX-009 | MUSS | Bei freigegebenem Kalibrierprofil wird Readout-Einheit/Validität konsistent angezeigt; ohne absoluten Referenzpunkt nie dB SPL. |
| AN-UX-010 | MUSS | `Inferno` steht als Wasserfallpalette zur Verfügung; Skalengrenzen und Overlay bleiben lesbar. |
| AN-UX-011 | MUSS | Mithörband-Overlay in beiden Diagrammen besitzt gleiche Grenzen, ist dezent und ändert nicht die eigentlichen Messdaten. |
| AN-UX-012 | MUSS | Band wird im Diagramm horizontal verschoben unter Erhalt der **Bandbreite in Hz**; Bandbreite wird primär im Spektrum-Einstellpanel eingegeben. |
| AN-UX-013 | MUSS | Kurzer Tap auf Mithörfeld unten links toggelt Mithören; vertikales Wischen auf demselben Feld verändert ausschließlich Nutzer-Gain ohne versehentlichen Toggle. |
| AN-UX-014 | MUSS | `INPUT_CLIP`, `MONITOR_CLIP`/Vorwarnung, effektive Auto-Absenkung und `Save aktiv` sind kompakt im Hauptbild erkennbar, auch bei erweiterten Wasserfall. |
| AN-UX-015 | MUSS | Gestenkonflikt Band/Cursor/Zoom/Wasserfall-Tap wird deterministisch gelöst; numerische Alternativen stehen im Panel bereit. |
| AN-STA-001 | MUSS | L50/L90 basieren auf zeitgewichteten, korrekt normierten Band-RMS-/Leistungspegeln innerhalb eines **eigenständigen** Statistikbandes; Standard 2–25 Hz. |
| AN-STA-002 | MUSS | Auswertezeit 120 s default, einstellbar; L50/L90 sind Überschreitungspegel, bei gültigem Datensatz L50 ≥ L90. |
| AN-STA-003 | MUSS | Linien erscheinen als kurze horizontale Striche nur im sichtbaren Statistikband-Abschnitt auf ihrer korrekten Pegel-Y-Höhe, alternativ abschaltbar; numerisches Readout mit Einheit. |
| AN-STA-004 | MUSS | Unvollständiges Zeitfenster wird mit Füllfortschritt und eingeschränktem Status angezeigt; bei nicht validierbaren Frequenzen keine Scheinpräzision/SPL-Behauptung. |
| AN-SUP-001 | MUSS | Superfast verfolgt jedes tatsächlich eingelesene digitale Eingangssample (nicht nur UI-/FFT-Frames) auf den höchsten Betrag; zeitliche Aggregation darf Peaks nicht verlieren. |
| AN-SUP-002 | MUSS | Maximum Sample zeigt Peak und zugeordneten Ereigniszeitstempel, Reset wirkt nur auf dessen Historie; Kennzeichnung **Sample-Peak dBFS**, nicht ungeprüft True Peak/Schalldruck. |
| AN-NOI-001 | MUSS | Noise-Profil wird mit Dauer, Messketten-/Kalibrier-/FFT-/PSD-Definition und stabiler ID aufgenommen, lokal gespeichert und geladen; fehlende Referenz erzeugt Fehlerstatus. |
| AN-NOI-002 | MUSS | Graue Referenzkurve und rechnerische Kompensation sind **unabhängige Schalter**; Default beide AUS. |
| AN-NOI-003 | MUSS | Subtraktion erfolgt in kompatibel normierten linearen Leistungs-/PSD-Werten und nie per dB-Subtraktion. Nicht bestimmbarer Rest wird maskiert/gekennzeichnet. |
| AN-NOI-004 | MUSS | Kompensation betrifft nur optionale Darstellung (Spektrum oder Spektrum+Wasserfall), niemals Originalmessung, Original-Average, L50/L90, Mithören oder Roh-CSV. |
| AN-NOI-005 | MUSS | Profilkompatibilität (Gerät/Route/Rate/Kalibrierung/FFT/PSD) ist vor Aktivierung zu prüfen; inkonsistente Profile werden nicht still verwendet. |
| AN-SAV-001 | MUSS | `Save aktiv` bezeichnet allein automatisch gespeicherte **PNG-Bilder**; kein Audio-Recording oder Audio-Replay. |
| AN-SAV-002 | MUSS | Erstes automatisches Bild erst nach erster vollständiger Wasserfallzeit seit Aktivierung, danach einmal pro weiterer vollständiger Wasserfallzeit; keine PNGs pro Renderframe. |
| AN-SAV-003 | MUSS | Bild zeigt Hauptdisplay inkl. gerade sichtbarer Overlays und Status; Dateiname ist konfliktfest mit Zeit und Setup-ID, ergänzende Metadaten tragen Kalibrier-/Referenz-ID und Pegelmodus. |
| AN-SAV-004 | MUSS | In Anzeige-Pause keine wiederholten identischen Auto-PNGs; ab Fortsetzen erst nach erneutem vollständigem Zyklus. Manueller PNG bleibt verfügbar (festgelegter sicherer Default). |
| AN-SAV-005 | MUSS | Nach Recall/Neustart bleibt `Save aktiv` aus, bis für die neue Laufzeit erneut aktiviert (sicherer Default; Änderung nur als dokumentierte Produktentscheidung). |
| AN-SAV-006 | MUSS | Speicherfehler/fehlender Zugriff wird sichtbar; Live-Analyse wird nicht blockiert; kein unbeabsichtigter Audio- oder Netzexport. |
| AN-SET-020 | MUSS | Parameter der Gruppen CFG-SP/AU/MO/DI/ST/CA/NF/SV/SE/EX sind in Setup-Schema/Migration abgedeckt oder als read-only/Runtime ausdrücklich markiert. |
| AN-SET-021 | MUSS | Gespeicherte Kalibrier-/Rauschboden-ID wird bei Recall aufgelöst und validiert; fehlende Referenz wird sichtbar deaktiviert und Rohdarstellung bleibt möglich. |
| AN-SET-022 | MUSS | Gewünschter Save-Modus ist Konfigurationswert; tatsächliches `Save aktiv` und tatsächliches Mithören sind sicherheitsbewusst getrennte **Sitzungszustände**. |

### 10.1 Normative Parameterliste

`references/Funktionsreferenz_v0.4.md` ist **unverkürzt Bestandteil dieses Pakets**. Jede dort dokumentierte `CFG-...`-Zeile ist eine testbare Eigenschaft des Gesamt-SRS. Ihre `VORSCHLAG`-Werte sind **provisorische Werkseinstellungen**, bis am Referenzgerät validiert. Das alte Referenzdokument bleibt zur Nachvollziehbarkeit unverändert; anders lautende historische Freitextstellen zu nur manuellen PNGs werden durch `AN-SAV-001…006` abgelöst.

### 10.2 Sicherheits-/Nichtfunktionskriterien

- Kein Mithör-AGC-Parameter darf Eingangsverstärkung oder Mess-DSP verändern; synthetische Identitätstests vor und nach Gain-Regelung.
- Eine sichtbare `0 Hz`-Achsenbeschriftung ist **keine** 0-Hz-Mikrofonempfindlichkeit; 2–25-Hz-SPL nur nach Messkettenvalidierung.
- Kopfhörerabzug, unklare Ausgabe-Route und Audio-Fokusverlust erzwingen Mute. Ein rein digitaler Limiter garantiert keinen ungefährlichen akustischen Kopfhörerpegel.
- Alle Audiozeitstempel und Statistikintervalle beziehen sich auf monotonen Erfassungszeitbezug; Dateinamen zusätzlich auf UTC/Ortszeit mit eindeutigem Format.
- Android `RECORD_AUDIO` ist für Live-Eingang nötig, erzeugt aber keine Audiodatei.

### 10.3 Nachweis

Die zusätzlichen Testfälle `AT-01…AT-16` der Funktionsreferenz und `T-24…T-43` in `06_Test_Validierung_v0.7.md` ergänzen die bestehenden `T-01…T-23`. Der Testplan enthält PASS/FAIL, Testmethode, Datensätze und Hardware-Gates. Die Matrix `08_Traceability_v0.7.md` führt die neuen Anforderungen und die bisherigen Anforderungen auf ihre Tests zurück.


## 11. Detaillierte Softwareanforderungen v0.6 – gemeinsame Erfassung, Lebenszyklus, PNG und Histogramme

**Vorrang:** Diese Ergänzung gilt zusammen mit allen zuvor normativen `AN-*`- und 146 `CFG-*`-Zeilen. Für die neuen Einstellparameter siehe `13_Parameter_Ergaenzungen_v0.7.md`. Bestehende Aufnahme anderer App-Module bleibt erhalten, aber die interne Capture-Verdrahtung darf im Rahmen eines geprüften Adapter-Refactorings verändert werden.

| ID | Prio | Prüffähige Anforderung | Test |
|---|---|---|---|
| AN-SHARE-001 | MUSS | Ein zentraler Capture-Owner liefert `PcmBlock`-Frames mit monotonic timestamp, Format/Rate, Sequenz, Geräte-/Routenkennung, Clip- und Drop-Flags als unveränderliche Fan-out-Ereignisse. | T-44 |
| AN-SHARE-002 | MUSS | Analyzer und vorhandener Recorder erhalten Daten aus demselben Eingang, ohne gegenseitig ihre Blöcke zu konsumieren; ein langsamer Abnehmer blockiert den Capture-Thread nicht. | T-44, T-45 |
| AN-SHARE-003 | MUSS | Der Recorder erhält den vertraglich geforderten Kanalumfang, die tatsächliche Rate und eine kontinuierliche Block-/Timestamp-Folge; jede Umwandlung ist versioniert, geprüft und mit ursprünglicher Rate dokumentiert. | T-45, T-46 |
| AN-SHARE-004 | MUSS | Ein Filterwechsel, Mithör-Gain, Abregelung, Noise-Abzug oder Display-Pause verändert die Recorder-PCM-Samples nicht. | T-46 |
| AN-SHARE-005 | MUSS | Die Konfigurationsentscheidung priorisiert eine bereits aktive Aufnahme: keine Rate-/Quellen-/Geräteänderung des laufenden Recorderstreams durch Analyzer-Setup-Recall. Unvereinbare Anforderungen werden angezeigt und blockiert bzw. mit expliziter Wahl behandelt. | T-47 |
| AN-SHARE-006 | MUSS | Capture-Start/Stopp erfolgt über dokumentierte Bedarfshalter/Session-Owner; das Schließen eines Panels darf einen Capture mit aktiven anderen Nutzern nicht stoppen. | T-48, T-49 |
| AN-SHARE-007 | MUSS | Aufnahmebeginn/Ende während Analyse und umgekehrt ergeben keine unbezeichnete Lücke, doppelte Eingabe oder gemuteten Pseudostream; Error-Events zeigen echte Unterbrechungen. | T-48 |
| AN-LIFE-001 | MUSS | Analyzer-Sitzungszustand und flüchtige Historie liegen außerhalb des Panel-View-Lifecycles. Internes Navigationstoggle zerstört keine Session. | T-49 |
| AN-LIFE-002 | MUSS | Bei unsichtbarem Analyzer-Panel sinkt dessen Bildschirm-Renderfrequenz auf null; Signalverarbeitung, Histogramsnapshot-Empfang und Save-Zeitbasis laufen bei aktiver Sitzung weiter. | T-49, T-51 |
| AN-LIFE-003 | MUSS | Eine aktive `Save aktiv`-Sitzung hält die Analyzer-Engine auch bei anderem Panel aktiv; Wiederöffnen zeigt die inzwischen weitergelaufene, aktuelle Wasserfallhistorie und Save-Zähler. | T-49, T-50 |
| AN-LIFE-004 | MUSS | `Pause` ist eine vom Nutzer gesetzte **Anzeigepause** und stoppt automatische PNGs entsprechend v0.5; interner Panelwechsel ist **keine** Pause. Nach Fortsetzen startet ein neuer gültiger PNG-Zyklus. | T-40, T-49 |
| AN-LIFE-005 | MUSS | Hintergrundbetrieb außerhalb einer sichtbaren Activity wird **nicht** stillschweigend vorausgesetzt; falls explizit aktiviert, nur über genehmigtes Dienst-/Berechtigungskonzept und dokumentierte Benutzersteuerung. | T-52 |
| AN-LIFE-006 | MUSS | Ein externer Mikrofonunterbruch, Prozessende oder fehlender Berechtigungszustand markiert Daten und Save-Zyklus ungültig; keine interpolierten Scheinframes. | T-52, T-54 |
| AN-PNG-007 | MUSS | Der Snapshot-Renderer rendert Spektrum, Wasserfall, Filter-/Statistik-Overlays, Status und optional Histogramme aus einem konsistenten Immutable-Zustand auch ohne sichtbare Analyzer-View. | T-50, T-53 |
| AN-PNG-008 | MUSS | PNG-Namensgebung enthält eindeutige Zeit-/Sequenzangabe; Sidecar oder eingebettete Metadaten nennen Snapshot-Capture-Zeit, Exportzeit, Histogramm-Zeiten, Setup-Revision, Quellen-/Clipstatus. | T-53, T-58 |
| AN-PNG-009 | MUSS | PNG-Erzeugung und Dateischreiben laufen asynchron mit begrenzter Queue; bei Überlast wird der Ausfall sichtbar protokolliert, nie die Audioerfassung blockiert oder still ein Bild doppelt gespeichert. | T-54 |
| AN-PNG-010 | MUSS | PNG-Bildinhalt entspricht dem **Analyzer-Hauptbild im gewählten Layoutmodus**, nicht einem fremden gerade sichtbaren LFN-Scout-Panel. | T-50, T-53 |
| AN-HIST-001 | MUSS | Ein Read-only-`HistogramSnapshot`-Zulieferer stellt die letzten maximal vier vollständigen vorhandenen Radiushistogramme mit stabiler ID, Zeitangabe, Einheit, Bins und Herkunft bereit. | T-55 |
| AN-HIST-002 | MUSS | Auswahl `AUS`, `LETZTES`, `LETZTE_4` steht in `Anzeige & Wasserfall`; vier Miniaturen sind chronologisch gekennzeichnet (neueste oben) in schmaler rechter Spalte. | T-56 |
| AN-HIST-003 | MUSS | Fehlende, noch nicht vollständige oder veraltete Histogramme erhalten Status/Platzhalter; die Miniatur behauptet keine aktuelle Synchronität, wenn der Datensatz älter ist. | T-56, T-58 |
| AN-HIST-004 | MUSS | Der Wasserfall behält eine korrekte Frequenzabbildung und lesbare Zeitskala; Histogrammspalte skaliert den Plot **layoutbasiert**, nicht dessen Datenachse. Bei zu geringer Breite wird die Spalte gemäß definierter Responsive-Regel ausgeblendet. | T-56 |
| AN-HIST-005 | MUSS | Event-Update, Panel-Wechsel oder PNG-Rendering verändern keine Histogrammdaten im Quellmodul. | T-57 |
| AN-HIST-006 | SOLL | Antippen einer Miniatur kann das vorhandene Histogramm-Panel mit derselben Snapshot-ID öffnen, wenn dessen echte Navigation diesen Aufruf unterstützt; andernfalls nur lesende Miniatur. | T-57 |
| AN-HIST-007 | MUSS | Das Histogramm-Interface übernimmt das tatsächlich dokumentierte Radius-Bin-/Einheitenmodell der bestehenden App; bis Repo-Mapping keine erfundenen Radiusdefinitionen oder numerischen Achsenfestlegungen. | T-55 |
| AN-PROT-001 | MUSS | Standalone-Testhülle und integriertes Panel verwenden exakt denselben versionierten Analyzer-Kern und dieselben algorithmischen Tests; unterschiedliche Hosts sind lediglich Adapter. | T-59 |
| AN-PROT-002 | MUSS | Testhülle unterstützt synthetisches PCM und simulierte Histogramm-Events; der Live-Mikrofoneingang ist separat einschaltbar und verwendet denselben Input-Vertrag. | T-59 |
| AN-PROT-003 | MUSS | Freigabe der Integration erfordert bestandene Tests mit **echter vorhandener Audioaufnahme**, realem Panelwechsel und Offscreen-PNG; nur Standalone-PASS reicht ausdrücklich nicht. | T-45, T-49, T-50 |

**Default-/Wertevertrag:** Histogrammmodus `LETZTE_4` (VORSCHLAG; aus Wunsch „vorzugsweise letzte 4“), Breitenanteil 20 % des Wasserfall-Plots (VORSCHLAG; responsive begrenzen), Veraltet-Schwelle erst nach Analyse des tatsächlichen Histogramm-Erzeugungsintervalls. Weiteres in `13_Parameter_Ergaenzungen_v0.7.md`.

## 12. Übergabe-, Zustands- und Datenqualitätsanforderungen v0.7

| ID | Verbindliche Anforderung | Test |
|---|---|---|
| AN-HND-001 | Die Implementierung DARF nicht aus Pixeln eines Konzeptbildes abgeleitet werden, wenn Bild und normative Texte widersprechen; `assets/concepts/BILDREGISTER_v0.7.md` definiert den Status. | T-60 |
| AN-HND-002 | Der Histogramm-Adapter MUSS vor Implementierung die vorhandene Radiusdefinition, Einheit, Zeitbasis, Persistenz und Snapshot-Verfügbarkeit verifizieren. Bei fehlenden Daten leer/„nicht verfügbar“, nie synthetische Livewerte. | T-61 |
| AN-HND-003 | Beim Start der bestehenden Audioaufnahme unter laufendem Analyzer MUSS der Audio-Owner eine gemeinsame PCM-Quelle verwenden oder eine explizite verständliche Unvereinbarkeitsmeldung erzeugen; stillschweigendes Umschalten auf einen zweiten Mic-Client ist verboten. | T-62 |
| AN-HND-004 | Ein Wechsel zu einem anderen internen Panel DARF den aktiven Analyzer nicht stoppen, wenn `Save aktiv`; Offscreen-Renderer verwendet einen konsistenten, zeitgestempelten Snapshot der Analyzer- und Histogrammdaten. | T-63 |
| AN-HND-005 | Nur die ausdrücklich freigegebenen Zustände AUS/EIN des Mithörfeldes dürfen Audioausgabe aktivieren. Audioaufnahme im Bestands-Recorder ist hiervon unabhängig; Auto-Abregelung wirkt nie auf PCM-Mess- oder Recorderzweig. | T-64 |
| AN-HND-006 | Superfast-8× bezeichnet ausschließlich eine validierte Analyse-/Anzeigevariante; die Anwendung DARF eine höhere reale Samplerate nicht behaupten, wenn keine solche PCM-Erfassung stattfindet. | T-65 |
| AN-HND-007 | Bei PNG-Ausfall, Quota, Stau oder fehlendem Ziel MUSS Save-Status sichtbar fehlerhaft werden; niemals still drop oder Auslösen einer Audio-Aufnahme. | T-66 |
| AN-HND-008 | Nach Setup-Recall, Prozessneustart oder Headset-Abzug bleibt tatsächliches Mithören AUS bis zu einer expliziten erneuten Nutzerhandlung; der Sollwert der Verstärkung darf persistieren, aber keine Output-Session starten. | T-67 |
| AN-HND-009 | Das Spektrum MUSS in normaler und erweiterter Wasserfallansicht sichtbar bleiben; Histogrammspalte kann aus/ein sein, aber durch Wasserfall-Tap werden nur untere Zusatzanzeigen umgeschaltet. | T-68 |
| AN-HND-010 | `Save aktiv` steht ausschließlich für automatische PNG-Grafikspeicherung. Screenshotbilder MÜSSEN die tatsächliche Anzeigeeinheit, Referenz-ID, Quelle, Zeit und ggf. Kompensationsstatus konsistent mit dem zugehörigen Signalzustand abbilden; `Aufnahme aktiv` ist in diesem Status nicht zulässig. | T-69 |
| AN-HND-011 | Kein dBFS-Anzeigewert DARF allein durch Bildgestaltung, Kalibrierprofil-Import oder den Rauschbodenabzug als valider dB-SPL-Messwert ausgegeben werden; Kompatibilität und Gültigkeitsbereich sind zur Laufzeit zu prüfen. | T-70 |
| AN-HND-012 | Der Analyzer DARF keinen neuen automatischen Audiodatei-Pfad und keinen unaufgeforderten Netzwerktransfer hinzufügen; vorhandene LFN-Scout-Recorder und Serverfunktionen bleiben unverändert. | T-71 |

**Zusätzliche Nichtfunktionsanforderung:** Jedes schwerwiegende reale Testversagen erhält einen dokumentierten Regressionstest. Die UI-Abnahme erfolgt auf einem Hochformat-Gerät mit Überlappungsschutz für Cursorreadout, L50/L90, Histogramme und Bandpass-Overlay.




---

# QUELLDOKUMENT: `docs/project/03_SDS_Architektur_v0.7.md`

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




---

# QUELLDOKUMENT: `docs/project/04_UI_Gesten_v0.7.md`

# UI-/Gestenkonzept – LFN-Scout Live-Analyzer

**ID:** LFN-AN-UI · **Version:** 0.7 · **Status:** verbindlicher Bedienentwurf, nicht als Pixelkopie oder getestete UI ausgeben. Hochformat ist die Standardausrichtung.

## 1. Hauptbild: minimal und immer gleich strukturiert

```text
┌────────────────────────────────────┐
│ LFN-Scout / Spektrum  [II/▶] [⚙]  │
│ Eingang OK  | Clip! | Save aktiv  │
├────────────────────────────────────┤
│ SPEKTRUM: 0 … 100 Hz (linear)     │
│ dB │ Gelb aktuell, Rot Mittel    │
│    │  ggf. Grau Referenz         │
│    │  + Fadenkreuz / Readout      │
│    │  ─L50/─L90 (nur 2–25 Hz)   │
│    │  ░▒ Mithörband ▒░           │
├────────────────────────────────────┤
│ WASSERFALL (Inferno)              │
│    ░▒ dasselbe Mithörband ▒░     │
│   Zeitachse / Save-Zyklus         │
│ [kurzer Tap: Zusatzkarten AUS]    │
├────────────────────────────────────┤
│ [Mithören AUS/EIN ↕ Lautstärke]   │
│ [Superfast / Max Sample]          │
│ [L50/L90 numerisch]               │
└────────────────────────────────────┘
```

**Erweiterungsmodus:** Nach Tap auf freie Wasserfallfläche werden ausschließlich untere Zusatzkarten verborgen; freigewordener Platz vergrößert den Wasserfall nach unten. **Spektrum und Wasserfall bleiben erhalten**. Das obere Pause-/Fortsetzen- und Einstellicon sowie Alarm-/Save-Status sind weiterhin bedienbar. Zweiter Tap auf freien Wasserfall stellt Normalansicht her. Unabhängige L50-/L90-Linien und Cursor im Spektrum bleiben sichtbar.

## 2. Gesten mit Prioritätsordnung

| Prio | Ziel / Geste | Wirkung | Konfliktregel |
|---|---|---|---|
| 1 | Sicherheits-/Pause-/⚙-Tasten | Sofort Zustandswechsel/Einstellpanel | nie als Plot-Geste behandeln |
| 2 | Mithörfeld **kurz antippen** | `MONITOR_OFF↔MONITOR_ACTIVE`; nur wenn sichere Route | Threshold für Tap; nach Drag niemals Toggle |
| 3 | Mithörfeld **vertikal wischen** | Benutzer-Gain ↑/↓ (dB) | vor Änderung Deadzone; Feedback mit Wert |
| 4 | aktives Mithörband **horizontal ziehen** | Bandmitte verschieben, Bandbreite [Hz] konstant | sowohl im Spektrum als auch Wasserfall; Filter und Overlay gekoppelt |
| 5 | Cursor-Editiermodus im Spektrum ziehen | X-Position; Y aus ausgewählter Kurve | bei Berührung im Band: explizit Cursor-Modus/Haltepunkt zur Auswahl |
| 6 | Plot außerhalb Editierzonen pinch/pan | **nur Anzeigezoom/Skalen** | Band- und Statistikgrenzen unverändert |
| 7 | freier Wasserfall kurzer Tap | untere Karten aus/ein, Plot erhält mehr Höhe | langes Drücken/Drag/Pinch nie als Tap |

Zur Kollisionsvermeidung muss auch bei **unsichtbarer Bandfläche** eine numerische Einstellmöglichkeit vorhanden sein. Bandgrenzen lassen sich in Experten-Edit optional getrennt numerisch verändern. Einzelpunkt/Mehrfinger-Gesten sind auf Hochformat-Testgerät zu prüfen; zunächst keine unsichtbaren Spezialgesten voraussetzen.

## 3. Fadenkreuz und Readout

Senkrechte Frequenzlinie **und** waagerechte Pegellinie treffen sich auf der selektierten Kurve, mit Punkt und lokalem `Cursor: 47,3 Hz | −58,2 dBFS`-Feld. Durch horizontales Ziehen ändern sich X und passender Y-Wert. Während Pause wird über einen **eingefrorenen Daten-Snapshot** gelesen. Bei „Beide“ gibt es zwei explizite zugehörige Y-Werte (gelb/rot); keine willkürliche Mischung. Wertebox am Rand automatisch ausweichen lassen, niemals relevante Peaks oder die Pegelachse dauerhaft überdecken. Ein festes zusätzliches Readout ist separat optional AUS. Der Cursor ist initial AUS.

## 4. Kurven, Statistik und Überlagerungen

- Gelb = Live (immer EIN); Rot = Average (initial EIN; 120 s als Vorschlag); Grau = gespeicherte Rauschbodenreferenz (initial AUS); Max Hold/Peak Marker getrennt AUS.
- L50 und L90: **zwei kurze horizontale Striche** im x-Abschnitt des unabhängigen Statistikbandes (Standard 2–25 Hz); Y-Werte entsprechen den Bandpegeln; ggf. clipping/fehlende Bandkalibrierung mit Symbol kennzeichnen.
- Dezent eingefärbter Mithörfilterbereich (Standard 20–80 Hz) in beiden Plots und klar bezeichnet. Darf weder Rauschboden noch Statistikband verschieben; Zoom beeinflusst lediglich die Sichtbarkeit.
- `INPUT_CLIP`, `MONITOR_CLIP`, `AUTO −x dB`, `Save aktiv` knapp in Header/Status; bei Mithören AUS bleibt Clipstatus eindeutig getrennt. Keine Aufschrift „Aufnahme aktiv“.

## 5. Einstellpanel: schmal, 1 Spalte, scrollbar

Kein Breitdisplay. Registerauswahl maximal eine kompakte Zeile mit horizontalen Tabs oder Dropdown; darunter vertikal scrollende Karten. Jede Eingabe zeigt Einheit und geprüften Wertebereich; bei Grenzfehler Inline-Feedback, ohne Werte still zu ändern. Zurück/OK nur nach Validierung. Die 146 Parameter aus der beigefügten Referenz bleiben den folgenden Gruppen zugeordnet:

| Gruppe | Feldreihenfolge (übergeordnete Auswahl; die Parameter-Tabelle enthält vollständige Liste) |
|---|---|
| **Spektrum** | Achse Linear/Log, Frequenz Min/Max [Hz], Pegel Min/Max [dB], FFT, Decimations, Window, Transformintervall, Smoothing, DC, Gelb/Rot/Grau, Average-Zeit, Peak/MaxHold, **Fadenkreuz**, **Mithörbandbreite [Hz]** |
| **Audio** | Mikrofoneingang, UNPROCESSED bevorzugt, tatsächliche Source, Rate, Mono/PCM, INPUT_CLIP-Schwelle/Status, Drops |
| **Mithören** | Ausgangsroute, gewünschte Bandmitte/fMin/fMax, User-Gain, Auto-Abregelung, Ziel-Peak, Attack/Release, Clipstatus; der Bandbreitenwert wird im Spektrumsetup primär geändert |
| **Anzeige & Wasserfall** | Spektrum/Wasserfall-Anteil, Wasserfallzeit [s], Inferno/Palette, separate FarbdB-Min/Max, Auto-Skalierung, Kontrast, Zusatzkartenmodus |
| **Statistik & Impuls** | L50/L90 EIN/AUS, Statistik-Min/Max [Hz], Fenster [s], Striche/Zahlen, Superfast, Max Sample, Reset, Anzeigerate |
| **Kalibrierung – Mikrofon** | Aktivierung, Datei-/Profilwahl, Gültigkeit, relative/absolute Referenz, Einheit, Korrekturansicht |
| **Kalibrierung – Rauschboden** | Messen 120 s, benanntes Profil Speichern/Laden, Grau zeigen, Abzug, Anzeigeziel, Kompatibilitätsstatus |
| **Save & Export** | `Save aktiv`, manuelles PNG/CSV, Zielordner, Datei-/Metadatenformat, Status und Fehler |
| **Setups** | Neu, Speichern, Speichern unter, Recall, Duplizieren, Löschen, Import/Export, Letztes Setup, Preset-Auswahl |
| **Erweitert & Diagnose** | Testsignal, tatsächliche Hz/Bin, tatsächliche Quelle/Rate/Latenz, CPU/Drops, Selbsttest/Reports |

Wichtig: **keine Audio-WAV-Aufnahme** und keine irreführende Recorder-Schaltfläche. Schon bestehende App-Aufnahmefunktionen in *anderen* Panels sind hier nicht Teil der UI.

## 6. UI-Zustände / Sichtbarkeit

| Funktion | Normal | Erweitert | Pause | Nach Recall |
|---|---|---|---|---|
| Spektrum + Wasserfall | beide | beide, Wasserfall höher | eingefroren | Live nach erfolgreichem Start |
| Fadenkreuz, wenn EIN | sichtbar/beweglich | sichtbar/beweglich | **beweglich** | Position aus Setup; Live-Daten neu |
| Mithörfeld unten links | sichtbar | verborgen | in Normalansicht nutzbar | Mithören **AUS** |
| Input-/Monitor-Clip | kompakt | kompakt | aktueller Live-Sicherheitsstatus ggf. separat | zurückgesetzt und getrennt |
| `Save aktiv` | Header | Header | ausgesetzt, kein PNG-Duplikat | **AUS** bis explizit aktiviert |
| Zusatzkarten | sichtbar | verborgen | eingefrorene Statistik | Setupabhängig |
| Settings | erreichbar | erreichbar | erreichbar | erreichbar |

## 7. Visuelle Quellen und Grenzen

`assets/concepts/` enthält **nur** Entwurfsbilder zur Illustration des Nutzerwunsches. Einige KI-Bilder können ungenaue Texte/Skalen oder überzählige Bedienelemente enthalten und sind **nicht bindend**. Bindend ist die vorliegende funktionale Spezifikation. Die vom Nutzer gelieferten Spectroid-Screenshots sind als Funktionsreferenz beigefügt; ein eigenständiges LFN-Scout-UI darf keine fremden Marken- oder Icon-Assets ohne Rechteprüfung übernehmen.


## 8. Rechter Wasserfallbereich: Radiushistogramme (v0.6)

**Anordnung Hochformat:** Spektrum bleibt unverändert volle Plotbreite. Unterhalb liegt ein horizontaler Container aus eigentlichem Wasserfall links und optionalem Histogrammstreifen rechts; beide besitzen ihre eigene Darstellung. Die Histogramme werden **nicht als zusätzliche Spektralkurven** und nicht als neue Frequenzkanäle interpretiert.

```text
┌──────────────────────────────────────┐
│ SPEKTRUM (gelb/rot/Cursor/L50/L90) │
├──────────────────────────┬───────────┤
│ Wasserfall / Inferno     │ H1 neu    │
│                          ├───────────┤
│ gleiche Zeit- und        │ H2        │
│ Frequenzreferenz wie     ├───────────┤
│ bisher; die gewählte     │ H3        │
│ Anzeige-X-Achse wird     ├───────────┤
│ nicht beschnitten        │ H4 alt    │
├──────────────────────────┴───────────┤
│ Mithören / Superfast / L50/L90 (*)  │
└──────────────────────────────────────┘
(*) nur Normalansicht; Wasserfall-Tap blendet nur diese Karten aus.
```

| UI-Element | Zustand/Default | Verhalten |
|---|---|---|
| Histogramm-Anzeige | `LETZTE_4` VORSCHLAG | AUS, LETZTES, LETZTE_4 wählbar; neueste oben. |
| Spaltenbreite | 20 % VORSCHLAG | responsive nach Text- und Achsenbreite begrenzen; bei zu schmalem Plot Spalte durch explizite Layoutregel ausblenden und Hinweis anzeigen. |
| Status je Miniatur | Immer | Zeitpunkt und Datenstatus, bei fehlender Herkunft/Einheit keine erfundene Kurve. |
| Miniatur-Tap | optional | Nur wenn bestehende Navigation das Öffnen exakt dieses Histogramms ermöglicht; sonst keine Aktion. |
| Save-PNG | wenn Save EIN | Derselbe Wasserfall-/Histogramm-Renderstate wie im sichtbaren Analyzer, auch wenn anderes Panel aktiv. |

**Gesten:** Der Wasserfall-Tap darf ausschließlich auf freier Wasserfall-Fläche auslösen, niemals beim Tippen auf ein Histogramm. Horizontaler Band-Drag funktioniert nur im Wasserfall-Plot, nicht über dem Histogrammstreifen. In erweiterter Ansicht bleibt die rechte Histogrammspalte sichtbar; nur untere Zusatzkarten verschwinden. Wenn die Spalte AUS ist, nutzt der Wasserfall die volle Breite.

**Einstellungen:** `Anzeige & Wasserfall → Radiushistogramme` enthält Modus AUS/1/4, Breitenanteil und Anzeigenalter; letzteres erst nach Kenntnis des tatsächlichen Produktionsintervalls mit geeignetem Default belegen. `Save & Export` zeigt den tatsächlichen `Save aktiv`-Status auch im anderen Panel über einen globalen, kompakten App-Status (soweit ein solcher im aktuellen UI vorhanden ist; ansonsten in der Navigation ergänzt).

## 9. Verbindliche Bildauswahl und Kollisionen v0.7

Die Bildübersicht unter [`../../assets/concepts/BILDREGISTER_v0.7.md`](docs/project/../../assets/concepts/BILDREGISTER_v0.7.md) ist Teil dieser Spezifikation. Das **LEITBILD `C13`** zeigt die vom Nutzer zuletzt akzeptierten Mithörzustände EIN/AUS. `C11` und `C12` illustrieren Fadenkreuz und Readout; `C03` die korrekte Wasserfall-Erweiterung, `C09` schmale Einstellseiten. `D01` ist nur ein neuer, noch nicht freigegebener Entwurf der Histogrammspalte. Andere Bilder sind **Referenz/Historie, nicht Pixel-Vorlagen**. Verworfenes Material wurde ausgeschlossen.

**Achtung, Bildfehler:** Einige Bilder zeigen veraltete Statuswörter, nichtlineare Achsenticks oder unrealistische Wasserfallachsen, geänderte Bandgrenzen, ein zusätzliches Recorderfeld oder nicht plausible L50-/L90-/Kurvenwerte. Diese Inhalte sind **nicht** implementierungsrelevant. Das tatsächliche Hauptbild muss das Spektrum oben und Wasserfall darunter mit **gemeinsamer horizontaler Frequenzachse** gemäß aktuellen Vorgaben besitzen; nur die Zeit läuft auf der Waterfall-Längsachse. Beispielbilder dürfen keine Daten- oder Kalibrierfreigabe begründen.

Die rechte Histo-Spalte ist eigenständig scroll-/tippbar, ohne Drag des darunterliegenden Mithörbandes auszulösen. Beim Wasserfall-Tap wird ausschließlich auf freier Wasserfall-Fläche der Zusatzkartenmodus umgeschaltet. Die Haupt-Header-Pause-Taste und `Save aktiv` bleiben erreichbar. Bedienelemente haben Mindest-Touchgrößen und numerische Alternativen; automatische Text-/Tooltip-Positionierung verhindert verdeckte Diagrammachsen.




---

# QUELLDOKUMENT: `docs/project/05_Algorithmen_Kalibrierung_v0.7.md`

# Algorithmen, Kalibrierung und Signalqualität – LFN-Scout Analyzer

**ID:** LFN-AN-ALG · **Version:** 0.7 · **Status:** Berechnungsentwurf mit Validierungspflicht.  
**Leitregel:** Kein Filter, Gain, Limiter oder referenzbasierter Abzug des **Mithör-/Anzeigezweigs** darf Original-PCM oder die physikalisch interpretierbaren Messwerte verändern.

## 1. Eingangssignal und Clip-Erkennung

Aufnahme über Live-API (`AudioRecord` kann der Android-Klassenname sein), intern als `x[n]` in −1…+1 normiert, mit echter Rate, Route und monotoniem Zeitstempel. Bevorzugt `UNPROCESSED` falls verfügbar, andernfalls sichtbarer und prüfbarer Fallback. `INPUT_CLIP`: Sättigung/mehrfacher Grenzwertkontakt oder konfigurierbares Near-Clip-Signal (Default-Vorschlag −1 dBFS); pro Frame und pro zeitliche Statistik als Datenqualitätsmaske. Schwellenberührung allein beweist keinen Hardwareclip; Android-Vorverarbeitung kann unsichtbar schon früher verzerren. Eingangsgain **nie** vom Mithörlimiter zurückregeln.

## 2. Analyseauflösung und FFT-Normierung

Aktive Sample Rate `F_s`, Decimationsfaktor `D_eff` gemäß **realer** Pipeline; nach wirksamem Anti-Alias-Filter `F'_s=F_s/D_eff`. Für `N` FFT-Punkte: `Δf = F'_s/N`; zeitliche Fensterlänge `T_w=N/F'_s`. Bei beispielhafter Halbierung je Decimation, 48 kHz, 3 Stufen, `N=8192`: `F'_s=6000 Hz`, `Δf≈0,7324 Hz`, `T_w≈1,365 s`. **Nur Beispiel**, nicht ungeprüft auf Spectroid-Implementierung/Android übertragen. 100-ms-Transformintervall beschreibt nur den Abstand der Aktualisierung, nicht die 1,365-s-Fensterlänge. 2 Hz besitzt hier wenige eigenständige Bins; reale Hardware-Hochpässe können den Bereich zusätzlich begrenzen.

Blackman-Harris (Default), Hann/Rechteck optional; kohärenten Gain und äquivalente Rauschbandbreite des gewählten Fensters dokumentieren. Vor der Decimation Anti-Alias-Filter und ggf. DC-Subtraktion. Nichts unterhalb der kalibrierten Geräte-Untergrenze als absoluten Schalldruck deuten.

## 3. Einheiten und Spektral-PSD

Für FFT `X[k]=FFT(w[n]x[n])` und `W2=Σw[n]^2` ist die **einseitige** Leistungsdichteschätzung für interne reelle Samples (ausgenommen DC/Nyquist) z. B.:

`PSD[k] = 2|X[k]|² / (F'_s × W2)`  [normierte Digitalamplitude²/Hz].

Für DC und gegebenenfalls Nyquist fehlt der Faktor 2. Fenster-/Filterbandkorrektur muss konsistent in Kalibrierung, Referenzdatei, Band-RMS und CSV angewendet werden. `P_band≈Σ PSD[k] Δf`, optional mit korrekter Teilbin-Integration, `rms_band=sqrt(P_band)` und `L_band_dBFS = 20 log10(rms_band/refRms)` für eine *dokumentierte* Referenz (z. B. Full-scale-Sinus-RMS oder Full-scale-Amplitude). **Es darf nicht die FFT-Amplituden-Kurvenhöhe mit einem integrierten Band-RMS-Pegel gleichgesetzt werden.** Für tonale Amplituden-Readouts kann eine getrennte kalibrierte Amplitudennormierung verwendet werden; die gewählte Größe muss im Readout/Export erkennbar sein.

## 4. Gelb Live, Rot Average und Max Hold

- Gelb zeigt aus einem aktuellen Analyseframe gewonnene Spektralkurve; `Smoothing=0,3` ist ein separates exponentielles Anzeige-Glätten mit ausdrücklich dokumentierter Richtung (`y_t = α new + (1−α) old`, wenn α so definiert wird). Kein dB-Arithmetik-Mix ohne Begründung.
- Rot: unabhängig zuschaltbare **zeitgewichtete lineare Leistungs-/PSD-Mittelung** über vorgeschlagen 120 s, anschließend Umrechnung in passende dB-Anzeige; Fensterfüllgrad und Reset.
- Max Hold: unabhängiges Maximum pro Bin seit letztem Reset, spektral kompatible Definition, bei Konfigurationswechsel invalidieren. Peak Marker ist eine eigene, schaltbare Frequenz-/Pegelbeschriftung.
- Sample-Peak/Superfast ist **kein** Ersatz für Spektral-/RMS-Mittelwert.

## 5. L50/L90 im unabhängigen Statistikband

1. Aus **unkompensierten**, ggf. gültig mikrofonkalibrierten Band-PSD-Werten pro gültigem Analyseintervall die Bandenergie/RMS zwischen Standard `2–25 Hz` bilden; `20–80 Hz`-Mithörband spielt keine Rolle.
2. Zeitgewichtete Pegelproben über rollende Default-`120 s` aufnehmen; Audio-/FFT-Frames können überlappen. Verfügbare Statistik ist eine **Zeitverteilung**, keine unbegründete Anzahl unabhängiger FFT-Frames.
3. Definiere Exceedance-Quantil: `L50 = Q_0.50(L(t))`, `L90 = Q_0.10(L(t))` bei aufsteigendem Quantil. Für vollständigen gültigen Datensatz gilt `L50 ≥ L90`.
4. Ungültige Eingangsframes, Clipping und fehlende kalibrierte Bandteile maske/kennzeichne; bei unzureichender gültiger Zeitspanne Status `FÜLLT SICH` bzw. `UNGÜLTIG`, nicht als vollständige Statistik darstellen.
5. Zwei horizontale kurze Linien liegen beim errechneten **Pegel auf der Y-Achse**, aber nur über dem im Plot sichtbaren Statistikfrequenzabschnitt. Zusätzlich Zahlen mit Band, Fenster, Einheit, Gültigkeitsgrad. Weder pro-Bin-L50 noch arithmetisches Mittel aus dB-Frequenzbins.

**Absolute SPL-Freigabe:** nur nach validierter kalibrierter Gesamtmesskette *über das gesamte verwendete Statistikband* und nachgewiesener Band-RMS-Normierung. Sonst als relative Bandgröße mit sauberer Definition anzeigen oder, wenn nicht belastbar, als ungültig markieren. Ohne Kalibrierung nicht still ein unkalibriertes FFT-dBFS als dB SPL ausgeben.

## 6. Superfast und Maximum Sample

Für jeden erfassten Rohblock `p_block=max(|x[n]|)`; historisches `p_max=max(p_max,p_block)`, Zeitstempel der verursachenden Sampleposition (Index/F_s) festhalten; in dBFS `20 log10(max(p_max,eps))`. 20-ms-Aggregation betrifft **nur die Anzeige**, nicht die vollständige Peak-Suche. Reset setzt historische Maximum-Felder zurück und beeinflusst keine FFT-/L50-/Mithörwerte. Warnung bei `INPUT_CLIP` separat. Keine automatische Behauptung „akustischer Impulspegel“, `True Peak`, Gehörbelastung oder dB SPL.

## 7. Mikrofonkalibrierung

Eigenständige versionierte JSON-/CSV-Datei mit Stützstellen `(f, C(f) dB)`, Vorzeichenkonvention `L_corrected = L_raw + C(f)`, Geräte-/Quellen-/Ratebindung, Gültigkeitsband, optionaler absoluter RMS-Bezug. Interpolation im deklarierten gültigen Band (Methode dokumentieren), **keine stille Extrapolation**. Bei unbekannter Route oder inkompatibler Rate Profil ungültig und Roh-dBFS bleibt verfügbar.

Frequenzgangkorrektur allein erzeugt **relativ korrigierte Pegel**, kein dB SPL. Absolute SPL-Ausgabe erfordert nachgewiesene Kombination aus Übertragungsfunktion, Referenz (z. B. zertifizierter geeigneter Kalibrator in validiertem Band), zeitlicher Gültigkeit, Messgröße und zugehöriger Analysekette. Referenzpunkt bei 1 kHz allein belegt nicht die 2-Hz-Empfindlichkeit. Beispiel unter `schemas/microphone_calibration_example.json` enthält **keine reale Kalibrierung**.

## 8. Rauschbodenreferenz und optionale lineare Subtraktion

1. Referenz über z. B. 120 s als robuste, definierte **lineare PSD** (nicht dB-Mittel) messen; ID, UTC, Geräte-/Route-/Rate-/Fenster-/FFT-/Decimation-/Kalibrierkontext, PSD-Skalierung, Bin-Gitter und Umgebungsart speichern.
2. Vor Anwendung sämtlichen Kontext vergleichen; nur mathematisch nachgewiesene Umrechnung/Interpolation zulassen. Inkompatible Dateien nicht aktivieren.
3. `P_res(f)=P_current(f)-P_noise(f)` in kompatiblen **linearen** Einheiten; falls `P_res≤0` oder innerhalb der Unsicherheitsgrenze, bin `unbestimmbar` markieren statt einen scheinpräzisen negativen/−∞-dB-Wert zu erzeugen. Optional `max(0,...)` intern, aber niemals als vollwertige Messzahl behandeln.
4. Graue Referenz und Kompensation sind getrennte Schalter; wahlweise Anzeige nur Spektrum oder auch Wasserfall. Roh-/Original-Average, L50/L90 und Mithören bleiben erhalten. Ist eine rote Anzeige explizit „kompensiert“, muss eine eigene Kennzeichnung/Kurvenlogik definiert werden; Default bleibt **Original-Average**.
5. Raumhintergrund ≠ Eigenrauschen des Mikrofons; stationärer, näherungsweise unkorrelierter Hintergrund wird vorausgesetzt. Keine allgemeine Quellenunterdrückung oder Rekonstruktion realer Wellenform behaupten.

## 9. Mithör-Bandpass und Sicherheitsregelung (nur Mithörzweig)

Separate Route: PCM → stabiler dynamischer Bandpass (`f_min<f_max`; Standard 20–80 Hz) → User-Gain (0 dB Default) → Auto-Absenkung → harter PCM-Limiter/Mute → Kopfhörer. Frequenzband im Spectrum-Setup als **Breite in Hz** änderbar; X-Drag verschiebt beide Grenzen unter Beibehaltung der Breite; fMin/fMax sind außerdem numerisch editierbar. Neue Filterkoeffizienten atomar und ggf. glatt umschalten, kein hörbarer Sprung durch unkontrollierten internen Filterzustand.

`peak_postGain` steuert Auto-Gain: Zielpegel z. B. −6 dBFS, schnelle Absenkung, sanfte Freigabe (vorgeschlagen 3 s), niemals über User-Gain zurück anheben. Ein harter interner Limiter bleibt bei abgeschaltetem Auto-Modus. `MONITOR_CLIP_RISK` beim Annähern, `MONITOR_CLIP` bei tatsächlicher numerischer Übersteuerung; effektive Absenkung im Hauptbild. `INPUT_CLIP` stammt aus dem *unveränderten* Eingang und ist **keine** Regelgröße für den Ausgang.

**Schutzgrenze:** Ein digitaler Headroom/Limiter kann akustische Kopfhörerlautstärke nicht garantieren. Erst nach sicherer Route, niedriger Startlautstärke und explizitem Tap starten; Kopfhörerabzug/unklare Route/Fehler sofort Mute und AUS. Infraschall kann trotz digitaler Verstärkung nicht hörbar sein.

## 10. Wasserfall, Pause und automatische PNGs

- Wasserfall speichert flüchtige Spektralzeilen in begrenztem RAM; Farbpalette `Inferno` enthalten. Farbe = zeit-/frequenzbezogene Pegelklasse, keine Kalibriervorschrift.
- Sichtbare Historie `T_wf` (Default 120 s). Nach `Save aktiv` erste PNG **nur nach vollständiger Füllung** ab Aktivierungszeitpunkt; danach ein Bild je weiterer kompletter `T_wf`, unabhängig von UI-Renderframes. Eindeutige Zeit-/Setup-ID; Metadaten enthalten Einheit/Cal/Noise-/Compensationstatus und Clipflags. CSV bleibt ausschließlich manuell.
- **Pause:** Anzeige-Snapshot einfrieren; Cursor über frozen Spektrum bedienbar. Autosave wird ausgesetzt, damit keine identischen eingefrorenen Bilder regelmäßig entstehen; nach Fortsetzen neuer kompletter Zyklus. Mithörzweig darf unabhängig weiterlaufen und Sicherheitswarnungen anzeigen. Ein Wechsel von `T_wf` startet die PNG-Zählung neu.
- `Save aktiv` nach App-Neustart/Recall Standard AUS; Konfiguration kann Wunsch speichern, aber nicht unbemerkt Dateien erzeugen.

## 11. Mathematische Negativ- und Referenztests

**R-FFT:** Sinus bei 10, 47, 50 und 100 Hz; Fehler ≤ 1 effektives Bin, amplitudennormierter Frequenzgang gegen Python/NumPy-Fixtures.  
**R-STAT:** konstante Bandpegel => L50=L90; 90 % bei −80 und 10 % bei −60 => L90≈−80, L50≈−80; 50/50 => L50 nahe Median nach dokumentierter Interpolation.  
**R-NOISE:** `P_current=P_noise` => unbestimmbar; `P_current=2P_noise` => Rest `P_noise`; `−60 dB − (−70 dB)` darf **niemals** als `+10 dB` interpretiert werden.  
**R-MON:** identische PCM-Fixtures bei Gain 0/+24 und automatischer Absenkung: gleiche FFT/L50/L90/CSV-Rohwerte; Output begrenzt.  
**R-SAVE:** 120 s Wasserfall, Start t=0 => Save t≈120/240/360 s, nicht 0 oder je Frame; Pause verhindert Wiederholungsbilder.  
**R-CAL:** Profil `C(50 Hz)=+6 dB` => relative dB-Korrektur +6 innerhalb Gültigkeit, keine SPL-Anzeige ohne absolute Referenz.

**Status:** Diese Formeln sind Entwurfsgrundlagen; numerische Toleranzen/Rate und Referenzgerätekalibrierung müssen in der Implementierung festgelegt und nachgewiesen werden.


## 12. v0.6: gemeinsamer PCM-Zeitbezug, Histogramm-Snapshots und gerendertes Save

### 12.1 Capture-Integrität und parallele bestehende Datei-Aufnahme

- Zeitachse: `SystemClock.elapsedRealtimeNanos()` bzw. effektiv zugeordnete Audio-Timestamps; Samplefolge und Ausfälle mit `sequence`, `frameCount`, `sampleRateHz` und `validFlags` erfassen. UTC ist nur zusätzlich für Dateien/Anzeige.
- Bei gleichzeitiger Aufnahme bekommen Analyzer und Recorder dieselbe Quelle (und spezifiziert dieselbe ursprüngliche Audiosequenz). Zusätzliche Anti-Alias-Filter oder Downsampling werden auf **privaten abgeleiteten Kopien** je Consumer ausgeführt; keine Rückwirkung auf Recorder-PCM.
- Recorder-/Analyzer-Synchronität gilt nur innerhalb dokumentierter Toleranz und unter tatsächlich verfügbarem Timestamp-API. Lücken markiert statt künstlich mit validen Samples gefüllt; keine Überbrückung von `INPUT_CLIP` durch Monitor-AGC.
- Wenn laufende Recorder-Rate nicht zur gewählten Analyzer-Konfiguration passt, folgt nur der Analyzer-Pfad einer **getesteten** Rate-Konvertierung; wenn nicht möglich, verweigert der Analyzer die neue Auswahl. Keine unbeabsichtigte Recorder-Neuinitialisierung.

### 12.2 Zeitgewichteter Wasserfallzyklus

`T_wf` ist der konfigurierte **sichtbare Zeitumfang des Wasserfalls**, z. B. 120 s. Auto-PNG folgt gültigen monotonic-Intervallen im **internen Analyzer-State**, nicht der Zeit seit dem letzten gezeichneten UI-Frame oder dem Ende eines Dateischreibvorgangs. Bei 120 s erwartet: t=0 → 0 Bilder, t=120 → 1, t=240 → 2. Bei gewollter Anzeigepause zunächst 0 neue Bilder und nach Resume erneute komplette Periode. Ein interner Panelwechsel ändert diese Zähler nicht.

Zeit-/Blockfehler: Lücke > erlaubter Toleranz führt zu `INCOMPLETE_CYCLE` und definierter Neufüllung oder einem ausdrücklich als unvollständig markierten manuellen Snapshot; kein automatisches „voll“ durch Einfüllen identischer Zeilen. Genaue Toleranz bis Realdaten-Test konfigurierbarer VORSCHLAG.

### 12.3 Histogramm-Overlay ist Datenkopie, keine akustische Transformation

Der Histogramm-Publisher liefert `snapshotId`, Erzeugungszeit, ggf. gültiges Zeitintervall, Bin-Kanten, Werte, Einheit, Kategorie/Raum-/Ortsbezug sofern bereits vorhanden, Qualitätsflag und Quellenrevision. Der Analyzer rendert nur diese vorhandenen Daten; keine Ableitung neuer Radiusformeln. Im PNG wird die zum `captureIntervalEnd` zuletzt vollständige, vorhandene Auswahl mit den tatsächlichen Quellenzeiten eingefroren. Historische Histogramme sind nicht automatisch zeitgleich zum akustischen Wasserfall: **Altersangabe/Status sichtbar**.

### 12.4 Lastbudget und Speicherstrategie

Der Capture-Callback arbeitet in einer eigenen priorisierten Schleife; Fan-out/FFT/Histogramm-Repository und PNG-Renderer dürfen seine Deadline nicht blockieren. Wasserfallhistory ist ein begrenzter Speicherpuffer, dessen maximale Kosten aus Zeitfenster × Zeilenrate × Frequenzpunkten berechnet und bei Profilwechsel kontrolliert neu zugewiesen werden. Offscreen-Snapshot-Zugriff ist unveränderlich/copy-on-write. Bei hoher Last UI-FPS absenken, optional Miniaturdetail reduzieren; **nie zuerst** die Recorder-Integrität oder Ausgangssicherheit abschalten. Speicherzielfehler -> explizites Save-Fehlerereignis, kein Endlosretry.

## 13. Numerische Sicherungen und getrennte Anzeigen v0.7

- **L50/L90:** Die statistischen Überschreitungspegel werden aus einer Folge zeitgewichteter, physikalisch konsistent aus linearer Bandenergie gebildeter Pegelwerte für das **unabhängige** Statistikband berechnet. `L50` = 50-%-Überschreitungswert und `L90` = 90-%-Überschreitungswert im gleitenden Fenster (Default 2–25 Hz, 120 s). Abbruch/Drop/Eingangsclip erfordert eine definierte Qualitätskennzeichnung; Ungültiges nicht unbemerkt in die Verteilung mischen.
- **Average (rot):** zeitgewichtete lineare Leistung/PSD je Bin (Default 120 s als Entwurfswert), logarithmische Umrechnung erst zur Anzeige. Als Average darf kein dB-Arithmetikmittel ohne explizite Modellentscheidung verwendet werden.
- **Superfast:** `8×` nur nach Definition von Beobachtungsrate/Zeitanzeige; Sample-Max aus erfassten PCM-Werten der tatsächlichen `fs`. Keine „8×“ nachträglich erzeugte neue Information und keine True-Peak-/SPL-Verwechslung.
- **Rauschboden:** Speicherung mitsamt Geräte-/AudioSource-/Rate-/FFT-/Fenster-/Decimation-/Kalibrier- und Einheitssignatur. Eine angeforderte **Leistungs**subtraktion im Darstellungszweig ist nur bei Kompatibilität zulässig; negativer/unsicherer Rest wird markiert. Roh-Messwerte, Bestands-Aufnahme und Mithörzweig bleiben unverändert.
- **Kalibrierung:** Relative Frequenzkorrektur, absolute Referenz und Gültigkeitsband ausdrücklich getrennt. Nur validierte absolute Referenz plus geeignete Hardware berechtigen zur konkreten dB-SPL-Anzeige. Auch eine angezeigte SPL-Zahl außerhalb des Kalibrierbandes muss gesperrt oder gekennzeichnet sein.
- **Offscreen-Snapshot:** Einheiten, Kurvenart, Statistik-Band, Wasserfallzeit und verwendete Histogrammgeneration müssen dieselbe eingefrorene Zeitzuordnung besitzen. Nur gültige Referenzen annotieren. Render-Parameter gehören mit effektiver Audio- und Kalibrier-Signatur zu den Metadaten.




---

# QUELLDOKUMENT: `docs/project/06_Test_Validierung_v0.7.md`

# Test-, Validierungs- und Regressionstestplan – LFN-Scout Analyzer

**ID:** LFN-AN-TEST · **Version:** 0.7 · **Status:** geplanter Nachweis, bisher **0 neue Tests ausgeführt**. Kein PASS-Status ohne reale Ausführung.  
**Voraussetzung:** `01_SRD`, `02_SRS`, `03_SDS`, `04_UI`, `05_Algorithmen` sowie reale App-/Geräteversion und `BUILD_WORKFLOWS.md` geprüft.

## 1. Testebenen, Daten und Nachweise

| Ebene | Umsetzung | Beweis |
|---|---|---|
| Unit/DSP | Kotlin/JVM oder vorhandenes Testframework, fest eingespielte synthetische PCM/PSD-Vektoren | numerischer Soll/Ist-Vergleich und PASS/FAIL |
| Android-Integration | Instrumented Tests/Emulator für Lifecycle, SAF, Dateien, Navigation, UI | Testreport/Log/Abbildungen |
| Echte Audio-Hardware | Referenztelefon und ggf. USB-Mikrofon/Kopfhörer | Rate/Route, Mic-Frequenzgang, Pegel und Drop-Logs |
| UI-Gesten | Portrait-Gerät mit realer Touchfläche | Video/Screenshots, Interaktionsprotokoll |
| Langzeittest | 10 min minimal, erweiterter mehrstündiger Test wenn vorgesehen | Speicher/FPS/Audio-Drops/PNG-Sequenz |
| Regression | reproduzierter Fehler erhält dauerhaft Tests | Problem → Fix → PASS/FAIL → Release-Version |

**Messnachweise:** App-/Git-/Commit-/Build-Version, Gerät/Android-API, effektive Sampling-Rate, Mic/Headset/USB-Route, Testsignal, Testprofil, UTC, Konfigurationshash, Erwartung, Messwert, Toleranz, PASS/FAIL. Keine Behauptung über Geräte-Hardware nur aus Emulator.

## 2. Übernommene Tests

Alle `T-01…T-23` aus `02_SRS` bleiben im Testbestand (Integrität, Setups, Input, FFT, Kalibrierung, Band, Monitor, PNG/CSV, Fehler und ausdrücklich keine Audioaufnahme). **T-15** gilt in v0.6 einschließlich Auto-PNG. **T-23** prüft weiterhin die semantisch unveränderten bestehenden Datei-Aufnahmefunktionen anderer Panels; T-44…T-48 prüfen deren gemeinsame Audioanbindung.

## 3. Neue Tests v0.6 (vor Freigabe verpflichtend)

| Test-ID | Ebene | Konfiguration / Aktion | PASS-Kriterium | REQ-Bezug |
|---|---|---|---|---|
| T-24 | UI | Portrait; normal → Tap freien Wasserfall → Tap | Beide Ansichten behalten Spektrum + Wasserfall; Zusatzkarten verschwinden/erscheinen; Pause/⚙ erreichbar | AN-UX-001/003 |
| T-25 | UI | Spektrum Min/Max numerisch; Linear/Log; Y-Min/Max | 0–100 linear Default, optional 0–200; ungültige Min/Max blockiert, 0 Hz bei Log nicht erlaubt | AN-UX-004 |
| T-26 | DSP/UI | Referenz-PCM, Average EIN/AUS, Noise Grau EIN/AUS | Gelb/Rot/Grau separat; Rot zeitgewichteter Leistungsmittelwert; Grau nur Referenz | AN-UX-005, AN-NOI-002 |
| T-27 | UI/DSP | Fadenkreuz auf 47-Hz-Peak, verschieben, Pause und erneut verschieben | X/Y-Linien, Punkt, Readout (Hz+dB), richtige Kurve, kein Peakwert-Sprung durch UI-Freeze | AN-UX-006/007/008 |
| T-28 | UI | Mithörfeld zwei Taps, 1 vertikaler Swipe | AUS→EIN→AUS; Gain ändert nur nach Swipe; kein unbeabsichtigter Toggle | AN-UX-013 |
| T-29 | UI/DSP | Bandbreite 60 Hz; 20–80 → +10 Hz Drag in beiden Plots | 30–90 Hz in beiden Overlays; keine Veränderung von Zoom oder Statistikband | AN-UX-011/012 |
| T-30 | UI | Inferno, Normal-/erweiterter Wasserfall, dB-Grenzen | Palette vorhanden, Farbe konsistent; Status lesbar | AN-UX-010 |
| T-31 | Signal | Konstante korrekt integrierte Bandleistung 2–25 Hz über >120 s | L50≈L90; richtiger band-RMS-Pegel und Zeitgültigkeit | AN-STA-001/002 |
| T-32 | Signal | 90 % −80 dB / 10 % −60 dB mit korrekter Zeitgewichtung | L90≈−80, L50≈−80, L50≥L90 | AN-STA-002 |
| T-33 | UI | Statistikband ändern, L50/L90-Linien EIN/AUS | nur horizontale Striche über passendem sichtbarem X-Band bei Y-Pegel; numerische Werte korrekt | AN-STA-003 |
| T-34 | Signal/UI | Nur 30 s Daten eines 120-s-Statistikfensters | „füllt sich“/25 % und nicht als abgeschlossener L90; Clipping invalidiert | AN-STA-004 |
| T-35 | Signal | Ein kurzer Rohsample-Peak zwischen 100-ms-FFT-Renderframes | Superfast/Maximum Sample erfasst Peak, Zeit und Reset; kein True-Peak-Claim | AN-SUP-001/002 |
| T-36 | Noise | zwei kompatible PSD-Vektoren Pcur=2Pref, danach Pcur=Pref | korrekter Rest in linearer Leistung; zweiter Rest unbestimmbar/maskiert | AN-NOI-003 |
| T-37 | Noise/UI | Noise-Referenz speichern/Recall; Grau EIN, Abzug AUS; Abzug EIN | unabhängige Schalter, Referenz-ID, Anzeigeziel; Originalpegel immer abrufbar | AN-NOI-001/002/004 |
| T-38 | Noise | Profilrate/Route/Fenster unpassend oder Datei fehlt | Kompensation deaktiviert; Warnung; Originaldaten/Statistik/Monitor unverändert | AN-NOI-005 |
| T-39 | Save | `T_wf=120 s`; Save bei t=0 aktivieren; Zeit 0/119/120/240/360 | 0,0,1,2,3 Bilder; höchstens ein PNG pro Zyklus; Dateinamen/Metadaten korrekt | AN-SAV-001/002/003 |
| T-40 | Save/Pause | Save EIN, Pause bei 130s über weitere 240s, Fortsetzen | keine identischen Auto-PNGs in Pause; neuer Zyklus; manueller PNG möglich | AN-SAV-004 |
| T-41 | Save/Setup | Save-Präferenz EIN im Setup, App neu starten oder Recall | tatsächliches `Save aktiv` AUS bis explizit neu aktiv; Mithören AUS | AN-SAV-005, AN-SET-022 |
| T-42 | Save/Robustheit | Schreibzugriff entziehen, Speicher voll, App-Stop während Write | sichtbare Fehler, keine Datenkorruption/Dateinamenskollision, Live stabil | AN-SAV-006 |
| T-43 | Ganzheitlich | Referenz-PCM mit aktivem Monitor-Gain, Auto-AGC, Noise-View, Average, L50/L90, CSV | Roh-/kalibriertes Spektrum, L50/L90 und Roh-CSV invariant; nur Monitor-PCM und optionale Anzeige ändern sich | AN-MON-017…020, AN-NOI-004, AN-STA-001 |

## 4. Beispiel-Testfixtures mit Sollgrößen

- `SINE_47Hz_-60dBFS`: FFT-Peak im erwarteten effektiven Bin, korrekt ausgewiesene Amplitudennormierung.
- `BAND_2_25_CONSTANT`: Band-RMS und L50=L90 nach 120 s.
- `BAND_EXCEEDANCE_90_10`: 90 % Zeit −80 dB, 10 % −60 dB; L90 und L50 nahe −80 dB.
- `SINGLE_SAMPLE_NEAR_FULL_SCALE`: einmaliger Samplepeak trotz langsamen UI-Renderings.
- `NOISE_PSD_PAIR`: Test lineare PSD-Subtraktion inklusive invalidem Rest.
- `MONITOR_CLIP_SEQUENCE`: hoher Gain, synthetischer transienter Peak, output-only Attenuation, unberührte Measurement-Historie.
- `SETUP_V1_V3_MIGRATION`: gezielt alte/ungültige Kalibrierreferenz und Save-Präferenz.
- `AUTOPNG_FAKE_CLOCK`: monotone virtuelle Zeit, Wasserfall-Fill und Pause; keine echten 360 s im Unit-Test nötig.

## 5. QA- und Release-Gates

**G0 Repository:** aktuelles Projekt und verbindliche Dokumente gesichtet, Merge-Basis dokumentiert, Bestands-Tests grün.  
**G1 Algorithmus:** FFT/PSD, Statistik, Superfast, Noise- und Cal-Fixtures grün.  
**G2 UI:** Portrait, Gesten, Pause, Anzeige-Normal/Erweitert, Settings und Setups im Emulator grün.  
**G3 Hardware:** reale Input-Rate/UNPROCESSED, Kopfhörerroute/Latenz, Input-Clip, Frequenzgang und ggf. SPL-Freigabe dokumentiert; ohne valide Kalibrierung kein absolutes SPL.  
**G4 Regression/CI:** `T-01…T-59`, ggf. `AT-01…AT-16`, offizieller Buildworkflow und Changelog freigegeben.  
**G5 Übergabe:** vollständige `docs/project/`, Bedienung, Quelle-Stand, APK/Tests, offene Punkte und Release-Notizen konsistent.

**Hinweis:** Vor Umsetzung sind zahlreiche Tests formal spezifiziert, aber in diesem Dokumentpaket noch nicht ausgeführt; alle Statusfelder bleiben `NOT RUN`.


## 6. Zusätzliche Abnahme- und Regressionstests v0.6 (alle zunächst `NOT RUN`)

| Test-ID | Ebene / Szenario | Erwartung (PASS-Kriterium) | Anforderungen |
|---|---|---|---|
| T-44 | Unit + Integration: identischer Input-PCM-Strom, Analyzer/Recorder parallel | Ein CaptureOwner; jeder Abnehmer erhält gleiche vollständige Eingangssequenz/-Zeitstempel entsprechend Vertrag; Slow Analyzer blockiert Recorder nicht. | AN-SHARE-001/002 |
| T-45 | Android-Gerät: Recorder zuerst, Analyzer dazu; umgekehrt | Bestehende Recorder-Datei dauerhaft dekodierbar; korrekte Dauer/Rate/Kanäle, FFT läuft und Input wird nicht unbemerkt stumm. | AN-SHARE-002/003, AN-PROT-003 |
| T-46 | DSP: Mithören Gain, Band, AGC und Noise an/aus während Recorder | Recorder-PCM bei identischer Input-Fixture bitgleich oder bei spezifizierter Konvertierung toleranzgleich; Format und eigene Timestamps unverändert. | AN-SHARE-003/004 |
| T-47 | Recorder aktiv; Analyzer-Recall mit widersprechender Rate/Route | Recorder unverändert; Analyzer kompatibel konvertiert **oder** verweigert mit sichtbarem Konflikt, kein stiller Neustart. | AN-SHARE-005 |
| T-48 | Start/Stopp von Analyzer und Recorder in beiden Reihenfolgen; Kopfhoererroute wechseln | Keine doppelte Mic-Instanz; keine verdeckten Lücken; letzter Lease schließt Capture, nötige Fehlermeldungen. | AN-SHARE-006/007 |
| T-49 | Save EIN, anderes Panel für 3×T_wf öffnen, Analyzer zurück | Engine/Statistik/History fortgeschrieben, Auto-Save nicht pausiert, letzte korrekte neue Wasserfallzeilen sichtbar; UI-Paint beim versteckten Analyzer AUS. | AN-LIFE-001…004 |
| T-50 | T_wf=120 s; nach 30 s von Analyzer zu anderem Panel, bis t=370 s dort | PNG bei t≈120/240/360, insgesamt 3, jedes bildet **Analyzer** und nicht das andere Panel ab. | AN-LIFE-003, AN-PNG-007/010 |
| T-51 | Analyzer-Panel 10× wechseln während FFT+Recording+Save | Kein Leak, kein mehrfacher Save-Job, keine Statistik-Neustarts, kein Plot-Rendering offscreen zwischen PNG-Jobs. | AN-LIFE-002, AN-SHARE-006 |
| T-52 | App verlassen, Bildschirm aus, Permission entziehen, OS-Kill | Ohne explizite Hintergrundfreigabe keine falsche Garantie; bei unterstützter freigegebener Hintergrundfunktion korrekte Android-FGS-/Permission-Prüfung; bei Verlust konsistente Fehlerzustände. | AN-LIFE-005/006 |
| T-53 | Offscreen-PNG mit L50/L90, Cursor, inferno, 4 vorhandenen Histos, anderem sichtbaren Panel | Vollständiger gültiger Analyzer-Frame, Source-Histogramm-IDs + Capture-/Exportzeit, aktuelle Setup-Revision im Metadatenvertrag. | AN-PNG-007/008/010 |
| T-54 | Simulierte langsame Medienablage, voller Speicher, 10 Events in Queue | begrenzte Queue; Drops mit `snapshotId` gemeldet; Capture-/Recorder-Frames nicht blockiert; keine PNG-Duplikate. | AN-PNG-009, AN-LIFE-006 |
| T-55 | Snapshot-Publisher sendet Histogramme 1…5 sowie doppelte ID | `lastFour()` liefert 5,4,3,2; nur abgeschlossene Werte; Originaldaten unverändert; korrekte Bin-/Einheitsmetadaten. | AN-HIST-001/005/007 |
| T-56 | Hochformat schmal, Histo AUS/1/4, normal/erweiterter WF | Immer sichtbares Spektrum, rechte Spalte falls Platz, korrekt lesbare Achsen; sonst definierter Ausblendhinweis. | AN-HIST-002/003/004 |
| T-57 | Histogramm-Panel versteckt und Analyzer sichtbar, umgekehrt; Miniatur-Tap | Daten kommen unabhängig vom UI; wenn Link unterstützt, exakt gewünschte ID geöffnet, sonst keine falsche Navigation. | AN-HIST-001/005/006 |
| T-58 | Histos veraltet/fehlend und andere Zeitbasis als Wasserfall | `keine Daten`/Zeitmarkierung; PNG enthält eingefrorene IDs und tatsächliches Histogrammalter, kein „zeitgleich“ ohne Beleg. | AN-HIST-003, AN-PNG-008 |
| T-59 | Standalone-Testhülle mit FakeClock + PCM/Histogramm-Fixture; danach integrierter Host | selbe DSP-/Snapshot-Outputs aus identischem Kern; Integrationstest bleibt Pflicht und kein zweiter eigener Mic-Stack. | AN-PROT-001…003 |

### 6.1 Testdurchführung / Akzeptanzprotokoll

Jeder T-44…T-59-Eintrag bekommt im realen Projekt `device`, Android/API, App-Commit, effektive Input-/Recorder-Rate, SetupHash, Fixture-ID, Start-/End-Monotonic, erwartete und tatsächliche PNG-Zeiten, Recorder-Hash/Formateigenschaften, Snapshot-ID, Status `PASS/FAIL/BLOCKED/NOT RUN` und nachvollziehbaren Log-/Screenshot-/Dateinachweis. Numerische Toleranzen und Zeitjitter **vor** der Messung anhand API-/Hardwaregrenzen definieren, nicht nachträglich an das Ergebnis anpassen.

**Freigabe-Gate G6:** T-44…T-59 inklusive T-45 (echte Recorderintegration), T-49 (Panelwechsel) und T-50 (echtes Offscreen-PNG) müssen PASS sein. Alle bestehenden T-01…T-43 und Bestandsregressionen bleiben verpflichtend; bei offenen Blockern keine Fertigmeldung.

## 7. Neue Abnahmetests v0.7 – Vollständigkeit und Produktionsintegration

Alle Tests `T-60`…`T-71` beginnen mit Status `NOT RUN`. Als Beleg ist je Test Eingabe/Testfixture, erwartetes Ergebnis, beobachtetes Ergebnis, Plattform/Build und PASS/FAIL anzugeben. Noch ausstehender Realtest darf nicht als PASS gelten.

| Test | Verfahren / Soll | Verknüpfte Anforderung |
|---|---|---|
| T-60 | Bildregister auf 13 vorhandene, nicht verworfene Konzeptbilder prüfen; keine verworfenen Assets im ZIP; UI trotz etwaiger Bildfehler normativ korrekt. | AN-HND-001 |
| T-61 | Histo-Provider mit 0,1,4,5 echten oder strukturell gültigen Test-Snapshots; neueste oben, Daten-ID und Zeitstempel korrekt, fehlende Werte als fehlend. | AN-HND-002 |
| T-62 | Bestehenden Recorder während aktiven Analyzers starten, Quelle/Rate-Konflikt simulieren; genau ein Capture-Owner oder klare Ablehnung ohne Datenlücke im Recorder. | AN-HND-003 |
| T-63 | Save 120 s, interne Panels wechseln, erstes PNG nach voller Historie, dann alle 120 s; Metadaten/Spektrum/Wasserfall/Histo zum selben Snapshot und keine UI-Abhängigkeit. | AN-HND-004 |
| T-64 | Mithörfeld mehrfach antippen, vertikalen Gain-Swipe, Ausgabe clippen; Referenz-PCM und Recorder-Datei bleiben bit-/toleranzgleich, nur Monitor-Output ändert sich. | AN-HND-005 |
| T-65 | Mit bekannter PCM-Rate Impulse setzen; Superfast-8× erhöht nicht vorgetäuscht Nyquist oder Eingangsauflösung; Max Sample/Reset korrekt. | AN-HND-006 |
| T-66 | Voller Speicher, SAF entzogen, langsame Writes/Queue-Overflow; sichtbarer Save-Fehler und definierte Skip-Zählung, Audio-Callback bleibt funktionsfähig. | AN-HND-007 |
| T-67 | Setup-Recall, Headset-Abzug, App-Neustart; tatsächliches Mithören AUS, Nutzer-Gain bleibt speicherbar, keine Lautsprecher-Route. | AN-HND-008 |
| T-68 | Wasserfall-Antippen in Normal/erweitert, Histo an/aus; Spektrum in beiden Ansichten erhalten, Cursor in Pause verschiebbar, Berührungszonen getrennt. | AN-HND-009 |
| T-69 | PNG-Labels bei Save EIN und AUS prüfen: nur `Save aktiv` bei tatsächlichem PNG-Zyklus, niemals Analyzer-„Aufnahme aktiv“; Screenshot reproduzierbar. | AN-HND-010 |
| T-70 | Nicht kalibrierte/inkompatible SPL-Profile und Noise-Referenzen importieren; dBFS-Labels und Signalqualität korrekt; ungültige SPL unterdrückt. | AN-HND-011 |
| T-71 | Dateisystem/Netzwerk/Privacy-Audit; Analyzer erzeugt keine Audio-Dateien, keine implizite Cloud-Aktion, alte Recorder-Panels unverändert. | AN-HND-012 |

**Release-Gate:** Bestehende Tests T-01…T-59 plus diese zwölf Tests und bestehende LFN-Scout-Regressionssuite müssen auf dem relevanten Code-/Gerätestand bewertet werden. BLOCKER bleiben offen, bis konkrete Fehler behoben oder als bewusster Scope-Change bestätigt sind.




---

# QUELLDOKUMENT: `docs/project/07_Integration_Uebergabe_v0.7.md`

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




---

# QUELLDOKUMENT: `docs/project/08_Traceability_v0.7.md`

# Traceability / Abdeckungsmatrix – LFN-Scout Analyzer

**ID:** LFN-AN-TRACE · **Version:** 0.7 · **Status:** Mapping für geplante Tests, **noch kein ausgeführter Test**.  
Legende: Alle Kennungen mit normativer Zeile in SRD/SRS/Parameterreferenz wurden indexiert. Eine Zuordnung zu einer Testgruppe ersetzt nicht den Nachweis, dass jedes Akzeptanzkriterium in einem realen Test verifiziert wurde. Tests T-01…T-23 stehen im SRS, T-24…T-59 im Testplan. Die historische, beim Wortlaut abweichende v0.3-Testbeschreibung wird durch die v0.6-Save-Festlegung präzisiert.

## 1. Systemanforderungen

| ID | Primärdokument | Testgruppen |
|---|---|---|
| `SYS-INT-001` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-002` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-003` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-004` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-005` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-006` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-007` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-INT-008` | SRD §4 / §10 | T-01/T-02/T-17…T-23 |
| `SYS-MES-001` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-MES-002` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-MES-003` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-MES-004` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-MES-005` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-MES-006` | SRD §4 / §10 | T-03…T-06, T-15 |
| `SYS-CAL-001` | SRD §4 / §10 | T-07/T-08/T-36…T-38 |
| `SYS-CAL-002` | SRD §4 / §10 | T-07/T-08/T-36…T-38 |
| `SYS-CAL-003` | SRD §4 / §10 | T-07/T-08/T-36…T-38 |
| `SYS-CAL-004` | SRD §4 / §10 | T-07/T-08/T-36…T-38 |
| `SYS-CAL-005` | SRD §4 / §10 | T-07/T-08/T-36…T-38 |
| `SYS-MON-001` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-002` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-003` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-004` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-005` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-006` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-007` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-008` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-009` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-010` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-011` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-MON-012` | SRD §4 / §10 | T-09…T-14/T-22/T-43 |
| `SYS-OPS-001` | SRD §4 / §10 | T-01/T-16 |
| `SYS-OPS-002` | SRD §4 / §10 | T-01/T-16 |
| `SYS-OPS-003` | SRD §4 / §10 | T-01/T-16 |
| `SYS-OPS-004` | SRD §4 / §10 | T-01/T-16 |
| `SYS-OPS-005` | SRD §4 / §10 | T-01/T-16 |
| `SYS-UX-001` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-002` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-003` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-004` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-005` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-006` | SRD §4 / §10 | T-24…T-30 |
| `SYS-UX-007` | SRD §4 / §10 | T-24…T-30 |
| `SYS-STAT-001` | SRD §4 / §10 | T-31…T-35 |
| `SYS-STAT-002` | SRD §4 / §10 | T-31…T-35 |
| `SYS-NOISE-001` | SRD §4 / §10 | T-36…T-38 |
| `SYS-SAVE-001` | SRD §4 / §10 | T-39…T-42 |
| `SYS-SET-001` | SRD §4 / §10 | T-17…T-21/T-41 |

## 2. Detaillierte Softwareanforderungen

| ID | SRS | Primäre Testgruppen |
|---|---|---|
| `AN-INT-001` | §4 oder §10 | T-01, T-23 |
| `AN-INT-002` | §4 oder §10 | T-01, T-23 |
| `AN-INT-003` | §4 oder §10 | T-01, T-23 |
| `AN-INT-004` | §4 oder §10 | T-01, T-23 |
| `AN-INT-005` | §4 oder §10 | T-01, T-23 |
| `AN-INT-006` | §4 oder §10 | T-01, T-23 |
| `AN-INT-007` | §4 oder §10 | T-01, T-23 |
| `AN-INT-008` | §4 oder §10 | T-01, T-23 |
| `AN-INT-009` | §4 oder §10 | T-01, T-23 |
| `AN-INT-010` | §4 oder §10 | T-01, T-23 |
| `AN-SET-001` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-002` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-003` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-004` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-005` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-006` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-007` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-008` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-009` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-010` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-011` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-012` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-013` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-014` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-015` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-016` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-017` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-018` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-019` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-IN-001` | §4 oder §10 | T-03, T-12 |
| `AN-IN-002` | §4 oder §10 | T-03, T-12 |
| `AN-IN-003` | §4 oder §10 | T-03, T-12 |
| `AN-IN-004` | §4 oder §10 | T-03, T-12 |
| `AN-IN-005` | §4 oder §10 | T-03, T-12 |
| `AN-IN-006` | §4 oder §10 | T-03, T-12 |
| `AN-IN-007` | §4 oder §10 | T-03, T-12 |
| `AN-IN-008` | §4 oder §10 | T-03, T-12 |
| `AN-IN-009` | §4 oder §10 | T-03, T-12 |
| `AN-DSP-001` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-002` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-003` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-004` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-005` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-006` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-007` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-008` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-009` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-010` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-011` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-DSP-012` | §4 oder §10 | T-04, T-05, T-11 |
| `AN-UI-001` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-002` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-003` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-004` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-005` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-006` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-007` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-008` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-009` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-010` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-UI-011` | §4 oder §10 | T-06, T-24…T-30 |
| `AN-CAL-001` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-002` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-003` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-004` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-005` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-006` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-007` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-008` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-009` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-010` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-011` | §4 oder §10 | T-07, T-08 |
| `AN-CAL-012` | §4 oder §10 | T-07, T-08 |
| `AN-BP-001` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-002` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-003` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-004` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-005` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-006` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-007` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-008` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-009` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-010` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-011` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-012` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-BP-013` | §4 oder §10 | T-09, T-10, T-29 |
| `AN-MON-001` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-002` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-003` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-004` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-005` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-006` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-007` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-008` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-009` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-010` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-011` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-012` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-013` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-014` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-015` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-016` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-017` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-018` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-019` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-MON-020` | §4 oder §10 | T-12…T-14, T-22, T-43 |
| `AN-EXP-001` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-002` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-003` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-004` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-005` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-006` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-007` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-EXP-008` | §4 oder §10 | T-15, T-39…T-42 |
| `AN-NFR-001` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-002` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-003` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-004` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-005` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-006` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-007` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-008` | §4 oder §10 | T-16, T-23 |
| `AN-NFR-009` | §4 oder §10 | T-16, T-23 |
| `AN-UX-001` | §4 oder §10 | T-24…T-30 |
| `AN-UX-002` | §4 oder §10 | T-24…T-30 |
| `AN-UX-003` | §4 oder §10 | T-24…T-30 |
| `AN-UX-004` | §4 oder §10 | T-24…T-30 |
| `AN-UX-005` | §4 oder §10 | T-24…T-30 |
| `AN-UX-006` | §4 oder §10 | T-24…T-30 |
| `AN-UX-007` | §4 oder §10 | T-24…T-30 |
| `AN-UX-008` | §4 oder §10 | T-24…T-30 |
| `AN-UX-009` | §4 oder §10 | T-24…T-30 |
| `AN-UX-010` | §4 oder §10 | T-24…T-30 |
| `AN-UX-011` | §4 oder §10 | T-24…T-30 |
| `AN-UX-012` | §4 oder §10 | T-24…T-30 |
| `AN-UX-013` | §4 oder §10 | T-24…T-30 |
| `AN-UX-014` | §4 oder §10 | T-24…T-30 |
| `AN-UX-015` | §4 oder §10 | T-24…T-30 |
| `AN-STA-001` | §4 oder §10 | T-31…T-34, T-43 |
| `AN-STA-002` | §4 oder §10 | T-31…T-34, T-43 |
| `AN-STA-003` | §4 oder §10 | T-31…T-34, T-43 |
| `AN-STA-004` | §4 oder §10 | T-31…T-34, T-43 |
| `AN-SUP-001` | §4 oder §10 | T-35 |
| `AN-SUP-002` | §4 oder §10 | T-35 |
| `AN-NOI-001` | §4 oder §10 | T-36…T-38, T-43 |
| `AN-NOI-002` | §4 oder §10 | T-36…T-38, T-43 |
| `AN-NOI-003` | §4 oder §10 | T-36…T-38, T-43 |
| `AN-NOI-004` | §4 oder §10 | T-36…T-38, T-43 |
| `AN-NOI-005` | §4 oder §10 | T-36…T-38, T-43 |
| `AN-SAV-001` | §4 oder §10 | T-39…T-42 |
| `AN-SAV-002` | §4 oder §10 | T-39…T-42 |
| `AN-SAV-003` | §4 oder §10 | T-39…T-42 |
| `AN-SAV-004` | §4 oder §10 | T-39…T-42 |
| `AN-SAV-005` | §4 oder §10 | T-39…T-42 |
| `AN-SAV-006` | §4 oder §10 | T-39…T-42 |
| `AN-SET-020` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-021` | §4 oder §10 | T-02, T-17…T-21 |
| `AN-SET-022` | §4 oder §10 | T-02, T-17…T-21 |

## 3. Parameter (jede normative CFG-ID einzeln)

| ID | Quellgruppe | Primäre Testgruppen |
|---|---|---|
| `CFG-SP-01` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-02` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-03` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-04` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-05` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-06` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-07` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-08` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-09` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-10` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-11` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-12` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-13` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-14` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-15` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-16` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-17` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-18` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-19` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-20` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-21` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-22` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-23` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-24` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-SP-25` | `references/Funktionsreferenz_v0.4.md` | T-04…T-06, T-25…T-27 |
| `CFG-AU-01` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-02` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-03` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-04` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-05` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-06` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-07` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-08` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-09` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-10` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-AU-11` | `references/Funktionsreferenz_v0.4.md` | T-03, T-12 |
| `CFG-MO-01` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-02` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-03` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-04` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-05` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-06` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-07` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-08` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-09` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-10` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-11` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-12` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-13` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-14` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-15` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-16` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-17` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-18` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-19` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-20` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-21` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-MO-22` | `references/Funktionsreferenz_v0.4.md` | T-09…T-14, T-28, T-29, T-43 |
| `CFG-DI-01` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-02` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-03` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-04` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-05` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-06` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-07` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-08` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-09` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-10` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-11` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-12` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-DI-13` | `references/Funktionsreferenz_v0.4.md` | T-06, T-24, T-30, T-39 |
| `CFG-ST-01` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-02` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-03` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-04` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-05` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-06` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-07` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-08` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-09` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-10` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-11` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-12` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-13` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-14` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-ST-15` | `references/Funktionsreferenz_v0.4.md` | T-31…T-35 |
| `CFG-CA-01` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-02` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-03` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-04` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-05` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-06` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-07` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-08` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-09` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-CA-10` | `references/Funktionsreferenz_v0.4.md` | T-07, T-08 |
| `CFG-NF-01` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-02` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-03` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-04` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-05` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-06` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-07` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-08` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-09` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-10` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-11` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-NF-12` | `references/Funktionsreferenz_v0.4.md` | T-36…T-38 |
| `CFG-SV-01` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-02` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-03` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-04` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-05` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-06` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-07` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-08` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-09` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-10` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-11` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-12` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SV-13` | `references/Funktionsreferenz_v0.4.md` | T-15, T-39…T-42 |
| `CFG-SE-01` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-02` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-03` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-04` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-05` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-06` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-07` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-08` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-09` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-10` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-11` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-12` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-13` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-14` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-SE-15` | `references/Funktionsreferenz_v0.4.md` | T-02, T-17…T-21, T-41 |
| `CFG-EX-01` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-02` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-03` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-04` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-05` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-06` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-07` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-08` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-09` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |
| `CFG-EX-10` | `references/Funktionsreferenz_v0.4.md` | T-04, T-16, T-23 |


## 4. Umfang und Lückenstatus

- Normative System-Zeilen: **48**.
- Normative Software-Zeilen: **158** (davon **35** neu aus der v0.6-Überarbeitung; einzelne Testanforderungen sind mehrfach gruppiert).
- Normative Parameter-Zeilen: **146**, in **10 Gruppen** (vorherige v0.4-Referenz **vollständig erhalten**).
- Testfälle: T-01…T-23 im SRS + T-24…T-43 im neuen Testplan, und AT-01…AT-16 in der Parameterreferenz. **Noch nicht ausgeführt.**
- Zuordnung ist auf Testgruppenebene; vor Release für jede Tabellenzeile die konkreten Assertion-IDs und Artefakt-Logs ergänzen.


## 4. v0.6 neue Systemanforderungen → Testgruppe

| Kennung | Primärstelle | Tests |
|---|---|---|
| `SYS-AUDSH-001` | SRD §11 | T-44…T-48 |
| `SYS-AUDSH-002` | SRD §11 | T-44…T-48 |
| `SYS-AUDSH-003` | SRD §11 | T-44…T-48 |
| `SYS-LIFE-001` | SRD §11 | T-49…T-52 |
| `SYS-LIFE-002` | SRD §11 | T-49…T-52 |
| `SYS-LIFE-003` | SRD §11 | T-49…T-52 |
| `SYS-PNG-001` | SRD §11 | T-39…T-42, T-50, T-53/54 |
| `SYS-PNG-002` | SRD §11 | T-39…T-42, T-50, T-53/54 |
| `SYS-HIST-001` | SRD §11 | T-55…T-58 |
| `SYS-HIST-002` | SRD §11 | T-55…T-58 |
| `SYS-HIST-003` | SRD §11 | T-55…T-58 |
| `SYS-PROT-001` | SRD §11 | T-59 |

## 5. v0.6 neue Softwareanforderungen → Testgruppe

| Kennung | Primärstelle | Tests |
|---|---|---|
| `AN-SHARE-001` | SRS §11 | T-44…T-48 |
| `AN-SHARE-002` | SRS §11 | T-44…T-48 |
| `AN-SHARE-003` | SRS §11 | T-44…T-48 |
| `AN-SHARE-004` | SRS §11 | T-44…T-48 |
| `AN-SHARE-005` | SRS §11 | T-44…T-48 |
| `AN-SHARE-006` | SRS §11 | T-44…T-48 |
| `AN-SHARE-007` | SRS §11 | T-44…T-48 |
| `AN-LIFE-001` | SRS §11 | T-49…T-52 |
| `AN-LIFE-002` | SRS §11 | T-49…T-52 |
| `AN-LIFE-003` | SRS §11 | T-49…T-52 |
| `AN-LIFE-004` | SRS §11 | T-49…T-52 |
| `AN-LIFE-005` | SRS §11 | T-49…T-52 |
| `AN-LIFE-006` | SRS §11 | T-49…T-52 |
| `AN-PNG-007` | SRS §11 | T-39…T-42, T-50, T-53/54 |
| `AN-PNG-008` | SRS §11 | T-39…T-42, T-50, T-53/54 |
| `AN-PNG-009` | SRS §11 | T-39…T-42, T-50, T-53/54 |
| `AN-PNG-010` | SRS §11 | T-39…T-42, T-50, T-53/54 |
| `AN-HIST-001` | SRS §11 | T-55…T-58 |
| `AN-HIST-002` | SRS §11 | T-55…T-58 |
| `AN-HIST-003` | SRS §11 | T-55…T-58 |
| `AN-HIST-004` | SRS §11 | T-55…T-58 |
| `AN-HIST-005` | SRS §11 | T-55…T-58 |
| `AN-HIST-006` | SRS §11 | T-55…T-58 |
| `AN-HIST-007` | SRS §11 | T-55…T-58 |
| `AN-PROT-001` | SRS §11 | T-59 |
| `AN-PROT-002` | SRS §11 | T-59 |
| `AN-PROT-003` | SRS §11 | T-59 |

## 6. v0.6 neue Einstell-/Statusparameter → Testgruppe

| Kennung | Primärstelle | Tests |
|---|---|---|
| `CFG-HI-001` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-002` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-003` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-004` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-005` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-006` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-HI-007` | Parameterergänzung §1 | T-55…T-58 |
| `CFG-LF-001` | Parameterergänzung §1 | T-49…T-52 |
| `CFG-LF-002` | Parameterergänzung §1 | T-49…T-52 |
| `CFG-LF-003` | Parameterergänzung §1 | T-49…T-52 |
| `CFG-LF-004` | Parameterergänzung §1 | T-49…T-52 |
| `CFG-AU-001` | Parameterergänzung §1 | T-44…T-48 |
| `CFG-AU-002` | Parameterergänzung §1 | T-44…T-48 |
| `CFG-AU-003` | Parameterergänzung §1 | T-44…T-48 |
| `CFG-AU-004` | Parameterergänzung §1 | T-44…T-48 |
| `CFG-SV-001` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-SV-002` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-SV-003` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-SV-004` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-SV-005` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-SV-006` | Parameterergänzung §1 | T-39…T-42, T-50, T-53/54 |
| `CFG-TST-001` | Parameterergänzung §1 | T-59 |
| `CFG-TST-002` | Parameterergänzung §1 | T-59 |

**Abdeckungshinweis:** Alle neuen IDs sind hier erfasst, die 146 bisherigen Parameter unverändert in §3/Originalreferenz. Testzuordnung ist geplant, **keiner** dieser Tests ist hier ausgeführt.

## 7. Traceability-Ergänzung v0.7

| SRD | SRS | Design/Regel | Test |
|---|---|---|---|
| SYS-HND-001–004 | AN-HND-001–012 | Bildregister, UI, gemeinsamer Capture, Offscreen-PNG, Histogrammprovider | T-60…T-71 |

Für die 12 neuen SRS-Anforderungen gilt die unmittelbare Zuordnung `AN-HND-001 ↔ T-60` bis `AN-HND-012 ↔ T-71` gemäß Testtabelle in `06_Test_Validierung_v0.7.md`. Die 146+23 bisherigen CFG-IDs und die älteren 59 Tests bleiben erhalten.




---

# QUELLDOKUMENT: `docs/project/09_Entscheidungen_Offene_Punkte_v0.7.md`

# Entscheidungen, offene Punkte und Hardwaregates

**ID:** LFN-AN-OPEN · **Version:** 0.7 · **Stand:** 19.09.2026. „FIX“ bedeutet explizite Nutzerfestlegung; „VORSCHLAG“ wird bei Implementierung festgelegt und getestet, nicht still als garantiert behandelt.

## 1. Fixe Produktentscheidungen

| ID | Entscheidung |
|---|---|
| DEC-01 | Live-Analyzer als Unterpanel, keine eigene Audioaufnahme; andere bestehende Recording-Panels unverändert. |
| DEC-02 | Hauptbild minimal: Spektrum + Wasserfall bleiben **immer** sichtbar; Tap auf Wasserfall verbirgt nur untere Karten. |
| DEC-03 | Standard Frequenzachse linear, 0–100 Hz; Min/Max numerisch; 0–200 Hz optional. |
| DEC-04 | Mithörband dezent in beiden Diagrammen; horizontal verschiebbar; **Breite in Hz im Spektrumsetup**. |
| DEC-05 | Mithörfeld unten links: Tap AUS/EIN, vertikaler Swipe Nutzer-Gain; mit sicherer Headset-Route. |
| DEC-06 | Automatische Mithör-Abregelung **nur im Mithörzweig**, Input-Clip unabhängig. |
| DEC-07 | Rot = Average, Gelb = Live, Grau = optionale Referenz (initial AUS); Inferno muss vorhanden sein. |
| DEC-08 | Fadenkreuz mit horizontaler+vertikaler Linie, Frequenz-/Pegel-Readout, in Pause beweglich. |
| DEC-09 | Superfast + Maximum Sample einschließlich Reset; nicht als True Peak oder SPL etikettieren. |
| DEC-10 | Unabhängiges L50/L90-Band initial 2–25 Hz über 120 s, kurze horizontale Pegelstriche. |
| DEC-11 | `Save aktiv` = automatische PNGs ab 1. vollständigem Wasserfall, dann je Bereichszeit; CSV nur manuell. |
| DEC-12 | Benannte Setups mit Save/Recall; Mithören nach Recall immer AUS. |
| DEC-13 | Optionale Mikrofonkalibrierung und getrennte Noise-Referenz Messen/Speichern/Laden/Grau/Abzug, nur Anzeigeabzug. |

## 2. Vor Implementierung/Release noch zu validieren

| ID | Themenfeld | Sicherer Entwurfswert | Erforderlicher Nachweis |
|---|---|---|---|
| OPEN-01 | aktueller Repo-/Git-/Gradle-Stand | nicht behaupten | echte Quellcodeprüfung und Integration-Mapping |
| OPEN-02 | 8192 FFT / Decimations 3 | Startprofil | echte effektive Rate/Δf, Timing, CPU/Drop-Test |
| OPEN-03 | 2–25-Hz-Übertragungsfunktion | ohne SPL-Freigabe | validierter Mikrofonsignalpfad und ggf. externe Hardware |
| OPEN-04 | Average-Algorithmus | 120 s, zeitgewichtete lineare PSD | numerische Referenz-/Endpunkt-/Reset-Tests |
| OPEN-05 | Superfast | volle Rohsample-Peaks, 20-ms-Anzeigeaggregation | echtes Block-/Timestamp-Verhalten testen |
| OPEN-06 | Noise-Unsicherheitsgrenze | negative/kleine Reste ungültig | PSD-Normierungs- und Kompatibilitätstest |
| OPEN-07 | Filtertyp/Attack/Release | stabiler Butterworth; ≤20 ms / 3 s Entwurf | Ausgangsrisiko und Gerätesignalroute prüfen |
| OPEN-08 | Save bei Pause | aussetzen, nach Fortsetzen neu füllen | Test T-40; Produktänderung dokumentieren |
| OPEN-09 | Save nach Recall | tatsächlicher Zustand AUS | Datenschutz-/Produktentscheidung dokumentieren, Test T-41 |
| OPEN-10 | Gestenkollisionen | priorisierte Touch-Zonen/numerische Alternative | Hochformat-Usability-Test |
| OPEN-11 | UI-Bilddatei inkl. Metadaten | PNG plus Sidecar/Metadaten | tatsächliche Android-Medienzugriffe/Crash-Safe-Save |
| OPEN-12 | Bluetooth/USB-Ausgabe | bis validiert sichere Kopfhörerroute | Audio-Fokus/Routing/Headset-Abzugstest |
| OPEN-13 | CSV-Semantik | Roh- oder kalibrierte Momentwerte, kein Rauschbodenabzug | Einheiten/Referenz-/Masken-Test |
| OPEN-14 | Lizenz-/Assetprüfung | eigene UI-Assets; Spectroid nur Referenz | keine Fremd-Icons/Logos/Code ohne Rechtebasis |

Offene Punkte sind **keine** zusätzlichen impliziten Feature-Zusagen und dürfen nicht unter falschem PASS-Status geschlossen werden.


## 3. Neue Festlegungen und Integrationsgates v0.6

| ID | Status | Entscheidung / offenes Gate |
|---|---|---|
| DEC-14 | FIX | Kein vollständig isolierter Produktions-Clone zuerst: ein gemeinsamer Analyzer-Kern + kleinste Standalone-Testhülle, frühzeitige Integration in LFN-Scout. |
| DEC-15 | FIX | Live-Analyzer bleibt bei internem Panelwechsel aktiv; bei `Save aktiv` muss PNG-Zyklus vollständig weiterlaufen. |
| DEC-16 | FIX | Analyzer und bestehende Recorderfunktion dürfen nicht mit unkoordinierten konkurrierenden Mic-Clients arbeiten; ein gemeinsamer PCM-Capture ist Zielarchitektur. |
| DEC-17 | FIX | Letztes oder letzte vier **Radiushistogramme** untereinander am rechten Wasserfallrand, abschaltbar; gemeinsame Quelldaten, nicht UI-Screenshot der Histogramm-App. |
| DEC-18 | FIX | Automatische PNGs zeigen das Analyzer-Hauptbild auch dann, wenn ein anderes LFN-Scout-Panel geöffnet ist. |
| DEC-19 | FIX | Audioaufnahme bleibt Bestandsfunktion anderer Panels; `Save aktiv` erzeugt weiterhin nur PNG. |
| OPEN-15 | GATE | Aktuelle Audio-/Recorder-Klassen und Besitzerregeln der bestehenden App tatsächlich ermitteln. |
| OPEN-16 | GATE | Ist-Format/Rate/Channels/Codec und konkurrierende Bedarfshalter prüfen; zulässige Konfigurationspriorität bestätigen. |
| OPEN-17 | GATE | Radiushistogramm-Quelle: Bin-/Radiusdefinition, Einheiten, Zeit-/Ortsbezug, Persistenz, Publikationsintervall/Veraltet-Schwelle ermitteln. |
| OPEN-18 | GATE | PNG-Ziel (App-Ordner oder SAF), UI-Rendergröße, Dateiquota, Format/sidecar und tatsächliche Android-APIs festlegen. |
| OPEN-19 | GATE | Session-Verhalten bei anderem Panel, App-im-Hintergrund und Bildschirm aus getrennt mit Android-Target-SDK/Berechtigungen prüfen. |
| OPEN-20 | GATE | Zulässiger rechter Histo-Spaltenanteil auf dem realen Hochformat-Gerät und vollständige Achsenlesbarkeit validieren. |
| OPEN-21 | GATE | Standalone-Testhülle darf keinen zweiten unabhängigen Code-/Buildpfad erzeugen; identische Kernartefakte nachweisen. |

**Offen ≠ Feature verzichtbar:** Die Gate-Punkte dürfen erst nach echter Projekt-/Gerätesichtung geschlossen werden, während alle FIX-Entscheidungen verbindlich bleiben.

## 4. Abschlussprüfung der Übergabe v0.7

**Entschieden (FIX):** 13 vorhandene, nicht ausdrücklich verworfene UI-Bilder im Paket; davon `C13` letzte akzeptierte Mithören-EIN/AUS-Ansicht. Das Hauptbild muss lineare Frequenzachse, immer Spektrum+Wasserfall, separate Statistik-/Mithörbänder und eindeutiges `Save aktiv` haben. Zwei explizit verworfene Bilder sind nicht Bestandteil dieses Pakets. Das neu erstellte `D01`-Histogramm-Layout bleibt ein **vorläufiges Schema**.

**Offene, vor Coding zu schließende Gates:** Reale Produkt-ZIP/Commit; tatsächliche Mikrofon-Owner-/Recorder-Datenpfade; Histogramm-Radius-/Zeitsemantik und reale Miniaturdarstellung; präzise PCM-Queue- und CPU-/Memory-Grenzen; vollständige Device-Tests für 2–25 Hz, Kalibrierung, UNPROCESSED und Mithörausgabe; Plattform-Lifecycle für App-Hintergrund/Screen-off; visuelle UI-Abnahme auf 1–2 Hochformat-Androidgeräten. Diese Punkte dürfen nicht durch Bilder oder Testfixtures als erledigt erscheinen.

**Beabsichtigte Default-Konvention:** Keine Audioaufnahme im Analyzer. `Save aktiv` und tatsächliches Mithören nach Recall **AUS**, solange nicht ausdrücklich wieder gestartet. `L50/L90`-Band 2–25 Hz, 120 s; Wasserfallzeit 120 s; linear 0–100 Hz; Inferno vorhanden und als Startvorschlag; Bildlayout und tatsächliche Histogrammdaten im Repository validieren.




---

# QUELLDOKUMENT: `docs/project/10_Bedienung_Kurz_v0.7.md`

# Kurzbedienung / Bedienkonzept – LFN-Scout Analyzer

**ID:** LFN-AN-UM · **Version:** 0.7 · **Status:** geplante Bedienung, noch keine verfügbare App-Funktion.

1. In LFN-Scout das Unterpanel **„Spektrum & Mithören“** öffnen. Standard: lineare Anzeige 0–100 Hz; Gelb = live, Rot = optionales 120-s-Average. Unterhalb des Spektrums läuft der Inferno-Wasserfall.
2. `⚙` öffnet ein **schmales, scrollbares Einstellpanel**. Dort Quelle `UNPROCESSED` auswählen, falls unterstützt, und FFT/Decimations/Skalen/Cal und weitere Parameter einstellen. Keine Einstellungen annehmen, die das Gerät nicht unterstützt.
3. Das dezente Band im Spektrum/Wasserfall kennzeichnet nur **Mithören**. Horizontal verschieben = Bandmitte ändern. Band**breite [Hz]** steht unter **Spektrum**; numerische Grenzen zusätzlich unter Mithören. Das Statistikband 2–25 Hz ist **unabhängig**.
4. **Mithörfeld unten links kurz antippen:** AUS↔EIN. Hoch/Runter wischen verändert Nutzer-Gain. Nur auf kompatibler Kopfhörerroute, mit vorsichtiger Lautstärke. Monitor-Clip/AUTO-Warnungen beobachten; sie ändern keine Messwerte.
5. **Pause** friert Spektrum/Wasserfall ein; eingeschalteter Fadenkreuzcursor aus X-/Y-Linie bleibt verschiebbar und zeigt Frequenz/Pegel aus eingefrorenem Spektrum. Erneut Pause/Fortsetzen setzt die Anzeige fort.
6. **Wasserfall kurz antippen:** untere Zusatzkarten verschwinden und Wasserfall wird höher; Spektrum bleibt sichtbar. Nochmal tippen: Normalansicht.
7. Unter Statistik L50/L90 zuschalten (Standard 2–25 Hz / 120 s). Werte sind erst nach vollständigem Fenster gültig und bei unzureichender Bandkalibrierung nicht als absolute SPL interpretierbar.
8. Superfast/Maximum Sample kann kurze Eingangssample-Spitzen beobachten. Der ausgegebene Sample-Peak ist keine garantierte akustische Impulsmessung.
9. Kalibrierprofil bei Bedarf importieren/aktivieren; nur bei gültiger **absoluter** Referenz und validiertem Frequenzband darf SPL angezeigt werden. Rauschboden kann als unabhängige graue Referenz angezeigt oder optional rechnerisch nur in der Anzeige abgezogen werden.
10. `Save aktiv` nach Nutzeraktion einschalten: erstes PNG nach kompletter Wasserfallfüllung, danach je gleicher Wasserfallzeit. Manuelles PNG/CSV ist getrennt. **Es wird kein Audio aufgenommen.**
11. Unter Setups mehrere benannte Konfigurationen speichern, duplizieren, Recall/Laden und exportieren/importieren. Nach Recall bleiben Mithören und tatsächliche Bildautomatik zunächst AUS.

**Vor Einsatz:** Auf eigenem Gerät Mikrofonfrequenzgang, tatsächliche Sampling-Rate, 2–25 Hz, Clip-/Headset-Sicherheit und die korrekte Anzeigeeinheit prüfen.


## Neu: Parallelbetrieb und Radiushistogramme (v0.6)

1. **Save einschalten:** In `Save & Export` aktivieren; „Save aktiv“ zeigt nur **Bildspeicherung** an. Nach einer vollen Wasserfallzeit erscheint die erste PNG-Datei. Es wird kein Audio durch dieses Unterpanel aufgezeichnet.
2. **Anderes Panel öffnen:** Die Analyzer-Sitzung und automatische PNG-Speicherung laufen während des internen Panelwechsels weiter. Beim Zurückkehren erscheint die fortgeschriebene Anzeige, keine neu gestartete künstliche Historie.
3. **Bestehende Audioaufnahme zusätzlich starten:** Sie verwendet appintern denselben koordinierten Mikrofoneingang. Bei inkompatiblen Eingangsparametern erscheint eine deutliche Meldung; eine laufende Datei-Aufnahme wird nicht still umkonfiguriert.
4. **Radiushistogramme:** Unter `Anzeige & Wasserfall → Radiushistogramme` AUS / Letztes / Letzte 4 wählen. Bei genügend Bildschirmbreite erscheinen die neuesten vier Miniaturen **rechts übereinander**. Zeitstempel und Status helfen, ältere Daten nicht mit der aktuellen Akustik zu verwechseln.
5. **Erweiterte Ansicht:** Wasserfall antippen blendet weiterhin nur die unteren Zusatzkarten aus; Spektrum und Histo-Spalte (falls eingeblendet) bleiben sichtbar. Noch einmal tippen für Normalansicht.
6. **Anzeige-Pause:** Auto-PNG wird ausgesetzt und nach Fortsetzen erst nach neuer vollständiger Wasserfallzeit wieder aktiv. Pause stoppt nicht automatisch die bestehende Datei-Aufnahme und ist kein Panelwechsel.
7. **App im Hintergrund:** Für die erste Integration nicht pauschal zugesichert; Freigabestatus und Android-Berechtigungen beachten. Interner Wechsel in der geöffneten App ist davon unabhängig.

## Bedienergänzung v0.7

Das **untere linke Mithörfeld selbst** kurz antippen, um nur die Kopfhörerausgabe EIN/AUS zu schalten. Lautstärke im Feld vertikal wischen; das Mithörband in Spektrum oder freier Wasserfallfläche horizontal verschieben. Ein Tippen auf freie Wasserfallfläche blendet die unteren Zusatzanzeigen aus und erweitert **nur** den Wasserfall nach unten; das Spektrum bleibt bestehen. Nochmals tippen stellt die Normalansicht wieder her. Gelb = Live, Rot = Average, Grau = nur bei aktivierter Rauschbodenreferenz. Cursor-Fadenkreuz liest aktuelle oder ausgewählte Kurve, auch bei Pause. `Save aktiv` erzeugt zeitgestempelte PNGs, keine Audiodateien; beim internen Panelwechsel läuft es weiter. Der separate bestehende Recorder verwendet bei parallelem Betrieb die gemeinsame Audioquelle.




---

# QUELLDOKUMENT: `docs/project/11_CHANGELOG_Herkunft_v0.7.md`

# Änderungsprotokoll und Herkunft – LFN-Scout Analyzer

**Version 0.5 · 19.09.2026 · Status: Dokumentation erstellt, keine Implementierung**

1. SRD/SRS v0.3 inhaltlich mit späteren Festlegungen und 146 CFG-IDs aus v0.4 zusammengeführt; historische Dateien unverändert im Archiv.
2. Verbindliches Hauptdisplay im Hochformat mit stets sichtbarem Spektrum und Wasserfall sowie separater schmaler Einstellansicht dokumentiert.
3. UI-/Gestenzustände für Mithörfeld-Tap/vertikales Wischen, Band-X-Drag, Fadenkreuz und Wasserfall-Expansion präzisiert.
4. Gelbe Live-, rote Average- und graue optionale Referenzkurve samt getrennten Ein/Aus-Zuständen aufgenommen.
5. Eigenständiges L50/L90-Statistikband (Default 2–25 Hz, 120 s), Superfast/Maximum Sample und PNG-Autosave spezifiziert.
6. Veraltete Formulierung „PNG ausschließlich manuell“ ersetzt: `Save aktiv` erzeugt PNG nach vollständiger Füllung und danach je Wasserfallzeit; CSV bleibt manuell; **keine Audiodateien**.
7. Mikrofonkalibrierung, Rauschbodenreferenz und optionale Anzeige-PSD-Subtraktion mit Gültigkeits- und Unsicherheitsbedingungen ausgearbeitet.
8. Erstmals SDS, UI-Konzept, Algorithmen/Kalibrierung, Testplan, Traceability, offene Punkte und Integrationsanweisung vollständig zusammengeführt.
9. Maschinell lesbare **illustrative** JSON-Beispiele für Setup, Mic-Cal und Noise-Referenz beigefügt; keine realen Messkalibrierdaten.
10. Vollständige aktuelle LFN-Scout-Quellcodeprüfung/Build ist **nicht** Bestandteil dieses Dokuments; alle vorgeschlagenen Klassennamen sind Entwurfsrollen.

**Quellenhistorie:** `references/legacy/SRD_v0.3.md`, `references/legacy/SRS_v0.3.md`, `references/Funktionsreferenz_v0.4.md`; Spectroid-Arbeitsanleitung v10.5 und benutzerseitige Screenshots, UI-Entwurfsbilder als **nicht bindende** Illustration. Die ausdrücklich verworfene Spectroid-Anleitung v10.4 wird nicht verwendet.

## Vor künftiger Änderung

Anforderungs-ID → Design → Code → PASS/FAIL-Test → Dokumentationsänderung nachführen; neu behobene Bugs mit dauerhaften Regressionstests sichern. Das Projekt-`CHANGELOG.md` erst nach tatsächlicher Codeänderung umsetzen, nicht aufgrund dieser Dokumentausgabe einen Software-Release behaupten.

## v0.6 – 19.09.2026 (Erweiterung nach Integrationsentscheidung)

- Einheitlicher Analyzer-Kern mit kleiner Standalone-Testhülle statt erst eigenständiger Produktions-Clone.
- Zentraler gemeinsamer Mic-Capture mit unveränderlichem PCM-Fan-out zu Analyzer und der bereits bestehenden LFN-Scout-Audioaufnahme.
- Analyzer-Session überlebt internen Panelwechsel; Renderer nur sichtbar aktiv, Offscreen-PNG-Renderer unabhängig vom sichtbaren Panel.
- Automatische PNGs bei `Save aktiv` weiterhin erst nach vollständigem Wasserfall und danach je Wasserfallzeit, auch unter anderem LFN-Scout-Panel. Keine WAV-/PCM-Aufnahme aus dem Analyzer.
- Optionale Radiushistogramme AUS/letztes/letzte vier rechts untereinander im Wasserfall, mit Quelle/Zeiten/Fehlstatus.
- Neue Anforderungen, zusätzliche Parameter und Testfälle T-44…T-59, ausführliches Repo-Mapping-Gate, Datenvertrags- und Testhost-Dokument.
- Alle bisherigen Funktionen (v0.5) samt ursprünglicher 146-Parameterreferenz erhalten.
- **Noch nicht durchgeführt:** echter Repo-Abgleich, App-Code, Build, Hardwaretests, Test-PASS.

## v0.7 – 19.09.2026 · Abschluss-Audit / vollständig bebilderte Übergabe

- Alle **13 vorhandenen nicht ausdrücklich verworfenen UI-Bilddateien** beigefügt, jeweils mit eindeutiger Inventar-ID, SHA-256, Freigabe-/Historienstatus und konkreter Verwendung. Zwei ausdrücklich abgelehnte Entwürfe konsequent ausgeschlossen. Drei breite/frühe Layouts nicht als verbindliche Hochformat-Vorlage geführt.
- Veralteten Asset-Verweis auf `04_UI_Gesten_v0.5.md` korrigiert. Separate bildbezogene normative Prüfungen, Hinweis auf Bildfehler und die neu schematisierte rechte Histo-Spalte aufgenommen.
- Neue Anforderungen `SYS-HND-001…004`, `AN-HND-001…012`, Tests `T-60…T-71`, aktualisiertes Repo-Gate, Dokumenten-Audit und konsistente Übergabeordnung.
- Kein Quellcode geändert, keine Android-APK gebaut, keine Hardware-/Mikrofontests als bestanden behauptet.




---

# QUELLDOKUMENT: `docs/project/12_Quellen_Referenzen_v0.7.md`

# Quellen und technische Referenzen – Analyzer v0.6

## 1. Nutzervorgaben / mitgelieferte Arbeitsgrundlagen

- Spectroid-Anleitung: `../../references/Spectroid_Anleitung_v10_5.docx` (funktionale Arbeitsreferenz; nicht als verbindliche App-Codebasis verwenden).
- Bereitgestellte Original-Screenshots: `../../references/Spectroid-Screens.zip` (Funktionen und Menübenennungen nachvollziehen; keine fremden UI-Assets übernehmen).
- SRD/SRS v0.3 im historischen Archiv `../../references/legacy/`; v0.4-Parameterreferenz unter `../../references/Funktionsreferenz_v0.4.md`.
- Aktuelle Nutzerfestlegungen vom 19. September 2026: Hauptbild, Gesten, Statistik, Fadenkreuz, Superfast, Noise, Save etc.; als v0.6-Anforderungen verarbeitet.

## 2. Offizielle Android-Dokumentation (bei Implementierung aktuelle API prüfen)

- [AudioRecord – Android API](https://developer.android.com/reference/android/media/AudioRecord): flüchtigen Mic-PCM-Stream lesen, ohne eine Audiodatei zu schreiben.
- [AudioTrack – Android API](https://developer.android.com/reference/android/media/AudioTrack): PCM-Ausgabe im separaten Mithörzweig.
- [MediaRecorder.AudioSource – UNPROCESSED](https://developer.android.com/reference/android/media/MediaRecorder.AudioSource): Quelle prüfen; keine garantierte Hardwarelinearität bei 2 Hz ableiten.
- [AudioManager / AudioDeviceInfo](https://developer.android.com/reference/android/media/AudioManager): Audio-Routing und Geräteinformationen, vor tatsächlichem Release prüfen.
- [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider): benutzergesteuerter Dateizugriff für Profile/Export.
- [Foreground services / microphone](https://developer.android.com/develop/background-work/services/fgs/service-types): nur relevant, falls später explizit Hintergrundbetrieb spezifiziert wird; **nicht Teil dieses Entwurfs**.

**Hinweis:** Dies sind inspectierbare technische Links, keine Bestätigung, dass die API-Verfügbarkeit oder der aktuelle App-Quellcode bereits geprüft wurde. Performance, Frequenzgang und Clipping müssen am realen Gerät getestet werden.


## Ergänzung v0.6: Android-Plattformreferenzen (am 19.09.2026 geprüft)

1. Android Developers – *Sharing audio input*. Beschreibt Priorisierung/Mute-Szenarien konkurrierender Eingabe-Clients; begründet den koordinierten appinternen Capture/Fan-out. https://developer.android.com/media/platform/sharing-audio-input
2. Android Developers – *Restrictions on starting a foreground service from the background*. Beschreibt Start- und While-in-use-Permission-Beschränkungen für Mikrofon-FGS. https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
3. Android Developers – *Foreground service types*. Microphone-Typ, notwendige Berechtigungen und Voraussetzungen je Target-SDK. https://developer.android.com/develop/background-work/services/fgs/service-types
4. Android Developers – *Declare foreground services and request permissions*. Manifest-/Typanforderungen, vor einem optionalen Hintergrundmodus prüfen. https://developer.android.com/develop/background-work/services/fgs/declare

Diese Quellen belegen Plattformgrenzen, **nicht** die tatsächliche Architektur des aktuellen privaten LFN-Scout-Repositories. Sie ersetzen nicht die geforderte Repo-Inspektion.



---

# QUELLDOKUMENT: `docs/project/13_Parameter_Ergaenzungen_v0.7.md`

# Ergänzende Einstellungen und Defaults v0.6 – Live-Betrieb, Histo, gemeinsame Audioquelle

**ID:** LFN-AN-PAR-06 · **Version:** 0.7 · **Status:** Soll-Parameter, noch nicht implementiert.  
**Vollständigkeit:** Die 146 bestehenden `CFG-*`-Einträge aus `references/Funktionsreferenz_v0.4.md` bleiben **unverändert normativ**. Die folgenden neu hinzugekommenen Einstellungen/Statusfelder ergänzen sie; hier keine Dublette der alten Listen.

| ID | Gruppe | Einstellparameter / Status | Datentyp / Wertebereich | Default | Schalter? | Verbindlichkeit / Hinweis |
|---|---|---|---|---|---|---|
| CFG-HI-001 | Anzeige & Wasserfall → Radiushistogramme | Histo-Anzeige | `AUS`, `LETZTES`, `LETZTE_4` | `LETZTE_4` | Ja | Anzahl 4 FIX, Startauswahl VORSCHLAG |
| CFG-HI-002 | Anzeige & Wasserfall → Radiushistogramme | Spaltenbreite | Anteil 0,15…0,30 (15–30 %), geräteabhängig | 0,20 | Ja über Histo-Modus | VORSCHLAG, niemals Frequenzachse verfälschen |
| CFG-HI-003 | Anzeige & Wasserfall → Radiushistogramme | Chronologische Reihenfolge | `NEUESTE_OBEN` | `NEUESTE_OBEN` | Nein | FIX |
| CFG-HI-004 | Anzeige & Wasserfall → Radiushistogramme | Zeitpunkt zeigen | Boolean | Ein | Ja | FIX, im Hochformat lesbar |
| CFG-HI-005 | Anzeige & Wasserfall → Radiushistogramme | Veraltet-Schwelle [s] | positive Zahl; erst nach Ist-Intervall festlegen | `AUTO_FROM_SOURCE` | Ja | OFFEN bis Repo-Mapping |
| CFG-HI-006 | Anzeige & Wasserfall → Radiushistogramme | Miniatur öffnen | Aktion / falls navigierbar | deaktiviert bis Mapping | Gerät | SOLL; niemals falsche ID öffnen |
| CFG-HI-007 | Anzeige & Wasserfall → Radiushistogramme | EffectiveVisibleCount | Integer 0…4 | 0 bis Daten vorhanden | Status | Laufzeitzustand, nicht als Inhalt eines Setups persistieren |
| CFG-LF-001 | Analyzer-Betrieb | Bei Wechsel zu anderem internen Panel fortsetzen | Boolean | Ein | Nein bei aktiver Sitzung | FIX, **keine** optionale Abschaltung während `Save aktiv` |
| CFG-LF-002 | Analyzer-Betrieb | Panel-Renderfrequenz bei Unsichtbarkeit | 0 Hz | 0 | Nein | Fixe Lastregel; Offscreen-Render bei Save separat |
| CFG-LF-003 | Analyzer-Betrieb | Session aktiv | Boolean | Aus bis expliziter Start | Status | Nicht Setup-Wert; `Save aktiv` hält laufende Session |
| CFG-LF-004 | Analyzer-Betrieb | Android-App-Hintergrundbetrieb | `NICHT_ZUGESICHERT`, später freigegebene Variante | `NICHT_ZUGESICHERT` | Nein | Nicht mit internem Panelwechsel verwechseln |
| CFG-AU-001 | Audio → gemeinsame Quelle | Capture-Owner-Policy | `SINGLE_SHARED_CAPTURE` | Gleich | Nein | FIX; kein unkoordiniertes zweites AudioRecord |
| CFG-AU-002 | Audio → gemeinsame Quelle | Während laufender Aufnahme Analyzer-Quellenwechsel | `REJECT_OR_ANALYZER_ONLY_ADAPT` | Gleich | Nein | FIX; bestehende Recorder-Semantik schützen |
| CFG-AU-003 | Audio → Diagnose | aktive PCM-Abnehmer | Zähler 0…n | 0 | Status | Run-Time, kein Setup |
| CFG-AU-004 | Audio → Diagnose | Capture-Route/effektive Rate | String / Hz | von Hardware | Status | Tatsächlichen Wert anzeigen |
| CFG-SV-001 | Save & Export | Offscreen-Renderer auch bei anderem Panel | Boolean | Ein bei `Save aktiv` | Nein | FIX |
| CFG-SV-002 | Save & Export | Snapshot-Histogramme einbeziehen | Boolean | Ein, wenn Histo-Anzeige aktiv | Ja | FIX Layoutübereinstimmung; Auswahl im Setup |
| CFG-SV-003 | Save & Export | PNG-Ausgabeauflösung | feste Pixel oder aktuelle verifizierte Viewport-Größe | `MATCH_ANALYZER_LAYOUT` | VORSCHLAG | Nicht fremden sichtbaren View fotografieren |
| CFG-SV-004 | Save & Export | max. Pending-Render-/Writer-Aufträge | Integer 1…5 | 2 | Ja/Erweitert | VORSCHLAG; Overload sichtbar melden |
| CFG-SV-005 | Save & Export → Diagnose | letzter PNG-Status | `IDLE/WRITING/OK/ERROR/SKIPPED` | IDLE | Status | Bei Save aktiv global sichtbar machen |
| CFG-SV-006 | Save & Export → Diagnose | Anzahl übersprungener Zyklen | Integer >=0 | 0 | Status | Runtime, nach Fehler nie schweigend vergessen |
| CFG-TST-001 | Erweitert & Diagnose | Test-Host-Modus | `FAKE_PCM`, `LIVE_ADAPTER` | `FAKE_PCM` nur Testhülle | Test | Keine Produktions-App-Einstellung |
| CFG-TST-002 | Erweitert & Diagnose | Histogramm-Testfixture | benannte Fixture-ID | NONE | Test | Kein Datenimport in produktive Histogramme |

**Persistenz:** Nur als normale Setup-Werte gekennzeichnete Nutzerpräferenzen werden gespeichert. Reale Mic-Leases, Laufzeitstatus, Histogramm-Snapshot-Payload, PNG-Queue und fremde Aufnahme-Zustände **nicht** ins Analyzer-Setup schreiben. `Save aktiv` und Mithören bleiben nach Recall/Neustart AUS; ein Profil darf die gewünschte Save-Präferenz, aber keinen aktivierten Laufzeitstatus enthalten.

**Priorität:** Bei Konflikten zwischen früherer Parameterbeschreibung und expliziter v0.6-Sitzungs-/Histogrammregel gilt v0.6 für die betroffenen Funktionen; die 146 Originalzeilen bleiben als separate historische und normative Basis im Paket. Freigabewerte `AUTO_FROM_SOURCE` und `MATCH_ANALYZER_LAYOUT` sind deklarative Default-Strategien, **keine** erfundenen Messwerte.

## 2. Parameter-Audit v0.7

Die bestehenden **146 + 23 = 169** CFG-Einträge bleiben vollständig bestehen. Im v0.7-Audit ergeben sich **keine zusätzlichen konfigurierbaren Laufzeitparameter**: Die neuen Übergaberegeln betreffen Asset-Freigabe, Architekturverifikation, PCM-Datenbesitz, Snapshotkonsistenz, Fehlerbehandlung und Tests. Neue Laufzeit-Zustände dürfen nicht als Setup-Werte persistiert werden, bevor entsprechende SRS-/Schema-/Test-Erweiterungen beschlossen sind.




---

# QUELLDOKUMENT: `docs/project/14_Histogramm_Integration_v0.7.md`

# Radiushistogramme – Datenvertrag, Anzeige und Zeitzuordnung

**ID:** LFN-AN-HIST · **Version:** 0.7 · **Status:** Integrationsentwurf bis zur Sichtung des tatsächlichen LFN-Scout-Histogrammmoduls.

## 1. Zweck und Bestandsschutz

Im Analyzer erscheinen wahlweise **kein**, **das letzte** oder **die letzten vier** bereits vorhandenen Radiushistogramme. Die letzten vier stehen untereinander am rechten Wasserfallrand. Der Analyzer **berechnet keine neue Radiusstatistik** und mutiert keine Histogramm-Persistenz. Inhalt, semantische Radiusdefinition, Einheiten, Zeitbezug, Herkunft und Veröffentlichungsintervall werden **aus dem aktuellen LFN-Scout-Quellcode** übernommen, nicht erraten.

## 2. Provider-Vertrag (semantisch)

```text
HistogramProvider.latestComplete(maxCount = 4): Immutable<List<HistogramSnapshot>>
HistogramProvider.observeCompleted(): Flow<HistogramSnapshot>
HistogramProvider.byId(snapshotId): HistogramSnapshot?   // optional für Navigation
```

`HistogramSnapshot` enthält: stabile `snapshotId`; `sourceVersion`; `createdAtUtc`; `windowStartUtc/windowEndUtc` **wenn von der Quelle vorhanden**; `binEdges`/`binCenters` in **Quell-Einheit**; `binValues` inkl. Basis/Einheit; Qualitätsflags; `sourceLabel`; gegebenenfalls vorhandene, datenschutzverträgliche Orts-/Kategorienreferenz. Welche Felder in der Praxis verfügbar sind, wird bei Mapping bestätigt. Fehlt eine Größe, wird sie im Renderer nicht erfunden. Der gezeigte JSON-Schemaentwurf ist illustrativ und muss an die echte Quelle angepasst werden.

## 3. Reihenfolge und Snapshot-Gültigkeit

- Nur **abgeschlossene** Histogramme übernehmen. Doppelte ID in Subscription ersetzt dieselbe Version oder wird idempotent ignoriert.
- Neueste vollständige Version oben, danach bis zu drei ältere. Bei weniger als vier Datensätzen die fehlenden Plätze nicht durch Kopien auffüllen.
- Pro Histo: Zeitpunkt, Status `AKTUELL/VERALTET/UNBEKANNT/FEHLT` anhand echter Quell-Kadenz; wenn keine Kadenz bekannt, lieber neutrales `Zeit: ...` statt eine feste „veraltet“-Schwelle zu behaupten.
- Quell-Zeitstempel und Analyzer-PCM-Monotonic-Zeit sind unterschiedliche Zeitdomänen. Für PNG `snapshotCaptureEndUtc`, `histogramCreatedAtUtc` und optional die Quellzeitspanne ausweisen; **keine Gleichzeitigkeit behaupten**, wenn Intervalle nicht überlappen.

## 4. Layoutregeln für Hochformat

1. Spektrum oben bleibt **vollständig**. Wasserfall unten wird in zwei nebeneinander liegende Bereiche aufgeteilt: Plot links; Mini-Histogramme rechts.
2. Histo-Spalte Startwert 20 % der verfügbaren Wasserfallbreite; bei zu geringem restlichem Platz nach einer festgelegten Mindestbreite automatisch verbergen (Status-Hinweis), niemals Hz-Zuordnung verzerren.
3. `LETZTE_4`: vertikale vier Panels gleicher Höhe, mit abgekürztem Datum/Uhrzeit und optional Einheit. `LETZTES`: ein Panel oben oder vertikal zentriert (bei Implementierung konsistent entscheiden).
4. Wasserfall-Panning/Band-Drag und kurzer Tap werden nur im Plotbereich erkannt. In Histo-Spalte keine Mithörband-Geste. Erweiterte Ansicht blendet nur untere Zusatzkarten aus.
5. Dieselbe Layoutfunktion erzeugt sichtbares Hauptbild **und** Offscreen-PNG. Histogramm-Payload wird vor Rendereraufruf unveränderlich kopiert; keine Live-Neulese mitten im Export.

## 5. Fehler und Datenschutz

Quelle nicht verfügbar → „Keine Histogrammdaten“ und weiterhin lauffähiger Analyzer. Inkompatibles Bin-Schema → Fehler mit Quellrevision statt interpolierter Kunstkurve. Orts-/Personenbezug und Upload-Regeln ausschließlich nach Bestandsprojekt; PNG-Speicherort darf keine ungeprüfte Veröffentlichung auslösen. Alter der Histogramme im PNG nicht verstecken.

## 6. Testfixtures

`HIST_5_EVENTS`: fünf abgeschlossene Daten (IDs 1…5) + Duplicate ID3; erwartet 5/4/3/2. `HIST_MISSING`: nur 1; zeigt ein gültiges Histo und leere Plätze. `HIST_OLD`: alte Quellzeit; zeigt Alter. `HIST_SCHEMA_MISMATCH`: Fehlerzustand, akustische Engine weiterhin funktionsfähig. Tests T-55…T-58.

## 7. Reale Repo-Mapping-Pflichten

| Zu prüfen | Ist-Stand | Entscheidung |
|---|---|---|
| Histogramm-Publisher / DB-Tabelle / Besitzer | OFFEN | tatsächliche Klasse/Datei nennen |
| Radiusdefinition und X-/Y-Einheit | OFFEN | aus bestehenden Code-/Projektdokumenten übernehmen |
| Erzeugungskadenz / letzter kompletter Datensatz | OFFEN | für Stale-Regel erfassen |
| Datums-/UTC-/lokale Zeitdarstellung | OFFEN | echte Formate übernehmen |
| Navigation zu einzelnen Histos | OFFEN | nur wenn vorhandenes Panel ID-spezifisch öffnen kann |
| Berechtigung / lokale oder Remote-Daten | OFFEN | keine ungewollten Netzrequests bei Analyzer-Frame |

## 8. Bild- und Datenstand v0.7

Die vorhandenen UI-Konzepte bilden die finale rechte Histogrammspalte **noch nicht** ab. `../../assets/diagrams/D01_Histogrammspalte_SOLL_Schema.png` ist ein neu erstelltes **vorläufiges Layoutschema**, kein freigegebenes Original-UI-Bild. Die Linien darin sind rein grafische Platzhalter. Vor einer Implementierung müssen Radiusachsen, Einheiten, Werte- und Zeitbasis realer LFN-Scout-Histogramme sowie die Platzverhältnisse auf dem Referenzgerät gesichtet und bestätigt werden. Bei nicht verfügbaren Daten keine Simulation anzeigen.




---

# QUELLDOKUMENT: `docs/project/15_PNG_Hintergrund_Panelwechsel_v0.7.md`

# Auto-PNG beim Panelwechsel – Betriebs- und Fehlervertrag

**ID:** LFN-AN-PNG · **Version:** 0.7 · **Status:** verbindlicher Funktionsentwurf; Android-Implementierung/HW-Test ausstehend.

## 1. Unterschiedliche Zustände

| Ereignis | Capture / Analyzer | Sichtbares Renderer-UI | Auto-PNG | Vorhandener Recorder |
|---|---|---|---|---|
| Analyzer sichtbar, Live | läuft | Spectrum + Waterfall | falls Save EIN | unabhängig |
| Innerhalb LFN-Scout anderes Panel | läuft bei aktiver Analyzer-Session | Analyzer-UI zeichnet **nicht** fortlaufend | **läuft weiter**, offscreen | unabhängig |
| Anzeige-Pause explizit | PCM-Verteilung läuft, Anzeige eingefroren | Cursor auf Freeze beweglich | **ausgesetzt**, Resume füllt neu | läuft, falls aktiv |
| Mithören AUS | Analyse läuft | gleich | gleich | unabhängig |
| Save AUS | Session nach Nutzerstart-/App-Policy | gleich | OFF | unabhängig |
| App im Hintergrund / Bildschirm aus | **separates nicht automatisch freigegebenes Feature** | keine sichtbare UI | nur nach Dienst-/Rechtefreigabe | Bestand prüfen |
| Mic-/Routenfehler | Ungültig/Fehler | Fehlerstatus | Zyklus invalidieren, kein gültiges PNG | Fehler/Warnung separat |

Ein Panelwechsel ist **nie** gleich Anzeige-Pause, Audio-Stopp oder Nutzer-Abwahl von Save.

## 2. Abstrakter Zustandsautomat

```text
SAVE_OFF --explizit EIN--> SAVE_FILLING [t0, gültige Wasserfallzeilen]
SAVE_FILLING --vollständige T_wf--> SAVE_ACTIVE [enqueue png #1]
SAVE_ACTIVE --jede neue volle T_wf--> SAVE_ACTIVE [enqueue png #n]
SAVE_FILLING/ACTIVE --UI-Panelwechsel--> UNVERÄNDERT
SAVE_FILLING/ACTIVE --Anzeige-Pause--> SAVE_SUSPENDED
SAVE_SUSPENDED --Resume--> SAVE_FILLING [neuer t0]
SAVE_* --Eingangsfehler / Zeitlücke--> SAVE_INVALID [Status; Refill]
SAVE_* --explizit AUS--> SAVE_OFF
```

`Save aktiv` im Header zeigt den tatsächlichen Session-Zustand; andere Panels sollen bei aktivem Save mindestens einen kompakten globalen Status oder eine über die Navigation erkennbare Kennzeichnung bieten. Nach Recall/Neustart ist der tatsächliche Zustand OFF.

## 3. PNG-Transaktion

1. T_wf beginnt beim aktivierten Save oder der notwendigen Neufüllung. Maßgeblich ist die monotone Capturezeit, nicht UI-Paint oder Dateisystemzeit.
2. `AnalyzerFrame` enthält kohärenten Spektrums-/Wasserfall-/Statistik-/Monitorstatus mit Gültigkeitsmaske und Capture-Intervall.
3. `HistogramProvider.latestComplete(4)` liefert zu `t_end` die letzte/n verfügbare/n Histogramme einschließlich *eigener* Zeitinformation. Eventuell vorhandene Histos dürfen älter sein.
4. Ein Offscreen-Renderer verwendet exakt dieselben Layoutregeln wie die sichtbare Analyzer-Ansicht, inklusive Histogramm-Modus und Erweiterungszustand; er greift nicht auf den aktuell sichtbaren View zu.
5. PNG und Metadaten werden asynchron geschrieben; Dateiname eindeutige ISO-Zeit + Sequenz + Setup-ID-Kurzkennung. Beim Zielsystem ohne atomaren Rename muss der Fehlerpfad explizit behandelt werden.
6. Erfolgreich erst nach bestätigter Persistenz; bei Queue-Überlauf oder Speicherfehler `SKIPPED`/`FAILED` zählen. **Kein mehrfaches Nachholen identischer Frames**, kein PCM-Callback-Busywait.

## 4. Speicherplan und Dateiquota

Speicherort per SAF oder unterstütztem App-Dateiziel auswählbar. Ohne Schreibrecht `Save aktiv` nicht still fortsetzen. `maxPendingPngJobs=2` ist **VORSCHLAG**; überschreitet der Durchsatz das eingestellte Intervall, wird die aktuelle Periode als ausgelassen protokolliert. Eine Dateiquota/Retentionspolitik ist vor Release geräte- und Nutzungsabhängig festzulegen; **keine stillen Löschungen** vorhandener Nutzerdaten. Ohne aktives Speichermedium weiter live messen und Fehler anzeigen.

## 5. Beispiel bei T_wf = 120 s

| Monotone Zeit seit Save EIN | Erwartung | Panel |
|---|---|---|
| t=0 | 0 PNG | Analyzer |
| t=30 | 0 PNG | Wechsel zu anderem Panel |
| t=120 | PNG #1 | anderes Panel |
| t=240 | PNG #2 | anderes Panel |
| t=360 | PNG #3 | anderes Panel |
| t=370 | 3 PNG vorhanden; aktueller Wasserfall zeigt neue Zeilen nach Rückkehr | Analyzer |

**Keine Audioaufzeichnung durch `Save aktiv`.** T-49, T-50, T-53 und T-54 sind zwingende Integrations-/Gerätetests. 

## 6. PNG-Bildinhalt und temporale Konsistenz v0.7

Das PNG muss beim Export aus der Laufzeit-Render-Snapshotstruktur entstehen und die gleiche Spektrum-/Wasserfallkonfiguration wie die sichtbare normale bzw. definierte Save-Ansicht dokumentieren. Wenn der Analyzer nicht sichtbar ist, darf das **gerade sichtbare fremde Panel** nicht abfotografiert werden. Anzuzeigende Histogramme stammen aus demselben gesicherten Snapshot oder tragen eindeutig ihren eigenen Zeitstempel und Alter-/Fehlstatus. Bei fehlenden Histogrammen wird kein Inhalt erfunden. Nur tatsächliches `Save aktiv` löst periodische PNGs aus; eine getrennte bestehende Audioaufnahme hat keinen Einfluss auf den PNG-Zeitplan.




---

# QUELLDOKUMENT: `docs/project/16_Repo_Mapping_und_Gates_v0.7.md`

# Vor-Implementierungs-Gate: Ist-Architektur und Mapping-Formular

**ID:** LFN-AN-MAP · **Version:** 0.7 · **Status:** **OFFEN** bis die aktuelle vollständige LFN-Scout-Projekt-ZIP bzw. Git-Commit untersucht wurde. Keine erfundenen Klassen-/Dateinamen übernehmen.

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

# QUELLDOKUMENT: `docs/project/17_Standalone_Testhost_v0.7.md`

# Standalone-Testhülle – gleicher Analyzer-Kern, keine separate Produktentwicklung

**ID:** LFN-AN-TESTHOST · **Version:** 0.7 · **Status:** Architektur-/Arbeitsplan.

## Ziel

Vor komplexen UI- und Bestandsintegrationsänderungen eine **kleine, unabhängig startbare Android-Testhülle** aufbauen. Sie testet FFT, Superfast, Kalibrierung, L50/L90, Monitoring, Wasserfall, Save und simulierte Radiushistogramme mit synthetischen Quellen. Sie ist bewusst **kein fertiger unabhängiger Spectroid-Clone**, der später vollständig transplantiert wird.

## Geteilter Code / getrennte Adapter

```text
shared analyzer-core (identische Quellen + Tests)
 ├─ Capture/Measurement/Statistics/Monitor/Render/Save/Setup (interfaces)
 ├─ Android TestHost: FakePCM/FakeHistogram/FakeClock, optionale echte Mic-Route
 └─ LFN-Scout Host: SharedCapture/ExistingRecorder/RealHistogram/PanelHost
```

Für beiden Hosts identische DSP-Testvektoren, `AnalyzerFrame`-Schema, Snapshot-Ausgabe und Parametervalidierung. Testhost-spezifisch nur Dependency Injection, einfache Aktivitätsnavigation und Fake-Quellen. Keine zweite produktive Nutzer-/Cloud-Datenbank oder Audio-Dateiaufzeichnung.

## Mindest-Testhülle

1. Start/Pause/Fortsetzen und Live-/Fake-Umschaltung; Status `Fake` deutlich sichtbar.
2. Synthetic 47-Hz-Sinus, Transienten, PCM-Clip, Noise-Referenz, Bandpass und Verstärkungsprüfung.
3. FakeClock für `T_wf=120 s`; Offscreen-PNG auch bei unsichtbarer Test-Activity prüfen.
4. FakeHistogram-Snapshots mit IDs/Zeit und Modi AUS/1/4.
5. Automatisierte Unit-/Instrumented-Tests und exportierbare PASS/FAIL-Protokolle.
6. Frühzeitige Integration in LFN-Scout nach der Kernel-Validierung; **echte Recorder- und Panelwechseltests sind erst dort vollständig möglich**.

## Abgrenzung und Nachweis

Ein Testhost-PASS ist kein LFN-Scout-Release. Test T-59 vergleicht identische Fixtures in beiden Hosts; Freigabe benötigt T-44…58 inklusive realem Recorder. Kein unabhängiger Release-Zweig, kein stiller Umbau der Bestandsarchitektur nach erfolgreichem isoliertem Prototyp.

## 3. Abschluss-Gate v0.7

Die Standalone-Testhülle ist ausdrücklich ein **Debug-/Test-Host desselben Analyzer-Kerns**, keine zusätzliche App mit separaten Einstellungen, Quellen oder langjähriger Parallelpflege. Sie muss synthetische PCM-Daten und eine lokale Mikrofonroute unterstützen. Gemeinsamer PCM-Owner, Bestands-Recorder, offscreen PNG unter internem Panelwechsel und echte Radius-Histogramme werden zusätzlich in der integrierten LFN-Scout-App geprüft; ein alleiniger Testhost-PASS genügt für die Produktionsfreigabe nicht.




---

# QUELLDOKUMENT: `docs/project/18_ABSCHLUSS_AUDIT_v0.7.md`

# Abschluss-Audit und verbliebene Integrationsgates – v0.7

**Ziel:** Nachvollziehbare letzte Prüfung des gesamten Übergabepakets. Dies ist eine **fachliche Dokumentenprüfung**, keine reale App-/Gerätevalidierung.

## 1. Gefundene Lücken und Behandlung

| Punkt | Befund v0.6 | Behandlung v0.7 |
|---|---|---|
| Bilder | 2 statt aller vorhandenen 13 nicht ausdrücklich verworfenen UI-Entwürfe; Bildstatus fehlte. | 13 Bilder samt Einzel-Status, SHA-256, Links; 2 ausdrücklich verworfene aus ZIP ausgeschlossen. |
| Alte Image-README | Verwies fälschlich auf v0.5-UI als verbindlich. | Neues Bildregister, normative Verweise überall auf v0.7 und Textvorrang. |
| Histo-Bild | Rechter Rand noch nicht in einem bereitgestellten finalen UI-Bild gezeigt. | Eigenständiges **vorläufiges** `D01`-Schema; echtes Daten-/Layout-Mapping bleibt Gate. |
| Capture/Recorder | Zielarchitektur vorhanden, reale App-Klassen/Owner unbekannt. | Expliziter Konflikt-/Ownership-Vertrag und Test T-62, kein zweites unkoordiniertes Mikrofon. |
| Save/Lifecycle | PNG bei anderem internem Panel spezifiziert, echte Android-Hintergrundgrenzen offen. | Separater Test T-63 und Zustandsvertrag: internes Panel != App-Hintergrund != Screen-off. |
| Bildfehler | KI-Mockups enthalten gelegentlich falsche Frequenz-/Zeitachsen, Zusatzbuttons, Statuswörter, Messzahlen. | Nur Designillustrationen, konkrete Textregeln und Abnahmetest T-60/T-69. |
| Messqualität | Frequenzbereich 2–25 Hz und SPL nicht für alle Telefon-Mikrofone validiert. | Qualitätsflags und Gerätemessgate; keine falschen SPL-/Infraschall-Garantien. |
| Superfast | 8× kann als vermeintlich höhere Mikrofon-Abtastrate fehlinterpretiert werden. | Festlegung: nur tatsächliches PCM und validierte Analyse-/Anzeigevariante; Test T-65. |
| Referenzabzug | Gefahr, Rauschboden mit Kalibrierung oder L50/L90-Rohwerten zu vermischen. | Darstellungskorrektur separat; normativer unveränderter Mess-/Mithör-/Recorderpfad. |
| Eindeutige Übergabe | v0.5-/v0.6-Text und Bildversionen verwirrend. | v0.7 Vorrang, Index, kopierfertiger Auftrag, SHA-Manifest und konsolidiertes Master-MD. |

## 2. MUST-PASS vor Entwicklung

1. Konkrete aktuelle vollständige LFN-Scout-Projekt-ZIP oder Git-Commit in der **Entwicklungsunterhaltung** bereitstellen.
2. Dort `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/` lesen; mögliche konkurrierende Normtexte vollständig vergleichen.
3. Tatsächliche Audio-Session, Recorder, PCM-Format/Timebase, Histogramm-Datenquelle, Schema-/Persistenz- und Navigation-Klassen belegen; `16_Repo_Mapping_und_Gates_v0.7.md` mit echten Namen ausfüllen.
4. Test- und Hardwarematrix erstellen: Kopfhörerroute, UNPROCESSED, 2–25 Hz, FFT/Decimation, Output-Clip, Save-Writes und Bildschirmgröße. Vorhandene Regressionstests nicht entfernen.
5. Aufnahme-Konflikte und Speicher-/Quota-Fehler mit realen Daten und Fehlerfällen testen, bevor kontinuierlicher Betrieb freigegeben wird.

## 3. Noch KEINE endgültige Produktfreigabe

- Weder die genaue Form der realen Radiushistogramm-Daten noch die UI-Miniaturen sind mit einem vorhandenen LFN-Scout-Build bestätigt.
- `D01` ist ein Vorschlag. Die 13 vorhandenen Bilder sind teils historische Varianten und sämtlich illustrativ; mit `C13` ist nur der **Interaktionszustand des Mithörfeldes** von dir akzeptiert, nicht jeder Mockup-Pixel.
- Angegebene 8192 FFT/3 Decimation/120 s Average/Inferno sind Entwurfs-/Defaultwerte; reale Leistung und Kalibrierung müssen auf dem Zielgerät ermittelt werden.
- Weder Android-Build/CI noch 71 Tests sind in diesem Dokumentenpaket ausgeführt. Teststatus bleibt `NOT RUN`.

## 4. Dokumenten- und Exportgrenzen

`Save aktiv` und Audioaufnahme sind getrennte Betriebszustände. Automatische PNG-Dateien sind Bildexport, nicht Recording. PNG-Offscreen soll nur bei einer tatsächlich aktiven Analyzer-Session anlaufen; Störungen werden sichtbar gezählt. Die rechte Histo-Spalte darf die Frequenzachse nicht verfälschen. Kein unangeforderter Netzwerk- oder Cloud-Transfer.



---

# QUELLDOKUMENT: `references/Funktionsreferenz_v0.4.md`

# LFN-Scout – Spektrum-Analyzer: vollständige Funktions- und Einstellungsreferenz

**Dokument-ID:** LFN-AN-FER · **Version:** 0.4 · **Stand:** 19.09.2026  
**Status:** kumulativer **Spezifikationsentwurf**, keine Aussage über bereits implementierte oder getestete Funktionen  
**Produkt:** eigenständiges, in LFN-Scout eingebettetes Unterpanel „Spektrum & Mithören“  
**Bezug:** `SRD_LFN-Scout_Analyzer_v0.3.md`, `SRS_LFN-Scout_Analyzer_v0.3.md`, Spectroid-Anleitung v10.5 und anschließend vereinbarte GUI-/Funktionsänderungen. Diese Referenz ergänzt die v0.3-Dokumente; bei Widersprüchen gehen **die ausdrücklich später vereinbarten Festlegungen in diesem Dokument** für den Analyzer vor. Die Änderungen sind in Kapitel 16 benannt.

> **Grundidee:** Ein **kontinuierlicher Live-Spektrumanalysator wie Spectroid**, nicht ein Audio-Recorder. Hauptansicht möglichst nur **Spektrum + Wasserfall + dezent überlagerter Mithörbereich**. Die übrigen Funktionen werden in einem **separaten, aufrechtformat-tauglichen, schmalen und scrollbaren Einstellpanel** verwaltet. Fotos/PNG des Displays sind ausdrücklich erlaubt; WAV-/PCM-Aufzeichnung, Audio-Replay und automatisches Rohdatenlogging sind hier **nicht** vorgesehen.

## 1. Leseschlüssel und Verbindlichkeit

Jede Parameter-ID `CFG-...` ist für GUI, Persistenz, Tests und spätere Änderungen referenzierbar. `FIX` = vom Nutzer ausdrücklich festgelegt; `BASIS` = aus SRD/SRS v0.3 übernommen; `VORSCHLAG` = konkreter, **noch zu validierender** Entwurfswert. `GERÄT` = nur bei tatsächlich unterstützter Hardware/Android-Route verfügbar. Alle Werte beziehen sich auf das jeweilige Analyzer-Setup, **sofern nicht ausdrücklich als flüchtiger Laufzeitzustand gekennzeichnet**.

**Schalter:** `Ja` = separat zuschaltbar; `Immer` = Kernfunktion, nicht separat abschaltbar; `Aktion` = Funktion wird auf Bedienhandlung ausgelöst; `Gerät` = von Hardware abhängig. **Default „AUS“** bedeutet, dass die Funktion existiert, aber erst nach Auswahl wirkt. Werkspresets sind Vorlagen, keine Vorschrift für jeden Eingang. Eingabewerte werden vor Anwendung validiert; wo die echte Android-/DSP-Grenze enger ist, gilt diese Grenze und wird angezeigt.

### 1.1 Unveränderliche Systemregeln

1. `Eingang → Messzweig → Spektrum/Wasserfall/Statistik/Snapshot` und `Eingang → unabhängiger Mithörzweig → Bandpass → Nutzer-Gain → automatische Abregelung/Limiter → Kopfhörer` sind **getrennte Signalwege**.
2. Der Mithör-Gain, die automatische Mithör-Abregelung und der Mithörbandpass **dürfen Messwerte, Kalibrierung, L50/L90, Roh- und Referenzspektren nicht verändern**. Der Mithörbereich wird nur als Overlay gezeigt.
3. Eingangsclipping (`INPUT_CLIP`) und Mithör-Ausgangsrisiko (`MONITOR_CLIP`) sind **getrennte Warnungen**. Ein übersteuerter Eingang bleibt als Messung ungültig/verdächtig; Ausgangsregelung repariert ihn nicht.
4. `Pause` friert **die Anzeige** ein, nicht die laufende flüchtige Signalerfassung und nicht zwingend das aktive Mithören. Dies ist keine Aufnahmepause. Eingangs-/Ausgabestatus bleiben kontrolliert.
5. Es werden aus diesem Unterpanel **keine Audiodateien** erzeugt. `Save aktiv` meint ausschließlich **automatische Bild-/PNG-Speicherung**; CSV ist eine separate manuelle Momentaufnahme.
6. Alle Prozent-/dB- und Frequenzangaben müssen zur gewählten Messgröße passen. `dBFS ≠ dB SPL`; ein Telefon ist ohne validierte Kette kein geeichtes Infraschallmessgerät.

## 2. Hauptdisplay und direkte Bedienung

Das **Hauptdisplay bleibt im Hochformat**. Oben stehen Titel, knappe Zustandsindikatoren und dauerhaft erreichbare **Pause/Fortsetzen**- und Einstellungs-Taste. Darunter bleiben **Spektrum und Wasserfall gleichzeitig sichtbar**. Das Spektrum enthält Gelb (Live), optional Rot (Average), optional Grau (Rauschboden), zuschaltbares Fadenkreuz, die kurzen L50-/L90-Linien sowie den dezenten Mithörbereich. Im Wasserfall ist der identische Mithörbereich überlagert. Unten liegt in der Normalansicht ein kleines Mithörfeld mit Status und ggf. weitere Zusatzanzeigen (z. B. Superfast-/Maximum-Sample, L50/L90, Save-Status). Keine große dauerhafte Leiste mit zahlreichen Einstellwerkzeugen.

| ID | Element / Interaktion | Verhalten | Standard | Status |
|---|---|---|---|---|
| UI-01 | Spektrum | Laufende Frequenzkurve; auch im erweiterten Wasserfallmodus sichtbar | sichtbar | FIX |
| UI-02 | Wasserfall | Laufende Zeit-/Frequenzkarte; unter dem Spektrum | sichtbar | FIX |
| UI-03 | Wasserfall **kurz antippen** | Nur untere Zusatzanzeigen ausblenden; Wasserfall dehnt sich **nach unten** aus. Zweites Antippen stellt Normalansicht her. **Spektrum bleibt sichtbar.** | Normalansicht | FIX |
| UI-04 | Mithörfeld **unten links antippen** | Mithören EIN ↔ AUS; unabhängig von Analyse/Anzeige | AUS | FIX |
| UI-05 | Mithörfeld **vertikal wischen** | Hoch = Nutzer-Gain erhöhen, runter = verringern; nur wenn das Feld bedienbar ist. Gain bleibt im Setup speicherbar; tatsächliches Mithören startet nicht automatisch. | 0 dB | FIX |
| UI-06 | Bandfläche im Spektrum horizontal ziehen | Mithörband links/rechts verschieben; Bandbreite bleibt erhalten | 20–80 Hz | FIX/BASIS |
| UI-07 | Mithörband im Wasserfall ziehen | Gleiche Bandgrenzen wie im Spektrum ändern; keine Filterung der Messgrafik | synchron | BASIS |
| UI-08 | Messcursor antippen/ziehen | Fadenkreuz an Frequenz positionieren, horizontale Linie folgt gewählter Kurve; auch in Pause beweglich | AUS | FIX |
| UI-09 | Pause/Fortsetzen | Anzeige einfrieren/freigeben; Status eindeutig | Live | FIX |
| UI-10 | `Save aktiv` | Kleiner Statuspunkt/Text für automatische **PNG-Bildspeicherung**; nie „Aufnahme aktiv“ | AUS | FIX |
| UI-11 | `INPUT_CLIP` | Separater Datenqualitätsalarm im Hauptbild, wenn Mikrofon/A-D gesättigt | nur bei Ereignis | BASIS |
| UI-12 | `MONITOR_CLIP` / AUTO | Kompakte Clip-/Vorwarn- und Abregelanzeige im Hauptbild, nicht nur in Einstellungen | nur bei Ereignis | FIX |
| UI-13 | `Superfast`/`Maximum Sample` | Kleine Zusatzanzeige für kurzen Sample-Peak, ggf. mit Reset; darf im erweiterten Wasserfallmodus ausgeblendet werden | AUS | FIX |
| UI-14 | Einstell-Icon | Öffnet das separate Einstellpanel; dort stehen die selteneren Parameter | vorhanden | FIX |
| UI-15 | Frequenz-Pinch / Pan | Verändert **nur die Anzeige-Frequenzskala**, nicht die Grenzen des Mithörfilters oder des Statistikbandes | aktiv außerhalb Editiergeste | BASIS |

**Gestenpriorität (verbindlicher Entwurf):** Deutliche Bewegung ≠ kurzer Tap. Start innerhalb aktivierter Bandfläche → Band verschieben; Cursor-Editiermodus → Cursor verschieben; innerhalb Mithörfeld vertikal → Gain; kurzer Tap auf Mithörfeld → EIN/AUS; kurzer Tap auf freien Wasserfall → Anzeige umschalten. Während eines aktiven Drag-Vorgangs darf kein Tap-Ereignis ausgelöst werden. Sämtliche Gesten erhalten numerische Alternativen im Einstellpanel. Wenn das Mithörband nicht im aktuellen Zoomfenster liegt, bleibt die numerische Bandbedienung möglich.

## 3. Gruppierung des separaten Einstellpanels

Das Einstellpanel ist **keine zweite breite Hauptansicht**. Hochformat, eine Spalte, vertikal scrollbar; oben kompakte Register/Auswahlliste. Zulässige Gruppen:

| Gruppe | Inhalt |
|---|---|
| **Spektrum** | FFT, Decimations, Fenster, Transformintervall, Glättung, DC, Kurven, Cursor, Peak/Max Hold, Frequenz- und Pegelskalierung, Mithör-**Bandbreite in Hz** |
| **Audio** | Eingangsgerät/-quelle, Sampling Rate, Kanal/PCM, Eingangsclip, Audio-/Routendiagnose |
| **Mithören** | Ausgangsroute, Ein/Aus-Verhalten, Nutzer-Gain, Bandgrenzen und Filtercharakteristik, Auto-Abregelung, Ausgangsclip |
| **Anzeige & Wasserfall** | Layout, Zeitraum, Palette einschließlich Inferno, Wasserfall-Pegelfenster, Farbmarkierungen, erweiterte Ansicht |
| **Statistik & Impuls** | L50/L90-Band 2–25 Hz, 120-s-Zeitfenster, grafische Anzeige, Superfast, Maximum Sample |
| **Kalibrierung** | Mikrofon-Frequenzkurve, relative/absolute Referenz, Gültigkeit, **Rauschboden: Referenz messen/speichern/laden/anzeigen/abziehen** |
| **Save & Export** | Automatische PNG-Bilder je Wasserfallzeit, Bildziel, manueller PNG-/CSV-Export, Dateibenennung, Speicherfehler |
| **Setups** | Neu, Speichern, Speichern unter, Recall, Duplizieren, Löschen, Import/Export, Werkspresets |
| **Erweitert & Diagnose** | Testsignal, reale FFT-Auflösung/Latenz, CPU/Drops, Selbsttests, Versions-/Fehlerdaten |

**Nicht doppeln:** Die Band-**breite** wird wie gewünscht in **Spektrum** eingestellt, die Band-**mitte bzw. Grenzen** sind zusätzlich direkt in der Grafik bedienbar und werden auch in **Mithören** numerisch gespiegelt. Das L50/L90-Band ist ein **anderes, völlig unabhängiges Frequenzband**.

## 4. Parametergruppe „Spektrum“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-SP-01 | Frequenzachse | Linear; Logarithmisch | **Linear** | Auswahl | FIX; 0 Hz nur auf linearer Achse |
| CFG-SP-02 | Frequenz-Minimum [Hz] | 0…gültiges Anzeige-Max., Zahlenfeld und Zoom | **0 Hz** | Einstellbar | FIX |
| CFG-SP-03 | Frequenz-Maximum [Hz] | > Min.; Presets 100/200 Hz; frei bis effektiver Nyquist-Grenze | **100 Hz** | Einstellbar | FIX; 200 Hz für technische Geräusche |
| CFG-SP-04 | FFT-Größe [Bins] | 2048, 4096, 8192, 16384 soweit performant/unterstützt | **8192** | Auswahl | BASIS; Fallback 4096 |
| CFG-SP-05 | Decimations [Stufen] | 0…5 bzw. sichere geräteabhängige Teilmenge | **3** | Auswahl | BASIS; konkrete Faktoren/Hz-bin anzeigen |
| CFG-SP-06 | Fensterfunktion | Blackman-Harris, Hann, Rechteck; weitere optional | **Blackman-Harris** | Auswahl | BASIS |
| CFG-SP-07 | Transform-Intervall [ms] | mindestens 50, 100, 250, 500; frei im realisierbaren Bereich | **100 ms** | Einstellbar | BASIS; tatsächliche Bild-/Zeitauflösung anzeigen |
| CFG-SP-08 | Exponential Smoothing [0…1] | 0…1, mathematische Richtung in GUI erklären | **0,3** | Einstellbar | BASIS; nicht mit Average verwechseln |
| CFG-SP-09 | Subtract DC | EIN/AUS | **EIN** | Ja | BASIS; behebt keinen Hardware-Hochpass |
| CFG-SP-10 | Pegelskala Minimum [dB] | z. B. −180…−10, stets < Max. und zum Pegelmodus passend | **−120 dBFS** | Einstellbar | BASIS/VORSCHLAG Bereich |
| CFG-SP-11 | Pegelskala Maximum [dB] | z. B. −80…+20 bei kalibriertem SPL; im unkalibrierten dBFS-Modus höchstens 0 | **0 dBFS** | Einstellbar | BASIS; Grenzwerte modusabhängig |
| CFG-SP-12 | Automatische Y-Skalierung | AUS/EIN, optional feste Padding-Angabe | **AUS** | Ja | VORSCHLAG; feste Skala für Vergleichsmessung |
| CFG-SP-13 | Live-Kurve | Gelb | **EIN** | Immer | FIX; Hauptkurve |
| CFG-SP-14 | Average-Kurve | Rot, unabhängig Ein/Aus | **EIN** | Ja | FIX; grau darf nicht für Average verwendet werden |
| CFG-SP-15 | Average-Zeit [s] | 1…600, z. B. 10/30/60/120/300 | **120 s** | Einstellbar | VORSCHLAG; zeitbezogener Leistungsmittelwert, getrennt von Smoothing |
| CFG-SP-16 | Max Hold | EIN/AUS, eigener Reset | **AUS** | Ja | BASIS; über bisherige Laufzeit oder seit Reset |
| CFG-SP-17 | Peak Marker | EIN/AUS; Frequenz/Pegel am Peak | **AUS** | Ja | BASIS; nicht mit Fadenkreuz gleichsetzen |
| CFG-SP-18 | Manuelle Zielfrequenz | Markierung per Zahl/Tap, z. B. 47 Hz | **AUS** | Ja | BASIS; separate frequenzfeste Markierung |
| CFG-SP-19 | Messcursor (Fadenkreuz) | EIN/AUS; senkrecht **und** waagerecht mit Punkt/Readout | **AUS** | Ja | FIX |
| CFG-SP-20 | Cursor-Kurvenquelle | Live (gelb), Average (rot), Beide | **Live** | Auswahl | FIX; bei Beide gleiche X-Position, zwei Y-Werte eindeutig kennzeichnen |
| CFG-SP-21 | Lokales Cursor-Readout | EIN/AUS; Hz + dB-Einheit, wandert ohne Plot zu verdecken | **EIN bei Cursor EIN** | Ja | FIX |
| CFG-SP-22 | Festes Cursor-Readout | EIN/AUS; kleines Statusfeld, optional beide Kurven | **AUS** | Ja | VORSCHLAG; keine unnötige doppelte Anzeige |
| CFG-SP-23 | Mithör-Bandbreite [Hz] | **Numerisch**, 2…200 Hz als Vorschlagsgrenze; tatsächliche Filter-/Nyquist-Grenze beachten | **60 Hz** (20–80) | Einstellbar | FIX; in **Spektrumsetup** |
| CFG-SP-24 | Effektive Auflösung [Hz/Bin] | Berechnet, nicht frei vorgebbar | dynamisch | Anzeige | BASIS; aus echter Pipeline, nicht angenommener Decimation |
| CFG-SP-25 | Reset/Neustart Kurvenstatistik | Average/Max Hold getrennt zurücksetzen | Aktion | Aktion | BASIS/VORSCHLAG |

**Hinweis zu 0 Hz:** Das linke Skalenende darf „0 Hz“ zeigen; daraus folgt **keine reale Mikrofonempfindlichkeit bei 0 Hz**. Bei kalibriertem Pegelmodus dürfen `Min/Max` nur mit derselben Einheit interpretiert werden.

## 5. Parametergruppe „Audio“ (Live-Eingang)

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-AU-01 | Eingangsgerät | Intern, erkannte externe/USB-Audioeingänge | internes Mikrofon | Gerät/Auswahl | BASIS |
| CFG-AU-02 | Audio Source | `UNPROCESSED`, sofern unterstützt; andernfalls sichtbar `VOICE_RECOGNITION`; andere bewusst auswählbar | **UNPROCESSED falls verfügbar** | Gerät/Auswahl | FIX/BASIS |
| CFG-AU-03 | Sampling Rate [Hz] | 44100, 48000, ggf. weitere tatsächlich unterstützte | **48000** | Gerät/Auswahl | BASIS |
| CFG-AU-04 | Kanalzahl | Mono; Mehrkanal nur wenn kompatibel und Kanalwahl definiert | **Mono** | Gerät/Auswahl | BASIS |
| CFG-AU-05 | PCM-Format | PCM_FLOAT bevorzugt, sonst PCM_16BIT; intern normiert | **AUTO** | Gerät/Auswahl | BASIS |
| CFG-AU-06 | Effektive Quelle/Rate | Tatsächliche Android-/Hardware-Route und Rate | automatisch | Immer/Anzeige | BASIS; gewünscht und tatsächlich trennen |
| CFG-AU-07 | Eingangs-Clip-Warnung | EIN/AUS **nur für Sichtbarkeit**; interne Erkennung muss laufen | **EIN** | Anzeige schaltbar | FIX/BASIS; Quelle nicht reparierbar |
| CFG-AU-08 | Eingangs-Clip-Schwelle [dBFS] | etwa −3…0, nur technische Diagnose | **−1 dBFS** | Expertenwert | VORSCHLAG; zusätzlich Sättigung erkennen |
| CFG-AU-09 | Buffer-/Drop-Anzeige | Status und Zähler, optional Detailseite | Hintergrundprüfung EIN | Ja für Detail | BASIS |
| CFG-AU-10 | Mikrofonberechtigung | Android-Laufzeitberechtigung, keine App-Einstellzahl | bei Bedarf | Gerät | BASIS; `RECORD_AUDIO` erlaubt Live-Eingang, nicht Datei-Aufnahme |
| CFG-AU-11 | Eingangsneustart bei Änderung | Warnung + kontrollierter Restart bei Rate/Quelle | Bestätigung | Aktion | BASIS |

## 6. Parametergruppe „Mithören“ (separater Audiozweig)

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-MO-01 | Mithören aktiv | Antippen **unten links**: EIN↔AUS; nach Start/Recall immer AUS | **AUS** | Ja | FIX |
| CFG-MO-02 | Wiedergaberoute | geeignete kabelgebundene/USB-Kopfhörer, ggf. geprüfte weitere Route | Kopfhörer bevorzugt | Gerät/Auswahl | BASIS; Lautsprecher standardmäßig gesperrt |
| CFG-MO-03 | Nutzer-Gain [dB] | −60…+24 als Entwurfsbereich; numerisch und Wischen ↑↓ | **0 dB** | Einstellbar | BASIS/FIX |
| CFG-MO-04 | Gain-Wischschritt [dB] | z. B. 0,5/1/2 pro definierter Wischstrecke | **1 dB** | Einstellbar | VORSCHLAG; keine sprunghaften Pegel |
| CFG-MO-05 | Band-Untergrenze [Hz] | >0, < Obergrenze; direkte Zahl, Ziehgriff | **20 Hz** | Einstellbar | BASIS |
| CFG-MO-06 | Band-Obergrenze [Hz] | > Untergrenze; direkte Zahl, Ziehgriff | **80 Hz** | Einstellbar | BASIS |
| CFG-MO-07 | Bandmitte [Hz] | Aus Grenzen abgeleitet, alternativ numerisch positionierbar | **50 Hz** | Einstellbar | VORSCHLAG |
| CFG-MO-08 | Bandbreite [Hz] | `f_max−f_min`; in **Spektrum** primär einstellbar, hier nur gespiegelt | **60 Hz** | Einstellbar | FIX; Änderungen synchronisieren |
| CFG-MO-09 | Mindestbandbreite [Hz] | >= technisch sicherem Filterminimum, z. B. 2 Hz | **2 Hz** | Expertenwert | BASIS/VORSCHLAG |
| CFG-MO-10 | Filtertyp | stabiler IIR-Bandpass; genauer Typ/Aufbau validieren | Butterworth-Entwurf | Auswahl/Expert | BASIS; keine exakte Kurvenform ohne Test garantieren |
| CFG-MO-11 | Band-Overlay | EIN/AUS; in beiden Plots gleiche Grenzen, dezente Farbfläche | **EIN** | Ja | FIX |
| CFG-MO-12 | Overlay-Transparenz [%] | z. B. 5…40 | **15 %** | Einstellbar | VORSCHLAG |
| CFG-MO-13 | Auto-Abregelung | EIN/AUS, **nur auf Mithörzweig** | **EIN** | Ja | FIX/BASIS; bei AUS bleibt harter digitaler Limiter aktiv |
| CFG-MO-14 | Vorwarn-/Regelbeginn [dBFS Ausgang] | −12…−1; ausreichend Abstand zu 0 | **−6 dBFS** | Expertenwert | BASIS |
| CFG-MO-15 | Ziel-Peak [dBFS Ausgang] | nicht höher als Regelbeginn | **−6 dBFS** | Expertenwert | BASIS |
| CFG-MO-16 | Attack [ms] | technisch geeignet, etwa 1…100 | **≤20 ms Ziel** | Expertenwert | BASIS; Lookahead/Peak-Limiter nötig für schnellere Spitzen |
| CFG-MO-17 | Release [s] | z. B. 0,5…10 | **3 s** | Expertenwert | BASIS |
| CFG-MO-18 | Maximaler Nutzer-Gain [dB] | 0…+24 als persönliche Obergrenze | **+24 dB** | Einstellbar | BASIS/VORSCHLAG; keine Gehörschutzgarantie |
| CFG-MO-19 | Ausgangs-Clipstatus im Hauptbild | Vorwarnung, Clip, `AUTO −x dB` getrennt | **EIN** | Ja nur Detail; Gefahr sichtbar | FIX |
| CFG-MO-20 | Effektiver Gain [dB] | `Nutzer-Gain + flüchtige Auto-Absenkung` | dynamisch | Anzeige | BASIS; Auto-Zustand **nicht** speichern |
| CFG-MO-21 | Automatische Schaltung bei Kopfhörerabzug | sofort Mute, Mithören AUS, keine Lautsprecherumschaltung | **EIN / fest** | Immer | BASIS |
| CFG-MO-22 | Fade beim manuellen Schalten [ms] | kurzes Ramp-in/out zur Knackvermeidung | **50 ms** | Expertenwert | VORSCHLAG; Fehler-Mute sofort |

**Wichtige Unterscheidung:** Ein dezentes farbiges Rechteck 20–80 Hz bezeichnet **nur** den Mithörfilter; außerhalb des Rechtecks bleiben alle Frequenzen in Messung, Spektrum, Wasserfall und Statistik enthalten. Eine Bandverschiebung darf an `CFG-SP-02/03` (Anzeige-Zoom) und `CFG-ST-02/03` (L50/L90-Band) **nichts** ändern. Tiefste Kopfhörerfrequenzen können trotz digitalem Gain physikalisch unhörbar bleiben.

## 7. Parametergruppe „Anzeige & Wasserfall“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-DI-01 | Aufteilung Spektrum/Wasserfall | z. B. 1/3 : 2/3; vertikal anpassbar | **1/3 : 2/3** | Einstellbar | BASIS |
| CFG-DI-02 | Wasserfall-Zeitspanne [s] | z. B. 30…3600, durch Speicher/Leistung begrenzt | **120 s** | Einstellbar | BASIS/FIX für Save-Periodik |
| CFG-DI-03 | Wasserfall-Palette | **Inferno**, Viridis, Magma, Plasma, weitere optional | **Inferno** | Auswahl | FIX (Inferno enthalten), Default VORSCHLAG |
| CFG-DI-04 | Wasserfall-Untergrenze [dB] | modusabhängig, < Obergrenze | **−120 dBFS** | Einstellbar | BASIS/VORSCHLAG |
| CFG-DI-05 | Wasserfall-Obergrenze [dB] | modusabhängig, > Untergrenze | **0 dBFS** | Einstellbar | BASIS/VORSCHLAG |
| CFG-DI-06 | Farbskalierung automatisch | EIN/AUS | **AUS** | Ja | VORSCHLAG; feste Farbskala für Vergleichbarkeit |
| CFG-DI-07 | Mitlauf-/Scrollrichtung | Zeitachse eindeutig beschriftet; ggf. optionale Richtung | wie Spectroid-Entwurf | Auswahl | BASIS; Orientierung vor Implementierung validieren |
| CFG-DI-08 | Erweiterter Wasserfall | Kurzer Tap auf freien Wasserfall schaltet nur untere Zusatzanzeigen AUS/EIN | **Normal** | Ja/Geste | FIX; Spektrum und Pause bleiben |
| CFG-DI-09 | Fußbereich in Erweiterung | Mithör-/Superfast-/Statistik-Zusatzkarten verborgen; Platz für Wasserfall | **verborgen** | Anzeige | FIX; nicht Live-Status/Pause verbergen |
| CFG-DI-10 | Renderer-Frequenz [Hz] | adaptiv; Ziel 10…20 auf Referenzgerät | **AUTO** | Gerätewert | BASIS; Audio hat Vorrang |
| CFG-DI-11 | Wasserfall bei Anzeige-Pause | Eingefrorene Darstellung; kein Wechsel in Audio-Recording | **eingefroren** | Status | FIX/BASIS |
| CFG-DI-12 | Hintergrund-/Overlay-Kontrast | für Inferno lesbare Linien/Farbstriche | Theme-Standard | Expertenwert | VORSCHLAG |
| CFG-DI-13 | Einblendungen im Hauptbild | minimal: Fadenkreuz-Readout, L50/L90, INPUT/MONITOR-Clip, Save | **kompakt** | je Feature | FIX |

**Darstellung:** `Inferno` ist eine **Farbpalette**, kein Pegelkalibrator. Wasserfall-Skalierung darf mit `CFG-SP-10/11` identisch vorbesetzt, aber separat änderbar sein. Bei einem Wechsel von dBFS zu gültigem dB SPL müssen Grenzen/eindeutige Einheit sichtbar aktualisiert werden.

## 8. Parametergruppe „Statistik & Impuls“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-ST-01 | L50/L90-Bandstatistik | EIN/AUS; unabhängig vom Mithören | **EIN** | Ja | FIX; Abschaltbarkeit ausdrücklich vorgesehen |
| CFG-ST-02 | Statistikband Minimum [Hz] | >0, < Max., validierter Messbereich | **2 Hz** | Einstellbar | FIX |
| CFG-ST-03 | Statistikband Maximum [Hz] | > Min., im wirksamen Nyquist/Mikrofonbereich | **25 Hz** | Einstellbar | FIX |
| CFG-ST-04 | Rollendes Statistikfenster [s] | 30/60/120/300/frei, z. B. 10…3600 | **120 s** | Einstellbar | FIX |
| CFG-ST-05 | L50-Markierung | Kurzer waagerechter Farbstrich **nur über dem sichtbaren Bandabschnitt** | **EIN bei ST-01** | Ja | FIX; z. B. Orange |
| CFG-ST-06 | L90-Markierung | Entsprechender kurzer waagerechter Farbstrich im selben Bereich | **EIN bei ST-01** | Ja | FIX; z. B. Cyan/Grün |
| CFG-ST-07 | Numerische L50/L90-Werte | kompakt, mit Band/Einheit und Gültigkeitsstatus | **EIN** | Ja | FIX |
| CFG-ST-08 | Statistik-Aktualisierung [s] | z. B. 0,1…5; nur abgeschlossene Analyseblöcke | **1 s** | Einstellbar | VORSCHLAG |
| CFG-ST-09 | Fenster noch nicht gefüllt | Fortschritt / „füllt sich“; nicht als final anzeigen | **EIN / fest** | Immer | FIX |
| CFG-ST-10 | Statistikquelle | Original-Messpfad nach ggf. **gültiger Mikrofonkalibrierung**, **vor Rauschboden-Subtraktion**, nie Mithörpfad | **unverändert** | Immer | FIX |
| CFG-ST-11 | Superfast-Samplemodus | EIN/AUS: höchsten **tatsächlich eingelesenen Rohsample-Peak** zwischen normalen UI-Updates mitführen | **AUS** | Ja | FIX; Algorithmus/Mehrkanalregel noch zu validieren |
| CFG-ST-12 | Maximum Sample / Impulse | Anzeige des bisher höchsten Rohsample-Peaks mit Zeitstempel und Reset | **AUS** | Ja | FIX; klar als **Sample Peak**, nicht ungeprüft „True Peak“/SPL ausweisen |
| CFG-ST-13 | Maximum-Sample-Reset | Peak/Zähler neu beginnen | Aktion | Aktion | FIX |
| CFG-ST-14 | Superfast-Zeitauflösung [ms] | tatsächliche Audio-Block-/Sample-Zeit, nicht UI-Intervall; optional Peak je 10/20/50/100 ms | **20 ms Anzeigeaggregation** | Einstellbar | VORSCHLAG; Rohpeak selbst auf Samplerate erfasst |
| CFG-ST-15 | Clip-Markierung an Impuls | INPUT_CLIP darf nicht durch Sample-Peak/Output-Regelung verdeckt werden | **EIN / fest** | Immer | FIX |

**L50/L90-Definition:** Pro definiertem Zeitintervall zunächst die **Bandenergie/den Band-RMS-Pegel** im unabhängigen Band 2–25 Hz bilden, dann aus der Folge der Bandpegel über die letzten 120 s die Überschreitungspegel bestimmen: `L50` wird in 50 % der Zeit überschritten, `L90` in 90 %. Somit gilt bei gleicher gültiger Datenbasis typischerweise **L50 ≥ L90**. **Nicht** pro Frequenzbin Dezibelwerte arithmetisch mitteln oder L50 und L90 auf der Frequenzachse als einzelne Messtonfrequenzen ausgeben. Die kurzen Linien erstrecken sich **horizontal in X-Richtung über den Bandabschnitt** und stehen auf dem zum Pegel gehörenden Y-Wert. Nicht sichtbare/verzerrte/nicht kalibrierte Bandteile sind zu kennzeichnen. Nur bei absolut validierter gesamter Bandkette darf `dB SPL` bzw. `dB(Z)` verwendet werden; sonst korrekt beschrifteter relativer Bandpegel, dessen Definition dokumentiert ist.

**Superfast-Abgrenzung:** Eine hohe Sample-Spitze ist nicht automatisch eine quantifizierte akustische Impulsspitze. `Maximum Sample` ist zunächst ein **digitaler Eingangs-Sample-Peak in dBFS**. Es wird kein Intersample-True-Peak, keine A-bewertete Impulszeitbewertung und keine akustische Exposition vorgetäuscht. Falls eine zusätzliche Bandpass-Impulsgröße gewünscht wird, braucht sie eine eigene Definition.

## 9. Parametergruppe „Kalibrierung – Mikrofon“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-CA-01 | Mikrofonkalibrierung aktiv | EIN/AUS | **AUS** | Ja | FIX/BASIS |
| CFG-CA-02 | Kalibrierprofil | Mehrere benannte Profile, JSON oder CSV-Import | **kein Profil** | Auswahl | BASIS |
| CFG-CA-03 | Frequenzkorrektur [dB je Hz-Stützstelle] | Endliche, aufsteigende Punkte; Korrektur in dokumentiertem Vorzeichen | aus Datei | Datei | BASIS |
| CFG-CA-04 | Gültigkeitsband [Hz] | aus Kalibrierdatei, keine stille Extrapolation | aus Datei | Anzeige | BASIS |
| CFG-CA-05 | Absoluter SPL-Referenzpunkt | optional kalibrierter dB-SPL-/RMS-dBFS-Bezug, z. B. validierter 94 dB bei 1 kHz | **keine Referenz** | Datei/Aktion | BASIS |
| CFG-CA-06 | Anzeige-Pegelmodus | Roh-dBFS; frequenzkorrigiert relativ; dB SPL nur bei freigegebener Referenz | **Roh-dBFS** | Auswahl | BASIS |
| CFG-CA-07 | Geräte-/Quellenbindung | Geräte-ID, Mikrofonweg, Audio Source, Sampling Rate prüfen | **Prüfung EIN** | Immer | BASIS |
| CFG-CA-08 | Korrekturkurve ansehen | Profilgrafik und Vorher/Nachher-Ansicht | **AUS** | Ja | BASIS |
| CFG-CA-09 | Kalibrierdatei importieren/exportieren | Lokale Dateiauswahl, Versions-/Integritätsprüfung | Aktion | Aktion | BASIS |
| CFG-CA-10 | Kalibrierung bei Routewechsel | inkompatibles Profil deaktivieren, Warnung; Roh-dBFS weiter möglich | **EIN / fest** | Immer | BASIS |

**Kalibrierlogik:** Bei Frequenzgangkorrektur ohne absoluten Referenzpunkt ist der Wert **relativ frequenzkorrigiert**, nicht „dB SPL“. Absolute Messung gilt ausschließlich im geprüften Frequenzbereich und für dieselbe Eingabekette. Eine Mikrofonkalibrierung verändert **nicht** den Mithörgain und ist **nicht** mit Rauschboden-Subtraktion gleichzusetzen.

## 10. Parametergruppe „Kalibrierung – Rauschboden“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-NF-01 | Rauschboden-Referenz messen | Live-Mittelung einer definierten Hintergrundsituation; nur auf Aktion | Aktion | Aktion | FIX |
| CFG-NF-02 | Referenz-Messdauer [s] | 30/60/120/300/600/frei, z. B. 10…3600 | **120 s** | Einstellbar | VORSCHLAG |
| CFG-NF-03 | Referenzprofil speichern | Name, UUID, Datum, Messketten-/FFT-/Kalibrierkontext, Zeit und PSD-/Pegeldefinition | Aktion | Aktion | FIX |
| CFG-NF-04 | Referenzprofil laden | lokale Liste/Datei, Kompatibilitätsprüfung | **keines** | Aktion | FIX |
| CFG-NF-05 | Graue Referenzkurve anzeigen | unabhängig Ein/Aus | **AUS** | Ja | FIX |
| CFG-NF-06 | Rechnerisch Rauschboden abziehen | unabhängig von NF-05 Ein/Aus | **AUS** | Ja | FIX |
| CFG-NF-07 | Kompensation anwenden auf | „nur Spektrum“ / „Spektrum + Wasserfall“ | **nur Spektrum** | Auswahl | FIX/VORSCHLAG Default |
| CFG-NF-08 | Berechnung | in **linearen, passend normierten Leistungs-/PSD-Werten** `max(0, P_aktuell−P_ref)`; keine dB-Subtraktion | fest | Immer | FIX |
| CFG-NF-09 | Nicht bestimmbarer Rest | bei `P_aktuell≤P_ref` und unter Unsicherheit: ungültig/unter Nachweisgrenze markieren, **keine erfundene −∞-Messung** | fest | Immer | FIX |
| CFG-NF-10 | Referenzkompatibilität | Gerät, Quelle, Rate, Fenster, FFT, Decimation, Pegeltyp, Kalibrier- und Bin-/PSD-Definition prüfen; nur dokumentiertes Resampling zulässig | **Prüfung EIN** | Immer | FIX |
| CFG-NF-11 | Roh-/korrigierte Anzeige | Originalmessung jederzeit wieder anzeigen; Korrektur ausdrücklich kennzeichnen | **Rohwert** | Auswahl | FIX |
| CFG-NF-12 | Referenztyp / Kommentar | Mikrofon-Eigenrauschen / Raumhintergrund / Vergleichssituation, Freitext | **Raumhintergrund** | Auswahl | VORSCHLAG |

**Physikalische Grenze:** Das subtrahierte Spektrum ist eine **Schätzung eines zusätzlichen unkorrelierten Anteils**, kein Ersatz für Messdaten. Ein Raumhintergrund kann reale Quellen enthalten und sich zwischen Referenz und Messung ändern. Die Rauschbodenfunktion beeinflusst ausschließlich den **optionalen Anzeigezweig**; unkompensierte Live-/Average-Messwerte, `L50/L90`, Export-Rohwerte und Mithören bleiben verfügbar/unverändert. Ein PNG kann die **sichtbare kompensierte Darstellung** abbilden; die Metadaten nennen dann ausdrücklich Referenz-ID, Modus und den zusätzlich zugeordneten Rohwertebezug. Gespeicherte Referenz-ID gehört ins Setup, die Referenzdatei bleibt eine eigenständige Ressource.

## 11. Parametergruppe „Save & Export“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-SV-01 | `Save aktiv`: Automatische PNG-Speicherung | EIN/AUS; **kein Audio** | **AUS** | Ja | FIX |
| CFG-SV-02 | Erste automatische Speicherung | Sobald Wasserfall nach Aktivierung **erstmals vollständig gefüllt** ist | fest | Immer wenn Save EIN | FIX |
| CFG-SV-03 | Wiederholung | Danach **nach jeder vollen Wasserfallbereichszeit** | `CFG-DI-02` = **120 s** | Automatisch | FIX |
| CFG-SV-04 | Zu speichernder Bildinhalt | Hauptbild mit Spektrum + Wasserfall + sichtbaren Overlays und Status, gleiche Ansicht wie Nutzer | Hauptbild | Auswahl | FIX/VORSCHLAG |
| CFG-SV-05 | Zielverzeichnis | app-eigener Ordner bzw. über SAF gewählt, Schreibbarkeit prüfen | app-eigener PNG-Ordner | Einstellbar | VORSCHLAG |
| CFG-SV-06 | Dateiname | `LFNScout_Analyzer_YYYYMMDD_HHMMSS_mmm_setupId.png` oder konfliktfeste Variante | Zeitstempel | Einstellbar | VORSCHLAG |
| CFG-SV-07 | Manuelles PNG | Sofortiger Screenshot auf Aktion | Aktion | Aktion | BASIS |
| CFG-SV-08 | Manuelles CSV | **Momentaner** Spektralwertestand mit Frequenz, Pegel und Einheit; kein kontinuierliches CSV-Logging | Aktion | Aktion | BASIS |
| CFG-SV-09 | Metadaten | Zeit, Setup-ID/Revision, Audioquelle, FFT, f-Min/Max, Kalibrier-ID, Statistikband, Mithörband, Referenz-/Kompensationsstatus, Clips | fest | Immer | BASIS/FIX |
| CFG-SV-10 | Pause und Autosave | **VORSCHLAG:** keine wiederholten Bilder der identischen eingefrorenen Anzeige; nach Fortsetzen neuen vollständigen Wasserfall-Zyklus abwarten | Save während Pause ausgesetzt | Verhalten | VORSCHLAG, vor Coding entscheiden |
| CFG-SV-11 | Speichermangel/Fehler | sichtbarer Fehlerstatus, keine stille Überschreibung, Live-Analyse bleibt stabil | fest | Immer | BASIS |
| CFG-SV-12 | Speicherintervall manuell anders als Wasserfallzeit | **Nicht vorgesehen**; Intervall ist ausdrücklich an sichtbare Wasserfallbereichszeit gekoppelt | gleich Wasserfallzeit | fest | FIX |
| CFG-SV-13 | Verfügbarkeit bei verdeckten Zusatzkarten | Auto-Save läuft weiter, Status im oberen Bereich ablesbar | EIN | fest | VORSCHLAG/FIX |

**Ablaufbeispiel:** `Wasserfall=120 s`, Save wird bei `t=0` eingeschaltet → erster PNG-Snapshot nach vollständigem Füllen bei ca. `t=120 s`, weitere bei `t=240 s, 360 s, …`, sofern fortlaufend gültige Anzeigedaten vorliegen. Beim Ändern der Wasserfallzeit beginnt ein neuer, eindeutig gekennzeichneter Zyklus. Nur ein Vollbild-PNG pro Zyklus; **kein** WAV, PCM oder Audio-Replay.

## 12. Parametergruppe „Setups“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-SE-01 | Aktives Setup | Liste von beliebig vielen benannten lokalen Setups, unveränderliche UUID | `Brummton 0–100 Hz` | Auswahl | BASIS |
| CFG-SE-02 | Neu | neues Setup aus Default oder aktuellem Zustand | Aktion | Aktion | BASIS |
| CFG-SE-03 | Speichern | Änderungen des aktiven Setups sichern; bei Ersetzung nachvollziehbar | Aktion | Aktion | FIX/BASIS |
| CFG-SE-04 | Speichern unter | neue ID und neuer Name; altes Setup bleibt | Aktion | Aktion | FIX/BASIS |
| CFG-SE-05 | Recall/Laden | gewähltes Profil validieren und anwenden | Aktion | Aktion | FIX/BASIS |
| CFG-SE-06 | Duplizieren / Löschen | eigene ID; Löschen mit Bestätigung | Aktion | Aktion | BASIS |
| CFG-SE-07 | Import/Export | einzelne/mehrere versionierte JSON-Setups über Android SAF | Aktion | Aktion | BASIS |
| CFG-SE-08 | Letztes Setup | `lastUsedSetupId` persistent, bei Start wiederherstellen | **EIN** | Immer | FIX/BASIS |
| CFG-SE-09 | Dirty-State | nicht gespeicherte Änderungen sichtbar; Warnung beim Wechsel | **EIN** | Immer | BASIS |
| CFG-SE-10 | Mithörzustand bei Recall | gespeicherte Parameter laden, **Audioausgabe AUS** belassen | **AUS / fest** | Immer | FIX/BASIS |
| CFG-SE-11 | Auto-Attenuation-Zustand | flüchtig, **nicht** im Setup | nicht gespeichert | Immer | FIX/BASIS |
| CFG-SE-12 | Speichern von Calibration-/Noise-IDs | Referenz auf getrennte Profil-Dateien; fehlende/inkompatible Dateien warnen | gespeicherte IDs | Immer | FIX |
| CFG-SE-13 | Wasserfall-/Statistik-/Save-Parameter | alle Nutzereinstellungen aus Gruppen 4–11 speichern, soweit nicht flüchtig | gespeicherte Werte | Immer | FIX |
| CFG-SE-14 | Werkspreset B | „Technische Geräusche 0–200 Hz“ als unveränderbare Vorlage | optional | Aktion | BASIS |
| CFG-SE-15 | Setup-Revision / Migration | `schemaVersion`, Revision, Zeitstempel, atomare Persistenz und kontrollierte Migration | fest | Immer | BASIS |

**Startverhalten:** Das zuletzt erfolgreiche Setup wird geladen; das **Mithören bleibt dennoch immer AUS**. `Save aktiv` wird als gespeicherte *Vorgabe* übernommen, startet aus Sicherheits-/Datenschutzgründen aber **VORSCHLAG: AUS bis der Nutzer es für die aktuelle Sitzung aktiviert**. Diese Save-Startentscheidung ist offen und darf nicht ohne Dokumentation in die Implementierung eingeschmuggelt werden. Weder aufgezeichnete Daten noch Clipzähler werden mit einem Setup geladen.

## 13. Parametergruppe „Erweitert & Diagnose“

| ID | Parameter / Einheit | Zulässige Werte / Bedienung | Default | Schalter | Stand / Zweck |
|---|---|---|---|---|---|
| CFG-EX-01 | Testsignal aktiv | EIN/AUS; unabhängig vom realen Messmikrofon | **AUS** | Ja | BASIS |
| CFG-EX-02 | Testsignalform | mindestens Sinus | Sinus | Auswahl | BASIS |
| CFG-EX-03 | Testfrequenz [Hz] | z. B. 10, 20, 47, 50, 100, frei innerhalb Testbandes | **50 Hz** | Einstellbar | VORSCHLAG |
| CFG-EX-04 | Testpegel [dBFS] | −120…−6 (bei synthetischem Testpfad) | **−60 dBFS** | Einstellbar | VORSCHLAG |
| CFG-EX-05 | Tatsächliche `Δf` | aus effektiver Sample Rate und FFT-N, als Hz/Bin | automatisch | Anzeige | BASIS |
| CFG-EX-06 | Analyseverzug/Latenz | tatsächlich/geschätzt, nach Konfigurationswechsel neu | automatisch | Anzeige | BASIS |
| CFG-EX-07 | CPU-Last / Dropped Blocks | Status, bei Engpässen reduzierte Renderlast | Diagnoseseite | Ja | BASIS |
| CFG-EX-08 | Selbsttest | Synthetische Referenz für FFT, Filter, Kalibrierung, Clip, Setup, Rendering, Autosave, Cursor, L50/L90 | Aktion | Aktion | FIX/BASIS |
| CFG-EX-09 | Testbericht | PASS/FAIL + Versions-/Geräteinformation | Ausgabe | Immer | BASIS |
| CFG-EX-10 | Betriebsart | Vordergrund-Live-Analyzer; kein stiller Mikrofon-Hintergrunddienst | **Vordergrund** | fest | BASIS |

## 14. Vorgeschlagene Werkseinstellungen in einem Block

Die folgende Tabelle ist eine **Kontrollliste** für das erste Preset; verbindliche/noch offene Werte sind durch die oben stehenden Kennungen definiert.

| Bereich | Startwert |
|---|---|
| Achsen / Anzeige | **Linear**, 0–100 Hz; Pegelskala −120…0 dBFS; Spektrum und Wasserfall sichtbar |
| Audio | internes Mikrofon; bevorzugt **UNPROCESSED** (falls unterstützt); 48 kHz mono |
| Analyse | FFT **8192**, Decimations **3**, Blackman-Harris, Transform-Intervall **100 ms**, Smoothing **0,3**, Subtract DC EIN |
| Kurven | Live **gelb EIN**; Average **rot EIN**, vorgeschlagen 120 s; Rauschboden **grau AUS**; Max Hold AUS; Peak Marker AUS |
| Cursor | Fadenkreuz AUS; bei EIN lokale Anzeige `Hz / dBFS` |
| Wasserfall | **Inferno** (Default-Vorschlag, Verfügbarkeit FIX), 120 s, 1/3 Spektrum : 2/3 Wasserfall |
| Mithören | **AUS**, Filter 20–80 Hz (Bandbreite 60 Hz), Nutzer-Gain 0 dB, Auto-Abregelung EIN, Monitor-Clip im Hauptbild |
| Statistik | **L50/L90 EIN**, unabhängig 2–25 Hz, rollend 120 s; kurze Linien im Spektrum + numerische Anzeige |
| Superfast | AUS, Maximum Sample AUS; bei Aktivierung digitaler Rohsample-Peak und Reset |
| Mikrofonkalibrierung | AUS; Roh-dBFS |
| Rauschboden-Referenz | keine geladen; graue Kurve AUS; Subtraktion AUS |
| Save | **Auto-PNG AUS**; falls eingeschaltet: erstes Bild nach vollem Wasserfall, dann je Wasserfallzeit; manuelles PNG/CSV möglich |
| Setups | Werkspreset „Brummton 0–100 Hz“; zuletzt verwendetes Setup beim Start; Mithören trotzdem AUS |

## 15. Prüffälle / Akzeptanzmatrix (Ergänzung zu SRS v0.3)

| Test-ID | PASS-Kriterium |
|---|---|
| AT-01 Hauptbild | Normal- und erweiterte Ansicht zeigen **beide** immer Spektrum und Wasserfall; nur Zusatzkarten verschwinden. Tap zweimal stellt Normalansicht her. |
| AT-02 Mithörtap | Zwei kurze Taps auf Feld unten links schalten AUS→EIN→AUS; unabhängige Live-Messung bleibt gleich. |
| AT-03 Mithörswipe | Hoch/runter verändert nur Nutzer-Gain, kein versehentlicher Ein/Aus-Tap oder FFT-Wechsel. |
| AT-04 Mithörband | Gesamt-Drag verschiebt beide Overlay-Grenzen synchron, Bandbreite in Hz konstant; Spektrum/Wasserfall/L50-L90 unverändert. |
| AT-05 Cursor | Kreuz besitzt **X- und Y-Linie**; horizontaler Drag folgt selektierter Kurve mit korrektem Readout; auch in Pause. |
| AT-06 Kurven | Gelb live, Rot Average, Grau Referenz; drei unabhängige Schalter; Average-Zeit nur rote Kurve. |
| AT-07 Inferno | Farbpalette **Inferno** ist verfügbar und in Normal-/erweiterter Ansicht konsistent. |
| AT-08 Superfast | Ein kurzer Rohsample-Peak zwischen UI-Frames wird im Maximum Sample gezeigt, mit Reset; keine Behauptung von True Peak/SPL. |
| AT-09 L50/L90 | 2–25 Hz/120 s; vollständige Fensterkennzeichnung, richtige Perzentilrichtung, Linien nur über Band und korrekte Y-Pegel. |
| AT-10 Auto-Save | 120-s-Wasserfall: PNG bei erster vollständiger Füllung und danach je 120 s, nicht pro Renderframe; „Save aktiv“ korrekt. |
| AT-11 Keine Audioaufnahme | Kein neues WAV/PCM, keine Audio-Replay-Oberfläche und kein Audio-Upload aus Analyzer; andere App-Panels unverändert. |
| AT-12 Monitor-Clip | Eingang und Ausgang getrennt; Output-Abregelung verändert weder Rohsignal noch Spektrum, Kalibrierung, L50/L90 oder Rauschboden. |
| AT-13 Rauschboden | Profil speichern/laden, grau unabhängig zeigen, optionale **Leistungs**subtraktion; negative Restleistung → ungültig; Messpfad erhalten. |
| AT-14 Kalibrierung | Gültiger Geräte-/Bandbezug; ohne absolute Referenz kein SPL, außerhalb gültigem Bereich Markierung. |
| AT-15 Setups | Zwei Setups abwechselnd recall; Frequenzbereich, Anzeige, Auto-Save-Konfiguration, Bandstatistik, Gain, Bandbreite, Kalibrier-ID und Noise-ID identisch; Mithören AUS. |
| AT-16 Fehler | Fehlende Kalibrier-/Rauschbodendatei, Drop, Clip, SAF-Schreibfehler: verständliche Warnung; Live-Audio bzw. gültige letzte Konfiguration bleibt stabil. |

## 16. Änderungsstand gegenüber SRD/SRS v0.3 – jetzt festgehalten

1. Hauptbild im **Portraitmodus**, möglichst nur Spektrum, Wasserfall, überlagerter Mithörbereich; Einstellungen aus Hauptbild ausgelagert.
2. Linear **Standard**; getrennte numerische **Min-/Max-Skalen** für Frequenz und Pegel.
3. **Inferno muss enthalten sein**; als erster Palette-Default vorgeschlagen.
4. Rote Average-Kurve zuschaltbar, grau nur optionaler Rauschboden; gelb live.
5. Zuschaltbarer **Fadenkreuzcursor** aus horizontaler und vertikaler Linie; verschiebbar auch bei Pause; korrektes Readout.
6. Mithörfeld unten links: **Tap AUS/EIN**, vertikales Wischen = Lautstärke; Mithörband horizontal verschiebbar; Breite in **Hz im Spektrumsetup**.
7. Monitoring-Clip und Auto-Abregelung ins Hauptbild; Abregelung **nur im Mithörpfad**.
8. Wasserfall-Tap blendet **nur untere Zusatzanzeigen** aus; Spektrum bleibt; Wasserfall füllt freien Raum.
9. `Save aktiv` = **automatische PNGs** bei erstmals vollständig gefülltem Wasserfall und danach je Wasserfallzeit; v0.3-Beschränkung „nur manuelles PNG“ wird für Bilder ausdrücklich ersetzt. **Keine Audiodateien**.
10. **Superfast** und Maximum Sample mit Reset als zusätzliche, optional aktivierbare Impulsindikatoren.
11. **L50/L90** auf unabhängigem Band **2–25 Hz**, Zeitfenster **120 s**, kurze waagerechte Farbstriche nur im Bandabschnitt + Zahlen, beide abschaltbar.
12. Separates optionales **Rauschbodenprofil** mit Messen/Speichern/Laden, grauer Referenz und rechnerischer Leistungssubtraktion ausschließlich in Anzeige; kein Ersatz der Mikrofonkalibrierung.
13. Mehrfach-Setups/Recall weiterhin umfassend, inklusive neuer Parameter; der aktive Mithörzustand wird **nie automatisch gestartet**.

## 17. Vor Implementierung ausdrücklich offene Entscheidungen

Diese Punkte sind **nicht** als bereits getestete Fakten auszugeben:

- **Superfast:** genaue Anzeigeaggregation (vorgeschlagen 20 ms), Mehrkanalregel, Sample-Peak-Definition und ggf. zusätzliche Impulsalgorithmik. Kein Versprechen höherer Abtastrate ohne echte Geräteunterstützung.
- **Average:** 120 s als Vorschlag; linearer Leistungs-/RMS-Mittelungsalgorithmus und rote Kurvenskalierung genau spezifizieren.
- **Rauschboden:** zulässiger PSD-/Bin-Normierungsabgleich und Unsicherheitsgrenze, reproduzierbare Profilkompatibilität, Verhalten des kompensierten Average und CSV-Exportfelder.
- **Save + Pause:** Der vorgeschlagene Modus „kein wiederholtes identisches PNG in Pause; neuer vollständiger Zyklus nach Fortsetzen“ benötigt finale Produktentscheidung.
- **Speicher beim Neustart:** Ob `Save aktiv` nach Recall automatisch wieder eingeschaltet werden darf, ist aus Datenschutz-/Nutzerperspektive noch zu entscheiden. Der sichere Vorschlag ist AUS bis zur expliziten Aktivierung.
- **Android-Audio:** Verfügbarkeiten von UNPROCESSED, Mikrofonfrequenzgang 2–25 Hz, externe USB-Routen, Kopfhörerlatenz und Clipverhalten am echten Referenzgerät validieren.
- **Anzeige bei nicht gültigen L50/L90-SPL-Werten:** Keine unkalibrierte dBFS-Bandzahl als absoluten Schalldruck ausgeben; Band-/FFT-Normierung und Einheiten in Tests bestätigen.
- **Gestenkollisionen:** Kleine Touchflächen und Band-/Cursor-Interaktion auf echtem Hochformat-Handy im Usability-Test prüfen; numerische Felder bleiben der sichere Alternativweg.
- **Repo-Integration:** Vor Coding die aktuelle `docs/project/`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, vorhandene Tests und den konkreten Android-Quellcode abgleichen. Diese Datei beschreibt **nur das geplante Modul**.

## 18. Dokumentierte Grundlagen / Quellen

1. Projektinterne Funktionsreferenz: `Spectroid_Anleitung_Brummton_v10_5_Zweiter_Teil_Bilder_aus_V9.docx` (bereitgestellte Arbeitsdatei).
2. `SRD_LFN-Scout_Analyzer_v0.3.md` und `SRS_LFN-Scout_Analyzer_v0.3.md` (kumulativer vorangegangener Entwurf).
3. Nutzerseitige GUI-/Funktionsfestlegungen vom 19.09.2026 (linear, Inferno, Cursor, Superfast, PNG-Automatik, L50/L90, Rauschboden, Mithörtap, Wasserfall-Erweiterung).
4. Android-Plattform: [AudioRecord](https://developer.android.com/reference/android/media/AudioRecord), [AudioTrack](https://developer.android.com/reference/android/media/AudioTrack), [MediaRecorder.AudioSource.UNPROCESSED](https://developer.android.com/reference/android/media/MediaRecorder.AudioSource).

**Ende – Referenz v0.4 (Entwurf).**


---

# QUELLDOKUMENT: `assets/concepts/BILDREGISTER_v0.7.md`

# Bildregister und Freigabestatus – LFN-Scout Analyzer v0.7

**Status:** 13 vorhandene nicht ausdrücklich verworfene Benutzer-UI-Konzeptbilder im ZIP. Quelle: in diesem Chat erstellte Bilder, Original-Dateinamen und SHA-256 im `asset_manifest.json`. Die Bilder sind **Designvorschläge, keine echte laufende App und keine Messnachweise**. Normativ ist `../../docs/project/04_UI_Gesten_v0.7.md` zusammen mit SRD/SRS und der Parameterliste. Die originale Benutzer-Spectroid-Screenshot-ZIP liegt ausschließlich unter `references/`.

## 1. Inklusive Bilder und Gebrauch

| ID | Status | Bilddatei | Wofür verwendbar, Grenzen |
|---|---|---|---|
| C01 | HISTORISCH | [C01_Erster_Hauptbild_Einstellbreit_Entwurf.png](assets/concepts/C01_Erster_Hauptbild_Einstellbreit_Entwurf.png) | Früher erster Hauptbild-/Breit-Einstell-Entwurf; wegen Hochformat nicht als Implementierungsvorlage. |
| C02 | HISTORISCH | [C02_L50_L90_und_Save_Fruehentwurf.png](assets/concepts/C02_L50_L90_und_Save_Fruehentwurf.png) | Frühes L50/L90-, PNG- und Superfast-Konzept; enthält grafische Vereinfachungen und überladene Zusatzkarten. |
| C03 | REFERENZ | [C03_Wasserfall_Erweiterung_KORREKT.png](assets/concepts/C03_Wasserfall_Erweiterung_KORREKT.png) | Richtige Umschaltidee: Spektrum bleibt sichtbar, nur untere Zusatzanzeigen verschwinden. Achsentexte im Bild nicht normativ. |
| C04 | REFERENZ | [C04_Rote_Average_Kurve.png](assets/concepts/C04_Rote_Average_Kurve.png) | Rot = Average, Gelb = aktuell. Frühere Header-/Speichertexte können vom aktuellen Soll abweichen. |
| C05 | REFERENZ | [C05_Audio_Settings_schmales_Panel.png](assets/concepts/C05_Audio_Settings_schmales_Panel.png) | Schmales Android-Audio-Einstellpanel mit Mithören und Clip; Beispielwerte, nicht alle Defaults verbindlich. |
| C06 | REFERENZ | [C06_Spektrum_und_Audio_Settings.png](assets/concepts/C06_Spektrum_und_Audio_Settings.png) | Spektrum-/Audio-Register als schmale Android-Einstellungen; exakte Felder laut normativer Parameterliste. |
| C07 | HISTORISCH | [C07_Breites_Settings_Fruehstadium.png](assets/concepts/C07_Breites_Settings_Fruehstadium.png) | Breites Einstellpanel wurde durch einspaltiges Android-Hochformat ersetzt. |
| C09 | REFERENZ | [C09_Sechs_Settings_Register.png](assets/concepts/C09_Sechs_Settings_Register.png) | Sechs schmale Einstellungen-Seiten; Benutzerwerte und technische Beispiele sind keine geprüften Defaults. |
| C10 | REFERENZ | [C10_Linearer_Frequenzbereich.png](assets/concepts/C10_Linearer_Frequenzbereich.png) | Lineare Frequenzachse als Standard. Frühere Headertexte „Aufnahme“ ignorieren; Save bezieht sich nur auf PNG. |
| C11 | REFERENZ | [C11_Fadenkreuz_Cursor.png](assets/concepts/C11_Fadenkreuz_Cursor.png) | Fadenkreuz senkrecht/waagerecht, Punkt und Frequenz-/Pegelreadout auch in Pause. |
| C12 | REFERENZ | [C12_Cursor_Readout_verbessert.png](assets/concepts/C12_Cursor_Readout_verbessert.png) | Kompaktes lokales sowie optionales festes Cursorreadout; Spektrum über Wasserfall. |
| C13 | LEITBILD | [C13_Mithoeren_EIN_AUS_FINAL.png](assets/concepts/C13_Mithoeren_EIN_AUS_FINAL.png) | Letzter ausdrücklich akzeptierter Zwei-Zustände-Entwurf. Tap auf unteres Mithörfeld schaltet EIN/AUS; Messung läuft unverändert. |
| C15 | HISTORISCH | [C15_Erster_Analyzer_Design_Entwurf.png](assets/concepts/C15_Erster_Analyzer_Design_Entwurf.png) | Erster allgemeiner Designentwurf vor Korrektur auf schmale Einstellungen; kein finales Layout. |

**Leitbild:** `C13` = Mithören EIN/AUS durch Tap auf das untere linke Mithörfeld. **Ergänzende Referenzen:** `C11`/`C12` Fadenkreuz/Readout, `C03` Umschalten der unteren Karten bei erhaltenem Spektrum, `C09` kompakte Einstellseiten, `C04` rote Mittelwertkurve, `C10` lineare Frequenzachse. `C05`/`C06` zeigen Audio-/Spektrumeinstellungen. Auch beim Leitbild sind simulierte Daten, UI-Beschriftung und Achsfehler nicht normativ.

## 2. Ausgeschlossen – ausdrücklich vom Nutzer abgelehnt

| Original-Dateiname (NICHT im ZIP) | Grund |
|---|---|
| `a_side_by_side_comparison_gui_mockup_screenshot_of.png` | Darin verschwand das Spektrum beim Vergrößern des Wasserfalls; Nutzer verlangte explizit Spektrum weiter sichtbar. |
| `a_clean_high_resolution_smartphone_app_screenshot.png` | Unerwünschtes neues Hauptbild/Bedienpanel, Nutzer verlangte Wiederherstellung der bisherigen Anzeige. |


Die Breitformat-/frühen Entwürfe `C01`, `C07`, `C15` sind **nur historisch** dabei, weil ihre Darstellung durch die spätere feste Hochformat-Anforderung überholt wurde. Sie sind keine UI-Freigabe. Es gibt **keine Freigabe** für das neue Histogrammschema `D01`.

## 3. Weitere verbindliche Bildregeln

1. Das Spektrum bleibt in Normal- und Wasserfall-Erweitert-Ansicht sichtbar; nur untere Zusatzkarten werden ausgeblendet.
2. Die Frequenzachse ist standardmäßig **linear 0–100 Hz**, alternativ 0–200 Hz. Eine logarithmische/fehlbeschriftete Bildachse ist kein Gegenbeweis.
3. `Save aktiv` = automatische PNG-Grafik, keine Audioaufnahme. Veraltete „Aufnahme aktiv“-Labels nicht übernehmen.
4. In Bildern gezeigte Beispielwerte, Filterbänder, Kurven-/L50-L90-Pegel, Mikrofonkalibrierungen, Histo-Kurven und Clipzustände sind **illustrativ**, nicht echte Messungen. Mithörband und Statistikband sind unabhängig.
5. Sensor-/Histogramm-Daten nur aus dem tatsächlichen LFN-Scout-Datenprovider, nicht aus Bildpixeln oder im Bild erfundenen Zahlen.
6. Keine Übernahme fremder Logos, Markenzeichen, Screenshots oder Code aus einer anderen App ohne gesonderte Rechteprüfung. Die Original-Spectroid-Screenshots nur als funktionale Dokumentationsreferenz verwenden.
