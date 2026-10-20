package de.brummbeobachter.app

/** Atomic, post-processing accounting at AudioRecord block boundaries.
 * This verifies software delivery from successful reads to the decimated ring.
 * It cannot detect samples lost in the microphone, HAL or Android audio stack.
 */
internal class RollingCaptureDeliveryAccounting {
    data class Snapshot(val epoch: Long, val blocks: Long, val sourceSamples: Long,
                        val emittedSamples: Long, val sourceReadErrors: Long)
    private var epoch = 0L
    private var blocks = 0L
    private var sourceSamples = 0L
    private var emittedSamples = 0L
    private var sourceReadErrors = 0L

    @Synchronized fun begin() {
        epoch++
        blocks = 0L
        sourceSamples = 0L
        emittedSamples = 0L
        sourceReadErrors = 0L
    }

    /** Only call after the entire read block has been processed by RollingAudioEngine. */
    @Synchronized fun completedRead(sourceCount: Int, emittedCount: Long) {
        if (sourceCount < 0) { sourceReadErrors++; return }
        if (sourceCount == 0) return
        require(emittedCount >= 0L)
        blocks++
        sourceSamples += sourceCount.toLong()
        emittedSamples += emittedCount
    }

    @Synchronized fun snapshot(): Snapshot = Snapshot(epoch, blocks, sourceSamples,
        emittedSamples, sourceReadErrors)
}
