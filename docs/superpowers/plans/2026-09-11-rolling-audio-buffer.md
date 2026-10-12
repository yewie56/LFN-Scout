# Rolling Audio Buffer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an optional 30-minute RAM rolling audio buffer that runs as a microphone foreground service, can be saved locally with comment/JSON, never uploads to Supabase, and shares its stream with 120-second recordings.

**Architecture:** `RollingAudioService` owns one continuous AudioRecord stream through `RollingAudioEngine`. The engine decimates/gains into a 1 kHz PCM16 ring and supports snapshot and future-capture consumers. Saved rolling files are managed by `AudioArchive`, tagged as rolling, accompanied by a same-stem visible JSON file, and shown in the existing local audio list.

**Tech Stack:** Kotlin, Android AudioRecord, foreground service, Jetpack Compose, DataStore, MediaStore, existing WaterfallGenerator.

**Spec:** `docs/superpowers/specs/2026-09-11-rolling-audio-buffer-design.md`

## Global Constraints
- Rolling capacity: 30 minutes at 1000 Hz mono PCM16 (~3.6 MB).
- Reuse current source/input/gain settings; no separate rolling audio parameters.
- Rolling files are local-only and never enter Supabase upload/retry/24h cleanup.
- Saved rolling files persist until explicit long-press deletion with confirmation.
- Normal 120-second capture shares the running rolling stream when available.
- Existing waterfall, zoom/pan, PNG/JPG selection remain usable.

---

### Task 1: Persistence and policy
Create rolling policy and DataStore flag; RED/GREEN static tests.

### Task 2: Shared rolling audio engine and foreground service
Create engine/service/boot receiver, manifest declarations, notification/restart handling; verify source structure and compilation if available.

### Task 3: Archive format and local JSON companion
Extend archive metadata for rolling files, visible same-stem JSON create/update/delete, exempt rolling entries from cleanup/upload.

### Task 4: Share stream with normal 120-second capture
Use rolling engine for future 120-second capture while active; retain existing standalone recorder as fallback.

### Task 5: Settings and MORE UI
Add persistent settings switch, service synchronization, save button, comment dialog, combined list marker, long-press delete confirmation.

### Task 6: Waterfall and regression verification
Ensure rolling entries use existing waterfall path, long files work with current image dimensions, verify prior v0.4.51 requirements, update version/changelog/build verification, package ZIP.
