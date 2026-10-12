package de.brummbeobachter.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

object ReminderInProcessToneScheduler {

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default
        )

    private val generation =
        AtomicLong(0L)

    private val lock =
        Any()

    private var jobs:
        List<Job> =
        emptyList()

    fun offsetForPackage(
        responseWindowMinutes: Int,
        packageIndex: Int
    ): Long {
        val offsets =
            ReminderPolicy
                .tonePackageOffsetsMillis(
                    responseWindowMinutes
                )

        val zeroBased =
            packageIndex - 1

        require(
            zeroBased in offsets.indices
        ) {
            "packageIndex out of range"
        }

        return offsets[zeroBased]
    }

    fun scheduleRemaining(
        context: Context,
        responseWindowMinutes: Int
    ) {
        val appContext =
            context.applicationContext

        cancel(
            appContext,
            log = false
        )

        /*
         * Beim Upgrade von älteren Versionen können noch zwei
         * AlarmManager-PendingIntents für Paket 2/3 existieren.
         * Diese werden beim Start des neuen Schemas entfernt.
         */
        ReminderTonePackageScheduler.cancel(
            appContext,
            log = false
        )

        val myGeneration =
            generation.get()

        val offsets =
            ReminderPolicy
                .tonePackageOffsetsMillis(
                    responseWindowMinutes
                )

        val newJobs =
            offsets
                .mapIndexedNotNull {
                        index,
                        offsetMs ->

                    if (index == 0) {
                        return@mapIndexedNotNull null
                    }

                    val packageIndex =
                        index + 1

                    DebugLogger.log(
                        appContext,
                        "INPROCESS_TONE_PACKAGE_SCHEDULED",
                        "package=$packageIndex/${offsets.size} " +
                            "offset_ms=$offsetMs " +
                            "response_window_min=$responseWindowMinutes"
                    )

                    scope.launch {
                        delay(offsetMs)

                        if (
                            generation.get() !=
                            myGeneration
                        ) {
                            return@launch
                        }

                        DebugLogger.log(
                            appContext,
                            "INPROCESS_TONE_PACKAGE_DUE",
                            "package=$packageIndex/${offsets.size} " +
                                "offset_ms=$offsetMs"
                        )

                        ReminderAlertController
                            .playAlarmTonePackage(
                                appContext,
                                packageIndex =
                                    packageIndex,
                                packageCount =
                                    offsets.size,
                                offsetMs =
                                    offsetMs,
                                responseWindowMinutes =
                                    responseWindowMinutes
                            )
                    }
                }

        synchronized(lock) {
            jobs =
                newJobs
        }
    }

    fun cancel(
        context: Context,
        log: Boolean = true
    ) {
        generation.incrementAndGet()

        val oldJobs =
            synchronized(lock) {
                val result =
                    jobs
                jobs =
                    emptyList()
                result
            }

        oldJobs.forEach {
            it.cancel()
        }

        if (log) {
            DebugLogger.log(
                context.applicationContext,
                "INPROCESS_TONE_PACKAGES_CANCELLED",
                "reason=explicit_cancel"
            )
        }
    }
}
