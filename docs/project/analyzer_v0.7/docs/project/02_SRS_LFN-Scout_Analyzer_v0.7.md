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

