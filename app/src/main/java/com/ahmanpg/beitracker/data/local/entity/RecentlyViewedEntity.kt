package com.ahmanpg.beitracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val url: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val source: String = "Jiji",
    val viewedAt: Long = System.currentTimeMillis()
)
