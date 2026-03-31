package com.ahmanpg.beitracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import okio.Source

@Entity(tableName = "price_history")
data class PriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productUrl: String,
    val title: String,
    val price: Double?,
    val timestamp: Long,
    val source: String // Jiji, Jumia, Darshopping etc
)
