package com.ahmanpg.beitracker.data.model

data class TrackedItem(
    val url: String,
    val name: String,
    val currentPrice: Double? = null,
    val source: String = "",
    val quantity: Int = 1,
    val history: List<Double> = emptyList(),
    val previousPrice: Double
) {
    val formattedCurrentPrice: String
        get() = currentPrice?.let { "TZS ${String.format("%,.0f", it)}" } ?: "N/A"

    val changePercent: Double?
        get() {
            if (history.size < 2) return null
            val first = history.first()
            if (first == 0.0) return null
            val last = history.last()
            return ((last - first) / first) * 100.0
        }
}
