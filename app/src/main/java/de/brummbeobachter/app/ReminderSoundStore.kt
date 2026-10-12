package de.brummbeobachter.app

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.reminderSoundDataStore by preferencesDataStore(
    name = "brummbeobachter_reminder_sound"
)

data class ReminderSound(
    val uri: String,
    val title: String
)

class ReminderSoundStore(private val context: Context) {

    private object Keys {
        val uri = stringPreferencesKey("sound_uri")
        val title = stringPreferencesKey("sound_title")
    }

    private fun defaultUri(): String =
        RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_ALARM
        )?.toString()
            ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )?.toString()
            .orEmpty()

    val soundFlow: Flow<ReminderSound> =
        context.reminderSoundDataStore.data.map { prefs ->
            ReminderSound(
                uri = prefs[Keys.uri] ?: defaultUri(),
                title = prefs[Keys.title] ?: "Standard-Weckton"
            )
        }

    suspend fun save(uri: Uri, title: String) {
        context.reminderSoundDataStore.edit { prefs ->
            prefs[Keys.uri] = uri.toString()
            prefs[Keys.title] = title.ifBlank { "Erinnerungston" }
        }
    }
}
