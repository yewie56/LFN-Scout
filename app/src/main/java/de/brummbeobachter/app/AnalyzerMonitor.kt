package de.brummbeobachter.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioDeviceInfo
import java.io.Closeable
import org.json.JSONObject
import kotlin.math.*

/** Independent pre-gain PCM monitoring branch; never feeds the analyzer, FFT or WAV. */
internal object AnalyzerMonitor {
    private const val PREFS = "lfn_analyzer_monitor_049"
    private const val OUTPUT_RATE = 48000
    private const val OUTPUT_RATE_LIMIT = 0.50 // fixed -6 dBFS digital safety ceiling, also with optional limiter OFF
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
    @Volatile var compressorEnabled = false; private set
    @Volatile var compressorThresholdDb = -30.0; private set
    @Volatile var compressorRatio = 4.0; private set
    @Volatile var compressorAttackMs = 15.0; private set
    @Volatile var compressorReleaseMs = 300.0; private set
    @Volatile var compressorKneeDb = 6.0; private set
    @Volatile var compressorMakeupDb = 0.0; private set
    @Volatile var limiterEnabled = true; private set
    @Volatile var limiterCeilingDb = -20.0; private set
    @Volatile var limiterAttackMs = 1.0; private set
    @Volatile var limiterReleaseMs = 100.0; private set
    @Volatile var fadeSeconds = 1.5; private set
    @Volatile var outputPeakDbfs = -120.0; private set
    @Volatile var gainReductionDb = 0.0; private set
    private var compressorEnvelope = 0.0
    private var compressorGain = 1.0
    private var limiterEnvelope = 0.0
    private var limiterGain = 1.0
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

    @Volatile private var initialized = false
    // Explicit Analyzer-setup live draft: never persist MON adjustments into legacy preferences.
    @Volatile private var setupDraftActive = false
    private fun persistIfLegacy(context: Context) { if (!setupDraftActive) save(context) }

    /** Read current effective MON values, without changing storage or runtime. */
    fun snapshotSetupParameters(): JSONObject = synchronized(lock) {
        JSONObject().put("center", centerHz).put("level", levelDb)
            .put("width", bandwidthHz).put("shift", shiftHz)
            .put("comp_on", compressorEnabled).put("comp_thr", compressorThresholdDb)
            .put("comp_ratio", compressorRatio).put("comp_attack", compressorAttackMs)
            .put("comp_release", compressorReleaseMs).put("comp_knee", compressorKneeDb)
            .put("comp_makeup", compressorMakeupDb).put("limit_on", limiterEnabled)
            .put("limit_ceiling", limiterCeilingDb).put("limit_attack", limiterAttackMs)
            .put("limit_release", limiterReleaseMs).put("fade", fadeSeconds)
    }

    /** Only explicit setup selection should call this. Reject partial MON payloads. */
    fun applySetupDraft(values: JSONObject) = synchronized(lock) {
        val copy = JSONObject(values.toString())
        AnalyzerSetupFormat.validate(JSONObject().put("monitor", copy))
        val expected = setOf("center", "level", "width", "shift", "comp_on", "comp_thr",
            "comp_ratio", "comp_attack", "comp_release", "comp_knee", "comp_makeup",
            "limit_on", "limit_ceiling", "limit_attack", "limit_release", "fade")
        require(copy.length() == expected.size && expected.all { copy.has(it) && !copy.isNull(it) }) {
            "MON setup draft is incomplete; legacy values would otherwise leak into the selected setup"
        }
        // Validate numeric ranges before mutating any runtime field.
        val limits = mapOf("center" to (1.0..200.0), "level" to (-60.0..120.0),
            "width" to (2.0..150.0), "shift" to (20.0..400.0),
            "comp_thr" to (-60.0..0.0), "comp_ratio" to (1.0..20.0),
            "comp_attack" to (1.0..200.0), "comp_release" to (20.0..2000.0),
            "comp_knee" to (0.0..20.0), "comp_makeup" to (0.0..30.0),
            "limit_ceiling" to (-30.0..-1.0), "limit_attack" to (0.1..20.0),
            "limit_release" to (10.0..1000.0), "fade" to (0.2..5.0))
        for ((key, range) in limits) require(copy.getDouble(key) in range) { "MON parameter out of range: $key" }
        centerHz=copy.getDouble("center");levelDb=copy.getDouble("level")
        bandwidthHz=copy.getDouble("width");shiftHz=copy.getDouble("shift")
        compressorEnabled=copy.getBoolean("comp_on");compressorThresholdDb=copy.getDouble("comp_thr")
        compressorRatio=copy.getDouble("comp_ratio");compressorAttackMs=copy.getDouble("comp_attack")
        compressorReleaseMs=copy.getDouble("comp_release");compressorKneeDb=copy.getDouble("comp_knee")
        compressorMakeupDb=copy.getDouble("comp_makeup");limiterEnabled=copy.getBoolean("limit_on")
        limiterCeilingDb=copy.getDouble("limit_ceiling");limiterAttackMs=copy.getDouble("limit_attack")
        limiterReleaseMs=copy.getDouble("limit_release");fadeSeconds=copy.getDouble("fade")
        setupDraftActive=true
        initialized=false
    }

    /** Return to original legacy persistence behavior when the new setup UI is not active. */
    fun releaseSetupDraft(context: Context) = synchronized(lock) {
        require(!enabled) { "Stop MON before releasing the setup draft" }
        setupDraftActive=false
        load(context)
    }


    fun load(context: Context) = synchronized(lock) {
        if (setupDraftActive) return@synchronized
        val p = context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
        centerHz=p.getFloat("center",20f).toDouble().coerceIn(1.0,200.0)
        levelDb=p.getFloat("level",-24f).toDouble().coerceIn(-60.0,120.0)
        bandwidthHz=p.getFloat("width",12f).toDouble().coerceIn(2.0,150.0)
        shiftHz=p.getFloat("shift",100f).toDouble().coerceIn(20.0,400.0)
        compressorEnabled=p.getBoolean("comp_on",false)
        compressorThresholdDb=p.getFloat("comp_thr",-30f).toDouble().coerceIn(-60.0,0.0)
        compressorRatio=p.getFloat("comp_ratio",4f).toDouble().coerceIn(1.0,20.0)
        compressorAttackMs=p.getFloat("comp_attack",15f).toDouble().coerceIn(1.0,200.0)
        compressorReleaseMs=p.getFloat("comp_release",300f).toDouble().coerceIn(20.0,2000.0)
        compressorKneeDb=p.getFloat("comp_knee",6f).toDouble().coerceIn(0.0,20.0)
        compressorMakeupDb=p.getFloat("comp_makeup",0f).toDouble().coerceIn(0.0,30.0)
        limiterEnabled=p.getBoolean("limit_on",true)
        limiterCeilingDb=p.getFloat("limit_ceiling",-20f).toDouble().coerceIn(-30.0,-1.0)
        limiterAttackMs=p.getFloat("limit_attack",1f).toDouble().coerceIn(0.1,20.0)
        limiterReleaseMs=p.getFloat("limit_release",100f).toDouble().coerceIn(10.0,1000.0)
        fadeSeconds=p.getFloat("fade",1.5f).toDouble().coerceIn(0.2,5.0)
        shifted=false; enabled=false
    }
    fun setCenter(context:Context,hz:Double) {
        centerHz=hz.coerceIn(1.0,200.0); initialized=false
    }
    fun setLevel(context:Context,db:Double) {
        levelDb=db.coerceIn(-60.0,120.0)
    }
    /** Persist once on touch release; never perform SharedPreferences I/O per move event. */
    fun persistTouchSettings(context:Context) = synchronized(lock) { persistIfLegacy(context) }
    fun setBandwidth(context:Context,hz:Double)=synchronized(lock) {
        bandwidthHz=hz.coerceIn(2.0,150.0); initialized=false;persistIfLegacy(context)
    }
    fun setShift(context:Context,hz:Double)=synchronized(lock) {
        shiftHz=hz.coerceIn(20.0,400.0);initialized=false;persistIfLegacy(context)
    }
    fun setDynamics(context:Context, key:String, value:Double)=synchronized(lock) {
        when(key) {
            "comp_thr" -> compressorThresholdDb=value.coerceIn(-60.0,0.0)
            "comp_ratio" -> compressorRatio=value.coerceIn(1.0,20.0)
            "comp_attack" -> compressorAttackMs=value.coerceIn(1.0,200.0)
            "comp_release" -> compressorReleaseMs=value.coerceIn(20.0,2000.0)
            "comp_knee" -> compressorKneeDb=value.coerceIn(0.0,20.0)
            "comp_makeup" -> compressorMakeupDb=value.coerceIn(0.0,30.0)
            "limit_ceiling" -> limiterCeilingDb=value.coerceIn(-30.0,-1.0)
            "limit_attack" -> limiterAttackMs=value.coerceIn(0.1,20.0)
            "limit_release" -> limiterReleaseMs=value.coerceIn(10.0,1000.0)
            "fade" -> fadeSeconds=value.coerceIn(0.2,5.0)
        }; persistIfLegacy(context)
    }
    fun setDynamicsEnabled(context:Context, compressor:Boolean?=null, limiter:Boolean?=null)=synchronized(lock) {
        compressor?.let { compressorEnabled=it }
        limiter?.let { limiterEnabled=it }
        persistIfLegacy(context)
    }
    private fun save(context: Context) {
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
            .putFloat("center",centerHz.toFloat()).putFloat("level",levelDb.toFloat())
            .putFloat("width",bandwidthHz.toFloat()).putFloat("shift",shiftHz.toFloat())
            .putBoolean("comp_on",compressorEnabled).putFloat("comp_thr",compressorThresholdDb.toFloat())
            .putFloat("comp_ratio",compressorRatio.toFloat()).putFloat("comp_attack",compressorAttackMs.toFloat())
            .putFloat("comp_release",compressorReleaseMs.toFloat()).putFloat("comp_knee",compressorKneeDb.toFloat())
            .putFloat("comp_makeup",compressorMakeupDb.toFloat()).putBoolean("limit_on",limiterEnabled)
            .putFloat("limit_ceiling",limiterCeilingDb.toFloat()).putFloat("limit_attack",limiterAttackMs.toFloat())
            .putFloat("limit_release",limiterReleaseMs.toFloat()).putFloat("fade",fadeSeconds.toFloat()).apply()
    }
    fun start(context:Context, preserveCurrentParameters:Boolean = false):Boolean = synchronized(lock) {
        if(enabled)return true
        if (!setupDraftActive && !preserveCurrentParameters) load(context)
        val engine = AnalyzerSessionGateway.rawPcm
        ramp=0.0;phase=0.0;rate=0;initialized=false
        compressorEnvelope=0.0;compressorGain=1.0;limiterEnvelope=0.0;limiterGain=1.0
        outputPeakDbfs=-120.0;gainReductionDb=0.0
        outputBlocks=0;outputErrors=0;limiterHits=0;outputDiagnostic="Start angefordert"
        diagnosticToneEnabled=false;tonePhase=0.0
        subscription=engine.subscribe(queueCapacity=8) { frame -> process(frame) }
        enabled=true; true
    }
    fun stop() = synchronized(lock) {
        enabled=false;shifted=false;diagnosticToneEnabled=false;subscription?.close();subscription=null
        try { output?.pause();output?.flush();output?.release() } catch (_:Exception) {}
        output=null;actualDevice="";outputRoute="Kein AudioTrack";rate=0;initialized=false;ramp=0.0
        outputPeakDbfs=-120.0
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
    private fun smootherCoeff(timeMs:Double):Double = exp(-1.0/(OUTPUT_RATE*timeMs*0.001).coerceAtLeast(1.0))
    private fun compressorTarget(db:Double):Double {
        val over=db-compressorThresholdDb
        val knee=compressorKneeDb
        val reduction=when {
            knee<=0.0 -> max(0.0,over)*(1.0-1.0/compressorRatio)
            over<=-knee/2 -> 0.0
            over>=knee/2 -> over*(1.0-1.0/compressorRatio)
            else -> (1.0-1.0/compressorRatio)*(over+knee/2).pow(2.0)/(2*knee)
        }
        return 10.0.pow((-reduction+compressorMakeupDb)/20.0)
    }
    private fun process(frame:AnalyzerPcmHub.Frame) {
        val packet = synchronized(lock) {
        if(!enabled || frame.sampleRateHz !in 8000..48000)return@synchronized null
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
            var blockPeak=0.0
            var blockReduction=0.0
            val ceiling=10.0.pow(limiterCeilingDb/20.0)
            val useCompressor=compressorEnabled && !diagnosticToneEnabled
            val useLimiter=limiterEnabled && !diagnosticToneEnabled
            for(i in 0 until count) {
                val position=i.toDouble()*rate/OUTPUT_RATE
                val idx=position.toInt().coerceIn(0,processed.lastIndex)
                val fraction=position-idx
                val x=processed[idx]*(1-fraction)+processed[min(idx+1,processed.lastIndex)]*fraction
                ramp=(ramp+1.0/(OUTPUT_RATE*fadeSeconds)).coerceAtMost(1.0)
                val raw=if(diagnosticToneEnabled) {
                    // Fixed low-level output-only 440-Hz test: bypasses input,
                    // bandpass, SHIFT and +100-dB gain to isolate output routing.
                    val tone=0.02*sin(tonePhase)*ramp
                    tonePhase+=2*PI*440/OUTPUT_RATE
                    if(tonePhase>=2*PI)tonePhase-=2*PI
                    tone
                } else x*gain*ramp
                // Feed-forward dynamics on the independent output branch only.
                var y=raw
                if(useCompressor) {
                    val power=abs(y)
                    val envCoeff=smootherCoeff(if(power>compressorEnvelope)compressorAttackMs else compressorReleaseMs)
                    compressorEnvelope=envCoeff*compressorEnvelope+(1-envCoeff)*power
                    val db=20*log10(compressorEnvelope.coerceAtLeast(1e-9))
                    val target=compressorTarget(db)
                    val coeff=smootherCoeff(if(target<compressorGain)compressorAttackMs else compressorReleaseMs)
                    compressorGain=coeff*compressorGain+(1-coeff)*target
                    y*=compressorGain
                }
                if(useLimiter) {
                    val power=abs(y)
                    val coeff=smootherCoeff(if(power>limiterEnvelope)limiterAttackMs else limiterReleaseMs)
                    limiterEnvelope=coeff*limiterEnvelope+(1-coeff)*power
                    val target=min(1.0,ceiling/limiterEnvelope.coerceAtLeast(1e-9))
                    val gainCoeff=smootherCoeff(if(target<limiterGain)limiterAttackMs else limiterReleaseMs)
                    limiterGain=gainCoeff*limiterGain+(1-gainCoeff)*target
                    y*=limiterGain
                    if(abs(y)>ceiling) { y=y.coerceIn(-ceiling,ceiling);limiterHits++ }
                    blockReduction=max(blockReduction,-20*log10(limiterGain.coerceAtLeast(1e-9)))
                }
                // Hard digital safety ceiling remains active even if optional limiter is OFF.
                val limited=y.coerceIn(-OUTPUT_RATE_LIMIT,OUTPUT_RATE_LIMIT)
                blockPeak=max(blockPeak,abs(limited))
                buffer[i]=(limited*32767.0).roundToInt().coerceIn(-32767,32767).toShort()
            }
            outputPeakDbfs=20*log10(blockPeak.coerceAtLeast(1e-6))
            gainReductionDb=blockReduction
            val track=output ?: return@synchronized null
            Pair(track,buffer)

        } catch (e:Exception) { outputErrors++;outputDiagnostic="Mithoer-Fehler: ${e.javaClass.simpleName}: ${e.message}"; stop(); null }
        }
        // Bluetooth/AudioTrack can block here; never hold the shared DSP/config lock.
        if(packet != null) {
            val (track,buffer)=packet
            AnalyzerMonitorRecording.accept(buffer)
            try {
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
                    outputDiagnostic="PLAYING=${track.playState==AudioTrack.PLAYSTATE_PLAYING}; out=$outputRoute; ${rate}->${OUTPUT_RATE}Hz; write=$wrote/${buffer.size}; Gain=${"%.1f".format(java.util.Locale.US,levelDb)} dB; Test440=$diagnosticToneEnabled; Limiter=$limiterHits; Kompressor=$compressorEnabled; LimiterAktiv=$limiterEnabled; Peak=${"%.1f".format(java.util.Locale.US,outputPeakDbfs)}dBFS; GR=${"%.1f".format(java.util.Locale.US,gainReductionDb)}dB; Fehler=$outputErrors"
                }
            } catch(e:Exception) {
                outputErrors++
                outputDiagnostic="AudioTrack.write: ${e.javaClass.simpleName}: ${e.message}"
            }
        }
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
