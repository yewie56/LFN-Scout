package de.brummbeobachter.app

/** Input health is determined from source freshness, never from compensated spectral level.
 * An all-zero spectrum is a valid measurement if live PCM data are still arriving.
 * Source timeout and capture errors are handled independently by the controller.
 */
internal object AnalyzerSignalValidity {
    enum class Result { NO_INPUT, BELOW_NOISE_FLOOR, VALID_SIGNAL }

    fun classify(originalDbFs: DoubleArray, residualDbFs: DoubleArray, correctionEnabled: Boolean): Result {
        // Empty/malformed FFT buffers are invalid; finite -180 dBFS bins are NOT missing PCM.
        if (originalDbFs.isEmpty() || originalDbFs.any { !it.isFinite() }) return Result.NO_INPUT
        val residualAboveFloor = residualDbFs.any { it.isFinite() && it > -180.0 }
        return if (correctionEnabled && !residualAboveFloor) Result.BELOW_NOISE_FLOOR
               else Result.VALID_SIGNAL
    }
}
