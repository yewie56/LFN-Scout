# TESTPLAN v0.5.14 (durchzuführen, kein Geräte-PASS)

1. Windows: PREPARE_PROJECT.bat; `gradlew.bat :app:testDebugUnitTest --stacktrace`; `gradlew.bat assembleDebug --stacktrace`.
2. Android 8k: alle neun Gruppen; in Spektrum Peakmarker ausgegraut, in Audio Quelle=gemeinsam/Rate=8000 ausgegraut, Filterbank verfügbar.
3. Android Direkt: alle neun Gruppen; Filterbank-Felder ausgegraut, Audioquelle/Rate und Decimation nur bei STOP ändern. Andere Anzeigeparameter während RUN prüfen.
4. Glättung Direkt: Zeitmodus einschalten und τ=5 s einstellen, anschließend Moduswechsel und Setup speichern/laden; α-Modus getrennt prüfen.
5. Alte Setup-1-/Setup-4-Slots exportieren: JSON v2 enthält gleiche Schlüsselmengen und neun Gruppen; fehlende Werte `null` mit `MISSING_IN_SAVED_SLOT`; kein Import angeboten. Nach Speichern neue Werte auch in JSON prüfen.
6. Wasserfall 8k und Direkt auf 100 ms stellen und speichern; nach Neustart/erneutem Export beide Intervalle prüfen. Anzeige-Historie-Reset beim Ändern und STOP-Fälle nachvollziehen.
7. Regression: Rolling-Exklusivität, STOP+Panelverlassen-Restore, ORIGINAL-/FILTER-Icons und Umschalter, FILTER-Autorange, Zeitachse und PNG/JPG, Diagnosedaten ohne private Nutzer-/Geodaten.
8. Testausgang als PASS/FAIL mit Android/SDK/JDK und Xiaomi-Gerätestand dokumentieren; 16:1-Pegelabweichung bleibt bekannt.
