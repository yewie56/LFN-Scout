# Build Verification v0.5.26

**Erstellungsstand:** 08.10.2026  
**Remote-Backend-Nachvalidierung:** 10.10.2026

## Änderung

Backend-/Supabase-Quelländerung und Dokumentation; Android v0.5.26 ruft die neuen Statistik-Endpunkte noch nicht produktiv auf.

## In der ursprünglichen Erstellungsumgebung ausgeführt

- `python tools/verify_v0526_delivery.py`: **32/32 PASS** (damaliger Quellvertrag)
- TypeScript-Transpile-/Syntaxprüfung aller 7 Edge-Function-`.ts`: **7/7 PASS**
- Kotlin-PSI: **92 Dateien / 0 Syntaxfehler PASS**
- Wasserfall-Regressionscheck: **PASS**
- ZIP-CRC/Probe-Extraktion/Manifest: bei damaliger Paketierung **PASS**

Diese Prüfungen waren statisch/hostbasiert und waren kein Android-/Remote-Supabase-Nachweis.

## Am 10.10.2026 real gegen Remote-Supabase ausgeführt

### Migrationen

**PASS:** Remote-Projekt synchron bis einschließlich:

- `20260903`
- `20260908`
- `20260909`
- `20261008`
- `20261010`
- `20261010163000`
- `20261010174500`

### DB-Struktur und Constraints

**PASS:**

- neue Tabellen vorhanden: `parameter_lookup`, `measurement_parameter_sets`, `half_hour_statistics`
- neue `audio_recordings`-Felder vorhanden
- `parameter_lookup` enthält 64 Slots
- `half_hour_statistics.bucket_id` Default `gen_random_uuid()`
- COMPLETE 1800 s gespeichert
- PARTIAL 1200 s gespeichert
- Coverage 1900 s durch `hhs_coverage_chk` abgewiesen
- 20-Minuten-Bucket durch `hhs_bucket_duration_chk` abgewiesen
- 30-Minuten-Bucket 14:10–14:40 durch `hhs_bucket_alignment_chk` abgewiesen
- Duplikat-Bucket durch Unique Constraint abgewiesen
- `overall_statistics` mit `set_index=0` gespeichert
- `filter_statistics` mit `set_index=1` gespeichert
- `filter_statistics` mit `set_index=40` gespeichert
- `filter_statistics` mit `set_index=41` durch `mps_filter_index_chk` abgewiesen

### Lookup-Namensbereinigung

**PASS:** Nach Migration `20261010174500` verwenden Lookup-Slots die kanonischen DB/API-Namen, darunter `valid_epochs` statt `epoch_count`.

### Edge Functions

**PASS deployed:**

- `upload_wav` v1.5.0
- `submit_audio_statistics` v1.0.0
- `submit_half_hour_statistics` v1.0.0

**PASS realer HTTP-/Legacy-Test:**

- bestehender alter WAV-Upload funktioniert gegen `upload_wav` v1.5.0
- Legacy-Fallback erzeugt `capture_origin=LEGACY_UNKNOWN`, `trigger_type=LEGACY_UNKNOWN`
- Standort-Fallback `FALLBACK_OBSERVER_BASE` beobachtet
- `submit_audio_statistics`: GET, Overall-POST, DB-Inhalt, Idempotenz und Filter-1-POST PASS
- `submit_half_hour_statistics`: GET, COMPLETE-POST, DB-Inhalt, Idempotenz PASS
- ungültiges `coverage_seconds=1900` wird HTTP-seitig mit `invalid_half_hour_request` abgewiesen

## Backup

**PASS vorhanden/lesbar:**

- `supabase/schema_before_v0526.sql`
- `supabase/data_before_v0526.sql`
- `supabase/schema_after_v0526.sql`
- `supabase/data_after_v0526.sql`

Der praktische Restore auf ein komplett neues leeres Supabase-Projekt ist **NOT RUN / zurückgestellt**.

## Weiterhin nicht nachgewiesen

- realer v0.5.26-Android-Lauf `:app:testDebugUnitTest`
- `assembleDebug`
- v0.5.26-APK-/ADB-Installation
- vollständiger Gerätetest dieses Quellstands
- produktiver Android-Aufruf von `submit_audio_statistics`
- produktiver Android-Aufruf von `submit_half_hour_statistics`
- Android-seitiges Senden von `capture_origin`/`trigger_type`

## Freigabestatus

Der **Backend-/Supabase-Teil von v0.5.26 ist deployed und in den oben aufgeführten Punkten real validiert**. Der gesamte Android-Release v0.5.26 ist damit jedoch noch **nicht als vollständig gebaut/getestet nachgewiesen**.
