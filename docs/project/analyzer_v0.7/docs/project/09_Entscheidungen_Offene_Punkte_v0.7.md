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

