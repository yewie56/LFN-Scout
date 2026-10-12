# LFN-Scout – verbindliche Übergabe für neuen Chat

**Stand:** 10.10.2026  
**Aktueller Quellstand:** v0.5.26 / `versionCode 173`  
**Sichtbarer App-Name:** LFN-Scout  
**Android-Paket:** `de.brummbeobachter.app`  
**Nächste geplante Version:** v0.5.27 / `versionCode 174`

## 1. Arbeitsregel für den neuen Chat

Vor jeder Codeänderung zuerst den tatsächlich hochgeladenen vollständigen Projektstand lesen, insbesondere `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md`, `README.md`, `docs/project/`, die Kotlin-/Android-Quellen, `supabase/` und die vorhandenen Tests/Self-Tests. `docs/project/` ist die dauerhaft verbindliche Projektdokumentation; `docs/superpowers/` enthält interne aufgabenspezifische Entwicklungspläne. Bei Widersprüchen hat der reale aktuelle Quellstand Vorrang vor älteren Übergabetexten. Keine nicht ausgeführten Tests als PASS deklarieren.

## 2. Status v0.5.26

v0.5.26 bleibt die Android-Quellversion. Der Supabase-Backendteil wurde nach Erstellung des ursprünglichen Source-Candidate-Pakets am 10.10.2026 real migriert, deployed und in zentralen Positiv-/Negativfällen validiert. Die Android-App nutzt die neuen Statistik-Endpunkte jedoch noch nicht produktiv. Ein aktueller realer v0.5.26-Gradle-/APK-/Gerätetest ist in dieser Übergabe nicht nachgewiesen.

## 3. Supabase-Projekt und Identität

Remote-Projekt: `BrummBeobachter`, Project Ref `kbejualnruiwtnmeytif`.

Die App verwendet keinen Supabase-E-Mail-/Passwort-Login. Es gilt der bestehende Observer-Key-Mechanismus: `observer_id` bleibt die fachliche Teilnehmer-ID (z. B. `BB001`), serverseitig liegt nur der SHA-256-Hash des Klartext-Keys. Die relevanten Edge Functions sind mit `verify_jwt=false` konfiguriert und prüfen den Observer-Key selbst.

## 4. Aktive Migrationen

Lokal und remote synchron:

```text
20260903
20260908
20260909
20261008
20261010
20261010163000
20261010174500
```

Neue v0.5.26-Migrationen:

- `20261008_add_measurement_statistics_v0526.sql`: erweitert `audio_recordings` additiv um Standort-/Origin-/Triggerfelder und erzeugt `parameter_lookup`, `measurement_parameter_sets`, `half_hour_statistics`.
- `20261010_seed_parameter_lookup_v0526.sql`: 64 Lookup-Slots, je 32 für Overall und Filter.
- `20261010163000_fix_half_hour_bucket_id_default_v0526.sql`: setzt für `half_hour_statistics.bucket_id` den Default `gen_random_uuid()`.
- `20261010174500_normalize_parameter_lookup_semantic_keys_v0526.sql`: vereinheitlicht Lookup-Semantik mit den tatsächlichen DB/API-Feldnamen.

## 5. Kanonische Statistiknamen

Ab jetzt sind die DB/API-Bezeichnungen die kanonischen Namen. Wichtig, weil während des HTTP-Tests die Inkonsistenz `epoch_count` gegenüber `valid_epochs` entdeckt wurde.

```text
P01  leq_dbfs
P02  lmin_dbfs
P03  lmax_dbfs
P04  l1_dbfs
P05  l10_dbfs
P06  l50_dbfs
P07  l90_dbfs
P08  l95_dbfs
P09  l99_dbfs
P10  mean_level_dbfs
P11  level_stddev_db
P12  crest_factor_db
P13  kurtosis_excess
P14  valid_epochs
P15  dominant_frequency_hz   (nur overall_statistics)
```

Bei `filter_statistics` ist P15 Reserve/inaktiv. Frühere Lookup-Bezeichnungen wie `min_epoch_dbfs`, `max_epoch_dbfs`, `mean_epoch_dbfs`, `stddev_epoch_db`, `crest_db`, `excess_kurtosis` und `epoch_count` nicht neu in App-Code übernehmen.

## 6. Datenmodell

### Audio-bezogene Statistik

Tabelle `measurement_parameter_sets`, gebunden an `observer_id` + `recording_id`.

- `overall_statistics`: `set_index=0`
- `filter_statistics`: `set_index=1..40`
- Filter 41 muss abgewiesen werden.
- Benannte Statistikspalten bleiben erhalten; P01..P32 sind der zusätzliche flexible/versionierte Layer und kein Ersatz der Kernfelder.
- Standort, Kommentar, Herkunft und Trigger werden beim Statistik-Endpoint aus dem zugehörigen `audio_recordings`-Datensatz übernommen.

### 30-Minuten-Statistik

Tabelle `half_hour_statistics`, ausdrücklich nicht an einzelne WAVs gebunden.

- exakte Dauer 1800 s
- Start nur auf `:00` oder `:30`
- feste Kernwerte `leq_dbfs`, `l50_dbfs`, `l90_dbfs`
- `coverage_seconds` 0..1800
- `valid_epoch_count` 0..1800
- `quality_status`: `COMPLETE`, `PARTIAL`, `NO_DATA`, `INVALID`
- Standortblock, Frequenzbereich, Kommentar, App-Version, Lookup-Version, P01..P32, Metadata

## 7. Edge Functions – remote deployed

### `upload_wav` v1.5.0

Unterstützt neue optionale Felder `capture_origin`, `trigger_type`, `trigger_metadata` und Standortdaten. Alte Requests bleiben gültig. Wenn der aktuelle Android-Client die neuen Felder nicht mitsendet, werden `capture_origin` und `trigger_type` korrekt als `LEGACY_UNKNOWN` gespeichert. Ohne Live-GPS kann `FALLBACK_OBSERVER_BASE` verwendet werden.

Vorgesehene Herkunftswerte:

```text
OBSERVER
ANALYZER
LEGACY_UNKNOWN
```

Trigger u. a.:

```text
MANUAL
ROLLING_LEVEL
ANALYZER_LEVEL
HARDWARE_EXTERNAL
LEGACY_UNKNOWN
```

### `submit_audio_statistics` v1.0.0

Akzeptiert die kanonischen benannten Metriken aus Abschnitt 5. Idempotenz erfolgt logisch über `recording_id`, `set_kind`, `set_index`, `lookup_version`: bei erneutem Senden wird derselbe Datensatz aktualisiert statt dupliziert.

### `submit_half_hour_statistics` v1.0.0

Validiert Observer-Key, exaktes Zeitfenster, :00/:30-Ausrichtung, Frequenzbereich, Coverage, Epochenzahl, Qualität, Standortkonsistenz und P-Slots. Idempotenz über `bucket_id`.

## 8. Tatsächlich durchgeführte Backendtests

### DB / Migration

**PASS:**

- Tabellen und neue `audio_recordings`-Felder vorhanden.
- `parameter_lookup` enthält 64 Slots.
- `half_hour_statistics.bucket_id` hat Default `gen_random_uuid()`.
- COMPLETE-Bucket mit 1800 s gespeichert.
- PARTIAL-Bucket mit 1200 s gespeichert.
- Coverage 1900 s durch `hhs_coverage_chk` abgewiesen.
- 20-Minuten-Bucket durch `hhs_bucket_duration_chk` abgewiesen.
- 14:10–14:40 durch `hhs_bucket_alignment_chk` abgewiesen.
- doppelter Bucket durch Unique Constraint abgewiesen.
- `overall_statistics` mit `set_index=0` gespeichert.
- `filter_statistics` mit `set_index=1` gespeichert.
- `filter_statistics` mit `set_index=40` gespeichert.
- `filter_statistics` mit `set_index=41` durch `mps_filter_index_chk` abgewiesen.
- Lookup nach `20261010174500` auf kanonische Namen geprüft.

### `upload_wav`

**PASS:** bestehender alter WAV-Upload funktioniert gegen v1.5.0. Bei den geprüften neuen Uploads wurden wegen noch fehlender Android-Felder `capture_origin=LEGACY_UNKNOWN` und `trigger_type=LEGACY_UNKNOWN` gespeichert; Standort-Fallback `FALLBACK_OBSERVER_BASE` funktionierte.

### `submit_audio_statistics`

**PASS:** GET v1.0.0, Overall-POST, Datenbankinhalt, Idempotenz (`duplicate=true` bei Wiederholung), Filter-1-POST. Der erste Versuch mit `epoch_count` wurde erwartungsgemäß als `unknown_metric` zurückgewiesen und führte zur anschließenden kanonischen Namensbereinigung auf `valid_epochs`.

### `submit_half_hour_statistics`

**PASS:** GET v1.0.0, COMPLETE-POST, Datenbankinhalt, Idempotenz, Negativtest `coverage_seconds=1900` mit `{"error":"invalid_half_hour_request"}`.

## 9. Backups

Vor Migration vorhanden:

- `supabase/schema_before_v0526.sql` – dokumentiert 10.157 Bytes
- `supabase/data_before_v0526.sql` – dokumentiert 140.100 Bytes
- Storage-Backup des Buckets `brumm-audio`: dokumentiert 85 Dateien / 20.403.740 Bytes (liegt gemäß ACTIVE/BASE-Regel ggf. im separaten BASE-/Recovery-Bestand)

Nach Migration vorhanden:

- `supabase/schema_after_v0526.sql` – 23.855 Bytes
- `supabase/data_after_v0526.sql` – 185.976 Bytes

Beide Post-Dumps wurden auf lesbaren PostgreSQL-Dump-Inhalt geprüft.

## 10. Noch NICHT als PASS behandeln

- kompletter Legacy-Regressionssatz für Rating 0..5, -1, get_recent/get_all, paused/revoked und Storage-Rollback wurde in diesem Backendlauf nicht vollständig neu protokolliert.
- gezielte v1.5.0-Tests für Live-GPS, `UNAVAILABLE`, alle neuen Origin-/Triggerwerte und ungültige Trigger fehlen noch.
- NO_DATA-Bucket wurde noch nicht gezielt getestet.
- praktischer Restore in ein komplett neues leeres Supabase-Projekt einschließlich DB + Storage + Functions ist **zurückgestellt**.
- aktueller v0.5.26-Android-Lauf `:app:testDebugUnitTest` ist in dieser Übergabe nicht nachgewiesen.
- `assembleDebug`, APK-/ADB-Installation und Gerätetest des aktuellen v0.5.26-Quellstands sind nicht nachgewiesen.
- Android nutzt `submit_audio_statistics` und `submit_half_hour_statistics` noch nicht produktiv.
- Android sendet `capture_origin` und `trigger_type` noch nicht.

## 11. Auftrag für v0.5.27

1. Tatsächlichen v0.5.26-Quellstand und alle verbindlichen Dokumente lesen.
2. Android-WAV-Upload um `capture_origin` und `trigger_type` ergänzen.
3. Observer manueller Save: `capture_origin=OBSERVER`, `trigger_type=MANUAL`.
4. Analyzer manueller Save: `capture_origin=ANALYZER`, `trigger_type=MANUAL`.
5. `submit_audio_statistics` produktiv anbinden: Overall + Filter 1..40, kanonische Feldnamen verwenden.
6. `submit_half_hour_statistics` für regelmäßige Leq/L50/L90-Buckets integrieren.
7. Offline-/Retry-/Idempotenzverhalten so auslegen, dass keine Duplikate entstehen.
8. Analyzer-Meldung beim Cloud-Speichern von `Supabase ok` auf **`Cloud ok`** ändern.
9. Bestehende Audio-/Bewertungs-/Analyzer-Funktionen unverändert erhalten.
10. Regressionstests für die neuen Pfade ergänzen und in den integrierten Self-Test aufnehmen.
11. `PROJECT_STATUS.md`, `CHANGELOG.md` und betroffene `docs/project/` nachführen.
12. Real `:app:testDebugUnitTest` und `assembleDebug` ausführen.
13. Danach APK-/ADB-/Gerätetest. Erst dann v0.5.27 als vollständig gebaut/getestet kennzeichnen.

## 12. Bestehende Projektregeln, die weiter gelten

- Paket-ID nicht ändern.
- Produktname bleibt LFN-Scout.
- Keine Kernfunktion stillschweigend entfernen.
- Temporäre Statusmeldungen als Overlay/Toast; keine bestehende Mess-/Displayzeile spontan umwidmen.
- Analyzer-/Observer-Audio verwenden denselben grundsätzlichen Speicher-/Upload-Lebenszyklus.
- lokaler Hauptpfad `Download/LFN-Scout/`; keine unnötigen neuen Unterverzeichnisse `audio/` oder `LocalSaves/`.
- Herkunft über Metadaten, nicht über Pfad oder Dateiname.
- Analyzer-Save PRE-GAIN; digitale Verstärkung exakt einmal berücksichtigen.
- SAVE AUDIO friert Audio vor dem Kommentar-Dialog ein.
- Kommentar-/Timeout-Verhalten des bestehenden gemeinsamen Dialogs erhalten.
- bis zu 40 Filterbänder.
- Android-Zurück navigiert im Verlauf und soll laufende Analyse/Aufnahme nicht unbeabsichtigt stoppen.
- Regressionstests und integrierter Self-Test früh mitplanen.
- bereitgestellte ZIPs möglichst ohne zusätzliches übergeordnetes Unterverzeichnis.

## 13. Starttext für den neuen Chat

> Wir setzen LFN-Scout fort. Maßgeblicher Ausgangsstand ist v0.5.26 / versionCode 173. Lies zuerst den vollständigen hochgeladenen Quellstand sowie DIRECTIVES.md, BUILD_WORKFLOWS.md, PROJECT_STATUS.md, CHANGELOG.md und docs/project/. Die Supabase-Migrationen bis 20261010174500 sind remote aktiv. upload_wav v1.5.0, submit_audio_statistics v1.0.0 und submit_half_hour_statistics v1.0.0 sind deployed und in den dokumentierten Kernfällen getestet. Die Statistik-Lookup-Namen wurden auf die kanonischen DB/API-Namen vereinheitlicht, insbesondere valid_epochs statt epoch_count. Android nutzt die neuen Statistik-Endpunkte noch nicht produktiv und sendet capture_origin/trigger_type noch nicht; Legacy-WAVs zeigen daher LEGACY_UNKNOWN. Für v0.5.27 sollen diese Android-Anbindungen umgesetzt werden; außerdem muss die Analyzer-Meldung „Supabase ok“ in „Cloud ok“ geändert werden. Android-Build und Gerätetest des aktuellen Stands sind noch offen und dürfen nicht als PASS bezeichnet werden. Bestehende Funktionen erhalten, Ist/Soll strikt trennen, Regressionstests und Self-Test ergänzen.

## 14. Zurückgestellte Aktion

Praktischer Restore-Test auf ein komplett neues leeres Supabase-Projekt und anschließender End-to-End-Wiederherstellungstest. Nicht vergessen, aber nicht stillschweigend als bereits bestanden markieren.
