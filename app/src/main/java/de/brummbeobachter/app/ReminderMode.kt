package de.brummbeobachter.app

enum class ReminderMode {
    VERY_NOTICEABLE,
    ALARM;

    companion object {
        fun fromStored(value: String?): ReminderMode =
            entries.firstOrNull { it.name == value } ?: VERY_NOTICEABLE
    }
}
