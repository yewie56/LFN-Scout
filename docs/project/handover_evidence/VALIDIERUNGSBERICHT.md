# LFN-Scout 0.5.40 – Filterbankvalidierung

Erzeugt: 2026-10-11T23:48:52
Konfiguration: 0–100 Hz, 10 und 20 Bänder, Abtastrate 1000 Hz, letzte von 6 simulierten Sekunden (1-s RMS), Toleranz ±0.35 dB.
**Ergebnis: PASS – 330/330 Einzelvergleiche bestanden.**

## Dateien
- `FilterResponse_10bands.csv`, `FilterResponse_20bands.csv`: analytische diskrete Filterantwort über Frequenz
- `FilterResponse_10bands.svg`, `FilterResponse_20bands.svg`: Frequenzganggrafiken
- `Crosstalk_10bands.svg`, `Crosstalk_20bands.svg`: Übersprechmatrizen (theoretische Gainwerte)
- `FilterSimulation_Soll_Ist.csv`: unabhängige Python-Nachbildung der Kotlin-Rekursion gegen analytische Theorie

## Einschränkungen
- Kein Android-Mikrofon, keine Android-Blockverarbeitung, keine Messgeräte- oder Akustikkalibrierung.
- Nominale Bandgrenzen sind NICHT scharfe Passbandgrenzen; erste Ordnung führt zu erheblicher Bandüberlappung.
- Simulation bildet den aktuellen Kotlin-Filteralgorithmus nach und ist deshalb keine unabhängige physikalische Filtermessung.
- Die geräteinternen Kotlin-Regressionen werden separat durch `VORTEST_INSTALL.bat` geprüft.
- Tests bei sehr niedriger Frequenz können bei kurzer Laufdauer wegen Einschwingverhalten scheitern. Diese Fehler werden nicht verschwiegen.
