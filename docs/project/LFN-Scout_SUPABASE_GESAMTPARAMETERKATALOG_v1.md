# LFN-Scout – Supabase-Gesamtparameterkatalog

**Dokumentversion:** 1.0 · **Stand:** 25.09.2026  
**Ausgangscode:** Android v0.5.17 (Portraitmodus auf Xiaomi vom Nutzer bestätigt)  
**Status:** Kumulativer **Datenvertrags- und Schemaentwurf**, **keine implementierte Datenbankmigration**, kein erneuter Build-/Restore-Test.

## 0. Geltungsbereich, Quellen und Leseschlüssel

Dieses Dokument bündelt **sämtliche bislang konkret benannten und geplanten Parameter**, die LFN-Scout mit Supabase austauschen oder für dessen Betrieb/Recovery verwalten soll: Teilnehmer/Keys, Beobachtungen, Audio, einstellbare Statistikfenster, Gesamt- und Spektralstatistik, unabhängige Filterbankstatistik (maximal **40** FAST-Bänder), Impulse, Modulation, Messqualität, wirksame Analyzer-Konfiguration, Upload-/Tagesquoten, Backup, Restore, Löschung und Audit.

Ausgewertete Projektgrundlagen:

- `LFN-Scout_Android_v0.5.17_Portrait.zip`, insbesondere `supabase/schema_full.sql`, `supabase/migrations/`, `supabase/functions/`, `AnalyzerInspectionExport.kt`, `PROJECT_STATUS.md`;
- `LFN-Scout_STAT120_GEMEINSAMER_AUDIO_SAVE_Anforderung_v1.md`;
- `LFN-Scout_STAT_AUDIO_SAVE_KONFIGURIERBARE_STATISTIK_v2.md` (neuere Festlegungen haben Vorrang);
- `LFN-Scout_SUPABASE_MANAGEMENT_TOOL_SPEZIFIKATION_v1.md`;
- die im Gespräch beschlossenen Bedingungen: max. **20 Teilnehmer**, Aktion **14 Tage**, gespeicherte WAV **standardmäßig 1.000 Hz / 16 Bit / Mono**, Aufnahmedauer und Statistikfrequenzbereich einstellbar, FILTER MIN/MAX unabhängig, maximal **40** Filter, gemeinsame Speicherung Observer/Analyzer.

**Kennzeichen:** `IST` = Feld in v0.5.17 im mitgelieferten SQL-Schema vorhanden. `PLAN` = noch zu implementierender Datenvertrags-/Schemaanteil. `OPTION` = noch nicht verbindlich spezifizierte Erweiterung. Ein Feld in einer geplanten Tabelle ist **nicht** automatisch eine existierende SQL-Spalte. In allen Tabellen `null` für tatsächlich fehlende/ungültige Messwerte; **nicht** mit `0` oder geratenen Defaults auffüllen. `timestamptz` und JSON-Datumsangaben grundsätzlich UTC; nur lokale Tagesquote mit expliziter Zeitzone. Einheiten immer ausweisen.

**Architektur:** Gemeinsamer `StatisticsWindowEngine` + lokaler Paket-Store + Upload für Observer und Analyzer; Observer-Paket **ohne** `analyzer_setup`, Analyzer-Paket **mit effektivem LIVE-Snapshot**. WAV und Statistik beziehen sich exakt auf denselben Sample-Zeitraum. FAST-Filtermerkmale sind nur enthalten, wenn sie für denselben Zeitraum und tatsächlichen 8-kHz-Pfad vorliegen. Kennzahl- oder Tabellenname allein beweist keine messtechnische Validität.

## 1. Abbildung auf Supabase (IST und PLAN)

| Einheit | Stand | Inhalt / Zuordnung |
|---|---|---|
| `public.app_config` | IST | Beobachtungsintervall und Antwortzeitfenster, Tageszeiten. |
| `public.observers` | IST | Teilnehmer-ID, Standort, SHA-256-Key-Hash, aktiv/pausiert/widerrufen. |
| `public.observations` | IST | Geplante und tatsächliche Beobachtungszeit, Rating, Koordinaten, optional Kommentar und WAV-Verweis. |
| `public.audio_recordings` | IST | Separate WAV-Metadaten, Teilnehmerverweis, Storage-Pfad, Kommentar. |
| `storage: brumm-audio` | IST | Privater WAV-Bucket, WAV-Bytes separat vom PostgreSQL-Dump. |
| `public.participant_keys` **oder** geeignete Key-Revisionstabelle | PLAN | Key-ID/Generation, Ablaufdatum, Berechtigungen, Widerruf und neuester Key; die konkrete Namens-/Schemaentscheidung ist offen. |
| `public.audio_analysis` | PLAN | Je Aufnahme ein versionierter Gesamtstatistik-, Spektral-, Modulations- und Qualitätsdatensatz. |
| `public.audio_analysis_bands` | PLAN | Eine Zeile je real ausgewertetem Band (0–40 FAST, daneben optional eigene Statistikbänder). |
| `public.audio_impulse_events` | PLAN | Optionale Ereignisliste je Aufnahme. |
| `public.audio_save_jobs` / Queue-Protokoll | PLAN | Idempotenz, Offline-/Uploadstatus, Paketvollständigkeit und Wiederaufnahme. |
| `public.daily_save_usage` / serverseitige Quotenfunktion | PLAN | Gemeinsames Tageskontingent der beiden Panels, ggf. geräteübergreifend. |
| `public.management_audit` oder geschütztes lokales Audit | PLAN | Management-/Backup-/Restore-/Löschaktionen; keine Secrets. |
| Recovery-ZIP mit `manifest.json` | PLAN | Schema, Daten, Storage-Objekte, Checksums, Regeln/Deploymentinformationen. |

**Keine stille Schemazusage:** Namen der neuen Tabellen/Felder sind vorgeschlagen, nicht deployt. Bestehende `observations.wav_*`-Metadaten und `audio_recordings` müssen beim Design als **ein konsistenter WAV-Bezug** vereinheitlicht werden; keine doppelten widersprüchlichen Quellen schaffen. Ein optionales `observation_event_id` in Audio-Metadaten kann die Zuordnung ermöglichen; bestehende historische Audioeinträge können unverbunden sein.

## 2. IST-Felder im mitgelieferten v0.5.17-Schema – exakte Namen

### 2.1 `public.app_config` (IST)

| Feld | Typ (SQL) | Bedeutung |
|---|---|---|
| `config_id` | text PK | Konfiguration (`default`). |
| `interval_minutes` | integer | Beobachtungsintervall, Default 30. |
| `response_window_minutes` | integer | Antwortzeitfenster, Default 10. |
| `active_from` | time | Beobachtungsbeginn, Default 07:00. |
| `active_until` | time | Beobachtungsende, Default 23:00. |
| `updated_at` | timestamptz | Aktualisierung. |

### 2.2 `public.observers` (IST)

| Feld | Typ (SQL) | Bedeutung |
|---|---|---|
| `observer_id` | text PK | Stabile pseudonyme Teilnehmerreferenz. |
| `latitude`, `longitude` | double precision | Registrierter Standort; `0/0` darf nicht als präzise Geolokalisierung interpretiert werden. |
| `key_hash` | text UNIQUE | SHA-256-Hash des bestehenden Keys, **kein Klartextkey**. |
| `enabled` | boolean | Grundfreigabe. |
| `created_at` | timestamptz | Anlegezeit. |
| `status` | text | `active`, `paused`, `revoked`. |
| `revoked_at`, `paused_at` | timestamptz NULL | Verwaltungszeitpunkte. |

### 2.3 `public.observations` (IST)

| Feld | Typ (SQL) | Bedeutung |
|---|---|---|
| `id` | bigint IDENTITY PK | Interner Tabellenindex. |
| `event_id` | uuid UNIQUE | Eindeutiges Ereignis, Duplikatschutz. |
| `observer_id` | text FK | Teilnehmerreferenz. |
| `scheduled_time` | timestamptz | Geplanter Bewertungszeitpunkt. |
| `response_time` | timestamptz NULL | Tatsächliche Antwortzeit; bei fehlender Antwort NULL. |
| `rating` | smallint | Bestehender Wertebereich `-1..5` (`-1` automatisch/Timeout, `0` nicht beobachtet, `1..5` Stärke gemäß vorhandener Bewertungsskala). |
| `latitude`, `longitude` | double precision | Koordinaten zum Beobachtungsereignis. |
| `app_version` | text NULL | Versionsbezeichnung. |
| `wav_present` | boolean | Bestehender WAV-Präsenzindikator. |
| `wav_path` | text NULL | Bestehender WAV-Verweis. |
| `wav_duration_sec` | integer NULL | WAV-Dauer; **bestehender CHECK 1..600** beachten, falls künftig längere Fenster erlaubt werden. |
| `wav_sample_rate` | integer NULL | WAV-Samplingrate. |
| `wav_channels` | integer NULL | Kanalzahl. |
| `wav_bits_per_sample` | integer NULL | Bitauflösung. |
| `wav_size_bytes` | bigint NULL | WAV-Größe. |
| `wav_sha256` | text NULL | Integritätsprüfsumme. |
| `received_at` | timestamptz | Serverempfang. |
| `comment` | text NULL | Optionaler Kommentar. |

### 2.4 `public.audio_recordings` (IST)

| Feld | Typ (SQL) | Bedeutung |
|---|---|---|
| `recording_id` | uuid PK | Aufnahmekennung und künftige Hauptverknüpfung. |
| `observer_id` | text FK | Teilnehmer. |
| `recorded_at` | timestamptz | Aufnahmedatum/-zeit. |
| `sample_rate_hz` | integer | Derzeit gespeicherte WAV-Rate. |
| `channels` | integer | Kanalzahl. |
| `bits_per_sample` | integer | Bittiefe. |
| `gain_db` | integer | Digitalgain in der derzeitigen Schnittstelle. |
| `clipped_samples` | integer | Clippingzähler. |
| `storage_path` | text | Privater Storage-Pfad. |
| `created_at` | timestamptz | Datensatzerstellung. |
| `comment` | text NULL | Optionaler Audiokommentar. |

**Wichtige IST-Grenze:** Die aktuelle Edge Function `upload_wav` akzeptiert nur `sample_rate_hz=1000`, `channels=1`, `bits_per_sample=16` und prüft ein WAV-Längenmaximum von 1 MiB. Sie kann daher zukünftige 8-kHz-WAVs oder beliebig lange konfigurierte Aufnahmefenster **nicht ohne Änderung** annehmen. Die aktuellen Tabellen enthalten noch keine vollständigen Statistikgruppen A–H, Filterzeilen, neuen Key-Rechte oder gemeinsame serverseitige Quote.

## 3. PLAN – Teilnehmer, Keys, Berechtigungen und Aktionsrahmen

### 3.1 Teilnehmerverwaltung

| ID | Feld | Typ/Einheit | Bedeutung |
|---|---|---|---|
| P01 | `observer_id` | text | Unveränderliche Teilnehmer-ID; Verweis in allen Nutzdatentabellen. |
| P02 | `display_name` | text, **lokal/geschützt** | Administrativer Name; Aufnahme in Supabase/Backup nur nach Datenminimierungsentscheidung. |
| P03 | `registration_location_text` | text NULL | Vom Admin eingegebener Ort. |
| P04–P05 | `latitude`, `longitude` | double | Teilnehmerstandort; ggf. abgeschnittene/gerundete Genauigkeit dokumentieren. |
| P06 | `location_source` | enum | `MANUAL`, `GEOCODED`, `DEVICE`, `UNKNOWN`. |
| P07 | `location_precision` | text/number | Rundung oder räumliche Auflösung. |
| P08 | `enabled` | boolean | Technische Freigabe. |
| P09 | `status` | enum | `active`, `paused`, `revoked`; bei Löschung endgültig sperren. |
| P10–P12 | `created_at`, `paused_at`, `revoked_at` | UTC | Statuszeiten. |
| P13 | `last_observation_at` | UTC NULL, abgeleitet | Nur Verwaltungsübersicht; nicht als zweite widersprüchliche Quelle führen. |
| P14 | `last_audio_at` | UTC NULL, abgeleitet | Analog. |
| P15–P16 | `observation_count`, `audio_count` | integer, abgeleitet | Anzeige im Verwaltungstool. |

### 3.2 Key-Lifecycle (zurückgestellt, PLAN)

| ID | Feld | Typ/Einheit | Bedeutung |
|---|---|---|---|
| K01 | `key_id` | uuid/text | Stabile ID der Key-Ausgabe. |
| K02 | `observer_id` | text FK | Zugeordneter Teilnehmer. |
| K03 | `key_generation` | integer | Monoton fortschreitende Version; **nur neueste gültig**. |
| K04 | `key_hash` | text | Prüfwert des gültigen Tokens; Klartext nicht speichern/loggen. |
| K05 | `issued_at` | UTC | Ausgabezeit. |
| K06 | `valid_from` | UTC | Gültigkeitsbeginn (falls abweichend). |
| K07 | `expires_at` | UTC | Einstellbares Verfallsdatum; manipulationsgeschützt **im Key kodiert** und serverseitig geprüft. |
| K08 | `validity_duration_s` | integer | Gewählte Laufzeit bei Erstellung. |
| K09 | `key_status` | enum | `ACTIVE`, `EXPIRED`, `REVOKED`, `SUPERSEDED`. |
| K10 | `revoked_at` | UTC NULL | Entwertungszeitpunkt. |
| K11 | `superseded_by_key_id` | uuid/text NULL | Nachfolgekey. |
| K12 | `allow_wav_1khz` | boolean | Berechtigung für 1.000-Hz-WAV. |
| K13 | `allow_wav_8khz` | boolean | Berechtigung für 8.000-Hz-WAV. |
| K14 | `key_format_version` | integer/text | Token-/Signaturformat. |
| K15 | `signature_key_id` | text | Bezeichner des serverseitigen Signaturschlüssels, **nicht dessen Geheimnis**. |
| K16 | `last_validated_at` | UTC NULL, optional | Betriebsdiagnose, nur wenn erforderlich. |

**Regel:** Token ist nur akzeptiert, wenn Signatur/Hash, Zeitraum, Teilnehmerstatus und neueste `key_generation`/`key_id` passen. Ein neues Token widerruft ältere sofort. Berechtigung beim Upload **serverseitig** prüfen; kein Admin-Key in Android. Das Verhalten bei Offline-Geräten, Ablauf/Zeitzonenwechsel und Restore bleibt konkret zu entscheiden. Ein alter Backup-Key darf durch Restore nicht unbeabsichtigt reaktiviert werden.

### 3.3 Kontingent/Aktionsrahmen (PLAN, Policy)

| ID | Feld | Typ/Einheit | Wert oder Regel |
|---|---|---|---|
| Q01 | `campaign_id` | text/uuid | Eindeutige Messaktions-ID; **neues Feld vorgeschlagen**. |
| Q02 | `campaign_start_at`, `campaign_end_at` | UTC | Für die geplante Aktion 14 Tage; konkrete Daten noch nicht festgelegt. |
| Q03 | `max_participants` | integer | Aktuelle Planungsgrenze **20**. |
| Q04 | `daily_save_limit` | integer | Default **10** pro Teilnehmer und Tag; bisheriger Einstellbereich 1–100. |
| Q05 | `quota_timezone` | IANA-Zeitzone | Maßgebliche Tagesgrenze festlegen, z. B. `Europe/Berlin` erst nach Entscheidung. |
| Q06 | `quota_date` | date | Fachlicher Tag nach `quota_timezone`. |
| Q07 | `completed_save_count` | integer | Erfolgreich dauerhaft **lokal** angelegte Pakete; geräteübergreifende Abbildung offen. |
| Q08 | `reserved_save_count` | integer | Atomare Reservierungen gegen parallele Save-Vorgänge. |
| Q09 | `quota_recording_id` | uuid | Einmalige Zuordnung: Retry darf **nicht** erneut zählen. |
| Q10 | `quota_source` | enum | `OBSERVER` oder `ANALYZER`; gemeinsame Quote. |
| Q11 | `quota_state` | enum | `RESERVED`, `COMMITTED`, `RELEASED`. |
| Q12 | `quota_updated_at` | UTC | Letzte Quotenänderung. |

**Zu entscheiden:** lokale und serverseitige Quote bei mehreren Geräten, Rücknahme einer Reservierung bei fehlgeschlagenem lokalen Save, Behandlung der bisherigen eigenständigen Rolling-Speicherung sowie UTC-/Lokalzeit-Tageswechsel. Keine zweite Quote je Panel.

## 4. PLAN – Aufnahme A: 25 Basisparameter (WAV + Herkunft)

Für `audio_recordings` (bestehende Felder weiterverwenden) bzw. ein mit `recording_id` verknüpftes versioniertes Aufnahmeobjekt:

| ID | Feld | Typ/Einheit | Inhalt |
|---|---|---|---|
| A01 | `recording_id` | uuid | Identität, auch für Retries unverändert. |
| A02 | `observer_id` | text | Teilnehmer-FK. |
| A03 | `origin` | enum | `OBSERVER` oder `ANALYZER`. |
| A04 | `observation_event_id` | uuid NULL | Zugehöriges Rating-Ereignis, falls vorhanden. |
| A05 | `recording_started_at` | UTC | Tatsächlicher Audioanfang. |
| A06 | `recording_ended_at` | UTC | Tatsächliches Audioende. |
| A07 | `configured_duration_s` (`save_duration_s`) | number, s | Eingestellte Dauer; Default **120 s**. |
| A08 | `actual_duration_s` | number, s | Aus Samplezählung abgeleitete Dauer. |
| A09 | `audio_storage_path` | text | Privater Storage-Objektpfad; Alias zum bestehenden `storage_path` abbilden. |
| A10 | `wav_sha256` | 64-stelliger Hexstring | Integrität der Original-WAV-Bytes. |
| A11 | `wav_size_bytes` | bigint | Reale Dateigröße. |
| A12 | `wav_sample_rate_hz` | integer | Gespeicherte WAV-Rate, **Default 1000**. |
| A13 | `source_sample_rate_hz` | integer | Tatsächliche Audioeingangsrate. |
| A14 | `analysis_sample_rate_hz` | integer | Effektive Rate der jeweiligen statistischen Berechnung. |
| A15 | `channels` | integer | WAV Default **1**. |
| A16 | `bits_per_sample` | integer | WAV Default **16**. |
| A17 | `audio_source` | enum/text | Tatsächlich verwendete Audioquelle; z. B. MIC/UNPROCESSED soweit verfügbar. |
| A18 | `gain_db` | number, dB | Tatsächlich angewendeter digitaler Gain. |
| A19 | `clipped_samples` | integer | Gezählt im definierten Signalpfad. |
| A20 | `decimation_factor` | number | Verwendete Dezimierung; bei mehreren Stufen zusätzlich Pfadbeschreibung. |
| A21 | `device_model`, `android_version` | text | Gerätekontext; Datenschutz beachten. |
| A22 | `app_version` | text | App-Version bei Aufnahme. |
| A23 | `analysis_schema_version`, `analysis_algorithm_version` | text/integer | Reproduzierbarkeit des Statistikvertrags. |
| A24 | `calibration_profile_id`, `calibration_valid` | text/bool NULL | Gültigkeit der Kalibrierung für den gewählten Bereich. |
| A25 | `analysis_status` | enum | `PENDING`, `COMPLETE`, `PARTIAL`, `INVALID`, `FAILED`; definiertes Gesamtpaket erforderlich. |

**Aufnahmegrenzen:** Dauer und Statistikbereich sind **zwei unterschiedliche Einstellungen**. Beim Analyzer darf SAVE nur erfolgen, wenn mindestens `configured_duration_s` zusammenhängender Audiobuffer verfügbar und Quote frei ist. RUN-SAVE wählt den letzten Abschnitt bis Tastendruck; PAUSE friert **Audio plus Konfiguration** vor späterem SAVE ein. OBSERVER endet automatisch nach eingestellter Dauer. Eine Änderung während des Puffers darf alte/neue Konfiguration nicht heimlich mischen. Zum Speichern gilt 1 kHz als Default; 8 kHz nur mit erlaubendem Key und angepasster Uploadfunktion.

## 5. PLAN – Auswertekonfiguration B und vollständiger Analyzer-Snapshot

### 5.1 Immer vorhandene gemeinsame Statistik-/Filterkonfiguration

| ID | Feld | Einheit/Typ | Bedeutung |
|---|---|---|---|
| B01 | `statistics_min_hz` | Hz | Unabhängiger Gesamtstatistik-Frequenzanfang, Default **2**. |
| B02 | `statistics_max_hz` | Hz | Unabhängiges Frequenzende, Default **25**. |
| B03 | `statistics_epoch_s` | s | Zeitraster für Prozentile; **1 s nur Vorschlag**, versionieren. |
| B04 | `statistics_band_plan_id` | text NULL | Optionaler eigener Statistik-Bandplan, **nicht** FAST-Bank. |
| B05 | `signal_path_id`, `signal_path_version` | text | Audio-/Dezimierungs-/Vorfilterpfad. |
| B06 | `level_reference` | enum | `DBFS` oder nur bei gültiger Kalibrierung `DB_SPL`; Referenzdefinition ergänzen. |
| B07 | `statistics_fft_size` | integer | FFT-Größe für Statistikspektrum. |
| B08 | `statistics_fft_window` | text | Fensterfunktion. |
| B09 | `statistics_fft_hop_samples` | integer | Hop der Auswerte-FFT. |
| B10 | `statistics_psd_normalization` | text | Einheit, Fensterleistung, Ein-/Zweiseitigkeit. |
| B11 | `filter_min_hz`, `filter_max_hz` | Hz | **Unabhängige** Grenzwerte des effektiven Filterbank-Setups. |
| B12 | `filter_bands` | integer | Tatsächlich aktive Bänder, **maximal 40**. |
| B13 | `filter_design_id`, `filter_design_version` | text | Filterimplementierung und Version. |
| B14 | `filter_order` | integer/array | Tatsächliche Ordnung je Band, falls unterschiedlich. |
| B15 | `filter_sample_rate_hz` | integer | Technische Filterbankrate, ggf. **8 kHz**, nicht aus WAV-Rate ableiten. |
| B16 | `filter_update_interval_ms` | ms | Tatsächliche Schrittweite. |
| B17 | `filter_fast_tau_ms` | ms/array | FAST-Integrationszeit je Band. |
| B18 | `filter_slow_tau_ms` | ms/array | SLOW-Referenz je Band. |
| B19 | `filter_warmup_s` | s/array | Einschwingzeit. |
| B20 | `filter_group_delay_ms` | ms/array NULL | Laufzeit, wenn bekannt. |
| B21 | `filter_effective_edges_hz` | Array je Band | Tatsächliche statt nur nominaler Bandkanten. |
| B22 | `filter_coefficients_hash` | text/array NULL | Identifikation der Koeffizienten; **keine** zwingende Speicherung sämtlicher Koeffizienten. |
| B23 | `filter_validated_min_hz`, `filter_validated_max_hz` | Hz NULL | Messtechnisch validierter Filterbereich. |
| B24 | `filter_signal_provenance` | enum/text | 8-kHz-Rohpfad, 1-kHz-WAV-Auswertung oder anderer Pfad. |
| B25 | `configuration_snapshot_at` | UTC | Zeitpunkt des wirksamen Konfigurations-Snapshots. |
| B26 | `configuration_hash` | text | Hash der effektiven, für Berechnung relevanten Konfiguration. |
| B27 | `statistics_algorithm_version`, `impulse_algorithm_version` | text | Tatsächlich benutzte Versionen; nicht durch Spezifikationsversion ersetzen. |
| B28 | `availability` / `provenance` | JSONB | Je Wert `AVAILABLE`, `FIXED`, `UNSUPPORTED`, `MISSING` bzw. tatsächliche Herkunft. |

`statistics_min_hz/max_hz` beschneiden **nie** `filter_min_hz/max_hz` oder Filteranzahl. Filterergebnisse dürfen außerhalb des Gesamtstatistikbereichs liegen. Nicht existierende FAST-Ergebnisse `NOT_AVAILABLE`, keine durch Displaybalken fingierten Zeitreihen.

### 5.2 **Nur bei `origin=ANALYZER`:** 47 bestehende Setup-Schlüssel als separater LIVE-Snapshot

Die nachstehende Schreibweise stammt aus der v0.5.17-Allowlist `AnalyzerInspectionExport.kt`. Deren heutiger JSON-v2-Export ist jedoch **ein gespeicherter Slot**, kein wirksamer LIVE-Snapshot. Für Supabase den tatsächlichen Zustand zum Aufnahmezeitpunkt ermitteln und mit `value`, `availability`, `provenance`, ggf. `effective_value` kennzeichnen. **Nicht den alten Slot blind hochladen.**

| Gruppe | Anzahl | Namen **exakt wie im Setup-Katalog** |
|---|---:|---|
| Spektrum | 15 | `min_frequency`, `range`, `decimation_factor`, `fft_size`, `fft_hop`, `window`, `smoothing_enabled`, `smoothing_time`, `direct_smoothing_alpha`, `direct_time_smoothing`, `direct_peak_markers`, `current`, `average`, `floor`, `max_hold` |
| Audio | 9 | `direct_audio_source`, `direct_sample_rate`, `direct_stay_awake`, `direct_subtract_dc`, `direct_test1_on`, `direct_test1_hz`, `direct_test2_on`, `direct_test2_hz`, `gain` |
| Anzeige | 5 | `display_min`, `display_max`, `history_seconds`, `water_interval`, `water_mode` |
| Statistik (bestehende Anzeige) | 4 | `stat_min`, `stat_max`, `stat_window`, `stat_average` |
| Filterbank | 9 | `filter_min_hz`, `filter_max_hz`, `filter_bands`, `filter_display_min`, `filter_display_max`, `filter_auto_duration`, `filter_auto_margin`, `filter_show_original`, `filter_show_filter` |
| Automatiken | 5 | `autorange_duration`, `autorange_margin`, `auto`, `image_format`, `jpg_quality` |
| **Summe** | **47** | Vollständige bestehende Allowlist; zusätzlich Setup-ID/Slot, Name, Modus `8K`/`DIRECT`, Speicher-/LIVE-Herkunft, Schema-/Algorithmusversion. |

**Nicht verwechseln:** Die bestehenden `stat_min`, `stat_max`, `stat_window`, `stat_average` sind historische Analyzer-Statistikeinstellungen. Neu und unabhängig sind `statistics_min_hz`, `statistics_max_hz`, `save_duration_s` und `statistics_epoch_s`; ihre Bedeutung muss bei Migration/GUI explizit abgebildet werden. Im OBSERVER-JSON das **gesamte** `analyzer_setup`-Objekt **weglassen**.

## 6. PLAN – Gesamtstatistik C (konfigurierbarer Gesamtbereich)

Alle Größen nur im Bereich `statistics_min_hz..statistics_max_hz` für exakt `actual_duration_s`; Pegel aus demselben dokumentierten Signalpfad. Leq **energetisch**, nicht durch Mittelung von dB. Prozentile aus festgelegten Epochen, Impulse aus separatem schnellen Pfad.

| ID | Feld | Einheit | Bedeutung |
|---|---|---|---|
| C01 | `leq_dbfs` | dBFS | Energieäquivalenter Gesamtpegel. |
| C02 | `lmin_dbfs` | dBFS | Kleinstes gültiges Epochenlevel. |
| C03 | `lmax_dbfs` | dBFS | Größtes gültiges Epochenlevel. |
| C04 | `l1_dbfs` | dBFS | Während 1 % der Zeit überschritten. |
| C05 | `l10_dbfs` | dBFS | Während 10 % der Zeit überschritten. |
| C06 | `l50_dbfs` | dBFS | Median-Pegel. |
| C07 | `l90_dbfs` | dBFS | Hintergrundpegel (90 %-Überschreitung). |
| C08 | `l95_dbfs` | dBFS | 95 %-Überschreitung. |
| C09 | `l99_dbfs` | dBFS | 99 %-Überschreitung. |
| C10 | `level_mean_dbfs` | dBFS | Arithmetisches Mittel der Epochenpegel, **nicht** Leq. |
| C11 | `level_stddev_db` | dB | Streuung der Epochenpegel. |
| C12 | `dynamic_range_db` | dB | Lmax − Lmin. |
| C13 | `l10_minus_l90_db` | dB | Schwankungsindikator. |
| C14 | `threshold_dbfs` | dBFS | Für Schwellenauswertung tatsächlich benutzter Pegel. |
| C15 | `time_above_threshold_s` | s | Dauer oberhalb dieser Schwelle. |
| C16 | `fraction_above_threshold` | 0..1 | Zeitanteil oberhalb der Schwelle. |
| C17 | `crest_factor_db` | dB | Peak zu RMS des definierten bandbegrenzten Zeitsignals. |
| C18 | `kurtosis_excess` | dimensionslos | Exzess-Kurtosis der festgelegten Zeitbereichsgröße. |

Falls gültig absolut kalibriert, zusätzlich **nicht als Ersatz** optionale `*_db_spl`-Darstellungen mit eindeutiger Kalibrier-/Frequenzgangversion. Keine pauschale Umbenennung von dBFS zu dB SPL.

## 7. PLAN – Filter- und Statistikbanddaten D: je Band vollständig (max. 40 FAST)

Bandtyp `FAST_FILTER` oder `FIXED_STATISTICS` (optional). FAST-Bänder folgen ausschließlich der **wirksamen** Filterbank inklusive ihrer effektiven Kanten; eigenständige feste Statistikbänder, sofern beschlossen, folgen dem allgemeinen Statistikbereich und dürfen **nicht** automatisch als festes 2–25-Hz-Sechserraster ausgegeben werden. Jedes Band hat `recording_id` + `band_type` + `band_index` als eindeutige Zuordnung.

| ID | Feld | Einheit/Typ | Bedeutung |
|---|---|---|---|
| D01 | `band_id` | uuid oder deterministische ID | Eindeutige Band-Identität. |
| D02 | `band_type`, `band_index` | enum, integer | `FAST_FILTER`/`FIXED_STATISTICS`, Reihenfolge. |
| D03 | `frequency_low_hz` | Hz | Nominale untere Bandgrenze. |
| D04 | `frequency_high_hz` | Hz | Nominale obere Bandgrenze. |
| D05 | `frequency_center_hz` | Hz | Berechnete nominelle Mitte. |
| D06 | `effective_low_hz`, `effective_high_hz` | Hz NULL | Tatsächliche verwertbare Kanten; Methode dokumentieren. |
| D07 | `filter_design_id`, `filter_order`, `filter_coefficients_hash` | text/int | Filterdefinition. |
| D08 | `filter_sample_rate_hz`, `filter_group_delay_ms` | Hz/ms | Herkunft und Verzögerung. |
| D09 | `band_valid`, `invalid_reason` | bool/text NULL | Gültigkeit/Begründung. |
| D10 | `band_leq_dbfs` | dBFS | Bandenergiepegel. |
| D11 | `band_lmin_dbfs` | dBFS | Minimum. |
| D12 | `band_lmax_dbfs` | dBFS | Maximum. |
| D13 | `band_l1_dbfs` | dBFS | 1 %-Überschreitung. |
| D14 | `band_l10_dbfs` | dBFS | 10 %-Überschreitung. |
| D15 | `band_l50_dbfs` | dBFS | Median. |
| D16 | `band_l90_dbfs` | dBFS | Hintergrund. |
| D17 | `band_l95_dbfs` | dBFS | 95 %-Überschreitung. |
| D18 | `band_l99_dbfs` | dBFS | 99 %-Überschreitung. |
| D19 | `band_level_stddev_db` | dB | Streuung. |
| D20 | `band_l10_minus_l90_db` | dB | Pegeldynamik. |
| D21 | `band_threshold_dbfs` | dBFS | Bandspezifische Schwelle. |
| D22 | `band_time_above_threshold_s` | s | Zeit oberhalb Schwelle. |
| D23 | `band_fraction_above_threshold` | 0..1 | Zeitanteil. |
| D24 | `band_fast_peak_dbfs` | dBFS | Höchster echter FAST-Pegel. |
| D25 | `band_slow_reference_dbfs` | dBFS | Dokumentierter SLOW-Referenzwert mit festgelegter Aggregation. |
| D26 | `band_peak_excess_db` | dB | FAST über SLOW. |
| D27 | `band_crest_factor_db` | dB | Bandbegrenzter Zeitbereich; nur falls wirklich vorhanden. |
| D28 | `band_kurtosis_excess` | dimensionslos | Verteilungskennwert; nur mit geeigneter Zeitreihe. |
| D29 | `band_valid_duration_s` | s | Tatsächliche Dauer unter Ausschluss von Warmup/Lücken. |
| D30 | `band_signal_provenance` | text | Tatsächlicher Audio-/Filterpfad, z. B. `RAW_8K`. |

**Wichtig:** Max. **40 FAST-Bänder** ist *keine* globale Grenze für alle Bandzeilen, falls zusätzliche feste Statistikbänder existieren. Zeilen für zeitlich fehlende FAST-Messwerte nicht erfinden. Speicherung tatsächlicher Grenzen/Algorithmusversion hat Vorrang vor Darstellungsfarben und Grafikeinstellungen.

## 8. PLAN – spektrale Statistik E

Spektralarrays als `jsonb` oder versioniertes kompaktes Objekt; Frequenzraster, Einheit und Normierung gemeinsam versionieren; einzelne Bins dürfen die Datenbank nicht unkontrolliert aufblähen.

| ID | Feld | Typ/Einheit | Bedeutung |
|---|---|---|---|
| E01 | `spectrum_frequency_hz` | number[] | Frequenzachse im allgemeinen Statistikbereich. |
| E02 | `spectrum_mean_psd` | number[] | Energetisch gemittelte lineare Leistungsdichte. |
| E03 | `spectrum_median_psd` | number[] | Median je Bin. |
| E04 | `spectrum_max_psd` | number[] | Maximalwert je Bin. |
| E05 | `spectrum_stddev_psd` | number[] | Streuung je Bin. |
| E06 | `spectrum_segment_count` | integer | Anzahl gültiger FFT-Segmente. |
| E07 | `spectrum_fft_size` | integer | Segmentgröße. |
| E08 | `spectrum_window` | text | Fenster (z. B. HANN, RECTANGULAR). |
| E09 | `spectrum_overlap` | 0..1 | Überlappung. |
| E10 | `spectrum_normalization`, `spectrum_unit` | text | PSD-Definition, z. B. dBFS²/Hz vs. lineare PSD; **konkret festzulegen**. |
| E11 | `dominant_frequency_hz` | Hz | Stärkstes lokales spektrales Maximum. |
| E12 | `dominant_frequency_level` | Einheit laut E10 | Pegel/PSD am Maximum. |
| E13 | `dominant_frequency_persistence` | 0..1 | Anteil passender Analyseabschnitte. |
| E14 | `dominant_frequency_drift_hz` | Hz | Zeitliche Frequenzänderung. |
| E15 | `dominant_peaks` | array bis 3 | Je Maximum: `frequency_hz`, `level`, `persistence`, `drift_hz`, optional `quality`. |

## 9. PLAN – schnelle Impulsstatistik F und Ereignisdetails

**Nicht implementiert / nicht validiert in v0.5.17.** Keine Ableitung von Mikropulsen allein aus langsamen Wasserfallzeilen. Filter-Ringing und Ereignisrefraktärzeit müssen im Algo berücksichtigt werden.

| ID | Feld | Einheit | Bedeutung |
|---|---|---|---|
| F01 | `impulse_count` | integer | Bestätigte Ereignisse; `0` nur nach gültiger Auswertung. |
| F02 | `impulse_rate_per_min` | min⁻¹ | Ereignisrate. |
| F03 | `impulse_max_level_dbfs` | dBFS | Stärkstes Ereignis. |
| F04 | `impulse_mean_level_dbfs` | dBFS | Mittel der Ereignispegel; Aggregationsmethode angeben. |
| F05 | `impulse_max_energy` | methodendefinierte Einheit | Maximalenergie. |
| F06 | `impulse_mean_energy` | gleiche Einheit | Mittelenergie. |
| F07 | `impulse_mean_duration_ms` | ms | Mittlere Ereignisdauer. |
| F08 | `impulse_max_duration_ms` | ms | Maximale Dauer. |
| F09 | `impulse_mean_rise_time_ms` | ms | Mittlere Anstiegszeit. |
| F10 | `impulse_mean_decay_time_ms` | ms | Mittlere Abklingzeit. |
| F11 | `impulse_mean_interval_s` | s | Mittlerer Ereignisabstand. |
| F12 | `impulse_interval_stddev_s` | s | Streuung. |
| F13 | `impulse_interval_cv` | dimensionslos | Relative Intervallstreuung. |
| F14 | `impulse_dominant_band_id` | ID NULL | Band größter Ereignisenergie. |
| F15 | `impulse_total_active_time_s` | s | Vereinigung erkannter Ereignisintervalle. |
| F16 | `impulse_clipped_count` | integer | Übersteuerte Ereignisse. |
| F17 | `impulse_unconfirmed_count` | integer | Kandidaten ohne Bestätigung. |
| F18 | `impulse_analysis_valid`, `impulse_algorithm_version` | bool/text | Gültigkeit und Algorithmusstand. |

`audio_impulse_events`: `event_id`, `recording_id`, `event_index`, `start_offset_ms`, `onset_offset_ms`, `trigger_offset_ms`, `end_offset_ms`, `peak_offset_ms`, `peak_level_dbfs`, `event_energy`, `energy_unit`, `duration_ms`, `rise_time_ms`, `decay_time_ms`, `inter_event_interval_ms`, `dominant_band_id`, `band_energies` (Band-ID + Energie), `quality_flags`, `ringing_assessment`, `algorithm_version`; zeitliche Offsets beziehen sich **auf die WAV-/Aufnahmezeitbasis**. Nur tatsächlich berechnete Werte ausgeben.

## 10. PLAN – Modulation und Periodik G

| ID | Feld | Einheit | Bedeutung |
|---|---|---|---|
| G01 | `modulation_frequency_hz` | Hz | Dominante Hüllkurven-Modulationsfrequenz. |
| G02 | `modulation_depth` | normiert/methodendefiniert | Modulationstiefe; Definition dokumentieren. |
| G03 | `modulation_spectrum` | Frequenz-/Wertearrays | Versioniertes Modulationsspektrum. |
| G04 | `autocorrelation_peak_lag_s` | s | Dominanter Wiederholungsabstand. |
| G05 | `period_estimate_s` | s | Geschätzte Wiederholungsperiode. |
| G06 | `period_stability` | normiert/methodendefiniert | Stabilität; Methode dokumentieren. |
| G07 | `level_trend_db_per_min` | dB/min | Pegeltrend im Aufnahmefenster. |
| G08 | `level_first_half_dbfs` | dBFS | Energetischer Pegel der **ersten Hälfte der eingestellten Dauer**. |
| G09 | `level_second_half_dbfs` | dBFS | Zweite Hälfte. |
| G10 | `level_half_difference_db` | dB | Differenz beider Hälften. |

Nicht fest auf zwei Hälften à 60 s kodieren. Für zu kurze Messungen oder zu wenige Modulationsperioden `null` mit Qualitätsgrund.

## 11. PLAN – Datenqualität H / pro Aufnahme und pro Band

| ID | Feld | Einheit/Typ | Bedeutung |
|---|---|---|---|
| H01 | `valid_audio_duration_s` | s | Auswertbare Audiodauer. |
| H02 | `valid_statistics_epochs` | integer | Gültige Epochen. |
| H03 | `missing_audio_frames` | integer | Fehlende Frames/Samples gemäß Methode. |
| H04 | `dropped_analysis_frames` | integer | Auswerte-Dropouts. |
| H05 | `gap_count` | integer | Erkannte Audiolücken. |
| H06 | `clipping_fraction` | 0..1 | Übersteuerungsanteil. |
| H07 | `filter_warmup_complete` | bool | Einschwingphase vollständig? |
| H08 | `calibration_status` | enum | `VALID`, `PARTIAL`, `MISSING`, `INVALID`. |
| H09 | `frequency_response_valid` | bool/NULL | Gültigkeit über den Statistikbereich. |
| H10 | `timestamp_quality` | text/enum | Verlässlichkeit/Zeitbasis. |
| H11 | `analysis_processing_lag_ms` | ms | Definierte Verarbeitungslatenz. |
| H12 | `input_continuity_valid` | bool | Durchgehendes, kompatibles Audiosegment. |
| H13 | `analysis_error_codes` | string[] | Fehler-/Hinweiscodes. |
| H14 | `analysis_completed_at` | UTC | Abschlusszeit. |
| H15 | `overall_validity`, `invalid_reason` | enum/text NULL | Validität der Gesamtstatistik. |
| H16 | `band_validity` | Array/über Bandzeilen | Je Band unabhängig. |
| H17 | `wav_reproducibility` | enum | `FULL`, `PARTIAL`, `NOT_REPRODUCIBLE`; wichtig für 8-kHz-FAST vs. 1-kHz-WAV. |
| H18 | `effective_statistics_min_hz`, `effective_statistics_max_hz` | Hz NULL | Tatsächlich validierter Auswertebereich. |

## 12. PLAN – Upload-Paket, Synchronisation, Serverstatus

| ID | Feld | Typ/Einheit | Zweck |
|---|---|---|---|
| U01 | `recording_id` | uuid | Idempotenzschlüssel über alle Upload-Teile. |
| U02 | `package_schema_version` | text/integer | Paketformat. |
| U03 | `package_created_at` | UTC | Lokales Erzeugen. |
| U04 | `local_package_sha256` | hex | Integrität der lokalen Paketmetadaten. |
| U05 | `upload_state` | enum | `LOCAL_PENDING`, `UPLOADING`, `PARTIAL`, `COMPLETE`, `FAILED_RETRYABLE`, `FAILED_PERMANENT`. |
| U06 | `upload_attempts` | integer | Anzahl Übertragungsversuche. |
| U07 | `last_upload_attempt_at`, `next_retry_at` | UTC NULL | Retryplanung. |
| U08 | `last_error_code`, `last_error_message` | text NULL | Diagnostik; keine Secrets. |
| U09 | `storage_uploaded`, `analysis_uploaded`, `bands_uploaded`, `events_uploaded` | bool | Phasenprotokoll. |
| U10 | `expected_fast_band_count`, `uploaded_fast_band_count` | integer 0..40 | Vollständigkeitsprüfung. |
| U11 | `expected_impulse_event_count`, `uploaded_impulse_event_count` | integer NULL | Ereignisanzahl und Prüfsumme/Status. |
| U12 | `server_received_at`, `server_verified_at` | UTC NULL | Serverseitige Verifikation. |
| U13 | `server_validation_version` | text | Validierungsregeln. |
| U14 | `quota_date`, `quota_committed` | date/bool | Einmalige Kontingentbelastung; Offlinefälle beachten. |
| U15 | `storage_bucket`, `storage_path` | text | Private Storage-Referenz. |
| U16 | `storage_object_sha256`, `storage_object_size_bytes` | hex/bigint | Download- und Restore-Prüfung. |
| U17 | `upload_key_id` | uuid/text NULL | Geprüfte Key-Revision, **kein Token**. |

`COMPLETE` ausschließlich nach überprüfter Zuordnung WAV + verpflichtender Statistik + aller erwarteten Band-/Ereignisdaten. Netzwerkfehler: lokale WAV/Statistik nie löschen. Kritische Vorgabe: kein zweites konkurrierendes `AudioRecord` öffnen; bestehende Direct-/Rolling-Exklusivität respektieren. Ein fehlender optionaler Filterpfad erzeugt korrekten `NOT_AVAILABLE`-Status, nicht eine fehlende „Pflichtzeile“.

## 13. PLAN – Management, Backup und Restore: Parameter und Artefakte

### 13.1 Management-Audit

| ID | Feld | Typ | Inhalt |
|---|---|---|---|
| M01 | `operation_id` | uuid | Eindeutiger Vorgang. |
| M02 | `operation_type` | enum | `CREATE_PARTICIPANT`, `ISSUE_KEY`, `REVOKE_KEY`, `BACKUP`, `RESTORE_FULL`, `RESTORE_LAST_OBSERVATION`, `RESTORE_LAST_AUDIO`, `RESTORE_LAST_BOTH`, `DELETE_ALL_DATA`, `DELETE_PARTICIPANT`. |
| M03 | `target_project_id` | text | Projektkennung, kein Secret. |
| M04 | `target_observer_id` | text NULL | Betroffener Teilnehmer. |
| M05 | `requested_at`, `started_at`, `completed_at` | UTC | Zeitstempel. |
| M06 | `dry_run` | bool | Vorschau statt Schreibaktion. |
| M07 | `confirmation_scope` | text | Bestätigtes Projekt/Teilnehmer, **keinen Geheimtext** mitschreiben. |
| M08 | `affected_table_counts` | JSON | Anzahl je Tabelle. |
| M09 | `affected_storage_count`, `affected_storage_bytes` | bigint | Betroffene WAV-Dateien/-Bytes. |
| M10 | `pre_backup_id`, `pre_backup_verified` | text/bool | Sicherung vor Löschung. |
| M11 | `operation_status` | enum | `PENDING`, `RUNNING`, `COMPLETE`, `PARTIAL`, `FAILED`. |
| M12 | `errors`, `warnings` | JSON | Fehler und Warnungen, ohne Tokens. |
| M13 | `validation_report_path` | text NULL | Prüfbericht. |

### 13.2 Recovery-Manifest (`manifest.json`)

| ID | Feld | Bedeutung |
|---|---|---|
| R01 | `backup_id`, `backup_format_version` | Archividentität/-format. |
| R02 | `created_at`, `source_project_id` | Sicherungszeit/-quelle. |
| R03 | `schema_version`, `app_compatibility` | Kompatibilität. |
| R04 | `table_counts` | Datensätze je Tabelle. |
| R05 | `storage_objects` | Für jedes Objekt: Bucket, Pfad, Bytes, SHA-256, Metadaten. |
| R06 | `database_dump_path`, `database_dump_sha256` | DB-Inhalt. |
| R07 | `storage_bucket_definitions` | Buckets und Zugriffskonfiguration, ohne Secrets. |
| R08 | `migrations`, `edge_function_versions` | Erforderliche reproduzierbare Infrastruktur. |
| R09 | `backup_status`, `errors` | `COMPLETE` oder `INCOMPLETE`; fehlende WAV erzwingt INCOMPLETE. |
| R10 | `backup_validation` | Zählungen, Referenzen und Checksums. |
| R11 | `restore_mode`, `restore_target_id` | Beim Restore separat zu protokollieren. |
| R12 | `restore_selection_time_rule` | UTC-Feld und Tie-Break-Regel für „letzte“. |
| R13 | `restore_key_policy` | Widerrufene/alte Keys **nicht** reaktivieren. |
| R14 | `restore_id_mapping` | Original-IDs oder explizite Zuordnung. |
| R15 | `restore_validation` | DB-/Storage-/End-to-End-Prüfung. |

**Restore-Modi:** `FULL`; `LAST_OBSERVATION` (zeitlich letzte gültige Beobachtung je Teilnehmer); `LAST_AUDIO` (letzte WAV + genau zugehörige Statistik/Bänder/Impulse); `LAST_BOTH` (beides unabhängig voneinander). Bei fehlendem Eintrag nichts erfinden. Ein erfolgreiches Archiv ist erst nach praktischem Restore auf **neues, leeres Supabase-Projekt** als wiederherstellbar nachgewiesen. Gesamtlöschung meint standardmäßig **Nutzdaten und WAV-Objekte**, nicht heimlich das Supabase-Projekt, Schema, Buckets und Funktionen. Teilnehmerentfernung zuerst sperren, dann referenzielle Daten und Storage konsistent entfernen; andere Teilnehmer unangetastet.

## 14. Datenschutz, Validierung und offene Entscheidungen

**Schutz:** Teilnehmernamen/Standorte und Bewertungs-/Audioinhalte sind vertraulich. Pseudonyme IDs verwenden; Klarnamen möglichst nur in geschützter Admin-Verwaltung. Keine Klartextkeys, Service-Role-Keys, Signaturschlüssel oder Access Tokens in Upload-Payload, Log, öffentlichem GitHub oder unverschlüsseltem Recovery-ZIP. Storage privat; API/Auth/RLS und Edge-Function-Berechtigung prüfen. Schon vorhandene ZIPs können private Recovery-Dateien enthalten.

**Ungeklärt, vor Implementierung zu entscheiden:**

1. Minimal-/Maximaldauer und Schrittweite von `save_duration_s`; aktueller `observations.wav_duration_sec`-CHECK bis 600 s und derzeitige 1-MiB-Uploadgrenze ggf. anpassen.
2. Ein mess- und numerisch sinnvoller Bereich für frei einstellbare `statistics_min_hz/max_hz`; validierte Mikrofon-/Filtergrenzen und DC/Nyquist berücksichtigen.
3. FFT-/PSD-Normierung, Epochendauer, Schwellenmethodik, Bandplan zusätzlicher fester Statistikbänder; null-/Statusschema je Metrik.
4. Ob für 8-kHz-FAST-Statistik zeitaufgelöste Merkmalsfolgen oder eine optionale Roh-WAV benötigt werden; 1-kHz-WAV alleine reproduziert nicht jeden Rohpfadbefund.
5. Tagesquoten bei mehreren Geräten/Offlinebetrieb und lokale vs. serverseitige Commitment-Semantik; Policy für separat vorhandene Rolling-Saves.
6. Key-Tokenformat, Signatur-/Schlüsselrotation, serverseitiges Vorzeit-Widerrufsmodell, Erneuerung und Verhalten bei Restore.
7. Konkrete SQL-Migrationen, RLS/Storage-Berechtigungen, Uploadtransport, Payload-Limits, Retry-/Idempotenz und Datenbankindizes.
8. Konkrete Lösch-/Aufbewahrungspolitik für Teilnehmer, historische Backups und Audit; sichere Verschlüsselung der Recovery-Archive.
9. Name/Format des geplanten `analyzer_setup` LIVE-Snapshots; Umstellung keinesfalls mit altem Slot-Export verwechseln.

## 15. Kapazitätsrahmen der geplanten Aktion (keine Tarifzusage)

**20 Teilnehmer × 14 Tage × 10 Saves/Tag = höchstens 2.800 Aufnahmen**, sofern alle die Quote vollständig nutzen. Bei Default 120 s, WAV 1.000 Hz, 16 Bit, Mono: ca. **672 MB WAV** zuzüglich Header/Objektmetadaten; Statistikdaten, Indizes, bisheriger DB-/Storage-Bestand und Backups zusätzlich. Bei 8-kHz-WAV wäre der WAV-Anteil ca. **5,376 GB** bei gleicher Dauer. Verdoppelung der Aufnahmedauer verdoppelt näherungsweise WAV-Bedarf. Tarif-/Quota-Grenzen vor Start separat aktuell prüfen.

## 16. Verbindliche Abnahme-Matrix und Umsetzungsstatus

| Prüffall | Erwartung | Status bei Dokumenterstellung |
|---|---|---|
| Bestehende Observer-/Observation-/Audio-Schemafelder | Tatsächliche SQL-Felder oben inventarisiert | **Quellstand geprüft**, kein Live-Supabase-Abgleich |
| Observer-Save und Analyzer RUN-/PAUSE-Save | Gemeinsames Paket, identische Audio-/Statistikzeitbasis | PLAN / NOT RUN |
| Default 1 kHz + ausdrücklich berechtigtes 8 kHz | Key-Recht, tatsächliche Rate, Serverprüfung | 1 kHz bisher vorhanden; Key-Erweiterung PLAN |
| Dauer-/Gesamtfrequenzänderung | Ohne Beeinflussung unabhängiger FILTER-Grenzen | PLAN / NOT RUN |
| FAST 10, 20, **40** Bänder | Jede tatsächliche Bandzeile gespeichert, keine Abschneidung | PLAN / NOT RUN |
| Fehlender FAST-Rohpfad | `NOT_AVAILABLE`, kein erdachter Wert | PLAN / NOT RUN |
| Supabase-Upload/Retries | Vollständigkeit, idempotent, Offlinepaket erhalten | PLAN / NOT RUN |
| Key-Ablauf/Neuausgabe | Nur neuester gültig, Formatberechtigung geprüft | PLAN / NOT RUN |
| Backup/Restore vollständig und reduziert | DB + WAV + Statistiken + Referenzen + Key-Status | PLAN / NOT RUN |
| Teilnehmer- und Gesamtlöschung | Dry Run, vorherige Sicherung, saubere Nachkontrolle | PLAN / NOT RUN |

**Abschluss:** Dieses Dokument ist der **kumulative Parameterkatalog**, **keine** neue Android-App-Version, **kein** SQL-Migrationsskript und **keine** Bestätigung eines erfolgreichen Supabase-End-to-End-Tests. Beim nächsten kontrollierten Projektstand unter `docs/project/` übernehmen und `DIRECTIVES.md`, `PROJECT_STATUS.md`, `CHANGELOG.md` fortführen. Vorher den bereits gesicherten v0.5.17-Stand nicht überschreiben.
