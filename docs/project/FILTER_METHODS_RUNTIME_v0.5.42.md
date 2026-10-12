# LFN-Scout v0.5.42 – Wählbare FAST-Filterverfahren und Performance

## Implementiert (Quellkandidat)
- **Legacy-Biquad**: bisherige unveränderte digitale 2.-Ordnung-Konstantbandbreite; unveränderter Default beim Upgrade.
- **Butterworth HP2 + LP2**: Digitaler HP zweiter Ordnung + LP zweiter Ordnung in Serie; zusammen bei MIN>0 vierter Ordnung. Für das Band ab DC nur LP2. Die Eckfrequenzen der Einzelstufen sind jeweils -3 dB; die Gesamtbandverstärkung ist keine auf 0 dB normalisierte Bandpassmitte.
- **FFT-Hann**: 4096 Samples bei 8 kHz = 512 ms Hann-Fenster; neue Analyse alle 512 Samples = 64 ms. FFT-Radix-2; Bin-Energie über nominelle Frequenzgrenzen summiert, Hann-Energie normalisiert; DC getrennt durch positive-Frequenz-Einseitengewichtung berücksichtigt. Keine kontinuierliche Audiofilterung, keine 5-ms-Impulslokalisation. Wegen endlicher FFT-Auflösung sind Nominalgrenzen nur näherungsweise gültig.

## Bedienung
Unter Analyzer 8 kHz → Einstellungen → Filterbank stehen drei Alternativen zur Verfügung. Auswahl wird dauerhaft in `lfn_scout_live_analyzer/filter_method` gespeichert. Wechsel nur bei STOP; neuer START erzeugt eine neue Filterbank-Instanz, ohne zweiten AudioRecord. Vorhandene Frequenzgrenzen (0–200 Hz), 2–40 Bänder, Anzeigemodi und dB-Farbskala bleiben.

## Performance
`FastFilterBank.performance()` liefert für die bisherigen Blöcke Mittelwert und Maximum in Mikrosekunden sowie den Zeitanteil 100 × CPU-Wall-Time / Audiozeit. Es handelt sich um die beobachtete Filterbank-Rechenzeit, **nicht** um die Android-Gesamt-CPU-Auslastung oder Energieaufnahme. JIT, Garbage Collection, Blockgröße und Gerätemodell beeinflussen die Werte. Vergleich auf demselben Gerät, mit gleichem PCM-Signal, 10/20/40 Bändern, mindestens 3 × 120 Sekunden, gleicher thermischer Ausgangssituation und Protokoll von P50/P95/P99 empfohlen. Die Anzeige ist ein vorläufiges Diagnosemittel.

## Offene Grenzen und Regression
- Das ursprüngliche Offline-Modell in v0.5.41 mit Legacy-Hoch-/Tiefpass 1. Ordnung ist **nicht** identisch mit dem Android-FAST-Biquad in `FastFilterBank.kt` (2. Ordnung). Deshalb dessen Bandbreitenzahlen nicht auf den Android-Legacy-Modus übertragen.
- Butterworth4 hat vor allem bei sehr schmalen, niedrigen Bändern lange Einschwingzeiten; Verstärkung/physikalische Kalibrierung und Übersprechen unabhängig vermessen.
- FFT-Hann braucht initial 512 ms Messdaten; die 64-ms-Hop-Ausgabe ist ausdrücklich nicht 5-ms-Ereignisauflösung und lässt kurze Transienten je nach Fensterposition verdünnt erscheinen.
- Werte verschiedener Methoden unterscheiden sich systematisch. Nach Umschaltung keine aus Messläufen verschiedener Methoden stammenden Pegel naiv gleichsetzen.
- Bestehende Upload-Queue mit `SENT_PENDING_DELETE` und 24-h-Retention sowie die vier lokal-only Impulskennwerte bleiben unverändert. Filterdiagnosewerte werden nicht in die Cloud übertragen.
- Aus Sicherheitsgründen kein automatischer Methodenwechsel während RUN; dieser erfordert STOP.
- Protokoll: `kotlinc`-Hostprüfung des FastFilterBank-Quellcodes PASS, synthetischer Sinus 35 Hz jeweils im Band 30–40 Hz gefunden, endliche Ausgabewerte, Benchmark verfügbar. Gradle/JUnit/Android-APK/ADB **NICHT AUSGEFÜHRT** (Gradle Distribution in Container nicht vorhanden/Netzwerk gesperrt). Windows `VORTEST_INSTALL.bat` und Gerätetest erforderlich.

## Gerätetestplan
1. Mit `VORTEST_INSTALL.bat` compileDebugKotlin, Unit-Test und APK-Build ausführen.
2. App 8k starten: Default Legacy-Biquad, gleiche Anzeige wie v0.5.41; STOP; Butterworth wählen; Neustart Messung; FFT wählen; Neustart Messung.
3. Je 10 und 20 Bänder mit Sinus 5, 35, 95 Hz, Impulsfolgen, Pause/Run/STOP und App-Neustart prüfen. FFT-Warmup 512 ms beachten.
4. Jede Methode identische 120-s-Signale, je drei Läufe, Gerät kühl/warm getrennt testen. Echtzeitanteil, Maximum, Audio-Underruns, thermische Drosselung, Batterieverbrauch prüfen.
5. Bestehende Funktionen regressiv testen: zwei Wasserfälle, Setup-Slots 1–4, WAV/CSV/Screenshot, Cloud-Retry und 24-h-Aufbewahrung, Direkt-Analyzer, Mikrofon-Pause/STOP, Orientierungssperre.
6. Ergebnisse erst nach bestandenem Android-/Gerätetest als PASS kennzeichnen.
