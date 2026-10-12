package de.brummbeobachter.app

import java.io.Closeable
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

/** Analyzer-only subscriber to the already available raw PCM stream.
 * Configurable 8k -> 2k/1k/500 FIR and independent analyzer gain; NEVER opens a microphone.
 * Exposes source and post-filter diagnostics to distinguish silence/rounding/UI defects.
 */
class AnalyzerRawLfSession(
    hub: AnalyzerPcmHub,
    private val onSpectrum: (LiveAnalyzerCore.Spectrum) -> Unit,
    private val onDiagnostics: (Diagnostics) -> Unit,
    private val maxFrequencyHz: Double = 200.0,
    queueCapacity: Int = 16,
    private val gainDb: Int = 0,
    private val hopSize: Int = AnalyzerTiming.DEFAULT_FFT_HOP,
    private val fftSize: Int = AnalyzerTiming.FFT_SIZE,
    private val windowType: AnalyzerWindow = AnalyzerWindow.HANN,
    private val averageTimeSeconds: Double = 5.0,
    val decimationFactor: Int = 8,
    val inputRateHz: Int = 8000,
    private val directMode: Boolean = false,
    private val smoothingAlpha: Double? = null,
    private val subtractDc: Boolean = true,
    private val filterConfig: FastFilterBank.Config? = null,
    private val onFilter: ((FastFilterBank.Levels) -> Unit)? = null
) : Closeable {
    init {
        require(gainDb in 0..50 && hopSize > 0 && fftSize in (if (directMode) DirectAnalyzerConfig.fftSizes else AnalyzerTiming.allowedFftSizes) &&
            averageTimeSeconds.isFinite() && averageTimeSeconds >= 0.0 &&
            ((inputRateHz == 8000 && decimationFactor in intArrayOf(4,8,16)) ||
             (directMode && DirectAnalyzerConfig.valid(inputRateHz, decimationFactor))))
    }
    data class Diagnostics(
        val frames: Long, val rawPeak: Int, val rawRms: Double,
        val lfRms: Double, val lfPeak: Double, val lfSamples: Long,
        val sourceRateHz: Int, val sourceLabel: String, val deviceLabel: String,
        val frameAgeNs: Long, val unsupportedRateFrames: Long,
        val nonzeroRawSamples: Int, val nonzeroLfSamples: Int
    )
    private val errors = AtomicLong()
    private val processed = AtomicLong()
    private val resets = AtomicLong()
    private val unsupported = AtomicLong()
    private val frameCount = AtomicLong()
    private var previousSeq: Long? = null
    private var oldSource: String? = null
    private val directDecimator = if (directMode && decimationFactor > 1) DirectFirDecimator(inputRateHz, decimationFactor) else null
    private var decimator = if (!directMode && decimationFactor != 1) RawLfDecimator(outputRateHz = 8000 / decimationFactor) else null
    private val analysisRate = if (directMode) DirectAnalyzerConfig.outputRate(inputRateHz, decimationFactor) else inputRateHz / decimationFactor
    private var core = LiveAnalyzerCore(analysisRate, fftSize, hopSize, maxFrequencyHz, averageTimeSeconds, windowType, smoothingAlpha, subtractDc)
    private val gain = 10.0.pow(gainDb / 20.0)
    private val filterBank = if (!directMode && filterConfig != null) FastFilterBank(filterConfig) else null
    @Volatile private var last: Diagnostics? = null
    private val subscription = hub.subscribe(queueCapacity) { frame ->
        val source = "${frame.sampleRateHz}|${frame.sourceLabel}|${frame.deviceLabel}"
        if (oldSource != source || previousSeq?.let { frame.sequence != it + 1 } == true) {
            if (oldSource != null) resets.incrementAndGet()
            decimator?.reset()
            directDecimator?.reset()
            core.reset()
            filterBank?.reset()
            oldSource = source
        }
        previousSeq = frame.sequence
        if (frame.sampleRateHz != inputRateHz) {
            unsupported.incrementAndGet()
            return@subscribe
        }
        var peak = 0
        var sum2 = 0.0
        var nonzero = 0
        for (s in frame.samples) {
            val value = abs(s.toInt())
            if (value > peak) peak = value
            if (value > 0) nonzero++
            sum2 += s.toDouble() * s
        }
        filterBank?.accept(frame.samples)?.let { levels ->
            try { onFilter?.invoke(levels) } catch (_: Exception) { errors.incrementAndGet() }
        }
        val rawRms = sqrt(sum2 / frame.samples.size) // PCM16 counts, not dB SPL
        val down = directDecimator?.accept(frame.samples) ?: decimator?.accept(frame.samples) ?: DoubleArray(frame.samples.size) { frame.samples[it] / 32768.0 }
        var lfPower = 0.0
        var lfPeak = 0.0
        var nonzeroLf = 0
        for (i in down.indices) {
            val d = down[i]
            lfPower += d * d
            lfPeak = maxOf(lfPeak, abs(d))
            if (abs(d) > 1.0e-12) nonzeroLf++
            down[i] = d * gain
        }
        val lfRms = if (down.isEmpty()) 0.0 else sqrt(lfPower / down.size) * 32768.0
        val diag = Diagnostics(frameCount.incrementAndGet(),peak,rawRms,lfRms,lfPeak * 32768.0,
            down.size.toLong(),frame.sampleRateHz,frame.sourceLabel,frame.deviceLabel,
            System.nanoTime(),unsupported.get(),nonzero,nonzeroLf)
        last = diag
        try { onDiagnostics(diag) } catch (_: Exception) { errors.incrementAndGet() }
        core.acceptNormalized(down).forEach { spectrum ->
            processed.incrementAndGet()
            try { onSpectrum(spectrum) } catch (_: Exception) { errors.incrementAndGet() }
        }
    }
    /** Reset statistical average without flushing 4096-sample FFT buffer or the WAV path. */
    fun resetAveraging() = core.resetAveraging()
    /** Reset only the held spectral maxima; leave FFT timing, averaging and the PCM stream intact. */
    fun resetMaxHold() = core.resetMaxHold()
    val diagnostics: Diagnostics? get() = last
    val droppedFrames: Long get() = subscription.droppedFrames
    val callbackErrors: Long get() = errors.get() + subscription.listenerFailures
    val discontinuityCount: Long get() = resets.get()
    val unsupportedRateFrames: Long get() = unsupported.get()
    val processedSpectra: Long get() = processed.get()
    override fun close() = subscription.close()
}
