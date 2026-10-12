# Setup-Export-Korrektur v0.5.16

## Anlass und nachweisbare Konfliktwerte

Alte `Setup_4_48k.json`: `range=22000`; aktueller interner Setup-Slot 4 aus Diagnose: `range=200`. v0.5.15 `migrateOne()` verweigert widersprüchliche Werte; `exportSetups()` warf bei `run()==false` eine `IllegalStateException` ohne näheren GUI-Text.

## Neues Verhalten

- Ein alter v1-Export wird nur bei vollständig kompatiblen bereits vorhandenen Werten eingelesen, committed, nachgelesen und dann gelöscht. Gespeicherte Werte und alte Datei werden bei Konflikt nicht verändert.
- Ein nicht erledigter Slot wird separat unter `Setup_N_CURRENT_v2.json` exportiert; sein alter Normalname bleibt unangetastet. Andere Slots bleiben exportierbar.
- Fehler pro Slot werden in `lfn_scout_setup_migration` als `legacy_error_N` gespeichert, mit Throwable an Android-Log weitergereicht und im Diagnose-ZIP unter `Settings/setup_migration.json` ausgewiesen.
- Exportzähler gibt erfolgreiche neue JSON-Schreibvorgänge zurück. Keine implizite manuelle Konfliktentscheidung. Nach späterer legitimer Behebung kann die Altdatei sicher migriert werden.

## Grenzen

- MediaStore-Sichtbarkeit und Besitzrechte variieren mit Android-Version und Installationshistorie; ein Xiaomi-Test mit vorhandener Altdatei ist zwingend.
- Die v2-Kopie ist weiterhin ausdrücklich read-only, kein allgemeiner Import. Es findet keine Migration des internen Preference-Schemas statt.
- Es werden keine Werte aus `mode_48` und `setup_4` verschmolzen und keine Audio-/DSP-Einstellungen verändert.
