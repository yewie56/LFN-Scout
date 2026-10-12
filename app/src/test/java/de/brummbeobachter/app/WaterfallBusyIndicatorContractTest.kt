package de.brummbeobachter.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WaterfallBusyIndicatorContractTest {
    @Test
    fun busy_text_is_bound_to_selected_recording_only() {
        val candidates = listOf(
            File("app/src/main/java/de/brummbeobachter/app/MainActivity.kt"),
            File("src/main/java/de/brummbeobachter/app/MainActivity.kt")
        )
        val sourceFile = candidates.firstOrNull { it.isFile }
            ?: error("MainActivity.kt not found from ${System.getProperty("user.dir")}")
        val source = sourceFile.readText()

        assertTrue(source.contains("var generatingWaterfallRecordingId by remember"))
        assertTrue(source.contains("generatingWaterfallRecordingId == entry.recordingId"))
        assertTrue(source.contains("generatingWaterfallRecordingId = null"))
    }
}
