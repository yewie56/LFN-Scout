package de.brummbeobachter.app

/** Independent parser of rendered one-second CSV, used before and after MediaStore write. */
internal object RollingEpochCsvIntegrity {
    data class Coverage(val distinctSeconds: Int, val missingSeconds: Int)

    fun verify(content: String, bucketMs: Long, expectedRows: Int): Coverage {
        require(bucketMs >= 0 && bucketMs % 1_800_000L == 0L) { "Invalid half-hour boundary" }
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.isNotEmpty() && lines.first().startsWith("epoch_start_ms;")) { "Invalid epoch CSV header" }
        val header = lines.first().split(';')
        require(header.size >= 3 && header[0] == "epoch_start_ms" &&
            header[1] == "overall_dbfs" && header.last() == "reference") { "Invalid epoch CSV columns" }
        require(header.drop(2).dropLast(1).withIndex().all { (i, name) ->
            name == "filter_${i + 1}_dbfs"
        }) { "Invalid filter column order" }
        val rows = lines.drop(1)
        require(rows.size == expectedRows) { "CSV rows ${rows.size} != $expectedRows" }
        val seconds = HashSet<Long>()
        for (row in rows) {
            val fields = row.split(';')
            require(fields.size == header.size) { "Invalid epoch CSV field count" }
            require(fields.last() == "PRE_GAIN_PCM_DIAGNOSTIC") { "Invalid epoch CSV reference" }
            for (value in fields.drop(1).dropLast(1)) {
                if (value.isNotEmpty()) {
                    require(value.toDoubleOrNull()?.isFinite() == true) { "Invalid/nonfinite FFT value" }
                }
            }
            val epoch = fields[0].toLongOrNull() ?: error("Invalid epoch timestamp")
            require(epoch % 1000L == 0L) { "Unaligned FFT epoch $epoch" }
            require(epoch >= bucketMs && epoch < bucketMs + 1_800_000L) { "Epoch outside half-hour" }
            require(seconds.add(epoch)) { "Duplicate epoch $epoch" }
        }
        return Coverage(seconds.size, 1800 - seconds.size)
    }
}
