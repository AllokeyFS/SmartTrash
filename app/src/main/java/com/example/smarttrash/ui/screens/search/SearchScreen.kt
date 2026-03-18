package com.example.smarttrash.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smarttrash.R
import com.example.smarttrash.ui.components.*
import com.example.smarttrash.ui.navigation.Screen
import com.example.smarttrash.ui.viewmodel.WasteUiState
import com.example.smarttrash.ui.viewmodel.WasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: WasteViewModel = hiltViewModel()
) {
    val query       by viewModel.searchQuery.collectAsState()
    val uiState     by viewModel.uiState.collectAsState()
    val syncState   by viewModel.syncState.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
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
        ) {
            SyncStatusBanner(syncState = syncState)

            TrashSearchBar(
                query         = query,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier      = Modifier.padding(
                    horizontal = 16.dp,
                    vertical   = 8.dp
                ),
                placeholder   = stringResource(R.string.search_placeholder)
            )

            LazyRow(
                contentPadding        = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(wasteCategories) { category ->
                    val isSelected = when (category) {
                        "All" -> selectedCat == null
                        else  -> selectedCat == category
                    }
                    CategoryChip(
                        category   = category,
                        isSelected = isSelected,
                        onClick    = {
                            viewModel.onCategorySelected(
                                if (category == "All") null else category
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            when (val state = uiState) {

                is WasteUiState.Loading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text  = stringResource(R.string.loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is WasteUiState.Error -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier            = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text  = "⚠️",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text  = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = { viewModel.syncFromRemote() }) {
                                Text(stringResource(R.string.btn_retry))
                            }
                        }
                    }
                }

                is WasteUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text  = "🔍",
                                    style = MaterialTheme.typography.displaySmall
                                )
                                Text(
                                    text  = if (query.isBlank())
                                        stringResource(R.string.search_empty_db)
                                    else
                                        stringResource(R.string.search_no_results, query),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (query.isNotBlank()) {
                                    Text(
                                        text  = stringResource(R.string.search_try_spelling),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text     = stringResource(
                                R.string.search_items_found,
                                state.items.size
                            ),
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical   = 4.dp
                            )
                        )

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(
                                items = state.items,
                                key   = { it.id }
                            ) { item ->
                                WasteItemCard(
                                    item    = item,
                                    onClick = {
                                        navController.navigate(
                                            Screen.Detail.createRoute(item.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}