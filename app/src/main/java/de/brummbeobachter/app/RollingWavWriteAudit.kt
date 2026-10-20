package de.brummbeobachter.app

import java.io.File
import java.security.MessageDigest

/** Records real write calls on the productive temporary WAV path and verifies disk bytes.
 * This is file-integrity evidence, NOT a continuous multi-hour stream guarantee.
 */
internal object RollingWavWriteAudit {
    data class Evidence(val blocks: Int, val frames: Int, val bytes: Long, val sha256: String)

    fun write(file: File, header: ByteArray, pcm: ByteArray, blockBytes: Int = 8192): Evidence {
        require(header.size == 44 && blockBytes > 0 && blockBytes % 2 == 0 && pcm.size % 2 == 0)
        val digest = MessageDigest.getInstance("SHA-256")
        var blocks = 0
        var written = 0
        file.outputStream().buffered().use { stream ->
            stream.write(header)
            var offset = 0
            while (offset < pcm.size) {
                val length = minOf(blockBytes, pcm.size - offset)
                stream.write(pcm, offset, length)
                digest.update(pcm, offset, length)
                blocks++
                written += length
                offset += length
            }
            stream.flush()
        }
        val expected = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        check(file.length() == header.size.toLong() + written) { "WAV size mismatch" }
        val verification = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { stream ->
            val actualHeader = ByteArray(header.size)
            var off = 0
            while (off < actualHeader.size) {
                val n = stream.read(actualHeader, off, actualHeader.size - off)
                check(n > 0) { "Truncated WAV header" }
                off += n
            }
            check(actualHeader.contentEquals(header)) { "WAV header mismatch" }
            val buffer = ByteArray(blockBytes)
            var bytesRead = 0L
            while (true) {
                val n = stream.read(buffer)
                if (n < 0) break
                if (n > 0) { verification.update(buffer, 0, n); bytesRead += n }
            }
            check(bytesRead == written.toLong()) { "WAV PCM length mismatch" }
        }
        val observed = verification.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        check(observed == expected) { "WAV PCM hash mismatch" }
        return Evidence(blocks, pcm.size / 2, written.toLong(), observed)
    }
}
