# LFN-Scout v0.5.56 – Phase 3B: JSON-Validierung und private Slot-Migration

## Stand und Grenzen
Der neue JSON-Setup-Speicher aus Phase 3A wurde durch `AnalyzerSetupFormat.kt` um eine konkrete Feld-/Typprüfung ergänzt. Die 47 Legacy-Feldnamen und die Typgruppen stammen aus `AnalyzerLegacySetupMigration.kt`; die 16 MON-Felder stammen aus `AnalyzerMonitor.kt`. Das Format ist *kein* bereits freigegebenes Laufzeit-Setup. Insbesondere fehlen noch vollständige Anwendungsadapter, alle Zoom-/Kurvenzustände, SAF-Dialoge, die Alt-Export-v1/v2-Überführung und UI-Panels.

## Migration
`AnalyzerPrivateSlotMigrator.kt` ist explizit opt-in und wird nicht automatisch von MainActivity ausgelöst. Slots 1–4 werden aus `lfn_scout_live_analyzer` gelesen und mit ihren gespeicherten Datentypen in app-interne JSON-Backups kopiert. Keine fehlenden Felder werden durch Live-Daten oder Defaults ersetzt. Die neue Datei bekommt je Slot eine deterministische UUID. Bereits vorhandene Ziel-Setups werden nicht überschrieben, sondern geprüft. Alte SharedPreferences werden nie geändert oder gelöscht. Nur ein Slot mit gespeichertem `range` wird überhaupt als belegt erkannt.

Einschränkung: Bisher wird die MON-Präferenzquelle nicht migriert; MON-Konfiguration und daraus abgeleitete Werte müssen vor einer produktiven Übernahme separat spezifiziert werden. Ein strikter Validator kann atypische alte Key-/Werttypen zurückweisen; solche Fälle bleiben unverändert in den alten Einstellungen.

## Verifikation
`python tools/verify_lfn056_phase3b.py`: 14/14 statische Prüfungen bestanden. Phase-3A-Smoketest: 11/11. Der statische Test ersetzt ausdrücklich **keinen** Kotlin-/Gradle-Build und keine echten Laufzeit- oder Migrationstests auf Android. `versionName` und `versionCode` bleiben unverändert auf dem Phase-3A-Quellstand v0.5.55 / 213. GitHub TEST ist vor produktiver Integration erforderlich.

## Nächste Schritte
- Kotlin-/Gradle-Kompilierung und aussagekräftige Gerätetests der Formatvalidierung, beschädigter JSONs, Backup-Recovery und Migration.
- Alte exportierte v1/v2-JSON-Dateien mit Null-/UNSUPPORTED-/FIXED-Semantik prüfen und getrennt importieren.
- MON-Persistenz und alle Anzeigewerte anbinden; sichere Live-Änderungen ohne unbeabsichtigtes Save.
- Analyzer-Setup-Auswahl, MRU, SAF und Scout-Panels implementieren.
