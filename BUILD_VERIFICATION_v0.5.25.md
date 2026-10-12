# BUILD VERIFICATION – LFN-Scout v0.5.25

Version: `0.5.25` / versionCode `172`
Datum: 25.09.2026

## Umfang

- Kompakte Analyzer-Statistikbox mit `Leq(min-max)`, `L50(min-max)`, `L90(min-max)`.
- Unbewerteter Live-Leq über den vollständig gefüllten konfigurierten Statistikzeitraum.
- Manueller Screendump-Kommentar rechtsbündig unten, ohne schwarzen Hintergrundkasten.

## Status bei Erstellung

- Versions-/Lieferprüfung `tools/verify_v0525_delivery.py`: **24/24 PASS**
- Kotlin-PSI-Syntaxprüfung: **92 Dateien / 0 Syntaxfehler PASS**
- Wasserfall-Regressionscheck: **PASS**
- ZIP-/Manifestprüfung: vor Auslieferung auszuführen
- Echter `:app:testDebugUnitTest`: NOT RUN in Erstellungsumgebung
- Echter `assembleDebug`: NOT RUN in Erstellungsumgebung
- APK: NOT RUN / nicht erzeugt
- ADB/Xiaomi: NOT RUN

Bis zum realen Gradle-PASS ist v0.5.25 ein **Source Candidate**.
