package de.brummbeobachter.app

import kotlin.math.*

/** Smooth non-negative reference model P(f)=white+K/f^alpha, fitted to a measured reference.
 * The model is a noise POWER estimate. Never use it as a frequency-response EQ.
 */
internal object NoisePowerModel {
    data class Fit(val white: Double, val k: Double, val alpha: Double,
                   val rmsLogErrorDb: Double, val binCount: Int) {
        fun power(hz: Double): Double = if (hz > 0.0) white + k / hz.pow(alpha) else Double.NaN
    }
    fun fit(powers: DoubleArray, sampleRateHz: Int, fftSize: Int,
            minHz: Double = 1.0, maxHz: Double = 100.0): Fit? {
        if (sampleRateHz <= 0 || fftSize <= 0 || powers.size < 8) return null
        val points = powers.indices.mapNotNull { i ->
            val f = i * sampleRateHz.toDouble() / fftSize
            val p = powers[i]
            if (f in minHz..maxHz && f > 0.0 && p.isFinite() && p > 0.0) f to p else null
        }
        if (points.size < 12) return null
        // Refuse a 1/f-like model for an essentially flat or rising spectrum.
        // dB power versus log10(f) must fall by at least 3 dB per decade.
        val xValues = points.map { log10(it.first) }
        val yValues = points.map { 10.0 * log10(it.second) }
        val xm = xValues.average(); val ym = yValues.average()
        val covariance = xValues.indices.sumOf { (xValues[it]-xm)*(yValues[it]-ym) }
        val variance = xValues.sumOf { (it-xm)*(it-xm) }
        if (variance <= 0.0 || covariance / variance > -3.0) return null
        val sorted = points.map { it.second }.sorted()
        val lower = sorted[sorted.size / 10]
        // Candidate positive white floors, including zero. Fit K in log-space for each (alpha,white).
        var best: Fit? = null
        for (fractionStep in 0..8) {
            val white = lower * fractionStep / 10.0
            for (aStep in 4..60) {
                val alpha = aStep * 0.05
                val valid = points.filter { it.second > white }
                if (valid.size < points.size * 0.9) continue
                val logK = valid.map { (f,p) -> ln(p - white) + alpha * ln(f) }.average()
                if (!logK.isFinite()) continue
                val k = exp(logK)
                if (!k.isFinite() || k <= 0.0) continue
                // Equal importance per logarithmic frequency decade, not every FFT bin.
                val errors = points.map { (f,p) ->
                    val predicted = white + k / f.pow(alpha)
                    val db = 10.0 * log10(predicted / p)
                    val weight = 1.0 / f
                    weight to db * db
                }
                val totalWeight = errors.sumOf { it.first }
                val rms = sqrt(errors.sumOf { it.first * it.second } / totalWeight)
                if (rms.isFinite() && (best == null || rms < best.rmsLogErrorDb))
                    best = Fit(white,k,alpha,rms,points.size)
            }
        }
        return best?.takeIf { it.rmsLogErrorDb <= 8.0 }
    }
    fun reference(profile: DoubleArray, sampleRateHz: Int, fftSize: Int): DoubleArray? {
        val model = fit(profile,sampleRateHz,fftSize) ?: return null
        return DoubleArray(profile.size) { i ->
            if (i == 0) profile[i] // DC not described by a 1/f model.
            else {
                val hz = i * sampleRateHz.toDouble() / fftSize
                if (hz in 1.0..100.0) model.power(hz) else profile[i]
            }
        }
    }
}
