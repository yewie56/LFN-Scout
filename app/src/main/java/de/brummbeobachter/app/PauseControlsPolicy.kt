package de.brummbeobachter.app

object PauseControlsPolicy {
    const val DISABLE = -1

    val rows: List<List<Int>> =
        listOf(
            listOf(1, 2, 4, 8),
            listOf(12, 24, 72, DISABLE)
        )
}
