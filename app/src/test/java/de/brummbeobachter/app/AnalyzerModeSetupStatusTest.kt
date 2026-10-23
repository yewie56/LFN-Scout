package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzerModeSetupStatusTest {
    private val entries = listOf("fft-id" to "FFT-Test", "direct-id" to "DIRMON1")

    @Test fun rollingAlwaysDisplaysRollingPinEvenAfterDirectSelectedGlobally() {
        assertEquals("Setup: FFT-Test (zugeordnet)", AnalyzerModeSetupStatus.label(entries, "fft-id"))
    }

    @Test fun directDisplaysItsOwnPin() {
        assertEquals("Setup: DIRMON1 (zugeordnet)", AnalyzerModeSetupStatus.label(entries, "direct-id"))
    }

    @Test fun unassignedModeDoesNotFallBackToGlobalSetup() {
        assertEquals("Setup: keines", AnalyzerModeSetupStatus.label(entries, null))
    }
}
