# LFN-Scout v0.5.04.2 – Audio-/Rolling-Diagnose

## Ziel und Grenzen

Ermittlung der Ursache sporadischer Abstürze bei (A) Rolling EIN → Analyzer 48 kHz → STOP → gestopptes Panel verlassen / Rolling neu starten und (B) nach wiederhergestelltem Rolling erneut 48 kHz anwählen. **Die 8-kHz-Modi funktionieren laut Nutzer.** Diese Version erhöht ausschließlich die Beobachtbarkeit; weder Crash noch fehlende PAUSE-Taste oder 16:1-Pegelabweichung sind hiermit behoben.

## Erfasste Informationen

Jeder Eintrag enthält Uhrzeit (persistentes Debuglog), Sitzungs-ID `sid`, Prozess-ID `pid`, Thread, Eventnamen und zustandsbezogene Attribute. Ereignisse:

| Gruppe | Ereignisse | Zweck |
|---|---|---|
| Sitzungsstart | `BEGIN_REQUEST`, `ORIGINAL_SAVED`, `PREPARE_MODE`, `BEGIN_SUCCESS`, `BEGIN_FAILED`, `BEGIN_EXCEPTION` | Ursprung / Moduswahl / Fehler |
| Rolling beenden/starten | `ROLLING_STOP_REQUESTED`, `ROLLING_RELEASE_RESULT`, `ROLLING_REQUEST_START`, `ROLLING_REQUEST_STOP`, `ROLLING_SERVICE_COMMAND`, `ROLLING_LEASE`, `ROLLING_RECORDING_STARTED`, `ROLLING_START_RESULT`, `ROLLING_CLEANUP_*` | Freigabe und erneuter Mikrofonbesitz |
| Direktquelle | `DIRECT_REQUEST_START`, `DIRECT_SERVICE_COMMAND`, `DIRECT_CAPTURE_ENTER`, `DIRECT_LEASE`, `DIRECT_RECORDING_STARTED`, `DIRECT_START_RESULT`, `DIRECT_CLEANUP_*`, `DIRECT_ON_DESTROY` | Service-/AudioRecord-Lebenszyklus |
| Datenfluss | `DIRECT_PCM_HEARTBEAT`, `ROLLING_PCM_HEARTBEAT`, `ANALYZER_SUBSCRIBED`, `ANALYZER_PCM` | Tatsächlicher Eingang / Analyzer-Verbraucher; Heartbeat ca. alle 15 s |
| STOP / Wechsel / Restore | `STOP_REQUEST`, `STOP_DONE`, `PANEL_EXIT_REQUEST`, `PANEL_EXIT_DONE`, `RESTORE_BEGIN`, `RESTORE_MIC_RELEASE`, `RESTORE_DONE`, `RESTART_REQUEST`, `RESTART_DONE`, `PROCESS_RECOVERY` | Reihenfolge und Überlappungen |
| Abbruch | `DIRECT_CAPTURE_FAILURE`, `ROLLING_CAPTURE_FAILED`, `*_CANCELLED`, `RESTORE_EXCEPTION` | Fehler / Abbruchursache |

Die Logs zeichnen **keine Audiosamples** auf. Eine `BEGIN_SUCCESS`-Meldung allein garantiert keine Audioframes; dazu `DIRECT_PCM_HEARTBEAT` und `ANALYZER_PCM` prüfen. Die Audio-Aufnahme wird vom Betriebssystem ggf. asynchron freigegeben: besonders `owner`, `capturing`, `engine` und ihre Reihenfolge vergleichen.

## Windows: Absturz möglichst vollständig erfassen

Zuerst die v0.5.04.2 bauen, installieren und reproduzieren. Mit per USB angeschlossenem Xiaomi (USB-Debugging aktiv) im **PowerShell-Fenster**:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices
& $adb logcat -c
& $adb logcat -v threadtime > "$env:USERPROFILE\Downloads\LFN-Scout_48k_logcat.txt"
```

Das letzte Kommando läuft bis `Strg+C`. **Vor** dem eigentlichen Test starten; nach einem Absturz noch ca. 10 s laufen lassen. Anschließend die Datei auf Einträge `LFN-AUDIO`, `FATAL EXCEPTION`, `AndroidRuntime`, `AudioRecord` prüfen. Das vollständige Logcat enthält möglicherweise persönliche Daten anderer Apps; vor Weitergabe ggf. schwärzen oder im gleichen Versuch gezielt das folgende gefilterte Log erzeugen:

```powershell
& $adb logcat -v threadtime -s LFN-AUDIO:I AndroidRuntime:E AudioRecord:E '*:S' > "$env:USERPROFILE\Downloads\LFN-Scout_48k_gefiltert.txt"
```

Alternativ das in der App bestehende Debuglog über die Debug-Log-Exportfunktion nach `Download/LFN-Scout/` speichern (`LFN-Scout_Log_*.txt`). Ein harter Prozessabsturz kann verhindern, dass alle letzten Zeilen in der App-Datei stehen; deshalb Logcat bevorzugen.

## Reproduktion

1. Rolling manuell EIN. Analyzer starten, `NEIN – 48 kHz` wählen. 20 s messen.
2. STOP; Panel **erst danach** verlassen. Rolling muss neu starten. Ereignisfolge notieren.
3. Erneut Analyzer öffnen, `NEIN – 48 kHz` wählen. Wiederholen, bis der sporadische Fehler beobachtet wird. Zwischen den Versuchen nicht die App-Daten löschen.
4. Bei jedem Fehler die zeitlich letzten 30–60 s Logcat inklusive `FATAL EXCEPTION` und die zugehörige `sid` sichern. Falls Crash nicht reproduzierbar: 8-kHz-Modus nur als kurze Gegenprobe.

**Fehler nicht als gelöst einstufen**, solange keine reproduzierbare Ursache/Fix plus Windows- und Xiaomi-Regression dokumentiert sind. Bei der Log-Weitergabe keine Supabase-Schlüssel, IP-Adressen oder Standortwerte mitsenden.
