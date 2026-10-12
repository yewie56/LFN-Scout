from pathlib import Path
import re, json, zipfile, hashlib
root=Path('/mnt/data/LFN-Scout_Android_v0.5.14')
panel=(root/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt').read_text()
export=(root/'app/src/main/java/de/brummbeobachter/app/AnalyzerInspectionExport.kt').read_text()
app=(root/'app/build.gradle.kts').read_text()
checks={
'versionName and versionCode': 'versionName = "0.5.14"' in app and 'versionCode = 157' in app,
'group names': all('Triple("'+g+'"' in panel for g in ['Spektrum','Audio','Anzeige','Statistik','Filterbank','Automatiken','Kalibrierung','Setups','Diagnose']),
'direct filter inactive': 'DisabledSetting("FILTER MIN Hz", "—")' in panel and 'DisabledSetting("FILTER anzeigen", "Nicht unterstützt")' in panel,
'8k audio inactive': 'DisabledSetting("Sampling Rate", "8000 Hz (fest)")' in panel,
'direct time constant editable': 'enabled=state.directTimeSmoothing' in panel and 'LiveAnalyzerController.setDsp(context,state.fftSize,state.windowType,state.smoothingEnabled,tau)' in panel,
'export version': 'lfn-scout-analyzer-setup-export-v2' in export,
'export missing values null': 'JSONObject.NULL' in export and 'MISSING_IN_SAVED_SLOT' in export,
'export preserved read only': 'put("read_only_copy", true)' in export,
'dual group and status': 'put("groups", groups)' in export and 'put("availability", availability)' in export,
'new export refresh': 'v0_5_14_export_done' in export,
'no importer added': 'fun importSetup' not in export,
'new documentation': (root/'docs/project/UNIFIED_ANALYZER_SETTINGS_v0.5.14.md').is_file(),
}
for name,passed in checks.items(): print(('PASS' if passed else 'FAIL'),name)
assert all(checks.values())
print('STATIC_CHECKS PASS',len(checks),'/',len(checks),'(NO Android compilation)')
