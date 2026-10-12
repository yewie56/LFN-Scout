# LFN-Scout v0.5.27 – Windows-11-Build und Testanweisung

**Stand:** 10.10.2026. **Artefakt:** SOURCE CANDIDATE, keine freigegebene APK.

## 1. Voraussetzungen

- Windows 11; Internetzugang für fehlende Gradle-/Android-Abhängigkeiten.
- Installiertes Android Studio mit Android-SDK; Java/JDK gemäß `BUILD_WORKFLOWS.md` und `PREPARE_PROJECT.bat`.
- Android-Handy mit aktivierten Entwickleroptionen und USB-Debugging für den Gerätetest.
- Quell-ZIP in einen **neuen, leeren** Ordner entpacken, z. B. `C:\LFN-Scout\v0.5.27` (Dateien liegen direkt im ZIP-Wurzelverzeichnis).
- Bestehende App-Daten vor dem Test sichern. Paketname bleibt `de.brummbeobachter.app`.

## 2. Build ohne automatische Installation

1. Im Explorer in den entpackten Ordner wechseln.
2. Doppelklick auf `VORTEST.bat`.
3. Das Script bereitet das Projekt vor, führt `:app:testDebugUnitTest` aus, anschließend `assembleDebug`, prüft die APK und schreibt ein Log unter `Builds/`.
4. **Nur wenn alle Schritte PASS zeigen**: Die erstellte Debug-APK unter `Builds/` verwenden. Bei einem FAIL das vollständige Log aufbewahren; nicht einfach installieren.
5. Alternativ manuell aus CMD im Projektordner:

```bat
PREPARE_PROJECT.bat --no-pause
gradlew.bat :app:testDebugUnitTest --stacktrace
gradlew.bat assembleDebug --stacktrace
```

Gradle benötigt eventuell beim ersten Lauf Internet. JDK, Android-SDK und akzeptierte SDK-Lizenzen müssen vorhanden sein.

## 3. Build und automatische ADB-Installation

1. Handy über USB anschließen und entsperren.
2. USB-Debugging am Handy einschalten; Abfrage nach RSA-Fingerabdruck bestätigen.
3. `VORTEST_INSTALL.bat` starten. Der Wrapper führt zuerst Vorbereitung, Unit-Tests und Build aus und wartet **erst nach PASS** auf genau ein autorisiertes ADB-Gerät.
4. Bei `unauthorized` am Handy autorisieren; bei zwei Geräten auf eines reduzieren. Vorgang kann mit Strg+C abgebrochen werden.
5. ADB installiert mit `adb install -r` (bestehendes Paket bleibt gleich). Installation beweist noch keine Funktionsfähigkeit.

## 4. Technische Prüfung am Gerät (PASS/FAIL jeweils dokumentieren)

| ID | Prüfung | Erwartung |
|---|---|---|
| T01 | App starten, Bestandseinstellungen/Setups laden | Keine unerwartete Migration, bisheriger Funktionsumfang erhalten |
| T02 | Einstellungen > Audioaufnahme | Feld `Max. Rollingbuffer-Betriebsdauer` zeigt beim erstmaligen Start 12 h |
| T03 | 1 h einstellen und speichern; App neu öffnen | 1 h weiterhin gespeichert |
| T04 | 0 und 169 h eingeben | Speichern gesperrt; gültige Werte 1–168 h |
| T05 | Rollingbuffer einschalten, Analyzer `ROLLING_8K` öffnen | Ein gemeinsamer Rolling-Audio-Datenstrom; Analyzer läuft |
| T06 | Betriebsdauer abwarten | Rollingbuffer wird deaktiviert; gekoppelter Analyzer wird gestoppt |
| T07 | Rollenwechsel: Analyzer auf `DIRECT_48K` | Eigenständigen Direktbetrieb nicht irrtümlich durch Rolling-Stopp abbrechen |
| T08 | Nach Stopp Rollingbuffer neu starten | Neue Betriebsphase startet; Ringkapazität bleibt 30 min |
| T09 | Observer-Rating 0–5 und −1 senden | Legacy-Beobachtungsweg weiterhin funktionsfähig |
| T10 | OBSERVER-WAV und ANALYZER-WAV speichern | Dateien, Upload und `capture_origin`/`trigger_type` prüfen |
| T11 | Cloud nicht verfügbar, danach wieder verfügbar | Offline-Statistik-Outbox bleibt erhalten und verarbeitet später ohne Duplikate |
| T12 | Gerät neu starten / App beenden während Rolling | Rolling-Limit-Restartverhalten gesondert prüfen; noch nicht als robust nachgewiesen |
| T13 | App zurück / Bildschirm drehen / Bildschirm aus | Kein unbeabsichtigter Abbruch laufender Aufnahme |
| T14 | Self-Test / bestehende Regressionen | Alle Tests dokumentiert, nur echte PASS zählen |

**Hinweis:** Die neue maximale Betriebsdauer ist nur in vollen Stunden einstellbar; zum automatischen End-to-End-Test muss eine volle Stunde abgewartet werden. Ein beschleunigter Test-Hook ist noch nicht enthalten.

## 5. Geplante, aber in diesem Candidate NICHT fertige Abnahmetests

- S01 30-Minuten-Overall-Statistik aus Rolling-PCM 2–20 Hz mit exakten :00/:30-Buckets.
- S02 Filterstatistik (Default AUS), 0–100 Hz, 10/20 Bänder; lokale CSV.
- S03 1-Sekunden-CSV, `HalfHourStatistics.csv`, Beobachtungsmittelwert nur lokal.
- S04 WAV-Impulsfilter-CSV bis 40 Filter separat schaltbar.
- S05 Statistik- und WAV-Ausgabeorte getrennt per Android-SAF/SD-Karte auswählbar.
- S06 Flash-schonendes Sicherungsintervall, Recovery und Hintergrund-Retry.

**Diese Funktionen dürfen vor deren tatsächlicher Codeimplementierung nicht als bestanden abgehakt werden.**

## 6. Bei Fehlern zurückmelden

Die Datei `Builds/LAST_VORTEST_RESULT.txt`, das zugehörige `VORTEST_*.log`, Android-Gerätemodell/-Version sowie kurze Beschreibung des gescheiterten Tests sichern. Keine vollständigen Observer-Keys oder Zugangstokens weitergeben.

## 7. Ist-/Soll-Abgrenzung

Neu in diesem Candidate: einstellbare maximale Rolling-BETRIEBSDAUER, Default 12 h; Datastore-Persistenz; Einstellfeld 1–168 h; Service-Timeout mit Stoppen des gekoppelte `ROLLING_8K`-Analyzers. Die RAM-Ringlänge 30 min bleibt unverändert. **Service-Neustarts, Prozessorabstürze und offene Statistik-/SAF-Anforderungen müssen noch technisch geprüft und vervollständigt werden.**
