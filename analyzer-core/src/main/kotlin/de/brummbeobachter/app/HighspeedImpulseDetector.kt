package de.brummbeobachter.app

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Raw-PCM impulse detector, intentionally independent of the 1-kHz FFT/recording branch.
 * No microphone access, Android dependencies, storage, or recorder gain.
 * Threshold-based onset/widths are INDICATORS, not calibrated acoustic pressure widths.
 * The input must be actual capture samples; never upsample 1-kHz PCM and label it high-speed.
 */
class HighspeedImpulseDetector(
    val sampleRateHz: Int,
    initialMinPeakPcm: Int = 1400,
    initialThresholdOverNoise: Double = 6.0,
    private val releaseFraction: Double = 0.35,
    releaseHoldMs: Double = 2.0,
    refractoryMs: Double = 20.0
) {
    @Volatile var minPeakPcm: Int = initialMinPeakPcm
        set(value) { require(value in 1..32767); field = value }
    @Volatile var lastEffectiveThresholdPcm: Double = 0.0
        private set
    @Volatile var lastNoiseAbsPcm: Double = 32.0
        private set
    @Volatile var thresholdOverNoise: Double = initialThresholdOverNoise
        set(value) { require(value.isFinite() && value in 1.5..12.0); field = value }

    init {
        require(sampleRateHz in 4_000..192_000)
        require(minPeakPcm in 1..32767 && thresholdOverNoise >= 1.0)
        require(releaseFraction in 0.01..0.99)
        require(releaseHoldMs > 0 && refractoryMs >= 0)
    }
    data class Event(
        val onsetSampleIndex: Long,
        val onsetMonotonicNs: Long,
        val peakSampleIndex: Long,
        val peakPcm: Int,
        val peakDbFs: Double,
        val durationSamples: Long,
        val durationMs: Double,
        val energyPcmSquared: Double,
        val clippedSamples: Long,
        val sampleRateHz: Int
    )

    private val releaseHoldSamples = max(1, (releaseHoldMs * sampleRateHz / 1000.0).toInt())
    private val refractorySamples = (refractoryMs * sampleRateHz / 1000.0).toLong()
    private val baselineAlpha = 1.0 - kotlin.math.exp(-1.0 / (sampleRateHz * 0.25))
    private var noiseAbs = 32.0
    private var sequenceSample = 0L
    private var active = false
    private var onsetIndex = 0L
    private var onsetNs = 0L
    private var peakIndex = 0L
    private var peak = 0
    private var energy = 0.0
    private var clipped = 0L
    private var lastAboveIndex = 0L
    private var quiet = 0
    private var lastEventEnd = Long.MIN_VALUE

    /** Reset on source/route/rate/sequence gaps. Never connect separated PCM fragments. */
    fun reset() {
        active = false; sequenceSample = 0L; noiseAbs = 32.0; lastNoiseAbsPcm = 32.0; lastEffectiveThresholdPcm = 0.0
        quiet = 0; lastEventEnd = Long.MIN_VALUE; peak = 0; energy = 0.0; clipped = 0
    }

    /** Input start time is the approximate monotonic instant of samples[0]. */
    fun accept(samples: ShortArray, length: Int = samples.size, startMonotonicNs: Long = 0L): List<Event> {
        require(length in 0..samples.size)
        val events = ArrayList<Event>()
        for (i in 0 until length) {
            val x = samples[i].toInt()
            val magnitude = abs(x) // 32768 is safely representable as Int
            val currentIndex = sequenceSample++
            val sampleNs = startMonotonicNs + (i.toLong() * 1_000_000_000L / sampleRateHz)
            val threshold = max(minPeakPcm.toDouble(), thresholdOverNoise * noiseAbs)
            lastEffectiveThresholdPcm = threshold
            if (!active) {
                val allowed = lastEventEnd == Long.MIN_VALUE || currentIndex - lastEventEnd >= refractorySamples
                if (allowed && magnitude >= threshold) {
                    active = true
                    onsetIndex = currentIndex; onsetNs = sampleNs
                    peakIndex = currentIndex; peak = magnitude
                    lastAboveIndex = currentIndex; quiet = 0
                    energy = x.toDouble() * x; clipped = if (magnitude >= 32767) 1L else 0L
                } else {
                    // Track quiet baseline in unamplified PCM, not the recorder's gain-adjusted stream.
                    if (magnitude < threshold) noiseAbs += baselineAlpha * (magnitude - noiseAbs)
                }
                continue
            }
            energy += x.toDouble() * x
            if (magnitude >= 32767) clipped++
            if (magnitude > peak) { peak = magnitude; peakIndex = currentIndex }
            if (magnitude >= threshold * releaseFraction) {
                quiet = 0; lastAboveIndex = currentIndex
            } else {
                quiet++
                if (quiet >= releaseHoldSamples) {
                    val duration = lastAboveIndex - onsetIndex + 1L
                    events += Event(onsetIndex, onsetNs, peakIndex, peak,
                        20.0 * ln(peak.toDouble() / 32768.0) / ln(10.0),
                        duration, duration.toDouble() * 1000.0 / sampleRateHz,
                        energy, clipped, sampleRateHz)
                    active = false; lastEventEnd = currentIndex; quiet = 0
                }
            }
        }
        lastNoiseAbsPcm = noiseAbs
        return events
    }
}
