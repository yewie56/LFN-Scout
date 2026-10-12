# Quellen und technische Referenzen – Analyzer v0.6

## 1. Nutzervorgaben / mitgelieferte Arbeitsgrundlagen

- Spectroid-Anleitung: `../../references/Spectroid_Anleitung_v10_5.docx` (funktionale Arbeitsreferenz; nicht als verbindliche App-Codebasis verwenden).
- Bereitgestellte Original-Screenshots: `../../references/Spectroid-Screens.zip` (Funktionen und Menübenennungen nachvollziehen; keine fremden UI-Assets übernehmen).
- SRD/SRS v0.3 im historischen Archiv `../../references/legacy/`; v0.4-Parameterreferenz unter `../../references/Funktionsreferenz_v0.4.md`.
- Aktuelle Nutzerfestlegungen vom 19. September 2026: Hauptbild, Gesten, Statistik, Fadenkreuz, Superfast, Noise, Save etc.; als v0.6-Anforderungen verarbeitet.

## 2. Offizielle Android-Dokumentation (bei Implementierung aktuelle API prüfen)

- [AudioRecord – Android API](https://developer.android.com/reference/android/media/AudioRecord): flüchtigen Mic-PCM-Stream lesen, ohne eine Audiodatei zu schreiben.
- [AudioTrack – Android API](https://developer.android.com/reference/android/media/AudioTrack): PCM-Ausgabe im separaten Mithörzweig.
- [MediaRecorder.AudioSource – UNPROCESSED](https://developer.android.com/reference/android/media/MediaRecorder.AudioSource): Quelle prüfen; keine garantierte Hardwarelinearität bei 2 Hz ableiten.
- [AudioManager / AudioDeviceInfo](https://developer.android.com/reference/android/media/AudioManager): Audio-Routing und Geräteinformationen, vor tatsächlichem Release prüfen.
- [Storage Access Framework](https://developer.android.com/guide/topics/providers/document-provider): benutzergesteuerter Dateizugriff für Profile/Export.
- [Foreground services / microphone](https://developer.android.com/develop/background-work/services/fgs/service-types): nur relevant, falls später explizit Hintergrundbetrieb spezifiziert wird; **nicht Teil dieses Entwurfs**.

**Hinweis:** Dies sind inspectierbare technische Links, keine Bestätigung, dass die API-Verfügbarkeit oder der aktuelle App-Quellcode bereits geprüft wurde. Performance, Frequenzgang und Clipping müssen am realen Gerät getestet werden.


## Ergänzung v0.6: Android-Plattformreferenzen (am 19.09.2026 geprüft)

1. Android Developers – *Sharing audio input*. Beschreibt Priorisierung/Mute-Szenarien konkurrierender Eingabe-Clients; begründet den koordinierten appinternen Capture/Fan-out. https://developer.android.com/media/platform/sharing-audio-input
2. Android Developers – *Restrictions on starting a foreground service from the background*. Beschreibt Start- und While-in-use-Permission-Beschränkungen für Mikrofon-FGS. https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
3. Android Developers – *Foreground service types*. Microphone-Typ, notwendige Berechtigungen und Voraussetzungen je Target-SDK. https://developer.android.com/develop/background-work/services/fgs/service-types
4. Android Developers – *Declare foreground services and request permissions*. Manifest-/Typanforderungen, vor einem optionalen Hintergrundmodus prüfen. https://developer.android.com/develop/background-work/services/fgs/declare

Diese Quellen belegen Plattformgrenzen, **nicht** die tatsächliche Architektur des aktuellen privaten LFN-Scout-Repositories. Sie ersetzen nicht die geforderte Repo-Inspektion.
