# Phase 3N – Audio-/FFT-Umschaltplan (Entwicklungskandidat)

- Die neue lesende Anzeige bereitet eine nachvollziehbare Reihenfolge für eine **spätere** transaktionale Audio-/FFT-Setupübernahme vor.
- Erkennt aktive Aufnahme, MON, Anzeige-Vorschau, laufende Messung mit STOP-pflichtigen Änderungen, fehlende/null-Felder und nicht unterstützte Felder.
- Ein Fingerprint der gespeicherten und laufenden Legacy-Parameter ermöglicht eine erneute Stale-Prüfung. Weder der Plan noch `stillCurrent` autorisieren eine wirkliche Umschaltung.
- `Plan.executable` ist bewusst fest `false`. Keine Setter, keine Dateioperationen, keine Aufnahmeunterbrechung, kein STOP oder START.
- Erforderliche Nacharbeiten: kompletter transaktionaler Executor mit tiefgehender Parameterbereichsprüfung, Echtzeit-Aufnahmeschutz, rollback-sicherem Audio/FFT-Neustart sowie Android-GitHub-Build und Gerätetests.
- Termux v4, Workflow, Cloud, Gradle-Versionen und existierende Runtime-Persistenz bleiben unverändert.
