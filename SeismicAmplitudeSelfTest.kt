package de.brummbeobachter.app

object SeismicAmplitudeSelfTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val fdsn = "GE|STU||HHZ|48.77|9.19|700|0|0|-90|Sensor|2500000000|1.0|M/S|100|2020-01-01T00:00:00|"
        val parsed = SeismicStationPolicy.parseFdsnChannelText(fdsn, 49.54, 8.60)
        check(parsed.channels.size == 1)
        val channel = parsed.channels.single()
        check(channel.sensitivity == 2_500_000_000.0)
        check(channel.sensitivityFrequencyHz == 1.0)
        check(channel.sensitivityUnits == "M/S")

        val scale = SeismicAmplitudePolicy.velocityNmPerSecPerCount(channel)
        check(scale != null)
        check(kotlin.math.abs(scale - 0.4) < 1e-12) { "expected 0.4 nm/s per count, got $scale" }
        check(SeismicAmplitudePolicy.velocityNmPerSecPerCount(channel.copy(sensitivityUnits = "M/S**2")) == null)

        val rows = SeismicHelicorderPolicy.buildRows(
            samples = intArrayOf(0, 1000, -1000, 0),
            sampleRateHz = 1.0,
            traceStartEpochMs = 0L,
            requestedStartEpochMs = 0L,
            requestedEndEpochMs = 4000L,
            binsPerRow = 4,
            valueScale = scale
        )
        val maxAbs = SeismicHelicorderPolicy.maxAbs(rows)
        check(kotlin.math.abs(maxAbs - 400.0) < 0.01) { "expected 400 nm/s, got $maxAbs" }

        check(SeismicAmplitudePolicy.WBBU_REFERENCE_SCALE_NM_PER_SEC == 40000.0)
        check(SeismicAmplitudePolicy.WBBU_NM_PER_SEC_PER_ROW_SPACING in 8000.0..8200.0)
        check(SeismicAmplitudePolicy.rowSpacingUnits(40000.0) in 4.8..5.1)

        println("SeismicAmplitudeSelfTest PASS")
    }
}
