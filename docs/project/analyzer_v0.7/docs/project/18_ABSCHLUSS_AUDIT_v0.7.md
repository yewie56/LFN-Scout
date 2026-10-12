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
