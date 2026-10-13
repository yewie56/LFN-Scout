# Phase 3I – speicherfreie Laufzeitübernahme ausgewählter Anzeigewerte

## Umfang

Die sechs bestehenden Setter für Original-Anzeigeskala, Original-Autorange, Kurvensichtbarkeit, FILTER-Anzeigeskala und FILTER-Autorange besitzen nun den optionalen Parameter `persist: Boolean = true`. Ohne Angabe des Parameters bleibt die Legacy-Speicherung vollständig erhalten. Mit `persist = false` ändern sie nur die Laufzeitdaten.

Neu: `AnalyzerDisplayDraftApplier.apply(context, legacyJson)` als **explizit aufrufbarer, noch nicht an die UI verdrahteter** Adapter. Er prüft den gültigen JSON-Typ, fehlende und null-Werte, beide Anzeigebereiche, Autorange-Grenzen und die FILTER-Sperre im Direktmodus vor dem ersten Setter. Unterstützt werden zwölf Anzeigefelder. Übrige Setupfelder werden als `excludedKeys` gemeldet und NICHT angewendet. Bei unerwarteter Ausnahme wird die vorherige Anzeige möglichst wiederhergestellt.

## Grenzen

- **Kein kompletter JSON-Setup-Wechsel** und keine Schreiboperation in den JSON-Dateien.
- Keine automatische Aktivierung beim Öffnen der Oberfläche; keine Audio-, FFT-, Filterbank-, MON- oder Aufnahme-Parameter geändert.
- Nicht als vollständig transaktional oder gerätegetestet bezeichnen: laufende externe Controller-Updates können trotz synchronisiertem Adapter andere Werte verändern; der Adapter benötigt später einen Controller-weiten Transaktionsschutz. Auch die Persistenzregeln anderer Funktionen wie Autorange sind noch zu prüfen.
- Während der Laufzeit ausgelöste Änderungen aus den alten UI-Settern schreiben weiterhin in SharedPreferences, da sie den Standardparameter verwenden. Der neue Entwurfsmodus muss vor voller Aktivierung alle relevanten Pfade erfassen.
- Legacy `filter_min_hz`/`filter_max_hz` sind bewusst nicht Teil dieser Lösung; sie brauchen eine eigene verlustfreie Typentscheidung.
- Vor Integration Android-TEST-Build und automatisierte/dynamische Regressionstests durchführen.
