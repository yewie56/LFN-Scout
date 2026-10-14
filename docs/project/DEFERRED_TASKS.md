## v0.5.56 – Phase 3M (Entwicklung, nicht freigegeben)

Lesende Audio-/FFT-Umschaltprüfung mit Änderungs-/STOP-/nicht-unterstützt-/null-Kategorisierung; keine tatsächliche Audio-/FFT-Aktivierung. GitHub-Android-Build und Gerätetest offen; MON-/Anzeige-Vorschau aus vorherigen Phasen erhalten.

## v0.5.56 – Phase 3J (Entwicklung, nicht freigegeben)

Analyzer-Anzeige-Vorschau mit 12 Feldern und expliziter Wiederherstellung ergänzt; JSON/SharedPreferences werden dabei nicht geschrieben. Vollständige Setup-Aktivierung, Prozessrestart-Verhalten, UI-Panels und Android-Gerätetest weiterhin offen. Bestehende R-/O-/A-Kennungen unverändert.

## v0.5.44 – Offene Abnahme
- Reale v0.5.44 Android-JUnit-/Build-/ADB-Tests; Smartphone-Messung für 3 Methoden × 10/20 Bänder, jeweils mit gleicher Eingabe und mehreren Durchläufen.
- MediaStore-Append, CSV-Zeilenanzahl, Performance-Ausreißer, CPU-/Akku-Leistung und Langzeitverhalten überprüfen. Die Performance-CSV ist kein physikalischer Messwert und kein Gesamt-CPU-Profil.
- Ursprüngliche R01–R07, O01–O08, A01–A05, 24-h-Retention und lokale Impulskennwerte bleiben bestehen.

## v0.5.43: CSV-Filter-Methodenkennzeichnung und Speed
Offen: Methodennamen revisionssicher pro CSV-Export dokumentieren; echten Android-Performancevergleich 10/20 Bänder (P50/P95/P99, Energieverbrauch) durchführen; Regression beider unveränderten Analyzer-Wasserfälle.

# Nachtrag v0.5.41 (12.10.2026)

**O09:** Der geplante Offline-Dreifachvergleich ist mit v0.5.41 als Quellwerkzeug und reproduzierbarer Bericht umgesetzt; Android-Integration/produktive Filterentscheidung weiter ausdrücklich **offen**. **O05/A05:** Physikalische Kalibrierung und spätere Geräteprüfung weiterhin offen. **O08:** 18/18 Offline-Self-Tests PASS; Windows Gradle/ADB/Langzeit offen. **O03:** Reale 24-h-Löschung, Offline-Retry und direkte Cloud-Tabellenkontrolle offen. R01–R07, O01–O08, A01–A05 bleiben mit ihrer bisherigen Bedeutung erhalten.

# LFN-Scout – Liste der zurückgestellten Aufgaben

**Aktueller Entwicklungsstand: v0.5.40** (Python-Report 330/330 PASS; Android-Build noch offen). Letzter nachgewiesener Android-Build: v0.5.39 PASS. Cloud-Queue-Gerätetest v0.5.38 PASS für Übernahme/Aufbewahrung; tatsächliche 24-h-Löschung noch offen.

Ich habe die bisherigen Projektentscheidungen und zurückgestellten Aufgaben zusammengestellt. Dabei unterscheide ich zwischen ausdrücklich zurückgestellten Funktionen und offenen Entwicklungsarbeiten.

## 1. Ausdrücklich zurückgestellte Aufgaben

| Nr. | Aufgabe | Vereinbarter Umfang |
|---|---|---|
| **R01** | Cloud-Erweiterung für Impulskennwerte | Vier explizite Spalten in `half_hour_statistics` ergänzen und Werte übertragen. Jetzt ausdrücklich zurückgestellt. |
| **R02** | GUI-Facelift | Moderne, übersichtliche Einstellungen mit Kategorien und gut zugänglichem Gain-Regler; bisherige Mockup-Grafik als Orientierung. |
| **R03** | Cloud-Restore-Test | Vollständige Wiederherstellung auf einem neuen, leeren Supabase-Projekt und anschließender End-to-End-Test. |
| **R04** | Backend-Sicherheit | Prüfen, welche technischen URLs, Schlüssel und Konfigurationsinformationen in APK, Logs und Fehlermeldungen sichtbar sind. |
| **R05** | API-Proxy | Später gegebenenfalls Cloudflare Worker oder vergleichbare Proxy-Lösung zur Abschirmung der Backend-Schnittstellen. |
| **R06** | Verständlichere WAV-Dateinamen | Namen für lokale Aufnahmen und Cloud-Dateien lesbarer gestalten. |
| **R07** | Teilnehmerkeys mit Ablaufdatum | Gültigkeit im Key kodieren; neuester Key exklusiv aktiv; Berechtigung für 1-kHz- bzw. 8-kHz-Analyzer im Key. |

## 2. Noch offene Funktionen der Messwerterfassung

| Nr. | Funktion | Aktueller Stand |
|---|---|---|
| **O01** | Subjektive Bewertungen in Halbstunden-CSV | v0.5.35: lokaler CSV-Standardfall mit 0 und 4 auf Gerät bestätigt. v0.5.36: zusätzliche Zeitstempel-/Grenzfall-Regressionstests implementiert, Windows-/Geräte-Sonderfalltests offen. Bereits vor v0.5.35 erzeugte Beobachtungen sind nicht rückwirkend lokal verfügbar. |
| **O02** | Cloud-Upload der akustischen Halbstunden-Gesamtstatistik | Noch umzusetzen; ohne zusätzliche Impulskennwerte und ohne aggregierte Bewertungen. |
| **O03** | Cloud-Retry und Nachlieferung | Unterbrochene Übertragungen zuverlässig nachholen, ohne Duplikate zu erzeugen. |
| **O04** | SD-Karten-Unterstützung | WAV- und CSV-Speicherort unabhängig auswählbar, einschließlich Android-SAF-Unterstützung. |
| **O05** | Filterkalibrierung | Frequenzgänge, Trennschärfe, Pegelabweichungen und Filterübersprechen quantitativ prüfen. |
| **O06** | Erweiterte Impulsdiagnose | Die vier Kennwerte sind lokal vorgesehen; weitergehende Ereigniserkennung, Zeitlokalisierung und Klassifikation noch offen. |
| **O07** | Parameter und Einstellungen | Sinnvolle zusätzliche Konfigurationsparameter einschließlich Speicherung und Validierung. |
| **O08** | Weitere Regressionstests und Self-Tests | Besonders Langzeitbetrieb, Rollingbuffer, Speicherfehler, CSV-Konsistenz und Wiederanlauf prüfen. |

## 3. Zurückgestellte Analyzer-Erweiterungen

Diese Punkte stammen aus der längerfristigen Planung des spezialisierten Analyzers.

| Nr. | Funktion | Beschreibung |
|---|---|---|
| **A01** | Mithören | Optionaler Monitoring-Pfad mit eigener Verstärkung und gegebenenfalls Bandpass. |
| **A02** | SUPERFAST | Zusätzliche besonders schnelle Analyse-/Darstellungsmöglichkeiten. |
| **A03** | Automatische Aufzeichnungen | Erweiterte Trigger, automatische WAV-/PNG-Speicherung und Diagnose. |
| **A04** | Erweiterte Impulsalgorithmen | Verschiedene Verfahren vergleichen; Filter-Ringing und Parameterwirkung berücksichtigen. |
| **A05** | Weiterführende Filteranalyse | Mehr Filterkonfigurationen beziehungsweise alternative Verfahren, soweit messtechnisch sinnvoll. |

Einzelne Grundlagen dieser Analyzer-Funktionen können bereits vorhanden sein. Die Tabelle bezeichnet deshalb den noch ausstehenden Erweiterungsumfang, nicht zwangsläufig eine vollständig fehlende Funktion.

## 4. Verbindliche Regeln für spätere Versionen

Diese Punkte sind keine zurückgestellten Aufgaben, sondern gelten bereits:

- **Cloud statt Supabase:** In sichtbaren Menüs, Meldungen und Einstellungen ausschließlich „Cloud“. Interne technische Bezeichner bleiben kompatibel.
- **Keine unnötigen Datenbankänderungen:** Die Cloud-Struktur möglichst stabil halten.
- **Vier Impulskennwerte vorerst ausschließlich lokal:** `peak_second_dbfs`, `top_1s_energy_pct`, `top_10s_energy_pct`, `leq_minus_l50_db`.
- **Später vier explizite Spalten:** Für diese Kennwerte keine vorläufige Festlegung auf `P01–P04`.
- **Filterstatistik unabhängig schaltbar:** Standard AUS; 10 oder 20 Bänder mit eigenem Frequenzbereich.
- **Regressionstests und integrierte Self-Tests:** Bei Erweiterungen möglichst gleich mitentwickeln.
- **Bestehende Funktionen erhalten:** Keine stillschweigenden Funktionsverluste.

## 5. Meine vorgeschlagene Bearbeitungsreihenfolge

| Priorität | Referenz | Aufgabe |
|---|---|---|
| 1 | O01 | Subjektive Bewertungen lokal vollständig integrieren |
| 2 | O05 | Filterbank messtechnisch validieren |
| 3 | O02/O03 | Akustische Halbstunden-Gesamtstatistik zuverlässig an Cloud übertragen |
| 4 | O04 | SD-Karten-Unterstützung |
| 5 | R03 | Vollständigen Cloud-Restore-Test durchführen |
| 6 | R02 | GUI-Facelift |
| Später | R01 | Vier Impulskennwerte als explizite Cloud-Spalten ergänzen |

**Insgesamt sind hier 7 ausdrücklich zurückgestellte Aufgaben, 8 offene Mess- und Infrastrukturaufgaben sowie 5 Analyzer-Erweiterungen erfasst.**

Die Kennzeichnung **R01–R07, O01–O08 und A01–A05** können wir künftig als dauerhafte Referenzen für die Projektplanung verwenden. Diese Zusammenstellung ist aus den bisherigen Projektentscheidungen abgeleitet; eine erneute vollständige Prüfung aller aktuellen Projektdateien steht noch aus.

## v0.5.37 – Statusaktualisierung

- **O02:** Cloud-Upload der akustischen Gesamt-Halbstundenstatistik im Quellstand implementiert; Windows-Build und Cloud-Gerätetest offen. Keine DB-Migration.
- **O03:** Lokaler persistenter Puffer, Retry bei Start/abgeschlossenem Bucket und idempotente bucket_id im Quellstand. Zeitgesteuerte unabhängige Hintergrund-Nachlieferung weiterhin offen; Langzeittest ausstehend.
- **R01:** Vier Impulskennwerte bleiben ausschließlich lokal; vier explizite Cloud-Spalten erst später.
- **O01:** Lokal bereits im v0.5.36-Standard-/Nichtantwort-CSV-Test bestätigt; Cloud-Aggregation weiter ausgeschlossen.

## v0.5.37 – Statusaktualisierung

- **O02:** Cloud-Upload der akustischen Gesamt-Halbstundenstatistik im Quellstand implementiert; Windows-Build und Cloud-Gerätetest offen. Keine DB-Migration.
- **O03:** Lokaler persistenter Puffer, Retry bei Start/abgeschlossenem Bucket und idempotente bucket_id im Quellstand. Zeitgesteuerte unabhängige Hintergrund-Nachlieferung weiterhin offen; Langzeittest ausstehend.
- **R01:** Vier Impulskennwerte bleiben ausschließlich lokal; vier explizite Cloud-Spalten erst später.
- **O01:** Lokal bereits im v0.5.36-Standard-/Nichtantwort-CSV-Test bestätigt; Cloud-Aggregation weiter ausgeschlossen.

## v0.5.37 – 24-h-Retention
- **O03:** `SENT_PENDING_DELETE` und frühestens 24 h nach bestätigtem Upload bereinigen: im Quellstand implementiert; Android-Build und realer Retention-Gerätetest noch offen. GUI-Statusanzeige nicht implementiert (später prüfen).

## v0.5.38 – Statusaktualisierung
- **O02:** v0.5.37 Windows-Build PASS, geräteseitige Cloud-Erfolgsbestätigung und lokales CSV vorhanden. Cloud-Datenbankdatensatz inhaltlich noch direkt zu vergleichen.
- **O03:** 24-h-Retention und keine Wiederholung nach Prozess-Neustart am Gerät bestätigt; tatsächliche Löschung nach 24 h und Offline-Retry offen. In v0.5.38 präzisere Diagnosezähler und Löschfrist, noch kein eigener Hintergrundjob oder GUI.
- **O08:** Zusätzliche Regression zur Aufbewahrungsfrist; Langzeit-Gerätetest offen.

## v0.5.39 – Aufgabenstatus
- **O05:** Quantitativer Soll-Ist-Vergleich der digitalen, unkalibrierten Filterbank als JVM-Regression ergänzt; Ausführung auf Windows noch offen. Physikalische Kalibrierung, Bandtrennschärfe und Mikrofonfrequenzgang weiter offen.
- **O03:** v0.5.38 Windows/ADB und Geräte-Queue-Status PASS. Fristablauf-Löschung, Offline-Retry und Cloud-Datenabgleich offen.
- **O08:** Software-Frequenzgang-Regression mit 10/20 Bändern ergänzt; noch nicht Windows-getestet.
- **R01:** vier Impulskennwerte weiterhin nur lokal.

- **O05 v0.5.40:** Python-Offline-Validierungsreport mit Frequenzgang/Übersprechmatrix und Soll-Ist-CSV implementiert; 330/330 simulierte Prüfungen PASS. Android-Build/Gerätetest und reale physikalische Kalibrierung weiter offen.

## Übergabeaktualisierung v0.5.40 (11.10.2026)

- **O01:** Bewertungsaggregation mit 0/4 und 2/4/-1 im Geräte-CSV nachgewiesen, weitere Grenzfälle offen.
- **O02:** bestehender Gesamt-Halbstunden-Cloud-Upload laut Android-App-Log erfolgreich; direkter Tabellen-Abgleich bleibt offen (ältere Tabelle oben ist ein historischer Planungsstand).
- **O03:** 24-h-Retention über Prozessneustart und Upgrade auf v0.5.38 nachgewiesen; vollständiger Zeitablauf und Offline-Retry offen.
- **O05:** v0.5.39 Windows-Unit-Tests PASS; v0.5.40 Python-Report 330/330 PASS; reale akustische Kalibrierung/Trennschärfe offen.
- **O09 (neu):** eigenständiger Vergleichsreport Legacy-Filter/Butterworth 4. Ordnung/FFT-Bandenergie inklusive Frequenzgang, Übersprechen, Einschwing-/Impulsantworten und PASS/FAIL für v0.5.41 **geplant, noch nicht implementiert**.
- **Build:** v0.5.39 Gradle, APK, ADB PASS; v0.5.40 Android Gradle/APK/ADB noch offen.
- Details in `docs/project/HANDOVER_v0.5.40_2026-10-11.md`.

## v0.5.42 ergänzte offene Aufgaben
- **O09:** Methodenvergleich auf echtem Xiaomi mit gleichen Testsignalen, 10/20/40 Bändern: Filter-CPU-Mittel/Maximum sowie P50/P95/P99, Thermal, Akku, Dropouts; v0.5.42 nur Quell- und Hosttest.
- **O10:** Physikalische Amplituden-/Bandnormierung aller 3 Verfahren und Impuls-Zeitauflösung unabhängig validieren, FFT-Hann-Fenstereinfluss getrennt ausweisen.
- **O11:** Methodenkennung in lokalen Messmetadaten/CSV (rückwärtskompatibel) ergänzen, damit Messwerte nicht ohne Filterkontext verglichen werden; noch nicht implementiert.

### v0.5.56 Phase 3F – Entwicklungsnotiz (keine Freigabe)
- Der reine JSON-Setup-Browser ist in der Analyzer-Setup-Seite sichtbar; aktiv bleibt die bisherige Slot-Speicherung.
- Noch offen: JSON-Setup-Aktivierung, tatsächliche MRU-Schnellzugriffe, Änderungen nur auf explizites Speichern persistieren, MON-/Analyzer-Synchronisation, acht Analyzer-Panels und sieben Scout-Panels, SAF-Import-/Export sowie automatisierte und Gerätetests.
