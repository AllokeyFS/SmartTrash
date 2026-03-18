package com.example.smarttrash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smarttrash.data.local.converter.StringListConverter
import com.example.smarttrash.data.local.dao.RecyclingBinDao
import com.example.smarttrash.data.local.dao.WasteItemDao
import com.example.smarttrash.data.local.entity.RecyclingBinEntity
import com.example.smarttrash.data.local.entity.WasteItemEntity


//Local DB
@Database(
    entities = [
        WasteItemEntity::class,
        RecyclingBinEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wasteItemDao(): WasteItemDao
    abstract fun recyclingBinDao(): RecyclingBinDao

    companion object {
        const val DATABASE_NAME = "smart_trash_db"
    }
}