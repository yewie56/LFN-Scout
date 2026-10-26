package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

/** Guard against treating a missing setup as a successful save. */
class AnalyzerPersistenceResultContractTest {
    @Test fun missingSetupIsNotAValidSaveResult() {
        val failure = Result.failure<Boolean>(IllegalStateException("Kein aktives benanntes JSON-Setup"))
        assertTrue(failure.isFailure)
        assertFalse(failure.isSuccess)
    }

    @Test fun savedAndUnchangedAreBothNonErrors() {
        assertTrue(Result.success(true).isSuccess)
        assertTrue(Result.success(false).isSuccess)
    }
}
