package de.brummbeobachter.app

import android.content.Context

/** Local-only recording/statistics settings; does not change existing Supabase payloads. */
internal object LocalAudioSaveSettings {
    const val MIN_SECONDS = 30
    const val MAX_SECONDS = 600
    const val DEFAULT_SECONDS = 120
    data class Values(val durationSeconds: Int = DEFAULT_SECONDS, val minHz: Int = 2, val maxHz: Int = 25, val exportWavFilterCsv: Boolean = false) {
        fun validate() {
            require(durationSeconds in MIN_SECONDS..MAX_SECONDS)
            require(minHz in 1..199 && maxHz in 2..200 && minHz < maxHz)
        }
    }
    private const val NAME = "lfn_local_audio_statistics_v1"
    fun load(context: Context): Values {
        val p = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val v = Values(p.getInt("duration_s", DEFAULT_SECONDS), p.getInt("min_hz", 2), p.getInt("max_hz", 25), p.getBoolean("export_wav_filter_csv", false))
        return if (runCatching { v.validate() }.isSuccess) v else Values()
    }
    fun store(context: Context, value: Values) {
        value.validate()
        check(context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putInt("duration_s", value.durationSeconds).putInt("min_hz", value.minHz)
            .putInt("max_hz", value.maxHz).putBoolean("export_wav_filter_csv", value.exportWavFilterCsv).commit()) { "SAVE-Einstellungen konnten nicht gespeichert werden" }
    }
}
