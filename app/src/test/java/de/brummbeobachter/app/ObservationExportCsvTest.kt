package de.brummbeobachter.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationExportCsvTest {

    @Test
    fun csv_contains_comment_and_core_fields() {
        val rows =
            listOf(
                OwnObservation(
                    observerId = "BB001",
                    eventId = "E1",
                    scheduledTime = "2026-09-03T08:00:00Z",
                    responseTime = "2026-09-03T08:01:00Z",
                    rating = 3,
                    comment = "Kommentar; mit Semikolon",
                    latitude = 49.5,
                    longitude = 8.5,
                    appVersion = "0.4.22",
                    wavPresent = false,
                    wavPath = null
                )
            )

        val csv =
            ObservationExportWriter.toCsv(rows)

        assertTrue(csv.contains("observer_id"))
        assertTrue(csv.contains("comment"))
        assertTrue(csv.contains("\"Kommentar; mit Semikolon\""))
    }
}
