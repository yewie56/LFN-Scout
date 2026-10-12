# Auto-PNG beim Panelwechsel – Betriebs- und Fehlervertrag

**ID:** LFN-AN-PNG · **Version:** 0.7 · **Status:** verbindlicher Funktionsentwurf; Android-Implementierung/HW-Test ausstehend.

## 1. Unterschiedliche Zustände

| Ereignis | Capture / Analyzer | Sichtbares Renderer-UI | Auto-PNG | Vorhandener Recorder |
|---|---|---|---|---|
| Analyzer sichtbar, Live | läuft | Spectrum + Waterfall | falls Save EIN | unabhängig |
| Innerhalb LFN-Scout anderes Panel | läuft bei aktiver Analyzer-Session | Analyzer-UI zeichnet **nicht** fortlaufend | **läuft weiter**, offscreen | unabhängig |
| Anzeige-Pause explizit | PCM-Verteilung läuft, Anzeige eingefroren | Cursor auf Freeze beweglich | **ausgesetzt**, Resume füllt neu | läuft, falls aktiv |
| Mithören AUS | Analyse läuft | gleich | gleich | unabhängig |
| Save AUS | Session nach Nutzerstart-/App-Policy | gleich | OFF | unabhängig |
| App im Hintergrund / Bildschirm aus | **separates nicht automatisch freigegebenes Feature** | keine sichtbare UI | nur nach Dienst-/Rechtefreigabe | Bestand prüfen |
| Mic-/Routenfehler | Ungültig/Fehler | Fehlerstatus | Zyklus invalidieren, kein gültiges PNG | Fehler/Warnung separat |

Ein Panelwechsel ist **nie** gleich Anzeige-Pause, Audio-Stopp oder Nutzer-Abwahl von Save.

## 2. Abstrakter Zustandsautomat

```text
SAVE_OFF --explizit EIN--> SAVE_FILLING [t0, gültige Wasserfallzeilen]
SAVE_FILLING --vollständige T_wf--> SAVE_ACTIVE [enqueue png #1]
SAVE_ACTIVE --jede neue volle T_wf--> SAVE_ACTIVE [enqueue png #n]
SAVE_FILLING/ACTIVE --UI-Panelwechsel--> UNVERÄNDERT
SAVE_FILLING/ACTIVE --Anzeige-Pause--> SAVE_SUSPENDED
SAVE_SUSPENDED --Resume--> SAVE_FILLING [neuer t0]
SAVE_* --Eingangsfehler / Zeitlücke--> SAVE_INVALID [Status; Refill]
SAVE_* --explizit AUS--> SAVE_OFF
```

`Save aktiv` im Header zeigt den tatsächlichen Session-Zustand; andere Panels sollen bei aktivem Save mindestens einen kompakten globalen Status oder eine über die Navigation erkennbare Kennzeichnung bieten. Nach Recall/Neustart ist der tatsächliche Zustand OFF.

## 3. PNG-Transaktion

1. T_wf beginnt beim aktivierten Save oder der notwendigen Neufüllung. Maßgeblich ist die monotone Capturezeit, nicht UI-Paint oder Dateisystemzeit.
2. `AnalyzerFrame` enthält kohärenten Spektrums-/Wasserfall-/Statistik-/Monitorstatus mit Gültigkeitsmaske und Capture-Intervall.
3. `HistogramProvider.latestComplete(4)` liefert zu `t_end` die letzte/n verfügbare/n Histogramme einschließlich *eigener* Zeitinformation. Eventuell vorhandene Histos dürfen älter sein.
4. Ein Offscreen-Renderer verwendet exakt dieselben Layoutregeln wie die sichtbare Analyzer-Ansicht, inklusive Histogramm-Modus und Erweiterungszustand; er greift nicht auf den aktuell sichtbaren View zu.
5. PNG und Metadaten werden asynchron geschrieben; Dateiname eindeutige ISO-Zeit + Sequenz + Setup-ID-Kurzkennung. Beim Zielsystem ohne atomaren Rename muss der Fehlerpfad explizit behandelt werden.
6. Erfolgreich erst nach bestätigter Persistenz; bei Queue-Überlauf oder Speicherfehler `SKIPPED`/`FAILED` zählen. **Kein mehrfaches Nachholen identischer Frames**, kein PCM-Callback-Busywait.

## 4. Speicherplan und Dateiquota

Speicherort per SAF oder unterstütztem App-Dateiziel auswählbar. Ohne Schreibrecht `Save aktiv` nicht still fortsetzen. `maxPendingPngJobs=2` ist **VORSCHLAG**; überschreitet der Durchsatz das eingestellte Intervall, wird die aktuelle Periode als ausgelassen protokolliert. Eine Dateiquota/Retentionspolitik ist vor Release geräte- und Nutzungsabhängig festzulegen; **keine stillen Löschungen** vorhandener Nutzerdaten. Ohne aktives Speichermedium weiter live messen und Fehler anzeigen.

## 5. Beispiel bei T_wf = 120 s

| Monotone Zeit seit Save EIN | Erwartung | Panel |
|---|---|---|
| t=0 | 0 PNG | Analyzer |
| t=30 | 0 PNG | Wechsel zu anderem Panel |
| t=120 | PNG #1 | anderes Panel |
| t=240 | PNG #2 | anderes Panel |
| t=360 | PNG #3 | anderes Panel |
| t=370 | 3 PNG vorhanden; aktueller Wasserfall zeigt neue Zeilen nach Rückkehr | Analyzer |

**Keine Audioaufzeichnung durch `Save aktiv`.** T-49, T-50, T-53 und T-54 sind zwingende Integrations-/Gerätetests. 

## 6. PNG-Bildinhalt und temporale Konsistenz v0.7

Das PNG muss beim Export aus der Laufzeit-Render-Snapshotstruktur entstehen und die gleiche Spektrum-/Wasserfallkonfiguration wie die sichtbare normale bzw. definierte Save-Ansicht dokumentieren. Wenn der Analyzer nicht sichtbar ist, darf das **gerade sichtbare fremde Panel** nicht abfotografiert werden. Anzuzeigende Histogramme stammen aus demselben gesicherten Snapshot oder tragen eindeutig ihren eigenen Zeitstempel und Alter-/Fehlstatus. Bei fehlenden Histogrammen wird kein Inhalt erfunden. Nur tatsächliches `Save aktiv` löst periodische PNGs aus; eine getrennte bestehende Audioaufnahme hat keinen Einfluss auf den PNG-Zeitplan.

