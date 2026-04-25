package com.ahmanpg.beitracker.data.model

data class SearchQuery(
    val keyword: String,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sort: SortType = SortType.RELEVANCE
)

enum class SortType {
    RELEVANCE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}
