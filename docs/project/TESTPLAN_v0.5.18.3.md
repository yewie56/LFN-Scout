# Testplan LFN-Scout v0.5.18.3 – korrigierter Windows-Vortest

## Ziel

v0.5.18.3 korrigiert ausschließlich die Windows-Vortest-/ADB-Automatisierung aus v0.5.18.2. Keine beabsichtigte Laufzeitänderung an Android-App, Audio, Statistik, Analyzer, UI oder Supabase.

## A. Regression des gemeldeten Java-17-Fehlers

1. `LFN-Scout_ACTIVE_v0.5.18.3.zip` in einen neuen Ordner entpacken.
2. `VORTEST_INSTALL.bat` starten.
3. `PREPARE_PROJECT.bat` darf die normale Ausgabe von `java -version` auf STDERR anzeigen, ohne dass PowerShell daraus einen `NativeCommandError` als Vortestfehler macht.
4. Entscheidend ist ausschließlich der reale Prozess-Exitcode. Bei Exitcode 0 muss der Ablauf zu den Unit-Tests weiterlaufen.
5. Im Log muss `PASS: PREPARE_PROJECT` erscheinen.

## B. Vollständiger Vortest

Nach erfolgreichem PREPARE müssen strikt nacheinander laufen:

1. `gradlew.bat :app:testDebugUnitTest --stacktrace`
2. `gradlew.bat assembleDebug --stacktrace`
3. Prüfung von `app/build/outputs/apk/debug/app-debug.apk`
4. Kopie als `Builds/LFN-Scout_v0.5.18.3-debug.apk`
5. Erzeugung und Prüfung von `Builds/LFN-Scout_ACTIVE_v0.5.18.3.zip`
6. Erzeugung der SHA-256-Datei und `LAST_VORTEST_RESULT.txt`

Bei einem nicht-null Exitcode muss der Ablauf sofort mit dem dafür vorgesehenen Fehlercode stoppen.

## C. ADB-Installation

1. USB-Debugging am Xiaomi aktivieren und PC autorisieren.
2. Genau ein ADB-Gerät anschließen.
3. `VORTEST_INSTALL.bat` ausführen.
4. Nach vollständigem Build-PASS muss `adb install -r` auf genau dieses Gerät ausgeführt werden.
5. Bei `unauthorized`, 0 oder mehr als einem freigegebenen Gerät darf nicht installiert werden.
6. Bei erfolgreicher Installation muss `LAST_VORTEST_RESULT.txt` `Install: PASS` enthalten.

## D. App-Regression

Da v0.5.18.3 keine beabsichtigte App-Laufzeitänderung enthält, nach Installation mindestens prüfen:

- App startet und zeigt Version 0.5.18.3.
- vorhandene Einstellungen/Setups bleiben erhalten,
- Portraitmodus weiterhin aktiv,
- Analyzer lässt sich öffnen und läuft,
- die offenen v0.5.18-Audio-/Statistiktests bleiben separat abzuarbeiten.

## Initialstatus

- Quell-/Skriptprüfung in `BUILD_VERIFICATION_v0.5.18.3.md`: statisch geprüft.
- Reale Windows-PowerShell-/Gradle-Ausführung: NOT RUN, bis Nutzerlog vorliegt.
- Reale ADB-Installation: NOT RUN, bis Nutzerlauf vorliegt.
- Xiaomi-App-Regression: NOT RUN.
