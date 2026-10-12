from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parents[1]
checks=[]
def txt(rel): return (ROOT/rel).read_text(encoding='utf-8')
def check(name, cond):
    ok=bool(cond); checks.append((name,ok)); print(('PASS' if ok else 'FAIL')+': '+name)

build=txt('app/build.gradle.kts')
controller=txt('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerController.kt')
screen=txt('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerScreen.kt')
settings=txt('app/src/main/java/de/brummbeobachter/app/LiveAnalyzerSettingsPanel.kt')
changelog=txt('CHANGELOG.md')
status=txt('PROJECT_STATUS.md')

check('versionName 0.5.25', 'versionName = "0.5.25"' in build)
check('versionCode 172', 'versionCode = 172' in build)
check('Snapshot exposes Leq', 'val leqDbFs: Double?' in controller)
check('Leq uses unsmoothed linear power history', 'leqPowerHistory.addLast(power)' in controller and 'leqPowerSum += power' in controller)
check('Leq window follows configured statisticsWindowSec', 'AnalyzerTiming.statisticFrames(fftHopSamples, statisticsWindowSec, s.sampleRateHz)' in controller)
check('Leq is energy averaged in linear domain', '10.0 * kotlin.math.log10((leqPowerSum / leqPowerHistory.size)' in controller)
check('Leq is unavailable until full configured window', 'if (leqPowerHistory.size >= required && required > 0)' in controller)
check('Statistics reset clears Leq history/cache', 'leqPowerHistory.clear()' in controller and 'cachedLeq = null' in controller)
check('Screen shows compact Leq label', 'Text("Leq($statsRange):' in screen)
check('Screen shows compact L50 label', 'Text("L50($statsRange):' in screen)
check('Screen shows compact L90 label', 'Text("L90($statsRange):' in screen)
check('Screen labels use hyphen range without Hz', 'val statsRange = "${state.statisticsMinHz}-${state.statisticsMaxHz}"' in screen)
check('Old separate Statistik title removed from stats box', 'Text("Statistik ${state.statisticsMinHz}' not in screen)
check('Three rows have tight zero spacing', 'verticalArrangement=Arrangement.spacedBy(0.dp)' in screen and 'lineHeight=10.sp' in screen)
check('Frame remains complete clickable reset surface', '.border(BorderStroke(1.dp,Color(0xFFE34949))' in screen and '.clickable {' in screen and 'resetBandStatisticsAndAverage()' in screen)
check('Rendered analyzer image includes Leq/L50/L90', 'val leqLabel = "Leq($statsRange):' in controller and 'val l50Label = "L50($statsRange):' in controller and 'val l90Label = "L90($statsRange):' in controller)
check('Rendered stats range uses compact hyphen', 'val statsRange = "${s.statisticsMinHz}-${s.statisticsMaxHz}"' in controller)
comment=controller.split('comment?.takeIf { it.isNotBlank() }?.let { raw ->',1)[1].split('label("${s.imageFormat.name}',1)[0]
check('Image comment has no black background rectangle', 'drawRoundRect' not in comment and 'Color.argb(205' not in comment)
check('Image comment is right aligned', 'p.textAlign = Paint.Align.RIGHT' in comment and 'right - 12f' in comment)
check('Image comment is bottom aligned in waterfall', 'val lastBaseline = wfBottom - 12f' in comment)
check('Image comment keeps readability shadow only', 'p.setShadowLayer' in comment and 'p.clearShadowLayer()' in comment)
check('Settings display follows same compact labels', 'SettingCaption("Leq($statsRange):' in settings and 'SettingCaption("L50($statsRange):' in settings and 'SettingCaption("L90($statsRange):' in settings)
check('No audio/Supabase lifecycle change claimed', 'Keine Änderung an Audio-Save-/Supabase-/24-h-Retention-Logik.' in changelog)
check('Project status identifies Source Candidate', 'Source Candidate' in status and '0.5.25' in status)

failed=[n for n,o in checks if not o]
print(f'RESULT {len(checks)-len(failed)}/{len(checks)} PASS')
if failed:
    print('FAILED:', '; '.join(failed))
    sys.exit(1)
