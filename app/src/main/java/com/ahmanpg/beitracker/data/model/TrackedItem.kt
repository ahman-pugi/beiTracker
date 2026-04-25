package com.ahmanpg.beitracker.data.model

import com.ahmanpg.beitracker.data.repository.PriceData
import com.ahmanpg.beitracker.util.PriceUtils

data class TrackedItem(
    val url: String,
    val name: String,
    val currentPrice: Double = 0.0,
    val previousPrice: Double = currentPrice,
    val source: String = "Jiji",
    val quantity: Int = 1,
    val history: List<Double> = emptyList(),
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val sellerName: String? = null,
    val sellerPhone: String? = null,
    val rating: Double? = null,
    val category: String? = null,
    val targetPrice: Double? = null,
    val isAlertEnabled: Boolean = true,
    val isTracked: Boolean = false,
    val alertCondition: String = "BELOW", // BELOW, ABOVE, ANY
    val notifyPush: Boolean = true,
    val notifySms: Boolean = false,
    val notifyEmail: Boolean = false,
    val notifyWhatsapp: Boolean = false,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val avgPrice: Double? = null,
    val recommendation: String = "WAIT", // BUY, WAIT, AVOID
    val trend: String = "STABLE", // UP, DOWN, STABLE
    val priceHistory: List<PriceData> = emptyList(),
    val manualManufactureYear: Int? = null
) {
    val formattedCurrentPrice: String
        get() = if (currentPrice > 0) "${String.format("%,.0f", currentPrice)}/=" else "N/A"

    val changePercent: Double?
        get() {
            if (previousPrice <= 0.0) return null
            return ((currentPrice - previousPrice) / previousPrice) * 100.0
        }

    /**
     * Extracts the manufacture year from the product name if it's a vehicle.
     * Looks for 4-digit numbers starting with 19 or 20.
     */
    val manufactureYear: Int?
        get() {
            val regex = Regex("\\b(19|20)\\d{2}\\b")
            return regex.find(name)?.value?.toIntOrNull()
        }

    /**
     * Extracts dimensions (e.g., 5x6x8, 4*6) from the product name.
     * Useful for mattresses and materials.
     */
    val dimensions: String?
        get() {
            val regex = Regex("\\b\\d+(?:\\.\\d+)?\\s*[x*]\\s*\\d+(?:\\.\\d+)?(?:\\s*[x*]\\s*\\d+(?:\\.\\d+)?)?\\b", RegexOption.IGNORE_CASE)
            return regex.find(name)?.value?.replace(" ", "")?.lowercase()
        }

    /**
     * Calculates the real price drop percentage using available historical data.
     * No simulated fallbacks.
     */
    val flashSaleDropPercent: Double
        get() {
            // 1. Check full price history first (most accurate)
            if (priceHistory.isNotEmpty()) {
                return PriceUtils.calculatePriceDropPercentage(priceHistory)
            }

            // 2. Check raw history points
            if (history.isNotEmpty()) {
                val drop = PriceUtils.calculatePriceDropFromRawHistory(history, currentPrice)
                if (drop > 0) return drop
            }

            // 3. Use market aggregates (max/avg) or previous known price as reference
            val reference = maxPrice ?: avgPrice ?: if (previousPrice > currentPrice) previousPrice else null
            
            return if (reference != null && reference > currentPrice) {
                ((reference - currentPrice) / reference) * 100.0
            } else 0.0
        }

    val priceChangeEmoji: String
        get() {
            val change = changePercent ?: return ""
            return when {
                change < -5.0 -> "🔥"       // big drop
                change < 0.0 -> "📉"          // small drop
                change > 5.0 -> "📈"          // big rise
                change > 0.0 -> "📊"          // small rise
                else -> "➖"                   // no change
            }
        }
}
