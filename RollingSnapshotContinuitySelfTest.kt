import de.brummbeobachter.app.RollingAudioEngine
import de.brummbeobachter.app.RollingAudioPolicy

fun main() {
    RollingAudioEngine.start("UNPROCESSED", "Test")
    repeat(RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ * 2) { index ->
        RollingAudioEngine.append(index.toShort(), false)
    }
    check(RollingAudioEngine.isRunning)
    check(RollingAudioEngine.bufferedSeconds() == 2)
    val first = RollingAudioEngine.snapshot(2_000L) ?: error("snapshot missing")
    check(first.durationSeconds == 2)
    check(RollingAudioEngine.isRunning)
    check(RollingAudioEngine.bufferedSeconds() == 2)
    repeat(RollingAudioPolicy.TARGET_SAMPLE_RATE_HZ) { index ->
        RollingAudioEngine.append((10_000 + index).toShort(), false)
    }
    check(RollingAudioEngine.isRunning)
    check(RollingAudioEngine.bufferedSeconds() == 3)
    val second = RollingAudioEngine.snapshot(3_000L) ?: error("snapshot missing")
    check(second.durationSeconds == 3)
    check(RollingAudioEngine.isRunning)
    println("PASS: RollingSnapshotContinuitySelfTest")
}
