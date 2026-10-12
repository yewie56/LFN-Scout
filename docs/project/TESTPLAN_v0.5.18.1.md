# Testplan LFN-Scout v0.5.18.1 – automatisierter Vortest

## A. Windows-Ein-Klick-Test

1. v0.5.18 bzw. den letzten Rückfallstand separat sichern.
2. v0.5.18.1 frisch in einen neuen Ordner entpacken.
3. `VORTEST.bat` doppelklicken. `PREPARE_PROJECT.bat` nicht mehr separat starten.
4. Prüfen, dass PREPARE ohne zusätzliche Pause durchläuft.
5. Bei erfolgreichen Unit-Tests muss automatisch `assembleDebug` folgen.
6. Bei PASS müssen vorhanden sein:
   - `Builds/LAST_VORTEST_RESULT.txt`
   - ein zeitgestempeltes `Builds/VORTEST_v0.5.18.1_*.log`
   - `Builds/LFN-Scout_v0.5.18.1-debug.apk`
7. `LAST_VORTEST_RESULT.txt` muss `Status: PASS` enthalten.
8. Bei einem provozierten Fehler darf der nachfolgende Schritt nicht ausgeführt werden; danach Projekt wieder frisch entpacken bzw. Änderung zurücknehmen.

## B. App-/Geräteregression

Die v0.5.18.1-Zwischenversion verändert die beabsichtigte App-Funktion nicht. Nach erfolgreichem Build den vollständigen v0.5.18-Gerätetest aus `docs/project/TESTPLAN_v0.5.18.md` fortsetzen; besonders Audio-SAVE, PAUSE-Snapshot, Gain-Korrektur, 40 Filter und bestehender Supabase-Upload.

## C. Status

Alle Windows-/Geräteschritte initial **NOT RUN**, bis ein tatsächliches Log bzw. Nutzerergebnis vorliegt.
