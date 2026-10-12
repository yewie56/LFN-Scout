# Standalone-Testhülle – gleicher Analyzer-Kern, keine separate Produktentwicklung

**ID:** LFN-AN-TESTHOST · **Version:** 0.7 · **Status:** Architektur-/Arbeitsplan.

## Ziel

Vor komplexen UI- und Bestandsintegrationsänderungen eine **kleine, unabhängig startbare Android-Testhülle** aufbauen. Sie testet FFT, Superfast, Kalibrierung, L50/L90, Monitoring, Wasserfall, Save und simulierte Radiushistogramme mit synthetischen Quellen. Sie ist bewusst **kein fertiger unabhängiger Spectroid-Clone**, der später vollständig transplantiert wird.

## Geteilter Code / getrennte Adapter

```text
shared analyzer-core (identische Quellen + Tests)
 ├─ Capture/Measurement/Statistics/Monitor/Render/Save/Setup (interfaces)
 ├─ Android TestHost: FakePCM/FakeHistogram/FakeClock, optionale echte Mic-Route
 └─ LFN-Scout Host: SharedCapture/ExistingRecorder/RealHistogram/PanelHost
```

Für beiden Hosts identische DSP-Testvektoren, `AnalyzerFrame`-Schema, Snapshot-Ausgabe und Parametervalidierung. Testhost-spezifisch nur Dependency Injection, einfache Aktivitätsnavigation und Fake-Quellen. Keine zweite produktive Nutzer-/Cloud-Datenbank oder Audio-Dateiaufzeichnung.

## Mindest-Testhülle

1. Start/Pause/Fortsetzen und Live-/Fake-Umschaltung; Status `Fake` deutlich sichtbar.
2. Synthetic 47-Hz-Sinus, Transienten, PCM-Clip, Noise-Referenz, Bandpass und Verstärkungsprüfung.
3. FakeClock für `T_wf=120 s`; Offscreen-PNG auch bei unsichtbarer Test-Activity prüfen.
4. FakeHistogram-Snapshots mit IDs/Zeit und Modi AUS/1/4.
5. Automatisierte Unit-/Instrumented-Tests und exportierbare PASS/FAIL-Protokolle.
6. Frühzeitige Integration in LFN-Scout nach der Kernel-Validierung; **echte Recorder- und Panelwechseltests sind erst dort vollständig möglich**.

## Abgrenzung und Nachweis

Ein Testhost-PASS ist kein LFN-Scout-Release. Test T-59 vergleicht identische Fixtures in beiden Hosts; Freigabe benötigt T-44…58 inklusive realem Recorder. Kein unabhängiger Release-Zweig, kein stiller Umbau der Bestandsarchitektur nach erfolgreichem isoliertem Prototyp.

## 3. Abschluss-Gate v0.7

Die Standalone-Testhülle ist ausdrücklich ein **Debug-/Test-Host desselben Analyzer-Kerns**, keine zusätzliche App mit separaten Einstellungen, Quellen oder langjähriger Parallelpflege. Sie muss synthetische PCM-Daten und eine lokale Mikrofonroute unterstützen. Gemeinsamer PCM-Owner, Bestands-Recorder, offscreen PNG unter internem Panelwechsel und echte Radius-Histogramme werden zusätzlich in der integrierten LFN-Scout-App geprüft; ein alleiniger Testhost-PASS genügt für die Produktionsfreigabe nicht.

