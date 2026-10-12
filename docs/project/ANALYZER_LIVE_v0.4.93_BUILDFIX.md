# Analyzer v0.4.93 – Controller-API-/Kompilierkorrektur

Der produktive Roh-PCM-Subscriber `AnalyzerRawLfSession` hat `callbackErrors: Long`; der ältere 1-kHz-Subscriber `AnalyzerLiveSession` besitzt dagegen `spectrumCallbackErrors: Long`. Der nach v0.4.91 tatsächlich verwendete Controller war nach dem Wechsel auf `startRawLf()` an einer Stelle nicht an den neuen API-Namen angepasst. Im Windows-Build von v0.4.92 zeigte sich `Unresolved reference 'spectrumCallbackErrors'`.

`LiveAnalyzerController.snapshotLocked()` benutzt deshalb ab v0.4.93 `session?.callbackErrors ?: 0L`. Der angezeigte Fehlerzähler bleibt derselbe Snapshot-Parameter; der neue Getter umfasst sowohl Fehler der Spektrums-/Diagnosecallbacks als auch vom Hub gezählte Listenerfehler. Der echte Android-Build ist nachzuholen. Audiocapture, FIR, FFT, Recorder, WAV, Backend und Darstellungsentwurf wurden nicht geändert. Der bislang ungeklärte Xiaomi-Leerbildschirm und der gewünschte visuelle Abgleich sind keine durch diesen Fix gelösten Themen.

Prävention: `verify_v0493_live_analyzer_api.py` validiert diese konkrete statische Schnittstelle. Entscheidend für die Freigabe bleibt zusätzlich der vollständige `:app:compileDebugKotlin`/`:app:testDebugUnitTest`-Lauf unter Android-Gradle.
