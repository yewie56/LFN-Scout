# LFN-Scout v0.5.45.4 – Rauschboden-Leistungssubtraktion

- Einmessung: Dauer einstellbar, Standard 10 s; arithmetisches Mittel der **linearen FFT-Leistungen** pro Bin.
- Kompensation: `P_rest = max(0, P_mess - P_ref)`. Kein 1,25-Faktor und kein Rueckfall auf das Originalsignal bei wenig SNR.
- Rolling-FFT: Nullrest bleibt intern exakt **0 lineare Leistung**; Summen werden aus Restleistungen gebildet.
- Grafik/legacy-dB-Arrays: 0 ist logarithmisch `-Inf`; aus Darstellungsgruenden wird `-180 dBFS` als **nicht gemessener Platzhalter** benutzt. Der Platzhalter darf nicht als physikalisch gemessener Pegel interpretiert werden.
- Offene Einschraenkung: Anzeige der getrennten Roh- und Restpegel sowie eine explizite Unter-Rauschboden-Kennzeichnung in allen CSV-Feldern sind noch nicht vollstaendig implementiert. Alter Analyzer und Rolling-FFT sind weiter unterschiedliche FFT-Ausfuehrungen.
- Ein gespeichertes Profil aus normaler Mikrofonmessung kann Nutzsignale enthalten; kompensierte Werte sind nur eine Schaetzung bei stationaerem, unkorreliertem Rauschen.
- FAST bleibt unabhaengig; 1/f-Ausgleich sowie CSV-Filterperformance bleiben zurueckgestellt.
- Tests: Nullrest, Rauschen groesser als Messung, addiertes Signal, Nullreferenz, NaN-Validierung.
