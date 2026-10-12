package de.brummbeobachter.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.reminderDataStore by preferencesDataStore(
    name = "brummbeobachter_reminders"
)

data class ReminderSettings(
    val enabled: Boolean = true,
    val intervalMinutes: Int = ReminderPolicy.DEFAULT_INTERVAL_MINUTES,
    val pauseUntilEpochMs: Long = 0L,
    val nextReminderEpochMs: Long = 0L,
    val mode: ReminderMode = ReminderMode.VERY_NOTICEABLE,
    val autoBackgroundAfterAction: Boolean = false,
    val radiusAnalysisEnabled: Boolean = false,
    val radiusAnalysisKm: Int =
        RadiusAnalysisPolicy.DEFAULT_RADIUS_KM,
    val alarmVolumePercent: Int =
        AudioRecordingPolicy.DEFAULT_ALARM_VOLUME_PERCENT,
    val audioGainDb: Int =
        AudioRecordingPolicy.DEFAULT_GAIN_DB,
    val audioSourceMode: AudioSourceMode =
        AudioSourceMode.AUTO,
    val audioInputDeviceId: Int =
        AudioInputSelectionPolicy.AUTO_DEVICE_ID,
    val maxAudioRecordingsPerDay: Int =
        AudioRecordingPolicy.DEFAULT_DAILY_LIMIT,
    val audioRecordingsToday: Int = 0,
    val developerModeEnabled: Boolean = false,
    val nearPlotGain: Int = SeismicPlotUiPolicy.DEFAULT_NEAR_PLOT_GAIN,
    val waterfallLevelAuto: Boolean = true,
    val waterfallLevelMinDbAt50: Int = WaterfallLevelPolicy.DEFAULT_MIN_DB_AT_50,
    val waterfallLevelMaxDbAt50: Int = WaterfallLevelPolicy.DEFAULT_MAX_DB_AT_50,
    val waterfallOutputFormat: WaterfallOutputFormat =
        WaterfallOutputFormat.PNG,
    val waterfallStatsMinHz: Int =
        WaterfallStatsPolicy.DEFAULT_MIN_HZ.toInt(),
    val waterfallStatsMaxHz: Int =
        WaterfallStatsPolicy.DEFAULT_MAX_HZ.toInt(),
    val rollingBufferEnabled: Boolean = false,
    val rollingMaxOperationHours: Int = 12,
    val ratingNoCommentTimeoutSeconds: Int =
        CommentAutoSendPolicy.DEFAULT_RATING_NO_COMMENT_TIMEOUT_SECONDS,
    val ratingCommentTimeoutSeconds: Int =
        CommentAutoSendPolicy.DEFAULT_RATING_COMMENT_TIMEOUT_SECONDS,
    val audioNoCommentTimeoutSeconds: Int =
        CommentAutoSendPolicy.DEFAULT_AUDIO_NO_COMMENT_TIMEOUT_SECONDS,
    val audioCommentTimeoutSeconds: Int =
        CommentAutoSendPolicy.DEFAULT_AUDIO_COMMENT_TIMEOUT_SECONDS
) {
    fun isPaused(
        nowEpochMs: Long = System.currentTimeMillis()
    ): Boolean =
        enabled && pauseUntilEpochMs > nowEpochMs
}

class ReminderSettingsStore(
    private val context: Context
) {

    private object Keys {
        val enabled =
            booleanPreferencesKey("enabled")
        val intervalMinutes =
            intPreferencesKey("interval_minutes")
        val pauseUntilEpochMs =
            longPreferencesKey("pause_until_epoch_ms")
        val nextReminderEpochMs =
            longPreferencesKey("next_reminder_epoch_ms")
        val mode =
            stringPreferencesKey("reminder_mode")
        val autoBackgroundAfterAction =
            booleanPreferencesKey("auto_background_after_action")
        val radiusAnalysisEnabled =
            booleanPreferencesKey("radius_analysis_enabled")
        val radiusAnalysisKm =
            intPreferencesKey("radius_analysis_km")
        val alarmVolumePercent =
            intPreferencesKey("alarm_volume_percent")
        val audioGainDb =
            intPreferencesKey("audio_gain_db")
        val audioSourceMode =
            stringPreferencesKey("audio_source_mode")
        val audioInputDeviceId =
            intPreferencesKey("audio_input_device_id")
        val maxAudioRecordingsPerDay =
            intPreferencesKey("max_audio_recordings_per_day")
        val audioCountDate =
            stringPreferencesKey("audio_count_date")
        val audioCountToday =
            intPreferencesKey("audio_count_today")
        val developerModeEnabled =
            booleanPreferencesKey("developer_mode_enabled")
        val nearPlotGain =
            intPreferencesKey("near_plot_gain")
        val waterfallLevelAuto =
            booleanPreferencesKey("waterfall_level_auto")
        val waterfallLevelMinDbAt50 =
            intPreferencesKey("waterfall_level_min_db_at_50")
        val waterfallLevelMaxDbAt50 =
            intPreferencesKey("waterfall_level_max_db_at_50")
        val waterfallOutputFormat =
            stringPreferencesKey("waterfall_output_format")
        val waterfallStatsMinHz =
            intPreferencesKey("waterfall_stats_min_hz")
        val waterfallStatsMaxHz =
            intPreferencesKey("waterfall_stats_max_hz")
        val rollingBufferEnabled =
            booleanPreferencesKey("rolling_buffer_enabled")
        val rollingMaxOperationHours = intPreferencesKey("rolling_max_operation_hours")
        val ratingNoCommentTimeoutSeconds =
            intPreferencesKey("rating_no_comment_timeout_seconds")
        val ratingCommentTimeoutSeconds =
            intPreferencesKey("rating_comment_timeout_seconds")
        val audioNoCommentTimeoutSeconds =
            intPreferencesKey("audio_no_comment_timeout_seconds")
        val audioCommentTimeoutSeconds =
            intPreferencesKey("audio_comment_timeout_seconds")
    }

    val settingsFlow: Flow<ReminderSettings> =
        context.reminderDataStore.data.map { prefs ->
            ReminderSettings(
                enabled =
                    prefs[Keys.enabled] ?: true,
                intervalMinutes =
                    ReminderPolicy.normalizeIntervalMinutes(
                        prefs[Keys.intervalMinutes]
                            ?: ReminderPolicy.DEFAULT_INTERVAL_MINUTES
                    ),
                pauseUntilEpochMs =
                    prefs[Keys.pauseUntilEpochMs] ?: 0L,
                nextReminderEpochMs =
                    prefs[Keys.nextReminderEpochMs] ?: 0L,
                mode =
                    ReminderMode.fromStored(
                        prefs[Keys.mode]
                    ),
                autoBackgroundAfterAction =
                    prefs[Keys.autoBackgroundAfterAction] ?: false,
                radiusAnalysisEnabled =
                    prefs[Keys.radiusAnalysisEnabled] ?: false,
                radiusAnalysisKm =
                    RadiusAnalysisPolicy.normalizeRadiusKm(
                        prefs[Keys.radiusAnalysisKm]
                            ?: RadiusAnalysisPolicy.DEFAULT_RADIUS_KM
                    ),
                alarmVolumePercent =
                    AudioRecordingPolicy.normalizeAlarmVolumePercent(
                        prefs[Keys.alarmVolumePercent]
                            ?: AudioRecordingPolicy.DEFAULT_ALARM_VOLUME_PERCENT
                    ),
                audioGainDb =
                    AudioRecordingPolicy.normalizeGainDb(
                        prefs[Keys.audioGainDb]
                            ?: AudioRecordingPolicy.DEFAULT_GAIN_DB
                    ),
                audioSourceMode =
                    AudioSourceMode.fromStored(
                        prefs[Keys.audioSourceMode]
                    ),
                audioInputDeviceId =
                    prefs[Keys.audioInputDeviceId]
                        ?: AudioInputSelectionPolicy.AUTO_DEVICE_ID,
                maxAudioRecordingsPerDay =
                    AudioRecordingPolicy.normalizeDailyLimit(
                        prefs[Keys.maxAudioRecordingsPerDay]
                            ?: AudioRecordingPolicy.DEFAULT_DAILY_LIMIT
                    ),
                audioRecordingsToday =
                    if (
                        prefs[Keys.audioCountDate] ==
                        LocalDate.now().toString()
                    ) {
                        (prefs[Keys.audioCountToday] ?: 0)
                            .coerceAtLeast(0)
                    } else {
                        0
                    },
                developerModeEnabled =
                    prefs[Keys.developerModeEnabled] ?: false,
                nearPlotGain =
                    SeismicPlotUiPolicy.normalizeNearPlotGain(
                        prefs[Keys.nearPlotGain] ?: SeismicPlotUiPolicy.DEFAULT_NEAR_PLOT_GAIN
                    ),
                waterfallLevelAuto =
                    prefs[Keys.waterfallLevelAuto] ?: true,
                waterfallLevelMinDbAt50 =
                    WaterfallLevelPolicy.normalizeMinDb(
                        prefs[Keys.waterfallLevelMinDbAt50] ?: WaterfallLevelPolicy.DEFAULT_MIN_DB_AT_50
                    ),
                waterfallLevelMaxDbAt50 =
                    WaterfallLevelPolicy.normalizeMaxDb(
                        prefs[Keys.waterfallLevelMaxDbAt50] ?: WaterfallLevelPolicy.DEFAULT_MAX_DB_AT_50
                    ),
                waterfallOutputFormat =
                    WaterfallOutputFormat.fromStored(
                        prefs[Keys.waterfallOutputFormat]
                    ),
                waterfallStatsMinHz =
                    prefs[Keys.waterfallStatsMinHz]
                        ?: WaterfallStatsPolicy.DEFAULT_MIN_HZ.toInt(),
                waterfallStatsMaxHz =
                    prefs[Keys.waterfallStatsMaxHz]
                        ?: WaterfallStatsPolicy.DEFAULT_MAX_HZ.toInt(),
                rollingBufferEnabled =
                    prefs[Keys.rollingBufferEnabled] ?: false,
                rollingMaxOperationHours = (prefs[Keys.rollingMaxOperationHours] ?: 12).coerceIn(1, 168),
                ratingNoCommentTimeoutSeconds =
                    CommentAutoSendPolicy.normalizeSeconds(
                        prefs[Keys.ratingNoCommentTimeoutSeconds]
                            ?: CommentAutoSendPolicy.DEFAULT_RATING_NO_COMMENT_TIMEOUT_SECONDS
                    ),
                ratingCommentTimeoutSeconds =
                    CommentAutoSendPolicy.normalizeSeconds(
                        prefs[Keys.ratingCommentTimeoutSeconds]
                            ?: CommentAutoSendPolicy.DEFAULT_RATING_COMMENT_TIMEOUT_SECONDS
                    ),
                audioNoCommentTimeoutSeconds =
                    CommentAutoSendPolicy.normalizeSeconds(
                        prefs[Keys.audioNoCommentTimeoutSeconds]
                            ?: CommentAutoSendPolicy.DEFAULT_AUDIO_NO_COMMENT_TIMEOUT_SECONDS
                    ),
                audioCommentTimeoutSeconds =
                    CommentAutoSendPolicy.normalizeSeconds(
                        prefs[Keys.audioCommentTimeoutSeconds]
                            ?: CommentAutoSendPolicy.DEFAULT_AUDIO_COMMENT_TIMEOUT_SECONDS
                    )
            )
        }

    suspend fun setIntervalMinutes(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.intervalMinutes] =
                ReminderPolicy.normalizeIntervalMinutes(
                    value
                )
        }
    }

    suspend fun setMode(value: ReminderMode) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.mode] = value.name
        }
    }

    suspend fun setAutoBackgroundAfterAction(value: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.autoBackgroundAfterAction] = value
        }
    }

    suspend fun setRadiusAnalysisEnabled(value: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.radiusAnalysisEnabled] = value
        }
    }

    suspend fun setRadiusAnalysisKm(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.radiusAnalysisKm] =
                RadiusAnalysisPolicy.normalizeRadiusKm(
                    value
                )
        }
    }

    suspend fun setAlarmVolumePercent(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.alarmVolumePercent] =
                AudioRecordingPolicy
                    .normalizeAlarmVolumePercent(value)
        }
    }

    suspend fun setAudioGainDb(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.audioGainDb] =
                AudioRecordingPolicy
                    .normalizeGainDb(value)
        }
    }

    suspend fun setAudioSourceMode(value: AudioSourceMode) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.audioSourceMode] = value.name
        }
    }

    suspend fun setAudioInputDeviceId(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.audioInputDeviceId] = value
        }
    }

    suspend fun setMaxAudioRecordingsPerDay(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.maxAudioRecordingsPerDay] =
                AudioRecordingPolicy
                    .normalizeDailyLimit(value)
        }
    }

    suspend fun setDeveloperModeEnabled(value: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.developerModeEnabled] = value
        }
    }

    suspend fun setNearPlotGain(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.nearPlotGain] = SeismicPlotUiPolicy.normalizeNearPlotGain(value)
        }
    }

    suspend fun setWaterfallLevelSettings(auto: Boolean, minDbAt50: Int, maxDbAt50: Int) {
        val normalized = WaterfallLevelPolicy.normalizeReferences(minDbAt50, maxDbAt50)
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.waterfallLevelAuto] = auto
            prefs[Keys.waterfallLevelMinDbAt50] = normalized.first
            prefs[Keys.waterfallLevelMaxDbAt50] = normalized.second
        }
    }

    suspend fun setWaterfallOutputFormat(
        value: WaterfallOutputFormat
    ) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.waterfallOutputFormat] = value.name
        }
    }

    suspend fun setWaterfallStatsRange(
        minHz: Int,
        maxHz: Int
    ) {
        val range = WaterfallStatsPolicy.effectiveRange(
            developerModeEnabled = true,
            storedMinHz = minHz,
            storedMaxHz = maxHz
        )
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.waterfallStatsMinHz] = range.minHz.toInt()
            prefs[Keys.waterfallStatsMaxHz] = range.maxHz.toInt()
        }
    }

    suspend fun setRollingMaxOperationHours(value: Int) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.rollingMaxOperationHours] = value.coerceIn(1, 168)
        }
    }

    suspend fun setRollingBufferEnabled(value: Boolean) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.rollingBufferEnabled] = value
        }
    }

    suspend fun setCommentTimeouts(
        ratingNoCommentSeconds: Int,
        ratingCommentSeconds: Int,
        audioNoCommentSeconds: Int,
        audioCommentSeconds: Int
    ) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.ratingNoCommentTimeoutSeconds] =
                CommentAutoSendPolicy.normalizeSeconds(ratingNoCommentSeconds)
            prefs[Keys.ratingCommentTimeoutSeconds] =
                CommentAutoSendPolicy.normalizeSeconds(ratingCommentSeconds)
            prefs[Keys.audioNoCommentTimeoutSeconds] =
                CommentAutoSendPolicy.normalizeSeconds(audioNoCommentSeconds)
            prefs[Keys.audioCommentTimeoutSeconds] =
                CommentAutoSendPolicy.normalizeSeconds(audioCommentSeconds)
        }
    }

    /** Atomic daily limit check and increment shared by observer and local analyzer. */
    suspend fun tryMarkAudioRecordingCompleted(): Int? {
        val today = LocalDate.now().toString()
        var result: Int? = null
        context.reminderDataStore.edit { prefs ->
            val current = if (prefs[Keys.audioCountDate] == today)
                (prefs[Keys.audioCountToday] ?: 0).coerceAtLeast(0) else 0
            val limit = AudioRecordingPolicy.normalizeDailyLimit(
                prefs[Keys.maxAudioRecordingsPerDay] ?: AudioRecordingPolicy.DEFAULT_DAILY_LIMIT)
            if (current < limit) {
                val next = current + 1
                result = next
                prefs[Keys.audioCountDate] = today
                prefs[Keys.audioCountToday] = next
            }
        }
        return result
    }

    suspend fun markAudioRecordingCompleted(): Int {
        val today =
            LocalDate.now().toString()

        var newCount = 1

        context.reminderDataStore.edit { prefs ->
            val storedDate =
                prefs[Keys.audioCountDate]
            val storedCount =
                prefs[Keys.audioCountToday] ?: 0

            newCount =
                if (storedDate == today) {
                    storedCount
                        .coerceAtLeast(0) + 1
                } else {
                    1
                }

            prefs[Keys.audioCountDate] = today
            prefs[Keys.audioCountToday] = newCount
        }

        return newCount
    }

    suspend fun setNextReminderEpochMs(
        value: Long
    ) {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.nextReminderEpochMs] =
                value.coerceAtLeast(0L)
        }
    }

    suspend fun enableNow() {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.enabled] = true
            prefs[Keys.pauseUntilEpochMs] = 0L
        }
    }

    suspend fun pauseHours(hours: Int): Long {
        val until =
            System.currentTimeMillis() +
                hours * 60L * 60L * 1000L

        context.reminderDataStore.edit { prefs ->
            prefs[Keys.enabled] = true
            prefs[Keys.pauseUntilEpochMs] = until
            prefs[Keys.nextReminderEpochMs] = until
        }

        return until
    }

    suspend fun disableCompletely() {
        context.reminderDataStore.edit { prefs ->
            prefs[Keys.enabled] = false
            prefs[Keys.pauseUntilEpochMs] = 0L
            prefs[Keys.nextReminderEpochMs] = 0L
        }
    }
}
