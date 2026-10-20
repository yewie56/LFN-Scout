package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin

/**
 * Visual helper for the MON overlay curve on top of the waterfall.
 * Mirrors the effective high/low-pass limits of [AnalyzerMonitor.rebuild]
 * and adds the configured MON gain in dB.
 */
internal object MonFrequencyResponse {
    const val DISPLAY_MIN_DB = -60.0
    const val DISPLAY_MAX_DB = 120.0
    private const val BUTTERWORTH_Q = 0.70710678
    private const val STAGES = 4

    fun totalGainDbAt(
        frequencyHz: Double,
        sampleRateHz: Int,
        rangeMinHz: Double,
        rangeMaxHz: Double,
        centerHz: Double,
        bandwidthHz: Double,
        levelDb: Double
    ): Double {
        if (!frequencyHz.isFinite() || !centerHz.isFinite() || !bandwidthHz.isFinite() ||
            !levelDb.isFinite() || sampleRateHz <= 0) {
            return DISPLAY_MIN_DB
        }
        // Frequencies at and beyond Nyquist cannot be represented by this PCM input.
        if (frequencyHz >= sampleRateHz / 2.0) return DISPLAY_MIN_DB
        val response = responseShape(
            sampleRateHz = sampleRateHz,
            rangeMinHz = rangeMinHz,
            rangeMaxHz = rangeMaxHz,
            centerHz = centerHz,
            bandwidthHz = bandwidthHz
        )
        val magnitude = response.magnitudeAt(frequencyHz)
        val centerMagnitude = response.magnitudeAt(centerHz).coerceAtLeast(1e-12)
        val responseDb = 20.0 * log10((magnitude / centerMagnitude).coerceAtLeast(1e-12))
        return (levelDb + responseDb).coerceIn(DISPLAY_MIN_DB, DISPLAY_MAX_DB)
    }

    private fun responseShape(
        sampleRateHz: Int,
        rangeMinHz: Double,
        rangeMaxHz: Double,
        centerHz: Double,
        bandwidthHz: Double
    ): ResponseShape {
        val nyquist = sampleRateHz / 2.0
        val requestedBottom = (centerHz - bandwidthHz / 2.0).coerceAtLeast(rangeMinHz)
        val requestedTop = (centerHz + bandwidthHz / 2.0).coerceAtMost(rangeMaxHz)
        val safeEdge = nyquist * 0.95
        val bypassHigh = requestedBottom <= 0.0
        val bypassLow = requestedTop >= safeEdge
        val bottom = requestedBottom.coerceIn(0.5, nyquist * 0.90)
        val top = requestedTop.coerceIn(0.5, safeEdge)
        val highpass = if (bypassHigh) emptyList() else List(STAGES) {
            BiquadResponse.highpass(bottom, sampleRateHz.toDouble(), BUTTERWORTH_Q)
        }
        val lowpass = if (bypassLow) emptyList() else List(STAGES) {
            BiquadResponse.lowpass(top, sampleRateHz.toDouble(), BUTTERWORTH_Q)
        }
        return ResponseShape(highpass, lowpass)
    }

    private data class ResponseShape(
        val highpass: List<BiquadResponse>,
        val lowpass: List<BiquadResponse>
    ) {
        fun magnitudeAt(frequencyHz: Double): Double {
            val hz = frequencyHz.coerceAtLeast(0.0)
            var magnitude = 1.0
            highpass.forEach { magnitude *= it.magnitudeAt(hz) }
            lowpass.forEach { magnitude *= it.magnitudeAt(hz) }
            return magnitude
        }
    }

    private data class BiquadResponse(
        val b0: Double,
        val b1: Double,
        val b2: Double,
        val a1: Double,
        val a2: Double,
        val sampleRateHz: Double
    ) {
        fun magnitudeAt(frequencyHz: Double): Double {
            val omega = 2.0 * PI * frequencyHz / sampleRateHz
            val c1 = cos(omega)
            val s1 = sin(omega)
            val c2 = cos(2.0 * omega)
            val s2 = sin(2.0 * omega)
            val numRe = b0 + b1 * c1 + b2 * c2
            val numIm = -b1 * s1 - b2 * s2
            val denRe = 1.0 + a1 * c1 + a2 * c2
            val denIm = -a1 * s1 - a2 * s2
            val numMag = kotlin.math.sqrt(numRe * numRe + numIm * numIm)
            val denMag = kotlin.math.sqrt(denRe * denRe + denIm * denIm).coerceAtLeast(1e-12)
            return (numMag / denMag).coerceAtLeast(1e-12)
        }

        companion object {
            fun lowpass(fc: Double, fs: Double, q: Double): BiquadResponse = setup(fc, fs, q, highpass = false)
            fun highpass(fc: Double, fs: Double, q: Double): BiquadResponse = setup(fc, fs, q, highpass = true)

            private fun setup(fc: Double, fs: Double, q: Double, highpass: Boolean): BiquadResponse {
                val omega = 2.0 * PI * fc / fs
                val c = cos(omega)
                val alpha = sin(omega) / (2.0 * q)
                val a0 = 1.0 + alpha
                val b0 = (1.0 + if (highpass) c else -c) / (2.0 * a0)
                val b1 = (if (highpass) -(1.0 + c) else (1.0 - c)) / a0
                val b2 = b0
                val a1 = -2.0 * c / a0
                val a2 = (1.0 - alpha) / a0
                return BiquadResponse(b0, b1, b2, a1, a2, fs)
            }
        }
    }
}
