"""v0.4.91 static contract test; NOT an Android Build, device or calibration test."""
from pathlib import Path
import sys
r=Path(__file__).resolve().parent
def read(f): return (r/f).read_text(encoding='utf8')
a='app/src/main/java/de/brummbeobachter/app/'
c='analyzer-core/src/main/kotlin/de/brummbeobachter/app/'
app=read('app/build.gradle.kts'); ctl=read(a+'LiveAnalyzerController.kt'); ui=read(a+'LiveAnalyzerScreen.kt')
gw=read(a+'AnalyzerSessionGateway.kt'); core=read(c+'LiveAnalyzerCore.kt'); raw=read(c+'AnalyzerRawLfSession.kt'); fir=read(c+'RawLfDecimator.kt')
roll=read(a+'RollingAudioService.kt'); direct=read(a+'AudioRecorder.kt')
checks={
'v0.4.91/131 and unchanged identity': 'versionName = "0.4.91"' in app and 'versionCode = 131' in app and 'applicationId = "de.brummbeobachter.app"' in app,
'raw 8 kHz source in both old microphone owners': 'rawPcm.publish(input, count, sourceRate' in roll and 'rawPcm.publish(input, count, sourceRate' in direct,
'one shared microphone, independent LF subscriber': 'AnalyzerSessionGateway.startRawLf' in ctl and 'AnalyzerRawLfSession(rawPcm' in gw and 'AudioRecord(' not in ctl+ui+raw+fir,
'129 tap filtered 8-to-1 instead of rounded legacy PCM': 'RawLfDecimator()' in raw and 'val taps: Int = 129' in fir and 'val cutoffHz: Double = 280.0' in fir and 'core.acceptNormalized(down)' in raw,
'legacy WAV rate and path untouched': all(t in read(a+'AudioRecordingPolicy.kt') for t in ['SOURCE_SAMPLE_RATE_HZ = 8000','TARGET_SAMPLE_RATE_HZ = 1000','DURATION_SECONDS = 120']) and 'AnalyzerSessionGateway.pcm.publish(' in roll and 'AnalyzerSessionGateway.pcm.publish(' in direct,
'double PCM and current fft algorithm': 'fun acceptNormalized(samples: DoubleArray)' in core and 'fun accept(samples: ShortArray' in core,
'signal validation and stale state': 'if (!realData)' in ctl and 'snap.signalAgeMs > 3000' in ctl and 'current == null || !validSignal' in ctl,
'diagnostics include stage peaks rms rate drops': all(t in ctl+ui+raw for t in ['rawPeak','rawRms','lfRms','sourceRateHz','droppedFrames','discontinuityCount']),
'layout classic waterfall time horizontal and frequency vertical': 'val x=width-rows.size+i' in ui and 'out[y*width+x]' in ui and 'FREQUENCY top to bottom' in ui,
'no impulse waterfall or fake audio controls': 'keine Impulsanalyse' in ui and 'Noch nicht aktiv' in ui and 'makeImpulseWaterfall' not in ui,
'PNG offscreen and only valid': 'private fun renderPng(' in ctl and 's.rows.takeLast(HISTORY_ROWS)' in ctl and 'history.size >= HISTORY_ROWS' in ctl,
'new regression tests exist': (r/'tools/analyzer_live491_selftest.kt').is_file() and (r/'analyzer-core/src/test/kotlin/de/brummbeobachter/app/RawLfDecimatorTest.kt').is_file(),
'official docs current': 'v0.4.91' in read('README.md') and 'v0.4.91' in read('CHANGELOG.md') and (r/'docs/project/ANALYZER_LIVE_v0.4.91.md').is_file(),
'13 original concepts preserved':len(list((r/'docs/project/analyzer_v0.7/assets/concepts').glob('C*.png'))) == 13,
'53 private recovery WAVs unchanged':len(list((r/'supabase/storage_backup').rglob('*.wav'))) == 53 and sum(x.stat().st_size for x in (r/'supabase/storage_backup').rglob('*.wav'))==12722332,
'no private machine-local config':not (r/'local.properties').exists() and not (r/'.tools/java17.path').exists() and not list(r.rglob('.git')),
}
for name,passed in checks.items():print(('PASS' if passed else 'FAIL')+': '+name)
if not all(checks.values()):sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} static v0.4.91 checks; Android/phone NOT RUN')
