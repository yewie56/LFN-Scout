package de.brummbeobachter.app

fun main() {
    check(SeismicPlotUiPolicy.MASTER_WIDTH_PX == 6144) { "MASTER_WIDTH_PX must be 6144" }
    check(SeismicPlotUiPolicy.MASTER_HEIGHT_PX == 3456) { "MASTER_HEIGHT_PX must be 3456" }
    check(SeismicPlotUiPolicy.EXPORT_SUPERSAMPLE == 4) { "EXPORT_SUPERSAMPLE must be 4" }

    val bin = SeismicEnvelopeBin(
        min = -8f,
        max = 7f,
        first = 1f,
        last = 2f,
        minBeforeMax = false
    )
    val values = SeismicScheliPolicy.orderedTraceValues(listOf(bin))
    check(values.contentEquals(floatArrayOf(1f, 7f, -8f, 2f))) {
        "Expected first -> max -> min -> last in time order, got ${values.contentToString()}"
    }
    println("PASS: SeismicDetailReductionSelfTest")
}
