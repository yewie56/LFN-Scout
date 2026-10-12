#!/usr/bin/env python3
"""v0.5.06 Windows compile-error regressions; static structure, NOT an Android build."""
from pathlib import Path
r = Path(__file__).resolve().parent.parent
app = r / 'app/src/main/java/de/brummbeobachter/app'
ui = (app / 'LiveAnalyzerScreen.kt').read_text(encoding='utf-8')
ctl = (app / 'LiveAnalyzerController.kt').read_text(encoding='utf-8')
session = (r / 'analyzer-core/src/main/kotlin/de/brummbeobachter/app/AnalyzerRawLfSession.kt').read_text(encoding='utf-8')
core = (r / 'analyzer-core/src/main/kotlin/de/brummbeobachter/app/LiveAnalyzerCore.kt').read_text(encoding='utf-8')
build = (r / 'app/build.gradle.kts').read_text(encoding='utf-8')
# Brace count also checks that each helper is outside the UI composable; do not claim full syntax validation.
def braces_until(text: str, pos: int) -> int:
    return text[:pos].count('{') - text[:pos].count('}')
composable_start = ui.index('internal fun LiveAnalyzerScreen(')
viewport = ui.index('BoxWithConstraints(Modifier.fillMaxSize().background(bg))')
scroll = ui.index('Column(Modifier.fillMaxSize().verticalScroll(screenScroll)')
end_scroll = ui.index('} // scrolling content')
popup = ui.index('if(saveMessage.isNotEmpty()) Box(Modifier.align(Alignment.TopCenter)')
last_viewport = ui.index('} // full-viewport dark background')
helper = ui.index('private fun cursorReadout(')
waterfall = ui.index('private fun makeWaterfall(')
checks = {
    'version 0.5.07 / 150': 'versionName = "0.5.07"' in build and 'versionCode = 150' in build,
    'controller invokes reset': 'session?.resetMaxHold()' in ctl,
    'real subscriber API exists': 'fun resetMaxHold() = core.resetMaxHold()' in session,
    'synchronized core reset exists': '@Synchronized\n    fun resetMaxHold()' in core,
    'viewport precedes scroll': viewport < scroll < end_scroll < popup < last_viewport < helper < waterfall,
    'scroll closes before popup': braces_until(ui, scroll) == 2 and braces_until(ui, end_scroll + len('} // scrolling content')) == 2,
    'popup belongs to viewport': braces_until(ui, popup) == 2,
    'UI closes before helpers': braces_until(ui, helper) == 0 and braces_until(ui, waterfall) == 0,
    'full brace count balanced': ui.count('{') == ui.count('}'),
    'MAX/CLR retains 48 dp size': 'if (state.showMaxHold) Box(Modifier.size(48.dp)' in ui,
    'camera popup remains async': 'LaunchedEffect(state.manualSaveEvent)' in ui and 'delay(2200)' in ui,
    'only one reset delegate declaration': session.count('fun resetMaxHold()') == 1,
}
for name, ok in checks.items():
    print(('PASS' if ok else 'FAIL') + ': ' + name)
print(f'STATIC BUILD FIX {sum(checks.values())}/{len(checks)}')
if not all(checks.values()):
    raise SystemExit(1)
