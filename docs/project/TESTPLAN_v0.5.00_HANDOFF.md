# Übergabe-Testplan LFN-Scout v0.5.00

**Keines der folgenden Kästchen wird durch die Existenz dieses Dokuments automatisch PASS.** Trage Datum/Handy/Android-Version/Ergebnis/Log-/PNG-Dateiname ein.

| ID | Priorität | Ablauf und Akzeptanzkriterium | Stand 20.09.2026 |
|---|---|---|---|
| BUILD-01 | P0 | Windows `PREPARE_PROJECT.bat`, `:app:testDebugUnitTest`, `assembleDebug`: jeweils `BUILD SUCCESSFUL` | NOT RUN für v0.5.00 |
| DEV-01 | P0 | Xiaomi installieren; dunkler Analyzer bis Navigationsleiste, kein weißer Reststreifen, beide Diagramme sichtbar | NOT RUN |
| DEV-02 | P0 | 100/200 Hz, Hop 10/100, Pause/Fortsetzen, Cursor+Reset, Hz/dBFS-Ticks | NOT RUN für v0.5.00; teils früher beobachtet |
| DEV-03 | P0 | Rolling starten, Analyzer 10 min live; keine AudioRecord-Dopplung, Drops/Fehler kontrollieren | NOT RUN für v0.5.00 |
| UI-01 | P1 | ⋮ → acht Settings-Unterpanels, Rückwege, Lesbarkeit, Scrollen, mittel/rausch schaltbar | NOT RUN |
| UI-02 | P1 | Min −120/Max −60 manuell überschreiben, Grenzprüfung (Fehler wenn Abstand <10), Neustart/Setup-Recall | NOT RUN |
| UI-03 | P1 | Autorange bei kurzen Dips (2 s, ±5 dB), persistente Grenzen, fehlerhafte Quelle ändert nichts | NOT RUN |
| WF-01 | P1 | Aktuell / Mittelwert / Maximum mit kontrolliertem Signal; Moduswechsel löscht nur WF-Historie | NOT RUN auf Gerät |
| WF-02 | P1 | 250 ms/Zeile, 480 Zeilen/120 s; nach 120 s Zeitachse/Übergang prüfen | NOT RUN |
| PNG-01 | P1 | Manuelles PNG öffnen; Achsen/Ticks/Farbskala und Verlauf vergleichen, Dateipfad/Name prüfen | NOT RUN |
| PNG-02 | P1 | Auto EIN: nach vollem 120-s-Zyklus und nach nächstem Zyklus neue Datei; auch intern anderes Panel | NOT RUN |
| AUDIO-01 | P1 | 120-s-WAV, Rolling-Snapshot, Bewertung R0..R5 und -1 ohne WAV unverändert, CLIP | NOT RUN für v0.5.00 |
| SYS-01 | P1 | Seismik/Bewertungen/Offline-Puffer unter gleichem APK gegen Regression prüfen | NOT RUN für v0.5.00 |
| PERF-01 | P1 | CPU/Temperatur/FPS bei FFT-Hop 10 vs. 50/100 messen; keine Aufnahmeaussetzer | NOT RUN |
| REST-01 | P1 | Restore auf komplett neues leeres Supabase-Testprojekt; Auth/Storage/Functions/DB E2E | NOT RUN |

**Hinweis:** `:app:testDebugUnitTest` zieht laut Projektkonfiguration `:analyzer-core:test` hinzu, aber Android Studio `Run ▶` allein führt Unit-Tests nicht zwingend aus. Keine `ignoreFailures`-Umgehung. Android-SDK fehlt in der Bereitstellungsumgebung; echte Buildresultate vom Windows-PC übernehmen.
