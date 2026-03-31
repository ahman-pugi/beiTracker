package com.ahmanpg.beitracker.data.repository

import android.util.Log
import com.ahmanpg.beitracker.data.model.TrackedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceScraper @Inject constructor() {

    private val TAG = "PriceScraper"

    /**
     * Main function: Search products on Jiji.co.tz using keyword
     */
    suspend fun searchProducts(query: String): List<TrackedItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TrackedItem>()
        val searchTerm = query.trim().replace(" ", "+")
        val searchUrl = "https://jiji.co.tz/search?query=$searchTerm"

        Log.d(TAG, "=== Starting search for: '$query' ===")
        Log.d(TAG, "Search URL: $searchUrl")

        try {
            val doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36")
                .timeout(15000)
                .get()

            Log.d(TAG, "Page title: ${doc.title()}")
            Log.d(TAG, "HTML length: ${doc.html().length}")

            // Best working selectors for Jiji in April 2026
            val productElements = doc.select(
                ".b-list-advert__item, " +
                        ".advert-item, " +
                        ".core-advert, " +
                        "[class*='advert'], " +
                        ".listing-item"
            )

            Log.d(TAG, "Found ${productElements.size} potential product elements")

            for ((index, element) in productElements.withIndex().take(25)) {
                val titleEl = element.selectFirst("h3, .b-advert-title__text, .advert-title, [class*='title']")
                val priceEl = element.selectFirst(".qa-advert-price, .b-advert-price, .price, strong, [class*='price']")
                val linkEl = element.selectFirst("a")

                val title = titleEl?.text()?.trim() ?: ""
                val priceText = priceEl?.text()?.trim() ?: ""
                val link = linkEl?.attr("abs:href") ?: ""

                Log.d(TAG, "Item $index | Title: '$title' | Price: '$priceText' | Link: $link")

                val price = cleanPrice(priceText)

                if (price > 0 && link.isNotEmpty() && title.length > 5) {
                    results.add(
                        TrackedItem(
                            url = link,
                            name = title,
                            currentPrice = price,
                            previousPrice = price,
                            history = mutableListOf(price)
                        )
                    )
                    Log.d(TAG, "✓ Successfully added: $title - TZS $price")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during Jiji search: ${e.message}", e)
        }

        Log.d(TAG, "=== Search completed. Total products found: ${results.size} ===")
        results
    }

    /**
     * Clean price string from Jiji format
     */
    private fun cleanPrice(raw: String): Double {
        if (raw.isBlank()) return 0.0

        val cleaned = raw
            .replace(Regex("(?i)(TZS|TSh|Sh|Shs|Price|USD|\\$)"), "")
            .replace(",", "")
            .replace(" ", "")
            .replace(Regex("[^0-9.]"), "")
            .trim()

        val price = cleaned.toDoubleOrNull() ?: 0.0
        Log.d(TAG, "Cleaned price: '$raw' → $price")
        return price
    }
}