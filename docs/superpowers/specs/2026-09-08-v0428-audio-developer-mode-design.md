# LFN-Scout v0.4.29 – Audio, Lautstärke und Entwicklermodus

## Ziel
v0.4.29 ergänzt echte WAV-Aufnahme, Alarm-Lautstärke, Audio-Gain,
Tageslimit und einen persistenten Entwicklermodus. Gleichzeitig werden die
vertikalen Abstände gegenüber v0.4.27 nochmals reduziert.

## Audio
- Hauptpanel: Audioaufnahme direkt erreichbar.
- Vor Start modaler Hinweis:
  „Bitte Handy auf eine leicht gepolsterte Oberfläche
  (zusammengefaltetes Küchentuch) legen und 2 min keine Geräusche machen.“
- 15-s-Countdown, abbrechbar.
- Danach automatische 120-s-Aufnahme.
- Zieldatei: standardkonforme PCM-WAV, mono, 16 Bit, 1000 Hz.
- Android-Aufnahme erfolgt robust mit 8000 Hz und wird auf 1000 Hz
  heruntergerechnet.
- Gain: 0…+30 dB, 1-dB-Schritte, persistent.
- Clipping wird erkannt und angezeigt.
- Nach Aufnahmeende automatischer Upload an `upload_wav`.
- Lokale Datei bleibt bei Uploadfehler erhalten und der Fehler wird angezeigt.

## Alarm-Lautstärke
- Einstellungen: Slider 0…100 %.
- steuert `AudioManager.STREAM_ALARM`.
- persistent, wird vor Alarmton und Selbsttest angewendet.

## Audio-Tageslimit
- Standard 10 pro lokalem Kalendertag.
- Hauptpanel zeigt `heute: n/max`.
- Aufnahmebutton bei erreichtem Limit deaktiviert.
- Maximalwert nur im Entwicklermodus editierbar.

## Entwicklermodus
- persistent bis zum bewussten Abschalten.
- Aktivierung erfordert Passwort.
- Passwort = dritte Vierergruppe des Teilnehmerschlüssels rückwärts.
- „Vierergruppe“ bedeutet: Bindestrichgruppen mit exakt 4 Zeichen;
  die dritte davon wird verwendet.
- kein Passwort wird gespeichert.
- Entwicklermodus sichtbar markiert.
- Tageslimit, Debug-/Diagnose- und technische Audioangaben sind nur im
  Entwicklermodus sichtbar.
- serverseitiger Schlüsselstatus `revoked` deaktiviert beim nächsten
  erkannten Serverkontakt den Entwicklermodus.

## Schlüsselstatus
- Observer-Status: `active`, `paused`, `revoked`.
- bestehendes `enabled` bleibt aus Kompatibilitätsgründen erhalten.
- Edge Functions lehnen paused/revoked getrennt ab.

## UI-Kompaktheit
Gegenüber v0.4.27 nochmals reduziert:
- Bildschirmrand 7 dp
- Abschnittsabstand 3 dp
- Karteninnenabstand 7 dp
- Karten-Titelabstand 2 dp
- Feldabstand 2 dp
- Radius-Rasterabstand 2 dp
- Rating-Button-Höhe 50 dp
