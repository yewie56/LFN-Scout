# Testplan v0.5.16 (noch NICHT auf Android ausgeführt)

1. Windows: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`; Gradle-Ergebnis dokumentieren.
2. Gerät: v0.5.15 mit alter v1 `Setup_4_48k.json` (range=22000) und internem Slot 4 (range=200) aktualisieren. JSON-Export: kein `IllegalStateException`; `Setup_4_48k.json` byte-identisch erhalten; `Setup_4_48k_CURRENT_v2.json` enthält `range=200`; Setup 1 exportiert regulär.
3. `Diagnose-ZIP` enthält `Settings/setup_migration.json` mit pending slot 4, Fehlermeldung `Conflict: setup_4_range`; keine Konten/GPS/PCM.
4. Idempotenz: Wiederholung des Exports erzeugt keine nummerierten Duplikate des CURRENT-Namens und verändert die v1-Datei nicht.
5. Kompatibler v1-Import in leerem bzw. wertegleichem Slot: Commit + Readback; v1-Datei anschließend gelöscht; v2-Regulärname exportiert.
6. Ein Lese-, Schreib-, Readback- oder Löschfehler muss die v1-Datei behalten; Export erfolgt unter CURRENT-v2-Namen, wenn möglich. Fehler wird in Diagnose angegeben.
7. Regressionen: v2-Altdatei nicht als v1 löschen, v1-Slotkonflikte nicht überschreiben, Direkt STOP-/ROLLING-Lifecycle, 8-kHz-FILTER-Buttons, Aufnahmen, PNG/JPG.
8. Prüfe beim Vergleich explizit `setup_4_direct_sample_rate=16000`, `setup_4_fft_hop=200`, `setup_4_water_interval=100` – diese Werte werden von v0.5.16 nicht geändert.

Alle Positionen bis zur realen Ausführung **NOT RUN**, nicht PASS.
