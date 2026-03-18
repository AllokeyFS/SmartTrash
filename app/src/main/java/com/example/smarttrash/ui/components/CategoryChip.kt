package com.example.smarttrash.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smarttrash.R
import com.example.smarttrash.ui.theme.*

val wasteCategories = listOf(
    "All", "Plastic", "Glass", "Organic", "Paper", "Metal", "Hazardous"
)

fun categoryColor(category: String): Color = when (category) {
    "Plastic"   -> ColorPlastic
    "Glass"     -> ColorGlass
    "Organic"   -> ColorOrganic
    "Paper"     -> ColorPaper
    "Metal"     -> ColorMetal
    "Hazardous" -> ColorHazard
    else        -> ColorDefault
}

fun categoryEmoji(category: String): String = when (category) {
    "Plastic"   -> "🧴"
    "Glass"     -> "🍶"
    "Organic"   -> "🌿"
    "Paper"     -> "📄"
    "Metal"     -> "🥫"
    "Hazardous" -> "⚠️"
    else        -> "♻️"
}

@Composable
fun categoryLocalizedName(category: String): String = when (category) {
    "All"       -> stringResource(R.string.category_all)
    "Plastic"   -> stringResource(R.string.category_plastic)
    "Glass"     -> stringResource(R.string.category_glass)
    "Organic"   -> stringResource(R.string.category_organic)
    "Paper"     -> stringResource(R.string.category_paper)
    "Metal"     -> stringResource(R.string.category_metal)
    "Hazardous" -> stringResource(R.string.category_hazardous)
    else        -> category
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick  = onClick,
        label    = {
            Text("${categoryEmoji(category)} ${categoryLocalizedName(category)}")
        },
        modifier = modifier.padding(horizontal = 4.dp),
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = categoryColor(category).copy(alpha = 0.85f),
            selectedLabelColor     = Color.White
        )
    )
}