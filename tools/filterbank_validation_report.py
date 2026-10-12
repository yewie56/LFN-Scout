#!/usr/bin/env python3
"""LFN-Scout digital filterbank diagnostic. Standard library only, no Android integration.
Algorithm is mirrored from RollingFilterHalfHourStatistics.kt; not an independent acoustic measurement.
"""
import argparse, csv, datetime, math, pathlib, sys

SR = 1000
A = 0.15
VERSION = '0.5.40'

def coefficients(low, high, sr=SR):
    return (math.exp(-2*math.pi*low/sr) if low else 0.0, math.exp(-2*math.pi*high/sr))

def magnitude(low, high, hz, sr=SR):
    a,b=coefficients(low,high,sr)
    w=2*math.pi*hz/sr
    denom=lambda q: math.sqrt((1-q)**2+2*q*(1-math.cos(w)))
    lp=(1-b)/denom(b)
    hp= a*math.sqrt(2-2*math.cos(w))/denom(a) if low else 1.0
    return hp*lp

def measure_all(hz, edges, seconds=6, sr=SR):
    states=[[0.,0.,0.,0.] for _ in edges] # xprior, hpValue, lpValue, final energy
    ab=[coefficients(l,h,sr) for l,h in edges]
    n=int(sr*seconds); start=n-sr
    for i in range(n):
        x=A*math.sin(2*math.pi*hz*i/sr)
        x=round(x*32767)/32768.0
        for j,((l,h), (a,b)) in enumerate(zip(edges,ab)):
            s=states[j]
            passed=a*(s[1]+x-s[0]) if l else x
            if l: s[1]=passed
            s[0]=x
            s[2]=(1-b)*passed+b*s[2]
            if i>=start: s[3]+=s[2]*s[2]
    return [10*math.log10(max(1.e-20,s[3]/sr)) for s in states]

def svg_lines(path, freqs, lines, labels, title):
    W,H=1100,580; left,right,top,bottom=85,30,58,70; width=W-left-right; height=H-top-bottom
    ymin,ymax=-85.,5.
    x=lambda f:left+width*f/120
    y=lambda d:top+height*(ymax-max(ymin,min(ymax,d)))/(ymax-ymin)
    parts=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">', '<rect width="100%" height="100%" fill="white"/>',f'<text x="{left}" y="28" font-size="19" font-family="Arial">{title}</text>']
    for db in range(-80,1,20):
        yy=y(db);parts.append(f'<path d="M{left} {yy:.1f}H{W-right}" stroke="#ddd"/><text x="{left-10}" y="{yy+4:.1f}" text-anchor="end" font-size="13">{db}</text>')
    for hz in range(0,121,20):
        xx=x(hz);parts.append(f'<path d="M{xx:.1f} {top}V{H-bottom}" stroke="#eee"/><text x="{xx:.1f}" y="{H-bottom+20}" text-anchor="middle" font-size="13">{hz}</text>')
    for j,line in enumerate(lines):
        hue=int(360*j/max(1,len(lines))); coords=' '.join(f'{x(f):.2f},{y(v):.2f}' for f,v in zip(freqs,line))
        parts.append(f'<polyline points="{coords}" fill="none" stroke="hsl({hue},75%,40%)" stroke-width="1.7"/>')
    parts.append(f'<text x="{W/2}" y="{H-14}" text-anchor="middle" font-size="14">Eingangsfrequenz (Hz)</text>')
    parts.append('<text x="12" y="47" font-size="13">dB</text></svg>')
    path.write_text('\n'.join(parts),encoding='utf-8')

def svg_matrix(path, edges, test_freqs, vals, name):
    cell=32;left=130;top=90;W=left+cell*len(test_freqs)+25;H=top+cell*len(edges)+75
    p=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}"><rect width="100%" height="100%" fill="white"/>',f'<text x="10" y="25" font-family="Arial" font-size="17">{name} – theoretische Dämpfung (dB, bezogen auf Input)</text>']
    for i,hz in enumerate(test_freqs): p.append(f'<text x="{left+i*cell+cell/2}" y="{top-10}" font-family="Arial" font-size="11" text-anchor="middle">{hz:g}</text>')
    for j,(lo,hi) in enumerate(edges):
        yy=top+j*cell;p.append(f'<text x="{left-8}" y="{yy+20}" text-anchor="end" font-family="Arial" font-size="11">{lo:g}–{hi:g}</text>')
        for i,hz in enumerate(test_freqs):
            db=vals[j][i]; t=max(0,min(1,(db+55)/55)); gray=int(245-160*t)
            p.append(f'<rect x="{left+i*cell}" y="{yy}" width="{cell-1}" height="{cell-1}" fill="rgb({gray},{gray+min(50,255-gray)},{255})"/>')
            p.append(f'<text x="{left+i*cell+cell/2}" y="{yy+20}" text-anchor="middle" font-family="Arial" font-size="10">{db:.0f}</text>')
    p.append(f'<text x="{W/2}" y="{H-20}" text-anchor="middle" font-family="Arial" font-size="13">Testfrequenz (Hz)</text></svg>')
    path.write_text('\n'.join(p),encoding='utf-8')

def writecsv(path, fields, rows):
    with path.open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=fields,delimiter=';');w.writeheader();w.writerows(rows)

def main():
    p=argparse.ArgumentParser();p.add_argument('--output',default='FilterbankReport_v0.5.40');p.add_argument('--min-hz',type=int,default=0);p.add_argument('--max-hz',type=int,default=100);p.add_argument('--seconds',type=int,default=6);p.add_argument('--tolerance-db',type=float,default=.35);a=p.parse_args()
    if not (0<=a.min_hz<a.max_hz<SR/2) or not (3<=a.seconds<=30) or not (.05<=a.tolerance_db<=10):p.error('ungueltiger Bereich / Sekunden / Toleranz')
    dest=pathlib.Path(a.output);dest.mkdir(parents=True,exist_ok=True)
    checks=[];dense=[0.,.5,1.,2.,3.,5.]+[float(x) for x in range(6,121,2)]
    tests=[.5,2.,5.,10.,15.,25.,35.,50.,75.,95.,120.]
    for n in (10,20):
        delta=(a.max_hz-a.min_hz)/n;edges=[(a.min_hz+i*delta,a.min_hz+(i+1)*delta) for i in range(n)]
        response=[]
        for i,(lo,hi) in enumerate(edges,1):
            for hz in dense:
                gain=max(1e-12,magnitude(lo,hi,hz));response.append({'band':i,'low_hz':f'{lo:.4f}','high_hz':f'{hi:.4f}','input_hz':hz,'theory_gain_db':f'{20*math.log10(gain):.5f}'})
        writecsv(dest/f'FilterResponse_{n}bands.csv',list(response[0]),response)
        lines=[[20*math.log10(max(1e-12,magnitude(lo,hi,hz))) for hz in dense] for lo,hi in edges]
        svg_lines(dest/f'FilterResponse_{n}bands.svg',dense,lines,edges,f'LFN-Scout – {n} Bänder {a.min_hz}–{a.max_hz} Hz (DSP-Modell)')
        vals=[[20*math.log10(max(1e-12,magnitude(lo,hi,hz))) for hz in tests] for lo,hi in edges]
        svg_matrix(dest/f'Crosstalk_{n}bands.svg',edges,tests,vals,f'{n} Bänder')
        for hz in tests:
            actuals=measure_all(hz,edges,a.seconds)
            for i,((lo,hi),actual) in enumerate(zip(edges,actuals),1):
                theory=20*math.log10(A/math.sqrt(2)*max(1e-12,magnitude(lo,hi,hz)))
                # 0.5Hz with few seconds may remain in transient; report failures explicitly
                deltaDb=actual-theory; ok=abs(deltaDb)<=a.tolerance_db
                checks.append({'bands':n,'band':i,'low_hz':f'{lo:.3f}','high_hz':f'{hi:.3f}', 'input_hz':hz, 'theory_dbfs':f'{theory:.5f}', 'simulated_dbfs':f'{actual:.5f}','delta_db':f'{deltaDb:.5f}', 'tolerance_db':a.tolerance_db,'result':'PASS' if ok else 'FAIL'})
    writecsv(dest/'FilterSimulation_Soll_Ist.csv',list(checks[0]),checks)
    failed=[x for x in checks if x['result']=='FAIL']
    text=[f'# LFN-Scout {VERSION} – Filterbankvalidierung', '',f'Erzeugt: {datetime.datetime.now().isoformat(timespec="seconds")}',f'Konfiguration: {a.min_hz}–{a.max_hz} Hz, 10 und 20 Bänder, Abtastrate {SR} Hz, letzte von {a.seconds} simulierten Sekunden (1-s RMS), Toleranz ±{a.tolerance_db} dB.',f'**Ergebnis: {"PASS" if not failed else "FAIL"} – {len(checks)-len(failed)}/{len(checks)} Einzelvergleiche bestanden.**','','## Dateien','- `FilterResponse_10bands.csv`, `FilterResponse_20bands.csv`: analytische diskrete Filterantwort über Frequenz','- `FilterResponse_10bands.svg`, `FilterResponse_20bands.svg`: Frequenzganggrafiken','- `Crosstalk_10bands.svg`, `Crosstalk_20bands.svg`: Übersprechmatrizen (theoretische Gainwerte)','- `FilterSimulation_Soll_Ist.csv`: unabhängige Python-Nachbildung der Kotlin-Rekursion gegen analytische Theorie', '', '## Einschränkungen', '- Kein Android-Mikrofon, keine Android-Blockverarbeitung, keine Messgeräte- oder Akustikkalibrierung.', '- Nominale Bandgrenzen sind NICHT scharfe Passbandgrenzen; erste Ordnung führt zu erheblicher Bandüberlappung.', '- Simulation bildet den aktuellen Kotlin-Filteralgorithmus nach und ist deshalb keine unabhängige physikalische Filtermessung.', '- Die geräteinternen Kotlin-Regressionen werden separat durch `VORTEST_INSTALL.bat` geprüft.', '- Tests bei sehr niedriger Frequenz können bei kurzer Laufdauer wegen Einschwingverhalten scheitern. Diese Fehler werden nicht verschwiegen.']
    if failed:text += ['', '## Fehlgeschlagene Einzelvergleiche (Auszug)']+[f'- {q["bands"]} Bänder / Band {q["band"]} / {q["input_hz"]} Hz: Δ={q["delta_db"]} dB' for q in failed[:40]]
    (dest/'VALIDIERUNGSBERICHT.md').write_text('\n'.join(text)+'\n',encoding='utf-8')
    print(f'REPORT={dest.resolve()} TESTS={len(checks)} PASS={len(checks)-len(failed)} FAIL={len(failed)}')
    return 0 if not failed else 1
if __name__=='__main__':sys.exit(main())
