# BUILD_VERIFICATION v0.5.13 – 24.09.2026

Basis v0.5.12/155 mit restaurierter kumulativer README → v0.5.13/156.

| Prüfung | Ergebnis | Aussagegrenze |
|---|---|---|
| `bash tools/RUN_WATERFALL_NOTICE_V0513_SELFTEST.sh` | PASS 11/11 | Tatsächlich ausgeführter Kotlin-JVM-Test des reinen Format-Helfers; keine Compose-Positionierung |
| `python tools/verify_v0513_notice.py` | PASS 17/17 | Quellvertrag: 5-s-Key, Position, bisherige Fonts, statische Zeilen entfernt; Renderer-, Zeitachse- und Icon-Binärgleichheit; keine Android-Typprüfung |
| Kotlin-PSI-Parser (`LiveAnalyzerScreen.kt` + `AnalyzerWaterfallNotice.kt`) | PASS 2/2 | Syntax ohne Android-/Compose-Typprüfung |
| v0.5.12 FILTER-Autorange-Hostregression | PASS 10/10 | Nur unveränderter FILTER-Kern |
| bestehende v0.5.09 Direkt-Analyzer-Hostregression | PASS 33/33 | Nur unveränderter Direkt-Core |
| historischer v0.5.12 Integrations-Check | 17/18, erwartetes versionCode-/versionName-Kriterium FAIL | Historischer Prüfvertrag ist an v0.5.12/155 gebunden; alle 17 funktionalen Regeln weiterhin PASS; neuer v0.5.13 Vertrag vollständig PASS |
| ZIP/Manifest | PASS (siehe Paketprüfung) | Integrität, keine UI-Freigabe |
| `:app:testDebugUnitTest` und `:app:assembleDebug` | NOT RUN | Android SDK/Gradle-Umgebung hier nicht vollständig vorhanden |
| Xiaomi/Android-Gerät | NOT RUN | Sichtbare zweizeilige Position/Dauer/Bildexport noch abzunehmen |

Die statische Prüfung vergleicht die `makeWaterfall`-Methode und den eigenständigen FILTER-/Zeitachsenrenderer bytegenau mit dem vorherigen Stand. Die tatsächliche Compose-Integration benötigt einen Windows-Gradle-Build. Bekannter 16:1-Pegelverlust und FAST-Onset bleiben offen.
