# v0.5.27 – Android Cloud-Anbindung (Teilimplementierung)

Stand: 10.10.2026 – Source Candidate, **kein Android-Build-PASS**.

## Umgesetzt

- Upload-WAV sendet `capture_origin` aus dem bestehenden AudioArchiveEntry (`OBSERVER`/`ANALYZER`) sowie `trigger_type=MANUAL` für die derzeitigen manuellen Aufrufpfade.
- Analyzer-Audiopfad sendet nach erfolgreichem WAV-Upload die im lokalen Statistik-Sidecar bereits berechneten Overall- und bis zu 40 Filterdatensätze über `submit_audio_statistics`.
- Zuordnung lokaler Filterindex 0..39 zu Supabase 1..40. Kein erneuter Gain-/Statistik-Rechenlauf.
- Feste benannte Kennwerte einschließlich `valid_epochs`; der nur lokal vorhandene Wert `l10_minus_l90_db` wird nicht als unbekanntes API-Feld übertragen.
- API-Idempotenzschlüssel: `recording_id`, `set_kind`, `set_index`, `lookup_version=1`.
- Analyzer-Statusformulierung `Cloud ok` statt `Supabase OK`.

## Offen / begrenzt

- Uploadpfad für Observer-Statistik-Sidecars noch nicht angeschlossen.
- Kontinuierliche, WAV-unabhängige 30-Minuten-Buckets noch nicht angeschlossen.
- Separater persistenter Retry-Zustand für Statistik fehlt: Scheitert nur ein Statistik-POST nach einem erfolgreichen WAV-POST, wird die Statistik derzeit lediglich protokolliert und nicht zuverlässig nachgeholt. **Daher nicht produktiv freigeben.**
- Exakte Trigger-Identifikation für automatische Rolling-/Hardware-Trigger fehlt.
- Live-GPS wird im Upload noch nicht mitgesendet; serverseitiger Observer-Basis-Fallback bleibt aktiv.
- Kein echter Android-Test, APK-Build oder Gerätetest; im Prüfcontainer verhindert fehlende Gradle-8.13-Distribution/DNS den Gradle-Lauf.
- Der sichtbare Erfolgshinweis bezieht sich auf den WAV-Upload, nicht auf vollständige Statistikübertragung.

## Nächste Schritte

Persistente Statistik-Outbox einschließlich Retry/Erfolg je Recording und Set implementieren, Observer-Sidecars dauerhaft zuordnen, 30-Minuten-Aggregation unabhängig von WAV aufsetzen, vollständige Regressionen, Windows-Vortest und Gerätetest.

## Fortsetzung 10.10.2026 – persistente Outbox (Source Candidate)

- Neu: `AudioStatisticsOutbox.kt`, je Statistikset eine atomar umbenannte JSON-Datei im privaten App-Speicher; der Observer-Key wird vor dem Persistieren entfernt und erst unmittelbar vor dem HTTP-POST aus dem aktuellen Schlüssel ergänzt.
- Observer: bestehende lokale Statistikberechnung erzeugt nun unmittelbar eine Queue-Kopie; ein Post wird erst durch `markRecordingUploaded` nach bestätigtem WAV-Upload freigegeben. Kein zusätzlicher Mikrofon-/Gain-Berechnungspfad.
- Analyzer: bei erfolgreichem WAV-Upload werden bestehende lokale Sidecar-Statistiken in die Outbox übernommen und gesendet. Einzelne erfolgreich bestätigte Sets werden entfernt, Fehler bleiben für spätere erneute Versuche erhalten.
- Bei manuellem Audio-Retry und beim folgenden erfolgreichen Observer-WAV-Upload wird ein erneuter Outbox-Versand versucht. Keine Hintergrund-/Zeitplan-Wiederholung implementiert; derzeit keine garantierte Wiederaufnahme ohne weiteres Uploadereignis.
- Bekannte Grenzen: Crash zwischen bestätigtem WAV-POST und Erzeugung der Analyzer-Outbox kann zum fehlenden Statistikset führen; Fehler beim Observer-Sidecar-Erstellen bleiben lokal protokolliert. Observer-Statistik wird vor WAV-Upload nur vorgemerkt. Der Observer-Key muss zum jeweiligen Observer des Datensatzes passen; ein Schlüsselwechsel wird noch nicht getrennt nach Owner verarbeitet.
- Exakte unabhängige :00/:30-Statistik erfordert kontinuierliche, lückenüberwachte Epoche-Samples über 1800 Sekunden; **nicht implementiert**. Keine aus einzelnen WAVs zusammenphantasierten COMPLETE-Zeitfenster.
- Kein Android-Build-/Gerätetest-PASS.
