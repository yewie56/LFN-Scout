package de.brummbeobachter.app

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicLong

object ReminderAlertController {

    /*
     * Keine Synchronized-Annotation auf suspend-Funktionen:
     * Kotlin verbietet das. Stattdessen schützt stateLock ausschließlich
     * sehr kurze synchrone Zustandsänderungen.
     *
     * generation verhindert zusätzlich, dass ein bereits gestarteter
     * suspendierender Startvorgang nach einem STOP den Alarm erneut startet.
     */
    private val stateLock = Any()
    private val generation = AtomicLong(0L)

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    suspend fun playAlarmTonePackage(
        context: Context,
        packageIndex: Int,
        packageCount: Int,
        offsetMs: Long,
        responseWindowMinutes: Int
    ) {
        val appContext = context.applicationContext
        val myGeneration = generation.get()

        val selected =
            ReminderSoundStore(appContext)
                .soundFlow
                .first()

        val volumeSettings =
            ReminderSettingsStore(appContext)
                .settingsFlow
                .first()

        AlarmVolumeController.applyPercent(
            appContext,
            volumeSettings.alarmVolumePercent
        )

        if (generation.get() != myGeneration) {
            DebugLogger.log(
                appContext,
                "ALARM_TONE_PACKAGE_ABORTED",
                "package=$packageIndex reason=stopped_during_sound_lookup"
            )
            return
        }

        playSingleAlarmPackage(
            context = appContext,
            selected = selected,
            myGeneration = myGeneration,
            packageIndex = packageIndex,
            packageCount = packageCount,
            offsetMs = offsetMs,
            responseWindowMinutes = responseWindowMinutes
        )
    }

    private fun playSingleAlarmPackage(
        context: Context,
        selected: ReminderSound,
        myGeneration: Long,
        packageIndex: Int,
        packageCount: Int,
        offsetMs: Long,
        responseWindowMinutes: Int
    ) {
        if (generation.get() != myGeneration) {
            DebugLogger.log(
                context,
                "ALARM_TONE_PACKAGE_ABORTED",
                "package=$packageIndex reason=stopped_before_start"
            )
            return
        }

        /*
         * Falls ein sehr langer Ton aus dem vorherigen Paket noch läuft,
         * wird nur dieser lokale Audio-/Vibrationszustand beendet.
         * Die Notification und die noch geplanten Tonpakete bleiben bestehen.
         */
        clearCurrentResources(
            context = context,
            cancelNotification = false,
            reason = "before_alarm_tone_package_$packageIndex"
        )

        if (generation.get() != myGeneration) {
            DebugLogger.log(
                context,
                "ALARM_TONE_PACKAGE_ABORTED",
                "package=$packageIndex reason=stopped_during_package_prepare"
            )
            return
        }

        val uri =
            selected.uri
                .takeIf { it.isNotBlank() }
                ?.let(Uri::parse)
                ?: RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

        val newRingtone =
            runCatching {
                RingtoneManager.getRingtone(
                    context,
                    uri
                )
            }.getOrNull()

        if (newRingtone == null) {
            DebugLogger.log(
                context,
                "ALARM_TONE_PACKAGE_FAILED",
                "package=$packageIndex reason=ringtone_unavailable title=${selected.title}"
            )
            return
        }

        val newVibrator =
            runCatching {
                @Suppress("DEPRECATION")
                context.getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as? Vibrator
            }.getOrNull()

        val started =
            synchronized(stateLock) {
                if (generation.get() != myGeneration) {
                    false
                } else {
                    runCatching {
                        newRingtone.audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(
                                    AudioAttributes.USAGE_ALARM
                                )
                                .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build()

                        if (Build.VERSION.SDK_INT >= 28) {
                            /*
                             * Ein Tonpaket = ausgewählten Ton genau einmal.
                             */
                            newRingtone.isLooping = false
                        }

                        ringtone = newRingtone
                        vibrator = newVibrator

                        newVibrator?.let {
                            startSinglePackageVibration(it)
                        }

                        newRingtone.play()
                    }.isSuccess
                }
            }

        if (!started) {
            runCatching {
                newRingtone.stop()
            }
            runCatching {
                newVibrator?.cancel()
            }

            synchronized(stateLock) {
                if (ringtone === newRingtone) {
                    ringtone = null
                }
                if (vibrator === newVibrator) {
                    vibrator = null
                }
            }

            DebugLogger.log(
                context,
                "ALARM_TONE_PACKAGE_ABORTED",
                "package=$packageIndex reason=stopped_before_play"
            )
            return
        }

        DebugLogger.log(
            context,
            "ALARM_TONE_PACKAGE_STARTED",
            "package=$packageIndex/$packageCount offset_ms=$offsetMs response_window_min=$responseWindowMinutes title=${selected.title}"
        )
    }

    suspend fun startSoundSelfTest(
        context: Context
    ): SoundSelfTestResult {
        val appContext = context.applicationContext
        val myGeneration = generation.incrementAndGet()

        clearCurrentResources(
            context = appContext,
            cancelNotification = false,
            reason = "restart_before_sound_selftest"
        )

        val selected =
            runCatching {
                ReminderSoundStore(appContext)
                    .soundFlow
                    .first()
            }.getOrElse { error ->
                DebugLogger.log(
                    appContext,
                    SoundSelfTestEvents.FAILED,
                    "reason=sound_lookup_failed error=${error.javaClass.simpleName}"
                )
                return SoundSelfTestResult.FAILED
            }

        val volumeSettings =
            ReminderSettingsStore(appContext)
                .settingsFlow
                .first()

        AlarmVolumeController.applyPercent(
            appContext,
            volumeSettings.alarmVolumePercent
        )

        if (generation.get() != myGeneration) {
            DebugLogger.log(
                appContext,
                SoundSelfTestEvents.STOPPED,
                "reason=stopped_during_sound_lookup"
            )
            return SoundSelfTestResult.STOPPED
        }

        val uri =
            selected.uri
                .takeIf { it.isNotBlank() }
                ?.let(Uri::parse)
                ?: RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

        val newRingtone =
            runCatching {
                RingtoneManager.getRingtone(
                    appContext,
                    uri
                )
            }.getOrNull()

        if (newRingtone == null) {
            DebugLogger.log(
                appContext,
                SoundSelfTestEvents.FAILED,
                "reason=ringtone_unavailable title=${selected.title}"
            )
            return SoundSelfTestResult.FAILED
        }

        val started =
            synchronized(stateLock) {
                if (generation.get() != myGeneration) {
                    false
                } else {
                    runCatching {
                        newRingtone.audioAttributes =
                            AudioAttributes.Builder()
                                .setUsage(
                                    AudioAttributes.USAGE_ALARM
                                )
                                .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build()

                        if (Build.VERSION.SDK_INT >= 28) {
                            newRingtone.isLooping = true
                        }

                        ringtone = newRingtone
                        vibrator = null
                        newRingtone.play()
                    }.isSuccess
                }
            }

        if (!started) {
            runCatching {
                newRingtone.stop()
            }

            synchronized(stateLock) {
                if (ringtone === newRingtone) {
                    ringtone = null
                }
            }

            val result =
                if (generation.get() != myGeneration) {
                    SoundSelfTestResult.STOPPED
                } else {
                    SoundSelfTestResult.FAILED
                }

            DebugLogger.log(
                appContext,
                if (result == SoundSelfTestResult.STOPPED) {
                    SoundSelfTestEvents.STOPPED
                } else {
                    SoundSelfTestEvents.FAILED
                },
                "reason=${if (result == SoundSelfTestResult.STOPPED) "stopped_before_start" else "playback_failed"}"
            )
            return result
        }

        DebugLogger.log(
            appContext,
            SoundSelfTestEvents.STARTED,
            "title=${selected.title} looping=${Build.VERSION.SDK_INT >= 28}"
        )
        return SoundSelfTestResult.STARTED
    }

    fun stopSoundSelfTest(
        context: Context,
        reason: String
    ) {
        generation.incrementAndGet()

        clearCurrentResources(
            context = context.applicationContext,
            cancelNotification = false,
            reason = reason
        )

        DebugLogger.log(
            context.applicationContext,
            SoundSelfTestEvents.STOPPED,
            "reason=$reason"
        )
    }

    fun stop(
        context: Context,
        reason: String
    ) {
        /*
         * Sofort zuerst alle noch laufenden/kommenden Starts ungültig machen.
         */
        generation.incrementAndGet()

        ReminderTonePackageScheduler.cancel(
            context.applicationContext
        )

        ReminderInProcessToneScheduler.cancel(
            context.applicationContext
        )

        clearCurrentResources(
            context = context.applicationContext,
            cancelNotification = true,
            reason = reason
        )
    }

    private fun clearCurrentResources(
        context: Context,
        cancelNotification: Boolean,
        reason: String
    ) {
        val oldRingtone: Ringtone?
        val oldVibrator: Vibrator?

        /*
         * Nur Referenzen austauschen; keinerlei suspendierende Arbeit.
         */
        synchronized(stateLock) {
            oldRingtone = ringtone
            oldVibrator = vibrator
            ringtone = null
            vibrator = null
        }

        runCatching {
            oldRingtone?.stop()
        }

        runCatching {
            oldVibrator?.cancel()
        }

        if (cancelNotification) {
            NotificationManagerCompat
                .from(context)
                .cancel(
                    ReminderWorker.NOTIFICATION_ID
                )
        }

        DebugLogger.log(
            context,
            "ALERT_STOPPED",
            "reason=$reason"
        )
    }

    @Suppress("DEPRECATION")
    private fun startSinglePackageVibration(
        service: Vibrator
    ) {
        val pattern =
            longArrayOf(
                0L,
                700L,
                250L,
                700L,
                250L,
                1200L
            )

        if (Build.VERSION.SDK_INT >= 26) {
            service.vibrate(
                VibrationEffect.createWaveform(
                    pattern,
                    -1
                )
            )
        } else {
            service.vibrate(
                pattern,
                -1
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun startRepeatingVibration(
        service: Vibrator
    ) {
        val pattern =
            longArrayOf(
                0L,
                700L,
                250L,
                700L,
                250L,
                1200L,
                500L
            )

        if (Build.VERSION.SDK_INT >= 26) {
            service.vibrate(
                VibrationEffect.createWaveform(
                    pattern,
                    0
                )
            )
        } else {
            service.vibrate(
                pattern,
                0
            )
        }
    }
}
