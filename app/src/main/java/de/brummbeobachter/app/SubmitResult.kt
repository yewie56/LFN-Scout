package de.brummbeobachter.app

import org.json.JSONObject

data class SubmitResult(
    val success: Boolean,
    val duplicate: Boolean = false,
    val observerId: String? = null,
    val retryable: Boolean = false,
    val keyRevoked: Boolean = false,
    val message: String
)

object SubmitResultParser {

    fun parse(httpCode: Int, body: String): SubmitResult {
        val json =
            runCatching {
                JSONObject(body)
            }.getOrNull()

        val ok =
            json?.optBoolean("ok", false) == true
        val duplicate =
            json?.optBoolean("duplicate", false) == true
        val observerId =
            json
                ?.optString("observer_id")
                ?.takeIf {
                    it.isNotBlank()
                }

        if (httpCode in 200..299 && ok) {
            return SubmitResult(
                success = true,
                duplicate = duplicate,
                observerId = observerId,
                message =
                    if (duplicate) {
                        "Bereits übertragen"
                    } else {
                        "Übertragung erfolgreich"
                    }
            )
        }

        val error =
            json
                ?.optString("error")
                ?.takeIf {
                    it.isNotBlank()
                }

        val retryable =
            httpCode == 408 ||
                httpCode == 429 ||
                httpCode in 500..599

        return SubmitResult(
            success = false,
            retryable = retryable,
            keyRevoked =
                error == "key_revoked",
            message = buildString {
                append(
                    "Übertragung fehlgeschlagen (HTTP $httpCode)"
                )
                if (error != null) {
                    append(": $error")
                }
            }
        )
    }
}
