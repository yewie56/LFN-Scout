# Analyzer-Build-Hotfix v0.4.95.1

## Fehlerbild

Windows/Android `:app:compileDebugKotlin` in v0.4.95: dreimal `Unresolved reference 'acceptDiagnostics'` (`LiveAnalyzerController.kt:110,136,174`). Der Roh-PCM-Sessionvertrag fordert zwei Callbacks: `(LiveAnalyzerCore.Spectrum) -> Unit` und `(AnalyzerRawLfSession.Diagnostics) -> Unit`. Die Diagnosefunktion war versehentlich beim Controller-Umbau entfernt worden.

## Korrektur

`acceptDiagnostics` als private Methode unter `synchronized(lock)` wiederhergestellt. Sie speichert `latestDiag`, wenn `running`, damit `Snapshot`/Signalalter/Diagnose wieder aktuelle Roh- und LF-Werte auswerten. Es wird **kein** weiteres Mikrofon geöffnet, kein PCM/WAV verändert und keine Visualisierung umgestaltet. Die drei Stellen des `startRawLf`-Aufrufs sind identisch zu v0.4.95.

## Prüfung und offene Punkte

`verify_v04951_diagnostics_contract.py` validiert Callback-Existenz, Parameter-Typ und alle drei Verwendungsstellen. Zusätzlich Host-Kotlin-Selbsttests. Das ersetzt keinen Android-Kotlin-/Compose-Build. Windows: `PREPARE_PROJECT.bat`, `gradlew.bat :app:testDebugUnitTest --stacktrace`, anschließend Android Studio Run. Autorange (Tipp dBFS-Achse, Erfassung 2 s, einstellbarer ±5-dB-Zuschlag, persistente Grenzen) ist für v0.4.96 geplant, **hier noch nicht enthalten**.
