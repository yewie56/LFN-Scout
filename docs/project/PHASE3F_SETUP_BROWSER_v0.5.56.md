# LFN-Scout 0.5.56 – Phase 3F: JSON-Setup-Browser

## Umsetzung
- Die bestehende Analyzer-Seite **Setups** zeigt eine separate, schreibgeschützte Übersicht des app-internen JSON-Setup-Repositories.
- Aktualisieren, vollständig scrollbare Liste (bis 250 dp Höhe), Name/Schema/Parametergruppen/Beschreibung und eine Anzeige der drei im Index als MRU geführten Einträge.
- Ein Tipp auf einen Eintrag zeigt ausschließlich die Details; es wird **kein** Setup aktiviert, kein JSON gespeichert, keine Migration angestoßen und keine MON-Laufzeit geändert.
- Fehlender oder unlesbarer Repository-Index wird sichtbar gemeldet.
- Bestehende Slots 1–4 und deren Schaltflächen bleiben weiterhin maßgeblich. JSON-Kandidaten werden nicht als aktiv dargestellt.

## Noch offen
- Sichere vollständige Aktivierung der JSON-Parameter (einschließlich Schutz ungespeicherter Änderungen).
- Sicherer Entwurfs-/Speicherpfad und Live-DSP-Übernahme; native SAF-Import-/Exportwahl; tatsächliche JSON-MRU-Auswahl und drei Schnellzugriffstasten.
- Kompilierung mit Android Gradle, funktionale Regression, Test auf Gerät und Versionsanhebung.

## Verifikation
- `python tools/verify_lfn056_phase3f.py`: 12/12 statische Prüfungen.
- Phasen 3A–3E: 66/66 statische Prüfungen.
- **Statische Prüfungen sind kein Android-Build.** Keine Freigabe.
