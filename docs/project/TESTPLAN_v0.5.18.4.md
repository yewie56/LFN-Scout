# Testplan LFN-Scout v0.5.18.4 – Live-Ausgabe des Windows-Vortests

## A. Live-Ausgabe

1. ACTIVE-ZIP in einen neuen Ordner entpacken.
2. `VORTEST_INSTALL.bat` starten.
3. Während PREPARE und Gradle laufen, müssen neue Zeilen fortlaufend im Konsolenfenster erscheinen; nicht erst nach Ende des jeweiligen Prozesses.
4. Normale STDERR-Ausgaben wie `java -version` müssen sichtbar sein und dürfen bei Exitcode 0 keinen Fehler auslösen.
5. Dieselben Inhalte müssen im zeitgestempelten Vortest-Log vorhanden sein.

## B. Build-/Paketierung

Erwartete Reihenfolge:

1. PREPARE PASS,
2. Unit-Tests PASS,
3. assembleDebug PASS,
4. APK CHECK PASS mit Pfad, Größe, SHA-256,
5. ACTIVE PACKAGE/CHECK PASS mit Pfad, Größe, SHA-256.

## C. ADB ohne Gerät

1. Kein Android-Gerät per ADB verbinden.
2. `VORTEST_INSTALL.bat` starten.
3. Nach erfolgreichem Build muss `adb devices` sichtbar ausgegeben werden.
4. Die Anzeige muss `Freigegebene ADB-Geraete: 0` melden.
5. Es müssen konkrete Anschluss-/USB-Debugging-Hinweise erscheinen.
6. Keine Installation; Fehlercode 62.
7. Die Schlusszusammenfassung muss Build/Paketierung als PASS und ADB/INSTALL als FAIL bzw. NOT RUN ausweisen.

## D. ADB mit genau einem autorisierten Gerät

1. Xiaomi per Datenkabel anschließen, entsperren, USB-Debugging aktivieren und PC autorisieren.
2. `VORTEST_INSTALL.bat` starten.
3. Genau ein Gerät muss als `device` erkannt werden.
4. `adb install -r` muss vollständig sichtbar laufen.
5. Bei Erfolg: INSTALL PASS und Gesamtergebnis PASS.

## E. App-Regression

Da keine beabsichtigte Android-Laufzeitänderung vorliegt, mindestens App-Start, Versionsanzeige, vorhandene Setups, Portraitmodus und Analyzer-Start prüfen. Die offenen Audio-/Statistik-Gerätetests von v0.5.18 bleiben separat bestehen.
