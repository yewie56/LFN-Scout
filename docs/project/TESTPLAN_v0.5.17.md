# Testplan v0.5.17 – Hochformat bei laufender Messung

**Anforderung:** Alle LFN-Scout-App-Ansichten, einschließlich Reminder, bleiben im Portraitmodus. Neigung in die Horizontale beeinflusst weder ausgewähltes Panel noch Messung, Anzeige, Aufnahme oder Speicherexport. Nicht getestet, bevor die Schritte wirklich durchgeführt wurden.

## 1. Vorbereitungen (Windows)
1. Original-Archiv v0.5.16 und dessen JSON-Dateien separat aufbewahren. v0.5.17 in eigenem Ordner entpacken.
2. `python tools/verify_portrait_v0517.py` (statischer Vertrag).
3. `PREPARE_PROJECT.bat` ausführen, danach `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`. Buildlog aufbewahren. Erst danach APK aktualisierend auf dem Xiaomi installieren.

## 2. Xiaomi – erwartetes Verhalten
1. Automatisches Drehen des Geräts **einschalten**; LFN-Scout starten, Gerät hochkant und danach 90° und 270° quer halten. App-Start/Navigation/Setups bleiben hochkant; Activity wird nicht wegen Sensorrotation neugestartet.
2. 8-kHz-Analyzer RUN: Spektrum, Wasserfall, Buttons und laufende Werte sichtbar lassen; Gerät drehen. Über 30 Sekunden beobachten: Darstellung bleibt im Portrait, Messung läuft weiter, Wasserfall erhält neue Zeilen, keine Unterbrechung/Verlust der Historie.
3. Bei aktivem 8-kHz-Analyzer Einstellungen öffnen und zurück; erneut quer halten. Panel- und Setupzustand bleiben erhalten; Audio-/Rolling-Status unverändert.
4. Direkt-Analyzer mit erlaubter Audioquelle RUN: Gerät quer halten, zur App zurückkehren und nach ≥30 s prüfen: kein STOP, kein Neuöffnen von `AudioRecord`, keine unerwünschte Rolling-Restaurierung. Anschließend STOP und Panel verlassen: bestehender Restore muss unverändert funktionieren.
5. Falls aktiviert: laufenden Bildexport und WAV-/Aufzeichnungszustand während Gerätedrehung beobachten; keine zusätzlichen Dateien/Abbrüche aufgrund Rotation.
6. Reminder-/Alarmansicht anzeigen: Portrait bei Querhaltung; danach zur App zurückkehren. Android-Statusleiste/systemeigene Dialoge sind außerhalb der App-Anforderung.
7. Exportkonflikt v0.5.16 regressionsprüfen: alte Setup-4-Datei erhalten, CURRENT-v2 getrennt, Setup 1 exportiert; keine Altdateien manuell löschen. PNG/JPG und FILTER-/ORIGINAL-Schalter prüfen.

## 3. Protokoll
Für jeden Schritt PASS/FAIL/NOT RUN, Gerät/Android-Version, Messart, Zeit, Logcat bei Fehler und Screenshots festhalten. Keine automatische Gerätefreigabe allein durch statischen Test.
