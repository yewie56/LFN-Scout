package de.brummbeobachter.app

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

/** Saved electrical FFT noise-power estimate (not microphone acoustic self-noise). */
internal object NoiseFloorReference {
    data class State(val enabled: Boolean, val available: Boolean, val capturing: Boolean,
                     val progress: Int, val targetSeconds: Int, val info: String, val mode: String)
    private val mutex = Any()
    private var file: File? = null
    private var enabled = false
    private var mode = "OFF" // OFF, MEASURED, MODEL; old enabled setting remains backward compatible
    private var profile: DoubleArray? = null
    private var rate = 0
    private var fft = 0
    private var window = ""
    private var gain = 0
    private val spectrumAverage = NoisePowerAverage()
    private var frames = 0
    private var startedMs = 0L
    private var targetMs = 10000L
    private var configuredSeconds = 10
    private var capturing = false
    private var info = "Keine Kalibration"
    // Additional independently measured 1-s band powers, keyed by the exact rolling configuration.
    // No assumed conversion from a spectral FFT bin to an IIR/FFT band energy.
    private val bandNoise = mutableMapOf<String, Double>()
    private val bandCaptureSum = mutableMapOf<String, Double>()
    private val bandCaptureCount = mutableMapOf<String, Int>()
    private var measuredAtMs = 0L
    // 1-kHz rolling FFT reference captured from the SAME raw stream, with identical
    // Hann-energy normalization; do not substitute 8-kHz coherent-gain powers.
    private var rollingPower: DoubleArray? = null
    private var rollingPowerSum = DoubleArray(0)
    private var rollingFrames = 0

    fun currentMode(): String = synchronized(mutex) { if (!enabled) "OFF" else mode }
    private fun selectedReference(measured: DoubleArray, fs: Int, n: Int): DoubleArray? =
        if (mode == "MODEL") NoisePowerModel.reference(measured, fs, n) else measured
    fun modelDiagnostic(): String = synchronized(mutex) {
        val values = profile ?: return@synchronized "Kein Profil"
        val fitted = NoisePowerModel.fit(values, rate, fft)
            ?: return@synchronized "1/f-Modell nicht verfügbar (Referenz ungeeignet)"
        "Referenzabschluss: 1 kΩ (Benutzerangabe) | alpha=%.2f | Modellfehler=%.2f dB | %d Bins".format(
            java.util.Locale.US, fitted.alpha, fitted.rmsLogErrorDb, fitted.binCount)
    }
    fun calibrationSeconds(): Int = synchronized(mutex) { configuredSeconds }
    fun setCalibrationSeconds(seconds: Int): Boolean = synchronized(mutex) {
        if (seconds !in 10..600 || capturing) return@synchronized false
        configuredSeconds = seconds
        contextRef?.getSharedPreferences("noise_floor_reference",Context.MODE_PRIVATE)
            ?.edit()?.putInt("capture_seconds",seconds)?.apply()
        true
    }
    /** Diagnostic readout of the unchanged measured profile (never the subtracted spectrum). */
    fun diagnosticReference(): String = synchronized(mutex) {
        val values=profile ?: return@synchronized "Keine Rauschreferenz gespeichert"
        if(rate <= 0 || fft <= 0 || values.size < 5) return@synchronized "Referenzparameter fehlen"
        val frequencies=DoubleArray(values.size) { it*rate.toDouble()/fft }
        "Referenz: " + NoiseDiagnostic.reportSlope(frequencies, values) + " | gültige Fenster: $frames"
    }

    fun rollingProfileReady(): Boolean = synchronized(mutex) { rollingPower?.size == 513 }

    /** Each rolling FFT frame is 1024 bins at fs=1000 Hz (513 one-sided powers). */
    fun compensateRollingSpectrum(input: DoubleArray): DoubleArray = synchronized(mutex) {
        require(input.size == 513)
        if (capturing) {
            if (rollingPowerSum.size != input.size) rollingPowerSum = DoubleArray(input.size)
            for (i in input.indices) rollingPowerSum[i] += input[i]
            rollingFrames++
            return@synchronized input
        }
        val measuredRef = rollingPower ?: return@synchronized input
        if (!enabled) return@synchronized input
        val ref = selectedReference(measuredRef, 1000, 1024) ?: return@synchronized input
        if (ref.size != input.size) return@synchronized input
        DoubleArray(input.size) { i ->
            val measured = input[i]
            if (!measured.isFinite() || measured < 0.0) measured
            else NoisePowerSubtraction.residual(measured, ref[i])
        }
    }


    fun overallKey(sampleRate: Int, lowHz: Int, highHz: Int): String =
        "overall:$sampleRate:$lowHz:$highHz"
    fun filterKey(sampleRate: Int, method: String, bandCount: Int, low: Double, high: Double): String =
        "filter:$sampleRate:$method:$bandCount:${"%.6f".format(java.util.Locale.US,low)}:${"%.6f".format(java.util.Locale.US,high)}"

    /** Called only at the 1-s epoch boundary before percentile aggregation. */
    fun compensateEpoch(key: String, originalDb: Double): Double = synchronized(mutex) {
        if (capturing) {
            bandCaptureSum[key] = (bandCaptureSum[key] ?: 0.0) + power(originalDb)
            bandCaptureCount[key] = (bandCaptureCount[key] ?: 0) + 1
            return@synchronized originalDb
        }
        val reference = bandNoise[key] ?: return@synchronized originalDb
        if (!enabled || !reference.isFinite() || reference < 0.0) return@synchronized originalDb
        val measured = power(originalDb)
        NoisePowerSubtraction.toDisplayDb(NoisePowerSubtraction.residual(measured, reference))
    }
    fun hasBandCalibration(key: String): Boolean = synchronized(mutex) { bandNoise.containsKey(key) }
    fun isActive(): Boolean = synchronized(mutex) { enabled && !capturing && profile != null }


    fun initialize(context: Context) = synchronized(mutex) {
        file = File(context.filesDir, "noise_floor_reference_v1.json")
        val settings = context.getSharedPreferences("noise_floor_reference", Context.MODE_PRIVATE)
        enabled = settings.getBoolean("enabled", false)
        mode = settings.getString("mode", if (enabled) "MEASURED" else "OFF")?.takeIf { it in setOf("OFF","MEASURED","MODEL") } ?: "OFF"
        if (mode == "OFF") enabled = false
        if (!enabled) mode = "OFF"
        configuredSeconds = settings.getInt("capture_seconds",10).coerceIn(10,600)
        val f = file ?: return@synchronized
        if (!f.exists()) { enabled = false; mode = "OFF"; return@synchronized }
        try {
            val o = JSONObject(f.readText())
            require(o.getInt("schema") == 1)
            val a = o.getJSONArray("power")
            require(a.length() in 2..32769)
            val values = DoubleArray(a.length()) { a.getDouble(it) }
            require(values.all { it.isFinite() && it >= 0.0 })
            profile = values
            rate = o.getInt("rate"); fft = o.getInt("fft")
            window = o.getString("window"); gain = o.getInt("gain")
            bandNoise.clear()
            o.optJSONObject("band_noise")?.let { bands ->
                val names = bands.keys()
                while (names.hasNext()) {
                    val key = names.next()
                    val value = bands.optDouble(key, Double.NaN)
                    if (value.isFinite() && value >= 0.0) bandNoise[key] = value
                }
            }
            measuredAtMs = o.optLong("measured_at_ms", 0L)
            rollingPower = o.optJSONArray("rolling_fft_1khz_1024_hann_power")?.let { spectrum ->
                if (spectrum.length() != 513) null else DoubleArray(513) { spectrum.getDouble(it) }
                    .takeIf { it.all { value -> value.isFinite() && value >= 0.0 } }
            }
            info = "Kalibration geladen"
        } catch (e: Exception) { profile = null; enabled = false; mode = "OFF"; info = "Kalibration ungueltig: ${e.message}" }
    }
    fun state(): State = synchronized(mutex) { State(enabled, profile != null, capturing,
        if (capturing) ((System.currentTimeMillis()-startedMs)*100L/targetMs).toInt().coerceIn(0,100) else 0,
        (targetMs/1000L).toInt(), info, if (!enabled) "OFF" else mode) }
    fun toggle(): String = synchronized(mutex) {
        if (capturing) return@synchronized "Einmessung laeuft"
        if (profile == null) return@synchronized "Kein Rauschprofil vorhanden – lange druecken"
        val next = when (if (enabled) mode else "OFF") {
            "OFF" -> "MEASURED"
            "MEASURED" -> "MODEL"
            else -> "OFF"
        }
        if (next == "MODEL" && (NoisePowerModel.fit(profile!!,rate,fft) == null ||
            (rollingPower != null && NoisePowerModel.fit(rollingPower!!,1000,1024) == null)))
            return@synchronized "1/f-Modell nicht verfuegbar: Messprofil pruefen"
        mode = next; enabled = next != "OFF"
        contextRef?.getSharedPreferences("noise_floor_reference",Context.MODE_PRIVATE)
            ?.edit()?.putBoolean("enabled",enabled)?.putString("mode",mode)?.apply()
        info = "NOISE $mode"
        info
    }
    private fun androidPreference(directory: File, on: Boolean) {
        // Context initialized before use; locate preferences via saved context reference.
        contextRef?.getSharedPreferences("noise_floor_reference",Context.MODE_PRIVATE)?.edit()?.putBoolean("enabled",on)?.apply()
    }
    private var contextRef: Context? = null
    fun attach(context: Context) { contextRef=context.applicationContext; initialize(context.applicationContext) }
    fun start(seconds: Int, sampleRate: Int, fftSize: Int, windowName: String, gainDb: Int): String = synchronized(mutex) {
        if (capturing) return@synchronized "Einmessung bereits aktiv"
        if (seconds !in 10..600) return@synchronized "Ungueltige Messdauer"
        rate=sampleRate; fft=fftSize;window=windowName;gain=gainDb
        spectrumAverage.reset();frames=0;bandCaptureSum.clear();bandCaptureCount.clear()
        rollingPowerSum=DoubleArray(0);rollingFrames=0;startedMs=System.currentTimeMillis()
        targetMs=seconds*1000L; capturing=true; enabled=false; mode="OFF"
        contextRef?.getSharedPreferences("noise_floor_reference",Context.MODE_PRIVATE)
            ?.edit()?.putBoolean("enabled",false)?.putString("mode","OFF")?.apply()
        info="Einmessung laeuft; Eingang sicher abschliessen"
        info
    }
    fun process(s: LiveAnalyzerCore.Spectrum, windowName: String, gainDb: Int): LiveAnalyzerCore.Spectrum = synchronized(mutex) {
        if (capturing) {
            if (s.sampleRateHz != rate || s.fftSize != fft || windowName != window || gainDb != gain) {
                capturing=false; info="Einmessung abgebrochen: Signalparameter geaendert"
            } else {
                // Always average the uncorrected instantaneous FFT spectrum in LINEAR POWER,
                // never display average, max-hold or logarithmic (dB) values.
                val linearPowers = DoubleArray(s.liveDbFs.size) { i -> power(s.liveDbFs[i]) }
                if (s.liveDbFs.all { it.isFinite() }) spectrumAverage.add(linearPowers)
                else spectrumAverage.add(doubleArrayOf(Double.NaN))
                frames = spectrumAverage.acceptedWindows.toInt()
                if (System.currentTimeMillis()-startedMs >= targetMs) {
                    val newProfile = spectrumAverage.mean()
                    if (newProfile == null) {
                        capturing = false
                        info = "Einmessung fehlgeschlagen: keine gueltigen FFT-Fenster"
                        return@synchronized s
                    }
                    val obj=JSONObject().put("schema",1).put("rate",rate).put("fft",fft)
                        .put("window",window).put("gain",gain).put("frames",frames)
                        .put("average_type","LINEAR_POWER_ARITHMETIC").put("rejected_frames",spectrumAverage.rejectedWindows)
                        .put("duration_ms",System.currentTimeMillis()-startedMs)
                        .put("measured_at_ms",System.currentTimeMillis())
                        .put("termination_type","RESISTOR_1000_OHM_USER_REPORTED")
                        .put("power",JSONArray(newProfile.toList()))
                    val savedBands = JSONObject()
                    for ((key, value) in bandCaptureSum) {
                        val n = bandCaptureCount[key] ?: 0
                        if (n >= 10) savedBands.put(key, value / n)
                    }
                    obj.put("band_noise",savedBands)
                    val newRolling = if (rollingFrames >= 10 && rollingPowerSum.size == 513)
                        DoubleArray(513) { rollingPowerSum[it] / rollingFrames } else null
                    if (newRolling != null) obj.put("rolling_fft_1khz_1024_hann_power", JSONArray(newRolling.toList()))
                    try {
                        val destination=requireNotNull(file)
                        val tmp=File(destination.parentFile,destination.name+".tmp")
                        tmp.writeText(obj.toString()); check(tmp.renameTo(destination))
                        profile=newProfile
                        rollingPower = newRolling
                        bandNoise.clear()
                        val names=savedBands.keys()
                        while (names.hasNext()) {
                            val k=names.next(); bandNoise[k]=savedBands.getDouble(k)
                        }
                        measuredAtMs=System.currentTimeMillis()
                        capturing=false;info="Kalibration gespeichert ($frames gemittelte FFT-Fenster; Rolling-FFT-Profil: ${if (newRolling != null) "JA" else "NEIN"})"
                    } catch(e:Exception) { capturing=false;info="Speichern fehlgeschlagen: ${e.message}" }
                }
            }
            return@synchronized s
        }
        val measuredReference=profile ?: return@synchronized s
        val reference=if (enabled) selectedReference(measuredReference,rate,fft) ?: return@synchronized s else measuredReference
        if (!enabled || reference.size!=s.liveDbFs.size || rate!=s.sampleRateHz ||
            fft!=s.fftSize || window!=windowName || gain!=gainDb) return@synchronized s
        fun corrected(a: DoubleArray): DoubleArray = DoubleArray(a.size) { i ->
            val measured = power(a[i])
            NoisePowerSubtraction.toDisplayDb(NoisePowerSubtraction.residual(measured, reference[i]))
        }
        s.copy(liveDbFs=corrected(s.liveDbFs),averageDbFs=corrected(s.averageDbFs),
            maxHoldDbFs=corrected(s.maxHoldDbFs))
    }
    private fun power(db:Double):Double = 10.0.pow(db.coerceIn(-180.0,100.0)/10.0)
}
