package com.example.smarttrash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttrash.data.local.entity.WasteItemEntity
import com.example.smarttrash.data.repository.WasteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WasteUiState {
    data object Loading : WasteUiState
    data class Success(val items: List<WasteItemEntity>) : WasteUiState
    data class Error(val message: String) : WasteUiState
}

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data object Success : SyncState
    data class Error(val message: String) : SyncState
}

@HiltViewModel
class WasteViewModel @Inject constructor(
    private val repository: WasteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * Комбинируем searchQuery + selectedCategory в один Flow.
     * combine() реагирует на изменение ЛЮБОГО из двух.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WasteUiState> = combine(
        _searchQuery.debounce(300L),
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        repository.getAllWasteItems()
            .map { allItems ->
                var filtered = allItems
                if (category != null) {
                    filtered = filtered.filter {
                        it.category.equals(category, ignoreCase = true)
                    }
                }
                if (query.isNotBlank()) {
                    val lowerQuery = query.lowercase().trim()
                    filtered = filtered.filter { item ->
                        val nameLower = item.name.lowercase()
                        val categoryLower = item.category.lowercase()

                        val exactMatch = nameLower.contains(lowerQuery) ||
                                categoryLower.contains(lowerQuery)

                        val fuzzyMatch = nameLower.split(" ").any { word ->
                            word.levenshteinSimilarity(lowerQuery) >= 0.6
                        }

                        exactMatch || fuzzyMatch
                    }.sortedByDescending { item ->
                        val nameLower = item.name.lowercase()
                        when {
                            nameLower.startsWith(lowerQuery) -> 3.0
                            nameLower.contains(lowerQuery)   -> 2.0
                            else -> nameLower.split(" ").maxOf { word ->
                                word.levenshteinSimilarity(lowerQuery)
                            }
                        }
                    }
                }

                WasteUiState.Success(filtered) as WasteUiState
            }
            .onStart { emit(WasteUiState.Loading) }
            .catch { e -> emit(WasteUiState.Error(e.message ?: "Error")) }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000L),
        initialValue = WasteUiState.Loading
    )

    private fun String.levenshteinSimilarity(other: String): Double {
        if (this.isEmpty() && other.isEmpty()) return 1.0
        val maxLength = maxOf(this.length, other.length)
        if (maxLength == 0) return 1.0
        return 1.0 - (levenshteinDistance(other).toDouble() / maxLength)
    }

    private fun String.levenshteinDistance(other: String): Int {
        if (this.isEmpty()) return other.length
        if (other.isEmpty()) return this.length
        if (this == other) return 0
        val shorter = if (this.length <= other.length) this else other
        val longer  = if (this.length <= other.length) other else this
        var previousRow = IntArray(shorter.length + 1) { it }
        var currentRow  = IntArray(shorter.length + 1)
        for (i in 1..longer.length) {
            currentRow[0] = i
            for (j in 1..shorter.length) {
                val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
                currentRow[j] = minOf(
                    currentRow[j - 1] + 1,
                    previousRow[j] + 1,
                    previousRow[j - 1] + cost
                )
            }
            val temp = previousRow; previousRow = currentRow; currentRow = temp
        }
        return previousRow[shorter.length]
    }

    init {
        syncFromRemote()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun syncFromRemote() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = repository.syncWasteItemsFromRemote()
            _syncState.value = when {
                result.isSuccess -> SyncState.Success
                else -> SyncState.Error(
                    result.exceptionOrNull()?.message ?: "Sync failed"
                )
            }
        }
    }

    suspend fun getItemById(id: String) = repository.getWasteItemById(id)
}