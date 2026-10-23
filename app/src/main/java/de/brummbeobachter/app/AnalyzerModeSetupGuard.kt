package de.brummbeobachter.app

import org.json.JSONObject

/** Pure, non-mutating preflight. Full validation still occurs in the STOP controller. */
internal object AnalyzerModeSetupGuard {
    fun reason(legacy: JSONObject, direct48k: Boolean): String {
        val factor = legacy.optInt("decimation_factor", -1)
        val range = legacy.optInt("range", -1)
        val min = legacy.optInt("min_frequency", -1)
        val size = legacy.optInt("fft_size", -1)
        val hop = legacy.optInt("fft_hop", -1)
        val rate = legacy.optInt("direct_sample_rate", -1)
        if (direct48k) {
            if (rate !in DirectAnalyzerConfig.inputRates || !DirectAnalyzerConfig.valid(rate, factor)) return "decimation_factor=$factor bei direct_sample_rate=$rate"
            if (!AnalyzerTiming.validWidebandRange(min, range) || range > rate / 2) return "Frequenzbereich $min..$range Hz"
            if (size !in DirectAnalyzerConfig.fftSizes) return "fft_size=$size"
            if (hop !in DirectAnalyzerConfig.permissibleHops) return "fft_hop=$hop"
        } else {
            if (factor !in AnalyzerTiming.allowedDecimationFactors) return "decimation_factor=$factor (zulässig: ${AnalyzerTiming.allowedDecimationFactors})"
            if (!AnalyzerTiming.validRange(min, range)) return "Frequenzbereich $min..$range Hz"
            if (size !in AnalyzerTiming.allowedFftSizes) return "fft_size=$size"
            if (hop !in AnalyzerTiming.allowedFftHops) return "fft_hop=$hop"
        }
        return "Weitere Parameterprüfung erforderlich"
    }
    fun compatible(legacy: JSONObject, direct48k: Boolean): Boolean =
        reason(legacy, direct48k) == "Weitere Parameterprüfung erforderlich"
}
