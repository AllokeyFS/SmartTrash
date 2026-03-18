package com.example.smarttrash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.smarttrash.data.local.converter.StringListConverter

@Entity(tableName = "recycling_bins")
@TypeConverters(StringListConverter::class)
data class RecyclingBinEntity(
    @PrimaryKey
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val acceptedTypes: List<String>,
    val address: String,
    val lastUpdated: Long = System.currentTimeMillis()
)