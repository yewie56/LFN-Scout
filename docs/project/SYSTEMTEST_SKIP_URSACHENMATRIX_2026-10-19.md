# LFN-Scout – SKIP-Ursachenmatrix (Weiter 1/5)

Quelle: `LFN-Scout_Systemtest_2026-10-19_22-08-17_241.zip`; appVersion `0.5.80`, runId `mvforhiw`.

**10 SKIP.** Keine SKIP-zu-PASS-Umdeutung.

| ID | Tatsächlicher Grund | Benötigter echter Nachweis |
|---|---|---|
| `live_mon_enabled_stop_restart` | MON was OFF at test start; STOP/RUN with MON enabled not exercised | MON im Test bewusst aktivieren, aktive Audioausgabe absichern, mit finally vollständig wiederherstellen; echter STOP/RUN |
| `rolling_started` | Recording starts=0; no explicit rolling session start; available log window only | Zeitfenster der Logdatei enthält Sessionstart nicht; Live-Gateway-Laufzustand zu Beginn mit Zeitstempel prüfen, separat von historischem Start |
| `rolling_lifecycle` | Recording starts=0, stop requests=0, cleanups=0; explicit user stop allowed; no explicit rolling session start; available log window only | Echte Servicegeneration und Start-/Stop-Zähler vor und nach dem Test direkt vergleichen |
| `analyzer_start_observed` | Analyzer successful initializations=0; no explicit rolling session start; available log window only | Analyzer-Start beim Lauf bereits vor Logfenster; echte restartMeasurement-Zyklen als getrennten Beleg anführen |
| `unhandled_crash_observed` | Fatal/restart failures in selected log window=0; Android-only crashes require logcat | UncaughtException-Nachweis benötigt Android-Logcat oder scoped Crash-Monitor, kein PASS aus leerem App-Log |
| `sample_perfect_continuity` | PCM block sequence/gap verification is not yet instrumented | Block-/Sample-Sequenzen über Quelle und Ringbuffer prüfen; physikalische Lückenfreiheit nicht vortäuschen |
| `recording_write_continuity` | Per-block long-term recording write acknowledgements not yet instrumented | Echte produktive Aufnahmekette mit ack/queue/file bytes, sichere Testdatei separat |
| `setup_mon_roundtrip` | Requires safe isolated setup fixture and state-controller harness | Isolierte benannte Live-Setupsteuerung A→B→A mit Save-before-switch und Rollback |
| `filterbank_roundtrip` | Requires safe isolated filterbank fixture and controller harness | Filterbank über echte STOP/Start-Steuerung ändern und vollständig zurücksetzen |
| `statistics_coverage` | Requires independent valid-sample and gap accounting | Counter pro 30-min-Fenster aus echter Aufnahme einschließlich fehlender Sekunden und persistierter CSV verifizieren |

## Strikte Sicherheitsgrenzen

- Keine produktiven benannten Setups ändern; Testdaten nur in app-internen isolierten Bereichen.
- Rolling-Aufnahme niemals stoppen; MON nur bei garantiertem automatischem Rollback verändern.
- Keine PASS-Umdeklaration ausschließlich aufgrund vorhandener anderer PASS-Ergebnisse.
- Android-Build und Live-Ergebnisse erst nach deinem tatsächlichen Gerätetest als geprüft ausweisen.
