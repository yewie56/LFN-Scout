#!/usr/bin/env python3
"""v0.5.01 source-contract regression only; NOT an Android compile or device test."""
from pathlib import Path
import re
R = Path(__file__).resolve().parent
base = R/'app/src/main/java/de/brummbeobachter/app'
u = (base/'LiveAnalyzerScreen.kt').read_text(encoding='utf-8')
s = (base/'LiveAnalyzerSettingsPanel.kt').read_text(encoding='utf-8')
c = (base/'LiveAnalyzerController.kt').read_text(encoding='utf-8')
a = (R/'app/build.gradle.kts').read_text(encoding='utf-8')
main = u.split('        } else {',1)[1].split('} // end main analyzer view',1)[0]
checks = {
    'version and install identity': all(x in a for x in ['versionName = "0.5.01"','versionCode = 142','applicationId = "de.brummbeobachter.app"']),
    'one short tap changes auto flag': 'onTap = {' in main and 'setAutoSave(context,!LiveAnalyzerController.snapshot().autoSave)' in main,
    'long press saves manual image': 'onLongPress = {' in main and 'LiveAnalyzerController.saveNow()' in main,
    'no extra Auto EIN/AUS text': 'Text("Auto ${if(state.autoSave)' not in main and 'Text("📷 PNG"' not in main,
    'red cross only when automatic OFF': 'if (!state.autoSave) Canvas(Modifier.matchParentSize())' in main and main.count('Color(0xFFF04438)') == 2,
    'back tap handles one level': 'if (settingsSection != null) settingsSection = null else settings = false' in u and 'clickable { backFromSettings() }' in u,
    'Android back matches top arrow': 'BackHandler(enabled=settings) { backFromSettings() }' in u,
    'section state lifted to parent': 'selected=settingsSection, onSelect={ settingsSection=it }' in u and 'onSelect(name)' in s and 'onSelect(null)' in s,
    'Hz shared numeric line in UI': main.count('bottom+17.dp.toPx(),textPaint)') >= 2 and 'bottom+30.dp.toPx()' not in main,
    'Hz shared numeric line in image': 'specBottom + 29f, 20f' in c and 'wfBottom+28f,20f' in c and 'specBottom + 60f' not in c,
    'two dBFS entries retain visible white text': s.count('textStyle=TextStyle(color=white,fontSize=16.sp)') == 2,
    'focused controls and negative numeric entry': s.count('onFocusChanged { editing') == 2 and 'keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal)' in s and '± Minimum' in s and '± Maximum' in s,
    'autorange updates non-focused entries': 'LaunchedEffect(state.minDisplayDb, editingMin)' in s and 'LaunchedEffect(state.maxDisplayDb, editingMax)' in s,
    'PNG/JPG format exposed and persisted': 'AnalyzerImageFormat.values().forEach' in s and '.putString("image_format", value.name)' in c,
    'JPEG quality exposed and persisted': 'listOf(70,80,90,100)' in s and '.putInt("jpg_quality", value)' in c,
    'format/quality survive setup save and load': all(x in s for x in ['.putString("setup_${slot}_image_format"','"setup_${slot}_jpg_quality",state.jpgQuality','getString("setup_${slot}_image_format"','getInt("setup_${slot}_jpg_quality",90)']),
    'export actual format and MIME': 'Bitmap.CompressFormat.JPEG' in c and 'Bitmap.CompressFormat.PNG' in c and 'put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)' in c and 'bitmap.compress(compressFormat, quality, stream)' in c,
    'snapshot freezes image settings': 'val imageFormat: AnalyzerImageFormat, val jpgQuality: Int' in c and 'Snapshot(running, paused, auto, imageFormat, jpgQuality' in c,
    'shared raw PCM/recorder unchanged': 'startRawLf(::accept, ::acceptDiagnostics, analyzerGainDb, fftHopSamples)' in c and 'AudioRecord(' not in u and 'AudioRecord(' not in s,
    'previous chart layout preserved': 'BoxWithConstraints(Modifier.fillMaxSize().background(bg))' in u and '.height(spectrumHeight)' in main and '.height(waterfallHeight)' in main,
}
for key, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + key)
print(f'TOTAL {sum(checks.values())}/{len(checks)}')
if not all(checks.values()): raise SystemExit(1)
