import de.brummbeobachter.app.SeismicPlotUiPolicy
import java.time.LocalDate
import java.time.LocalDateTime

fun main() {
    check(SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX == 1.6f)
    check(SeismicPlotUiPolicy.clampZoom(0.2f) == 1f)
    check(SeismicPlotUiPolicy.clampZoom(9f) == 5f)
    check(SeismicPlotUiPolicy.LEGEND_VALUE_NM_PER_SEC == 40000.0)
    check(SeismicPlotUiPolicy.LIVE_ASPECT_RATIO == 16f / 9f)
    check(SeismicPlotUiPolicy.normalizeNearPlotGain(0) == 1)
    check(SeismicPlotUiPolicy.normalizeNearPlotGain(2) == 2)
    check(SeismicPlotUiPolicy.normalizeNearPlotGain(25) == 20)
    check(SeismicPlotUiPolicy.EXPORT_WIDTH_PX == 1536)
    check(SeismicPlotUiPolicy.EXPORT_HEIGHT_PX == 864)
    check(SeismicPlotUiPolicy.EXPORT_SUPERSAMPLE == 4)
    check(SeismicPlotUiPolicy.MASTER_WIDTH_PX == 6144)
    check(SeismicPlotUiPolicy.MASTER_HEIGHT_PX == 3456)
    check(
        SeismicPlotUiPolicy.exportFileName(
            LocalDate.of(2026, 9, 17), "HS", "WBBU", "", "HLNUG_WBBU_IMAGE",
            LocalDateTime.of(2026, 9, 18, 10, 52, 3)
        ) == "Seismik_2026-09-17_10-52-03_HS-WBBU.png"
    )
    check(
        SeismicPlotUiPolicy.exportFileName(
            LocalDate.of(2026, 9, 16), "GE", "STU", "HHZ", "FDSN",
            LocalDateTime.of(2026, 9, 18, 10, 52, 4)
        ) == "Seismik_2026-09-16_10-52-04_GE-STU-HHZ.png"
    )
    println("SeismicPlotUiSelfTest PASS")
}
