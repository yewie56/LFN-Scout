# LFN-Scout v0.5.36 – Regression Bewertungskopplung

- `LocalHalfHourObservations.fromPayload` isoliert die Zeitstempelentscheidung und prüft ID, Wertebereich und ISO-Zeitstempel vor der Speicherung.
- Antwort 0–5 verwendet ausschließlich `responseTime`, automatische Nichtantwort -1 ausschließlich `scheduledTime`.
- Ungültige Zeitstempel und fehlende Antwortzeit werden mit Diagnosemeldung verworfen, statt stillschweigend einer falschen Halbstunde zugeordnet.
- Halbstundenaggregation bleibt im Intervall `[start, end)`; gleiche Ereignis-ID wird nur einmal berücksichtigt.
- Neue JVM-Regressionstests: Grenzzeitpunkte, Zeitverschiebungen, -1, Nullbewertung, Duplikate und ungültige Ereignisse.
- Persistenz ist weiterhin über Android SharedPreferences vorhanden, aber ein Neustart-/Prozessabbruch-Gerätetest ist **nicht** durch die reinen JVM-Tests ersetzt.
- Kein neues Cloud-Schema, keine Änderung an `submit_observation`, keine Übertragung aggregierter Bewertungen oder Impulskennwerte.
- Bestehende Filterbank- und Sekundenwertberechnung unverändert.
