# Phase 3J – Anzeige-Vorschau, v0.5.56-Entwicklung

## Umfang
- Neue `AnalyzerDisplayPreviewSession.kt`, in `AnalyzerJsonSetupBrowser` explizite Vorschau und Wiederherstellung.
- Anzeige-Vorschau: zwölf vorhandene DISPLAY-Felder, keine Audio-, FFT-, Filterbank-DSP- oder MON-Änderung.
- Kein Schreiben in SharedPreferences oder JSON, keine automatische Aktivierung.
- MON muss gestoppt sein; während MON-Aufnahme gesperrt.
- Vorherige DISPLAY-Laufzeitwerte vor Start zwischengespeichert; nach Fehler bei Anwendung bleibt die Vorschau inaktiv.
- Vor erneutem Vorschau-Start muss die frühere Vorschau ausdrücklich zurückgesetzt werden.
- Restore überschreibt auch manuelle Anzeigeänderungen, die nach Beginn der Vorschau vorgenommen wurden; in der UI ausdrücklich angegeben.

## Grenzen und Folgetests
- Inaktive oder fehlende Legacy-Felder bleiben unberücksichtigt; ohne passenden Anzeigewert wird abgebrochen.
- Die Vorschau und ihre Baseline sind nur prozesslokal; nach Prozessende keine automatische Wiederherstellung.
- Änderungen anderer Teile der Analyzer-Oberfläche nach Start werden nicht als JSON-Entwurf gespeichert.
- Nach Moduswechsel wird die Wiederherstellung bis zur Rückkehr in den ursprünglichen 8-kHz-/48-kHz-Modus verweigert. Im 48-kHz-Modus keine FILTER-Anzeigewerte anwenden; ein Setup mit FILTER-Werten wird vorab abgelehnt.
- Kontrollierter Android-GitHub-TEST-Build und Gerätetests erforderlich: Start, Restore, laufende Messung, MON-Sperren, 48 kHz, Prozess-Neustart, Nichtpersistenz.
- Die vollständige JSON-Setup-Aktivierung und die sieben Scout-/acht Analyzer-Panels sind weiterhin offen.
- Versionskennung des Projekts vorerst 0.5.55; dies ist kein freigegebenes 0.5.56.
