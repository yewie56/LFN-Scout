package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class SystemTestArtifactStoreTest {
    @Test fun pairedGenerationsAndCorruptionFailClosed() {
        val dir = Files.createTempDirectory("lfn-system-report-").toFile()
        try {
            assertNull(SystemTestArtifactStore.readFiles(dir, "systemtest_results.json"))
            SystemTestArtifactStore.saveFiles(dir, "first-json", "first-csv")
            assertEquals("first-json", SystemTestArtifactStore.readFiles(dir, "systemtest_results.json"))
            assertEquals("first-csv", SystemTestArtifactStore.readFiles(dir, "systemtest_results.csv"))
            SystemTestArtifactStore.saveFiles(dir, "second-json", "second-csv")
            assertEquals("second-json", SystemTestArtifactStore.readFiles(dir, "systemtest_results.json"))
            assertEquals("second-csv", SystemTestArtifactStore.readFiles(dir, "systemtest_results.csv"))
            val generation = dir.resolve("current_generation").readText().trim()
            assertTrue(dir.resolve(generation).resolve("systemtest_results.csv").delete())
            assertTrue(runCatching { SystemTestArtifactStore.readFiles(dir, "systemtest_results.json") }.isFailure)
            assertTrue(runCatching { SystemTestArtifactStore.readFiles(dir, "../invalid") }.isFailure)
        } finally { dir.deleteRecursively() }
    }

    @Test fun legacyReportsRemainReadableBeforeFirstNewSave() {
        val dir = Files.createTempDirectory("lfn-system-report-legacy-").toFile()
        try {
            dir.resolve("systemtest_results.json").writeText("legacy-json")
            dir.resolve("systemtest_results.csv").writeText("legacy-csv")
            assertEquals("legacy-json", SystemTestArtifactStore.readFiles(dir, "systemtest_results.json"))
            assertEquals("legacy-csv", SystemTestArtifactStore.readFiles(dir, "systemtest_results.csv"))
            SystemTestArtifactStore.saveFiles(dir, "new-json", "new-csv")
            assertEquals("new-json", SystemTestArtifactStore.readFiles(dir, "systemtest_results.json"))
            assertEquals("new-csv", SystemTestArtifactStore.readFiles(dir, "systemtest_results.csv"))
        } finally { dir.deleteRecursively() }
    }
}
