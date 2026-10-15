package de.brummbeobachter.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SeismicPlotInteractionContractTest {
    private fun source(path: String): String {
        val candidates = listOf(File(path), File(path.removePrefix("app/")))
        return candidates.firstOrNull { it.isFile }?.readText()
            ?: error("Source not found: $path")
    }

    /** Check the handler itself, not just the presence of a label in the file. */
    private fun gestureBlock(main: String, key: String): String {
        val token = ".pointerInput(data.loadedAtEpochMs, \"$key\")"
        val start = main.indexOf(token)
        assertTrue("Missing gesture handler: $key", start >= 0)
        return main.substring(start, (start + 620).coerceAtMost(main.length))
    }

    @Test
    fun near_plot_trace_width_is_doubled() {
        val policy = source("app/src/main/java/de/brummbeobachter/app/SeismicPlotUiPolicy.kt")
        val exporter = source("app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt")
        assertTrue(policy.contains("TRACE_STROKE_WIDTH_PX = 1.6f"))
        assertTrue(exporter.contains("strokeWidth = SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX"))
    }

    @Test
    fun both_plot_types_are_zoomable_and_resettable() {
        val main = source("app/src/main/java/de/brummbeobachter/app/MainActivity.kt")
        for (key in listOf("wbbu_zoom", "near_bitmap_zoom")) {
            val block = gestureBlock(main, key)
            assertTrue("No transform gesture in $key", block.contains("detectTransformGestures"))
            assertTrue("No zoom clamp in $key", block.contains("SeismicPlotUiPolicy.clampZoom"))
            assertTrue("No offset update in $key", block.contains("seismicPlotOffset ="))
        }
        for (key in listOf("wbbu_double_tap_reset", "near_bitmap_double_tap_reset")) {
            val block = gestureBlock(main, key)
            assertTrue("No double-tap gesture in $key", block.contains("detectTapGestures(onDoubleTap ="))
            assertTrue("No zoom reset in $key", block.contains("seismicPlotZoom = SeismicPlotUiPolicy.MIN_ZOOM"))
            assertTrue("No pan reset in $key", block.contains("seismicPlotOffset = Offset.Zero"))
        }
    }

    @Test
    fun near_plot_contains_distance_legend_and_save_feedback() {
        val main = source("app/src/main/java/de/brummbeobachter/app/MainActivity.kt")
        assertTrue(main.contains("40000 nm/s"))
        assertTrue(main.contains("Text(\"Plot speichern\")"))
        assertTrue(main.contains("Plot erfolgreich gespeichert"))
        assertTrue(main.contains("SEISMIC_PLOT_SAVE_OK"))
    }

    @Test
    fun public_export_directory_is_variant_specific() {
        val policy = source("app/src/main/java/de/brummbeobachter/app/PublicDataDirectory.kt")
        val exporter = source("app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt")
        assertTrue(policy.contains("BuildConfig.APPLICATION_ID.endsWith(\".githubdebug\")"))
        assertTrue(policy.contains("if (isTest) \"lfn-scout-test\" else \"LFN-Scout\""))
        assertTrue(policy.contains("\"Download/\$folder/\""))
        assertTrue(exporter.contains("val PUBLIC_RELATIVE_PATH = PublicDataDirectory.relativePath"))
        assertTrue(exporter.contains("put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_RELATIVE_PATH)"))
    }

    @Test
    fun exporter_writes_full_near_plot_not_only_zoom_viewport() {
        val exporter = source("app/src/main/java/de/brummbeobachter/app/SeismicPlotExporter.kt")
        assertTrue(exporter.contains("renderNearPlot(snapshot)"))
        assertTrue(exporter.contains("snapshot.helicorderRows.forEachIndexed"))
        assertTrue(exporter.contains("PublicDataDirectory.relativePath"))
        assertTrue(exporter.contains("MASTER_WIDTH_PX"))
        assertTrue(exporter.contains("MASTER_HEIGHT_PX"))
        assertTrue(!exporter.contains("Bitmap.createScaledBitmap"))
    }
}
