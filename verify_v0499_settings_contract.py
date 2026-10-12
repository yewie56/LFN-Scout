#!/usr/bin/env python3
"""Static regression gates, NOT a replacement for a Compose/Android build."""
from pathlib import Path
import re
root = Path(__file__).resolve().parent
base = root/'app/src/main/java/de/brummbeobachter/app'
ui=(base/'LiveAnalyzerScreen.kt').read_text()
settings=(base/'LiveAnalyzerSettingsPanel.kt').read_text()
ctl=(base/'LiveAnalyzerController.kt').read_text()
gradle=(root/'app/build.gradle.kts').read_text()
tests={
 'New one-column panel wired to live screen': 'LiveAnalyzerSettingsPanel(context,state' in ui,
 'Legacy horizontal settings removed': 'private fun LiveAnalyzerSettings(' not in ui,
 'Eight navigation categories present': all('Triple("'+k+'"' in settings for k in ['Spektrum','Audio','Anzeige','Statistik','Automatiken','Kalibrierung','Setups','Diagnose']),
 'Back navigation to category overview': 'selected=null; onNavigate()' in settings,
 'Persistent setup selector': 'last_setup_slot' in settings and 'putInt("last_setup_slot"' in settings,
 'New setup cannot overwrite existing': 'firstOrNull{ !prefs.contains' in settings,
 'Manual min and max input fields': 'label={Text("Minimum dBFS")}' in settings and 'label={Text("Maximum dBFS")}' in settings,
 'Manual min and max written through controller': 'LiveAnalyzerController.setDisplayRange(context,low,high)' in settings and 'putInt("display_min_db", minDb)' in ctl and 'putInt("display_max_db", maxDb)' in ctl,
 'Invalid range guarded': 'high-low<10' in settings and 'low !in -240..40' in settings and 'high !in -230..50' in settings,
 'Min and max recalled from setup': 'setup_${slot}_display_min' in settings and 'setup_${slot}_display_max' in settings,
 'Auto range still provided': 'setAutoRangeSettings' in settings and 'startAutoRange()' in ui,
 'Three waterfall modes retained': 'WaterfallMode.values().forEach' in settings,
 'Manual and auto PNG controls retained': 'LiveAnalyzerController.saveNow()' in ui and 'setAutoSave(context,it)' in settings,
 'PNG is in public downloads on API29+': 'MediaStore.Downloads.EXTERNAL_CONTENT_URI' in ctl and 'Environment.DIRECTORY_DOWNLOADS + "/LFN-Scout"' in ctl,
 'No second microphone capture in new UI': 'AudioRecord(' not in settings,
 'Version 0.4.99 code 140': 'versionName = "0.4.99"' in gradle and 'versionCode = 140' in gradle,
}
for k, v in tests.items(): print(f'{"PASS" if v else "FAIL"}: {k}')
print(f'TOTAL {sum(tests.values())}/{len(tests)}')
if not all(tests.values()): raise SystemExit(1)
