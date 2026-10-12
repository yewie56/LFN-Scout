-- LFN-Scout v0.5.26
-- Fix missing UUID default for half_hour_statistics.bucket_id
-- Date: 2026-10-10
-- Additive correction migration; no existing rows are modified.

begin;

alter table public.half_hour_statistics
    alter column bucket_id set default gen_random_uuid();

commit;
