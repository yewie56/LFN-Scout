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
