# LFN-Scout – CURRENT_STATE (verbindliche laufende Übergabe)

**Stand:** 2026-10-14 · **Quellkandidat:** v0.5.56, Phase 3O, Teilstufe 7 · **Noch kein Release**

## Buildstatus und Versionsschutz

- Gradle `versionName = "0.5.55"`, `versionCode = 213`: bis Releasefreigabe unverändert.
- **Letzter vom Nutzer als GRÜN bestätigter GitHub-TEST:** Phase 3O, **Teilstufe 5** (Commit-ID nicht bekanntgegeben). Für Teilstufe 6 liegt hier keine Buildbestätigung vor.
- **Teilstufe 7:** statische Quellprüfungen lokal ausführbar; GitHub TEST, Kotlin-/Gradle-Kompilation und Android-Gerätetest **noch offen**.
- TEST genügt vorerst; REGULAR nur bei ausdrücklichem Bedarf, besonders für Updates der bestehenden App.

## Aktuell implementiert

- Phasen 3A–3N siehe `docs/project/PHASE3*.md`; Phase 3N ist ausdrücklich lesender Umschaltplan mit `Plan.executable == false`.
- Phase 3O/1–6: Preflight-/Präzisionsschutz, speicherfreie STOP-gebundene DSP-Setter, DSP-Start-Override und gestoppter DSP-Checkpoint (siehe `PHASE3O_TRANSAKTIONSSCHUTZ_v0.5.56.md`).
- **Neu 3O/7:** `StoppedAudioTimingCheckpoint` im `LiveAnalyzerController` erfasst die zusätzlichen internen Direct-Mode- und Timing-Felder ausschließlich bei STOP. `isStoppedAudioTimingCheckpointCurrent` erkennt zwischenzeitliche Änderungen unter demselben Controller-Lock; es gibt keinerlei Zustandsänderungen oder Preference-Zugriffe.

## Was ausdrücklich NICHT funktioniert / gesperrt bleibt

- Keine echte JSON-Audio-/FFT-Setupaktivierung, kein STOP/START-Executor, keine Aufnahme-Koordination, kein Audio-/Recorder-Rollback, keine automatische Umschaltung des 8-/48-kHz-Modus.
- STOP-Checkpoints sind **keine** vollständige Recorder-/Service-/Audio-Hardware-Sicherung und persistieren nicht über Prozessneustarts.
- Kein GitHub-Erfolg für den neuen Kandidaten behauptet; statische Python-Tests beweisen keine Kotlin-Kompilation.
- Nicht unterstützte, fehlende oder null-Werte nicht stillschweigend ersetzen.

## Verbindliche Regeln

- `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `PROJECT_STATUS.md`, `docs/project/DEFERRED_TASKS.md` vorher lesen und fortführen.
- Termux-Skript **v4 unverändert**; Workflows, Signatur, `gradle.properties`, `.gitignore`, Cloud/Backend/Recorder nicht unbeabsichtigt verändern.
- JSON nur explizit speichern; bei Dirty-Wechsel Speichern/Verwerfen/Abbrechen; während Aufnahme niemals umschalten.
- Jede komplette Quell-ZIP enthält diese Datei und alle Projektquellen direkt in der ZIP-Wurzel, mit Regressionstests.
- Zurückgestelltes Vereinfachungspaket: GitHub-Projektgedächtnis, größere Entwicklungspakete, Autopilot, automatisierte Fehlerabfrage, ChatGPT-Projekt. **Der bisherige manuelle Entwicklungsablauf gilt weiterhin.**

## Nächster Schritt

Erst vollständige Validierung von Audio-/Timing-Checkpoint + mode-/recorderübergreifender atomarer STOP-/START-/Rollback-Vertrag und dynamische Regressionen. Keine vorzeitige Freigabe; Gerätetests ausdrücklich nachholen.

## Übergabe in neuem Chat

Aktuelle vollständige Quell-ZIP verwenden, danach `CURRENT_STATE.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `DEFERRED_TASKS.md` sowie Phasendokumentation lesen. Immer letzten *bestätigten* grünen Commit vom neuen Quellkandidaten unterscheiden.
