# v0.5.45.2 – zeitliche Rauschbodenmittelung

Die bisherige Einmessung mittelte bereits zeitliche lineare FFT-Leistungen. Diese Version trennt die Mittelung in eine testbare `NoisePowerAverage`-Klasse aus und verwirft ungültige Fenster vollständig. Es werden nur die **momentanen** FFT-Leistungen `liveDbFs` vor bestehender Noise-Floor-Kompensation berücksichtigt, nicht `averageDbFs`, `maxHoldDbFs` oder dB-Durchschnittswerte.

Mit dem langen Druck auf den Analyzer-Rauschboden-Button startet die bestehende 120-s-Einmessung. Abbruch/inkompatible Einstellungen zerstören das bisherige gespeicherte Profil nicht. Speicherung weiter `noise_floor_reference_v1.json`, ergänzt um die Zahl verworfener FFT-Fenster und die Durchschnittsmethode. Auch Rolling-FFT wird nur dann parallel gemittelt, wenn deren Audiodienst läuft. Ein physikalischer Kurzschluss ist nur mit dafür geeigneter externer Hardware zulässig.

Tests: `NoisePowerAverageTest` (arithmetische Mittelung, ungültige Fenster, Reset). Android-Gradle-Tests und Gerätetest ausstehend; WAV/FAST/Cloud unverändert. Die separate Rolling-FFT bleibt eine bekannte Abweichung von der Zielarchitektur des gemeinsamen FFT-Rechenkerns.
