package de.brummbeobachter.app

import java.util.Locale
import kotlin.math.log10

/** Pure diagnostics. Never modifies an audio buffer or a noise reference. */
internal object NoisePowerComparison {
    fun db(power: Double): String = when {
        !power.isFinite() || power < 0.0 -> "ungueltig"
        power == 0.0 -> "-Inf"
        else -> String.format(Locale.US, "%.1f", 10.0 * log10(power))
    }
    fun ratioDb(reference: Double, live: Double): String = when {
        !reference.isFinite() || !live.isFinite() || reference < 0.0 || live < 0.0 -> "ungueltig"
        reference == 0.0 && live == 0.0 -> "unbestimmt"
        reference == 0.0 -> "-Inf"
        live == 0.0 -> "+Inf"
        else -> String.format(Locale.US, "%+.1f", 10.0 * log10(reference / live))
    }
}
