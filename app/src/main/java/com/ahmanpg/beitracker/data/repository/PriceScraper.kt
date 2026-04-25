package com.ahmanpg.beitracker.data.repository

import android.util.Log
import com.ahmanpg.beitracker.data.model.TrackedItem
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A sophisticated web scraper that extracts product and price information
 * from popular Tanzanian e-commerce platforms like Jiji and Zudua.
 * 
 * Features include:
 * - Intent-based filtering (distinguishing between products and accessories).
 * - Anti-blocking measures (User-Agent rotation, connection retries).
 * - Multi-source aggregation with relevance ranking.
 * - Cache management to reduce network overhead.
 */
@Singleton
class PriceScraper @Inject constructor() {

    private val TAG = "PriceScraper"

    companion object {
        private const val BASE_URL = "https://jiji.co.tz"
        private const val SEARCH_URL = "$BASE_URL/search"
        
        private const val ZUDUA_URL = "https://zudua.co.tz"
        private const val ZUDUA_SEARCH = "$ZUDUA_URL/search?q="

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

        private const val MOBILE_AGENT =
            "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36"

        private const val TIMEOUT = 30000
        private const val CACHE_EXPIRY = 5 * 60_000L // 5 minutes
        
        private val ACCESSORY_KEYWORDS = listOf(
            "case", "cover", "protector", "charger", "cable", "pouch", "stand", "holder", 
            "skin", "lens", "adapter", "tempered", "glass", "strap", "band", "plug",
            "earphone", "headphone", "airpods", "remote", "battery", "display", "screen", "keyboard"
        )

        private val BUDGET_KEYWORDS = listOf("cheap", "budget", "low cost", "affordable", "price", "low", "sale", "discount")
        
        private val CORE_BRANDS = listOf(
            "apple", "iphone", "ipad", "macbook", "imac", "iwatch",
            "samsung", "galaxy", "pixel", "google", "huawei", "xiaomi", "redmi", "oppo", "vivo", "realme", "infinix", "tecno",
            "hp", "dell", "lenovo", "thinkpad", "asus", "acer", "msi", "microsoft", "surface",
            "sony", "playstation", "ps4", "ps5", "xbox", "nintendo", "switch", "canon", "nikon",
            "toyota", "nissan", "honda", "subaru", "suzuki", "mazda", "mercedes", "bmw", "audi", "tvs", "bajaj"
        )
        
        // Category Keyword Expansion
        private val CATEGORY_EXPANSION = mapOf(
            "smartphone" to listOf("phone", "iphone", "galaxy", "pixel", "redmi", "tecno", "infinix"),
            "television" to listOf("tv", "led", "smart tv", "hdtv", "hisense", "lg", "sony tv"),
            "laptop" to listOf("macbook", "thinkpad", "notebook", "ultrabook", "computer"),
            "car" to listOf("toyota", "vitz", "ist", "nissan", "vehicle", "automobile"),
            "motorcycle" to listOf("pikipiki", "bodaboda", "tvs", "bajaj", "boxer")
        )
    }

    private val cache = mutableMapOf<String, Pair<Long, List<TrackedItem>>>()

    // ===============================
    // 🔍 PUBLIC API
    // ===============================

    suspend fun searchProducts(query: String): List<TrackedItem> =
        withContext(Dispatchers.IO) {
            val intent = identifyIntent(query)
            
            val jijiResults = async { 
                val url = "$SEARCH_URL?query=${URLEncoder.encode(query, "UTF-8")}"
                scrapeJijiList(url)
            }
            
            val zuduaResults = async {
                scrapeZudua(query)
            }
            
            var allResults = jijiResults.await() + zuduaResults.await()
            
            // Apply Intent-Based Filtering and Category Enforcement
            allResults = filterByIntent(allResults, intent)
            
            // Robust fallback for development/demo
            if (allResults.isEmpty()) {
                allResults = filterByIntent(getMockResults(query), intent)
            }
            
            // Rank by price relevance if budget terms are used
            if (intent.isBudgetSearch) {
                allResults.sortedBy { it.currentPrice }
            } else {
                allResults.sortedByDescending { relevanceScore(it, intent) }
            }
        }

    suspend fun getPopularProducts(): List<TrackedItem> =
        withContext(Dispatchers.IO) {
            val results = scrapeJijiList(BASE_URL)
            if (results.isEmpty()) getMockPopular() else results
        }

    suspend fun getTopPriceDrops(): List<TrackedItem> =
        withContext(Dispatchers.IO) {
            val results = scrapeJijiList(BASE_URL)
            if (results.isEmpty()) getMockDrops() else results
        }

    suspend fun getHighlightedDeals(): List<TrackedItem> =
        withContext(Dispatchers.IO) {
            val results = scrapeJijiList(BASE_URL)
            if (results.isEmpty()) getMockPopular().take(3) else results
        }

    suspend fun refreshProductPrice(productUrl: String): TrackedItem? =
        withContext(Dispatchers.IO) {
            if (productUrl.contains("zudua.com") || productUrl.contains("zudua.co.tz")) scrapeZuduaDetail(productUrl)
            else scrapeJijiDetail(productUrl)
        }

    // ===============================
    // 🧠 INTENT & FILTERING LOGIC
    // ===============================

    private data class SearchIntent(
        val originalQuery: String,
        val coreQuery: String,
        val keywords: List<String>,
        val isBudgetSearch: Boolean,
        val isAccessorySearch: Boolean,
        val targetBrands: List<String>,
        val expandedKeywords: List<String>
    )

    /**
     * Identifies the 'Search Intent' from a raw string.
     * Determines if the user is looking for a budget option, a specific brand,
     * or an accessory. This powers the smart filtering logic.
     */
    private fun identifyIntent(query: String): SearchIntent {
        val normalized = query.lowercase().trim()
        val words = normalized.split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
        
        val isBudget = words.any { it in BUDGET_KEYWORDS } || normalized.contains("low cost")
        val isAccessory = words.any { it in ACCESSORY_KEYWORDS }
        
        // Target brands identified from query
        val targetBrands = words.filter { it in CORE_BRANDS }
        
        // Remove noise words to find the core product type
        val coreWords = words.filter { it !in BUDGET_KEYWORDS && it !in listOf("a", "the", "for", "with", "and") }
        val coreQuery = coreWords.joinToString(" ")
        
        val expanded = coreWords.flatMap { word ->
            CATEGORY_EXPANSION[word] ?: listOf(word)
        }.distinct()
        
        return SearchIntent(
            originalQuery = normalized,
            coreQuery = coreQuery,
            keywords = coreWords,
            isBudgetSearch = isBudget,
            isAccessorySearch = isAccessory,
            targetBrands = targetBrands,
            expandedKeywords = expanded
        )
    }

    /**
     * Filters a list of raw items based on the identified user intent.
     * Prevents "category pollution" (e.g., showing motorcycle spare parts 
     * when searching for a TVS brand television).
     */
    private fun filterByIntent(items: List<TrackedItem>, intent: SearchIntent): List<TrackedItem> {
        if (intent.coreQuery.isBlank()) return items

        return items.filter { item ->
            val itemName = item.name.lowercase()
            
            // 1. Mandatory keyword matching
            val matchesKeywords = intent.expandedKeywords.any { keyword -> 
                itemName.contains(keyword) 
            } || intent.keywords.any { it in itemName }
            
            // 2. Strict Category Purity (Brand Enforcement)
            val brandCheck = if (intent.targetBrands.isNotEmpty()) {
                intent.targetBrands.any { brand -> itemName.contains(brand) }
            } else {
                true
            }
            
            // Special case for "TVS" brand vs "Television"
            val categoryCheck = if (intent.keywords.any { it.contains("television") || it == "tv" }) {
                val vehicleKeywords = listOf("motorcycle", "pikipiki", "bike", "tyre", "engine", "tvs hlx", "tvs star", "hlx", "125", "150")
                vehicleKeywords.none { itemName.contains(it) }
            } else if (intent.keywords.any { it == "motorcycle" || it == "pikipiki" || it == "vehicles" }) {
                 true 
            } else {
                true
            }
            
            // 3. Accessory Enforcement (Stronger)
            val isItemAccessory = ACCESSORY_KEYWORDS.any { acc -> 
                itemName.contains(acc) || (acc == "glass" && itemName.contains("tempered"))
            }
            val accessoryCheck = if (!intent.isAccessorySearch) {
                // If user is searching for a product, exclude items with accessory keywords
                !isItemAccessory
            } else {
                // If user is specifically searching for an accessory, include them
                isItemAccessory
            }

            // 4. Variant/Loosely related filtering
            val exclusionKeywords = listOf("broken", "faulty", "parts", "iclou", "locked", "box only", "dummy", "cracked", "not working", "for repair")
            val exclusionCheck = exclusionKeywords.none { itemName.contains(it) }

            matchesKeywords && brandCheck && accessoryCheck && exclusionCheck && categoryCheck
        }
    }

    private fun relevanceScore(item: TrackedItem, intent: SearchIntent): Int {
        val name = item.name.lowercase()
        var score = 0
        
        // Priority to exact phrase matches
        if (name.contains(intent.coreQuery)) score += 100
        
        // Priority to brand at the start of title
        if (intent.targetBrands.any { name.startsWith(it) }) score += 50
        
        // Priority to expanded keyword matches
        intent.expandedKeywords.forEach { kw ->
            if (name.contains(kw)) score += 20
        }
        
        // Penalty for generic/used listings if searching for new
        if (name.contains("refurbished") || name.contains("used")) score -= 10
        
        return score
    }

    // ===============================
    // 🧠 JIJI SCRAPER
    // ===============================

    private suspend fun scrapeJijiList(url: String): List<TrackedItem> =
        withContext(Dispatchers.IO) {

            val cached = cache[url]
            if (cached != null && System.currentTimeMillis() - cached.first < CACHE_EXPIRY) {
                return@withContext cached.second
            }

            var currentAgent = USER_AGENT
            var doc = tryConnect(url, currentAgent)

            if (doc == null || isBlocked(doc)) {
                delay((1000..2000).random().toLong())
                currentAgent = MOBILE_AGENT
                doc = tryConnect(url, currentAgent)
            }

            if (doc == null || isBlocked(doc)) {
                Log.w(TAG, "Scraping blocked or failed for $url")
                return@withContext emptyList()
            }

            val items = doc.select(
                ".b-list-advert-base, .qa-advert-list-item, .b-list-advert__item-wrapper, .b-advert-card, .masonry-item"
            )

            val results = items.map { el ->
                async {
                    parseJijiElement(el)
                }
            }.awaitAll().filterNotNull()

            val distinctResults = results.distinctBy { it.url }
                .distinctBy { "${it.name}-${it.currentPrice}" }

            if (distinctResults.isNotEmpty()) {
                cache[url] = System.currentTimeMillis() to distinctResults
            }

            distinctResults
        }

    private suspend fun parseJijiElement(el: org.jsoup.nodes.Element): TrackedItem? {
        try {
            val linkEl = el.selectFirst("a[href*='/ad/'], a.b-list-advert-base__link, a.qa-advert-list-item, .b-advert-title-inner a") ?: return null
            val link = linkEl.attr("abs:href")
            if (link.isBlank()) return null

            val title = extractJijiTitle(el)
            val price = extractJijiPrice(el)
            val finalTitle = cleanTitle(if (title.isBlank()) linkEl.text() else title)
            
            // Filter out obviously wrong listings (like TZS 1 listings used for visibility)
            if (price < 1000) return null

            val image = extractJijiImage(el)
            val location = extractJijiLocation(el)

            return TrackedItem(
                url = link,
                name = finalTitle.ifBlank { "Listing on Jiji" },
                currentPrice = price,
                previousPrice = price,
                history = mutableListOf(price),
                imageUrl = image,
                images = if (image != null) listOf(image) else emptyList(),
                source = "Jiji",
                category = location
            )
        } catch (e: Exception) {
            return null
        }
    }

    private suspend fun scrapeJijiDetail(url: String): TrackedItem? =
        withContext(Dispatchers.IO) {
            var currentAgent = USER_AGENT
            var doc = tryConnect(url, currentAgent)

            // Retry with mobile agent if blocked or failed
            if (doc == null || isBlocked(doc)) {
                delay((500..1500).random().toLong())
                currentAgent = MOBILE_AGENT
                doc = tryConnect(url, currentAgent)
            }

            if (doc == null || isBlocked(doc)) return@withContext null

            try {
                val title = cleanTitle(extractDetailTitle(doc))
                val price = extractJijiDetailPrice(doc)
                val images = extractJijiDetailImages(doc)
                val mainImage = images.firstOrNull() ?: extractJijiDetailImage(doc)

                if (price <= 0) return@withContext null

                TrackedItem(
                    url = url,
                    name = title.ifBlank { "Jiji Product" },
                    currentPrice = price,
                    imageUrl = mainImage,
                    images = images.ifEmpty { if (mainImage != null) listOf(mainImage) else emptyList() },
                    source = "Jiji"
                )
            } catch (e: Exception) {
                null
            }
        }

    // ===============================
    // 🛍️ ZUDUA SCRAPER
    // ===============================

    private suspend fun scrapeZudua(query: String): List<TrackedItem> =
        withContext(Dispatchers.IO) {
            try {
                val url = "$ZUDUA_SEARCH${URLEncoder.encode(query, "UTF-8")}"
                val doc = tryConnect(url, USER_AGENT) ?: return@withContext emptyList()
                
                val items = doc.select(".product-card, .grid-item, .product-item, .item-inner")
                items.map { el ->
                    val title = el.select(".product-title, .title, h3, .name").text().trim()
                    val priceStr = el.select(".price, .current-price, .amount, .price-new").text().trim()
                    val price = cleanPrice(priceStr)
                    val link = el.select("a").attr("abs:href")
                    val img = el.select("img").attr("abs:src").ifBlank { el.select("img").attr("abs:data-src") }
                    
                    if (title.isNotBlank() && price > 1000 && link.isNotBlank()) {
                        TrackedItem(
                            url = link,
                            name = title,
                            currentPrice = price,
                            previousPrice = price,
                            source = "Zudua",
                            imageUrl = img,
                            images = if (img.isNotBlank()) listOf(img) else emptyList()
                        )
                    } else null
                }.filterNotNull()
            } catch (e: Exception) {
                emptyList()
            }
        }

    private suspend fun scrapeZuduaDetail(url: String): TrackedItem? =
        withContext(Dispatchers.IO) {
            try {
                val doc = tryConnect(url, USER_AGENT) ?: return@withContext null
                val title = doc.select("h1, .product-name, .title").text().trim()
                val priceStr = doc.select(".price-amount, .current-price, .price-new").first()?.text() ?: ""
                val price = cleanPrice(priceStr)
                val image = doc.select(".main-image img, meta[property=og:image]").attr("abs:src").ifBlank { doc.select("meta[property=og:image]").attr("content") }
                
                if (price > 0) {
                    TrackedItem(
                        url = url,
                        name = title,
                        currentPrice = price,
                        source = "Zudua",
                        imageUrl = image
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

    // ===============================
    // 🔧 HELPERS
    // ===============================

    private fun tryConnect(url: String, agent: String): org.jsoup.nodes.Document? {
        return try {
            Jsoup.connect(url)
                .userAgent(agent)
                .timeout(TIMEOUT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Referer", BASE_URL)
                .get()
        } catch (e: Exception) {
            null
        }
    }

    private fun isBlocked(doc: org.jsoup.nodes.Document): Boolean {
        val title = doc.title().lowercase()
        val text = doc.text().lowercase()
        return title.contains("access denied") || title.contains("just a moment") || text.contains("enable cookies") || text.contains("checking your browser")
    }

    private fun extractJijiTitle(el: org.jsoup.nodes.Element): String {
        val selectors = listOf(".b-list-advert-base__title", ".qa-advert-list-item-title", ".b-advert-title-inner", "h4", "h3")
        for (selector in selectors) {
            val text = el.selectFirst(selector)?.text()?.trim()
            if (!text.isNullOrBlank()) return text
        }
        return ""
    }

    private fun extractJijiPrice(el: org.jsoup.nodes.Element): Double {
        val raw = el.selectFirst(".qa-advert-price, .b-advert-price, .b-list-advert-base__price, .price")?.text() ?: ""
        return cleanPrice(raw)
    }

    private fun extractJijiLocation(el: org.jsoup.nodes.Element): String? {
        return el.selectFirst(".b-list-advert__region, .qa-advert-location, .region")?.text()?.trim()
    }

    private fun extractJijiImage(el: org.jsoup.nodes.Element): String? {
        val img = el.selectFirst("img") ?: return null
        return img.attr("abs:data-src").ifBlank { img.attr("abs:src") }
    }

    private fun extractDetailTitle(doc: org.jsoup.nodes.Document): String {
        val og = doc.selectFirst("meta[property=og:title]")?.attr("content")
        return og ?: doc.selectFirst("h1")?.text() ?: doc.title()
    }

    private fun extractJijiDetailPrice(doc: org.jsoup.nodes.Document): Double {
        val raw = doc.selectFirst(".qa-advert-price, .b-advert-price, [itemprop=price], .price")?.text() ?: ""
        var price = cleanPrice(raw)
        if (price <= 0) {
            val script = doc.selectFirst("script[type=application/ld+json]")?.data()
            val match = Regex("\"price\"\\s*:\\s*\"?(\\d+)\"?").find(script ?: "")
            price = match?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
        }
        return price
    }

    private fun extractJijiDetailImage(doc: org.jsoup.nodes.Document): String? {
        return doc.selectFirst("meta[property=og:image]")?.attr("content")
    }

    private fun extractJijiDetailImages(doc: org.jsoup.nodes.Document): List<String> {
        val images = doc.select(".b-advert-gallery img, .qa-advert-gallery img, .fw-slider img").map { img ->
            img.attr("abs:data-src").ifBlank { img.attr("abs:src") }
        }.filter { it.isNotBlank() }
        return images.distinct()
    }

    private fun cleanTitle(raw: String): String {
        return raw.replace(Regex("(?i) - Jiji.co.tz|Jiji.co.tz:?|\\| Jiji.co.tz"), "").replace(Regex("\\s+"), " ").trim()
    }

    private fun cleanPrice(raw: String): Double {
        val text = raw.lowercase().replace(",", "").replace("tzs", "").replace("sh", "").trim()
        return when {
            text.contains("m") -> (Regex("(\\d+(\\.\\d+)?)").find(text)?.value?.toDoubleOrNull() ?: 0.0) * 1_000_000
            text.contains("k") -> (Regex("(\\d+(\\.\\d+)?)").find(text)?.value?.toDoubleOrNull() ?: 0.0) * 1_000
            else -> Regex("(\\d+(\\.\\d+)?)").find(text)?.value?.toDoubleOrNull() ?: 0.0
        }
    }

    // ===============================
    // 🎭 MOCK DATA FALLBACKS
    // ===============================

    private fun getMockResults(query: String): List<TrackedItem> {
        val q = query.lowercase()
        return when {
            q.contains("laptop") -> listOf(
                TrackedItem("https://jiji.co.tz/ad/lp1", "HP EliteBook 840 G5 Core i5", 850000.0, 950000.0, source = "Jiji"),
                TrackedItem("https://jiji.co.tz/ad/lp2", "MacBook Air M1 2020 8GB/256GB", 1850000.0, 2000000.0, source = "Jiji"),
                TrackedItem("https://zudua.com/p/lp3", "Lenovo ThinkPad X1 Carbon Gen 9", 2450000.0, 2600000.0, source = "Zudua"),
                TrackedItem("https://jiji.co.tz/ad/acc1", "Laptop Sleeve Case 14 inch", 45000.0, source = "Jiji")
            )
            q.contains("iphone") || q.contains("smartphone") || q.contains("phone") -> listOf(
                TrackedItem("https://jiji.co.tz/ad/ph1", "iPhone 15 Pro Max 256GB", 3200000.0, 3400000.0, source = "Jiji"),
                TrackedItem("https://jiji.co.tz/ad/ph2", "Samsung Galaxy S23 Ultra", 1950000.0, 2100000.0, source = "Jiji"),
                TrackedItem("https://zudua.com/p/ph3", "Google Pixel 8 Pro", 1750000.0, 1900000.0, source = "Zudua"),
                TrackedItem("https://jiji.co.tz/ad/ph4", "iPhone 11 64GB UK Used", 650000.0, 750000.0, source = "Jiji"),
                TrackedItem("https://jiji.co.tz/ad/acc2", "iPhone 15 Pro Silicone Case", 35000.0, source = "Jiji")
            )
            q.contains("television") || q.contains("tv") -> listOf(
                TrackedItem("https://jiji.co.tz/ad/tv1", "Samsung 55 inch Crystal UHD 4K", 1250000.0, 1450000.0, source = "Jiji"),
                TrackedItem("https://jiji.co.tz/ad/tv2", "LG 43 inch Smart TV", 850000.0, 950000.0, source = "Jiji"),
                TrackedItem("https://zudua.com/p/tv3", "Hisense 50 inch 4K Android TV", 1100000.0, 1200000.0, source = "Zudua")
            )
            else -> listOf(
                TrackedItem("https://jiji.co.tz/ad/gen1", "Sony WH-1000XM5 Headphones", 750000.0, 850000.0, source = "Jiji"),
                TrackedItem("https://jiji.co.tz/ad/gen2", "PlayStation 5 Console Slim", 1450000.0, 1600000.0, source = "Jiji"),
                TrackedItem("https://zudua.com/p/gen3", "Apple Watch Series 9 GPS", 950000.0, 1100000.0, source = "Zudua")
            )
        }
    }

    private fun getMockPopular(): List<TrackedItem> {
        return listOf(
            TrackedItem("https://jiji.co.tz/popular/1", "MacBook Pro M3 Max", 8500000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/popular/2", "Sony PlayStation 5 Slim", 1450000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/popular/3", "AirPods Pro (2nd Gen)", 450000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/popular/4", "iPhone 15 Plus 128GB", 2150000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/popular/5", "Samsung Galaxy Z Fold 5", 3450000.0, source = "Jiji")
        )
    }

    private fun getMockDrops(): List<TrackedItem> {
        return listOf(
            TrackedItem("https://jiji.co.tz/drop/1", "Dell XPS 15 9530", 4200000.0, 4800000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/drop/2", "iPad Air 5th Gen 64GB", 1250000.0, 1450000.0, source = "Jiji"),
            TrackedItem("https://jiji.co.tz/drop/3", "Canon EOS R6 Mark II", 5800000.0, 6500000.0, source = "Jiji")
        )
    }
}
