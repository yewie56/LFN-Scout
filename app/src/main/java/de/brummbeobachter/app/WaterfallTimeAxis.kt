package de.brummbeobachter.app

import java.util.TimeZone

/** Calendar-time labels for a chronological waterfall (oldest first).
 * One timestamp is stored for each rendered row. Does not generate invented time rows.
 * No Android dependency: can be tested with a plain JVM Kotlin compiler.
 */
internal object WaterfallTimeAxis {
    private val stepsMinutes = intArrayOf(1, 2, 5, 10, 20, 30, 60)
    data class Tick(val ageRows: Float, val epochMillis: Long)

    fun intervalMinutes(historySeconds: Int): Int {
        val seconds = historySeconds.coerceAtLeast(0).toLong()
        return stepsMinutes.firstOrNull { seconds <= it * 60L * 4L } ?: stepsMinutes.last()
    }

    /** Produces the actual newest-row timestamp plus up to four whole-clock ticks.
     * Timestamp gaps caused by PAUSE must not be stretched into fake waterfall time.
     * Alignment to local minute boundaries accounts for fractional-hour UTC offsets.
     */
    fun ticks(rowMillis: List<Long>, historySeconds: Int, capacity: Int,
              rowIntervalMs: Int, zone: TimeZone = TimeZone.getDefault()): List<Tick> {
        if (capacity < 1 || rowMillis.isEmpty()) return emptyList()
        val stepMillis = intervalMinutes(historySeconds) * 60_000L
        val newestFirst = rowMillis.takeLast(capacity).asReversed()
        // One visible timestamp immediately after start, even before the first full minute.
        val candidates = ArrayList<Tick>(5)
        candidates.add(Tick(0f, newestFirst.first()))
        val maxAdjacentMs = maxOf(5_000L, rowIntervalMs.coerceAtLeast(1) * 3L)
        for (age in 0 until newestFirst.lastIndex) {
            val recent = newestFirst[age]
            val older = newestFirst[age + 1]
            val gap = recent - older
            if (gap <= 0L || gap > maxAdjacentMs) continue // paused, clock changed, or invalid ordering
            val offset = zone.getOffset(recent).toLong()
            val boundary = Math.floorDiv(recent + offset, stepMillis) * stepMillis - offset
            if (boundary <= older || boundary > recent) continue
            // The leading current-time label already covers this same minute.
            if (newestFirst.first() - boundary < stepMillis / 8L) continue
            // Row ages match the waterfall raster: newest = 0, next row = 1, etc.
            val fraction = (recent - boundary).toFloat() / gap
            candidates.add(Tick(age + fraction, boundary))
            if (candidates.size == 5) break
        }
        return candidates
    }
}
