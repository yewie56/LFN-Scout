package de.brummbeobachter.app

/** Pure geometry for symmetrically compressing a MON passband at both range edges.
 * A gesture retains its initial [width] while [virtualCenter] may move beyond the
 * display boundaries. Clipping the translated edges automatically re-expands the
 * band on the return gesture. No filter with zero/negative width is emitted.
 */
internal object MonBandEdgeGesture {
    /** Recover the virtual (unclipped) drag position from the squeezed band.
     * This is needed when the user releases at a boundary and starts a new gesture. */
    fun resumeCenter(minHz: Double, maxHz: Double, lowHz: Double, highHz: Double, targetWidth: Double): Double {
        val half = targetWidth / 2.0
        return when {
            lowHz <= minHz + 1e-6 && highHz < maxHz - 1e-6 -> highHz - half
            highHz >= maxHz - 1e-6 && lowHz > minHz + 1e-6 -> lowHz + half
            else -> (lowHz + highHz) / 2.0
        }
    }

    fun edges(minHz: Double, maxHz: Double, virtualCenter: Double, width: Double): Pair<Double, Double> {
        require(minHz.isFinite() && maxHz.isFinite() && maxHz - minHz >= 0.5)
        require(virtualCenter.isFinite() && width.isFinite() && width >= 0.5)
        val safeWidth = width.coerceAtMost(maxHz - minHz)
        val half = safeWidth / 2.0
        val lo = (virtualCenter - half).coerceIn(minHz, maxHz)
        val hi = (virtualCenter + half).coerceIn(minHz, maxHz)
        if (hi - lo >= 0.5) return lo to hi
        return if (virtualCenter <= (minHz + maxHz) / 2.0)
            minHz to minHz + 0.5 else maxHz - 0.5 to maxHz
    }
}
