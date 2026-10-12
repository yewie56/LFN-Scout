# LFN-Scout v0.4.98 – Wasserfall-Auswertungsmodi

Stand 2026-09-20. Basis v0.4.97 (versionCode 138), neuer Stand v0.4.98 (139). Kein vollständiger Android-/Xiaomi-Abnahmetest in dieser Umgebung.

## Änderung

Das bestehende Register „Anzeige“ enthält eine Auswahl für **Aktuell / Mittelwert / Maximum**. **Aktuell bleibt Default**. Für jede Wasserfallzeile werden alle gültigen FFT-Spektren seit dem letzten tatsächlichen Zeilenzeitpunkt berücksichtigt:

- Aktuell = letztes FFT-Spektrum des Zeilenintervalls, unverändertes Bestandsverhalten.
- Mittelwert = frequenzbinweise Mittelung von **linearen Leistungen** `10^(dBFS/10)`, dann `10*log10(P_mean)`. Keine arithmetische dBFS-Mittelung.
- Maximum = größter dBFS-Pegel je Frequenzbin aller FFT-Spektren im selben Zeilenintervall. Dies ist **kein** Peak/Max-Hold des sichtbaren Spektrums und kein roher Sample-Peak.

Beispiel: 1 kHz LF, FFT-Hop 10, 250 ms/Zeile → bei kontinuierlicher Anlieferung ungefähr 25 FFT-Spektren/Zeile. Die FFT-Fenster überlappen mit 4096 Samples stark und sind nicht unabhängig. Ein 4,1-s-FFT-Fenster begrenzt die zeitliche Auflösung kurzer Ereignisse weiterhin. Die Auswahl ist persistent und wird in den drei bestehenden Setup-Slots mitgespeichert. Die PNG-Ausgabe verwendet exakt die fertigen gespeicherten Zeilen; der gewählte Modus wird im PNG-Fuß genannt.

Ein Moduswechsel setzt nur **Wasserfall-Historie und beginnende Zeilenaggregation** zurück (keine Mischung verschiedener Auswertearten in einem Wasserfall), nicht FFT, Recorder/WAV, L50/L90 oder rote Average-Kurve. Auto-PNG beginnt wieder nach einer vollständig neu gefüllten 120-s-Historie. Beim vorherigen Full-History-Status darf kein PNG mit vermischten Modi entstehen.

## Nicht verändert

Kein Spektrum-Max-Hold in der UI (später). Keine neue Mikrofoninstanz, kein geänderter Roh-PCM-, FIR-, FFT-, WAV-, Supabase- oder Histogrammweg. FFT-Hop, Wasserfallintervall, Farbskala, Zeitscrollrichtung und Anzeigegain unverändert.

## Regression

`WaterfallAggregatorTest` (5 Methoden) und `tools/RUN_ANALYZER_WATERFALL498_SELFTEST.sh` (6 Prüfungen) testen Default/Aktuell, linearen Mittelwert, binweises Maximum, Flush/Reset, Input-Copy/Binwechsel und 250-ms-Zeitbasis. Verbleibende Gates: Android-/Compose-Build, Setup-Recall, realer 120-s-Export und Gerät/CPU bei FFT-Hop 10. Siehe `BUILD_VERIFICATION_v0.4.98.md`.

## Einstellpanel

Der Vorschlag zur Neuordnung **aller bisherigen plus geplanten Analyzer-Parameter** steht in `ANALYZER_SETTINGS_PANEL_VORSCHLAG_v0.4.98.md`. Er ist noch kein implementiertes Layout.
