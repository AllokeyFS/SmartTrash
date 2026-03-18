package com.example.smarttrash.data.repository

import android.location.Location
import com.example.smarttrash.data.local.dao.RecyclingBinDao
import com.example.smarttrash.data.local.dao.WasteItemDao
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.data.local.entity.WasteItemEntity
import com.example.smarttrash.data.remote.FirestoreDataSource
import com.example.smarttrash.data.util.levenshteinSimilarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class WasteRepositoryImpl @Inject constructor(
    private val wasteItemDao: WasteItemDao,
    private val recyclingBinDao: RecyclingBinDao,
    private val firestoreDataSource: FirestoreDataSource
) : WasteRepository {

    companion object {
        private const val SEARCH_RADIUS_KM = 5.0
        private const val FUZZY_SIMILARITY_THRESHOLD = 0.6
        private const val BOUNDING_BOX_DEGREES = 0.05
    }

    override fun getAllWasteItems(): Flow<List<WasteItemEntity>> =
        wasteItemDao.getAllWasteItems()


    /**
     * OFFLINE-FIRST + FUZZY SEARCH стратегия:
     * 1. Читаем из Room (мгновенно, без сети)
     * 2. Применяем Levenshtein фильтрацию к результатам
     *
     * Почему не в SQL: SQLite не поддерживает Levenshtein нативно.
     * Bounding фильтр через LIKE делает предварительный отбор быстрее.
     */
    override fun searchWasteItems(query: String): Flow<List<WasteItemEntity>> {
        if (query.isBlank()) return getAllWasteItems()

        return wasteItemDao.getAllWasteItems().map { allItems ->
            val lowerQuery = query.lowercase().trim()

            allItems.filter { item ->
                val nameLower = item.name.lowercase()
                val categoryLower = item.category.lowercase()

                // Prioritizing the exact similarity
                val exactMatch = nameLower.contains(lowerQuery) ||
                        categoryLower.contains(lowerQuery)

                val fuzzyMatch = nameLower.split(" ").any { word ->
                    word.levenshteinSimilarity(lowerQuery) >= FUZZY_SIMILARITY_THRESHOLD
                }

                exactMatch || fuzzyMatch
            }.sortedByDescending { item ->
                val nameLower = item.name.lowercase()
                when {
                    nameLower.startsWith(lowerQuery) -> 3.0
                    nameLower.contains(lowerQuery) -> 2.0
                    else -> nameLower.split(" ").maxOf { word ->
                        word.levenshteinSimilarity(lowerQuery)
                    }
                }
            }
        }
    }

    override fun getWasteItemsByCategory(category: String): Flow<List<WasteItemEntity>> =
        wasteItemDao.getWasteItemsByCategory(category)

    override suspend fun getWasteItemById(id: String): WasteItemEntity? =
        wasteItemDao.getWasteItemById(id)

    // First checking data in Room
    /**1) No data (first launch) - waiting the Firebase
     * 2) If there - returning Room, Firebase syncing in the bg
     */
    override suspend fun syncWasteItemsFromRemote(): Result<Unit> {
        return firestoreDataSource.fetchAllWasteItems().fold(
            onSuccess = { remoteItems ->
                wasteItemDao.upsertAll(remoteItems)
                Result.success(Unit)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    override fun getAllRecyclingBins(): Flow<List<RecyclingBinEntity>> =
        recyclingBinDao.getAllRecyclingBins()

    /**
     * GEOSPATIAL FILTERING
     *
     * Stage 1 — Bounding Box (SQL, very fast):
     *   Rectangle around the user, cuts off 99% of distant points
     *
     * Stage 2 — Haversine Formula
     *   Calculates the actual distance across the Earth's sphere for the remaining points.
     *   Formula: a = sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlon/2)
     *             c = 2·atan2(√a, √(1−a))
     *             d = R·c  (R = 6371 km)
     */
    override suspend fun getBinsNearLocation(
        location: Location,
        radiusKm: Double
    ): List<RecyclingBinEntity> {
        val userLat = location.latitude
        val userLng = location.longitude

        // Bounding box динамический — зависит от радиуса
        val degreesPerKm = 0.009
        val boundingBox  = radiusKm * degreesPerKm

        val candidateBins = recyclingBinDao.getBinsInBoundingBox(
            minLat = userLat - boundingBox,
            maxLat = userLat + boundingBox,
            minLng = userLng - boundingBox,
            maxLng = userLng + boundingBox
        )

        return candidateBins.filter { bin ->
            haversineDistanceKm(
                lat1 = userLat, lon1 = userLng,
                lat2 = bin.latitude, lon2 = bin.longitude
            ) <= radiusKm
        }.sortedBy { bin ->
            haversineDistanceKm(userLat, userLng, bin.latitude, bin.longitude)
        }
    }

    override fun getBinsByAcceptedType(wasteType: String): Flow<List<RecyclingBinEntity>> =
        recyclingBinDao.getBinsByAcceptedType(wasteType)

    override suspend fun syncRecyclingBinsFromRemote(): Result<Unit> {
        return firestoreDataSource.fetchAllRecyclingBins().fold(
            onSuccess = { remoteBins ->
                recyclingBinDao.upsertAll(remoteBins)
                Result.success(Unit)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    private fun haversineDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}