# TESTPLAN v0.5.20

## 1. Windows-Vortest

1. Projekt frisch entpacken.
2. `VORTEST_INSTALL.bat` starten.
3. PREPARE, Unit-Tests, `assembleDebug`, APK und ACTIVE-ZIP müssen PASS melden.
4. Ohne Handy muss der Ablauf sichtbar weiter auf ADB warten.
5. Xiaomi anschließen, entsperren und USB-Debugging bestätigen; erst bei genau einem `device` darf `adb install -r` starten.

## 2. Wasserfall aus gespeicherter Beobachter-WAV

1. Eine vorhandene Aufnahme mit bekanntem Recording-Gain, z. B. +50 dB, in Zusatzfunktionen antippen (`Wasserfall öffnen`).
2. Prüfen: L50/L90 stimmen mit dem zugehörigen `statistics.json` bis auf Anzeige-Rundung überein.
3. Prüfen: eigentlicher Wasserfall ist sichtbar, nicht nur Colorbar/Texte.
4. Prüfen: zeitlich/frequenzabhängige Farbstruktur vorhanden; nicht vollständig einfarbig am unteren Colorbar-Wert.
5. Automatik und manuelle Pegelskala testen; Kopfzeile muss `automatisch` bzw. `manuell` korrekt nennen.

## 3. Analyzer SAVE AUDIO + Kommentar

1. Analyzer laufen lassen, bis mindestens die konfigurierte Save-Dauer gepuffert ist.
2. `SAVE AUDIO` tippen und direkt danach tippen/schreiben.
3. Der Kommentar-Dialog darf erst nach fixiertem Audioausschnitt erscheinen; die nachfolgenden Tippgeräusche dürfen nicht in den gespeicherten Clip gelangen.
4. Ohne Texteingabe Timeout gemäß Beobachter-Audioeinstellung prüfen (Default 60 s).
5. Mit Texteingabe Timeout nach letzter Änderung prüfen (Default 10 s).
6. `Jetzt speichern`, `Ohne Kommentar speichern` und Auto-Timeout jeweils testen.
7. `statistics.json` und Manifest: richtiger `recording_id`, Kommentar bzw. null; WAV/Statistik vollständig.
8. Erfolgsmeldung `Lokal gespeichert` nur als 3-s-Overlay nahe dem Actionbutton, keine neue Displayzeile.
9. Zusatzinfo neben dem Audio-Save-Button muss zweizeilig sein.

## 4. Beobachterpanel kompakt

1. Hauptpanel öffnen: kein separater `Audio / Statistik`-Setup-Button im Audio-Aufzeichnungsblock.
2. Einstellungen → Audioaufnahme: lokale Audio-/Statistik-Konfiguration erreichbar und speicherbar.
3. Zurück zum Beobachterpanel; neue Dauer muss bei der nächsten Aufnahme verwendet werden.
4. Entwicklermodus AUS: keine Kennzeichnung im Titel.
5. Entwicklermodus EIN: `ENTWICKLERMODUS` oben rechts auf Höhe von `LFN-Scout`, keine eigene Zeile.

## 5. Zusatzfunktionen

1. Entwicklermodus AUS: Highspeed-Impulsdiagnose nicht sichtbar.
2. Entwicklermodus EIN: Highspeed-Impulsdiagnose sichtbar und ganz unten nach den regulären Zusatzfunktionen/Info.

## 6. Android-Zurück

1. Hauptpanel → Zusatzfunktionen → Analyzer. Android-Zurück: zurück zu Zusatzfunktionen.
2. Analyzer → Analyzer-Einstellungen. Android-Zurück: zuerst Analyzer-Einstellungen schließen, Analyzer bleibt erhalten.
3. Laufenden Analyzer verlassen und per Zurück/Panelnavigation wechseln: Messung darf nicht allein durch Panelwechsel gestoppt werden.
4. Einstellungen/Seismik/Zusatzfunktionen aus verschiedenen Einstiegswegen prüfen: Android-Zurück führt jeweils zum zuletzt besuchten Panel.
5. Auf Hauptpanel: normales Android-Zurück-Verhalten.

## 7. Regression

- Portrait bleibt erzwungen.
- Kamera-/PNG-Funktion und ORIGINAL/FILTER-Schalter unverändert.
- Beobachter-Audioaufnahme/Cloud-Upload unverändert funktionsfähig.
- Keine neuen Supabase-Schemafelder erforderlich.
- Tagesquote bleibt zwischen Beobachter und Analyzer gemeinsam.
