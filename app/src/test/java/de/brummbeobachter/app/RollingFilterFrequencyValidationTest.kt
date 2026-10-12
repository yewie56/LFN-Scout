package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/** Reproducible diagnostic frequency-response characterisation, NOT calibration. */
class RollingFilterFrequencyValidationTest {
    private val sr = 1000
    private val start = 1800000L * 500

    private fun measure(frequencyHz: Double, amplitude: Double = 0.12, count: Int = 10): List<RollingFilterHalfHourStatistics.BandResult> {
        val completed = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
        val bank = RollingFilterHalfHourStatistics(sampleRate=sr, minimumHz=0, maximumHz=100,
            bandCount=count, onCompleted={ completed.add(it) })
        repeat(4000) { i ->
            val sample = (amplitude * 32767.0 * sin(2.0 * PI * frequencyHz * i / sr)).roundToInt().toShort()
            bank.accept(sample, start + i.toLong())
        }
        bank.stop()
        return completed.single()
    }

    @Test fun sinusoidalResponseIsFiniteAndOrderedByIndex() {
        for (frequency in listOf(3.0, 7.0, 15.0, 35.0, 75.0, 95.0)) {
            val bands = measure(frequency)
            assertEquals(10, bands.size)
            bands.forEachIndexed { i, b ->
                assertEquals(i + 1, b.bandIndex)
                assertEquals(i * 10.0, b.minimumHz, 1e-10)
                assertEquals((i+1) * 10.0, b.maximumHz, 1e-10)
                assertEquals(4, b.validEpochs)
                assertTrue(b.leqDbfs!!.isFinite())
                assertTrue(b.l50Dbfs!!.isFinite())
                assertTrue(b.l90Dbfs!!.isFinite())
            }
        }
    }

    @Test fun doubledAmplitudeAddsApproximatelySixDbInEachBand() {
        val a = measure(15.0, 0.06)
        val b = measure(15.0, 0.12)
        a.zip(b).forEach { (low, high) ->
            val delta = high.leqDbfs!! - low.leqDbfs!!
            assertEquals(6.0206, delta, 0.15)
        }
    }

    @Test fun zeroSignalProducesFiniteFloorAndNoDataIsNotInvented() {
        val results = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
        val bank = RollingFilterHalfHourStatistics(onCompleted={ results.add(it) })
        repeat(1000) { bank.accept(0, start+it.toLong()) }
        bank.stop()
        assertEquals(1, results.single().first().validEpochs)
        assertEquals(-200.0, results.single().first().leqDbfs!!, 0.0001)
        val incomplete = mutableListOf<List<RollingFilterHalfHourStatistics.BandResult>>()
        val shortBank = RollingFilterHalfHourStatistics(onCompleted={ incomplete.add(it) })
        repeat(100) { shortBank.accept(0, start+it.toLong()) }
        shortBank.stop()
        assertEquals(0, incomplete.single().first().validEpochs)
        assertNull(incomplete.single().first().leqDbfs)
    }

    @Test fun twentyBandModeCoversRequestedFrequencyRange() {
        val bands = measure(27.5, count=20)
        assertEquals(20, bands.size)
        assertEquals(0.0, bands.first().minimumHz, 1e-10)
        assertEquals(100.0, bands.last().maximumHz, 1e-10)
        assertTrue(bands.all { abs(it.maximumHz-it.minimumHz-5.0) < 1e-10 })
    }
}
