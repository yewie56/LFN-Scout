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
    private const val MON_MAX_HZ = 48000.0 // setup validation ceiling; live selection comes from the analyzer range
    private var rangeMinHz = 0.0
    private var rangeMaxHz = 4000.0
    private var bypassHigh = false
    private var bypassLow = false
    private var bypassShiftHigh = false
    private var bypassShiftLow = false
    private const val OUTPUT_RATE_LIMIT = 0.50 // fixed -6 dBFS digital safety ceiling, also with optional limiter OFF
    private val lock = Any()
    @Volatile var enabled = false; private set
    @Volatile var shifted = false; private set
    /** Direct analyzer only; F is the legacy monitoring bandpass. */
    @Volatile var eqMode = false; private set
    private val eqGains = DoubleArray(8)
    private val eqCutoffs = Array(7) { Array(2) { Biquad() } }
    private val eqCutoffEnabled = BooleanArray(7)
    fun eqGainSnapshot(): List<Double> = synchronized(lock) { eqGains.toList() }
    fun setEqMode(value: Boolean) = synchronized(lock) { eqMode = value; initialized = false }
    fun setEqGain(index: Int, db: Double) = synchronized(lock) {
        if (index in 0..7 && db.isFinite()) eqGains[index] = db.coerceIn(-60.0, 140.0)
    }

    @Volatile var centerHz = 20.0; private set
    @Volatile var levelDb = -24.0; private set
    @Volatile var bandwidthHz = 12.0; private set
    /** Desired width survives edge squeezing and subsequent drag gestures. */
    @Volatile var gestureTargetWidthHz = 12.0; private set
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
        val limits = mapOf("center" to (0.0..MON_MAX_HZ), "level" to (-60.0..140.0),
            "width" to (0.5..MON_MAX_HZ), "shift" to (20.0..400.0),
            "comp_thr" to (-60.0..0.0), "comp_ratio" to (1.0..20.0),
            "comp_attack" to (1.0..200.0), "comp_release" to (20.0..2000.0),
            "comp_knee" to (0.0..20.0), "comp_makeup" to (0.0..30.0),
            "limit_ceiling" to (-30.0..-1.0), "limit_attack" to (0.1..20.0),
            "limit_release" to (10.0..1000.0), "fade" to (0.2..5.0))
        for ((key, range) in limits) require(copy.getDouble(key) in range) { "MON parameter out of range: $key" }
        centerHz=copy.getDouble("center");levelDb=copy.getDouble("level")
        bandwidthHz=copy.getDouble("width");gestureTargetWidthHz=bandwidthHz;shiftHz=copy.getDouble("shift")
        compressorEnabled=copy.getBoolean("comp_on");compressorThresholdDb=copy.getDouble("comp_thr")
        compressorRatio=copy.getDouble("comp_ratio");compressorAttackMs=copy.getDouble("comp_attack")
        compressorReleaseMs=copy.getDouble("comp_release");compressorKneeDb=copy.getDouble("comp_knee")
        compressorMakeupDb=copy.getDouble("comp_makeup");limiterEnabled=copy.getBoolean("limit_on")
        limiterCeilingDb=copy.getDouble("limit_ceiling");limiterAttackMs=copy.getDouble("limit_attack")
        limiterReleaseMs=copy.getDouble("limit_release");fadeSeconds=copy.getDouble("fade")
        setupDraftActive=true
        initialized=false
    }

    /** Non-persistent controller A/B/A probe for the autonomous system test.
     * Preserves the draft/persistence mode as well as all effective MON parameters.
     * Never starts MON or writes preferences. Refuses to run while MON is enabled.
     */
    internal fun probeSetupDraftRoundtrip(a: JSONObject, b: JSONObject, onStage: ((Int) -> Unit)? = null): Boolean = synchronized(lock) {
        require(!enabled) { "MON must be OFF for the non-persistent A/B/A probe" }
        val original = snapshotSetupParameters()
        val originalDraftActive = setupDraftActive
        val originalInitialized = initialized
        try {
            applySetupDraft(a)
            check(snapshotSetupParameters().toString() == a.toString()) { "MON A not applied" }
            onStage?.invoke(0)
            applySetupDraft(b)
            check(snapshotSetupParameters().toString() == b.toString()) { "MON B not applied" }
            onStage?.invoke(1)
            applySetupDraft(a)
            check(snapshotSetupParameters().toString() == a.toString()) { "MON A not restored after B" }
            onStage?.invoke(2)
            true
        } finally {
            // Restore parameters even when the probe throws; preserving the exact
            // prior draft mode avoids turning legacy preference saves off.
            try {
                applySetupDraft(original)
            } finally {
                setupDraftActive = originalDraftActive
                initialized = originalInitialized
            }
        }
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
        centerHz=p.getFloat("center",20f).toDouble().coerceIn(0.0,MON_MAX_HZ)
        levelDb=p.getFloat("level",-24f).toDouble().coerceIn(-60.0,140.0)
        bandwidthHz=p.getFloat("width",12f).toDouble().coerceIn(0.5,MON_MAX_HZ)
        gestureTargetWidthHz=p.getFloat("gesture_target_width",bandwidthHz.toFloat()).toDouble().coerceIn(bandwidthHz,MON_MAX_HZ)
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
    /** Current display range, clipped to the supported MON selection range. */
    fun setSelectionRange(minHz: Double, maxHz: Double) = synchronized(lock) {
        if (!minHz.isFinite() || !maxHz.isFinite()) return@synchronized
        val lo = minHz.coerceIn(0.0, MON_MAX_HZ)
        val hi = maxHz.coerceIn(0.0, MON_MAX_HZ)
        if (hi - lo < 0.5) return@synchronized
        rangeMinHz = lo; rangeMaxHz = hi
        normalizeSelection()
    }
    private fun normalizeSelection() {
        val available = rangeMaxHz - rangeMinHz
        bandwidthHz = bandwidthHz.coerceIn(0.5, available)
        gestureTargetWidthHz = gestureTargetWidthHz.coerceIn(bandwidthHz, available)
        centerHz = centerHz.coerceIn(rangeMinHz + bandwidthHz / 2.0,
            rangeMaxHz - bandwidthHz / 2.0)
        initialized = false
    }
    fun availableBandwidthHz(): Double = synchronized(lock) { rangeMaxHz - rangeMinHz }
    fun selectionMinHz(): Double = synchronized(lock) { rangeMinHz }
    fun selectionMaxHz(): Double = synchronized(lock) { rangeMaxHz }
    fun setCenter(context:Context,hz:Double)=synchronized(lock) {
        if (hz.isFinite()) {
            centerHz=hz.coerceIn(rangeMinHz+bandwidthHz/2,rangeMaxHz-bandwidthHz/2)
            initialized=false
        }
    }
    /** Apply both edges together during a MON drag. A single atomic update avoids
     * intermediate invalid centre/width combinations and never writes settings per frame. */
    fun setGestureEdges(lowHz: Double, highHz: Double) = synchronized(lock) {
        if (!lowHz.isFinite() || !highHz.isFinite()) return@synchronized
        val low = lowHz.coerceIn(rangeMinHz, rangeMaxHz)
        val high = highHz.coerceIn(rangeMinHz, rangeMaxHz)
        if (high - low < 0.5) return@synchronized
        bandwidthHz = high - low
        centerHz = (low + high) / 2.0
        initialized = false
    }
    fun setLevel(context:Context,db:Double) {
        levelDb=db.coerceIn(-60.0,140.0)
    }
    /** Persist once on touch release; never perform SharedPreferences I/O per move event. */
    fun persistTouchSettings(context:Context) = synchronized(lock) { persistIfLegacy(context) }
    fun setBandwidth(context:Context,hz:Double)=synchronized(lock) {
        if (hz.isFinite()) { bandwidthHz=hz.coerceIn(0.5,rangeMaxHz-rangeMinHz); gestureTargetWidthHz=bandwidthHz; normalizeSelection();persistIfLegacy(context) }
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
            .putFloat("width",bandwidthHz.toFloat()).putFloat("gesture_target_width",gestureTargetWidthHz.toFloat()).putFloat("shift",shiftHz.toFloat())
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
        // The requested filter range is a selection, not a promise of passband
        // fidelity. Restrict physical cutoffs to the current input sample rate.
        val requestedBottom=(centerHz-bandwidthHz/2).coerceAtLeast(0.0)
        val requestedTop=(centerHz+bandwidthHz/2).coerceAtMost(rangeMaxHz)
        val safeEdge=nyquist*0.95
        bypassHigh=requestedBottom<=0.0
        bypassLow=requestedTop>=safeEdge
        val bottom=requestedBottom.coerceIn(0.5,nyquist*0.90)
        val top=requestedTop.coerceIn(0.5,safeEdge)
        // SHIFT filtering follows the original band and cannot exceed Nyquist.
        bypassShiftHigh=shiftHz+requestedBottom<=0.0
        bypassShiftLow=shiftHz+requestedTop>=safeEdge
        for(i in 0..3) {
            if (!bypassHigh) high[i].setupHighpass(bottom,rate.toDouble(),0.70710678)
            if (!bypassLow) low[i].setupLowpass(top,rate.toDouble(),0.70710678)
            if (!bypassShiftHigh) shiftHigh[i].setupHighpass((shiftHz+bottom).coerceAtMost(nyquist*0.90),rate.toDouble(),0.70710678)
            if (!bypassShiftLow) shiftLow[i].setupLowpass((shiftHz+top).coerceAtMost(safeEdge),rate.toDouble(),0.70710678)
        }
        val start = rangeMinHz
        val width = (rangeMaxHz - start) / 8.0
        for (k in 0..6) {
            val cutoff = start + (k+1) * width
            eqCutoffEnabled[k] = cutoff > 0.0 && cutoff < nyquist * 0.95
            if (eqCutoffEnabled[k]) {
                for (stage in 0..1) eqCutoffs[k][stage].setupLowpass(cutoff, rate.toDouble(), 0.70710678)
            }
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
        if(!enabled || frame.sampleRateHz !in 8000..96000)return@synchronized null
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
                if (eqMode) {
                    // Parallel lowpass-crossover difference bank. The eight contributions
                    // telescope back to the original PCM at 0 dB on every band.
                    // Gain transitions are smooth in frequency, not rectangular masks.
                    val original = x
                    var previous = 0.0
                    var mixed = 0.0
                    for (band in 0..6) {
                        var cumulative = original
                        if (eqCutoffEnabled[band]) {
                            for (stage in 0..1) cumulative = eqCutoffs[band][stage].process(cumulative)
                        }
                        val part = cumulative - previous
                        mixed += part * 10.0.pow(eqGains[band] / 20.0)
                        previous = cumulative
                    }
                    mixed += (original - previous) * 10.0.pow(eqGains[7] / 20.0)
                    x = mixed
                } else {
                    for(s in 0..3) { if (!bypassHigh) x=high[s].process(x); if (!bypassLow) x=low[s].process(x) }
                }
                if(shifted && !eqMode) {
                    x *= 2.0*cos(phase)
                    phase+=delta; if(phase>=2*PI) phase-=2*PI
                    for(s in 0..3) { if (!bypassShiftHigh) x=shiftHigh[s].process(x); if (!bypassShiftLow) x=shiftLow[s].process(x) }
                }
                processed[i]=if(x.isFinite()) x else 0.0
            }
            // Interpolate the post-filtered samples into a 48-kHz media stream.
            // Linear interpolation is sufficient for the 8-kHz MON selection at
            // 48-kHz output only with sample rates supporting that selection;
            // a future quality pass should use a polyphase resampler.
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
