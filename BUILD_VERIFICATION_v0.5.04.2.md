# Build-Verifikation v0.5.04.2

Basis: v0.5.04.1. Zusätzliche Logs in vier bestehenden App-Kotlin-Dateien plus `AnalyzerAudioTrace.kt`; alle neuen Logger-Stellen sind non-fatal (`runCatching`, `DebugLogger` selbst fehlertolerant). Keine beabsichtigte Änderung der Audio-Modus-/FFT-Logik.

**Android/Compose-Gradle:** NOT RUN (Android-SDK im Erstellungscontainer nicht verfügbar). **Xiaomi:** NOT RUN. **Crashursache:** unbekannt; bisherige Meldungen sind Nutzerbeobachtungen. **8-kHz-Modi:** vom Nutzer als funktionierend bestätigt. Status von Code-/Selftests siehe tatsächliches CLI-Ergebnis; statische Verträge sind kein Android-Compile.

Windows-Befehle:

```powershell
.\PREPARE_PROJECT.bat
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```
