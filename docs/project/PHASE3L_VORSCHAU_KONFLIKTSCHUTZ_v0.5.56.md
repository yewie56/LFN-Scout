# LFN-Scout v0.5.56 – Phase 3L: Vorschau-Abschluss und Konfliktschutz

## Änderungen

1. Der Abschlussdialog einer Anzeige-Vorschau hat genau drei unabhängige Aktionen: **Speichern**, **Verwerfen**, **Abbrechen**. Ein in Phase 3K versehentlich im Dialog wiederholter Vorschau-Abschlussknopf wurde entfernt.
2. Ein MON-Setupwechsel ist gesperrt, solange eine Anzeige-Vorschau aktiv ist. Der Nutzer beendet zuerst die Vorschau; der bestehende MON-Entwurf wird dabei nicht automatisch gespeichert.
3. Beim Beginn einer Anzeige-Vorschau werden ausschließlich die tatsächlich angewendeten Anzeige-Felder als unveränderter Vergleichsstand erfasst. Beim Speichern werden diese Felder mit dem aktuellen JSON verglichen. Wurden sie zwischenzeitlich in derselben JSON-Setup-Datei geändert, wird Speichern abgelehnt. Die Vorschau bleibt aktiv, und die Meldung empfiehlt Verwerfen/Neustart. Unbeteiligte Parameter dürfen sich verändert haben und bleiben erhalten.
4. Bereits bestehende Vorsichtsregeln bleiben: kein implizites Speichern in SharedPreferences; kein Abschluss während MON/MON-Aufnahme; keine Audio-/FFT-Umschaltung; keine automatische Legacy-Migration.

## Testumfang / Grenzen

Die neuen statischen Regressionen prüfen UI-Aktionsführung, Sperre und Versionskonflikt-Logik anhand des Quellcodes. Sie ersetzen weder Kotlin/Compose-Kompilierung noch dynamische JVM-/Android-Gerätetests. Es ist weiterhin ein Entwicklungskandidat; `versionName` bleibt **0.5.55** bis zum vollständigen v0.5.56-Release.

Vor weiteren Controller-Eingriffen erneut GitHub-TEST-Build durchführen. Anschließend auf dem Gerät besonders prüfen: Speichern, Verwerfen, Abbrechen, MON-Setupwechsel während Vorschau, externe JSON-Änderung vor Speichern, Fehlerdialog mit erneutem Verwerfen.
