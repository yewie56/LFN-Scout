-- LFN-Scout v0.4.28
-- Key lifecycle + independent WAV recordings.

alter table public.observers
    add column if not exists status text not null default 'active',
    add column if not exists revoked_at timestamptz,
    add column if not exists paused_at timestamptz;

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'observers_status_check'
    ) then
        alter table public.observers
            add constraint observers_status_check
            check (status in ('active', 'paused', 'revoked'));
    end if;
end $$;

create table if not exists public.audio_recordings (
    recording_id uuid primary key,
    observer_id text not null references public.observers(observer_id),
    recorded_at timestamptz not null,
    sample_rate_hz integer not null,
    channels integer not null,
    bits_per_sample integer not null,
    gain_db integer not null,
    clipped_samples integer not null default 0,
    storage_path text not null,
    created_at timestamptz not null default now()
);

insert into storage.buckets (id, name, public)
values ('brumm-audio', 'brumm-audio', false)
on conflict (id) do update set public = false;
