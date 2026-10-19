# Systemtest: großes zusammenhängendes Entwicklungspaket 0.5.79

## Ziel und Freigabekriterium
Keine Folge von Mini-Releases. Eine Smartphone-Testfreigabe erfordert erfolgreiches Android-Build, tatsächlich ausgeführte JUnit-/Regressionstests und kontrollierten Ein-Klick-Systemtest mit ZIP-Export. Statische Strukturprüfungen ersetzen die Kompilation nicht.

## Implementierte Prüffamilien
- Scout-eigenes Rolling über drei tatsächliche Analyzer STOP/RUN-Zyklen und PCM-/Generation-/Lifecycle-Zähler, nur wenn die sicheren Vorbedingungen erfüllt sind.
- Isolierte echte Repository-Schreib-/Lese-/Wechseltests für zwei A/B-JSON-Setups einschließlich separater MON-/Filterbankwerte.
- Persistenter, atomarer Save von Testsetup B und Reopen, A-Nichtbeeinflussung, MRU-Konsistenz, fail-closed bei ungültigem Save und ungültiger Selektion.
- Kennzeichnung aller nicht tatsächlich ausführbaren Prüfungen als SKIP. Keine Benutzer-Setups oder laufende Aufnahmen werden vom Fixture-Test geändert.

## Offene Arbeit – verbindlich für vollautomatisches Ziel
1. Tatsächlich gesteuerter produktiver STOP-/RUN-Setupwechsel über dedizierte Testprofile, mit garantiertem Rollback.
2. Aktive Filterbank-Mutation und MON-EIN-Wiederherstellung, statt nur lesender Vergleiche.
3. Blocksequenznummern für erfasste und geschriebene PCM-Blöcke, einschließlich GAP-Zähler und unabhängiger Prüfstichprobe.
4. Statistik- und Aufzeichnungsabdeckung inklusive Erfolg/Ausfall beim Dateischreiben.
5. Robuste Crash-/Kill-Recovery für Test-Transaktion und Testbericht.
6. Tatsächlicher Android-Build und ausgeführte JVM-Regressionstests.

## Status
Quellkandidat, ausdrücklich **keine** Smartphone-Testfreigabe.

## Arbeitsstand nach Übergabe (19.10.2026; UNFREIGEGEBENE ARBEITSKOPIE)
- Der Live-Bericht erhält eine `runId`, damit Exporte einem automatischen Testlauf zugeordnet werden können.
- MON-EIN-STOP/RUN wird nur dann als PASS bewertet, wenn MON vor dem Test tatsächlich eingeschaltet war und die realen Zyklen ohne Fehler abgeschlossen wurden; bei MON AUS SKIP.
- Gradle-Aufruf versucht; BLOCKIERT: Gradle 8.13 nicht installiert und `services.gradle.org` nicht auflösbar. Kein Android-Build und keine ausgeführten JVM-Tests.
- PCM-Blocksequenzen, Dateischreibbestätigungen, Statistikabdeckung und produktionsnahe isolierte aktive Setup-/Filterbankwechsel weiter offen. Nicht ausliefern.
