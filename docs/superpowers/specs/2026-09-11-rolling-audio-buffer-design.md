# 30-Minuten-Rolling-Audiobuffer – Design

## 1. Ziel

LFN-Scout erhält optional einen permanent laufenden 30-Minuten-Rolling-Audiobuffer. Er soll niederfrequente Ereignisse nachträglich sichern können, auch wenn der Benutzer die Aufnahme nicht vor dem Ereignis gestartet hat.

Der Rolling-Buffer ist bewusst ein **rein lokales** Zusatzsystem. Er wird nicht zu Supabase übertragen und verändert die bestehende Beobachtungs-/Bewertungslogik nicht.

## 2. Grundprinzip

Wenn der Rolling-Buffer in den Einstellungen aktiviert ist, läuft ein Android-Foreground-Service mit einem einzigen `AudioRecord`-Stream. Dieser Stream verwendet dieselben Audioeinstellungen wie die normale 120-s-Aufnahme:

- aktuell ausgewählter AudioSource-Modus (`AUTO`, `UNPROCESSED` oder `MIC`),
- aktuell ausgewählter physischer Audioeingang,
- aktueller Gain,
- 1000 Hz,
- mono,
- 16 Bit PCM.

Es gibt zunächst **keine eigenen Rolling-Buffer-Audioeinstellungen**.

Der Dienst speist einen Ringpuffer im RAM. Zusätzlich kann derselbe laufende Stream eine normale 120-s-Aufnahme bedienen. Dadurch wird das Mikrofon nicht zweimal gleichzeitig geöffnet und der Rolling-Buffer wird durch eine normale Aufnahme nicht unterbrochen.

Wenn der Rolling-Buffer deaktiviert ist, bleibt der bisherige Ablauf der normalen 120-s-Aufnahme erhalten.

## 3. RAM-Ringpuffer

Der Ringpuffer enthält maximal die letzten 30 Minuten Audio.

Bei 1000 Hz, mono und 16 Bit ergibt sich:

`1000 Samples/s × 1800 s × 2 Byte = 3.600.000 Byte`

Der eigentliche PCM-Puffer benötigt damit ungefähr 3,6 MB RAM zuzüglich kleiner Verwaltungsdaten.

Nach dem Start wächst der Puffer von 0 bis 30 Minuten. Danach werden fortlaufend die ältesten Samples überschrieben. Es wird im laufenden Betrieb **nicht permanent auf Flash geschrieben**.

Ein noch nicht gesicherter RAM-Puffer darf bei Prozessabbruch, Geräte-Neustart oder endgültigem Beenden des Dienstes verloren gehen. Bereits gesicherte Rolling-Dateien bleiben davon unberührt.

## 4. Konsistenter Schnappschuss beim Sichern

Im Panel `MORE` gibt es einen deutlich sichtbaren Button:

**Rolling-Buffer sichern**

Beim Antippen wird ein konsistenter Schnappschuss des zu diesem Zeitpunkt verfügbaren Ringpuffers erzeugt:

- ist der Puffer erst 8 Minuten aktiv, werden diese 8 Minuten gespeichert,
- ist er 30 Minuten oder länger aktiv, werden exakt die jeweils letzten 30 Minuten gespeichert.

Der laufende Recorder wird für das Schreiben der Datei nicht gestoppt. Die Audioaufnahme läuft unmittelbar weiter.

Die technische Umsetzung soll den Ringpuffer nur so kurz wie nötig sperren. Bevorzugt wird ein Kopieren des gültigen PCM-Bereichs in einen Snapshot und anschließendes asynchrones Schreiben der WAV-Datei.

## 5. Kommentar

Unmittelbar nach `Rolling-Buffer sichern` wird ein Kommentardialog geöffnet.

Der Kommentar wird nicht in die WAV-Datei eingebettet. Zu jeder Rolling-WAV wird eine gleichnamige JSON-Datei gespeichert.

Beispiel:

```text
BB001_ROLL_2026-09-11_05-32-10_bis_06-02-10_G30_UNP_R3.wav
BB001_ROLL_2026-09-11_05-32-10_bis_06-02-10_G30_UNP_R3.json
```

Die JSON-Datei enthält mindestens:

- `rolling_buffer: true`,
- Observer-ID,
- Startzeit des gesicherten Ausschnitts,
- Endzeit des gesicherten Ausschnitts,
- tatsächliche Dauer,
- Sample-Rate,
- Kanalzahl,
- Bittiefe,
- Gain,
- tatsächlich verwendete Audioquelle,
- tatsächlich verwendeten/gerouteten Audioeingang, soweit verfügbar,
- zugeordnete Bewertung bzw. `RX`,
- Kommentar,
- Erstellzeit der Dateien,
- WAV-Dateiname.

Der Kommentar gehört damit dauerhaft zu genau dieser Rolling-Aufnahme.

## 6. Bewertungszuordnung

Der Dateiname erhält die letzte gültige Bewertung `0..5`, die innerhalb der 30 Minuten vor dem Zeitpunkt des Sicherns abgegeben wurde.

- `-1` wird nicht als Bewertung verwendet.
- Liegt keine passende Bewertung vor, wird `RX` verwendet.
- Genau 30 Minuten zurückliegende gültige Bewertung zählt noch.

Beispiel:

```text
BB001_ROLL_2026-09-11_05-32-10_bis_06-02-10_G30_UNP_R3.wav
```

Die Bewertung beschreibt die letzte passende Beobachtung vor dem Sicherungszeitpunkt. Sie muss nicht innerhalb des tatsächlich vorhandenen Audioausschnitts liegen, wenn der Puffer beim Start noch keine 30 Minuten gefüllt war.

## 7. Lokale Speicherung und Supabase-Trennung

Rolling-Dateien sind **ausschließlich lokal**.

Sie dürfen:

- nicht in die normale Audio-Upload-Warteschlange gelangen,
- nicht an `upload_wav` gesendet werden,
- nicht in `audio_recordings` auf Supabase eingetragen werden,
- nicht durch die bestehende 24-h-Löschlogik erfolgreicher Supabase-Aufnahmen entfernt werden.

Gesicherte Rolling-WAVs und die zugehörigen JSON-Dateien bleiben dauerhaft gespeichert, bis der Benutzer sie ausdrücklich löscht.

Als Ablage wird derselbe sichtbare lokale Audio-Bereich unter `Download/LFN-Scout/` verwendet, damit normale und Rolling-Aufnahmen gemeinsam auffindbar bleiben.

## 8. Gemeinsame Audio-Liste in MORE

Normale 120-s-Aufnahmen und Rolling-Aufnahmen erscheinen zusammen in der bestehenden Audio-Liste, neueste zuerst.

Rolling-Einträge werden optisch eindeutig gekennzeichnet, zum Beispiel:

```text
ROLL · 30:00 min
ROLL · 08:17 min
```

Die Darstellung soll vorhandene Metadaten wie Zeit, Gain, Quelle, Bewertung und Kommentar weiterhin zugänglich machen.

Ein normaler Tipp auf den Datei-/Listeneintrag öffnet bzw. erzeugt wie bisher die Wasserfall-Darstellung.

## 9. Löschen per langem Druck

Ein langer Druck auf einen Rolling-Eintrag öffnet eine Löschrückfrage.

Erst nach Bestätigung werden gemeinsam gelöscht:

- Rolling-WAV,
- gleichnamige Rolling-JSON,
- gegebenenfalls lokal erzeugte Wasserfallbilder, soweit sie eindeutig dieser Aufnahme zugeordnet werden können.

Ein Abbruch der Rückfrage verändert keine Datei.

Die bestehende Bedienung normaler 120-s-Aufnahmen soll dadurch nicht unbeabsichtigt verändert werden.

## 10. Wasserfall für Rolling-Aufnahmen

Für Rolling-Aufnahmen wird **kein neues Spektrogramm-Verfahren** eingeführt. Es wird dieselbe vorhandene fallende Wasserfall-/Spektraldarstellung verwendet wie für normale lokale WAV-Dateien.

Beibehalten werden insbesondere:

- bisheriger Frequenzbereich,
- Inferno-Darstellung,
- Pegelskala,
- L50/L90,
- Kommentar,
- Zoom,
- Pan,
- Doppeltipp zum Zurücksetzen,
- konfigurierter PNG-/JPG-/PNG+JPG-Export.

Bei langen Dateien bis 30 Minuten werden weiterhin alle vorgesehenen FFT-Auswertungen durchgeführt. Für die begrenzte Bildhöhe werden mehrere benachbarte Zeitsegmente auf eine Bildzeile verdichtet. Die Frequenzauflösung und die Pegel-/Statistikauswertung sollen dadurch nicht auf ein separates vereinfachtes 30-Minuten-Verfahren umgestellt werden.

Die Verdichtung muss deterministisch sein, damit gleiche WAV-Dateien reproduzierbare Bilder und Statistiken liefern.

## 11. Foreground Service und Hintergrundbetrieb

Der Rolling-Buffer läuft in einem Android-Foreground-Service mit dem passenden Mikrofon-Service-Typ und einer dauerhaft sichtbaren Systembenachrichtigung.

Die Benachrichtigung soll mindestens den aktiven Zustand erkennen lassen, zum Beispiel:

```text
Rolling-Buffer · 17:24 / 30:00 min
```

Der Dienst läuft auch bei ausgeschaltetem Display und wenn die App nicht im Vordergrund ist, soweit Android dies zulässt.

Die App darf den laufenden Rolling-Dienst nicht allein deshalb beenden, weil die Activity geschlossen wird.

## 12. Verhalten bei Geräte-Neustart und Dienstabbruch

Die Einstellung `Rolling-Buffer aktiv` wird persistent gespeichert.

Nach einem Geräte-Neustart soll die App versuchen, den Rolling-Dienst wieder zu aktivieren, soweit die jeweilige Android-Version und deren Hintergrund-/Mikrofonregeln dies zulassen.

Kann Android den Mikrofon-Foreground-Service nicht automatisch aus dem Hintergrund starten, wird stattdessen eine Benachrichtigung angeboten, über die der Benutzer den Rolling-Buffer mit einer sichtbaren Benutzeraktion wieder starten kann.

Wird der Dienst unerwartet beendet, gilt dieselbe Strategie:

1. automatische Wiederherstellung, wenn Android dies zulässt,
2. andernfalls Benachrichtigung `Rolling-Buffer gestoppt – tippen zum Fortsetzen`.

Nach einem echten Neustart des Recorders beginnt der RAM-Puffer wieder bei 0 Minuten.

## 13. Einstellung

Im Einstellungs-Panel wird zunächst genau eine neue persistente Option ergänzt:

**30-min Rolling-Buffer** `Ein/Aus`

`Ein` startet bzw. aktiviert den Rolling-Dienst. `Aus` beendet ihn und verwirft den aktuell noch nicht gesicherten RAM-Puffer.

Bereits gespeicherte Rolling-Dateien werden beim Deaktivieren nicht gelöscht.

Es werden zunächst keine gesonderten Einstellungen für Dauer, Gain, Quelle, Frequenzbereich oder Dateiformat des Rolling-Buffers eingeführt.

## 14. Änderungen vorhandener Audioeinstellungen während aktivem Rolling-Buffer

Da Rolling-Buffer und normale Aufnahme denselben Audio-Stream verwenden, können Änderungen an Audioquelle oder physischem Eingang nicht beliebig im laufenden `AudioRecord` übernommen werden.

Für die erste Version gilt daher die einfache und reproduzierbare Regel:

- Wird eine Audioeinstellung geändert, die ein Neuöffnen von `AudioRecord` erfordert, wird der Rolling-Recorder kontrolliert neu gestartet.
- Der bis dahin **nicht gesicherte** RAM-Puffer wird dabei verworfen.
- Bereits gespeicherte Rolling-Dateien bleiben erhalten.
- Gain-Änderungen werden entsprechend der vorhandenen Signalverarbeitung übernommen; falls die konkrete Implementierung den Gain vor dem gemeinsamen Ringpuffer anwendet, beginnt ab der Änderung ein entsprechend gemischter Puffer. Bevorzugt soll der Gain deshalb für einen laufenden 30-Minuten-Snapshot konsistent behandelt und bei erforderlicher Signalpfadänderung der Puffer ebenfalls neu begonnen werden.

Diese Regel wird in der UI nachvollziehbar protokolliert bzw. angezeigt, damit ein Puffer-Reset nicht unbemerkt bleibt.

## 15. Zusammenspiel mit der normalen 120-s-Aufnahme

Wenn der Rolling-Dienst aktiv ist, wird eine normale 120-s-Aufnahme aus demselben laufenden PCM-Strom erzeugt.

Daraus folgen folgende Anforderungen:

- kein zweiter gleichzeitiger `AudioRecord`,
- keine Unterbrechung des Rolling-Buffers,
- normale 120-s-Datei behält ihren bestehenden Kommentar-/Upload-/24-h-Ablauf,
- normale 120-s-Datei darf weiterhin zu Supabase übertragen werden,
- Rolling-Dateien bleiben davon vollständig getrennt.

Wenn der Rolling-Dienst nicht aktiv ist, bleibt die bisherige 120-s-Aufnahme funktionsfähig.

## 16. Fehlerverhalten

Der Rolling-Dienst muss Fehler sichtbar und nachvollziehbar behandeln.

Mindestens zu unterscheiden sind:

- Mikrofonberechtigung fehlt,
- ausgewählte Audioquelle nicht verfügbar,
- ausgewähltes physisches Gerät nicht mehr vorhanden,
- `AudioRecord` kann nicht gestartet werden,
- Dienst durch Android beendet,
- lokales Schreiben der WAV fehlgeschlagen,
- JSON konnte nicht geschrieben werden,
- Löschvorgang nur teilweise erfolgreich.

Ein fehlgeschlagenes Speichern darf den laufenden Ringpuffer möglichst nicht beenden.

Es dürfen keine geheimen Schlüssel oder Kommentarinhalte in Debug-Logs geschrieben werden.

## 17. Selbsttests und Regressionstests

Die Erweiterung wird mit PASS/FAIL-Tests abgesichert. Mindestens vorgesehen sind:

- Ringpuffer wächst bis maximal 30 Minuten,
- nach >30 Minuten sind exakt die neuesten Samples enthalten,
- Snapshot vor 30 Minuten enthält nur die tatsächlich vorhandene Dauer,
- Snapshot bei vollem Buffer enthält exakt 30 Minuten,
- Wrap-around-Reihenfolge ist korrekt,
- Bewertung `0..5` innerhalb 30 Minuten wird korrekt gewählt,
- `-1` wird ausgeschlossen,
- ohne gültige Bewertung entsteht `RX`,
- Rolling-Dateien werden nicht dem Supabase-Upload zugeführt,
- Rolling-Dateien unterliegen nicht der 24-h-Löschung,
- langer Druck löscht nach Bestätigung WAV und JSON gemeinsam,
- Abbruch der Löschbestätigung löscht nichts,
- normale 120-s-Aufnahme verwendet bei aktivem Rolling denselben Stream,
- Deaktivieren verwirft nur den ungesicherten RAM-Puffer,
- gespeicherte Dateien bleiben nach Deaktivieren erhalten,
- Wasserfall akzeptiert Rolling-WAVs bis 30 Minuten,
- vorhandene normale Audio-/Wasserfall-Funktionen bleiben als Regressionstests erhalten.

## 18. Nicht Bestandteil der ersten Version

Bewusst nicht vorgesehen sind zunächst:

- eigene Rolling-Buffer-Sample-Rate,
- eigene Rolling-Buffer-Verstärkung,
- eigene Rolling-Buffer-Audioquelle,
- frei einstellbare Pufferdauer,
- permanentes Segmentieren auf Flash,
- Cloud-Upload der Rolling-Dateien,
- Supabase-Datenbankeinträge für Rolling-Aufnahmen,
- separates Spektrogramm-Verfahren,
- Wiederherstellung eines ungesicherten Puffers nach Prozess-/Geräte-Neustart.

## 19. Versionsziel

Diese Spezifikation beschreibt das nächste Rolling-Buffer-Subsystem nach LFN-Scout v0.4.51. Die konkrete Versionsnummer der Implementierung wird im Implementierungsplan festgelegt.
