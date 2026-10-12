"""Static controller/core-API regression for v0.4.93. NOT a Kotlin/Android build."""
from pathlib import Path
import re, json, sys
r=Path(__file__).resolve().parent
app=(r/'app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt').read_text(encoding='utf-8')
raw=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt').read_text(encoding='utf-8')
old=(r/'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerLiveSession.kt').read_text(encoding='utf-8')
gw=(r/'app/src/main/java/de/brummbeobachter/app/AnalyzerSessionGateway.kt').read_text(encoding='utf-8')
build=(r/'app/build.gradle.kts').read_text(encoding='utf-8')
manifest=json.loads((r/'docs/reconstruction/project_manifest.json').read_text(encoding='utf-8'))
checks={
 'v0.4.93/versionCode 133/unchanged app id': 'versionCode = 133' in build and 'versionName = "0.4.93"' in build and 'applicationId = "de.brummbeobachter.app"' in build,
 'controller raw session type from gateway': 'private var session: AnalyzerRawLfSession? = null' in app and re.search(r'fun\s+startRawLf\([\s\S]*?\): AnalyzerRawLfSession\s*=',gw) is not None,
 'raw session exposes callbackErrors': re.search(r'val\s+callbackErrors\s*:\s*Long\s+get\(',raw) is not None,
 'controller reads new callbackErrors member': 'session?.callbackErrors ?: 0L' in app,
 'removed invalid spectrumCallbackErrors reference in raw controller': 'session?.spectrumCallbackErrors' not in app,
 'historic member belongs only to old session': 'val spectrumCallbackErrors: Long' in old and 'val spectrumCallbackErrors: Long' not in raw,
 'all nullable session property references declared by raw session': set(re.findall(r'session\?\.(\w+)',app)) <= set(re.findall(r'\b(?:val|var)\s+(\w+)',raw)) | set(re.findall(r'\bfun\s+(\w+)\(',raw)),
 'versioned manifest and docs present': manifest['documented_version']=='0.4.93' and manifest['version_code']==133 and (r/'docs/project/ANALYZER_LIVE_v0.4.93_BUILDFIX.md').is_file(),
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL') + ': ' + name)
if not all(checks.values()): sys.exit(1)
print(f'PASS: {len(checks)}/{len(checks)} static checks; Android/phone NOT RUN')
