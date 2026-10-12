package de.brummbeobachter.app

data class ObservationPayload(
    val key: String,
    val eventId: String,
    val scheduledTime: String,
    val responseTime: String?,
    val rating: Int,
    val latitude: Double,
    val longitude: Double,
    val appVersion: String,
    val comment: String? = null
) {
    fun toJson(): String {
        require(rating in -1..5) { "rating must be between -1 and 5" }

        val responseJson =
            responseTime?.let {
                "\"${escapeJson(it)}\""
            } ?: "null"

        val commentJson =
            if (rating == -1) {
                "null"
            } else {
                comment
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?.let {
                        "\"${escapeJson(it)}\""
                    }
                    ?: "null"
            }

        return buildString {
            append("{")
            append("\"key\":\"${escapeJson(key)}\",")
            append("\"event_id\":\"${escapeJson(eventId)}\",")
            append("\"scheduled_time\":\"${escapeJson(scheduledTime)}\",")
            append("\"response_time\":$responseJson,")
            append("\"rating\":$rating,")
            append("\"latitude\":$latitude,")
            append("\"longitude\":$longitude,")
            append("\"app_version\":\"${escapeJson(appVersion)}\",")
            append("\"comment\":$commentJson,")
            append("\"wav_present\":false")
            append("}")
        }
    }

    private fun escapeJson(value: String): String =
        buildString(value.length + 8) {
            value.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (ch.code < 0x20) {
                            append("\\u%04x".format(ch.code))
                        } else {
                            append(ch)
                        }
                    }
                }
            }
        }
}
