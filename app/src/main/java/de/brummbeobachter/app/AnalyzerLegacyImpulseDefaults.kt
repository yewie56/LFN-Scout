package de.brummbeobachter.app

import org.json.JSONObject

/** Adds v0.5.119 impulse defaults only to a temporary 8-kHz draft.
 *  Never modify on-disk named setups; Direct mode must not receive impulse keys.
 *  Existing explicitly stored values (including invalid ones) are never overwritten.
 */
internal object AnalyzerLegacyImpulseDefaults {
    fun forMode(saved: JSONObject, direct48k: Boolean): JSONObject {
        val draft = JSONObject(saved.toString())
        if (!direct48k) {
            if (!draft.has("filter_impulse_mode")) draft.put("filter_impulse_mode", false)
            if (!draft.has("filter_impulse_threshold")) draft.put("filter_impulse_threshold", 3)
        }
        return draft
    }
}
