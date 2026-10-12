from pathlib import Path
m=Path('app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
p=Path('app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text(encoding='utf-8')
e=Path('app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
g=Path('app/build.gradle.kts').read_text(encoding='utf-8')
checks=[
('versionName', 'versionName = "0.4.71"' in g),
('versionCode', 'versionCode = 111' in g),
('double trace width', 'TRACE_STROKE_WIDTH_PX = 1.6f' in p and 'strokeWidth = SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX' in m),
('legend value', 'LEGEND_VALUE_NM_PER_SEC = 40000.0' in p and '40000\\nnm/s' in m),
('WBBU zoom', 'wbbu_zoom' in m and 'wbbu_double_tap_reset' in m),
('near zoom', 'near_zoom' in m and 'near_double_tap_reset' in m),
('zoom range', 'MAX_ZOOM = 5f' in p and 'MIN_ZOOM = 1f' in p),
('save button', 'Text("Plot speichern")' in m),
('save success', 'Plot erfolgreich gespeichert' in m and 'SEISMIC_PLOT_SAVE_OK' in m),
('save failure', 'SEISMIC_PLOT_SAVE_FAILED' in m),
('exporter exists', Path('app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').exists()),
('export full near plot', 'renderNearPlot(snapshot)' in e and 'helicorderRows.forEachIndexed' in e),
('export WBBU', 'HLNUG_WBBU_IMAGE' in e and 'decodeByteArray' in e),
('public path', 'Download/LFN-Scout/Seismik/' in e),
('png names', 'Seismik_${date}_$safe.png' in p),
]
for name,ok in checks: print(('PASS' if ok else 'FAIL'), name)
print(f'{sum(x[1] for x in checks)}/{len(checks)} PASS')
raise SystemExit(0 if all(x[1] for x in checks) else 1)
