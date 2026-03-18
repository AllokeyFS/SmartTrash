package com.example.smarttrash.data.repository

import android.location.Location
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.data.local.entity.WasteItemEntity
import kotlinx.coroutines.flow.Flow

interface WasteRepository {

    // Local DB return flow
    fun getAllWasteItems(): Flow<List<WasteItemEntity>>

    fun searchWasteItems(query: String): Flow<List<WasteItemEntity>>

    fun getWasteItemsByCategory(category: String): Flow<List<WasteItemEntity>>

    suspend fun getWasteItemById(id: String): WasteItemEntity?

    // Firebase Sync
    suspend fun syncWasteItemsFromRemote(): Result<Unit>

    fun getAllRecyclingBins(): Flow<List<RecyclingBinEntity>>

    // 5km range bins
    suspend fun getBinsNearLocation(
        location: Location,
        radiusKm: Double = 5.0
    ): List<RecyclingBinEntity>

    fun getBinsByAcceptedType(wasteType: String): Flow<List<RecyclingBinEntity>>

    suspend fun syncRecyclingBinsFromRemote(): Result<Unit>
}