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

