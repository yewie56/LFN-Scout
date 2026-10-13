# v0.5.56 Phase 3E – MON-Laufzeitentwurf

Neuer isolierter, standardmäßig inaktiver MON-Draft-Pfad in `AnalyzerMonitor.kt`.

- `snapshotSetupParameters()` liest die 16 effektiven MON-Werte.
- `applySetupDraft(JSONObject)` prüft 16/16 MON-Felder und deren Grenzen vollständig, bevor die Runtime verändert wird. Eine nur teilweise migrierte MON-Gruppe kann **noch nicht aktiviert** werden; sie muss erst durch eine explizite Nutzerauswahl vollständig ergänzt werden.
- `setupDraftActive` verhindert, dass `start()` und `load()` aus alten Preferences über einen aktiven Setup-Draft schreiben.
- MON-Slider und Schalter speichern nur in der unveränderten Legacy-Betriebsart unmittelbar. Im Draft-Betrieb bleiben Änderungen bis zum expliziten Repository-Save flüchtig.
- `releaseSetupDraft(context)` darf nur bei gestopptem MON ausgeführt werden und lädt die alten Werte erneut.
- Bestehende UI ist nicht auf diesen Pfad umgestellt; Cloud, Recording, alte Persistenz und Termux-Skript unverändert.

**Nicht enthalten:** automatische Alt-Setup-Aktivierung, acht Panels, Save/Discard/Cancel, sichere Setup-Umschaltung während Messung, SAF Import/Export, vollständiger Android-Build. `versionName` weiterhin `0.5.55` (nicht freigegebener Entwicklungsquellkandidat).

Prüfung: `python tools/verify_lfn056_phase3e.py` (statische Prüfung), anschließend Android `:app:testDebugUnitTest` und `:app:assembleDebug` erforderlich.
