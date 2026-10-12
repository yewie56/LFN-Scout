package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class NoisePowerModelTest {
    private val fs = 1000
    private val n = 4096
    private fun synthetic(alpha: Double = 1.97) = DoubleArray(n/2+1) { k ->
        val hz = k.toDouble()*fs/n
        if (hz == 0.0) 0.001 else 1e-8 + 0.0001 / hz.pow(alpha)
    }
    @Test fun fittedNoiseSlopeCloseToReference() {
        val f=NoisePowerModel.fit(synthetic(),fs,n)
        assertNotNull(f)
        assertEquals(1.97,f!!.alpha,0.25)
    }
    @Test fun preservesDcAndOutOfBandMeasuredReference() {
        val source=synthetic()
        val result=NoisePowerModel.reference(source,fs,n)!!
        assertEquals(source[0],result[0],0.0)
        val over100=(110.0*n/fs).toInt()
        assertEquals(source[over100],result[over100],0.0)
        val under1=1
        assertEquals(source[under1],result[under1],0.0)
        assertTrue(result[(10.0*n/fs).toInt()] > 0.0)
    }
    @Test fun refusesFlatWhiteNoiseAsOneOverF() {
        assertNull(NoisePowerModel.fit(DoubleArray(n/2+1){1e-5},fs,n))
    }
    @Test fun invalidInputsRefused() {
        assertNull(NoisePowerModel.fit(doubleArrayOf(0.0,1.0),fs,n))
    }
}
