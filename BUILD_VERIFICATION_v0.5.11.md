# BUILD_VERIFICATION v0.5.11 – 24.09.2026

Basis v0.5.10 /153, neue Quellversion 0.5.11 /154. Kein neuer AudioRecord. Experimenteller FAST-Filterbank-Bildpfad, zwei freigegebene Buttons, Direkt-5-ms-Hop und Zeitglättungsmodus; siehe `docs/project/FILTERBANK_v0.5.11.md`.

| Prüfung | Ergebnis und Grenze |
|---|---|
| Reiner JVM-FAST-Filterbank-Test (11 Fälle) | PASS 11/11 nach Korrektur des ersten fehlerhaften Referenz-Warm-ups; prüft exemplarisch 35-Hz-Sinus, keine Android- oder Impulslatenz-Messung. |
| Historischer v0.5.09-Direkt-DSP-Hosttest | PASS 33/33 im lokalen Lauf. |
| Kotlin PSI Syntax gesamter Projektdateien | PASS 186 Dateien/0 Syntaxfehler nach Ergänzung des JUnit-Tests erneut prüfen (vor JUnit-Ergänzung 185/0), **keine Compose-/Android-Typprüfung**. |
| Statische Integrationsprüfung | PASS 21/21, prüft nur verbindliche Quelltext-Strukturen. |
| Alter Timing-Hosttest v0.5.04.1 | in diesem Lauf TIMEOUT / nicht erfolgreich abgeschlossen, historischer Befund bleibt historisch. |
| Vollständiger Android-/Compose-Build `testDebugUnitTest`, `assembleDebug` | NOT RUN (Android SDK hier nicht vorhanden). |
| Xiaomi-Test einschließlich FAST-Impulsdauer/Genauigkeit | NOT RUN. |
| Vollständiger Impuls-Testplan 01–35 | NOT RUN / teilweise NOT IMPLEMENTED. |

**Keine Behauptung** über nachgewiesene 5-ms-Impulserkennung: 5 ms ist ausschließlich der DSP-Blockschritt und der zusätzliche wählbare Direkt-FFT-Hop. SLOW-Zeilen werden nicht beschleunigt.
