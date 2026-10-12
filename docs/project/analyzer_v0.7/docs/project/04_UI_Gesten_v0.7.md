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

Die Bildübersicht unter [`../../assets/concepts/BILDREGISTER_v0.7.md`](../../assets/concepts/BILDREGISTER_v0.7.md) ist Teil dieser Spezifikation. Das **LEITBILD `C13`** zeigt die vom Nutzer zuletzt akzeptierten Mithörzustände EIN/AUS. `C11` und `C12` illustrieren Fadenkreuz und Readout; `C03` die korrekte Wasserfall-Erweiterung, `C09` schmale Einstellseiten. `D01` ist nur ein neuer, noch nicht freigegebener Entwurf der Histogrammspalte. Andere Bilder sind **Referenz/Historie, nicht Pixel-Vorlagen**. Verworfenes Material wurde ausgeschlossen.

**Achtung, Bildfehler:** Einige Bilder zeigen veraltete Statuswörter, nichtlineare Achsenticks oder unrealistische Wasserfallachsen, geänderte Bandgrenzen, ein zusätzliches Recorderfeld oder nicht plausible L50-/L90-/Kurvenwerte. Diese Inhalte sind **nicht** implementierungsrelevant. Das tatsächliche Hauptbild muss das Spektrum oben und Wasserfall darunter mit **gemeinsamer horizontaler Frequenzachse** gemäß aktuellen Vorgaben besitzen; nur die Zeit läuft auf der Waterfall-Längsachse. Beispielbilder dürfen keine Daten- oder Kalibrierfreigabe begründen.

Die rechte Histo-Spalte ist eigenständig scroll-/tippbar, ohne Drag des darunterliegenden Mithörbandes auszulösen. Beim Wasserfall-Tap wird ausschließlich auf freier Wasserfall-Fläche der Zusatzkartenmodus umgeschaltet. Die Haupt-Header-Pause-Taste und `Save aktiv` bleiben erreichbar. Bedienelemente haben Mindest-Touchgrößen und numerische Alternativen; automatische Text-/Tooltip-Positionierung verhindert verdeckte Diagrammachsen.

