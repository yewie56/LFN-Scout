# Testplan LFN-Scout v0.5.18.2 – Vortest, ACTIVE-Paket, ADB-Installation

## A. Standard-Vortest und ACTIVE-ZIP

1. v0.5.18.1 bzw. den letzten Rückfallstand separat sichern.
2. `LFN-Scout_ACTIVE_v0.5.18.2.zip` in einen neuen Ordner entpacken. Das BASE-Paket ist für den Build nicht erforderlich.
3. `VORTEST.bat` doppelklicken.
4. Prüfen: PREPARE → Unit-Tests → assembleDebug laufen strikt nacheinander; kein zweiter Schritt bei Fehler des vorigen.
5. Bei PASS müssen unter `Builds/` vorhanden sein:
   - `LFN-Scout_v0.5.18.2-debug.apk`
   - `LFN-Scout_ACTIVE_v0.5.18.2.zip`
   - `LFN-Scout_ACTIVE_v0.5.18.2.zip.sha256`
   - zeitgestempeltes VORTEST-Log
   - `LAST_VORTEST_RESULT.txt`
6. `LAST_VORTEST_RESULT.txt` muss `Status: PASS` enthalten.
7. Das erzeugte ACTIVE-ZIP in einen zweiten leeren Ordner entpacken und dort erneut `VORTEST.bat` starten können. Damit wird geprüft, dass keine ausgelagerten BASE-Dateien zum Build erforderlich sind.
8. Prüfen, dass `local.properties`, `.tools`, `.gradle`, `Builds`, `app/build`, APK/AAB und die BASE-Pfade im ACTIVE-ZIP nicht enthalten sind.
9. SHA-256-Datei gegen das ACTIVE-ZIP prüfen.

## B. BASE-Paket

1. `BASE_PACKAGE_REQUIRED.txt` lesen.
2. SHA-256 von `LFN-Scout_BASE_ASSETS_R1.zip` vergleichen.
3. BASE-ZIP testweise entpacken; `BASE_CONTENT_MANIFEST.json` und die ausgelagerten Pfade müssen vorhanden sein.
4. BASE nicht in öffentliches GitHub hochladen, weil Supabase-Recovery-Daten enthalten sein können.

## C. ADB-Installation

1. Xiaomi per USB anschließen, USB-Debugging aktivieren und Computer auf dem Gerät autorisieren.
2. Nur ein Android-Gerät/Emulator als `device` verbunden lassen.
3. `VORTEST_INSTALL.bat` doppelklicken.
4. Erst nach erfolgreichem Build und ACTIVE-Paket darf `adb install -r` aufgerufen werden.
5. `LAST_VORTEST_RESULT.txt` muss bei Erfolg `Install: PASS` enthalten.
6. App manuell öffnen und Versionsanzeige 0.5.18.2 prüfen.
7. Bestehende Einstellungen/Setups müssen erhalten sein.
8. Negativtest: Gerät abziehen → `VORTEST.bat --install` muss nach erfolgreichem Build mit ADB-Fehler abbrechen, nicht irgendein Ziel auswählen.
9. Negativtest optional: zweites ADB-Gerät/Emulator verbinden → automatische Installation muss verweigert werden.

## D. App-Regressionsprüfung

v0.5.18.2 enthält keine beabsichtigte Laufzeitänderung gegenüber v0.5.18.1. Nach erfolgreicher Installation die noch offenen v0.5.18-Gerätetests weiterführen, insbesondere SAVE AUDIO, PAUSE-Snapshot, Gain-Korrektur, Statistik/40 Filter, Navigation/Portrait und bestehenden Supabase-Beobachter-Upload.

## E. Initialstatus

- Statische Skript-/Paketvertragsprüfung: in `BUILD_VERIFICATION_v0.5.18.2.md` dokumentiert.
- Windows PowerShell/Gradle/Android-Build: **NOT RUN**, bis Nutzerlog vorliegt.
- ADB-Installation: **NOT RUN**, bis Nutzerlauf vorliegt.
- Xiaomi-Funktionsregression: **NOT RUN**, bis Nutzerlauf vorliegt.
