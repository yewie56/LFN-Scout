# LFN-Scout v0.5.64

## Neu in v0.5.64

- Helles, vereinfachtes Panel für benannte JSON-Analyzer-Setups mit MRU-Anzeige.
- Automatisches atomisches Speichern der aktuell ausgewählten Analyzer- und MON-JSON-Daten beim Verlassen des gesamten Analyzer-Screens, nicht beim Panelwechsel.
- Legacy-Setup-Leiste aus der normalen UI entfernt, intern vorerst erhalten.
- **Grenze:** Vollständig persistenter Setupwechsel beim Messstart und automatische Übernahme aller bisher bestätigungspflichtigen Parameter noch ausstehend. Keine Android-Gradle-/Xiaomi-Testfreigabe.
- Verbindlicher Stand: `docs/project/CURRENT_STATE.md`.

---

**Aktueller Quellkandidat: v0.5.37 (Cloud-Halbstunden-Gesamtstatistik). Build- und Gerätetest ausstehend.** Siehe `ANLEITUNG_BUILD_TEST_v0.5.37.md`.

**Aktueller Quellkandidat: v0.5.37 (Cloud-Halbstunden-Gesamtstatistik). Build- und Gerätetest ausstehend.** Siehe `ANLEITUNG_BUILD_TEST_v0.5.37.md`.

## v0.5.36
Lokale Beobachtungs-Zeitstempelprüfungen und Regressionstests. Siehe `ANLEITUNG_BUILD_TEST_v0.5.36.md` und `docs/project/OBSERVATION_REGRESSION_v0.5.36.md`.

## v0.5.35
Lokale Beobachtungen (0..5, -1) werden ab dieser Version in der Halbstunden-CSV zusammengefasst. Siehe `ANLEITUNG_BUILD_TEST_v0.5.35.md` und `docs/project/DEFERRED_TASKS.md`.

## v0.5.34
Diagnostische Impuls-Kennwerte je Halbstunde im öffentlichen Gesamt-CSV. Siehe ANLEITUNG_BUILD_TEST_v0.5.34.md.


Aktueller Source Candidate: v0.5.33 (versionCode 180). Optionaler Sekunden-CSV-Export; Filterbank weiterhin nicht kalibriert. Windows-Android-Build offen.

## Neu in v0.5.31
Optionale Rolling-Halbstunden-Filterstatistik (10/20 Bänder, eigene Grenzen 0–100 Hz, Default AUS) und lokale CSV. Siehe `ANLEITUNG_BUILD_TEST_v0.5.31.md`.

## Stand v0.5.30 (Quellkandidat)

Neu: PRE-GAIN-Halbstundenwerte und optional direkt zugänglicher CSV-Export. Anleitung: `ANLEITUNG_BUILD_TEST_v0.5.30.md`. Die fachliche 30-Minuten-Statistik ist weiterhin nicht produktiv freigegeben.

## LFN-Scout v0.5.28 – Source Candidate

Optionaler CSV-Export der WAV-bezogenen Filterstatistik. Anleitung und Testplan: `docs/project/WAV_FILTER_CSV_v0.5.28.md`. Für Windows Build `VORTEST.bat` und `VORTEST_INSTALL.bat`. Keine Gerätetest-Freigabe.

# LFN-Scout v0.5.27

## Neu in v0.5.27

- WAV-Herkunft/Trigger und Analyzer-Statistik-Cloud-Anbindung.
- Observer-Statistik über persistente lokale Statistik-Outbox; Key nicht darin gespeichert.
- Wiederholungsversand bei weiteren Uploadereignissen, **kein autonomer Background-Retry**.
- 30-Minuten-Zeitreihe noch nicht umgesetzt, keine Android-/Xiaomi-Abnahme.
- Details: `docs/project/ANDROID_CLOUD_V0.5.27.md`.

## v0.5.27 – Source Candidate

Erste Android-Anbindung für WAV-Herkunft und Analyzer-Audiostatistik. **Noch nicht vollständig / nicht produktiv freigegeben.** Siehe `docs/project/ANDROID_CLOUD_V0.5.27.md`.

# LFN-Scout v0.5.26

## Neu in v0.5.26

- Additive Supabase-Erweiterung für Audio-Kontext, Audio-Statistik und unabhängige 30-Minuten-Statistik.
- Bestehende Tabellen und Legacy-Requests bleiben erhalten; `submit_observation` ist unverändert.
- Neue 30-Minuten-Tabelle mit festen `leq_dbfs`, `l50_dbfs`, `l90_dbfs`, Frequenzbereich, Coverage, Qualitätsstatus, Observer-ID, Geolocation und optionalem Kommentar.
- Audio-Statistik: eine Overall-Zeile plus 0..40 Filterzeilen je gespeicherter WAV.
- Bestehende Statistikkennwerte bleiben explizit benannt; `P01...P32` sind nur flexible Zusatzslots.
- `upload_wav` v1.5.0 sowie `submit_audio_statistics` v1.0.0 und `submit_half_hour_statistics` v1.0.0 sind auf dem Remote-Supabase-Projekt deployed und in zentralen Positiv-/Negativfällen real validiert.
- Remote-Migrationen sind bis `20261010174500` synchron; die Lookup-Semantik verwendet die kanonischen DB/API-Namen, insbesondere `valid_epochs` statt `epoch_count`.
- Pre-/Post-Migrations-Dumps liegen unter `supabase/`; der Restore auf ein komplett neues leeres Supabase-Projekt bleibt zurückgestellt.
- Android sendet die neuen Statistikdaten sowie `capture_origin`/`trigger_type` noch nicht produktiv; diese Aktivierung gehört in die Folgeversion.

Details: `docs/project/SUPABASE_STATISTICS_V0.5.26.md` und `docs/project/TESTPLAN_v0.5.26.md`.

Unter Windows für den Android-Quellstand weiterhin:

```bat
VORTEST_INSTALL.bat
```

Der Backend-/Supabase-Teil ist in den dokumentierten Punkten validiert. Ein realer v0.5.26-Android-Gradle-/APK-/Gerätetest ist weiterhin **nicht nachgewiesen**.

---

# LFN-Scout v0.5.25

## Neu in v0.5.25

- Analyzer-Statistikbox oben rechts kompakt mit `Leq(min-max)`, `L50(min-max)`, `L90(min-max)`; Default z. B. `Leq(2-25)`. Frequenzbereich und Statistikdauer bleiben einstellbar.
- Leq wird unbewertet aus der linearen Bandenergie über den vollständig gefüllten konfigurierten Statistikzeitraum berechnet.
- Drei Statistikzeilen eng gestapelt; keine zusätzliche Überschriftszeile. Rahmen und komplette Box bleiben sensitive Reset-Fläche.
- Manueller Analyzer-Screendump-Kommentar unten rechtsbündig direkt im Wasserfall, ohne schwarzen Hintergrundkasten.
- Audio-/Supabase-/24-h-Retention-Logik unverändert gegenüber v0.5.24/v0.5.23.

Unter Windows:

```bat
VORTEST_INSTALL.bat
```

Bis `UNIT TESTS` und `ASSEMBLE DEBUG` real PASS sind, bleibt v0.5.25 ausdrücklich **Source Candidate – Build noch nicht verifiziert**.

---

# LFN-Scout v0.5.24

## Zweck

Reine Compiler-Reparatur von v0.5.23. Keine neue Laufzeitfunktion.

- `OutlinedTextField`-Import für den Analyzer-Bildkommentar ergänzt.
- Nullable-WAV-Handling im Analyzer-Supabase-Upload korrigiert.
- v0.5.23 scheiterte real in `:app:compileDebugKotlin`; deshalb gilt v0.5.24 bis zum erfolgreichen Windows-Vortest ausdrücklich als **Source Candidate**.

Unter Windows:

```bat
VORTEST_INSTALL.bat
```

Erst wenn `UNIT TESTS` und `ASSEMBLE DEBUG` PASS sind, gilt der Android-Build als bestätigt.

---

# LFN-Scout v0.5.21

## Neu in v0.5.21

- Reine Vortest-/Packaging-Korrektur; Android-Laufzeit entspricht v0.5.20.
- `VORTEST.bat`, `PREPARE_PROJECT.bat` und `tools/run-vortest.ps1` lesen die Projektversion jetzt direkt aus `app/build.gradle.kts`.
- Behebt den real beobachteten Fehler 51, bei dem v0.5.20 gepackt wurde, der Prüfschritt aber noch eine v0.5.19-Datei suchte.
- APK-, ACTIVE-ZIP- und Logdateinamen werden künftig aus derselben Versionsquelle gebildet.
- BASE R1 bleibt unverändert.

Unter Windows weiterhin:

```bat
VORTEST_INSTALL.bat
```

Der reale v0.5.20-Lauf hat PREPARE, Unit-Tests und `assembleDebug` bereits erfolgreich durchlaufen; v0.5.21 muss den korrigierten ACTIVE-Check und anschließend die ADB-Warteschleife noch real bestätigen.

---

# LFN-Scout v0.5.20

## Neu in v0.5.20

- Zusatzfunktionen/WAV-Wasserfall: v0.5.19-Regressionsfix; gain-korrigierte Spektralbins unter -120 dBFS bleiben numerisch erhalten und werden erst bei der Farbabbildung begrenzt. L50/L90 bleiben über den gemeinsamen Statistikpfad konsistent.
- Analyzer-Audiosave: Audio wird vor Öffnen des Kommentardialogs vollständig eingefroren. Beobachter und Analyzer verwenden denselben Kommentar-Dialog und dieselben Audio-Timeouts. Kommentar wird beim Analyzer in Statistik-JSON/Manifest mitgespeichert.
- Analyzer-Actionleiste: Audio-Puffer-/Quoteninfo zweizeilig.
- Beobachterpanel: Audio-/Statistik-Setup nicht mehr im Hauptpanel, sondern in Einstellungen → Audioaufnahme; Entwicklermodus-Hinweis bei Bedarf oben rechts neben `LFN-Scout`.
- Zusatzfunktionen: Highspeed-Impulsdiagnose im Entwicklermodus ganz unten.
- Android-Zurück-Taste/-Geste: Panel-Verlauf zurück statt pauschal Hauptpanel.
- Keine Supabase-Schemaänderung; BASE R1 unverändert.

## Prüfen/Installieren

Unter Windows bevorzugt:

```bat
VORTEST_INSTALL.bat
```

Lokale Liefer-/Hostchecks des Quellstands: statisch **24/24 PASS**, Wasserfall-Regression **PASS**, Statistik-/Gain-Hosttest **PASS**, Kotlin-PSI **92 Dateien / 0 Syntaxfehler PASS**. Ein Android-/Compose-Gradle-Build konnte in der Erstellungsumgebung mangels `gradle-wrapper.jar` nicht gestartet werden.

Nach erfolgreichem PREPARE, Unit-Test, Build, APK- und ACTIVE-Paket wartet der Ablauf auf genau ein autorisiertes ADB-Gerät und installiert dann per `adb install -r`. v0.5.20 ist erst nach diesem realen Build und dem Xiaomi-Test als gerätebestätigt anzusehen.

---

# LFN-Scout v0.5.19

## Neu in v0.5.19

- Analyzer: `SAVE-Setup` fuer lokale Audio-/Statistik-Saves liegt jetzt unter **Einstellungen → Statistik** statt in der Hauptanzeige.
- Analyzer: `SAVE AUDIO` sitzt unten in der Actionleiste als 48-dp-Actionbutton im Format des Kamerabuttons; der knappe Buffer-/Tagesquoten-Infotext steht direkt daneben.
- Nach erfolgreichem lokalen Save erscheint `Lokal gespeichert` fuer 3 Sekunden als Overlay nahe dem Actionbutton; Status-/Fehlertexte werden nicht in bestehende Displayzeilen eingeschoben.
- WAV-Spektrum/Spektrogramm und lokale Statistik verwenden eine zentrale Gain-Referenzierung. Ein im analysierten PCM enthaltener Recording-Gain wird exakt einmal zurueckgerechnet; bei +50 dB also -50 dB auf die Pegelreferenz. PRE-GAIN-Analyzer-Saves werden nicht doppelt korrigiert.
- Bei 1-kHz-WAV mit ganzen Sekunden berechnen Zusatzfunktionen L50/L90 ueber denselben `StatisticsWindowEngine.analyzeOverall()`-Pfad wie `statistics.json`, damit identische Eingangsdaten bis auf Rundung identische Werte liefern.
- `VORTEST_INSTALL.bat` wartet nach erfolgreichem Build/Paketieren ohne Standard-Timeout auf genau ein autorisiertes ADB-Geraet und prueft alle 2 Sekunden erneut; `unauthorized`, `offline` und mehrere Geraete werden sichtbar gemeldet.
- Zwei bekannte Kotlin-Warnungen wurden bereinigt.
- BASE R1 bleibt unveraendert; fuer die normale Weiterentwicklung wird nur das neue ACTIVE-Paket benoetigt.

## Pruefstatus v0.5.19

Host-Kotlin-Selbsttest fuer den gemeinsamen Statistik-/Gain-Pfad: PASS. Versionsspezifische statische Lieferpruefung und ZIP-Integritaet werden in `BUILD_VERIFICATION_v0.5.19.md` dokumentiert. Echter Windows-Gradle-/ADB-Lauf und Xiaomi-Test von v0.5.19 sind bis zum Nutzerlauf **NOT RUN**.

---

# LFN-Scout v0.5.18.4

## Neu in v0.5.18.4

- Windows-Vortest zeigt jetzt **saemtliche Meldungen live auf dem Bildschirm** und schreibt dieselben Ausgaben gleichzeitig in die zeitgestempelte Logdatei. Das gilt fuer STDOUT und STDERR von PREPARE, Gradle, ACTIVE-Paketierung und ADB.
- APK-Pruefung, SHA-256, ACTIVE-ZIP-Pruefung, ADB-Pfad, Geraeteanzahl und Installationsstatus werden ebenfalls direkt auf dem Screen ausgegeben.
- Am Ende erscheint immer eine kompakte Schrittzusammenfassung mit PASS/FAIL fuer PREPARE, Unit-Tests, assembleDebug, APK, ACTIVE-ZIP, ADB-Geraet und Installation.
- Bei ADB-Fehlercode 62 wird nun explizit angezeigt, dass kein bzw. nicht genau ein freigegebenes Geraet gefunden wurde, inklusive Hinweisen zu USB-Datenkabel, Entsperren und USB-Debugging.
- Der reale Nutzerlauf von v0.5.18.3 hat PREPARE, Unit-Tests, `assembleDebug` und ACTIVE-Paketierung erfolgreich bestanden. Die Installation scheiterte ausschliesslich daran, dass `adb devices` kein freigegebenes Geraet meldete.
- Keine beabsichtigte Laufzeitanderung an Audio, Statistik, Analyzer, UI oder Supabase gegenueber v0.5.18.3. BASE R1 bleibt unveraendert.

## Pruefstatus v0.5.18.4

Statische Liefer-/Automationspruefung und ZIP-Integritaet werden in `BUILD_VERIFICATION_v0.5.18.4.md` dokumentiert. Der echte Windows-/Gradle-/ADB-Lauf von v0.5.18.4 ist bis zum Nutzerlauf **NOT RUN**.

---

# LFN-Scout v0.5.18.3

## Neu in v0.5.18.3

- Korrigierter Windows-Vortest: Normale STDERR-Ausgabe nativer Programme (insbesondere `java -version`) wird nicht mehr als PowerShell-`NativeCommandError` fehlinterpretiert.
- `tools/run-vortest.ps1` startet PREPARE, Gradle, Paketierung und ADB über `System.Diagnostics.Process`; der reale Prozess-Exitcode entscheidet über PASS/FAIL.
- `VORTEST_INSTALL.bat` behält den Ablauf PREPARE → Unit-Tests → Debug-Build → ACTIVE-ZIP → ADB-Installation bei.
- ACTIVE/BASE-Trennung bleibt unverändert; `LFN-Scout_BASE_ASSETS_R1.zip` wird weiterverwendet.
- Keine beabsichtigte Laufzeitänderung an Audio, Statistik, Analyzer, UI oder Supabase gegenüber v0.5.18.2.
- Details: `docs/project/PROJECT_PACKAGING_v0.5.18.3.md`, Testplan `docs/project/TESTPLAN_v0.5.18.3.md`, Nachweis `BUILD_VERIFICATION_v0.5.18.3.md`.

## Prüfstatus v0.5.18.3

Statische Liefer-/Automationsprüfung und ZIP-Integritätsprüfung werden im Buildnachweis dokumentiert. Windows-Gradle-Build, ADB-Installation und Xiaomi-Funktionstest sind bis zum tatsächlichen Nutzerlauf **NOT RUN**.

---

# LFN-Scout v0.5.18.2

## Neu in v0.5.18.2

- Windows-Vortest erweitert: Nach PREPARE, Unit-Tests und Debug-Build wird bei PASS automatisch das kleine buildfähige `LFN-Scout_ACTIVE_v0.5.18.2.zip` erzeugt, verifiziert und mit SHA-256 versehen.
- Projektübergabe erstmals zweigeteilt: ACTIVE bei jeder Version; großes `LFN-Scout_BASE_ASSETS_R1.zip` nur bei Änderung der ausgelagerten Referenz-/Recovery-Inhalte. Zuordnung über `BASE_PACKAGE_REQUIRED.txt`.
- `VORTEST_INSTALL.bat` bzw. `VORTEST.bat --install` kann nach vollständigem PASS die Debug-APK mit `adb install -r` auf genau ein autorisiertes angeschlossenes Android-Gerät installieren.
- Keine beabsichtigte Laufzeitänderung an Audio, Statistik, Analyzer, UI oder Supabase gegenüber v0.5.18.1.
- Details: `docs/project/PROJECT_PACKAGING_v0.5.18.2.md`, Testplan `docs/project/TESTPLAN_v0.5.18.2.md`, Nachweis `BUILD_VERIFICATION_v0.5.18.2.md`.

## Prüfstatus v0.5.18.2

Statische Liefer-/Automationsprüfung und ZIP-Integritätsprüfung werden im Buildnachweis dokumentiert. Windows-Gradle-Build, ADB-Installation und Xiaomi-Funktionstest sind bis zum tatsächlichen Nutzerlauf **NOT RUN**.

---
# LFN-Scout v0.5.18.1

Android v0.5.18.1 · `versionCode 162` · 25.09.2026

## Neu in v0.5.18.1

Diese Zwischenversion automatisiert ausschließlich den verpflichtenden Windows-Vortest. Im frisch entpackten Projekt genügt ein Doppelklick auf `VORTEST.bat`. Das Skript bereitet JDK/SDK/Gradle über `PREPARE_PROJECT.bat --no-pause` vor, führt `:app:testDebugUnitTest` aus und baut nur bei erfolgreichen Tests mit `assembleDebug` die Debug-APK. Log, Kurzstatus und eine versioniert kopierte APK landen unter `Builds/`.

Die App-Funktionalität von v0.5.18 wurde nicht absichtlich verändert. Insbesondere keine neue Supabase-Integration und keine weitere Statistik-/Audiofunktion. Der echte Windows-Gradle-Lauf und die Xiaomi-Abnahme sind noch nicht durch diese Lieferumgebung ausgeführt. Siehe `BUILD_VERIFICATION_v0.5.18.1.md` und `docs/project/TESTPLAN_v0.5.18.1.md`.

---
# v0.5.18 – 25.09.2026 – lokaler Audio-/Statistik-Save (Quellkandidat)

- Hauptnavigation: eigenständiger Analyzer-Button mit `activity`; danach `blocks` Zusatzfunktionen, `settings` Einstellungen und `waves` Seismik. Vorhandene Portrait-Vorgabe bleibt bestehen.
- Gemeinsames SAVE-Setup im Beobachter/Analyzer: 30–600 s (Default 120 s), Gesamtstatistik 1–200 Hz konfigurierbar (Default 2–25 Hz), WAV standardmäßig 1 kHz/16 Bit/mono.
- Analyzer-Save nur bei vollständigem 1-kHz-Audiofenster, RUN oder beim PAUSE eingefrorenem Fenster und freier gemeinsamer Tagesquote. Kein zweiter AudioRecord; lokale WAV/JSON/Manifest-Dateien inkl. SHA-256.
- Beobachter erstellt zusätzlich lokalen JSON-Sidecar zu bestehender WAV; bisheriger Supabase-Datenvertrag und Upload bleiben unverändert. LIVE-Analyzer-Setup nur in Analyzer-Sidecar.
- Rolling-Beobachteraufnahme: bisheriger Roh-PCM-Pfad wendet nun wie die Direktaufnahme den konfigurierten WAV-Gain einmalig auf die exportierte Aufnahme an; Roh-Rolling-Ring bleibt unverändert.
- Einmalige +Gain-Kompensation für Analyzer-FFT-Darstellung/Wasserfall und 1-kHz-Statistiken; Filterstatistik bis 40 nominale lokale Biquad-Bänder. Originalgetreue 8-kHz-FAST-Statistik, Impulse, Modulation und SPL-Kalibrierung weiterhin offen.
- Quellstand; Host-/statische Checks separat in `BUILD_VERIFICATION_v0.5.18.md` dokumentieren. Android-Gradle-Build und Xiaomi-Regression **NOT RUN**. Projekt-original v0.5.17 unverändert aufbewahren.

---
# LFN-Scout v0.5.17

Android v0.5.17 · `versionCode 160` · 24.09.2026

## Neu in v0.5.17

Die gesamte App bleibt im Hochformat – auch wenn das Smartphone quer gehalten wird. Das betrifft Hauptansicht/Analyzer und die eigene Erinnerungsansicht. Das Drehen ist kein Grund, den laufenden Analyzer auszublenden oder Audio/FFT/Wasserfall anzuhalten. Es gibt keine Kotlin- oder Audio-/DSP-Änderung. Für den konkreten Exportkonflikt v0.5.16 wurden vom Nutzer alte und neue JSON-Dateien erfolgreich nachgewiesen; vollständige Tests bleiben getrennt erforderlich.

**Status:** Manifest-Quelländerung; Android-Build und Xiaomi-Rotationstest v0.5.17 noch nicht durchgeführt. Siehe `BUILD_VERIFICATION_v0.5.17.md` und `docs/project/TESTPLAN_v0.5.17.md`.

---
# LFN-Scout v0.5.16

Android v0.5.16 · `versionCode 159` · 24.09.2026

## Neu in v0.5.16

- Fehlerkorrektur für `Export fehlgeschlagen: IllegalStateException` bei Konflikt zwischen älterer Download-JSON und internem Setup. Betroffene Altdatei bleibt unverändert; exportierte interne Konfiguration landet in `Setup_N_CURRENT_v2.json` statt denselben Dateinamen zu überschreiben.
- Genauere Fehlermeldungen und `Settings/setup_migration.json` im Diagnose-ZIP für ausstehende und fehlgeschlagene Migrationen.
- Alte v1-Datei nur bei erfolgreichem Import, Preference-Readback und erfolgreicher Löschung als erledigt markieren. Kein automatisch entschiedener Konflikt; kein allgemeiner Import und keine interne Schema-v2-Migration.
- **Status:** Quell-/statische Prüfungen; Android-/Compose-Build und Xiaomi-Test NICHT AUSGEFÜHRT. Testplan: `docs/project/TESTPLAN_v0.5.16.md`; Verifikation: `BUILD_VERIFICATION_v0.5.16.md`.

---

## Neu in v0.5.15

Alte, zugängliche v1-Setup-JSON-Dateien werden vor dem neuen v2-Export einmalig konfliktbewusst eingelesen, nach erfolgreichem Readback gelöscht. Bei Fehler oder internem Konflikt bleiben sie erhalten und der Export wird blockiert. Kein allgemeiner JSON-Import; siehe `docs/project/LEGACY_SETUP_MIGRATION_v0.5.15.md`.

## Neu in v0.5.14

Gemeinsame neunteilige Analyzer-Einstellungsstruktur, sichtbare ausgegraute Werte für modusspezifisch nicht unterstützte Parameter und vergleichbare schlüsselvollständige read-only-JSON-v2-Exporte mit `null` für fehlende Altwerte. Keine Änderung am Audio-/FFT-Laufzeitpfad; Android- und Xiaomi-Abnahme ausstehend. Direkt-Zeitkonstante im Sekundenmodus jetzt editierbar. Siehe `docs/project/UNIFIED_ANALYZER_SETTINGS_v0.5.14.md`.

# LFN-Scout v0.5.14

Android v0.5.14 · `versionCode 157` · Quellstand 24.09.2026

> **Aktueller Quellkandidat:** [Projektstatus](PROJECT_STATUS.md) · [Changelog](CHANGELOG.md) · [Buildprüfung](BUILD_VERIFICATION_v0.5.14.md) · [Geräte-Testplan](docs/project/TESTPLAN_v0.5.14.md). Android-/Compose-Build und Xiaomi-Abnahme v0.5.14 **NOT RUN**. Frühere Versions- und Testaussagen sind historisch.

## Neu in v0.5.13

- **Wasserfall-Pop-up:** Die bisher dauerhaft unter dem Wasserfall eingeblendeten Informationen `Wasserfall …` / `FILTER: Band …` / FILTER-Autorange-Status erscheinen nun beim Darstellungswechsel als zweizeiliges, fünf Sekunden sichtbares Pop-up **unten rechts direkt über dem Wasserfall**. Vorige Schriftgrößen 10/9 sp bleiben erhalten; Text wird platzsparend formuliert.
- **Kein Eingriff in die Darstellung:** Original-/Filterwasserfall und Mischbild bleiben unverändert. Pop-up wird ausschließlich auf dem Bildschirm über den vorhandenen Wasserfall gelegt; keine Änderungen an Frequenzflucht, Datum/Uhrzeit-Ticks, Pegel-Colorbar, PNG/JPG und laufender Analyse. Die Kamera-Erfolgsbenachrichtigung bleibt eigenständig.
- **Stabiler Anzeigewechsel:** Neue FFT-Zeilen und periodische Messwertaktualisierungen starten das Pop-up nicht neu. ORIGINAL- und FILTER-Buttons einschließlich ihrer verbindlichen Symbole und FILTER-Langdruck bleiben wie in v0.5.12.
- **Prüfstatus:** Kotlin-JVM-Format-Selbsttest und statischer Quellvertrag separat protokolliert; Android-/Compose-Build und Xiaomi **NOT RUN**, keine Gerätefreigabe. Siehe [`docs/project/ANALYZER_WATERFALL_NOTICE_v0.5.13.md`](docs/project/ANALYZER_WATERFALL_NOTICE_v0.5.13.md).

---

# LFN-Scout v0.5.12 – historisch

Android v0.5.12 · `versionCode 155` · Dokumentationsrevision 24.09.2026 (ohne Änderung der App-Version)

> **Aktueller Quellkandidat:** [Projektstatus](PROJECT_STATUS.md) · [Changelog](CHANGELOG.md) · [Buildprüfung](BUILD_VERIFICATION_v0.5.12.md) · [Geräte-Testplan](docs/project/TESTPLAN_v0.5.12.md). Android-/Compose-Build und Xiaomi-Test für v0.5.12 **NOT RUN**. Ältere Versions- und Testaussagen im folgenden kumulativen Verlauf sind historisch und keine Freigabe für v0.5.12.

## Neu in v0.5.12

- **ORIGINAL-Button:** Das exakt vom Nutzer gelieferte Wasserfall-PNG ersetzt das bisherige synthetische Symbol; 48 dp wie Kamera. FILTER behält das festgelegte Symbol mit fünf symmetrischen Magenta/Rosa/Weiß-Balken. Kurz-Tipp schaltet weiterhin jeweils nur den zugehörigen Wasserfall EIN/AUS.
- **FILTER-Pegelautomatik:** Langer Druck auf FILTER ermittelt ausschließlich aus tatsächlichen Filterband-RMS-Pegeln eigene dBFS-Anzeigegrenzen; Vorgabe 2 s Erfassung, einstellbare Dauer 1–30 s und Reserve 0–30 dB (Vorgabe ±5 dB). ORIGINAL-Autorange bleibt unabhängig. Die ermittelten Grenzen werden bis zur nächsten Änderung festgehalten.
- **Anzeige, Speicherung, Setups:** Separate FILTER MIN/MAX-dBFS-Werte in der FILTER-Colorbar und bei PNG/JPG; Werte, Erfassungsdauer und Reserve persistent in den 8-kHz-Profilen und Setups 1–3. Die Audio-/Rolling- und Direkt-Analyzer-Wege wurden nicht absichtlich geändert.
- **Prüfstatus:** FILTER-Hosttest 10/10, Renderer-Hosttest 5/5, statische Integrationsprüfung 18/18 sowie Direkt-Core-Hosttest 33/33 PASS. Älterer v0.5.06-Selbsttest TIMEOUT, nicht PASS. Vollständiger Android-/Compose-Build und Xiaomi-Test **NOT RUN**. FILTER-Colorbar-Tap, vollständige Impulsereigniserkennung und 16:1-Pegelabweichung weiterhin offen. Siehe [`docs/project/FILTER_AUTORANGE_AND_ICONS_v0.5.12.md`](docs/project/FILTER_AUTORANGE_AND_ICONS_v0.5.12.md).

---

# LFN-Scout v0.5.11 – historisch

## Neu in v0.5.11

- **FAST-Filterwasserfall im 8-kHz-Analyzer (Stufe A):** Filterbank aus dem bestehenden 8-kHz-PCM-Datenstrom, ohne zweite Mikrofonaufnahme; 2–40 Filterbänder (Standard 10) mit eigenen FILTER MIN/MAX-Frequenzgrenzen. Separater FILTER-Wasserfall mit Magenta/Rosa/Weiß-LUT und digitaler dBFS-Colorbar.
- **Bedienung:** ORIGINAL und FILTER als unabhängige 48-dp-Buttons; ORIGINAL allein, FILTER allein, beide mit transientabhängiger Farbmischung oder beide AUS. Zeit- und Frequenzzuordnung sowie PNG/JPG-Rendering in den bestehenden Wasserfall übernommen. Das ORIGINAL-Symbol der damaligen Version wurde mit v0.5.12 ersetzt.
- **Direkt-Analyzer:** 5-ms-FFT-Hop ergänzt; Glättungszeit in Sekunden (einschließlich 5 s) als zusätzliche Einstellmöglichkeit. Neue Einstellungen und Setups mitgeführt.
- **Prüfstatus:** Neuer FAST-JVM-Hosttest 11/11 und Direkt-Core-Hosttest 33/33 PASS; Kotlin-PSI-Syntaxprüfung ohne Fehler. Android-/Compose-Build und Xiaomi v0.5.11 **NOT RUN**. Separate Impulsereignisse, Onset-Erkennung und Ringing-Fusion nicht implementiert; 16:1-Pegelabweichung offen. Siehe [`docs/project/FILTERBANK_v0.5.11.md`](docs/project/FILTERBANK_v0.5.11.md).

---

# LFN-Scout v0.5.10 – historisch

## Neu in v0.5.10

- **Kotlin-Buildkorrektur:** Nach nachgewiesenem Windows-Gradle-FAIL von v0.5.09 wurde in `LiveAnalyzerController.setTiming` die unzulässige gemeinsame `contains`-Prüfung über `Set<Int>` und `IntArray` durch zwei typsichere Zweige ersetzt. Zulässige FFT-Hops und Audio-/Rolling-Semantik bleiben unverändert.
- **Prüfstatus:** Separater Kotlin-Regressionstest und statische Quellprüfung PASS; vollständiger Android-/Compose-Build und Xiaomi v0.5.10 weiterhin nicht nachgewiesen. Siehe [`BUILD_VERIFICATION_v0.5.10.md`](BUILD_VERIFICATION_v0.5.10.md).

---

# LFN-Scout v0.5.09 – historisch

## Neu in v0.5.09

- **Konfigurierbarer Direkt-Analyzer:** Wählbare Audioquelle (AUTO/UNPROCESSED/MIC/VOICE_RECOGNITION), Eingangsrate 8000–96000 Hz und neun FFT-Größen 64–16384; neun Fensterfunktionen, direkter Dezimierungsfaktor 0–12 (0 = AUS), sieben Transformationsintervalle 10–100 ms, Glättungsfaktor 0,00–1,00.
- **Weitere Analyseoptionen:** 0–2 Peakmarker, Stay Awake, DC-Abzug und zwei unabhängig schaltbare digitale Testtöne. Persistente Konfiguration einschließlich Setup 4; exklusive Audio-/Rolling-Betriebsarten beibehalten.
- **Prüfstatus:** DSP-Hosttest 33/33 PASS; Windows-Gradle-Build von v0.5.09 nachweislich FAIL, durch v0.5.10 gezielt adressiert. Xiaomi-Test für v0.5.09 nicht durchgeführt. Siehe [`docs/project/DIRECT_ANALYZER_v0.5.09.md`](docs/project/DIRECT_ANALYZER_v0.5.09.md).

---

# LFN-Scout v0.5.08 – historisch

## Neu in v0.5.08

- Zeitband `dd.MM / HH:mm` mit 1/2/5/10/20/30/60-minütigen echten Zeitmarken; Farbskala und identische Frequenzgrenzen bleiben erhalten.
- PNG/JPG mit denselben Zeitmarken; Kamera-Speicherbestätigung in der Nähe des Kamerasymbols.
- Keine Änderung des Audio-/FFT-/Rolling-/WAV-Zweigs; 16:1-Pegelabweichung weiterhin offen.
- Android-/Compose-Build sowie Xiaomi-Test dieser Version ausstehend.

---

> **Historischer Versionshinweis – v0.5.07 / versionCode 150 (21.09.2026):** Korrektur des fehlgeschlagenen Android-Builds von v0.5.06: Maxhold-Reset am Subscriber ergänzt, fehlende Klammer der UI-Scroll-Column repariert. [Status](PROJECT_STATUS.md), [Prüfbericht](BUILD_VERIFICATION_v0.5.07.md), [Gerätetest](docs/project/TESTPLAN_v0.5.07.md). Android-/Compose-Build und Xiaomi v0.5.07 **NOT RUN**; 16:1-Pegelabweichung offen. Ältere `AKTUELL`-Zeilen unten sind historisch.

# LFN-Scout v0.5.07 – historisch

## Neu in v0.5.07

- Gezielte Korrekturen zweier belegter v0.5.06-Kompilierursachen (`AnalyzerRawLfSession.resetMaxHold` und UI-Scope).
- Der Kamera-Pop-up bleibt im äußeren Viewport; MAX/CLR und Spektrumkurven bleiben wie in v0.5.06 vorgesehen.
- Keine beabsichtigte Änderung an Audio-/Rolling-Betriebsarten, WAV, Einstellungen und Exporten. Kein bestätigter Android-Build dieser Korrektur; zuerst Windows-Gradle-Tests.

---

# LFN-Scout v0.5.06 – historisch

## Neu in v0.5.06

- **Spektrumkurven:** Aktuell (gelb), Average (rot) und Maxhold (türkis) unabhängig schaltbar; grauer Rauschboden separat. Persistenz in Setup-Slots 1–4 und Betriebsprofilen. MAX/CLR nur bei sichtbar geschaltetem Maxhold, maximal 48 dp, ohne Einfluss auf Average/Wasserfall/Audio.
- **Manuelles Bild:** Kamera-Langdruck löst Datei-Export mit Abschlussereignis und gut sichtbarem Pop-up erst nach dem Schreiben oder bei Fehler aus; automatische Bilder lösen keine falsche manuelle Erfolgsmeldung aus.
- **Prüfstatus:** Host-Selbsttest 8/8 und statischer Quellvertrag 21/21 PASS; echter Nutzer-Windows-Build v0.5.06 anschließend **FAIL** wegen Kotlin-Kompilierfehlern, in v0.5.07 gezielt korrigiert. Xiaomi v0.5.06 nicht bestätigt. Ältere Nutzerabnahme v0.5.05 galt bis auf das Kamera-Pop-up; 16:1-Pegelabweichung offen. Siehe [`BUILD_VERIFICATION_v0.5.06.md`](BUILD_VERIFICATION_v0.5.06.md).

---

> **Historischer Versionshinweis – v0.5.05 / versionCode 148 (21.09.2026):** Zeitbasis-Fix für 48-kHz-STOP + Panel verlassen, PAUSE wieder sichtbar, JSON-Setup- und Diagnoseexport. Vollständiger Android-Build und Xiaomi-Gerätetest noch ausstehend. Siehe [Status](PROJECT_STATUS.md) und [Testplan](docs/project/TESTPLAN_v0.5.05.md). Alle hiernach aufgeführten Versionsangaben sind historisch.

# LFN-Scout v0.5.05

## Neu in v0.5.05

- **Versionsregel:** keine zusätzliche vierte Versionskomponente erforderlich; v0.5.05 / versionCode 148 ist die nächste normale Version nach 0.5.04.2 / 147.
- **Crash-Fix im Quellcode:** Nach 48 kHz → STOP → Panel verlassen wurde `direct48k=false` gesetzt, aber der im Controller verbliebene Faktor 1 bei einer UI-Neuberechnung an `AnalyzerTiming.lfRateHz()` übergeben. Synchrones Zurückladen der 8-kHz-Controllerwerte; Snapshot-Sample-Rate mit explizitem Fallback für ungültige inaktive Zwischenzustände. JUnit-Regression `restoredIdleModeNeverCrashesOnOldWidebandFactor` (HOST PASS; noch kein Android-/Xiaomi-Nachweis).
- Rolling-Restore protokolliert `RESTORE_RESULT`, wartet bei zuvor aktivem Rolling auf echte Engine/Owner-Bestätigung; `RESTORE_DONE` wird erst bei bestätigter Wiederherstellung geschrieben. Bei Timeout bleibt das Panel offen, die Wiederherstellung ist offen; keine falsche Erfolgsbestätigung.
- **PAUSE** wieder als separate Schaltfläche neben RUN/STOP vorhanden; `setPaused` unterbricht Spektrum/Wasserfall-Fortschreibung, ohne den Rolling-Betriebsmodus zu ändern. STOP + Panel verlassen hat weiterhin eigene Semantik.
- **Lesbarer Export:** vorhandene Setups 1–4 werden nach Update und beim Speichern als JSON-Kopien unter `Download/LFN-Scout/Setups/` bereitgestellt; separate Schaltflächen unter Einstellungen → Setups/Diagnose. Diagnose-ZIP enthält nur explizit freigegebene Analyzer-/Test-/Impuls-Einstellungsfelder und `AUDIO_DIAG_*`-Zeilen. Keine Beobachtungsdaten, Zugangsschlüssel oder vollständiger privater Dateidump. Vorhandene Setups bleiben intern führend, JSON-Import nicht implementiert.
- **Nicht als behoben behaupten:** Pegelabweichung 16:1; Android-/Compose-Build und Xiaomi-Abnahme dieses Standes; MAX/CLR- und Kurvensteuerung weiterhin geplant. Die vom Nutzer bestätigten 8-kHz-Modi wurden nicht absichtlich funktional verändert.


---

> **Historischer Versionshinweis –: LFN-Scout v0.5.04.2 / versionCode 147 (21.09.2026).** Diagnoserelease auf Basis v0.5.04.1. Der 8-kHz-Modus funktioniert laut Nutzer. Logging für sporadische 48-kHz-/Rolling-Abstürze ergänzt. Android-Build und Xiaomi-Test für diesen Stand **offen**. [Status](PROJECT_STATUS.md) · [Diagnose](docs/project/AUDIO_DIAGNOSE_v0.5.04.2.md) · [Testplan](docs/project/TESTPLAN_v0.5.04.2.md).

# LFN-Scout v0.5.04.2

## Neu in v0.5.04.2

- Pro Sitzung identifizierbare Audio-/Rolling-Statusereignisse in App-Debuglog und Logcat (Tag `LFN-AUDIO`).
- Diagnose vor/nach Start, STOP, Panel-Verlassen, Rolling-Restore und erneuter 48-kHz-Erfassung, inklusive AudioRecord-Status und periodischer PCM-Heartbeat.
- Keine funktionale Änderung des 8-kHz-Modus, keine behauptete Absturzkorrektur. Fehlende PAUSE und 16:1-Pegelabweichung bleiben offen. Android-/Gerätetest noch offen.

> **Historischer Versionshinweis – LFN-Scout v0.5.04.1 / versionCode 146 – Buildkorrektur.** [Status](PROJECT_STATUS.md) · [Prüfbericht](BUILD_VERIFICATION_v0.5.04.1.md) · [Gerätetest](docs/project/TESTPLAN_v0.5.04.md). Android-/Compose-Build auf dem Windows-PC erneut ausstehend. v0.5.04 schlug mit zwei JUnit-Fehlern und einem Kotlin-Smart-Cast-Fehler fehl.

# LFN-Scout v0.5.04.1

## Neu in v0.5.04.1

- Kotlin-Smart-Cast-Fehler im Autorange-Callback korrigiert; JUnit-Zeitbasisprüfungen nach LF- und 48-kHz-Betrieb getrennt, zusätzlicher 48-kHz-Fall.
- Keine Änderung an Audio-Betriebsart, Rolling-Status, FFT-Algorithmus oder Bildexport. Android-Build und Xiaomi v0.5.04.1 bleiben **NOT RUN**; 16:1-Pegelabweichung bleibt offen.

> **Historischer Versionshinweis – LFN-Scout v0.5.04 / versionCode 145.** [Projektstatus](PROJECT_STATUS.md) · [Implementierung](docs/project/ANALYZER_v0.5.04.md) · [Geräte-Testplan](docs/project/TESTPLAN_v0.5.04.md). Android-/Compose-Build und Xiaomi für v0.5.04 NOT RUN; v0.5.03 16:1-Pegelabweichung offen.

# LFN-Scout v0.5.04

## Neu in v0.5.04

- Sitzungsabhängige Audio-Betriebsarten: 8-kHz-Rolling oder exklusiver 48-kHz-Analyzer, optional digitaler Test 50/195 Hz. STOP hält die Betriebsart aufrecht; Rolling-Restore erst nach STOP **und** Panelverlassen.
- Eigenes 48-kHz-Profil/Setup 4, 0–22 kHz, FFT 8192/16384; Langzeitwasserfall bis 14400 s mit begrenzter Zeilenzahl; Akt/MW/MAX-Moduswechsel erhält frühere Historie.
- Erfolgs-/Fehlerrückmeldung beim Kameralangdruck; Dateiname/Bild mit TESTSIGNAL-Hinweis bei Digitalquelle. Bereits gespeicherte WAV-Dateien bleiben unberührt; Rolling ist ein RAM-Puffer, keine neu implementierte kontinuierliche WAV-Diskaufnahme.
- Hosttest v0.5.04 16/16 und statischer Vertrag 20/20 PASS; Android-Build/Xiaomi **NOT RUN**. Ausführlich unter `BUILD_VERIFICATION_v0.5.04.md`.

> **Historischer Versionshinweis – LFN-Scout v0.5.03 / 144.** [Projektstatus](PROJECT_STATUS.md) · [Implementierung](docs/project/ANALYZER_v0.5.03.md) · [Geräte-Testplan](docs/project/TESTPLAN_v0.5.03.md). v0.5.02: Nutzer meldete Testplan vollständig PASS. v0.5.03: Android-Build/Gerät noch NOT RUN.

# LFN-Scout v0.5.03

## Neu in v0.5.03

- Decimation 4:1, 8:1, 16:1 mit eigener Analyzer-FIR-Anti-Alias-Filterung und korrekter FFT-Zeitbasis.
- Gleiche Cursor-Readout-Höhe; vertikale Cursorlinie bis zum Wasserfallende, einschließlich Bildexport.
- Benannte und beschriebene lokale Setup-Slots; neue Statistik-Frequenzgrenzen/Zeitfenster/Average Time persistent und grafisch synchron.
- 20/20 synthetische FIR-/Timing-Hosttests; Android-/Compose-Build und Xiaomi-Gerätetest ausstehend.


> **Historischer Versionshinweis – Quellstand v0.5.02 / 143:** [PROJECT_STATUS.md](PROJECT_STATUS.md) und [Analyzer v0.5.02](docs/project/ANALYZER_v0.5.02.md). Die v0.5.01-Xiaomi-Tests sind Nutzerberichte; **Hz-Abnahme war FAIL**. v0.5.02 Android-/Geräteprüfung steht aus.

# LFN-Scout v0.5.02

## Neu in v0.5.02

- Hz korrekt mittig zwischen Ticks; Wasserfall-Kennzeichnung Akt/MW/MAX; schmale Farbleiste und direkte dBFS-Zahleneingabe ohne ±-Buttons.
- Frei eingebbare Wasserfallzeit in Sekunden, dynamische PNG/JPG-Zyklen und persistente Setups.
- FFT-Hop 5; FFT Size, frei gewählte Anzeige-Frequenzgrenzen, Window, Transformationsintervall und Smoothing/Faktor unter Spektrum. Decimation weiterhin fest 8:1.
- Host-Tests 15/15 und vorhandene Wasserfall-/Timing-Selbsttests PASS. Android-Build und Xiaomi-Test **noch nicht ausgeführt**.

> **Historischer Versionshinweis – Code v0.5.01 (20.09.2026):** [Projektstatus](PROJECT_STATUS.md) · [Änderungen und neuer Gerätetest](docs/project/ANALYZER_CONTROLS_v0.5.01.md). Für diese Version kein Android-Build/kein Xiaomi-Test; ältere Angaben zu v0.5.00 sind historisch.

# LFN-Scout v0.5.01

Android v0.5.01

## Neu in v0.5.01

- Kamerasymbol kurz: Auto EIN/AUS; bei AUS rotes X statt Statuswort. Lang: manuelles Bild, ohne den Automatikschalter zu ändern.
- Bildformat PNG/JPG und JPG-Qualität unter Automatiken; persistent einschließlich Setup-Plätzen 1–3. Gemeinsame Auswahl für automatischen und manuellen Export.
- Hz platzsparend zwischen Frequenz-Ticks, gleiche Achsenzeile bei Spektrum/Wasserfall und Export.
- Rückpfeil/Android-Back je eine Einstellungsebene; kontrastreiche dBFS-Eingaben und Autorange-/Preset-Übernahme bei nicht fokussierten Feldern.
- Bestehende WAV-/Rolling-/DSP-/Backend-Funktionen nicht umgebaut. Android-Build und Xiaomi v0.5.01 offen. Siehe `docs/project/ANALYZER_CONTROLS_v0.5.01.md`.

> **Übergaberevision 20.09.2026 (Dokumentation, keine neue App-Version):** [`PROJECT_STATUS.md`](PROJECT_STATUS.md) und [`docs/project/UEBERGABE_v0.5.00_2026-09-20.md`](docs/project/UEBERGABE_v0.5.00_2026-09-20.md) sind der konsolidierte Einstieg. v0.5.00 ist ein **nicht auf Xiaomi/Android gebaute bzw. abgenommener Testkandidat**; letzter Screenshot des realen Analyzerlaufs stammt aus v0.4.99. Historische Erfolgsmeldungen nicht auf v0.5.00 übertragen.

# LFN-Scout v0.5.00

Android v0.5.00

## Neu in v0.5.00

- Vollflächiger dunkel hinterlegter Analyzer: kein freigelassener weißer Restbereich. Diagrammhöhen passen sich an die verfügbare Viewport-Höhe an; auf kleinen Displays bleibt der Scroll-Fallback erhalten.
- `Mithören`/`Superfast` (weiterhin nicht implementiert) und redundante Statuskarte `Anzeige` entfallen aus dem Hauptbild. Auch die unteren Buttons `Einstellungen`/`Diagnose` und die dauerhafte Hinweiszeile entfallen. Unten bleiben **PNG** und **Auto EIN/AUS**.
- Die Schalter für Mittelwert/Rauschboden befinden sich weiterhin im Einstellungs-Unterpanel `Spektrum`; Diagnose unter `Diagnose`. Keine Audio-/DSP-/WAV-Änderung. `versionName=0.5.00`, `versionCode=141`; Android-/Xiaomi-Test offen. Siehe `docs/project/ANALYZER_LAYOUT_v0.5.00.md`.

## Neu in v0.4.99

- Vollständiges einspaltiges Analyzer-Einstellpanel: aktives lokales Setup (Plätze 1–3) oben, acht Kategorien mit Rücknavigation: Spektrum, Audio, Anzeige, Statistik, Automatiken, Kalibrierung, Setups, Diagnose. Kein altes horizontales Registerband.
- **Manuelle dBFS-Min- und Max-Grenzen** im Bereich Anzeige, jeweils frei editierbar, geprüft und sofort persistent gespeichert (auch in den Setup-Slots). Presets und Achsen-Autorange zusätzlich erhalten.
- Funktionsfähige Optionen aus dem bisherigen Panel umsortiert, darunter FFT-Hop, Analysegain, 3 Wasserfall-Modi, Wasserfall-Zeit, Auto-PNG und Statistikreset. Unimplementierte Audio-/Kalibrierfunktionen ohne unwirksame Schalter gekennzeichnet.
- PNG-Ziel und Dateiname im Bereich Automatiken erläutert: ab Android 10 via MediaStore `Download/LFN-Scout/LFN-Scout_Analyzer_yyyy-MM-dd_HH-mm-ss_SSS.png`. Auf Android 9 und älter im app-spezifischen `Android/data/de.brummbeobachter.app/files/Pictures/Analyzer/`.
- `versionName=0.4.99` / `versionCode=140`. Android-/Compose-Kompilierung und Xiaomi-Gerätetest sind noch nicht durchgeführt. Siehe `docs/project/ANALYZER_SETTINGS_PANEL_v0.4.99.md`.

## Neu in v0.4.98

- Wasserfall-Modus wählbar: Aktuell (wie bisher), Mittelwert der linearen FFT-Leistung je Zeilenintervall oder binweises Maximum. Kein Max-Hold in der Spektrum-UI.
- Persistenz der Wasserfall-Auswertung in Preferences und Setups 1–3; Moduswechsel beginnt neue Wasserfall-Historie/PNG-Zyklus. PNG enthält Modusnamen.
- `docs/project/ANALYZER_SETTINGS_PANEL_VORSCHLAG_v0.4.98.md` bündelt alle Analyzer-Parameter in geplante Unterpanels; UI-Neuordnung bleibt nach Nutzerfreigabe vorbehalten.
- Version 0.4.98 / 139; Host-Tests und statische Prüfungen dokumentiert. Android-Build/Xiaomi-Abnahme offen.

## Neu in v0.4.97

- Autorange gegen kurze negative Einbrüche und einzelne schmale Spitzen robuster (Perzentile über Frequenz und Zeit); feste ±5 dB Reserve und persistente Grenzen bleiben.
- Cursor-Hz/dBFS nur als Text, ohne Rahmen/Hintergrund. Roter L50/L90-Resetrahmen passt sich mit 3 dp Innenabstand dem Text an, keine Füllung.
- Linke Farbskala ein Drittel schmaler; Spektrum und Wasserfall verwenden die gleiche vergrößerte Frequenzplotbreite. Offscreen-PNG angepasst.
- Version 0.4.97 / 138. Host- und statische Prüfungen PASS; vollständiger Android-Build und Xiaomi-Freigabe OFFEN. Details: `docs/project/ANALYZER_LIVE_v0.4.97.md`.

## Neu in v0.4.96

- Autorange durch Tipp auf die linke dBFS-Achse: 2 s gültige FFT-Pegel erfassen, feste ±5 dB, Dauer/Reserve einstellbar; beide Grenzen nach Abschluss sofort übernehmen und für nächsten Start speichern.
- L50(2–25)/L90(2–25) in schmalem roten abgerundeten Rahmen: Tipp setzt Statistik und FFT-Averaging zurück, Wasserfall bleibt erhalten.
- Fingerziehbarer Cursor mit Hz-/dBFS-Readout an der oberen Spitze; Tipp aufs Readout schaltet den Cursor aus. Dünnere Kurven und mehr Höhe fürs Spektrum.
- Farbskala links mit innenstehenden dBFS-Zahlen, exakt fluchtende Hz-Achsen; überzählige Wasserfall-Überschrift und Achsenerklärungszeile entfernt. PNG-Layout angepasst.
- `versionName=0.4.96`, `versionCode=137`; Gradle-/Xiaomi-Test dieser Version noch offen. Details: `docs/project/ANALYZER_LIVE_v0.4.96.md`.

## Neu in v0.4.95.1

- Nutzer-Build der v0.4.95 scheiterte an drei `Unresolved reference 'acceptDiagnostics'` in `LiveAnalyzerController.kt` (Zeilen 110/136/174). Die drei Übergaben waren vorhanden, aber die private Callback-Methode war beim UI-Umbau entfallen.
- `private fun acceptDiagnostics(diag: AnalyzerRawLfSession.Diagnostics)` wiederhergestellt. Sie nimmt Diagnosewerte unter dem bestehenden Controller-Lock entgegen, sofern der Analyzer läuft. Keine Änderung an PCM-Fan-out, FIR/FFT, Wasserfall-Timing, WAV, Rolling-Buffer oder den v0.4.95-Anzeigevorgaben.
- Regression `verify_v04951_diagnostics_contract.py` prüft jetzt die drei Callback-Übergaben **und die vorhandene Methodendeklaration mit passendem Typ**, damit die statische Prüfung diesen Fehler künftig erkennt.
- `versionName = "0.4.95.1"`, `versionCode = 136`, `applicationId` unverändert. Autorange durch Tipp auf die dBFS-Achse (2 s / einstellbare ±5 dB / persistente Grenzen) bleibt für v0.4.96 geplant und wurde in diesem Build-Hotfix **nicht** implementiert.
- Der Nutzer hat den fehlgeschlagenen Windows-/Android-Kompilierlauf für v0.4.95 vorgelegt. Vollständiger Android-Build und Xiaomi-Test für v0.4.95.1 noch offen.

## Neu in v0.4.95

- Arbeits-Preset **−60 bis −120 dBFS** (oben −60, unten −120) für Spektrum und Wasserfall samt Farbleiste/PNG; Anzeige-Presets −40…−120 und 0…−120 optional; keine SPL-Kalibrierung.
- FFT-Hop zusätzlich 10 LF-Samples (rechnerisch 100 FFT/s; nicht garantierte Bildrate). Default 100 bleibt. Wasserfall-Intervall zeitbasiert 100/200/250/500/1000 ms, Default 250 ms: 480 Zeilen = 120 s. Bei FFT-Hops 500/1000 ms wird ein zu kurzes Wasserfallintervall automatisch auf die reale Spektrumkadenz angehoben. 1-kHz-FIR, bestehender Recorder und WAV unverändert.
- Haupttitel `LFN-Scout · 0–100 Hz`, darunter einmalig die Statuszeile `Live · 8 kHz → 1 kHz FIR · dBFS · Gain +19 dB uncal` (Gain ist beispielhaft und zeigt den tatsächlichen Wert). Doppelte Spektrum-/Gain-Überschrift und Kurvenlegende entfernt; L50(2–25)/L90(2–25) oben rechts im Diagramm. Mehr Höhe fürs Spektrum. Rauschboden-Kurve Default AUS; im GUI umschaltbar, Zustand und Anzeigebereich persistent und in Setups 1–3 gespeichert.
- UI-/PNG-dBFS-Achsen und Farbskala verwenden denselben gewählten Anzeigebereich. Auto-PNG weiterhin erst nach vollen 120 Sekunden und frühestens nach jedem weiteren 120-s-Zyklus.
- Der Nutzer bestätigte für v0.4.94 sichtbares Spektrum/Wasserfall und vorliegende Diagnose mit 0 Drops/Resets/Fehlern; dies gilt nicht als v0.4.95-Gerätetest. Android-Gradle-/Compose-Build und Xiaomi-Visual-Abnahme dieser Version offen.

## Neu in v0.4.94

- Live-Spektrum mit getrennt einstellbarem FFT-Hop (50/100/200/500/1000 LF-Samples; Standard 100) und schnellerem UI-Polling (100 ms; bei 50 Samples 50 ms). Frequenzauflösung unverändert bei 1000/4096 ≈ 0,244 Hz.
- Wasserfall-Hop 1/2/5/10 Spektren (Standard 5); Zeitachse von oben nach unten, Frequenz horizontal. Dynamische ~120-s-Historie; dBFS- und Frequenzachsen mit echten Tickmarken, eigenständige Einheitsbeschriftungen. PNG-Renderer angepasst.
- Neue Parameter in lokalen Preferences und drei Setups. Der bestehende funktionierende 8-kHz-Roh-PCM-/1-kHz-LF-Pfad, Rolling-Buffer und WAV-Aufnahme werden nicht verändert.
- Teststatus: Host-Kotlin- und statische Checks PASS; vollständiger Android-Build, PNG-/Langzeit- und Xiaomi-UI-Test **ausstehend**. `BUILD_VERIFICATION_v0.4.94.md` und `docs/project/ANALYZER_LIVE_v0.4.94.md`.

# LFN-Scout v0.4.93

Android v0.4.93



## Neu in v0.4.93

- Kompilierfehler aus dem v0.4.92-Windows-Test korrigiert: `LiveAnalyzerController` verwendet jetzt `AnalyzerRawLfSession.callbackErrors` statt der nicht vorhandenen Eigenschaft `spectrumCallbackErrors`.
- Kein geändertes PCM/WAV/DSP/UI-Verhalten; Live-Kurve und Design auf Xiaomi weiterhin zu testen. `versionName=0.4.93`, `versionCode=133`, `applicationId` unverändert.
- Neue statische API-Regression: `python verify_v0493_live_analyzer_api.py`; Gradle-Tests, Android-Build und Gerätetest für v0.4.93 **NOT RUN**. Siehe `BUILD_VERIFICATION_v0.4.93.md`.

## Neu in v0.4.92

- Regressionstest für 8-kHz→1-kHz-FFT-Startup korrigiert: vor 4.096 LF-Samples ist die FFT-Ergebnisliste erwartungsgemäß leer; danach wird der Nullsignal-Pegel geprüft. Produktionsquellen unverändert.
- Version `0.4.92` / `132`; Android-Gradle- und Xiaomi-Prüfungen für diese Version noch offen. `BUILD_VERIFICATION_v0.4.92.md` enthält Befund und Testschritte.

## Neu in v0.4.91

- Fehlerbild v0.4.90: Nach 235/235 Wasserfallzeilen alle Kurven bei −240 dBFS; Ursache im bisherigen 1-kHz-Recorderzweig/Anzeigebereich nicht abschließend auf dem Gerät bewiesen. Statt die dB-Achse kosmetisch zu verschieben, verwendet der Live-Analyzer jetzt bereits vorhandene **unveränderte 8-kHz-Rohdaten** aus demselben Mikrofon-Capture, eigene 129-Tap-LF-FIR-Decimation auf 1 kHz und vollpräzise Double-PCM für die FFT.
- Eigener optionaler Analysegain 0–50 dB (Default 0), getrennt vom bestehenden WAV-Gain. Bei Quellenwechsel, Roh-PCM-Ausfall und ausschließlich numerischem −240-dBFS-Pegel wird **kein gültiges Nutzsignal vorgetäuscht**. Roh-/LF-RMS, Peaks, Rate, Datenalter, Drops und Resets sind als Diagnose sichtbar.
- UI an das vom Nutzer bereitgestellte dunkle Zwei-Zustands-Konzept angenähert: kompakte Kopfzeile, Spektrum mit 2–25-Hz-Band, klassischer Wasserfall in der Bild-Ausrichtung (Zeit horizontal, Frequenz vertikal), kompakte untere Karten und bedienbare PNG-/Einstellungsleiste. Fehlende Funktionen wie Mithören sind klar als nicht aktiv benannt.
- Vollständige WAV-/Rolling-Audio- und getrennte Impulsdiagnose bleiben unverändert. Keine neue Impulsanalyse, keine neuen Mikrofon-Clients, kein 48-kHz-/20-kHz-Modus. Version 0.4.91/131; Android-Build/Xiaomi-Test noch **NOT RUN**.
- Technische Details und Testplan: `docs/project/ANALYZER_LIVE_v0.4.91.md`.

## Neu in v0.4.90

- Neues Unterpanel **„Spektrum-Analyzer“** in Zusatzfunktionen mit realem 1-kHz-PCM aus vorhandenem Rolling-/Recorder-Pfad. Echte FFT-Live-Kurve, Mittelung, Rauschboden-Schätzung und klassischer Inferno-Wasserfall (keine Impulsanalyse).
- Lineare LF-Anzeige bis 20/50/100/200 Hz, Cursor, Pause/Fortsetzen, erweiterbarer Wasserfall, relativer L50/L90-Statistikbereich 2–25 Hz.
- Separater Controller behält Analyzer-Session bei internem Panelwechsel; manuelle/automatische Offscreen-PNGs. Drei lokale Setup-Slots für LF-Bereich und Auto-PNG.
- Bewusst noch offen: Mithören im Live-Panel, absolute SPL-Kalibrierung, Histogrammspalte, autarker Mic-Capture ohne Rolling/Recorder, 48-kHz-/20-kHz-Breitband, Impuls-Wasserfall und vollständige Android-/Xiaomi-Abnahme.
- `versionName = 0.4.90`, `versionCode = 130`, `applicationId` unverändert. Details unter `docs/project/ANALYZER_LIVE_v0.4.90.md`.

## Neu in v0.4.89

- Highspeed-Impulsdiagnose: Roh-PCM-Mindestschwelle (50–3000 PCM vor Analyse-Gain), adaptiven Rauschfaktor (1,5–12), bisherigen digitalen Gain (0–50 dB) getrennt einstellen.
- Drei Presets **Robust / Mittel / Empfindlich**, jeweils ohne Änderung des Gain. Werte gerätelokal gespeichert; zusätzliche Anzeige für letzten Block-Peak, geschätzte Rauschbasis, festen/adaptiven/effektiven Schwellenanteil.
- Diagnosedaten lokal in die Zwischenablage kopieren. Kein Export von Roh-PCM, Standort, Schlüssel oder Netzwerkdaten.
- versionName 0.4.89 / versionCode 129; keine Änderung von Aufnahme, WAV, Capture oder Paket-ID. Android- und Xiaomi-Tests dieses Stands noch offen.
- Technische Einzelheiten: `docs/project/ANALYZER_PHASE2F_SCHWELLE_v0.4.89.md`.

## Neu in v0.4.88

- Digitaler Diagnose-Gain 0–50 dB, der den festen Schwellwert im Roh-PCM-Äquivalent senkt; adaptive Rauschschwelle erhalten.
- Geräteeinsatz von v0.4.88 bislang nicht bestätigt. `docs/project/ANALYZER_PHASE2E_GAIN_v0.4.88.md`.

## Neu in v0.4.87

- Projektlokale JDK-17-Suche, Entwicklermodus-Diagnose für Roh-PCM, Impulsereignisse und Fehler. Auf Xiaomi bei 8 kHz mit 5/5 lauten Testimpulsen geprüft.

## Neu in v0.4.86

- `PREPARE_PROJECT.bat` richtet den lokalen JDK-17-Pfad nun **vor** dem Gradle-Start sowohl fuer `org.gradle.java.home` (Daemon) als auch `org.gradle.java.installations.paths` (Kotlin/Java-Toolchain im `analyzer-core`) ein.
- Keine fest codierten Fremdrechner-Pfade in der ausgelieferten `gradle.properties`; lokale Java-/SDK-Pfade werden erst auf dem jeweiligen Windows-PC erzeugt.
- `tools/configure-gradle-jdk17.ps1`, JDK-Selbsttest und genaue Anleitung fuer Android Studio / CLI ergaenzt. Keine Aenderung am Audio-/Analyzer-Produktivcode.
- **Teststatus:** Nutzer hat das v0.4.85-Vorgaengerpaket erfolgreich mit Android-Gradle getestet und gebaut; v0.4.86 erfordert einen neuen Windows-/Android-Studio-Lauf.

## Entwicklungsnachtrag Phase 2H – Seismik-Regressionskorrektur (weiterhin v0.4.85)

Zwei veraltete App-Contract-Tests prüfen jetzt den tatsächlichen gemeinsamen Bitmap-Helicorder und die realen Zoom-/Doppeltipp-Handler. Nur die Tests und Dokumentation wurden angepasst, nicht der App-Produktivcode. Isolierter Kotlin-Test des aktualisierten Testpaars: 6/6 PASS; kompletter Android-Gradle-Test/APK **noch offen**. Siehe [`docs/project/SEISMIC_REGRESSION_PHASE2H_FIX.md`](docs/project/SEISMIC_REGRESSION_PHASE2H_FIX.md).

## Entwicklungsnachtrag Analyzer Phase 2H – Highspeed (auf v0.4.85, keine neue APK)

Vor der bisherigen 8:1-Mittelung kann nun ein zusätzlicher, asynchroner **8-kHz-Rohdatenpfad ohne Recorder-Gain** für die Impulsdetektion abonniert werden. Der wiederverwendbare Kotlin-Detektor kann außerdem 48-kHz-Fixtures verarbeiten; der Android-Mikrofoneingang wurde **nicht** auf 48 kHz umgestellt. Das bisherige 1-kHz-WAV-Format bleibt unverändert. Kotlin-CLI-Selbsttests: PASS; Android-Build/Gerät: NOT RUN. Details: [`docs/project/ANALYZER_PHASE2_HIGHSPEED.md`](docs/project/ANALYZER_PHASE2_HIGHSPEED.md).

## Entwicklungsnachtrag Analyzer Phase 2 (auf v0.4.85, KEINE neue APK)

- Android-unabhängiger Analyzer-DSP als gemeinsames `:analyzer-core`-Modul für LFN-Scout und späteren Standalone-Host.
- PCM-Fan-out an bestehende Rolling-/Recorder-Wege angebunden, exklusive Mikrofon-Ownership, JUnit-Regressionstests und Kotlin-CLI-Selbsttest.
- Noch kein Analyzer-Panel, eigenständiger Analyzer-Capture, Offscreen-PNG, Mithören, Standalone-APK oder validierter Android-Build.
- Details in [`docs/project/ANALYZER_PHASE2_AUDIO_STANDALONE.md`](docs/project/ANALYZER_PHASE2_AUDIO_STANDALONE.md).

## Neu in v0.4.85

- Vollständige Supabase-Recovery-Unterlagen in den Projektstand aufgenommen: `schema_full.sql`, `data_full.sql`, `auth_schema.sql`, `storage_schema.sql`, `roles_original.sql`, lokale `roles.sql`, `project_config.json` und `secret_names.txt`.
- Supabase-Storage lokal gespiegelt und gegen Remote-Dateianzahl geprüft: **53/53 WAV-Dateien**.
- Remote und lokal identische fünf Edge Functions dokumentiert.
- Neue Rekonstruktionsanleitung `docs/reconstruction/21_SUPABASE_DISASTER_RECOVERY.md`.
- Echte Passwörter, Management-API-Token und Secret-Werte sind ausdrücklich **nicht** Bestandteil des Projekt-ZIPs.
- `data_full.sql`, `storage_backup/` und `project_config.json` sind Recovery-Artefakte und werden durch `.gitignore` vom normalen öffentlichen Git-Commit ausgeschlossen; sie bleiben im privaten Master-ZIP erhalten.


## Neu in v0.4.84

- Audioarchiv nach der Umstellung auf `Download/LFN-Scout/` kompatibel mit vorhandenen WAV-Dateien im früheren Ordner `Download/BrummBeobachter/`.
- Lesen und Löschen versucht bei veralteten gespeicherten URIs zusätzlich eine Suche nach dem WAV-Dateinamen in aktuellem und historischem Downloadordner.
- Die 24-h-Bereinigung ist jetzt idempotent: Ist eine erfolgreich hochgeladene WAV bereits nicht mehr vorhanden, gilt der gewünschte Löschzustand als erreicht und der verwaiste Metadateneintrag wird ebenfalls entfernt.
- Noch tatsächlich vorhandene Legacy-WAVs werden bei der Bereinigung gefunden und nach den bestehenden Retention-Regeln gelöscht; neue Dateien werden weiterhin ausschließlich unter `Download/LFN-Scout/` angelegt.
- Rolling-Dateien und nicht erfolgreich hochgeladene normale Aufnahmen behalten ihre bisherigen Löschregeln.
- Neue Regression: `AudioArchiveCompatibilitySelfTest.kt`.

## Neu in v0.4.83

- Produkt-, Projekt-, ZIP-, Export-, Log- und Dokumentname auf **LFN-Scout** vereinheitlicht.
- Projektordner/ZIP: `LFN-Scout_Android_v0.4.83` bzw. `LFN-Scout_Android_v0.4.83.zip`.
- Öffentlicher Android-Speicherpfad: `Download/LFN-Scout/`. Bereits vorhandene Dateien in früheren Ordnern werden nicht automatisch gelöscht.
- GitHub-Workflow, APK-/Artifact-Namen und Buildtexte auf `LFN-Scout` umgestellt.
- UI-/Theme-/interne Symbolnamen mit dem alten Produktnamen wurden auf `LFNScout...` umgestellt.
- **Kompatibilitätsausnahme:** Android `applicationId`/`namespace` und Kotlin-Package bleiben `de.brummbeobachter.app`, damit bestehende Installationen aktualisierbar bleiben.

## Neu in v0.4.82

- Der Restsekunden-Countdown der normalen Audioaufnahme ist kein modaler Dialog mehr.
- Die große Restsekundenzahl wird transparent über die bestehende Aufnahmeansicht gelegt.
- Die **CLIP-Anzeige bleibt sichtbar**.
- Die gelb hinterlegte Taste **`Aufnahme abbrechen`** bleibt während der gesamten normalen Aufnahme sichtbar und erreichbar.
- ROLL-/Rolling-Aufnahmen bleiben unverändert und erhalten weiterhin kein Countdown-Overlay.

## Neu in v0.4.81

- Seismik-Dateinamen enthalten zusätzlich die lokale Speicher-Uhrzeit bis auf Sekunden, für WBBU und Näheplots.
- Fertige WBBU- und Näheplot-Bitmaps werden als Ganzes **90° nach links gedreht**; Diagramminhalte und Beschriftungen werden nicht einzeln neu angeordnet.
- Näheplot-Messwertverstärkung im Entwicklermodus ganzzahlig **1–20** einstellbar und persistent; Standardwert `2`.
- Die feste Seismik-Y-Skala bleibt bei Messwertverstärkung unverändert; starke Kurven dürfen über Zeilengrenzen hinausreichen.
- Wasserfall-Pegelbereich: bestehende **Automatik bleibt Standard**. Zusätzlich gibt es einen manuellen Modus mit einstellbarem `Min @ 50 dB` und `Max @ 50 dB`.
- Bei reduziertem Audio-Gain wird der manuelle Min/Max-Bereich gemeinsam weiter ins Negative verschoben; die Bereichsbreite bleibt erhalten.
- Normale Audioaufnahmen zeigen einen großen Restsekunden-Countdown; ROLL-Aufnahmen bleiben davon unberührt.

## Neu in v0.4.80

- Nähe-Helicorder auf **3072 Zeit-Bins je 30-Minuten-Zeile** erhöht.
- Plot-Messwerte des selbst erzeugten Näheplots probeweise mit Faktor `2.0` verstärkt.
- Die feste WBBU-Referenzskala bleibt dabei unverändert; Ausschläge dürfen deutlich über benachbarte Zeilen hinausreichen.
- Master-Bitmap bleibt bei **6144 × 3456 Pixeln**.
- Kein zusätzlicher Frequenzfilter eingeführt.

## Neu in v0.4.79

- Nähe-Helicorder intern jetzt **6144×3456 Pixel** statt 3072×1728.
- 4×-Supersampling für dieselbe gemeinsame Bitmap-Darstellung in App und PNG-Export.
- Pro 30-Minuten-Zeile **1536 Zeit-Bins** statt 240.
- Pro Bin werden erster Wert, beide Extrema in echter zeitlicher Reihenfolge und letzter Wert erhalten.
- Dadurch deutlich weniger zeitliche Reduktion und mehr sichtbare Seismikstruktur.
- Es wurde **kein Frequenzfilter** ergänzt.

## Neu in v0.4.78

- Näheplot wird nur noch einmal als vollständiges Master-Bitmap 3072×1728 gerendert.
- Dasselbe Master-Bitmap wird für App-Anzeige und PNG-Speicherung verwendet; kein abweichender Compose-Canvas-Renderer mehr.
- Uhrzeiten 00:00 bis 23:30, 0–30-min-X-Achse und Amplitudenmaßstab sind Bestandteil des Bildes und bleiben beim Zoomen erhalten.
- WBBU und Näheplot besitzen jeweils einen direkt im zugehörigen Plot-Card sichtbaren Button **Plot speichern**.
- WBBU wird weiterhin als originales HLNUG-PNG gespeichert; Näheplot als vollständiges 3072×1728-Master-PNG nach `Download/LFN-Scout/`.

## Neu in v0.4.77

- Gemeinsamer, immer sichtbarer **Plot speichern**-Button für WBBU und Näheplot.
- WBBU speichert das originale HLNUG-PNG; der Näheplot speichert den selbst erzeugten Helicorder.
- Der Näheplot nutzt jetzt ein scheli-artiges kontinuierliches Trace-Rendering statt isolierter Min/Max-Stäbe.
- Geometrie: 48 × 30 Minuten, rot/blau alternierend, feste gemeinsame Amplitudenskala, Zoom/Pan wie bisher.
- Speicherziel bleibt `Download/LFN-Scout/`.


## Neu in v0.4.76

- WBBU-Plot kann wieder über den gemeinsamen Button **Plot speichern** exportiert werden.
- Das originale HLNUG-PNG wird bei WBBU direkt bytegenau in `Download/LFN-Scout/` geschrieben.
- Der selbst erzeugte Nähe-Helicorder bleibt als eigener Render-/Exportpfad unverändert erhalten.
- Nach erfolgreichem Speichern bleibt die sichtbare Erfolgsmeldung erhalten.

## Neu in v0.4.75

- Reiner Build-Fix auf Basis von v0.4.74.
- Eine fehlende schließende `}` am Ende von `SeismicScreen` ergänzt.
- Dadurch liegen `PanelHeader`, `MoreFunctionsScreen` und die folgenden `private`-Funktionen wieder außerhalb von `SeismicScreen` und werden vom Kotlin-Compiler nicht mehr als lokale Funktionen interpretiert.
- Neuer Struktur-Regressionstest prüft ausgeglichene Kotlin-Klammern und die Top-Level-Grenze vor `PanelHeader`.
- Keine funktionale Änderung an LFN-Scout, Seismikdaten, Landscape-Plot, Zoom, Export oder WBBU-/Nähelogik.


## Neu in v0.4.74

- Sichtbarer App-Name: **LFN-Scout**. Technische Projekt-/ZIP-/Verzeichnisnamen bleiben **LFN-Scout**.
- Näheplot in der App jetzt fest **16:9 Landscape**; 0–30 min laufen entlang der langen horizontalen Achse.
- Uhrzeitlabels links auf **4 sp** reduziert und über die Plot-Höhe verteilt.
- Exportformat weiterhin 1536×864 px mit 2× Supersampling (3072×1728 intern).
- `40000 nm/s` steht vertikal direkt an der linken Maßstrecke.
- Zwei redundante Float-Konvertierungswarnungen im Seismik-Exporter bereinigt.

- Näheplot-PNG jetzt im WBBU-nahen **16:9-Endformat 1536 × 864 px**.
- Internes Rendering mit **3072 × 1728 px (2× in X und Y)**; anschließend hochwertiges Downsampling auf 1536 × 864 px.
- Intensitätsmaßstrecke ganz links **vor den Uhrzeiten**.
- Beschriftung **`40000 nm/s` direkt parallel an der Maßstrecke**, inklusive Einheit.
- Kompakter WBBU-naher Header sowie X-Achse 0, 5min, 10min, 15min, 20min, 25min, 30min.
- Speicherziel bleibt `Download/LFN-Scout/`.
- Datenabruf, WBBU-Sonderlogik, MiniSEED-Decodierung, Zoom und physikalische Skalierung bleiben unverändert.

## Neu in v0.4.71

- Näheplot-Strichstärke verdoppelt.
- Sichtbare Intensitätslegende als Maßstrecke; bei nm/s die WBBU-Referenz 40000 nm/s, sonst Counts-Kennzeichnung.
- WBBU- und Näheplots: Pinch-Zoom 1x bis 5x, Pan im Zoom, Doppeltipp-Reset.
- `Plot speichern` exportiert ein PNG nach `Download/LFN-Scout/` und meldet den Erfolg sichtbar.
- Beim Näheplot wird der vollständige Plot inklusive Zeitangaben und Legende gespeichert, unabhängig vom aktuellen Zoomausschnitt.


## Neu in v0.4.70

- Reiner Compose-Build-Fix am klassischen Seismik-Helicorder.
- Fehlenden `Box`-Import ergänzt.
- Zeitlabel-Schleife auf direkte Kotlin-`for`-Schleife umgestellt, damit `Box` und `Text` im `@Composable`-Kontext bleiben.
- Regressionstest für genau diese beiden Compilerfehler ergänzt.
- Seismikfunktion und Darstellung aus v0.4.69 bleiben unverändert.

## Neu in v0.4.69

- Klassischer 24-h-Seismikplot mit 48 Halb­stundenzeilen bleibt erhalten.
- Näheplots verwenden bei geeigneter FDSN-Geschwindigkeitssensitivität eine feste WBBU-vergleichbare Y-Skalierung in nm/s.
- Referenz aus dem bereitgestellten WBBU-Plot: 40000 nm/s ≈ 4,95 Zeilenabstände (≈ 8080 nm/s je Zeilenabstand).
- `Scale`, `ScaleFrequency` und `ScaleUnits` aus FDSN-Channel-Metadaten werden ausgewertet.
- Ohne geeignete Geschwindigkeitssensitivität wird transparent auf Counts-Autoskalierung zurückgefallen.
- Starke Ausschläge können im gemeinsamen Helicorder-Canvas über benachbarte Zeilen reichen.
- Hinweis in der App: sensitivitätsbasierte Skalierung ist keine vollständige frequenzabhängige Response-Deconvolution.


## Neu in v0.4.67

- MiniSEED-Steim1/Steim2-Decoder korrigiert. Die Kontrollnibbles des 64-Byte-Steim-Frames werden jetzt gemäß SEED/libmseed dem tatsächlichen Wortindex zugeordnet; dadurch werden reale GEOFON-Records nicht mehr wegen zu weniger dekodierter Samples verworfen.
- Steim-Regressionstests auf SEED-konforme Kontrollwortbelegung umgestellt und um einen Mehrframe-Test ergänzt.
- `SEISMIC_ROUTE_NONE` überspringt den Kandidaten sofort; der Federator wird nicht mehr sinnlos für Streams ohne Route abgefragt.
- Bei erfolgreichem Routing bleibt der Federator als DataSelect-Fallback erhalten.
- BGR-Zertifikatsprobleme bleiben ein nicht-blockierender Quellenfehler; GEOFON übernimmt ohne unsichere TLS-Ausnahme.
- Datumsnavigation und Tagesfenster bleiben unverändert.

## Neu in v0.4.66

- Seismik-Stationssuche beschleunigt: zuerst direkte FDSN-Station-Abfragen bei BGR und GEOFON; der ORFEUS-Federator dient nur noch als Fallback.
- EIDA-Routing wird erst für wenige nahe Z-Kanal-Kandidaten ausgeführt, nicht mehr für die komplette europaweite Kanalliste.
- Vor dem vollständigen Tagesabruf wird ein kurzer DataSelect-Probeabruf von maximal 120 s ausgeführt; bei langen Zeitfenstern endet die Probe fünf Minuten vor dem angefragten Ende, um typische Echtzeit-Latenzen zu umgehen.
- Neue Diagnose-Logs: `SEISMIC_CHANNEL_SOURCE_OK`, `SEISMIC_CHANNEL_SOURCE_EMPTY`, `SEISMIC_CHANNEL_SOURCE_FAIL`, `SEISMIC_CHANNEL_DIRECT_READY`, `SEISMIC_CHANNEL_FALLBACK`, `SEISMIC_PROBE_START`, `SEISMIC_PROBE_OK`.
- Datumsauswahl und Tagesfenster aus v0.4.65 bleiben unverändert: heute 00:00 lokal bis jetzt, vergangene Tage 00:00 bis 24:00 lokal.
- Regressionstest und Selbsttest für direkte Metadatenquellen, Kandidaten-Deduplizierung und Probezeitfenster ergänzt.


## Neu in v0.4.65

- Seismik-Datenquelle auf ORFEUS EIDA erweitert: Stationssuche über den EIDA Federator, Datenknoten-Auflösung über EIDA Routing.
- Der bisherige zwingende `fdsnws-availability`-Vortest entfällt; nach erfolgreichem Routing wird direkt `fdsnws-dataselect` verwendet.
- Seismik-Zeitfenster ist jetzt der lokale Kalendertag: heute von **00:00 lokaler Zeit bis jetzt**, vergangene Tage von **00:00 bis 24:00 lokaler Zeit**.
- Lokale Tagesgrenzen werden mit `ZoneId.systemDefault()` berechnet und berücksichtigen Sommer-/Winterzeit einschließlich 23-/25-Stunden-Tagen.
- Datumsnavigation im Seismik-Panel ergänzt: **◀**, antippbares Datum mit Datumsauswähler, **▶** und **Heute**. Zukunftsdaten sind gesperrt.
- Neue Logs: `SEISMIC_DATE_SELECTED`, `SEISMIC_WINDOW`, `SEISMIC_ROUTE_OK`, `SEISMIC_ROUTE_NONE`; Datenknoten wird im Panel angezeigt.
- MiniSEED-Decodierung speichereffizienter gemacht: Samples werden in ein vorab dimensioniertes `IntArray` geschrieben statt über eine boxed `ArrayList<Int>`.
- Regressionstests für Tagesfenster, DST, Zukunftssperre und EIDA-Routing ergänzt.

## Neu in v0.4.64

- Seismik-Panel verwendet keine GEOFON-Stationskarten-PNGs mehr.
- Vertikale Wellenformdaten werden direkt per GEOFON FDSN Station/Availability/DataSelect geladen.
- Der 60-Minuten-Plot wird nativ aus MiniSEED-2-Daten erzeugt; HHZ/BHZ werden bevorzugt.
- Fehlerfälle wie keine aktuellen Daten, Timeout, HTTP-Fehler und MiniSEED-Parsing werden detailliert protokolliert.



## Neu in v0.4.62

- Zentrale verbindliche Build-/Update-Dokumentation `BUILD_WORKFLOWS.md` ergänzt.
- Drei Buildwege präzise dokumentiert: A Android Studio, B Gradle-Kommandozeile, C Android-only mit Work + GitSync + GitHub Actions.
- Rolle von `PREPARE_PROJECT.bat` verbindlich geklärt: Windows-Vorbereitung für A/B, nicht erforderlich für C.
- Android-only-Workflow mit vollständiger Projekt-ZIP, geschütztem `.git/` und `Builds/`, Pull/Entpacken/Ersetzen/Commit/Push/Build/APK-Installation Schritt für Schritt dokumentiert.
- Benötigte Android-Apps und minimale Bedienfolge dokumentiert.
- Keine Änderung der App-Funktionalität.

## Neu in v0.4.61

- Windows-Updater unter `Tools/WindowsUpdater/` ergänzt.
- Ausführliche Anleitung zusätzlich als `UPDATER_ANLEITUNG.md`.
- Updater findet die neueste Projekt-ZIP, prüft Git/gh, schützt `.git` und `Builds`, zeigt einen Dry-Run bzw. eine Änderungsübersicht und führt erst nach Bestätigung Commit/Push aus.
- GitHub Actions speichert bei der derzeit aktiven Variante B erfolgreiche Debug-APKs zusätzlich versioniert unter `Builds/debug/`.
- Das normale GitHub-Artifact bleibt parallel erhalten.
- Wechsel auf Variante A ist über `STORE_APK_IN_REPOSITORY: "false"` vorbereitet.

## Neu in v0.4.60

> Projekt-/Repository-Name: **LFN-IN**. Der öffentliche App-Name ist ab v0.4.74 **LFN-Scout**. Projekt-, Verzeichnis- und ZIP-Namen bleiben aus Kompatibilitätsgründen **LFN-Scout**.
> Aus Kompatibilitätsgründen bleiben Android-Package/Namespace (`de.brummbeobachter.app`) und bestehende Download-Verzeichnisse zunächst unverändert.


- GitHub Actions Build ergänzt (`.github/workflows/build-android.yml`).
- Der Build kann auf GitHub manuell über **Actions -> Build LFN-Scout -> Run workflow** gestartet werden.
- Bei Push und Pull Request laufen die Unit-Tests und anschließend der Debug-APK-Build automatisch.
- Die erzeugte Debug-APK wird als GitHub-Artifact `LFN-Scout-v0.4.60-debug` bereitgestellt.
- Android-Studio- und lokaler Kommandozeilen-Build bleiben unverändert nutzbar.
- Keine Änderung an App-Logik, Supabase-Schema oder Edge Functions.

Weitere Hinweise: `GITHUB_BUILD.md`.

## Neu in v0.4.58

- Neues eigenes Panel **Seismik**.
- GPS-basierte Suche nach der nächstgelegenen verfügbaren GEOFON-Station.
- Anzeige des aktuellen 24-h-Monitorseismogramms der breitbandigen Vertikalkomponente; GEOFON aktualisiert die Tagesplots ungefähr alle 10 Minuten.
- Fallback auf weitere nahe Stationen, falls der Liveplot der geometrisch nächsten Station nicht verfügbar ist.
- Seismik-Logging für Start, Stationswahl, Erfolg, Timeout, Nichtverfügbarkeit, HTTP- und Parsingfehler.
- Wasserfall-Status wird nur noch an der tatsächlich bearbeiteten Audioaufzeichnung angezeigt.


Wartungsversion mit zyklisch aktualisierter Rolling-Buffer-Zeitanzeige in den Zusatzfunktionen. Die Anzeige wird auf App-Ebene jede Sekunde aus dem tatsächlich gefüllten Ringpuffer aktualisiert und bleibt dadurch auch nach normalen Audioaufnahmen zuverlässig aktuell.

# LFN-Scout Android v0.4.54

## Änderungen v0.4.54

- K1-Fix: Abgebrochene normale Aufnahme bei aktivem Rolling-Buffer gibt den Future-Capture-Slot jetzt garantiert frei. Nach einem Abbruch kann sofort wieder aufgenommen werden, ohne `Rolling-Audiostream nicht verfügbar`.
- Lokale WAV-Dateinamen verwenden nun die zum tatsächlichen Aufnahmebeginn aktuelle Bewertungsliste und nicht mehr einen vor dem 15-s-Countdown eingefrorenen Compose-Zustand. Dadurch stimmen lokale Rating-Suffixe mit der serverseitigen 30-Minuten-Zuordnung zuverlässig überein, sofern beide denselben Beobachtungsstand besitzen.
- Regressionstests für Capture-Cancellation und aktuelle Rating-State-Verwendung ergänzt.
- Keine Supabase-, Schema-, Migration- oder Edge-Function-Änderung. Die zurückgestellte Priorität-1-Prüfung `wav_present` / `wav_path` bleibt unverändert offen.

LFN-Scout ist die Android-App zur strukturierten Erfassung von
Brummton-Beobachtungen. Die App verbindet subjektive Bewertungen,
Zeitstempel, Standortdaten und optionale WAV-Aufnahmen mit einem privaten
Supabase-Backend.

## Hauptfunktionen

- Bewertungen 0–5 für die aktuell wahrgenommene Stärke.
- `-1` steht ausschließlich für „keine Antwort innerhalb des Antwortfensters“
  und ist keine normale Bewertung.
- Optionaler Kommentar pro Bewertung, maximal 2000 Zeichen.
- Erfassung des aktuellen Standorts bei jeder Bewertung.
- Wenn kein Standort verfügbar ist, werden `0.0 / 0.0` übertragen.
- Lokaler Offline-Puffer mit späterem Wiederholungsversand bei temporären
  Netzwerkfehlern.
- Verlauf der letzten Bewertungen sowie Export der eigenen Beobachtungen.
- Optionale Radius-Auswertung mit konfigurierbarem Radius.

## Erinnerungen und Pausen

Das Erinnerungsintervall ist konfigurierbar; Standard sind 30 Minuten.
Das Antwortfenster beträgt 30 % des Intervalls, mindestens 5 und höchstens
15 Minuten.

Im Wecker-Modus wird der ausgewählte Ton in drei kurzen Tonpaketen abgespielt:
sofort sowie nach 1/3 und 2/3 des Antwortfensters. Die späteren Tonpakete
laufen innerhalb der App und werden bei Bewertung oder Pause verworfen.

Pausen stehen kompakt in zwei Zeilen zur Verfügung:

- 1 h, 2 h, 4 h, 8 h
- 12 h, 24 h, 72 h, Ganz aus

Die Alarm-Lautstärke kann in der App eingestellt werden und wirkt auf den
Android-Alarmstream.

## Audioaufnahme

Die App kann eine zweiminütige WAV-Datei aufzeichnen:

- 120 s
- 1000 Hz
- mono
- 16 Bit PCM
- Rohaufnahme intern mit 8000 Hz
- anschließendes 8:1-Downsampling

Als AudioSource stehen zur Verfügung:

- `Auto · UNPROCESSED bevorzugt`
- `UNPROCESSED`
- `MIC`

Zusätzlich kann ein konkreter physischer Android-Audioeingang gewählt werden.
Die tatsächlich verwendete Quelle und das tatsächlich geroutete Eingabegerät
werden ermittelt.

Die Verstärkung ist auf dem Hauptpanel von 0 bis +50 dB einstellbar. Während
der Aufnahme wird die Zahl geclippter Samples live angezeigt. Der Button
`Aufnahme abbrechen` ist während der laufenden Aufnahme gelb hervorgehoben.

Das tägliche Aufnahmelimit beträgt standardmäßig 10 Aufnahmen und kann im
Entwicklermodus geändert werden.

### Kommentar nach der Audioaufnahme

Nach einer erfolgreichen 120-s-Aufnahme erscheint automatisch der Dialog
`Kommentar zur Audioaufnahme`.

- Ohne Nutzung des Kommentarfelds wird nach 10 Sekunden automatisch ohne
  Kommentar gesendet.
- Sobald das Kommentarfeld benutzt wird, gilt ein sichtbarer
  60-Sekunden-Inaktivitäts-Countdown.
- Jede weitere Texteingabe startet die 60 Sekunden neu.
- `Jetzt senden` überträgt sofort mit dem aktuellen Kommentar.
- `Ohne Kommentar senden` überträgt sofort ohne Kommentar.
- Audio-Kommentare sind auf 2000 Zeichen begrenzt.

Der Kommentar wird zusammen mit der Zeile in `audio_recordings` gespeichert
und verändert den WAV-Dateinamen nicht.

### Lokales Audio-Aufzeichnungsverzeichnis

WAV-Dateien werden nicht mehr unmittelbar nach erfolgreichem Upload gelöscht.
Sie liegen im privaten App-Verzeichnis:

`File(context.filesDir, "audio_recordings")`

Der tatsächlich verwendete absolute Pfad wird unter
`Zusatzfunktionen -> Audio-Aufzeichnungen` angezeigt und kann mit
`Pfad kopieren` in die Zwischenablage übernommen werden. Typischerweise sieht
der Pfad auf Android etwa so aus:

`/data/user/0/de.brummbeobachter.app/files/audio_recordings/`

Zu jeder WAV-Datei liegt dort eine kleine JSON-Metadatei mit `recording_id`,
Aufnahmezeit, Gain, AudioSource, CLIP-Zahl, Kommentar, Uploadstatus,
Uploadzeitpunkt, Storage-Pfad und letztem Uploadfehler.

Erfolgreich gesendete WAV-Dateien und ihre Metadaten werden erst 24 Stunden
nach dem erfolgreichen Upload automatisch gelöscht. Nicht erfolgreich
gesendete Aufnahmen werden durch diese Bereinigung nicht gelöscht. Unter
`Zusatzfunktionen -> Audio-Aufzeichnungen` können wartende Aufnahmen mit
`Erneut senden` erneut übertragen werden.

## WAV-Dateinamen im Supabase Storage

Neue WAV-Dateien werden lesbar gespeichert, zum Beispiel:

`BB001/2026-09-09_01-23-45_G30_UNP_R3.wav`

Bedeutung:

- `BB001` – Beobachter-ID
- `2026-09-09_01-23-45` – lokaler Startzeitpunkt der Aufnahme
- `G30` – Gain +30 dB
- `UNP` – tatsächlich verwendete Quelle `UNPROCESSED`
- `MIC` – tatsächlich verwendete Quelle `MIC`
- `R0` bis `R5` – letzte gültige Bewertung, wenn sie beim Aufnahmestart
  höchstens 30 Minuten alt war
- `RX` – keine gültige Bewertung 0–5 innerhalb der letzten 30 Minuten

Die Zuordnung der Bewertung zum WAV-Dateinamen erfolgt serverseitig in der
Edge Function `upload_wav`. `-1`-Timeouts werden dafür nicht verwendet.
Die UUID `recording_id` bleibt als technischer Datenbankschlüssel erhalten,
erscheint aber nicht mehr im Storage-Dateinamen.

Bestehende WAV-Dateien mit alten UUID-Dateinamen werden nicht automatisch
umbenannt.

## Supabase-Backend

Die App verwendet ein privates Supabase-Projekt. Die Teilnehmerauthentisierung
erfolgt über einen eigenen LFN-Scout-Schlüssel; ein Supabase
`service_role`-Schlüssel befindet sich nicht in der Android-App.

Aktuell verwendete Edge Functions:

- `submit_observation`
- `upload_wav`
- `get_recent_observations`
- `get_radius_histograms`
- `get_all_observations`

Der Bucket `brumm-audio` ist privat. Metadaten der Aufnahmen werden in
`audio_recordings` gespeichert.

Für v0.4.44 muss zusätzlich die Migration
`20260909_add_audio_recording_comment.sql` ausgeführt werden. Danach muss die
aktuelle Edge Function `upload_wav` erneut deployed werden; sie akzeptiert
jetzt den optionalen Audio-Kommentar und meldet Version `1.3.0`.

Die Dateien der Function bleiben:

- `supabase/functions/upload_wav/index.ts`
- `supabase/functions/upload_wav/storage_filename.js`

JWT-Verifikation bleibt für diese Function ausgeschaltet.

## Entwickler-Modus

Der Entwicklermodus bleibt persistent, bis er wieder ausgeschaltet wird.
Die lokale Freischaltung verwendet aus dem Teilnehmerschlüssel die dritte
vierstellige Gruppe in umgekehrter Reihenfolge. Es wird kein separates
Entwicklerpasswort gespeichert.

Im Entwicklermodus sind zusätzliche Diagnoseinformationen und die Änderung
des täglichen Audio-Limits verfügbar.

## App-Icon

Seit v0.4.41 enthält das Projekt:

- Adaptive Icon für Android 8+
- Round Icon
- Monochrome/Themed Icon
- Legacy-PNGs für mdpi bis xxxhdpi

Das Manifest verwendet `@mipmap/ic_launcher` und
`@mipmap/ic_launcher_round`.

## Versionsanzeige

Die angezeigte App-Version wird ab v0.4.42 aus
`BuildConfig.VERSION_NAME` bezogen. Dadurch müssen Systemstatus,
Beobachtungsmetadaten und sichtbare Versionsanzeige bei einem Versionssprung
nicht mehr separat von Hand nachgezogen werden.

## Projekt vorbereiten und bauen

Für den Android-Build wird Java 17 verwendet. Ein global installiertes Java 8
muss nicht verändert werden.

`PREPARE_PROJECT.bat` sucht zuerst nach einem vorhandenen geeigneten Java 17.
Falls es gefunden wird, wird es nur für den laufenden Prozess als `JAVA_HOME`
und über `PATH` verwendet. `gradle.properties` erhält zusätzlich den
projektbezogenen Pfad für `org.gradle.java.home`.

Die Vorbereitung verändert die globale Java-Konfiguration von Windows nicht.

Ablauf:

1. ZIP in einen eigenen Projektordner entpacken.
2. `PREPARE_PROJECT.bat` ausführen.
3. Auf `PASS: Projekt vorbereitet.` achten.
4. Projekt anschließend in Android Studio öffnen.
5. Einen vollständigen Debug-Build ausführen und die App auf dem Zielgerät
   testen.

## Wichtige Dateien

- `app/src/main/java/de/brummbeobachter/app/` – Android-Quellcode
- `app/src/main/res/` – Android-Ressourcen und App-Icon
- `supabase/functions/` – Edge Functions
- `supabase/migrations/` – SQL-Migrationen
- `SUPABASE_v0.4.40_SETUP.txt` – Hinweis zur lesbaren WAV-Namensgebung
- `WAV_DOWNLOAD_HINWEIS.txt` – vorgesehener Sammeldownload der WAV-Dateien
- `CHANGELOG.md` – Versionshistorie

## Verifikation

Vor der Ausgabe neuer Versionen werden statische Regressionstests durchgeführt.
Ein echter Android-/Kotlin-Build mit Android SDK und ein realer Gerätetest
können dadurch nicht ersetzt werden und müssen weiterhin in Android Studio
bzw. auf dem Zielgerät erfolgen.


## Änderungen in v0.4.45

- Lokale WAV-Aufzeichnungen werden auf Android 10+ über MediaStore unter
  `Download/LFN-Scout/` gespeichert, damit sie mit normalen
  Dateimanagern sichtbar sind.
- Die alten WAV-Dateien aus dem bisherigen privaten Verzeichnis
  `/data/user/0/de.brummbeobachter.app/files/audio_recordings/` werden beim
  ersten Start von v0.4.45 genau einmal gelöscht. Ein SharedPreferences-Flag
  verhindert jede spätere Wiederholung.
- Das Debug-Protokoll hält wieder bis zu 2000 Zeilen und protokolliert den
  Audio-Ablauf deutlich ausführlicher.
- Supabase-WAV-Dateinamen enthalten die Benutzerkennung zusätzlich im
  Dateinamen, z. B.
  `BB001/BB001_2026-09-09_01-23-45_G30_UNP_R3.wav`.
- `upload_wav` meldet Version `1.4.0`.


## Änderungen in v0.4.46

Neu ist eine lokale Wasserfall-Belohnungsansicht für die letzte vorhandene
Audioaufnahme. Unter `Zusatzfunktionen -> Audio-Aufzeichnungen` gibt es den
Button `Wasserfall letzte Aufnahme`.

Die Darstellung wird vollständig lokal auf dem Smartphone berechnet:

- lineare Frequenzachse 0 bis 100 Hz,
- Zeit läuft von oben nach unten,
- feste Colormap `Inferno`,
- Hann-Fenster,
- FFT-Größe 1024 Samples,
- Hop Size 256 Samples,
- Pegel in dBFS.

Die Pegelfarbskala wird einmal für die komplette Aufnahme robust automatisch
angepasst. Für die Skalierung werden nur 2 bis 100 Hz verwendet. Die Grenzen
sind `P5 - 3 dB` und `P99,5 + 3 dB`, mit mindestens 30 dB Darstellungsumfang
und harten Grenzen von -120 bis 0 dBFS. Die tatsächlich verwendeten
Pegelgrenzen stehen im Wasserfallbild.

Das erzeugte Wasserfallspektrum wird zunächst direkt in der App angezeigt.
Mit `PNG speichern` wird es zusätzlich unter `Download/LFN-Scout/`
gespeichert.

Die Funktion arbeitet nur mit lokal vorhandenen WAV-Dateien und benötigt
keine Supabase-Abfrage. Für v0.4.46 ist deshalb keine neue SQL-Migration und
kein neues Edge-Function-Deployment erforderlich.


## Änderungen in v0.4.47

Die Wasserfall-Belohnungsfunktion wurde erweitert.

Neu in `Zusatzfunktionen -> Audio-Aufzeichnungen`:

- Frequenzbereich jetzt **0 bis 140 Hz** statt 0 bis 100 Hz.
- Automatische Pegelanpassung jetzt robust aus **2 bis 140 Hz**.
- In das Bild werden zusätzlich **L50** und **L90** in dBFS eingeblendet.
- Falls zur Aufnahme ein Kommentar gespeichert wurde, wird der Kommentar ebenfalls
  im Bild angezeigt.
- Das Wasserfallspektrum ist jetzt nicht mehr nur für die letzte Aufnahme verfügbar.
  Zu jeder lokal verfügbaren Aufnahme gibt es einen Button
  **`Wasserfall für diese Aufnahme`**.
- Es werden jetzt **alle** lokal verfügbaren Aufnahmen angezeigt, nicht nur die
  20 neuesten.
- Der PNG-Dateiname enthält jetzt zusätzlich **L50** und **L90**. Beispiel:

  `BB001_2026-09-09_01-23-45_G30_UNP_R3_L50m54p2_L90m67p8_WF140.png`

Die eigentliche PNG-Erzeugung bleibt lokal auf dem Handy. Es sind weiterhin
**keine Supabase-Änderungen** für diese Funktion nötig.


## Änderungen in v0.4.48

Die lokale WAV-Datei erhält jetzt bereits beim Speichern denselben sprechenden
Namen, der bisher vor allem für die Serverablage vorgesehen war.

Beispiel:

`BB001_2026-09-09_01-23-45_G30_UNP_R3.wav`

Die Bewertung wird aus der letzten gültigen Bewertung 0 bis 5 übernommen,
wenn sie zum Startzeitpunkt der Audioaufnahme höchstens 30 Minuten alt ist.
Genau 30:00 Minuten zählen noch. Ist keine passende Bewertung vorhanden,
wird `RX` verwendet.

Die Beobachterkennung wird aus der bereits vorhandenen Serverantwort
`observer_id` übernommen und lokal zusammen mit dem Teilnehmerschlüssel
gespeichert. Beim Speichern eines neuen Schlüssels wird die alte
Beobachterkennung verworfen und neu vom Server ermittelt.

Die Wasserfall-PNG übernimmt exakt den Stamm des lokalen WAV-Namens und hängt
nur L50, L90 und die tatsächlich dargestellte Maximalfrequenz an:

`BB001_2026-09-09_01-23-45_G30_UNP_R3_L50m54p2_L90m67p8_WF140.png`

`WF140` ist nicht fest verdrahtet, sondern wird aus der aktuellen
`FREQ_MAX_HZ` gebildet.


## Änderungen in v0.4.49

- Wasserfallbild in der Vorschau per Zwei-Finger-Geste zoombar (1x bis 5x), bei Zoom verschiebbar und per Doppeltipp zurücksetzbar.
- In `Audio-Aufzeichnungen` öffnet ein Tipp auf die Dateiinfo den Wasserfall; der bisherige Einzelaufnahme-Wasserfallbutton entfällt.
- Unter `Einstellungen > Audioaufnahme` ist das Wasserfall-Bildformat persistent zwischen `PNG`, `JPG` und `PNG + JPG` wählbar. JPG nutzt Qualität 95.
- Horizontale Panelnavigation nicht zyklisch: MAIN Wisch rechts -> MORE; MORE Wisch links -> MAIN / rechts -> SETTINGS; SETTINGS Wisch links -> MORE.


## Änderungen in v0.4.50

- Korrigierte nicht-zyklische Panel-Wischlogik: MAIN links → MORE; MORE rechts → MAIN und links → SETTINGS; SETTINGS rechts → MORE.
- Button „Wasserfall neueste Aufnahme“ entfernt. Die neueste Aufnahme erscheint wie alle anderen als erster Listeneintrag und wird durch Tipp auf ihre Dateiinfo geöffnet.
- Bewertungsüberschrift und Erläuterung stehen kompakt in einer Zeile.
- Erinnerungsstatus und Modus stehen kompakt in einer Zeile; die nächste Erinnerung bleibt separat.




## Änderungen in v0.4.53

- Rolling-Buffer-Verhalten ausdrücklich als kontinuierlicher Ringpuffer abgesichert.
- Nur nach Start wächst der Puffer einmalig bis 30 Minuten an; danach enthält er immer die letzten 30 Minuten.
- `Rolling-Buffer sichern` erstellt nur einen Snapshot und leert oder startet den Puffer nicht neu.
- Statusanzeige zeigt nach vollständiger Füllung `letzte 30:00 min` statt `30:00 / 30:00 min`.
- Keine Supabase-Änderung.

## Änderungen in v0.4.52

- Optionaler 30-Minuten-Rolling-Audiobuffer im RAM (1000 Hz, mono, 16 Bit), auch im Hintergrund über einen Mikrofon-Foreground-Service.
- Rolling-Buffer verwendet die vorhandenen Audioquelle-, Eingang- und Gain-Einstellungen; keine separaten Rolling-Audioparameter.
- Normale 120-s-Aufnahmen verwenden bei aktivem Rolling-Buffer denselben laufenden Audiostrom.
- In MORE: „Rolling-Buffer sichern“. Auch ein noch nicht voller Puffer wird vollständig mit seiner aktuellen Dauer gesichert.
- Rolling-WAVs erhalten Zeitbereich, Gain, Quelle und letzte gültige Bewertung der letzten 30 Minuten im Dateinamen.
- Zu jeder Rolling-WAV wird eine gleichnamige JSON-Datei mit Kommentar und Metadaten im Download-Verzeichnis angelegt.
- Rolling-Aufnahmen erscheinen in derselben lokalen Audio-Liste, werden nie zu Supabase übertragen und nie automatisch nach 24 h gelöscht.
- Langer Druck auf eine Rolling-Aufnahme öffnet eine Löschbestätigung; WAV, JSON und interne Metadaten werden gemeinsam entfernt.
- Vorhandener Wasserfall/PNG/JPG-Export kann auch für gespeicherte Rolling-Aufnahmen verwendet werden.
- Persistenter Schalter „30-min Rolling-Buffer“ in den Einstellungen; Wiederanlauf bei App-/Dienst-Neustart soweit Android dies zulässt.

## Änderungen in v0.4.51

- Bewertungsüberschrift und Erläuterung stehen in einer einzigen Zeile.
- `Bewertung` bleibt groß/fett; `0 = nichts wahrgenommen · 5 = sehr stark` wird deutlich kleiner (8 sp) gesetzt.
- Audioüberschrift und WAV-Parameter stehen in einer einzigen Zeile.
- `Audio aufzeichnen` bleibt groß/fett; `WAV · 120 s · 1000 Hz · mono · 16 Bit` wird deutlich kleiner (8 sp) gesetzt.
- Beide Zusatztexte verwenden `maxLines = 1` und `softWrap = false`; damit wird kein Zeilenumbruch erzeugt.
- Alle Funktionen aus v0.4.50 bleiben unverändert.

## Quellen Seismik

- GEOFON Live-Seismogramme: https://geofon.gfz.de/waveform/liveseis.php
- GEOFON FDSN Stationsdienst: https://geofon.gfz.de/fdsnws/station/1/
- GEOFON beschreibt die Liveplots als 24-h-Monitorplots; die PNG-Dateien des aktuellen Tages werden ungefähr alle 10 Minuten aktualisiert.
- Fachpublikation mit ausdrücklicher Beschreibung der Liveplots als breitbandige Vertikalkomponente: https://essd.copernicus.org/articles/17/5149/2025/

## Projektdokumentation

Dauerhafte Projektübersichten und Roadmaps liegen unter `docs/project/`.
Interne aufgabenspezifische Entwicklungspläne und Design-Spezifikationen liegen getrennt unter `docs/superpowers/`.

## Seismik v0.4.68

Der Tagesplot wird als klassischer 24-h-Helicorder mit 48 Halb­stundenzeilen dargestellt. Rohdaten-Zeilen wechseln zwischen Blau und Rot. Im Umkreis von 30 km um WBBU Bürstadt gilt nur für den aktuellen Tag eine regionale Sonderquelle: das aktuelle HLNUG-24-h-Seismogramm `HS.WBBU.png`. Für frühere Tage sowie außerhalb dieser Zone wird dynamisch die nächstgelegene tatsächlich verfügbare offene FDSN/EIDA-Station mit vertikalem Z-Kanal verwendet.


## Seismik v0.4.69 – WBBU-vergleichbare Y-Skalierung

Bei FDSN-Rohdaten wird die im Channel-Text gelieferte Gesamtempfindlichkeit (`Scale`, `ScaleFrequency`, `ScaleUnits`) ausgewertet. Geschwindigkeitskanäle mit `M/S` bzw. `NM/S` werden sensitivitätsbasiert in `nm/s` skaliert. Die grafische Referenz entspricht dem WBBU-Plot: 40000 nm/s ≈ 4,95 Halb­stunden-Zeilenabstände. Fehlt eine geeignete Geschwindigkeitssensitivität, bleibt der Plot in Counts und wird ausdrücklich als Fallback gekennzeichnet. Die sensitivitätsbasierte Skalierung ersetzt keine vollständige frequenzabhängige Instrument-Response-Deconvolution.


## Hinweis zu v0.4.80
- Der selbst erzeugte Nähe-Helicorder verwendet jetzt `3072` Bins pro 30-Minuten-Zeile.
- Die Plot-Messwerte werden probeweise mit Faktor `2` verstärkt.
- Die feste WBBU-Referenzskala bleibt unverändert, daher dürfen Kurven sichtbar über benachbarte Zeilen hinausreichen.


## v0.4.81
- Seismikexporte mit Speicher-Uhrzeit und vollständiger 90°-Linksdrehung.
- Entwicklermodus: Näheplot-Gain 1–20, Wasserfall Auto/Manuell mit 50-dB-Referenz.
- Normale Audioaufnahme: großer Restsekunden-Countdown und Abbruchbutton.


## Hinweis zu v0.4.82
Bei der normalen Audioaufnahme wird die verbleibende Aufnahmezeit jetzt als große transparente Zahl über die bestehende Aufnahmekarte gelegt. Die CLIP-Anzeige und die gelbe Abbruchtaste bleiben sichtbar und bedienbar. ROLL-Aufnahmen sind davon nicht betroffen.


## Namensumstellung v0.4.83
- Öffentlicher Produktname, Projektordner, ZIP-, APK-, Log-, Export- und Dokumentnamen: **LFN-Scout**.
- Öffentlicher Speicherpfad: `Download/LFN-Scout/`.
- Die interne Android-ID `de.brummbeobachter.app` bleibt ausschließlich zur Update-Kompatibilität unverändert.


## Vollständige Rekonstruktionsdokumentation

Ab v0.4.83 enthält das Projekt unter `docs/reconstruction/` eine vollständige Wiederaufbauakte mit aktueller SRS, Architektur/SDS, UI-, Audio-, Waterfall-, Seismik-, Backend-, Settings-, Build- und Testdokumentation sowie maschinenlesbaren Manifesten. Einstiegspunkt ist `docs/reconstruction/00_RECONSTRUCTION_INDEX.md`. Der aktuelle Quellcode bleibt bei Widersprüchen die höchste technische Autorität.


## Dokumentationsrevision 2026-09-19 (v0.4.85 – App-Code unverändert)

Die bestehende Rekonstruktionsdokumentation wurde anhand von `CHATS.zip` **innerhalb der bestehenden Kapitel** erweitert: SRS, Architektur, UI, Audio, Beobachtungen, Backend, Build, Teststrategie, Entscheidungen und Recovery. Neu: `docs/reconstruction/22_CHAT_TRACEABILITY_MATRIX.md`, `23_BACKLOG_CHAT_REFERENCES.md`, `24_CHAT_SOURCE_METHOD.md`; drei vollständige Chat-Markdowns wurden in `source_history/` aktualisiert. Das Lieferpaket ist **Private Recovery Master**: `supabase/data_full.sql`, `supabase/storage_backup/` und `supabase/project_config.json` niemals veröffentlichen. Versionscode 125 bleibt unverändert; ein erfolgreicher Android-Build oder produktiver Restore wird nicht behauptet.


## Analyzer v0.7 – Entwicklungsnachtrag Phase 1 (keine neue App-Version)

Die vollständige Übergabe liegt unter `docs/project/analyzer_v0.7/`, inklusive realem Mapping `docs/project/analyzer_v0.7/docs/project/16_Repo_Mapping_und_Gates_v0.7.md`. Ein isolierter DSP-Core mit Test-Fixtures wurde hinzugefügt; **das Unterpanel „Spektrum & Mithören“ ist NOCH NICHT IMPLEMENTIERT**. Keine Behauptung einer lauffähigen Analyzer-APK. Die bestehende App-Version bleibt 0.4.85. Details und Teststatus: `docs/reconstruction/25_ANALYZER_V07_INTEGRATION.md`.
