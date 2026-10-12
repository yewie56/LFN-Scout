#!/usr/bin/env python3
"""v0.5.09 integration invariants: source-level only, NOT an Android/Compose build."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
core=r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app'
app=r/'app/src/main/java/de/brummbeobachter/app'
read=lambda path:path.read_text(encoding='utf-8')
conf=read(core/'DirectAnalyzerConfig.kt')
fir=read(core/'DirectFirDecimator.kt')
fft=read(core/'LiveAnalyzerCore.kt')
session=read(core/'AnalyzerRawLfSession.kt')
timing=read(core/'AnalyzerTiming.kt')
peaks=read(core/'AnalyzerPeakMarkers.kt')
svc=read(app/'AnalyzerDirectAudioService.kt')
mode=read(app/'AnalyzerModeCoordinator.kt')
ctrl=read(app/'LiveAnalyzerController.kt')
ui=read(app/'LiveAnalyzerScreen.kt')
settings=read(app/'LiveAnalyzerSettingsPanel.kt')
tone=read(app/'AnalyzerTestSignal.kt')
gradle=read(r/'app/build.gradle.kts')
checks={
    'version 0.5.09/152': 'versionName = "0.5.09"' in gradle and 'versionCode = 152' in gradle,
    'all input rates': all(x in conf for x in ('8000, 11025, 16000, 22050, 44100, 48000, 96000','0..12','rate % effectiveFactor(selected) == 0')),
    'all nine FFT sizes': '64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384' in conf,
    'all seven transformation intervals': '10, 15, 20, 25, 30, 50, 100' in conf,
    'direct FIR normalized and source-independent': 'class DirectFirDecimator' in fir and 'val sum = array.sum()' in fir and 'samples % factor == 0L' in fir,
    'core nine windows': all(x in fft for x in ('HANN, HAMMING, BLACKMAN, RECTANGULAR, COSINE, BLACKMAN_HARRIS, FLAT_TOP, TRIANGULAR, WELCH', 'coherentGain')),
    'core accepts hop larger than window': 'require(hopSize > 0)' in fft and 'require(hopSize in 1..fftSize)' not in fft,
    'core optional DC subtraction and direct alpha': 'if (subtractDc) dc else 0.0' in fft and 'smoothingAlpha ?:' in fft,
    'session matches actual sample rate and uses new FIR': all(x in session for x in ('directDecimator?.accept(frame.samples)', 'frame.sampleRateHz != inputRateHz', 'DirectAnalyzerConfig.outputRate', 'smoothingAlpha, subtractDc')),
    'legacy LF factor validation preserved': 'inputRateHz == 8000 && decimationFactor in intArrayOf(4,8,16)' in session,
    'old timing 33 hop regression retained': 'fun validHop(hop: Int, rateHz:' in timing,
    'capture service uses selected rate end to end': all(x in svc for x in ('mode_48_direct_sample_rate','AudioRecord.getMinBufferSize(rate,','AudioRecord(source, rate,','active.sampleRate != rate','rawPcm.publish(input, count, rate')),
    'input source manual/automatic': all(x in svc for x in ('"AUTO"','"UNPROCESSED"','"MIC"','"VOICE_RECOGNITION"')),
    'capture error and exclusive ownership retained': 'MicCaptureArbiter.tryAcquire("analyzer_48k")' in svc and 'lastError = e.message' in svc,
    'direct/rolling state restore preserved': 'stopMeasurement(context:' in mode and 'finishStopped(context:' in mode and 'chosen_mode' in mode,
    'controller uses real input/output rates': all(x in ctrl for x in ('directInputRateHz', 'DirectAnalyzerConfig.outputRate', 'directMode = direct48k','directSmoothingAlpha','directSubtractDc')),
    'controller setup profile stores direct keys': '"direct_sample_rate", "direct_audio_source"' in ctrl and '"mode_48_$key"' in ctrl,
    'setup stores direct configuration': all(x in settings for x in ('setup_${slot}_direct_sample_rate','setup_${slot}_direct_test1_on','setup_${slot}_direct_smoothing_alpha')),
    'setup restores capture before range': settings.index('LiveAnalyzerController.setDirectCapture(context, loadedRate') < settings.index('LiveAnalyzerController.setFrequencyRange(context,'),
    'direct settings available in UI': all(x in settings for x in ('Stay Awake', 'Subtract DC','Peak Markers','VOICE_RECOGNITION','Testsignal 1 / 2')),
    'test tones selected per direct profile': all(x in tone for x in ('mode_48_direct_test1_hz','mode_48_direct_test2_hz','if(on1)','if(on2)')),
    'peak markers are drawn in UI and PNG': 'AnalyzerPeakMarkers.find' in ui and 'AnalyzerPeakMarkers.find' in ctrl and 'fun find(' in peaks,
    'waterfall clock, x-axis and colorbar retained': all(x in ui for x in ('WaterfallTimeAxis.ticks(state.rowEpochMillis','val plotLeftDp = 54.dp','val barRight=barLeft+labelWidth+2.dp.toPx()')),
    'manual camera popup remains anchored': 'Popup(alignment=Alignment.TopStart' in ui,
    'testplan and design exist': (r/'docs/project/TESTPLAN_v0.5.09.md').exists() and (r/'docs/project/DIRECT_ANALYZER_v0.5.09.md').exists(),
}
for name,ok in checks.items(): print(f"{'PASS' if ok else 'FAIL'}: {name}")
print(f"STATIC DIRECT CONTRACT {sum(checks.values())}/{len(checks)} (NOT Android build)")
if not all(checks.values()):raise SystemExit(1)
