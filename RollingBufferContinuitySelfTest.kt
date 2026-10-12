import de.brummbeobachter.app.RollingAudioPolicy

fun main() {
    check(RollingAudioPolicy.bufferStatusText(0) == "0:00 / 30:00 min")
    check(RollingAudioPolicy.bufferStatusText(8 * 60) == "8:00 / 30:00 min")
    check(RollingAudioPolicy.bufferStatusText(30 * 60) == "letzte 30:00 min")
    check(RollingAudioPolicy.bufferStatusText(31 * 60) == "letzte 30:00 min")
    println("PASS: RollingBufferContinuitySelfTest")
}
