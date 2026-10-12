# LFN-Scout v0.5.00 – platzfüllendes Analyzer-Hauptbild

**Basis:** v0.4.99, Code 140. **Neuer Testkandidat:** v0.5.00, Code 141. Nutzerbeobachtung: Auf dem Xiaomi lag unter dem Android-Analyzer ein großer weißer Streifen; redundante Karten und Bedienelemente beanspruchten zusätzlichen Platz.

## 1. Implementierter Umfang

1. `LiveAnalyzerScreen` nutzt einen dunkel hinterlegten `BoxWithConstraints(Modifier.fillMaxSize())` als ganzen Viewport. Der scrollbare Inhalt füllt ebenfalls die Größe. Ziel: Auch wenn der Inhalt kürzer ist, kein heller Hintergrund aus dem übergeordneten Main-UI sichtbar.
2. Von `maxHeight` werden 134 dp für kompakte Kopfzeile, Status, Wasserfall-Status, PNG-Aktionszeile und Innenabstände reserviert. Die verbleibende Höhe wird normal mit 53 % Spektrum und 47 % Wasserfall verteilt. Die Untergrenzen von 215/190 dp stellen den Scroll-Fallback sicher. Im erweiterten Wasserfallmodus bleibt das Spektrum 150 dp und der Wasserfall erhält die verbleibende Höhe (mindestens 300 dp). Die Beträge sind Layout-Startwerte, **keine garantierten Pixelpositionen** auf jedem Android-Gerät.
3. Hauptbild: `Mithören`, `Superfast`, `Anzeige` als komplette Kartenzeile entfernt. `Mittelwert`- und `Rauschboden`-Schalter wurden nicht funktional entfernt; sie liegen weiterhin im Settings-Unterpanel `Spektrum` und sind dort persistent. `Diagnose` liegt im neuen Unterpanel `Diagnose`; der Live-Fehlertext führt ins Einstellpanel.
4. Fußbereich: nur `📷 PNG` und `Auto EIN/AUS`; doppelte Buttons `Einstellungen`/`Diagnose` und die statische Informationszeile entfallen. Manuelle PNG-Aufträge und automatischer 120-s-Zyklus bleiben unverändert.

## 2. Nicht geändert

- Gemeinsamer Mic-/Roh-PCM-Owner, 8 kHz → 1 kHz FIR, FFT-Hop, Wasserfall-Hop und drei Zeilenmodi, Statistikreset, Cursor, dBFS-Autorange und manuelle Grenzwerte.
- 120-s-WAV und bestehende Aufzeichnung, Supabase-/Recovery-Dateien, Installation unter `de.brummbeobachter.app`, PNG-Speicherort.
- `Mithören`/`Superfast` weiter für spätere Versionen geplant, **nicht** implementiert.

## 3. Abnahmefälle auf Xiaomi

- Nach dem Start kein weißer Streifen innerhalb des verfügbaren App-Viewports; Android-Navigationsleiste ist separat zu beurteilen.
- Spektrum und Wasserfall nutzen den frei gewordenen Platz; dBFS- und Hz-Ticks sowie linke Farbskala bleiben sichtbar. Bei Bedarf im kleinen Fenster scrollen.
- Über ⋮ → Einstellungen → Spektrum: Mittelwert- und Rauschbodenanzeige umschaltbar; → Diagnose: PCM-Diagnosedaten sichtbar.
- `📷 PNG` erstellt die manuelle Datei und Auto-PNG bleibt per unterem Schalter bedienbar; Einstellung der Auto-Funktion unter `Automatiken` bleibt synchron.
- Wechsel zu anderem Panel/Rolling-Aufnahme weiterhin möglich; kein 2. AudioRecord.

**Teststatus:** Statischer Source-Contract und ZIP-/Manifest-Integrität lokal; Android-Build, Gerätescreenshot und PNG-Langzeittest ausstehend.
