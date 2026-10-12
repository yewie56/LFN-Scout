package de.brummbeobachter.app

import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.util.BitSet

object RollingAudioEngine {
    data class Snapshot(
        val pcm16: ByteArray,
        val startEpochMs: Long,
        val endEpochMs: Long,
        val sourceLabel: String,
        val deviceLabel: String,
        val clippedSamples: Int
    ) {
        val durationSeconds: Int
            get() = pcm16.size / 2 / RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ
    }

    private data class FutureCapture(
        val targetSamples: Int,
        val pcm: ByteArrayOutputStream,
        var clippedSamples: Int = 0,
        var completed: Boolean = false
    )

    private val lock = Any()
    private val ring = ShortArray(RollingAudioPolicy.CAPACITY_SAMPLES)
    private val clippedRing = BitSet(RollingAudioPolicy.CAPACITY_SAMPLES)
    private var writeIndex = 0
    private var size = 0
    private var totalClipped = 0
    private var capture: FutureCapture? = null

    @Volatile var isRunning: Boolean = false
        private set
    @Volatile var sourceLabel: String = ""
        private set
    @Volatile var deviceLabel: String = ""
        private set

    fun start(source: String, device: String) {
        synchronized(lock) {
            writeIndex = 0
            size = 0
            totalClipped = 0
            clippedRing.clear()
            capture = null
            sourceLabel = source
            deviceLabel = device
            isRunning = true
        }
    }

    fun updateInput(source: String, device: String) {
        sourceLabel = source
        deviceLabel = device
    }

    fun stop() {
        synchronized(lock) {
            isRunning = false
            capture?.completed = true
            capture = null
        }
    }

    fun append(sample: Short, clipped: Boolean) {
        synchronized(lock) {
            if (!isRunning) return
            if (size == ring.size && clippedRing.get(writeIndex)) {
                totalClipped--
            }
            ring[writeIndex] = sample
            clippedRing.set(writeIndex, clipped)
            writeIndex = (writeIndex + 1) % ring.size
            if (size < ring.size) size++
            if (clipped) totalClipped++

            val active = capture
            if (active != null && !active.completed) {
                active.pcm.write(sample.toInt() and 0xFF)
                active.pcm.write((sample.toInt() shr 8) and 0xFF)
                if (clipped) active.clippedSamples++
                if (active.pcm.size() / 2 >= active.targetSamples) {
                    active.completed = true
                }
            }
        }
    }

    fun bufferedSeconds(): Int = synchronized(lock) {
        size / RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ
    }

    fun snapshot(nowEpochMs: Long = System.currentTimeMillis()): Snapshot? = synchronized(lock) {
        if (!isRunning || size <= 0) return@synchronized null
        val out = ByteArray(size * 2)
        val start = if (size == ring.size) writeIndex else 0
        for (i in 0 until size) {
            val sample = ring[(start + i) % ring.size].toInt()
            out[i * 2] = (sample and 0xFF).toByte()
            out[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        val durationMs = size * 1000L / RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ
        Snapshot(
            pcm16 = out,
            startEpochMs = nowEpochMs - durationMs,
            endEpochMs = nowEpochMs,
            sourceLabel = sourceLabel,
            deviceLabel = deviceLabel,
            clippedSamples = totalClipped
        )
    }

    suspend fun captureNext(
        seconds: Int,
        onProgressSeconds: suspend (Int) -> Unit = {},
        onClippingChanged: suspend (Int) -> Unit = {}
    ): Snapshot? {
        if (!isRunning || seconds <= 0) return null
        val started = System.currentTimeMillis()
        val localCapture = synchronized(lock) {
            if (capture != null) return null
            FutureCapture(
                targetSamples = seconds * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ,
                pcm = ByteArrayOutputStream(seconds * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ * 2)
            ).also { capture = it }
        }

        try {
            var lastProgress = -1
            var lastClip = -1
            while (isRunning) {
                val state = synchronized(lock) {
                    Triple(localCapture.completed, localCapture.pcm.size() / 2, localCapture.clippedSamples)
                }
                val progress = (state.second / RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ).coerceAtMost(seconds)
                if (progress != lastProgress) {
                    lastProgress = progress
                    onProgressSeconds(progress)
                }
                if (state.third != lastClip) {
                    lastClip = state.third
                    onClippingChanged(state.third)
                }
                if (state.first) break
                delay(100)
            }

            val bytes: ByteArray
            val clips: Int
            synchronized(lock) {
                bytes = localCapture.pcm.toByteArray()
                clips = localCapture.clippedSamples
            }
            if (bytes.isEmpty()) return null
            val end = System.currentTimeMillis()
            return Snapshot(bytes, started, end, sourceLabel, deviceLabel, clips)
        } finally {
            synchronized(lock) {
                if (capture === localCapture) {
                    capture = null
                }
            }
        }
    }
}
