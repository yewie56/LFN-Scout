package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Experimental analyzer-only causal filterbank. Consumes SHARED PCM16; never opens AudioRecord.
 * Each output is the PEAK 5-ms block RMS dBFS observed since the last accept() call.
 * A distinct baseline yields relative transient excess (not an absolute SPL reading).
 * Band shape is a 2nd-order constant-skirt-gain bandpass; widths are nominal, not brick-wall edges.
 */
class FastFilterBank(val config: Config, private val sampleRateHz: Int = 8000) {
    data class Config(val minHz: Double = 0.0, val maxHz: Double = 100.0, val bands: Int = 10) {
        init { require(minHz.isFinite() && maxHz.isFinite() && minHz >= 0.0 &&
            maxHz <= 200.0 && maxHz > minHz && bands in 2..40) }
        val widthHz: Double get() = (maxHz-minHz)/bands
        fun low(index: Int): Double = minHz + widthHz*index
        fun high(index: Int): Double = minHz + widthHz*(index+1)
    }
    data class Levels(val absoluteDbFs: DoubleArray, val excessDb: DoubleArray)
    private class Biquad(low: Double, high: Double, rate: Int) {
        private val center = max(0.5, (low+high)*0.5)
        private val width = max(0.2, high-low)
        private val w = 2.0*PI*center/rate
        private val a = sin(w)/(2.0*max(0.05,center/width))
        private val a0 = 1.0+a
        private val b0 = a/a0
        private val b2 = -a/a0
        private val a1 = -2.0*cos(w)/a0
        private val a2 = (1.0-a)/a0
        private var z1 = 0.0
        private var z2 = 0.0
        fun take(x: Double): Double {
            val out = b0*x + z1
            z1 = z2 - a1*out
            z2 = b2*x - a2*out
            return out
        }
        fun reset() { z1=0.0; z2=0.0 }
    }
    private val filters = Array(config.bands) { i -> Biquad(config.low(i),config.high(i),sampleRateHz) }
    private val sum = DoubleArray(config.bands)
    private val baseline = DoubleArray(config.bands)
    private val envelope = DoubleArray(config.bands)
    private val samplesPerStep = max(1,sampleRateHz/200) // 5 ms at 8 kHz
    private val fastAlpha = DoubleArray(config.bands) { i ->
        val center=max(0.5,(config.low(i)+config.high(i))*0.5)
        val tau=max(0.05,1.0/(2.0*center))
        1.0-exp(-samplesPerStep.toDouble()/sampleRateHz/tau)
    }
    private var pending = 0
    private var elapsed = 0L
    private val baselineAlpha = 1.0-exp(-samplesPerStep.toDouble()/sampleRateHz/5.0)
    init { require(sampleRateHz==8000) { "Filterbank benötigt bestehenden 8-kHz-Rohpfad" } }
    fun reset() {
        filters.forEach { it.reset() }; sum.fill(0.0); baseline.fill(0.0); envelope.fill(0.0)
        pending=0; elapsed=0L
    }
    fun accept(samples: ShortArray): Levels? {
        val maximum = DoubleArray(config.bands) { -240.0 }
        val excess = DoubleArray(config.bands)
        var updated=false
        for (pcm in samples) {
            val x=pcm.toDouble()/32768.0
            for(i in filters.indices) { val y=filters[i].take(x); sum[i]+=y*y }
            pending++
            if(pending>=samplesPerStep) {
                val n=pending.toDouble()
                for(i in filters.indices) {
                    val instantaneous=sum[i]/n
                    envelope[i]+=fastAlpha[i]*(instantaneous-envelope[i])
                    val power=envelope[i]
                    // RMS power is relative to digital full scale, never dB SPL.
                    val db=10.0*log10(max(power,1.0e-24))
                    maximum[i]=max(maximum[i],db)
                    if(elapsed>sampleRateHz.toLong()*10L && baseline[i]>1.0e-24) {
                        excess[i]=max(excess[i],10.0*log10(max(power,1.0e-24)/baseline[i]).coerceAtLeast(0.0))
                    }
                    // Only use a bounded rise of the reference while the band is transient.
                    if(elapsed<=sampleRateHz.toLong()*10L || power<=baseline[i]*2.0 || baseline[i]<=0.0)
                        baseline[i]+=baselineAlpha*(power-baseline[i])
                    sum[i]=0.0
                }
                elapsed+=pending; pending=0; updated=true
            }
        }
        return if(updated) Levels(maximum,excess) else null
    }
}
