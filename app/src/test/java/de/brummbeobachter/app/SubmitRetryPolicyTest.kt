package de.brummbeobachter.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitRetryPolicyTest {

    @Test
    fun network_failures_are_retryable() {
        assertTrue(
            SubmitResult(
                success = false,
                retryable = true,
                message = "Netzwerkfehler"
            ).retryable
        )
    }

    @Test
    fun permanent_server_rejection_is_not_retryable() {
        assertFalse(
            SubmitResultParser.parse(
                401,
                "{\"error\":\"invalid key\"}"
            ).retryable
        )
    }

    @Test
    fun transient_server_failure_is_retryable() {
        assertTrue(
            SubmitResultParser.parse(
                503,
                "{\"error\":\"temporarily unavailable\"}"
            ).retryable
        )
    }
}
