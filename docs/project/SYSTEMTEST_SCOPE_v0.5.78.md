# LFN-Scout v0.5.78 – Systemtest, Prüfumfang und Grenzen

## Automatisierte echte Prüfungen

- Isoliertes, versioniertes Analyzer-JSON-Repository im temporären App-Cache aufsetzen.
- Zwei verschieden parametrierte benannte Setups A und B (MON und Filterbank) anlegen, A→B→A auswählen, Index/MRU auf Festplatte prüfen.
- Repository neu öffnen, gespeicherte MON-Profile (einschließlich abweichender Pegel) und Filterbank-Grenzen/Bandanzahl unabhängig vergleichen.
- Ungültigen Import ablehnen und Nachweis, dass die Fixture-Dateien vollständig entfernt werden.
- Keine Änderung an produktiven Setup-Verzeichnissen und kein Umschalten der laufenden MON-/Analyzer-Parameter bei den Fixture-Tests.
- Bei bestehender Scout-eigener Rolling-Sitzung, Analyzer im 8-kHz-RUN und ohne MON-WAV: drei reale Analyzer STOP/RUN-Zyklen mit Rolling-Engine-Countern und MON-Parametervergleich.
- Zusätzlich vergleichen: Live-Filterbank-Grenzen, Anzahl Filterbänder, Anzeigebereich und Sichtbarkeit über alle drei Neustarts hinweg.
- Gespeicherter JSON-/CSV-Bericht wird unverändert exportiert.

## Noch kein Ende-zu-Ende-PASS

- Kein absichtliches A/B-Schalten *aktiver* benannter Setups: das würde produktive Konfiguration/Persistenz betreffen und benötigt eine isolierte aktive Setup-Transaktion.
- Keine absichtliche Änderung des *aktiven* Filterbank-Parameters. Die v0.5.78-Prüfung zeigt JSON-Roundtrip und Stabilität über Neustarts, nicht Filterbank-Controller-Roundtrip.
- Kein samplegenauer Nachweis von PCM-Blocklücken, Schreibbestätigungen, Statistikabdeckung oder Absturzfreiheit außerhalb der eigenen Logzeilen.
- Live-Tests bleiben SKIP, wenn die sicheren Vorbedingungen nicht erfüllt sind. Isolierte Fixture-Tests laufen trotzdem.

## Entwicklungsverifikation

- Quellcode- und ZIP-Prüfungen möglich.
- Kotlin/Compose/Android-Build und JVM-Tests müssen vor einer Freigabe ausgeführt werden. Die Erstellungsumgebung kann Gradle 8.13 wegen fehlendem Netzwerk nicht laden.
