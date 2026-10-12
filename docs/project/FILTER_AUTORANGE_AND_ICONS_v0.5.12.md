# LFN-Scout – ORIGINAL-Icon und unabhängige FILTER-Pegelautomatik (v0.5.12)

**Status:** Quellcode implementiert; Host- und statische Prüfungen separat durchgeführt. Android/Compose-Build und Xiaomi **NOT RUN**.

## 1. Verbindliche Symbole

- ORIGINAL: **Exakt die vom Nutzer am 24.09.2026 hochgeladene PNG-Datei** `Screenshot 2026-09-24 173005.png`, unverändert als Binärasset unter `app/src/main/res/drawable-nodpi/original_waterfall_icon.png`; Dokumentkopie `docs/project/FILTERBANK_UI_REFERENCE/ORIGINAL_ICON_NUTZER_2026-09-24.png`. Nicht durch einen generierten Spektrogramm-Gradienten ersetzen. Der alte Referenzentwurf `ORIGINAL_FILTER_BINDEND.png` ist für das ORIGINAL-Icon überholt.
- FILTER: unverändert die **fünf** symmetrischen Pegelbalken kurz–mittel–hoch–mittel–kurz, Magenta/Rosa/Weiß; Symbolzahl ist von der tatsächlichen Filterzahl 2–40 unabhängig.
- Beide jeweils **48 dp**, wie Kamerabutton; bei AUS weiterhin rotes Kreuz als Bedienzustand.

## 2. Interaktion

| Eingabe | Wirkung |
|---|---|
| ORIGINAL kurz | ORIGINAL EIN/AUS |
| ORIGINAL lang | Keine Sonderfunktion, keine Pegeländerung |
| FILTER kurz | FILTER EIN/AUS |
| FILTER lang | FILTER-Autorange starten, sofern 8-kHz-Analyzer aktiv, nicht pausiert und gültiges Signal |

Die Audioaufnahme, die ursprünglichen FFT-Einstellungen, die STOP-/Rolling-Zustandsmaschine und das Speichern von Bildern werden durch diese Tasten **nicht** geändert. Die FILTER-Autorange ist unabhängig von ORIGINAL-Autorange.

## 3. FILTER-Pegelautomatik

- Unter Einstellungen → Filterbank: FILTER-Anzeige MIN/MAX in dBFS (vorgabe −120…−60); separate Erfassungsdauer 1–30 s (Standard 2 s), separate Reserve 0–30 dB (Standard ±5 dB). Die manuelle Einstellung und die Automatik ändern ausschließlich die FILTER-LUT-Normierung und eigene FILTER-Colorbar.
- Lange FILTER-Betätigung: `FilterDisplayRange` sammelt die **tatsächlichen Band-RMS-Werte** aus dem bestehenden Filterbank-Callback, nicht aus dem SLOW-FFT-Spektrum. Numerischen −240-dBFS-Silence-Sentinel und nicht-finite Werte ignorieren. Für den unteren Wert 10%-Band- und Zeit-Quantile, für den oberen Wert den gültigen kurzzeitigen Maximalwert verwenden; zuzüglich gewählter Reserve; mindestens 10 dB Anzeigespanne, Grenzen −240…+50 dBFS.
- Ergebnis nach Ablauf der Erfassung **festhalten und persistent speichern**; kein kontinuierliches Nachregeln. Keine gültigen Bandwerte → Skalierung unverändert und Statusmeldung. STOP, PAUSE und Session-Reset brechen den laufenden Vorgang ab. Zeitunterbrechung ohne neue Filterdaten führt nach Timeout zum Abbruch.
- FILTER allein: eigene Magenta-Rosa-Weiß-Farbrampe mit eigener beschrifteter dBFS-Skala. ORIGINAL allein: bisherige Inferno-ähnliche Skalierung. Beide EIN: Grundbild und Colorbar beziehen sich weiterhin auf ORIGINAL; relative Filter-Ereignisse werden mit bisheriger dynamischer Mischung markiert und anhand der **separaten** FILTER-Normierung eingefärbt. Gleiche Skalenwerte/Frequenzpositionen im Live-Bild und Bitmap-PNG/JPG.
- Setup-Slots 1–3 speichern/laden FILTER dBFS MIN/MAX, Dauer und Reserve. Ein separater 8-kHz-Modus-Profilsatz führt diese Felder ebenfalls mit. Alte Setups erhalten sichere Defaultwerte. Setup 4/Direkt-Analyzer bleibt ohne Filterbankfunktion.

## 4. Grenzen und Rückstellungen

- FILTER-Pegel sind digitale, unkalibrierte Band-RMS-Pegel; keine dB SPL. Kurze Einzelspitzen können die Auto-Obergrenze vergrößern; die Skala ist keine automatische Kalibrierung. Die alten Filterkoeffizienten und der 5-ms-Verarbeitungsschritt werden nicht verändert.
- Optionales Antippen der FILTER-Colorbar ist **in v0.5.12 nicht implementiert**; Long-Press FILTER ist die tatsächlich verdrahtete Auslösung.
- Eigenständiger FAST-Onset-Trigger, Ringing-Fusion, 5-ms-Zeilenanzeige, akustische Validierung, 16:1-Pegelverlust und Supabase-Restore bleiben offen.
- Besondere Validierung: Last mit 2–40 Bändern, Mikrofonwechsel, Filter-Autorange bei kurzen Impulsen, Bildschirm- und Export-Colorbar, bereits laufende Aufnahme, langer Panelwechsel, Setup-Roundtrip, tatsächliche Android-Kompilierung.
