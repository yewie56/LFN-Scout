package de.brummbeobachter.app

/** Tracks accepted versus persisted PCM16 blocks in one MON recording session. */
internal class MonPcmWriteAccounting {
    private var acceptedBlocks = 0L
    private var acceptedBytes = 0L
    private var writtenBlocks = 0L
    private var writtenBytes = 0L
    private var rejectedBlocks = 0L
    private val acceptedCrc = java.util.zip.CRC32()
    private val writtenCrc = java.util.zip.CRC32()

    @Synchronized fun accepted(bytes: Int) {
        require(bytes > 0 && bytes % 2 == 0)
        acceptedBlocks++
        acceptedBytes += bytes
    }
    @Synchronized fun written(bytes: Int) {
        require(bytes > 0 && bytes % 2 == 0)
        writtenBlocks++
        writtenBytes += bytes
    }
    @Synchronized fun acceptedBlock(data: ByteArray) {
        accepted(data.size)
        acceptedCrc.update(data)
    }
    @Synchronized fun writtenBlock(data: ByteArray) {
        written(data.size)
        writtenCrc.update(data)
    }
    @Synchronized fun rejected() { rejectedBlocks++ }
    @Synchronized fun snapshot() = Snapshot(acceptedBlocks, acceptedBytes, writtenBlocks, writtenBytes, rejectedBlocks, acceptedCrc.value, writtenCrc.value)
    /** Read the persisted PCM payload back from disk. This is independent of the
     * CRC calculated from the buffers submitted to the writer thread. */
    @Synchronized fun persistedPayloadMatches(file: java.io.File): Boolean {
        val expected = snapshot()
        if (!expected.complete(file.length())) return false
        return try {
            val crc = java.util.zip.CRC32()
            file.inputStream().buffered().use { input ->
                var headerRemaining = 44L
                while (headerRemaining > 0L) {
                    val skipped = input.skip(headerRemaining)
                    if (skipped <= 0) {
                        if (input.read() < 0) return false
                        headerRemaining--
                    } else headerRemaining -= skipped
                }
                val bytes = ByteArray(8192)
                var remaining = expected.writtenBytes
                while (remaining > 0L) {
                    val count = input.read(bytes, 0, minOf(bytes.size.toLong(), remaining).toInt())
                    if (count <= 0) return false
                    crc.update(bytes, 0, count)
                    remaining -= count
                }
                if (input.read() != -1) return false
            }
            crc.value == expected.writtenCrc32
        } catch (_: java.io.IOException) { false }
    }

    internal data class Snapshot(
        val acceptedBlocks: Long, val acceptedBytes: Long,
        val writtenBlocks: Long, val writtenBytes: Long, val rejectedBlocks: Long,
        val acceptedCrc32: Long, val writtenCrc32: Long
    ) {
        fun complete(fileLength: Long): Boolean =
            rejectedBlocks == 0L && acceptedBlocks > 0L &&
            acceptedBlocks == writtenBlocks && acceptedBytes == writtenBytes && acceptedCrc32 == writtenCrc32 &&
            fileLength == 44L + writtenBytes
    }
}
