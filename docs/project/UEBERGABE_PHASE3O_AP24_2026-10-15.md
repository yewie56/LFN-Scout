# LFN-Scout – vollständige Übergabe an einen neuen Chat

**Datum:** 15.10.2026  |  **Stand:** Phase 3O, Arbeitspaket 24 – vom Benutzer im GitHub-/Termux-TEST **GRÜN** bestätigt.  
**Quellgrundlage:** `LFN-Scout_v0.5.56_PHASE3O_ARBEITSPAKET24_QUELLKANDIDAT.zip`.  
**Übergabe-ZIP:** gleiche Anwendungsquellen wie Arbeitspaket 24, ergänzt ausschließlich um diese Übergabedokumentation und die aktualisierte `CURRENT_STATE.md`.

## 1. Verbindliche Arbeitsanweisung an den neuen Chat

Fortsetzung des bestehenden Entwicklungswegs **ohne Zurücksetzen**, Neuentwurf oder Wechsel auf eine alte Version. Der Benutzer hat ausdrücklich entschieden, **Phase 3O auf Basis des grünen Arbeitspakets 24** weiterzuführen. Frühere Vorschläge des Assistenten zum Rücksprung oder zur Verwerfung von Phase 3O waren falsch und sind NICHT umzusetzen.

Vor jedem weiteren Eingriff die Projektdokumente `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `PROJECT_STATUS.md`, `docs/project/CURRENT_STATE.md`, `docs/project/DEFERRED_TASKS.md` und die einschlägigen Phase-3O-Dokumente lesen. `CURRENT_STATE.md` ist **bei jedem Paket zwingend zu aktualisieren**. Kleine, isolierte Eingriffe vermeiden, sofern ein sicherer sinnvoller zusammenhängender Arbeitsschritt möglich ist. Vollständige, am ZIP-Wurzelverzeichnis beginnende Projekt-ZIP liefern (kein zusätzliches Oberverzeichnis), statische Prüfer/Regressionen sowie ZIP-Integrität prüfen.

## 2. Eindeutige Unterscheidung von TEST und HARDWARE

- Der Benutzer meldete für AP24 **„Grün“**, später jedoch ausdrücklich: **„Nur Termux“**. Also ist AP24 ausschließlich durch den GitHub-/Termux-Build/Test bestätigt.
- Nach etwa 65 ungetesteten Entwicklungsständen wurde nun die **separat installierbare TEST-APK** auf einem Android-Gerät für einen realen Hardwaretest herangezogen.
- **Aktueller, noch ungelöster Hardwarebefund:** „Mikrofonberechtigung fehlt“. Es wurde noch nicht geklärt, ob die Berechtigung im Android-Berechtigungsdialog fehlt, verweigert wird oder ob eine Laufzeitabfrage ausbleibt.
- **Unmittelbar nächster Arbeitsschritt:** Diesen Befund gezielt diagnostizieren und beheben, bevor weitere umfangreiche Änderungen ungetestet auflaufen. Auf Gerät App-Info der TEST-App -> Berechtigungen -> Mikrofon prüfen; außerdem im APK-Merged-Manifest und in der Runtime-Permission-Anforderung prüfen. Keine Behauptung, dass Manifest-Recht allein automatisch die Android-Laufzeitberechtigung erteilt.
- **Quellbefund:** `app/src/main/AndroidManifest.xml` enthält `<uses-permission android:name="android.permission.RECORD_AUDIO" />`. `app/build.gradle.kts` enthält für den mit `-PlfnGithubTestApp=true` gebauten Debug-Typ den `applicationIdSuffix = ".githubdebug"` und die Beschriftung `LFN-Scout TEST`. `applicationId` regulär `de.brummbeobachter.app`, TEST `de.brummbeobachter.app.githubdebug`. Dies im tatsächlich heruntergeladenen APK-Manifestauszug bei Bedarf verifizieren.
- REGULAR-App sowie ihre gespeicherten Setups dürfen nicht verändert oder ersetzt werden; Test nur mit separat installiertem TEST-Build. Die getrennte `applicationId` trennt app-interne Daten, schützt aber nicht automatisch vor Manipulation gemeinsam zugänglicher exportierter Dateien.

## 3. Projektstand und technische Schutzregeln

- Projekt/Repository: `yewie56/LFN-Scout`, Branch `main`; sichtbarer App-Name `LFN-Scout`.
- Entwicklungsziel `0.5.56`; **Gradle `versionName 0.5.55`, `versionCode 213` nicht unaufgefordert ändern**.
- Produktiver automatischer Setupwechsel weiter gesperrt: **`Plan.executable = false`**. Keine Freischaltung ohne echte STOP–Reconfigure–START-Transaktion, überprüfte Hardwarefreigabe, vollständigen Rollback und Android-Gerätetest.
- Termux-Skript `~/LFN_GITHUB_TERMUX.sh` v4, GitHub-Workflows, Signierung, `gradle.properties`, `.gitignore`, Supabase/Cloud und andere nicht betroffene Bereiche unverändert lassen.
- Historisches `supabase/storage_backup_before_v0526/` bleibt auf GitHub getrackt, darf aber nicht erneut in Projekt-ZIPs aufgenommen oder durch fehlende ZIP-Dateien auf GitHub gelöscht werden.
- Bestehende funktionierende Analyzer-/Scout-Funktionen und Datenpersistenz schützen. Eine neue produktive Setup-Transaktion wurde bislang NICHT auf Android validiert.

## 4. Zusammenfassung der jüngsten Phase-3O-Pakete

- AP10–13: Synchronous Setup-/Mikrofon-Leases, zentraler Eintrittsschutz, Konkurrenz- und Regressionstests.
- AP14: Controller-Integrationshürden und Fehler-Injektion dokumentiert.
- AP15: `LiveAnalyzerController.startFromStoppedConfiguration(context, expected)` als bislang nicht produktiv verwendeter, preference-freier Eintritt.
- AP16–18: Read-only STOP-Diagnostik, Lifecycle-/Mikrofonbesitz-Abfrage und Roh-PCM-Subscriber-Zähler. Die Diagnosen allein beweisen keine physische Ressourcenfreigabe.
- AP19–20: Fehlgeschlagene `AudioRecord.release()`-Freigaben einschließlich verworfener Kandidaten quarantänisieren den Mikrofon-Lease.
- AP21: `AudioRecord`-Referenz vor `setPreferredDevice()` gesichert, damit Fehler im Cleanup erreichbar bleiben.
- AP22: Capture-Neustart nur nach tatsächlicher `Job.isCompleted` statt bereits bei `!isActive`.
- AP23: Rolling-Audio-Neustartschleife bei unsicherer Mikrofonfreigabe unterbunden.
- **AP24:** Rolling und Direct verweigern neue Service-Einstiege (`onStartCommand`) bei bestehendem `MicCaptureArbiter.uncertainReleaseOwner`, bevor ein neuer Capture-Job registriert wird; statische Prüfungen 38/38 PASS, GitHub-/Termux-TEST anschließend vom Benutzer GRÜN bestätigt.

## 5. Vereinbarte Setup-Oberfläche / langfristiger Auftrag

Die Verbesserung der Analyzer-/Scout-Setups bleibt Teil des Gesamtziels: Analyzer-Setups als versionierte JSON-Dateien, unbegrenzt benennbar, Dropdown und Schnellzugriff auf drei zuletzt verwendete Setups, Import/Export über Android-Dateiauswahl; Scout-Einstellungsoberfläche in logisch gruppierte Panels, Beobachtung/Erinnerung zuerst, Zahnrad neben Bewertung, bestehende Scout-Speicherung beibehalten. Bereits funktionierende Analyse-/Audio-Pfade nicht unnötig umbauen. **Trotzdem ausdrücklich NICHT zu einem alten Entwicklungsstand zurückspringen:** Der Nutzer hat die Fortführung des eingeschlagenen Phase-3O-Wegs angeordnet.

## 6. Aktuell nächste konkrete Schritte

1. **Jetzt:** Mikrofonberechtigung in der INSTALLIERTEN TEST-APK/Android-Einstellungen diagnostizieren. Prüfen: Paket-ID des installierten Builds, `RECORD_AUDIO` im gemergten Manifest der APK, Laufzeit-Permission-Request, Android-/MIUI-Berechtigungsmanager und Mikrofon-Privatsphäre-Schalter. Nach Ergebnissen gezielte Fehlerkorrektur, Regressionstest und neue vollständige ZIP, sofern nötig.
2. TEST-APK auf Hardware testen: App-Start, Mikrofon, Analyzer, Spektrum, Wasserfall, MON, Aufnahme/STOP/START, vorhandene Setup-Verwaltung. Tatsächlich getestete und nicht getestete Funktionen getrennt dokumentieren.
3. Danach Phase 3O weiterführen: asynchrone Capture-Cleanups, nachgewiesene Hardwarefreigabe, Controller-Adapter, Rollback und Gerätevalidierung. Keine ungetestete produktive Freigabe.
4. Bei jeder neuen Version Benutzer „GitHub/Termux GRÜN/ROT“ versus „Hardware GRÜN/ROT“ getrennt melden lassen. Build-Erfolg nicht als Gerätevalidierung bezeichnen.

## 7. Workflow und Lieferumfang

Bei Folgepaketen: vollständige Projekt-ZIP mit flachem Wurzelverzeichnis, Pflichtdokumente und vollständige Quelltexte; `tools/verify_lfn056_phase3o*.py` und geeignete Gradle-/JUnit-Tests; ZIP `testzip()`; geänderte Quelltexte/MD ohne störende Whitespaces (`git diff --check` im Termux-Workflow). Keine fiktiven Build-/Hardwareerfolge behaupten. Der Benutzer nutzt üblicherweise `bash ~/LFN_GITHUB_TERMUX.sh`, Menüoption 1 für den GitHub-TEST; anschließend das **TEST-debug-Artefakt** aus GitHub Actions für den Gerätetest. Kein Austausch der produktiven APK.

## 8. Direkt kopierbarer Übergabetext

> Wir setzen das Android-Projekt LFN-Scout (`yewie56/LFN-Scout`, `main`) unmittelbar auf dem beigefügten vollständigen Quellstand **Phase 3O Arbeitspaket 24, GitHub-/Termux-TEST GRÜN** fort. Keine ältere Version, kein Rückbau, keine Neuausrichtung: Die Fortführung des bestehenden Phase-3O-Weges ist ausdrücklich beschlossen. Lies die Pflichtdokumente einschließlich `docs/project/CURRENT_STATE.md`, `DIRECTIVES.md`, `BUILD_WORKFLOWS.md`, `CHANGELOG.md`, `PROJECT_STATUS.md` und `DEFERRED_TASKS.md`. Gradle bleibt `0.5.55/213`, `Plan.executable=false`, Termux v4 und GitHub-Workflows/Cloud unverändert. Jede Iteration benötigt aktualisiertes `CURRENT_STATE.md`, Tests und eine vollständige Projekt-ZIP ohne Oberverzeichnis oder historisches Supabase-Backup. **Jetzt zuerst den realen Android-Hardwarebefund bearbeiten: In der separat installierten TEST-APK fehlt die Mikrofonberechtigung.** `RECORD_AUDIO` steht im Quell-Manifest und TEST hat `applicationIdSuffix .githubdebug`; bitte gemergtes APK-Manifest, Runtime-Request, Android-Berechtigungsdialog und MIUI-Einstellungen überprüfen. Produktive APK und vorhandene Setups nicht anfassen. Github-TEST grün ist noch kein Hardwaretest. Danach realen Audio-/Analyzer-/MON-Funktionstest durchführen und Phase 3O mit Controller-Freigabe/Rollback fortsetzen. Gib vollständige ausführbare Ergebnisse, keine Code-Diffs, keine erfundenen Testnachweise.
