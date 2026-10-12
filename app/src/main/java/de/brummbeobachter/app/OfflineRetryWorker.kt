package de.brummbeobachter.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OfflineRetryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(
    appContext,
    params
) {

    override suspend fun doWork(): Result {
        DebugLogger.log(
            applicationContext,
            "OFFLINE_RETRY_2H_STARTED"
        )

        OfflineObservationRetry.flush(
            applicationContext,
            "periodic_2h"
        )

        OfflineRetryScheduler.syncWithQueue(
            applicationContext
        )

        return Result.success()
    }
}
