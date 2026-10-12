-- LFN-Scout v0.5.26 – additive Supabase statistics extension
-- Date: 2026-10-08
-- IMPORTANT: migration is deliberately additive and keeps all legacy tables/columns.
-- Deploy BEFORE the new/updated Edge Functions.

begin;

-- ============================================================
-- 1. Existing audio_recordings: add event context only.
-- ============================================================
alter table public.audio_recordings
    add column if not exists latitude double precision,
    add column if not exists longitude double precision,
    add column if not exists location_accuracy_m double precision,
    add column if not exists location_timestamp timestamptz,
    add column if not exists location_source text,
    add column if not exists location_status text not null default 'LEGACY_UNKNOWN',
    add column if not exists capture_origin text not null default 'LEGACY_UNKNOWN',
    add column if not exists trigger_type text not null default 'LEGACY_UNKNOWN',
    add column if not exists trigger_metadata jsonb not null default '{}'::jsonb;

do $$
begin
    if not exists (select 1 from pg_constraint where conname='audio_recordings_latitude_chk') then
        alter table public.audio_recordings add constraint audio_recordings_latitude_chk
            check (latitude is null or latitude between -90 and 90);
    end if;
    if not exists (select 1 from pg_constraint where conname='audio_recordings_longitude_chk') then
        alter table public.audio_recordings add constraint audio_recordings_longitude_chk
            check (longitude is null or longitude between -180 and 180);
    end if;
    if not exists (select 1 from pg_constraint where conname='audio_recordings_location_accuracy_chk') then
        alter table public.audio_recordings add constraint audio_recordings_location_accuracy_chk
            check (location_accuracy_m is null or location_accuracy_m >= 0);
    end if;
    if not exists (select 1 from pg_constraint where conname='audio_recordings_location_status_chk') then
        alter table public.audio_recordings add constraint audio_recordings_location_status_chk
            check (location_status in ('VALID_LIVE','FALLBACK_OBSERVER_BASE','UNAVAILABLE','LEGACY_UNKNOWN'));
    end if;
    if not exists (select 1 from pg_constraint where conname='audio_recordings_capture_origin_chk') then
        alter table public.audio_recordings add constraint audio_recordings_capture_origin_chk
            check (capture_origin in ('OBSERVER','ANALYZER','LEGACY_UNKNOWN'));
    end if;
    if not exists (select 1 from pg_constraint where conname='audio_recordings_trigger_type_chk') then
        alter table public.audio_recordings add constraint audio_recordings_trigger_type_chk
            check (trigger_type in ('MANUAL','ROLLING_LEVEL','ANALYZER_LEVEL','HARDWARE_EXTERNAL','LEGACY_UNKNOWN'));
    end if;
end $$;

-- ============================================================
-- 2. Flexible extra-parameter lookup.
-- Existing named metrics stay named; P01..P32 are reserve only.
-- ============================================================
create table if not exists public.parameter_lookup (
    lookup_version integer not null,
    set_kind text not null,
    slot_no integer not null check (slot_no between 1 and 32),
    slot_code text not null,
    semantic_key text,
    display_label text,
    unit text,
    value_type text not null default 'double',
    active boolean not null default false,
    description text,
    introduced_in text,
    notes text,
    primary key (lookup_version, set_kind, slot_no),
    unique (lookup_version, set_kind, slot_code)
);

-- ============================================================
-- 3. Statistics belonging to a stored audio recording.
-- One overall row + 0..40 filter rows per recording/version.
-- Core metrics keep explicit stable names.
-- ============================================================
create table if not exists public.measurement_parameter_sets (
    id uuid primary key default gen_random_uuid(),
    observer_id text not null references public.observers(observer_id),
    recording_id uuid not null references public.audio_recordings(recording_id) on delete cascade,
    captured_at timestamptz not null,

    latitude double precision,
    longitude double precision,
    location_accuracy_m double precision,
    location_timestamp timestamptz,
    location_source text,
    location_status text not null,
    comment text,

    capture_origin text not null,
    trigger_type text not null,

    set_kind text not null,
    set_index integer not null default 0,
    lookup_version integer not null default 1,
    duration_s double precision,
    range_min_hz double precision,
    range_max_hz double precision,

    leq_dbfs double precision,
    lmin_dbfs double precision,
    lmax_dbfs double precision,
    l1_dbfs double precision,
    l10_dbfs double precision,
    l50_dbfs double precision,
    l90_dbfs double precision,
    l95_dbfs double precision,
    l99_dbfs double precision,
    mean_level_dbfs double precision,
    level_stddev_db double precision,
    crest_factor_db double precision,
    kurtosis_excess double precision,
    valid_epochs integer,
    dominant_frequency_hz double precision,

    p01 double precision, p02 double precision, p03 double precision, p04 double precision,
    p05 double precision, p06 double precision, p07 double precision, p08 double precision,
    p09 double precision, p10 double precision, p11 double precision, p12 double precision,
    p13 double precision, p14 double precision, p15 double precision, p16 double precision,
    p17 double precision, p18 double precision, p19 double precision, p20 double precision,
    p21 double precision, p22 double precision, p23 double precision, p24 double precision,
    p25 double precision, p26 double precision, p27 double precision, p28 double precision,
    p29 double precision, p30 double precision, p31 double precision, p32 double precision,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),

    constraint mps_comment_chk check (comment is null or char_length(comment)<=2000),
    constraint mps_latitude_chk check (latitude is null or latitude between -90 and 90),
    constraint mps_longitude_chk check (longitude is null or longitude between -180 and 180),
    constraint mps_accuracy_chk check (location_accuracy_m is null or location_accuracy_m>=0),
    constraint mps_location_pair_chk check ((latitude is null)=(longitude is null)),
    constraint mps_location_status_chk check (location_status in ('VALID_LIVE','FALLBACK_OBSERVER_BASE','UNAVAILABLE','LEGACY_UNKNOWN')),
    constraint mps_location_validity_chk check (location_status in ('UNAVAILABLE','LEGACY_UNKNOWN') or (latitude is not null and longitude is not null)),
    constraint mps_origin_chk check (capture_origin in ('OBSERVER','ANALYZER','LEGACY_UNKNOWN')),
    constraint mps_trigger_chk check (trigger_type in ('MANUAL','ROLLING_LEVEL','ANALYZER_LEVEL','HARDWARE_EXTERNAL','LEGACY_UNKNOWN')),
    constraint mps_kind_chk check (set_kind in ('overall_statistics','filter_statistics')),
    constraint mps_filter_index_chk check ((set_kind='overall_statistics' and set_index=0) or (set_kind='filter_statistics' and set_index between 1 and 40)),
    constraint mps_duration_chk check (duration_s is null or duration_s>=0),
    constraint mps_range_chk check (range_min_hz is null or range_max_hz is null or range_max_hz>range_min_hz),
    constraint mps_valid_epochs_chk check (valid_epochs is null or valid_epochs>=0),
    unique (recording_id,set_kind,set_index,lookup_version)
);

create index if not exists idx_mps_observer_time on public.measurement_parameter_sets(observer_id,captured_at);
create index if not exists idx_mps_recording on public.measurement_parameter_sets(recording_id);

-- ============================================================
-- 4. Independent 30-minute statistics time series.
-- Not tied to a stored WAV. Fixed core fields remain explicit.
-- ============================================================
create table if not exists public.half_hour_statistics (
    bucket_id uuid primary key,
    observer_id text not null references public.observers(observer_id),
    bucket_start timestamptz not null,
    bucket_end timestamptz not null,
    statistics_source text not null default 'ROLLING',

    latitude double precision,
    longitude double precision,
    location_accuracy_m double precision,
    location_timestamp timestamptz,
    location_source text,
    location_status text not null,
    comment text,
    app_version text,

    range_min_hz double precision not null,
    range_max_hz double precision not null,
    leq_dbfs double precision,
    l50_dbfs double precision,
    l90_dbfs double precision,
    coverage_seconds integer not null default 0,
    valid_epoch_count integer not null default 0,
    quality_status text not null,

    lookup_version integer not null default 1,
    p01 double precision, p02 double precision, p03 double precision, p04 double precision,
    p05 double precision, p06 double precision, p07 double precision, p08 double precision,
    p09 double precision, p10 double precision, p11 double precision, p12 double precision,
    p13 double precision, p14 double precision, p15 double precision, p16 double precision,
    p17 double precision, p18 double precision, p19 double precision, p20 double precision,
    p21 double precision, p22 double precision, p23 double precision, p24 double precision,
    p25 double precision, p26 double precision, p27 double precision, p28 double precision,
    p29 double precision, p30 double precision, p31 double precision, p32 double precision,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),

    constraint hhs_bucket_duration_chk check (bucket_end=bucket_start+interval '30 minutes'),
    constraint hhs_bucket_alignment_chk check (
        extract(minute from bucket_start at time zone 'UTC') in (0,30)
        and extract(second from bucket_start at time zone 'UTC')=0
    ),
    constraint hhs_source_chk check (statistics_source in ('ROLLING','ANALYZER','OBSERVER','OTHER','LEGACY_UNKNOWN')),
    constraint hhs_latitude_chk check (latitude is null or latitude between -90 and 90),
    constraint hhs_longitude_chk check (longitude is null or longitude between -180 and 180),
    constraint hhs_accuracy_chk check (location_accuracy_m is null or location_accuracy_m>=0),
    constraint hhs_location_pair_chk check ((latitude is null)=(longitude is null)),
    constraint hhs_location_status_chk check (location_status in ('VALID_LIVE','FALLBACK_OBSERVER_BASE','UNAVAILABLE','LEGACY_UNKNOWN')),
    constraint hhs_location_validity_chk check (location_status in ('UNAVAILABLE','LEGACY_UNKNOWN') or (latitude is not null and longitude is not null)),
    constraint hhs_comment_chk check (comment is null or char_length(comment)<=2000),
    constraint hhs_range_chk check (range_max_hz>range_min_hz),
    constraint hhs_coverage_chk check (coverage_seconds between 0 and 1800),
    constraint hhs_epoch_chk check (valid_epoch_count between 0 and 1800),
    constraint hhs_quality_chk check (quality_status in ('COMPLETE','PARTIAL','NO_DATA','INVALID')),
    unique (observer_id,bucket_start,statistics_source,range_min_hz,range_max_hz)
);

create index if not exists idx_hhs_observer_time on public.half_hour_statistics(observer_id,bucket_start);
create index if not exists idx_hhs_time on public.half_hour_statistics(bucket_start);

alter table public.parameter_lookup enable row level security;
alter table public.measurement_parameter_sets enable row level security;
alter table public.half_hour_statistics enable row level security;

-- Current architecture uses service-role Edge Functions. Deliberately no anon/authenticated policies.

commit;
