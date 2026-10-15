# v0.5.62 / versionCode 220 – Kotlin-Kompilierungsfehler AP31 korrigiert (15.10.2026)

- Grundlage: vollständig hochgeladenes v0.5.61-Paket, Phase 3O AP31. GitHub-TEST-Fehlerlog Run 38746929506 ausgewertet.
- `AnalyzerSetupSwitchPreflight.kt`: `when`-Bedingung für `requiresStop` und `stoppedDraftFields` in gültige Kotlin-Mehrfachbedingung umgestellt.
- `LiveAnalyzerController.kt`: expliziter `Unit`-Rückgabetyp für rekursive STOP-JSON-Rollback-Funktion; bedingte `in`-Prüfungen auf FFT-Größe (zwei Stellen) und Hop (eine Stelle) geklammert.
- Versionswechsel `0.5.61/219` → `0.5.62/220`; TEST- und REGULAR-Paketkennungen, Signier- und Buildverfahren bleiben unverändert.
- Prüfung: Vollständige ZIP-Struktur und alle gezielten Quelltextkorrekturen lokal verifiziert. Ein Android-Gradle-Build und eine APK wurden **nicht** durchgeführt/erzeugt. GitHub-TEST und Hardwaretest offen.
- Kein Setup-Facelift-Funktionsausbau; produktive vollständige Setup-Aktivierung weiterhin nicht freigegeben (`Plan.executable=false`).

---

# Phase 3O – AP31 STOP-JSON-Controller-Adapter (15.10.2026)

- Neu: `LiveAnalyzerController.applyStoppedJsonDraft` mit vollständiger Schlüsselgleichheit gegenüber dem aktuellen Controller-Snapshot, vollständiger vorgelagerter Validierung, speicherfreiem In-Memory-Apply und Restore beim Fehlschlag.
- Neu: `AnalyzerStoppedJsonControllerPort` implementiert den zuvor unverbundenen Transaktions-Port und verifiziert nach Apply/Restore die genaue vollständige JSON-Gleichheit.
- `AnalyzerJsonSetupBrowser`: Schaltfläche „JSON als temporären STOP-Entwurf anwenden“ mit ehrlicher Statusmeldung. Neuer Quellvertrags-Regressions-Test.
- **Kritische Grenze:** Kein vollständiger produktiver Setup-Wechsel: Die normale START-Funktion liest weiterhin alte SharedPreferences, MON wird nicht in derselben Transaktion übernommen, einige importierte Setups enthalten unvollständige/nicht abbildbare Daten. Die neue Schaltfläche erlaubt ausschließlich einen temporären, gestoppten Entwurf. Keine permanente Aktivierung, kein Slot-Ersatz, `Plan.executable=false` als öffentliche Plan-Freigabe unverändert.
- **Prüfstand:** Quelltext und ZIP lokal geprüft, Android SDK/Gradle-Unit-Tests hier nicht ausführbar; GitHub-TEST und Xiaomi-Hardwaretest offen. Dies ist ein Quellkandidat, kein fertiges Setup-Facelift.
- Gradle-Quellversion weiterhin 0.5.61/219, keine Audio-/Cloud-Änderungen oder Legacy-Slot-Schreibzugriffe.

---

# Phase 3O – AP31 fortgesetzt: STOP-Transaktionskern (15.10.2026)

- Konkret geändert: `AnalyzerSetupSwitchTransaction` kann für **explizit opt-in-fähige** Ports eine gestoppte Konfiguration anwenden, den Ergebniszustand prüfen und bei Fehlschlag auf den zuvor aufgenommenen Checkpoint zurücksetzen.
- Vor jedem Anwenden werden JSON/Controller/Recording-Status erneut kontrolliert. Ein fehlgeschlagener oder unbestätigter Restore wird als `rollbackSucceeded=false` gemeldet; keine vorgetäuschte Rücknahme.
- Neue `AnalyzerSetupStoppedRollbackTest` mit Fällen Erfolg, veralteter Zustand, Anwende-Fehler, Prüf-Fehler, fehlgeschlagener Restore und fehlgeschlagener Checkpoint.
- **Kein Produktiv-Adapter opt-in:** `supportsAtomicStoppedApply` standardmäßig `false`; der volle Analyzer-JSON-Wechsel und das Setup-Facelift sind weiterhin nicht freigegeben. `Plan.executable=false` bleibt. Audio/Cloud/Legacy-Slots nicht verändert.
- Android-/Gradle-Testumgebung hier nicht nachgewiesen; GitHub-Test und Xiaomi-Test offen. Entwicklungsstand bleibt 0.5.61/219; **nicht als neue freigegebene TEST-Version ausgeben**.

---

# Phase 3O – AP31 (ENTWICKLUNGSSTAND, KEIN FREIGEGEBENES RELEASE) – 15.10.2026

- Grundlage: AP30 v0.5.60/218. Geänderter Gradle-Quellkandidat 0.5.61/219.
- AnalyzerJsonSetupBrowser: echtes Erstellen einer versionierten JSON-Datei aus der aktuellen Controller-Momentaufnahme plus MON, SAF-Dokumentauswahl für Import/Export, Umbenennen, Löschen mit internem Backup. Import vergibt bewusst neue UUID und überschreibt keine Setups; Dateigröße begrenzt auf 2 MiB. Alte 1–4-Slots bleiben unverändert.
- Nicht erledigt: Transaktionales Anwenden der gesamten Analyzer-/Audio-/FFT-Parameter auf den laufenden Controller, Ersetzung der Slot-Steuerung, vollständige Parametererfassung inklusive geräteabhängiger Quellen und abgesicherte Rücknahme. `Plan.executable=false` bleibt absichtlich; kein Scheinerfolg.
- Validierung: Quelltext-/Paket-Struktur geprüft; Android-Gradle-Build und Hardwaretest NICHT ausgeführt. Nicht als lauffähige TEST-APK bezeichnen. Vor Hardwareeinsatz GitHub-Unit-Tests und vollständigen APK-Build erforderlich.

---

# Phase 3O – AP30: Kotlin-Testkompilierung korrigiert (15.10.2026)

- Ausgangslage: AP29 GitHub-TEST ROT (`38665752366`): `SeismicPlotInteractionContractTest.kt:62:49 Unresolved reference 'folder'` bei `:app:compileDebugUnitTestKotlin`. Die Test-Assertion enthielt eine nicht escapte Kotlin-Stringinterpolation `$folder`.
- Gezielter Fix: Im reinen Test-Quellcode das `$` als `\$folder` escaped, damit die Assertion weiterhin exakt den Quelltext `"Download/$folder/"` von `PublicDataDirectory` prüft. Der Produktivcode und die Pfadtrennung bleiben unverändert.
- Versionsstand `0.5.60 / 218` für nächstes TEST-Update; `Plan.executable=false` unverändert. REGULAR wird nicht gebaut oder installiert.
- Verifikation: Quelltext-Assertion und ZIP-Struktur statisch geprüft. Gradle-Unit-Tests, GitHub-Build und Android-Hardwaretest OFFEN.

---

# Phase 3O – AP29: Seismogramm-Exportvertrag nach TEST-Speichertrennung (15.10.2026)

- Ausgangslage: AP28 GitHub-TEST ROT: `SeismicPlotInteractionContractTest.exporter_writes_full_near_plot_not_only_zoom_viewport`, 260/261 Tests bestanden. Der Vertrag erwartete den festen Legacy-Pfad `Download/LFN-Scout/`, während der reale Export seit AP28 über `PublicDataDirectory.relativePath` den passenden TEST-/REGULAR-Pfad wählt.
- Korrektur: Nur den veralteten Unit-Test auf die zentrale Pfadbindung umgestellt; neue Regression gegen versehentliches Zusammenlegen der TEST-/REGULAR-Ausgabe. Seismogramm-Renderer, Audio, Setup-Speicher, GitHub-Workflow und Supabase bleiben unverändert.
- Gradle `0.5.59` / `217` für installierbares TEST-Update; `Plan.executable=false` bleibt unverändert.
- Prüfstand: lokale Struktur- und ZIP-Prüfung, GitHub-Unit-Tests, Gradle-Build und Android-Hardwaretest **OFFEN**. Keine TEST-APK vor erfolgreichem GitHub-Build.

---

# Phase 3O – AP28: TEST-Speichertrennung (Quellkandidat, 15.10.2026)

- Nutzervorgabe: In der Android-Dateimanageransicht **Downloads/lfn-scout-test/**. Technisch wird der von Android bereitgestellte öffentliche Downloads-Ordner (`Environment.DIRECTORY_DOWNLOADS`, MediaStore `Download/`) verwendet; damit kein zusätzliches paralleles `Downloads`-Verzeichnis entsteht.
- Nur `applicationId` mit `.githubdebug` verwendet `lfn-scout-test`; REGULAR bleibt `LFN-Scout`. Es wird weder automatisch migriert noch eine REGULAR-Datei kopiert. Legacy-Lookups der TEST-App werden auf das neue Verzeichnis eingeschränkt.
- Modifizierte Bereiche: Audioarchiv/WAV, Statistiken/CSV, Wasserfallbilder, Diagnose, Export/Importordner, Setup-Dateien. App-interne Android-Daten werden weiterhin durch getrennte Paketkennungen isoliert.
- Gradle: `0.5.58` / `216`; Workflow, Signierung, Supabase, Mic/FFT und `Plan.executable=false` unverändert.
- Prüfstatus: ZIP-/statische Pfadprüfung lokal. Android-Gradle und Gerätemessung noch offen.

---

# Phase 3O – AP25 Setup-Facelift (Quellkandidat, 15.10.2026)

**Basis:** Vollständiges AP24-Übergabe-ZIP. AP24 GitHub-/Termux-TEST vom Benutzer GRÜN bestätigt. Nach manueller Freigabe des Mikrofons meldet der Benutzer einen fehlerfreien Android-Gerätetest für die bestehenden Funktionen. Das ist kein Test von AP25.

**Sichtbare Änderung AP25:** Analyzer-Einstellseite zeigt eine eindeutige Setup-Verwaltung mit ausgewähltem Platz, Speicherstatus, Namen, Warnung vor Überschreiben und direkter Schaltfläche zur Setup-Kategorie. Die Setup-Kategorie steht an erster Stelle. JSON wird ausdrücklich als noch nicht aktivierbare Übersicht bezeichnet. Existierende Laden-/Speichern-Mechanik, drei Slots, Direct-Slot 4 und alle Audiofunktionen bleiben unverändert.

**Nicht erledigt:** Beliebige JSON-Setups, MRU, SAF, Scout-Panels, vollständiger Audio-Controller-/Rollback-Executor. `AnalyzerSetupSwitchPlan.Plan.executable=false` bleibt. Gradle `0.5.55 / 213` unverändert, TEST-Paketkennung getrennt. Keine Änderungen an Workflow, Termux, Signierung, Supabase oder produktiver Installation. Historisches Supabase-Storage-Backup bleibt in GitHub und fehlt im Projekt-ZIP.

**Tests AP25:** Quell-/Paketprüfung lokal; Android-Gradle-Build, GitHub-/Termux-Test und Android-Hardwaretest OFFEN. Als Source Candidate behandeln, nicht als erfolgreiche APK ausgeben.

---

# ÜBERGABE 15.10.2026 – Arbeitspaket 24 / TEST-APK-Hardwarebefund

**GitHub-/Termux-TEST AP24: GRÜN (Benutzer bestätigt). Hardwaretest: begonnen, noch NICHT grün.** Die separat installierte TEST-APK zeigt laut Benutzer „Mikrofonberechtigung fehlt“; Ursache offen. `RECORD_AUDIO` ist im Quell-Manifest enthalten, TEST-Build nutzt eigene `applicationId` mit `.githubdebug`. Nächste Aufgabe: Laufzeitberechtigungsanforderung, tatsächliches gemergtes APK-Manifest und Android-/MIUI-Berechtigungsstatus prüfen. Produktive Installation und alte Setups nicht ändern. Phase 3O auf AP24 fortsetzen, kein Rollback auf alte Version. `Plan.executable=false`. Vollständige Übergabe: `docs/project/UEBERGABE_PHASE3O_AP24_2026-10-15.md`.

---

# Phase 3O – Arbeitspaket 24 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 23 **GRÜN**.
**Änderung:** Bei einem bereits quarantänisierten Mikrofon (`MicCaptureArbiter.uncertainReleaseOwner != null`) verweigern Rolling- und Direct-Service nun auch neue Android-`onStartCommand`-Starts *vor* Foreground-Aktivierung und Capture-Job-Registrierung. Rolling führt seinen vorhandenen STOP-Cleanup aus; Direct beendet den Service. Normale Starts bleiben unverändert. Neuer statischer Regressionstest prüft die Reihenfolge.
**Grenzen:** Keine positive Bestätigung physischer Hardwarefreigabe; keine automatische Entsperrung. Controller-Adapter, Rollback und Gerätetest weiter offen, `Plan.executable=false`.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert; historisches Storage-Backup nur in GitHub.

---

# Phase 3O – Arbeitspaket 23 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 22 **GRÜN**.
**Änderung:** `RollingAudioService` beendet die automatische Capture-Neustartschleife nach `runRecorder()`, sobald `MicCaptureArbiter.uncertainReleaseOwner` eine fehlgeschlagene `AudioRecord.release()`-Freigabe meldet. Neuer Quell-Regressionsprüfer verifiziert die Reihenfolge vor `settingsFlow` und `delay(5_000)`. Der Quarantäne-Token bleibt bis Prozessende gesperrt.
**Grenzen:** Keine nachgewiesene Android-Hardwarefreigabe, kein produktiver Setupwechsel, kein Hardware-Rollback und kein Gerätetest. `Plan.executable=false`.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert. Historisches Backup nur in GitHub.

---

# Phase 3O – Arbeitspaket 22 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 21 **GRÜN**.
**Änderung:** `RollingAudioService` und `AnalyzerDirectAudioService` prüfen vor einem erneuten Capture-Start die **Job-Fertigstellung** (`isCompleted`) statt nur `isActive`. Ein durch STOP bereits abgebrochener, aber im `finally` noch freigebender AudioRecord-Job ist nicht mehr als frei anzusehen. Die STOP-Pfade behalten die Job-Referenz bis zur Coroutine-Fertigstellung; eine neue Registrierung darf erst nach tatsächlichem Job-Abschluss erfolgen. Neuer statischer Regressionstest.
**Grenze:** Android-Service-Neustart und Fehlerfälle außerhalb dieses In-Process-Jobs sind keine bewiesene Hardware-Freigabe. Kein produktiver Setupwechsel, kein Hardware-Rollback und kein Gerätetest; `Plan.executable=false`.
**Prüfstatus:** Statische Prüfungen lokal; GitHub-JUnit/Android-Build für AP22 noch offen.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert. Historisches Backup nur in GitHub.

---

# Phase 3O – Arbeitspaket 21 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 20 **GRÜN**.
**Änderung:** In `RollingAudioService` und `AnalyzerDirectAudioService` wird ein bereits erfolgreich initialisiertes `AudioRecord`-Objekt vor dem optionalen `setPreferredDevice()` in der lokal durch das `finally` bereinigten Recorder-Referenz registriert. Wirft die Gerätezuweisung eine Ausnahme, läuft die bestehende `finally`-Freigabe über das korrekte Objekt. Bei einer `release()`-Ausnahme wird wie seit AP19 die Mikrofon-Lease quarantänisiert. Neuer statischer Regressionstest erzwingt die Reihenfolge.
**Grenzen:** Keine atomare Android-Hardwarefreigabebestätigung, kein produktiver Setupwechsel/Hardware-Rollback, kein Gerätetest. `Plan.executable=false` bleibt unverändert.
**Tests:** Lokale statische Phase-3O-Prüfer; JUnit/Android-Build für AP21 erst durch GitHub-TEST zu bestätigen.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert; historisches Backup nur in GitHub.

---

# Phase 3O – Arbeitspaket 20 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 19 **GRÜN**.
**Änderung:** Bei der AudioRecord-Quellenauswahl wird ein `release()`-Fehler eines verworfenen Kandidaten in `AudioRecorder`, `RollingAudioService` und `AnalyzerDirectAudioService` nun konservativ behandelt: bereits gehaltenes Mikrofon-Token als unsicher markieren und den Auswahlversuch abbrechen. Dadurch kein Start einer weiteren AudioRecord-Instanz nach ungewisser Freigabe im selben Prozess. Erfolgreiche Kandidatenfreigaben bleiben unverändert. Regressionstest auf Quellcodeebene hinzugefügt.
**Grenzen:** Keine verifizierte physische Freigabe durch Android, keine automatische Entsperrung, kein Hardware-Rollback und kein produktiver Setupwechsel. `Plan.executable=false`.
**Tests:** Statische Prüfungen lokal; JUnit/Android-GitHub-Build für Arbeitspaket 20 ausstehend, kein Gerätetest.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert. Historisches Storage-Backup ausschließlich auf GitHub.

---

# Phase 3O – Arbeitspaket 19 (Quellkandidat)

**Stand:** 15.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 18 **GRÜN**.
**Änderung:** Wenn `AudioRecord.release()` in Direct, Rolling oder Scout-Recorder eine Ausnahme auslöst, markiert der bereits gehaltene `MicCaptureArbiter.Token` die Hardwarefreigabe als unsicher. `close()` gibt diesen Token dann nicht frei; weitere Capture-Starts bleiben innerhalb desselben Prozesses blockiert. Erfolgreiche normale Freigaben folgen unverändert dem bisherigen Ablauf. Der Status `uncertainReleaseOwner` ist diagnostisch abrufbar. Zwei zusätzliche JUnit-Fälle prüfen die Quarantäne und den Normalfall.
**Grenzen:** Android bietet durch die Rückkehr von `release()` keinen separaten Hardware-Freigabenachweis. Fehler bei `stop()` und andere nicht erkannte Hardware-Fehler sind damit nicht vollständig erfasst; nach einem Release-Fehler kann ein App-Prozessneustart erforderlich sein. Keine automatische Entsperrung ohne Hardware-Nachweis; keine produktive Setup-Transaktion. `Plan.executable=false`.
**Prüfstatus:** Statische Tests lokal; neue JUnit-Fälle und Android-Build erst im GitHub-TEST zu bestätigen, kein Gerätetest.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert; historisches Storage-Backup nur auf GitHub.

---

# Phase 3O – Arbeitspaket 18 (Quellkandidat)

**Stand:** 14.10.2026; Zielversion `0.5.56`; Gradle `0.5.55 / 213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 17 **GRÜN**.
**Änderung:** Rein lesende STOP-Grenzdiagnose berücksichtigt jetzt zusätzlich die Abonnentenzahl des gemeinsamen Roh-PCM-Hubs (`AnalyzerSessionGateway.rawPcm.activeSubscribers`). Positive Abonnentenzahlen, negative unplausible Werte und Ausnahmen liefern getrennte Sperrgründe. Die Produktionsabfrage übergibt die bestehende Hub-Beobachtung; drei neue JUnit-Regressionsfälle sichern alle drei Fehlerklassen ab. Kein Stopp, kein Hardware-Release, keine Mutation.
**Sicherheitsgrenze:** Der Abonnentenzähler ist keine atomare Hardware-Release-Bestätigung. `executionAuthorized=false`, `Plan.executable=false`. Controlleradapter, echte AudioRecord-Freigabebestätigung, Rollback und Android-Gerätetest bleiben offen.
**Tests:** Statische Quellprüfungen lokal; JUnit und vollständiger Android-Build für Paket 18 erst im GitHub-TEST zu bestätigen.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Cloud unverändert; Storage-Backup nur auf GitHub.

---

# Phase 3O – Arbeitspaket 17 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`, Gradle `0.5.55 / 213`.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 16 **GRÜN**.
**Änderung:** `AnalyzerSetupTransitionCoordinator.diagnoseStoppedBoundary()` vereinigt die bisherigen Owner-/Lifecycle-Blocker mit der lokalen STOP-Diagnose des `LiveAnalyzerController`. Separate Gründe für laufenden Analyzer, vorhandene Session und PCM-Subscription. Probe-Ausfälle werden fail-closed gekennzeichnet. `diagnoseCurrentStoppedBoundary()` ist ein rein lesender Produktionsdiagnose-Einstieg ohne Aufrufer im produktiven Umschaltpfad. Drei neue JUnit-Regressionen.
**Grenze:** Die Beobachtungen sind zeitlich nicht atomar, weder Hardware-Release noch Android-Service-Stopp sind dadurch bestätigt; `executionAuthorized=false` und `Plan.executable=false` bleiben bestehen. Kein realer Controller-Adapter, kein Gerätetest.
**Tests:** Statische Quellprüfungen lokal; JUnit-Ausführung/Android-GitHub-Build für Paket 17 durch Nutzer noch ausstehend.
**Schutz:** Termux v4, Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert. Historisches Storage-Backup nur auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 16 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 15 **GRÜN**.
**Arbeitspaket 16:** Rein diagnostische lokale STOP-Prüfung `LiveAnalyzerController.verifyLocalStopForSetupTransition()` eingeführt. Das Ergebnis erfasst unter dem Controller-Lock `running`, die bestehende Session und die lokale PCM-Puffer-Subscription. `LocalAnalyzerAudioBuffer.hasActiveSubscription()` liest ausschließlich den bestehenden Pufferzustand unter seinem Lock. Keine Änderung an produktivem STOP/START-Verhalten.
**Sicherheitsgrenze:** `locallyStopped` ist **kein** Nachweis für freigegebenes AudioRecord, Recorder-, MON-, Rolling-/Direct-Service- oder asynchrone Capture-Lifecycle-Ressourcen. Es erfolgt keine Freischaltung und keine Hardware-Aktion. `Plan.executable=false` bleibt verbindlich.
**Prüfstatus:** Statische Skripte und neuer Guard lokal zu prüfen; JUnit/Android-Build und Gerätetest für Paket 16 ausstehend.
**Schutz:** Termux v4, GitHub-Workflows, Gradle/Signierung, `.gitignore`, Supabase/Cloud unverändert. Historisches Storage-Backup bleibt nur auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 15 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 14 **GRÜN**.
**Arbeitspaket 15:** Opt-in `LiveAnalyzerController.startFromStoppedConfiguration(context, expected)` ergänzt. Die bestehende `start(context, dspOverride)`-Signatur und ihr Legacy-Preference-Reload bleiben erhalten. Der neue Pfad prüft unter dem bestehenden Controller-Lock `!running` und die Übereinstimmung des bereits bestehenden STOP-DSP-/Audio-Timing-Checkpoints. Er verwendet ausschließlich die schon eingestellte In-Memory-Konfiguration für den Start und lädt keine Live-Analyzer-Preferences nach. Kein neuer produktiver Aufrufer.
**Sicherheitsgrenze:** Checkpoint umfasst nicht alle UI-/Statistikwerte, keinen AudioRecord-Besitz, keine aktiven Service-Lebenszyklen und keine Recorderfreigabe. Der neue Einstieg erwirbt selbst keine Microphone-Lease; Aufrufer müssen diese gesondert absichern. Vollständiger STOP-/START-/Rollback-Vertrag und Hardwaretests fehlen. `Plan.executable=false` bleibt unverändert.
**Prüfstatus:** Die vorhandenen 28 statischen Phase-3O-Prüfskripte und ein neuer statischer Guard bestanden; GitHub-JUnit/Android-Build sowie Gerätetest dieses Quellkandidaten offen. Workflows, Gradle, Signatur, `.gitignore`, Cloud/Supabase und Termux v4 unverändert. Historisches Storage-Backup bleibt nur auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 14 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Arbeitspaket 13 **GRÜN**.
**Arbeitspaket 14:** Controller-Integrationsaudit und zwei JUnit-Fault-Injection-Regressionen für Lease-Verlust nach STOP sowie wiederholten START-Fehler im Rollback. Ein neuer statischer Guard hält die dokumentierten Controller-Integrationshindernisse sichtbar. Produktive Audio-/Recorder-Routinen und Setup-Executor sind unverändert.

**Freigabeblocker, im echten Quellstand bestätigt:** `LiveAnalyzerController.start()` liest Konfiguration aus `SharedPreferences` und stellt noch keinen vollständig preference-freien transaktionalen Start bereit. `stop()` besitzt noch keinen nachgewiesenen Hardware-/Service-Release-Vertrag. Die bestehenden Controller-Checkpoints enthalten keine Audio-Eigentümer oder aktive Recorder-Ressourcen. Damit ist ein produktiver STOP–Reconfigure–START-Adapter weiter **NICHT** sicher anschließbar. `Plan.executable=false` bleibt erhalten.

**Prüfstatus:** Statische Tests lokal; neue JUnit-Fälle noch nicht durch GitHub ausgeführt. GitHub-TEST für Arbeitspaket 14 und Android-Gerätetest offen. Kein Hardware-Rollback nachgewiesen. Historisches Supabase-Backup nur auf GitHub; Workflows, Signierung, Gradle, `.gitignore`, Cloud, Termux v4 unverändert.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 13 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 12 **GRÜN**.
**Arbeitspaket 13:** Drei echte Multi-Thread-JUnit-Regressionen prüfen, dass eine laufende Setup-Lease Rolling-/Direct-Starts sperrt, dass zwei Setup-Operationen sich nicht überlappen und dass nach einem fehlgeschlagenen Callback der Mikrofon-Token freigegeben ist. Die produktive Koordinatorlogik bleibt unverändert; dies ist ein Test-/Stabilisierungspaket.
**Grenze:** Die JUnit-Tests beweisen nur In-Process-Koordination unter synthetischen Race-Bedingungen. Android-Service-Lifecycle, AudioRecord-Release, produktiver STOP–Reconfigure–START-Adapter und Hardware-Rollback bleiben offen. `Plan.executable=false` bleibt verbindlich.
**Teststatus:** GitHub-JUnit/Android-Build für Paket 13 noch ausstehend; kein Gerätetest. Termux v4, Workflows, Gradle, Signierung, `.gitignore`, Cloud/Supabase unverändert. Historisches Storage-Backup verbleibt auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 12 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 11 **GRÜN**.
**Paket 12:** Der bestehende, bisher isolierte synchrone Hilfseinstieg `AnalyzerSetupMicrophoneReservation.withReservation` delegiert jetzt an `AnalyzerSetupTransitionCoordinator.withSetupReservation`. Dadurch ist ein isolierter Mikrofon-Token-Erwerb unter Umgehung der gemeinsamen Service-/Setup-Marker nicht mehr möglich. Dieser Helper besitzt derzeit keine produktiven Aufrufer. Die produktiven Recorder-Routinen sind unverändert.
**Regressionen:** Drei JUnit-Fälle prüfen gemeinsame Sperre/Token, Verweigerung bei offenem Rolling-Lebenszyklus und Freigabe nach Ausnahme. Neuer statischer Quellprüfer.
**Grenze:** Diese Änderung schließt einen Legacy-Nebenpfad, liefert aber noch KEINE atomare Ende-zu-Ende-Koordination asynchroner Audio-Starts oder den Hardware-STOP–Reconfigure–START-Rollback. `Plan.executable=false` bleibt bestehen. GitHub-JUnit/Android-Build und Gerätetest für Paket 12 stehen aus.
**Schutz:** Termux v4, Workflows, Gradle, Signierung, `.gitignore`, Supabase/Cloud unverändert; historisches Storage-Backup nur auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 11 (Quellkandidat)

**Stand:** 14.10.2026; Zielversion `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 10 **GRÜN**.
**Arbeitspaket 11:** Der vorhandene `withOwnerCheckedReservation`-Pfad reserviert nun das Token des bestehenden `MicCaptureArbiter` in derselben synchronisierten Zulassungsentscheidung, in der auch die Koordinator-Marker und der Audio-Owner-Status geprüft werden. Der produktive `withSetupReservation`-Aufruf verwendet denselben zentralen Mechanismus statt einer nachgeschalteten Token-Erwerbung. Bei bereits vergebenem Token bleibt der Nutzerblock unausgeführt. Token und Koordinator-Marker werden auch nach Ausnahme in definierter Reihenfolge freigegeben; weitere Startentscheidungen sehen den offenen Marker bis zur Tokenfreigabe.
**Tests:** Drei neue JUnit-Regressionen für Token innerhalb des injizierbaren Reservierungspfads, konkurrierenden Besitzer und Ausnahme-Cleanup. 26 lokale statische Phase-3O-Prüfer; GitHub-JUnit/Android-Build für Paket 11 noch offen. Ältere statische Prüfskripte auf die gleichwertige zentralisierte Reservierung aktualisiert.
**Wichtige Grenze:** Diese Verbesserung betrifft ausschließlich die synchrone In-Process-Zulassung. Kein Ende-zu-Ende-Atomaritätsbeweis für asynchrone Android-Services, keine produktive Controller-Anbindung, keine Hardware-Wiederherstellung und keine Gerätetests. `Plan.executable=false` bleibt erhalten.
**Schutz:** Termux v4, Workflows, Gradle, Signierung, `.gitignore`, Cloud und Supabase unverändert. `supabase/storage_backup_before_v0526/` bleibt nur auf GitHub und wird nicht mitgeliefert.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 10 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 9 **GRÜN**.
**Arbeitspaket 10:** Bestehende Legacy-Eintrittspunkte `withMonStart` und `withServiceStart` verwenden nun die gemeinsame fail-closed-Markerprüfung und verweigern Starts bei belegtem `MicCaptureArbiter` oder aktiver MON-Aufnahme. Drei neue JUnit-Fälle für belegtes Mikrofon und offenes Service-Lifecycle. Keine neuen Audio-Koordinatoren oder Änderungen an produktiven Recorder-Routinen.
**Grenze:** `withServiceStart` hält KEINE asynchrone Lifecycle-Reservierung; neue Android-Service-Starts müssen `withTrackedServiceStart` verwenden. Noch keine Ende-zu-Ende-Atomarität, kein Hardware-Rollback, keine Controller-Anbindung oder Gerätetest. `Plan.executable=false` bleibt bestehen.
**Schutz:** Versionsnummern, Termux v4, Cloud, Workflows, Signierung, `.gitignore` unverändert. Historisches Supabase-Storage-Backup bleibt ausschließlich auf GitHub.
**Teststatus:** Lokale statische Quellprüfungen separat; GitHub-JUnit/Android-Build für Arbeitspaket 10 offen.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 9 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 8 **GRÜN**.
**Arbeitspaket 9:** `withTrackedServiceStart` weist Rolling-/Direct-Startanforderungen nun bereits vor Registrierung des asynchronen Lifecycle-Markers ab, wenn der bestehende `MicCaptureArbiter` einen aktiven Besitzer meldet. Die bestehende synchronisierte Koordinatorentscheidung und deren Marker bleiben maßgeblich. Zusätzliche JUnit-Regressionen für belegten Recorder, Setup-Token und Start nach Token-Freigabe sowie statischer Prüfer.
**Sicherheitsgrenze:** MIC-Owner-Lesen ist nur eine ablehnende Momentaufnahme; es reserviert keine künftige Hardware-Nutzung des asynchronen Services. Produktive Transaktion und Controller-Anbindung sind nicht freigeschaltet (`Plan.executable=false`). Hardware-Rollback, vollständiger allseitiger Atomaritätsnachweis und Android-Gerätetests bleiben offen.
**Schutz:** Version `0.5.55` / `213`, Termux v4, Workflows, Signierung, `.gitignore`, Cloud und Supabase unverändert. `supabase/storage_backup_before_v0526/` bleibt ausschließlich auf GitHub und fehlt absichtlich in der ZIP.
**Teststatus:** Statische Phase-3O-Prüfer lokal auszuführen; GitHub-JUnit/Android-Build und Geräteprüfung für Arbeitspaket 9 noch offen.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 8 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 7 **GRÜN**.
**Arbeitspaket 8:** `withSetupReservation` hält während der vollständigen synchronen Operation zusätzlich einen echten Token des bestehenden `MicCaptureArbiter` (`analyzer_setup_preflight`). Kann das Token nicht erworben werden, wird der Nutzerblock nicht ausgeführt; ein bereits aktiver Besitzer bleibt unangetastet. Die Freigabe erfolgt mittels `use` auch bei Ausnahmen; die Koordinatorreservierung bleibt bis zum Ende des Vorgangs gesetzt. Die injizierbare, rein testorientierte Owner-Probe bleibt unverändert.
**Regressionen:** Drei neue JUnit-Tests für Lease-Haltung, Belegung und Exception-Cleanup sowie neuer statischer Prüfer. 23 Phase-3O-Quellprüfungen lokal zu verifizieren; GitHub-JUnit/Android-Build für Paket 8 offen.
**Grenzen:** Kein produktiver Controller-Adapter, keine Session-Reinitialisierung, keine Hardware-Rollback- oder Android-Gerätevalidierung. Nicht alle vorhandenen Android-Audio-Start-/Stop-Routen sind atomar koordiniert. Der Token darf nicht als Freigabe des Setup-Executors gewertet werden; `Plan.executable=false` bleibt bestehen. Termux v4, Workflows, Gradle, Cloud, Signierung und Supabase unverändert. `supabase/storage_backup_before_v0526/` bleibt nur auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 7 (Quellkandidat)

**Stand:** 14.10.2026; Ziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Arbeitspaket 6 **GRÜN**.
**Arbeitspaket 7:** Rolling-/Direct-Startanforderungen werden nun beim Vorliegen irgendeines aktiven Rolling-/Direct-Lifecycle- oder Capture-Cleanup-Markers abgelehnt (nicht nur bei gleichem Service). Ein aktiver MON-Recorder verhindert ebenfalls den getrackten Start; MON-Start wird während aktiver/pending Service-Lifecycles verweigert. Die bestehende Koordinator-Synchronisierung wird verwendet, kein paralleler Arbiter.
**Regressionen:** Drei zusätzliche JUnit-Fälle für beide Cross-Service-Konflikte und MON-Start; vorhandene Phase-3O-Quellprüfungen lokal erfolgreich. GitHub-JUnit/Android-Build und Gerätetest für diesen Quellkandidaten offen.
**Sicherheitsgrenze:** Das ist eine konservative In-Process-Startbarriere, noch KEIN durchgängiger atomarer Hardware-Lease und keine produktive Controller-Anbindung. `Plan.executable=false` bleibt verbindlich. Reale Rollback-/Gerätevalidierung offen. Termux v4, GitHub-Workflows, Gradle, Cloud und Supabase unverändert; historisches Storage-Backup bleibt ausschließlich auf GitHub.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 6 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter bestätigter GitHub-TEST:** Arbeitspaket 5 FIX **GRÜN** (Benutzermeldung).
**Aktueller Stand:** Arbeitspaket 6, GitHub-JUnit/Android-Build OFFEN.

**Arbeitspaket 6:** Die vorhandene `AnalyzerSetupTransitionCoordinator.withSetupReservation` prüft nun vor der Reservierung neben sämtlichen Lifecycle-/Capture-Markern auch den aktuellen `AnalyzerSetupAudioOwnerGate`-Status. Fehler beim Lesen der Besitzer werden konservativ abgewiesen. Der bestehende MON-Schutz und die garantierte Reservierungsfreigabe bleiben erhalten. Kein neuer produktiver Setup-Executor oder paralleler Recorder-Koordinator eingeführt. Eine testbare interne Probe-Variante ermöglicht vier JUnit-Regressionstests für Besitzerkonflikte, Fehlerabfragen, Mehrfachreservierung und exception-sichere Freigabe.

**Sicherheitsgrenze:** Die Statusabfrage ist kein übergreifender atomarer Lock für Audio-Hardware und alle Service-Starts. Sie verleiht keine Aktivierungserlaubnis. `Plan.executable=false` bleibt unverändert. Noch offen: Einbindung aller tatsächlichen Recorder-Start/Stop-Pfade in eine gemeinsame Exklusivgrenze, reale Controller-Schnittstelle, Rollback mit Hardwarevalidierung und Gerätetests.

**Prüfstatus:** Lokale statische Prüfungen sind separat dokumentiert; JUnit und vollständiger Android-Build sind erst mit GitHub-Test bestätigt. Workflows, Gradle, Signing, Termux v4, Cloud/Supabase unverändert. `supabase/storage_backup_before_v0526/` verbleibt auf GitHub und fehlt absichtlich in der ZIP.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 5 FIX (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter bestätigter GitHub-TEST:** Arbeitspaket 4 GRÜN.
**Arbeitspaket 5:** GitHub-TEST ROT (225 Unit-Tests, 1 Fehler: `lostLeaseDoesNotClaimSafeStopped`, Build #72).
**Aktueller Quellkandidat:** Arbeitspaket 5 FIX, GitHub-TEST ausstehend.

**Fehlerursache:** Bei einer erfolgreich betretenen, aber bereits ungültigen `ExclusiveBoundary` löste die erste Checkpoint-Prüfung eine Ausnahme aus. Der allgemeine Protokollpfad wertete diese als Fehler *vor STOP* und meldete fälschlich `rollbackSucceeded=true`. Eine verlorene Besitzgarantie erlaubt jedoch keine positive Rollback-Aussage.

**Fix:** Vor jedem Zugriff auf `ControlledPort` wird innerhalb der Besitzgrenze die Exklusivität verifiziert. Bei ungültigem Besitz: `Result(succeeded=false, rollbackSucceeded=false, stage=CHECKPOINT)` und keine Controller-Aufrufe. Weiterer JUnit-Regressionstest ergänzt. Der bisherige Fehlerfall bleibt als Regression enthalten. Kein produktiver Controller angeschlossen, `Plan.executable=false` unverändert.

**Prüfstatus:** Isolierte Kotlin-Kompilierung und statische Projektprüfungen lokal; GitHub-JUnit/Android-Build und Android-Gerätetest erst nach Import. Termux v4, Workflows, Signatur, Supabase, Cloud, Gradle und historisches Storage-Backup unverändert. Storage-Backup bleibt in GitHub und ist aus der ZIP ausgeschlossen.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 5 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Phase 3O, Arbeitspaket 4 **GRÜN**.
**Aktueller Kandidat:** Arbeitspaket 5; GitHub-JUnit/Android-Build und Gerätetest OFFEN.

Der bisher isolierte `AnalyzerSetupTransitionProtocol` hat nun zusätzlich `ExclusiveBoundary` und `exerciseWithBoundary` für den **ausschließlich simulierten** Besitz einer durchgängigen Audio-Exklusivsperre. Fehlende oder vorzeitig verlorene Exklusivität wird fail-closed abgewiesen. Die Sperre bleibt in der Testumgebung auch beim Rollback erhalten und wird per `finally` freigegeben. Vier neue JUnit-Regressionstests: nicht verfügbare Sperre, Erfolgsfall, Rollback und Verlust der Exklusivität. Kotlin-Protokollkern lokal isoliert kompiliert (Warnungen); Android-Kompilierung nicht lokal ausgeführt.

**Wichtig:** Diese Schnittstelle ist kein produktiver Mikrofon-Arbiter und noch nicht mit Recorder/MON/Controller gekoppelt; ohne hardwareübergreifende exklusive Grenze, vollständige Controller-Adapter und echte Gerätetests darf es keine produktive Umschaltung geben. `Plan.executable=false` unverändert. Der vorherige reine Fake-Protokollpfad bleibt zur Regression bestehen.

**Nächste Schritte:** Einheitliche Sperre über MON, Scout, Rolling, Direct und Analyzer-Session; preference-freier Controller-Adapter; Fehler-Injektion auch beim Ressourcen-Stopp; UI-Anbindung; Gerätevalidierung. Historisches Supabase-Storage-Backup bleibt ausschließlich auf GitHub, nicht in Quell-ZIPs. Termux v4, Workflows, Signierung, Cloud und Gradle-Version unverändert.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 4 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Phase 3O, Arbeitspaket 3 **GRÜN**.
**Aktueller Kandidat:** Arbeitspaket 4; GitHub-JUnit/Android-Build und Gerätetest OFFEN.

Isoliertes, nicht produktiv angeschlossenes `AnalyzerSetupTransitionProtocol` ergänzt. Die synchrone STOP–Reconfigure–START-Protokollfolge führt einen vollständigen Checkpoint, verifizierten Start, Rollback-Stopp, Restore, Restart und Rückprüfungen durch. Fehler der Rücknahme werden separat erfasst, ohne den Ursprungsfehler zu überschreiben. Falls eine Wiederherstellung scheitert, meldet die Simulation einen sicheren STOP-Zustand nur nach positiver Überprüfung. Sechs neue JUnit-Fälle: Erfolg, Checkpoint-Ausfall, Fehler in vier Phasen, negatives Verify, Restore-Fehler und nicht nachweisbarer Safe-Stop.

**Sicherheitsgrenze:** Der Protokollkern wird ausschließlich über `exerciseWithControlledPort` von Fakes getestet. Es gibt weder einen produktiven Controller-Adapter noch einen atomaren MIC-Lease; deshalb weiterhin `Plan.executable=false` und KEINE Audio-/FFT-Setup-Aktivierung. Reale Freigabe erst nach gemeinsamem Hardware-Arbiter, Integration, vollständiger Fault-Injection und Gerätetests.

**Noch offen:** Exklusive Eigentümerschaft, vollständiger Controller-Adapter mit Pref-freiem START/STOP und Ressourcenverifikation, Rollback bei unvollständigem STOP, UI-Integration, Android-Gerätetest. Cloud, Recorder, Signierung, Termux v4 und historische GitHub-Sicherung unverändert.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 3 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter vom Benutzer bestätigter GitHub-TEST:** Phase 3O, Arbeitspaket 2 **GRÜN**.
**Aktueller Kandidat:** Arbeitspaket 3; GitHub-TEST, JUnit und Android-Gerätetest **OFFEN**.

## Umsetzung

- Der bestehende `AnalyzerSetupTransitionCoordinator` verwaltet Capture-Generationen nun über eindeutige Tokens pro Capture-Job. `captureJobsPending` bleibt ein abgeleiteter Sperrmarker und wird erst nach Abschluss sämtlicher Tokens eines Services freigegeben.
- `RollingAudioService` und `AnalyzerDirectAudioService` tragen den beim Job-Start vergebenen Token bis zum `invokeOnCompletion` weiter. Verspätete und doppelte Completion-Callbacks können andere Jobgenerationen nicht mehr freigeben.
- Die bisherigen tokenlosen Coordinator-Methoden bleiben für bestehende Tests/Kompatibilität vorhanden, können jedoch tokenisierte Jobs nicht beenden.
- Zwei JUnit-Regressionen prüfen überlappende Generationen sowie verspätete und doppelte Completions.
- **Keine** Freischaltung: `Plan.executable=false`. STOP–Reconfigure–START, Rollback und abschließende MIC-Arbiter-Grenze sind weiter offen.

## Nächste Arbeitspakete

1. Exklusive Audio-Besitzgrenze und STOP–Reconfigure–START-Transaktionskern unter sicherer Sperre.
2. Fehler-Injektion, Rollback und sicherer Endzustand.
3. UI-Anbindung, Integration, Regressionen und ausdrückliche Freigabe nach Android-Geräteabnahme.

## Prüfstatus

22/22 Phase-3O-Quellprüfungen lokal PASS (statische Quellprüfungen, einschließlich angepasster Legacy-Prüfer). GitHub-TEST und echte Android-Gerätetests stehen für diesen Quellkandidaten aus. Die Benutzerbestätigung `GRÜN` für Arbeitspaket 2 wird als vorausgehender Stand übernommen.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 2 (Quellkandidat)

**Stand:** 14.10.2026; Entwicklungsziel `0.5.56`; Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Phase 3O, Arbeitspaket 1 **GRÜN**.
**Aktueller Kandidat:** Phase 3O, Arbeitspaket 2; GitHub-TEST und Android-Gerätetest **OFFEN**.

## Umsetzung

- Die interne Markerentscheidung wird aus `blockingReasonsLocked()` abgeleitet und sowohl bei `assessGate()` als auch bei `withSetupReservation()` unter demselben Koordinator-Mutex verwendet. Insbesondere blockieren inkonsistente/verwaiste Destroy-Marker jetzt auch eine Reservierung (Fail-closed).
- `withTrackedServiceStart()` lehnt unbekannte Service-Bezeichner ohne Startaufruf ab und akzeptiert keine bereits vorhandenen Destroy-Marker desselben Service.
- Drei JUnit-Regressionstests: unbekannte Services, Ablehnung einer Reservierung während eines Service-Lifecycles sowie geschachtelte Reservierungs-/Start-Sperre.
- Ergänzter statischer Regressionstest `tools/verify_lfn056_phase3o_package2_gate_consistency.py`.

## Grenzen und verbleibende Arbeit

Dieser Schritt verbessert die gemeinsame interne Gate-Entscheidung, **stellt aber keine atomare Hardware-/Audio-Owner-Reservierung her**. Insbesondere bleiben Überlappungen verschiedener Capture-Jobs, fremde Recorder, asynchrone Lifecycle-Rennen, tatsächlicher STOP–Reconfigure–START, Rollback und UI-Aktivierung offen. Kein produktiver Setupwechsel; `Plan.executable=false`. Keine Modifikation an Services, AudioRecord, Cloud, Workflows, Signatur oder Termux v4. Historisches `supabase/storage_backup_before_v0526/` bleibt auf GitHub und wird nicht in Entwicklungs-ZIPs aufgenommen.

## Verbleibende größere Arbeitspakete

1. Abschluss aller asynchronen Audio-/Capture-Lifecycle-Sicherheitsgrenzen, einschließlich mehrerer Job-Generationen und externem Mic-Arbiter.
2. Transaktionaler STOP–Reconfigure–START-Kern unter nachgewiesen exklusivem Besitz.
3. Rollback, Fehler-Injektion und sichere Fehler-Endzustände.
4. Setup-UI, integrierte Regressionen und Geräteabnahme; Freischaltung erst nach ausdrücklich geprüfter Abnahme.

## Testnachweis

Sämtliche 21 Phase-3O-Quellskripte lokal PASS (einschließlich zwei aktualisierter statischer Prüfer für die gemeinsame Markerfunktion); JUnit wird ausschließlich durch GitHub-TEST verifiziert, nicht durch lokale Quellprüfungen. GitHub-TEST für Arbeitspaket 2 steht aus.

---

# LFN-Scout CURRENT_STATE – Phase 3O, Arbeitspaket 1 (nach Teilstufe 20)

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `0.5.55` / `213` unverändert.
**Letzter durch Benutzer bestätigter GitHub-TEST:** Phase 3O/20 **GRÜN**.
**Quellkandidat:** Phase 3O, Arbeitspaket 1. **GitHub-TEST GRÜN laut Benutzer**, Android-Gerätetest nicht belegt.

## Inhalt dieses Arbeitspakets

- `AnalyzerSetupTransitionCoordinator.diagnoseSafety()` führt die bisherigen
  internen Sperrgründe mit der bestehenden Audio-Eigentümerabfrage zusammen.
  Probe-Ausfälle ergeben `audio_owner_probe_failed` statt einer scheinbaren Freigabe.
- `AnalyzerSetupSwitchTransaction.execute()` verweigert den bisherigen
  *nicht ausführbaren* Pfad zusätzlich bei internen Service-/Cleanup-Sperren;
  Fehler der Port-Statusabfrage werden nun ebenfalls kontrolliert abgewiesen.
- Drei dynamische JUnit-Testfälle hinzugefügt und zwei statische Verifikatoren
  angepasst bzw. ergänzt. Kein neuer Recorder-Lifecycle und keine zusätzlichen
  JSON-/Preference-Schreibzugriffe.
- `SafetyDiagnosis.executionAuthorized=false` und `Plan.executable=false` bleiben
  bindend. Eine leere Sperrliste ist KEIN Freigabe-Nachweis; zwischen den
  Beobachtungen kann ein Android-Service seinen Zustand ändern (TOCTOU).

## Restplan – vier größere Pakete nach diesem Paket

1. **Gemeinsames Start-/Stop-Protokoll:** alle Aufnahmebesitzer und
   asynchronen Capture-Finalisierungen auf echte Reservierungsgrenzen prüfen;
   keine unbekannten Zustände als frei behandeln.
2. **Transaktionskern:** atomare STOP–Reconfigure–START-State-Machine über den
   bestehenden Controller, ausschließlich bei nachweislich exklusivem Besitz.
3. **Rollback und Fehler-Injektion:** Checkpoints, vollständiges Recovery,
   sichere Endzustände, fault-injection- und Race-Regressionen.
4. **UI-Anbindung und Stabilisierung:** Setup-Auswahl/Vorschau/Save-Dialoge
   integrieren, Android-Gerätetests durchführen, Freischaltung erst nach Abnahme.

## Prüfstatus

Quellbasierte Phase-3O-Regressionen: 17/20 Skripte innerhalb der lokalen 5-s-Grenze erfolgreich; drei Skripte (`verify_lfn056_phase3o.py`, `verify_lfn056_phase3o_14_fix_return.py`, `verify_lfn056_phase3o_async_start_tracking.py`) überschritten die Grenze und bleiben **nicht abschließend geprüft**. Die beiden unmittelbar betroffenen Sicherheits-Verifikatoren (`owner_gate`, `safety_diagnosis`) sind erfolgreich. Java/Kotlin-JUnit, Android-Build und Hardware-Gerätetest wurden lokal **nicht** ausgeführt; GitHub-TEST ausstehend.

## Historischer Stand

# LFN-Scout CURRENT_STATE – Phase 3O/20

**Stand:** 14.10.2026 · Entwicklungsziel `0.5.56` · Gradle `versionName 0.5.55`, `versionCode 213` unverändert.
**Letzter bestätigter GitHub TEST:** Phase 3O/19 **GRÜN (Nutzerbestätigung)**. Keine Commit-ID neu verifiziert.
**Neuer Quellkandidat:** Phase 3O/20; GitHub TEST und Android-Gerätetest OFFEN.

## Implementiert in 3O/20

`AnalyzerSetupTransitionCoordinator.assessGate()` stellt unter einem gemeinsamen Lock eine **nur diagnostische** Entscheidung mit stabilen Sperrgründen bereit (`setup_reserved`, `service_in_flight:<service>`, `capture_cleanup_pending:<service>`, optional verwaiste Destroy-Marker). `coordinatorMarkersClear=true` bedeutet **nur**, dass die internen Marker leer sind; es ist **keine Ausführungsfreigabe** und prüft weder externe Recorder noch Hardware/Mikrofon-Arbiter. Keine automatische Freigabe, kein UI-Aufruf, kein Executor; `Plan.executable=false`. Zwei JUnit-Tests und statische Regression ergänzt. **GitHub TEST 3O/20 offen.**

## Implementiert in 3O/19

Der `AnalyzerSetupTransitionCoordinator` stellt einen synchronisierten, **rein lesenden** `GateSnapshot` mit Setup-Reservierung, laufenden Service-Starts, Destroy-Markern und ausstehenden Capture-Cleanups bereit. Die Sets werden kopiert; Diagnostik kann damit keine Koordinatorzustände verändern oder ausstehende Marker löschen. Zwei JUnit-Regressionsfälle sowie `tools/verify_lfn056_phase3o_gate_snapshot.py` hinzugefügt. Dies ist noch keine UI-Anzeige, kein automatischer Timeout und keine Freigabe einer Transaktion. GitHub TEST von 3O/19 offen.

## Implementiert in 3O/18

Doppelte bzw. verspätete `serviceDestroyed()`-Callbacks erzeugen keine veralteten `destroyedServices`-Marker mehr, wenn weder Start noch Capture-Job ausstehen. Doppelte `captureJobFinished()`-Callbacks dürfen keine andere Sitzung abschließen. Zwei neue JUnit-Tests und ein zusätzlicher statischer Regressionstest. Der ältere MON-Quelltest wurde auf die strengere `captureJobsPending`-Abfrage aktualisiert. GitHub-TEST von 3O/18 offen. **Kein ausführbarer Setupwechsel.**

## Implementiert in 3O/17

`withTrackedServiceStart` verweigert jetzt einen zweiten START für denselben Service, solange dessen vorheriger `inFlightServices`-Marker oder ein Capture-Cleanup besteht. Damit kann eine doppelt angenommene asynchrone Anfrage nicht denselben Set-Eintrag teilen und anschließend verfrüht freigeben. Drei JUnit-Regressionen für doppelte Anforderung, ausstehenden Cleanup und ordnungsgemäßen Neustart nach Freigabe ergänzt. Diese konservative Sperre ändert das Verhalten bei wiederholten START-Anfragen; die Android-Integration muss im TEST und am Gerät überprüft werden. **Kein ausführbarer Setupwechsel.**

## Vorheriger Stand 3O/16

Die asynchronen Rolling-/Direct-Capture-Jobs werden vor dem Launch als ausstehend vorgemerkt. `onDestroy()` kennzeichnet nur das Ende des Service-Lifecycles; erst das zugehörige `Job.invokeOnCompletion` bestätigt den Abschluss des Coroutine-`finally` und damit den Capture-Cleanup. Die Startvormerkung wird nur entfernt, wenn beide Bedingungen erfüllt sind. Ein Setup-Reservierungsversuch scheitert auch bei noch ausstehendem Capture-Job. Drei JUnit-Regressionen und ein neuer Quelltest ergänzt. **Kein ausführbarer Setupwechsel.**

## Vorheriger Stand 3O/15

`AnalyzerSetupTransitionCoordinator` protokolliert erfolgreiche Rolling- und Direct-Startanforderungen vor dem asynchronen Android-Service-Start unter einem `inFlightServices`-Marker. Die Setup-Reservierung wird verweigert, solange mindestens ein Marker besteht. `Service.onDestroy()` kennzeichnet den Service als zerstört; der Marker wird seit 3O/16 erst nach Coroutine-Jobende entfernt. Fehlgeschlagene Startanforderungen und Exceptions entfernen ihn ebenfalls. Entsprechende statische Checks und drei JUnit-Tests ergänzt. Ältere statische Guards akzeptieren jetzt die erweiterte `withTrackedServiceStart`-Schnittstelle.

## Grenzen

- Noch **kein** vollumfänglicher atomarer Recorder-/Service-/AudioRecord-Übergang. Der Jobabschluss erfasst den synchronen Cleanup im Capture-`finally`, nicht zwingend alle fremden asynchronen Folgearbeiten; der Mikrofon-Arbiter und die übrigen Audio-Owner-Prüfungen bleiben notwendig.
- Bei erfolgreich akzeptiertem START ohne späteres Service-`onDestroy()` bleibt die Setup-Sperre vorsichtshalber gesetzt. Keine Zeitüberschreitung mit unsicherer Freigabe.
- `Plan.executable=false`, keine produktive Audio-/FFT-Umschaltung und kein vollständiger Hardware-Rollback.
- Neue JUnit-Tests enthalten, noch nicht via GitHub ausgeführt. Statische Regressionen sind keine Android-Kompilierung.

## Unverändert

Termux v4, Workflows/Signatur, Cloud, Gradle-Version. `supabase/storage_backup_before_v0526/` bleibt historisch auf GitHub `main` und ist bewusst nicht in den Quell-ZIPs; fehlende Dateien sind keine Löschanweisung. `docs/project/CURRENT_STATE.md` bei jedem Quell-ZIP aktualisieren.

## Nächster Schritt

Weitere Audio-/Recorder-Ressourcen inklusive Fremd-Coroutinen und stale `inFlight`-Marker absichern und danach erst einen vollständigen STOP-Reconfigure-START-Executor mit Rollback vorbereiten. Ausschließlich GitHub-TEST nötig, REGULAR nicht obligatorisch.

## Prüfhistorie 3O/16

Phase-3O-Quelltests PASS. Ältere Phase-3F-/3J-Quelltests schlagen wegen später hinzugefügter Setup-UI-/Session-Funktionen fehl (3F 3 von 12 Checks, 3J 2 von 18); diese Tests wurden nicht als PASS ausgegeben. GitHub TEST von 3O/16 GRÜN (Nutzerbestätigung); GitHub TEST von 3O/17 und 3O/18 GRÜN (Nutzerbestätigung).

Historie: Phase 3O/14 führte gemeinsame Rolling-/Direct-Startschranken ein; Phase 3O/14-FIX1 korrigierte den Kotlin-Return-Fehler.

## Phase 3O / AP26 – TEST-Installations-/Versionskorrektur (15.10.2026)

- Nutzer meldet: AP25 TEST-APK lässt sich über bestehende TEST-Installation nicht installieren; die Versionsnummer blieb 0.5.55/213.
- Gradle `defaultConfig` nach ausdrücklicher Beanstandung des fehlenden Hochzählens auf `versionName = "0.5.56"`, `versionCode = 214` angehoben. Diese Änderung betrifft APK-Versionierung von TEST **und** REGULAR bei einem künftigen Build; es wird keine REGULAR-APK installiert.
- Separater TEST-Paketname (`.githubdebug`), unveränderte Workflows und Signaturwege. Keine Änderungen an Audio-, Setup- und Cloud-Implementierung gegenüber AP25.
- Android kann auch APKs mit gleicher versionCode aktualisieren; der genaue Installationsfehler ist noch nicht diagnostiziert. Bei `INSTALL_FAILED_UPDATE_INCOMPATIBLE` Signatur prüfen und nicht blind Daten löschen/deinstallieren.
- Quellkandidat: Android/Gradle-Test und Hardwareinstallation noch offen. AP25-Android-Prüfung ist nicht als erfolgreich zu bewerten.

## Phase 3O / AP27 – Mikrofon- und GPS-Berechtigungsdialog (15.10.2026)

- Beim ersten Öffnen einer neuen Activity-Instanz zeigt die App bei fehlenden Runtime-Berechtigungen einen erklärenden Dialog zu Mikrofon (Analyzer/MON/Aufnahme) und GPS/Standort (Messort). `Berechtigungen erteilen` fordert ausschließlich fehlende Rechte über Androids Systemdialog(e) an; `Später` ist jederzeit möglich. Keine automatische Audio-/GPS-Aktivierung und keine Änderung an Audio-/Cloud-/Setup-Routinen.
- Manifestberechtigungen existierten bereits. Standort erlaubt auch ungefähre Ortung; Fine und Coarse werden gemeinsam abgefragt. Ein verweigertes Recht blockiert nicht den App-Start. Bereits genehmigte Rechte werden nicht erneut abgefragt.
- `versionName=0.5.57`, `versionCode=215` für aktualisierbare TEST-APK. Separater TEST-Paketname und Build-/Signierwege unverändert. `Plan.executable=false` unverändert.
- Validierung: Quell- und ZIP-Prüfungen lokal; GitHub/Gradle-Build und Android-Hardwaretest noch ausstehend.


## AP31 kombinierter STOP-Entwurf (2026-10-15)
Neue kombinierte Analyzer+MON-Entwurfsübernahme mit Vorab-STOP-Sperren, Snapshots und Rücknahme bei MON-Fehler. Noch nicht persistent; normales START kann alte Legacy-Einstellungen laden. Kein Hardwaretest und kein Gradle-Lauf bestätigt.
