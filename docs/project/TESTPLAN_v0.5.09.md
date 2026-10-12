# LFN-Scout v0.5.09 – Windows-/Xiaomi-Abnahmeplan (PASS/FAIL)

**VersionName 0.5.09 / versionCode 152.** Tester trägt für jeden Punkt Datum, Gerät, Ergebnis und ggf. Logcat ein. Keine Vorab-PASS-Annahme bei noch nicht ausgeführtem Test. Bei einem Android-Buildfehler keine Xiaomi-Freigabe.

## A. Windows-Build

1. Neues ZIP in eigenen Projektordner entpacken. `PREPARE_PROJECT.bat` starten, auf PASS und Java 17 achten.
2. `./gradlew.bat :app:testDebugUnitTest --stacktrace` bzw. in PowerShell ` .\gradlew.bat :app:testDebugUnitTest --stacktrace `: erwartetes `BUILD SUCCESSFUL`.
3. ` .\gradlew.bat assembleDebug --stacktrace `: `BUILD SUCCESSFUL`, APK unter `app/build/outputs/apk/debug/`.
4. Prüfen: sichtbarer Appname LFN-Scout, Version 0.5.09; Paketkennung unverändert, Datenbestand/Setups nach Upgrade erhalten.

## B. Direkt-Analyzer – Quelle und Abtastrate

5. STOP → Einstellungen → Audio: AUTO/UNPROCESSED/MIC/VOICE_RECOGNITION sichtbar. Eine Quelle gezielt auswählen, START. Effektive Quelle und tatsächlich verwendete Rate in Diagnose/Log prüfen. Unzulässige Kombination muss erklärbaren Fehler melden, nicht abstürzen.
6. Für jede Rate 8000, 11025, 16000, 22050, 44100, 48000, 96000 Hz testen, sofern Hardware unterstützt. Unterstützte Kombinationen: FFT-Zähler wächst; Diagnoserate und Nyquist stimmen. Nicht unterstützte Kombinationen **getrennt als gerätebedingt NICHT UNTERSTÜTZT** dokumentieren, nicht als PASS für die Aufnahme.
7. STOP und anschließender START mit anderer Rate; kein zweiter Recorder, keine alte Frequenzskala. Während laufender Messung sind Quelle/Rate/Decimation nicht umschaltbar.
8. Exklusivität: Bei Direktbetrieb kein gleichzeitiges Rolling; STOP allein stellt Rolling NICHT wieder her. Nach STOP+Panelverlassen zuvor eingeschaltetes Rolling mit frischem RAM-Puffer wieder gestartet; zuvor ausgeschaltetes Rolling bleibt AUS. Bei laufender Messung Panelwechsel ohne Audio-Unterbrechung.

## C. FFT, Dezimierung, Zeitbasis, Pegel

9. FFT-Größen 64/128/256/512/1024/2048/4096/8192/16384: kein Absturz; Δf = f_out/N; bei kleiner FFT darf Hop größer als N sein.
10. Decimation 0 und 1 jeweils 1:1; Faktoren 2–12 **nur wenn Eingangsrate exakt teilbar**; z. B. 48000/4 = 12000 Hz; 96000/12 = 8000 Hz. Bei 11025/2 kein falsches gerundetes Ergebnis anbieten.
11. 50-Hz-Referenzton vor/nach FIR mit geeignetem FFT-Fenster vergleichen; erwarteter Pegel im Durchlassbereich annähernd gleich. 16:1-Altfehler bleibt unabhängig davon offen. Frequenzen oberhalb Ausgangs-Nyquist dürfen nicht in den Nutzbereich aliasen.
12. Fenster: Cosine, Hann, Hamming, Blackman, Blackman-Harris, Flat Top, Rectangular, Triangular, Welch. Im Ton-Testmodus Spitzenposition und plausible dBFS-Abweichungen dokumentieren; keine NaN/Inf.
13. Transform-Intervalle 10/15/20/25/30/50/100 ms durchschalten; Datum-/Uhrzeitachse korrekt, keine mehr als fünf Zeitmarken, keine Verdichtung auf falsche Sekunden. Auf längere Historien und PAUSE/RUN prüfen.
14. α=0,00 / 0,25 / 1,00: Mittelwertverhalten plausibel; Aktuell und Maxhold bleiben unabhängig. Subtract DC EIN/AUS mit Offset-Tests vergleichen.
15. Peakmarker 0/1/2: korrektes Umschalten; bei zwei getrennten Sinustönen zwei Peaks mit Hz/dBFS; Darstellung auch in PNG/JPG. Cursor, MAX/CLR und bestehende Linien bleiben funktionsfähig.

## D. Digitale Testsignale und Geräteverhalten

16. Testsignal 1 allein: frei eingestellte Hz. Testsignal 2 allein: frei eingestellte Hz. Beide EIN: zwei Maxima. Beide AUS: vor Test-START erwarteter Hinweis; während Test keine ungewollte stumme Messung. Werte über Nyquist abweisen.
17. Reales Mikrofon und Testmodi wechseln, niemals synthetische Samples in Rolling oder WAV. TESTSIGNAL/KEINE MESSUNG sichtbar und im exportierten Bild.
18. Stay Awake EIN/AUS im sichtbaren Direkt-Analyzer: bei aktivem Messbetrieb Screen-On beibehalten bzw. normales Display-Timeout zulassen. Batterie- und Hintergrundeffekte beachten.
19. 8-kHz-Rolling + 4/8/16-FIR Regression: Aufnahme, WAV, Beurteilung und STOP/PAUSE unverändert. Historischer 16:1-Pegelfehler getrennt notieren.
20. Wasserfall-Farbskala links + schwarzes Uhrzeitband/Ticks, Spektrum/Wasserfall x-Flucht, Kameralangdruck-Meldung unmittelbar über Button; Bildexport bei einem anderen Panel im Hintergrund weiter möglich.

## E. Bei Fehler

Vollständigen ersten Compilerfehler vor dem Stacktrace bzw. Logcat-Auszug mit `AUDIO_DIAG_*` und gewählter Source/Rate/FFT/Decimation senden. Wenn möglich Diagnose-ZIP über Einstellungen → Diagnose erstellen; niemals private Schlüssel veröffentlichen.
