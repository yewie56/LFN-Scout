# Phase 3C – v2-Inspektions-Export, verlustarme Import-Vorbereitung

Die neue Klasse `AnalyzerLegacyExportV2Converter` konvertiert eine explizite, read-only v2-Kopie (`lfn-scout-analyzer-setup-export-v2`) in ein **inaktives** v3-Dokument. Es werden weder Dateien gespeichert noch Setups automatisch geladen oder angewendet.

Prüfungen: Schema/Version, read-only-Markierung, Quellspeicher, Slot/Modus, bekannte Feldnamen, identische Mengen von Settings/Availability/Provenance und Widerspruchsfreiheit der gruppierten Projektion. `null` und `MISSING_IN_SAVED_SLOT` müssen übereinstimmen; solche Felder werden **nicht** durch Defaults ersetzt. `FIXED`/`UNSUPPORTED` bleiben als diagnostische Warnung in der Konvertierung ersichtlich und dürfen beim späteren Anwenden nicht verwendet werden. Die v3-Datei speichert Feldwerte zu Revisionszwecken einschließlich solcher, die im jeweiligen Betriebsmodus nicht anwendbar sind.

**Nicht enthalten:** v1-Importer, UI, SAF, echtes Importieren über Dateiauswahl, Runtime-Anwendungsadapter und Gerätetests. Die Kennzeichnung wird hierfür in `parameters.provenance.notApplicableFields` dauerhaft übernommen. Ein Runtime-Anwendungsadapter muss diese Information später zwingend berücksichtigen. **Bis dahin nicht zur Aktivierung verwenden.**

Version in Gradle bleibt unverändert v0.5.55 (213); Phase 3C ist ein isolierter Kandidat der geplanten v0.5.56. Statische Quellprüfungen ersetzen keinen Gradle-Build.
