package de.brummbeobachter.app

import java.io.Closeable
import java.util.concurrent.atomic.AtomicLong

/** Optional session independent of Activity; no Android dependencies. */
class AnalyzerLiveSession(
    hub: AnalyzerPcmHub,
    private val onSpectrum: (LiveAnalyzerCore.Spectrum) -> Unit,
    private val fftSize: Int = 4096,
    private val hopSize: Int = 512,
    private val maxFrequencyHz: Double = 100.0,
    queueCapacity: Int = 8
) : Closeable {
    private var core: LiveAnalyzerCore? = null
    private var previousSequence: Long? = null
    private var config: Triple<Int, String, Int>? = null
    private val discontinuities = AtomicLong()
    private val processed = AtomicLong()
    private val callbackErrors = AtomicLong()
    private val subscription = hub.subscribe(queueCapacity) { frame ->
        val currentConfig = Triple(frame.sampleRateHz, frame.sourceLabel + "|" + frame.deviceLabel, frame.gainDb)
        if (frame.sampleRateHz < (maxFrequencyHz * 2.0)) return@subscribe
        if (config != currentConfig || previousSequence?.let { frame.sequence != it + 1 } == true) {
            if (config != null) discontinuities.incrementAndGet()
            core = LiveAnalyzerCore(frame.sampleRateHz, fftSize, hopSize, maxFrequencyHz)
            config = currentConfig
        }
        previousSequence = frame.sequence
        core?.accept(frame.samples)?.forEach {
            processed.incrementAndGet()
            try { onSpectrum(it) } catch (_: Exception) { callbackErrors.incrementAndGet() }
        }
    }

    val droppedFrames: Long get() = subscription.droppedFrames
    val discontinuityCount: Long get() = discontinuities.get()
    val processedSpectra: Long get() = processed.get()
    val spectrumCallbackErrors: Long get() = callbackErrors.get()
    override fun close() = subscription.close()
}
