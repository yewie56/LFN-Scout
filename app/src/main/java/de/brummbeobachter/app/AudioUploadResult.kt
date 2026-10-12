package de.brummbeobachter.app

data class AudioUploadResult(
    val success: Boolean,
    val keyRevoked: Boolean = false,
    val storagePath: String? = null,
    val message: String
)
