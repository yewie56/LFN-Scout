package de.brummbeobachter.app

enum class WaterfallOutputFormat(
    val displayName: String
) {
    PNG("PNG"),
    JPG("JPG"),
    PNG_AND_JPG("PNG + JPG");

    companion object {
        fun fromStored(
            value: String?
        ): WaterfallOutputFormat =
            entries.firstOrNull {
                it.name == value
            } ?: PNG
    }
}
