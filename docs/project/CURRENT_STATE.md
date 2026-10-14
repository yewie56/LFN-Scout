# LFN-Scout – CURRENT_STATE, Phase 3O/11

**Stand:** 14.10.2026. Entwicklungsziel v0.5.56; Gradle unverändert `versionName 0.5.55`, `versionCode 213`.
**Letzter vom Nutzer bestätigter grüner GitHub TEST:** Phase 3O, Teilstufe 10. Commit-ID hier nicht unabhängig nachgewiesen.
**Neuer Quellkandidat 3O/11:** Statische Tests durchgeführt; GitHub TEST und Gerätetest noch offen.

**Neu:** `AnalyzerSetupAudioOwnerGate.kt` liest Mikrofon-Owner, Rolling-, Direct- und MON-Aufnahmezustand. Die bisher ohnehin nicht produktiv angeschlossene `AnalyzerSetupSwitchTransaction` lehnt bei aktiver Nutzung oder Abfragefehler ab. Kein Recorder und kein Audio-Service wird verändert.

**Sicherheitsgrenze:** Dies ist eine zusätzliche lesende Abweisung, **KEINE atomare Besitzersperre**, kein Hardware-/Recorder-Rollback und keine ausführbare Setup-Umschaltung. `AnalyzerSetupSwitchPlan.Plan.executable == false`. Zur Freigabe müssen alle Zustandsübergänge denselben Lock oder ein vergleichbar garantiertes Service-Protokoll verwenden und mit dynamischen Race-Tests geprüft werden.

**Pflichtregeln:** `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md` sowie Phase-3-Dokumentation lesen; vollständige ZIP ohne Wurzelordner; Regressionen. Termux v4 nicht ändern, geschützte Workflows/Signatur, Cloud, Recorder und Gradle unverändert lassen. Nur GitHub TEST zunächst ausreichend.

**Historisches Archiv:** `supabase/storage_backup_before_v0526` bleibt unverändert in GitHub `main`, gehört nicht in Entwicklungs-ZIPs; Fehlen im Archiv darf nicht zum Löschen im Termux-Arbeitsverzeichnis führen.

**Als nächstes:** Gemeinsam garantierte Koordination der tatsächlichen Recorder-/Service-Übergänge und dynamische Tests; danach speicherfreien STOP/START und verifizierbaren Rollback implementieren.

---

# LFN-Scout CURRENT STATE – Phase 3O/10, Quellkandidat

**Projekt:** v0.5.56 in Entwicklung, Gradle weiterhin `versionName 0.5.55`, `versionCode 213`.
**Letzter vom Nutzer bestätigter grüner GitHub-TEST:** Phase 3O/9. **Phase 3O/10:** statisch geprüft; GitHub-TEST und Android-Gerätetest offen. Letzter bestätigter Commit hier nicht erneut unabhängig ermittelt.

**Neu:** `LiveAnalyzerController` rollt bei einem Fehler der Session-Initialisierung den vorher erfassten DSP-/Audio-/Timing-STOP-Checkpoint ohne Speicherung zurück. Den Originalfehler nicht durch einen sekundären Rollback-Fehler ersetzen. Statische Regression `tools/verify_lfn056_phase3o_start_rollback.py`.

**Sperren und Grenzen:** Phase-3N-Plan bleibt `executable=false`. Keine vollständige Audio-/FFT-/JSON-Aktivierung, keine atomare Aufnahmeeigentümer-Koordination, kein umfassender Rollback für Hardware/Recorder oder Fehler vor der Session-Initialisierung. Keine mobile Funktionsprüfung behaupten.

**Sicherungen:** `supabase/storage_backup_before_v0526` verbleibt unverändert auf GitHub `main`, aus Quell-ZIPs ausgeschlossen, fehlende ZIP-Dateien niemals als Löschauftrag behandeln.

**Verbindlich:** Termux v4 unverändert, Gradle 0.5.55/213, GitHub TEST allein genügt zunächst, `CURRENT_STATE.md` bei jeder ZIP aktualisieren; `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `PROJECT_STATUS.md`, `DEFERRED_TASKS.md` beachten.

**Nächster Schritt:** echte Recorder-/Service-Owner-Koordination und dynamische Fault-Injection-Tests; erst dann ausführungssichere STOP-/START-Übernahme evaluieren.

---

# LFN-Scout – CURRENT_STATE (verbindliche laufende Übergabe)

**Stand:** 2026-10-14 · **Quellkandidat:** v0.5.56, Phase 3O, Teilstufe 9 · **Noch kein Release**

## Buildstatus und Versionsschutz

- Gradle `versionName = "0.5.55"`, `versionCode = 213`: bis Releasefreigabe unverändert.
- **Letzter vom Nutzer als GRÜN bestätigter GitHub-TEST:** Phase 3O, **Teilstufe 8** (Commit-ID nicht bekanntgegeben).
- **Teilstufe 9:** statische Quellprüfungen lokal ausführbar; GitHub TEST, Kotlin-/Gradle-Kompilation und Android-Gerätetest **noch offen**.
- TEST genügt vorerst; REGULAR nur bei ausdrücklichem Bedarf, besonders für Updates der bestehenden App.

## Aktuell implementiert

- Phasen 3A–3N siehe `docs/project/PHASE3*.md`; Phase 3N ist ausdrücklich lesender Umschaltplan mit `Plan.executable == false`.
- Phase 3O/1–6: Preflight-/Präzisionsschutz, speicherfreie STOP-gebundene DSP-Setter, DSP-Start-Override und gestoppter DSP-Checkpoint (siehe `PHASE3O_TRANSAKTIONSSCHUTZ_v0.5.56.md`).
- **Neu 3O/7:** `StoppedAudioTimingCheckpoint` im `LiveAnalyzerController` erfasst die zusätzlichen internen Direct-Mode- und Timing-Felder ausschließlich bei STOP. `isStoppedAudioTimingCheckpointCurrent` erkennt zwischenzeitliche Änderungen unter demselben Controller-Lock; es gibt keinerlei Zustandsänderungen oder Preference-Zugriffe.

- **Neu 3O/8:** `StoppedAnalyzerConfigCheckpoint` verbindet DSP- und Audio-/Timing-Checkpoint in einem Controller-Lock; gemeinsame Stale-State-Prüfung ohne Persistenz. Kein Recorder-/Hardware-Rollback.
- **Backup-Auslagerung:** `supabase/storage_backup_before_v0526` (85 Dateien) ist in separatem, byteweise verifiziertem Recovery-ZIP gesichert und in dieser Projekt-ZIP nicht enthalten. Das Backup bleibt vereinbarungsgemäß in GitHub `main` versioniert und wird nicht mehr in Projekt-ZIPs mitgeliefert. Originalrepository und Git-Tracking werden nicht geändert; bei Termux-v4-Import bleiben die Dateien bestehen. Separates Backup als zusätzliche Absicherung aufbewahren.

- **Neu 3O/9:** Lokale Session-Startbereinigung: `running=true` erst nach erfolgreichem Session-Aufbau; bei Initialisierungsfehler best-effort Cleanup und Fehlerweitergabe. Kein vollständiger Zustandsrollback.

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

Als Nächstes die tatsächliche Recorder-/Service-Owner-Koordination sowie Fault-Injection-Tests und ein ausführbarer STOP-/START-/Rollback-Vertrag mit atomarer Besitzer-Sperre, dynamischen Tests; keine Aktivierung vor Nachweis und dynamische Regressionen. Keine vorzeitige Freigabe; Gerätetests ausdrücklich nachholen.

## Übergabe in neuem Chat

Aktuelle vollständige Quell-ZIP verwenden, danach `CURRENT_STATE.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `DEFERRED_TASKS.md` sowie Phasendokumentation lesen. Immer letzten *bestätigten* grünen Commit vom neuen Quellkandidaten unterscheiden.
