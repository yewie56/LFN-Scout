
package de.brummbeobachter.app

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AudioFileNamingPolicy {

    const val MAX_RATING_AGE_MINUTES = 30L

    private val fileTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    fun selectRatingTag(
        observations: List<RecentObservation>,
        recordingStart: OffsetDateTime
    ): String {
        val valid =
            observations
                .asSequence()
                .filter {
                    it.rating in 0..5
                }
                .mapNotNull {
                    observation ->
                    val text =
                        observation.responseTime
                            ?: observation.scheduledTime

                    val time =
                        runCatching {
                            OffsetDateTime.parse(
                                text
                            )
                        }.getOrNull()
                            ?: return@mapNotNull null

                    Pair(
                        observation.rating,
                        time
                    )
                }
                .filter {
                    (_, time) ->
                    !time.isAfter(
                        recordingStart
                    )
                }
                .map {
                    (rating, time) ->
                    Triple(
                        rating,
                        time,
                        Duration.between(
                            time,
                            recordingStart
                        )
                    )
                }
                .filter {
                    (_, _, age) ->
                    !age.isNegative &&
                        age <=
                            Duration.ofMinutes(
                                MAX_RATING_AGE_MINUTES
                            )
                    // exactly 30 minutes counts
                }
                .maxByOrNull {
                    (_, time, _) ->
                    time.toInstant()
                }

        if (
            valid == null
        ) {
            return "RX"
        }

        return "R${valid.first}"
    }

    fun buildLocalWavFileName(
        observerId: String,
        recordingStart: OffsetDateTime,
        gainDb: Int,
        audioSource: String,
        observations: List<RecentObservation>
    ): String {
        val safeObserverId =
            observerId
                .trim()
                .uppercase()
                .replace(
                    Regex(
                        "[^A-Z0-9_-]"
                    ),
                    "_"
                )
                .ifBlank {
                    "BBUNKNOWN"
                }

        val localTime =
            recordingStart
                .atZoneSameInstant(
                    ZoneId.systemDefault()
                )
                .format(
                    fileTimeFormatter
                )

        val safeSource =
            if (
                audioSource.uppercase() ==
                "MIC"
            ) {
                "MIC"
            } else {
                "UNP"
            }

        val ratingTag =
            selectRatingTag(
                observations =
                    observations,
                recordingStart =
                    recordingStart
            )

        return buildString {
            append(
                safeObserverId
            )
            append("_")
            append(
                localTime
            )
            append("_G")
            append(
                gainDb.coerceIn(
                    0,
                    50
                )
            )
            append("_")
            append(
                safeSource
            )
            append("_")
            append(
                ratingTag
            )
            append(".wav")
        }
    }
    fun buildRollingWavFileName(
        observerId: String,
        start: OffsetDateTime,
        end: OffsetDateTime,
        gainDb: Int,
        audioSource: String,
        observations: List<RecentObservation>
    ): String {
        val safeObserverId = observerId.trim().uppercase()
            .replace(Regex("[^A-Z0-9_-]"), "_")
            .ifBlank { "BBUNKNOWN" }
        val startLocal = start.atZoneSameInstant(ZoneId.systemDefault()).format(fileTimeFormatter)
        val endLocal = end.atZoneSameInstant(ZoneId.systemDefault()).format(fileTimeFormatter)
        val safeSource = if (audioSource.uppercase() == "MIC") "MIC" else "UNP"
        val ratingTag = selectRatingTag(observations, end)
        return "${safeObserverId}_ROLL_${startLocal}_bis_${endLocal}_G${gainDb.coerceIn(0, 50)}_${safeSource}_${ratingTag}.wav"
    }

}
