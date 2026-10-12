# v0.4.54 Audio Regression Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Release v0.4.54 with the confirmed rolling-capture cancellation bug fixed and local audio filenames using the current rating state at actual recording start.

**Architecture:** Keep the existing single-stream rolling architecture. Make `RollingAudioEngine.captureNext()` cancellation-safe with unconditional cleanup in `finally`, and prevent stale Compose closure state from choosing the local rating tag by reading the latest `recentItems` state at recording start. No Supabase function, schema, migration, or storage changes.

**Tech Stack:** Kotlin 2.3.0, Jetpack Compose, kotlinx.coroutines, JUnit 4, Android Gradle Plugin 8.13.2, Gradle 8.13.

**Spec:** `docs/superpowers/specs/2026-09-11-rolling-audio-buffer-design.md`

## Global Constraints

- Preserve one AudioRecord stream while Rolling is active.
- Rolling snapshots remain non-destructive.
- Rolling audio remains local-only.
- Keep `wav_present` / `wav_path` backend work deferred; no backend changes in this release.
- Keep Java 17 project-local policy and do not change global Java settings.
- Preserve existing UI layout and behavior except bug fixes.

---

### Task 1: Cancellation-safe rolling future capture

**Files:**
- Modify: `app/src/main/java/de/brummbeobachter/app/RollingAudioEngine.kt`
- Create: `app/src/test/java/de/brummbeobachter/app/RollingAudioEngineCancellationTest.kt`

**Interfaces:**
- Consumes: `RollingAudioEngine.captureNext(seconds, ...)`
- Produces: same public signature; cancelled capture always releases the single capture slot.

- [ ] **Step 1: Write failing regression test**
- [ ] **Step 2: Run test and verify it fails because the second capture returns null after cancellation**
- [ ] **Step 3: Add minimal try/finally cleanup around capture lifetime**
- [ ] **Step 4: Run focused test and full JVM tests**

### Task 2: Use current rating state for local WAV naming

**Files:**
- Modify: `app/src/main/java/de/brummbeobachter/app/MainActivity.kt`
- Create: `app/src/test/java/de/brummbeobachter/app/AudioCurrentRatingNamingContractTest.kt`

**Interfaces:**
- Consumes: Compose `recentItems` state and `AudioFileNamingPolicy.buildLocalWavFileName(...)`
- Produces: local filename rating is resolved from the latest observation list at actual recording start, not the stale list captured when the user pressed the audio button before the 15-second countdown.

- [ ] **Step 1: Add failing contract regression test for latest-state access**
- [ ] **Step 2: Verify RED**
- [ ] **Step 3: Use `rememberUpdatedState(recentItems)` and snapshot latest observations immediately after countdown when `recordedAt` is created**
- [ ] **Step 4: Use that snapshot for local filename generation**
- [ ] **Step 5: Run focused and full JVM tests**

### Task 3: Version, changelog and verification artifact

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `CHANGELOG.md`
- Modify: `README.md`
- Create: `verify_v0454_requirements.py`
- Create: `BUILD_VERIFICATION_v0.4.54.txt`

**Interfaces:**
- Produces: versionCode 94, versionName 0.4.54, static verification of the two bug fixes.

- [ ] **Step 1: Bump version to 0.4.54 / 94**
- [ ] **Step 2: Document both fixes and explicitly state no Supabase changes**
- [ ] **Step 3: Add static verification script**
- [ ] **Step 4: Run verification script and tests**
- [ ] **Step 5: Package complete Android Studio project as ZIP**
