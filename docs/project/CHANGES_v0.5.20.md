# LFN-Scout v0.5.20 – Änderungsbeschreibung

v0.5.20 behebt die nach der Gain-/L50-/L90-Vereinheitlichung gemeldete Wasserfall-Regression, ohne die nun korrekten Statistikwerte zurückzudrehen. Der Fehler lag in einer zu frühen Begrenzung gain-referenzierter FFT-Bins auf -120 dBFS. Die Colorbar konnte deshalb sichtbar bleiben, während große Teile der Heatmap denselben unteren Farbwert erhielten. v0.5.20 erhält die Werte bis zu einem weiten numerischen Sicherheitsbereich und begrenzt erst bei der Abbildung auf die Farbskala.

Für Analyzer-Audio-Saves wird der relevante Clip jetzt vor jeder Kommentartätigkeit fixiert. Erst danach erscheint die gemeinsame Dialogkomponente, die auch der Beobachter verwendet. Die konfigurierten Audio-Timeouts gelten damit identisch in beiden Pfaden. Der Analyzer-Kommentar wird dem lokalen Recording zugeordnet und in Statistik-JSON und Manifest geschrieben.

Die Hauptoberfläche wird kompakter: Audio-Save-Zusatzinfo zweizeilig; Beobachter-Audio-/Statistik-Setup in die Einstellungen verschoben; Entwicklermodus-Kennzeichnung in die Titelzeile; Highspeed-Impulsdiagnose als letzte Entwickler-Zusatzfunktion. Android-System-Zurück folgt nun dem Panel-Verlauf statt pauschal auf das Hauptpanel zu springen.

Keine Backend-/Supabase-Schemaänderung in dieser Version.
