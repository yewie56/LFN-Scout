package de.brummbeobachter.app

/** Testable, allocation-free constraints for direct MON user interaction. */
internal object MonInteractionPolicy {
    fun agcApplicable(centerHz: Double): Boolean = centerHz.isFinite() && centerHz >= 100.0

    fun pinchedWidth(startWidthHz: Double, scale: Double, centerHz: Double,
                     minHz: Double, maxHz: Double): Double {
        val available = 2.0 * minOf(centerHz - minHz, maxHz - centerHz)
        if (!available.isFinite() || available < 0.5 || !scale.isFinite() || scale <= 0.0)
            return startWidthHz
        return (startWidthHz * scale).coerceIn(0.5, available)
    }
}
