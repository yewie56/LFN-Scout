package de.brummbeobachter.app

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

/** Disposable PCM16 WAV filesystem roundtrip; never opens a user's recording. */
internal object IsolatedWavIntegrityTest {
    data class Result(val ok: Boolean, val detail: String)

    fun execute(cacheDir: File): Result {
        val fixture = File.createTempFile("lfn_wav_integrity_", ".wav", cacheDir)
        val outcome = try {
            val frameCount = 8192
            val payload = ByteArray(frameCount * 2)
            for (i in 0 until frameCount) {
                val sample = ((i * 7919) and 0xffff) - 32768
                payload[i * 2] = sample.toByte()
                payload[i * 2 + 1] = (sample ushr 8).toByte()
            }
            val header = WavPcm16.buildHeader(8000, 1, 16, payload.size)
            FileOutputStream(fixture).use { stream ->
                stream.write(header)
                // Different block boundaries force multiple real filesystem writes.
                var position = 0
                for (size in intArrayOf(13, 101, 4096, 3000, payload.size)) {
                    if (position >= payload.size) break
                    val n = minOf(size, payload.size - position)
                    stream.write(payload, position, n)
                    position += n
                }
                if (position < payload.size) stream.write(payload, position, payload.size - position)
                stream.fd.sync()
            }
            val readBack = fixture.readBytes()
            val h = ByteBuffer.wrap(readBack).order(ByteOrder.LITTLE_ENDIAN)
            val structural = readBack.size == 44 + payload.size &&
                String(readBack, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(readBack, 8, 4, Charsets.US_ASCII) == "WAVE" &&
                String(readBack, 36, 4, Charsets.US_ASCII) == "data" &&
                h.getInt(4) == readBack.size - 8 && h.getInt(24) == 8000 &&
                h.getShort(22).toInt() == 1 && h.getShort(34).toInt() == 16 &&
                h.getInt(40) == payload.size
            val expectedHash = MessageDigest.getInstance("SHA-256").digest(payload)
            val actualHash = MessageDigest.getInstance("SHA-256")
                .digest(readBack.copyOfRange(44, readBack.size))
            Result(structural && expectedHash.contentEquals(actualHash),
                "Isolated file WAV PCM16; frames=$frameCount payloadBytes=${payload.size}; headerValid=$structural SHA256=${expectedHash.contentEquals(actualHash)}; NOT production recording path")
        } catch (e: Exception) {
            Result(false, "Fixture error ${e.javaClass.simpleName}: ${e.message}")
        } finally {
        }
        // Do not claim fixture cleanup succeeded unless filesystem deletion did.
        val deleted = !fixture.exists() || fixture.delete()
        return if (deleted) outcome else Result(false, outcome.detail + "; FAILED to delete temporary WAV")
    }
}
