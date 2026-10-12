package de.brummbeobachter.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioCurrentRatingNamingContractTest {

    @Test
    fun normal_audio_filename_uses_latest_recent_items_at_recording_start() {
        val candidates = listOf(
            File("app/src/main/java/de/brummbeobachter/app/MainActivity.kt"),
            File("src/main/java/de/brummbeobachter/app/MainActivity.kt")
        )
        val sourceFile = candidates.firstOrNull { it.isFile }
            ?: error("MainActivity.kt not found from ${System.getProperty("user.dir")}")
        val source = sourceFile.readText()

        assertTrue(
            "MainActivity must keep the current recentItems state for audio naming",
            source.contains("val latestAudioRecentItems =\n        rememberUpdatedState(\n            recentItems\n        )")
        )
        assertTrue(
            "The recording must snapshot current observations when recordedAt is created",
            source.contains("val observationsAtRecordingStart =\n                    latestAudioRecentItems.value")
        )
        assertTrue(
            "Local WAV naming must use the recording-start snapshot",
            source.contains("observations =\n                                observationsAtRecordingStart")
        )
    }
}
