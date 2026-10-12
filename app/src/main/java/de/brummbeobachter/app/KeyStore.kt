package de.brummbeobachter.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.brummBeobachterDataStore by preferencesDataStore(
    name = "brummbeobachter_settings"
)

class KeyStore(private val context: Context) {

    private object Keys {
        val observerKey = stringPreferencesKey("observer_key")
        val observerId = stringPreferencesKey("observer_id")
    }

    val keyFlow: Flow<String> =
        context.brummBeobachterDataStore.data.map { preferences ->
            preferences[Keys.observerKey] ?: ""
        }

    val observerIdFlow: Flow<String> =
        context.brummBeobachterDataStore.data.map { preferences ->
            preferences[Keys.observerId] ?: ""
        }

    suspend fun saveKey(value: String) {
        context.brummBeobachterDataStore.edit { preferences ->
            preferences[Keys.observerKey] = value.trim()
        }
    }

    suspend fun saveObserverId(value: String) {
        context.brummBeobachterDataStore.edit { preferences ->
            preferences[Keys.observerId] =
                value.trim().uppercase()
        }
    }
}
