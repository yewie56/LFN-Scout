package de.brummbeobachter.app

import org.json.JSONArray
import org.json.JSONObject

data class RadiusHistogramBlock(
    val startTime: String,
    val endTime: String,
    val ratingCounts: List<Int>,
    val noResponseCount: Int
)

data class RadiusRawObservation(
    val observerId: String,
    val rating: Int,
    val comment: String? = null,
    val scheduledTime: String,
    val responseTime: String?,
    val latitude: Double,
    val longitude: Double
)

data class RadiusHistogramResult(
    val success: Boolean,
    val message: String,
    val keyRevoked: Boolean = false,
    val centerSource: String = "",
    val radiusKm: Int = 0,
    val observerCount: Int = 0,
    val observationCount: Int = 0,
    val blocks: List<RadiusHistogramBlock> = emptyList(),
    val observations: List<RadiusRawObservation> = emptyList()
)

object RadiusHistogramParser {

    fun parse(
        httpCode: Int,
        body: String
    ): RadiusHistogramResult {
        if (httpCode !in 200..299) {
            val message =
                runCatching {
                    JSONObject(body)
                        .optString(
                            "error",
                            "HTTP $httpCode"
                        )
                }.getOrDefault(
                    "HTTP $httpCode"
                )

            return RadiusHistogramResult(
                success = false,
                message = message,
                keyRevoked =
                    message == "key_revoked"
            )
        }

        return runCatching {
            val root =
                JSONObject(body)

            if (!root.optBoolean("ok", false)) {
                return@runCatching RadiusHistogramResult(
                    success = false,
                    message =
                        root.optString(
                            "error",
                            "Ungültige Serverantwort"
                        )
                )
            }

            val blocksJson =
                root.optJSONArray("blocks")
                    ?: JSONArray()

            val blocks =
                buildList {
                    for (
                        index in 0
                        until blocksJson.length()
                    ) {
                        val block =
                            blocksJson
                                .getJSONObject(index)

                        val countsJson =
                            block.optJSONArray(
                                "counts"
                            ) ?: JSONArray()

                        val counts =
                            (0..5).map { rating ->
                                countsJson
                                    .optInt(
                                        rating,
                                        0
                                    )
                            }

                        add(
                            RadiusHistogramBlock(
                                startTime =
                                    block.optString(
                                        "start_time"
                                    ),
                                endTime =
                                    block.optString(
                                        "end_time"
                                    ),
                                ratingCounts = counts,
                                noResponseCount =
                                    block.optInt(
                                        "no_response",
                                        0
                                    )
                            )
                        )
                    }
                }

            val observationsJson =
                root.optJSONArray(
                    "observations"
                ) ?: JSONArray()

            val observations =
                buildList {
                    for (
                        index in 0
                        until observationsJson.length()
                    ) {
                        val item =
                            observationsJson
                                .getJSONObject(index)

                        val responseTime =
                            if (
                                item.isNull(
                                    "response_time"
                                )
                            ) {
                                null
                            } else {
                                item.optString(
                                    "response_time"
                                )
                            }

                        add(
                            RadiusRawObservation(
                                observerId =
                                    item.optString(
                                        "observer_id"
                                    ),
                                rating =
                                    item.optInt(
                                        "rating",
                                        -1
                                    ),
                                comment =
                                    if (
                                        !item.has("comment") ||
                                        item.isNull("comment")
                                    ) {
                                        null
                                    } else {
                                        item.optString(
                                            "comment"
                                        ).takeIf {
                                            it.isNotBlank()
                                        }
                                    },
                                scheduledTime =
                                    item.optString(
                                        "scheduled_time"
                                    ),
                                responseTime =
                                    responseTime,
                                latitude =
                                    item.optDouble(
                                        "latitude",
                                        0.0
                                    ),
                                longitude =
                                    item.optDouble(
                                        "longitude",
                                        0.0
                                    )
                            )
                        )
                    }
                }

            RadiusHistogramResult(
                success = true,
                message = "",
                centerSource =
                    root.optString(
                        "center_source"
                    ),
                radiusKm =
                    root.optInt(
                        "radius_km"
                    ),
                observerCount =
                    root.optInt(
                        "observer_count"
                    ),
                observationCount =
                    root.optInt(
                        "observation_count"
                    ),
                blocks = blocks,
                observations =
                    observations
            )
        }.getOrElse { error ->
            RadiusHistogramResult(
                success = false,
                message =
                    "Auswertungsantwort konnte nicht gelesen werden: " +
                        (
                            error.message
                                ?: error.javaClass.simpleName
                            )
            )
        }
    }
}
