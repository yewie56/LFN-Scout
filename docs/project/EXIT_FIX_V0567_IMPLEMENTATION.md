# LFN-Scout v0.5.67 – Analyzer verlassen / MON-Persistenz

## Verbindliches Verhalten

- „Ja – weiterlaufen“: Vor dem Routenwechsel wird das aktive JSON inklusive der aktuellen MON-Werte gesichert. Analyzer-RUN, MON und Rolling bleiben aktiv, die Oberfläche geht zurück zu Scout.
- „Nein – stoppen“: Erst JSON sichern, dann MON-Aufnahme geordnet beenden, MON-/Analyzer-Messung stoppen, ursprünglichen Rolling-Zustand bestätigen und nach erfolgreicher Wiederherstellung zu Scout wechseln.
- Rolling lief vor Analyzer-Beginn: bleibt nach „Stoppen“ erhalten. Andernfalls wird der vom Analyzer aktivierte Rolling-Dienst wieder beendet.
- Scheitert die JSON-Sicherung, bleibt der Analyzer-Bildschirm geöffnet; es wird kein Stop ausgelöst.
- Kann der Audiomodus nicht sicher wiederhergestellt werden, bleibt der Bildschirm geöffnet und zeigt den Fehler. Ein unbestätigter Stop darf nicht als Erfolg gemeldet werden.

## Befund und Änderungen

Die bisherige `DisposableEffect.onDispose`-Sicherung war für „Stoppen“ zu spät: `finishStopped()` stellte davor bereits den Audiomodus wieder her. Jetzt wird vor den beiden Dialogentscheidungen explizit gespeichert. Die bisherige doppelt verschachtelte Coroutine beim Stoppen wurde entfernt. `restore()` wartete nur bei ursprünglich aktivem Rolling auf den Dienst; bei ursprünglich inaktivem Rolling wird jetzt ebenfalls bis zur bestätigten Freigabe gewartet. Die Herkunft wird beim Beginnen anhand der laufenden Engine statt allein der gespeicherten Präferenz festgestellt.

`AnalyzerMonitor.stop()` ist aus dem Oberflächen-`onDispose` entfernt: ein Routenwechsel mit „Weiterlaufen“ ist **kein** MON-/Analyzer-STOP. Der Stoppen-Dialog beendet MON explizit.

## Prüfungen und Grenzen

- Statische Regression: `python tools/verify_v0567_exit.py` (14/14 PASS).
- ZIP-Test, Whitespace und bytegleiche Termux-v11-Datei wurden bei der Paketierung geprüft.
- **Offen:** Gradle `testDebugUnitTest`, Kotlin-/Android-Compile, integrierter Geräte-Self-Test, reale PCM-/Rolling-Kontinuität und Gerätetest der beiden Dialogwege, auch nach erneutem Öffnen.
- Erwartete Gerätefälle: MON-Gain während RUN ändern; „Weiterlaufen“ und zurückkehren; Gain erhalten, Analyzer und Rolling weiterhin RUN. „Stoppen“ bei zuvor aktivem Rolling erhält dieses. „Stoppen“ bei zuvor inaktivem Rolling beendet es. Jede Stoppen-Entscheidung führt nach einmaligem Druck zu Scout. Fehlerhafte JSON-Datei/Schreibfehler darf keine Navigation oder Aufnahmebeendigung auslösen.

## Ergänzung: fehlgeschlagene JSON-Speicherung

- Vor dem Speichern wird das aktuelle Analyzer-/MON-JSON mit der vorhandenen Datei verglichen. Bei identischem Inhalt erfolgt kein Schreibzugriff.
- Beim benannten Setupwechsel erscheint bei Schreibfehler ein Popup mit „Erneut speichern“, „Trotzdem wechseln“ und „Abbrechen“. Nur eine ausdrücklich bestätigte Verwerfaktion setzt den Wechsel ohne Speichern fort; Sicherheits- und STOP-Prüfungen des Setupwechsel-Ports bleiben bestehen.
- Beim Analyzer-Verlassen wird bei fehlgeschlagener Speicherung eine Warnung mit „Erneut speichern“, „Trotzdem verlassen“ und „Abbrechen“ angezeigt. „Trotzdem verlassen“ führt den zuvor gewählten Weiterlaufen- oder Stoppen-Weg aus.
- Offene Integrationstests: Kotlin-/Android-Build, Popup auf Gerät, Schreibfehler-Simulation, Audioressourcen und dauerhafte MON-Persistenz.
