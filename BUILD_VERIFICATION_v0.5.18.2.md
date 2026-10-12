# Build-/Testnachweis v0.5.18.2 – automatischer Vortest, ACTIVE/BASE und ADB

**Stand:** 25.09.2026  
**Android-Version:** 0.5.18.2 / 163  
**Basis:** v0.5.18.1; keine beabsichtigte App-Laufzeitänderung außer Versionskennung und Windows-Entwicklungs-/Übergabewerkzeugen.

| Prüfung | Ergebnis | Aussage |
|---|---|---|
| `python tools/verify_v05182_delivery.py` | **PASS 24/24** | statischer Vertrag für Version, Buildreihenfolge, ACTIVE-Paket und ADB-Gates |
| Erzeugtes BASE R1 mit ZIP-Test | **PASS** | 133 Quelldateien im BASE; ZIP vollständig lesbar; SHA-256 `3f662bd281689c67c00f7fb1720dbef38a3252d22b8a9e18eeed85071ed95d08` |
| ACTIVE-Paketlogik / Testpaket mit vollständigem ZIP-Test | **PASS** | 625 Einträge im Testpaket; buildkritische Quelldateien vorhanden, dokumentierte BASE-Pfade ausgeschlossen; finaler Lieferhash wird außerhalb dieses Dokuments angegeben |
| Windows `VORTEST.bat` | **NOT RUN** | Windows/PowerShell/Android-SDK in Erstellungsumgebung nicht verfügbar |
| `:app:testDebugUnitTest` | **NOT RUN** | realer Windows-Lauf ausstehend |
| `assembleDebug` | **NOT RUN** | reale APK ausstehend |
| `VORTEST_INSTALL.bat` / ADB | **NOT RUN** | echtes Windows/USB-Gerät erforderlich |
| Xiaomi-Regressionsprüfung | **NOT RUN** | offene v0.5.18-Funktionstests bleiben bestehen |

## Neue Automatisierung

- Standard-`VORTEST.bat`: PREPARE → Unit-Tests → assembleDebug → APK-Prüfung → ACTIVE-ZIP + SHA-256.
- `VORTEST_INSTALL.bat` bzw. `VORTEST.bat --install`: erst nach vollständigem PASS zusätzlich ADB-Prüfung und `adb install -r`.
- Kein automatisches Installieren bei 0, mehreren oder nicht autorisierten Geräten.
- `LFN-Scout_BASE_ASSETS_R1.zip` wird nicht bei jedem Build neu erzeugt. `BASE_PACKAGE_REQUIRED.txt` bindet ACTIVE an Revision und SHA-256.

**Keine Freigabebehauptung:** Die statischen/ZIP-Prüfungen ersetzen keinen echten Gradle-/ADB-/Xiaomi-Lauf. Die PowerShell-Paketierungsroutine selbst wurde in der Linux-Erstellungsumgebung nicht ausgeführt; das Liefer-ZIP wird mit denselben Ausschlussregeln separat erzeugt und per ZIP-Test geprüft.
