#!/usr/bin/env python3
"""Host regression for the v0.5.19 waterfall collapse: gain-referenced bins below -120 dBFS must stay distinct."""
from pathlib import Path
import re
src=(Path(__file__).resolve().parents[1]/'app/src/main/java/de/brummbeobachter/app/WaterfallGenerator.kt').read_text()
lo=float(re.search(r'LEVEL_HARD_MIN_DB = (-?\d+(?:\.\d+)?)',src).group(1))
hi=float(re.search(r'LEVEL_HARD_MAX_DB = (-?\d+(?:\.\d+)?)',src).group(1))
assert lo <= -200.0, lo
assert hi >= 0.0, hi
# Example bins after correct +50 dB compensation. In v0.5.19 the first three collapsed to -120.
values=[-170.0,-145.0,-125.0,-110.0]
kept=[min(hi,max(lo,v)) for v in values]
assert len(set(kept)) == len(values), kept
# A representative auto-color range must produce different positions, including below -120 dBFS.
mn,mx=-175.0,-105.0
norm=[min(1.0,max(0.0,(v-mn)/(mx-mn))) for v in kept]
assert norm[0] < norm[1] < norm[2] < norm[3], norm
assert 'if (levelAuto) "automatisch" else "manuell"' in src
print('PASS waterfall v0.5.20: gain-referenced values below -120 dBFS remain distinct and color-mappable')
