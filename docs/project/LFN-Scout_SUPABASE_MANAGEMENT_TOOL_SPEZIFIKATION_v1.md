# LFN-Scout – Supabase-Management-Tool

**Dokumentversion:** 1.0  
**Status:** Anforderung und technischer Entwurf, **nicht implementiert**  
**App-Ausgangsstand:** LFN-Scout v0.5.17 (Portraitmodus auf Xiaomi bestätigt). Dieses Dokument erzeugt keine neue Android-App-Version.  
**Verknüpfte Anforderungen:** `LFN-Scout_STAT_AUDIO_SAVE_KONFIGURIERBARE_STATISTIK_v2.md` und die zurückgestellte Teilnehmerkey-Erweiterung.

## 1. Zweck und Geltungsbereich

Eine eigenständige **Windows-Python-GUI** soll die administrative Verwaltung des LFN-Scout-Supabase-Projekts ermöglichen: Teilnehmer/Keys, vollständiges Backup, vollständiger oder reduzierter Restore, vollständige Löschung von LFN-Scout-Nutzdaten sowie gezielte Teilnehmerentfernung. Administrative Geheimnisse gehören ausschließlich in das lokale Management-Tool bzw. sicher verwaltete Server-Komponenten, **niemals in die Android-App oder ein öffentliches Repository**.

Bestehenden Code für das Erstellen neuer Teilnehmerkeys nach tatsächlicher Sichtung wiederverwenden. Historisch vorgesehen: Eingabe von Name, Ort in Deutschland oder Geokoordinaten, bei fehlenden Koordinaten automatische Geokodierung; jede explizite Neuanlage erzeugt einen neuen Teilnehmer. Dateipfad, tatsächliche API, Sicherheitsmodell und Nutzbarkeit dieses Codes sind vor Implementierung **noch zu verifizieren**.

## 2. Verbindliche Bedienfunktionen

| ID | Funktion | Sollverhalten |
|---|---|---|
| M01 | Teilnehmer anlegen / Key ausstellen | Neuer Teilnehmer mit Name und Ort/Koordinaten; Key aus bestehendem Generator; Gültigkeitsdauer und Rechte speichern. |
| M02 | Key erneuern / entwerten | Für denselben Teilnehmer neue Keys ausstellen; **nur der zuletzt ausgestellte Key** ist aktiv. Alle älteren sind unmittelbar serverseitig ungültig. Ablaufdatum im manipulationsgeschützten Key kodieren, serverseitig prüfen. |
| M03 | Teilnehmerübersicht | ID, beschreibende Angaben, Key-Status, Ablauf, WAV-Berechtigung, letzte Beobachtung/Aufnahme, Anzahl verbundener Daten anzeigen. |
| M04 | Vollständiges Backup | Datenbankschema und Nutzdaten sowie *separat* sämtliche zugehörigen WAV-/Storage-Objekte, Konfiguration und Manifest exportieren. |
| M05 | Vollständiger Restore | Konsistente Wiederherstellung aller gesicherten Daten und Storage-Objekte samt Zugriffsregeln in ein festgelegtes Zielprojekt. |
| M06 | Reduzierter Restore | Auswahl: je Teilnehmer nur letzte Beobachtung, nur letzte WAV oder beide; dazu jeweils ihre zwingend benötigten Referenzen und zugehörigen Statistiken. |
| M07 | Gesamtlöschung | Nach Vorschau, Backup und expliziter Bestätigung die **LFN-Scout-Nutzdaten und zugehörige WAV-Objekte** löschen; nicht unbemerkt das gesamte Supabase-Projekt zerstören. |
| M08 | Teilnehmer entfernen | Teilnehmer sperren, alle Keys widerrufen, ausgewählte/zugehörige Beobachtungs-, Audio-, Statistik- und Storage-Daten konsistent entfernen; übrige Teilnehmer unberührt. |
| M09 | Prüfungen / Audit | Vorher-/Nachher-Zählungen, Integritätsprüfungen, Fehlerdiagnose, lokale Aktionsprotokolle und sichere Wiederaufnahme fehlgeschlagener Schritte. |

## 3. Zurückgestellte Key-Erweiterung

- Einstellbare Key-Gültigkeitsdauer; Key verfällt automatisch zum kodierten Ablaufzeitpunkt.
- Je Teilnehmer können weitere Keys ausgestellt werden. Server prüft die *aktuell gültige Key-ID oder Generation* und weist sämtliche älteren Keys zurück, **auch vor ihrem Ablaufdatum**.
- Im signierten bzw. anderweitig gegen Manipulation geschützten Key stehen Ablaufdatum und Rechte für WAV-Speicherung mit **1 kHz**, **8 kHz** oder **beiden** Abtastraten.
- Default für neue WAV-Speicherung bleibt **1.000 Hz, 16 Bit, Mono**. Die Key-Berechtigung ist hiervon unabhängig; 8-kHz-Speicherung ist nur bei expliziter Freigabe zulässig. Quell-, Analyse- und WAV-Samplingrate getrennt protokollieren.
- Der Server muss beim Upload Key-Status und Berechtigung prüfen; allein ein clientseitig dekodiertes Feld genügt nicht. Eine Sperrung muss auch bei der Löschung eines Teilnehmers wirksam bleiben.
- **Status:** geplant, noch nicht umgesetzt. Kryptografisches Schlüsselformat, Mehrgeräteverhalten und Offline-Ablauf bedürfen der konkreten Festlegung.

## 4. Vollständiges Backup

Ein reiner PostgreSQL-Dump reicht nicht für einen vollständigen LFN-Scout-Restore: Die tatsächlichen WAV-Bytes liegen in Supabase Storage und müssen separat exportiert werden. Das Archiv umfasst, soweit im Istsystem vorhanden:

1. Datenbankschema und Daten einschließlich Teilnehmern, Keys/Key-Zuständen, Beobachtungen, Audio-Metadaten, Aufnahme-/Statistikdatensätzen, 0–40 FAST-Filterstatistiken je Aufnahme und Impulsereignissen.
2. Alle referenzierten Objekte aus dem privaten Audio-Storage einschließlich Pfad, Größe, Prüfsumme und Objektmetadaten.
3. Für einen Restore benötigte Konfiguration: Policies/RLS, Bucket-Definitionen, relevante Migrationen und Edge-Function-**Quelltexte bzw. reproduzierbare Deploy-Anweisungen**, soweit tatsächlich verfügbar. Deployment-Secrets niemals unverschlüsselt ins ZIP schreiben.
4. `manifest.json` mit Backupformat/-version, Erstellzeit UTC, Quellprojektkennung (ohne Geheimnisse), Schemainformationen, Datensatzanzahlen, Datei- und SHA-256-Liste, Vollständigkeitsstatus sowie aufgetretenen Fehlern.
5. Optional `README_RESTORE.md` mit Zielumgebung, Voraussetzungen, Ablauf und zu prüfenden Nachbedingungen.

Ein Backup wird **nur als COMPLETE ausgewiesen**, wenn DB-Dump, referenzierte Storage-Objekte und Manifest vollständig und konsistent sind. Bei einem Fehler Teilresultate erhalten, aber klar als INCOMPLETE kennzeichnen. Archivzugang schützen (geeignete Verschlüsselung bzw. sicherer lokaler Speicherort); personenbezogene Daten und vertrauliche Recovery-Informationen nicht öffentlich bereitstellen.

### 4.1 Dateistruktur (Entwurf)

```text
LFN-Scout_Recovery_<UTC-Zeitstempel>.zip
  manifest.json
  README_RESTORE.md
  database/schema.sql
  database/data.dump
  storage/brumm-audio/...
  deployment/buckets.json
  deployment/migrations/...
  deployment/functions/...
  reports/backup_validation.json
```

Diese Struktur ist konzeptionell; genaue PostgreSQL- und Storage-Exportformate müssen gegen den vorhandenen Projektstand geprüft werden.

## 5. Restore-Modi

### 5.1 Vollständiger Restore

Zielprojekt prüfen, Manifest/Prüfsummen validieren, benötigte Strukturen und Policies anlegen, DB-Daten sowie WAV-Objekte wiederherstellen, Referenzen prüfen, Funktionstest ausführen. Restore nach Möglichkeit zuerst in einem **neuen, leeren Supabase-Projekt** erproben. Ein Datenbank-Dump allein ersetzt keine Prüfung der Storage-Dateien, Funktionen und Rechte. Bestehende Zieldaten niemals stillschweigend überschreiben.

### 5.2 Reduzierter Restore je Teilnehmer

| Modus | Zu übernehmen |
|---|---|
| `LAST_OBSERVATION` | Genau die zeitlich letzte gültige Beobachtung je Teilnehmer und zwingend benötigte Metadaten/Referenzen. Keine WAV, die nicht ausdrücklich benötigt wird. |
| `LAST_AUDIO` | Genau die zeitlich letzte gültige Aufnahme je Teilnehmer: WAV, Aufnahme-Metadaten, dazugehörige Statistik- und Filter-/Ereignisdaten. |
| `LAST_BOTH` | Letzte Beobachtung **und** letzte Aufnahme unabhängig voneinander; diese müssen nicht denselben Zeitpunkt haben. |
| `FULL` | Alle im Backup enthaltenen Daten und Objekte. |

Letzte Einträge nach festgelegtem fachlichem UTC-Zeitstempel ermitteln; bei Gleichstand deterministisch nach stabiler ID. Fehlende Beobachtungen/WAVs als fehlend protokollieren, **nicht** mit älteren Werten oder erfundenen Platzhaltern auffüllen. Abhängigkeiten mittels Original-IDs oder dokumentierter ID-Abbildung erhalten. Der reduzierte Restore muss festlegen, ob nur aktive Teilnehmer/Keys oder auch gesperrte/abgelaufene Identitäten importiert werden; **ein alter widerrufener Key darf keinesfalls versehentlich reaktiviert werden**. Vor dem eigentlichen Schreiben Vorschau und Zählungen erstellen.

## 6. Löschfunktionen

### 6.1 Gesamtlöschung (`DELETE_ALL_DATA`)

Löscht nach gesonderter bestätigter Aktion **alle LFN-Scout-Nutzdaten und zugehörigen Storage-Objekte**. Ob Schema, Buckets und Serverfunktionen erhalten bleiben, muss in der GUI ausdrücklich beschrieben sein; Vorgabe: administrative Infrastruktur möglichst erhalten. Das Löschen des gesamten Supabase-Projekts ist **kein** impliziter Teil dieser Aktion.

### 6.2 Teilnehmerentfernung (`DELETE_PARTICIPANT`)

Vor Ausführung exakte Teilnehmer-ID/Anzeigenamen zeigen. Zunächst Key/Teilnehmer serverseitig sperren; anschließend betroffene Beobachtungen, WAV-Dateien, Audio-Metadaten, Statistikgruppen und Ereignisse gemäß gewählter Löschpolitik entfernen. Falls rechtlich/organisatorisch eine anonymisierte Auditspur vorgesehen ist, diese ohne reaktivierbare Keys behandeln und gesondert ausweisen. Bei Teilfehler darf die Sperre nicht rückgängig gemacht werden.

### 6.3 Verpflichtende Schutzmaßnahmen

- Standardmäßig **Dry Run** mit Anzahl Datensätze, Storage-Objekten, Speichergröße und Referenzkonflikten.
- Frisches, vollständig geprüftes Backup verlangen oder eine begründete explizite Ausnahme mit gesonderter Bestätigung vorsehen.
- Für destruktive Aktionen Bestätigungsdialog mit ausgeschriebenem Projekt bzw. Teilnehmer und der zu löschenden Objektanzahl; keine One-Click-Löschung.
- Idempotente bzw. wiederanlaufbare Löschschritte, Audit mit Zeit, Operation, Ziel und Erfolg/Fehler; **keine Zugangstokens im Log**.
- Nachkontrolle: keine verwaisten Audio-/Statistikreferenzen oder versehentlich aktiven Keys.

## 7. Gemeinsame Audio-/Statistikdaten berücksichtigen

Die geplante Speicherpipeline erzeugt Audio-/Statistikpakete sowohl aus dem Beobachterpanel als auch aus dem Analyzer. Die Aufnahme- und Statistikdauer ist konfigurierbar (Default **120 s**), ebenso der Gesamtstatistikbereich (Default **2–25 Hz**). Filterstatistiken folgen **unabhängig** dem effektiven FILTER-MIN/MAX-Bereich und der Bandzahl aus dem Filterbank-Setup; maximal **40 Filterbänder** pro Aufnahme. Beobachterpakete enthalten **keinen Analyzer-Setup-Snapshot**; Analyzerpakete enthalten den gültigen LIVE-Snapshot. Archiv und Restore übernehmen alle jeweils tatsächlich verfügbaren Werte samt Qualitätsstatus.

Der Default der gespeicherten WAV beträgt **1 kHz / 16 Bit / Mono**. Ein künftiges 8-kHz-WAV bleibt eine separate Berechtigung und Formatentscheidung. Backups dürfen unterschiedliche Formate enthalten und müssen diese korrekt verzeichnen.

## 8. GUI, technische Umsetzung und Zugangsdaten

Vorgeschlagene Register: **Teilnehmer & Keys | Backup | Restore | Bereinigung | Diagnose**. Globale Kopfzeile zeigt aktives Supabase-Zielprojekt und ausdrücklich `TEST`/`PRODUKTION` an. Aktionen auf Produktion sind bewusst von Restore-Tests zu trennen. Benutzerfreundlicher Fortschritt, Abbrechen vor irreversiblen Schritten, Resume-Status für große Downloads und exportierbare Prüfberichte vorsehen.

Python-Tool als eigenständiges Windows-Programm mit persistenten **nicht geheimen** GUI-Einstellungen (`settings.json`), vollständigem Quellcode, Requirements, optionalem PyInstaller-Rezept und ausführbaren Regression-/Selftests planen. Geheime Zugangsdaten in sicherem lokalen Credential Store oder per Umgebung/gesicherter Eingabe halten; keine Admin-Keys in settings.json, ZIP-Backups, Logs, Android-Code oder GitHub einbetten. Zugriff auf Produktionsprojekt nach dem Prinzip geringster Rechte, wo möglich; administrative Operationen explizit begrenzen.

## 9. Akzeptanztests / Regression

1. M01: Teilnehmer mit Ort und alternativ Koordinaten anlegen; unvollständige Geokodierung und doppelte Eingaben behandeln.
2. M02: Key-Ablauf, vorzeitiger Widerruf bei Neuausstellung, unzulässiges 8-kHz-Uploadformat und manipuliertes Ablaufdatum abweisen.
3. M04: Datenbank- und Storage-Anzahlen vor/nach Backup vergleichen; WAV-Hashes validieren; fehlendes Storage-Objekt erzeugt INCOMPLETE.
4. M05: Vollständiger End-to-End-Restore auf **neues, leeres Testprojekt**; Lese-/Upload-/Rechteprüfung.
5. M06: Je Teilnehmer letzte Beobachtung, letzte Audio und beide unabhängig testen; Gleichstände, Nullfälle, Referenzen, Statistik-/Filterdatensätze, 40 Bänder und widerrufene Keys prüfen.
6. M07/M08: Dry Run, falsches Ziel, Abbruch, Backupvoraussetzung, echte Löschung im Testprojekt; andere Teilnehmer und Infrastruktur dürfen nicht unbeabsichtigt betroffen sein.
7. Netzunterbrechung/Teilversagen, erneuter Aufruf, doppelte Objekte, unvollständiges Archiv und Wiederaufnahme testen.
8. Keinerlei Geheimnisse in Archiv, Logs oder öffentlichem Code; Restore darf alte Keys nicht reaktivieren.
9. Status strikt trennen: **spezifiziert / implementiert / statisch geprüft / Integrationstest / echter Restore-Test**.

## 10. Offene Entscheidungen / Voraussetzungen

- Vorhandenen Keygenerator-Quellcode und die aktuellen Supabase-Tabellen/Edge Functions tatsächlich lokalisieren und prüfen; **keine bereits vorhandene vollständige Management-App behaupten**.
- Schema, Storage-Pfade, RLS, Schlüsselvalidierung und Backupformate anhand der wirklichen Projektmigrationen festlegen.
- Retention-/Löschpolitik bei Teilnehmerentfernung, Definition des fachlichen „letzten“ Zeitstempels und Behandlung von Audit-/Backup-Kopien entscheiden.
- Sichere Konfiguration für Test-/Produktivprojekt, Verschlüsselung der Recovery-ZIPs und Deployment der Edge Functions konkretisieren.
- Übernahme in `DIRECTIVES.md`, `PROJECT_STATUS.md`, `CHANGELOG.md` und `docs/project/` erst beim nächsten kontrollierten Projektstand; **dieses Dokument allein verändert v0.5.17 nicht**.

---

**Abnahmezustand bei Dokumenterstellung:** Anforderungen dokumentiert; keine Änderungen an Android-App, Supabase-Datenbank oder Teilnehmerkeys durchgeführt. Bestehende funktionierende Projektarchive nicht überschreiben.
