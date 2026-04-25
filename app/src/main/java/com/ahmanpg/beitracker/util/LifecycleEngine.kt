package com.ahmanpg.beitracker.util

import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.repository.PriceData
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class LifecycleInsight(
    val stage: LifecycleStage,
    val message: String,
    val progress: Float, // 0.0 to 1.0 within the stage
    val seasonalFactor: String? = null,
    val nextExpectedDrop: String? = null
)

enum class LifecycleStage {
    NEW_ENTRY,      // Just hit the market, high price
    PEAK_POPULARITY, // Stable but high price
    MATURE,         // Frequent drops, good time to buy
    END_OF_LIFE,    // Deep discounts, newer model likely out
    LEGACY          // Rare, collectors or overpriced old stock
}

object LifecycleEngine {

    fun analyze(item: TrackedItem): LifecycleInsight {
        val history = item.priceHistory
        val currentPrice = item.currentPrice
        
        // 1. Determine "Age" in our system
        val firstSeen = history.minByOrNull { it.timestamp }?.timestamp ?: System.currentTimeMillis()
        val ageDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - firstSeen)
        
        // 2. Determine Price Position relative to historical max
        val maxPrice = item.maxPrice ?: currentPrice
        val minPrice = item.minPrice ?: currentPrice
        val priceDropFromMax = if (maxPrice > 0) (maxPrice - currentPrice) / maxPrice else 0.0
        
        // 3. Logic for Stage Determination
        return when {
            ageDays < 14 && priceDropFromMax < 0.05 -> {
                LifecycleInsight(
                    stage = LifecycleStage.NEW_ENTRY,
                    message = "Fresh on the market. Prices are currently at a premium.",
                    progress = (ageDays / 14f).coerceIn(0f, 1f),
                    nextExpectedDrop = "Expected in 4-6 weeks"
                )
            }
            priceDropFromMax > 0.30 -> {
                LifecycleInsight(
                    stage = LifecycleStage.END_OF_LIFE,
                    message = "Deeply discounted. A newer model might be available soon.",
                    progress = 0.9f,
                    seasonalFactor = "Clearance phase"
                )
            }
            priceDropFromMax > 0.15 -> {
                LifecycleInsight(
                    stage = LifecycleStage.MATURE,
                    message = "Settled market price. This is typically the best time to buy.",
                    progress = 0.6f,
                    nextExpectedDrop = "Minor fluctuations likely"
                )
            }
            else -> {
                LifecycleInsight(
                    stage = LifecycleStage.PEAK_POPULARITY,
                    message = "High demand. Prices are stable but staying high.",
                    progress = 0.3f,
                    seasonalFactor = "Peak Cycle"
                )
            }
        }
    }
    
    fun getSeasonalContext(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        
        return when (month) {
            Calendar.NOVEMBER, Calendar.DECEMBER -> "Holiday season: High volatility, look for flash sales."
            Calendar.JANUARY -> "Post-holiday slump: Great for clearing old stock."
            Calendar.SEPTEMBER -> "Back to school/tech season: New models often announced."
            else -> "Standard market cycle."
        }
    }
}
