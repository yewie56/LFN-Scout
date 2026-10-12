package de.brummbeobachter.app

object SeismicAmplitudePolicy {
    // Aus dem vom Nutzer bereitgestellten WBBU-Plot abgeleitete Darstellungsreferenz:
    // 40000 nm/s entsprechen dort ca. 4.95 Zeilenabstaenden.
    const val WBBU_REFERENCE_SCALE_NM_PER_SEC = 40000.0
    const val WBBU_REFERENCE_ROW_SPACINGS = 4.95
    const val WBBU_NM_PER_SEC_PER_ROW_SPACING = WBBU_REFERENCE_SCALE_NM_PER_SEC / WBBU_REFERENCE_ROW_SPACINGS

    fun velocityNmPerSecPerCount(channel: SeismicChannel): Double? {
        val sensitivity = channel.sensitivity ?: return null
        if (!sensitivity.isFinite() || sensitivity <= 0.0) return null
        val units = normalizeUnits(channel.sensitivityUnits)
        return when (units) {
            "M/S", "M/SEC", "M/S^1" -> 1.0e9 / sensitivity
            "NM/S", "NM/SEC" -> 1.0 / sensitivity
            else -> null
        }
    }

    fun rowSpacingUnits(amplitudeNmPerSec: Double): Double =
        amplitudeNmPerSec / WBBU_NM_PER_SEC_PER_ROW_SPACING

    private fun normalizeUnits(value: String): String = value
        .trim()
        .uppercase()
        .replace(" ", "")
        .replace("METERS/SECOND", "M/S")
        .replace("METER/SECOND", "M/S")
}
