from pathlib import Path
import re, sys
root = Path(__file__).resolve().parent
main = (root/'app/src/main/java/de/brummbeobachter/app/MainActivity.kt').read_text(encoding='utf-8')
exporter = (root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt').read_text(encoding='utf-8')
policy = (root/'app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt').read_text(encoding='utf-8')
strings = (root/'app/src/main/res/values/strings.xml').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle.kts').read_text(encoding='utf-8')
checks = []
def ck(name, cond):
    checks.append((name, bool(cond)))

ck('versionName 0.4.74', 'versionName = "0.4.74"' in gradle)
ck('versionCode 114', 'versionCode = 114' in gradle)
ck('visible app name LFN-Scout', '<string name="app_name">LFN-Scout</string>' in strings)
ck('live near plot is 16:9 landscape', '.aspectRatio(SeismicPlotUiPolicy.LIVE_ASPECT_RATIO)' in main)
ck('live aspect ratio constant 16:9', 'const val LIVE_ASPECT_RATIO = 16f / 9f' in policy)
ck('time labels halved from 8sp to 4sp', '"%02d:%02d".format(hh, mm)' in main and 'fontSize = SeismicPlotUiPolicy.TIME_LABEL_FONT_SP.sp' in main and 'const val TIME_LABEL_FONT_SP = 4f' in policy)
ck('time rows share landscape plot height', '.weight(1f)' in main and 'TIME_LABEL_FONT_SP' in main)
ck('export width remains landscape 1536', 'const val EXPORT_WIDTH_PX = 1536' in policy)
ck('export height remains landscape 864', 'const val EXPORT_HEIGHT_PX = 864' in policy)
ck('redundant bin max conversion removed', 'bin.max.toFloat()' not in exporter)
ck('redundant bin min conversion removed', 'bin.min.toFloat()' not in exporter)
ck('legend label includes value and unit', '"40000 nm/s"' in main and '"40000 nm/s"' in exporter)

fails=[n for n,v in checks if not v]
for n,v in checks:
    print(('PASS' if v else 'FAIL'), n)
print(f'{len(checks)-len(fails)}/{len(checks)} PASS')
sys.exit(1 if fails else 0)
