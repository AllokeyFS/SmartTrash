package com.example.smarttrash.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttrash.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val darkTheme: StateFlow<Boolean?> = userPreferences.darkThemeFlow
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = null
        )

    val language: StateFlow<String> = userPreferences.languageFlow
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = "en"
        )

    val notificationsEnabled: StateFlow<Boolean> = userPreferences.notificationsFlow
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = true
        )

    fun setDarkTheme(isDark: Boolean?) {
        viewModelScope.launch {
            userPreferences.setDarkTheme(isDark)
        }
    }

    /**
     * Applying a language via AppCompatDelegate — works on Android 13+
     * This is Google's official method for changing the language in an app.
     */
    fun applyLanguage(context: Context, langCode: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(langCode)
        }

        // Method 1: AppCompatDelegate (Android 13+ / API 33+)
        val localeList = LocaleListCompat.forLanguageTags(langCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }
}