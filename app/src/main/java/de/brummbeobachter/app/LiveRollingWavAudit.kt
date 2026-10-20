package de.brummbeobachter.app

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/** Non-destructive audit of real live Rolling PCM through the production WAV header.
 * Does NOT certify archive copy/upload or uninterrupted original microphone samples.
 */
internal object LiveRollingWavAudit {
    data class Evidence(val status: String, val detail: String)

    fun execute(context: Context): Evidence {
        if (!RollingAudioEngine.isRunning) return Evidence("SKIP", "Scout Rolling not running")
        val epochBefore = RollingAudioEngine.continuity()
        val snapshot = RollingAudioEngine.snapshot()
            ?: return Evidence("SKIP", "No live Rolling PCM snapshot")
        if (snapshot.pcm16.size < 2 || snapshot.pcm16.size % 2 != 0)
            return Evidence("FAIL", "Invalid PCM16 byte count=${snapshot.pcm16.size}")
        val file = File.createTempFile("lfn_live_rolling_audit_", ".wav", context.cacheDir)
        var verdict = Evidence("FAIL", "Unfinished live WAV audit")
        try {
            val header = WavPcm16.buildHeader(RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ, 1, 16, snapshot.pcm16.size)
            FileOutputStream(file).use { output ->
                output.write(header)
                var offset = 0
                while (offset < snapshot.pcm16.size) {
                    val n = minOf(4093, snapshot.pcm16.size - offset)
                    output.write(snapshot.pcm16, offset, n)
                    offset += n
                }
                output.fd.sync()
            }
            val expected = MessageDigest.getInstance("SHA-256").digest(snapshot.pcm16)
            val actual = MessageDigest.getInstance("SHA-256")
            val raw = file.inputStream().use { input ->
                val wavHeader = ByteArray(44)
                var read = 0
                while (read < 44) {
                    val n = input.read(wavHeader, read, 44 - read)
                    if (n < 0) break
                    read += n
                }
                val bytes = input.readBytes()
                if (read != 44 || !wavHeader.contentEquals(header)) null
                else bytes
            }
            val epochAfter = RollingAudioEngine.continuity()
            val sameEpoch = epochBefore.generation == epochAfter.generation &&
                epochBefore.starts == epochAfter.starts && epochBefore.stops == epochAfter.stops
            val equal = raw != null && raw.size == snapshot.pcm16.size &&
                actual.digest(raw).contentEquals(expected) && file.length() == (44L + snapshot.pcm16.size)
            verdict = Evidence(if (sameEpoch && equal) "PASS" else "FAIL",
                "Live Rolling snapshot frames=${snapshot.pcm16.size / 2}; WAV header/length/payload SHA256=$equal; capture epoch unchanged=$sameEpoch; disposable WAV only, NOT archive writer continuity")
        } catch (e: Exception) {
            verdict = Evidence("FAIL", "Live WAV audit error ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            if (file.exists() && !file.delete()) verdict = Evidence("FAIL", verdict.detail + "; temporary WAV deletion failed")
        }
        return verdict
    }
}
