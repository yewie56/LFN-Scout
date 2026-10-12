# LFN-Scout – Analyzer-Übergabepaket v0.7

**Stand:** 19.09.2026 · **Status:** inhaltlich konsolidiertes **Soll-/Entwicklungspaket**, noch **keine Implementierung**, kein geprüfter Android-Build oder Gerätemessnachweis.

## 1. START HIER

1. [`UEBERGABE_AUFTRAG.txt`](UEBERGABE_AUFTRAG.txt) – kopierfertiger Auftrag für den bisherigen LFN-Scout-Entwicklungs-Chat.
2. [`docs/project/00_INDEX.md`](docs/project/00_INDEX.md) – 17 revidierte Fachdokumente plus ergänzende Audit-/Bilddokumente.
3. [`assets/concepts/BILDREGISTER_v0.7.md`](assets/concepts/BILDREGISTER_v0.7.md) – vollständige Auswahl **13 nicht ausdrücklich verworfener UI-Bilder** samt Status, Grenzen und Dateien; **2 abgelehnte Bilder fehlen bewusst**.
4. [`docs/project/18_ABSCHLUSS_AUDIT_v0.7.md`](docs/project/18_ABSCHLUSS_AUDIT_v0.7.md) – was zusätzlich entdeckt wurde, welche Risiken noch echte Repo-/Geräteprüfung erfordern.
5. [`docs/project/07_Integration_Uebergabe_v0.7.md`](docs/project/07_Integration_Uebergabe_v0.7.md) – Implementierungsreihenfolge und Pflicht-Mapping.

## 2. Produktziel und verbindliche Funktion

Neues LFN-Scout-Unterpanel **„Spektrum & Mithören“**: kontinuierlicher Live-Analyzer, nicht das neue Audio-Recording-System. Spektrum + Wasserfall immer sichtbar; lineare Achse 0–100 Hz als Default; gelb Live, rot Average, optional grau Rauschboden, Inferno-Wasserfall; Fadenkreuz auch in Pause; Superfast/Maximum Sample; unabhängiges L50/L90-Band 2–25 Hz/120 s; optional kalibrierter Messzweig; separat bandbegrenztes Mithören mit unabhängigem Gain/Clip/Auto-Abregelung nur im Output. Mehrere benannte Setups mit Recall. `Save aktiv` steht ausschließlich für automatische PNGs bei erstem vollen Wasserfall und jedem folgenden Wasserfall-Zyklus – auch bei Wechsel zu anderem internen Panel.

Bestehende separate LFN-Scout-Audioaufnahmen werden **nicht** durch Analyzer-Aufnahmen ersetzt: Analyzer und bestehender Recorder sollen eine koordinierte PCM-Quelle teilen. Optional 1 oder 4 letzte reale Radiushistogramme untereinander in schmaler rechter Wasserfallspalte; bei fehlenden Daten sichtbar leer, nie erfunden. Ein Testhost startet denselben Analyzer-Kern separat; kein unabhängig gepflegter Produktions-Clone.

## 3. Eindeutiger Versions-/Vorrangvertrag

1. **Dieser v0.7-README, `01_SRD`, `02_SRS`, `03_SDS`, `04_UI`, `05_Algorithmen`, `06_Tests`, `07_Integration` und `18_ABSCHLUSS_AUDIT`** gelten zusammen für das Analyzer-Soll. Jede v0.7-Ergänzung in den entsprechenden Dokumenten präzisiert entgegenstehende Altpassagen.
2. Die `references/Funktionsreferenz_v0.4.md` bleibt mit **146 ursprünglichen CFG-Parametern** normativer Basisanhang; `docs/project/13_Parameter_Ergaenzungen_v0.7.md` ergänzt **23 CFG-Einträge**. Werte mit Kennung `VORSCHLAG` sind nicht per Bild freigegeben.
3. `assets/concepts/` illustriert; **keine** generierte Bildbeschriftung hat Vorrang vor Text/Einheiten. `C13` ist das letzte akzeptierte Mithören-EIN/AUS-Leitbild, `C03` zeigt den korrekten Wasserfall-Modus. `D01` Histo-Schema ist nicht freigegeben.
4. `references/legacy/` und die Spectroid-Anleitung sind historische/funktionale Referenzen; die ausdrücklich verworfene Spectroid-Anleitung **v10.4** ist ausgeschlossen. Die bestehenden LFN-Scout-`docs/project/`- und Root-Anweisungen sind beim Integrationsschritt zuerst zu lesen und **nicht blind zu überschreiben**.

## 4. Noch unbekannte reale Integrationsfakten

Der aktuelle LFN-Scout-Gesamtquellcode (Git-Commit oder Projekt-ZIP), tatsächliche Audio-/Recorder-/Histogrammklassen, echte Radiusachsen/Zeiteinheiten, Android-Berechtigungs-/Target-SDK-Details und konkrete Geräteperformance liegen **hier nicht vor**. Deshalb enthält das Paket ein verbindliches, vor der ersten Codeänderung auszufüllendes Mappingformular. Keine erfundenen Klassen, keine behauptete bereits funktionierende APK.

## 5. Inhalt und Schutz der Daten

- Normative Unterlagen in `docs/project/`, Bildregister und alle verfügbaren Referenzen.
- Beispiel-JSONs sind **Beispiele**, keine Nachweise echter Kalibrierung oder Histowerte.
- Kein neuer Analyzer-WAV-/PCM-Pfad, keine ungefragte Upload-/Cloud-Funktion und kein UI-Element „Aufnahme aktiv“ für die reine PNG-Speicherung. Bestehende App-Funktionen und Installationsidentität unverändert belassen.
- Statische QA: `DOKUMENTEN_QA_v0.7.md`; SHA256SUMS und ZIP-Integritätsprüfung.
