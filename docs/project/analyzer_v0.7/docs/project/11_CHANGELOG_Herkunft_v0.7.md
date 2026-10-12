# Änderungsprotokoll und Herkunft – LFN-Scout Analyzer

**Version 0.5 · 19.09.2026 · Status: Dokumentation erstellt, keine Implementierung**

1. SRD/SRS v0.3 inhaltlich mit späteren Festlegungen und 146 CFG-IDs aus v0.4 zusammengeführt; historische Dateien unverändert im Archiv.
2. Verbindliches Hauptdisplay im Hochformat mit stets sichtbarem Spektrum und Wasserfall sowie separater schmaler Einstellansicht dokumentiert.
3. UI-/Gestenzustände für Mithörfeld-Tap/vertikales Wischen, Band-X-Drag, Fadenkreuz und Wasserfall-Expansion präzisiert.
4. Gelbe Live-, rote Average- und graue optionale Referenzkurve samt getrennten Ein/Aus-Zuständen aufgenommen.
5. Eigenständiges L50/L90-Statistikband (Default 2–25 Hz, 120 s), Superfast/Maximum Sample und PNG-Autosave spezifiziert.
6. Veraltete Formulierung „PNG ausschließlich manuell“ ersetzt: `Save aktiv` erzeugt PNG nach vollständiger Füllung und danach je Wasserfallzeit; CSV bleibt manuell; **keine Audiodateien**.
7. Mikrofonkalibrierung, Rauschbodenreferenz und optionale Anzeige-PSD-Subtraktion mit Gültigkeits- und Unsicherheitsbedingungen ausgearbeitet.
8. Erstmals SDS, UI-Konzept, Algorithmen/Kalibrierung, Testplan, Traceability, offene Punkte und Integrationsanweisung vollständig zusammengeführt.
9. Maschinell lesbare **illustrative** JSON-Beispiele für Setup, Mic-Cal und Noise-Referenz beigefügt; keine realen Messkalibrierdaten.
10. Vollständige aktuelle LFN-Scout-Quellcodeprüfung/Build ist **nicht** Bestandteil dieses Dokuments; alle vorgeschlagenen Klassennamen sind Entwurfsrollen.

**Quellenhistorie:** `references/legacy/SRD_v0.3.md`, `references/legacy/SRS_v0.3.md`, `references/Funktionsreferenz_v0.4.md`; Spectroid-Arbeitsanleitung v10.5 und benutzerseitige Screenshots, UI-Entwurfsbilder als **nicht bindende** Illustration. Die ausdrücklich verworfene Spectroid-Anleitung v10.4 wird nicht verwendet.

## Vor künftiger Änderung

Anforderungs-ID → Design → Code → PASS/FAIL-Test → Dokumentationsänderung nachführen; neu behobene Bugs mit dauerhaften Regressionstests sichern. Das Projekt-`CHANGELOG.md` erst nach tatsächlicher Codeänderung umsetzen, nicht aufgrund dieser Dokumentausgabe einen Software-Release behaupten.

## v0.6 – 19.09.2026 (Erweiterung nach Integrationsentscheidung)

- Einheitlicher Analyzer-Kern mit kleiner Standalone-Testhülle statt erst eigenständiger Produktions-Clone.
- Zentraler gemeinsamer Mic-Capture mit unveränderlichem PCM-Fan-out zu Analyzer und der bereits bestehenden LFN-Scout-Audioaufnahme.
- Analyzer-Session überlebt internen Panelwechsel; Renderer nur sichtbar aktiv, Offscreen-PNG-Renderer unabhängig vom sichtbaren Panel.
- Automatische PNGs bei `Save aktiv` weiterhin erst nach vollständigem Wasserfall und danach je Wasserfallzeit, auch unter anderem LFN-Scout-Panel. Keine WAV-/PCM-Aufnahme aus dem Analyzer.
- Optionale Radiushistogramme AUS/letztes/letzte vier rechts untereinander im Wasserfall, mit Quelle/Zeiten/Fehlstatus.
- Neue Anforderungen, zusätzliche Parameter und Testfälle T-44…T-59, ausführliches Repo-Mapping-Gate, Datenvertrags- und Testhost-Dokument.
- Alle bisherigen Funktionen (v0.5) samt ursprünglicher 146-Parameterreferenz erhalten.
- **Noch nicht durchgeführt:** echter Repo-Abgleich, App-Code, Build, Hardwaretests, Test-PASS.

## v0.7 – 19.09.2026 · Abschluss-Audit / vollständig bebilderte Übergabe

- Alle **13 vorhandenen nicht ausdrücklich verworfenen UI-Bilddateien** beigefügt, jeweils mit eindeutiger Inventar-ID, SHA-256, Freigabe-/Historienstatus und konkreter Verwendung. Zwei ausdrücklich abgelehnte Entwürfe konsequent ausgeschlossen. Drei breite/frühe Layouts nicht als verbindliche Hochformat-Vorlage geführt.
- Veralteten Asset-Verweis auf `04_UI_Gesten_v0.5.md` korrigiert. Separate bildbezogene normative Prüfungen, Hinweis auf Bildfehler und die neu schematisierte rechte Histo-Spalte aufgenommen.
- Neue Anforderungen `SYS-HND-001…004`, `AN-HND-001…012`, Tests `T-60…T-71`, aktualisiertes Repo-Gate, Dokumenten-Audit und konsistente Übergabeordnung.
- Kein Quellcode geändert, keine Android-APK gebaut, keine Hardware-/Mikrofontests als bestanden behauptet.

