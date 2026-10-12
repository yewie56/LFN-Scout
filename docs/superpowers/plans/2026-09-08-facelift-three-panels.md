# LFN-Scout Three-Panel Facelift Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Die bestehende v0.4.25-Oberfläche in Hauptpanel, Einstellungen und Zusatzfunktionen gliedern und die Radius-Histogramme als kompaktes 2×2-Raster darstellen.

**Architecture:** `MainActivity.kt` behält die bestehende Geschäftslogik. Ein kleiner `AppPanel`-Navigationszustand entscheidet, welches Panel gerendert wird; die UI wird in fokussierte Composables getrennt. Bestehende Submit-, Reminder-, Export- und Debug-Funktionen werden nur neu verdrahtet.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android 12+, bestehende DataStore-/Supabase-Implementierung.

**Spec:** `docs/superpowers/specs/2026-09-08-facelift-three-panels-design.md`

## Global Constraints

- Version bleibt `0.4.25`.
- In-Process-Tonpakete aus v0.4.25 bleiben unverändert.
- v0.4.24 und die ursprüngliche v0.4.25-ZIP werden nicht überschrieben.
- Kein expliziter Import `androidx.compose.foundation.layout.weight`.
- Keine neue externe UI-Abhängigkeit.
- Android-WAV-Aufnahme wird nicht als funktionsfähig dargestellt, solange sie nicht implementiert ist.

---

### Task 1: Panelmodell und Layoutregeln

**Files:**
- Create: `app/src/main/java/de/brummbeobachter/app/AppPanel.kt`
- Test: `app/src/test/java/de/brummbeobachter/app/AppPanelTest.kt`

- [ ] RED: Test für drei Panels und 2×2-Histogrammraster schreiben und fehlschlagen lassen.
- [ ] GREEN: `AppPanel` und reine Layoutkonstanten minimal implementieren.
- [ ] Tests erneut ausführen.

### Task 2: Hauptpanel

**Files:**
- Modify: `app/src/main/java/de/brummbeobachter/app/MainActivity.kt`

- [ ] Kopfzeile mit `⚙` und `⋮`.
- [ ] Reminderstatus als kompakte Karte.
- [ ] Pausensteuerung ins Hauptpanel verschieben.
- [ ] Bewertung/Kommentar/Status/letzte Bewertungen beibehalten.
- [ ] Audio-Karte sichtbar einbauen und nicht implementierte Aufnahme eindeutig kennzeichnen.
- [ ] Radius-Histogramme in 2×2-Raster umstellen.

### Task 3: Einstellungen

**Files:**
- Modify: `app/src/main/java/de/brummbeobachter/app/MainActivity.kt`

- [ ] Bestehende Reminder-/Sound-/Berechtigungs-/Radius-Einstellungen in Karten gruppieren.
- [ ] Teilnehmerschlüssel aus Hauptpanel hierher verschieben.
- [ ] Pausensteuerung aus Einstellungen entfernen.

### Task 4: Zusatzfunktionen

**Files:**
- Modify: `app/src/main/java/de/brummbeobachter/app/MainActivity.kt`

- [ ] CSV/XLSX-Export aus Hauptpanel verschieben.
- [ ] Debug-Protokoll hier erreichbar machen.
- [ ] Systemstatus und spätere Rohdaten/WAV-Funktionen klar getrennt anzeigen.

### Task 5: Regression und Paket

**Files:**
- Modify: `CHANGELOG.md`
- Create: `SELFTEST_v0.4.25_FACELIFT.txt`

- [ ] Statische Regressionen prüfen.
- [ ] Unit-Test/Gradle-Build versuchen.
- [ ] ZIP-Integrität prüfen.
- [ ] Separate Facelift-ZIP erzeugen.
