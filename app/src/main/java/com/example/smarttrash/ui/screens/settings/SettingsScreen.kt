package com.example.smarttrash.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttrash.R
import com.example.smarttrash.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context              = LocalContext.current
    val darkTheme            by viewModel.darkTheme.collectAsState()
    val language             by viewModel.language.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            SettingsSectionHeader(
                title = stringResource(R.string.settings_appearance)
            )

            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text       = stringResource(R.string.settings_theme),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(modifier = Modifier.selectableGroup()) {
                        ThemeOption(
                            label    = stringResource(R.string.settings_theme_dark),
                            selected = darkTheme == true,
                            onClick  = { viewModel.setDarkTheme(true) }
                        )
                        ThemeOption(
                            label    = stringResource(R.string.settings_theme_light),
                            selected = darkTheme == false,
                            onClick  = { viewModel.setDarkTheme(false) }
                        )
                        ThemeOption(
                            label    = stringResource(R.string.settings_theme_system),
                            selected = darkTheme == null,
                            onClick  = { viewModel.setDarkTheme(null) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSectionHeader(
                title = stringResource(R.string.settings_language)
            )

            SettingsCard {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .selectableGroup()
                ) {
                    LanguageOption(
                        flag     = "🇬🇧",
                        label    = "English",
                        selected = language == "en",
                        onClick  = {
                            viewModel.applyLanguage(context, "en")
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LanguageOption(
                        flag     = "🇭🇺",
                        label    = "Magyar (Hungarian)",
                        selected = language == "hu",
                        onClick  = {
                            viewModel.applyLanguage(context, "hu")
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSectionHeader(
                title = stringResource(R.string.settings_notifications)
            )

            SettingsCard {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Notifications,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text       = stringResource(R.string.settings_notif_title),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text  = stringResource(R.string.settings_notif_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked         = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSectionHeader(
                title = stringResource(R.string.settings_about)
            )

            SettingsCard {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AboutRow(
                        label = stringResource(R.string.settings_version),
                        value = "1.0.0"
                    )
                    HorizontalDivider()
                    AboutRow(
                        label = stringResource(R.string.settings_developer),
                        value = "Smart Trash Team"
                    )
                    HorizontalDivider()
                    AboutRow(label = "Database",    value = "Firebase Firestore")
                    HorizontalDivider()
                    AboutRow(label = "Maps",        value = "Google Maps SDK")
                    HorizontalDivider()
                    AboutRow(label = "Barcode API", value = "Open Food Facts")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        content()
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick  = onClick,
                role     = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LanguageOption(
    flag: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick  = onClick,
                role     = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = flag,  style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
