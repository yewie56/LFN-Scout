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



## Entwicklungsnachtrag Phase 2H (19.09.2026)

Sondertests H-01…H-08 in `../../../ANALYZER_PHASE2_HIGHSPEED.md` dokumentiert. Nur synthetische Kotlin-CLI-Fixtures H-01/H-03/H-04/H-05 und statischer H-02-Nachweis sind PASS. Android-/Gerätetests H-06…H-08 bleiben NOT RUN. Die formalen T-01…T-71 sind weiterhin nicht vollständig abgeschlossen.
