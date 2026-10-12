alter table public.audio_recordings
    add column if not exists comment text;

alter table public.audio_recordings
    drop constraint if exists audio_recordings_comment_length_check;

alter table public.audio_recordings
    add constraint audio_recordings_comment_length_check
    check (comment is null or char_length(comment) <= 2000);
