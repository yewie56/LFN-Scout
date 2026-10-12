# LFN-Scout v0.4.97 – Build-Verifikation

**20.09.2026 · Baseline v0.4.96 · versionName 0.4.97 · versionCode 138**

## Ausgeführte Prüfungen

| Prüfung | Ergebnis | Geltungsbereich |
|---|---|---|
| `kotlinc` auf alle Analyzer-Core-Hauptdateien und neuen Host-Selbsttest | PASS | JVM ohne Android-Abhängigkeiten |
| `tools/AnalyzerDisplayRangeHostSelfTest.kt` | **7/7 PASS** | feste dB-Reserve, ungültige Werte, Frequenzbereich, robuste Frequenz-/Zeit-Perzentile, Ticks, Mittelwert-Reset |
| Sechs originale Analyzer-Core-JUnit-Testklassen mit lokalem JUnit-API-kompatiblem Reflection-Runner | **35/35 PASS** | Nicht Gradle/JUnit selbst; lokale Java-JUnit-Stubs ausschließlich außerhalb der Projekt-ZIP |
| `python verify_v0497_analyzer_contract.py` | **19/19 PASS** | Statischer Controller-/UI-/PNG-/Version-Vertrag; kein Android-Compose-Build |
| Android `:app:testDebugUnitTest` und `assembleDebug` | **NOT RUN** | Hier kein Android SDK verfügbar |
| Xiaomi: Autorange/Touch/Textgröße/Farbskala und PNG | **NOT RUN** | Nur durch Gerätetest zu bestätigen |
| ZIP-Integrität, SHA-Manifest und 53 WAVs | **PASS** | ZIP-CRC, 593 SHA-Einträge, 53 WAV identisch zur v0.4.96, 13 Konzept-PNGs; keine `.git`/lokalen Pfade |

## Windows-/Xiaomi-Abnahme

1. Neues Projekt entpacken, `PREPARE_PROJECT.bat`, danach `./gradlew.bat :app:testDebugUnitTest --stacktrace` und optional `assembleDebug` bzw. Android Studio Run ▶. Ein Kotlin-/Compose-Compilerfehler muss als Buildfehler behandelt und korrigiert werden.
2. Rolling-Buffer starten; Spektrum und Wasserfall müssen weiter reagieren und zueinander passende Frequenz-Ticks anzeigen. Anzeige- und WAV-Gain nicht verändern.
3. dBFS-Achse antippen; bei einem kurzen schmalen Dip bleibt die neue Untergrenze nahe dem allgemeinen Rauschpegel und sollte nicht auf den Dip absinken; 2-s-Dauer, ±5 dB, Neustart-Persistenz kontrollieren. Bei breiten realen Einbrüchen darf Autorange weiter nach unten reagieren.
4. Cursor setzen/ziehen; Hz/dBFS-Text ohne Rahmen oder gefüllte Fläche, mit Tipp auf den Text ausblenden. L50/L90 im eng anliegenden ungefüllten roten Rahmen zurücksetzen.
5. Farbskala links etwa 1/3 schmaler: innenliegende dBFS-Zahlen müssen **auch bei vier Zeichen** lesbar sein. Frequenzachsen in Flucht; Zeit- und Farbskalen-Ticks nicht abschneiden. PNG manuell und automatische Historie testen.

**Keine Behauptung eines erfolgreichen Android-Builds oder einer Gerätefreigabe.**
