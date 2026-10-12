package de.brummbeobachter.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression for the former Compose-label issue. Since v0.4.78 the labels are
 * painted into the same master bitmap used by the UI and PNG export. There is
 * deliberately no longer a Compose loop over helicorderRows in MainActivity.
 */
class SeismicHelicorderComposeContractTest {
    private fun source(name: String): String {
        val relative = "app/src/main/java/de/brummbeobachter/app/$name"
        val candidates = listOf(File(relative), File(relative.removePrefix("app/")))
        return candidates.firstOrNull { it.isFile }?.readText()
            ?: error("Source not found: $relative from ${System.getProperty("user.dir")}")
    }

    @Test
    fun helicorder_time_labels_are_drawn_in_master_bitmap() {
        val exporter = source("SeismicPlotExporter.kt")
        assertTrue(exporter.contains("snapshot.helicorderRows.forEachIndexed { rowIndex, row ->"))
        assertTrue(exporter.contains("val hh = row.startMinuteOfDay / 60"))
        assertTrue(exporter.contains("val mm = row.startMinuteOfDay % 60"))
        assertTrue(exporter.contains("val time = \"%02d:%02d\".format(hh, mm)"))
        assertTrue(exporter.contains("canvas.drawText(time,"))
        assertTrue(exporter.contains("return rotateLeft(hiRes)"))
    }

    @Test
    fun helicorder_ui_and_png_share_the_rendered_bitmap() {
        val main = source("MainActivity.kt")
        val exporter = source("SeismicPlotExporter.kt")
        assertTrue(main.contains("val nearPlotBitmap = remember(data.loadedAtEpochMs)"))
        assertTrue(main.contains("SeismicPlotExporter.renderNearPlot(data)"))
        assertTrue(main.contains("val nearPlotImage = remember(nearPlotBitmap) { nearPlotBitmap.asImageBitmap() }"))
        assertTrue(main.contains("bitmap = nearPlotImage"))
        assertTrue(main.contains("SeismicPlotExporter.save(context, data, nearPlotBitmap)"))
        assertTrue(exporter.contains("val bitmap = nearPlotBitmap ?: renderNearPlot(snapshot)"))
        assertTrue(exporter.contains("return saveBitmap(context.applicationContext, bitmap, fileName)"))
        assertFalse(main.contains("data.helicorderRows.forEach { row ->"))
    }
}
