package com.example.smarttrash.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarttrash.ui.viewmodel.SyncState

@Composable
fun SyncStatusBanner(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = syncState is SyncState.Syncing || syncState is SyncState.Error,
        enter    = slideInVertically() + fadeIn(),
        exit     = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = when (syncState) {
                is SyncState.Syncing -> MaterialTheme.colorScheme.primaryContainer
                is SyncState.Error   -> MaterialTheme.colorScheme.errorContainer
                else                 -> MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (syncState is SyncState.Syncing) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(
                    text  = when (syncState) {
                        is SyncState.Syncing -> "☁️ Syncing with cloud..."
                        is SyncState.Error   -> "⚠️ Offline mode: ${syncState.message}"
                        else                 -> ""
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}