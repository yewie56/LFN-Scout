package de.brummbeobachter.app

enum class AppPanel {
    MAIN,
    SEISMIC,
    SETTINGS,
    MORE,
    ANALYZER
}

object FaceliftLayoutPolicy {
    const val RADIUS_HISTOGRAM_COLUMNS = 2
    const val SCREEN_PADDING_DP = 7
    const val SECTION_GAP_DP = 3
    const val CARD_PADDING_DP = 7
    const val CARD_TITLE_GAP_DP = 2
    const val MAIN_SECTION_GAP_DP = 2
    const val MAIN_CARD_PADDING_DP = 5
    const val MAIN_CARD_TITLE_GAP_DP = 1
    const val FIELD_GAP_DP = 2
    const val RADIUS_GAP_DP = 2
    const val RATING_BUTTON_HEIGHT_DP = 50
}

object FaceliftThemePolicy {
    const val PRIMARY_HEX = "#1F8F8A"
    const val ACCENT_HEX = "#E0B000"
}
