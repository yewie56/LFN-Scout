package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

/** Regression for legacy index schema 1: never guess a Direct setup among multiple candidates. */
class AnalyzerModePinMigrationTest {
    @Test fun uniqueRollingCandidateIsPinned() {
        assertEquals("FFT-Test", AnalyzerModePinMigration.uniqueCandidate(listOf("FFT-Test")))
    }

    @Test fun ambiguousDirectCandidatesAreNeverChosen() {
        assertNull(AnalyzerModePinMigration.uniqueCandidate(listOf("DIRMON1", "uht2")))
    }

    @Test fun emptyCandidatesRemainUnassigned() {
        assertNull(AnalyzerModePinMigration.uniqueCandidate(emptyList()))
    }
}
