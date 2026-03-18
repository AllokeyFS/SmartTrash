package com.example.smarttrash.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttrash.R
import com.example.smarttrash.ui.components.categoryColor
import com.example.smarttrash.ui.components.categoryEmoji
import com.example.smarttrash.ui.components.categoryLocalizedName
import com.example.smarttrash.ui.navigation.Screen
import com.example.smarttrash.ui.viewmodel.WasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasteDetailScreen(
    itemId: String,
    onBackPressed: () -> Unit,
    navController: NavController,
    viewModel: WasteViewModel = hiltViewModel()
) {
    var itemName         by remember { mutableStateOf("") }
    var itemCategory     by remember { mutableStateOf("") }
    var itemInstructions by remember { mutableStateOf("") }
    var isLoading        by remember { mutableStateOf(true) }

    val locale      = LocalConfiguration.current.locales[0]
    val isHungarian = locale.language == "hu"

    LaunchedEffect(itemId) {
        viewModel.getItemById(itemId)?.let { item ->
            itemName = if (isHungarian && item.nameHu.isNotBlank())
                item.nameHu else item.name

            itemCategory = item.category

            itemInstructions = if (isHungarian && item.instructionsHu.isNotBlank())
                item.instructionsHu else item.instructions
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isLoading) stringResource(R.string.loading) else itemName)
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape    = MaterialTheme.shapes.extraLarge,
                        color    = categoryColor(itemCategory).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text  = categoryEmoji(itemCategory),
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }
                }
                Text(
                    text       = itemName,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.align(Alignment.CenterHorizontally)
                )
                Surface(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape    = MaterialTheme.shapes.large,
                    color    = categoryColor(itemCategory).copy(alpha = 0.15f)
                ) {
                    Text(
                        text       = "${categoryEmoji(itemCategory)} ${categoryLocalizedName(itemCategory)}",
                        color      = categoryColor(itemCategory),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(
                            horizontal = 16.dp,
                            vertical   = 8.dp
                        )
                    )
                }

                HorizontalDivider()
                if (itemInstructions.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    text  = "📋",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text       = stringResource(R.string.detail_how_to_dispose),
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text  = itemInstructions,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Button(
                    onClick  = {
                        navController.navigate(
                            Screen.Map.createRoute(itemCategory)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = categoryColor(itemCategory)
                    )
                ) {
                    Text(
                        text       = stringResource(
                            R.string.detail_find_bin,
                            categoryLocalizedName(itemCategory)
                        ),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}