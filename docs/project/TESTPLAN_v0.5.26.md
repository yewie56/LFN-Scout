# LFN-Scout v0.5.26 – Test- und Deployplan Supabase

**Stand:** 10.10.2026. `[x]` bedeutet in diesem Projektstand tatsächlich nachgewiesen; offene Punkte bleiben bewusst `[ ]`.

## A. Vor Deployment

- [x] Schema-Dump vor Migration erzeugt (`schema_before_v0526.sql`)
- [x] Data-Dump vor Migration erzeugt (`data_before_v0526.sql`)
- [x] Bucket `brumm-audio` gesichert (85 Dateien / 20.403.740 Bytes im dokumentierten Pre-Migrations-Backup)
- [ ] Edge Functions separat als Remote-Backup exportiert
- [x] lokaler Restore-/Rückfallpunkt durch Pre-Migrations-Dumps dokumentiert

## B. Migration

- [x] `20261008_add_measurement_statistics_v0526.sql` auf Remote-Projekt angewendet
- [x] vorhandene Tabellen weiterhin vorhanden
- [x] `audio_recordings` hat neue Location-/Origin-/Triggerfelder
- [x] `parameter_lookup`, `measurement_parameter_sets`, `half_hour_statistics` vorhanden
- [x] Seed-Migration `20261010_seed_parameter_lookup_v0526.sql` angewendet
- [x] `bucket_id`-Default-Fix `20261010163000...` angewendet
- [x] Lookup-Namensnormalisierung `20261010174500...` angewendet
- [ ] RLS remote separat per Policy-/Katalogabfrage nachgewiesen

## C. Legacy Regression

- [ ] bisherige installierte App sendet Rating 0..5 in diesem Testlauf separat protokolliert
- [ ] automatische Nichtantwort -1 separat protokolliert
- [ ] Kommentar Beobachtung separat protokolliert
- [ ] `get_recent_observations` separat protokolliert
- [ ] `get_all_observations` separat protokolliert
- [x] bisheriger WAV-Upload ohne neue Felder funktioniert
- [x] Audio-Kommentar beim geprüften WAV-Upload gespeichert
- [x] WAV liegt im unveränderten `brumm-audio`-Pfad
- [ ] DB-Insertfehler entfernt Storageobjekt weiterhin – separat erneut provoziert
- [ ] paused/revoked Key wird weiterhin blockiert – separat erneut provoziert

## D. `upload_wav` v1.5.0

- [x] alter Request -> 2xx; neue Herkunftsfelder fallen auf `LEGACY_UNKNOWN`
- [ ] Request mit Live-GPS -> `VALID_LIVE` gezielt über v1.5.0 getestet
- [x] ohne Live-GPS und gültige Observer-Basis -> `FALLBACK_OBSERVER_BASE`
- [ ] ohne irgendeine Position -> `UNAVAILABLE` gezielt getestet
- [ ] `capture_origin=OBSERVER` aus neuem Android-Client getestet
- [ ] `capture_origin=ANALYZER` aus neuem Android-Client getestet
- [ ] `trigger_type=MANUAL` aus neuem Android-Client getestet
- [ ] `ROLLING_LEVEL`, `ANALYZER_LEVEL`, `HARDWARE_EXTERNAL` gezielt getestet
- [ ] ungültige Koordinaten/Trigger/Herkunft -> 400 gezielt getestet

## E. Audio-Statistik

- [x] Overall-Zeile `set_index=0` in DB gespeichert
- [ ] 10 Filter als zusammengehöriger API-Lauf getestet
- [ ] 20 Filter als zusammengehöriger API-Lauf getestet
- [x] Filter-Grenze `set_index=40` in DB akzeptiert
- [x] Filter 41 -> DB verweigert (`mps_filter_index_chk`)
- [x] Observer-ID entspricht Audio beim HTTP-Test
- [x] Geolocation/Kommentar/Origin/Trigger werden beim HTTP-Test aus Audio übernommen
- [x] Retry aktualisiert denselben Datensatz statt Duplikat
- [x] feste Leq/L50/L90-Felder korrekt gespeichert
- [x] P-Slots optional/parallel zu benannten Feldern gespeichert
- [x] `submit_audio_statistics` GET v1.0.0 erreichbar
- [x] `submit_audio_statistics` Overall-POST erfolgreich
- [x] `submit_audio_statistics` Filter-1-POST erfolgreich
- [x] kanonischer Name `valid_epochs` verifiziert; Lookup entsprechend normalisiert

## F. 30-Minuten-Statistik

- [x] korrekt ausgerichteter :00–:30-Bucket akzeptiert
- [x] korrekt ausgerichteter :30–:00-Bucket akzeptiert
- [x] 14:10–14:40 verweigert (`hhs_bucket_alignment_chk`)
- [x] Dauer != 1800 s verweigert (`hhs_bucket_duration_chk`)
- [x] vollständiger Bucket: coverage=1800, quality=COMPLETE
- [x] Teilbucket: coverage=1200, quality=PARTIAL
- [ ] NO_DATA mit fehlenden Pegelwerten gezielt getestet
- [x] Leq/L50/L90 explizit benannt gespeichert
- [x] Frequenzbereich gespeichert
- [x] Observer-ID + Locationstatus gespeichert
- [x] Kommentar gespeichert
- [x] Retry mit gleicher `bucket_id` idempotent
- [x] HTTP-Eingabeprüfung verweigert `coverage_seconds=1900` mit `invalid_half_hour_request`

## G. Backup/Restore

- [x] neue Tabellen im Post-Migrations-Schema-Dump
- [x] Lookup/Statistikdaten im Post-Migrations-Data-Dump
- [x] `schema_after_v0526.sql` und `data_after_v0526.sql` auf lesbaren Dump-Inhalt geprüft
- [ ] Restore in leeres Supabase-Testprojekt – **zurückgestellt**
- [ ] Audio-Storage + DB nach Restore konsistent – **zurückgestellt**
- [ ] Edge Functions nach Restore deploybar – **zurückgestellt**

## H. Android / Folgeschritt

- [ ] Android sendet `capture_origin=OBSERVER|ANALYZER`
- [ ] Android sendet passende `trigger_type`-Werte
- [ ] Android ruft `submit_audio_statistics` produktiv auf
- [ ] Android ruft `submit_half_hour_statistics` produktiv auf
- [ ] Analyzer-UI-Text `Supabase ok` → `Cloud ok`
- [ ] `:app:testDebugUnitTest` für den aktuellen v0.5.26/v0.5.27-Quellstand real PASS
- [ ] `assembleDebug` real PASS
- [ ] APK-/ADB-/Gerätetest real PASS

## Freigabe

Der Backend-/Supabase-Teil ist in den oben mit `[x]` markierten Punkten real validiert. Nicht markierte Legacy-, Restore- und Android-Punkte dürfen nicht als getestet gelten. Die produktive Android-Statistikübertragung gehört in die Folgeversion.
