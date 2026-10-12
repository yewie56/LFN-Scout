#!/usr/bin/env python3
"""LFN-Scout v0.5.41 standalone DSP comparison; no Android or cloud writes.
Requires numpy and scipy. FFT is a windowed RMS energy estimator, not an IIR output.
"""
import argparse
import csv
import datetime as dt
import math
from pathlib import Path
import sys
import numpy as np
from scipy import signal

FS=1000
AMP=0.15
METHODS=('Legacy-IIR','Butterworth4','FFT-Hann')

def bands(lo,hi,count):
    e=np.linspace(lo,hi,count+1)
    return list(zip(e[:-1],e[1:]))

def legacy_filter(x,low,high,fs=FS):
    a=math.exp(-2*math.pi*low/fs) if low>0 else 0.
    b=math.exp(-2*math.pi*high/fs)
    # Exactly reproduce Kotlin initial states / sample update order.
    prior=hp=lp=0.
    y=np.empty(len(x))
    for i,v in enumerate(x):
        passed=a*(hp+v-prior) if low>0 else v
        if low>0: hp=passed
        prior=v
        lp=(1-b)*passed+b*lp
        y[i]=lp
    return y

def legacy_gain(low,high,freqs,fs=FS):
    a=math.exp(-2*math.pi*low/fs) if low>0 else 0.
    b=math.exp(-2*math.pi*high/fs)
    w=2*np.pi*np.asarray(freqs)/fs
    z=np.exp(-1j*w)
    hp=a*(1-z)/(1-a*z) if low>0 else np.ones_like(z)
    lp=(1-b)/(1-b*z)
    return abs(hp*lp)

def butter_sos(low,high,fs=FS):
    # Zero lower edge is a genuine 4th-order low pass, not a zero-Hz high pass.
    if low<=0: return signal.butter(4,high,btype='lowpass',fs=fs,output='sos')
    # '4th order bandpass' = overall degree 4, hence prototype order 2.
    return signal.butter(2,[low,high],btype='bandpass',fs=fs,output='sos')

def fft_energy(x,low,high,fs=FS,window_len=4096,hop=1024):
    """Non-overlapping per-frame squared normalized amplitudes, sum bin powers.
    One-sided PSD via scipy.periodogram and window-equivalent noise bandwidth.
    Output is frame RMS (power spectrum includes DC and Nyquist correctly).
    """
    if len(x)<window_len:return np.empty(0)
    window=signal.windows.hann(window_len,sym=False)
    freqs=np.fft.rfftfreq(window_len,1/fs)
    mask=(freqs>=low)&(freqs<high)
    # Include Nyquist when high == Nyquist (normally not used).
    vals=[]
    norm=np.sum(window**2)
    for start in range(0,len(x)-window_len+1,hop):
        transformed=np.fft.rfft(x[start:start+window_len]*window)
        powers=np.abs(transformed)**2 / norm**2
        if len(powers)>2:powers[1:-1]*=2
        # Hann coherent gain correction is for tones; energy conversion uses window RMS.
        vals.append(float(np.sum(powers[mask])*norm/window_len))
    return np.array(vals)

def fft_tone_gain(f,low,high,fs=FS,n=4096):
    # Sample-grid coherent response using windowed physical sinusoid; compensate
    # RMS reference from the SAME window; frequency bins include no fuzzy edges.
    t=np.arange(n)/fs
    x=np.sin(2*np.pi*f*t)
    w=signal.windows.hann(n,sym=False)
    F=np.fft.rfft(x*w)
    power=np.abs(F)**2
    if len(power)>2:power[1:-1]*=2
    hz=np.fft.rfftfreq(n,1/fs)
    mask=(hz>=low)&(hz<high)
    measured=np.sum(power[mask])/(n*np.sum(w*w))
    expected=np.sum((x*w)**2)/np.sum(w*w)
    return math.sqrt(max(0,float(measured/expected))) if expected>0 else 0.

def db(x):return 20*math.log10(max(float(x),1e-12))

def rms(x):return float(np.sqrt(np.mean(np.asarray(x)**2)))

def write_csv(path,rows):
    if not rows:return
    with path.open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,list(rows[0]),delimiter=';')
        writer.writeheader();writer.writerows(rows)

def svg_lines(path, x, datasets, title, xlabel='Frequenz (Hz)', ymin=-90, ymax=5):
    from xml.sax.saxutils import escape
    W,H=1050,500; l,r,t,b=78,20,52,65
    xx=lambda v:l+(W-l-r)*(v-min(x))/(max(x)-min(x))
    yy=lambda v:t+(H-t-b)*(ymax-max(ymin,min(ymax,v)))/(ymax-ymin)
    out=[f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}"><rect width="100%" height="100%" fill="white"/>',f'<text x="20" y="28" font-size="19">{escape(title)}</text>']
    for level in np.linspace(ymin,ymax,6):out.append(f'<path d="M{l} {yy(level):.1f}H{W-r}" stroke="#ddd"/><text x="{l-7}" y="{yy(level)+4:.1f}" text-anchor="end" font-size="12">{level:.0f}</text>')
    colors=['#165b9c','#d45b2a','#24915b','#7d3ca1','#b27810']
    for i,(label,y) in enumerate(datasets):
        coords=' '.join(f'{xx(a):.1f},{yy(v):.1f}' for a,v in zip(x,y))
        out.append(f'<polyline points="{coords}" fill="none" stroke="{colors[i%len(colors)]}" stroke-width="1.5"/>')
        out.append(f'<text x="{l+(i%3)*275}" y="{H-38+(i//3)*14}" font-size="11" fill="{colors[i%len(colors)]}">{escape(label)}</text>')
    out.append(f'<text x="{W/2}" y="{H-10}" text-anchor="middle">{escape(xlabel)}</text></svg>')
    path.write_text('\n'.join(out),encoding='utf-8')

def svg_heatmap(path, matrix, edges, tones, title):
    from xml.sax.saxutils import escape
    cell=27;l=115;t=65;W=l+cell*len(tones)+25;H=t+cell*len(edges)+50
    out=[f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}"><rect width="100%" height="100%" fill="white"/><text x="12" y="25" font-size="16">{escape(title)}</text>']
    for i,f in enumerate(tones):out.append(f'<text x="{l+i*cell+cell/2}" y="{t-10}" font-size="10" text-anchor="middle">{f:g}</text>')
    for j,(lo,hi) in enumerate(edges):
        out.append(f'<text x="{l-6}" y="{t+j*cell+18}" text-anchor="end" font-size="10">{lo:g}-{hi:g}</text>')
        for i in range(len(tones)):
            v=matrix[j,i];intensity=max(0,min(1,(v+70)/70)); c=int(245-170*intensity)
            out.append(f'<rect x="{l+i*cell}" y="{t+j*cell}" width="{cell-1}" height="{cell-1}" fill="rgb({c},{min(255,c+25)},250)"/><text x="{l+i*cell+cell/2}" y="{t+j*cell+18}" text-anchor="middle" font-size="9">{v:.0f}</text>')
    out.append('</svg>');path.write_text('\n'.join(out),encoding='utf-8')

def bandwidth(freq,gain):
    peak=float(np.max(gain));idx=int(np.argmax(gain));threshold=peak/np.sqrt(2)
    good=gain>=threshold
    a=idx
    while a>0 and good[a-1]:a-=1
    b=idx
    while b+1<len(freq) and good[b+1]:b+=1
    return peak,float(freq[idx]),float(freq[a]),float(freq[b]),float(freq[b]-freq[a]),a==0,b==len(freq)-1

def run(out,windows=(4096,1024),fs=FS):
    out.mkdir(parents=True,exist_ok=True)
    nfft,hop=windows
    response=[];coupling=[];metrics=[];amplitudes=[];times=[];checks=[]
    configs=((0.,100.),(10.,60.))
    # Precalculate windowed FFT spectra ONCE for every swept tone; reuse for all
    # 120 bands. Avoid thousands of duplicated FFTs per band.
    freqs=np.linspace(0,120,2401)
    w=signal.windows.hann(nfft,sym=False)
    bin_freqs=np.fft.rfftfreq(nfft,1/fs)
    fft_spectra=np.empty((len(freqs),len(bin_freqs)),dtype=np.float64)
    sample_idx=np.arange(nfft)/fs
    for begin in range(0,len(freqs),64):
        hz=freqs[begin:begin+64,None]
        x=np.sin(2*np.pi*hz*sample_idx)*w
        ffts=np.fft.rfft(x,axis=1)
        powers=abs(ffts)**2
        if powers.shape[1]>2:powers[:,1:-1]*=2
        expectations=np.sum(x*x,axis=1)/np.sum(w*w)
        fft_spectra[begin:begin+len(x)] = powers/(nfft*np.sum(w*w)*np.maximum(expectations[:,None],1e-30))
    for lower,upper in configs:
        for count in (10,20):
            edges=bands(lower,upper,count)
            # 0.05 Hz spacing captures narrow 2.5 Hz bands but bins constrain FFT.
            tones=np.array([ (lo+hi)/2 for lo,hi in edges ])
            curves={m:[] for m in METHODS}
            for i,(lo,hi) in enumerate(edges,1):
                curves['Legacy-IIR'].append(legacy_gain(lo,hi,freqs))
                sos=butter_sos(lo,hi)
                curves['Butterworth4'].append(abs(signal.sosfreqz(sos,worN=2*np.pi*freqs/fs)[1]))
                curves['FFT-Hann'].append(np.sqrt(np.sum(fft_spectra[:,(bin_freqs>=lo)&(bin_freqs<hi)],axis=1)))
                for method in METHODS:
                    g=curves[method][-1]
                    peak,pk,lcut,hcut,width,openleft,openright=bandwidth(freqs,g)
                    metrics.append(dict(range_hz=f'{lower:g}-{upper:g}',bands=count,band=i,low_hz=lo,high_hz=hi,method=method,max_gain_db=db(peak),peak_frequency_hz=pk,minus3_low_hz=lcut,minus3_high_hz=hcut,minus3_width_hz=width,lower_truncated=openleft,upper_truncated=openright,fft_window_samples=nfft,fft_resolution_hz=fs/nfft))
                    for f,gain in zip(freqs,g):response.append(dict(range_hz=f'{lower:g}-{upper:g}',bands=count,band=i,method=method,frequency_hz=round(float(f),3),gain_db=round(db(gain),5)))
            for method in METHODS:
                mat=np.array([[db(np.interp(f,freqs,g)) for f in tones] for g in curves[method]])
                for j,(lo,hi) in enumerate(edges,1):
                    for k,tone in enumerate(tones,1):coupling.append(dict(range_hz=f'{lower:g}-{upper:g}',bands=count,method=method,output_band=j,low_hz=lo,high_hz=hi,input_band=k,tone_hz=tone,gain_db=round(float(mat[j-1,k-1]),5)))
                stem=f'{lower:g}-{upper:g}Hz_{count}bands_{method.replace("-","_")}'
                svg_heatmap(out/f'Coupling_{stem}.svg',mat,edges,tones,stem)
            for chosen in [0,count//2,count-1]:
                svg_lines(out/f'Response_{lower:g}-{upper:g}Hz_{count}bands_band{chosen+1}.svg',freqs,[(m,[db(v) for v in curves[m][chosen]]) for m in METHODS],f'{lower:g}–{upper:g} Hz / {count} bands / band {chosen+1}')
            # linearity: input amplitude ratio; use same physical sine, quantify filter outputs.
            probe=(edges[count//2][0]+edges[count//2][1])/2
            lo,hi=edges[count//2]
            t=np.arange(fs*24)/fs
            base=np.sin(2*np.pi*probe*t)
            for m in METHODS:
                values=[]
                for a in (.03,.06,.12):
                    if m=='Legacy-IIR':v=rms(legacy_filter(base*a,lo,hi)[-fs*4:])
                    elif m=='Butterworth4':v=rms(signal.sosfilt(butter_sos(lo,hi),base*a)[-fs*4:])
                    else:v=math.sqrt(float(np.mean(fft_energy(base*a,lo,hi,window_len=nfft,hop=hop)[-4:])))
                    values.append(v)
                    amplitudes.append(dict(range_hz=f'{lower:g}-{upper:g}',bands=count,method=m,band=count//2+1,tone_hz=probe,input_amplitude=a,output_rms=v))
                delta=db(values[-1]/max(values[0],1e-20))-db(.12/.03)
                checks.append(dict(test='amplitude_linear',configuration=f'{lower:g}-{upper:g}/{count}/{m}',delta=delta,tolerance=.02,result='PASS' if abs(delta)<.02 else 'FAIL'))
    # Temporal response: impulse sample followed by decay, step gates at 6 and 12 s.
    # Use 0.5–2 Hz separately for settling: at least 90 seconds, no fake 6s steady state.
    for lo,hi in ((0.,10.),(10.,20.),(.5,2.),(30.,40.)):
        total=120 if hi<=2 else 24
        N=fs*total
        impulses=np.zeros(N);impulses[fs]=1.
        # unit sinusoidal burst long enough for low-frequency steady state
        t=np.arange(N)/fs
        tone=max(.75,(lo+hi)/2)
        burst=np.sin(2*np.pi*tone*t)*AMP
        on=10 if hi<=2 else 4
        off=100 if hi<=2 else 16
        burst[(t<on)|(t>=off)]=0
        for m in METHODS:
            if m=='Legacy-IIR':
                imp=legacy_filter(impulses,lo,hi); y=legacy_filter(burst,lo,hi)
                epoch=np.array([rms(y[i*fs:(i+1)*fs]) for i in range(total)])
            elif m=='Butterworth4':
                sos=butter_sos(lo,hi);imp=signal.sosfilt(sos,impulses);y=signal.sosfilt(sos,burst)
                epoch=np.array([rms(y[i*fs:(i+1)*fs]) for i in range(total)])
            else:
                imp=None
                powers=fft_energy(burst,lo,hi,window_len=nfft,hop=hop)
                epoch=np.sqrt(powers)
            if imp is not None:
                peak=float(np.max(abs(imp)))
                end=np.flatnonzero(abs(imp)>peak*.01)
                impulse_duration=(end[-1]-fs)/fs if len(end) else 0
                impulse_peak=peak
                energy=np.cumsum(imp**2); centroid=float(np.sum(t*imp*imp)/energy[-1]-1) if energy[-1]>0 else 0
            else:
                # An FFT output is one value per frame; delta pulse is smeared over
                # overlapping frames: window+hop determines temporal footprint.
                impulse_duration=nfft/fs
                impulse_peak=float('nan');centroid=float('nan')
            if m=='FFT-Hann':
                frame_t=(np.arange(len(epoch))*hop+nfft/2)/fs
                # Analyze observed window response: frames with centers entirely within tone burst.
                valid=(frame_t>=on+nfft/fs)&(frame_t<off-nfft/fs)
                steady=float(np.median(epoch[valid])) if np.any(valid) else float('nan')
                onset=frame_t[np.flatnonzero(epoch>=.9*steady)[0]]-on if np.isfinite(steady) and np.any(epoch>=.9*steady) else float('nan')
                tail=frame_t[np.flatnonzero((frame_t>=off)&(epoch>=.1*steady))[-1]]-off if np.isfinite(steady) and np.any((frame_t>=off)&(epoch>=.1*steady)) else 0.
            else:
                steady=float(np.median(epoch[on+max(3,int(6/max(lo,.5))):off-2])) if off-on>max(6,int(6/max(lo,.5))) else float(np.median(epoch[on+1:off]))
                onset=next((float(i-on) for i in range(on,off) if epoch[i]>=.9*steady),float('nan'))
                tail=max([float(i-off+1) for i in range(off,total) if epoch[i]>=.1*steady] or [0.])
            times.append(dict(low_hz=lo,high_hz=hi,method=m,tone_hz=tone,burst_start_s=on,burst_end_s=off,steady_rms=steady,rise90_s=onset,decay10_s=tail,impulse_1percent_support_s=impulse_duration,impulse_peak=impulse_peak,impulse_energy_centroid_s=centroid,fft_window_s=nfft/fs,fft_hop_s=hop/fs,notes='FFT frame-energy response: not a physical impulse waveform' if m=='FFT-Hann' else 'causal filter; sample impulse at 1s'))
            if imp is not None:
                ix=np.arange(fs,min(N,fs*4))/fs-1
                svg_lines(out/f'Impulse_{lo:g}-{hi:g}Hz_{m.replace("-","_")}.svg',ix,[(m,20*np.log10(np.maximum(abs(imp[fs:min(N,fs*4)]),1e-12)))],f'Impulse {lo:g}–{hi:g} Hz: {m}',xlabel='Zeit nach Impuls (s)',ymin=-120,ymax=0)
    # Invariants / independent deterministic self tests
    zero=np.zeros(1000)
    checks.extend([
        dict(test='legacy_zero',configuration='10-20Hz',delta=float(np.max(abs(legacy_filter(zero,10,20)))),tolerance=1e-14,result='PASS'),
        dict(test='butter_zero',configuration='10-20Hz',delta=float(np.max(abs(signal.sosfilt(butter_sos(10,20),zero)))),tolerance=1e-14,result='PASS'),
        dict(test='fft_zero',configuration='10-20Hz',delta=float(max(fft_energy(np.zeros(nfft),10,20,window_len=nfft,hop=hop))),tolerance=1e-14,result='PASS'),
    ])
    for c in checks[-3:]:c['result']='PASS' if abs(c['delta'])<=c['tolerance'] else 'FAIL'
    # Discrete analytic vs time-domain test independent of the plotting grid.
    tone=15.; N=fs*40; sine=AMP*np.sin(2*np.pi*tone*np.arange(N)/fs)
    gain=rms(legacy_filter(sine,10,20)[-fs*10:])/(AMP/math.sqrt(2))
    error=db(gain)-db(legacy_gain(10,20,[tone])[0])
    checks.append(dict(test='legacy_analytic_vs_recursion',configuration='10-20Hz/15Hz/40s',delta=error,tolerance=.02,result='PASS' if abs(error)<.02 else 'FAIL'))
    sos=butter_sos(10,20);measured=rms(signal.sosfilt(sos,sine)[-fs*10:])/(AMP/math.sqrt(2))
    theoretical=abs(signal.sosfreqz(sos,worN=[2*np.pi*tone/fs])[1][0])
    error=db(measured)-db(theoretical)
    checks.append(dict(test='butter_analytic_vs_sos',configuration='10-20Hz/15Hz/40s',delta=error,tolerance=.02,result='PASS' if abs(error)<.02 else 'FAIL'))
    # FFT energy conservation for exact bin-centred tone with Hann and away from band edges.
    bin_freq=round(15*nfft/fs)*fs/nfft
    tone_signal=AMP*np.sin(2*np.pi*bin_freq*np.arange(nfft)/fs)
    band_power=fft_energy(tone_signal,10,20,window_len=nfft,hop=hop)[0]
    err=db(math.sqrt(band_power)/(AMP/math.sqrt(2)))
    checks.append(dict(test='fft_bin_center_rms',configuration=f'10-20Hz/{bin_freq:.6g}Hz',delta=err,tolerance=.1,result='PASS' if abs(err)<.1 else 'FAIL'))
    for name,rows in [('Frequency_response.csv',response),('Coupling_matrix.csv',coupling),('Band_metrics.csv',metrics),('Amplitude_linearity.csv',amplitudes),('Impulse_and_settling.csv',times),('SELFTEST.csv',checks)]:write_csv(out/name,rows)
    good=sum(c['result']=='PASS' for c in checks)
    report=f'''# LFN-Scout v0.5.41 – Vergleich Legacy-IIR / Butterworth4 / FFT

Erstellt: {dt.datetime.now().isoformat(timespec='seconds')}

**Offline-Self-Test: {good}/{len(checks)} PASS, {len(checks)-good} FAIL.** Einzelheiten: `SELFTEST.csv`. Dies ist **kein** Android-Build, kein Mikrofonversuch und keine physikalische Kalibrierung.

## Aufbau und Normierung

- Abtastrate **{fs} Hz**, identische nominale Bandgrenzen für 10 und 20 Bänder, jeweils **0–100 Hz und 10–60 Hz**. Die FFT misst fensterweise **quadratische Bandenergie**, nicht einen kontinuierlichen gefilterten Sample-Strom.
- Legacy-IIR: unveränderte HP/LP-Rekursion erster Ordnung aus `RollingFilterHalfHourStatistics.kt`, HP für untere Grenze 0 Hz **weggelassen**, LP bleibt aktiv. Zustände anfangs null.
- Butterworth4: bei unterer Grenze >0 Hz **Gesamtordnung vier** (`butter(2, bandpass)` -> vier Pole), SOS; bei 0 Hz **4. Ordnung Tiefpass**, kein 0-Hz-Hochpass. Kein Zero-Phase-`filtfilt`, sondern kausal.
- FFT-Hann: Hann-Fenster **{nfft} Samples = {nfft/fs:.3f} s**, Hop **{hop} Samples = {hop/fs:.3f} s**, binäre Bin-Zuordnung `low <= f < high`, Frequenzauflösung **{fs/nfft:.6f} Hz**, keine Partial-Bin-Korrektur. FFT-Band-RMS basiert auf einseitigem Leistungsspektrum (DC/Nyquist einfach, innere Bins doppelt), normiert mit Fensterenergie. Gerade an Bandgrenzen: Leakage und Bin-Lage beeinflussen den Wert.
- `Frequency_response.csv`: relative **Amplitude** (Sinus, idealisierte Nullzustand-/stationäre Betrachtung). FFT: begrenzte Fenster-Projektion, nicht LTI-Frequenzgang. `Band_metrics.csv`: Maximum und zusammenhängendes Intervall oberhalb Peak/√2 aus einem **0,05-Hz-Raster** (nicht notwendigerweise die nominalen Grenzen). `lower_truncated`/`upper_truncated`: abgeschnittene Messgrenze; für FFT sind die -3-dB-Grenzen nur effektive Schätzgrößen.
- `Coupling_matrix.csv`: Eingangs-Sinustöne in der Mitte jedes nominalen Bandes, in jedem Ausgabeband gegen **denselben Eingangs-RMS** gemessen; kein vertikales Summieren von dB-Werten. Heatmaps zeigen Dämpfung in dB. FFT-Energie ist nicht gleichbedeutend mit kausalem Übersprechen.
- `Amplitude_linearity.csv`: Pegel 0,03, 0,06 und 0,12 Spitzenamplitude, Sinuston Bandmitte; nur *numerische* Linearitätskontrolle. `Impulse_and_settling.csv`: Einschwingzeit bis 90 % des gemessenen stationären 1-s-RMS, Ausschwingen bis unter 10 %. Der Sample-Impuls bei 1 s ist nur bei beiden IIR-Methoden eine echte Filterimpulsantwort. FFT-Impulsbreite bezeichnet stattdessen eine Fenster-Lokalisation; FFT-Gruppenlaufzeit ist als Filtereigenschaft **nicht definiert**.
- Tieffrequenz 0,5–2 Hz: eigene **120-s-Simulation** mit Sinus-Burst von 10 bis 100 s; nicht mit einer kurzen 6-s-Simulation als eingeschwungen bewerten. Sonstige Bursts: 4 bis 16 s, Gesamtdauer 24 s. Kein weiterer Gain vor Messung (PRE_GAIN-Konzept).

## Ausgaben

`Frequency_response.csv`, `Band_metrics.csv`, `Coupling_matrix.csv`, `Amplitude_linearity.csv`, `Impulse_and_settling.csv`, `SELFTEST.csv`, zusätzliche `Response_*.svg`, `Coupling_*.svg`, `Impulse_*.svg`.

## Grenzen und Risiken

1. Keine Aussage zur Mikrofonkalibrierung, Gebäudeakustik, absolutem dB SPL oder Android-Performance. FFT mit langen Fenstern verwischt zeitlich; ein stationär gutes Frequenzband muss impulsförmige Signale nicht gut zeitlich lokalisieren.
2. SOS-Butterworths haben Übergänge/Ringing. 0-Hz-Tiefpass und endliche Beobachtungszeit gesondert betrachten. Gruppenlaufzeit bei IIR frequenzabhängig; sie wird hier **nicht quantitativ geschätzt**, um keine Scheingenauigkeit zu behaupten.
3. Die SVG-Impulse zeigen absolute Sample-Amplitude in dB relativ zu 1. Alle dB-Angaben sind digitale Modellwerte, **nicht dB SPL**.
4. Kein automatischer Sieger: Auswahl nach Ziel **Trennschärfe vs. Latenz vs. Impulstreue**, Leistungs-/Gerätetest ist ein eigener späterer Auftrag.
5. Der Bericht ist ein offline wiederholbarer Quellvergleich, kein Beleg für einen Android-Gradle-Test oder Cloud-Übertragung.
'''
    (out/'VERGLEICHSBERICHT.md').write_text(report,encoding='utf-8')
    print(f'REPORT={out.resolve()} TESTS={len(checks)} PASS={good} FAIL={len(checks)-good}')
    return 0 if good==len(checks) else 1

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output',default='Filtervergleich_v0.5.41')
    p.add_argument('--fft-window',type=int,default=4096)
    p.add_argument('--fft-hop',type=int,default=1024)
    a=p.parse_args()
    if a.fft_window<1024 or a.fft_window>16384 or a.fft_window&(a.fft_window-1):p.error('fft-window must be a power of two in 1024..16384')
    if not 1<=a.fft_hop<=a.fft_window:p.error('hop must be 1..fft-window')
    return run(Path(a.output),(a.fft_window,a.fft_hop))

if __name__=='__main__':sys.exit(main())
