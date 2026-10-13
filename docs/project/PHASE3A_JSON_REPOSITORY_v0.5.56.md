# LFN-Scout v0.5.56 – Phase 3A: JSON-Repository (Entwicklungskandidat)

Ausgangsstand: vollständiges Projekt v0.5.55, versionCode 213. Diese Phase ändert **keine** App-Versionsnummer und **keine** vorhandenen Analyzer-/Scout-Aufrufe. Es existiert noch keine aktive Setup-Umschaltung auf JSON.

## Geliefert

`app/src/main/java/de/brummbeobachter/app/AnalyzerJsonSetupRepository.kt` ist ein isoliertes, derzeit unbenutztes Kotlin-Repository mit:

- App-internem Speicher `filesDir/analyzer_setups/setups/<UUID>.json`.
- Unbegrenzter Zahl von Setups, `create`, `save`, `load`, `delete`, `listing`.
- JSON-Schema-Kennung und `schemaVersion=3`, grundlegender Prüfung von ID, Name und Parametercontainer.
- MRU der letzten drei erfolgreich selektierten Setups, `activeId`, Index in `setup_index.json`.
- Atomarem Schreibvorgang über `android.util.AtomicFile`; Backup vor Löschung.
- `importNew` legt eine neue ID an und überschreibt nie importierte IDs; `exportCopy` erzeugt JSON als String für eine spätere SAF-Anbindung.
- Keine Zugriffe auf Scout-DataStore, Supabase, Recording oder SharedPreferences.

## Noch ausdrücklich offen (Sperrbedingungen für Aktivierung)

1. Vollständiger Parametervalidator für die 49 Analyzer- und 16 MON-Felder, Zoom, Gerätebindung, enums und Wertebereiche. Derzeit sind leere Parameterobjekte technisch akzeptiert: **nicht produktiv**.
2. Sichere, einmalige SharedPreferences-Migration mit unveränderlichen Backups, Fallback und Idempotenz.
3. Test für `AtomicFile`-Abbruch/Indexschaden, Datenmengen, Umbenennen/Duplizieren, ungültigen JSON-Import und Fehleranzeigen.
4. SAF-Dateiauswahl für Import/Export; der Repository-String allein ist keine UI-Funktion.
5. Vollständige Laufzeitkopplung, Draftzustand, MON-Snapshot, Aufnahmesperre, Gerätersatz.
6. Neue Analyzer-/Scout-Paneloberflächen und Scout-Schnellzugriff.
7. Android-JUnit, Gradle/Compose-Kompilierung, GitHub-TEST, Gerätetest.

## Selbstprüfung

`python3 tools/verify_lfn056_phase3a.py` verifiziert die Existenz, Schnittstellen und wichtige Schutzkriterien statisch. Sie ersetzt ausdrücklich keinen Kotlin-/Android-Build.
