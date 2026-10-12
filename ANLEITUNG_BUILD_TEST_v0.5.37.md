# LFN-Scout v0.5.37 – Windows-Build und Cloud-Gerätetest

1. ZIP direkt nach `C:\LFN537` entpacken, ohne zusätzliche Verzeichnisebene.
2. Handy via USB mit aktiviertem USB-Debugging verbinden.
3. `VORTEST_INSTALL.bat` ausführen. `Builds/LAST_VORTEST_RESULT.txt` und VORTEST-Log auf PASS prüfen.
4. App: den gültigen Teilnehmer-Schlüssel konfigurieren; Rollingbuffer + 30-Minuten-Statistik aktivieren. Die lokale Gesamt-CSV darf eingeschaltet bleiben.
5. Mindestens bis zur folgenden HH:00-/HH:30-Grenze aufnehmen. Gesamt-CSV sollte weiterhin unter Download/LFN-Scout erscheinen.
6. Log nach `ROLLING_CLOUD_SYNC` durchsuchen (sent=1 und pending=0 bei Erfolg). Der Backend-Endpunkt `submit_half_hour_statistics` ist bereits deployt; es wird **keine Migration** ausgeführt.
7. In der bestehenden Tabelle `half_hour_statistics` nur prüfen, ob der zugehörige Teilnehmer + Bucket enthalten ist: range_min_hz, range_max_hz, leq_dbfs, l50_dbfs, l90_dbfs, coverage_seconds, valid_epoch_count, quality_status.
8. Offlinefall testen: vor Ablauf eines Buckets Flugmodus einschalten. Erwartung: lokale CSV bleibt, `ROLLING_CLOUD_SYNC` mit pending>=1. Nach Wiederherstellung der Verbindung den Rollingbuffer neu starten oder eine weitere Halbstunde abschließen; auf Nachlieferung prüfen. Gleiche bucket_id bedeutet idempotentes Update statt neuer Zeile.
9. Es dürfen keine Filterstatistiken, einzelnen Sekundenwerte, subjektiven Aggregatwerte oder vier neuen Impulskennwerte hochgeladen werden.
10. Ergebnisdatei, Log und einen anonymisierten Cloud-Datensatz (ohne Schlüssel/Geokoordinaten) für Prüfung hochladen.

**Einschränkungen:** Keine Android-Kompilierung oder Cloud-E2E-Prüfung in diesem Quellpaket. Die Cloud-Nachlieferung wird aktuell bei Rolling-Service-Start und bei Abschluss einer Halbstunde ausgelöst, nicht durch einen unabhängigen zeitgesteuerten Hintergrundjob. Ohne eingerichteten Teilnehmer-Schlüssel kann keine Cloud-Übertragung erfolgen. Der schlüsselfreie Puffer ist je Teilnehmer-Schlüssel isoliert.

## Nachtrag 24-h-Retention
- Nach erfolgreichem Upload im Diagnose-Log `ROLLING_CLOUD_QUEUE_STATUS` kontrollieren. Ein bestätigter Bucket bleibt lokal als `SENT_PENDING_DELETE`; `total_local` zählt auch bestätigte, noch nicht gelöschte Buckets.
- Nach Neustart innerhalb von 24 h darf keine erneute Übertragung desselben Buckets erfolgen. Erst beim folgenden Durchlauf nach 24 h darf die Bereinigung erfolgen.
- Aktuell keine eigene Warteschlangen-GUI; der Status ist im Diagnose-Log sichtbar.
