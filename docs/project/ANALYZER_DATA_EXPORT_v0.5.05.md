# LFN-Scout v0.5.05 – Setup-/Diagnoseexport

## Speicherorte

Die Originalwerte liegen weiterhin in Android-internen `SharedPreferences` (`lfn_scout_live_analyzer`), nicht in separaten privaten JSON-Dateien. Nur **vorhandene** Slots werden exportiert: `Download/LFN-Scout/Setups/Setup_1.json`, `Setup_2.json`, `Setup_3.json`, `Setup_4_48k.json`. Slot 4 entsteht beim ersten 48-kHz-Start. Beim ersten Start nach dem Update werden bestehende Slots kopiert, bei Setup-Speichern wird neu exportiert. Exportierte Dateien sind Inspektionskopien; externe Änderungen daran werden nicht eingelesen.

Im Analyzer unter Einstellungen → Setups gibt es **Setups als JSON exportieren**. Unter Einstellungen → Diagnose gibt es **Diagnose-ZIP exportieren**. Dieses liegt in `Download/LFN-Scout/Diagnostics/` und enthält: Setup-Kopien, erlaubte Analyzer-Konfigurationswerte, Audio-Modusmarker, erlaubte Testgenerator-/Impulsparameter, gefilterte `AUDIO_DIAG_*`-Zeilen. Der volle Android-Logcat wird ausdrücklich **nicht** in das ZIP kopiert. Für einen harten Crash bei Bedarf weiterhin `adb -s <GERAET> logcat ...` verwenden.

## Datenschutz und Grenzen

Keine `pending_observations.json`, ungeschützten Authentisierungs-/Teilnehmerschlüssel, GPS-/Beobachterdaten, Audio-WAV-Dateien, binären DataStore-Dumps oder privaten Datenbankkopien. Das Archiv enthält nur eine bewusst freigegebene **Teilmenge**, nicht alle internen Dateien. Setup-Namen und -Beschreibungen können selbst frei eingegebene persönliche Texte enthalten und sind nach dem Export öffentlich zugänglich; Dateien daher vor Weitergabe prüfen. Der manuelle Export wird bei Bedarf vom Benutzer ausgelöst. Android 9 und älter können für Downloads eine Legacy-Speicherberechtigung erfordern; Android 10+ MediaStore.

## Fehlerfälle

Fehlschläge zeigen die UI-Nachricht, und die ursprünglichen internen Setups bleiben erhalten. Erst bei bestätigtem Schreibabschluss wird eine Datei als exportiert gemeldet. Android-/Xiaomi-Test dieser Funktion steht noch aus.
