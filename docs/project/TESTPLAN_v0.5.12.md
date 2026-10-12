# LFN-Scout v0.5.12 – Build- und Gerätetestplan

**Kein Android-/Xiaomi-Test bisher ausgeführt; Ergebnisse hier nachtragen.**

1. Frisch entpacken und `PREPARE_PROJECT.bat`; `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`; beide tatsächlichen Resultate dokumentieren. **NOT RUN**.
2. ORIGINAL-Symbol visuell mit der vom Nutzer hochgeladenen PNG vergleichen; unverändertes Motiv, 48 dp, unabhängig vom Filter und Kamera. **NOT RUN**.
3. FILTER-Symbol weiterhin fünf Magenta/Rosa/Weiß-Balken, 48 dp. Kurztipp EIN/AUS unabhängig vom ORIGINAL; beide EIN/OFF korrekt, Aufnahme läuft. **NOT RUN**.
4. FILTER lange drücken bei aktivem 8-kHz-Signal. Anzeigestatus zeigt Fortschritt; nach gewählter Dauer skaliert sich nur FILTER, ORIGINAL-Anzeigebereich und DSP unverändert. **NOT RUN**.
5. Ungültiges Eingangssignal, PAUSE, STOP bzw. Direktmodus: keine falsche Bestätigung, keine Änderung der Filteranzeigegrenzen. **NOT RUN**.
6. FILTER-Autorange mit 35-Hz-Sinus, kurzem Burst und stillem Eingang; Peaks bei kurzer Messdauer und Filterantwort nachvollziehen. **NOT RUN**.
7. FILTER-MIN/MAX/Reserve/Dauer manuell einstellen; Setup 1–3 speichern, Neustart, laden; ältere Setups ohne neue Felder laden. **NOT RUN**.
8. Vergleich ORIGINAL allein, FILTER allein, BEIDE, KEINES; Farbbalken mit richtiger dBFS-Skala und Untergrenze; Frequenzflucht und Uhrzeitband beibehalten. **NOT RUN**.
9. PNG und JPG bei FILTER allein und BEIDE: Colorbar, Filterfarbe und Zeitstempel wie Livebild; Auto-Bildspeicherung unter Panelwechsel. **NOT RUN**.
10. Alte Rolling-/Audio-/STOP-/PAUSE- und Direkt-Analyzer-Regressionen; Langzeitlauf, CPU/RAM, 16:1-Pegelabweichung getrennt dokumentieren. **NOT RUN**.

Der neue Hosttest und statische Quellvertrag sind keine Android-/Gerätefreigabe.
