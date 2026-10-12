# LFN-Scout v0.5.36 – Windows 11 Vortest und Gerätetest

1. ZIP nach `C:\LFN536` entpacken (Dateien liegen direkt in ZIP-Wurzel).
2. Handy per USB verbinden, USB-Debugging aktivieren.
3. `VORTEST_INSTALL.bat` starten. PASS in `Builds/LAST_VORTEST_RESULT.txt` kontrollieren und Log aufbewahren.
4. Handy: Rollingbuffer mit lokaler 30-Minuten-CSV aktivieren, innerhalb einer Halbstunde 0 und 4 bewerten; CSV-Mittel 2, Anzahl 2, min 0, max 4, sofern keine weiteren Bewertungen.
5. Test -1: Erinnerung ohne Reaktion auslaufen lassen; im passenden Zeitfenster sollte `observation_unavailable_count` steigen.
6. App nach Bewertung schließen und neu starten; nächste passende CSV-Erzeugung/erneuter Export muss lokale Werte noch enthalten. Hinweis: Bereits abgeschlossene CSV-Dateien werden durch spätere Bewertungen nicht automatisch aktualisiert.
7. Test direkt an HH:00/HH:30: Antwort ist nach tatsächlicher Antwortzeit einzuordnen; Nichtantwort nach geplanter Erinnerungszeit.
8. Log und CSV hochladen.

JVM-Tests prüfen Pure-Logik. Android-Build und insbesondere Geräte-Persistenz sind **nicht** im Quellpaket getestet.
