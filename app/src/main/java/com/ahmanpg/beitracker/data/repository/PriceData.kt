package com.ahmanpg.beitracker.data.repository

data class PriceData(
    val title: String,
    val price: Double,
    val changeType: String = "SAME",
    val changeAmount: Double = 0.0,
    val url: String,
    val imageUrl: String? = null,
    val source: String = "jiji",
    val timestamp: Long = System.currentTimeMillis()
)
