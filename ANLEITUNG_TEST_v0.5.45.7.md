# LFN-Scout v0.5.45.7 – Windows- und Android-Testanleitung

## 1. Windows 11 – Build und Installation
1. Vollständiges ZIP direkt in `C:\LFN5457` entpacken (kein zusätzliches Unterverzeichnis).
2. Android-Smartphone per USB verbinden, entsperren; USB-Debugging erlauben.
3. `C:\LFN5457\VORTEST_INSTALL.bat` ausführen.
4. `C:\LFN5457\Builds\LAST_VORTEST_RESULT.txt` prüfen: PREPARE, UNIT TESTS, ASSEMBLE DEBUG, APK, ACTIVE ZIP, ADB DEVICE und INSTALL müssen PASS sein.
5. Bei FAIL außerdem die neueste `Builds\VORTEST_*.log` sichern; keine erfolgreiche Installation annehmen.

## 2. Gerätetest – Signalstatus
**Voraussetzung:** Derselbe korrekt angeschlossene analoge Eingang mit 1-kΩ-Abschluss wie bei der gültigen Referenz, unveränderte Audio-/FFT-Einstellungen. Bei Mikrofon-Bias-Spannung Anschluss sorgfältig prüfen. Nicht mit einem ungeprüften direkten Kurzschluss arbeiten.
1. Analyzer starten, NOISE auf **AUS**: Originalspektrum sichtbar; Status darf bei laufender Eingangsmessung nicht fälschlich eine Unterbrechung anzeigen.
2. Einmessung falls nötig mit langem NOISE-Druck (10 s Standard, bei tieferen Frequenzen 60 s bevorzugt), Abschluss unverändert lassen.
3. Kurzer NOISE-Druck **MESS**: kompensiertes Spektrum beobachten; wenn alle Bins bei der Untergrenze liegen, **"Unter Rauschboden / kein auflösbares LF-Nutzsignal · Eingang aktiv"** statt "Kein verwertbares LF-Nutzsignal" erwarten.
4. NOISE auf **1/f**: Vorgang wiederholen; Wasserfall muss weiterlaufen, die Statistik darf nicht wegen der vollständigen Kompensation angehalten werden. Nullreste sind keine nachgewiesenen Nutzsignale.
5. NOISE zurück **AUS**: Originalspektrum muss wiederkehren.
6. Falls verfügbar einen eindeutig über Rauschboden liegenden Sinus einspeisen; in MESS und 1/f müssen Signal-Bins erhalten bleiben.
7. Quellunterbrechung simulieren (Audio-Aufnahme stoppen/Quelle trennen): Fehlender PCM-Datenstrom muss weiter als Unterbrechung erkennbar sein; dies darf nicht als "Unter Rauschboden" fehlinterpretiert werden.
8. Screenshot je Modus und `LAST_VORTEST_RESULT.txt` bereitstellen.

## 3. Technische Grenzen
- Signalprüfung nutzt **unkompensierte, gain-korrigierte FFT-Bins** zur Erkennung, ob die Quelle FFT-Leistung liefert. Sie ersetzt keine vollständige rohe PCM-/Zeitstempelprüfung.
- `-180 dBFS` bleibt technisch bedingter Platzhalter in bestehenden Datenstrukturen; keine physikalisch gemessene Restleistung.
- Bei vollständig kompensierter Leistung kann eine L-Statistik intern an der bisherigen numerischen Anzeigeuntergrenze liegen; gesonderte explizite CSV-Kennzeichnung "unter Rauschboden" ist offen.
- Vollständiger Android-Gradle-Build und Gerätetest sind erst nach Windows-Vortest nachgewiesen.
