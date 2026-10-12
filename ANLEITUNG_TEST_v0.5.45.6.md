# LFN-Scout v0.5.45.6 – Installation und Rauschmodell-Test

## 1. Voraussetzungen
- Windows 11 mit Android SDK, USB-Kabel, Entwickleroptionen / USB-Debugging auf dem Android-Gerät.
- Für die elektrische Referenz ist der **1-kΩ-Abschlusswiderstand** an dem tatsächlich verwendeten Eingang vorgesehen. Die erste Referenzmessung (−11,67 dB/Dekade) war eine **Fehlmessung** und ist zu verwerfen. Gültige bisherige Steigung: −19,72 dB/Dekade.
- Eingangsspeisung/BIAS berücksichtigen: nur zulässige, sichere Widerstandsbeschaltung verwenden; Ausgang und Eingang nicht kurzschließen.

## 2. Windows: exakte Installationsschritte
1. Neues vollständiges ZIP nach `C:\LFN5456` entpacken. `C:\LFN5456\VORTEST_INSTALL.bat` muss existieren.
2. Android-Gerät per USB anschließen, entsperren, USB-Debugging erlauben.
3. `VORTEST_INSTALL.bat` ausführen; evtl. Debugging-Dialog auf Android bestätigen.
4. Datei `Builds\LAST_VORTEST_RESULT.txt` lesen. Nur bei durchgehendem PASS der Schritte PREPARE / UNIT TESTS / ASSEMBLE DEBUG / APK / ACTIVE ZIP / ADB DEVICE / INSTALL fortfahren. Bei FAIL das Protokoll `Builds\VORTEST_*.log` sichern.
5. Kontrollieren, dass die App-Version **0.5.45.6** lautet.

## 3. Analyzer: NOISE-Modi
1. Analyzer öffnen, Messung starten und NOISE zunächst AUS belassen.
2. Ein kurzer Druck auf NOISE schaltet **AUS → MESS → 1/f → AUS**. Die Anzeige wechselt entsprechend.
3. **MESS** subtrahiert die gespeicherten linearen gemittelten FFT-Bin-Leistungen.
4. **1/f** passt das nichtnegative Modell `P(f)=P_white+K/f^alpha` an das gespeicherte Rauschprofil im Bereich **1–100 Hz** an. Außerhalb wird die gemessene Referenz verwendet; kein Frequenzgang-Equalizer für Nutzsignale.
5. Der Modus 1/f ist nur bei gültigem Modell aktivierbar. Bei fehlender/ungeeigneter Referenz erscheint eine Meldung.

## 4. Neue elektrische Referenz einmessen
1. Den vorgesehenen **1-kΩ-Abschluss** am tatsächlich verwendeten Audioeingang herstellen. Prüfen, dass Android nicht auf ein internes Mikrofon umschaltet.
2. Analyzer-Einstellungen → Spektrum → Einmessdauer wählen (zunächst 10 s, für zuverlässigere Steigung 60 s).
3. **NOISE lange drücken**. Fortschrittsanzeige vollständig abwarten. Während der Einmessung keine Pegel-/FFT-/Quellenänderung vornehmen.
4. Mit **DIAG** die Referenz-Steigung in dB/Dekade sowie gültige FFT-Fenster und den Modell-Exponent alpha dokumentieren. Der historische Wert −19,72 dB/Dekade dient nur zum Vergleich; es wird immer das tatsächlich neue Profil modelliert.

## 5. Geräte-Regressionsprüfung
1. Bei 1-kΩ-Abschluss NOISE AUS: Spektrum bei 1, 10, 100 Hz und Leq/L50/L90 dokumentieren (fehlende L-Werte als Fehler markieren).
2. NOISE MESS: Erwartet wird Reduktion des breitbandigen elektrischen Rauschens; Zacken und Floor-Bins sind bei Einzel-FFT möglich.
3. NOISE 1/f: Glattere Referenz-Spektralform erwarten, aber keine garantierte glatte **Momentan-Restkurve**. Leere Bins nicht als gemessenen −180-dBFS-Pegel interpretieren.
4. NOISE AUS: Originalspektrum muss wieder erscheinen.
5. Messquelle ändern oder Aufnahmegerät umschalten: Prüfen, dass eine unpassende Referenz **nicht** als gültige Kalibration ausgegeben wird.
6. DIAG: Weißrausch-Selbsttest (PASS/FAIL) und Referenz-/Modellwerte protokollieren.
7. Optional echten Sinus bei 5, 10, 50, 100 Hz mit konstanten Bedingungen einspeisen: Nutzsignal muss in MESS/1/f sichtbar bleiben, sofern deutlich oberhalb des Rauschbodens.

## 6. Noch nicht verifiziert
- Vollständiger Gradle-Build und Android-Installation in dieser Laufzeitumgebung sind ggf. offen; der Windows-Vortest ist maßgeblich.
- Fehlende L-Werte bei NOISE AUS und doppelte Analyzer-/Rolling-FFT bleiben separate offene Punkte.
- Signal-/Restkurven gleichzeitig anzeigen sowie statistisch korrekte Kennzeichnung Nullrestleistung als „unter Rauschboden“ ist nicht vollständig umgesetzt.
- FAST-Filter bleiben unverändert. Ältere CSV-Filterbank bleibt im Code und in der GUI ausgegraut.
