# Analyzer-Zeitband v0.5.08 – Implementierungsdokument

- Ausgangsquelle `LiveAnalyzerController` verwaltet zwei synchrone Deques: unveränderte Spektrumszeilen `history` und `historyEpochMillis` (Epoch-Millis des realen Zeilenabschlusses). Bei Reset, Quellewechsel, Setupänderung und Kapazitäts-Trim werden beide gemeinsam bearbeitet. `Snapshot.rowEpochMillis` ist die zugehörige immutable Listenkopie.
- `WaterfallTimeAxis` ist Android-unabhängig und gibt die neueste echte Aufnahmezeit sowie höchstens vier ganze Uhrzeitticks zurück. Intervallstufen 1, 2, 5, 10, 20, 30, 60 min anhand der Wasserfallzeit. Bei Zeitlücken (PAUSE), nicht-monotonen Zeitstempeln oder ungültigem Übergang werden keine fiktiven Zwischenzeitpunkte interpoliert.
- `LiveAnalyzerScreen`: schwarzes Band zwischen dynamischem schmalem dBFS-Farbbalken und unverändertem `plotLeftDp=54.dp`; weißes `dd.MM` / `HH:mm`; Tick direkt an der Datenkante. Frequenzkoordinaten beider Diagramme unverändert. Kürzere Labels und horizontale Textskalierung verhindern Achsverschiebung.
- `LiveAnalyzerController.renderPng`: dieselben Echtzeit-Ticks und Snapshotzeiten. Der Farbbalken links bleibt erhalten; x-Achse und Cursor verwenden weiter `left=124f` und `right=970f`.
- Kamera-Erfolgs-/Fehlermeldung: unverändertes asynchrones Controller-Ereignis, UI-Popup am Kamerabutton (kein eigenes Nachrichtensystem), automatische Bilder lösen kein Pop-up aus.
- Zeitmarken sind eine *Darstellung*, keine persistente Zeilenhistorie über App-Neustarts. Nach einem Neustart beginnt die RAM-Historie wie bisher neu. Die bereits gespeicherten PNG/JPG enthalten ihre im Aufnahmezeitpunkt dargestellte Achse.
- Getestet: reine JVM-Zeitachsenlogik; vollständige Android-/Compose-/Geräteabnahme ausstehend. 16:1-Pegelabweichung bleibt offen.
