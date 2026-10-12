from pathlib import Path
root=Path(__file__).resolve().parent
core=root/'analyzer-core/src/main/kotlin/de/brummbeobachter/app'
app=root/'app/src/main/java/de/brummbeobachter/app'
read=lambda p: p.read_text(encoding='utf-8')
timing=read(core/'AnalyzerTiming.kt')
fir=read(core/'RawLfDecimator.kt')
session=read(core/'AnalyzerRawLfSession.kt')
ctrl=read(app/'LiveAnalyzerController.kt')
gate=read(app/'AnalyzerSessionGateway.kt')
ui=read(app/'LiveAnalyzerScreen.kt')
settings=read(app/'LiveAnalyzerSettingsPanel.kt')
gradle=read(root/'app/build.gradle.kts')
checks={
 'version 144': 'versionCode = 144' in gradle and 'versionName = "0.5.03"' in gradle,
 'package unchanged': 'applicationId = "de.brummbeobachter.app"' in gradle,
 'allowed FIR factors': 'intArrayOf(4, 8, 16)' in timing,
 'factor-dependent taps': 'if (outputRateHz == 500) 513 else 129' in fir,
 'factor-dependent cutoff': 'if (outputRateHz == 500) 215.0 else 280.0' in fir,
 'session factor': 'val decimationFactor: Int = 8' in session,
 'session dynamic rate': 'LiveAnalyzerCore(8000 / decimationFactor' in session,
 'gateway passes factor': 'decimationFactor = decimationFactor' in gate,
 'dynamic waterfall phase': 'fftHop * 1000' in timing and 'intervalMs * sampleRateHz' in timing,
 'stats timebase': 'fun statisticFrames(fftHop: Int, seconds: Int' in timing,
 'stats band configurable': 'statisticsMaxHz' in ctrl and 'statisticsMinHz' in ctrl,
 'stats power averaging': 'statsPowerSum / statsPowerWindow.size' in ctrl,
 'stats cache': 'lastStatsComputeNs >= 1_000_000_000L' in ctrl,
 'stats prefs': '.putInt("statistics_window_sec", windowSec)' in ctrl,
 'decimation prefs': '.putInt("decimation_factor", factor)' in ctrl,
 'stats range visible': 'Statistik ${state.statisticsMinHz}–${state.statisticsMaxHz} Hz' in ui,
 'spectrum stats band': 'val bandLeft=left+((maxOf(state.statisticsMinHz' in ui,
 'cursor y aligned': 'val baseline=top+22.dp.toPx()' in ui and 'val ly = top + 9.dp.toPx()' in ui,
 'waterfall cursor': 'drawLine(cyan,Offset(x,top),Offset(x,bottom)' in ui,
 'cursor in export': 's.cursorFrequencyHz?.takeIf' in ctrl,
 'setup names stored': '.putString("setup_${slot}_name",setupName.trim())' in settings,
 'setup descriptions stored': '.putString("setup_${slot}_description",setupDescription.trim())' in settings,
 'setup stats stored': '.putFloat("setup_${slot}_stat_average"' in settings,
 'setup stats loaded': 'LiveAnalyzerController.setStatistics(context,' in settings,
 'setup decimation loaded': 'LiveAnalyzerController.setDecimation(context,savedFactor)' in settings,
 'UI desired ms dynamic': 'state.fftHopSamples*1000.0/state.sampleRateHz' in settings,
 'image export stats metadata': 'Statistik ${s.statisticsMinHz}–${s.statisticsMaxHz} Hz' in ctrl,
 'new test plan': (root/'docs/project/TESTPLAN_v0.5.03.md').exists(),
 'new product documentation': (root/'docs/project/ANALYZER_v0.5.03.md').exists()
}
for label,valid in checks.items(): print(('PASS' if valid else 'FAIL')+': '+label)
print(f'STATIC v0.5.03: {sum(checks.values())}/{len(checks)}')
raise SystemExit(0 if all(checks.values()) else 1)
