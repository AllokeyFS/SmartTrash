package com.example.smarttrash.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension to create DataStore
private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        //Key to store settings
        val KEY_DARK_THEME    = booleanPreferencesKey("dark_theme")
        val KEY_LANGUAGE      = stringPreferencesKey("language")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_TOTAL_SORTED  = stringPreferencesKey("total_sorted_count")
    }

    // Theme

    /**
     * null = system
     * true = always dark
     * false = always light
     */
    val darkThemeFlow: Flow<Boolean?> = context.dataStore.data
        .map { prefs ->
            prefs[KEY_DARK_THEME]
        }

    suspend fun setDarkTheme(isDark: Boolean?) {
        context.dataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(KEY_DARK_THEME)
            } else {
                prefs[KEY_DARK_THEME] = isDark
            }
        }
    }

    // Language

    val languageFlow: Flow<String> = context.dataStore.data
        .map { prefs ->
            prefs[KEY_LANGUAGE] ?: "en"
        }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    // Notification - didn't finish

    val notificationsFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[KEY_NOTIFICATIONS] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS] = enabled
        }
    }
}