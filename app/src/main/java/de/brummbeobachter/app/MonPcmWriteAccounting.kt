package de.brummbeobachter.app

/** Tracks accepted versus persisted PCM16 blocks in one MON recording session. */
internal class MonPcmWriteAccounting {
    private var acceptedBlocks = 0L
    private var acceptedBytes = 0L
    private var writtenBlocks = 0L
    private var writtenBytes = 0L
    private var rejectedBlocks = 0L

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
    @Synchronized fun rejected() { rejectedBlocks++ }
    @Synchronized fun snapshot() = Snapshot(acceptedBlocks, acceptedBytes, writtenBlocks, writtenBytes, rejectedBlocks)
    internal data class Snapshot(
        val acceptedBlocks: Long, val acceptedBytes: Long,
        val writtenBlocks: Long, val writtenBytes: Long, val rejectedBlocks: Long
    ) {
        fun complete(fileLength: Long): Boolean =
            rejectedBlocks == 0L && acceptedBlocks > 0L &&
            acceptedBlocks == writtenBlocks && acceptedBytes == writtenBytes &&
            fileLength == 44L + writtenBytes
    }
}
