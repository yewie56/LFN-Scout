# LFN-Scout v0.5.13 – Testplan für Wasserfall-Pop-up

**Gerätetests bis zur Rückmeldung: NOT RUN.** Im tatsächlichen Testprotokoll jeweils Build/Handy, Datum, PASS/FAIL und Screenshot/Log nachtragen.

1. `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace` in frisch entpacktem Windows-Ordner; tatsächliche Rückmeldungen dokumentieren. **NOT RUN**.
2. ORIGINAL allein, FILTER allein, beide zusammen und beide aus: Wechsel blendet genau zwei Infozeilen unten rechts **über dem Wasserfall** ein; keine statischen Zeilen darunter. **NOT RUN**.
3. Akt → MW → MAX sowie History-, Hop-, Intervall-, Frequenz- und Pegelwechsel: Textinhalt und 5-s-Neustart prüfen. **NOT RUN**.
4. Messung 15 s unverändert: Meldung verschwindet spätestens nach 5 s trotz fortlaufender FFT, neuer Wasserfallzeilen und Autorefresh; sie darf nicht periodisch aufblinken. **NOT RUN**.
5. FILTER lange drücken: Auto-Start und Abschluss optional kurz anzeigen; **kein dauerhaftes Fortschrittsfeld**. FILTER-Pegel und Colorbar unabhängig von ORIGINAL prüfen. **NOT RUN**.
6. Frequenzachse Spektrum/Wasserfall, schwarz-weißes Uhrzeitband, dBFS-Colorbar und Wasserfall-Bitmap vor/während/nach Pop-up vergleichen; Pop-up verdeckt nur temporär einen kleinen Bereich und verschiebt nichts. **NOT RUN**.
7. Umschalten bei PAUSE, STOP, Re-START, Panelwechsel und kleinem Xiaomi-Viewport; 10-/9-sp-Text zweizeilig, ausreichend sichtbar, ohne Layoutsprung. **NOT RUN**.
8. PNG/JPG manuell und automatisch mit/ohne aktivem Pop-up speichern: keine eingebrannte Info-Bubble; Kamera-Erfolgs-Pop-up nach echtem Dateiergebnis weiter unmittelbar am Kamerabutton. **NOT RUN**.
9. Alte Setup-Roundtrip-, Audio-/Rolling-/48-kHz-/FAST- und 16:1-Pegel-Regressionsfälle getrennt verifizieren. **NOT RUN**.

Die statischen/JVM-Tests ersetzen keinen Android-/Compose-Build und keine visuelle Geräteabnahme.
