package com.example.smarttrash.data.local.dao

import androidx.room.*
import com.example.smarttrash.data.local.entity.WasteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteItemDao {


    // Returning Flow - When DB changes automatically updates UI
    @Query("SELECT * FROM waste_items ORDER BY name ASC")
    fun getAllWasteItems(): Flow<List<WasteItemEntity>>

    // Search by name and category
    @Query("""
        SELECT * FROM waste_items 
        WHERE name LIKE '%' || :query || '%' 
        OR category LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchWasteItems(query: String): Flow<List<WasteItemEntity>>

    @Query("SELECT * FROM waste_items WHERE id = :id")
    suspend fun getWasteItemById(id: String): WasteItemEntity?

    @Query("SELECT * FROM waste_items WHERE category = :category ORDER BY name ASC")
    fun getWasteItemsByCategory(category: String): Flow<List<WasteItemEntity>>

    // Being used to sync with Firebase
    @Upsert
    suspend fun upsertAll(items: List<WasteItemEntity>)

    @Upsert
    suspend fun upsert(item: WasteItemEntity)

    @Delete
    suspend fun delete(item: WasteItemEntity)

    @Query("DELETE FROM waste_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM waste_items")
    suspend fun getCount(): Int
}