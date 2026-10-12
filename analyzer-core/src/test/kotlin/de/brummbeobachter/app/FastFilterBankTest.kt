package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class FastFilterBankTest {
    @Test fun configurableLinearBandsAndValidation() {
        val c=FastFilterBank.Config(10.0,60.0,20)
        assertEquals(2.5,c.widthHz,1e-8)
        assertEquals(35.0,c.low(10),1e-8)
        assertEquals(60.0,c.high(19),1e-8)
        assertThrows(IllegalArgumentException::class.java) { FastFilterBank.Config(0.0,201.0,10) }
        assertThrows(IllegalArgumentException::class.java) { FastFilterBank.Config(0.0,100.0,41) }
    }

    @Test fun thirtyFiveHzExcitesItsNominalBandWithoutSteadyFalseTransient() {
        val bank=FastFilterBank(FastFilterBank.Config(0.0,100.0,10))
        var levels:FastFilterBank.Levels?=null
        repeat(220) { chunk ->
            val samples=ShortArray(800) { i -> (11000*sin(2*PI*35.0*(chunk*800+i)/8000.0)).toInt().toShort() }
            levels=bank.accept(samples)
        }
        val result=requireNotNull(levels)
        assertEquals(3,result.absoluteDbFs.indices.maxByOrNull { result.absoluteDbFs[it] })
        assertTrue(result.absoluteDbFs.all { it.isFinite() })
        assertTrue(result.excessDb.all { it.isFinite() })
        assertTrue(result.excessDb.maxOrNull()!!<3.0)
        bank.reset()
        assertTrue(requireNotNull(bank.accept(ShortArray(800))).absoluteDbFs.all { it<=-230.0 })
    }

    @Test fun directModeFiveMsHopIsAvailable() {
        assertTrue(5 in DirectAnalyzerConfig.transformIntervalsMs)
        assertEquals(5,DirectAnalyzerConfig.hop(8000,8,5))
    }
}
