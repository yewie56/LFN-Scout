package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class IsolatedWavIntegrityTestTest {
    @Test fun disposableWavRoundtripDoesNotLeaveFilesBehind() {
        val dir = Files.createTempDirectory("lfn_wav_test_").toFile()
        try {
            val before = dir.listFiles()?.size ?: 0
            val result = IsolatedWavIntegrityTest.execute(dir)
            assertTrue(result.detail, result.ok)
            assertEquals("Fixture cleanup", before, dir.listFiles()?.size ?: -1)
        } finally {
            dir.deleteRecursively()
        }
    }
}
