package de.brummbeobachter.app

import android.content.Context
import kotlinx.coroutines.flow.first
import java.util.concurrent.atomic.AtomicBoolean

object OfflineObservationRetry {

    private val retryRunning =
        AtomicBoolean(false)

    suspend fun flush(
        context: Context,
        reason: String
    ) {
        val appContext =
            context.applicationContext

        if (
            !retryRunning.compareAndSet(
                false,
                true
            )
        ) {
            DebugLogger.log(
                appContext,
                "OFFLINE_RETRY_SKIPPED",
                "reason=$reason already_running=true"
            )
            return
        }

        try {
            flushInternal(
                appContext,
                reason
            )
        } finally {
            retryRunning.set(false)
        }
    }

    private suspend fun flushInternal(
        appContext: Context,
        reason: String
    ) {
        val pending =
            OfflineObservationQueue.snapshot(
                appContext
            )

        if (pending.isEmpty()) {
            return
        }

        val key =
            KeyStore(appContext)
                .keyFlow
                .first()
                .trim()

        if (key.isBlank()) {
            DebugLogger.log(
                appContext,
                "OFFLINE_RETRY_SKIPPED",
                "reason=$reason missing_key=true pending=${pending.size}"
            )
            return
        }

        DebugLogger.log(
            appContext,
            "OFFLINE_RETRY_STARTED",
            "reason=$reason pending=${pending.size}"
        )

        val client =
            SupabaseClient()

        for (item in pending) {
            val result =
                client.submitObservation(
                    item.toPayload(key)
                )

            if (result.success) {
                val remaining =
                    OfflineObservationQueue.remove(
                        appContext,
                        item.eventId
                    )

                DebugLogger.log(
                    appContext,
                    "OFFLINE_RETRY_OK",
                    "event_id=${item.eventId} rating=${item.rating} remaining=$remaining"
                )
            } else {
                DebugLogger.log(
                    appContext,
                    "OFFLINE_RETRY_FAIL",
                    "event_id=${item.eventId} rating=${item.rating} retryable=${result.retryable} message=${result.message}"
                )

                if (!result.retryable) {
                    DataTransferFailureNotifier
                        .showPermanentFailure(
                            appContext,
                            result.message
                        )
                }

                /*
                 * Bei transienten Fehlern bringt weiteres sofortiges Senden
                 * nichts. Der Rest bleibt persistent für den nächsten Lauf.
                 * Permanente Ablehnungen bleiben ebenfalls erhalten, damit
                 * keine Messung automatisch verworfen wird.
                 */
                break
            }
        }
    }
}
