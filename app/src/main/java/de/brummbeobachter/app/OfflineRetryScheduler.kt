package de.brummbeobachter.app

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object OfflineRetryScheduler {

    private const val UNIQUE_WORK_NAME =
        "brummbeobachter_offline_retry_2h"

    fun syncWithQueue(
        context: Context
    ) {
        val appContext =
            context.applicationContext

        val pending =
            OfflineObservationQueue.size(
                appContext
            )

        if (pending <= 0) {
            WorkManager.getInstance(
                appContext
            ).cancelUniqueWork(
                UNIQUE_WORK_NAME
            )

            DebugLogger.log(
                appContext,
                "OFFLINE_RETRY_2H_CANCELLED",
                "pending=0"
            )
            return
        }

        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                )
                .build()

        val request =
            PeriodicWorkRequestBuilder<OfflineRetryWorker>(
                OfflineRetrySchedulePolicy.RETRY_INTERVAL_MS,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(
                    constraints
                )
                .build()

        WorkManager.getInstance(
            appContext
        ).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        DebugLogger.log(
            appContext,
            "OFFLINE_RETRY_2H_SCHEDULED",
            "pending=$pending interval_hours=2"
        )
    }
}
