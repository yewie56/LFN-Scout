from pathlib import Path
import re

root = Path(__file__).resolve().parent
core = (root/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt').read_text()
timing = (root/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text()
ctrl = (root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
screen = (root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
settings = (root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt').read_text()
gateway = (root/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text()
build = (root/'app/build.gradle.kts').read_text()
checks = {
    'new version / package untouched': 'versionCode = 143' in build and 'versionName = "0.5.02"' in build and 'applicationId = "de.brummbeobachter.app"' in build,
    'real frequency unit midpoint helper': 'return (ticks[1] + ticks[2]) / 2.0' in timing,
    'both axes call unit helper': screen.count('AnalyzerTiming.unitFrequencyHz(state.minFrequencyHz,state.rangeHz)') == 2,
    'offscreen unit uses same helper': 'xHz(AnalyzerTiming.unitFrequencyHz(s.minFrequencyHz, s.rangeHz))' in ctrl,
    'mode short label': all(f'WaterfallMode.{a} -> "{b}"' in screen for a,b in [('CURRENT','Akt'),('AVERAGE','MW'),('MAXIMUM','MAX')]),
    'no sign buttons in settings': '± Minimum' not in settings and '± Maximum' not in settings,
    'free waterfall duration input and save': 'historyText.toIntOrNull()' in settings and 'setHistorySeconds(context,seconds)' in settings,
    'dynamic waterfall capacity': 'historyRows(waterfallIntervalMs, historySeconds)' in ctrl,
    'variable auto interval': 'historySeconds * 1_000_000_000L' in ctrl,
    'colorbar measures widest number screen/export': '.maxOf { db ->' in screen and '.maxOf { p.measureText("$it") }' in ctrl,
    'FFT hop includes 5': 'intArrayOf(5, 10, 50, 100, 200, 500, 1000)' in timing,
    'FFT sizes all present': 'intArrayOf(2048, 4096, 8192)' in timing,
    'FFT window coherent gain retained': 'coherentGain = window.sum() / fftSize' in core and 'AnalyzerWindow.BLACKMAN' in core,
    'DSP settings passed through gateway': 'windowType = windowType' in gateway and 'averageTimeSeconds = averageTimeSeconds' in gateway,
    'coupled transform interval': 'setTiming(context,requested,state.waterfallIntervalMs)' in settings,
    'frequency crop screen and export': 'val lo=freq.indexOfFirst' in screen and 'val minBin = s.frequencyHz.indexOfFirst' in ctrl,
    'smoothing alpha and off': 'if (averageTimeSeconds == 0.0) 1.0' in core and 'setDsp(context,state.fftSize,state.windowType,false,state.smoothingTimeSeconds)' in settings,
    'new preferences written': all(('put'+t+'("'+k+'"') in ctrl for t,k in [('Int','fft_size'),('Int','min_frequency_hz'),('Int','history_seconds'),('String','fft_window'),('Boolean','smoothing_enabled'),('Float','smoothing_time_sec')]),
    'setup persistence': all(f'setup_${{slot}}_{k}' in settings for k in ['min_frequency','history_seconds','fft_size','window','smoothing_enabled','smoothing_time']),
    'old shared audio source remains': 'AnalyzerSessionGateway.startRawLf' in ctrl and 'rawPcm' in gateway and 'AudioRecord' not in core,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL')+': '+name)
print('STATIC CONTRACT:',sum(checks.values()),'/',len(checks))
raise SystemExit(0 if all(checks.values()) else 1)
