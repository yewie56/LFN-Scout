# LFN-Scout – FAST-Filterwasserfall v0.5.11

Status: **Implementierungsstufe A – experimenteller Buildkandidat. Kein Android-/Xiaomi-PASS.**

## Maßgebliche Entscheidungen

- 8-kHz-Analyzer ergänzen, keine neue Audioaufnahme. Original- und FILTER-Button: je 48 dp, Mini-Spektrogramm in der tatsächlich vorhandenen Inferno-ähnlichen LUT / **fünf** symmetrische Balken in Magenta/Rosa/Weiß. Verbindliches Referenzasset `FILTERBANK_UI_REFERENCE/ORIGINAL_FILTER_BINDEND.png`. Das Icon bleibt bei beliebiger realer Filteranzahl bei fünf Balken.
- Filter-MIN/MAX getrennt vom Original: 0 ≤ MIN < MAX ≤ 200 Hz; **2–40** Filter frei, Standard **10**, 20 verfügbar; lineare nominale Bandaufteilung. Bandpassüberlappung und physikalische Auslaufzeiten bedeuten: nicht ideal steil begrenzte, unabhängige Frequenzkästchen.
- ORIGINAL und FILTER unabhängig EIN/AUS, beide EIN transientabhängige Farbmischung; FILTER allein digitale Band-RMS-Pegel in **dBFS**, keine dB SPL. ORIGINAL allein bisheriges Bild. Beide AUS zeigt dunkles Bild; Messung läuft weiter.
- Vorhandene Zeitachse, Frequenzplotbreite, Inferno-Colorbar, Bildexport und Setup-Slots erhalten. Die FILTER-Alleinansicht nutzt Magenta–Rosa–Weiß statt Inferno bei derselben einstellbaren dBFS-Skalierung; bei Mischung ist Magenta-Overlay ein relativer **dB-Anstieg** und die originale Colorbar bezieht sich weiterhin nur auf SLOW. Die verbale Overlay-Legende ist ein Entwurfs-/Prüfpunkt.

## Tatsächlich implementiert

- `FastFilterBank` im JVM-Kern als Experiment: ein zweiter Ordnung IIR-Bandpass pro Band bei 8 kHz, 5-ms-Blockaktualisierung; frequenzabhängige schnelle Leistungshüllkurve, etwa 5-s-Hintergrund, 10-s-Anlauf, dBFS und relativer Überschuss. Filterlänge und Reaktionszeit hängen von Bandmitte/Schmalbandigkeit ab. Vorläufige Koeffizienten und Schwellen sind **nicht kalibriert/validiert**.
- Bereits bestehender `AnalyzerRawLfSession`-Konsument zweigt Roh-PCM vor der Dezimierung ab, ohne weiteren `AudioRecord`. Transienten-Maxima werden bis zur nächsten vorhandenen SLOW-Wasserfallzeile gepuffert; Zeilen erhalten gleiche Historien-/Zeitpositionen. **Einschränkung:** erste sichtbare FAST-Zeile setzt SLOW-FFT-Warm-up voraus; Bildzeilenintervall ist weiterhin das bisherige Wasserfallintervall. Damit ist dieses Release **noch keine nachgewiesene Millisekunden-Impulsanzeige**.
- Gemeinsamer Bitmap-Farbvertrag für Bildschirm und PNG/JPG. Zwei kompakte unabhängige Actionbuttons am Kamerabutton. Filterkonfiguration unter Einstellungen → Filterbank, Umkonfiguration nur nach STOP; EIN/AUS während Messung möglich. Setup 1–3 speichert/minimiert entsprechende zusätzliche Schlüssel, alte Setups laden mit konservativen Defaults.
- Direkt-Analyzer: zusätzliches 5-ms-Transformintervall und wählbarer Glättungs-Zeitkonstantenmodus mit bestehendem Feld in Sekunden (5 s möglich). α bleibt alternativer Bestandsmodus. Test nach Android-Build nötig.

## Nicht implementiert / offen (LFN-IMP-001 v1.0)

- Eigener frühzeitiger Rohtrigger mit Onset-Schätzung, Ereignisfusion, Entkopplung echten Folgeimpulses von Ringing, Ereignislog/-export, Qualitätsflags, Synchronisations- und Latenzmetrik.
- Unabhängige 5-ms-Ausgabe *im sichtbaren Wasserfall*: tatsächliche Zeilen entstehen weiterhin gemäß SLOW-FFT und Benutzer-Wasserfallintervall. Ebenso getrennte FAST-/SLOW-Datenpipelines in konkurrierenden Sampleraten und zuverlässige quantitative Laufzeitkorrektur.
- Hardwarekalibrierung, garantierte Bandgrenzen, akustische dB-SPL-Werte, echte Geräte-Impulsgenauigkeit; PeakdBFS in schmalen Filtern ist wegen Filterübertragungsfunktion und Integration nicht direkt mit Vollband-Peak gleichsetzbar.
- Sämtliche noch nicht ausgeführten IMP-TEST-01…35; alter 16:1-Pegelverlust offen, praktische Supabase-Restore-Probe, Radiushistogramme und Mithören zurückgestellt.

## Vorrangregel / Dokumentkompatibilität

LFN-IMP-001 v1.0 bleibt als unimplementierte *Voll-Spezifikation* mit 5 experimentellen Bändern erhalten. Die spätere verbindliche Nutzerentscheidung **2–40 mit Standard 10, eigene MIN/MAX, zwei 48-dp-Buttons** ersetzt ausschließlich deren fünfbandigen Startplan und nicht die weitergehenden Validierungsgates. Noch keine zuverlässige Trigger-Engine behaupten.
