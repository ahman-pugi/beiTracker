package com.ahmanpg.beitracker.util

import com.ahmanpg.beitracker.data.repository.PriceData
import kotlin.math.abs

data class PriceDropResult(
    val dropPercent: Double,
    val referencePrice: Double,
    val currentPrice: Double,
    val isDrop: Boolean
)

object PriceUtils {
    /**
     * Calculates the price drop percentage using a time-series of prices.
     * 
     * Rules:
     * 1. Sort all price data chronologically.
     * 2. Identify the current price as the last data point.
     * 3. From all previous data points, find the maximum reliable price (reference price).
     * 4. Ignore outliers (sudden spikes > 50% that don't persist).
     */
    fun calculatePriceDrop(history: List<PriceData>): PriceDropResult {
        if (history.isEmpty()) return PriceDropResult(0.0, 0.0, 0.0, false)
        
        // 1. Sort chronologically
        val sorted = history.sortedBy { it.timestamp }
        val currentPrice = sorted.last().price
        
        if (sorted.size < 2) {
            return PriceDropResult(0.0, currentPrice, currentPrice, false)
        }

        val previousData = sorted.dropLast(1)

        // 2. Identify Reference Price (Highest observed BEFORE current)
        // Ignoring outliers: sudden spikes beyond 50% increase from the previous point
        // that don't represent a sustained price level.
        val cleanPrevious = mutableListOf<PriceData>()
        previousData.forEachIndexed { index, point ->
            val p = point.price
            val prevPrice = if (index > 0) cleanPrevious.lastOrNull()?.price ?: p else p
            
            // If price jumps > 50% from previous known good price, check if it stays up
            val isSpike = p > prevPrice * 1.5
            val staysUp = if (isSpike && index < previousData.size - 1) {
                previousData[index + 1].price > prevPrice * 1.2 // Stays at least 20% higher
            } else false

            if (!isSpike || staysUp) {
                cleanPrevious.add(point)
            }
        }

        if (cleanPrevious.isEmpty()) {
            return PriceDropResult(0.0, currentPrice, currentPrice, false)
        }

        // 3. Identify Reference Price (Highest observed BEFORE current)
        // Use the highest historical price as the reference
        val referencePrice = cleanPrevious.maxOf { it.price }

        // 4. Edge Case: currentPrice >= referencePrice
        if (currentPrice >= referencePrice || referencePrice <= 0) {
            return PriceDropResult(0.0, referencePrice, currentPrice, false)
        }

        // 5. Calculate percentage drop
        val dropPercent = ((referencePrice - currentPrice) / referencePrice) * 100.0
        
        return PriceDropResult(dropPercent, referencePrice, currentPrice, true)
    }

    /**
     * Convenience method to get just the percentage drop from PriceData history.
     */
    fun calculatePriceDropPercentage(history: List<PriceData>): Double {
        return calculatePriceDrop(history).dropPercent
    }

    /**
     * Calculates the price drop percentage from a simple list of prices.
     * Assumed to be in chronological order.
     */
    fun calculatePriceDropFromRawHistory(history: List<Double>, currentPrice: Double? = null): Double {
        if (history.isEmpty()) return 0.0
        
        val actualCurrentPrice = currentPrice ?: history.last()
        val previousPrices = if (currentPrice == null) history.dropLast(1) else history
        
        if (previousPrices.isEmpty()) return 0.0
        
        // Simple outlier filter for raw history
        val referencePrice = previousPrices.filter { it > 0 }.maxOrNull() ?: return 0.0
        
        if (actualCurrentPrice >= referencePrice || referencePrice <= 0) return 0.0
        
        return ((referencePrice - actualCurrentPrice) / referencePrice) * 100.0
    }
}
