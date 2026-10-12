# 30-Minuten-Filterstatistik v0.5.31

Implementierter Stand: eigenständige 10-/20-Band-Filterbank, nur während des Rollingbuffer-Betriebs und bei aktiviertem übergeordnetem Statistikschalter. Eigener Schalter Filterstatistik standardmäßig AUS. FILTER MIN=0 Hz, FILTER MAX=100 Hz, lineare Frequenzteilung; separat gespeichert. Ein-Sekunden-Pegel, :00/:30-Buckets, Leq/L50/L90, Qualitätskennzeichnung, optionale CSV pro Bucket im öffentlich zugänglichen Download/LFN-Scout/-Ordner. Kein Upload der Filterwerte. Keine zweite Mikrofoninstanz.

**Einschränkung:** Vorläufige Kaskade aus Hochpass erster Ordnung und Tiefpass erster Ordnung, nicht kalibriert. Bei nominell 0 Hz wird kein Hochpass angewendet (DC bleibt enthalten). Die Filterantwort hat keine steile Bandbegrenzung; Werte nur zu Diagnosezwecken. Kein Anspruch auf Gleichwertigkeit zu den WAV-Impulsfiltern. Der Filterzustand wird bei Bucketübergängen beibehalten.

Für Build und Gerätetest: `ANLEITUNG_BUILD_TEST_v0.5.31.md`. Ein Gradle-/APK-Gerätetest für diese Version muss noch stattfinden.
