# LFN-Scout – Neuordnung der Analyzer-Einstellungen (VORSCHLAG)

Stand: 2026-09-20 · Bezugsstand v0.4.98. **Dies ist ein Navigations-/Parameterentwurf, keine Behauptung, dass geplante Funktionen bereits existieren.** Verbindliche Originalreferenz: `analyzer_v0.7/references/Funktionsreferenz_v0.4.md` (146 CFG-Einträge), ergänzt um `analyzer_v0.7/docs/project/13_Parameter_Ergaenzungen_v0.7.md` (23 CFG-Einträge). Später vereinbarte Werte haben Vorrang. Das alte Registerlayout bleibt bis zur ausdrücklichen Entscheidung bestehen; allein der Wasserfallmodus wird in v0.4.98 bereits im bisherigen Register „Anzeige“ bedienbar.

## 1. Bedienprinzip

Eigenes hochformatiges Einstellpanel, keine ausgedehnte zweite Hauptanzeige. Einspaltig, vertikal scrollbar, oben persistent **aktives Setup / Speichern / Laden** und darunter Kategorienavigation. Jede Gruppe hat eine Kurzansicht (häufige Werte) und nach Bedarf „Weitere Optionen“. Bei einem gesperrten/nicht entwickelten Feature nur informierenden Text „geplant“, keine funktionslosen Schalter. Einheiten, gültige Werte, tatsächlicher Geräte-Status und Folgen eines Neustarts direkt am Feld. Global: „Schließen“ statt Rücksprung durch horizontales Registerband.

## 2. Kategorien und konkrete Zuordnung

### A. Spektrum / FFT (CFG-SP)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| Frequenz | Linear, obere LF-Grenze **20/50/100/200 Hz**, untere 0 Hz; später Pan/Zoom, logarithmische Achse nur >0 | 20/50/100/200 implementiert; Zoom/Log geplant |
| Verarbeitung | Tatsächlich **8000 Hz Roh-PCM → 129-Tap FIR → 1000 Hz LF**, Decimation 8:1 (Status), FFT-Größe **4096** und Hann (Status/fest), Auflösung **0,244 Hz/Bin** | implementiert und nicht frei änderbar |
| Zeitauflösung | FFT-Hop **10/50/100/200/500/1000 LF-Samples**, gewählte Soll-FFT-Rate und Warmup-Hinweis | implementiert |
| Kurven | Live EIN (gelb); zeitliches Leistungs-Average EIN (rot, derzeit 5 s fest); geschätzte Rauschboden-Kurve standardmäßig AUS | implementiert; Average-Dauer noch nicht einstellbar |
| Cursor | Ein-/Ausschalten per Tipp, Position per Finger, Readout an Spitze; geplante numerische Frequenz/Cursor-Quellenauswahl | Basiscursor implementiert |
| Zukünftige Analyse | optionale FFT-Größen, Fenster, DC-/Filterparameter, Smoothing, Peak Marker, **Spektrum-Max-Hold später, jetzt ausdrücklich nicht** | geplant; nicht als funktionierende Regler zeigen |

### B. Audio (CFG-AU + CFG-MO + CFG-LF-AU)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| Eingang | Gewünschte/effektive Audioquelle (AUTO/UNPROCESSED/MIC), internes/USB-Gerät, Rate, PCM-Format, Kanal, Mikrofonberechtigung; Quelle vom **gemeinsamen Recorder**, kein zweites Mikrofon | effektiver Status/Diagnose existiert; neue Analyzer-eigene Geräte-/Quellenwahl NICHT implementiert |
| Messpegel | **Analysegain 0…50 dB** separat vom bestehenden WAV-/Recorder-Gain; Input-Clip; Roh-/LF-Peak und -RMS | Analysegain und Diagnose implementiert |
| Mithören – Ausgang | Ein/Aus, unterstützte Kopfhörerroute, Monitor-Gain, CLIP-Warnung, Auto-Abregelung, sichere Ausgangsbegrenzung | im neuen Analyzer nicht implementiert; AUS nach Neustart |
| Mithören – Filter | Band-Unter-/Obergrenze, Mitte, Bandbreite, Charakteristik, Grenzwarnung, Test | geplant; strikt getrennt von Messweg/Recorder |
| Aufnahme | Bestehende WAV-Aufnahme nur als Status/Link zur bestehenden Aufnahmebedienung, **kein Analyzer-WAV** | bestehende App-Funktion unverändert |

### C. Anzeige / Display / Wasserfall (CFG-DI + CFG-HI)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| Spektrum-Darstellung | dBFS-Unter-/Obergrenze, Presets **−120…−60** (Default), −120…−40, −120…0; Ticks, Einheiten, dünne Kurven | gemeinsamer dBFS-Bereich implementiert |
| Wasserfall-Zeit | **120 s** Historie; neue Zeile oben, alte nach unten; **100/200/250/500/1000 ms/Zeile**, effektiv nicht schneller als FFT-Kadenz | implementiert |
| Wasserfall-Auswertung | **Aktuell** (Default: letzte FFT) / **Mittelwert** (lineare FFT-Leistung pro Zeilenzeit) / **Maximum** (je Frequenzbin und Zeilenzeit) | NEU in v0.4.98; Auswahl persistent + in 3 Setups |
| Wasserfall-Farben | Inferno, linke schmale Skala mit innenliegenden dBFS-Zahlen; Frequenzachsen fluchtend; spätere alternative LUTs / unabhängige Farbbereichsgrenzen | Inferno + gemeinsame Grenzen implementiert; weitere geplant |
| Layout | Spektrum-/Wasserfall-Höhe, erweiterte Ansicht, Kurvenfarben/-stärken, Statistikoverlay, Scroll-/Zeichenrate | teilweise implementiert; freie Layoutregler geplant |
| Radiushistogramme | AUS / letztes / letzte 4, rechts mit echten Daten, Zeitstempel/Veraltet-Status | geplant, keine simulierten Daten |

### D. Statistik (CFG-ST)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| L50/L90 | Band **2–25 Hz**, Messdauer **120 s**, numerische dBFS-Werte; gemeinsamer roter Resetrahmen | implementiert (Band und Fenster derzeit fest) |
| Kurvenmittelung | zeitliche Leistungsmittelung der roten Spektralkurve, aktuell **5 s**; gemeinsamer Reset mit L50/L90 | implementiert; Zeitdauer derzeit fest |
| Datenqualität | Statistik-Validität, Füllfortschritt, keine unzulässige SPL-Aussage | teilweise vorhanden |
| Superfast / Sample-Maximum | optionaler Rohsample-Peak, eigener Reset/Zeitstempel, Clip-Unterscheidung | separate Entwickler-Diagnose existiert; nicht in diesem Panel integriert |
| Impulsanalyse | Impulsdetektor/-Wasserfall nur nach separater Freigabe | zurückgestellt |

### E. Automatiken und Export (CFG-SV + CFG-LF + Autorange)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| Autorange | Tipp auf Spektrum-dBFS-Achse; Dauer **2 s** (1/2/3/5/10), feste Reserve **±5 dB** (0/3/5/10/15); robustes Frequenz-/Zeitperzentil; Grenzen sofort speichern | implementiert |
| Automatische PNGs | EIN/AUS, erster Export bei **vollständigen 120 s**, danach einmal je 120-s-Zyklus; bei internem Panelwechsel weiter | implementiert, Langzeit-/Gerätetest offen |
| Manuelle Exporte | PNG-Aktion, Ort `Download/LFN-Scout/`; später CSV-Momentaufnahme/Metadatenvarianten | PNG implementiert, CSV geplant |
| Sicherheit | Speicherfehler/Drop-/Queue-Anzeige, keine doppelten PNGs bei Pause, Retry-Regeln | teilweise implementiert; weitere Prüfung geplant |
| Spätere Trigger | Pegelschwelle/Zeitfenster/Ereignis-PNG, externe Trigger | nicht implementiert |

### F. Kalibrierung / Rauschreferenz (CFG-CA + CFG-NF)

| Abschnitt | Parameter und Aktionen | Ist-Stand |
|---|---|---|
| Mikrofonprofile | Profil auswählen/importieren/exportieren, Frequenzgang-Korrekturdatei, Gültigkeitsband, Quellenbindung | geplant |
| Absolutkalibrierung | 94-dB-/1-kHz-Bezug auf geeignetem Testaufbau; Umrechnung nur nach validierter Kette auf dB SPL | geplant; derzeit unkalibriertes dBFS |
| Rauschreferenz | Referenz messen (Dauer), speichern/laden, anzeigen, optional **lineare Leistungs-Subtraktion** mit Kompatibilitätsprüfung | derzeit nur gleitende Minimum-Schätzung als optionale graue Kurve; keine Referenz-Subtraktion |

### G. Setups (CFG-SE)

- Oben immer sichtbares aktuelles Setup, Laden, Speichern; im jetzigen Stand **3 lokale Slots** mit Frequenzbereich, Anzeige, FFT-Hop, Wasserfallintervall, **Wasserfallmodus**, Auto-PNG-Präferenz und Kurvenzuständen.
- Perspektivisch benannte JSON-Setups mit UUID/SchemaVersion, Neu/Speichern unter/Duplizieren/Löschen, Import/Export, letztes Setup, Revision/Dirty-Status. Geräte-/Laufzeitstatus **nicht** als Setup-Wert persistieren; Mithören bei Recall AUS.
- **Offene Bestandslücke:** Gegenwärtige 3 Slots speichern z. B. Autorange-Dauer/Reserve und Analysegain noch nicht vollständig. Neue UI soll komplette konsistente Parameter-Snapshots nach Migration unterstützen.

### H. Diagnose / Erweitert (CFG-EX + CFG-LF/TST)

- Tatsächliche Audioquelle und Abtastrate, Roh-/LF-RMS und Peak, Datenalter, Clip/Drop/CallbackErrors/Discontinuity; effektiver FFT-Hop, Soll-/Ist-FFT-/UI-Frequenz; diese Werte sind **Status**, keine Nutzereinstellungen.
- Versions-/Buildinformation, exportierbarer Fehlerbericht, Selbsttests mit PASS/FAIL. Testsignal/Synthetic PCM und CPU-Messung nur nach Implementierung anbieten; keine Testsignale in produktive Messung einschleusen.

## 3. Zuordnungs- und Migrationsregeln

- Es gibt **keine zweite Audio-Engine**; Benutzerwünsche für Input/Monitoring werden erst nach Routing-/Safety-Gate mit dem bestehenden Recorder integriert.
- Keine Doppelregler: FFT-Hop bei Spektrum; Zeilenintervall und Wasserfallmodus bei Anzeige; Averaging-Zeit und L50/L90 bei Statistik; Autorange bei Automatiken; dBFS-Grenzen bei Anzeige; Kalibrierung getrennt von Gain.
- Auf Hauptbild nur häufige Sofortaktionen (Pause, Cursor, Autorange über Achse, Statistikreset über Box, PNG). Vollständige Einstellungen im Panel.
- Änderungen an Abtastrate, FFT-Größe, Quelle etc. müssen Warmup/History-Restart ankündigen; Änderungen der reinen Darstellung dürfen nie die WAV-Aufnahme verändern.
- Alle vorhandenen `CFG-*` bleiben erhalten, wobei **Stand und Default aus v0.4.97/0.4.98 gegenüber älteren v0.7-Sollwerten** Vorrang haben, wenn später ausdrücklich beschlossen.

## 4. Noch nicht beschlossen

Diese Navigation ist ausdrücklich **ein Vorschlag**. Erst nach Bestätigung durch den Nutzer soll die vollständige Einstellungsoberfläche umgesetzt werden. Im Release v0.4.98 wird lediglich die neue Auswahl für den Wasserfall im bisherigen Register „Anzeige“ ergänzt.
