# LFN-Scout v0.4.90 – Live-Spektrum und klassischer Wasserfall

**Status:** Android-Quellen integriert; `:analyzer-core` mit synthetischen Fixtures geprüft. Android-/Xiaomi-Build und Echt-Mikrofontests dieser Version **NICHT DURCHGEFÜHRT**. Keine Aussage über zuverlässige kalibrierte SPL-Messungen.

## 1. Quellenpriorität und Scope

- Baseline: vom Nutzer erneut bereitgestellte vollständige `LFN-Scout_Android_v0.4.89(1).zip`.
- Normativer Designvertrag: `docs/project/analyzer_v0.7/`, besonders die UI-Quellen und das Bildregister. Verbindliches neues Hauptbild aus dem Nutzerchat (2026-09-19) ist die visuellen Referenz. In den ursprünglichen Assets enthaltene ältere Achsen-/Skalenwerte sind nicht normativ.
- **Kein Impuls-Wasserfall**, keine neue Impulsberechnung: bisherige Entwickler-Diagnose v0.4.89 bleibt unverändert.
- Eigenständige Standalone-App und 48-kHz-Breitbandmodus bleiben spätere Vorhaben.

## 2. Reale Codeanker

- `app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt`: app-langlebiger Controller mit `AnalyzerSessionGateway.pcm` (1-kHz-PCM aus vorhandenen Rolling-/Recording-Produzenten). Keine zweite `AudioRecord`-Instanz, kein eigener WAV-Pfad. Session lebt bei internem Panelwechsel weiter, bis Benutzer Stop wählt oder App-Prozess beendet wird.
- `app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt`: Compose-Panel über `AppPanel.ANALYZER`, Einstieg über „Zusatzfunktionen → Spektrum-Analyzer öffnen“. Live/Mittel/Rauschboden-Schätzung, 120-s-Wasserfall, 2–25-Hz-Bandstatistik, Cursor, Pause, Wasserfallvergrößerung, Settings-Register und Setup-Slots.
- `app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt`: allgemeine Session auf bis zu 200 Hz, reiner Kotlin-DSP in `:analyzer-core`. `LiveAnalyzerCore.kt` weiterverwendet ohne DSP-Änderung.
- Offscreen-PNG wird über Android Bitmap/Canvas **ohne sichtbare Compose-View** im Hintergrund-Writer erzeugt. Android 10+ MediaStore `Download/LFN-Scout/`; vor Android 10 app-spezifischer Bilderordner. Bei manuellem Speichern wird mindestens ein echtes FFT-Ergebnis vorausgesetzt.

## 3. Funktion und genaue Grenzen

| Merkmal | Ist v0.4.90 |
|---|---|
| Eingang | vorhandene 8-kHz-Mikrofonerfassung / 8:1-Downsampling auf 1 kHz / bisheriger Recorder-Gain; kein autarker Analyzer-Mikrofon-Owner |
| LF-Obergrenze | 20, 50, 100 (Default) oder 200 Hz; lineare Frequenzachse, 0 Hz Untergrenze |
| FFT | 4096, Hann, DC-Abzug, 512 Hop, ca. 0,244 Hz Bins, ca. 0,512 s je Zeile |
| Spektrum | Live (gelb), 5-s-Mittelung (rot), schätzender Min-Tracker (grau) |
| Wasserfall | Inferno-ähnliche LUT, maximal 235 Frames (~120,32 s), bei Pause eingefroren |
| L50/L90 | Prozentile über ~120 s, 2–25 Hz, **relativer dBFS-Bandwert aus FFT-Bins**; keine Norm-/SPL-Konformität nachgewiesen |
| Screenshot | Manuell PNG ab erster echter FFT; Auto erst nach vollständiger Historie, danach mindestens 120 s; Offscreen-Render unabhängig vom sichtbaren Panel |
| Setups | drei lokal persistierte Slots für LF-Obergrenze/PNG-Automatik, Standardwerte persistent |
| Audio-Konflikt | benötigt PCM eines aktiven Rolling-Buffers oder der vorhandenen WAV-Aufnahme; expliziter Button zum Einschalten von Rolling; bei nicht vorhandenem Capture nur Wartezustand |
| Mithören, Kalibrierung, Audio-Tab, Superfast | Im Live-Panel als **noch nicht implementiert** benannt, vorhandene separate Funktionen unverändert |
| Histogrammspalte rechts | ausstehend, keine erdachten Histogrammdaten |
| 20 kHz / 48 kHz | ausstehend (kein falsches Versprechen durch 1-kHz-Eingang) |

`Save aktiv` speichert nur PNG, niemals Mikrofonaudio. Auto-Save ist nicht mit OS-weitem Dauerbetrieb gleichzusetzen; Android-Hintergrund/Screen-off benötigt einen separaten Foreground-/Power-Management-Test. Screenshots werden bei fehlendem MediaStore nicht als erfolgreich gemeldet. Die PNG-Erstellung läuft nicht im AudioRecord-Thread; die asynchrone PCM-Queue ist begrenzt.

## 4. Testmatrix / Reproduzierbarer Einstieg

1. Windows: Projekt neu entpacken, `PREPARE_PROJECT.bat`, dann `gradlew.bat :analyzer-core:test`, `gradlew.bat :app:testDebugUnitTest`, `gradlew.bat assembleDebug`.
2. In Android Studio auf dem Xiaomi: Zusatzfunktionen → Spektrum-Analyzer öffnen; `Rolling-Buffer einschalten`, danach `Start`. Nach 4–5 s muss eine reale Kurve und fortlaufend ein Wasserfall zu sehen sein.
3. Mit bekanntem 20-/100-/180-Hz-Ton die angezeigten Bins vergleichen, aber **kein** vollständiger Messgenauigkeitsnachweis ohne externes kalibriertes Gerät.
4. Frequenzbereiche 20/50/100/200, Pause/Fortsetzen, Wasserfall vergrößern, drei lokale Setup-Slots nach App-Neustart.
5. Nach ~120 s PNG-Auto aktivieren, auf anderes Panel wechseln, zurückkehren und erstelltes PNG im Downloadordner prüfen; nach weiteren 120 s erneut prüfen. Veränderte Pegel müssen im späteren PNG sichtbar sein.
6. Parallel WAV 120 s aufnehmen, alte Offline-Wasserfallfunktion erzeugen, Seismik/Bewertungen prüfen; keine zwei gleichzeitig geöffneten Mikrofone.
7. `Drops`/Callback-Fehler, PNG-Fehlermeldungen sowie Hintergrund/Screen-off separat dokumentieren.

**Nicht als PASS behauptet:** alle formalen T-01…T-71 aus v0.7, exakte L50/L90-Kalibrierung, Foreground-Lifecycle und Gerätetests der v0.4.90.

**Freigabestatus:** Android-Gradle-Build NOT RUN; Xiaomi-Gerätetest NOT RUN; automatische PNG- und Panelwechsel-Abnahme NOT RUN.
