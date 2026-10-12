# LFN-Scout v0.5.04 – Analyzer: Betriebsarten, 48 kHz, Testquelle, Langzeitwasserfall

**Datum:** 21.09.2026 · **Quellcode:** `versionName 0.5.04`, `versionCode 145`, `applicationId de.brummbeobachter.app`.
**Nachweise:** Host-Core- und statische Prüfungen; **Android-/Compose-Build und Xiaomi-Praxisprüfung noch NOT RUN**. v0.5.02 vom Nutzer vollständig abgenommen, v0.5.03 nicht vollständig abgenommen (16:1-Pegelverlust gemeldet).

## 1. Verbindliche Sitzung und Audio-Zustände

Beim *ersten* Analyzer-Start in einer neuen Sitzung wird `rollingBufferEnabled` gesichert; wählbar sind:

| Modus | Audioquelle | Rolling während Sitzung | Analyzer |
|---|---|---|---|
| JA: 8 kHz | bestehender Rolling-Dienst, 8-kHz-PCM-Hub | EIN, falls zuvor AUS temporär einschalten | FIR 4/8/16 und LF-FFT |
| NEIN: 48 kHz | exklusiver 48-kHz-AudioRecord-Foreground-Service | AUS, alter Rolling-Puffer darf verloren gehen | 1:1, breitbandige FFT |
| Digitaler Test 8/48 kHz | rein synthetisches PCM zum Analyzer-Hub | AUS | deutlich `TESTSIGNAL · KEINE MESSUNG` |

**STOP:** nur Analyzer-Berechnung und ggf. exklusive 48-kHz-Quelle/Testquelle stoppen. Die gewählte **Betriebsart und Rolling-Einstellung bleiben unverändert**. Erneuter START startet dieselbe Quelle ohne erneute Abfrage. Ein Panelwechsel **bei laufendem** Analyzer stoppt nichts. **Nur bei gestopptem Analyzer + Verlassen des Panels** wird die gesicherte Rolling-Einstellung wiederhergestellt; falls vorher aktiv, wird Rolling mit neuem, leerem Ringpuffer neu gestartet. `BackHandler` und UI-Linkspfeil sollen dieselbe Regel umsetzen. Bei Startfehler soll der Ausgangszustand wiederhergestellt werden. Nach Prozess-Neustart wird ein persistierter Restore-Auftrag abgearbeitet.

**Wichtige Abgrenzung:** Der vorhandene Rolling-Dienst puffert fortlaufend PCM im RAM. Er schreibt **nicht automatisch eine durchgehende WAV-Datei auf Disk**. Normale, separat ausgelöste WAV-Aufnahmen sind ein anderer Pfad. Eine vollständige automatische WAV-Daueraufzeichnung beim JA-Modus ist in dieser Version **nicht implementiert und nicht als freigegeben anzusehen**. Beim Abschalten dürfen ungesicherte Rollingaudiodaten verloren gehen, bereits gespeicherte Dateien dürfen nicht gelöscht werden. Vorgeschalteter Recorder muss frei sein; MicCaptureArbiter verhindert einen zweiten in-process AudioRecord-Owner.

## 2. Separates Breitband-Setup

Nur für 48-kHz-Start werden die aktuell aktiven LF-Einstellungen in `mode_8_*` gesichert und `mode_48_*` geladen. Bei erstmaliger Wahl entstehen die Defaults 48 kHz, Decimation 1:1, 0–22000 Hz, FFT 8192, Hop 4800 (=100 ms), Hann und 120-s-Wasserfall. Unter Setups steht der zusätzliche separate Slot 4 **Analyzer 48 kHz** mit Name/Beschreibung zur Verfügung; Slots 1–3 bleiben erhalten. FFT 16384 verfügbar (bei 48 kHz Δf ≈2,93 Hz, Fenster ≈341 ms). Vor dem Restore wird das aktive 48-kHz-Profil gespeichert. Das LF-Profil bleibt nur während der Sitzung ersetzt.

## 3. Langer Wasserfall

`historySeconds` jetzt frei **10–14400 s**, einschließlich 3600/7200 s. Die Zeilenintervalle 100…20000 ms werden auf ein höchstens 2400-Zeilen-Limit angepasst. Der Analyzer-FFT-Hop ändert sich dabei nicht. 48-kHz-Wasserfallzeilen werden auf 513 Frequenzwerte für bis zu 22 kHz speicherbegrenzt verdichtet; dabei wird das Binmaximum jedes zusammengefassten Frequenzabschnitts gespeichert. Diese Frequenzverdichtung ist nicht identisch mit einer hochauflösenden FFT-Historie. Im Langzeitwasserfall können Einzelereignisse unterhalb der Zeilen- und Bildschirmauflösung nicht einzeln dargestellt werden. Ein ZIP-/CSV-Rohspektrenarchiv wurde **nicht** implementiert.

Der Wechsel `Akt/MW/MAX` löscht die vorhandene Historie nicht mehr; nur die neuen Zeilen verwenden den neuen Modus. Die neue Modus-Grenze wird in UI und PNG/JPG markiert, die bestehende Legende bezeichnet jeweils den aktuellen Modus. Beim Wechsel von FFT-Größe, Hop/Zeilenintervall oder Audioquelle wird die Historie weiterhin entsprechend der gültigen Zeitbasis zurückgesetzt; kein unbegründetes lückenloses FFT-Versprechen.

## 4. Testgenerator und Bildspeicherung

Alternativ zu Mikrofonmodi: digitaler Sinus 50 Hz, optional zweiter Sinus 195 Hz; Frequenzen/−100…−1 dBFS-Pegel in `Einstellungen → Audio` editierbar. Bei zwei Sinussen wird der eingestellte digitale Amplitudenfaktor halbiert, um Clipping zu vermeiden. Neu übernommene Werte starten die Testquelle neu. Der Testgenerator publiziert ausschließlich an den Analyzer-Roh-PCM-Hub, nicht in den Rolling- oder WAV-Pfad. TESTSIGNAL-Kennzeichnung auch im Offscreen-Bild und Dateinamen. Diese Werte sind digitale dBFS, keine kalibrierten akustischen SPL-Werte.

Kamera-Langdruck löst unverändert nur einen Export aus. `Bild wird gespeichert …` entwertet den vorigen Erfolgsstatus; nach erfolgreichem Dateischreiben zeigt die UI ca. 2 s `Bild gespeichert: PNG/JPG`, bei Fehler entsprechende Fehlermeldung. Das reale MediaStore-/JPEG-Verhalten bleibt am Gerät zu testen.

## 5. Offene Punkte und Einschränkungen

- **Decimation 16:1**: vom Nutzer gemeldeter Energie-/Pegelverlust, Ursache unbekannt; isolierte frühere FIR-Hosttests sind kein akustischer Nachweis.
- **Audio-Übergänge**: nicht auf Xiaomi getestet; Android-Foreground-Service-/Berechtigungsfälle, STOP + Navigation, Fehlerfall und Boot/Process-Death müssen getestet werden. Realisierte 48-kHz-Rate auf Gerät und Mikrofonfrequenzgang unabhängig prüfen.
- **48-kHz-Statistiken**: derzeit weiterhin auf den vorhandenen frei wählbaren **0–200-Hz-Statistikbereich** beschränkt, nicht automatisch 0–22 kHz.
- **Keine automatische WAV-Daueraufzeichnung** durch Rolling und kein verlustfreier Übergang zu 48 kHz; alte RAM-Historie darf verloren gehen.
- 16-k FFT/48-kHz-CPU-Last, Drop-Rate, Dauerbetrieb und Bildexport unter Belastung nicht nachgewiesen.
- Mithören, Superfast, Spektrum-Max-Hold, Radiushistogrammspalte und Supabase-Restore auf neues leeres Projekt zurückgestellt.

## 6. Prüfverträge

`tools/RUN_ANALYZER_V0504_SELFTEST.sh` prüft ausschließlich **JVM-Analyzer-Core**; `python tools/verify_v0504_source.py` prüft **statische App-Quellcode-Verträge**. Keiner von beiden kompiliert Compose oder führt echte Audio-Zustandsübergänge auf Android aus. Der ausführliche Geräteplan ist `docs/project/TESTPLAN_v0.5.04.md`. Buildwege unverändert laut `BUILD_WORKFLOWS.md`.
