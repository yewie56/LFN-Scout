package de.brummbeobachter.app

object AudioArchiveCompatibilityPolicy {
    const val CURRENT_RELATIVE_PATH = "Download/LFN-Scout/"
    const val LEGACY_RELATIVE_PATH = "Download/BrummBeobachter/"

    enum class DeleteResult {
        DELETED,
        ALREADY_ABSENT,
        FAILED
    }

    fun relativePaths(): List<String> = listOf(
        CURRENT_RELATIVE_PATH,
        LEGACY_RELATIVE_PATH
    )

    fun shouldRemoveMetadataAfterDelete(deleteResult: DeleteResult): Boolean =
        deleteResult != DeleteResult.FAILED
}
