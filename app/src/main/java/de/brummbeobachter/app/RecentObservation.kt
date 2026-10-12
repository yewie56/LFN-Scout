package de.brummbeobachter.app

import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class RecentObservation(
    val rating: Int,
    val responseTime: String?,
    val scheduledTime: String
) {
    val displayTime: String
        get() = responseTime ?: scheduledTime

    fun formattedLocalTime(): String {
        return try {
            OffsetDateTime
                .parse(displayTime)
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm"))
        } catch (_: Exception) {
            displayTime
        }
    }
}

data class RecentObservationsResult(
    val success: Boolean,
    val observerId: String = "",
    val items: List<RecentObservation> = emptyList(),
    val keyRevoked: Boolean = false,
    val message: String
)

object RecentObservationsParser {

    fun parse(httpCode: Int, body: String): RecentObservationsResult {
        if (httpCode !in 200..299) {
            val error = try {
                JSONObject(body).optString("error").takeIf { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }

            return RecentObservationsResult(
                success = false,
                keyRevoked =
                    error == "key_revoked",
                message = buildString {
                    append("Abruf fehlgeschlagen (HTTP $httpCode)")
                    if (!error.isNullOrBlank()) append(": $error")
                }
            )
        }

        return try {
            val root = JSONObject(body)

            if (!root.optBoolean("ok", false)) {
                return RecentObservationsResult(
                    success = false,
                    message = "Ungültige Serverantwort"
                )
            }

            val observerId =
                root.optString(
                    "observer_id",
                    ""
                )

            val array = root.optJSONArray("observations")
                ?: return RecentObservationsResult(
                    success = true,
                    observerId = observerId,
                    items = emptyList(),
                    message = "Keine Bewertungen vorhanden"
                )

            val items = buildList {
                val count = minOf(array.length(), 5)

                for (index in 0 until count) {
                    val obj = array.optJSONObject(index) ?: continue

                    if (!obj.has("rating") || !obj.has("scheduled_time")) {
                        continue
                    }

                    val rating = obj.optInt("rating", Int.MIN_VALUE)
                    if (rating == Int.MIN_VALUE) continue

                    val scheduledTime = obj.optString("scheduled_time", "")
                    if (scheduledTime.isBlank()) continue

                    val responseTime =
                        if (obj.isNull("response_time")) {
                            null
                        } else {
                            obj.optString("response_time", "")
                                .takeIf { it.isNotBlank() }
                        }

                    add(
                        RecentObservation(
                            rating = rating,
                            responseTime = responseTime,
                            scheduledTime = scheduledTime
                        )
                    )
                }
            }

            RecentObservationsResult(
                success = true,
                observerId = observerId,
                items = items,
                message = if (items.isEmpty()) {
                    "Keine Bewertungen vorhanden"
                } else {
                    "Abruf erfolgreich"
                }
            )
        } catch (e: Exception) {
            RecentObservationsResult(
                success = false,
                message = "Serverantwort konnte nicht gelesen werden: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }
}
