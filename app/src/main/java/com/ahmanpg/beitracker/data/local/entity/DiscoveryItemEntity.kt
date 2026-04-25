package com.ahmanpg.beitracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discovery_items")
data class DiscoveryItemEntity(
    @PrimaryKey val url: String,
    val name: String,
    val currentPrice: Double,
    val previousPrice: Double = 0.0,
    val imageUrl: String? = null,
    val source: String,
    val section: String, // e.g., "POPULAR", "TOP_DROPS", "FEATURED", "CAT_Smartphones", etc.
    val updatedAt: Long = System.currentTimeMillis()
)
