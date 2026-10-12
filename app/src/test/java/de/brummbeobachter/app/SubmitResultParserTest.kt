package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmitResultParserTest {

    @Test
    fun parses_success_response() {
        val result = SubmitResultParser.parse(
            httpCode = 200,
            body = """{"ok":true,"duplicate":false,"observer_id":"BB001"}"""
        )

        assertTrue(result.success)
        assertEquals("BB001", result.observerId)
        assertEquals(false, result.duplicate)
    }

    @Test
    fun converts_http_error_to_readable_failure() {
        val result = SubmitResultParser.parse(
            httpCode = 401,
            body = """{"error":"invalid_or_disabled_key"}"""
        )

        assertEquals(false, result.success)
        assertTrue(result.message.contains("401"))
        assertTrue(result.message.contains("invalid_or_disabled_key"))
    }
}
