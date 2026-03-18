package com.example.smarttrash.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.data.repository.WasteRepository
import com.example.smarttrash.location.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MapUiState {
    data object Loading : MapUiState
    data object LocationNotAvailable : MapUiState
    data class Success(
        val nearbyBins: List<RecyclingBinEntity>,
        val userLocation: Location
    ) : MapUiState
    data class Error(val message: String) : MapUiState
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: WasteRepository,
    private val locationService: LocationService
) : ViewModel() {

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 10_000L // каждые 10 сек
    }

    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val mapUiState: StateFlow<MapUiState> = _mapUiState.asStateFlow()

    private val _selectedWasteTypeFilter = MutableStateFlow<String?>(null)
    val selectedWasteTypeFilter: StateFlow<String?> =
        _selectedWasteTypeFilter.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow(5f)
    val searchRadiusKm: StateFlow<Float> = _searchRadiusKm.asStateFlow()

    fun onRadiusChanged(radiusKm: Float) {
        _searchRadiusKm.value = radiusKm
        lastKnownLocation?.let { loadNearbyBins(it) }
    }

    private var lastKnownLocation: Location? = null

    val allBins: StateFlow<List<RecyclingBinEntity>> = repository
        .getAllRecyclingBins()
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    init {
        syncBinsFromRemote()
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            try {
                locationService
                    .getLocationUpdates(LOCATION_UPDATE_INTERVAL_MS)
                    .catch { e ->
                        _mapUiState.value = MapUiState.Error(
                            e.message ?: "Location error"
                        )
                    }
                    .collect { location ->
                        lastKnownLocation = location
                        loadNearbyBins(location)
                    }
            } catch (e: SecurityException) {
                _mapUiState.value = MapUiState.LocationNotAvailable
            }
        }
    }
    fun fetchLocationOnce() {
        viewModelScope.launch {
            _mapUiState.value = MapUiState.Loading
            try {
                val location = locationService.getLastLocation()
                if (location != null) {
                    lastKnownLocation = location
                    loadNearbyBins(location)
                } else {
                    _mapUiState.value = MapUiState.LocationNotAvailable
                }
            } catch (e: SecurityException) {
                _mapUiState.value = MapUiState.LocationNotAvailable
            }
        }
    }

    fun onLocationReceived(location: Location) {
        lastKnownLocation = location
        loadNearbyBins(location)
    }

    fun onWasteTypeFilterSelected(wasteType: String?) {
        _selectedWasteTypeFilter.value = wasteType
        // Применяем фильтр к уже известной локации
        lastKnownLocation?.let { loadNearbyBins(it) }
    }

    private fun loadNearbyBins(location: Location) {
        viewModelScope.launch {
            try {
                var nearbyBins = repository.getBinsNearLocation(
                    location  = location,
                    radiusKm  = _searchRadiusKm.value.toDouble()
                )

                val filter = _selectedWasteTypeFilter.value
                if (filter != null) {
                    nearbyBins = nearbyBins.filter { bin ->
                        bin.acceptedTypes.any { it.equals(filter, ignoreCase = true) }
                    }
                }

                _mapUiState.value = MapUiState.Success(
                    nearbyBins   = nearbyBins,
                    userLocation = location
                )
            } catch (e: Exception) {
                _mapUiState.value = MapUiState.Error(e.message ?: "Failed")
            }
        }
    }

    private fun syncBinsFromRemote() {
        viewModelScope.launch {
            repository.syncRecyclingBinsFromRemote()
        }
    }
}