# LFN-Scout – Frequenzaufgelöste Impulserkennung

**Implementierungsspezifikation und Übergabe an den LFN-Scout-Entwicklungs-Chat**  
**Dokument-ID:** LFN-IMP-001  
**Dokumentversion:** 1.0 (24.09.2026)  
**Status:** Neuer fachlich-technischer Entwurf; **nicht implementiert, nicht gebaut und nicht getestet**.  
**Zielplattform:** vorhandene Android-Anwendung **LFN-Scout**; kein zweites Produkt.  
**Empfohlene Ablage:** `docs/project/LFN-Scout_Frequenzaufgeloeste_Impulserkennung_Implementierung_v1.0.md`

> **Auftrag an den nächsten Chat:** Dieses Dokument als neuen, ausdrücklich noch nicht umgesetzten Erweiterungsauftrag behandeln. Vor Codeänderungen den tatsächlich neuesten vollständigen Projektstand und `BUILD_WORKFLOWS.md`, `DIRECTIVES.md`, `CHANGELOG.md`, `PROJECT_STATUS.md` (falls vorhanden), `docs/project/` sowie die verbindlichen UI-Referenzbilder lesen. Entwurfsklassennamen sind **keine Behauptung über vorhandene Klassen**. Anforderungen und Änderungen gegen die reale Repository-Struktur mappen. Bestehendes Verhalten erhalten. Jede nachweislich implementierte Änderung mit Regressionstest, Selbsttest, Dokumentations- und Changelog-Eintrag versehen. Keine Tests als PASS deklarieren, die nicht tatsächlich gelaufen sind.

---

## 0. Leseregeln, Priorität, Ist-/Soll-Trennung

0.1. **IST laut bisheriger Übergabe bzw. bisheriger Chat-Rückmeldung, nicht im Rahmen dieses Dokuments neu geprüft:** Ein gemeinsamer 8-kHz-Rohdatenpfad und ein 1-kHz-LF-Zweig sowie 8-kHz- und teilweise direkte höherfrequente Analyzer-Modi wurden zuletzt besprochen bzw. für einzelne Zwischenstände als implementiert/gemeldet. In einer früheren Rückmeldung wurde v0.5.05 als auf Xiaomi getestet bezeichnet; zu v0.5.09 wurden Hosttests, aber ein offener Android-/Gerätetest berichtet. Diese Angaben sind **keine Verifikation des hier nicht vorliegenden aktuellen Repositorys**. Das ältere Analyzer-Fachpaket v0.7 war ausdrücklich ein Spezifikationsstand, kein App-Release. Die neueste Softwareversion ist beim Einbau direkt aus dem Repository zu bestimmen.

0.2. **BESTANDSSCHUTZ:** Die aktuelle Audioaufnahme, Bewertungen, Datenbank-/Supabase-Schnittstellen, Anzeige, Spektrum, normale Wasserfälle, Laufzeit beim internen Panelwechsel, PNG-Mechanik und bestehende Setups müssen unverändert funktionieren. Die neue Impulsfunktion darf den vorhandenen Android-Audiopfad nicht heimlich neu konfigurieren.

0.3. **Verbindliche UI-Entscheidung aus dem späteren Projektverlauf:** ORIGINAL ist ein kleiner quadratischer dunkler Button mit Mini-Spektrogramm in der vorhandenen Inferno-ähnlichen LUT. FILTER ist gleich groß, dunkel und zeigt **genau fünf** symmetrische vertikale Balken (kurz–mittel–hoch–mittel–kurz) mit Magenta/Rosa/Weiß-LUT. Beide besitzen EIN/AUS-Status und sind unabhängig schaltbar; bei gleichzeitig EIN wird intelligent überlagert. **Keine Umgestaltung dieser zwei Buttons und kein Ersetzen des FILTER-Icons durch ein Spektrogramm.** Referenz ist die ausdrücklich akzeptierte Abbildung; ihr tatsächlich vorliegendes Asset muss beim Einbau geprüft werden. Ein zusätzlicher „Impuls“-Actionbutton wird hier **nicht** freigegeben.

0.4. **Konfliktregel:** Neuere tatsächliche Projektentscheidungen haben Vorrang vor älteren Fachpaketen. Ungeklärte Widersprüche dokumentieren und nicht still durch eine Vermutung lösen. Parameter in diesem Dokument sind als **Vorschlag/Test-Startwert** gekennzeichnet, bis sie im Projekt freigegeben sind.

0.5. Die folgenden Kennungen `IMP-REQ-*`, `IMP-TEST-*` und `IMP-GATE-*` sind neue, hier definierte IDs. Sie dürfen nicht mit alten T- oder AT-Testnummern verwechselt werden.

---

## 1. Ziel, Anwendungsfälle, Nicht-Ziele

### 1.1 Ziel

Ein sehr kurzer akustischer Stoß soll zunächst schnell erkannt und zeitgestempelt werden. Zusätzlich soll er **frequenzaufgelöst** charakterisiert werden: betroffene Bänder, zusätzliche Energie gegenüber dem Hintergrund, Anstiegszeit, Dauer, Frequenz-/Bandprofil, gegebenenfalls Wiederholung. Die Analyse soll auch dann weiterlaufen können, wenn der Anwender zwischen internen Panels wechselt, soweit dies der aktuelle Analyzer-Lifecycle erlaubt.

### 1.2 Hauptfälle

- **A:** Breitbandiger kurzer Druckstoß, eventuell mit überwiegender LF-Energie.
- **B:** Kurzer Tonburst bei beispielsweise 10, 25 oder 50 Hz.
- **C:** Kurzzeitige Pegelerhöhung auf einem fortlaufenden 25-Hz-Brummton.
- **D:** Mehrere dicht folgende Impulse; Unterscheidung tatsächlicher Ereignisse von Filter-Nachschwingen.
- **E:** Sehr tieffrequente Veränderung bei 1–5 Hz; schnelle *vorläufige* Rohsignal-Triggerung bei zugleich verzögerter, unsicherheitsbehafteter Frequenzzuordnung.
- **F:** Sehr kurzer hochfrequenter Stoß, der zwar in 8-kHz-PCM sichtbar sein kann, aber im 1-kHz-LF-Zweig nicht sinnvoll detektierbar ist.

### 1.3 Nicht-Ziele dieser Erweiterung

Kein akustischer Quellennachweis, kein medizinischer Nachweis eines Brummtons, keine automatische Unterscheidung WKA/Verkehr/Haustechnik, keine garantierte Rekonstruktion physikalischer Impulsdauer aus schmalbandig gefilterten Signalen, **keine neue unabhängige WAV- oder PCM-Aufnahmefunktion im Analyzer**, keine Änderung bestehender Hör-/Aufnahmewege und keine neue Supabase-Übertragung ohne gesonderte Datenvertragsfreigabe. Ein reines PNG eines Impuls-Wasserfalls ist kein Audioarchiv.

---

## 2. Physikalische und messtechnische Grenzen

### 2.1 Zeit-Frequenz-Kompromiss

Für eine nicht mit Zero-Padding interpolierte FFT beträgt der Abstand der Stützstellen `Δf = fs/N = 1/T`. Die reale Trennschärfe hängt zusätzlich vom Fenster und der Signalform ab. Beispielwerte:

| Auswertefenster | nomineller FFT-Abstand | Interpretation |
|---:|---:|---|
| 20 ms | 50 Hz | sehr kurze Ereignisse zeitlich sichtbar, keine präzise LF-Frequenz |
| 50 ms | 20 Hz | grobe breitbandige Verteilung |
| 100 ms | 10 Hz | eingeschränkte LF-Zuordnung |
| 500 ms | 2 Hz | besserer LF-Kontext, längere Latenz |
| 1 s | 1 Hz | feinere LF-Struktur, nur wenn Ereignis hinreichend lange anhält |
| 5 s | 0,2 Hz | langfristiger Kontext, **kein** Nachweis, dass ein 20-ms-Stoß eine 0,2-Hz-„Impulsfrequenz“ besaß |

Ein Trigger nach wenigen Millisekunden bedeutet nicht, dass schon nach wenigen Millisekunden das 2-Hz-Spektrum gesichert ist. Auch eine nachträgliche lange FFT über Vor- und Nachlauf schätzt in diesem Fall das Spektrum des *gesamten Ausschnitts* (Ereignis plus Hintergrund), nicht automatisch das des kurzen Impulses allein.

### 2.2 Abtastung und Eingang

Bei 1 kHz ist die Nyquist-Grenze 500 Hz; als robuster geplanter Analysebereich wird 1–200 Hz vorgeschlagen. Im 8-kHz-Rohpfad lassen sich schnellere Änderungen untersuchen, **sofern Mikrofon, Android-Audiokette und Anti-Aliasing diese Information überhaupt liefern**. Ein 48-kHz-Direktmodus darf nur genutzt werden, wenn er im aktuellen Projekt tatsächlich vorhanden und für den Betrieb verfügbar ist. Keine behauptete absolute dB-SPL-Empfindlichkeit bei 1–5 Hz ohne gültige Messkettenkalibrierung.

### 2.3 Verschiedene Zeitangaben

1. `t_capture`: dem PCM-Frame zugeordneter Aufnahmezeitpunkt.
2. `t_trigger`: Zeitpunkt, zu dem der Algorithmus eine Bedingung erfüllt.
3. `t_onset_est`: nachträglich aus dem Rohsignal geschätzter physikalischer Ereignisbeginn; mit Unsicherheit.
4. `t_classified`: Zeitpunkt, zu dem das Spektralprofil nach Vor-/Nachlauf feststeht.

**Diese vier Angaben nie gleichsetzen.** Android-Audiotimestamps müssen nach Verfügbarkeit aus `AudioRecord.getTimestamp(...)` auf Frameposition/monotone Zeitbasis abgebildet werden; bei fehlendem oder unstetem Timestamp auf markierten Fallback und höhere Unsicherheit umschalten.

### 2.4 Nachschwingen

Eine schmale Bandpassantwort kann nach einem kurzen breitbandigen Stoß länger oszillieren. Das ist **zunächst Filterverhalten, keine Folge realer Impulse**. Die maximale Dauer des Nachschwingens muss als Funktion von Filterkoeffizienten und Schwelle synthetisch vermessen werden. Refraktärzeit allein ist kein ausreichender Ersatz für die Beurteilung periodischer echter Impulse.

---

## 3. Soll-Architektur und Signalwege

### 3.1 Überblick

```text
Bereits vorhandener zentraler Android-Audio-Capture (EIN Eigentümer)
      │  PCM-Frames + monotone Frame-ID/Timestamp + Quellenmetadaten
      ├─────────────> bestehende Audioaufnahme (UNVERÄNDERT)
      ├─────────────> bestehender 8-kHz-Analyzer / 48-kHz-Modus, soweit vorhanden
      └─────────────> neuer ImpulseEngine (Feature-Flag, eigene Queue)
                           │
                           ├─ Highspeed-Rohsignal-Trigger (geeignete Originalrate)
                           ├─ vorhandene LF-Decimation oder expliziter geprüfter LF-Zweig
                           ├─ parallele Bandpass-/Mehrskalenbank
                           │       └─ Fast-/Slow-Energie, Anstieg, Clipping-Status
                           ├─ Ereignis-Fusion + Zustandsautomat
                           ├─ Ringpuffer-Zugriff für Vor-/Nachanalyse (flüchtig)
                           └─ immutable ImpulseSnapshot / ImpulseEvent
                                      ├─ UI-Overlay im bisherigen Analyzer
                                      ├─ optionale Impuls-Karte (nur nach UI-Abgleich)
                                      └─ separater Ereignismetadaten-Export, falls freigegeben
```

Die bereits vorhandene automatische PNG-Speicherung bleibt im bisherigen Eigentümer; vorhandene Spektrum-/Wasserfall-Snapshots dürfen durch eine optionale, reproduzierbar gerenderte Ereignisebene ergänzt werden. **„Save aktiv“ bleibt PNG und bedeutet nicht Audioaufnahme.** CSV bleibt entsprechend der vorhandenen App-Semantik manuell, sofern keine spätere Erweiterung ausdrücklich beschlossen wird.

### 3.2 Datenbesitz und Nebenläufigkeit

- Genau **ein** Capture-Eigentümer: `ImpulseEngine` öffnet kein eigenes `AudioRecord` und verändert dessen Sample Rate nicht.
- PCM-Frames sind auf Konsumentenseite unveränderlich. Bei Speicherwiederverwendung Lebenszeit/Referenzierung definieren, sonst veraltete Pufferkopien.
- Capture-Thread darf nicht auf FFT, Dateisystem, Renderer, Netzwerk oder Sperren eines langsamen Impuls-Konsumenten warten.
- Begrenzte Queue mit Metriken `droppedFrames`, `gapCount`, `maxProcessingLagMs`; kein unbemerkter Datenverlust. Nach Überlauf Zustand als *unterbrochen* markieren, Filter-/Referenzzustand kontrolliert neu initialisieren und Ereignisse nicht über die Lücke hinweg verschmelzen.
- Der Renderer bekommt unveränderliche Snapshots; optional Offscreen-PNG denselben Daten-/Zeitstand. Prozess-/Panel-Lifecycle getrennt von View-Lifecycle.
- 8-/1-kHz-Zweige anhand gemeinsamer Original-Frame-IDs statt allein Wandzeit zuordnen; Resampling-Delay berücksichtigen.

### 3.3 Integrationseingänge (vor Implementierung gegen realen Code mappen)

| logische Schnittstelle | Pflichtfelder |
|---|---|
| `PcmFrame` | Quelle, Sample Rate, Kanalzahl, Start-Frame-ID, monotoner Timestamp/Qualität, normalisierter PCM-Block, Clipping-/Unterbrechungsflag |
| `ImpulseConfig` | Algorithmusversion, Betriebszustand, Bandplan, Zeitkonstanten, Schwellen, Gruppen-/Refraktärregel, Export-/Overlayeinstellung |
| `ImpulseSnapshot` | Zeit- und Frequenzraster, Differenzmetriken, Gültigkeit, LUT-/Skalierungsdefinition, Quelle/Setup-ID |
| `ImpulseEvent` | stabile Ereignis-ID, Beginn/Trigger/Ende, Konfidenz-/Qualitätsflags, Bandvektor, Amplitude, Latenz, Revision/Finalisierung |

Diese Namen sind **Rollenbezeichnungen**. Bestehende Klassen, Module und JSON-Felder sollen nach Möglichkeit wiederverwendet werden.

---

## 4. Signalverarbeitung: stufenweise Referenzlösung

### 4.1 Vorverarbeitung

- Rohsignal für Trigger **möglichst vor** 8:1-Mittelung bzw. LF-Decimation abzweigen. Mittelung kann kurze Stöße abschwächen oder auslöschen.
- Eingang `x[n]` in normierten PCM-Einheiten; DC-Offset messen und für LF-Auswertung kontrolliert entfernen. Einen bestehenden Hochpass **nicht unbesehen** übernehmen: ein zu hoher Cutoff kann niederfrequente Druckstöße verzerren. Rohsignalpfad und nachverarbeiteter Analysepfad unterscheidbar halten.
- Eingang-Clipping, NaN/Infinity, Kanalwechsel, Sample-Rate-Wechsel, Filter-Warm-up und fehlende Frames als Gültigkeitsflags führen.
- Keine Vorverstärkung, Kompression oder Mithör-AGC vor dem Mess-Detektor; AGC/Clipping für Mithören gehört ausschließlich in dessen Outputpfad.

### 4.2 Stufe A – schneller Rohsignal-Kandidat

Berechne auf Roh-PCM z. B. schnelle lokale Energie und optional eine robuste Differenzgröße:

`P_raw[k] = (1/M) * Σ x[n]^2` (über `M` Rohsamples),  
`D_raw[k] = P_raw[k] - P_raw[k-1]`.

Dazu eine langsamere, robuste Vergleichsgröße `B_raw`, die bei erkanntem Kandidaten nicht unkontrolliert nachläuft. Eine nur von der Sampleamplitude abhängige Triggerung würde langsame tiefe Wellen mit kurzen Transienten verwechseln; eine nur von der Differenz abhängige Triggerung kann niederfrequente Ereignisse verpassen. Daher beide Merkmale **einzeln protokollieren** und ihre OR-/AND-Verknüpfung über Tests evaluieren.

Vorschlag: Rohsignal-Prüfschritt 5–10 ms; der tatsächlich erreichbare Zeitstempel hängt von Capture-/Blockgröße und Hardware ab. Ein Rohkandidat erhält `PENDING`, nicht sofort die Behauptung eines sicher erkannten Impulses.

### 4.3 Stufe B – parallele frequenzselektive Filterbank

**Startbandplan (experimentell; keine Freigabe):**

| Band-ID | Frequenz [Hz] | typ. schnelle Integrationszeit als Startwert | Hinweis |
|---|---:|---:|---|
| B1 | 1–5 | 500 ms | langsame Bestätigung; Impulszeitpunkt bei 1 Hz nicht millisekundengenau bestimmbar |
| B2 | 5–20 | 150 ms | Bandenergie kann bei 5 Hz erst verzögert repräsentativ sein |
| B3 | 20–60 | 40 ms | schnelle LF-Impulse |
| B4 | 60–120 | 20 ms | kürzere Ereignisse |
| B5 | 120–200 | 10 ms | ggf. erweitern bis sinnvoller Nyquist-/Messkettenbereich |

Die Bandgrenzen, Überlappung, Filterordnung, Durchlasswelligkeit, Sperrdämpfung und Gruppenlaufzeit sind **nach der Impulsantwort** und dem konkreten Mikrofon abzustimmen. Einfache kaskadierte IIR-Biquads/SOS sind Rechenzeit-Kandidaten, aber keine voreingestellte Wahrheit. Filterkoeffizienten müssen bei `fs`-Wechsel neu gerechnet oder geprüft werden. Pole/Stabilität und `f_low > 0`, `f_high < fs/2` validieren. Den unteren Bereich optional in **weitere Unterbänder** aufteilen, falls Messung und Zielanwendung es verlangen; B1 bleibt anfangs eher ein grobes Energiefenster.

### 4.4 Hüllkurven und Hintergrund

Für jedes Band `b`:

`p_b[n] = y_b[n]^2`  
`F_b[n] = a_f F_b[n-1] + (1-a_f) p_b[n]`  
`S_b[n] = a_s S_b[n-1] + (1-a_s) p_b[n]`  
`a = exp(-1 / (fs * tau))`.

`F_b` ist schnelle geglättete **Leistung**, `S_b` die langsame **Leistungsreferenz**. Die schnelle Zeitkonstante ist je Band verschieden; beispielsweise ist 10 ms in B1 nicht repräsentativ. Der langsame Referenzwert kann über einige Sekunden bis deutlich länger ermittelt werden. Bei Impulskandidaten Referenz **einfrieren oder nur begrenzt weiterführen**, danach kontrolliert freigeben. Optional robuste Median-/Quantil-Referenz aus ereignisfreien Abschnitten als späterer Algorithmus-Switch.

Berechne

`R_b = 10 log10((F_b + eps_b) / (S_b + eps_b))` in dB relativer Leistungsanstieg,  
`A_b = max(0, R_b[k] - R_b[k-h]) / Δt` in dB/s.

`eps_b` ist eine kleine, datenabhängig oder numerisch begründete Energieuntergrenze; festen „Fake-Boden“ nicht als Messwert ausgeben. `F_b`, `S_b`, `R_b` und `A_b` haben **verschiedene Einheiten**. Die Anzeige darf sie nicht unbeschriftet austauschen.

Eine Hilbert-Hüllkurve ist ein alternativer Kandidat für engere Bänder; die hier genannte exponentielle Leistungsglättung ist zunächst leichter kausal streambar. Änderungen über Algorithmusversion und Regression dokumentieren.

### 4.5 Entscheidungslogik

Vorgeschlagene, zunächst testweise Regel je Band:

- `valid_b`: Signal gültig, Filter warm, keine ungemeldete Lücke; Clipping gesondert kennzeichnen.
- `excess_b`: `R_b >= THRESHOLD_REL_DB[b]`.
- `rise_b`: `A_b >= THRESHOLD_RISE_DB_PER_S[b]` oder hinreichend schneller Betragssprung.
- `energy_b`: absolute Bandenergie bzw. SNR über minimalem Messketten-/Rauschbodenwert, falls gültig bestimmt.
- `candidate_b = valid_b && excess_b && rise_b && energy_b` als **Start-Hypothese**. Durch Referenzdatensätze prüfen, ob `rise_b` in B1 gelockert werden muss und ob ein Kandidat ohne parallelen Rohtrigger zulässig ist.

Ein erstes grobes Beispiel `THRESHOLD_REL_DB = 6 dB` ist **kein nachgewiesener Grenzwert**, insbesondere nicht bei unterschiedlichen Bandbreiten, Quellen und Telefonmikrofonen. Standardwerte **erst nach Receiver-Operating-Characteristic-/Fehlalarmversuchen** festlegen; bis dahin explizites `experimental`-Preset.

### 4.6 Fusion und Nicht-Doppelzählung

- Zeitlich zusammengehörige Bandkandidaten innerhalb einer **laufzeitkorrigierten**, bandabhängigen Zusammenführungsbreite zu einem Ereignis bündeln.
- Rohtrigger kann einen frühen Zeitpunkt setzen; B1 darf das Ereignis später ergänzen, aber nicht rückwirkend Präzision vortäuschen.
- Eigenständige Impulse in einer Folge bleiben separat, wenn im Originalsignal neue unabhängige Onsets erkennbar sind; bloßes Nachschwingen wird derselben Ereignis-ID zugeordnet.
- Bei nur schmalbandigem Kandidaten einen entsprechend markierten Ereignistyp `BAND_ONLY` zulassen, sofern die Tests dessen Aussagekraft stützen.
- Ein `CLIPPED`-Ereignis kann erkannt werden, aber amplituden- und spektralquantitative Eigenschaften sind ungültig bzw. eingeschränkt.

### 4.7 Zustandsautomat (pro Ereignisgruppe)

```text
IDLE → RAW_PENDING oder BAND_PENDING
     → CONFIRMED (Mindestbedingungen erfüllt)
     → ACTIVE (weitere Bandantworten/Peaks sammeln)
     → POSTROLL (Ereignisende + Kontext)
     → FINALIZED (Spektralprofil und Qualitätsflags endgültig)
     → IDLE
Sonderfälle: CANCELLED_FALSE_CANDIDATE / INTERRUPTED_GAP / CLIPPED / CONFIG_CHANGED
```

Jedes `ImpulseEvent` erhält eine stabile ID; die Anzeige darf einen vorläufigen Eintrag nachreichen/aktualisieren. `FINALIZED` muss bei langen B1-Fenstern bewusst später auftreten können. Bei STOP/Panelwechsel je nach Capture-Lifecycle ausstehende Ereignisse korrekt abschließen oder explizit unterbrochen markieren.

### 4.8 Stufe C – nachgelagerte spektrale Detailanalyse

Für ein bestätigtes Ereignis einen Ausschnitt mit Vor- und Nachlauf aus dem **bereits existierenden oder neu als flüchtiges Datenmodell vorgesehenen** Ringpuffer nehmen. Vorschlag: 2–5 s Vorlauf plus ereignisabhängiger Nachlauf, speicher- und quelldatenabhängig. Falls Speicher-/Privacy-Vorgaben entgegenstehen, nur notwendige flüchtige Sampledaten halten.

Drei getrennte Darstellungen:

1. **Rohsignal-Zeitbild** mit nachvollziehbar geschätztem Onset, Peak, Ende.
2. **Bandenergie-/Anstiegsbild**: zeitlicher Verlauf von `R_b` bzw. `A_b` je Band (explizit pro Band unterschiedliche Glättung und Laufzeit). Dies ist die primäre Impulskarte.
3. **STFT-Vergleich**: kurze Fenster für die Stoßphase; längere Fenster zur Analyse anhaltender Resonanz und des Vor-/Nachhintergrunds. Die Spektralwerte des Hintergrunds vor und nach dem Ereignis getrennt kennzeichnen; optionale lineare Leistungsdifferenz nur bei valider Vergleichbarkeit, nie unkontrolliert dB-Werte subtrahieren.

Die vorhandenen regulären FFT-/Wasserfall-Einstellungen bleiben unverändert; eventuelle Zusatzfenster sind getrennt konfiguriert. Zero-Padding verändert die Frequenzrasterdichte, **nicht** die aus kurzer Signaldauer gewinnbare Trennschärfe. Eine Wavelet-/Multi-Resolution-Variante kann später als ausdrücklich versionierter Algorithmus-Switch verglichen werden, nicht als ungetesteter Ersatz der Basisimplementierung.

### 4.9 Ereignisintensität und physikalische Bedeutung

Als vergleichbares Primärmerkmal wird pro Band der **relative Leistungsüberschuss `R_b` in dB** gespeichert. Zusätzlich `F_b`/Peak in digitalen Einheiten (und bei freigegebener Kalibrierung separat dB SPL); niemals `R_b` als absoluten Schalldruckpegel beschriften. Ein skalarer „Gesamt-Impulsscore“ wäre eine **abgeleitete Heuristik**, nicht eigenständig ein physikalisches dB-Maß. Falls implementiert: klare Formel, Algorithmusversion und Einheitenlosigkeit festhalten.

---

## 5. Parametervertrag und persistente Setups

### 5.1 Grundsatz

Die vorhandenen Analyzer-Parameter und Setups dürfen nicht umbenannt oder still überschrieben werden. Neuer Namensraum z. B. `impulseAnalysis` mit einer **eigenen Schema- und Algorithmusversion**. Die konkrete Verschachtelung erst nach Sichtung des aktuellen Setup-Schemas anpassen. Unbekannte Felder nach geltender Projektdirektive erhalten/behandeln, alte Setups mit **Funktion AUS** verlustfrei laden. Nicht persistieren: Live-Filterzustand, Ereignisqueue, laufende Frame-ID, zufällige Event-ID oder Status `ACTIVE`.

### 5.2 Start-Parameter (nicht normativ)

| Konfigurationsschlüssel (Entwurfsname) | Vorschlag | Zweck / Validierung |
|---|---|---|
| `enabled` | `false` bis Abnahme | Feature-Flag und Rückfall |
| `algorithmVersion` | `FB_FAST_SLOW_V1` | reproduzierbare Auswertung |
| `inputPreference` | `SHARED_RAW_PLUS_LF` | nur verfügbare Bestandsquelle wählen |
| `analysisMinHz`/`analysisMaxHz` | `1` / `200` | im gültigen Nyquist-Bereich |
| `bandsHz` | `[[1,5],[5,20],[20,60],[60,120],[120,200]]` | nicht leer, sortiert; Überlappung definiert |
| `hopMs` | `10` | muss zur effektiven Samplerate passen |
| `fastTauMs` | `[500,150,40,20,10]` | positiv, frequenzabhängig |
| `slowTauMs` | `[10000,10000,5000,5000,5000]` | positive Zeitkonstante, vorläufig |
| `relativeThresholdDb` | `[6,6,6,6,6]` | nur experimenteller Startwert |
| `riseThresholdDbPerSec` | zunächst `null` / zu kalibrieren | **keine erfundenen fixen Produkt-Defaults** |
| `warmupSec` | `max(3*slowTau)` oder explizit Status „Referenz unreif“ | Startfehler vermeiden; 30 s für 10-s-Tau als rechnerischer Vorschlag |
| `preRollSec`/`postRollSec` | `3` / `3` | flüchtiger Kontext, Speicher prüfen |
| `eventMergeWindowMs` | band-/filterabhängig ermitteln | keine feste, universelle Schwelle |
| `renderMode` | `MARKERS_ONLY` | initial ohne UI-Redesign |
| `persistEventMetadata` | `false` bis Freigabe | Datenschutz / aktuelles Dateikonzept |
| `debugMetrics` | `false` | bei Aktivierung Diagnose; keine PCM-Leaks |

Fehlerhafte Setups müssen **vor Aktivierung** abgelehnt oder nach explizit dokumentierter Migrationsregel behandelt werden. Änderungen der Filterparameter während einer Session bilden eine neue Konfigurationsepoche; warm-up und Validität neu starten.

### 5.3 Illustratives JSON (nicht blind ins Repository übernehmen)

```json
{
  "impulseAnalysis": {
    "schemaVersion": 1,
    "enabled": false,
    "algorithmVersion": "FB_FAST_SLOW_V1",
    "inputPreference": "SHARED_RAW_PLUS_LF",
    "analysisMinHz": 1.0,
    "analysisMaxHz": 200.0,
    "bandsHz": [[1,5],[5,20],[20,60],[60,120],[120,200]],
    "hopMs": 10,
    "fastTauMs": [500,150,40,20,10],
    "slowTauMs": [10000,10000,5000,5000,5000],
    "relativeThresholdDb": [6,6,6,6,6],
    "riseThresholdDbPerSec": null,
    "preRollSec": 3,
    "postRollSec": 3,
    "renderMode": "MARKERS_ONLY",
    "persistEventMetadata": false
  }
}
```

---

## 6. GUI- und Bedienkonzept ohne Bruch der Freigabe

### 6.1 Zwingend unverändert

- ORIGINAL- und FILTER-Button wie in Abschnitt 0.3; ihre Bildvorlage ist eine **separate bestehende Freigabe**.
- Spektrum und bestehender Wasserfall einschließlich dessen Bedienung, normal/erweitert, Farbskalen/Colorbars und LFN-spezifischer Anzeige.
- Bei internem Panelwechsel ggf. Analyzer-Session und automatische PNGs wie im tatsächlich implementierten aktuellen Stand; keine künstliche Pause durch neue Impulsanzeige.
- Rechts neben dem Wasserfall wahlweise 0/1/4 echte bestehende Radiushistogramme. Keine Veränderung ihrer Semantik oder Datenquelle.

### 6.2 Minimaler Einbauvorschlag

Unter **Einstellungen → Analyzer → Impulsanalyse** (nur soweit mit existierender Navigationsstruktur vereinbar): Master-EIN/AUS, Test-Preset, Frequenzbereich, Bandplan (Expertenmodus), relative Schwellen, Zeitkonstanten, Marker-EIN/AUS sowie ggf. Diagnose. Auf dem Hauptbild **keinen dritten frei erfundenen Actionbutton**. Standard zunächst dünne zeitliche Ereignismarker im vorhandenen Wasserfall, identifizierbar im Status/Tooltip und in exportierten PNGs, falls der Snapshotvertrag das zulässt. Die Entscheidung über ein zusätzliches dauerhaftes Impuls-Wasserfall-Unterpanel bleibt gesondert und benötigt freigegebenes UI-Layout.

### 6.3 Falls eine Impulskarte später freigegeben wird

- X = Zeit, Y = Frequenzband bzw. eindeutig zugeordnete Bandmitte (1–200 Hz); keine falsche Behauptung einer feingranularen kontinuierlichen Frequenzauflösung bei nur fünf Bändern.
- Pixelwert = `R_b` [dB über Hintergrund] **oder** `A_b` [dB/s], per gewähltem Modus und Colorbar eindeutig beschriftet.
- Rohsignal-Trigger, bestätigtes Ereignis und finale Charakterisierung durch unterschiedliche Markerzustände; keine neue Palette ohne UI-Abgleich.
- Legende enthält Bandgrenzen, pro Band zeitliche Glättung, Event-ID, Onset-Schätzintervall und Messketten-Gültigkeit.
- Zeitbasis mit Originalwasserfall synchronisieren, auch bei Pause, Zoom/Pan, Panelwechsel und PNG-Snapshot.

### 6.4 Fehleranzeige

Mindestens Status `OFF`, `WARMING_UP`, `RUNNING`, `INPUT_UNAVAILABLE`, `DATA_GAP`, `OVERLOAD`, `CLIPPING`, `CALIBRATION_UNAVAILABLE` (Kalibrierstatus unabhängig von digitaler Erkennung). Keine „0 Impulse“ ausgeben, wenn wegen Datenlücke gar keine gültige Messung vorlag.

---

## 7. Ereignisdaten, Export, Datenschutz

### 7.1 Vorgeschlagener `ImpulseEvent`-Vertrag

```json
{
  "schemaVersion": 1,
  "algorithmVersion": "FB_FAST_SLOW_V1",
  "eventId": "illustrative-unique-id",
  "sessionId": "illustrative-session-id",
  "status": "FINALIZED",
  "sourceId": "microphone-route-id",
  "setupId": "existing-setup-id",
  "sampleRateHz": 1000,
  "rawSampleRateHz": 8000,
  "onsetFrame": 123456,
  "triggerFrame": 123472,
  "onsetMonotonicNs": null,
  "onsetUtc": null,
  "timestampQuality": "FRAME_RELATIVE_ONLY",
  "onsetUncertaintyMs": null,
  "durationMs": null,
  "bandEdgesHz": [[1,5],[5,20],[20,60],[60,120],[120,200]],
  "relativeExcessDb": [null,null,null,null,null],
  "riseDbPerSec": [null,null,null,null,null],
  "absoluteLevelDbSpl": null,
  "flags": ["ILLUSTRATIVE_NOT_MEASURED"],
  "configEpoch": 1
}
```

Alle `null`-Felder zeigen, dass Daten **nicht vorliegen**. Beispielzahlen sind ausdrücklich keine Messung. Timestamp-Mapping auf UTC nur, wenn UTC-/Monotonic-Beziehung dokumentiert ist; bei Zeitsprüngen Qualität herabsetzen. `onsetFrame`/`triggerFrame` müssen auf **explizit bezeichnete Samplerate und Ursprung** bezogen sein; ansonsten separate rohe und decimierte Framepositionen führen.

### 7.2 Speichern

Ein internes flüchtiges Event-Ringlog dient der Anzeige. Persistenz/exportierte JSON-/CSV-Ereignismetadaten sind **optional**, bis vorhandenes Datenmodell, Nutzerfreigabe, Datenschutz und Schema entschieden sind. Nichts still in die bestehende Bewertungs- oder Supabase-Tabelle hochladen. Falls gesonderte Audioaufzeichnung später erwünscht: eigener, explizit freizugebender Auftrag; aktuell **nicht Bestandteil** dieses Dokuments. PNG-Speichern des vorhandenen Wasserfalls muss unabhängig davon bleiben.

---

## 8. Implementierungsschritte und Integrationsgates

### IMP-GATE-0 – Bestandsaufnahme

1. Tatsächliche Repo-Version, Git-Commit, Buildumgebung, Produkt-/Modulnamen und offiziellen Buildweg protokollieren.
2. Obige Referenzdokumente, Quellcode und aktuellen UI-Asset-Freigabestand lesen; einen **Mapping-Plan realer Klassen** erstellen.
3. Funktionierende Referenz messen: 8-kHz- und LF-Datenpfad, eventuelle 48-kHz-Exklusivität, bestehende Aufnahme parallel, PNG unter internem Panelwechsel, Ursprung der Histogramme, Settings-Schema und Renderer.
4. Auf dem Gerät die erreichbaren Sampleraten/Audioquellen und Roh-/LF-Raten prüfen; offene Angaben ausdrücklich offen lassen.

### IMP-GATE-1 – isolierter DSP-Referenzhost

Einen deterministischen Testhost mit demselben portablen Algorithmuskern (oder strikt äquivalentem Referenzmodell) auf synthetische Signale aufsetzen. Rohtrigger, Filterantwort, Bandleistungen, Backoff und Event-Fusion durch gespeicherte Fixtures verifizieren. Keine Anpassung von Schwellwerten anhand nur eines einzigen Testfalls. FFT-/Bandmetrik gegen ein unabhängiges SciPy-Referenzskript prüfen; das Skript gehört zum Testmaterial und ist nicht zwingend Android-Laufzeitabhängigkeit.

### IMP-GATE-2 – PCM-Anschluss ohne UI

Engine als Feature-Flag an bestehenden PCM-Fan-out anschließen. Betrieb AUS muss bitgenau oder innerhalb vorher definierter unvermeidlicher Timing-Toleranzen das alte Signal und alle relevanten App-Funktionen erhalten. Kein zusätzlicher `AudioRecord`; Queue-/Drop-/Warm-up-/Ressourcenmetriken implementieren.

### IMP-GATE-3 – minimaler UI-Einbau

Einstellungen, Marker und Diagnose ohne Änderungen der zwei freigegebenen Actionbuttons integrieren. Screenshot-/Snapshot-Test, Skalierung/Rotation/Panelwechsel/Pause/STOP prüfen. Separate Impulskarte nur nach gesonderter Layoutentscheidung.

### IMP-GATE-4 – Persistenz / optionaler Export

Schema-Versionen und Legacy-Setup-Migration testen. Nur beschlossene Event-Metadaten exportieren; bisheriges PNG-Verhalten schützen. Keine stille Änderung von Audioaufnahme/Netzwerk.

### IMP-GATE-5 – reale Android-/Xiaomi-Validierung

Langer Dauerlauf mit gleichzeitigem Analyzer/Bestandsaufnahme/PNG, Testsignal, Panelwechsel, Display-Sperre soweit laut aktuellem Lifecycle unterstützt, Audio-Routing-Wechsel, STOP und Neustart. Zeitversatz und erkannte Ereignisse mit externem Referenzsignal prüfen; Telefonmikrofon bei 1–5 Hz nicht ungeprüft als messfähig ausgeben.

### IMP-GATE-6 – Release

Ausgeführte Tests mit tatsächlichem PASS/FAIL/SKIP, Device/OS, Commit, Setup, Reproduktionsanweisung und Artefakten ablegen. Betroffene `docs/project/`-Dateien, Bedienung, `CHANGELOG.md`, `PROJECT_STATUS.md` aktualisieren. Vollständigen installierbaren Projektstand nur bei tatsächlicher Prüfung als entsprechend getesteten Stand ausgeben.

---

## 9. Reproduzierbarer Test- und Validierungsplan

**Jeder Test enthält:** Test-ID, Fixture/Quellensignal, Samplerate, erwartete beobachtbare Größen, Mess-/Akzeptanzgrenze, tatsächliche Werte, Commit, PASS/FAIL/SKIP und Log-/Screenshotpfad. Ohne Durchführung `NOT RUN`.

| ID | Testsignal / Situation | Erwartetes Verhalten |
|---|---|---|
| IMP-TEST-01 | 0-Signal / Ruhe | keine erfundenen Events, definierter Rauschbodenstatus |
| IMP-TEST-02 | stationärer 25-Hz-Sinus | kein dauernder Impulsalarm nach Warm-up |
| IMP-TEST-03 | stationärer 25-Hz-Sinus mit Rauschen | Fehlalarmrate über vorab definiertes Zeitfenster messen |
| IMP-TEST-04 | einzelner digitaler Sampleimpuls | frühes Rohkandidat-Ereignis, gefiltertes Ringing **nicht** doppelt zählen |
| IMP-TEST-05 | 10-ms-/20-ms-/50-ms-Breitbandburst | Abhängigkeit von Länge/Amplitude/Band reproduzierbar |
| IMP-TEST-06 | 10-Hz-Tonburst über mehrere Perioden | plausible langsamere B2-Bandklassifizierung |
| IMP-TEST-07 | 25-Hz-Tonburst auf stationärem 25-Hz-Hintergrund | Zusatzereignis gegenüber adaptiver Referenz |
| IMP-TEST-08 | 50-Hz-Tonburst / variable Hüllkurve | B3/B4-Zuordnung abhängig von Bandkanten protokolliert |
| IMP-TEST-09 | 1-/2-/5-Hz-Bursts | unzureichende Zeit-/Frequenzsicherheit ehrlich kenntlich |
| IMP-TEST-10 | zwei Impulse mit 20/50/100/200 ms Abstand | Event-Trennung vs. Merge parametrisch vermessen |
| IMP-TEST-11 | periodische Impulsfolge 8,4 Hz | keine Filter-Ringing-Doppelzählung; echte Wiederholungen erkennbar nach geeigneter Parametrierung |
| IMP-TEST-12 | wechselnder lauter Hintergrund ohne kurze Flanke | Fehltrigger/Hintergrund-Freeze prüfen |
| IMP-TEST-13 | sehr langsamer Pegelanstieg | keine fälschliche Millisekunden-Onset-Behauptung |
| IMP-TEST-14 | kurzzeitige digitale Übersteuerung | `CLIPPED`; SPL-/Amplitude-Unsicherheit gesetzt |
| IMP-TEST-15 | Dropout / fehlender PCM-Block | Gapflag, Reset/Neu-Warm-up, keine Event-Fusion über Lücke |
| IMP-TEST-16 | PCM-Blockgröße wechseln | Eventzeit im Framekoordinatensystem konsistent |
| IMP-TEST-17 | 8-kHz-/1-kHz-Parallelpfad | Bandprofil/Onset-Samplezuordnung inkl. Decimation-Delay |
| IMP-TEST-18 | Quellen-/Sample-Rate-Wechsel | gültige Filterrekonfiguration, Epochenseparation |
| IMP-TEST-19 | Onset vor und nach Ringpuffergrenze | kein Buffer-Overread, fehlender Kontext markiert |
| IMP-TEST-20 | Legacy-Setup ohne neuen Block | verlustfreies Laden; Impulsfunktion AUS |
| IMP-TEST-21 | ungültige Bänder/negative Tau/NaN | sicher abgewiesen, verständliche Fehlermeldung |
| IMP-TEST-22 | Setup speichern → Neustart → laden | nur Konfiguration persistiert; keine flüchtigen Zustände |
| IMP-TEST-23 | Impulsfunktion AUS/EIN während Analyzer | Bestandsdaten und UI unverändert bei AUS; Reset korrekt bei EIN |
| IMP-TEST-24 | ORIGINAL/FILTER einzeln/zusammen | zwei **freigegebene Icons** und Modi unangetastet |
| IMP-TEST-25 | PNG während anderem internem Panel | bisherige Save-Semantik/zeitliche Zuordnung erhalten |
| IMP-TEST-26 | bestehende Audioaufnahme parallel | keine zweite Capture-Instanz, kein stiller Quellenkonflikt |
| IMP-TEST-27 | 0/1/4 Radiushistogramme | unveränderte echte Histogrammprovider- und Layoutfunktion |
| IMP-TEST-28 | Dauerlauf 24–72 h, wo möglich | CPU/RAM/Akku/GC/Queue-Lag protokollieren; keine Speicherleck-/Eventflut |
| IMP-TEST-29 | Kamera-/Mikrofonberechtigung und Routing | kontrollierter Fehler statt Hänger |
| IMP-TEST-30 | UTC-Wanduhrsprung / monotone Zeit | Framezeit stabil, UTC-Qualität entsprechend markiert |
| IMP-TEST-31 | pausieren / STOP / Panelwechsel | Event-Finalisierung entsprechend tatsächlichem Lifecycle |
| IMP-TEST-32 | identische Eingabe + identische Config | deterministische Ergebnisse innerhalb dokumentierter FP-Toleranz |
| IMP-TEST-33 | unterschiedliche Filterordnungen / Bandbreiten | Ringing, Latenz, Detektionsfehler quantitativ verglichen |
| IMP-TEST-34 | Kalibrierung fehlt / unplausibel | keine absolute dB-SPL-Aussage, relative dB-Werte zulässig |
| IMP-TEST-35 | reale Hardware + Referenzsensor | Empfindlichkeit/Onset-Latenz pro Band **gemessen**, nicht aus Theorie behauptet |

### 9.1 Akzeptanzkriterien festlegen statt erfinden

Eine harte Millisekundengrenze für 1–5 Hz wäre nicht gerechtfertigt. Für synthetische digitale Rohimpulse kann eine **vorläufige Zielgröße** der Onset-Fehler relativ zum Referenzsample in der Größenordnung eines Verarbeitungshops (etwa 10 ms bei 10-ms-Hop) getestet werden, jedoch nicht als Hardwaregarantie. Für jede Bandklasse separat Detektionswahrscheinlichkeit, Fehlalarme pro Stunde, Latenzverteilung (Median/P95), Merge-/Doppelzählrate und Übersteuerungsquote festlegen. Grenzen vor Blindtest auf einem unabhängigen Fixture-Set fixieren.

### 9.2 Muss dokumentiert werden

Frequenzgang/Empfindlichkeit der gesamten Messkette; Filterkoeffizienten oder reproduzierbare Filterdefinitionen; Preprocessing; dB-Referenz; Datentyp/Clipping; Warm-up; Stichproben-/Timestamp-Vertrauen; genaue Hop-/Fensterzuordnung und Geräte-/Android-Version. Ergebnisse mit den vorherigen Analyzer-Regressions- und Selbsttests zusammenführen.

---

## 10. Anforderungsliste und Traceability

| ID | Anforderung | zu prüfende Tests |
|---|---|---|
| IMP-REQ-001 | Kein zweiter Capture-Eigentümer | 17, 26 |
| IMP-REQ-002 | Rohsignal-Trigger vor zeitlich glättender LF-Verarbeitung | 04, 05, 17 |
| IMP-REQ-003 | Bandabhängige kausale Energieanalyse | 06–09, 33 |
| IMP-REQ-004 | Adaptive Slow-Referenz mit kontrolliertem Freeze | 02, 07, 12 |
| IMP-REQ-005 | Ereignisbeginn/Trigger/Finalisierung getrennt | 04, 09, 30–32 |
| IMP-REQ-006 | Kein Doppelzähl-Effekt durch Filter-Ringing | 04, 10, 11, 33 |
| IMP-REQ-007 | Datenlücken/Clipping und Unsicherheit sichtbar | 14, 15, 19, 34 |
| IMP-REQ-008 | Auswertung/Renderer/PNG mit konsistentem Snapshot | 24, 25, 31 |
| IMP-REQ-009 | Bestehende ORIGINAL-/FILTER-Buttons bleiben unverändert | 24 |
| IMP-REQ-010 | Bestehende Audioaufnahme, Histogramme, Panel-Lifecycle erhalten | 25–27, 31 |
| IMP-REQ-011 | Legacy-Setups und Versionierung kompatibel | 20–23 |
| IMP-REQ-012 | Keine unkalibrierte absolute dB-SPL-Behauptung | 09, 34, 35 |
| IMP-REQ-013 | Begrenzte Ressourcen, Messung von Lag/Drops | 15, 28 |
| IMP-REQ-014 | Keine neue persistente Audio-/Cloud-Übertragung | 22, 26 sowie Code-Audit |
| IMP-REQ-015 | Regressionen und Selbsttest bei jeder Änderung | gesamter Plan |

---

## 11. Geordneter Einbauauftrag (copy/paste für Entwickler)

1. Aktuelles LFN-Scout-Repository / vollständig übergebenes Projekt-ZIP **zuerst** gegen Projektstatus, Changelog, offizielle `docs/project/`, Build-Workflows und freigegebene UI-Referenz prüfen. Aktuellen Commit/Version und tatsächlich vorhandene Audio-/GUI-Klassen in einer Mapping-Tabelle dokumentieren.
2. Diese neue Spezifikation in `docs/project/` aufnehmen, zunächst `Status: geplant`; noch **keine** Softwareversionsnummer wegen Dokumentannahme erhöhen. Änderungen der SRD/SRS/SDS/Bedienung und Traceability vorbereiten.
3. Die neue Analyse als ausschaltbaren, testbaren Konsumenten des vorhandenen PCM-Fan-out realisieren. Bestandsaufnahme und Analyzer dürfen nicht gegeneinander um AudioRecord konkurrieren; 8-kHz-/1-kHz-Laufzeiten und eventuelle Exklusivmodi respektieren.
4. Mit **deterministischen synthetischen Fixturedaten** zuerst Rohtrigger, Bandpass-/Energieanalyse, Hintergrund-Freeze, Bandfusion und Ringing-Vermeidung implementieren. Parameter nicht als wissenschaftlich validierte Grenzwerte ausgeben.
5. Die Spektralnachanalyse erst auf bereits bestätigte Events aufsetzen. Zeitangaben, dB-Definitionen und Unsicherheit durchgehend getrennt führen.
6. Minimalen UI-Einbau ohne Redesign der ORIGINAL-/FILTER-Buttons liefern; eine neue Impulskarte oder weiterer Actionbutton benötigen eigene Layoutfreigabe. Export-/Cloud-/Audioaufnahmeänderungen bleiben ausgeschlossen.
7. Sämtliche Tests `IMP-TEST-01…35` plus die bestehenden Regressionen und Selbsttests durchführen bzw. ehrlich als `NOT RUN`/`SKIP` dokumentieren. Mindestens den tatsächlichen offiziellen Android-Build und Xiaomi-Lauf verifizieren, bevor eine Gerätefreigabe behauptet wird.
8. Vollständigen, versionierten Projektstand einschließlich aktualisierter Dokumente, Testlogs und Change-/Status-Dateien liefern. Keine bloßen Diffs, keine unbestätigten PASS-Aussagen.

---

## 12. Offene Entscheidungen für das Repository-Mapping

- Was ist die neueste **wirklich vorliegende** Projektversion/Commit- und Testsituation? Der hier berichtete Zwischenstand kann überholt sein.
- Welche vorhandene Klasse verwaltet den zentralen Audio-Capture, seine Ringpuffer, die 8:1-Decimation, Sample-Timestamps und die aktuelle Aufnahme?
- Ist der höherfrequente Direkteingang zur selben Zeit wie LF-Daten verfügbar, oder ein exklusiver Modus? Falls exklusiv: keine implizite Modusänderung durch die Impulsfunktion.
- Wie groß ist der verfügbare Vorlaufpuffer, welche Speicher-/Privacy-Vorgaben gelten und was geschieht beim Panelwechsel?
- Wie ist das **aktuelle** Setup-Schema verschachtelt, und wie werden neue Felder und Legacy-Versionen migriert?
- Soll zunächst lediglich ein Marker sichtbar sein oder später eine eigene frequenzaufgelöste Ereigniskarte? Keine Änderung der zwei Actionbuttons ohne neue Entscheidung.
- Wie soll der Anwender relative Bandüberschüsse vs. absolute, kalibrierte Pegel interpretieren, insbesondere für Infraschall unterhalb der validierten Mikrofonbandbreite?
- Werden Ereignismetadaten überhaupt dauerhaft gespeichert, und falls ja wo und wie lange? **Keine automatische Supabase-Übertragung als Annahme**.
- Welche Grenzwerte gelten nach dem unabhängigen Detektions-/Fehlalarmtest als Produkt-Defaults?

---

## 13. Quellen und technische Nachschlagewerke

Die folgenden Quellen belegen **Methodik bzw. APIs**, nicht die Leistungsfähigkeit des konkreten LFN-Scout-Codes oder eines bestimmten Smartphones.

1. **Android `AudioRecord` – API und `getTimestamp`**: https://developer.android.com/reference/android/media/AudioRecord  (Frameposition, Zeitskala, Capture-/Leseverhalten).
2. **Android `AudioTimestamp`**: https://developer.android.com/reference/android/media/AudioTimestamp (monotone Zeitbasis und Frameposition).
3. **SciPy `signal` – Filter, Hilbert, Resampling**: https://docs.scipy.org/doc/scipy/reference/signal.html (Referenzwerkzeuge; nicht als Android-Bibliotheksvorschrift verstehen).
4. **SciPy `ShortTimeFFT`**: https://docs.scipy.org/doc/scipy/reference/generated/scipy.signal.ShortTimeFFT.html (STFT und Zeit-/Frequenzkoordinaten).
5. **SciPy `ShortTimeFFT.spectrogram`**: https://docs.scipy.org/doc/scipy/reference/generated/scipy.signal.ShortTimeFFT.spectrogram.html (Leistungsspektrogramm).
6. **SciPy `find_peaks`**: https://docs.scipy.org/doc/scipy/reference/generated/scipy.signal.find_peaks.html (Peak-Eigenschaften, Abstand, Prominenz).
7. **Tary et al., „Spectral estimation—What is new? What is next?“, Reviews of Geophysics (2014)**: https://doi.org/10.1002/2014RG000461 (Zeit-/Frequenzlokalisierung von STFT und Wavelets).
8. **Iatsenko, McClintock und Stefanovska (2015), Zeit-Frequenz-Darstellungen**, Digital Signal Processing: https://doi.org/10.1016/j.dsp.2015.03.004.
9. **Projektintern (vor Einbau im aktuellen Repository prüfen):** `BUILD_WORKFLOWS.md`, `DIRECTIVES.md`, `CHANGELOG.md`, `docs/project/`, Analyzer-Übergabepaket v0.7 einschließlich normativer Funktions-/Parametertabellen, aktuelle freigegebene ORIGINAL-/FILTER-Bildreferenz.

---

## 14. Änderungsnachweis dieses Dokuments

| Version | Datum | Änderung | Software-Status |
|---|---|---|---|
| 1.0 | 24.09.2026 | Erstspezifikation mit DSP, Architektur, JSON-Beispiel, GUI-Bestandsschutz, 35 Testfällen und Einbauauftrag | **Keine neue Implementierung; keine Tests ausgeführt** |

**Ende des Übergabedokuments.**
