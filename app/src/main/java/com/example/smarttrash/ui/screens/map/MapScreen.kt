package com.example.smarttrash.ui.screens.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smarttrash.R
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.ui.components.CategoryChip
import com.example.smarttrash.ui.components.wasteCategories
import com.example.smarttrash.ui.viewmodel.MapUiState
import com.example.smarttrash.ui.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {

    val context        = LocalContext.current
    val mapUiState     by viewModel.mapUiState.collectAsState()
    val selectedFilter by viewModel.selectedWasteTypeFilter.collectAsState()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsState()

    var selectedBin by remember { mutableStateOf<RecyclingBinEntity?>(null) }

    val locationPermission = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchLocationOnce()
            viewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            viewModel.fetchLocationOnce()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = mapUiState) {
                is MapUiState.Loading -> {
                    Column(
                        modifier            = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text  = stringResource(R.string.map_finding_location),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is MapUiState.LocationNotAvailable -> {
                    Column(
                        modifier            = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text  = "📍",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text       = stringResource(R.string.map_permission_title),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            text      = stringResource(R.string.map_permission_desc),
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick  = { locationPermission.launchPermissionRequest() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.map_grant_permission))
                        }
                    }
                }
                is MapUiState.Success -> {
                    val userLatLng = LatLng(
                        state.userLocation.latitude,
                        state.userLocation.longitude
                    )
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
                    }

                    LaunchedEffect(state.userLocation) {
                        cameraState.position =
                            CameraPosition.fromLatLngZoom(userLatLng, 13f)
                    }

                    GoogleMap(
                        modifier            = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        properties          = MapProperties(
                            isMyLocationEnabled = true
                        ),
                        uiSettings          = MapUiSettings(
                            zoomControlsEnabled     = true,
                            myLocationButtonEnabled = true
                        )
                    ) {
                        state.nearbyBins.forEach { bin ->
                            Marker(
                                state   = MarkerState(
                                    position = LatLng(bin.latitude, bin.longitude)
                                ),
                                title   = bin.address,
                                snippet = stringResource(R.string.map_accepts) +
                                        " ${bin.acceptedTypes.joinToString(", ")}",
                                onClick = {
                                    selectedBin = bin
                                    false
                                }
                            )
                        }
                    }

                    LazyRow(
                        modifier       = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(listOf("All") + wasteCategories.drop(1)) { category ->
                            CategoryChip(
                                category   = category,
                                isSelected = if (category == "All")
                                    selectedFilter == null
                                else
                                    selectedFilter == category,
                                onClick    = {
                                    viewModel.onWasteTypeFilterSelected(
                                        if (category == "All") null else category
                                    )
                                }
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    text       = stringResource(
                                        R.string.map_bins_found,
                                        state.nearbyBins.size,
                                        searchRadiusKm.toInt()
                                    ),
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text  = stringResource(
                                        R.string.map_radius,
                                        searchRadiusKm.toInt()
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Slider(
                                value         = searchRadiusKm,
                                onValueChange = { viewModel.onRadiusChanged(it) },
                                valueRange    = 1f..20f,
                                steps         = 18,
                                modifier      = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text  = "1 km",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text  = "20 km",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            if (state.nearbyBins.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = stringResource(R.string.map_tap_marker),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is MapUiState.Error -> {
                    Column(
                        modifier            = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text  = "⚠️",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Text(
                            text      = state.message,
                            color     = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { viewModel.fetchLocationOnce() }) {
                            Text(stringResource(R.string.btn_retry))
                        }
                    }
                }
            }

            selectedBin?.let { bin ->
                AlertDialog(
                    onDismissRequest = { selectedBin = null },
                    title = {
                        Text(
                            text       = "♻️ Recycling Bin",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text  = "📍 ${bin.address}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text  = stringResource(R.string.map_accepts),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            bin.acceptedTypes.forEach { type ->
                                Text(
                                    text  = "  • $type",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "google.navigation:q=${bin.latitude}," +
                                            "${bin.longitude}&mode=w"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val browserUri = Uri.parse(
                                        "https://www.google.com/maps/dir/?api=1" +
                                                "&destination=${bin.latitude},${bin.longitude}"
                                    )
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, browserUri)
                                    )
                                }
                                selectedBin = null
                            }
                        ) {
                            Text(stringResource(R.string.map_navigate))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedBin = null }) {
                            Text(stringResource(R.string.map_close))
                        }
                    }
                )
            }
        }
    }
}