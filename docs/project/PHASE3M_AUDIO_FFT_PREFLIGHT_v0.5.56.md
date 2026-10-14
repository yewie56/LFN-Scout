# v0.5.56 – Phase 3M: Audio-/FFT-Umschaltprüfung (Quellkandidat)

## Zweck
Neuer lesender Vorabvergleich des gewählten JSON-Setups mit dem LiveAnalyzerController-Snapshot. Drei disjunkte Kategorien **geänderter und vergleichbarer** Legacy-Parameter: 12 bekannte Anzeigefelder, 13 Felder mit STOP/Neustart-Anforderung (Audioquelle, Sampling, FFT, Frequenzbereich, Filterbank-/History-Konfiguration), andere bislang nicht im JSON-Laufzeitpfad unterstützte Felder. Fehlende und `null`-Felder werden getrennt ausgewiesen. Bereits gleiche Felder werden nicht als ausstehende Änderung gezählt.

## Sicherheit
Es gibt in dieser Phase **keine** Audio-/FFT-Apply-Funktion; `canApplyCompleteSetup` ist explizit `false`. Die Umschaltprüfung selbst ist lesend, verändert weder laufenden Analyzer noch JSON-Dateien/SharedPreferences. `running` und Zustand der MON-Aufnahme werden nur dokumentiert, nicht als umfassende Recording-Freigabe interpretiert. Beim tatsächlichen zukünftigen Anwenden müssen beide Zustände erneut geprüft werden; insbesondere bleiben Scout-Rolling-Aufnahme und weitere Recorder gesondert zu koordinieren.

## Tests
`python tools/verify_lfn056_phase3m.py`: 18/18 statische Invarianten PASS. Ein Android-Kotlin-/Gradle-Build und Smartphone-Regression bleiben offen. GitHub-/Gradle-/Signierkonfiguration sowie Termux v4 nicht verändert; `versionName = "0.5.55"`, `versionCode = 213`.

## Folgephase
Vollständige Bewertung der Aufnahmezustände und ein transaktionssicherer Controller-Draft für gestoppte Audio-/FFT-Wechsel mit expliziter Rücknahme. Keine direkte Nutzung der bislang persistierenden Legacy-Setter ohne Schutz.
