# Algorithmen, Kalibrierung und Signalqualität – LFN-Scout Analyzer

**ID:** LFN-AN-ALG · **Version:** 0.7 · **Status:** Berechnungsentwurf mit Validierungspflicht.  
**Leitregel:** Kein Filter, Gain, Limiter oder referenzbasierter Abzug des **Mithör-/Anzeigezweigs** darf Original-PCM oder die physikalisch interpretierbaren Messwerte verändern.

## 1. Eingangssignal und Clip-Erkennung

Aufnahme über Live-API (`AudioRecord` kann der Android-Klassenname sein), intern als `x[n]` in −1…+1 normiert, mit echter Rate, Route und monotoniem Zeitstempel. Bevorzugt `UNPROCESSED` falls verfügbar, andernfalls sichtbarer und prüfbarer Fallback. `INPUT_CLIP`: Sättigung/mehrfacher Grenzwertkontakt oder konfigurierbares Near-Clip-Signal (Default-Vorschlag −1 dBFS); pro Frame und pro zeitliche Statistik als Datenqualitätsmaske. Schwellenberührung allein beweist keinen Hardwareclip; Android-Vorverarbeitung kann unsichtbar schon früher verzerren. Eingangsgain **nie** vom Mithörlimiter zurückregeln.

## 2. Analyseauflösung und FFT-Normierung

Aktive Sample Rate `F_s`, Decimationsfaktor `D_eff` gemäß **realer** Pipeline; nach wirksamem Anti-Alias-Filter `F'_s=F_s/D_eff`. Für `N` FFT-Punkte: `Δf = F'_s/N`; zeitliche Fensterlänge `T_w=N/F'_s`. Bei beispielhafter Halbierung je Decimation, 48 kHz, 3 Stufen, `N=8192`: `F'_s=6000 Hz`, `Δf≈0,7324 Hz`, `T_w≈1,365 s`. **Nur Beispiel**, nicht ungeprüft auf Spectroid-Implementierung/Android übertragen. 100-ms-Transformintervall beschreibt nur den Abstand der Aktualisierung, nicht die 1,365-s-Fensterlänge. 2 Hz besitzt hier wenige eigenständige Bins; reale Hardware-Hochpässe können den Bereich zusätzlich begrenzen.

Blackman-Harris (Default), Hann/Rechteck optional; kohärenten Gain und äquivalente Rauschbandbreite des gewählten Fensters dokumentieren. Vor der Decimation Anti-Alias-Filter und ggf. DC-Subtraktion. Nichts unterhalb der kalibrierten Geräte-Untergrenze als absoluten Schalldruck deuten.

## 3. Einheiten und Spektral-PSD

Für FFT `X[k]=FFT(w[n]x[n])` und `W2=Σw[n]^2` ist die **einseitige** Leistungsdichteschätzung für interne reelle Samples (ausgenommen DC/Nyquist) z. B.:

`PSD[k] = 2|X[k]|² / (F'_s × W2)`  [normierte Digitalamplitude²/Hz].

Für DC und gegebenenfalls Nyquist fehlt der Faktor 2. Fenster-/Filterbandkorrektur muss konsistent in Kalibrierung, Referenzdatei, Band-RMS und CSV angewendet werden. `P_band≈Σ PSD[k] Δf`, optional mit korrekter Teilbin-Integration, `rms_band=sqrt(P_band)` und `L_band_dBFS = 20 log10(rms_band/refRms)` für eine *dokumentierte* Referenz (z. B. Full-scale-Sinus-RMS oder Full-scale-Amplitude). **Es darf nicht die FFT-Amplituden-Kurvenhöhe mit einem integrierten Band-RMS-Pegel gleichgesetzt werden.** Für tonale Amplituden-Readouts kann eine getrennte kalibrierte Amplitudennormierung verwendet werden; die gewählte Größe muss im Readout/Export erkennbar sein.

## 4. Gelb Live, Rot Average und Max Hold

- Gelb zeigt aus einem aktuellen Analyseframe gewonnene Spektralkurve; `Smoothing=0,3` ist ein separates exponentielles Anzeige-Glätten mit ausdrücklich dokumentierter Richtung (`y_t = α new + (1−α) old`, wenn α so definiert wird). Kein dB-Arithmetik-Mix ohne Begründung.
- Rot: unabhängig zuschaltbare **zeitgewichtete lineare Leistungs-/PSD-Mittelung** über vorgeschlagen 120 s, anschließend Umrechnung in passende dB-Anzeige; Fensterfüllgrad und Reset.
- Max Hold: unabhängiges Maximum pro Bin seit letztem Reset, spektral kompatible Definition, bei Konfigurationswechsel invalidieren. Peak Marker ist eine eigene, schaltbare Frequenz-/Pegelbeschriftung.
- Sample-Peak/Superfast ist **kein** Ersatz für Spektral-/RMS-Mittelwert.

## 5. L50/L90 im unabhängigen Statistikband

1. Aus **unkompensierten**, ggf. gültig mikrofonkalibrierten Band-PSD-Werten pro gültigem Analyseintervall die Bandenergie/RMS zwischen Standard `2–25 Hz` bilden; `20–80 Hz`-Mithörband spielt keine Rolle.
2. Zeitgewichtete Pegelproben über rollende Default-`120 s` aufnehmen; Audio-/FFT-Frames können überlappen. Verfügbare Statistik ist eine **Zeitverteilung**, keine unbegründete Anzahl unabhängiger FFT-Frames.
3. Definiere Exceedance-Quantil: `L50 = Q_0.50(L(t))`, `L90 = Q_0.10(L(t))` bei aufsteigendem Quantil. Für vollständigen gültigen Datensatz gilt `L50 ≥ L90`.
4. Ungültige Eingangsframes, Clipping und fehlende kalibrierte Bandteile maske/kennzeichne; bei unzureichender gültiger Zeitspanne Status `FÜLLT SICH` bzw. `UNGÜLTIG`, nicht als vollständige Statistik darstellen.
5. Zwei horizontale kurze Linien liegen beim errechneten **Pegel auf der Y-Achse**, aber nur über dem im Plot sichtbaren Statistikfrequenzabschnitt. Zusätzlich Zahlen mit Band, Fenster, Einheit, Gültigkeitsgrad. Weder pro-Bin-L50 noch arithmetisches Mittel aus dB-Frequenzbins.

**Absolute SPL-Freigabe:** nur nach validierter kalibrierter Gesamtmesskette *über das gesamte verwendete Statistikband* und nachgewiesener Band-RMS-Normierung. Sonst als relative Bandgröße mit sauberer Definition anzeigen oder, wenn nicht belastbar, als ungültig markieren. Ohne Kalibrierung nicht still ein unkalibriertes FFT-dBFS als dB SPL ausgeben.

## 6. Superfast und Maximum Sample

Für jeden erfassten Rohblock `p_block=max(|x[n]|)`; historisches `p_max=max(p_max,p_block)`, Zeitstempel der verursachenden Sampleposition (Index/F_s) festhalten; in dBFS `20 log10(max(p_max,eps))`. 20-ms-Aggregation betrifft **nur die Anzeige**, nicht die vollständige Peak-Suche. Reset setzt historische Maximum-Felder zurück und beeinflusst keine FFT-/L50-/Mithörwerte. Warnung bei `INPUT_CLIP` separat. Keine automatische Behauptung „akustischer Impulspegel“, `True Peak`, Gehörbelastung oder dB SPL.

## 7. Mikrofonkalibrierung

Eigenständige versionierte JSON-/CSV-Datei mit Stützstellen `(f, C(f) dB)`, Vorzeichenkonvention `L_corrected = L_raw + C(f)`, Geräte-/Quellen-/Ratebindung, Gültigkeitsband, optionaler absoluter RMS-Bezug. Interpolation im deklarierten gültigen Band (Methode dokumentieren), **keine stille Extrapolation**. Bei unbekannter Route oder inkompatibler Rate Profil ungültig und Roh-dBFS bleibt verfügbar.

Frequenzgangkorrektur allein erzeugt **relativ korrigierte Pegel**, kein dB SPL. Absolute SPL-Ausgabe erfordert nachgewiesene Kombination aus Übertragungsfunktion, Referenz (z. B. zertifizierter geeigneter Kalibrator in validiertem Band), zeitlicher Gültigkeit, Messgröße und zugehöriger Analysekette. Referenzpunkt bei 1 kHz allein belegt nicht die 2-Hz-Empfindlichkeit. Beispiel unter `schemas/microphone_calibration_example.json` enthält **keine reale Kalibrierung**.

## 8. Rauschbodenreferenz und optionale lineare Subtraktion

1. Referenz über z. B. 120 s als robuste, definierte **lineare PSD** (nicht dB-Mittel) messen; ID, UTC, Geräte-/Route-/Rate-/Fenster-/FFT-/Decimation-/Kalibrierkontext, PSD-Skalierung, Bin-Gitter und Umgebungsart speichern.
2. Vor Anwendung sämtlichen Kontext vergleichen; nur mathematisch nachgewiesene Umrechnung/Interpolation zulassen. Inkompatible Dateien nicht aktivieren.
3. `P_res(f)=P_current(f)-P_noise(f)` in kompatiblen **linearen** Einheiten; falls `P_res≤0` oder innerhalb der Unsicherheitsgrenze, bin `unbestimmbar` markieren statt einen scheinpräzisen negativen/−∞-dB-Wert zu erzeugen. Optional `max(0,...)` intern, aber niemals als vollwertige Messzahl behandeln.
4. Graue Referenz und Kompensation sind getrennte Schalter; wahlweise Anzeige nur Spektrum oder auch Wasserfall. Roh-/Original-Average, L50/L90 und Mithören bleiben erhalten. Ist eine rote Anzeige explizit „kompensiert“, muss eine eigene Kennzeichnung/Kurvenlogik definiert werden; Default bleibt **Original-Average**.
5. Raumhintergrund ≠ Eigenrauschen des Mikrofons; stationärer, näherungsweise unkorrelierter Hintergrund wird vorausgesetzt. Keine allgemeine Quellenunterdrückung oder Rekonstruktion realer Wellenform behaupten.

## 9. Mithör-Bandpass und Sicherheitsregelung (nur Mithörzweig)

Separate Route: PCM → stabiler dynamischer Bandpass (`f_min<f_max`; Standard 20–80 Hz) → User-Gain (0 dB Default) → Auto-Absenkung → harter PCM-Limiter/Mute → Kopfhörer. Frequenzband im Spectrum-Setup als **Breite in Hz** änderbar; X-Drag verschiebt beide Grenzen unter Beibehaltung der Breite; fMin/fMax sind außerdem numerisch editierbar. Neue Filterkoeffizienten atomar und ggf. glatt umschalten, kein hörbarer Sprung durch unkontrollierten internen Filterzustand.

`peak_postGain` steuert Auto-Gain: Zielpegel z. B. −6 dBFS, schnelle Absenkung, sanfte Freigabe (vorgeschlagen 3 s), niemals über User-Gain zurück anheben. Ein harter interner Limiter bleibt bei abgeschaltetem Auto-Modus. `MONITOR_CLIP_RISK` beim Annähern, `MONITOR_CLIP` bei tatsächlicher numerischer Übersteuerung; effektive Absenkung im Hauptbild. `INPUT_CLIP` stammt aus dem *unveränderten* Eingang und ist **keine** Regelgröße für den Ausgang.

**Schutzgrenze:** Ein digitaler Headroom/Limiter kann akustische Kopfhörerlautstärke nicht garantieren. Erst nach sicherer Route, niedriger Startlautstärke und explizitem Tap starten; Kopfhörerabzug/unklare Route/Fehler sofort Mute und AUS. Infraschall kann trotz digitaler Verstärkung nicht hörbar sein.

## 10. Wasserfall, Pause und automatische PNGs

- Wasserfall speichert flüchtige Spektralzeilen in begrenztem RAM; Farbpalette `Inferno` enthalten. Farbe = zeit-/frequenzbezogene Pegelklasse, keine Kalibriervorschrift.
- Sichtbare Historie `T_wf` (Default 120 s). Nach `Save aktiv` erste PNG **nur nach vollständiger Füllung** ab Aktivierungszeitpunkt; danach ein Bild je weiterer kompletter `T_wf`, unabhängig von UI-Renderframes. Eindeutige Zeit-/Setup-ID; Metadaten enthalten Einheit/Cal/Noise-/Compensationstatus und Clipflags. CSV bleibt ausschließlich manuell.
- **Pause:** Anzeige-Snapshot einfrieren; Cursor über frozen Spektrum bedienbar. Autosave wird ausgesetzt, damit keine identischen eingefrorenen Bilder regelmäßig entstehen; nach Fortsetzen neuer kompletter Zyklus. Mithörzweig darf unabhängig weiterlaufen und Sicherheitswarnungen anzeigen. Ein Wechsel von `T_wf` startet die PNG-Zählung neu.
- `Save aktiv` nach App-Neustart/Recall Standard AUS; Konfiguration kann Wunsch speichern, aber nicht unbemerkt Dateien erzeugen.

## 11. Mathematische Negativ- und Referenztests

**R-FFT:** Sinus bei 10, 47, 50 und 100 Hz; Fehler ≤ 1 effektives Bin, amplitudennormierter Frequenzgang gegen Python/NumPy-Fixtures.  
**R-STAT:** konstante Bandpegel => L50=L90; 90 % bei −80 und 10 % bei −60 => L90≈−80, L50≈−80; 50/50 => L50 nahe Median nach dokumentierter Interpolation.  
**R-NOISE:** `P_current=P_noise` => unbestimmbar; `P_current=2P_noise` => Rest `P_noise`; `−60 dB − (−70 dB)` darf **niemals** als `+10 dB` interpretiert werden.  
**R-MON:** identische PCM-Fixtures bei Gain 0/+24 und automatischer Absenkung: gleiche FFT/L50/L90/CSV-Rohwerte; Output begrenzt.  
**R-SAVE:** 120 s Wasserfall, Start t=0 => Save t≈120/240/360 s, nicht 0 oder je Frame; Pause verhindert Wiederholungsbilder.  
**R-CAL:** Profil `C(50 Hz)=+6 dB` => relative dB-Korrektur +6 innerhalb Gültigkeit, keine SPL-Anzeige ohne absolute Referenz.

**Status:** Diese Formeln sind Entwurfsgrundlagen; numerische Toleranzen/Rate und Referenzgerätekalibrierung müssen in der Implementierung festgelegt und nachgewiesen werden.


## 12. v0.6: gemeinsamer PCM-Zeitbezug, Histogramm-Snapshots und gerendertes Save

### 12.1 Capture-Integrität und parallele bestehende Datei-Aufnahme

- Zeitachse: `SystemClock.elapsedRealtimeNanos()` bzw. effektiv zugeordnete Audio-Timestamps; Samplefolge und Ausfälle mit `sequence`, `frameCount`, `sampleRateHz` und `validFlags` erfassen. UTC ist nur zusätzlich für Dateien/Anzeige.
- Bei gleichzeitiger Aufnahme bekommen Analyzer und Recorder dieselbe Quelle (und spezifiziert dieselbe ursprüngliche Audiosequenz). Zusätzliche Anti-Alias-Filter oder Downsampling werden auf **privaten abgeleiteten Kopien** je Consumer ausgeführt; keine Rückwirkung auf Recorder-PCM.
- Recorder-/Analyzer-Synchronität gilt nur innerhalb dokumentierter Toleranz und unter tatsächlich verfügbarem Timestamp-API. Lücken markiert statt künstlich mit validen Samples gefüllt; keine Überbrückung von `INPUT_CLIP` durch Monitor-AGC.
- Wenn laufende Recorder-Rate nicht zur gewählten Analyzer-Konfiguration passt, folgt nur der Analyzer-Pfad einer **getesteten** Rate-Konvertierung; wenn nicht möglich, verweigert der Analyzer die neue Auswahl. Keine unbeabsichtigte Recorder-Neuinitialisierung.

### 12.2 Zeitgewichteter Wasserfallzyklus

`T_wf` ist der konfigurierte **sichtbare Zeitumfang des Wasserfalls**, z. B. 120 s. Auto-PNG folgt gültigen monotonic-Intervallen im **internen Analyzer-State**, nicht der Zeit seit dem letzten gezeichneten UI-Frame oder dem Ende eines Dateischreibvorgangs. Bei 120 s erwartet: t=0 → 0 Bilder, t=120 → 1, t=240 → 2. Bei gewollter Anzeigepause zunächst 0 neue Bilder und nach Resume erneute komplette Periode. Ein interner Panelwechsel ändert diese Zähler nicht.

Zeit-/Blockfehler: Lücke > erlaubter Toleranz führt zu `INCOMPLETE_CYCLE` und definierter Neufüllung oder einem ausdrücklich als unvollständig markierten manuellen Snapshot; kein automatisches „voll“ durch Einfüllen identischer Zeilen. Genaue Toleranz bis Realdaten-Test konfigurierbarer VORSCHLAG.

### 12.3 Histogramm-Overlay ist Datenkopie, keine akustische Transformation

Der Histogramm-Publisher liefert `snapshotId`, Erzeugungszeit, ggf. gültiges Zeitintervall, Bin-Kanten, Werte, Einheit, Kategorie/Raum-/Ortsbezug sofern bereits vorhanden, Qualitätsflag und Quellenrevision. Der Analyzer rendert nur diese vorhandenen Daten; keine Ableitung neuer Radiusformeln. Im PNG wird die zum `captureIntervalEnd` zuletzt vollständige, vorhandene Auswahl mit den tatsächlichen Quellenzeiten eingefroren. Historische Histogramme sind nicht automatisch zeitgleich zum akustischen Wasserfall: **Altersangabe/Status sichtbar**.

### 12.4 Lastbudget und Speicherstrategie

Der Capture-Callback arbeitet in einer eigenen priorisierten Schleife; Fan-out/FFT/Histogramm-Repository und PNG-Renderer dürfen seine Deadline nicht blockieren. Wasserfallhistory ist ein begrenzter Speicherpuffer, dessen maximale Kosten aus Zeitfenster × Zeilenrate × Frequenzpunkten berechnet und bei Profilwechsel kontrolliert neu zugewiesen werden. Offscreen-Snapshot-Zugriff ist unveränderlich/copy-on-write. Bei hoher Last UI-FPS absenken, optional Miniaturdetail reduzieren; **nie zuerst** die Recorder-Integrität oder Ausgangssicherheit abschalten. Speicherzielfehler -> explizites Save-Fehlerereignis, kein Endlosretry.

## 13. Numerische Sicherungen und getrennte Anzeigen v0.7

- **L50/L90:** Die statistischen Überschreitungspegel werden aus einer Folge zeitgewichteter, physikalisch konsistent aus linearer Bandenergie gebildeter Pegelwerte für das **unabhängige** Statistikband berechnet. `L50` = 50-%-Überschreitungswert und `L90` = 90-%-Überschreitungswert im gleitenden Fenster (Default 2–25 Hz, 120 s). Abbruch/Drop/Eingangsclip erfordert eine definierte Qualitätskennzeichnung; Ungültiges nicht unbemerkt in die Verteilung mischen.
- **Average (rot):** zeitgewichtete lineare Leistung/PSD je Bin (Default 120 s als Entwurfswert), logarithmische Umrechnung erst zur Anzeige. Als Average darf kein dB-Arithmetikmittel ohne explizite Modellentscheidung verwendet werden.
- **Superfast:** `8×` nur nach Definition von Beobachtungsrate/Zeitanzeige; Sample-Max aus erfassten PCM-Werten der tatsächlichen `fs`. Keine „8×“ nachträglich erzeugte neue Information und keine True-Peak-/SPL-Verwechslung.
- **Rauschboden:** Speicherung mitsamt Geräte-/AudioSource-/Rate-/FFT-/Fenster-/Decimation-/Kalibrier- und Einheitssignatur. Eine angeforderte **Leistungs**subtraktion im Darstellungszweig ist nur bei Kompatibilität zulässig; negativer/unsicherer Rest wird markiert. Roh-Messwerte, Bestands-Aufnahme und Mithörzweig bleiben unverändert.
- **Kalibrierung:** Relative Frequenzkorrektur, absolute Referenz und Gültigkeitsband ausdrücklich getrennt. Nur validierte absolute Referenz plus geeignete Hardware berechtigen zur konkreten dB-SPL-Anzeige. Auch eine angezeigte SPL-Zahl außerhalb des Kalibrierbandes muss gesperrt oder gekennzeichnet sein.
- **Offscreen-Snapshot:** Einheiten, Kurvenart, Statistik-Band, Wasserfallzeit und verwendete Histogrammgeneration müssen dieselbe eingefrorene Zeitzuordnung besitzen. Nur gültige Referenzen annotieren. Render-Parameter gehören mit effektiver Audio- und Kalibrier-Signatur zu den Metadaten.



## Entwicklungsnachtrag Phase 2H (19.09.2026)

Die **bereits implementierte** 8000-Hz-Roh-PCM-Verteilung vor Recorder-Gain und 8:1-Mittelung ist unter `../../../ANALYZER_PHASE2_HIGHSPEED.md` im realen Projekt dokumentiert. 48-kHz-Geräteerfassung, Anti-Alias-Decimation 48:1 und physikalisch validierte Impuls-Kalibrierung sind weiterhin **Soll/NOT RUN**. Die SRS-Defaults für den 1000-Hz-FFT-Pfad werden durch die zusätzliche Roh-PCM-Verteilung nicht geändert.
