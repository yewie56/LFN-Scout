# LFN-Scout v0.5.03 – Geräteabnahme / PASS–FAIL

**Iststatus sämtlicher folgenden v0.5.03-Geräteprüfungen: NOT RUN.** Nutzer hat v0.5.02-Testplan vollständig bestanden gemeldet. In der Quell-Erstellungsumgebung fehlen Android-SDK und Xiaomi, keine neue APK gebaut. Ergebnisse erst nach realem Test eintragen.

| Nr. | Test | Soll | Ergebnis |
|---:|---|---|---|
| 1 | Update über v0.5.02, ohne App-Daten zu löschen | `versionCode 144`, Paket-ID gleich, alte Slots erhalten; `:app:testDebugUnitTest` und `assembleDebug` auf Windows PASS | NOT RUN |
| 2 | Decimation 4:1, 8:1, 16:1 | Anzeige LF 2000/1000/500 Hz; FFT-Auflösung und tatsächliche Hop-Zeit jeweils korrekt | NOT RUN |
| 3 | Bei allen drei Faktoren 50/195-Hz-Signal und außerhalb Nyquist liegende Sinussignale prüfen | keine unerwarteten Aliase, unveränderte WAV-Größe/-Abtastrate; keine zusätzlichen Mic-Owner | NOT RUN |
| 4 | FFT 2048/4096/8192, Hop 5/100/1000 kombinieren | gültige Kombinationen korrekt, 16:1/1000 Hop erfordert ≥2-s-Wasserfallintervall | NOT RUN |
| 5 | FFT 8192/Hop 5 mit WAV/Rolling und Auto-Bild | CPU/Drops/Resets/Temperatur protokollieren, keine Aufnahmeunterbrechung | NOT RUN |
| 6 | Cursor nach links/rechts ziehen | linkes/rechtes Readout auf gleicher Höhe; keine Sprünge | NOT RUN |
| 7 | Cursor durch Spektrum bis Ende Wasserfall | Frequenzposition fluchtet; auch PNG und JPG mit sichtbarem Cursor | NOT RUN |
| 8 | Slots 1–3 benennen/beschreiben und speichern/laden/neustarten | Name/Beschreibung/Persistenz unverändert; alte namenlose Slots ladbar | NOT RUN |
| 9 | Statistik 7–173 Hz, Fenster 37 s, Average 2,5 s | Anzeige oben rechts und Bandmarkierung im Spektrum/PNG-JPG gleich; neue L50/L90 erst nach vollem Fenster | NOT RUN |
| 10 | Statistikfenster 120 s, 600 s, Average 0,1 und 5 s, außerhalb Anzeigebereich | freie Eingaben, getrennte Wasserfallzeit; Teilschnitt der Spektrum-Markierung, kein Absturz | NOT RUN |
| 11 | Ungültige Statistik- und Wasserfallwerte | verständliche Ablehnung, kein unbeabsichtigter Reset oder Datenverlust | NOT RUN |
| 12 | Bestehende Regression v0.5.02 | Hz-Zeile, Akt/MW/MAX, dBFS, PNG/JPG (2 Autozyklen), Panelwechsel, Navigation, WAV, Rolling, Bewertung 0/−1, Seismik | NOT RUN |

Bitte Buildlogs, Testsetup, CPU-/Dropdiagnose, Screenshot und Dateiexporte bei Fehlern beilegen. **Keine Abnahme durch Quellcodeprüfung ersetzen.**
