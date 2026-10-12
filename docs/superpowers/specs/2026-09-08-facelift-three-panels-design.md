# LFN-Scout v0.4.25 – Drei-Panel-Facelift

## Ziel

Die bestehende funktionale Oberfläche wird ohne Änderung des Bewertungs-,
Reminder-, Offline- oder Tonpaket-Verhaltens in drei klar getrennte Ansichten
gegliedert.

## Panel 1 – Hauptpanel

Enthält nur Funktionen für den laufenden Betrieb:

- Kopfzeile „LFN-Scout“ mit `⚙` für Einstellungen und `⋮` für Zusatzfunktionen.
- kompakter Reminder-Status und nächste Erinnerung.
- Pausensteuerung direkt auf dem Hauptpanel:
  `1 h`, `2 h`, `4 h`, `8 h`, `24 h`, `72 h`, `Ganz aus`.
- Bewertung `0..5`.
- optionaler Kommentar und bestehendes 5-Sekunden-Auto-Senden.
- Übertragungs- und Standortstatus.
- Audio-Bereich als sichtbarer Bestandteil des Hauptpanels.
  Android-WAV-Aufnahme/Upload wird in diesem Facelift nicht erfunden:
  solange keine Aufnahmeimplementierung vorhanden ist, ist die Aktion
  eindeutig als noch nicht aktiviert gekennzeichnet.
- letzte fünf Bewertungen.
- Radius-Histogramme klein als 2×2-Raster; jeder Block bleibt separat normiert.

Nicht im Hauptpanel:
- Teilnehmerschlüssel
- Export
- Debug
- Detailkonfiguration der Reminder

## Panel 2 – Einstellungen (`⚙`)

Enthält:

- Erinnerungen ein/aus.
- Erinnerungsintervall und berechnetes Antwortfenster.
- Modus „Sehr auffällig“ / „Wecker-Modus“.
- Erinnerungston, Ton-Test und Erinnerungs-Test.
- Sonderrechte: exakte Alarme, Vollbild/Sperrbildschirm, Nicht-stören.
- Auto-Hintergrund.
- Radius-Auswertung ein/aus und Radius in km.
- Teilnehmerschlüssel speichern.

Die Pausensteuerung wird aus diesem Panel entfernt.

## Panel 3 – Zusatzfunktionen (`⋮`)

Enthält:

- alle eigenen Beobachtungen abrufen/exportieren.
- CSV speichern.
- XLSX speichern.
- Debug-Protokoll öffnen.
- Systemstatus/Version kompakt.
- Platzhalter/Übersicht für spätere Rohdaten-/WAV-Funktionen, ohne
  Funktionalität vorzutäuschen.

## Design

- Material 3.
- helle Karten mit klarer Gruppierung.
- blau als Hauptakzent; ausgewählte Bewertung optisch hervorgehoben.
- keine neue Icon-Abhängigkeit; Kopfzeilen-Symbole als Unicode `⚙` und `⋮`.
- bestehende Geschäftslogik möglichst unverändert lassen.
- `Modifier.weight(...)` nur kontextuell, kein expliziter
  `androidx.compose.foundation.layout.weight`-Import.

## Rückfall

Die bereits erzeugte ursprüngliche v0.4.25-ZIP bleibt unverändert.
Das Facelift wird als separate ZIP `LFN-Scout_Android_v0.4.25_Facelift.zip`
ausgegeben.
