# Analyzer: gemeinsame Einstellungsoberfläche und JSON-Projektion v0.5.14

## Status
Implementiert (Quellcode; Android/Compose-Build und Gerätetest NICHT ausgeführt):
- Neun vorhandene Kategorien bleiben in beiden Modi in identischer Reihenfolge erhalten.
- In der 8-kHz-Ansicht erscheinen die nicht separat unterstützten Audiooptionen und Peakmarker als nicht anklickbare graue Werte. In Direkt ist die komplette Filterbank als ausgegraute Einzelparameter sichtbar statt als einziger Hinweis.
- Bei Direkt sind Audioquelle, Eingangsrate und Decimation sichtbar STOP-pflichtig; die bestehende Laufzeitprüfung bleibt erhalten.
- Gemeinsame JSON-v2-Projektion: gleiche flache `settings`-Schlüssel und gleiche `groups`-Struktur, `availability` je Schlüssel sowie `provenance`; fehlende alte Slot-Werte bleiben `null` statt erfundene Standardwerte zu erhalten. 8k-Festwerte gelten in der Oberfläche; aus alten Slots ungesicherte Festwerte werden im JSON nicht erfunden.
- Nach dem App-Update wird vorhandener Setup-Export einmalig neu geschrieben. Gespeicherte Slots und mode_8/mode_48 bleiben unverändert.

## Absichtlich nicht implementiert / weiterhin offen
- **Kein JSON-Import**, keine Migration der alten Slots und keine Änderung des internen Preferences-Schemas. `read_only_copy=true` bleibt verbindlich.
- Laufzeitvorschriften werden noch nicht vollständig aus einem einzigen zentralen Parameterkatalog abgeleitet (der neue Export hat eine zentrale Key-/Gruppenliste); keine Änderung der DSP- und Audio-Laufzeit.
- Die Zeitkonstante im Direktmodus ist bei aktiviertem Sekundenmodus jetzt editierbar; der Alpha-Modus bleibt separat. Diese Änderung benötigt Android-/Gerätevalidierung.
- Die 8-kHz-Testsignale sind weiterhin in ihrer bestehenden TEST-Betriebsart; direkte Testton-Keys ersetzen diesen Pfad nicht.
- `availability` beschreibt die Funktion des Modus, nicht Geräte-Hardwarefähigkeit oder Laufzeitpolicy. `provenance=MISSING_IN_SAVED_SLOT` ist bewusst weder FALSE noch Default.
- Veränderter Wasserfallmodus betrifft neue Zeilen; die Historie wird nicht rückwirkend neu berechnet.

## Abnahmeplan
1. Frisch entpacken, Windows `PREPARE_PROJECT.bat` (SDK-Pfad konfigurieren).
2. `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace`.
3. Xiaomi: 8k und Direkt Einstellungen öffnen; Gruppen/ausgegraute Werte/Speichern/STOP prüfen.
4. Alten Setup-1/4-Slot ohne Speichern exportieren: gleiche Schlüsselmengen, fehlende Werte `null`, nach Speichern vorhandene Werte. Beim Speichern eines Direkt-Setups 100-ms-Wasserfallintervall prüfen.
5. Rollback/STOP/Panelverlassen, Buttons/Filterautoranges, PNG/JPG und Diagnoseexport regressionsprüfen.

Kein PASS für Android-Build, Emulator oder Xiaomi ohne tatsächlichen Lauf. 16:1-Pegelabweichung und vollständige Impulserkennung bleiben offen.
