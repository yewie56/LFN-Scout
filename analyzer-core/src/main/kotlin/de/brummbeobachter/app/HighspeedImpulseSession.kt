package de.brummbeobachter.app

import java.io.Closeable
import java.util.concurrent.atomic.AtomicLong

/** Async high-speed consumer of pre-decimation, pre-gain microphone PCM. */
class HighspeedImpulseSession(
    hub: AnalyzerPcmHub,
    private val onEvent: (HighspeedImpulseDetector.Event) -> Unit,
    private val minPeakPcm: Int = 1400
) : Closeable {
    /** One atomic, immutable configuration per PCM frame; shared capture/WAV untouched. */
    @Volatile var sensitivity: ImpulseSensitivity = ImpulseSensitivity()
    var analysisGainDb: Int
        get() = sensitivity.gainDb
        set(value) { sensitivity = sensitivity.copy(gainDb = value) }
    @Volatile var effectiveThresholdRawPcm: Double = 0.0
        private set
    @Volatile var measuredGainDb: Int = 0
        private set
    @Volatile var measuredNoisePcm: Double = 0.0
        private set
    @Volatile var adaptiveThresholdRawPcm: Double = 0.0
        private set
    @Volatile var fixedThresholdRawPcm: Int = 1400
        private set
    private var detector: HighspeedImpulseDetector? = null
    private var previousSequence = 0L
    private var previousSource = ""
    private var previousRate = 0
    private val resets = AtomicLong()
    private val events = AtomicLong()
    val discontinuityCount: Long get() = resets.get()
    val detectedEvents: Long get() = events.get()
    private val subscription = hub.subscribe(queueCapacity = 8) { frame ->
        val source = frame.sourceLabel + "|" + frame.deviceLabel
        val discontinuity = previousSequence != 0L && frame.sequence != previousSequence + 1L
        if (detector == null || discontinuity || source != previousSource || frame.sampleRateHz != previousRate) {
            if (detector != null) resets.incrementAndGet()
            detector = if (frame.sampleRateHz in 4_000..192_000) {
                HighspeedImpulseDetector(frame.sampleRateHz, minPeakPcm)
            } else null
        }
        previousSequence = frame.sequence; previousSource = source; previousRate = frame.sampleRateHz
        val settings = sensitivity // immutable snapshot of all three user controls
        val minimumRaw = settings.fixedThresholdRawPcm()
        detector?.minPeakPcm = minimumRaw
        detector?.thresholdOverNoise = settings.noiseMultiplier
        measuredGainDb = settings.gainDb
        fixedThresholdRawPcm = minimumRaw
        detector?.accept(frame.samples, startMonotonicNs = frame.captureMonotonicNs)?.also {
            measuredNoisePcm = detector?.lastNoiseAbsPcm ?: 0.0
            adaptiveThresholdRawPcm = settings.noiseMultiplier * measuredNoisePcm
            effectiveThresholdRawPcm = detector?.lastEffectiveThresholdPcm ?: 0.0
        }?.forEach {
            events.incrementAndGet()
            onEvent(it)
        }
    }
    val droppedFrames: Long get() = subscription.droppedFrames
    val callbackFailures: Long get() = subscription.listenerFailures
    override fun close() = subscription.close()
}
