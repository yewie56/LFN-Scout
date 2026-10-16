# LFN-Scout v0.5.72 – MON und Filterbank bei temporärem Analyzer-STOP

## Grundlage
Die v0.5.71-Geräteprüfung ergab: Nach Rückkehr aus Setups war MON AUS, obwohl zuvor EIN. Manuelles MON-EIN stellte den Ton und die vorherigen Parameter unmittelbar wieder her. Außerdem war die FAST-Filterkonfiguration während Analyzer-RUN nicht änderbar.

## Umsetzung
- Der UI-Übergang nach Setups oder Filterbank stoppt bei ursprünglich RUN ausschließlich die Analyzer-Auswertung. Rolling-Service/PCM-Puffer werden hier nicht angefasst.
- Ein temporärer STOP wird innerhalb des Analyzer-Screens ausdrücklich gekennzeichnet. Die bestehende `LaunchedEffect(state.running)`-Abschaltung des MON-Ausgangs greift nur bei endgültigem STOP, nicht bei dieser zeitweiligen Pause. Ein bereits eingeschalteter MON-Ausgang bleibt aktiv; die Filter-/Gainwerte bleiben erhalten.
- Beim Verlassen der STOP-pflichtigen Einstellungsbereiche wird RUN bedingt durch den Eintrittszustand wieder aufgenommen; bei vorherigem STOP bleibt STOP. Auch ein vorheriger PAUSE-Zustand wird beim Neustart wieder hergestellt.
- Ein direkter Wechsel zwischen Setups und Filterbank löst keinen unnötigen Zwischenstart aus.
- Im Filterbank-Panel bleibt die Schaltfläche „Filter übernehmen“ verfügbar, während die Analyzer-Auswertung angehalten ist. Bestehende Validierung und Controller-Aufruf bleiben erhalten.

## Grenzen und Risiken
- Ändern der physischen Audioquelle, Umstellen anderer STOP-pflichtiger DSP-Parameter und Erfassung von Statistik-Abdeckungszeiten bleiben offen.
- Während der Analyzer-Auswertungspause fehlen Analyzer-Statistikwerte; laufendes Rolling kann unabhängig weiter Daten sammeln. Nicht auf 100 % Messwertabdeckung schließen.
- Android-/Kotlin-Build und Gerätestest sind nicht lokal nachgewiesen. Dieser Quellkandidat benötigt einen GitHub-/Termux-Build und gezielte Smartphone-Regression einschließlich MON, Filterbank und Rolling.
