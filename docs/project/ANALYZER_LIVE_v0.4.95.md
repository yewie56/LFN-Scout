# LFN-Scout Live-Analyzer v0.4.95 – Anzeige- und Timing-Vertrag

## Benutzerabnahme v0.4.94 und neue Wünsche

V0.4.94 zeigt auf Xiaomi Live-Spektrum und 120-s-Wasserfall, mit Rohquelle UNPROCESSED, Rohpeak/RMS und LF-Peak/RMS; gemeldet: 0 Drops, 0 Resets, 0 Callbackfehler. Die unmittelbaren Wünsche wurden nachträglich konkretisiert; jüngste Vorgabe **Default −60 bis −120 dBFS** hat Vorrang gegenüber früherem −20/−40.

## Implementierung v0.4.95

- LF-Mikrofonpfad weiterhin 8 kHz PCM, 8:1 FIR→1 kHz, kein zweiter Mikrofonzugriff; bestehende WAV-/Rolling-Funktion bleibt unberührt.
- FFT-Größe 4096; LF-Hops 10, 50, 100, 200, 500, 1000. Default 100. Hop 10 ist eine rechnerische Kadenz von 100 FFT/s, keine garantierte Update-/Bildrate. UI-Abfrage 50/100 ms bleibt unabhängig.
- Wasserfall-Zeilentakt 100/200/250/500/1000 ms, Standard 250 ms. Effektive Schrittweite ist mindestens die FFT-Kadenz, wenn sie langsamer ist. Kapazität =120000 / effektives Intervall. Alle Reihen in absteigender Zeit (neueste oben), 120 s Historie. UI rastert auf max. 480 echte Alterszeilen; Offscreen-PNG nutzt den vollen History-Speicher.
- Spektrum und Wasserfall farbcodiert mit anfänglich −120…−60 dBFS (Min…Max), optionale Obergrenzen −40 und 0; untere Grenze −120 fest. Gleiche Skala im PNG. Nach dem Ändern der Displayrange wird die FFT/PCM-Session nicht gestartet.
- Kopfzeile: `LFN-Scout · 0–100 Hz` (dynamisch 20/50/100/200 Hz), darunter `Live · 8 kHz → 1 kHz FIR · dBFS · Gain +19 dB uncal` (Gain-Wert aktuell). Keine weitere Spektrum-Überschrift, keine zweite Gainangabe, keine Legendenzeile. L50(2–25)/L90(2–25) oben rechts innerhalb des Spektrums, relativ/unkalibriert; Rauschboden default AUS.
- Drei Setup-Slots sichern Range, FFT-Hop, Wasserfallintervall, dB-Obergrenze, Mittel/Rauschboden und Auto-PNG. Alte v0.4.94-Setup-Wasserfall-Hop-Zahlen werden nicht als Millisekunden fehlinterpretiert. Bestehende globalen Preferences der alten Hop-Variante werden auf neuen Default umgestellt.

## Tests und Grenzen

Host-Kotlin-Selbsttest der 30 Hop-/Zeitkombinationen, 50-Hz-Sinus und Raw-PCM-Hub; alte LF-FIR-Regressionssuite; statische Quellenprüfung für v0.4.95. Android-SDK/Compose-Build und Xiaomi-Tests müssen separat erfolgen. Manuelle/automatische PNG-Dateien, CPU- und Thermiklast bei Hop 10 und UI-Proportionen sind noch nicht gerätegeprüft. Unkalibriert: kein dB SPL.
