# Rolling-Halbstundenstatistik v0.5.30

## Implementiert

- Rolling-PCM-Ein-Sekunden-Aggregation mit Bucketgrenzen :00/:30.
- PCM-Abgriff nun VOR digitaler Gain-Anwendung und vor Clip auf 16 Bit. Somit bleibt der Rollingbuffer-Signalpfad selbst unverändert.
- Frequenzbereich Gesamtstandard 2–20 Hz, GUI-konfigurierbar.
- CSV-Switch in „Audio / Statistik lokal“, Standard EIN, persistent in `lfn_rolling_halfhour_v1`.
- Eine CSV pro Zeitfenster `Download/LFN-Scout/HalfHourStatistics_<startEpochMs>.csv`; stabile Dateinamen, keine einzelnen Sekunden-Schreibvorgänge. Speicherung im Hintergrund über MediaStore.
- Qualitätsmarkierung PARTIAL/COMPLETE/NO_DATA; beim geordneten Stopp wird ein PARTIAL-Bucket geschrieben.
- Die CSV enthält `bucket_start_ms`, `bucket_end_ms`, `min_hz`, `max_hz`, `valid_epochs`, `coverage_seconds`, `leq_dbfs`, `l50_dbfs`, `l90_dbfs`, `quality_status`, `reference`.

## Einschränkungen

- Filterkern weiterhin einfacher erster Ordnung, nicht amplitudenkalibriert, insbesondere Bandpassform nicht normiert; Werte ausschließlich für Entwicklungsdiagnose.
- `coverage_seconds` ist bei der aktuellen 1-s-Epoche gleich `valid_epochs`; tatsächliche Teilepochen werden nicht anteilig erfasst.
- Kein Recovery-Checkpoint im laufenden Bucket; Absturz vor Halbstundengrenze kann Zwischenwerte verlieren.
- Noch kein Eintrag der subjektiven Beobachtungen, keine Filterbank, keine Sekundenwerte-CSV, kein SD-Kartenziel, kein Cloud-Halbstunden-Upload.
- Kein End-to-End-PASS für v0.5.30 ohne Windows-Build und reale Geräteprüfung.
