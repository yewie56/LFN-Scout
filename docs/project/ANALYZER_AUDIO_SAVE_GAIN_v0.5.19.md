# LFN-Scout v0.5.19 – Analyzer Audio-Save, Gain-Bezug und ADB-Warten

**Stand:** 25.09.2026  
**Version:** 0.5.19 / versionCode 166  
**Basis:** v0.5.18.4. Supabase-Schema und Cloud-Upload werden nicht erweitert.

## 1. Analyzer-UI

- Der bisherige `SAVE-Setup`-Button ist aus der Analyzer-Hauptanzeige entfernt.
- Die lokalen Audio-/Statistik-Save-Einstellungen (Dauer und Statistik-Frequenzbereich) werden unter **Analyzer → Einstellungen → Statistik → SAVE-Setup · Audio / lokale Statistik** geöffnet.
- `SAVE AUDIO` liegt unten in der Actionleiste als 48-dp-Actionbutton im Format des Kamerabuttons. Das Symbol kombiniert eine Audio-Wellenform mit einem Speichern-Pfeil.
- Der feste Audio-Infotext `Puffer/Save-Dauer · Tageszähler/Tageslimit` steht direkt neben dem Audio-Save-Actionbutton.
- Erfolgreiches Speichern zeigt **„Lokal gespeichert“** als Popup nahe dem Actionbutton für 3 Sekunden. Fehler und der kurze Speichervorgang werden ebenfalls als Popup angezeigt. Es wird kein zusätzlicher Status-/Hinweistext in eine bestehende Displayzeile eingeschoben.
- Die Freigaberegel bleibt: Save nur bei vollständigem zusammenhängendem Audiopuffer der konfigurierten Dauer, laufendem Analyzer, nicht im Testmodus, nicht während eines laufenden Saves und unterhalb der gemeinsamen Tagesquote.

## 2. Einheitlicher Gain-Bezug

`GainReferencePolicy` ist die zentrale Regel für die Rückreferenzierung digital verstärkter Werte:

`Pegel_vor_Gain = Pegel_nach_Gain - Gain_dB`

Bei +50 dB wird daher einmalig 50 dB abgezogen. Die Regel darf nur auf Signalpfade angewendet werden, in denen der Gain tatsächlich enthalten ist.

- Beobachter-WAV: der eingestellte Aufnahmegain ist im gespeicherten PCM enthalten; Spektrogramm-/Spektralwerte werden um diesen Gain korrigiert.
- WAV-L50/L90 bei 1 kHz und ganzen Sekunden: werden über denselben `StatisticsWindowEngine.analyzeOverall()`-Pfad berechnet wie die lokale Statistik-JSON. Bei identischem WAV, Gain und Statistik-Frequenzbereich müssen die Werte bis auf Anzeige-Rundung übereinstimmen.
- Analyzer-Live-FFT: der Analyzer-DSP fügt den Analysegain hinzu; der Controller referenziert die FFT-Pegel bereits vor weiterer Anzeige/Statistik einmalig zurück. v0.5.19 nutzt dafür ebenfalls `GainReferencePolicy`.
- Analyzer-Save-WAV: der lokale Analyzer-Ringpuffer ist PRE-GAIN; für dessen Statistik bleibt `applied_wav_gain_db = 0`. Der Analyzer-Anzeigegain wird nur als Metadatum dokumentiert und nicht nochmals vom PRE-GAIN-WAV abgezogen.
- Clipping wird durch eine spätere Gain-Subtraktion nicht repariert; entsprechende Qualitätsflags bleiben erforderlich.

Die manuelle Wasserfall-Farbskala ist ab v0.5.19 ebenfalls auf gain-korrigierte dBFS-Werte bezogen und verschiebt sich nicht mehr mit dem Recorder-Gain.

## 3. VORTEST_INSTALL – auf ADB-Gerät warten

Nach erfolgreichem PREPARE, Unit-Test, `assembleDebug`, APK-Prüfung und ACTIVE-Paketierung sucht das Skript `adb.exe`, startet den ADB-Server und wartet anschließend ohne Standard-Timeout auf genau ein autorisiertes Zielgerät.

- kein Gerät: alle 2 Sekunden erneut prüfen;
- `unauthorized`: weiter warten und zur Bestätigung von USB-Debugging auf dem Handy auffordern;
- `offline`: weiter warten;
- mehrere Geräte: nicht installieren; warten, bis nur noch ein Zielgerät vorhanden ist;
- genau ein `device`: mit `adb install -r` fortfahren;
- manueller Abbruch jederzeit mit `Ctrl+C`.

Alle Meldungen von stdout/stderr sowie die Wartemeldungen bleiben live auf Bildschirm und im Vortest-Log.

## 4. Technische Bereinigung

- `LocalAnalyzerAudioBuffer.accept()` verwendet einen Block-Body mit explizitem `Unit`; die für Kotlin 2.4 angekündigte Expression-Body-Return-Warnung ist beseitigt.
- `ReminderSettingsStore.tryMarkAudioRecordingCompleted()` speichert den neuen Zähler ohne unnötiges `!!`.

## 5. Nicht Bestandteil

Keine neue Supabase-Tabelle, kein neuer Statistik-Upload, keine Key-Verwaltung, keine vollständige Impulserkennung und noch keine zeitlich zugeordnete 8-kHz-FAST-Statistik im Save-Paket.
