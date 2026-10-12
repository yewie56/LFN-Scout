# Analyzer – gemeinsamer Audio-Archivpfad ab v0.5.23

## Ziel

Analyzer- und Beobachter-WAVs sollen denselben bestehenden lokalen Audio-Lifecycle nutzen. Für den Übergang darf kein neuer Audio-Unterordner entstehen, weil die bestehende 24-h-Verwaltung auf dem bisherigen Hauptpfad arbeitet.

## Verbindlicher Pfad

- Beobachter-WAV: `Download/LFN-Scout/`
- Analyzer-WAV: `Download/LFN-Scout/`
- Analyzer-Statistik-JSON: `Download/LFN-Scout/`
- Analyzer-Manifest-JSON: `Download/LFN-Scout/`

Für Analyzer-Saves wird insbesondere **kein** `Download/LFN-Scout/audio/` und kein `Download/LFN-Scout/LocalSaves/` neu verwendet.

## Dateinamensnomenklatur

Analyzer-WAVs verwenden `AudioFileNamingPolicy.buildLocalWavFileName()` genau wie Beobachter-WAVs. Der gespeicherte Analyzer-Puffer ist PRE-GAIN; deshalb ist `G0` im WAV-Dateinamen korrekt. Der reine Analyzer-Anzeigegain bleibt als eigenes Metadatum im Statistik-JSON erhalten und darf nicht als tatsächlich in die WAV eingerechneter Gain ausgegeben werden.

Die Statistik-/Manifestdateien verwenden denselben WAV-Stamm plus `_statistics.json` bzw. `_manifest.json`.

## Archivmetadaten

`AudioArchiveEntry.origin` unterscheidet `OBSERVER` und `ANALYZER`. Herkunft wird nicht mehr über unterschiedliche Verzeichnisse abgebildet.

## Supabase und Retention

Analyzer-WAVs verwenden denselben bestehenden `SupabaseClient.uploadWav()`-Pfad wie Beobachter-WAVs. Nach erfolgreichem Upload wird `AudioArchive.markUploaded()` gesetzt; die bestehende 24-h-Retention gilt damit unverändert. Uploadfehler lassen die WAV lokal und der bestehende Retry aus dem lokalen Audioarchiv kann sie erneut übertragen.

Statistik-JSON und Manifest werden noch nicht nach Supabase übertragen. Sie bleiben lokale Begleitdateien, bis ein passendes Supabase-Schema eingerichtet ist. Beim lokalen 24-h-Cleanup eines erfolgreich übertragenen Analyzer-WAVs werden beide Begleitdateien zusammen mit WAV und Archivmetadaten entfernt.

## Nachträgliche Analyse

Weil Analyzer-WAVs jetzt als normale `AudioArchiveEntry` geführt werden, können sie über den bestehenden lokalen Audio-/Wasserfallweg erneut ausgewählt und zu Spektrum/Wasserfall/L50/L90 ausgewertet werden.

## Manueller Analyzer-Bildsave

Beim Kamera-Langdruck wird zuerst ein `LiveAnalyzerController.Snapshot` fixiert. Erst danach erscheint der optionale Kommentardialog. Der Kommentar wird anschließend direkt unten in den Wasserfall des exportierten PNG/JPG gerendert; erst dann wird die Bilddatei geschrieben. Automatische Bildexports bleiben ohne Kommentardialog.
