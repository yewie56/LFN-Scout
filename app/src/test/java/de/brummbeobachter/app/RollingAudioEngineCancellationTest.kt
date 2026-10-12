package de.brummbeobachter.app

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RollingAudioEngineCancellationTest {

    @Before
    fun setUp() {
        RollingAudioEngine.stop()
        RollingAudioEngine.start("UNPROCESSED", "Test")
    }

    @After
    fun tearDown() {
        RollingAudioEngine.stop()
    }

    @Test
    fun cancelled_future_capture_releases_slot_for_next_recording() = runBlocking {
        val first = launch(start = CoroutineStart.UNDISPATCHED) {
            RollingAudioEngine.captureNext(seconds = 120)
        }

        first.cancelAndJoin()

        val second = async(start = CoroutineStart.UNDISPATCHED) {
            RollingAudioEngine.captureNext(seconds = 1)
        }

        repeat(RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ) {
            RollingAudioEngine.append(1234, clipped = false)
        }

        val result = second.await()
        assertNotNull("second capture must be available after cancelling the first", result)
        assertEquals(1, result!!.durationSeconds)
    }
}
