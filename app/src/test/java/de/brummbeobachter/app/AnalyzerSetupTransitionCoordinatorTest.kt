package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AnalyzerSetupTransitionCoordinatorTest {
    @Test fun setupReservationBlocksConcurrentMonStart() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val t = Thread {
            AnalyzerSetupTransitionCoordinator.withSetupReservation {
                entered.countDown()
                release.await(3, TimeUnit.SECONDS)
            }
        }
        t.start()
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS))
            val called = AtomicBoolean(false)
            assertFalse(AnalyzerSetupTransitionCoordinator.withMonStart { called.set(true); true })
            assertFalse(called.get())
        } finally { release.countDown(); t.join(3000) }
        assertTrue(AnalyzerSetupTransitionCoordinator.withMonStart { true })
    }

    @Test fun failingReservationReleasesGate() {
        try {
            AnalyzerSetupTransitionCoordinator.withSetupReservation<Unit> { throw IllegalStateException("test") }
            fail("exception expected")
        } catch (_: IllegalStateException) { }
        assertTrue(AnalyzerSetupTransitionCoordinator.withMonStart { true })
    }

    @Test fun secondSetupReservationRejected() {
        val result = AnalyzerSetupTransitionCoordinator.withSetupReservation {
            AnalyzerSetupTransitionCoordinator.withSetupReservation { "not allowed" }
        }
        assertNull(result)
    }
}
