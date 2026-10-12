package de.brummbeobachter.app

object OfflineObservationQueuePolicy {

    fun isQueueableRating(rating: Int): Boolean =
        rating in -1..5

    fun upsert(
        current: List<OfflineQueueItem>,
        item: OfflineQueueItem
    ): List<OfflineQueueItem> {
        require(
            isQueueableRating(item.rating)
        ) {
            "rating must be between -1 and 5"
        }

        return current
            .filterNot {
                it.eventId == item.eventId
            }
            .plus(item)
            .sortedBy {
                it.createdAtEpochMs
            }
    }

    fun removeByEventId(
        current: List<OfflineQueueItem>,
        eventId: String
    ): List<OfflineQueueItem> =
        current.filterNot {
            it.eventId == eventId
        }
}
