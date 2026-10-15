package de.brummbeobachter.app

/** Public downloads are separated at build time. App-private data is already separated
 * by the TEST applicationId suffix; no REGULAR storage is migrated or read by TEST.
 * Android 10+ exports use MediaStore with these relative paths.
 * The user-facing Files app calls this parent "Downloads"; Android identifies it as
 * Environment.DIRECTORY_DOWNLOADS (normally the relative path "Download/").
 */
internal object PublicDataDirectory {
    val isTest: Boolean = BuildConfig.APPLICATION_ID.endsWith(".githubdebug")
    val folder: String = if (isTest) "lfn-scout-test" else "LFN-Scout"
    val relativePath: String = "Download/$folder/"
    val setupFolder: String = "$folder/Setups/"
    val diagnosticFolder: String = "$folder/Diagnostics/"
    val localSaveFolder: String = "$folder/LocalSaves/"
}
