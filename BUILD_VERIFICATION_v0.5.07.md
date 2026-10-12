# Build-/Teststatus LFN-Scout v0.5.07 (21.09.2026)

## Nutzer-Windows-Build der Ausgangsversion v0.5.06

`gradlew.bat :app:testDebugUnitTest --stacktrace` => **FAIL** bei `:app:compileDebugKotlin`. Primärdiagnosen: `LiveAnalyzerController.kt:437:22 Unresolved reference resetMaxHold`; `LiveAnalyzerScreen.kt:590:2 Expecting }`; weitere Unresolved-Reference-/Typinferenz- und Compose-Fehler im selben UI-File.

## Korrekturen

1. `AnalyzerRawLfSession.kt`: `fun resetMaxHold() = core.resetMaxHold()` ergänzt. Der Core hat bereits den synchronisierten Reset; der Controller-Aufruf ist damit typmäßig verbunden.
2. `LiveAnalyzerScreen.kt`: schließendes `}` nach dem Ende des Analyzer-Hauptinhalts ergänzt; gesamte scrollende Column wird geschlossen, **bevor** das Popup an der äußeren Viewport-Box mit `Modifier.align(Alignment.TopCenter)` angehängt wird. `cursorReadout` und `makeWaterfall` sind wieder Datei-/Top-Level-Helfer.
3. Kein beabsichtigter Eingriff in PCM, Rolling, 48-kHz-Zustandsübergänge, PNG-/JPG-Writer oder Core-FFT.

## Verifikation dieser Korrektur

- Lokaler Kotlin-Core-MaxHold-Selbsttest: **PASS 8/8**.
- Lokaler Kotlin-Timing-JUnit-Logiktest mit Teststub: **PASS 6/6**.
- `tools/verify_v0507_buildfix.py`: **PASS** (Quelle, Subscriber-Methode, Scope-Balance, statische Anker und Versionsnummer); keine echte Compose-Typprüfung.
- Längere FIR-/48-kHz-Hosttestläufe: hier wegen Laufzeitbegrenzung **nicht abgeschlossen**, kein PASS behauptet.
- Vollständiger Android-/Compose-Build: **NOT RUN** (kein Android-SDK in dieser Umgebung). Xiaomi-Test: **NOT RUN**.
- Neuverpacktes ZIP und SHA-256-Manifest: bei Paketerstellung gesondert verifiziert.

## Windows-Rezept

```powershell
.\PREPARE_PROJECT.bat
.\gradlew.bat :app:testDebugUnitTest --stacktrace
.\gradlew.bat assembleDebug --stacktrace
```

Beide Gradle-Befehle müssen auf dem Windows-PC `BUILD SUCCESSFUL` ergeben. Erst danach manuellen Xiaomi-Test aus `docs/project/TESTPLAN_v0.5.07.md` durchführen. Bei weiterem `FAIL` den Abschnitt mit den ersten `e:`-Zeilen schicken, nicht ausschließlich den Gradle-Stacktrace. Die bekannte 16:1-Pegelabweichung bleibt offen.
