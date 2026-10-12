# LFN-Scout v0.5.39 – Build und Filterbank-Regressionsprüfung

1. ZIP **ohne zusätzlichen Oberordner** nach `C:\LFN539` entpacken.
2. Handy per USB verbinden, USB-Debugging freigeben.
3. `VORTEST_INSTALL.bat` ausführen; `Builds/LAST_VORTEST_RESULT.txt` und neues `VORTEST_*.log` zur Prüfung hochladen.
4. Die JUnit-Tests enthalten `RollingFilterTransferFunctionTest`, inklusive analytischem Vergleich von HP-/LP-Übertragungsfunktionen und 10-/20-Band-Vektoren.
5. Weiterhin physikalisch **unkalibriert**: Filterkennlinien mit realer Audio-Hardware, Quellen und externen Referenzpegeln künftig validieren.
6. Nach 12.10.2026 23:00:03 Uhr deutscher Zeit Rollingbuffer neu starten und Diagnose-Log zur Prüfung der 24-Stunden-Aufbewahrung und Bereinigung exportieren.
7. Auch Cloud-Datenbankinhalt gegenüber lokaler Gesamt-CSV prüfen; hierfür die bestehenden Backend-Zugänge verwenden, keine Credentials in Logs veröffentlichen.

**Ausgangslage:** Windows/ADB v0.5.38 PASS, Cloud-Retention nach Neustart PASS. v0.5.39 Windows-Build und Android-Gerätetest offen. Keine Cloud-Migration.
