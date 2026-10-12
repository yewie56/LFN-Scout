package de.brummbeobachter.app

import java.io.Closeable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Non-blocking, opt-in PCM fan-out. Published PCM is a defensive copy. A slow listener
 * loses its oldest frame; the sequence gap lets consumers reset continuity.
 * The producer never calls listener code on an AudioRecord thread.
 * This class owns neither a microphone nor a persistence/recording pathway.
 */
class AnalyzerPcmHub {
    data class Frame(
        val sequence: Long,
        val samples: ShortArray,
        val sampleRateHz: Int,
        val sourceLabel: String,
        val deviceLabel: String,
        val gainDb: Int,
        val captureMonotonicNs: Long
    )

    class Subscription internal constructor(
        private val parent: AnalyzerPcmHub,
        private val callback: (Frame) -> Unit,
        queueCapacity: Int
    ) : Closeable {
        private val queue = ArrayBlockingQueue<Frame>(queueCapacity)
        private val dropped = AtomicLong(0)
        private val failures = AtomicLong(0)
        @Volatile private var open = true
        private val worker = Thread({
            while (open || queue.isNotEmpty()) {
                val item = try {
                    queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                } catch (_: InterruptedException) { break } ?: continue
                if (!open) break
                try { callback(item) } catch (_: Exception) { failures.incrementAndGet() }
            }
        }, "LFN-Analyzer-PCM").apply { isDaemon = true; start() }

        val droppedFrames: Long get() = dropped.get()
        val listenerFailures: Long get() = failures.get()

        internal fun offer(frame: Frame) {
            if (!open) return
            if (!queue.offer(frame)) {
                queue.poll()
                dropped.incrementAndGet()
                if (!queue.offer(frame)) dropped.incrementAndGet()
            }
        }

        override fun close() {
            if (!open) return
            open = false
            parent.unregister(this)
            queue.clear()
            worker.interrupt()
        }
    }

    private val lock = Any()
    private val sequence = AtomicLong()
    private val subscribers = LinkedHashSet<Subscription>()
    val activeSubscribers: Int get() = synchronized(lock) { subscribers.size }

    fun subscribe(queueCapacity: Int = 8, onFrame: (Frame) -> Unit): Subscription {
        require(queueCapacity in 1..128)
        return Subscription(this, onFrame, queueCapacity).also { synchronized(lock) { subscribers.add(it) } }
    }

    private fun unregister(subscription: Subscription) { synchronized(lock) { subscribers.remove(subscription) } }

    /** Separate instances serve either raw pre-gain PCM or legacy 1-kHz post-gain PCM.
     * captureMonotonicNs refers to the producer-provided frame timebase; raw producers
     * estimate first-sample time, while legacy 1-kHz producers use publication time.
     */
    fun markDiscontinuity() { sequence.incrementAndGet() }

    fun publish(
        input: ShortArray,
        length: Int,
        sampleRateHz: Int,
        sourceLabel: String,
        deviceLabel: String,
        gainDb: Int,
        captureMonotonicNs: Long = System.nanoTime()
    ) {
        require(length >= 0 && length <= input.size && sampleRateHz > 0)
        if (length == 0) return
        synchronized(lock) {
            if (subscribers.isEmpty()) return
            val frameSequence = sequence.incrementAndGet()
            // No callbacks or blocking queue operations while holding this lock.
            // Each listener gets a separate copy, so one cannot mutate another's samples.
            subscribers.forEach { listener ->
                listener.offer(Frame(frameSequence, input.copyOf(length), sampleRateHz,
                    sourceLabel, deviceLabel, gainDb, captureMonotonicNs))
            }
        }
    }
}
