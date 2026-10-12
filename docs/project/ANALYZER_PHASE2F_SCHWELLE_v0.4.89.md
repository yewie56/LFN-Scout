# LFN-Scout v0.4.89 – Highspeed-Impulsschwelle

## Änderungen (nur Entwicklungs-/Diagnosepfad)

- **Analyzer-Gain** 0–50 dB (unabhängig, unverändert gegenüber 0.4.88).
- **Minimale Impulsschwelle** 50–3000 PCM vor Gain, 50-PCM-Schritte (Robust: 1400; Mittel: 700; Empfindlich: 250).
- **Rauschmultiplikator** 1,5–12,0 in 0,5-Schritten (Robust: 6,0; Mittel: 4,0; Empfindlich: 2,5).
- Die Presets verändern **nur** die beiden Schwellenparameter und lassen die Verstärkung unverändert. Jede manuelle Änderung der beiden Schwellen wechselt die Presetanzeige ggf. zu „Manuell“. Einstellungen werden lokal gespeichert, niemals mit der WAV-Aufnahme synchronisiert.
- Auslesbar: letzte Blockspitze und Gesamt-Maximum, Rauschbasis, feste und adaptive Rohschwelle, effektiver Maximalwert, letzte Ereignisdauer/Peak sowie Verluste und Resets. Ein Knopf kopiert nur dieses kompakte Diagnoseprotokoll lokal in die Zwischenablage.

## Berechnung und Grenze

Die feste Rohschwelle ist `max(1, round(minPeakPcm / 10^(gainDb/20)))`. Die adaptive Schwelle ist `noiseMultiplier * noiseAbs`. Die tatsächliche Schwelle ist das **Maximum** der beiden Rohschwellen. Rauschbasis wird weiterhin aus nicht aktiven, nicht ausgelösten Samples im unverstärkten PCM ermittelt. Digitaler Gain verbessert nicht das SNR. Preset „Empfindlich“ kann in lauter Umgebung häufige Fehltrigger ergeben; für diesen Fall höheren Rauschfaktor oder die robuste Voreinstellung wählen. Angezeigte Impulsdauern beruhen auf Schwellwerten, nicht auf kalibrierter physikalischer Druckdauer.

Der bestehende Android-Capture bleibt 8 kHz und die WAV-Aufnahme 1 kHz. Kein zusätzlicher Mic-Owner, keine automatische WAV/PCM-Speicherung, keine Änderung am bestehenden Recorder. 48-kHz-Hardwareerfassung sowie das eigentliche Analyzer-Spektrum/Standalone-App bleiben separate Arbeiten.

## Geräteprüfung nach Build

1. `PREPARE_PROJECT.bat`, `:analyzer-core:test`, `:app:testDebugUnitTest`, `assembleDebug`, Android Studio Run. Fehler nicht überspringen.
2. Rolling-Buffer ein, Diagnose starten, zuerst Robust/+0 dB und jeweils fünf einzelne moderate Impulse/30 s Ruhe messen; dann Mittel und Empfindlich bei gleichem Gain wiederholen. Min-/Adaptiv-/Effektivschwelle und Fehltrigger notieren.
3. Schwelle manuell ändern, kurz diagnostizieren, App neu starten und Persistenz kontrollieren. Zwischenablageprotokoll prüfen (ohne Identifikatoren/Standort).
4. WAV (120 s) und Wasserfall parallel zum Diagnosetest prüfen; Drop-/Fehlerzähler beobachten. Auf dem Gerät sind diese neuen Funktionen noch **nicht** freigegeben.
