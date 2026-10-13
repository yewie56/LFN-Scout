# Phase 3H – typisierter Laufzeit-Snapshot und Vergleich (nicht aktivierend)

- Neues `AnalyzerRuntimeSetupSnapshot.kt`: erfasst 42 tatsächlich aus `LiveAnalyzerController.Snapshot` verfügbare Einstellungen. Kein Lesezugriff auf alte Slots, keine Default-Auffüllung. `direct_audio_source` und Generatorwerte fehlen absichtlich, weil sie nicht Bestandteil des Controller-Snapshots sind.
- Neues Browser-Bedienelement „Mit laufendem Analyzer vergleichen (nur lesen)“: zeigt Anzahl abweichender, gleicher und nicht vergleichbarer gespeicherter Legacy-Felder. Fehlende oder `null`-Werte werden als nicht vergleichbar geführt.
- Ein Vergleich ist keine Aktivierung: Auch in Phase 3H betrifft die einzige aktive JSON-Teilübernahme ausschließlich MON; andere Analyzer-Einstellungen bleiben bei den bisherigen Controllern/Prefs.
- Speichervorgänge werden nicht automatisch ausgelöst. `AnalyzerSetupFormat` validiert erzeugte Laufzeitdaten.
- Einschränkung: `filterMinHz`/`filterMaxHz` liegen im Controller als Double vor, das bisherige Legacy-JSON erwartet Integer; die Konvertierung `toInt` kann Nachkommastellen abschneiden. Daher ist der Vergleich in diesen beiden Feldern ein Indikator, keine verlustfreie Rückschreibvorlage. Vor aktiver Übernahme ist eine Format-/Wertebereichsentscheidung erforderlich.
- Der Vergleich ist bewusst kein Dirty-Erkennungs-Ersatz für alle 47 Legacy-Felder; nicht vergleichbare Werte bleiben getrennt.
- Die bestehenden Legacy-Slots und MON-MRU bleiben unverändert. `versionName` bleibt bis zur Freigabe bei 0.5.55.
- Prüfung: statischer Feldabgleich plus Phase-A–G-Checks; Android Gradle/Instrumentierungs- und Gerätetests stehen aus.

## Nächster Schritt
Für die umfassende Aktivierung müssen die persistierenden `LiveAnalyzerController.set...`-Methoden erst einen speicherfreien Setup-Entwurfsmodus erhalten; zusätzlich vollständige Felder, Aufnahme-Sperren, atomare Rücknahme und Regressionstests.
