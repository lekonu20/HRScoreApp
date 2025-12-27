package com.savani.hrscore.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first


private val Context.dataStore by preferencesDataStore(name = "hrscore_prefs")

class KeyStore(private val context: Context) {

    companion object {
        private val KEY_MANAGER = stringPreferencesKey("manager_key")

        // =========================
        // 🔧 TEST MODE
        // true  = bỏ qua KEY (test app)
        // false = bắt buộc KEY (production)
        // =========================
        const val TEST_MODE = true
    }

    /**
     * Flow trả về KEY manager (nullable)
     */
    val managerKeyFlow: Flow<String?> =
        context.dataStore.data.map { prefs: Preferences ->
            prefs[KEY_MANAGER]
        }

    /**
     * Flow kiểm tra có được phép ghi lỗi / điểm hay không
     */
    val canApplyFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            if (TEST_MODE) {
                true
            } else {
                !prefs[KEY_MANAGER].isNullOrBlank()
            }
        }

    /**
     * Check nhanh (dùng trong ViewModel / logic ghi điểm)
     */
    suspend fun canApply(): Boolean {
        if (TEST_MODE) return true

        val prefs = context.dataStore.data.map { it }.first()
        return !prefs[KEY_MANAGER].isNullOrBlank()
    }

    /**
     * Lưu KEY manager
     */
    suspend fun saveManagerKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MANAGER] = key.trim()
        }
    }

    /**
     * Xóa KEY (logout / reset)
     */
    suspend fun clearKey() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_MANAGER)
        }
    }
}
