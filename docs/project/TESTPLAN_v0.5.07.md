# Xiaomi-Testplan LFN-Scout v0.5.07

**Voraussetzung:** Erfolgreicher Windows-Gradle-Test- und APK-Build. Kein Löschen der bestehenden App-Daten bei der Installation. Ausgangsstand v0.5.05 wurde vom Nutzer bis auf fehlendes Kamera-Pop-up bestätigt; v0.5.06 ließ sich nicht kompilieren.

1. **MAX/CLR:** Mit 8-kHz-Testsignal 50 Hz und 195 Hz MAX sichtbar machen, anschließend Signal abschwächen. Gespeichertes Maximum muss erhalten bleiben. MAX/CLR drücken: nur MAX zurücksetzen; Average, Aktuell, Rolling und Wasserfall bleiben erhalten. Knopf nicht größer als Kamerabedienfläche.
2. **Drei Kurven:** Aktuell, Average und Maxhold jeweils separat ein-/ausschalten; alle Kombinationen sowie PNG und JPG prüfen. Sichtbarkeitswerte in Setup 1–4 speichern/laden und App neu starten.
3. **Kamera-Langdruck:** Mit Auto EIN und AUS jeweils genau einen manuellen PNG-/JPG-Export auslösen. Erst nach erfolgreichem Schreiben ca. 2,2 s sichtbares Pop-up; kurzer Tipp schaltet nur Auto. Fehler muss sichtbar gemeldet werden. Auch bei gescrollter Ansicht prüfen.
4. **Audio-Regression:** Rolling bereits EIN -> 8-kHz-Modus; 48 kHz -> STOP -> Panel verlassen -> Rolling startet neu; erneuter 48-kHz-Start. STOP alleine ändert die Betriebsart nicht; PAUSE bleibt separat. Panelwechsel bei laufendem Analyzer unterbricht die Analyse nicht.
5. **Weitere Regression:** Wasserfall-Moduswechsel Akt/MW/MAX ohne Historienverlust; Zeitachse; WAV, Bewertung, Seismik, PNG-Automatik und Setup-Export gemäß vorigen Testplänen. Die 16:1-Pegelabweichung gesondert dokumentieren, noch nicht als behoben werten.

Ergebnisse mit PASS/FAIL und ggf. Logcat festhalten. Erst erfolgreiche Gerätetests erlauben den Stand als Xiaomi-geprüft zu kennzeichnen.
