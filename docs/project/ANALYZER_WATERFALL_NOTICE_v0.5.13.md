# LFN-Scout v0.5.13 – Wasserfall-Zustandsmeldung als 5-s-Pop-up

**Status:** Quellcode implementiert, JVM-Texttest und statische Quellprüfung geplant/auszuführen. Vollständiger Android-/Compose-Build und Xiaomi-Abnahme **NOT RUN**.

## Fachliche Anforderung

- Die unter dem Wasserfall bisher permanent ausgegebenen Statuszeilen `Wasserfall …`, `FILTER …` und `FILTER-Autorange …` entfallen als statische UI-Elemente.
- Bei Wechseln von Akt/MW/MAX, Wasserfall-Zeit/Frequenz/Pegel, ORIGINAL/FILTER EIN/AUS, Filterbandplan oder FILTER-Pegelautomatik erscheint eine **zweizeilige** Meldung **unten rechts direkt über dem Wasserfall**. Eine Vergrößerung/Verkleinerung des Wasserfalls gilt ebenfalls als Anzeigenwechsel.
- Erste Zeile: Wasserfall-Modus, Historie, Abtastrate und FFT-Hop bei bisheriger **10-sp-Schrift**. Zweite Zeile: gewählter ORIGINAL-/FILTER-Modus, Filterzahl und Filterbereich, bei FILTER 9 sp. Lange Texte werden notfalls mit Ellipse gekürzt, nicht durch Verkleinern der Schrift.
- Meldung läuft **5.000 ms ab dem letzten Anzeigeschaltvorgang**; ein neuer Vorgang ersetzt sie und beginnt den Zeitraum neu. Kein erneuter Start nur durch neue FFT-Daten, neue Zeilen, Filter-Autorange-Fortschrittsupdates oder sonstige periodische Snapshots.
- Die Meldung ist eine separate Compose-Overlay-Ebene *im Wasserfall-Box-Viewport*. Sie belegt keinen Platz im Layout: Wasserfall-Bitmap, Frequenzflucht, Zeitband und Colorbar bleiben exakt an Ort und Stelle. Sie wird nicht in das gespeicherte PNG/JPG eingerechnet. Der darunterliegende Wasserfall bleibt währenddessen in Echtzeit aktiv und unverändert im Datenmodell; nur die darüberliegende Meldung verdeckt temporär einen kleinen Bereich.
- Keine Änderung der Kamera-Bestätigung, der beiden freigegebenen ORIGINAL-/FILTER-Icons und ihrer 48-dp-Touchbereiche, der Audioaufnahme, der FFT, der Filterbank oder der Setups.

## Implementierung

- `AnalyzerWaterfallNotice.kt`: reine Kotlin-Textformatierung mit zwei Zeilen und FILTER-Farbflag.
- `LiveAnalyzerScreen.kt`: darstellungsabhängiger Schlüssel ohne `rows.size`/`waterfallRevision`/Live-Autorange-Prozent, `LaunchedEffect(key)` mit 5-s-Timeout und `Column(Modifier.align(Alignment.BottomEnd)...)` nach dem Canvas im Wasserfall-`Box`. Kein Android-Service oder neuer Timer im Capture-Thread.
- `tools/waterfall_notice_v0513_selftest.kt` und `tools/verify_v0513_notice.py` decken Formate und Quellcode-Verträge ab. Android-Compose-Position und visuelle Lesbarkeit bleiben durch Gerätetest abzunehmen.

## Offen

Die noch ausstehende vollständige Impulsereigniserkennung, die 16:1-Pegelabweichung, FILTER-Colorbar-Tap und Geräte-/Android-Abnahme werden durch diesen reinen UI-Umbau **nicht** behoben.
