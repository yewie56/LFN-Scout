from pathlib import Path
root=Path(__file__).parent
m=(root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
e=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
p=(root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text(encoding='utf-8')
g=(root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks=[
('version', 'versionName = "0.4.72"' in g and 'versionCode = 112' in g),
('compact ui rows', 'HELICORDER_ROW_HEIGHT_DP = 11f' in p),
('legend left before time', 'width(28.dp)' in m and 'width(38.dp)' in m),
('vertical legend ui', 'rotationZ = -90f' in m),
('dense trace policy', 'TRACE_DENSITY_FACTOR = 1.35f' in p),
('5 minute axis ui', 'for (minute in 0..30 step 5)' in m),
('export brummbeobachter', 'Download/LFN-Scout/' in e and 'LFN-Scout/Seismik' not in e),
('compact export rows', 'val rowHeight = 17f' in e),
('vertical export legend', 'canvas.rotate(-90f' in e),
('dense export trace', 'TRACE_DENSITY_FACTOR' in e),
('5 minute axis export', 'for (minute in 0..30 step 5)' in e),
]
for n,ok in checks: print(('PASS' if ok else 'FAIL'), n)
raise SystemExit(0 if all(ok for _,ok in checks) else 1)
