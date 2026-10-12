## Zentrale offene und zurueckgestellte Aufgaben

- [DEFERRED_TASKS.md](DEFERRED_TASKS.md) – verbindliche referenzierbare Uebersicht (R01–R07, O01–O08, A01–A05). Bei jeder neuen Version auf Implementierungs- und Teststatus pruefen und fortschreiben.

> **Aktuell v0.5.03:** [Analyzer-Implementierung](ANALYZER_v0.5.03.md) · [Xiaomi-Testplan](TESTPLAN_v0.5.03.md). Nutzer meldete v0.5.02 vollständig PASS, neuer Android-Build/Xiaomi v0.5.03 NOT RUN.

> **Aktuell v0.5.02:** [Analyzer-Implementierung](ANALYZER_v0.5.02.md) · [Xiaomi-Testplan](TESTPLAN_v0.5.02.md). v0.5.01-Tests mit ausdrücklich fehlgeschlagener Hz-Positionierung; v0.5.02 noch nicht auf Android gebaut/getestet.

> **Aktueller Stand v0.5.01:** [Analyzer-Bedienung, Bildformate und Testplan](ANALYZER_CONTROLS_v0.5.01.md). [v0.5.00-Übergabe](UEBERGABE_v0.5.00_2026-09-20.md) bleibt die unveränderte Eingangsbasis.

> **Aktueller Übergabestand:** [Projektübergabe v0.5.00 (20.09.2026)](UEBERGABE_v0.5.00_2026-09-20.md) und [Testplan](TESTPLAN_v0.5.00_HANDOFF.md). Die untenstehenden älteren Phasenhinweise sind historisch; der produktive Live-Analyzer ist im aktuellen Code vorhanden, jedoch für v0.5.00 nicht Android-/Xiaomi-abgenommen.

# LFN-Scout – Projektdokumentation

Dieser Ordner enthält die **offizielle, dauerhaft mitgeführte Projektdokumentation** von LFN-Scout.

Er ist bewusst getrennt von:

```text
docs/superpowers/
```

`docs/superpowers/` enthält interne Entwicklungspläne und Design-Spezifikationen aus einzelnen Entwicklungsaufgaben. Diese Dateien dokumentieren den Entstehungsprozess und sind **nicht** die zentrale Projektdokumentation.

## Enthaltene Projektdokumente

- `LFN-Scout_Aenderungen_seit_v0.4.58.md`  
  Zusammenfassung der Änderungen vom hochgeladenen Ausgangsstand v0.4.58 bis zum aktuellen Entwicklungsstand.

- `LFN-Scout_Geplante_Erweiterungen.md`  
  Bisher diskutierte, noch nicht vollständig umgesetzte Erweiterungen, insbesondere InversionAnalyzer/KIT-Mast ab v5.0 sowie weitere Build- und Workflow-Themen.

## Weitere maßgebliche Dokumente im Projektstamm

- `BUILD_WORKFLOWS.md` – verbindliche Build- und Updatewege A/B/C
- `CHANGELOG.md` – chronologische Versionsänderungen
- `DIRECTIVES.md` – verbindliche Entwicklungsregeln
- `README.md` – Projektübersicht
- `GITHUB_BUILD.md` – GitHub-Actions-Build
- `UPDATER_ANLEITUNG.md` – optionaler Windows-Updater

## Regel für künftige Versionen

Neue offizielle Projektübersichten, Roadmaps, Änderungszusammenfassungen und vergleichbare dauerhafte Dokumente gehören nach:

```text
docs/project/
```

Interne aufgabenspezifische Planungs- und Design-Dateien bleiben unter:

```text
docs/superpowers/
```

### v0.4.68 – regionales WBBU-Sonderverfahren und klassischer 24-h-Plot
- Klassischer 24-h-Seismikplot: 48 Zeilen à 30 Minuten, abwechselnd blau/rot.
- WBBU-Sonderverfahren ausschließlich innerhalb 30 km um Bürstadt und nur für den aktuellen Tag: aktuelles HLNUG-Tagesseismogramm.
- Für vergangene Tage und außerhalb der Sonderzone gilt weiterhin das Näheprinzip mit tatsächlich verfügbaren offenen EIDA/FDSN-Rohdaten.
- Valencia und andere Standorte: keine Sonderstation, rein dynamische Nähe-/Verfügbarkeitsauswahl.

### v0.4.74 – LFN-Scout und Landscape-Liveplot

- Sichtbarer App-Name auf **LFN-Scout** geändert; technische Projekt-/ZIP-/Verzeichnisnamen bleiben **LFN-Scout**.
- Live-Näheplot fest auf **16:9 Landscape** gesetzt.
- Zeitachse 0–30 min entlang der langen horizontalen Strecke.
- Uhrzeitbeschriftungen links auf **4 sp** reduziert.
- Export bleibt 1536×864 px / intern 3072×1728.
- `40000 nm/s` bleibt vertikal direkt an der Maßstrecke.

### v0.4.73 – WBBU-nahes 16:9-Exportformat
- Näheplot-Export 1536 × 864 px (16:9).
- Intern 3072 × 1728 px rendern und anschließend auf Endgröße herunterrechnen.
- Maßstrecke ganz links vor den Uhrzeiten; `40000 nm/s` direkt parallel an der Strecke.
- Kompakter Header und 5-Minuten-X-Achse näher am WBBU-Layout.
- Speicherziel weiterhin `Download/LFN-Scout/`.

### v0.4.72 – WBBU-nahe Helicorder-Darstellung
- Näheplots mit kompakteren 30-Minuten-Zeilen und dichterem Min/Max-Rendering.
- Intensitätsmaßstrecke ganz links; Beschriftung vertikal, Uhrzeiten direkt daneben.
- 5-Minuten-Teilung der 0–30-min-X-Achse analog zum WBBU-Erscheinungsbild.
- PNG-Export entspricht dem neuen Layout und speichert unter `Download/LFN-Scout/`.
- Physikalische Skalierung, Zoom/Pan und Doppeltipp-Reset bleiben erhalten.

### v0.4.71 – Seismik-Zoom, Intensitätslegende und PNG-Export
- Näheplots mit verdoppelter Wellenform-Strichstärke.
- Intensitäts-/Amplitudenlegende als sichtbare Maßstrecke; bei kalibrierten Daten WBBU-Referenz 40000 nm/s, sonst Counts-Kennzeichnung.
- WBBU- und Näheplots per Pinch-Zoom/Pan bedienbar; Doppeltipp setzt auf 1x zurück.
- `Plot speichern` exportiert PNG nach `Download/LFN-Scout/` und zeigt nach Erfolg eine sichtbare Meldung.


## Analyzer v0.7 – verbindliche Übergabe (Entwicklungsstand Phase 1)

Die normativen Analyzer-SRD/SRS/SDS/UI-/Test-Unterlagen, alle 13 Bildkonzepte und das **reale Ist-/Soll-Mapping** liegen unter [`analyzer_v0.7/`](analyzer_v0.7/README.md) sowie [`analyzer_v0.7/docs/project/16_Repo_Mapping_und_Gates_v0.7.md`](analyzer_v0.7/docs/project/16_Repo_Mapping_und_Gates_v0.7.md). Der reine FFT-Kern ist isoliert implementiert; ein produktiver Live-Analyzer ist ausdrücklich noch nicht integriert.


## Analyzer Phase 2 (aktuell: Entwicklungsstand auf v0.4.85)

[`ANALYZER_PHASE2_AUDIO_STANDALONE.md`](ANALYZER_PHASE2_AUDIO_STANDALONE.md) ist das ergänzende aktuelle Ist-/Soll-Mapping zu gemeinsamem PCM, exklusivem Capture und Standalone-vorbereitendem `:analyzer-core`. Die ursprüngliche v0.7-Übergabe bleibt normative Funktionsbeschreibung, aber ältere Pfade zu `LiveAnalyzerCore.kt` und der alte Teststatus sind seit der Phase-2-Umstrukturierung historisch.

- `ANALYZER_PHASE2_HIGHSPEED.md` – Highspeed-Rohdatenabgriff, Detektor, Tests, Grenzen und 48-kHz-Migrationsgates (kein Android-Release).

## Seismik-Regressionskorrektur (Phase 2H)

[`SEISMIC_REGRESSION_PHASE2H_FIX.md`](SEISMIC_REGRESSION_PHASE2H_FIX.md) erläutert die Korrektur der beiden veralteten Contract-Tests und die noch offenen Android-/Gerätetests. App-Version und Produktivcode bleiben unverändert.

- [`JDK17_ANDROID_STUDIO_v0.4.86.md`](JDK17_ANDROID_STUDIO_v0.4.86.md) – Windows/Android-Studio-JDK-17-Vorbereitung, Fehlerdiagnose und Buildtest.

- [Analyzer Phase 2D – Diagnose v0.4.87](ANALYZER_PHASE2D_DIAGNOSE_v0.4.87.md) – Developer-Testpanel und JDK-Priorität.

- [Analyzer Phase 2E – Gain v0.4.88](ANALYZER_PHASE2E_GAIN_v0.4.88.md) – Unabhängiger Impulsdiagnose-Gain und Validierung.

- [Analyzer Phase 2F – Schwelle v0.4.89](ANALYZER_PHASE2F_SCHWELLE_v0.4.89.md) – Manuelle adaptive/absolute Empfindlichkeit, Presets und Prüfungen.

- [Live-Analyzer v0.4.90](ANALYZER_LIVE_v0.4.90.md) – Tatsächliches LF-Panel, PNG, Setup-Slots, Teststatus und offener Umfang.

- `ANALYZER_LIVE_v0.4.94.md` – aktueller Live-Analyzer-Stand, Achsen und unabhängige FFT-/Wasserfall-Hops, Testgates.

- [Live-Analyzer v0.4.98](ANALYZER_LIVE_v0.4.98.md) – Wasserfallmodi, Zeitbasis und Regression.
- [Analyzer-Einstellpanel v0.4.98 – Vorschlag](ANALYZER_SETTINGS_PANEL_VORSCHLAG_v0.4.98.md) – vollständige fachliche Aufgliederung, noch keine UI-Freigabe.
