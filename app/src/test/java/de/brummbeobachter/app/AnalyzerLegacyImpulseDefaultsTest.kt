package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class AnalyzerLegacyImpulseDefaultsTest {
    @Test fun old8kSetupGetsRuntimeDefaultsWithoutMutation() {
        val original = JSONObject().put("range", 200)
        val migrated = AnalyzerLegacyImpulseDefaults.forMode(original, false)
        assertEquals(false, migrated.getBoolean("filter_impulse_mode"))
        assertEquals(3, migrated.getInt("filter_impulse_threshold"))
        assertFalse(original.has("filter_impulse_mode"))
        assertFalse(original.has("filter_impulse_threshold"))
    }
    @Test fun explicit8kValuesPreserved() {
        val original = JSONObject().put("filter_impulse_mode", true).put("filter_impulse_threshold", 8)
        val migrated = AnalyzerLegacyImpulseDefaults.forMode(original, false)
        assertTrue(migrated.getBoolean("filter_impulse_mode"))
        assertEquals(8, migrated.getInt("filter_impulse_threshold"))
    }
    @Test fun directModeNeverReceivesNewFields() {
        val original = JSONObject().put("range", 200)
        val migrated = AnalyzerLegacyImpulseDefaults.forMode(original, true)
        assertFalse(migrated.has("filter_impulse_mode"))
        assertFalse(migrated.has("filter_impulse_threshold"))
    }
}
