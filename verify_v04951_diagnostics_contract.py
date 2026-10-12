#!/usr/bin/env python3
"""Static callback regression, not a replacement for :app:compileDebugKotlin."""
from pathlib import Path
import re, sys
r=Path(__file__).resolve().parent
c=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text(encoding='utf-8')
gw=(r/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text(encoding='utf-8')
core=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt').read_text(encoding='utf-8')
g=(r/'app/build.gradle.kts').read_text(encoding='utf-8')
checks={
    '3/3 callback references': c.count('startRawLf(::accept, ::acceptDiagnostics, analyzerGainDb, fftHopSamples)') == 3,
    'Controller callback declared once': len(re.findall(r'private\s+fun\s+acceptDiagnostics\s*\(\s*diag\s*:\s*AnalyzerRawLfSession\.Diagnostics\s*\)',c)) == 1,
    'Callback stores latestDiag guarded by running': 'synchronized(lock) { if (running) latestDiag = diag }' in c,
    'Gateway requires matching diagnostics lambda': re.search(r'onDiagnostics\s*:\s*\(AnalyzerRawLfSession\.Diagnostics\)\s*->\s*Unit',gw) is not None,
    'Core invokes diagnostics callback': 'onDiagnostics(diag)' in core,
    'v0.4.95.1/versionCode136, compatible app ID': 'versionName = "0.4.95.1"' in g and 'versionCode = 136' in g and 'applicationId = "de.brummbeobachter.app"' in g,
    'No second mic in controller': 'AudioRecord(' not in c,
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()): sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} static API checks; Android compile NOT RUN')
