# LFN-Scout v0.5.38 – Build und Cloud-Warteschlangentest

1. ZIP direkt nach `C:\LFN538` entpacken, ohne zusätzlichen Oberordner.
2. Handy per USB verbinden und USB-Debugging autorisieren.
3. `VORTEST_INSTALL.bat` starten. `Builds/LAST_VORTEST_RESULT.txt` und das VORTEST-Log hochladen.
4. Rollingbuffer starten, Diagnose-Log exportieren. Der bereits in v0.5.37 bestätigte Bucket muss als `retained_sent=1`, `pending_upload=0` erscheinen, sofern derselbe Teilnehmerkey genutzt wird und 24 h noch nicht abgelaufen sind.
5. `next_delete_at_ms` ist die früheste erlaubte Löschzeit in Unix-Millisekunden; `0` heißt keine bekannte Frist. Die genaue Zeit lässt sich unter Windows mit PowerShell berechnen: `[DateTimeOffset]::FromUnixTimeMilliseconds(1791838803414).ToLocalTime()` (nur Beispieldatum).
6. Nach Ablauf von 24 h Rollingbuffer neu starten: `deleted_expired=1` muss erscheinen, sofern ein alter Datensatz vorhanden ist. Nach dem nächsten Start `total_local=0` für diesen alten Bucket.
7. Für Offline-Retry: während einer neuen Halbstunde Netzwerk aus; `pending_upload>=1` erwartet. Nach Rückkehr des Netzwerks Rollingbuffer erneut starten; `newly_sent>=1`, `pending_upload=0`, `retained_sent>=1` erwartet.
8. Keine Cloud-Übertragung der vier Impulskennwerte, Filter-/Epochenwerte oder aggregierten subjektiven Bewertungen. Keine SQL-Migration.

**Status:** Nur Quellkandidat; Gerätetests der v0.5.38 noch offen. Bei sensiblen Angaben Log vor Weitergabe prüfen.
