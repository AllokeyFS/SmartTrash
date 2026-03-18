package com.example.smarttrash.data.local.dao

import androidx.room.*
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecyclingBinDao {

    @Query("SELECT * FROM recycling_bins ORDER BY id ASC")
    fun getAllRecyclingBins(): Flow<List<RecyclingBinEntity>>

    @Query("SELECT * FROM recycling_bins WHERE id = :id")
    suspend fun getRecyclingBinById(id: String): RecyclingBinEntity?


    @Query("""
        SELECT * FROM recycling_bins 
        WHERE latitude BETWEEN :minLat AND :maxLat
        AND longitude BETWEEN :minLng AND :maxLng
    """)
    suspend fun getBinsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): List<RecyclingBinEntity>


    // acceptedTypes stored as JSON, using LIKE

    @Query("""
        SELECT * FROM recycling_bins 
        WHERE acceptedTypes LIKE '%' || :wasteType || '%'
        ORDER BY id ASC
    """)
    fun getBinsByAcceptedType(wasteType: String): Flow<List<RecyclingBinEntity>>

    @Upsert
    suspend fun upsertAll(bins: List<RecyclingBinEntity>)

    @Upsert
    suspend fun upsert(bin: RecyclingBinEntity)

    @Delete
    suspend fun delete(bin: RecyclingBinEntity)

    @Query("DELETE FROM recycling_bins")
    suspend fun clearAll()
}