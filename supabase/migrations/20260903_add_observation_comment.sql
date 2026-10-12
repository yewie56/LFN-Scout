alter table public.observations
    add column if not exists comment text;

comment on column public.observations.comment is
    'Optional free-text comment entered by the observer for ratings 0..5; null for no comment and automatic rating -1.';
