import de.brummbeobachter.app.RollingAudioEngine
import de.brummbeobachter.app.RollingAudioPolicy

fun decodeSample(bytes: ByteArray, sampleIndex: Int): Int {
    val offset = sampleIndex * 2
    val lo = bytes[offset].toInt() and 0xFF
    val hi = bytes[offset + 1].toInt()
    return ((hi shl 8) or lo).toShort().toInt()
}

fun main() {
    RollingAudioEngine.start("UNPROCESSED", "Auto")

    repeat(8 * 60 * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ) {
        RollingAudioEngine.append((it % 20000).toShort(), false)
    }
    check(RollingAudioEngine.bufferedSeconds() == 8 * 60)
    val partial = RollingAudioEngine.snapshot(1_000_000L) ?: error("partial snapshot missing")
    check(partial.durationSeconds == 8 * 60)
    check(RollingAudioEngine.bufferedSeconds() == 8 * 60) { "snapshot must not clear partial buffer" }

    val remaining = RollingAudioPolicy.CAPACITY_SAMPLES - 8 * 60 * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ
    repeat(remaining) {
        RollingAudioEngine.append(((it + 123) % 20000).toShort(), false)
    }
    check(RollingAudioEngine.bufferedSeconds() == RollingAudioPolicy.ROLLING_DURATION_SECONDS)
    val full1 = RollingAudioEngine.snapshot(2_000_000L) ?: error("full snapshot missing")
    check(full1.durationSeconds == RollingAudioPolicy.ROLLING_DURATION_SECONDS)
    val expectedFirstAfter = decodeSample(full1.pcm16, 60 * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ)

    repeat(60 * RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ) {
        RollingAudioEngine.append(12345.toShort(), false)
    }
    check(RollingAudioEngine.bufferedSeconds() == RollingAudioPolicy.ROLLING_DURATION_SECONDS)
    val full2 = RollingAudioEngine.snapshot(3_000_000L) ?: error("second full snapshot missing")
    check(full2.durationSeconds == RollingAudioPolicy.ROLLING_DURATION_SECONDS)
    check(RollingAudioEngine.bufferedSeconds() == RollingAudioPolicy.ROLLING_DURATION_SECONDS) { "snapshot must not clear full buffer" }
    val firstAfter = decodeSample(full2.pcm16, 0)
    check(firstAfter == expectedFirstAfter) { "oldest sample must advance exactly by overwritten samples" }

    RollingAudioEngine.stop()
    println("PASS: RollingBufferEngineSelfTest")
}
