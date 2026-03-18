package com.example.smarttrash.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для получения геолокации.
 * Абстрагирует FusedLocationProvider — удобно для тестов.
 */
interface LocationService {

    // Returns the Flow with location updates. Automatically stops when the Flow is canceled.
    fun getLocationUpdates(intervalMs: Long): Flow<Location>

    //One-time request for the last known location. Returns null if the location is unavailable.
    suspend fun getLastLocation(): Location?
}