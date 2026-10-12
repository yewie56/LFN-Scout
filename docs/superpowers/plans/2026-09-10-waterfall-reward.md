# Waterfall Reward Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Generate and display an Inferno waterfall from the newest local WAV and optionally save it as PNG.

**Architecture:** Add a focused `WaterfallGenerator.kt` for WAV parsing, STFT, adaptive level scaling and Bitmap generation. MainActivity only coordinates selection, UI dialog, save action, and logging.

**Tech Stack:** Kotlin, Jetpack Compose, Android Bitmap/Canvas, MediaStore.

**Spec:** `docs/superpowers/specs/2026-09-10-waterfall-reward-design.md`

## Global Constraints
- Display frequency: 0..100 Hz linear.
- Auto-level statistics: 2..100 Hz.
- Level rule: P5-3 dB / P99.5+3 dB, minimum span 30 dB, clamp -120..0 dBFS.
- Colormap: Inferno.
- Time increases downward.
- Source: newest local WAV from AudioArchive.
- No new external dependency.

---

### Task 1: DSP and rendering
- [ ] Write failing static/behavior tests for WAV parsing, 0..100 Hz mapping, adaptive scale constants, and Inferno renderer.
- [ ] Implement `WaterfallGenerator.kt`.
- [ ] Verify tests.

### Task 2: UI
- [ ] Write failing checks for the new button and result dialog.
- [ ] Add generation state, preview dialog, error handling and save action.
- [ ] Verify static checks.

### Task 3: PNG save and logging
- [ ] Add MediaStore PNG save to `Download/LFN-Scout/`.
- [ ] Add detailed log events.
- [ ] Verify no changes to audio upload/storage behavior.

### Task 4: Version/docs/package
- [ ] Bump app to 0.4.46 / code 86.
- [ ] Update README and CHANGELOG.
- [ ] Run regression checks and package ZIP.
