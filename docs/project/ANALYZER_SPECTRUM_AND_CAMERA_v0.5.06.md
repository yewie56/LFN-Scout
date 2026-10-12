# LFN-Scout v0.5.06 – Spektrumkurven und manuelle Kamerarückmeldung

## Ausgangsbasis

v0.5.05, Nutzer auf Xiaomi getestet; einziges nachgemeldetes Problem: nach langem Kameradruck kein deutliches Pop-up. Core-Baustein `LiveAnalyzerCore.Spectrum.maxHoldDbFs` und `resetMaxHold()` bestanden bereits vor dieser Version; bisher keine schaltbare MAX-Kurve im Live-Panel. Betriebsarten, Kamera-Automatik und WAV unverändert lassen.

## Darstellung

- **Aktuell:** gelb, Standard EIN, in `show_current` persistent.
- **Average:** rot, Standard EIN, vorhandene Einstellung `show_average` beibehalten.
- **Maxhold:** türkis, Standard AUS, in `show_max_hold` persistent; Core verfolgt den Maximalwert auch bei ausgeblendeter Kurve seit letztem Analyzer-Reset. Bei Änderung EIN wird deshalb die bis dahin vorhandene Maximalwerthistorie sichtbar. `MAX / CLR` setzt den Maximalwertverlauf ausdrücklich zurück.
- **Rauschboden:** bestehende graue Kurve bleibt eigenständig per `show_floor` schaltbar.
- Beliebige Kombinationen bis hin zu allen Kurven AUS zulässig; PCM, FFT und Statistik laufen unverändert weiter. Bildschirm und Offscreen-Bildexport prüfen jeweils dieselben Sichtbarkeitsschalter.
- MAX / CLR nur bei aktiviertem Maxhold; zweizeilig, 48 dp und nicht höher/breiter als die 48-dp-Kamera. Reset verändert nur den Core-MaxHold und die aktuelle MaxHold-Anzeige. Wasserfall, L50/L90, Average, PCM und Audio-Betriebsart bleiben unberührt.

## Speicherung und Migration

Im internen Store `lfn_scout_live_analyzer` werden `show_current`, `show_average`, `show_max_hold`, `show_floor` verwendet. Alle drei relevanten Flags werden mit dem jeweiligen Setup 1–4 gespeichert; Legacy-Slots ohne Felder laden Defaults Aktuell EIN / Maxhold AUS. Die getrennten `mode_8_*`-/`mode_48_*`-Profile kopieren diese Schlüssel ebenfalls. Export-JSON bleibt eine **lesbare Kopie**. Nicht im exportierten JSON editierte Werte zurück in die App importieren.

## Manuelle Kamerarückmeldung

Ein kurzer Klick toggelt Auto-Bilder. Ein Langdruck versucht genau **einen** manuellen PNG-/JPG-Export, unabhängig vom Auto-Status. Der Writer verarbeitet den Auftrag asynchron. Nach tatsächlichem Dateischreiben bzw. Fehler erzeugt ausschließlich der manuelle Auftrag ein Abschlussereignis (`manualSaveEvent`, `manualSaveMessage`). Die UI beobachtet diese Nummer und zeigt den Text für ca. **2200 ms** als Pop-up **außerhalb des vertikal scrollenden Inhalts**, auf dunklem Untergrund in gut sichtbarer Schrift. Bei unmittelbarer Ablehnung (kein Signal / anderer Writer aktiv) wird stattdessen eine Fehlermeldung gezeigt. Auto-Zyklen erzeugen keine manuelle Meldung. `lastSave` und allgemeiner Analyzer-Status bleiben für andere Funktionen erhalten.

## Testgrenze

Host-Core-MaxHold-Selbsttest und statische Quellprüfung sind keine Android-/Compose- oder Xiaomi-Abnahme. Auf dem Gerät sowohl PNG als auch JPG, aktiviertes/deaktiviertes Auto, mehrere aufeinanderfolgende Exporte, Wechsel zwischen Panels und Setup-Slots testen. Bei einem Absturz Logcat mit `sid` bereitstellen. Offene 16:1-Pegelabweichung nicht als korrigiert melden.
