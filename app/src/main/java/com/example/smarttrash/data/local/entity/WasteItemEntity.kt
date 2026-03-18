package com.example.smarttrash.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "waste_items")
data class WasteItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val instructions: String,
    val nameHu: String = "",          // Hungarian namings
    val instructionsHu: String = "",  // Hungarian instructions
    val lastUpdated: Long = System.currentTimeMillis()
)