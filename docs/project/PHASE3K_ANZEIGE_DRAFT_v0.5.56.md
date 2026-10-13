# LFN-Scout v0.5.56 – Phase 3K (Entwicklungsstand)

## Änderung

Die explizite Anzeige-Vorschau (Phase 3J) erhält einen Abschlussdialog mit drei Aktionen:

- **Speichern**: Liest die aktuellen Controller-Anzeigewerte und speichert nur jene Legacy-Felder, die beim Vorschau-Start tatsächlich übernommen wurden, in der ausgewählten JSON-Setup-Datei. Alle anderen Setupfelder bleiben erhalten. Das Repository führt vor dem atomaren Speichern eine vollständige JSON-Typprüfung durch. Die bisherige SharedPreferences-Speicherung wird von diesem Pfad nicht angesprochen.
- **Verwerfen**: Stellt die vor Vorschau-Beginn erfassten Anzeigeparameter ohne Persistenz wieder her und beendet die Vorschau. Manuelle Anzeigeänderungen während der Vorschau werden dabei ebenfalls zurückgesetzt.
- **Abbrechen**: Schließt den Dialog, ohne den laufenden Entwurf zu verändern.

Schlägt das Speichern fehl, bleibt die Vorschau aktiv und kann erneut gespeichert oder verworfen werden. Das Speichern und Verwerfen ist bei laufendem MON oder aktiver MON-Aufnahme gesperrt. Ein Wechsel des 8-/48-kHz-Modus während der Vorschau verhindert die Abschlussaktion bis zur Rückkehr in den Ursprungsmodus.

Die Vorschau bleibt prozesslokal: Bei einem Prozessabsturz oder App-Neustart wird ihr Zustand nicht rekonstruiert. Bestehende sonstige Einstellungen können weiterhin über ihren alten UI-Pfad in SharedPreferences gespeichert werden; eine vollständige Transaktion über sämtliche Analyzer-Parameter ist **noch nicht** vorhanden.

## Grenzen

- Nicht implementiert: aktive Audioquellen- und FFT-Umschaltung, SAF-Import/Export, komplette A1–A8/S1–S7-Panelstruktur und vollständiger Gesamt-Setup-Wechsel.
- Die interne Versionskennung bleibt 0.5.55; ZIP ist ein Entwicklungskandidat.
- Statische Prüfungen sind kein Ersatz für Android-Kompilierung, JUnit und Gerätetests.
- Nach GitHub-TEST-Build sollte besonders geprüft werden: Vorschau -> Verwerfen, Vorschau -> Speichern -> JSON laden, Abbrechen, 48-kHz-Modus, MON-Aufnahmesperre, Prozessneustart und wiederholtes Speichern.
