package de.brummbeobachter.app

object RollingAudioPolicy {
    const val ROLLING_DURATION_SECONDS = 30 * 60
    const val TARGET_SAMPLE_RATE_HZ = 1000
    const val CAPACITY_SAMPLES = ROLLING_DURATION_SECONDS * TARGET_SAMPLE_RATE_HZ

    fun bufferStatusText(bufferedSeconds: Int): String {
        val sec = bufferedSeconds.coerceAtLeast(0)
        return if (sec >= ROLLING_DURATION_SECONDS) {
            "letzte 30:00 min"
        } else {
            "%d:%02d / 30:00 min".format(sec / 60, sec % 60)
        }
    }
}
