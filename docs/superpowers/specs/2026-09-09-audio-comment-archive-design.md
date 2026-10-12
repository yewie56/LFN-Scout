# Audio-Kommentar und 24h-Lokalarchiv – Design

## Ziel
Nach jeder erfolgreichen 120-s-WAV-Aufnahme wird vor dem Upload ein Kommentardialog gezeigt. Ohne Interaktion erfolgt nach 10 s der Upload ohne Kommentar. Nach erster Interaktion mit dem Kommentarfeld gilt ein sichtbarer 60-s-Inaktivitäts-Countdown; jede Texteingabe startet diese 60 s neu. Danach wird mit dem bis dahin vorhandenen Kommentar gesendet.

## Lokale Aufbewahrung
WAV-Dateien liegen unter `File(context.filesDir, "audio_recordings")`. Zu jeder WAV existiert eine JSON-Metadatei mit recording_id, Aufnahmezeit, Gain, Quelle, Clip-Zahl, Kommentar, Uploadstatus, Uploadzeit und optional storage_path/Fehler. Erfolgreich hochgeladene Dateien werden ab erfolgreichem Upload 24 h aufbewahrt und danach zusammen mit ihrer JSON-Datei gelöscht. Nicht erfolgreich gesendete Dateien werden nicht automatisch gelöscht.

## UI
Unter „Zusatzfunktionen“ gibt es „Audio-Aufzeichnungen“ mit dem tatsächlich ermittelten Verzeichnispfad, „Pfad kopieren“, Aktualisieren, Liste lokaler Aufnahmen und „Erneut senden“ für nicht erfolgreich gesendete Aufnahmen.

## Backend
`audio_recordings` erhält `comment text` (nullable, max. 2000 Zeichen per CHECK). `upload_wav` akzeptiert `comment`, validiert max. 2000 Zeichen und speichert es zusammen mit den Metadaten. WAV-Dateiname und 30-Minuten-Rating-Regel bleiben unverändert.

## Version
Android-App v0.4.44, versionCode 84. Edge Function `upload_wav` Version 1.3.0.
