package de.brummbeobachter.app

import org.json.JSONObject

data class OwnObservation(
    val observerId: String,
    val eventId: String,
    val scheduledTime: String,
    val responseTime: String?,
    val rating: Int,
    val comment: String?,
    val latitude: Double,
    val longitude: Double,
    val appVersion: String,
    val wavPresent: Boolean,
    val wavPath: String?
)

data class OwnObservationsPageResult(
    val success: Boolean,
    val observerId: String = "",
    val items: List<OwnObservation> = emptyList(),
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val keyRevoked: Boolean = false,
    val message: String
)

object OwnObservationsParser {

    fun parse(
        httpCode: Int,
        body: String
    ): OwnObservationsPageResult {
        if (httpCode !in 200..299) {
            val error =
                runCatching {
                    JSONObject(body)
                        .optString("error")
                }.getOrDefault("")

            return OwnObservationsPageResult(
                success = false,
                keyRevoked =
                    error == "key_revoked",
                message =
                    "Abruf fehlgeschlagen (HTTP $httpCode)" +
                        if (error.isBlank()) {
                            ""
                        } else {
                            ": $error"
                        }
            )
        }

        return runCatching {
            val root =
                JSONObject(body)

            if (!root.optBoolean("ok", false)) {
                return@runCatching OwnObservationsPageResult(
                    success = false,
                    message = "Ungültige Serverantwort"
                )
            }

            val observerId =
                root.optString("observer_id")

            val array =
                root.optJSONArray("observations")

            val items =
                buildList {
                    if (array != null) {
                        for (
                            index in 0
                            until array.length()
                        ) {
                            val obj =
                                array.optJSONObject(index)
                                    ?: continue

                            add(
                                OwnObservation(
                                    observerId =
                                        obj.optString(
                                            "observer_id",
                                            observerId
                                        ),
                                    eventId =
                                        obj.optString(
                                            "event_id"
                                        ),
                                    scheduledTime =
                                        obj.optString(
                                            "scheduled_time"
                                        ),
                                    responseTime =
                                        if (
                                            obj.isNull(
                                                "response_time"
                                            )
                                        ) {
                                            null
                                        } else {
                                            obj.optString(
                                                "response_time"
                                            ).takeIf {
                                                it.isNotBlank()
                                            }
                                        },
                                    rating =
                                        obj.optInt(
                                            "rating",
                                            -1
                                        ),
                                    comment =
                                        if (
                                            !obj.has("comment") ||
                                            obj.isNull("comment")
                                        ) {
                                            null
                                        } else {
                                            obj.optString(
                                                "comment"
                                            ).takeIf {
                                                it.isNotBlank()
                                            }
                                        },
                                    latitude =
                                        obj.optDouble(
                                            "latitude",
                                            0.0
                                        ),
                                    longitude =
                                        obj.optDouble(
                                            "longitude",
                                            0.0
                                        ),
                                    appVersion =
                                        obj.optString(
                                            "app_version"
                                        ),
                                    wavPresent =
                                        obj.optBoolean(
                                            "wav_present",
                                            false
                                        ),
                                    wavPath =
                                        if (
                                            !obj.has("wav_path") ||
                                            obj.isNull("wav_path")
                                        ) {
                                            null
                                        } else {
                                            obj.optString(
                                                "wav_path"
                                            ).takeIf {
                                                it.isNotBlank()
                                            }
                                        }
                                )
                            )
                        }
                    }
                }

            OwnObservationsPageResult(
                success = true,
                observerId = observerId,
                items = items,
                hasMore =
                    root.optBoolean(
                        "has_more",
                        false
                    ),
                nextOffset =
                    root.optInt(
                        "next_offset",
                        items.size
                    ),
                message = "Abruf erfolgreich"
            )
        }.getOrElse { error ->
            OwnObservationsPageResult(
                success = false,
                message =
                    "Serverantwort konnte nicht gelesen werden: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )
            )
        }
    }
}

data class AllOwnObservationsResult(
    val success: Boolean,
    val observerId: String = "",
    val items: List<OwnObservation> = emptyList(),
    val keyRevoked: Boolean = false,
    val message: String
)
