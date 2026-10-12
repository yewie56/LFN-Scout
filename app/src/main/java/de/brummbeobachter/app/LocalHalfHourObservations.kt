package de.brummbeobachter.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime

/** Bounded, local-only observation index. No keys, comments or geolocation stored. */
internal object LocalHalfHourObservations {
    private const val PREFS = "lfn_local_halfhour_observations_v1"
    private const val KEY = "events"
    private const val MAX_ITEMS = 4000
    private const val RETAIN_MS = 45L * 24 * 60 * 60 * 1000
    data class Event(val id: String, val timeMs: Long, val rating: Int)
    data class Summary(val mean: Double?, val count: Int, val minimum: Int?, val maximum: Int?, val unavailable: Int)

    /** Half-open UTC epoch-ms windows. First occurrence of an event ID wins. */
    fun summarize(events: List<Event>, startMs: Long, endMs: Long): Summary {
        require(endMs > startMs) { "Invalid half-hour interval" }
        val inWindow = events.filter { it.timeMs >= startMs && it.timeMs < endMs }.distinctBy { it.id }
        val valid = inWindow.map { it.rating }.filter { it in 0..5 }
        return Summary(valid.takeIf { it.isNotEmpty() }?.average(), valid.size,
            valid.minOrNull(), valid.maxOrNull(), inWindow.count { it.rating == -1 })
    }

    /** Use planned time only for -1, otherwise actual response time. Invalid responses are rejected. */
    fun fromPayload(payload: ObservationPayload): Event? {
        if (payload.rating !in -1..5 || payload.eventId.isBlank()) return null
        val whenText = if (payload.rating == -1) payload.scheduledTime else payload.responseTime
        val timeMs = runCatching { OffsetDateTime.parse(whenText ?: return null).toInstant().toEpochMilli() }.getOrNull() ?: return null
        return Event(payload.eventId, timeMs, payload.rating)
    }

    @Synchronized fun record(context: Context, payload: ObservationPayload) {
        val event = fromPayload(payload) ?: run {
            DebugLogger.log(context, "LOCAL_OBSERVATION_ERROR", "Invalid event ID, rating or timestamp")
            return
        }
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val records = read(preferences.getString(KEY, null)).toMutableList()
        if (records.none { it.id == event.id }) records.add(event)
        val cutoff = System.currentTimeMillis() - RETAIN_MS
        val trimmed = records.filter { it.timeMs >= cutoff }.takeLast(MAX_ITEMS)
        val array = JSONArray()
        trimmed.forEach { array.put(JSONObject().put("id", it.id).put("t", it.timeMs).put("r", it.rating)) }
        if (!preferences.edit().putString(KEY, array.toString()).commit()) {
            DebugLogger.log(context, "LOCAL_OBSERVATION_ERROR", "Unable to persist observation index")
        }
    }

    @Synchronized fun forBucket(context: Context, startMs: Long, endMs: Long): Summary {
        val text = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        return summarize(read(text), startMs, endMs)
    }

    private fun read(text: String?): List<Event> {
        if (text.isNullOrEmpty()) return emptyList()
        return runCatching {
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optString("id")
                    val rating = item.optInt("r", -2)
                    if (id.isNotBlank() && rating in -1..5) add(Event(id, item.optLong("t"), rating))
                }
            }
        }.getOrDefault(emptyList())
    }
}
