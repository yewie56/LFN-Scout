# LFN-Scout v0.5.04 – Verpflichtender Windows-/Xiaomi-Testplan

**Vorbereitung:** v0.5.03-Projekt sichern. v0.5.04 ohne App-Datenlöschung darüber installieren; **vorher bereits abgeschlossene WAVs sichern**. Hardwarebereitstellung und Hintergrund-Mikrofonberechtigungen kontrollieren. Dies ist ein PLAN, keine Erfolgsmeldung.

## A. Build und Integration

- [ ] `PREPARE_PROJECT.bat` im entpackten neuen Projektordner.
- [ ] `gradlew.bat :app:testDebugUnitTest --stacktrace` → tatsächliches Ergebnis erfassen.
- [ ] `gradlew.bat assembleDebug --stacktrace` → keine Kotlin-/Compose-Fehler; resultierende APK für Installation verwenden.
- [ ] Paketidentität `de.brummbeobachter.app`, Version `0.5.04 / 145`, In-Place-Update; Setups 1–3 bleiben erhalten.

## B. Audio-Zustandsmatrix (KRITISCH)

Jeden Fall für **UI-Linkspfeil und Android-Zurück** separat ausführen, insbesondere echte STOP-Übergänge. Nach jedem Fall Rolling-Status und vorhandene bzw. neu angelegte WAV-Dateien dokumentieren.

| Ausgangslage | Wahl beim Start | Nach START | Nach STOP (Panel offen) | Nach Verlassen bei STOP |
|---|---|---|---|---|
| Rolling AUS | JA 8k | Rolling EIN, Analyzer aktiv | Rolling **bleibt EIN** | Rolling AUS |
| Rolling EIN | JA 8k | Rolling EIN, Analyzer aktiv | Rolling **bleibt EIN** | Rolling EIN |
| Rolling AUS | NEIN 48k | Rolling AUS, Analyzer aktiv | Rolling **bleibt AUS** | Rolling AUS |
| Rolling EIN | NEIN 48k | Rolling AUS, Analyzer aktiv | Rolling **bleibt AUS** | Rolling **neu gestartet** |
| Rolling EIN, ungesicherte RAM-Historie | NEIN 48k | alte RAM-Daten dürfen wegfallen | Rolling AUS | neuer leerer Puffer |

- [ ] STOP → START → STOP: keine neue 8k/48k-Abfrage; während STOP keinen Rolling-Umschaltvorgang.
- [ ] Panel verlassen **bei laufendem** Analyzer → Analyzer läuft weiter, Originalzustand **noch nicht** wiederhergestellt; Rückkehr zum Panel ohne neue Moduswahl.
- [ ] Mehrfachwechsel 8k ↔ 48k nur über STOP+Panelverlassen; kein doppelter Mikrofon-Owner / kein gesperrter AudioRecord.
- [ ] 48-kHz-Start scheitert gezielt (Mikrofonberechtigung entzogen oder Quelle blockiert) → Fehlermeldung und Rolling-Restore prüfen.
- [ ] Bildschirm aus, App-Hintergrund, Dienstneustart/Prozessneustart: tatsächlicher Status dokumentieren; keine ungewollte gleichzeitige Mikrofonerfassung.
- [ ] Bereits abgeschlossene lokale WAV bleiben lesbar und unverändert. Ungesicherte Rolling-Puffer dürfen verloren gehen. Eine automatische durchgehende WAV-Diskaufnahme im JA-Modus **NICHT** als PASS abhaken (der Code bietet nur RAM-Rolling).

## C. Breitband 48 kHz

- [ ] Bei erster Wahl NEIN Setup 4 `Analyzer 48 kHz` wird angelegt, Default 48k, Decimation 1, 0–22000 Hz, FFT8192, Hop4800, Wasserfall 120 s.
- [ ] FFT16384 wählen, Δf = 48000/16384 ≈2,93 Hz; Einstellungs- und Bildschirmstatus sowie Frequenzachsen prüfen.
- [ ] Direktquelle liefert tatsächlich 48.000 Samples/s; 50/195/10000-Hz-Test über externen Erreger oder optional über DIGITAL testen; reale Mikrofonkette gesondert kennzeichnen.
- [ ] Setup 4 Name/Beschreibung/FFT/Format/JPG-Qualität speichern, STOP+Verlassen, neu 48k starten: Werte bleiben. LF-Slots 1–3 unverändert.

## D. Digitaler Testgenerator

- [ ] Modus TEST 8k: 50 Hz und optional 195 Hz als digitale PCM-Peaks sichtbar, Amplitudenänderung nachvollziehbar.
- [ ] Modus TEST 48k: beide Frequenzen, 8192/16384, Frequenz/Level in Einstellungen editieren und übernehmen.
- [ ] Aktiver TESTSIGNAL-Banner in App, Bild und Dateiname; automatische sowie manuelle PNG/JPG verifizieren.
- [ ] Rolling aus, keine Audio-/WAV-Aufnahme eines synthetischen Tests. Mikrofonhardware **nicht** durch digitalen DSP-Test als geprüft ansehen.

## E. Wasserfall und Bildexport

- [ ] Im 8k- UND 48k-Modus freie Wasserfallzeiten 120, 600, 3600, 7200 Sekunden eingeben; tatsächliches Zeilenintervall und Zeitachse prüfen.
- [ ] Nach längerer Laufzeit Akt → MW → MAX: ältere Historie bleibt, Umschaltgrenze korrekt, Zeilenmodus nach Umschaltzeitpunkt stimmt; Bildexport enthält Grenze und aktuellen Modus.
- [ ] Langer Kameradruck bei Auto AUS und EIN: jeweils exakt 1 neues Bild, kein Auto-Toggle, **erst nach Erfolg** sichtbare Meldung ca. 2 s; Speicherfehler mit Fehlermeldung.
- [ ] Ganze Auto-Bildzyklen mit Panelwechsel und nach längerer Wasserfallzeit; PNG/JPG-Dateien im Download-Pfad decodierbar.

## F. Regression und offene Fehler

- [ ] Vollständigen `docs/project/TESTPLAN_v0.5.03.md` wiederholen: FIR 4/8/16, Cursor, Stats, Setup-Name/-Beschreibung, Bewertungen, Seismik, WAV und Rolling.
- [ ] Die vom Nutzer gemeldete **16:1-Energieabweichung** bei 50 und 195 Hz gegen 8:1 mit gleichem digitalen Eingangspegel quantitativ erfassen. Erst mit reproduzierbaren Messwerten Ursache/Fix nachweisen; bis dahin **OFFEN**.
- [ ] Die sechs Kotlin-Warnungen aus v0.5.03 separat notieren, falls noch vorhanden. Keine Warnungsfreiheit ohne tatsächlichen Build behaupten.

**Freigaberegel:** Nur die tatsächlich durchgeführten Prüfungen als PASS markieren. Der alte v0.5.02-Gerätetest gilt nicht für die neuen 48-kHz-/Service-Funktionen.
