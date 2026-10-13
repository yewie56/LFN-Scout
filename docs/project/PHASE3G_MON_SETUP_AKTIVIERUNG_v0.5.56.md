# LFN-Scout v0.5.56 – Phase 3G: begrenzte MON-Teilaktivierung

Status: Entwicklungsquellkandidat. Kein vollständiger Analyzer-JSON-Setup-Schalter, kein freigegebener Release.

## Umsetzung
- Der Setups-Browser bietet nach ausdrücklicher Bestätigung die Aktivierung einer *vollständigen* MON-Gruppe (16 Parameter) eines JSON-Setups an.
- Während MON aktiv ist oder eine MON-Aufnahme läuft, kann der Entwurf nicht umgeschaltet werden. Der bestehende MON-DSP-Baustein prüft die numerischen Grenzen und Felder vor Übernahme.
- Ein MON-Entwurf wird im Prozess speicherfrei gehalten; die Compose-Oberfläche erhält seine ID/Baseline nach Neuerstellung aus `AnalyzerMonDraftSession`. Das ist keine dauerhafte Setup-Wiederherstellung nach Prozessende.
- Änderungen während des MON-Entwurfs bleiben zunächst ungespeichert. Beim Wechsel eines veränderten MON-Entwurfs gibt es Speichern, Verwerfen oder Abbrechen. Nur explizites Speichern ruft `AnalyzerJsonSetupRepository.save` auf.
- MRU wird erst nach erfolgreicher MON-Parameterübernahme aktualisiert.
- Fehlende MON-Gruppe oder unvollständige JSON-Parameter führen zum Ablehnen des MON-Entwurfs; es wird nicht mit Legacy-Werten aufgefüllt.

## Grenzen und offene Risiken
- Die JSON-`legacy`-Gruppe wird **nicht** auf die Analyzer-Controller angewendet; nur MON wird aktiviert. Die vorhandenen Slots bleiben wirksam.
- Die drei MRU-Einträge sind noch Anzeige-/Inspektionsknöpfe, keine echten Schnellaktivierungen.
- Kein automatischer Import, keine SAF-UI, keine eigentliche achtteilige Panel-Struktur.
- `MON speichern` betrifft nur die MON-Gruppe des gewählten JSON-Setups. Es gibt noch keinen gemeinsamen vollumfänglichen Anwenden/Speichern/Verwerfen-Mechanismus für Legacy-/Audio-/Anzeigeparameter.
- Rotation innerhalb desselben Prozesses bewahrt die Entwurfskennung; Prozessende führt noch nicht zu automatischem Wiederherstellen der Entwurfskennung. Es gibt keinen gerätegestützten Funktionstest dieser Zustandsmaschine.
- Der Code wurde nur statisch geprüft, nicht mit Android Gradle kompiliert oder auf einem Gerät getestet. Der Projektstand führt weiterhin `versionName 0.5.55`.

## Nächster Schritt
Phase 3H: Controller-Snapshot/JSON-Adapter (inkl. umfassender Dirty-Erkennung und Aufnahme-Sperre), dann echte Setup-Schaltflächen. Vor Freigabe Gradle-/Gerätetests einschließlich Restore- und Regressionstests.
