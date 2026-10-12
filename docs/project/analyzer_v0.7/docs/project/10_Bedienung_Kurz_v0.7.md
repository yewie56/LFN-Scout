# Kurzbedienung / Bedienkonzept – LFN-Scout Analyzer

**ID:** LFN-AN-UM · **Version:** 0.7 · **Status:** geplante Bedienung, noch keine verfügbare App-Funktion.

1. In LFN-Scout das Unterpanel **„Spektrum & Mithören“** öffnen. Standard: lineare Anzeige 0–100 Hz; Gelb = live, Rot = optionales 120-s-Average. Unterhalb des Spektrums läuft der Inferno-Wasserfall.
2. `⚙` öffnet ein **schmales, scrollbares Einstellpanel**. Dort Quelle `UNPROCESSED` auswählen, falls unterstützt, und FFT/Decimations/Skalen/Cal und weitere Parameter einstellen. Keine Einstellungen annehmen, die das Gerät nicht unterstützt.
3. Das dezente Band im Spektrum/Wasserfall kennzeichnet nur **Mithören**. Horizontal verschieben = Bandmitte ändern. Band**breite [Hz]** steht unter **Spektrum**; numerische Grenzen zusätzlich unter Mithören. Das Statistikband 2–25 Hz ist **unabhängig**.
4. **Mithörfeld unten links kurz antippen:** AUS↔EIN. Hoch/Runter wischen verändert Nutzer-Gain. Nur auf kompatibler Kopfhörerroute, mit vorsichtiger Lautstärke. Monitor-Clip/AUTO-Warnungen beobachten; sie ändern keine Messwerte.
5. **Pause** friert Spektrum/Wasserfall ein; eingeschalteter Fadenkreuzcursor aus X-/Y-Linie bleibt verschiebbar und zeigt Frequenz/Pegel aus eingefrorenem Spektrum. Erneut Pause/Fortsetzen setzt die Anzeige fort.
6. **Wasserfall kurz antippen:** untere Zusatzkarten verschwinden und Wasserfall wird höher; Spektrum bleibt sichtbar. Nochmal tippen: Normalansicht.
7. Unter Statistik L50/L90 zuschalten (Standard 2–25 Hz / 120 s). Werte sind erst nach vollständigem Fenster gültig und bei unzureichender Bandkalibrierung nicht als absolute SPL interpretierbar.
8. Superfast/Maximum Sample kann kurze Eingangssample-Spitzen beobachten. Der ausgegebene Sample-Peak ist keine garantierte akustische Impulsmessung.
9. Kalibrierprofil bei Bedarf importieren/aktivieren; nur bei gültiger **absoluter** Referenz und validiertem Frequenzband darf SPL angezeigt werden. Rauschboden kann als unabhängige graue Referenz angezeigt oder optional rechnerisch nur in der Anzeige abgezogen werden.
10. `Save aktiv` nach Nutzeraktion einschalten: erstes PNG nach kompletter Wasserfallfüllung, danach je gleicher Wasserfallzeit. Manuelles PNG/CSV ist getrennt. **Es wird kein Audio aufgenommen.**
11. Unter Setups mehrere benannte Konfigurationen speichern, duplizieren, Recall/Laden und exportieren/importieren. Nach Recall bleiben Mithören und tatsächliche Bildautomatik zunächst AUS.

**Vor Einsatz:** Auf eigenem Gerät Mikrofonfrequenzgang, tatsächliche Sampling-Rate, 2–25 Hz, Clip-/Headset-Sicherheit und die korrekte Anzeigeeinheit prüfen.


## Neu: Parallelbetrieb und Radiushistogramme (v0.6)

1. **Save einschalten:** In `Save & Export` aktivieren; „Save aktiv“ zeigt nur **Bildspeicherung** an. Nach einer vollen Wasserfallzeit erscheint die erste PNG-Datei. Es wird kein Audio durch dieses Unterpanel aufgezeichnet.
2. **Anderes Panel öffnen:** Die Analyzer-Sitzung und automatische PNG-Speicherung laufen während des internen Panelwechsels weiter. Beim Zurückkehren erscheint die fortgeschriebene Anzeige, keine neu gestartete künstliche Historie.
3. **Bestehende Audioaufnahme zusätzlich starten:** Sie verwendet appintern denselben koordinierten Mikrofoneingang. Bei inkompatiblen Eingangsparametern erscheint eine deutliche Meldung; eine laufende Datei-Aufnahme wird nicht still umkonfiguriert.
4. **Radiushistogramme:** Unter `Anzeige & Wasserfall → Radiushistogramme` AUS / Letztes / Letzte 4 wählen. Bei genügend Bildschirmbreite erscheinen die neuesten vier Miniaturen **rechts übereinander**. Zeitstempel und Status helfen, ältere Daten nicht mit der aktuellen Akustik zu verwechseln.
5. **Erweiterte Ansicht:** Wasserfall antippen blendet weiterhin nur die unteren Zusatzkarten aus; Spektrum und Histo-Spalte (falls eingeblendet) bleiben sichtbar. Noch einmal tippen für Normalansicht.
6. **Anzeige-Pause:** Auto-PNG wird ausgesetzt und nach Fortsetzen erst nach neuer vollständiger Wasserfallzeit wieder aktiv. Pause stoppt nicht automatisch die bestehende Datei-Aufnahme und ist kein Panelwechsel.
7. **App im Hintergrund:** Für die erste Integration nicht pauschal zugesichert; Freigabestatus und Android-Berechtigungen beachten. Interner Wechsel in der geöffneten App ist davon unabhängig.

## Bedienergänzung v0.7

Das **untere linke Mithörfeld selbst** kurz antippen, um nur die Kopfhörerausgabe EIN/AUS zu schalten. Lautstärke im Feld vertikal wischen; das Mithörband in Spektrum oder freier Wasserfallfläche horizontal verschieben. Ein Tippen auf freie Wasserfallfläche blendet die unteren Zusatzanzeigen aus und erweitert **nur** den Wasserfall nach unten; das Spektrum bleibt bestehen. Nochmals tippen stellt die Normalansicht wieder her. Gelb = Live, Rot = Average, Grau = nur bei aktivierter Rauschbodenreferenz. Cursor-Fadenkreuz liest aktuelle oder ausgewählte Kurve, auch bei Pause. `Save aktiv` erzeugt zeitgestempelte PNGs, keine Audiodateien; beim internen Panelwechsel läuft es weiter. Der separate bestehende Recorder verwendet bei parallelem Betrieb die gemeinsame Audioquelle.

