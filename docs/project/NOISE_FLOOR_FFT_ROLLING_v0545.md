# LFN-Scout v0.5.45 – FFT-basierte Rolling-Statistik (Buildkandidat)

## Ziel und Abgrenzung
- FFT-berechnete 1-Sekunden-Leistungsdaten speisen die Rolling-Gesamtstatistik (Leq, L50, L90) und die 10/20 CSV-Frequenzbänder.
- Die drei alten PCM-CSV-Filtermethoden und ihre Settings werden NICHT gelöscht. Die Methoden bleiben im Quellcode; die Auswahl und Band-Parameter bleiben in der GUI sichtbar, sind aber ausgegraut.
- Checkbox **FFT-Frequenzbandstatistik für CSV aktivieren** schaltet ausschließlich den FFT-Band-CSV-Export ein/aus.
- FAST-Analyzer und Wasserfall werden nicht angefasst.
- Der vorhandene Rauschboden-Button bleibt: kurz Ein/Aus; lang 120-s-Einmessung, JSON-Profil persistent.
- Die 1/f-Kompensation und der Vergleich der alten Filterverfahren sind zurückgestellt.

## Tatsächlich implementierte FFT-Integration (Grenze dieser Zwischenversion)
Der Rolling-Service hat bisher kein frei verwendbares Spektrum des 8-kHz-Live-Analyzers ohne Anzeige zur Verfügung gestellt. Deshalb nutzt er mit v0.5.45 einen **eigenen 1-kHz-/1024-Punkte-Hann-FFT-Strom**, um die beiden bislang separaten PCM-Statistikfilter abzulösen. Gesamt-L-Werte und 10/20 Bandwerte stammen gemeinsam aus **derselben** neuen Rolling-FFT. Ein gemeinsamer FFT-Rechenkern mit dem bestehenden 8-kHz-Analyzer wurde **noch nicht hergestellt**. Bei geöffnetem Analyzer kann daher weiterhin eine zusätzliche FFT-Rechenlast auftreten. Diese Abweichung vom endgültigen Ziel ist offen und darf nicht als erledigt gelten.

Die Rolling-FFT hat nominell ca. 0,977 Hz Binbreite und ca. 1 s Zeitfenster. Hann-Leistungsnormierung und DC-Abzug sind nicht numerisch identisch mit allen auswählbaren Parametern des 8-kHz-Analyzers. Vor Übernahme in eine messdatenkompatible Freigabe müssen Vergleichskalibration und Laufzeitmessung erfolgen.

## Rauschboden-Kompensation
Das vorhandene 8-kHz-FFT-Profil wird in `noise_floor_reference_v1.json` gespeichert. Zusätzlich wird während derselben Einmessung **nur falls der Rolling-FFT-Strom tatsächlich läuft** ein zur 1-kHz-FFT passendes, ebenfalls gemitteltes Leistungsprofil unter `rolling_fft_1khz_1024_hann_power` gespeichert. Bei eingeschalteter Kompensation wird die hinterlegte Leistung vor der Rolling-Leq-/L50-/L90- und Bandstatistik subtrahiert (linear, auf Null begrenzt). Die Datenbasis für gemittelte L-Werte ist somit eine eigene Folge korrigierter FFT-Leistungen. Fehlt das passende Rolling-Profil, bleibt dessen Statistik **unkorrigiert**. Niemals wird ein 8-kHz-Profil ohne Normierungs- und Geräteprüfung interpoliert.

**Einmessbedingung:** geeigneter abgeschlossener externer Mikrofoneingang. Keine pauschalen Kurzschlüsse bei Bias-/Phantomspeisung; Smartphone-internes Mikrofon kann nicht elektrisch kurzgeschlossen werden. Das Kurzschlussprofil enthält nicht das Kapsel-Eigenrauschen. Profile sind an Audioquelle, Verstärkung und Messkonfiguration gebunden.

## Nicht verändert
Alte CSV-Dateinamen und -Spalten, Outbox/Cloud, WAV-Rohaufnahme, FAST-Filter. Alte PCM-Filterklassen bleiben im Quellbaum, werden vom normalen Rolling-Service nicht aufgerufen.

## Tests und offene Abnahme
- Isolierter Kotlin-Hosttest für FFT-Frequenzselektivität: PASS.
- Neuen JUnit-Regressions-Test `RollingFftEpochEngineTest` hinzugefügt; Android-Gradle-Tests müssen noch unter Windows ausgeführt werden.
- Android-Build, ADB-Installation, Laufzeittest, gleiche Datenbasis Live-vs-Rolling, Qualitätsprüfung der Statistik und Kalibrationsworkflow sind noch offen.
- Frühere v0.5.43 Tests bescheinigen nur frühere Versionen, nicht v0.5.45.
