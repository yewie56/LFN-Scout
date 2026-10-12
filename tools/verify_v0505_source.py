#!/usr/bin/env python3
"""Fast static coverage of required v0.5.05 source contracts. NOT an Android build."""
from pathlib import Path
r = Path(__file__).resolve().parent.parent
app = r / 'app/src/main/java/de/brummbeobachter/app'
ctl = (app/'LiveAnalyzerController.kt').read_text()
mode = (app/'AnalyzerModeCoordinator.kt').read_text()
ui = (app/'LiveAnalyzerScreen.kt').read_text()
sets = (app/'LiveAnalyzerSettingsPanel.kt').read_text()
export = (app/'AnalyzerInspectionExport.kt').read_text()
timing = (r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerTiming.kt').read_text()
tests = (r/'analyzer-core/src/test/kotlin/de/brummbeobachter/app/AnalyzerTimingTest.kt').read_text()
checks = {
    'version 0.5.05 / 148': 'versionName = "0.5.05"' in (r/'app/build.gradle.kts').read_text() and 'versionCode = 148' in (r/'app/build.gradle.kts').read_text(),
    'idle sample rate safe': 'AnalyzerTiming.displayRateHz(direct48k, decimationFactor)' in ctl,
    '8k restored factor cached': 'decimationFactor = prefs.getInt("decimation_factor", 8)' in ctl,
    '8k cached values protected by controller lock': 'synchronized(lock)' in ctl,
    'idle wideband factor test': 'restoredIdleModeNeverCrashesOnOldWidebandFactor' in tests,
    'strict LF rate retained': 'require(decimationFactor in allowedDecimationFactors)' in timing,
    'pause button separate': 'LiveAnalyzerController.setPaused(!state.paused)' in ui,
    'stop uses coordinator': 'AnalyzerModeCoordinator.stopMeasurement(context)' in ui,
    'restore only after stopped exit': 'if (LiveAnalyzerController.snapshot().running) return false' in mode,
    'restoration waits for rolling': 'RollingAudioEngine.isRunning && MicCaptureArbiter.owner == "rolling"' in mode,
    'no premature RESTORE_DONE': mode.index('"RESTORE_RESULT"') < mode.index('"RESTORE_DONE"'),
    'restore on failure remains pending': 'if (!restored) return false' in mode,
    'setup JSON export': 'fun exportSetups(context: Context)' in export,
    'slot 4 exported': 'Setup_4_48k.json' in export,
    'diagnostic zip export': 'fun exportDiagnostics(context: Context)' in export,
    'diagnosis only AUDIO_DIAG lines': 'it.contains("AUDIO_DIAG_")' in export,
    'no private raw file dumping': 'pending_observations' not in export and 'contentResolver' in export,
    'manual export buttons': 'Setups als JSON exportieren' in sets and 'Diagnose-ZIP exportieren' in sets,
    'export on setup save': '.commit()' in sets and 'AnalyzerInspectionExport.exportSetups(context)' in sets,
    'export once on app launch': 'AnalyzerInspectionExport.exportOnceAfterUpdate' in (app/'MainActivity.kt').read_text(),
    'no direct diagnose STOP circumventing coordinator': 'LiveAnalyzerController.stop();onRefresh()' not in sets,
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' ' + name)
print(f'STATIC V0505 {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
