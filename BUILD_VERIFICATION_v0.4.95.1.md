# LFN-Scout v0.4.95.1 – Build-Hotfix / Prüfnachweis

Basis: vollständige Projekt-ZIP v0.4.95. Neue Version 0.4.95.1 / versionCode 136. Einzige geänderte Android-Produktivdatei: `LiveAnalyzerController.kt` (fehlenden Diagnose-Callback ergänzt).

Ursprung: Nutzerlog mit drei `Unresolved reference acceptDiagnostics`. Statische Rückfallprüfung: `python verify_v04951_diagnostics_contract.py`. Host-Kotlin-Selbsttests, ZIP-CRC, SHA-256-Manifest und 53/53 Recovery-WAV-Vergleich bei der Paketierung.

Vollständiger Android/Compose-Gradle-Build und Gerätetest: NOT RUN (Android-SDK in dieser Umgebung nicht verfügbar). Frühere Erfolgsmeldungen gelten nicht als v0.4.95.1-Buildnachweis.

**Datenschutz:** ZIP enthält private Supabase-Recovery-Daten, nicht öffentlich bereitstellen.
