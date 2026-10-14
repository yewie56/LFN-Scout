# Phase 3O Teilstufe 16 – Capture-Cleanup-Quittierung

Service-`onDestroy()` und `Job.invokeOnCompletion` sind zwei separate Ereignisse. Der Setup-Startmarker bleibt bis zu beiden Ereignissen bestehen. Es findet keine Freigabe der Setup-Aktivierung statt. Keine garantierte Terminierung eines hängenden Jobs; fail-closed. Status: statische Tests, GitHub TEST offen.
