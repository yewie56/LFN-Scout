# v0.5.70 – Neu gegliederte Analyzer-Einstellungsnavigation

**Ausgangsbasis:** v0.5.69, versionCode 227. **Neuer Quellstand:** v0.5.70, versionCode 228.

## Umgesetzt

- Helle, kontrastreiche Karten in der Einstellungsübersicht, mit vier fachlichen Gruppen: **Setups & Profile**, **Messung & Signal**, **Anzeige & Auswertung**, **Service**.
- Die neun bisher existierenden Unterpanels behalten ihre stabilen internen Kennungen und ihre bisherigen Controller-Aufrufe. Dadurch bleiben alle vorhandenen Einstellmöglichkeiten und Validierungen erreichbar.
- Die Navigation zur Übersicht und zurück bleibt unverändert. Die MON-Live-Bedienung bleibt im Analyzer erhalten; es gibt keine zusätzliche globale MON-Persistenz.
- Keine Änderungen an Audio-/Rolling-Streaming, Filteralgorithmus, JSON-Speicherung, Setupwechsel, Cloud, GitHub-Workflows oder Signierung.

## Abgrenzung und offene Arbeiten

Diese Version liefert bewusst **eine Neugestaltung der Einstellungsübersicht und Gruppierung**, keine physische Zusammenlegung der neun Unterpanels zu sieben Seiten. Insbesondere sind die alten internen Audio-/Anzeige-/Statistik-Panels weiter vorhanden; der existierende MON-Live-Block wurde nicht in ein Unterpanel verschoben. Auch die bestehenden expliziten Übernehmen-Knöpfe innerhalb alter Panels bleiben zunächst unverändert. MRU und benannte JSON-Setups liegen unverändert im bestehenden Setup-Unterpanel.

Eine weitergehende Umstrukturierung der Unterpanels sowie ihre vereinheitlichte helle Gestaltung erfordert eigene Android-Gerätetests und wird getrennt geplant. Keine Dummy-Einstellschalter ergänzen.

## Prüfungen

`python3 tools/verify_v0570_settings_navigation.py` als statischer Regressionstest. Android-Gradle-Kompilierung, integrierter Self-Test und Geräteabnahme müssen noch durchgeführt werden. Die bestehende App-Oberfläche muss auf dem Gerät auf Navigation, Panelrückkehr, MON-Liveänderung sowie Setupwechsel bei STOP/RUN geprüft werden.
