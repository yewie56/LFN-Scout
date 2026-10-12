package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationLocationTest {

    @Test
    fun unavailable_location_uses_zero_zero() {
        val location = ObservationLocation.unavailable()

        assertFalse(location.available)
        assertEquals(0.0, location.latitude, 0.0)
        assertEquals(0.0, location.longitude, 0.0)
    }

    @Test
    fun available_location_preserves_full_double_precision() {
        val location = ObservationLocation(
            latitude = 49.537123456,
            longitude = 8.578987654,
            accuracyMeters = 8.5f,
            available = true
        )

        assertTrue(location.available)
        assertEquals(49.537123456, location.latitude, 0.0)
        assertEquals(8.578987654, location.longitude, 0.0)
    }
}
