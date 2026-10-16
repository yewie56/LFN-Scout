# v0.5.68 – Anzeige-Synchronisation benannter MON-Setups

## Symptom

Beim Wechsel A → B → A zeigt der Analyzer für MON-Center und MON-Gain weiterhin denselben Wert, obwohl die STOP-Transaktion den vollständigen MON-Abschnitt der jeweils ausgewählten JSON-Datei über `AnalyzerMonitor.applySetupDraft()` einsetzt und anschließend verifiziert.

## Änderungen

- `LiveAnalyzerScreen`: In der bestehenden Aktualisierungsschleife werden `monitorCenter` und `monitorLevel` jetzt aus den aktuellen, verifizierten MON-Laufzeitwerten gelesen. Während eines laufenden Touch-Drags werden die Anzeigegriffe nicht durch die periodische Aktualisierung übersteuert.
- Kein Entfernen der Legacy-Speicherung, keine Änderung der Audioverarbeitung, der JSON-Dateiformate oder des Rolling-Verhaltens.
- Benannte Setups enthalten weiterhin individuelle MON-Parameter unter `parameters.monitor`.

## Tests und Grenzen

- `python tools/verify_v0568_mon_setup.py` prüft statisch die Verbindung JSON → STOP-Übernahme → MON-Controller → Anzeige und die Abschirmung des Legacy-Speichers.
- Noch ausstehend: Android-Kotlin-Kompilierung, Unit-/Self-Test und Gerätetest mit zwei **nachweislich verschiedenen** gespeicherten MON-Gain-Werten A und B; Wechsel A → B → A, einschließlich App-Neustart und Vergleich der JSON-Inhalte. Statische Quellprüfungen belegen nicht, dass alle Werte zur Laufzeit korrekt getrennt bleiben.
