# BUILD_VERIFICATION v0.5.12 – 24.09.2026

Basis v0.5.11/154 → v0.5.12/155. Vollständiges Projekt aus der tatsächlichen v0.5.11-ZIP kopiert; Icon exakt aus aktuellem Nutzerupload übernommen.

| Prüfung | Ergebnis | Aussagegrenze |
|---|---|---|
| `bash tools/RUN_FILTER_DISPLAY_V0512_SELFTEST.sh` | PASS 10/10 | echter Kotlin-JVM-Test, nur Filter-Autorange-Kern |
| `python tools/verify_v0512_integration.py` | PASS 18/18 | statischer Quellvertrag, Hashgleichheit der beiden Icon-Kopien; kein Android-Compile |
| `bash tools/RUN_FILTER_RENDER_V0512_SELFTEST.sh` | PASS 5/5 | Separat kompilierter Kotlin-Renderer mit minimalem Android-Color-Teststub, **kein** Android-Build |
| Kotlin-PSI (UI + Core) | 85 + 25 Dateien, 0 Syntaxfehler | Keine Android-/Compose-Typprüfung |
| `bash tools/RUN_ANALYZER_V0509_SELFTEST.sh` | PASS 33/33 | Direkt-Analyzer-DSP-Regression |
| `bash tools/RUN_ANALYZER_V0506_SELFTEST.sh` | TIMEOUT / Terminated | NICHT als PASS werten |
| `:app:testDebugUnitTest`, `:app:assembleDebug` | NOT RUN | Android SDK in dieser Umgebung nicht verfügbar |
| Xiaomi-Gerätetest | NOT RUN | keine praktische UI-/Pegel-Freigabe |
| Vollständige LFN-IMP-001-Ereignisvalidierung | NOT IMPLEMENTED/NOT RUN | Nur FAST-Filterbank Stufe A, keine verifizierte Onset-Erkennung |

Manuelles Setzen/Auto-Ermitteln der FILTER-Skala verändert ausschließlich die Darstellung; die physikalische Filterantwort ist weiter experimentell. Der separat gemeldete 16:1-Pegelverlust bleibt offen.
