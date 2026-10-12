# LFN-Scout v0.5.27 – verbindliche nächste Statistik-Implementierung

Diese Dokumentation ist eine Spezifikation und kein Ausführungsnachweis.

- Rolling-Pufferlänge weiterhin 30 Minuten in RAM. Konfigurierbare maximale **Betriebsdauer** Default 12 h; Zeitablauf stoppt Rolling und gekoppelten Analyzer.
- Während aktivem Rollingbetrieb und nur dann 30-Minuten-Overall-Statistik, auch wenn Analyzer mitläuft. Globaler Statistik-Schalter Default EIN.
- Overall-Frequenzbereich: MIN 2 Hz, MAX 20 Hz, unabhängig vom Analyzer und frei einstellbar.
- Unabhängige 30-Minuten-Filterbank: separat EIN/AUS (Default AUS), FILTER MIN 0 Hz, FILTER MAX 100 Hz, 10 oder 20 linear aufgeteilte Bänder. Keine zusätzlichen WAV-Impulsfilter automatisch als Halbstundenfilter interpretieren.
- Sekundenepochen, Leq/L50/L90, 1800-s-Buckets exakt :00/:30; Coverage, Qualität, Lückenbehandlung, Konfigurationswechsel beachten.
- Lokale CSV-Ausgaben unter zugänglichem Speicherziel: Gesamtstatistik (Default EIN), Filter (bei aktiver Filterstatistik), Sekundenwerte (Default AUS), WAV-Impulsfilterwerte (separat Default AUS).
- Lokaler Halbstunden-Bewertungsmittelwert: gültige Ratings 0..5, −1 ausgenommen; außerdem count/min/max/unavailable. **Nur CSV, nicht Supabase**.
- 30-Minuten-Overall-Statistik an Supabase; kontinuierliche Halbstunden-Filterwerte nicht an Supabase; WAV-gebundene Overall-/Filterwerte bis 40 weiterhin an Supabase.
- WAV-Dateien und Statistik-CSV mit **unabhängig** auswählbarem Speicherziel (intern Download/LFN-Scout oder SAF-Unterordner auf SD-Karte).
- Flash-Wear-Out minimieren: RAM-Aggregation, gepufferte Schreibvorgänge; Sicherungsintervall Default 300 s konfigurierbar, Ausfall-/Recovery-Verhalten dokumentieren. Keine synchronen Dateischreibzugriffe im Audiopfad.
- Offline-Retry/Outbox mit Idempotenz; Observer-Key nicht im Warteschlangen-Datensatz speichern.
- Alle fachlichen Parameter GUI-editierbar und persistent; alte Setups behalten kompatible Defaults. Kein doppelter PCM-Pfad und Gain nur einmal kompensieren.

Noch umzusetzende Punkte nicht mit `PASS` markieren.
