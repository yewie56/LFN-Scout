# LFN-Scout – verbindliche Projektübergabe v0.5.40

**Übergabestand:** 11.10.2026; Dokumentationsrevision ohne Änderung des Android-Quellcodes, `versionName=0.5.40`, `versionCode=187`. **Dies ist eine vollständige Quellprojekt-Übergabe, keine neue v0.5.41 und keine freigegebene APK.**

## 1. Sofortanweisung für den nächsten Chat

1. Das **gesamte** ZIP hochladen. Dateien liegen direkt im Archivwurzelverzeichnis, ohne zusätzliche äußere Verzeichnisebene.
2. Zuerst `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md` und diese Datei lesen. Auch projektspezifische Unterlagen unter `docs/project/` berücksichtigen.
3. Basis ist **das in diesem ZIP enthaltene vollständige v0.5.40-Projekt**, einschließlich des zweimal korrigierten Python-Reportstarters. Nie stillschweigend auf eine ältere v0.5.40-Variante zurückgehen.
4. Nächste beauftragte Entwicklungsarbeit ist **v0.5.41: Vergleichsreport für drei Filterverfahren** (Legacy-IIR erster Ordnung, Butterworth-Bandpass vierter Ordnung, FFT-basierte fensterweise Bandenergie). Die Implementierung war besprochen, aber **bei Übergabe noch nicht erstellt**.
5. **Keine** Behauptung von Android-Tests ohne Windows-Build-Log; **keine** Änderung der aktuellen Rollingbuffer-/Cloud-Produktivmessung durch das Vergleichswerkzeug; Regressionstest und Self-Test früh vorsehen.

## 2. Nachgewiesener Versions-, Build- und Teststand

| Gegenstand | Nachweis / Stand |
|---|---|
| v0.5.39, versionCode 186 | Windows-Vortest `VORTEST_v0.5.39_20261011_232908.log`: PREPARE, `:app:testDebugUnitTest`, `assembleDebug`, APK, ACTIVE-ZIP, ADB und Installation **PASS** (11.10.2026, 23:32:56). Letzter **nachweislich erfolgreicher Android-Build**. |
| v0.5.40, versionCode 187 | Vollständiger Quellstand hier vorhanden; **Windows-Gradle-Unit-Tests, APK-Build und ADB-Installation dieser Version noch nicht durch ein Log nachgewiesen**. |
| v0.5.40 Offline-Report | Nutzer erzeugte am 11.10.2026 23:48:52 `VALIDIERUNGSBERICHT.md`: **330/330 PASS**, 0–100 Hz, 10/20 Bänder, Simulation mit Fs=1000 Hz, 6 s, letztes 1-s-RMS, Toleranz ±0,35 dB. |
| Python-Starter | Erstes Windows-Skript scheiterte am Microsoft-Store-Alias, zweites an überzähligen Anführungszeichen. Das zuletzt korrigierte Skript erzeugte erfolgreich den vom Nutzer hochgeladenen Bericht. |
| Filter-Referenz | `FilterResponse_10bands.csv`, `FilterResponse_20bands.csv`, `FilterSimulation_Soll_Ist.csv` als Referenzbelege unter `docs/project/handover_evidence/`. |
| v0.5.38 Cloud-Queue | Windows-Build/ADB PASS 11.10.2026 23:20:35; Gerätelog: nach Update `retained_sent=1`, `pending_upload=0`, `deleted_expired=0`, `unreadable=0`, `next_delete_at_ms=1791838803406`. |
| v0.5.37 Cloud-Queue | Windows Build/ADB PASS; erster Cloud-Upload laut App-Log `newly_sent=1`, nach Neustart `newly_sent=0`, `retained_sent=1`, `total_local=1`. **Cloud-Tabelleninhalt noch nicht direkt mit CSV verifiziert.** |
| 24-h-Löschung | **Offen**: der um 11.10.2026 23:00:03 bestätigte Eintrag darf frühestens 12.10.2026 23:00:03 MESZ gelöscht werden; tatsächliche Bereinigung noch nicht nachgewiesen. |

**Genauigkeit der Simulation:** Vergleich analytischer Übertragungsfunktion mit unabhängiger Python-Nachbildung der Kotlin-Rekursion; keine hardwareunabhängige physikalische Bestätigung. Die erste Ordnung besitzt erhebliche Bandüberlappung. Messergebnisse sind `dBFS`/PRE_GAIN-Diagnostik, keine allgemein gültigen absoluten Schallpegel.

## 3. Funktionsüberblick, keine Behauptung einer generellen Freigabe

- Android-App mit Beobachter-Bewertungen 0..5, Nichtantwort −1, optionalem 8-kHz-Analyzer, Rollingbuffer, 30-Minuten-Gesamt-/Filterstatistiken, optionalen Sekunden-Epochen und lokaler CSV-Archivierung.
- Rollingbuffer: **30 Minuten PCM-Ringkapazität**, Betriebsdauer separat konfigurierbar (Standard 12 Stunden). Laufzeit unabhängig vom Öffnen/Schließen des Analyzers. PRE_GAIN für Statistik (digitaler Gain darf sie nicht verändern).
- Globale Halbstunden 00/30: Leq/L50/L90, gültige Sekunden, PARTIAL/COMPLETE, optionale lokale Bewertungen, vier **ausschließlich lokale** Impulsindikatoren `peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`.
- Kontinuierliche Filter-Halbstundenstatistiken sind standardmäßig AUS; 10 oder 20 Bänder, eigene Filter-Min/Max-Grenzen. Ihre CSV bleibt lokal; **kein Cloud-Upload**.
- Cloud: nur bestehender akustischer Gesamt-Halbstunden-Datensatz (Leq/L50/L90 und Metadaten) über bestehenden `submit_half_hour_statistics`, keine SQL-Migration. Idempotente Bucket-ID. Keine Cloud-Übertragung der vier Impulswerte, aggregierten Bewertungen, Filter- oder Sekundenstatistiken.
- Persistente Queue: `PENDING` -> nach bestätigtem Erfolg `SENT_PENDING_DELETE` mit Zeitpunkt -> **erst nach mindestens 24 Stunden**, beim nächsten Queue-Durchlauf löschen. Keine reguläre Doppelübertragung; keine Zugangsschlüssel in Queue-Dateien; Fehler und andere Observer getrennt behandeln.
- Sichtbare App-Bezeichnungen verwenden **Cloud**, nicht „Supabase“. Technische Schnittstellen-/DB-Namen bleiben unverändert.

## 4. Verbindlicher nächster Auftrag v0.5.41: Filterverfahren vergleichen

**Ziel:** eigenständiges Windows-Validierungswerkzeug mit vergleichbaren Testvektoren und reproduzierbaren Ausgaben für (a) Legacy-Hoch-/Tiefpass erster Ordnung, (b) Butterworth-Bandpass 4. Ordnung als SOS-Biquads, (c) FFT-basierte Bandenergie.

Erforderliche Reportdimensionen: Frequenzgang, Bandmaximum, -3-dB-Bandbreite (mit sauber definierten Grenzen und Einschränkungen), Übersprech-/Kopplungsmatrix, Amplitudenlinearität, Einschwing- und Ausschwingzeit, Impulsantwort/Impulsverschmierung und ggf. Gruppenlaufzeit. 10 und 20 Bänder; Standard 0–100 Hz, dazu eigene Grenzen (10–60 Hz). 0-Hz/DC-Band technisch gesondert korrekt behandeln: kein undefinierter 0-Hz-Hochpass. FFT ist **Energiezuordnung pro Zeitfenster**, kein identischer kontinuierlicher IIR-Filter. Fensterlänge/Überlappung/Leckage/Zeitauflösung explizit kennzeichnen. Für 0,5–2 Hz ausreichend lange Beobachtungszeit testen und Einschwingen nicht mit Filterfehler verwechseln. Gleiche Signale, gleiche Pegelreferenz, gleiche Banddefinition für fairen Vergleich; Grenzfrequenzen und Nyquist prüfen.

Reportdateien: tabellarische CSVs, SVG-Frequenzgänge, SVG-Übersprech-Heatmaps, Impulsantworten, Markdown-Gesamtbericht mit toleranzgebundenem PASS/FAIL und nachvollziehbarer Methodik; Python 3.11 unter Windows/Anaconda bevorzugt, möglichst wenige Dependencies. Für das bestehende Projekt **keine** Änderung der Android-Filterberechnung, Halbstunden-CSV, Cloud-API oder DB. Spätere produktive Filterwahl gesondert beschließen.

## 5. Offene / zurückgestellte Aufgaben (stabile Referenzen)

`docs/project/DEFERRED_TASKS.md` ist das zentrale Register und muss in jeder neuen Version gepflegt werden.

- **R01** explizit spätere vier benannte Cloud-Spalten für Impulsindikatoren (nicht P01–P04), vorerst lokal. **R02** GUI-Facelift. **R03** vollständiger Restore auf neuem leeren Cloud-Projekt. **R04** Secret-/API-Sicherheit. **R05** eventuell API-Proxy. **R06** WAV-Namen. **R07** Teilnehmerkey-Ablauf/Berechtigung.
- **O01** lokale Bewertungsaggregation umgesetzt, Standardfall inkl. −1 geräteseitig bestätigt; weitere Randfälle. **O02** Gesamt-Halbstunden-Cloud-Upload im Gerätelog erfolgreich, direkter Tabellenabgleich offen. **O03** Queue/Retry, 24-h-Retention implementiert, Offline-/24-h-Lösch-Endtest offen. **O04** WAV/CSV-SD-Pfade mit SAF offen. **O05** Filtermesskette/Trennschärfe offen, v0.5.39/40 nur numerische Validierung. **O06** weitergehende Impulsalgorithmen. **O07** zusätzliche persistente Parameter. **O08** Langzeit/Regression/Selftest.
- **A01** Mithören; **A02** SUPERFAST; **A03** automatische Triggeraufzeichnung; **A04** Impulsvarianten unter Ringing-Berücksichtigung; **A05** weitere Filterverfahren.
- **O09 (neu)** v0.5.41 Vergleichsreport Legacy/Butterworth4/FFT mit Impulstests, **geplant, nicht implementiert**.

## 6. Reproduzierbarer Arbeitsablauf Windows 11

1. ZIP ohne Unterverzeichnis nach `C:\LFN540` entpacken; für neue Version eigene Arbeitskopie z. B. `C:\LFN541`.
2. Python-Report: `FILTERBANK_REPORT_STARTEN.bat` (Anaconda-Interpreter wird erkannt). Ausgabe `FilterbankReport_v0.5.40/VALIDIERUNGSBERICHT.md`, CSVs, SVGs.
3. Android Build/Test/Install: `VORTEST_INSTALL.bat`; Logs in `Builds/`, insbesondere `LAST_VORTEST_RESULT.txt`, `VORTEST_*.log`; `:app:testDebugUnitTest` und `assembleDebug` müssen PASS zeigen.
4. Alle Quell-Dateien, Hilfsdateien, Testdateien, Dokumente **direkt im ZIP-Wurzelverzeichnis** verpacken, keine doppelte übergeordnete Ordnerstruktur. Einstellungen/Alt-Installationen kompatibel halten.
5. Nach jeder Änderung Regressionstests, Self-Test, Changelog und Status sowie `docs/project/DEFERRED_TASKS.md` aktualisieren. Jeder Teststand separat als Quellprüfung, lokale Python-Simulation, Windows-Gradle, APK-Installation oder echter Android-Live-/Cloud-Test deklarieren.

## 7. Hinweise zur Quelldokumentation

Historische Abschnitte in `CHANGELOG.md` und `DIRECTIVES.md` enthalten teils damalige Aussagen „Test noch offen“. Diese werden durch diese **chronologisch neuere** Übergabe und den Statusnachtrag ergänzt, nicht rückwirkend als falsch gelöscht. Das ältere Übergabedokument v0.5.26 ist nicht die aktuelle Autorität. Bei Konflikten gilt der neueste nachgewiesene Stand, **niemals** ein Status ohne Nachweis.
