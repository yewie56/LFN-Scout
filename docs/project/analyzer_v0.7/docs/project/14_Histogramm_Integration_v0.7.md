# Radiushistogramme – Datenvertrag, Anzeige und Zeitzuordnung

**ID:** LFN-AN-HIST · **Version:** 0.7 · **Status:** Integrationsentwurf bis zur Sichtung des tatsächlichen LFN-Scout-Histogrammmoduls.

## 1. Zweck und Bestandsschutz

Im Analyzer erscheinen wahlweise **kein**, **das letzte** oder **die letzten vier** bereits vorhandenen Radiushistogramme. Die letzten vier stehen untereinander am rechten Wasserfallrand. Der Analyzer **berechnet keine neue Radiusstatistik** und mutiert keine Histogramm-Persistenz. Inhalt, semantische Radiusdefinition, Einheiten, Zeitbezug, Herkunft und Veröffentlichungsintervall werden **aus dem aktuellen LFN-Scout-Quellcode** übernommen, nicht erraten.

## 2. Provider-Vertrag (semantisch)

```text
HistogramProvider.latestComplete(maxCount = 4): Immutable<List<HistogramSnapshot>>
HistogramProvider.observeCompleted(): Flow<HistogramSnapshot>
HistogramProvider.byId(snapshotId): HistogramSnapshot?   // optional für Navigation
```

`HistogramSnapshot` enthält: stabile `snapshotId`; `sourceVersion`; `createdAtUtc`; `windowStartUtc/windowEndUtc` **wenn von der Quelle vorhanden**; `binEdges`/`binCenters` in **Quell-Einheit**; `binValues` inkl. Basis/Einheit; Qualitätsflags; `sourceLabel`; gegebenenfalls vorhandene, datenschutzverträgliche Orts-/Kategorienreferenz. Welche Felder in der Praxis verfügbar sind, wird bei Mapping bestätigt. Fehlt eine Größe, wird sie im Renderer nicht erfunden. Der gezeigte JSON-Schemaentwurf ist illustrativ und muss an die echte Quelle angepasst werden.

## 3. Reihenfolge und Snapshot-Gültigkeit

- Nur **abgeschlossene** Histogramme übernehmen. Doppelte ID in Subscription ersetzt dieselbe Version oder wird idempotent ignoriert.
- Neueste vollständige Version oben, danach bis zu drei ältere. Bei weniger als vier Datensätzen die fehlenden Plätze nicht durch Kopien auffüllen.
- Pro Histo: Zeitpunkt, Status `AKTUELL/VERALTET/UNBEKANNT/FEHLT` anhand echter Quell-Kadenz; wenn keine Kadenz bekannt, lieber neutrales `Zeit: ...` statt eine feste „veraltet“-Schwelle zu behaupten.
- Quell-Zeitstempel und Analyzer-PCM-Monotonic-Zeit sind unterschiedliche Zeitdomänen. Für PNG `snapshotCaptureEndUtc`, `histogramCreatedAtUtc` und optional die Quellzeitspanne ausweisen; **keine Gleichzeitigkeit behaupten**, wenn Intervalle nicht überlappen.

## 4. Layoutregeln für Hochformat

1. Spektrum oben bleibt **vollständig**. Wasserfall unten wird in zwei nebeneinander liegende Bereiche aufgeteilt: Plot links; Mini-Histogramme rechts.
2. Histo-Spalte Startwert 20 % der verfügbaren Wasserfallbreite; bei zu geringem restlichem Platz nach einer festgelegten Mindestbreite automatisch verbergen (Status-Hinweis), niemals Hz-Zuordnung verzerren.
3. `LETZTE_4`: vertikale vier Panels gleicher Höhe, mit abgekürztem Datum/Uhrzeit und optional Einheit. `LETZTES`: ein Panel oben oder vertikal zentriert (bei Implementierung konsistent entscheiden).
4. Wasserfall-Panning/Band-Drag und kurzer Tap werden nur im Plotbereich erkannt. In Histo-Spalte keine Mithörband-Geste. Erweiterte Ansicht blendet nur untere Zusatzkarten aus.
5. Dieselbe Layoutfunktion erzeugt sichtbares Hauptbild **und** Offscreen-PNG. Histogramm-Payload wird vor Rendereraufruf unveränderlich kopiert; keine Live-Neulese mitten im Export.

## 5. Fehler und Datenschutz

Quelle nicht verfügbar → „Keine Histogrammdaten“ und weiterhin lauffähiger Analyzer. Inkompatibles Bin-Schema → Fehler mit Quellrevision statt interpolierter Kunstkurve. Orts-/Personenbezug und Upload-Regeln ausschließlich nach Bestandsprojekt; PNG-Speicherort darf keine ungeprüfte Veröffentlichung auslösen. Alter der Histogramme im PNG nicht verstecken.

## 6. Testfixtures

`HIST_5_EVENTS`: fünf abgeschlossene Daten (IDs 1…5) + Duplicate ID3; erwartet 5/4/3/2. `HIST_MISSING`: nur 1; zeigt ein gültiges Histo und leere Plätze. `HIST_OLD`: alte Quellzeit; zeigt Alter. `HIST_SCHEMA_MISMATCH`: Fehlerzustand, akustische Engine weiterhin funktionsfähig. Tests T-55…T-58.

## 7. Reale Repo-Mapping-Pflichten

| Zu prüfen | Ist-Stand | Entscheidung |
|---|---|---|
| Histogramm-Publisher / DB-Tabelle / Besitzer | OFFEN | tatsächliche Klasse/Datei nennen |
| Radiusdefinition und X-/Y-Einheit | OFFEN | aus bestehenden Code-/Projektdokumenten übernehmen |
| Erzeugungskadenz / letzter kompletter Datensatz | OFFEN | für Stale-Regel erfassen |
| Datums-/UTC-/lokale Zeitdarstellung | OFFEN | echte Formate übernehmen |
| Navigation zu einzelnen Histos | OFFEN | nur wenn vorhandenes Panel ID-spezifisch öffnen kann |
| Berechtigung / lokale oder Remote-Daten | OFFEN | keine ungewollten Netzrequests bei Analyzer-Frame |

## 8. Bild- und Datenstand v0.7

Die vorhandenen UI-Konzepte bilden die finale rechte Histogrammspalte **noch nicht** ab. `../../assets/diagrams/D01_Histogrammspalte_SOLL_Schema.png` ist ein neu erstelltes **vorläufiges Layoutschema**, kein freigegebenes Original-UI-Bild. Die Linien darin sind rein grafische Platzhalter. Vor einer Implementierung müssen Radiusachsen, Einheiten, Werte- und Zeitbasis realer LFN-Scout-Histogramme sowie die Platzverhältnisse auf dem Referenzgerät gesichtet und bestätigt werden. Bei nicht verfügbaren Daten keine Simulation anzeigen.

