# LFN-Scout v0.4.88 – Analyse-Gain für Highspeed-Impulsdiagnose

## Umfang und Abgrenzung

Die bisherige Aufnahme, 8-kHz-Roh-PCM und 1-kHz-WAV bleiben unverändert. Der Regler 0–50 dB (Start 0 dB) wirkt ausschließlich im Highspeed-Impulsdiagnosezweig. Er ist eine unabhängige Einstellung, nicht der bisherige Aufnahme-Gain. Er löst weder eine neue Mikrofonaufnahme noch eine Änderung der bestehenden Analyzer-FFT aus. Die vollständige Spektrum/Wasserfall-UI ist weiterhin offen.

## Berechnung

Verstärkungsfaktor G = 10^(gainDb/20). Die bisherige feste Mindestimpulsschwelle 1400 PCM wird als Roh-PCM-Äquivalent max(1, round(1400/G)) verwendet. Der adaptive Schwellenanteil bleibt `6 * noiseAbs`; effektive Schwelle ist max(beide). So erhöht Gain die Empfindlichkeit gegenüber der *festen* Mindestschwelle, aber nicht gegenüber dem rauschabhängigen Anteil. Der Gain wird nicht in das 16-Bit-PCM multipliziert; kein zusätzliches Quantisieren oder digitales Clipping. Ereignis-Peak bleibt unverstärkt und in Roh-PCM ausgewiesen. Anzeige Analyse-Peak = Rohsample-Peak * G, ausdrücklich ein mathematisches Äquivalent ohne 16-Bit-Begrenzung. 50 dB kann keine fehlende physikalische Mikrofonempfindlichkeit wiederherstellen.

## Bedienung und Test

Entwicklermodus → Zusatzfunktionen → Highspeed-Impulsdiagnose → Diagnose starten. Den Regler 0/10/20/30/40/50 dB in Stufen erhöhen. Jeweils fünf einzelne mäßig leise Testgeräusche im Abstand von 2 s, dann 30 s Ruhe. Ereigniszähler und Rohpeak/Analysepeak/effektive Schwelle notieren. Wenn Rohpeak kaum variiert, aber der Zähler steigt, ist nur die digitale Schwelle geändert worden. Audioaufnahme und daraus berechneten Wasserfall unverändert prüfen. Keine akustisch gefährlichen Geräusche direkt am Handy erzeugen. Bei hoher Verstärkung können Fehltrigger auftreten.

## Nachweise und Grenzen

Nur synthetische Tests und statische Projektprüfung in der Lieferumgebung; Android-Build und Geräteprüfung von v0.4.88 müssen lokal durchgeführt werden. Die tatsächliche 48-kHz-Erfassung ist nicht implementiert. Spektralanalyse mit unabhängigem Analyse-Gain bleibt für spätere Integration. Keine Messung von dB SPL ohne Kalibrierung.
