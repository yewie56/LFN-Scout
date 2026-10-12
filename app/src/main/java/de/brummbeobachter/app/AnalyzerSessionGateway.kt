package de.brummbeobachter.app

/** Shared in-process PCM contract for rolling and direct recorder paths.
 * The standalone host supplies its own producer to an AnalyzerPcmHub, using the same core.
 * There is NO automatic session start in Phase 2 and no second AudioRecord here.
 */
object AnalyzerSessionGateway {
    val pcm = AnalyzerPcmHub() // Legacy 1-kHz post-gain FFT (not high-speed).
    /** Real source-rate PCM, untouched by recorder gain or decimation. */
    val rawPcm = AnalyzerPcmHub()
    fun startRawLf(onSpectrum: (LiveAnalyzerCore.Spectrum) -> Unit,
        onDiagnostics: (AnalyzerRawLfSession.Diagnostics) -> Unit,
        gainDb: Int = 0,
        hopSamples: Int = AnalyzerTiming.DEFAULT_FFT_HOP,
        fftSize: Int = AnalyzerTiming.FFT_SIZE,
        windowType: AnalyzerWindow = AnalyzerWindow.HANN,
        averageTimeSeconds: Double = 5.0,
        decimationFactor: Int = 8,
        inputRateHz: Int = 8000,
        maxFrequencyHz: Double = 200.0,
        directMode: Boolean = false, smoothingAlpha: Double? = null, subtractDc: Boolean = true,
        filterConfig: FastFilterBank.Config? = null, onFilter: ((FastFilterBank.Levels) -> Unit)? = null): AnalyzerRawLfSession =
        AnalyzerRawLfSession(rawPcm, onSpectrum, onDiagnostics, maxFrequencyHz, gainDb = gainDb,
            hopSize = hopSamples, fftSize = fftSize, windowType = windowType,
            averageTimeSeconds = averageTimeSeconds, decimationFactor = decimationFactor, inputRateHz = inputRateHz,
            directMode = directMode, smoothingAlpha = smoothingAlpha, subtractDc = subtractDc,
            filterConfig = filterConfig, onFilter = onFilter)
    fun startImpulseDetector(onEvent: (HighspeedImpulseDetector.Event) -> Unit): HighspeedImpulseSession =
        HighspeedImpulseSession(rawPcm, onEvent)
    fun start(onSpectrum: (LiveAnalyzerCore.Spectrum) -> Unit, maxFrequencyHz: Double = 200.0): AnalyzerLiveSession =
        AnalyzerLiveSession(pcm, onSpectrum, maxFrequencyHz = maxFrequencyHz)
}
