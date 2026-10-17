# LFN-Scout v0.5.74 – MON-sicherer Wechsel benannter Analyzer-Setups

## Fehler und Nachweis

Auf dem Smartphone schlägt ein Wechsel zu einem anderen benannten Setup bei eingeschaltetem MON mit `Setupwechsel nur bei ausgeschaltetem MON möglich` fehl; bei ausgeschaltetem MON gelingt derselbe Wechsel.

## Eingriff

Nur `AnalyzerSimpleSetupPanel.kt` steuert eine *temporäre* MON-Wiedergabepause um die bestehende STOP-Transaktion. Reihenfolge: alten Setupstand vor MON-Stopp sichern, neues JSON vorladen, MON bei Bedarf anhalten, bestehende STOP-Transaktion inklusive Prüfung ausführen, aktive ID erst nach Erfolg setzen, MON auch bei Fehler wiederherstellen. Bei Fehlschlag wird die vorherige Analyzer-/MON-Parameterkonfiguration zurückgesetzt, soweit die Controller dies zulassen.

MON-WAV-Aufnahmen werden bewusst nicht automatisch gestoppt. Rolling-Audioquelle und Puffer bleiben unangetastet. Der zuvor gewählte MON-EIN/AUS- und SHIFT-Betriebszustand bleibt erhalten; die Parameter stammen bei erfolgreicher Auswahl aus dem neuen benannten Setup. Es existiert in der bisherigen JSON-MON-Parameterstruktur kein eigener `enabled`-Wert.

## Geräteszenarien

1. Rolling RUN / Analyzer RUN / MON EIN → Setups öffnen → Setup B auswählen → zurück: Analyzer RUN, MON EIN mit B-Parametern, Rolling ohne Unterbrechung.
2. A/B/A wechseln: MON-Parameter pro Setup bleiben isoliert; alte MON-Parameter werden vor dem Wechsel gespeichert.
3. MON AUS: B-Wechsel ohne MON-Start.
4. Speicherfehler vor dem Wechsel: Dialog mit Wiederholen, Trotzdem wechseln, Abbrechen und kein unbeabsichtigter MON-Zustandswechsel.
5. Aktive MON-WAV-Aufnahme: Wechsel wird abgelehnt; keine Aufnahme beendet.
6. Ungültiges neues JSON: alte Konfiguration wiederhergestellt, MON wieder eingeschaltet, aktive Setup-ID unverändert.
7. App beenden und neu starten: korrektes aktives Setup samt MON-Parametern.

## Grenzen

Android-Build und Hardware-Regressionslauf stehen noch aus; nur Code-Statischeinheiten können hier geprüft werden. Das Bearbeiten des Audioquellenwechsels und Statistik-Abdeckung gehören nicht zu dieser Korrektur.
