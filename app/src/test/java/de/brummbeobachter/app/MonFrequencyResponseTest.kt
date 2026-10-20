package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Test

class MonFrequencyResponseTest {
    @Test
    fun centerPassbandTracksConfiguredMonLevel() {
        val gain = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 50.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 50.0,
            bandwidthHz = 20.0,
            levelDb = 30.0
        )
        assertTrue("center gain should stay close to MON level, actual=$gain", gain > 25.0 && gain <= 30.1)
    }

    @Test
    fun offBandIsClearlyLowerThanPassband() {
        val center = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 50.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 50.0,
            bandwidthHz = 20.0,
            levelDb = 10.0
        )
        val lowOff = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 5.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 50.0,
            bandwidthHz = 20.0,
            levelDb = 10.0
        )
        val highOff = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 150.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 50.0,
            bandwidthHz = 20.0,
            levelDb = 10.0
        )
        assertTrue("low off-band must be lower than passband", lowOff < center - 20.0)
        assertTrue("high off-band must be lower than passband", highOff < center - 20.0)
    }

    @Test
    fun lowEdgeSqueezeRemainsFiniteAndVisible() {
        val gainNearCenter = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 4.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 4.0,
            bandwidthHz = 8.0,
            levelDb = -12.0
        )
        val gainFarAway = MonFrequencyResponse.totalGainDbAt(
            frequencyHz = 80.0,
            sampleRateHz = 8000,
            rangeMinHz = 0.0,
            rangeMaxHz = 200.0,
            centerHz = 4.0,
            bandwidthHz = 8.0,
            levelDb = -12.0
        )
        assertTrue("squeezed edge response should remain finite", gainNearCenter.isFinite())
        assertTrue("squeezed edge response should still emphasize the selected band", gainNearCenter > gainFarAway)
    }
}
