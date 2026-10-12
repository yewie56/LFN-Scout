#!/usr/bin/env python3
"""Source-contract regression checks. Does not replace Android/Compose build or device validation."""
from pathlib import Path
import re
root=Path(__file__).resolve().parent
ui=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt').read_text()
settings=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt').read_text()
ctl=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text()
app=(root/'app/build.gradle.kts').read_text()
main=ui.split('        } else {',1)[1].split('} // end main analyzer view',1)[0]
checks={
    'Whole viewport has dark background': 'BoxWithConstraints(Modifier.fillMaxSize().background(bg))' in ui,
    'Scrollable column also fills viewport': bool(re.search(r'Column\(Modifier\.fillMaxSize\(\)\.verticalScroll\(screenScroll\)',ui)),
    'Plot heights share available budget': 'maxHeight - 150.dp' in ui and 'plotBudget * 0.53f' in ui and 'plotBudget * 0.47f' in ui,
    'Small screen plot minimum heights': '.coerceAtLeast(215.dp)' in ui and '.coerceAtLeast(190.dp)' in ui,
    'Expanded waterfall maintains zoom': 'if (expanded) 150.dp' in ui and 'if (expanded) (plotBudget - 150.dp)' in ui,
    'Spectrum and waterfall use adaptive heights': '.height(spectrumHeight)' in main and '.height(waterfallHeight)' in main,
    'No legacy fixed plot heights': '285.dp' not in main and '220.dp' not in main and '380.dp' not in main,
    'Lower three legacy cards gone': all('Text("'+label+'"' not in main for label in ['Mithören','Superfast','Anzeige']),
    'PNG manual and auto retained': 'LiveAnalyzerController.saveNow()' in main and 'LiveAnalyzerController.setAutoSave(context,!LiveAnalyzerController.snapshot().autoSave)' in main,
    'Duplicate bottom settings and diagnose gone': '⚙ Einstellungen' not in main and 'Text("Diagnose",' not in main,
    'Main screen status disclaimer gone': 'Unkalibrierter relativer Pegel; WAV bleibt unverändert.' not in main,
    'Spectrum options remain in settings': 'SettingToggle("Rote Mittelwertkurve (5 s)"' in settings and 'SettingToggle("Grauer Rauschboden"' in settings,
    'Diagnostics remains in settings': 'Triple("Diagnose"' in settings and '"Diagnose" -> {' in settings,
    'Settings reachable from top right': 'settings=true' in main and 'Text("⋮"' in main,
    'Preserved frequency alignment': 'val plotLeftDp = 60.dp' in main and main.count('val left=plotLeftDp.toPx()') >= 2,
    'Manual dBFS limits preserved': 'label={Text("Minimum dBFS")}' in settings and 'label={Text("Maximum dBFS")}' in settings,
    'Autorange and cursor retained': 'LiveAnalyzerController.startAutoRange()' in main and 'cursorReadout(state,hz)' in main,
    'Recording stream not accessed in UI': 'AudioRecord(' not in ui and 'AudioRecord(' not in settings,
    'App identity/version updated': 'versionName = "0.5.01"' in app and 'versionCode = 142' in app and 'applicationId = "de.brummbeobachter.app"' in app,
    'No modified PCM/WAV controller API': 'private fun acceptDiagnostics(' in ctl and 'startRawLf' in ctl,
}
for name, success in checks.items():print(('PASS' if success else 'FAIL')+': '+name)
print('TOTAL '+str(sum(checks.values()))+'/'+str(len(checks)))
if not all(checks.values()):raise SystemExit(1)
