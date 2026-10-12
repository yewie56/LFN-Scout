#!/usr/bin/env python3
"""Static source regression v0.4.95, not an Android/Compose build."""
from pathlib import Path
r=Path(__file__).resolve().parent
ct=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
ui=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
tm=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text()
gw=(r/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text()
gr=(r/'app/build.gradle.kts').read_text()
checks={
'Version monotonic': 'versionName = "0.4.95"' in gr and 'versionCode = 135' in gr,
'FFT hop includes 10': 'intArrayOf(10, 50, 100, 200, 500, 1000)' in tm,
'WAV path not modified by hop': 'startRawLf(::accept, ::acceptDiagnostics, analyzerGainDb, fftHopSamples)' in ct and 'hopSize = hopSamples' in gw,
'Default -120..-60': 'private var displayMinDb = -120' in ct and 'private var displayMaxDb = -60' in ct,
'Preset -60 retained': 'listOf(-60,-40,0)' in ui,
'Both dB axes scaled': 'for(db in state.minDisplayDb..state.maxDisplayDb step 20)' in ui and 'for(db in state.maxDisplayDb downTo state.minDisplayDb step 20)' in ui,
'UI and PNG colors use same scale': 'color(row[bin],minDb,maxDb)' in ui and 'color(row[bin],s.minDisplayDb,s.maxDisplayDb)' in ct,
'Head title matches range': 'Text("LFN-Scout · 0–${state.rangeHz} Hz"' in ui,
'Single data status below head': 'Text(state.status,' in ui and 'else "Live · 8 kHz → 1 kHz FIR · dBFS · Gain +${analyzerGainDb} dB uncal"' in ct,
'Redundant spectrum and legend removed': 'Spektrum · 0–${state.rangeHz}' not in ui and '▌2–25 Hz' not in ui,
'L50 and L90 inside plot': 'Modifier.align(Alignment.TopEnd)' in ui and 'Text("L50(2–25)' in ui and 'Text("L90(2–25)' in ui,
'Floor off by default and persistent': 'private var showFloor = false' in ct and 'getBoolean("show_floor", false)' in ct and 'setTraceVisibility(context,state.showAverage,it)' in ui,
'Waterfall timebase independent': 'effectiveWaterfallIntervalMs(fftHop, intervalMs)' in ct and 'advanceWaterfall(' in ct and 'historyRows(waterfallIntervalMs)' in ct,
'Diagnostic callback correct': 'session?.callbackErrors' in ct and 'session?.spectrumCallbackErrors' not in ct,
'Saved setup includes new UI': 'setup_${slot}_display_max' in ui and 'setup_${slot}_floor' in ui and 'setup_${slot}_water_interval' in ui,
'No second microphone': 'AudioRecord(' not in ct and 'AudioRecord(' not in ui,
}
for name,good in checks.items(): print(('PASS' if good else 'FAIL')+': '+name)
assert all(checks.values()),'v0.4.95 static contract failed'
print('PASS: %d/%d static checks; Android/Compose build NOT RUN'%(len(checks),len(checks)))
