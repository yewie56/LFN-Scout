# LFN-Scout v0.5.46 – 1/f Cache und Diagnosetest

## Befund aus dem Geraetetest v0.5.45.9.1
- Main FFT: 725 Fits/149 s, durchschnittlich 95,7 ms je Fit.
- Rolling FFT: 73 Fits/149 s, durchschnittlich 81,8 ms je Fit.
- `cancelled=0`, `fallback=0`, `invalid_ref=0`, aber zeitweise PCM-Alter >4 s.
- Korrelation legt hohe Rechenlast als moegliche Ursache nahe, nicht als bewiesene Ursache.

## Änderung
Ein eigenes Cacheobjekt pro Main- bzw. Rolling-FFT speichert die *berechnete* Referenz.
Der Cache wird nur fuer dieselbe immutable Messreferenz, gleiche Abtastrate und gleiche FFT-Groesse verwendet.
Bei neuen Referenzen oder Rasteränderungen wird neu gerechnet. Auch ungeeignete Profile werden zwischengespeichert, um wiederholte 1/f-Fits zu vermeiden.
Die modellierte Referenz und die Subtraktionsformel bleiben numerisch unveraendert.

## Diagnose: Testablauf
1. GitHub TEST bauen, Unit-Tests gruen abwarten; danach REGULAR mit bestehendem Signaturschluessel erstellen.
2. NOISE AUS 30 s beobachten, DIAG kopieren.
3. NOISE 1/f 120 s, DIAG unmittelbar nach Umschalten und nach 120 s kopieren.
4. NOISE AUS 30 s; danach NOISE 1/f weitere 120 s.
5. Vergleichen: `fits` und `cache_miss` sollten bei gleichbleibenden Referenzen pro FFT-Pfad nur um 1 steigen, `cache_hit` fortlaufend.
6. PCM-Alter, FFT-Zähler und `validSignal` im DIAG weiterhin getrennt beobachten.

## Interpretation
Ein geringerer `fit_avg` ist nicht sinnvoll vergleichbar, weil nach dem ersten Cache-Miss keine weiteren Fits stattfinden. Entscheidend sind `fits`, `cache_hit`, `cache_miss`, `total_max` und ob PCM-Alter stabil bleibt. Die ursprüngliche PCM-Unterbrechung ist damit noch nicht bewiesen behoben.

## Build
GitHub TEST per Push, REGULAR ueber Run workflow. Secrets im GitHub-Repository lassen, niemals im ZIP. GitHub-Workflow und alle Windows-Build-BAT-Dateien unveraendert.

## Tests
`NoiseModelReferenceCacheTest` prueft Identitaets-Cache, Frequenzwechsel, Referenzwechsel, clear und gecachte Fehler. Ein vollstaendiger Android-Gradle-Lauf ist erst ueber GitHub Actions moeglich.
