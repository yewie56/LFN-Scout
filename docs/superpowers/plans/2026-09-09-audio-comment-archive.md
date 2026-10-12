# Audio Comment + Local Archive Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add post-recording comments with 10 s/60 s automatic send behavior, persist WAVs locally, and delete only successfully uploaded recordings after 24 h.

**Architecture:** Introduce a focused `AudioArchive` file for local WAV metadata and retention policy. Keep upload transport in `SupabaseClient`, while `MainActivity` coordinates dialog state and retry UI.

**Tech Stack:** Kotlin, Jetpack Compose, org.json, Supabase Edge Functions (Deno/TypeScript), PostgreSQL migration.

**Spec:** `docs/superpowers/specs/2026-09-09-audio-comment-archive-design.md`

## Global Constraints
- Android minSdk 23, target/compile 36.
- Keep existing audio format and WAV naming behavior.
- No service-role key in Android.
- Comment max 2000 characters.
- Delete local WAV only if upload succeeded and successful-upload age is at least 24 h.

---

### Task 1: Local audio archive
- [ ] Write failing policy tests for retention and metadata state.
- [ ] Implement `AudioArchive.kt`.
- [ ] Verify tests/static checks.

### Task 2: Upload comment backend
- [ ] Write failing checks for request/Edge metadata comment.
- [ ] Add SQL migration and `upload_wav` comment validation/storage.
- [ ] Verify existing WAV naming tests remain green.

### Task 3: Post-recording comment dialog
- [ ] Write failing checks for 10 s/60 s policy and no immediate upload.
- [ ] Add dialog state, visible countdown and reset-on-input.
- [ ] Persist comment as it is typed and upload on timeout/manual action.

### Task 4: 24h local archive UI and retry
- [ ] Write failing checks for path display and retry action.
- [ ] Add Audio-Aufzeichnungen section in Zusatzfunktionen.
- [ ] Add path copy, refresh and retry.

### Task 5: Version/docs/regression
- [ ] Bump to 0.4.44 / code 84.
- [ ] Update README, CHANGELOG and Supabase setup note.
- [ ] Run regression checks and package ZIP.
