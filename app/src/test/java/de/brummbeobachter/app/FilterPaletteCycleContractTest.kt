package de.brummbeobachter.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterPaletteCycleContractTest {
    private fun source(name: String): String {
        val roots = listOf(File("app/src/main/java/de/brummbeobachter/app"),
            File("src/main/java/de/brummbeobachter/app"))
        return roots.map { File(it,name) }.firstOrNull { it.isFile }
            ?.readText() ?: error("Source missing: $name")
    }

    @Test fun filter_button_cycles_off_violet_green_off() {
        val controller = source("LiveAnalyzerController.kt")
        assertTrue(controller.contains("fun cycleFilterPalette(context: Context)"))
        assertTrue(controller.contains("!showFilterWaterfall -> { showFilterWaterfall = true; filterPaletteGreen = false }"))
        assertTrue(controller.contains("!filterPaletteGreen -> filterPaletteGreen = true"))
        assertTrue(controller.contains("else -> { showFilterWaterfall = false; filterPaletteGreen = false }"))
        assertTrue(controller.contains(".putBoolean(\"filter_palette_green\", filterPaletteGreen)"))
        val screen = source("LiveAnalyzerScreen.kt")
        assertTrue(screen.contains("LiveAnalyzerController.cycleFilterPalette(context)"))
        assertTrue(screen.contains("R.drawable.original_waterfall_icon"))
        assertTrue(screen.contains("filterPaletteGreen=state.filterPaletteGreen"))
    }

    @Test fun filter_colors_propagate_to_display_and_image_export() {
        val renderer = source("FilterWaterfallRenderer.kt")
        val screen = source("LiveAnalyzerScreen.kt")
        val controller = source("LiveAnalyzerController.kt")
        assertTrue(renderer.contains("private val greenPalette"))
        assertTrue(renderer.contains("if (green) greenPalette else this.palette"))
        assertTrue(screen.contains("state.filterMinHz,state.filterMaxHz,state.filterPaletteGreen"))
        assertTrue(controller.contains("s.filterDisplayMinDb,s.filterDisplayMaxDb,s.filterPaletteGreen"))
        assertTrue(controller.contains("FilterWaterfallRenderer.filterColor(db,barMin,barMax,s.filterPaletteGreen)"))
    }
}
