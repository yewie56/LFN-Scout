## v0.5.45.8 – 12.10.2026 – Android-GitHub-Build + Signalstatus-Regression (Quellkandidat)
- `AnalyzerSignalValidity`: gültige Original-FFT-Daten im Bereich der technischen Anzeigeuntergrenze sind kein Audioausfall. Nur leere/nichtfinite FFT-Puffer führen zu `NO_INPUT`; Quellenalter und Audioaufnahmefehler bleiben unabhängig zu prüfen.
- Regressionstests für wechselnden 1/f-Restpegel sowie durchgehend unter Rauschboden liegende FFT.
- Version `0.5.45.8` / versionCode 201.
- Handy-Publishing per Termux + GitHub CLI, privates Repo und existierender `.github/workflows/build-android.yml`; `ANLEITUNG_GITHUB_ANDROID_v0.5.45.8.md`.
- Zusätzliche `.gitignore`-Ausschlüsse für Backups/Geheimnisse.
- FAST, Cloud, Rauschmodell, FFT-Frequenzbänder und CSV-Filterbank-Einstellungen unverändert.
- Vollständiger GitHub-Actions-Build / Android-Geräteprüfung noch OFFEN.

## v0.5.45.7 – Statuskorrektur bei vollständig kompensierter Rauschleistung (Quellkandidat)
- Fehlerbehebung: `LiveAnalyzerController.accept()` klassifiziert FFT-Eingangsgültigkeit anhand der **unkompensierten** gain-korrigierten FFT-Werte, nicht der bereits NOISE-subtrahierten Werte.
- Ist der ursprüngliche FFT-Eingang vorhanden, aber die komplette Restleistung kompensiert, bleibt das Analyzer-Fenster gültig; Status "Unter Rauschboden / kein auflösbares LF-Nutzsignal · Eingang aktiv". Geschichte und Statistiken werden nicht allein deswegen abgebrochen.
- Bei tatsächlich fehlenden/nicht numerischen Original-FFT-Werten bleibt `NO_INPUT` und bisheriges Fehlerverhalten erhalten.
- Neue `AnalyzerSignalValidity`-Policy und fünf JUnit-Regressionen. Kotlin-Hostprüfung 5/5 PASS; Android-Build/ADB noch offen.
- Keine Änderungen an NOISE-MESS-/1/f-Fit, FAST, gespeicherten Referenzen, Cloud oder deaktivierten CSV-Filterbank-Einstellungen. Referenzabschluss weiterhin 1 kΩ, Fehlmessung −11,67 dB/Dekade ausgeschlossen.
- Testprozedur: `ANLEITUNG_TEST_v0.5.45.7.md`.

## v0.5.45.6 – NOISE AUS / MESSPROFIL / 1/f-MODELL (Quellkandidat)
- Abschlusswiderstand nach Benutzerkorrektur **1 kΩ**; erste Steigung −11,67 dB/Dekade verworfen; gültige Referenz −19,72 dB/Dekade.
- Modell `P_white+K/f^alpha` auf gespeicherte Referenz gefittet und nur 1–100 Hz verwendet; unveränderte Messreferenz außerhalb. Keine pauschale Signal-EQ.
- NOISE kurz schaltet zyklisch AUS → MESSPROFIL → 1/f-MODELL → AUS; Langdruck startet unverändert die zeitlich gemittelte Referenz-Einmessung.
- Persistenter Modus, rückwärtskompatible `enabled`-Einstellung, Tests für Modellverhalten, vorhandene FAST/CSV-Filter nicht verändert.
- Android-Gradle/ADB noch nicht in dieser Umgebung nachgewiesen. Exakte Prüfschritte: `ANLEITUNG_TEST_v0.5.45.6.md`.

## v0.5.45.5 – Diagnostik 1/f-Verdacht (Quellkandidat)
- Neuer DIAG-Dialog unmittelbar bei NOISE: fest-seedierter synthetischer Weißrauschtest über denselben LiveAnalyzerCore wie der Analyzer, 1000 Hz / 4096 / Hann, lineare Leistungsmittelung und lineare Regression des dB-Leistungsspektrums über log10(f) im Bereich 1–100 Hz. Toleranz ±2,5 dB/Dekade, kein physikalischer Kalibrationsnachweis.
- Gespeichertes Original-Rauschprofil: Steigung 1–100 Hz aus unkompensierten Profil-Power-Bins; Modell-Hinweis, keine gemessene Mikrofonsensitivität.
- Kein Eingriff in FAST, Roh-PCM/WAV, Cloud, Rolling-CSV, FFT-Produktivberechnung oder NOISE-Subtraktion.
- Grenze: DIAG zeigt die Referenz als **numerische Steigung**, nicht als zweite Spektralkurve; Original/Rest lassen sich über NOISE ein/aus vergleichen. Vollständige 3-Kurven-Überlagerung und automatische Roh-PCM-/Dezimationsvergleichsdiagnose sind noch offen.
- Teststatus: Kotlin-Hosttest ggf. separat dokumentiert; Gradle Android/JUnit/ADB ausstehend.

## v0.5.45.3 – Rauschboden 10-s-Power-Average, einstellbare Dauer, Nullsignal-Schutz (Quellkandidat)
- Einmessdauer im Analyzer unter Spektrum: 10/30/60/120/300/600 s, Standard 10 s, persistent.
- Langdruck auf NOISE misst weiterhin das zeitliche lineare Power Average aller gueltigen FFT-Fenster.
- Wenn gemessene Leistung nahe oder unter Referenzleistung liegt (<= 1,25 x Referenz), wird der Originalwert durchgereicht statt auf Null geclippt. Die Werte sind dann NICHT rauschkompensiert und muessen als nicht sicher trennbar interpretiert werden; hoher SNR wird weiterhin in linearer Leistung subtrahiert.
- Physikalischer Hinweis: Wird ein Nutzsignal bei der Kalibration mit eingemessen, wird es als Rauschen behandelt. Zeitliche Mittelung allein behebt dies nicht.
- FAST, Roh-WAV, Cloud und bisherige CSV-Filterbank unveraendert; Rolling-Halbstundenpfad bleibt FFT-basiert.
- Gradle / Android-Build und Geraetetests offen.

## v0.5.45.2 – Einmessung als Power Average
Erweiterung v0.5.45.1: Einmessung sammelt alle gültigen sofortigen FFT-Leistungen über die 120-s-Kalibrierzeit. JSON-Felder `average_type=LINEAR_POWER_ARITHMETIC`, `frames` und `rejected_frames`. Start: langer Druck auf Rauschboden-Button, zuerst geeigneten externen Eingang sicher abschließen. VORTEST_INSTALL.bat ausführen; Android/ADB noch nicht bestätigt.

## v0.5.45.1 – Buildfix / neuer Windows-Vortest erforderlich
Der Vortest v0.5.45 vom 12.10.2026 scheiterte in `:app:compileDebugKotlin` (`RollingAudioService.kt:309`, `writeFilterPerformanceWindow`). Der äußere ungültige Aufruf ist entfernt. Vor weiterer Entwicklung zuerst `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md` und `docs/project/NOISE_FLOOR_FFT_ROLLING_v0545.md` beachten. `VORTEST_INSTALL.bat` unter Windows 11 ausführen; erfolgreicher Build/ADB noch offen.

## v0.5.45
WICHTIG: Bitte **zuerst** `docs/project/NOISE_FLOOR_FFT_ROLLING_v0545.md` lesen. FFT-Rolling-CSV-Pfad eingeführt, alte Filterbank stillgelegt aber erhalten. Eine vollständig gemeinsame FFT-Berechnung mit dem 8-kHz-Live-Analyzer ist noch NICHT erreicht; Build/ADB noch offen.

# LFN-Scout v0.5.44 – CSV-Filter-Performance (SOURCE CANDIDATE)

**Zuerst lesen:** `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md`, `docs/project/CSV_FILTER_METHODS_CORRECTION_v0.5.43.md`, `docs/project/CSV_FILTER_PERFORMANCE_v0.5.44.md`.

v0.5.43 ist der letzte mit Windows-Build/JUnit/APK/ADB und sechs Smartphone-Funktionstests bestätigte Stand. v0.5.44 ergänzt **nur** Performance-Diagnostik der drei Rolling-CSV-Filter. Beide Analyzer-Wasserfälle bleiben unverändert. Für einen echten Android-Build unter Windows `VORTEST_INSTALL.bat` ausführen. Neue Ausgabe: `Download/LFN-Scout/RollingCsvFilterPerformance.csv` (getrennt von Messwertdateien); Export/Append auf Gerät prüfen. Keine Cloud/API/DB-Änderung.

# LFN-Scout v0.5.43 – KORREKTUR: Filterauswahl nur für CSV-Dokumentation

**Erst lesen:** `docs/project/CSV_FILTER_METHODS_CORRECTION_v0.5.43.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md`.

Die v0.5.42-Analyzer-Filterwahl wurde zurückgenommen. Original FFT + FAST und beide Analyzer-Wasserfälle sind unverändert (Quellbasis v0.5.41). Drei Methoden nur im Rolling-CSV-Filterstatistikpfad auswählbar (Legacy-IIR ist Default). Validierung Kotlin-Hosttest PASS; Android-Gradle/ADB offen. Siehe `docs/project/LFN_Scout_Signalfluss_KORRIGIERT_v0.5.43.svg`.

# LFN-Scout v0.5.42 – Filterauswahl und Performance
Aktuelle Dokumentation: `docs/project/FILTER_METHODS_RUNTIME_v0.5.42.md`. Ausgangsbasis vollständige v0.5.41. Drei auswählbare FAST-Verfahren bei STOP, letzter Legacy-Modus als Default, FFT mit 512-ms-Fenster, Echtzeitanteil pro Audioblock. Kotlin-Hostprobe PASS, Android-Gradle/APK/ADB **OFFEN**. Bestehende Direktiven und Aufgaben weiterhin verbindlich.

# LFN-Scout – v0.5.41

Ausgangspunkt war die vollständige v0.5.40-Übergabe. Beibehalten: `docs/project/HANDOVER_v0.5.40_2026-10-11.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `PROJECT_STATUS.md`, `CHANGELOG.md`, `docs/project/DEFERRED_TASKS.md`.

**Neu:** `FILTERVERGLEICH_STARTEN.bat` für Offline-Vergleich Legacy-IIR / Butterworth4 / FFT. NumPy und SciPy erforderlich. Ergebnis: `Filtervergleich_v0.5.41/VERGLEICHSBERICHT.md`. Referenzprüfung siehe `docs/project/filter_comparison_evidence_v0541/`.

**Teststatus:** 18/18 Python-Selftests PASS (lokale Python-Umgebung), Windows-Gradle/Android-ADB v0.5.41 **NICHT geprüft**. v0.5.39 bleibt der letzte nachgewiesene Android-Build. Für Android bitte `VORTEST_INSTALL.bat` unter Windows ausführen und Logs sichern. Keine Produktivänderungen an Filtern/Cloud/DB/CSV.
