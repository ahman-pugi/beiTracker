package com.ahmanpg.beitracker.util

import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.repository.PriceData
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.abs

data class BuyScoreResult(
    val score: Int,
    val recommendation: String, // BUY, WAIT, OVERPRICED
    val factors: List<ScoreFactor>,
    val confidenceScore: Int
)

data class ScoreFactor(
    val description: String,
    val impact: Int,
    val isPositive: Boolean
)

/**
 * A mathematical engine that evaluates whether a product is a "Good Buy"
 * based on historical price data, market trends, and volatility.
 * 
 * It calculates a score from 0-100 and provides a recommendation (BUY, WAIT, OVERPRICED).
 * It also assesses its own 'Confidence Score' based on the amount of available data.
 */
object BuyScoreEngine {

    fun calculateScore(item: TrackedItem): BuyScoreResult {
        var score = 50
        val factors = mutableListOf<ScoreFactor>()

        val history = item.priceHistory
        val prices = history.map { it.price }

        val currentPrice = item.currentPrice
        
        // Fallback to market aggregates if individual history is missing
        val minPrice = listOfNotNull(item.minPrice, prices.minOrNull()).minOrNull()
        val maxPrice = listOfNotNull(item.maxPrice, prices.maxOrNull()).maxOrNull()
        val avgList = listOfNotNull(item.avgPrice, prices.average().takeIf { !it.isNaN() })
        val avgPrice = if (avgList.isEmpty()) null else avgList.average()

        if (currentPrice <= 0 || (prices.isEmpty() && minPrice == null)) {
            return BuyScoreResult(0, "WAIT", emptyList(), 0)
        }

        // -----------------------------
        // 1. Price Position (Market or History)
        // -----------------------------
        if (maxPrice != null && minPrice != null && maxPrice > minPrice) {
            val range = maxPrice - minPrice
            val position = (currentPrice - minPrice) / range

            when {
                position <= 0.1 -> {
                    val impact = 30
                    score += impact
                    factors.add(ScoreFactor("At lowest known market price", impact, true))
                }
                position <= 0.3 -> {
                    val impact = 20
                    score += impact
                    factors.add(ScoreFactor("Cheaper than most listings", impact, true))
                }
                position >= 0.9 -> {
                    val impact = -30
                    score += impact
                    factors.add(ScoreFactor("At highest known market price", impact, false))
                }
                position >= 0.7 -> {
                    val impact = -20
                    score += impact
                    factors.add(ScoreFactor("Priced above market average", impact, false))
                }
            }
        } else if (avgPrice != null && avgPrice > 0) {
            // If we only have an average, compare to it
            val diff = (currentPrice - avgPrice) / avgPrice
            if (diff < -0.15) {
                val impact = 20
                score += impact
                factors.add(ScoreFactor("Priced below market average", impact, true))
            } else if (diff > 0.15) {
                val impact = -20
                score += impact
                factors.add(ScoreFactor("Priced above market average", impact, false))
            }
        }

        // -----------------------------
        // 2. Trend (requires history)
        // -----------------------------
        if (prices.size >= 3) {
            val trendSignal = calculateTrend(prices)
            when {
                trendSignal > 0.6 -> {
                    val impact = 15
                    score += impact
                    factors.add(ScoreFactor("Price trending down", impact, true))
                }
                trendSignal < 0.4 -> {
                    val impact = -15
                    score += impact
                    factors.add(ScoreFactor("Price trending up", impact, false))
                }
            }
        }

        // -----------------------------
        // 3. Volatility
        // -----------------------------
        if (prices.size >= 3) {
            val volatility = calculateVolatility(history)

            if (volatility > 0.18) {
                val impact = -15
                score += impact
                factors.add(ScoreFactor("High price volatility", impact, false))
            } else if (volatility < 0.05 && prices.size >= 5) {
                val impact = 10
                score += impact
                factors.add(ScoreFactor("Stable pricing history", impact, true))
            }
        }

        // -----------------------------
        // 4. Recent Price Drop
        // -----------------------------
        if (prices.size >= 2) {
            val dropSignal = calculateRecentDrop(prices)
            when {
                dropSignal > 0.1 -> {
                    val impact = 25
                    score += impact
                    factors.add(ScoreFactor("Significant recent price drop", impact, true))
                }
                dropSignal > 0.05 -> {
                    val impact = 15
                    score += impact
                    factors.add(ScoreFactor("Moderate recent price drop", impact, true))
                }
            }
        }

        // -----------------------------
        // Clamp score
        // -----------------------------
        
        // Special Case: Unique Product (No historical range and no market context)
        val isUnique = (maxPrice == null || maxPrice == minPrice) && (avgPrice == null || avgPrice == currentPrice) && prices.size <= 1
        if (isUnique) {
            score = 95
            factors.clear()
            factors.add(ScoreFactor("Unique product found", 45, true))
            factors.add(ScoreFactor("No cheaper alternatives detected", 0, true))
        }

        score = score.coerceIn(0, 100)

        val recommendation = when {
            score >= 70 -> "BUY"
            score >= 45 -> "WAIT"
            else -> "OVERPRICED"
        }

        // -----------------------------
        // Confidence Score
        // -----------------------------
        var confidence = when {
            isUnique -> 100 // We are confident it's the only one
            history.size >= 20 -> 90
            history.size >= 10 -> 80
            history.size >= 5 -> 70
            history.size >= 3 -> 55
            history.size >= 2 -> 35
            else -> 15
        }

        // Market Data Bonus: If we have market insights from similar items
        if (item.minPrice != null && item.maxPrice != null) {
            confidence += 30 // Significant boost because we know the market context
        } else if (item.avgPrice != null) {
            confidence += 15
        }

        // Data Age/Density adjustments
        if (history.size >= 2) {
            val firstTime = history.minOf { it.timestamp }
            val lastTime = history.maxOf { it.timestamp }
            val days = (lastTime - firstTime) / (1000 * 60 * 60 * 24)

            if (days >= 30) confidence += 10
            else if (days >= 7) confidence += 5
        }

        confidence = confidence.coerceIn(0, 100)

        return BuyScoreResult(
            score = score,
            recommendation = recommendation,
            factors = factors.sortedByDescending { abs(it.impact) },
            confidenceScore = confidence
        )
    }

    private fun calculateTrend(prices: List<Double>): Double {
        if (prices.size < 3) return 0.5

        val first = prices.first()
        val last = prices.last()

        val change = (last - first) / first

        return when {
            change < -0.05 -> 1.0
            change < -0.02 -> 0.7
            change < 0.02 -> 0.5
            change < 0.05 -> 0.3
            else -> 0.0
        }
    }

    private fun calculateRecentDrop(prices: List<Double>): Double {
        if (prices.size < 2) return 0.0

        val recent = prices.takeLast(3)
        val maxRecent = recent.maxOrNull()!!
        val current = prices.last()

        return (maxRecent - current) / maxRecent
    }

    private fun calculateVolatility(history: List<PriceData>): Double {
        if (history.size < 2) return 0.0

        val prices = history.map { it.price }
        val avg = prices.average()
        if (avg <= 0.0) return 0.0

        val variance = prices.map { (it - avg).pow(2.0) }.average()
        return sqrt(variance) / avg
    }
}
