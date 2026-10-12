# v0.5.15 – Einmalige Migration bisheriger JSON-Setups

## Implementiert

- Beim ersten Start **vor** dem automatischen v2-Export: im bekannten App-Downloadordner `Download/LFN-Scout/Setups/` nach `Setup_1.json`, `Setup_2.json`, `Setup_3.json`, `Setup_4_48k.json` suchen. Auf Android 10+ erfolgt der Zugriff über MediaStore; darunter über den bestehenden Dateiweg.
- Nur Dateien mit `schema = lfn-scout-analyzer-setup-export-v1` und passender Slotnummer werden als Altdateien behandelt. Daten werden mit einer ausdrücklich erlaubten Typ-/Schlüsselliste in vorhandene `setup_N_*`-Preferences eingelesen.
- Vorhandene interne Werte haben Vorrang: bei einer Abweichung Abbruch für den betreffenden Slot; **keine automatische Übernahme, kein Löschen, kein Überschreiben durch den v2-Export**. Kein stilles Mischen zweier unterschiedlicher Versionen.
- Nach Commit werden sämtliche importierten Werte aus dem Preferences-Store zurückgelesen und verglichen. Nur bei Erfolg wird die alte öffentliche JSON-Datei gelöscht. Auf Fehler kann beim nächsten Start erneut versucht werden. Eine Migrationserfolgskennung wird je Slot gesetzt.
- Das Schema v2 bleibt der lesbare gemeinsame Export. Eine allgemeine Importfunktion für beliebige JSON-Dateien oder ein Ordnerpicker wurde nicht implementiert.

## Technische Grenzen

- **Bereits von einem früheren v2-Export überschriebene Altdateien können nicht wiederhergestellt werden.** Die Migration behandelt bestehende v2-Dateien als aktuelle Exportkopien und löscht sie nicht.
- Das neue Schema v2 ist eine gemeinsame *Exportdarstellung*. Die App benutzt intern weiterhin die bisherigen `setup_N_*`- und `mode_*`-Preferences; eine vollständige Umstellung auf ein neues internes Schema erfolgte nicht.
- Fehlende alte Werte werden **nicht erfunden**; sie bleiben intern undefiniert, bis sie explizit gespeichert werden. JSON v2 kennzeichnet sie weiterhin mit `null` und `MISSING_IN_SAVED_SLOT`.
- Externe Dateien, die Android nicht über die bekannte Download-Collection bereitstellt, werden nicht automatisch verarbeitet. Es wird keine breite Dateiberechtigung verlangt.
- Ein Konflikt bleibt bewusst zur manuellen Klärung stehen. In diesem Fall wird der v2-Export gestoppt, damit die vorhandene Altdatei erhalten bleibt.

## Abnahme / Tests

1. Frische Installation mit vier gültigen v1-Dateien: Import, Rücklesen, Löschen der vier Altdateien, danach v2-Export.
2. Bereits gespeicherter interner Wert unterscheidet sich vom Export: unveränderte Daten, v1 bleibt liegen, kein überschreibender v2-Export.
3. Falscher Slot, ungültige Typen, unbekanntes Feld, defekte JSON-Datei, MediaStore-Fehler und Löschfehler: keine Datenlöschung, erneut versuchbar.
4. Bereits vorhandene v2-Datei: erhalten; keine Rückwärtsmigration oder Löschung.
5. Android-Build, START/STOP, 8k/Direkt-Rolling-Restore sowie Xiaomi-Gerätetest erforderlich.

**Teststand:** Quellcodeimplementierung; Android-Build und Geräte-/MediaStore-Migrationstest noch nicht ausgeführt. Die Migration darf bis zu solchen Tests nicht als geräteverifiziert bezeichnet werden.
