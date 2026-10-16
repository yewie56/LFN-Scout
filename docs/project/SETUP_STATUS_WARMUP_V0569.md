# v0.5.69: Setup-Status und FFT-Warm-up

## Zweck

- Der Setup-Name im Analyzer-Kopf zeigt die aktuelle Repository-Zuordnung, auch wenn der Analyzer-Screen beim Setupwechsel nicht neu aufgebaut wird.
- Der versuchte Setupwechsel bei RUN wird vor einer eventuellen Speicherung über einen Dialog mit OK abgewiesen. Der aktive Datensatz und Rolling bleiben unverändert.
- Bei STOP bleibt der Setupwechsel nach den bestehenden MON-, Preview-, Capture- und Ressourcenkonflikt-Prüfungen erlaubt. Eine laufende Rolling-Aufnahme allein blockiert ihn nicht.
- Eine Eingangssignal-Warnung ist während des FFT-Warm-ups nicht sinnvoll. Sie erscheint nur bei laufendem Analyzer nach erster Spektrenbildung und einem tatsächlichen ungültigen Signalzustand.

## Tests

- Quelltext-/Release-Verträge: `python3 tools/verify_v0569_setup_status.py`.
- Gerät: RUN → Setup B antippen: Dialog, keine Aktivierung, keine Aufnahmeunterbrechung.
- Gerät: STOP bei weiterlaufendem Rolling → Setup B: obere Anzeige zeigt B, anschließend A zeigt A.
- Gerät: STOP → RUN: Warm-up ohne falsche Eingangssignal-Warnung; bei gültigen Daten wieder normales Spektrum.
- Gerät: Fehlerzustand *nach* Warm-up: Diagnose weiterhin sichtbar.

Hinweis: Lokale statische Tests ersetzen weder Kotlin-Kompilierung noch den integrierten Android-Self-Test.
