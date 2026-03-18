package com.example.smarttrash.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarttrash.data.local.entity.WasteItemEntity

@Composable
fun WasteItemCard(
    item: WasteItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale      = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val isHungarian = locale.language == "hu"

    val displayName = if (isHungarian && item.nameHu.isNotBlank())
        item.nameHu else item.name
    val displayInstructions = if (isHungarian && item.instructionsHu.isNotBlank())
        item.instructionsHu else item.instructions

    Card(
        onClick   = onClick,
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape    = MaterialTheme.shapes.medium,
                color    = categoryColor(item.category).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text  = categoryEmoji(item.category),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = displayName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = categoryColor(item.category).copy(alpha = 0.12f)
                ) {
                    Text(
                        text     = categoryLocalizedName(item.category),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = categoryColor(item.category),
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical   = 2.dp
                        )
                    )
                }
                if (displayInstructions.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = displayInstructions,
                        style    = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text  = "›",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}