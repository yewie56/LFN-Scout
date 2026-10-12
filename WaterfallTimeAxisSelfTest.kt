import de.brummbeobachter.app.WaterfallTimeAxisPolicy
import java.time.ZoneId

fun main() {
    val zone = ZoneId.of("Europe/Berlin")
    val start = java.time.ZonedDateTime.of(2026, 9, 13, 14, 40, 0, 0, zone).toInstant().toEpochMilli()
    val labels = WaterfallTimeAxisPolicy.labels(start, 1800.0, zone)
    check(labels == listOf("14:40:00", "14:47:30", "14:55:00", "15:02:30", "15:10:00")) { labels }
    println("PASS: WaterfallTimeAxisSelfTest")
}
