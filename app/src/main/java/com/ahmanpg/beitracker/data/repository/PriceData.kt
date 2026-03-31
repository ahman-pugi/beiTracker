package com.ahmanpg.beitracker.data.repository

data class PriceData(
    val title: String,
    val price: Double?,
    val url: String,
    val timestamp: Long? = null
)
