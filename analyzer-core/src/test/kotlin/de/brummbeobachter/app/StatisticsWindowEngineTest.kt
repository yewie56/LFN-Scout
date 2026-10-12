package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

class StatisticsWindowEngineTest {
    @Test fun gainPlus50CompensatedOnceOverallBandsAndSpectrum() {
        val raw = ShortArray(10_000) { i -> (60.0 * sin(2.0*PI*10.0*i/1000.0)).roundToInt().toShort() }
        val factor = 10.0.pow(50.0/20.0)
        val boosted = ShortArray(raw.size) { i -> (raw[i] * factor).roundToInt().toShort() }
        val cfg = StatisticsWindowEngine.Configuration(filterMinHz=2.0,filterMaxHz=25.0,filterBands=40)
        val reference = StatisticsWindowEngine.analyze(raw, 0.0, cfg)
        val corrected = StatisticsWindowEngine.analyze(boosted, 50.0, cfg)
        assertEquals(40, corrected.bands.size)
        assertEquals(reference.overall.leq, corrected.overall.leq, 0.2)
        assertEquals(reference.overall.l90, corrected.overall.l90, 0.2)
        for (i in 0 until 40) assertEquals(reference.bands[i].levels.leq, corrected.bands[i].levels.leq, 0.2)
        val index = corrected.frequenciesHz.indices.minByOrNull { kotlin.math.abs(corrected.frequenciesHz[it]-10.0) }!!
        assertEquals(reference.meanPsd[index], corrected.meanPsd[index], reference.meanPsd[index]*0.08)
    }

    @Test fun validationRejectsOutOfRangeAndAllowsFortyBands() {
        val config = StatisticsWindowEngine.Configuration(2.0,25.0,0.0,100.0,40)
        assertEquals(40,config.filterBands)
        assertThrows(IllegalArgumentException::class.java) {
            StatisticsWindowEngine.Configuration(2.0,25.0,0.0,100.0,41)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StatisticsWindowEngine.Configuration(25.0,2.0,0.0,100.0,10)
        }
    }

    @Test fun analyzeOverallMatchesFullResultWithGainCompensation() {
        val raw = ShortArray(12_000) { i ->
            (40.0 * sin(2.0*PI*8.0*i/1000.0) + 25.0 * sin(2.0*PI*17.0*i/1000.0)).roundToInt().toShort()
        }
        val factor = 10.0.pow(50.0/20.0)
        val boosted = ShortArray(raw.size) { i -> (raw[i] * factor).roundToInt().toShort() }
        val cfg = StatisticsWindowEngine.Configuration(2.0, 25.0, 2.0, 25.0, 10)
        val full = StatisticsWindowEngine.analyze(boosted, 50.0, cfg)
        val shared = StatisticsWindowEngine.analyzeOverall(boosted, 50.0, 2.0, 25.0)
        assertEquals(full.overall.l50, shared.l50, 1.0e-9)
        assertEquals(full.overall.l90, shared.l90, 1.0e-9)
        assertEquals(full.overall.leq, shared.leq, 1.0e-9)
    }
}
