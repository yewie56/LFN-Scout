# LFN-Scout – permanenter System- und Regressionstest, Startversion 0.5.75

## Verbindliche dauerhafte Richtlinie

Der Systemtest bleibt in allen zukünftigen Versionen über **Analyzer → Einstellungen → Diagnose** verfügbar. Neue Funktionen und Fehlerbehebungen erhalten zusätzliche automatisierte Prüfungen. PASS/FAIL/SKIP werden getrennt dokumentiert. Das Tool ist kein Ersatz für tatsächliche Android-Geräte-, Compose-UI- und Audiohardwaretests.

## Implementierungsstand 0.5.75

**Stufe 1 – Implementiert:** Read-only-Testcontroller `PersistentSystemTest`, der die bereits vorhandenen internen Logereignisse auswertet. Keine Steuerung des Audio-/Rolling-Lebenszyklus; keine Änderungen an Setups, Aufnahmen oder Cloud-Warteschlangen. Interne Diagnose-Schaltfläche "Automatischen Systemtest starten" und "Systemtest-Paket exportieren". ZIP enthält `systemtest_results.json`, `systemtest_results.csv`, `systemtest.log`, `app_diagnostic.log`, `README.txt`. Das Log wird beim Export aus der bestehenden Logdatei gelesen und nach den bestehenden Exportregeln bereinigt.

**Nicht implementiert / SKIP:** Vollständig automatisiertes Drücken oder äquivalentes Auslösen von Analyzer-STOP/RUN, Setupwechsel, MON und Filterbank; isolierte Tests mit temporären JSON-Setups; Audio-Blocksequenzen/Lückenerkennung; reale Langzeit-Aufnahme-Schreibkontinuität; unabhängige Statistik-Abdeckungsmetrik. Der Test darf diese Punkte nicht als PASS ausgeben. Insbesondere beweist das Vorhandensein von Heartbeats keine samplegenaue Kontinuität. Die Log-Auswertung bezieht sich nur auf die vorhandenen Logzeilen und ist kein aktiver Wiedergabetest.

## Nächste inkrementelle Entwicklungsstufe

Ein sicherer Testcontroller für echte App-Steuerpfade benötigt eine Transaktions-/Rollback-Schnittstelle, isolierte Test-Setups und eine explizite Prüfung vorhandener Recording/Cloud-Aufträge. Erst damit können automatisch echte STOP/RUN-, Setup- und MON-Übergänge ausgelöst werden. Ein Testabbruch darf keine Nutzdaten verlieren. Diese Aufgaben bleiben verbindlich offen.

## Qualitätsregeln

- Fehlende Daten => SKIP statt PASS.
- Android-Exceptions, die nur in System-Logcat stehen, sind durch das interne Log nicht immer erkennbar.
- Vor Freigabe: Android-Gradle-Build, JVM-Unit-Tests, integrierter Self-Test, echte Geräteprüfung.
- Regressionen zum Rolling- und Analyzer-Zusammenspiel sind dauerhaft im Testregister zu halten.
