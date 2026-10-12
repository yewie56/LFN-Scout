# Build-Verifikation LFN-Scout v0.5.17

Datum: 24.09.2026. VersionName `0.5.17`, versionCode `160`, Paket `de.brummbeobachter.app`.

| Prüfung | Status |
|---|---|
| ZIP-Quellbasis v0.5.16 original separat gesichert | PASS (bestehendes Archiv) |
| Änderung an Manifest-Orientierung für Main/Reminder | Quellcode implementiert |
| Statischer XML-/Manifest-Vertrag `tools/verify_portrait_v0517.py` | PASS (1 statischer Vertragstest) |
| `:app:testDebugUnitTest` mit Android SDK | NOT RUN |
| `:app:assembleDebug` mit Android SDK | NOT RUN |
| Xiaomi Portrait-/Querhalte-Test mit laufendem 8-kHz-/Direkt-Analyzer | NOT RUN |
| Xiaomi Reminder-/ROLLING-Regression | NOT RUN |

Auf dieser Arbeitsumgebung sind Android-SDK und geeignete Build-Umgebung nicht bereitgestellt. Keine APK oder Gerätefreigabe behaupten. Vor Installation: Windows `PREPARE_PROJECT.bat`, Gradle-Tests und Debug-Build; dann Geräteschritte. Das Manifest verhindert bei unterstützten Android-Konfigurationen die gewöhnliche Sensor-Drehung der Activity; gerätespezifische Besonderheiten müssen geprüft werden.
