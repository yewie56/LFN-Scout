package de.brummbeobachter.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.io.Closeable
import kotlin.math.*

/** Independent pre-gain PCM monitoring branch; never feeds the analyzer, FFT or WAV. */
internal object AnalyzerMonitor {
    private const val PREFS = "lfn_analyzer_monitor_049"
    private val lock = Any()
    @Volatile var enabled = false; private set
    @Volatile var shifted = false; private set
    @Volatile var centerHz = 20.0; private set
    @Volatile var levelDb = -24.0; private set
    @Volatile var bandwidthHz = 12.0; private set
    @Volatile var shiftHz = 100.0; private set
    @Volatile var actualDevice = ""; private set
    private var subscription: Closeable? = null
    private var output: AudioTrack? = null
    private var rate = 0
    private var low = Array(4) { Biquad() }
    private var high = Array(4) { Biquad() }
    private var shiftLow = Array(4) { Biquad() }
    private var shiftHigh = Array(4) { Biquad() }
    private var ramp = 0.0
    private var phase = 0.0

    private var initialized = false

    fun load(context: Context) = synchronized(lock) {
        val p = context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
        centerHz=p.getFloat("center",20f).toDouble().coerceIn(1.0,200.0)
        levelDb=p.getFloat("level",-24f).toDouble().coerceIn(-60.0,-6.0)
        bandwidthHz=p.getFloat("width",12f).toDouble().coerceIn(2.0,150.0)
        shiftHz=p.getFloat("shift",100f).toDouble().coerceIn(20.0,400.0)
        shifted=false; enabled=false
    }
    fun setCenter(context:Context,hz:Double)=synchronized(lock) {
        centerHz=hz.coerceIn(1.0,200.0); initialized=false; save(context)
    }
    fun setLevel(context:Context,db:Double)=synchronized(lock) {
        levelDb=db.coerceIn(-60.0,-6.0); save(context)
    }
    fun setBandwidth(context:Context,hz:Double)=synchronized(lock) {
        bandwidthHz=hz.coerceIn(2.0,150.0); initialized=false;save(context)
    }
    fun setShift(context:Context,hz:Double)=synchronized(lock) {
        shiftHz=hz.coerceIn(20.0,400.0);initialized=false;save(context)
    }
    private fun save(context: Context) {
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putFloat("center",centerHz.toFloat()).putFloat("level",levelDb.toFloat())
            .putFloat("width",bandwidthHz.toFloat()).putFloat("shift",shiftHz.toFloat()).apply()
    }
    fun start(context:Context):Boolean = synchronized(lock) {
        if(enabled)return true
        load(context)
        val engine = AnalyzerSessionGateway.rawPcm
        ramp=0.0;phase=0.0;rate=0;initialized=false
        subscription=engine.subscribe(queueCapacity=8) { frame -> process(frame) }
        enabled=true; true
    }
    fun stop() = synchronized(lock) {
        enabled=false;shifted=false;subscription?.close();subscription=null
        try { output?.pause();output?.flush();output?.release() } catch (_:Exception) {}
        output=null;actualDevice="";rate=0;initialized=false;ramp=0.0
    }
    fun toggleShift()=synchronized(lock) { if(enabled) { shifted=!shifted;ramp=0.0;initialized=false } }
    private fun configure(sampleRate:Int) {
        if(sampleRate==rate && output!=null)return
        try { output?.pause();output?.release() } catch (_:Exception) {}
        rate=sampleRate;initialized=false;ramp=0.0
        val min=AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT)
        require(min>0)
        output=AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(max(min*2,rate/4*2)).setTransferMode(AudioTrack.MODE_STREAM).build().also { it.play() }
    }
    private fun rebuild() {
        val nyquist=rate/2.0
        val bottom=(centerHz-bandwidthHz/2).coerceIn(0.5,nyquist*0.8)
        val top=(centerHz+bandwidthHz/2).coerceIn(bottom+0.5,nyquist*0.9)
        for(i in 0..3) { high[i].setupHighpass(bottom,rate.toDouble(),0.70710678)
            low[i].setupLowpass(top,rate.toDouble(),0.70710678)
            shiftHigh[i].setupHighpass((shiftHz+bottom).coerceAtMost(nyquist*0.85),rate.toDouble(),0.70710678)
            shiftLow[i].setupLowpass((shiftHz+top).coerceAtMost(nyquist*0.9),rate.toDouble(),0.70710678)
        }
        initialized=true
    }
    private fun process(frame:AnalyzerPcmHub.Frame) = synchronized(lock) {
        if(!enabled || frame.sampleRateHz !in 8000..48000)return@synchronized
        try {
            configure(frame.sampleRateHz)
            if(!initialized)rebuild()
            actualDevice=frame.deviceLabel
            val buffer=ShortArray(frame.samples.size)
            val gain=10.0.pow(levelDb/20.0)
            val delta=2*PI*shiftHz/rate
            for(i in frame.samples.indices) {
                var x=frame.samples[i].toDouble()/32768.0
                for(s in 0..3) { x=high[s].process(x);x=low[s].process(x) }
                if(shifted) { // Real additive modulation: double sideband; no pitch resampling.
                    x *= 2.0*cos(phase)
                    phase+=delta; if(phase>=2*PI) phase-=2*PI
                    // Suppress the lower heterodyne sideband, leaving the upshifted band.
                    for(s in 0..3) { x=shiftHigh[s].process(x);x=shiftLow[s].process(x) }
                }
                ramp=(ramp+1.0/(rate*0.35)).coerceAtMost(1.0)
                // Hard safety ceiling (prevents overflow), user gain is restricted to attenuation.
                buffer[i]=(x*gain*ramp*32767.0).roundToInt().coerceIn(-32767,32767).toShort()
            }
            output?.write(buffer,0,buffer.size,AudioTrack.WRITE_BLOCKING)
        } catch (_:Exception) { stop() }
    }
    private class Biquad {
        private var b0=1.0;private var b1=0.0;private var b2=0.0;private var a1=0.0;private var a2=0.0
        private var z1=0.0;private var z2=0.0
        fun setupLowpass(fc:Double,fs:Double,q:Double) { setup(fc,fs,q,false) }
        fun setupHighpass(fc:Double,fs:Double,q:Double) { setup(fc,fs,q,true) }
        private fun setup(fc:Double,fs:Double,q:Double,hp:Boolean) {
            val omega=2*PI*fc/fs;val c=cos(omega);val alpha=sin(omega)/(2*q)
            val a0=1+alpha
            b0=(1+(if(hp)c else -c))/(2*a0)
            b1=(if(hp) -(1+c) else (1-c))/a0
            b2=b0;a1=-2*c/a0;a2=(1-alpha)/a0
            z1=0.0;z2=0.0
        }
        fun process(x:Double):Double { val y=b0*x+z1;z1=b1*x-a1*y+z2;z2=b2*x-a2*y;return y }
    }
}
