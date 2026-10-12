# LFN-Scout – Geplante Erweiterungen

Stand: 17.09.2026

Diese Datei fasst die bisher diskutierten, aber noch nicht vollständig umgesetzten Erweiterungen für **LFN-Scout** zusammen.

## 1. InversionAnalyzer-Integration ab Version 5.0

Die bestehende Funktionalität des InversionAnalyzer soll als eigenes Panel **„Inversion“** in LFN-Scout integriert werden.

Wichtig:
- Diese Erweiterung ist **nicht für die 4.x-Versionen** vorgesehen.
- Die Integration ist für **Version 5.0 und folgende** geplant.
- Die bestehende InversionAnalyzer-Logik soll möglichst übernommen und nicht unnötig neu implementiert werden.

Geplante Inhalte:
- Inversionsindex
- Temperaturgradienten
- Temperaturschichtung
- Modellwerte
- lokale Inversionsbewertung
- Radiosonden-Daten
- ICON-D2-Daten
- Zeitverläufe und Profilansichten
- Vergleich unterschiedlicher Datenquellen

## 2. KIT-Mast als Referenzdatenquelle

Die Daten des KIT-Mastes sollen als zusätzliche Referenzreihe in das Inversions-Panel aufgenommen werden.

Geplante Höhenstufen:

- 2 m
- 10 m
- 30 m
- 60 m
- 100 m
- 130 m
- 160 m
- 200 m

Geplante Darstellungen:
- vertikales Temperaturprofil
- Temperaturdifferenzen zwischen Höhenstufen
- Zeitverläufe
- Vergleich mit Modell- und Radiosondendaten
- separate Kennzeichnung der KIT-Mast-Daten

Die Darstellung soll nativ in Android erfolgen.

## 3. Daten- und Fehlerprotokollierung für Inversionsdaten

Für die InversionAnalyzer-Integration soll die Datenbeschaffung nachvollziehbar protokolliert werden.

Geplant sind:
- Zeitpunkt des Abrufs
- Quelle
- Datenstand
- Erfolg / Fehler
- fehlende Werte
- verwendete Fallback-Quelle
- Validierungsstatus

Fehler bei einzelnen Datenquellen sollen nicht zum Ausfall des gesamten Panels führen.

## 4. GitHub- und Build-Infrastruktur weiter ausbauen

Der bestehende GitHub-Actions-Workflow soll schrittweise erweitert werden.

Geplante Punkte:
- versionierte APKs unter `Builds/debug/`
- GitHub-Artifact weiterhin zusätzlich bereitstellen
- eindeutige Dateinamen nach Version
- später optional signierte Release-APKs
- Schutz gegen Build-Schleifen bei automatischen APK-Commits
- späterer Wechsel von „APK im Repository“ auf „nur Artifact / Release“ ohne grundlegenden Umbau

Beispiel:

```text
Builds/
└── debug/
    ├── LFN-Scout-v0.4.62-debug.apk
    ├── LFN-Scout-v0.4.63-debug.apk
    └── ...
```

## 5. Android-only Entwicklungs- und Build-Workflow

Der Standardablauf soll vollständig ohne eingeschalteten Windows-PC möglich sein.

Geplanter bzw. bereits vorbereiteter Ablauf:

```text
ChatGPT Work auf Android
→ Änderungswunsch formulieren
→ vollständige neue Projekt-ZIP erzeugen
→ ZIP auf dem Handy herunterladen
→ ZIP in temporären Ordner entpacken
→ neuen Projektinhalt in lokales GitSync-Repository übernehmen
→ `.git` und `Builds` erhalten
→ GitSync: Pull / Stage / Commit / Push
→ GitHub Actions: Tests + Build
→ APK herunterladen
→ APK auf demselben Handy installieren
```

Dabei soll jede neue Version als **vollständige Projekt-ZIP** geliefert werden.

Die ZIP soll **keinen `.git`-Ordner** enthalten.

## 6. Windows-/Android-Studio-Weg parallel erhalten

Trotz des Android-only-Workflows sollen die bisherigen Windows-Buildwege erhalten bleiben.

Weiterhin vorgesehen:

### Buildweg A – Android Studio

```text
PREPARE_PROJECT.bat
→ Projekt in Android Studio öffnen
→ Gradle Sync
→ Build / Run
```

### Buildweg B – Gradle-Kommandozeile

```text
PREPARE_PROJECT.bat
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

### Buildweg C – GitHub Actions

```text
GitSync Push
→ GitHub Actions
→ Unit-Tests
→ assembleDebug
→ APK
```

Alle drei Wege sollen dasselbe Projekt bauen.

## 7. Regressionstests und integrierter Selbsttest

Für neue Funktionen sollen weiterhin frühzeitig Regressionstests vorgesehen werden.

Grundsätze:
- bereits behobene Fehler als Regressionstest sichern
- automatisierte PASS/FAIL-Prüfungen
- Build nur dann als erfolgreich betrachten, wenn die vorgesehenen Tests bestanden sind
- keine dauerhafte Umgehung fehlschlagender Tests durch `ignoreFailures`

Für geeignete Funktionen soll zusätzlich ein integrierter Selbsttest vorgesehen werden.

## 8. Noch nicht verbindlich terminierte Erweiterungen

Folgende Punkte wurden als sinnvoll diskutiert, sind aber noch nicht auf eine konkrete Version festgelegt:

- vereinfachte mobile Projektaktualisierung
- weitere Automatisierung zwischen Work, GitSync und GitHub
- bequemere Bereitstellung der jeweils neuesten APK
- optionaler Wechsel zu GitHub Releases
- weitere Inversionsdarstellungen und zusätzliche Referenzquellen

## 9. Versionszuordnung

Aktueller Stand:
- 4.x: bestehende App weiter stabilisieren
- ab 5.0: InversionAnalyzer-Integration als eigenes Panel
- spätere 5.x-Versionen: Ausbau der Inversions- und Referenzdatenfunktionen

## 10. Leitlinie

Neue Erweiterungen sollen möglichst:
- einfach
- robust
- testbar
- rückwärtskompatibel
- mit möglichst wenigen Komponenten

umgesetzt werden.

Unnötige Architekturkomplexität soll vermieden werden.
