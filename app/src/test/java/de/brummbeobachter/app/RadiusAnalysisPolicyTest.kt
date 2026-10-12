package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class RadiusAnalysisPolicyTest {

    @Test
    fun default_radius_is_600_km() {
        assertEquals(
            600,
            RadiusAnalysisPolicy.DEFAULT_RADIUS_KM
        )
    }

    @Test
    fun radius_is_clamped_to_supported_range() {
        assertEquals(
            1,
            RadiusAnalysisPolicy.normalizeRadiusKm(0)
        )
        assertEquals(
            600,
            RadiusAnalysisPolicy.normalizeRadiusKm(600)
        )
        assertEquals(
            20000,
            RadiusAnalysisPolicy.normalizeRadiusKm(50000)
        )
    }
}
