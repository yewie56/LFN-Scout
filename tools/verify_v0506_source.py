#!/usr/bin/env python3
"""Static contracts only; no Android/Compose compilation or device validation."""
from pathlib import Path
r=Path(__file__).resolve().parent.parent
app=r/'app/src/main/java/de/brummbeobachter/app'
ctl=(app/'LiveAnalyzerController.kt').read_text()
ui=(app/'LiveAnalyzerScreen.kt').read_text()
settings=(app/'LiveAnalyzerSettingsPanel.kt').read_text()
export=(app/'AnalyzerInspectionExport.kt').read_text()
core=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt').read_text()
build=(r/'app/build.gradle.kts').read_text()
checks={
    'version name / monotonic code': ('versionName = "0.5.06"' in build and 'versionCode = 149' in build) or ('versionName = "0.5.07"' in build and 'versionCode = 150' in build),
    'package name preserved': 'applicationId = "de.brummbeobachter.app"' in build,
    'manual save request marked separately': 'return export(snap, manual = true)' in ctl,
    'automatic export has independent default': 'private fun export(snap: Snapshot, manual: Boolean = false)' in ctl,
    'manual completion event increments ONLY for manual results': ctl.count('if (manual) { manualSaveMessage') == 2,
    'manual completion event passed to UI': 'manualSaveEvent, manualSaveMessage)' in ctl,
    'popup independent of scrolling area': ui.index('} // scrolling content') < ui.index('if(saveMessage.isNotEmpty()) Box(Modifier.align(Alignment.TopCenter)'),
    'popup timer': 'LaunchedEffect(saveMessage)' in ui and 'delay(2200)' in ui,
    'no legacy pending save state': 'pendingManualSave' not in ui,
    'short tap separate from long press': 'onTap = {' in ui and 'onLongPress = {' in ui,
    'max hold core reset': 'fun resetMaxHold()' in core and 'maxHoldPower.fill(0.0)' in core,
    'max hold subscriber reset': 'session?.resetMaxHold()' in ctl,
    'visible selection independent': 'fun setSpectrumVisibility(' in ctl and 'show_current' in ctl and 'show_max_hold' in ctl,
    'both max and current rendered UI': 'if(state.showMaxHold) curve(state.maxHold' in ui and 'if(state.showCurrent) curve(state.live' in ui,
    'both max and current rendered PNG/JPG': 'if (s.showMaxHold) trace(s.maxHold' in ctl and 'if (s.showCurrent) trace(s.live' in ctl,
    'max clr 48 dp': 'if (state.showMaxHold) Box(Modifier.size(48.dp)' in ui,
    'max clr two lines': 'Text("MAX"' in ui and 'Text("CLR"' in ui,
    'setup persists three independent switches': 'setup_${slot}_current' in settings and 'setup_${slot}_average' in settings and 'setup_${slot}_max_hold' in settings,
    '48k and 8k distinct visibility prefs': '"show_current", "show_average", "show_max_hold"' in ctl,
    'diagnostic export includes visibility prefs': '"show_current", "show_average", "show_max_hold"' in export,
    'no rolling change in screen': 'AnalyzerModeCoordinator.finishStopped(context)' in ui,
}
for name,ok in checks.items():print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'STATIC V0506 {sum(checks.values())}/{len(checks)}')
if not all(checks.values()):raise SystemExit(1)
