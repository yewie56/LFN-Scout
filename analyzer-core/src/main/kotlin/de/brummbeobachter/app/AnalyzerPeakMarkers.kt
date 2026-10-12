package de.brummbeobachter.app

/** Adjacent-bin local maxima; excludes DC, avoids returning adjacent bins for one peak. */
object AnalyzerPeakMarkers {
    data class Peak(val frequencyHz: Double, val dbFs: Double, val bin: Int)
    fun find(frequenciesHz: DoubleArray, dbFs: DoubleArray, minHz: Int, maxHz: Int, count: Int): List<Peak> {
        if (count <= 0 || frequenciesHz.size != dbFs.size) return emptyList()
        val candidates = (1 until dbFs.lastIndex).filter { i ->
            frequenciesHz[i] >= minHz && frequenciesHz[i] <= maxHz &&
                dbFs[i].isFinite() && dbFs[i] > -180.0 &&
                dbFs[i] >= dbFs[i-1] && dbFs[i] > dbFs[i+1]
        }.sortedByDescending { dbFs[it] }
        val selected = ArrayList<Peak>(count.coerceAtMost(2))
        for (i in candidates) {
            if (selected.all { kotlin.math.abs(it.bin-i) >= 3 }) {
                selected.add(Peak(frequenciesHz[i],dbFs[i],i))
                if (selected.size >= count.coerceAtMost(2)) break
            }
        }
        return selected
    }
}
