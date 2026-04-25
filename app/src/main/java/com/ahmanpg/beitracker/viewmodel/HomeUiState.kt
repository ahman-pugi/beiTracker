package com.ahmanpg.beitracker.viewmodel

import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity

data class CategoryDeals(
    val title: String,
    val items: List<TrackedItem>
)

data class HomeUiState(
    val userName: String = "",
    val watchlistSummary: String? = null,
    val featuredPriceDrops: List<TrackedItem> = emptyList(),
    val watchlist: List<TrackedItem> = emptyList(),
    val categories: List<CategoryDeals> = emptyList(),
    val alerts: List<PriceAlertEntity> = emptyList()
)
