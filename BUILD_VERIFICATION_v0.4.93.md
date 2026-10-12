# LFN-Scout v0.4.93 – Build-/Testbericht (2026-09-20)

**Status:** Quellcode-Korrektur, **keine freigegebene APK**. Der Nutzer-Windows-Lauf mit v0.4.92 hat den Kompilierfehler nachgewiesen. Android-Gradle-Build und Xiaomi-Prüfung für v0.4.93: **NOT RUN**.

## Reproduzierbarer Befund

`gradlew.bat :app:testDebugUnitTest --stacktrace` → `:app:compileDebugKotlin FAILED` → `LiveAnalyzerController.kt:202:52: Unresolved reference 'spectrumCallbackErrors'`.

Die Android-App startet `AnalyzerSessionGateway.startRawLf()`, das eine `AnalyzerRawLfSession` zurückgibt. Diese Klasse definiert `callbackErrors` (inklusive Subscription-Listenerfehler), **nicht** `spectrumCallbackErrors`. Letzterer Name gehört ausschließlich zur alten `AnalyzerLiveSession`. Somit war der bisherige Controller unvereinbar mit dem tatsächlich verwendeten Session-Typ. `:analyzer-core:test` kann das nicht aufdecken, weil es keine Android-App-Quellen kompiliert.

## Korrektur

`LiveAnalyzerController.kt`: Zugriff in `snapshotLocked()` von `session?.spectrumCallbackErrors` auf `session?.callbackErrors` korrigiert. Kein verändertes Verhalten des Audio-/WAV-/FFT-Signalwegs. App- und Versionsdokumentation auf 0.4.93 / Code 133 aktualisiert. Neue statische API-Regression `verify_v0493_live_analyzer_api.py` hinzugefügt; dieser Check ersetzt **keinen** Kotlin/Android-Build.

## Verifikationsstatus

- Statischer Controller–Session-Vertrag: **8/8 PASS** via `python verify_v0493_live_analyzer_api.py` (nur Quelltextprüfung).
- Unveränderte synthetische Kernkette: `bash tools/RUN_ANALYZER_LIVE491_SELFTEST.sh` **4/4 PASS** (47/180 Hz, Aliasunterdrückung, Nullsignal, Raw-Hub).
- Zip-Integrität, `docs/reconstruction/file_manifest.sha256` und 53 lokale Recovery-WAVs: siehe Paketprüfung.
- `:analyzer-core:test` der v0.4.92: vom Nutzer zuvor als fehlgeschlagen gemeldet (Nullsignal-Test) für v0.4.91; v0.4.92-Resultat wurde nicht mitgeteilt. Für v0.4.93: **NOT RUN**.
- `:app:testDebugUnitTest`: v0.4.92 **FAILED (Kotlin compile)**; v0.4.93 **NOT RUN**.
- `assembleDebug`, APK, Xiaomi-Live-Spektrum/Wasserfall und visuelle Freigabe: **NOT RUN**.

## Windows-Prüfung

Neuen ZIP-Projektordner entpacken. `PREPARE_PROJECT.bat` nur bei fehlender/neuer Java-SDK-Konfiguration ausführen; dann in PowerShell:

```powershell
.\gradlew.bat :analyzer-core:test --stacktrace
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Alternativ Android Studio → Projekt öffnen → Gradle-Sync → Run. **Run führt Unit-Tests nicht automatisch aus.** Bei Erfolg Android-App auf Xiaomi prüfen. Das zuvor beobachtete fehlende Live-Spektrum und das unzureichende Design sind mit dieser reinen Kompilierkorrektur **nicht nachweislich behoben**.

**Datenschutz:** Projekt enthält private Supabase-Recovery-Artefakte; ZIP nicht öffentlich veröffentlichen.
