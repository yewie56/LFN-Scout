package de.brummbeobachter.app

/** Pure display state for the analyzer's compact Rolling-Buffer audio save button. */
object RecordingButtonPolicy {
    enum class Cloud { NONE, PENDING, CONFIRMED }
    data class View(val seconds: String, val recordings: String, val cloud: Cloud)
    fun view(bufferedSeconds: Int, configuredSeconds: Int, savedToday: Int,
             maxToday: Int, lastSavedCloud: Cloud): View {
        require(configuredSeconds>0 && bufferedSeconds>=0 && savedToday>=0 && maxToday>=0)
        return View("${bufferedSeconds.coerceAtMost(configuredSeconds)}/$configuredSeconds",
            "$savedToday/$maxToday REC",lastSavedCloud)
    }
    fun resetCloudOnNewBuffer(previousSeconds:Int,newSeconds:Int, configuredSeconds:Int):Boolean =
        previousSeconds>=configuredSeconds && newSeconds<previousSeconds
}
