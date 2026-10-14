package de.brummbeobachter.app

import java.io.Closeable
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt

/** Dedicated bounded 1-kHz PRE-GAIN copy of existing raw hub PCM. No microphone ownership.
 * A discontinuity, timestamp gap, source/rate change invalidates the window. PAUSE freezes
 * exactly one immutable snapshot; resume invalidates it and resumes collecting fresh audio.
 */
internal object LocalAnalyzerAudioBuffer {
    data class Clip(val pcm: ShortArray, val startEpochMs: Long, val endEpochMs: Long,
                    val source: String, val device: String, val inputRateHz: Int,
                    val clippedSamples: Int, val fromPause: Boolean)
    private const val RATE = 1000
    private val lock = Any()
    private var subscription: Closeable? = null
    private val ring = ShortArray(LocalAudioSaveSettings.MAX_SECONDS * RATE)
    private val clipping = BooleanArray(ring.size)
    private var pos = 0
    private var count = 0
    private var previousSeq: Long? = null
    private var previousEndNs: Long? = null
    private var inputRate = 0
    private var source = ""
    private var device = ""
    private var frozen: Clip? = null
    private var frozenConsumed = false
    private var fractional = 0
    private val lowpass = DoubleArray(6)
    private var clippedCount = 0
    private var pendingInputClip = false
    private var isActive = false
    fun start() = synchronized(lock) {
        stopInternal()
        isActive = true
        subscription = AnalyzerSessionGateway.rawPcm.subscribe(queueCapacity = 64) { frame -> accept(frame) }
    }
    fun stop() = synchronized(lock) { stopInternal() }
    /** Local subscriber state only; never a microphone/recorder ownership assertion. */
    fun hasActiveSubscription(): Boolean = synchronized(lock) { isActive || subscription != null }
    private fun stopInternal() {
        isActive = false
        subscription?.close(); subscription = null
        clearInternal()
        frozen = null; frozenConsumed = false
    }
    private fun clearInternal() {
        pos = 0; count = 0; clippedCount = 0
        previousSeq = null; previousEndNs = null
        inputRate = 0; source = ""; device = ""
        fractional = 0; lowpass.fill(0.0); pendingInputClip = false
    }
    private fun accept(frame: AnalyzerPcmHub.Frame): Unit {
        synchronized(lock) {
            if (!isActive || frame.sampleRateHz < RATE) return@synchronized
            val endNs = frame.captureMonotonicNs + frame.samples.size.toLong() * 1_000_000_000L / frame.sampleRateHz
            val gapNs = previousEndNs?.let { frame.captureMonotonicNs - it }
            val broken = (previousSeq != null && frame.sequence != previousSeq!! + 1) ||
                (inputRate != 0 && inputRate != frame.sampleRateHz) ||
                (source.isNotEmpty() && (source != frame.sourceLabel || device != frame.deviceLabel)) ||
                (gapNs != null && (gapNs > 150_000_000L || gapNs < -150_000_000L))
            if (broken) {
                clearInternal()
                // Previously frozen PAUSE snapshot is immutable even if capture source later breaks.
            }
            inputRate = frame.sampleRateHz
            source = frame.sourceLabel; device = frame.deviceLabel
            previousSeq = frame.sequence; previousEndNs = endNs
            val alpha = 1.0 - exp(-2.0 * PI * 160.0 / inputRate)
            for (s in frame.samples) {
                if (s == Short.MIN_VALUE || s == Short.MAX_VALUE) pendingInputClip = true
                var sample = s.toDouble()
                for (stage in lowpass.indices) {
                    lowpass[stage] += alpha * (sample - lowpass[stage])
                    sample = lowpass[stage]
                }
                fractional += RATE
                if (fractional >= inputRate) {
                    fractional -= inputRate
                    val x = sample.roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    if (count == ring.size && clipping[pos]) clippedCount--
                    ring[pos] = x
                    clipping[pos] = pendingInputClip
                    pendingInputClip = false
                    if (clipping[pos]) clippedCount++
                    pos = (pos + 1) % ring.size
                    if (count < ring.size) count++
                }
            }
        }
    }
    fun bufferedSeconds(): Int = synchronized(lock) { count / RATE }
    fun canSave(seconds: Int, paused: Boolean): Boolean = synchronized(lock) {
        seconds in LocalAudioSaveSettings.MIN_SECONDS..LocalAudioSaveSettings.MAX_SECONDS &&
            if (paused) frozen != null && !frozenConsumed && frozen!!.pcm.size == seconds * RATE
            else isActive && count >= seconds * RATE && previousEndNs?.let {
                System.nanoTime() - it < 1_000_000_000L
            } == true
    }
    private fun extract(seconds: Int, pause: Boolean): Clip? {
        if (!isActive || seconds !in LocalAudioSaveSettings.MIN_SECONDS..LocalAudioSaveSettings.MAX_SECONDS || count < seconds * RATE ||
            previousEndNs?.let { System.nanoTime() - it >= 1_000_000_000L } != false) return null
        val n = seconds * RATE
        val samples = ShortArray(n)
        var clips = 0
        val start = (pos - n + ring.size) % ring.size
        for (i in 0 until n) {
            val at = (start + i) % ring.size
            samples[i] = ring[at]
            if (clipping[at]) clips++
        }
        val latencyMs = previousEndNs?.let { ((System.nanoTime() - it) / 1_000_000L).coerceAtLeast(0L) } ?: 0L
        val end = System.currentTimeMillis() - latencyMs
        return Clip(samples, end - seconds * 1000L, end, source, device, inputRate, clips, pause)
    }
    fun freeze(seconds: Int) = synchronized(lock) {
        frozen = extract(seconds, true)
        frozenConsumed = false
    }
    fun resume() = synchronized(lock) { frozen = null; frozenConsumed = false }
    fun restoreFrozenOnFailure() = synchronized(lock) { if (frozen != null) frozenConsumed = false }
    fun take(seconds: Int, paused: Boolean): Clip? = synchronized(lock) {
        if (paused) {
            frozen?.takeIf { !frozenConsumed && it.pcm.size == seconds * RATE }?.also { frozenConsumed = true }
        } else extract(seconds, false)
    }
}
