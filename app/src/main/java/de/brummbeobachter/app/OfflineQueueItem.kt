package de.brummbeobachter.app

data class OfflineQueueItem(
    val eventId: String,
    val scheduledTime: String,
    val responseTime: String?,
    val rating: Int,
    val latitude: Double,
    val longitude: Double,
    val appVersion: String,
    val comment: String? = null,
    val createdAtEpochMs: Long
) {
    fun toPayload(key: String): ObservationPayload =
        ObservationPayload(
            key = key,
            eventId = eventId,
            scheduledTime = scheduledTime,
            responseTime = responseTime,
            rating = rating,
            latitude = latitude,
            longitude = longitude,
            appVersion = appVersion,
            comment = comment
        )

    companion object {
        fun fromPayload(
            payload: ObservationPayload,
            createdAtEpochMs: Long =
                System.currentTimeMillis()
        ): OfflineQueueItem =
            OfflineQueueItem(
                eventId = payload.eventId,
                scheduledTime = payload.scheduledTime,
                responseTime = payload.responseTime,
                rating = payload.rating,
                latitude = payload.latitude,
                longitude = payload.longitude,
                appVersion = payload.appVersion,
                comment = payload.comment,
                createdAtEpochMs = createdAtEpochMs
            )
    }
}
