# Testplan LFN-Scout v0.5.19

## A. Windows-Vortest und Installation

1. ACTIVE-ZIP in einen neuen Ordner entpacken.
2. `VORTEST_INSTALL.bat` starten, zunächst ohne angeschlossenes Handy.
3. PREPARE, Unit-Tests, `assembleDebug`, APK und ACTIVE-ZIP müssen PASS erreichen.
4. Danach muss der Vortest sichtbar in `Warte auf genau ein freigegebenes Android-Geraet ...` bleiben und alle 2 s erneut prüfen; kein Fehlercode 62 nur wegen fehlendem Gerät.
5. Xiaomi anschließen/entsperren/USB-Debugging autorisieren. Bei `unauthorized` muss ein passender Hinweis erscheinen.
6. Bei genau einem `device` muss `adb install -r` automatisch starten und bei Erfolg INSTALL PASS ergeben.
7. Mit zwei ADB-Geräten darf keine Installation erfolgen, bis nur noch ein Zielgerät vorhanden ist.

## B. Analyzer-UI

1. Analyzer öffnen: oberhalb der Diagramme darf kein `SAVE-Setup`- oder `SAVE AUDIO`-Textbutton mehr stehen.
2. Einstellungen → Statistik öffnen: `SAVE-Setup · Audio / lokale Statistik` muss vorhanden sein und den bestehenden Dialog für Dauer/Statistik-MIN/MAX öffnen.
3. Analyzer starten und Puffer füllen. Unten in der Actionleiste muss ein 48-dp-Audio-Save-Actionbutton neben dem Kamerabutton erscheinen; der Puffer-/Quotentext steht direkt daneben.
4. Vor vollständigem Puffer muss der Save-Actionbutton deaktiviert sein; nach vollständigem Puffer und freier Quote aktiv.
5. Save während RUN: Analyzer/Wasserfall laufen weiter.
6. PAUSE → Save: der bei PAUSE eingefrorene Zeitraum wird gespeichert.
7. Nach erfolgreichem Save erscheint nahe dem Actionbutton exakt `Lokal gespeichert` und verschwindet nach ca. 3 s. Keine Displayzeile darf für diese Meldung eingefügt oder verschoben werden.

## C. Gain-/Statistik-Regression

1. Eine 1-kHz-WAV aus dem Beobachter mit bekanntem Aufnahmegain, bevorzugt +50 dB, verwenden.
2. Statistikbereich in Zusatzfunktionen auf denselben MIN/MAX-Bereich wie im zugehörigen neuen Statistik-JSON setzen.
3. Spektrum/Wasserfall aus der WAV erzeugen.
4. L50 und L90 der Anzeige mit `overall.l50_dbfs` und `overall.l90_dbfs` im JSON vergleichen. Erwartung: gleiche Werte bis auf Anzeige-Rundung (Ziel < 0,05 dB vor Rundung; 0,1-dB-Anzeige entsprechend).
5. Bei +50 dB muss der spektrale/spektrogrammbezogene dB-Wert gegenüber dem verstärkten PCM einmalig um 50 dB auf PRE-GAIN referenziert werden.
6. Gain 0 dB gegenprüfen; keine Verschiebung.
7. Analyzer mit Analysegain +50 dB: Live-Anzeige/Statistik darf den Gain nur einmal kompensieren. Der lokal gespeicherte Analyzer-PRE-GAIN-WAV darf nicht nochmals um 50 dB reduziert werden.
8. Clipping-Fall prüfen: Qualitätskennzeichnung bleibt gesetzt; Gain-Korrektur darf Clipping nicht als gültig ausgeben.

## D. Regression

Portraitmodus, Panelwechsel, Kamera/Auto-Bild, ORIGINAL/FILTER, Setups, lokale WAV/JSON/Manifest-Dateien, gemeinsame Tagesquote und bestehender Beobachter-Supabase-Upload mindestens stichprobenartig prüfen.
