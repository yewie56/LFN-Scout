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

