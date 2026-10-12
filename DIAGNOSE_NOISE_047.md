# LFN-Scout v0.5.47 – Vergleich NOISE AUS / MESS / 1/f

1. Gleiche Mikrofon- und Gain-Einstellungen beibehalten. Vorhandene 1-kΩ-Einmessung verwenden. Nicht neu einmessen.
2. NOISE AUS: 30 Sekunden prüfen, ob das Mikrofonspektrum sichtbar ist.
3. NOISE MESS: 30 Sekunden; DIAG öffnen und „Diagnose kopieren“ nutzen.
4. NOISE 1/f: 30 Sekunden; DIAG erneut kopieren.
5. Wenn möglich bei 20 Hz ein reproduzierbares akustisches Testsignal zusätzlich einspeisen und den Messwert kontrollieren.

Messwerte: `live`, `ref` und `rest` sind 10*log10 der linearen Leistung, nicht eine separat neu kalibrierte Schalldruck-SPL-Skala. `-Inf` bedeutet genau Null; `ref-live` positiv bedeutet, dass die Referenzleistung größer ist als die gemessene Live-Leistung. Die ausgewiesene Bin-Unterdrückung ist eine Momentaufnahme ca. einmal pro Sekunde, kein Verlaufsmittel. Das Main-Spektrum wird vor dem Abzug auf seine ursprünglichen dBFS-Werte zurückgerechnet; Rolling nutzt bereits lineare FFT-Leistungen.

Zur Ursachenklärung bei weiterhin verschwindendem Spektrum auch testen, ob die genaue Mikrofon-Eingangskonfiguration (Gain, automatische Pegelregelung, Frequenzgang, Speisung) gegenüber 1-kΩ-Abschluss unverändert ist. Ein elektrischer Abschluss ist nicht zwangsläufig äquivalent zur Mikrofonimpedanz.

GitHub: TEST automatisch nach Push, REGULAR manuell über den Termux-Uploader. Secrets bleiben bei GitHub, Windows-Build unverändert. Bei Import mit dem Uploader ZIP auswählen und Commit bestätigen.
