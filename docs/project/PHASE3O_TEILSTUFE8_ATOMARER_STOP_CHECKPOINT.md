# Phase 3O – Teilstufe 8: gemeinsamer STOP-Checkpoint

- DSP und Audio-/Timing-Snapshot werden unter *demselben* `LiveAnalyzerController.lock` in einem unveränderlichen `StoppedAnalyzerConfigCheckpoint` erfasst.
- `isStoppedAnalyzerConfigCheckpointCurrent` prüft beide Snapshots atomar unter Controller-Lock und liefert bei laufendem Analyzer false.
- Kein STOP/START, keine Recorder-Koordination, keine SharedPreferences-Schreibzugriffe, kein JSON-Apply und kein Rollback von Audiohardware. `Plan.executable` bleibt false.
- Neuer statischer Regressionstest: `tools/verify_lfn056_phase3o_atomic_checkpoint.py`.
- 85 historische Supabase-Audiodateien sind aus diesem *Quellpaket* ausgelagert; externes, bitweise mit den Ausgangsdateien abgeglichenes Recovery-ZIP separat bereitgestellt. Vor Löschung irgendwo zuerst Backup dauerhaft und redundant sichern. Kein `git rm` und keine Änderung an `.gitignore`, Workflows oder Termux v4.
- GitHub-Test und Android-Gerätetest für diese Teilstufe noch ausstehend.
