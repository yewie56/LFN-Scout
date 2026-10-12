# v0.5.45.3 Rauschboden-Korrektur

- Ziel: 10 Sekunden als Standard-Einmessdauer mit Zeitwahl 10, 30, 60, 120, 300, 600 s unter Analyzer > Spektrum.
- Mathematisch: lineares Power Average der gueltigen FFT-Fenster, unveraenderte JSON-Referenz.
- Nullsignal-Fehlerbild: Wenn Referenzspektrum das Nutzsignal enthaelt oder die aktuelle Leistung kleiner als die Referenz ist, kann direkte Leistungssubtraktion alle Bins praktisch auf Null bringen.
- Schutz: bei Leistung <= 1.25 * Rauschreferenz wird der nicht kompensierte Originalwert durchgereicht; oberhalb davon subtrahiert die Software Referenzleistung. Dies ist ein konservativer Darstellungs-/Messschutz und kein wissenschaftlich exakter Rauschtrenner; eine Markierung dieser schwachen Bins fuer den CSV-Export waere spaeter noch zu ergaenzen.
- WICHTIG: Kalibration nur bei sicher elektrisch abgeschlossenem Mikrofoneingang. Ein live aufgenommenes, tonal belastetes Signal darf nicht als elektrisches Nullsignalprofil gelten.
- Testaufgaben: Gradle, Unit-Tests, Eingemessenes Nutzsignal, SNR-Niedrigbereich, Profilpersistenz, 10-s-Auswahl, FAST/CSV-Regression.
