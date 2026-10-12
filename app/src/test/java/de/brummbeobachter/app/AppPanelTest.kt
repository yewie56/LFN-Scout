package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPanelTest {

    @Test
    fun all_five_panels_exist() {
        assertEquals(
            listOf(
                AppPanel.MAIN,
                AppPanel.SEISMIC,
                AppPanel.SETTINGS,
                AppPanel.MORE,
                AppPanel.ANALYZER
            ),
            AppPanel.entries.toList()
        )
    }

    @Test
    fun radius_histograms_use_two_columns() {
        assertEquals(
            2,
            FaceliftLayoutPolicy.RADIUS_HISTOGRAM_COLUMNS
        )
    }
}
