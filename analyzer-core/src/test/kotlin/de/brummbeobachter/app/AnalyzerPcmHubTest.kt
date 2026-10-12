package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class AnalyzerPcmHubTest {
    @Test fun producerNotBlockedAndDataIsCopied() {
        val hub = AnalyzerPcmHub()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val received = CountDownLatch(2)
        val observed = mutableListOf<Int>()
        val sub = hub.subscribe(2) { frame ->
            if (frame.sequence == 1L) { entered.countDown(); release.await(2, TimeUnit.SECONDS) }
            synchronized(observed) { observed.add(frame.samples[0].toInt()) }
            received.countDown()
        }
        try {
            val sample = shortArrayOf(12)
            hub.publish(sample, 1, 1000, "MIC", "test", 0)
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            sample[0] = 99
            val start = System.nanoTime()
            hub.publish(shortArrayOf(13), 1, 1000, "MIC", "test", 0)
            assertTrue("producer should not await slow callback", System.nanoTime() - start < 200_000_000L)
            release.countDown()
            assertTrue(received.await(2, TimeUnit.SECONDS))
            assertEquals(listOf(12, 13), synchronized(observed) { observed.toList() })
            assertEquals(0, sub.droppedFrames)
        } finally { release.countDown(); sub.close() }
    }

    @Test fun backpressureDropsOldestWithoutStoppingProducer() {
        val hub = AnalyzerPcmHub()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val observed = CountDownLatch(2)
        val sub = hub.subscribe(1) {
            if (it.sequence == 1L) { entered.countDown(); release.await(2, TimeUnit.SECONDS) }
            observed.countDown()
        }
        try {
            hub.publish(shortArrayOf(1), 1, 1000, "MIC", "test", 0)
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            for (i in 2..20) hub.publish(shortArrayOf(i.toShort()), 1, 1000, "MIC", "test", 0)
            assertTrue(sub.droppedFrames >= 18)
            release.countDown()
            assertTrue(observed.await(2, TimeUnit.SECONDS))
            assertEquals(0, sub.listenerFailures)
        } finally { release.countDown(); sub.close() }
    }

    @Test fun separateSubscribersAndFailureIsolation() {
        val hub = AnalyzerPcmHub()
        val seen = CountDownLatch(1)
        val a = hub.subscribe { throw IllegalStateException("test exception") }
        val b = hub.subscribe { seen.countDown() }
        try {
            hub.publish(shortArrayOf(1), 1, 1000, "MIC", "test", 0)
            assertTrue(seen.await(2, TimeUnit.SECONDS))
            // Both consumers accepted the frame, regardless of listener exception.
            assertEquals(2, hub.activeSubscribers)
        } finally { a.close(); b.close() }
        assertEquals(0, hub.activeSubscribers)
    }

    @Test fun sessionEmitsSpectraAndResetsOnSourceChange() {
        val hub = AnalyzerPcmHub()
        val seen = CountDownLatch(2)
        val count = AtomicInteger()
        val session = AnalyzerLiveSession(hub, { count.incrementAndGet(); seen.countDown() }, 1024, 256)
        try {
            val sample = ShortArray(256) { i -> (13000 * sin(2 * PI * 10 * i / 1000)).roundToInt().toShort() }
            repeat(8) { hub.publish(sample, sample.size, 1000, "MIC", "test", 0) }
            assertTrue(seen.await(2, TimeUnit.SECONDS))
            assertTrue(count.get() >= 2)
            hub.publish(sample, sample.size, 1000, "UNPROCESSED", "test", 0)
            // A new full FFT window must be collected before emitting spectra again.
            Thread.sleep(150)
            assertTrue(session.discontinuityCount >= 1)
        } finally { session.close() }
    }
}
