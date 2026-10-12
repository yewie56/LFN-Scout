package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

/** Compare implemented 1-pole HP/LP bank with its discrete-time analytic transfer.
 * This establishes reproducibility of the DSP only, NOT acoustic calibration, filter isolation,
 * or a claim that nominal band edges are -3 dB passband limits.
 */
class RollingFilterTransferFunctionTest {
    private val sr = 1000
    private val start = 1_800_000L * 510

    private fun poleMagnitude(a: Double, w: Double): Double =
        sqrt(1.0 + a*a - 2.0*a*cos(w))

    private fun expectedMagnitude(lowHz: Double, highHz: Double, freqHz: Double): Double {
        val w = 2.0 * PI * freqHz / sr
        val b = exp(-2.0 * PI * highHz / sr)
        val lp = (1.0 - b) / poleMagnitude(b, w)
        if (lowHz == 0.0) return lp
        val a = exp(-2.0 * PI * lowHz / sr)
        val hp = a * sqrt(2.0 - 2.0*cos(w)) / poleMagnitude(a, w)
        return hp * lp
    }

    private fun epochLevels(frequencyHz: Double, count: Int, minHz: Int, maxHz: Int): List<Double> {
        val epochs = mutableListOf<List<Double>>()
        val bank = RollingFilterHalfHourStatistics(sampleRate=sr, minimumHz=minHz,
            maximumHz=maxHz, bandCount=count, onEpoch={ _, levels -> epochs.add(levels) })
        // 5 complete seconds: choose the final second so transients have decayed.
        repeat(5000) { i ->
            val pcm = (0.15 * 32767.0 * sin(2.0 * PI * frequencyHz*i / sr)).roundToInt().toShort()
            bank.accept(pcm, start + i)
        }
        bank.stop()
        assertEquals(5, epochs.size)
        return epochs.last()
    }

    @Test fun measuredSinusResponseMatchesDiscreteTimeTransferForTenAndTwentyBands() {
        for (bands in listOf(10, 20)) {
            for (hz in listOf(2.0, 5.0, 10.0, 15.0, 35.0, 75.0, 95.0)) {
                val db = epochLevels(hz, bands, 0, 100)
                val width = 100.0 / bands
                db.forEachIndexed { i, actual ->
                    val gain = expectedMagnitude(width*i, width*(i+1), hz)
                    val theoretical = 20.0*log10(0.15/sqrt(2.0)*gain)
                    assertEquals("bands=$bands hz=$hz band=$i", theoretical, actual, 0.12)
                }
            }
        }
    }

    @Test fun nonZeroMinimumAndNarrowBandsMatchTheory() {
        for (bands in listOf(10, 20)) {
            val levels = epochLevels(23.0, bands, 10, 60)
            val width = 50.0 / bands
            levels.forEachIndexed { i, actual ->
                val theoretical = 20.0*log10(0.15/sqrt(2.0)*
                    expectedMagnitude(10.0+i*width, 10.0+(i+1)*width, 23.0))
                assertEquals("band=$i", theoretical, actual, 0.12)
            }
        }
    }

    @Test fun nominalBandsAreNotIndependentBrickWallFilters() {
        val levels = epochLevels(35.0, 10, 0, 100)
        // A sinus outside 0-10 Hz still passes the nominal 0-10-Hz low-pass.
        // Records the currently broad 1st-order roll-off as a diagnostic limitation.
        assertTrue(levels.first().isFinite())
        assertTrue(levels.first() > -100.0)
    }
}
