package de.brummbeobachter.app

object ReminderResponseTimeoutPolicy {

    fun timeoutTriggerEpochMs(
        reminderEpochMs: Long,
        responseWindowMinutes: Int
    ): Long {
        require(responseWindowMinutes > 0) {
            "responseWindowMinutes must be > 0"
        }
        return reminderEpochMs +
            responseWindowMinutes * 60L * 1000L
    }
}
