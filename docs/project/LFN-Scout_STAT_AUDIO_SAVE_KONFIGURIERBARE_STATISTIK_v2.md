# LFN-Scout – gemeinsamer Audio-/Statistik-Save: konfigurierbare Dauer, Analysefrequenzen und maximal 40 Filter

**Dokumentversion:** 2 · **Status:** verbindliche Anforderung / technische Spezifikation, **noch nicht implementiert**. Ausgangsstand: LFN-Scout v0.5.17; Ergänzung und Präzisierung von `LFN-Scout_STAT120_GEMEINSAMER_AUDIO_SAVE_Anforderung_v1.md`. Diese Dokumentversion ist **keine neue App-Version und kein Nachweis eines Android-Builds**.

## 1. Neue verbindliche Festlegungen

1. Die bisher festen **120 Sekunden** werden durch die konfigurierbare **Audio-/Statistikfensterdauer `save_duration_s`** ersetzt; **Default: 120 s**. In beiden Panels bezieht sich der Speichervorgang auf exakt diese Dauer und die Statistik auf exakt dieselben Audiosamples. Die Anzeigehistorie des Wasserfalls ist unabhängig davon.
2. Der bisher feste statistische Gesamtbereich **2–25 Hz** wird durch die unabhängig konfigurierbaren Grenzen `statistics_min_hz` und `statistics_max_hz` ersetzt; **Defaults: 2 Hz und 25 Hz**. Die Grenzen gelten für Gesamtstatistik, daraus abgeleitete Statistikspektren und gegebenenfalls eigene feste Statistikbänder.
3. **Filterstatistiken** richten sich dagegen nach der **tatsächlich wirksamen Filterbank-Konfiguration** (`filter_min_hz`, `filter_max_hz`, `filter_bands`, effektive Bandgrenzen und Filterparameter). Es gibt **keine automatische Beschneidung auf den allgemeinen Statistik-Frequenzbereich**. Maximal **40 Filterbänder** können berücksichtigt und samt Einzelstatistiken gespeichert werden; eine kleinere konfigurierte Bandzahl führt zu genau dieser Anzahl.
4. Diese drei Konfigurationsgrößen sind unabhängig: Die Statistikbereichsgrenzen ändern weder Filterbankbereich noch Bandzahl; Filterbankänderungen ändern nicht automatisch den Gesamtstatistikbereich; die Fensterdauer ändert keine Frequenzgrenzen.
5. Beide Panels verwenden eine **gemeinsame Statistik- und Save-/Upload-Pipeline**. Nur beim Analyzer kommt ein Snapshot des tatsächlich wirksamen Analyzer-Setups hinzu. Im Beobachterpanel werden **keine Analyzer-Setup-Informationen erfunden oder angehängt**; die technischen Parameter der gemeinsamen Statistik und, soweit vorhanden, des tatsächlich verwendeten Filterpfads werden trotzdem dokumentiert.
6. Eine Änderung von `save_duration_s` oder einer wirksamen Signalkonfiguration während RUN/PAUSE erzeugt **keinen nachträglich gemischten Snapshot**. Für einen neuen Speichervorgang muss ein ausreichend langes, lückenloses Segment mit konsistenter, versionierter Konfiguration vorliegen. Der PAUSE-Snapshot bleibt unverändert, auch wenn anschließend ein Setup geändert wird.

## 2. Bedien- und Einstellparameter

| Parameter | Bedeutung | Default / Grenze |
|---|---|---|
| `save_duration_s` | Dauer der aufzunehmenden / zu speichernden WAV und ihres Statistikzeitfensters | **120 s**; weitere zulässige Min-/Max-/Schrittwerte **vor Implementierung festzulegen**, abhängig von verfügbarem Puffer, Speicher und Rechenzeit. |
| `statistics_min_hz` | Untergrenze der gemeinsamen bandbegrenzten Gesamtstatistik | **2 Hz** |
| `statistics_max_hz` | Obergrenze der gemeinsamen bandbegrenzten Gesamtstatistik | **25 Hz** |
| `statistics_epoch_s` | Zeitraster für Pegelpercentile und Schwellenüberschreitung | Vorschlag: **1 s**; in der Methode versionieren. Kein Ersatz für schnelle Impulsauswertung. |
| `filter_min_hz` / `filter_max_hz` | Separater Bereich der Filterbank | Werte aus dem **effektiven Filterbank-Setup**; nicht aus `statistics_min_hz/max_hz` ableiten. |
| `filter_bands` | Anzahl tatsächlich aktivierter Filter | Aus Filterbank-Setup, **maximal 40**; bestehender 10-/20-Band-Anwendungsfall bleibt zulässig. |
| `daily_save_limit` | Gemeinsame Tagesquote für Observer und Analyzer | Vorhandener Default **10**, bisheriger Einstellbereich **1–100**; Policy und Speichervorgänge panelübergreifend berücksichtigen. |

**Validierung:** `statistics_min_hz < statistics_max_hz`, gültige untere Grenze über DC, obere Grenze kleiner als die für den verwendeten Signalpfad zuverlässig nutzbare Nyquist-/Übertragungsgrenze. Reine rechnerische Nyquist-Konformität belegt noch keine gültige Mikrofonkalibrierung. Die Filterbank muss ihre Grenzen, Filterstabilität, Einschwingzeit, effektive Bandkanten und die technische Abtastrate **unabhängig** validieren. Ungültige Konfiguration darf nicht stillschweigend angepasst oder mit scheinbar gültigen Ergebnissen hochgeladen werden. Falls nur ein Teilbereich messbar ist: tatsächlichen effektiven Bereich und Qualitätsstatus speichern oder den Save begründet blockieren.

**Noch offen:** Konkrete zulässige Dauergrenzen, GUI-Bedienelemente, Pufferkapazität und ggf. Vorlauf für Filterwarmup. Keine willkürliche maximale Aufnahmedauer als bereits beschlossen ausgeben.

## 3. Zwei Auslöser – ein Datenvertrag

### 3.1 Beobachterpanel (`OBSERVER`)

- Die vorhandene automatische 120-s-Aufnahme wird für die neue, einstellbare Dauer `save_duration_s` erweitert. Bei Ablauf werden WAV und zugehörige Statistik für **denselben exakten Aufnahmezeitraum** lokal als zusammengehöriges Paket gesichert und nach Supabase übertragen.
- Speicherdatensatz: Aufnahme-, Teilnehmer- und Beobachtungsmetadaten, `statistics_min_hz/max_hz`, tatsächliche Auswertemethode/Kalibrierung, Statistikgruppen A–H, gültige Filterstatistiken **nur wenn ein zeitlich zugehöriger Filterpfad tatsächlich verfügbar ist**. **Kein `analyzer_setup`-Objekt.**
- Das gemeinsame Tageslimit bleibt wirksam. Ein fehlender Analyzer-Setup-Snapshot darf eine Beobachteraufnahme **nicht** ungültig machen.

### 3.2 Analyzerpanel (`ANALYZER`)

- Neue Taste **SAVE AUDIO**. Sie wird nur freigegeben, wenn ein gültiger, zusammenhängender Audiobuffer von mindestens `save_duration_s` verfügbar ist, die Tagesquote nicht erreicht ist, die Quelle save-fähig ist und kein unvereinbarer Speichervorgang läuft.
- **RUN → SAVE AUDIO:** unveränderlicher Snapshot der letzten `save_duration_s` unmittelbar vor dem Tastendruck; laufende Audioerfassung, Spektrum und Wasserfall werden nicht gestoppt.
- **RUN → PAUSE → SAVE AUDIO:** bereits beim PAUSE-Druck Snapshot der letzten `save_duration_s` einfrieren. Ein späterer SAVE verwendet nur diesen Snapshot und den dazugehörigen damaligen LIVE-Konfigurationsstand; kein Verschieben des Endzeitpunkts.
- Analyzer-Paket enthält zusätzlich `analyzer_setup` als **effektiven LIVE-Snapshot** mit Setup-ID/-Name, Modus, Herkunft, Verfügbarkeit und Algorithmusversionen; nicht bloß alte gespeicherte Slot-Werte.
- STOP, Quellen-/Ratenwechsel und Audiolücken setzen die Freigabe bis zu einem neuen vollständigen validen Fenster zurück. Nach einmaligem Save darf derselbe PAUSE-Snapshot nicht erneut als neue Aufnahme zählen.

### 3.3 Konfigurationsänderung

- In einem laufenden Segment dürfen alte/neue Bandgrenzen, Filteranzahl, Gain, Abtastrate oder andere für Statistik relevante Parameter nicht ohne Kennzeichnung zusammengeführt werden. Puffer neu qualifizieren oder getrennt segmentieren; ein Save benötigt **einen klar definierten gültigen Zustand**.
- Bei PAUSE werden Audio **und** Auswertungs-/Filterkonfiguration gemeinsam eingefroren. Späterer Nutzerwechsel der Einstellung verändert diesen Snapshot nicht.
- Statistikfenster und WAV-Dauer werden über Samplezählung (`sampleCount/sampleRate`) bestimmt, nicht aus Bildschirmhistorie oder allein aus UI-Zeitstempeln.

## 4. Gültigkeitsbedingungen und Tageskontingent

Deaktivierungsgründe: `BUFFER < save_duration_s`, `TAGESLIMIT`, `QUELLE NICHT VERFÜGBAR`, `SAVE LÄUFT`, `UNGÜLTIGE KONFIGURATION`. Restpufferzeit = `max(0, save_duration_s − contiguous_buffer_s)`.

- Gemeinsames Kontingent für `OBSERVER` und `ANALYZER`; genau einmal beim erfolgreichen **dauerhaften lokalen Anlegen eines vollständigen WAV-/Statistikpakets** zählen, nicht bei Tastendruck oder späterem Upload.
- Atomare Reservierung gegen parallele Saves; Fehler geben Reservierung zurück. Offline-Pakete zählen bereits, Netzwerk-Retries nicht erneut. Recording-ID bleibt über Retry und Supabase-Datensätze identisch.
- Lokaler Tageswechsel, Uhrzeit-/Zeitzonenwechsel, Wiedereinspielung und mehr als ein Gerät müssen im späteren Quotenkonzept berücksichtigt werden.
- Die bestehende separate 30-min-Rolling-Speicherung nicht ohne gesonderte Entscheidung in dieselbe Quote einbeziehen.

## 5. Gemeinsamer Statistik-Datenvertrag A–H

Alle Kennwerte werden aus dem **gesicherten Aufnahmezeitfenster `save_duration_s`** berechnet. Historische Namen `STAT120` und `Statistics120Engine` werden in der neuen Architektur durch generische Namen wie `StatisticsWindowEngine` ersetzt; vorhandener Code wird dafür erst bei Implementierung geändert. Bei fehlenden optionalen Analysen werden Status `NOT_AVAILABLE` und `null` gespeichert, nicht irreführende Nullen.

**A – Identität und Audio:** Recording-ID, Quelle (`OBSERVER`/`ANALYZER`), Teilnehmerreferenz, Start/Ende UTC, `configured_duration_s`, `actual_duration_s`, WAV-Pfad/Hash/Größe, gespeicherte und Quell-Abtastrate, Kanäle, Bitauflösung, Audioquelle, Gain, Dezimierung, Clipping, Geräte-/App-Version, Analyse-/Schemasversion, Kalibrierung und Status.

**B – wirksame Konfiguration:** **Immer** `statistics_min_hz/max_hz`, `statistics_epoch_s`, tatsächliche Filter-/Signalverarbeitungsverfahren, Fenster, FFT-Konfiguration, Kalibrierungs-/Pegelbasis, Algorithmusversionen und Verfügbarkeitsflags; dazu `filter_min_hz/max_hz`, `filter_bands` und effektive Kanten jedes tatsächlich verwendeten Filters. **Nur bei `ANALYZER`** das gesonderte `analyzer_setup`-Objekt mit tatsächlichen LIVE-Werten; für `OBSERVER` Feld weglassen, nicht mit fingierten Defaults füllen.

**C – Gesamtstatistik innerhalb `statistics_min_hz..statistics_max_hz`:** Leq, Lmin, Lmax, L1, L10, L50, L90, L95, L99, Mittelwert und Standardabweichung der zeitbewerteten Pegel, Dynamik, L10−L90, Schwelle, Dauer/Anteil oberhalb Schwelle, Crest-Faktor, Exzess-Kurtosis. Leq energetisch aus linearen Größen bilden; andere Kenngrößen mit dokumentiertem Zeitraster und Signalpfad.

**D – Bandstatistik:** Die bisher vorgeschlagenen **sechs festen 2–25-Hz-Bänder dürfen nicht mehr starr vorausgesetzt werden.** Optionale feste Statistikbänder müssen sich aus dem eingestellten Gesamtbereich ergeben und als separater Bandplan mit tatsächlichen Grenzwerten dokumentiert sein. Unabhängig davon **alle tatsächlich aktiven Filterbankbänder bis einschließlich 40** speichern, mit Band-ID/-Index/-Typ, nominalen und effektiven unteren/oberen und Mittenfrequenzen, Filterdesign/-ordnung/-Koeffizientenreferenz/-Abtastrate/-Laufzeit, Validität, Leq, Lmin/Lmax, L1/L10/L50/L90/L95/L99, Streuung, L10−L90, Schwelle/Zeit/Anteil oberhalb Schwelle, FAST-Peak, SLOW-Referenz, Peak-Überschuss, Crest-Faktor und Kurtosis, soweit methodisch verfügbar. Einzelne Werte erhalten bei fehlender Messbarkeit `null` samt Grund; kein vorgetäuschtes Vollspektrum aus Displaybalken. Die Filterband-Ergebnisse dürfen außerhalb des Gesamtstatistikbereichs liegen, **weil sie dem Filterbank-Setup folgen**.

**E – Spektralstatistik:** Frequenzvektor/PSD-Mittelwert/Median/Maximalwert/Streuung je Bin, Segmentanzahl, FFT-/Fenster-/Overlap-/Normierungsangaben, dominante Frequenzen mit Pegel, Persistenz und Drift. Gesamtbereich gemäß `statistics_min_hz/max_hz`; ein separates Filterbankspektrum braucht ausdrückliche Herkunftskennzeichnung.

**F – Impulse:** Anzahl/Rate, Spitzenpegel, Energie, Dauer, Anstiegs-/Abklingzeiten, Intervallstatistik, Bandbezug, Clipping und Gültigkeit sowie Ereignisliste mit Recording-ID. Vollständige schnelle Impulserkennung ist **geplant, noch nicht implementiert**; Filterringing darf keine künstlichen mehrfachen Ereignisse ergeben.

**G – Modulation:** dominante Modulationsfrequenz, -tiefe, Modulationsspektrum, Autokorrelationslag, Periode/Stabilität, Pegeltrend; Vergleich **zweier gleich langer Hälften von `save_duration_s`** statt fest 60/60 s. Zu kurze Aufnahme oder zu wenige Perioden als eingeschränkt/ungültig kennzeichnen.

**H – Qualität:** gültige Dauer, gültige Epochen, Audio-Lücken/Dropouts, Clipping, Filterwarmup, Frequenzgang-/Kalibrierstatus, Zeitstempelqualität, Verarbeitungslatenz, Fehlercodes und Abschlusszeit. Gültigkeit ist je Gesamtstatistik **und je Band** auszudrücken.

**Wichtig zum Signalpfad:** Die bisher gespeicherte WAV und die im 8-kHz-Rohpfad laufende FAST-Bank können unterschiedliche Abtastraten und Signalherkünfte besitzen. FAST-Statistik nur für exakt zeitlich zugeordnete Ergebnisse aufzeichnen; andernfalls `NOT_AVAILABLE`. Bei Bandspezifikation oberhalb der tatsächlichen WAV-Nutzbandgrenze darf nicht suggeriert werden, die Ergebnisse seien später aus der WAV reproduzierbar. Diese Herkunft und Einschränkung in Supabase speichern. Kein zweites konkurrierendes `AudioRecord` öffnen.

## 6. Supabase-Daten und atomarer Speicherablauf

Jedes Paket enthält eine `recording_id`, WAV im privaten Audio-Storage, gemeinsame Analysemetadaten und vollständige gültige Werte der Gruppen A–H. Das vorhandene `audio_recordings` kann um Felder erweitert werden; daneben sind versionierte `audio_analysis`, `audio_analysis_bands` (0 bis 40 FAST-Bänder plus ggf. separat gekennzeichnete feste Bänder) und `audio_impulse_events` zweckmäßig. Bandanzahl niemals auf 10 oder 20 fest kodieren; Upload/Schema, JSON-Serialisierung, SQL-Validierung und Export müssen **40 Filter** zulassen.

```
OBSERVER: WAV nach konfigurierter Dauer ─┐
                                        ├─> AudioSaveRequest(id, origin, unveränderliches Audio,
ANALYZER: RUN/PAUSE Snapshot ────────────┘                    effektive Metadaten, ggf. LIVE-Setup,
                                                              zeitlich zugehörige Filtermerkmale)
  -> validieren -> WAV + Statistik lokal dauerhaft sichern
  -> gemeinsame StatisticsWindowEngine -> Upload-Warteschlange
  -> Supabase WAV + Statistik + sämtliche verfügbaren Band-/Ereignisdaten
  -> serverseitige Zuordnung/Verifikation -> COMPLETE
```

`COMPLETE` nur nach überprüfbarer Speicherung aller verpflichtenden Teile; Upload in Teilschritten darf Zwischenstände, Retry und idempotente Upserts benutzen. Unvollständige Pakete nie bei Netzfehler löschen. Private Storage-/RLS-/Recovery-/Backup-Verfahren müssen das erweiterte Schema mitführen.

## 7. Verbindliche Regressionstests / Abnahme

1. **Defaultkompatibilität:** 120 s und 2–25 Hz führen zum bisherigen Standardverhalten, sofern der ursprüngliche Signalpfad verfügbar ist.
2. **Daueränderung:** Beispiel anderer konfigurierter Dauer: Observer-Aufnahme endet nach dieser Sampleanzahl; Analyzer-SAVE wird erst bei genügend zusammenhängenden Samples freigegeben; WAV und Statistik sind zeitlich deckungsgleich.
3. **PAUSE:** Audio- und Konfigurationssnapshot bleibt über Wartezeit und spätere Einstellungsänderung unverändert.
4. **Frequenzänderung:** Neue Gesamtstatistikgrenzen ändern C/E und optionale feste Statistikbänder, aber **nicht** die unabhängigen FAST-Filterbankkanten.
5. **Filteränderung:** FILTER MIN/MAX und Bandzahl bestimmen die gespeicherte FAST-Bandliste, unabhängig vom Gesamtstatistikbereich; nominale und effektive Kanten prüfen.
6. **Maximalfall:** 40 aktive Filter ergeben **40 unterschiedlich identifizierte, zugehörige Bandobjekte** ohne Abschneiden, Indexfehler, Payload-/Schema-Fehler oder UI-Absturz. Ebenfalls 10, 20 und kleinere zulässige Bandzahlen testen.
7. **Observer:** Enthält technische gemeinsame Analyse- und reale Filtermetadaten, aber **kein Analyzer-Setup**; Upload gültig.
8. **Analyzer:** Enthält wirksamen LIVE-Setup-Snapshot; RUN-SAVE unterbricht Analyzer nicht; Direct-/Rolling-Exklusivität, Panelwechsel und Portraitbetrieb bleiben intakt.
9. **Quote/Offline:** Limit panelübergreifend und atomar; mehrere schnelle Saves/Retries belasten nur einmal; Offline-Paket und Statistik bleiben erhalten.
10. **Messqualität:** Ungültiger Frequenzbereich, Quelle ohne nötige Bandbreite, unvollständige Dauer, Filterwarmup, FFT-Insuffizienz und Datenlücken werden korrekt abgelehnt bzw. als ungültig/nicht verfügbar ausgewiesen.
11. **Nachweis trennen:** Quellcode implementiert ≠ statisch geprüft ≠ Gradle-Build ≠ Xiaomi-Gerätetest ≠ Supabase-End-to-End-Test.

## 8. Noch zu entscheiden – nicht als implementiert ausgeben

- Zulässiger Wertebereich/Schrittweite der Aufnahme- und Statistikfensterdauer und benötigte Audiopuffergröße, einschließlich Untergrenze für stabile langsame Statistiken.
- Position und Darstellung der SAVE-AUDIO-Taste sowie Einstellorte für Dauer und Gesamtfrequenzbereich.
- Genaues Raster optionaler fester Statistikbänder bei frei wählbarem Statistikbereich; diese sind **nicht** mit den unabhängigen FAST-Filterbändern gleichzusetzen.
- Umgang mit einem Wechsel der Konfiguration mitten in längeren Audiopuffern; vorzugsweise neuen gültigen Segmentbeginn verlangen.
- Ob weitere getrennte Rolling-Speicheraktionen zur Tagesquote zählen und wie eine Mehrgeräte-Serverquote umgesetzt werden soll.

**Änderungsumfang:** Dies ist eine Spezifikation. Bestehende v0.5.17-App, Gradle-/APK- und Supabase-Datenstände werden hierdurch **nicht verändert**.
