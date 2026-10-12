#!/usr/bin/env python3
"""Static checks for the v0.4.94 Android UI contract. NOT an Android build."""
from pathlib import Path
r=Path(__file__).resolve().parent
core=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt').read_text()
gateway=(r/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text()
controller=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
screen=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
timing=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text()
checks={
 'Raw rate remains 8000Hz': 'frame.sampleRateHz != 8000' in core,
 'Analyzer only subscriber': 'hub.subscribe(queueCapacity)' in core,
 'FFT hop passed to FFT core': 'LiveAnalyzerCore(1000, 4096, hopSize, maxFrequencyHz)' in core,
 'Gateway carries hop': 'hopSize = hopSamples' in gateway,
 'Controller callback member exists': 'session?.callbackErrors' in controller and 'spectrumCallbackErrors' not in controller,
 'Timing preferences persistent': 'putInt("fft_hop_samples", fftHop)' in controller and 'putInt("waterfall_hop_spectra", waterfallHop)' in controller,
 'FFT/waterfall configurations distinct': 'allowedFftHops' in timing and 'allowedWaterfallHops' in timing,
 'Waterfall newest at top': 'rows.takeLast(capacity).asReversed()' in screen and 'takeLast(height).asReversed()' in controller,
 'Both dB tick grids present': 'for(db in -140..0 step 20)' in screen and 'for (db in -140..0 step 20)' in controller,
 'Frequency ticks present in plots': 'frequencyTicks(state.rangeHz)' in screen and 'tickHz()' in controller,
 'Hz label separate from numeric tick': 'Frequenz (Hz)' in screen and 'Frequenz (Hz)' in controller,
 'UI refresh no longer 500 ms': 'delay(500)' not in screen and 'delay(if (state.fftHopSamples <= 50) 50L else 100L)' in screen,
 'No old fixed history rows': 'HISTORY_ROWS' not in controller and 'HISTORY_ROWS' not in screen,
 'No additional mic owner': 'AudioRecord(' not in core and 'AudioRecord(' not in controller,
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL')+': '+name)
assert all(checks.values()),'Static contract failures'
print(f'PASS: {len(checks)}/{len(checks)} static checks; Android/GUI testing still required')
