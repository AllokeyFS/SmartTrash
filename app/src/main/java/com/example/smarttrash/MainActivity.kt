package com.example.smarttrash

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.smarttrash.data.preferences.UserPreferences
import com.example.smarttrash.ui.navigation.SmartTrashNavGraph
import com.example.smarttrash.ui.theme.SmartTrashTheme
import com.example.smarttrash.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore the language at startup
        lifecycleScope.launch {
            val savedLanguage = userPreferences.languageFlow.first()
            if (savedLanguage != "en") {
                val localeList = LocaleListCompat.forLanguageTags(savedLanguage)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val darkThemeSetting by settingsViewModel.darkTheme.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDark = when (darkThemeSetting) {
                true  -> true
                false -> false
                null  -> systemDark
            }

            SmartTrashTheme(
                darkTheme    = isDark,
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                SmartTrashNavGraph(navController = navController)
            }
        }
    }
}