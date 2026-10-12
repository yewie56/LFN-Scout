# LFN-Scout v0.4.99 – Test-/Buildbericht

- **Basis:** v0.4.98 / 139. **Ziel:** v0.4.99 / 140.
- **Implementierung:** neue einspaltige Analyzer-Settings mit acht Kategorien, drei aktive lokale Setup-Slots, separat manuelle dBFS-Unter-/Obergrenze mit Validierung und sofortiger Persistenz; bisherige Presets, Autorange und Wasserfallmodi bleiben.
- **Quellcodevertrag:** `verify_v0499_settings_contract.py`: 16/16 PASS (statische Referenz-/UI-/Persistenzprüfung).
- **DSP-Regression:** `tools/RUN_ANALYZER_WATERFALL498_SELFTEST.sh`: 6/6 PASS; `tools/RUN_ANALYZER_LIVE491_SELFTEST.sh`: vier ausgegebene PASS-Prüfungen.
- **Android-/Compose-Kompilierung:** NOT RUN. In dieser Umgebung fehlt das Android SDK. Ein lokaler `kotlinc`-Syntaxcheck ohne Android/Compose-Klassen ist **kein** gültiger Android-Build. Kein APK erzeugt.
- **Xiaomi-Gerätetest:** NOT RUN. Manuelle Eingabe, Tastatur/kleine Displays, Scroll-/Zurückverhalten, Setup-Restore und PNG-Speicherung auf Gerät prüfen.
- **Unveränderte Pfade:** Recorder, Roh-PCM, FFT, WAV, Supabase und private Recovery-WAVs nicht modifiziert. Verzeichnisänderungen: neues `LiveAnalyzerSettingsPanel.kt` und Dokumentation/Verifier.
- **PNG:** ab Android 10 via MediaStore nach `Download/LFN-Scout/`; ältere Versionen app-spezifisch unter `Android/data/de.brummbeobachter.app/files/Pictures/Analyzer/`. Manuelles `📷 PNG` benötigt gültiges Spektrum; Auto-PNG nach voller Historie und folgendem Zeitzyklus. Schreibstatus/Fehler im Analyzer.
- **Gerätetest-Befehl:** Nach `PREPARE_PROJECT.bat`: `gradlew.bat :app:testDebugUnitTest --stacktrace`, dann Android Studio Run. Zusätzlich Dateimanager `Download/LFN-Scout/` prüfen.

**Freigabestatus:** Android-Build und reale Bedien-/PNG-Prüfung ausstehend.
