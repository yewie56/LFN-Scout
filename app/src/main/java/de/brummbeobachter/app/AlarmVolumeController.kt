package de.brummbeobachter.app

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt

object AlarmVolumeController {

    fun applyPercent(
        context: Context,
        percent: Int
    ): Int {
        val audioManager =
            context.applicationContext
                .getSystemService(Context.AUDIO_SERVICE)
                as AudioManager

        val normalized =
            AudioRecordingPolicy
                .normalizeAlarmVolumePercent(percent)

        val max =
            audioManager.getStreamMaxVolume(
                AudioManager.STREAM_ALARM
            ).coerceAtLeast(1)

        val target =
            (
                max *
                    normalized / 100.0
                ).roundToInt()
                .coerceIn(0, max)

        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            target,
            0
        )

        return target
    }
}
