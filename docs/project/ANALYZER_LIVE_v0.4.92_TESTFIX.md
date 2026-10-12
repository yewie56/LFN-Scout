# Analyzer v0.4.92 – Regressionstest-Startup

Die Pipeline aus v0.4.91 bleibt unverändert. Bei 8-kHz-Eingang, Decimationsfaktor 8 und FFT-Größe 4096 sind etwa 4,1 s Nutzdaten erforderlich, zuzüglich Filter-Startup. Der erste reine Nullsignal-Test stellte nur 8200 Rohsamples (ca. 1,025 s) bereit und versuchte fälschlich `.last()`. Der neue Test verifiziert zunächst das korrekte Ausbleiben eines FFT-Frames und anschließend mit weiteren 32800 Rohsamples den Nullsignal-Pegel. Es handelt sich nicht um einen durch diesen Test nachgewiesenen Produktionsfehler.

Gerätetest v0.4.92 bleibt offen.
