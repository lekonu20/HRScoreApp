package com.savani.hrscore.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "hrscore_prefs")

class KeyStore(private val context: Context) {

    companion object {
        private val KEY_MANAGER = stringPreferencesKey("manager_key")
    }

    val managerKeyFlow: Flow<String?> =
        context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_MANAGER]
        }

    suspend fun saveManagerKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MANAGER] = key.trim()
        }
    }

    suspend fun clearKey() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_MANAGER)
        }
    }
}
