# LFN-Scout v0.5.02 – Xiaomi-Geräteabnahme / PASS–FAIL

**Iststatus: sämtliche folgenden Android-/Geräteprüfungen NOT RUN.** Weder APK noch Xamarin-/Android-SDK-Build wurde in der Quell-Erstellungsumgebung durchgeführt. Ergebnisse je Punkt erst nach Beobachtung eintragen.

| Nr. | Prüfung | Sollkriterium | Ergebnis |
|---:|---|---|---|
| 1 | Update über v0.5.01 ohne Datenlöschen | Package-ID, bestehende Setups und laufende Daten erhalten; Build `:app:testDebugUnitTest`/`assembleDebug` PASS | NOT RUN |
| 2 | Hz bei 0–20/50/100/200 Hz | Spektrum und Wasserfall jeweils mittig zwischen **zwei benachbarten Achsenzahlen**, gleiche Grundlinie, kein Überdruck; PNG/JPG ebenso | NOT RUN |
| 3 | Wasserfallmodus Akt/MW/MAX wechseln | Fußzeile und gespeicherte Grafik spiegeln jeweils Modus; keine Spektrum-Max-Hold-Anzeige | NOT RUN |
| 4 | dBFS-Werte −120/−60 sowie Preset/Autorange bearbeiten | Minus ohne Zusatzbuttons eingebbar; Werte gut lesbar, korrekt persistent, keine Überschreibung beim Tippen | NOT RUN |
| 5 | Freie Wasserfallzeit 37, 120, 123, 600 s (250 ms) | Freie Werte ohne Presetrundung, korrekt nach Setup und Neustart; 600 s/100 ms wird verständlich abgelehnt | NOT RUN |
| 6 | Wasserfall nach Dauerwechsel | Zeit-Ticks korrekt, keine alten Zeilen/Auto-Zyklen gemischt; laufende WAV-/Rolling-Aufnahme bleibt bestehen | NOT RUN |
| 7 | Farbleiste mit negativen langen Zahlen | Barbreite höchstens breitester Tick + kleiner Abstand, alle Zahlen lesbar; kein Verlust rechter Plotfläche | NOT RUN |
| 8 | FFT Size 2048/4096/8192 | Δf und FFT-Warmup plausibel; im Setup und Neustart gespeichert; kein zweiter AudioRecord | NOT RUN |
| 9 | FFT-Hop 5 und Transformationsintervall 5 ms | 200 FFT/s ist theoretischer Sollwert; reale FPS, dropped Frames, CPU, Temperatur messen, WAV gleichzeitig testen | NOT RUN |
| 10 | Fenster Hann/Hamming/Blackman/Rechteck | Konfiguration verfügbar, Korrelationssignale/Anzeige plausibel, Konfiguration persistent | NOT RUN |
| 11 | Frequenz Min/Max frei z. B. 7–173 Hz | beide Diagramme/PNG, Ticks, Cursor und Farbwerte berücksichtigen Crop; ungültige Werte abgewiesen | NOT RUN |
| 12 | Smoothing AUS/EIN, τ=0,1/5/20 s und Faktor | roten Verlauf und α prüfen; α=1 schaltet Smoothing aus, hop-gekoppeltes α | NOT RUN |
| 13 | PNG/JPG Auto/Manuell, ≥2 volle Historienzyklen und Panelwechsel | jedes gespeicherte Format decodierbar; kein doppelter Long-Press-Export; neue freie Zeit im Autozyklus | NOT RUN |
| 14 | Altfunktionen | WAV 120 s, Rolling, Bewertung 0 vs. −1, Supabase-Puffer, Seismik, Navigation wie `TESTPLAN_v0.5.00_HANDOFF.md` | NOT RUN |

Für Fehlversuche Gerätetyp, Android-Version, Setup-Slot, FFT Size/Hop/Window, Dauer/Zeilenintervall, Screenshot und Buildlog mitliefern. **Kein** PASS aufgrund bloßer Implementierung setzen.
