# LFN-Scout v0.5.01 – Analyzer-Bedienung und Bildformat

**Quelle:** tatsächlicher vollständiger v0.5.00-Übergabestand; **neuer Quellstand:** `versionName=0.5.01`, `versionCode=142`, installierbare Package-ID unverändert `de.brummbeobachter.app`. Implementierung am 20.09.2026. Android-Gradle-Build und Xiaomi-Test dieser Version **NICHT DURCHGEFÜHRT**.

## Implementierte Änderungen (Stufe A)

1. Kurzer Tipp auf das einzige Kamerasymbol schaltet die automatische Bildspeicherung. Aus: sichtbares rotes X über der Kamera; Ein: Kamera ohne X. Der gesonderte Text „Auto EIN/AUS“ entfällt. Langer Druck fordert unabhängig vom Automatikzustand genau einen manuellen Offscreen-Bildexport an; der lange Druck löst keinen kurzen Tipp aus. Das Symbol besitzt eine 48-dp-Touchfläche. Kein gültiges Nutzsignal und paralleler Export liefern eine Fehl-/Ablehnungsmeldung.
2. Im Unterpanel Automatiken PNG/JPG und bei JPG Qualität 70/80/90/100 % wählen. Default PNG/90 %. Gemeinsame Auswahl für automatische und manuelle Bilder. Dateiendung, MIME und Bitmap-Komprimierung sind formatgerecht; Ziele ab Android 10: `Download/LFN-Scout/`, ältere Android-Versionen app-spezifischer Pictures/Analyzer-Ordner. Format/Qualität werden in SharedPreferences **und** in drei Setup-Slots gespeichert und wieder geladen. Der 120-s-Autozyklus bleibt unverändert.
3. `Hz` befindet sich bei beiden linearen Frequenzachsen und im Offscreen-Bitmap zwischen numerischen Ticks auf derselben Grundlinie. Die bisherige separate Hz-Zeile entfällt; der für die Achse reservierte untere Innenrand wurde verkleinert.
4. Der Kopf-Rückpfeil **und** Android-Back navigieren aus einer Einstellungskategorie zur Übersicht und erst danach zum Analyzer; der Kategorie-interne Link „Alle Bereiche“ funktioniert weiter.
5. dBFS-Min-/Max-Text ist weiß und kontrastreich auf dunklem Eingabehintergrund; Cyan für Cursor, Auswahl und Fokus. Die beiden ±-Tasten erlauben negative Zahlen auch auf numerischen Tastaturen ohne Minuszeichen. Bei Autorange oder Preset übernimmt das nicht fokussierte Eingabefeld den Controllerwert automatisch; ein gerade bearbeitetes Feld wird dabei nicht überschrieben. Persistenz-/Plausibilitätslogik im Controller ist unverändert.

## Nicht implementiert (Stufen B/C, keine vorweggenommene Freigabe)

- FFT-Hop **5**, FFT Size 2048/8192, freie Frequenz-Min-/Max-Grenzen, weitere Fensterfunktionen, Transformationsintervall, variable exponentielle Mittelung und einstellbare Decimation. Iststand bleibt FFT 4096/Hann/1 kHz; FFT-Hop 10/50/100/200/500/1000; Frequenzobergrenze 20/50/100/200 Hz; Durchschnitt 5 s.
- Spektrum-Max-Hold, Mithören, Superfast-Wasserfall, SPL-Kalibrierung und Radiushistogrammspalte bleiben zurückgestellt.

## Nachweise und Grenzen

`python verify_v0501_analyzer_controls.py`: statischer Quelltextvertrag 20/20 PASS. `python verify_v0500_analyzer_layout.py`: erhaltener Layoutvertrag 20/20 PASS. Kotlin-Host: `analyzer_waterfall498_selftest.kt` 6/6 PASS, `AnalyzerDisplayRangeHostSelfTest.kt` 7/7 PASS. Diese Tests beweisen **weder** die Compose-Kompilierung **noch** eine APK oder neue Xiaomi-Gerätefunktion. Das Android-SDK ist in dieser Laufumgebung nicht vorhanden; `:app:testDebugUnitTest` und `assembleDebug`: NOT RUN. Die v0.5.00-Ergebnisse aus dem vorherigen Xiaomi-Test (Layout, Bild gespeichert, Weiterlaufen bei Panelwechsel) sind *nur* für v0.5.00 nach Nutzerangabe bestätigt.

## Xiaomi-Abnahme v0.5.01

1. Frische Installation über v0.5.00 (kein App-Datenlöschen). Analyzer mit Rolling starten. Diagramme und letzte Hz-Zeile bei 20/50/100/200 Hz prüfen.
2. Kamerasymbol kurz tippen: X AUS/EIN, Automatikstatus persistent; langer Druck mit Auto AUS und EIN erzeugt jeweils **genau eine** Datei; keinen Toggle beim langen Druck.
3. In Automatiken erst PNG, dann JPG (z. B. 80 %) setzen. Manuell und automatisch schreiben lassen; Dateien im Download-Ordner inklusive tatsächlicher Decodierbarkeit/Format prüfen. Mindestens zwei vollständige Autozyklen mit Panelwechsel testen. Setup-Slot speichern/laden und Format/Qualität verifizieren.
4. Einstellungen → Anzeige: negative dBFS-Werte gut sichtbar editieren, übernehmen, Preset laden, Autorange über Spektrumachse ausführen und Min/Max im Einstellpanel (sowie nach App-Neustart) kontrollieren. Während manueller Eingabe keine Überschreibung.
5. Beide Zurückwege (UI-Pfeil und Android-Back) in Detailkategorie → Übersicht → Analyzer testen. WAV-/Rolling-/Bewertungs-/Seismikregression aus `TESTPLAN_v0.5.00_HANDOFF.md` wiederholen.

**Datenschutz:** Das vollständige Projekt-ZIP führt weiterhin private Supabase-Recovery-Dateien und WAV-Backups mit. Nicht ungeprüft öffentlich hochladen. Kein Recovery-Restore durchgeführt.
