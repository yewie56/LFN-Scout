package de.brummbeobachter.app

import org.json.JSONObject
import java.math.BigDecimal

/** Phase 3O: deterministic legacy comparison without Double precision loss.
 * Comparing keys is separate from comparing values: absence is never a default.
 */
internal object AnalyzerSetupExactComparison {
    fun same(a: Any, b: Any): Boolean = when {
        a is Number && b is Number -> try {
            BigDecimal(a.toString()).compareTo(BigDecimal(b.toString())) == 0
        } catch (_: NumberFormatException) { false }
        a is Number || b is Number -> false
        else -> a == b
    }

    fun keys(o: JSONObject): Set<String> = buildSet {
        val it = o.keys()
        while (it.hasNext()) add(it.next())
    }

    fun sameComplete(a: JSONObject, b: JSONObject): Boolean =
        keys(a) == keys(b) && keys(a).all { key ->
            a.isNull(key) == b.isNull(key) &&
                (a.isNull(key) || same(a.get(key), b.get(key)))
        }
}
