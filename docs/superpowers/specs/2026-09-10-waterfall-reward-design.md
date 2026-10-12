# Wasserfall-Belohnung – Design

## Ziel
Aus der letzten lokal vorhandenen Audioaufnahme wird auf Anforderung ein fallendes Wasserfallspektrum als Belohnungsansicht erzeugt.

## Darstellung
- Frequenzachse: 0 bis 100 Hz, linear.
- Zeitachse: Beginn oben, spätere Zeit nach unten (fallend).
- Colormap: Inferno.
- Pegel: dBFS, automatisch einmal für die komplette Aufnahme adaptiert.
- Auto-Pegelbestimmung: robuste Werte aus 2 bis 100 Hz.
- Untergrenze: P5 - 3 dB.
- Obergrenze: P99.5 + 3 dB.
- Mindestdynamik: 30 dB.
- Harte Grenzen: -120 bis 0 dBFS.
- Tatsächlich verwendete Pegelgrenzen werden im Bild angezeigt.

## Bedienung
Unter Zusatzfunktionen -> Audio-Aufzeichnungen gibt es einen Button
`Wasserfall letzte Aufnahme`.

Die App nimmt die neueste lokal vorhandene WAV. Das Ergebnis wird in einem Dialog angezeigt.
Ein zusätzlicher Button speichert das PNG unter `Download/LFN-Scout/`.

## Technische Umsetzung
- WAV-Quelle: lokale AudioArchive-Metadaten + MediaStore/Downloads.
- WAV: PCM16 mono, 1000 Hz.
- STFT lokal auf dem Smartphone, keine Supabase-Abhängigkeit.
- Hann-Fenster, 1024 Samples, 75 % Überlappung.
- FFT lokal in Kotlin, keine zusätzliche Bibliothek.
- Zeit nimmt im Raster nach unten zu.
- PNG-Erzeugung als Android Bitmap.
- Inferno-LUT intern als kompakte Interpolation über Stützpunkte.
- Umfangreiches Debug-Logging für Start, Eingabedatei, FFT, Pegelgrenzen, Erfolg, Fehler und PNG-Speicherung.

## Version
Android v0.4.46, versionCode 86.
Keine neue Supabase-Migration und kein Edge-Function-Deployment erforderlich.
