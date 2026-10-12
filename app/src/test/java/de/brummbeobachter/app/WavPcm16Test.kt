package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class WavPcm16Test {

    @Test
    fun header_is_standard_pcm16_mono_1000_hz() {
        val header = WavPcm16.buildHeader(
            sampleRateHz = 1000,
            channels = 1,
            bitsPerSample = 16,
            dataBytes = 240000
        )
        assertEquals(44, header.size)
        assertEquals("RIFF", header.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", header.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("fmt ", header.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("data", header.copyOfRange(36, 40).toString(Charsets.US_ASCII))
    }

    @Test
    fun thirty_db_is_about_31_point_62_times_amplitude() {
        val factor = AudioRecordingPolicy.gainFactor(30)
        assertTrue(abs(factor - 31.622776) < 0.001)
    }
}
