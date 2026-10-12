# LFN-Scout v0.5.26 – Supabase-Erweiterung und 30-Minuten-Statistik

**Stand:** 10.10.2026  
**Status:** Remote-Supabase migriert; neue Edge Functions deployed und in zentralen Positiv-/Negativfällen real validiert. Android-Produktivintegration der Statistik-Endpunkte weiterhin offen.  
**Kompatibilitätsziel:** Das bisherige Beobachtungs- und WAV-Handling muss vollständig weiter funktionieren.

## 1. Zweck

v0.5.26 bereitet Supabase für drei Datenklassen vor, ohne die vorhandenen Tabellen umzubauen:

1. bestehende Brummton-Beobachtungen (`observations`),
2. gespeicherte Audios (`audio_recordings`) mit zusätzlichem Orts-/Triggerkontext,
3. Statistikdaten:
   - Statistik zu einer gespeicherten WAV (`measurement_parameter_sets`),
   - unabhängige fortlaufende 30-Minuten-Statistik (`half_hour_statistics`).

Die 30-Minuten-Daten sind bewusst **nicht** an `recording_id` gebunden. Dadurch können kontinuierlich Leq/L50/L90 gespeichert werden, auch wenn in diesem Zeitraum keine WAV dauerhaft gesichert wurde.

## 2. Unveränderte Legacy-Struktur

Die bestehenden Tabellen bleiben erhalten:

```text
observers
observations
app_config
audio_recordings
```

Bestehende Felder werden weder gelöscht noch umbenannt. `submit_observation` bleibt unverändert. Alte `upload_wav`-Requests bleiben gültig.

## 3. Gemeinsamer Identitäts-/Ortsgrundsatz

Jeder neue Statistikdatensatz enthält explizit:

```text
observer_id
latitude
longitude
location_accuracy_m
location_timestamp
location_source
location_status
comment
```

`observer_id` bleibt die vorhandene fachliche Teilnehmer-ID (z. B. `BB001`). Es gibt keine zweite `user_id`.

Positionen werden unterschieden:

- `VALID_LIVE`: echte Ereignisposition vom Client,
- `FALLBACK_OBSERVER_BASE`: keine Live-Position, gespeicherte Basisposition aus `observers`,
- `UNAVAILABLE`: weder Live- noch brauchbare Basisposition,
- `LEGACY_UNKNOWN`: historischer/noch nicht klassifizierter Zustand.

Bei `UNAVAILABLE` dürfen Latitude/Longitude `NULL` sein. Es wird nicht mehr so getan, als sei `0/0` eine echte Position.

## 4. Audio-Kontext

`audio_recordings` erhält additiv:

```text
latitude / longitude / location_*
capture_origin
trigger_type
trigger_metadata
```

Wichtig: das vorhandene `audio_source = UNP/MIC` bleibt die Mikrofonquelle und wird nicht umgedeutet.

`capture_origin` beschreibt dagegen, wo die Speicherung ausgelöst wurde:

```text
OBSERVER
ANALYZER
LEGACY_UNKNOWN
```

`trigger_type` beschreibt den Auslöser:

```text
MANUAL
ROLLING_LEVEL
ANALYZER_LEVEL
HARDWARE_EXTERNAL
LEGACY_UNKNOWN
```

## 5. Statistik zu gespeicherten Audios

`measurement_parameter_sets` enthält je gespeicherter WAV:

- genau eine mögliche `overall_statistics`-Zeile (`set_index=0`),
- 0 bis 40 `filter_statistics`-Zeilen (`set_index=1..40`).

Alle Zeilen tragen dieselbe `observer_id` und `recording_id`. Ortsdaten, Kommentar, Herkunft und Trigger werden serverseitig aus `audio_recordings` übernommen. So können Statistik und Audio nicht versehentlich unterschiedlichen Teilnehmern/Positionen zugeordnet werden.

### 5.1 Bisherige Statistikparameter bleiben benannt

Die vorhandenen Kennwerte werden **nicht** in P-Slots umbenannt. Sie besitzen feste Spalten:

```text
leq_dbfs
lmin_dbfs
lmax_dbfs
l1_dbfs
l10_dbfs
l50_dbfs
l90_dbfs
l95_dbfs
l99_dbfs
mean_level_dbfs
level_stddev_db
crest_factor_db
kurtosis_excess
valid_epochs
dominant_frequency_hz
```

`P01...P32` sind ausschließlich Reserve für später frei belegbare Zusatzparameter. Die Bedeutung dieser Slots wird über `parameter_lookup` versioniert.

## 6. 30-Minuten-Statistik – genaue Semantik

Neue Tabelle:

```text
half_hour_statistics
```

Ein Datensatz repräsentiert exakt ein halbstündiges Zeitfenster.

### 6.1 Zeitraster

Die Buckets sind auf:

```text
HH:00:00 bis HH:30:00
HH:30:00 bis (HH+1):00:00
```

ausgerichtet. Gespeichert werden `timestamptz`-Werte. Die DB prüft eine exakte Dauer von 1800 s sowie :00/:30-Ausrichtung.

### 6.2 Feste Kernwerte

Jeder Bucket hat feste Felder:

```text
range_min_hz
range_max_hz
leq_dbfs
l50_dbfs
l90_dbfs
```

Beispiel:

```text
observer_id       = BB001
bucket_start      = 2026-10-08T12:00:00Z
bucket_end        = 2026-10-08T12:30:00Z
statistics_source = ROLLING
range_min_hz      = 2
range_max_hz      = 25
leq_dbfs          = -71.4
l50_dbfs          = -71.8
l90_dbfs          = -73.1
coverage_seconds  = 1800
valid_epoch_count = 1800
quality_status    = COMPLETE
```

### 6.3 Coverage und Qualität

Ein 30-Minuten-Datensatz darf auch bei Datenlücken gespeichert werden, darf dann aber nicht wie eine vollständige Messung erscheinen.

Dafür gibt es:

```text
coverage_seconds   0..1800
valid_epoch_count  0..1800
quality_status
```

Status:

- `COMPLETE`: vollständige 30 Minuten,
- `PARTIAL`: nur Teil des Fensters vorhanden,
- `NO_DATA`: Bucket erzeugt, aber keine verwertbaren Messwerte,
- `INVALID`: Daten vorhanden, aber fachlich ungültig (z. B. Clipping/Fehler nach späterer Policy).

Damit kann der spätere Analyzer gezielt nur vollständige Buckets verwenden oder Teilwerte separat behandeln.

### 6.4 Statistikquelle

`statistics_source` ist unabhängig von Audio-Herkunft:

```text
ROLLING
ANALYZER
OBSERVER
OTHER
LEGACY_UNKNOWN
```

So können später beispielsweise kontinuierliche Rolling-Werte und Analyzer-Zeitreihen getrennt ausgewertet werden.

### 6.5 Kommentar

Auch ein 30-Minuten-Bucket besitzt `comment`. Das Feld darf `NULL` sein. Damit bleibt die gemeinsame Regel erhalten: Kommentar ist bei jedem fachlichen Datensatz strukturell vorgesehen.

### 6.6 Flexible Zusatzwerte

Zusätzlich stehen `P01...P32` zur Verfügung. Sie sind **nicht** für Leq/L50/L90 vorgesehen, sondern für später wechselnde Zusatzgrößen. Beispielhaft könnten hier später experimentelle Statistiken oder abgeleitete Umweltwerte liegen. Ihre Bedeutung wird über `lookup_version` + `parameter_lookup` beschrieben.

## 7. Datenmenge

Bei einem Bucket alle 30 Minuten entstehen pro Observer:

```text
48 Datensätze/Tag
336/Woche
ca. 17.520/Jahr
```

Das ist für PostgreSQL/Supabase eine kleine Zeitreihe. Indizes liegen auf `(observer_id,bucket_start)` und `bucket_start`.

## 8. Neue Edge Functions

### `upload_wav` v1.5.0

Bleibt rückwärtskompatibel. Neue optionale Requestfelder:

```text
latitude
longitude
location_accuracy_m
location_timestamp
location_source
capture_origin
trigger_type
trigger_metadata
```

Fehlen diese Felder, werden Legacy-Werte bzw. Observer-Basisposition verwendet.

### `submit_audio_statistics` v1.0.0

Speichert Gesamt- oder Filterstatistik für eine bereits vorhandene `recording_id`. Observer, Ort, Kommentar, Herkunft und Trigger werden aus dem Audio-Datensatz übernommen. Wiederholter Upload desselben `recording_id/set_kind/set_index/lookup_version` aktualisiert idempotent denselben Datensatz.

### `submit_half_hour_statistics` v1.0.0

Speichert einen 30-Minuten-Bucket. `bucket_id` dient als idempotente Ereignis-ID. Live-Geolocation ist optional im Request; fehlt sie, wird die Observer-Basisposition verwendet und als Fallback markiert.

## 9. Deployment-Reihenfolge

Verbindlich:

1. vollständiges Remote-Backup DB + Storage,
2. Migration `20261008_add_measurement_statistics_v0526.sql`,
3. `upload_wav` v1.5.0 deployen,
4. `submit_audio_statistics` deployen,
5. `submit_half_hour_statistics` deployen,
6. Legacy-Tests mit bisheriger App,
7. erst danach Android-Upload der neuen Statistik aktivieren.

Neue Edge Functions sind in `supabase/config.toml` mit `verify_jwt=false` eingetragen, weil das bestehende Custom-Key-Verfahren weiterhin serverseitig verwendet wird.

## 10. Was v0.5.26 absichtlich noch NICHT tut

- Die Android-App sendet noch keine Statistikdaten an die neuen Endpunkte.
- Sie erzeugt noch keinen produktiven halbstündlichen Cloud-Upload.
- Die Migration wurde vom ChatGPT-Arbeitsstand nicht auf das Remote-Projekt angewendet.
- Keine Remote-Edge-Function wurde deployed.
- KIT-MAST-/Inversionszeitreihen werden nicht nach Supabase kopiert.

Damit bleibt die Android-Laufzeit gegenüber v0.5.25 möglichst risikoarm. v0.5.26 liefert zunächst die geprüfte Backend-/Schema-Grundlage. Nach realem Deployment- und Legacy-Test kann die Android-Übertragung in einer folgenden Version aktiviert werden.

## 11. Späterer gemeinsamer Analyzer

Der spätere Datenanalysator kann über Zeit und Geolocation verbinden:

```text
Supabase:
  observations
  audio_recordings
  measurement_parameter_sets
  half_hour_statistics

GitHub/InversionAnalyzer:
  KIT-MAST-Zeitreihen
  Inversionsmodelle
  DWD/ICON/Profile
```

Dadurch können beispielsweise Brummtonrating, 30-min-Leq/L50/L90, Filterstatistik und meteorologische/Inversionsbedingungen auf derselben Zeitachse korreliert werden.

## 12. Remote-Deployment und Validierungsstand vom 10.10.2026

Die Remote-DB ist bis Migration `20261010174500` synchron. Zusätzlich zur Hauptmigration wurden Seed- und Korrekturmigrationen angewendet:

```text
20261010_seed_parameter_lookup_v0526.sql
20261010163000_fix_half_hour_bucket_id_default_v0526.sql
20261010174500_normalize_parameter_lookup_semantic_keys_v0526.sql
```

Die letzte Korrektur vereinheitlicht die Lookup-Semantik mit den tatsächlichen DB-/API-Feldern. Kanonisch gelten für die betroffenen Slots:

```text
P02 lmin_dbfs
P03 lmax_dbfs
P10 mean_level_dbfs
P11 level_stddev_db
P12 crest_factor_db
P13 kurtosis_excess
P14 valid_epochs
P15 dominant_frequency_hz   (nur Overall)
```

Insbesondere ist `valid_epochs` der kanonische Name; der frühere Lookup-Name `epoch_count` ist nicht mehr zu verwenden.

Real geprüft wurden unter anderem:

- COMPLETE und PARTIAL 30-Minuten-Buckets,
- DB-Ablehnung von Coverage >1800 s,
- DB-Ablehnung falscher Bucket-Dauer und falscher :00/:30-Ausrichtung,
- DB-Duplikatschutz,
- Overall `set_index=0`, Filter `1` und `40`, Ablehnung von Filter `41`,
- HTTP-Overall-/Filter-Upload über `submit_audio_statistics`,
- Idempotenz von `submit_audio_statistics`,
- HTTP-30-Minuten-Upload über `submit_half_hour_statistics`,
- Idempotenz von `submit_half_hour_statistics`,
- HTTP-Ablehnung von `coverage_seconds=1900`,
- Legacy-WAV-Upload gegen `upload_wav` v1.5.0.

Die Android-App sendet die neuen Statistikdaten sowie `capture_origin`/`trigger_type` noch nicht produktiv. Deshalb ist bei aktuellen Legacy-Uploads weiterhin `LEGACY_UNKNOWN` für diese Herkunftsfelder korrekt. Eine Folgeversion soll diese Android-Felder und die Statistikendpunkte aktiv anbinden.

Pre- und Post-Migrations-Dumps liegen unter `supabase/` vor. Ein Restore in ein komplett neues leeres Supabase-Projekt bleibt als separater End-to-End-Test zurückgestellt.

