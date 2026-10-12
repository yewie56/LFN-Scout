# Build-/Testnachweis v0.5.18 (25.09.2026)

**Android-Version:** 0.5.18 / 161, Paket `de.brummbeobachter.app`.
**Quellbasis:** vom Nutzer zuvor vollständig hochgeladenes v0.5.17-Portrait-Projekt; separat erhalten.

| Prüfung | Ergebnis | Aussage |
|---|---|---|
| Statischer Projektvertrag `python tools/verify_v0518_local_audio.py` | PASS | Nav-Reihenfolge, lokale SAVE-Anbindung, Doku-/Version-/Gain-/Bandzahl-Textvertrag |
| Portrait-Regressionsvertrag `python tools/verify_portrait_v0517.py` | PASS | Manifest/UI-Kontext (statisch) |
| Kotlin-JVM-Hosttest `kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/StatisticsWindowEngine.kt tools/test_local_statistics_v0518.kt -include-runtime -d local-test.jar && java -jar local-test.jar` | PASS | 40 Filter, +50-dB-Korrektur, Leq/L90 und dominanter 10-Hz-Ton für synthetischen Input |
| `kotlinc` AnalyzerPcmHub + LocalAnalyzerAudioBuffer + lokale Test-Stubs | PASS | Kotlin-Typprüfung dieses Puffers, KEIN Android-Lifecycle-Test |
| Kotlin PSI-Parser | PASS | 91 App-Kotlin- und 17 Core-Kotlin-Dateien ohne Parserfehler; keine Android-Typprüfung |
| `supabase/` Inhalt gegenüber v0.5.17 | IDENTISCH | Kein geänderter vorhandener Supabase-Dateistand |
| Vollständiger `:app:testDebugUnitTest` | **NOT RUN** | Kein Android SDK / vollständige Android-Gradle-Umgebung verfügbar |
| Vollständiger `:app:assembleDebug` | **NOT RUN** | Keine APK gebaut; mögliche Compose-/Android-Typfehler noch offen |
| Xiaomi-Gerätetest v0.5.18 | **NOT RUN** | Keine neue Gerätefreigabe. v0.5.17 vom Nutzer für Portrait/Analyzer bestätigt. |

## Noch nicht als fertig markieren

- Supabase: bestehender Beobachter-Upload unverändert; **keine neuen Statistik-/Analyzer-Uploads**, keine SQL-Migration, kein vollständiger Restore.
- `StatisticsWindowEngine` rechnet lokale nominale Biquad-Statistik aus der 1-kHz-WAV. **Keine** Gleichwertigkeit mit bestehender experimenteller 8-kHz-FAST-Bank. Frequenzgang/Absolute SPL/Impulsdetektor/Modulation NICHT validiert.
- Gleichzeitiger Beobachter/Analyzer-Save und Quotenkollision sind noch geräteseitig zu testen. In Fehlerfällen bei knappem Speicher und bei App-Abbruch vor Sidecar-Publikation sind ggf. bereits bestehende Beobachter-WAVs ohne Sidecar möglich.
- Aufnahmefenster 30–600 s ist eine vorläufige Geräte-/UI-Grenze, noch nicht als optimale Speicher-/CPU-Grenze bestätigt.
- Der zuvor gainlose Rolling-Beobachter-WAV-Pfad enthält nun eine einmalige Gain-Anwendung vor WAV-Erzeugung (Quellcode); dies ist gesondert auf Xiaomi zu prüfen.
- Prüfen, ob Analyzer-Gain intern exakt einmal angewandt wird, direkter 8–96-kHz-Pfad inkl. nicht ganzzahliger Eingangsrate und fremder USB-Audioquelle; Hosttest allein beweist es nicht.

**Abnahmepfad:** `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, `gradlew.bat assembleDebug --stacktrace`; vollständiges Log, danach `docs/project/TESTPLAN_v0.5.18.md` auf Xiaomi. Vor funktionierender Abnahme v0.5.17 nicht überschreiben.
