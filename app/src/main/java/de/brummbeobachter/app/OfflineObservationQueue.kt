package de.brummbeobachter.app

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object OfflineObservationQueue {

    private const val FILE_NAME =
        "pending_observations.json"

    private val lock = Any()

    fun enqueue(
        context: Context,
        payload: ObservationPayload
    ): Int =
        synchronized(lock) {
            val current =
                readLocked(context)

            val updated =
                OfflineObservationQueuePolicy.upsert(
                    current,
                    OfflineQueueItem.fromPayload(payload)
                )

            writeLocked(
                context,
                updated
            )

            updated.size
        }.also {
            OfflineRetryScheduler.syncWithQueue(
                context
            )
        }

    fun remove(
        context: Context,
        eventId: String
    ): Int =
        synchronized(lock) {
            val current =
                readLocked(context)

            val updated =
                OfflineObservationQueuePolicy
                    .removeByEventId(
                        current,
                        eventId
                    )

            writeLocked(
                context,
                updated
            )

            updated.size
        }.also {
            OfflineRetryScheduler.syncWithQueue(
                context
            )
        }

    fun snapshot(
        context: Context
    ): List<OfflineQueueItem> =
        synchronized(lock) {
            readLocked(context)
        }

    fun size(
        context: Context
    ): Int =
        synchronized(lock) {
            readLocked(context).size
        }

    private fun readLocked(
        context: Context
    ): List<OfflineQueueItem> {
        val file =
            queueFile(context)

        if (!file.exists()) {
            return emptyList()
        }

        return runCatching {
            val array =
                JSONArray(
                    file.readText(
                        Charsets.UTF_8
                    )
                )

            buildList {
                for (index in 0 until array.length()) {
                    val obj =
                        array.getJSONObject(index)

                    add(
                        OfflineQueueItem(
                            eventId =
                                obj.getString("event_id"),
                            scheduledTime =
                                obj.getString("scheduled_time"),
                            responseTime =
                                if (
                                    obj.isNull("response_time")
                                ) {
                                    null
                                } else {
                                    obj.getString(
                                        "response_time"
                                    )
                                },
                            rating =
                                obj.getInt("rating"),
                            latitude =
                                obj.getDouble("latitude"),
                            longitude =
                                obj.getDouble("longitude"),
                            appVersion =
                                obj.getString("app_version"),
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
                            createdAtEpochMs =
                                obj.getLong(
                                    "created_at_epoch_ms"
                                )
                        )
                    )
                }
            }
        }.getOrElse { error ->
            DebugLogger.log(
                context.applicationContext,
                "OFFLINE_QUEUE_READ_FAIL",
                "message=${error.message ?: error.javaClass.simpleName}"
            )
            emptyList()
        }
    }

    private fun writeLocked(
        context: Context,
        items: List<OfflineQueueItem>
    ) {
        val appContext =
            context.applicationContext

        val array =
            JSONArray()

        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put(
                        "event_id",
                        item.eventId
                    )
                    put(
                        "scheduled_time",
                        item.scheduledTime
                    )
                    if (item.responseTime == null) {
                        put(
                            "response_time",
                            JSONObject.NULL
                        )
                    } else {
                        put(
                            "response_time",
                            item.responseTime
                        )
                    }
                    put(
                        "rating",
                        item.rating
                    )
                    put(
                        "latitude",
                        item.latitude
                    )
                    put(
                        "longitude",
                        item.longitude
                    )
                    put(
                        "app_version",
                        item.appVersion
                    )
                    if (item.comment == null) {
                        put(
                            "comment",
                            JSONObject.NULL
                        )
                    } else {
                        put(
                            "comment",
                            item.comment
                        )
                    }
                    put(
                        "created_at_epoch_ms",
                        item.createdAtEpochMs
                    )
                }
            )
        }

        val atomicFile =
            AtomicFile(
                queueFile(appContext)
            )

        val output =
            atomicFile.startWrite()

        try {
            output.write(
                array.toString()
                    .toByteArray(Charsets.UTF_8)
            )
            output.flush()
            atomicFile.finishWrite(output)
        } catch (error: Exception) {
            atomicFile.failWrite(output)
            throw error
        }
    }

    private fun queueFile(
        context: Context
    ): File =
        File(
            context.applicationContext.filesDir,
            FILE_NAME
        )
}
