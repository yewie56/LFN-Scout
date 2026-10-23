package de.brummbeobachter.app

/** Maps the mode-specific selection to the indicator text; never consults the legacy global activeId. */
internal object AnalyzerModeSetupStatus {
    fun label(entries: List<Pair<String, String>>, modeSelectedId: String?): String {
        val name = entries.firstOrNull { it.first == modeSelectedId }?.second
        return if (name == null) "Setup: keines" else "Setup: $name (zugeordnet)"
    }
}
