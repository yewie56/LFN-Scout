package de.brummbeobachter.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioDeviceInfo
import java.io.Closeable
import kotlin.math.*

/** Independent pre-gain PCM monitoring branch; never feeds the analyzer, FFT or WAV. */
internal object AnalyzerMonitor {
    private const val PREFS = "lfn_analyzer_monitor_049"
    private const val OUTPUT_RATE = 48000
    private const val OUTPUT_LIMIT = 0.10 // -20 dBFS peak, digital ceiling only
    private val lock = Any()
    @Volatile var enabled = false; private set
    @Volatile var shifted = false; private set
    @Volatile var centerHz = 20.0; private set
    @Volatile var levelDb = -24.0; private set
    @Volatile var bandwidthHz = 12.0; private set
    @Volatile var shiftHz = 100.0; private set
    @Volatile var actualDevice = ""; private set
    @Volatile var outputRoute = "Kein AudioTrack"; private set
    @Volatile var outputDiagnostic = "Noch keine Wiedergabe"; private set
    @Volatile var outputBlocks: Long = 0; private set
    @Volatile var outputErrors: Long = 0; private set
    @Volatile var limiterHits: Long = 0; private set
    @Volatile var diagnosticToneEnabled = false; private set
    private var tonePhase = 0.0
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
        levelDb=p.getFloat("level",-24f).toDouble().coerceIn(-60.0,100.0)
        bandwidthHz=p.getFloat("width",12f).toDouble().coerceIn(2.0,150.0)
        shiftHz=p.getFloat("shift",100f).toDouble().coerceIn(20.0,400.0)
        shifted=false; enabled=false
    }
    fun setCenter(context:Context,hz:Double)=synchronized(lock) {
        centerHz=hz.coerceIn(1.0,200.0); initialized=false; save(context)
    }
    fun setLevel(context:Context,db:Double)=synchronized(lock) {
        levelDb=db.coerceIn(-60.0,100.0); save(context)
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
        outputBlocks=0;outputErrors=0;limiterHits=0;outputDiagnostic="Start angefordert"
        diagnosticToneEnabled=false;tonePhase=0.0
        subscription=engine.subscribe(queueCapacity=8) { frame -> process(frame) }
        enabled=true; true
    }
    fun stop() = synchronized(lock) {
        enabled=false;shifted=false;diagnosticToneEnabled=false;subscription?.close();subscription=null
        try { output?.pause();output?.flush();output?.release() } catch (_:Exception) {}
        output=null;actualDevice="";outputRoute="Kein AudioTrack";rate=0;initialized=false;ramp=0.0
    }
    fun toggleDiagnosticTone()=synchronized(lock) { if(enabled) { diagnosticToneEnabled=!diagnosticToneEnabled; tonePhase=0.0 } }
    fun toggleShift()=synchronized(lock) { if(enabled) { shifted=!shifted;ramp=0.0;initialized=false } }
    private fun configure(sampleRate:Int) {
        if(sampleRate==rate && output!=null)return
        try { output?.pause();output?.release() } catch (_:Exception) {}
        rate=sampleRate;initialized=false;ramp=0.0
        val min=AudioTrack.getMinBufferSize(OUTPUT_RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT)
        require(min>0)
        output=AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(OUTPUT_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(max(min*2,OUTPUT_RATE/8*2)).setTransferMode(AudioTrack.MODE_STREAM).build().also { it.play() }
        outputDiagnostic="AudioTrack PLAYING; ${sampleRate} -> ${OUTPUT_RATE} Hz; ${output?.state}"
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
            // Input filter runs at its original sample rate. Output is always 48 kHz:
            // Bluetooth A2DP devices commonly reject 8-kHz media streams.
            val processed=DoubleArray(frame.samples.size)
            val gain=10.0.pow(levelDb/20.0)
            val delta=2*PI*shiftHz/rate
            for(i in frame.samples.indices) {
                var x=frame.samples[i].toDouble()/32768.0
                for(s in 0..3) { x=high[s].process(x);x=low[s].process(x) }
                if(shifted) {
                    x *= 2.0*cos(phase)
                    phase+=delta; if(phase>=2*PI) phase-=2*PI
                    for(s in 0..3) { x=shiftHigh[s].process(x);x=shiftLow[s].process(x) }
                }
                processed[i]=if(x.isFinite()) x else 0.0
            }
            // Interpolate the post-filtered samples into a 48-kHz media stream.
            // This is not a bandlimited resampler; the preceding <400-Hz bandpass
            // leaves ample separation from Nyquist for the current monitor use.
            val count=(processed.size.toLong()*OUTPUT_RATE/rate).toInt().coerceAtLeast(1)
            val buffer=ShortArray(count)
            for(i in 0 until count) {
                val position=i.toDouble()*rate/OUTPUT_RATE
                val idx=position.toInt().coerceIn(0,processed.lastIndex)
                val fraction=position-idx
                val x=processed[idx]*(1-fraction)+processed[min(idx+1,processed.lastIndex)]*fraction
                ramp=(ramp+1.0/(OUTPUT_RATE*1.5)).coerceAtMost(1.0)
                val raw=if(diagnosticToneEnabled) {
                    // Fixed low-level output-only 440-Hz test: bypasses input,
                    // bandpass, SHIFT and +100-dB gain to isolate output routing.
                    val tone=0.02*sin(tonePhase)*ramp
                    tonePhase+=2*PI*440/OUTPUT_RATE
                    if(tonePhase>=2*PI)tonePhase-=2*PI
                    tone
                } else x*gain*ramp
                // Always keep the digital output below -20 dBFS even for +100 dB gain.
                // Does NOT guarantee safe sound pressure from arbitrary headphones.
                val limited=OUTPUT_LIMIT*tanh(raw/OUTPUT_LIMIT)
                if(abs(raw)>OUTPUT_LIMIT) limiterHits++
                buffer[i]=(limited*32767.0).roundToInt().coerceIn(-32767,32767).toShort()
            }
            val track=output ?: return@synchronized
            val wrote=track.write(buffer,0,buffer.size,AudioTrack.WRITE_BLOCKING)
            if(wrote<0) { outputErrors++;outputDiagnostic="AudioTrack.write Fehler $wrote" }
            else {
                outputBlocks++
                val routed=track.routedDevice
                outputRoute=if(routed==null) "Unbekannt" else when(routed.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Kabel-Kopfhörer"
                    AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Kabel-Headset"
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Lautsprecher"
                    AudioDeviceInfo.TYPE_USB_HEADSET -> "USB-Headset"
                    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB-Audio"
                    else -> "Typ ${routed.type}: ${routed.productName}"
                }
                outputDiagnostic="PLAYING=${track.playState==AudioTrack.PLAYSTATE_PLAYING}; out=$outputRoute; ${rate}->${OUTPUT_RATE}Hz; write=$wrote/${buffer.size}; Gain=${"%.1f".format(java.util.Locale.US,levelDb)} dB; Test440=$diagnosticToneEnabled; Limiter=$limiterHits; Fehler=$outputErrors"
            }
        } catch (e:Exception) { outputErrors++;outputDiagnostic="Mithoer-Fehler: ${e.javaClass.simpleName}: ${e.message}"; stop() }
    }
    fun diagnostic():String = "MON enabled=$enabled SHIFT=$shifted input=$actualDevice output=$outputRoute blocks=$outputBlocks errors=$outputErrors Test440=$diagnosticToneEnabled; $outputDiagnostic"
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
