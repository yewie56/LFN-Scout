# LFN-Scout v0.5.06 – Build- und Xiaomi-Testplan

Die folgenden Checkboxen sind **noch nicht abgenommen**. Auslieferung des Quellcodes ist kein Build- oder Geräteerfolg.

## A – Windows-Build

- [ ] Neues Projekt vollständig entpacken und `PREPARE_PROJECT.bat` ausführen.
- [ ] `gradlew.bat :app:testDebugUnitTest --stacktrace` = BUILD SUCCESSFUL.
- [ ] `gradlew.bat assembleDebug --stacktrace` = BUILD SUCCESSFUL; APK als Update ohne Löschen der App-Daten installieren.

## B – Kamera (nachgemeldeter Fehler)

- [ ] Auto AUS (rotes X): Kamera **lang drücken** → exakt ein PNG; Pop-up „Bild gespeichert: PNG“ erst nach erfolgreichem Dateischreiben, für ca. zwei Sekunden gut sichtbar; kein Auto-Toggle.
- [ ] Dasselbe mit JPG (z. B. 80 %), Dateiendung, Decodierbarkeit und tatsächlichen Bildinhalt prüfen.
- [ ] Auto EIN: einmal lang drücken → exakt ein manueller Export und eine Pop-up-Meldung; zusätzlich laufende Automatik bleibt unverändert.
- [ ] Zwei automatische Bildzyklen ohne Langdruck → Dateien vorhanden, **kein manuelles Erfolgspop-up**.
- [ ] Export blockiert/kein gültiges Signal → deutliche Fehlermeldung, keine falsche Erfolgsmeldung.
- [ ] Bei ganz nach unten gescrolltem Wasserfall steht Pop-up trotzdem sichtbar oben im Viewport. Nach Neustart/Panelwechsel keine alte Meldung erneut ausgeben.

## C – Drei Spektrumkurven und MAX/CLR

- [ ] Unter Einstellungen → Spektrum Aktuell, Average, Maxhold **einzeln** EIN/AUS; auch Kombinationen (nur MAX, nur Akt, alle AUS) korrekt.
- [ ] MAX EIN → türkise Kurve und MAX/CLR in der unteren Buttonleiste; Bedienelement höchstens so groß wie Kamera; MAX AUS → Knopf verschwindet.
- [ ] Konstantes 50-Hz-Testsignal, dann leiser machen: MAX hält ursprüngliche Spitze; MAX/CLR → Spitze fällt bei neuen FFT-Daten; Akt/Avg bleiben unverändert.
- [ ] MAX/CLR ändert weder die Wasserfall-Historie noch L50/L90, Rolling, WAV oder Audio-Betriebsart.
- [ ] Spektrumkurven auch im erzeugten PNG/JPG korrekt ein-/ausgeblendet.
- [ ] In Setup-Slot 1–3 und 48-kHz-Slot 4 jeweils Kombination speichern/laden; alte Setups ohne Flags benutzen Aktuell EIN / MAX AUS. JSON-Export prüfen.
- [ ] Getrennter 8-/48-kHz-Modus behält jeweils seine Kurveneinstellungen nach erneutem Start.

## D – Regression und bekannte Fehler

- [ ] 8-kHz-Modi mit Rolling EIN/AUS; PAUSE, STOP und getrenntes Verlassen des Panels. Bei 48 kHz mehrfach Wechsel → kein Absturz; ursprünglichen Rolling-Zustand nur bei STOP + Panelverlassen wiederherstellen.
- [ ] Kamera-Kurztipp weiterhin nur Auto EIN/AUS; zwei vollständige PNG-/JPG-Zyklen und Panelwechsel.
- [ ] WAV, Rolling, Bewertung, Seismik, Statistik, Frequenzachse, Wasserfallhistoriendauer und JSON-/Diagnoseexport prüfen.
- [ ] 16:1-Pegelabweichung mit 50/195 Hz separat protokollieren; **offen, nicht als behoben markieren**.
