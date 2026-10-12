import de.brummbeobachter.app.AudioArchiveCompatibilityPolicy

fun main() {
    check(AudioArchiveCompatibilityPolicy.relativePaths() == listOf(
        "Download/LFN-Scout/",
        "Download/BrummBeobachter/"
    )) { "Current and legacy public paths must both be searched" }

    check(AudioArchiveCompatibilityPolicy.shouldRemoveMetadataAfterDelete(
        deleteResult = AudioArchiveCompatibilityPolicy.DeleteResult.DELETED
    ))
    check(AudioArchiveCompatibilityPolicy.shouldRemoveMetadataAfterDelete(
        deleteResult = AudioArchiveCompatibilityPolicy.DeleteResult.ALREADY_ABSENT
    )) { "Already absent WAV must count as successful cleanup" }
    check(!AudioArchiveCompatibilityPolicy.shouldRemoveMetadataAfterDelete(
        deleteResult = AudioArchiveCompatibilityPolicy.DeleteResult.FAILED
    )) { "Real delete failure must retain metadata for retry" }

    println("AudioArchiveCompatibilitySelfTest PASS")
}
