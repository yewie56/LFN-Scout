package de.brummbeobachter.app

fun main() {
    val rows = SeismicHelicorderPolicy.buildRows(
        samples = intArrayOf(10, 0, 20, 5),
        sampleRateHz = 1.0,
        traceStartEpochMs = 0L,
        requestedStartEpochMs = 0L,
        requestedEndEpochMs = 1_800_000L,
        binsPerRow = 1,
        valueScale = 1.0
    )
    val bin = rows.first().bins.single()
    check(bin.first == 1.25f) { "first=${bin.first}" }
    check(bin.min == -8.75f) { "min=${bin.min}" }
    check(bin.max == 11.25f) { "max=${bin.max}" }
    check(bin.last == -3.75f) { "last=${bin.last}" }
    check(bin.minBeforeMax) { "Expected minimum before maximum" }
    val trace = SeismicScheliPolicy.orderedTraceValues(listOf(bin))
    check(trace.contentEquals(floatArrayOf(1.25f, -8.75f, 11.25f, -3.75f))) {
        "Temporal order lost: ${trace.contentToString()}"
    }
    println("PASS: SeismicBinDetailSelfTest")
}
