# LFN-Scout – STAT120: gemeinsamer Audio-/Statistik-Save für Beobachter und Analyzer

**Status:** verbindlicher Änderungswunsch / technische Spezifikation, **nicht implementiert**. Bezug: übernommener Quellstand v0.5.17, zuvor definierter Parameterkatalog A–H (2–25 Hz / 120 s). Keine Aussage über erfolgreichen Android-Build oder Geräteprüfung für diese Erweiterung.

## 1. Ziel und zwei Auslöser

### 1.1 Beobachterpanel (`OBSERVER`)
- Die bereits vorhandene, explizite 120-Sekunden-Audioaufnahme wird vollständig beendet und lokal gesichert.
- Unmittelbar anschließend werden die Statistiken **aus exakt diesen 120 Sekunden Audiodaten** berechnet und zusammen mit der Aufnahme über den gemeinsamen Speichervorgang nach Supabase übertragen.
- Die Daten enthalten **keinen Analyzer-Setup-Snapshot** und keine erfundenen Analyzer-Einstellungen. Technisch notwendige, tatsächlich wirksame Berechnungsparameter (Analyseband 2–25 Hz, Verfahren, Filterversion, Pegelbasis/Kalibrierung, Epochenlänge, Qualität) sind *gemeinsame Analyse-Metadaten* und bleiben enthalten.
- Vorhandene Beobachtungs-/Teilnehmer- und Aufnahme-Metadaten, einschließlich Kommentaren, bleiben dem Vorgang zugeordnet. Bestehende Freigabe-/Timeout-Logik des Kommentars berücksichtigen.

### 1.2 Analyzerpanel (`ANALYZER`)
- Neue, noch zu gestaltende Taste **Save Audio**. Es gibt genau zwei zulässige Abläufe:
  1. **RUN → SAVE AUDIO:** Zum Tastendruck wird ein unveränderlicher Snapshot der letzten **120 vollständigen, zusammenhängenden Sekunden** des nutzbaren Audiosignals erzeugt; Analyse und Anzeige laufen weiter.
  2. **RUN → PAUSE → SAVE AUDIO:** Beim Druck auf PAUSE wird ein Snapshot derselben 120 Sekunden eingefroren; ein späterer Druck auf SAVE AUDIO verwendet **diesen eingefrorenen Snapshot**, nicht die inzwischen nachgelaufenen Samples. Die aktuelle PAUSE-Funktion friert bislang nur die Analyzer-Anzeige ein: Einfrieren und Halten der Audiodaten ist deshalb eine neue Funktion.
- Die Taste wird nur freigegeben, wenn ein gültiger, lückenloser 120-Sekunden-Snapshot vorhanden ist, das Tageskontingent nicht erreicht ist, keine unvereinbare laufende Speicheroperation stattfindet und eine Speicherung im gewählten Audiomodus technisch möglich ist.
- Der Analyzer speichert zusätzlich einen **Snapshot der tatsächlich wirksamen LIVE-Analyzer-Einstellungen** mit Setup-ID/-Name, Modus und Gültigkeits-/Herkunftsangaben; *nicht* allein die Werte eines gespeicherten Setup-Slots.
- SAVE AUDIO ist kein STOP; die Analyzer-Session, andere Panels, laufende Auswertung und Audioerfassung dürfen durch Aufbereitung/Upload nicht gestoppt werden. Bei PAUSE wird nur die definierte Anzeige/Analyse eingefroren; die tatsächliche Wirkung auf die weiteren Audiopfade ist bei Implementierung explizit festzulegen und zu testen.

## 2. Exakte Aufnahmesemantik

- Die **Dauer des gespeicherten Audiosegments** beträgt exakt 120 s, bemessen an tatsächlichen Audioframes (`sampleCount / sampleRate`); weder UI-Wasserfall-Historie noch die reine Uhrzeit reicht als Nachweis aus.
- `audio_start_utc`, `audio_end_utc`, Sample-Zähler, Abtastrate, Quelle, Geräte-/Ratenwechsel, Signalunterbrechungen und Clipping werden zum Snapshot eingefroren.
- Wenn vor 120 s ein Eingangs- oder Moduswechsel, ein Stopp oder eine Lücke liegt: kein gültiger Snapshot. Die Freigabe beginnt erst nach 120 s kontinuierlicher Daten erneut.
- Zwei schnelle Tastendrücke auf denselben Snapshot dürfen keinen Doppel-Upload oder doppelte Kontingentbelastung erzeugen. Nach einem SAVE aus PAUSE ist derselbe eingefrorene Snapshot nicht erneut speicherbar; erst ein neuer Zeitabschnitt erlaubt eine neue Aufnahme.
- Ein laufender SAVE während RUN muss Daten kopieren/pinnen, bevor der Ringpuffer sie überschreibt; langsame Signalverarbeitung und Netztransport erfolgen außerhalb des Audiothreads.

## 3. Gemeinsamer Statistik-Datenvertrag

Grundlage sind **alle bisher spezifizierten Gruppen A–H**:
- A: Aufnahmeidentität, Audioformat, Zeitfenster, Kalibrierung, Aufnahmequalität;
- B: tatsächlich wirksame Signal-/Analyse-/Filterparameter; `analyzer_setup` nur für `ANALYZER`;
- C: Gesamtpegel- und Verteilungsstatistik 2–25 Hz;
- D: feste Frequenzbänder **und** FAST-Filterbänder, jeweils mit Grenzfrequenzen, Pegelverteilungen, zeitlicher Aktivität und FAST/SLOW-Ergebnissen, sofern aus dem zugehörigen Signalpfad verfügbar;
- E: Spektralstatistik, dominante Frequenzen, Persistenz/Drift;
- F: Impulsstatistik und Ereignisliste, sofern Algorithmus implementiert und gültig;
- G: Modulation/Periodik/Trend;
- H: Datenqualität/Fehler/Gültigkeitsflags.

**Keine vorgetäuschten Werte:** Nicht implementierte oder bei diesem Aufnahmemodus nicht verfügbare Statistiken sind `null`/`NOT_AVAILABLE`, nicht `0`; die Paketvollständigkeit ist davon unabhängig, ob optionale Methoden vorhanden sind. `analysis_schema_version`, `algorithm_versions` und ein anwendbarer `features_available`-Status machen Ergebnisse vergleichbar. Die feste 2–25-Hz-Statistik verwendet eine dokumentierte gemeinsame, reproduzierbare Referenzkonfiguration. Nur für den Analyzer kommt die effektive Analyzer-Konfiguration als zusätzliches Feld hinzu. Der Beobachter erhält keine künstliche oder verborgene Analyzer-Abhängigkeit.

**Vorsicht FAST:** Gegenwärtig laufen FAST-Filter am 8-kHz-Rohsignal und die WAV-Ablage bei 1 kHz. FAST-Statistiken dürfen nur dann als zur Aufnahme gehörig abgelegt werden, wenn ihre Zeitfenster und Signalherkunft exakt übereinstimmen. Falls der gewählte Direkt-Analyzer-Modus keinen geeigneten 120-s-Audio-Ring oder keinen Rohpfad für FAST bereitstellt, ist dies vor dem Save zu beheben oder die entsprechenden Merkmale als nicht verfügbar zu markieren; keine zweite konkurrierende Mikrofoninstanz öffnen.

## 4. Gemeinsames Kontingent

- **Eine Tagesquote für beide Save-Einstiege zusammen**, Teilnehmer/Installation und lokales Kalendertag-Kriterium explizit dokumentieren; Wert aus den vorhandenen Einstellungen übernehmen, soweit dieselbe Policy gelten soll (bestehender Standard 10/Tag, zulässige Konfiguration 1–100/Tag).
- Tageszähler wird nicht beim bloßen Drücken und nicht erst beim erfolgreichen Netztransfer belastet, sondern genau einmal beim **erfolgreichen lokalen, dauerhaften Anlegen** eines neuen vollständigen Aufnahme-/Statistikpakets. Vor dem Anlegen atomare Prüfung/Reservierung (auch bei gleichzeitigen Aufrufen aus beiden Panels). Reservierungen bei Fehler zurückgeben.
- Offline-Pakete zählen zur Quote. Wiederholter Upload derselben `recording_id` zählt nicht erneut. Tageswechsel, Uhrzeit-/Zeitzonenwechsel und lokale Datenbankwiederherstellung dürfen das Kontingent nicht unbemerkt umgehen; bei serverseitigen Quoten ebenfalls serverseitig validieren.
- Nicht erfüllte Bedingungen: Taste sichtbar, aber deaktiviert, mit unterscheidbaren Gründen `BUFFER < 120 s`, `TAGESLIMIT`, `QUELLE NICHT VERFÜGBAR`, `SAVE LÄUFT`; vorhandene Informationen zur verbleibenden Zeit und Quote anzeigen.

## 5. Gemeinsame Pipeline und Upload

```
Observer: abgeschlossene 120-s-WAV ─┐
                                    ├→ AudioSaveRequest(recording_id, origin, immutable 120-s audio,
Analyzer: RUN-Snapshot / PAUSE ─────┘                    capture metadata, optional analyzer setup,
                                                       optional aligned FAST features)
 → validate → local durable package (WAV + statistics JSON, same recording_id)
 → common Statistics120Engine → common UploadQueue → existing/private Supabase audio storage
 → linked recording/statistics/bands/events → server verification → COMPLETE
```

- Ein **gemeinsamer Kern** für Fenster/Statistik, Versionierung, Validierung, lokale Ablage, Upload-Retry, Status und Idempotenz; die beiden Panels liefern nur unterschiedliche Auslöser und Metadaten.
- Der gemeinsame Kern akzeptiert von Observer die bereits fertig aufgenommene Datei und von Analyzer einen eingefrorenen Ring-/Direct-Stream-Snapshot; er steuert nicht selbständig eine zweite Mikrofonaufnahme.
- Upload per derselben `recording_id` und eindeutigem `origin=OBSERVER|ANALYZER`; Analyzer-Setup-Objekt nur bei `ANALYZER`. Aufnahme und Statistik im Supabase-Schema verknüpfen. Private Storage-/RLS-Grenzen, Datenminimierung und die bestehenden Recovery-/Backup-Jobs beibehalten und erweitern.
- Eine nicht erfolgreich übertragene lokale Aufnahme **niemals wegen Netzfehler löschen**; Status `LOCAL_READY`, `UPLOAD_PENDING`, `COMPLETE`, `FAILED_RETRYABLE` bzw. `INVALID` sauber trennen.
- Bestehende separate Rolling-Save-Funktion speichert derzeit lokal und ist nicht im Cloud-Upload enthalten; sie nicht stillschweigend mit dieser neuen Save-Audio-Aktion gleichsetzen. Das Verhältnis zu weiteren Rolling-Saves muss vor Implementierung der gemeinsamen Tagesquote ausdrücklich geregelt werden.

## 6. Prüfungen und Definition of Done

1. Observer: 120 s vollständig → gleicher Samplezeitraum für WAV und Statistik; Analyzer-Felder fehlen; gemeinsame Cloud-Zuordnung.
2. Analyzer RUN: Taste vor 120 s gesperrt, bei 120 s frei; Druck friert die letzten 120 s ein, während Analyzer läuft.
3. Analyzer PAUSE: Snapshot wird genau beim PAUSE-Tastendruck eingefroren; beliebige Wartezeit vor SAVE ändert Inhalt/Start-/Endzeit nicht.
4. Vorzeichen/Zeiten: nach STOP, Quellen-/Ratenwechsel, Audiolücke und Neustart erst wieder 120 s gültiger Puffer.
5. Tageslimit gilt **panelübergreifend**, auch bei gleichzeitigem Observer-/Analyzer-Save und Offline-Speicherung; Netz-Retry erzeugt keine neue Tageszählung.
6. Nicht verfügbare FAST-/Impuls-/Modulationsparameter `null` mit erklärtem Status; keine erfundenen Analyzer-Setup-Werte im Observer-Paket.
7. Audioeinheit, Kalibrierstatus, 1-kHz-/8-kHz-Abgleich und Filterkoeffizienten-/Versionsreferenz sind nachvollziehbar.
8. Bei fehlendem Internet, abgebrochenem Upload und App-Neustart bleiben WAV/Statistik gekoppelt und erneut sendbar; Doppeleinträge werden verhindert.
9. Rolling/Direct-Audio-Exklusivität, Panelwechsel, Portraitmodus und bereits bestätigter v0.5.17-Funktionsstand regressionsprüfen.
10. Separate Verifikation: Quellcode vorhanden ≠ statisch geprüft ≠ Gradle-Build bestanden ≠ Xiaomi-Test bestanden ≠ Supabase-End-to-End bestätigt.

**Nicht ohne weiteres entschieden:** genaue Anordnung/Beschriftung der Save-Audio-Taste im Analyzer, ob der bestehende unabhängige 30-min-Rolling-Save zusätzlich in die gemeinsame Tagesquote aufgenommen werden soll, Serverquote bei Mehrfachgeräten. Bis diese Punkte geklärt sind, keine implizite Änderung bestehender Rolling-Save- oder Observer-Verfahren.
