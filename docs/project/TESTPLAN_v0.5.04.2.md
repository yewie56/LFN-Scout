# LFN-Scout v0.5.04.2 – Diagnoserelease-Testplan

**Status: geplante Tests, keine PASS-Angaben.** Siehe `AUDIO_DIAGNOSE_v0.5.04.2.md`.

- [ ] `PREPARE_PROJECT.bat` (neuer Ordner), `gradlew.bat :app:testDebugUnitTest --stacktrace` und `gradlew.bat assembleDebug --stacktrace` erfolgreich.
- [ ] Update über v0.5.04.1 ohne Datenlöschung; `versionName 0.5.04.2`, versionCode 147.
- [ ] Rolling EIN → 48 kHz → STOP (Rolling bleibt AUS) → Panel verlassen (Rolling neu EIN); vollständige `AUDIO_DIAG_*`-Kette mit `sid` aufzeichnen.
- [ ] Anschließend erneut 48-kHz-Modus aufrufen, 20 s laufen lassen, STOP und verlassen; mindestens zehn kontrollierte Wiederholungen und Fehler/Absturz samt AndroidRuntime-Logcat sichern.
- [ ] Rolling AUS → 48 kHz → STOP + Panel verlassen → Rolling bleibt AUS, ebenfalls Logging prüfen.
- [ ] Kurze 8-kHz-Gegenprobe: beide 8-kHz-Modi funktionieren weiterhin; keine neue Funktionalitätsabweichung.
- [ ] `DIRECT_PCM_HEARTBEAT` und `ANALYZER_PCM` bestätigen echte 48-kHz-Frames nach START, nicht nur Dienststatus.
- [ ] Bisheriger Testplan `TESTPLAN_v0.5.04.md` bei einer späteren Fehlerkorrektur wiederholen.

**Weiter offen:** PAUSE-Taste; 16:1-Energieabweichung; Ursache sporadischer 48-kHz-/Rolling-Abstürze. Neue Spektrumkurven Aktuell/Average/Maxhold und MAX/CLR sind nicht Teil dieses Diagnosereleases.
