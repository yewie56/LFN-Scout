# Permanenter Systemtest – Korrektur v0.5.76

## Problem
v0.5.75 verglich Heartbeat-Samplezähler über mehrere unabhängig gestartete Rolling-Sitzungen. Ein regulärer Neustart setzte den Zähler zurück und wurde fälschlich als FAIL bewertet.

## Implementierung
`PersistentSystemTest.evaluate` grenzt die Auswertung auf die jüngste `AUDIO_DIAG_ROLLING_RECORDING_STARTED`-Sitzung ein. Historische Starts/Stopps, Wiederanlauf-Fehler und Heartbeats werden nicht in den aktuellen PASS/FAIL-Werten vermischt. Wird kein Beginn gefunden, wird der gesamte vorhandene Log-Ausschnitt ausgewertet und `rolling_started` mit SKIP markiert.

## Grenzen
Der Systemtest führt weiterhin keine automatischen UI-Interaktionen durch. Es existieren noch keine expliziten Start-/Endmarker eines aktiven Testablaufs und kein Sample-genauer Lückennachweis. Die ausgewertete jüngste Sitzung kann bereits älter sein, wenn nach ihr noch kein neuer Rolling-Start stattgefunden hat. Die dokumentierten SKIP-Ergebnisse bleiben verbindlich.

## Regressionen
- Zwei gültige aufeinanderfolgende Rolling-Sitzungen mit jeweils neuem Samplezähler: PASS.
- Rückwärtszählung innerhalb der jüngsten Sitzung: FAIL.
- Alter Fehlereintrag vor Beginn der letzten Sitzung: nicht als aktueller FAIL ausgewiesen.
- Kein Start-Ereignis: SKIP für Start-Nachweis.
