# LFN-Scout v0.5.18.4 – Live-Vortest, ACTIVE/BASE und ADB-Diagnose

**Stand:** 25.09.2026  
**Version:** 0.5.18.4 / versionCode 165  
**Basis:** v0.5.18.3. Keine beabsichtigte Änderung an Audio-, Statistik-, Analyzer-, UI- oder Supabase-Laufzeitfunktion.

## Anlass

Der reale Nutzerlauf von v0.5.18.3 bestätigte PREPARE, Unit-Tests, `assembleDebug` und ACTIVE-Paketierung. Die Konsolenausgabe zeigte aber, dass die Prozessausgaben in größeren Blöcken statt garantiert zeilenweise live ausgegeben wurden. Außerdem endete `VORTEST_INSTALL.bat` mit Fehlercode 62, weil `adb devices` kein angeschlossenes freigegebenes Gerät meldete; die Abschlussanzeige unterschied den erfolgreichen Build nicht deutlich genug von der fehlenden Installation.

## Änderung v0.5.18.4

`tools/run-vortest.ps1` liest STDOUT und STDERR von nativen Prozessen asynchron zeilenweise. Jede Zeile wird sofort auf der Konsole angezeigt und gleichzeitig an das zeitgestempelte Log angehängt. Der Prozess-Exitcode bleibt das einzige Fehlerkriterium.

Live sichtbar sind:

1. PREPARE inklusive Java-/SDK-/Gradle-Meldungen,
2. Unit-Test-Ausgabe,
3. assembleDebug-Ausgabe,
4. APK-Pfad, Dateigröße und SHA-256,
5. ACTIVE-Paketierung und vollständige ZIP-Prüfung,
6. ACTIVE-Pfad, Dateigröße und SHA-256,
7. ADB-Pfad, Serverstart und `adb devices`,
8. Anzahl freigegebener, nicht autorisierter und offline Geräte,
9. vollständige `adb install -r`-Ausgabe,
10. kompakte Endzusammenfassung mit PASS/FAIL je Schritt.

Bei 0 oder mehr als einem freigegebenen Gerät bleibt die Sicherheitsregel bestehen: keine Installation. Fehlercode 62 zeigt zusätzlich konkrete Hinweise zu USB-Datenkabel, entsperrtem Gerät und USB-Debugging.

## ACTIVE/BASE

ACTIVE wird bei jedem erfolgreichen Build neu erzeugt. BASE bleibt `LFN-Scout_BASE_ASSETS_R1.zip` gemäß `BASE_PACKAGE_REQUIRED.txt`. BASE ist für den normalen Android-Build nicht erforderlich.
