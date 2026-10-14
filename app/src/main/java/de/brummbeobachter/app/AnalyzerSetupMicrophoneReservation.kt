package de.brummbeobachter.app

/** Legacy synchronous setup-preflight entry point.
 * Delegate to the single coordinator admission decision. Acquiring the MIC
 * token alone is insufficient while asynchronous service lifecycles are open.
 * NOT an authorization to execute an audio/FFT setup transition.
 */
internal object AnalyzerSetupMicrophoneReservation {
    fun <T> withReservation(operation: () -> T): T? =
        AnalyzerSetupTransitionCoordinator.withSetupReservation(operation)
}
