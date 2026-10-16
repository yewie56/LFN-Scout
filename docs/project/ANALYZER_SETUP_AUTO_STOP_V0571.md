# LFN-Scout 0.5.71 – Analyzer Setup STOP/RUN und MON-Panel

## Implementierter Funktionsumfang (Quellkandidat)

- Ein eigenständiger Navigationsbereich „MON & Audio“ zeigt die bereits vorhandenen MON-Einstellungen nur noch dort; bestehende MON-Controller bleiben erhalten.
- Betreten des Panels „Setups“ bei RUN: nur Analyzer-Auswertung stoppen; bei zuvor pausiertem Analyzer den Pausenzustand für den Neustart vormerken. Der Rolling-Dienst wird hier nicht angefasst.
- Verlassen von „Setups“: Nur bei zuvor laufendem Analyzer automatischer Wiederanlauf via bestehendem AnalyzerModeCoordinator.restartMeasurement. Bei ursprünglich STOP kein Anlauf. Fehler bleibt sichtbar, kein erfundener Erfolg.
- Hinweis im Setup-Panel, dass ein Analyzer-STOP die Analyzer-Statistik während dieser Pause ohne neue Messwerte lässt.

## Noch ausdrücklich offen / NICHT implementiert

- Parameterabhängiges automatisches STOP/RUN für FFT-Größe, Decimation, FAST-Filterbank etc. (erfordert sichere Übergabe *vor* Änderung einzelner DSP-Parameter, sonst Rennen).
- Kontrollierte Unterbrechung/Neustart von Rolling bei echtem Wechsel der physischen Audioquelle. Die bestehende Moduswechsel-Logik stoppt Rolling in Nicht-Rolling-Modi und kann Inhalte verwerfen; nicht für gewöhnliche Setupwechsel verwenden.
- Exaktes, persistentes Abdeckungsprotokoll (`analyzer_active_seconds`, `rolling_gap_seconds`, `statistics_coverage_pct` etc.) und CSV-/Datenbankschema.
- Android-/Kotlin-Kompilierung, Gerätetest, vollständige Regression und integrierter Self-Test.

## Sicherheitshinweis

Audioquellen-/Betriebsmoduswechsel wurden in v0.5.71 *nicht* umgebaut. Bei Modi außerhalb ROLLING_8K bleibt die bisherige Audio-Lifecycle-Regel aktiv. Vor einer Moduswechsel-Neukonstruktion ist eine getrennte Sicherung/Snapshot-Strategie für Rolling nötig.

## Gezielt zu prüfen auf dem Smartphone

1. Bei laufendem 8-kHz-Analyzer und Rolling: „Setups“ öffnen – Analyzer STOP, Rolling läuft und Ringpuffer wächst weiter.
2. Benanntes Setup auswählen; „Alle Bereiche“ – vorheriger RUN wird wiederhergestellt und neuer Setup-Name sichtbar.
3. Aus STOP in „Setups“ und zurück: bleibt STOP.
4. Aus RUN + PAUSE in „Setups“ und zurück: Analyzer wird wieder in PAUSE gesetzt.
5. „MON & Audio“ zeigt bekannte MON-Bandpass-/Kompressor-/Limiter-Regler; während RUN bleibt MON live veränderbar.
6. Keine automatischen Neustarts nach fehlgeschlagener Setup-Speicherung ohne die ausdrückliche bestehende Fehlerdialog-Entscheidung.
