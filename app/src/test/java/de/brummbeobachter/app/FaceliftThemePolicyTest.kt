package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class FaceliftThemePolicyTest {

    @Test
    fun system_colors_are_dark_turquoise_and_saturated_chrome_yellow() {
        assertEquals("#1F8F8A", FaceliftThemePolicy.PRIMARY_HEX)
        assertEquals("#E0B000", FaceliftThemePolicy.ACCENT_HEX)
    }

    @Test
    fun vertical_layout_is_compact() {
        assertEquals(7, FaceliftLayoutPolicy.SCREEN_PADDING_DP)
        assertEquals(3, FaceliftLayoutPolicy.SECTION_GAP_DP)
        assertEquals(7, FaceliftLayoutPolicy.CARD_PADDING_DP)
        assertEquals(2, FaceliftLayoutPolicy.FIELD_GAP_DP)
    }
}
