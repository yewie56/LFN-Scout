# BUILD_VERIFICATION v0.5.23

Datum: 25.09.2026

## Version

- `versionName = 0.5.23`
- `versionCode = 170`
- Paket: `de.brummbeobachter.app`

## Implementierter Umfang

1. Analyzer-SAVE-AUDIO verwendet dieselbe `AudioFileNamingPolicy` wie Beobachter-Audio.
2. Analyzer-WAV, Statistik-JSON und Manifest werden direkt in `Download/LFN-Scout/` geschrieben; kein neues Audio-/`LocalSaves`-Unterverzeichnis für Analyzer-Saves.
3. Analyzer-WAV wird als `AudioArchiveEntry(origin="ANALYZER")` in den bestehenden Archiv-/Retry-/24-h-Retention-Pfad aufgenommen.
4. Analyzer-WAV wird über denselben bestehenden `SupabaseClient.uploadWav()`-Pfad übertragen; Statistik-JSON/Manifest werden noch nicht nach Supabase hochgeladen.
5. Beim 24-h-Cleanup werden zum Analyzer-WAV gehörende Statistik-JSON/Manifest ebenfalls entfernt.
6. Bereits gespeicherte Analyzer-WAVs erscheinen dadurch im bestehenden lokalen Audioarchiv und können über den bestehenden Wasserfall-/Spektrum-Pfad erneut analysiert werden.
7. Manueller Analyzer-Bildsave friert zunächst den Snapshot ein, fragt danach optional einen Kommentar ab und rendert diesen vor dem finalen PNG/JPG-Schreiben unten in den Wasserfall.

## Lokale Prüfungen in der Erstellungsumgebung

- `python tools/verify_v0523_delivery.py`: **21/21 PASS**.
- Kotlin-PSI-Syntaxprüfung über alle `app/src/main/java`-Kotlin-Dateien: **92 Dateien / 0 Syntaxfehler PASS**.
- Statistik-/Gain-Hosttest mit `GainReferencePolicy.kt` + `StatisticsWindowEngine.kt`: **PASS** (40 Bänder, +50-dB-Kompensation, L90, Leq, 10-Hz-Dominanz).
- Wasserfall-v0.5.20-Regressionscheck: **PASS** (gain-referenzierte Werte unter -120 dBFS bleiben unterscheidbar/farbabbildbar).
- Der alte Sammelstarter `RUN_LOCAL_AUDIO_V0518_SELFTEST.sh` ist versionsfest auf `0.5.18` und bricht bei v0.5.23 erwartungsgemäß an seiner Versionsassertion ab; die darin enthaltene aktuelle Statistikprüfung wurde deshalb separat mit den benötigten Quellen ausgeführt und bestand.
- ACTIVE-ZIP-CRC/Probe-Extraktion/Manifestprüfung: bei Paketierung auszuführen.

## Nicht als getestet markieren

- Echter Windows-Gradle-Lauf `:app:testDebugUnitTest`: **NOT RUN** für v0.5.23.
- Echter Windows-Gradle-Lauf `assembleDebug`: **NOT RUN** für v0.5.23.
- ADB-Installation: **NOT RUN** für v0.5.23.
- Xiaomi-Gerätetest der neuen Analyzer-Audio-/Bildfunktionen: **NOT RUN**.
- Realer Supabase-Upload einer Analyzer-WAV: **NOT RUN**.

Der Nutzer soll hierfür `VORTEST_INSTALL.bat` im entpackten v0.5.23-Projekt ausführen.
