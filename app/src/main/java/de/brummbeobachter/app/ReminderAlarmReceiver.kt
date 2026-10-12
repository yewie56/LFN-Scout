package de.brummbeobachter.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {

                if (intent.action == ACTION_RESPONSE_WINDOW_TIMEOUT) {
                    val reminderEpochMs =
                        intent.getLongExtra(
                            EXTRA_REMINDER_EPOCH_MS,
                            0L
                        )
                    val responseWindowMinutes =
                        intent.getIntExtra(
                            EXTRA_RESPONSE_WINDOW_MINUTES,
                            0
                        )
                    val eventId =
                        intent.getStringExtra(
                            EXTRA_TIMEOUT_EVENT_ID
                        ).orEmpty()

                    val currentSettings =
                        ReminderSettingsStore(appContext)
                            .settingsFlow
                            .first()
                    val timeoutNow =
                        System.currentTimeMillis()

                    if (
                        !currentSettings.enabled ||
                        currentSettings.pauseUntilEpochMs > timeoutNow
                    ) {
                        DebugLogger.log(
                            appContext,
                            "RESPONSE_TIMEOUT_IGNORED",
                            "enabled=${currentSettings.enabled} paused=" +
                                "${currentSettings.pauseUntilEpochMs > timeoutNow}"
                        )
                        return@launch
                    }

                    DebugLogger.log(
                        appContext,
                        "RESPONSE_WINDOW_EXPIRED",
                        "response_window_min=$responseWindowMinutes"
                    )

                    ReminderAlertController.stop(
                        appContext,
                        "RESPONSE_WINDOW_EXPIRED"
                    )

                    ReminderFullscreenController
                        .close(
                            "RESPONSE_WINDOW_EXPIRED"
                        )

                    DebugLogger.log(
                        appContext,
                        "FULL_SCREEN_DIRECT_CLOSE_REQUESTED",
                        "reason=response_window_timeout"
                    )

                    appContext.sendBroadcast(
                        Intent(
                            ReminderAlarmActivity
                                .ACTION_CLOSE_REMINDER_FULLSCREEN
                        ).apply {
                            setPackage(
                                appContext.packageName
                            )
                        }
                    )

                    DebugLogger.log(
                        appContext,
                        "FULL_SCREEN_CLOSE_REQUESTED",
                        "reason=response_window_timeout"
                    )

                    val key =
                        KeyStore(appContext)
                            .keyFlow
                            .first()
                            .trim()

                    if (key.isBlank()) {
                        DebugLogger.log(
                            appContext,
                            "NO_RESPONSE_SUBMIT_FAIL",
                            "rating=-1 reason=missing_key"
                        )
                        return@launch
                    }

                    val safeEventId =
                        eventId.ifBlank {
                            UUID.randomUUID().toString()
                        }

                    val scheduledEpoch =
                        reminderEpochMs.takeIf { it > 0L }
                            ?: System.currentTimeMillis()

                    val scheduledTime =
                        Instant.ofEpochMilli(scheduledEpoch)
                            .atZone(ZoneId.systemDefault())
                            .toOffsetDateTime()
                            .format(
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            )

                    val payload =
                        ObservationPayload(
                            key = key,
                            eventId = safeEventId,
                            scheduledTime = scheduledTime,
                            responseTime = null,
                            rating = -1,
                            latitude = 0.0,
                            longitude = 0.0,
                            appVersion = "0.4.41"
                        )

                    DebugLogger.log(
                        appContext,
                        "NO_RESPONSE_SUBMIT_STARTED",
                        "rating=-1 response_time=null wav_present=false"
                    )

                    LocalHalfHourObservations.record(appContext, payload)

                    val result =
                        SupabaseClient()
                            .submitObservation(payload)

                    if (result.success) {
                        OfflineObservationQueue.remove(
                            appContext,
                            payload.eventId
                        )
                    } else if (result.retryable) {
                        val pendingCount =
                            OfflineObservationQueue.enqueue(
                                appContext,
                                payload
                            )

                        DebugLogger.log(
                            appContext,
                            "OFFLINE_QUEUE_ENQUEUED",
                            "event_id=${payload.eventId} rating=-1 pending=$pendingCount"
                        )
                    } else {
                        DataTransferFailureNotifier
                            .showPermanentFailure(
                                appContext,
                                result.message
                            )
                    }

                    DebugLogger.log(
                        appContext,
                        if (result.success) {
                            "NO_RESPONSE_SUBMIT_OK"
                        } else {
                            "NO_RESPONSE_SUBMIT_FAIL"
                        },
                        "rating=-1 retryable=${result.retryable} message=${result.message}"
                    )

                    return@launch
                }

                if (intent.action == ACTION_ALARM_TONE_PACKAGE) {
                    val packageIndex =
                        intent.getIntExtra(EXTRA_PACKAGE_INDEX, 0)
                    val packageCount =
                        intent.getIntExtra(EXTRA_PACKAGE_COUNT, 3)
                    val offsetMs =
                        intent.getLongExtra(EXTRA_PACKAGE_OFFSET_MS, 0L)
                    val responseWindowMinutes =
                        intent.getIntExtra(
                            EXTRA_RESPONSE_WINDOW_MINUTES,
                            0
                        )

                    DebugLogger.log(
                        appContext,
                        "ALARM_TONE_PACKAGE_RECEIVED",
                        "package=$packageIndex/$packageCount offset_ms=$offsetMs"
                    )

                    ReminderAlertController.playAlarmTonePackage(
                        appContext,
                        packageIndex = packageIndex,
                        packageCount = packageCount,
                        offsetMs = offsetMs,
                        responseWindowMinutes = responseWindowMinutes
                    )
                    return@launch
                }

                if (intent.action != ACTION_EXACT_REMINDER) {
                    return@launch
                }

                OfflineObservationRetry.flush(
                    appContext,
                    "exact_reminder"
                )

                val store =
                    ReminderSettingsStore(appContext)

                val settings =
                    store.settingsFlow.first()

                val now = System.currentTimeMillis()
                val delaySec =
                    if (settings.nextReminderEpochMs > 0L) {
                        (
                            now -
                                settings.nextReminderEpochMs
                            ) / 1000L
                    } else {
                        0L
                    }

                DebugLogger.log(
                    appContext,
                    "EXACT_ALARM_RECEIVED",
                    "planned=${DebugLogger.formatEpoch(settings.nextReminderEpochMs)} " +
                        "delay_sec=$delaySec"
                )

                if (!settings.enabled) {
                    store.setNextReminderEpochMs(0L)
                    return@launch
                }

                if (
                    settings.pauseUntilEpochMs > now
                ) {
                    ReminderEngine.scheduleAt(
                        appContext,
                        store,
                        settings,
                        settings.pauseUntilEpochMs,
                        "alarm_pause_until"
                    )
                    return@launch
                }

                ReminderNotifier.show(
                    appContext,
                    requestFullScreen = true
                )

                ReminderEngine.scheduleAfterInterval(
                    appContext,
                    store,
                    settings,
                    "alarm_next_cycle"
                )

                val responseWindowMinutes =
                    ReminderPolicy.responseWindowMinutes(
                        settings.intervalMinutes
                    )

                ReminderAlertController.playAlarmTonePackage(
                    appContext,
                    packageIndex = 1,
                    packageCount = 3,
                    offsetMs = 0L,
                    responseWindowMinutes = responseWindowMinutes
                )

                ReminderInProcessToneScheduler.scheduleRemaining(
                    appContext,
                    responseWindowMinutes
                )

                ReminderResponseTimeoutScheduler.schedule(
                    appContext,
                    reminderEpochMs = now,
                    responseWindowMinutes = responseWindowMinutes,
                    eventId = UUID.randomUUID().toString()
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_EXACT_REMINDER =
            "de.brummbeobachter.app.action.EXACT_REMINDER"

        const val ACTION_ALARM_TONE_PACKAGE =
            "de.brummbeobachter.app.action.ALARM_TONE_PACKAGE"

        const val ACTION_RESPONSE_WINDOW_TIMEOUT =
            "de.brummbeobachter.app.action.RESPONSE_WINDOW_TIMEOUT"

        const val EXTRA_PACKAGE_INDEX = "package_index"
        const val EXTRA_PACKAGE_COUNT = "package_count"
        const val EXTRA_PACKAGE_OFFSET_MS = "package_offset_ms"
        const val EXTRA_RESPONSE_WINDOW_MINUTES =
            "response_window_minutes"
        const val EXTRA_REMINDER_EPOCH_MS =
            "reminder_epoch_ms"
        const val EXTRA_TIMEOUT_EVENT_ID =
            "timeout_event_id"
    }
}
