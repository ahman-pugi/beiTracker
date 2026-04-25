package com.ahmanpg.beitracker.data.repository

import android.util.Log
import com.ahmanpg.beitracker.data.local.dao.PriceDao
import com.ahmanpg.beitracker.data.local.entity.*
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.remote.model.PriceAlert
import com.ahmanpg.beitracker.data.remote.model.PriceHistoryEntry
import com.ahmanpg.beitracker.data.remote.model.Product
import com.ahmanpg.beitracker.data.remote.model.TrackedProduct
import com.ahmanpg.beitracker.data.remote.repository.FirestoreRepository
import com.ahmanpg.beitracker.util.BuyScoreEngine
import com.ahmanpg.beitracker.util.HashUtils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository managing price data, product tracking, and history.
 * Coordinates between the local SQLite database (Room), remote Firestore,
 * and web scrapers to provide a unified data source for the UI.
 */
@Singleton
class PriceRepository @Inject constructor(
    private val dao: PriceDao,
    private val scraper: PriceScraper,
    private val firestoreRepository: FirestoreRepository,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "PriceRepository"
        private const val HEARTBEAT_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: "anonymous"

    // ==================== SEARCH ====================

    /**
     * Searches for products using the PriceScraper. 
     * This is typically the entry point for new products into the system.
     */
    suspend fun searchProducts(query: String): List<TrackedItem> {
        return scraper.searchProducts(query)
    }

    // ==================== DISCOVERY / EXPLORE ====================

    suspend fun getPopularProducts(): List<TrackedItem> {
        return scraper.getPopularProducts()
    }

    suspend fun getTopPriceDrops(): List<TrackedItem> {
        return scraper.getTopPriceDrops()
    }

    suspend fun getHighlightedDeals(): List<TrackedItem> {
        return scraper.getHighlightedDeals()
    }

    fun getCachedDiscoveryItems(section: String): Flow<List<TrackedItem>> {
        return dao.getDiscoveryItems(section).map { entities ->
            entities.map { entity ->
                TrackedItem(
                    url = entity.url,
                    name = entity.name,
                    currentPrice = entity.currentPrice,
                    previousPrice = entity.previousPrice,
                    source = entity.source,
                    imageUrl = entity.imageUrl,
                    isTracked = false // Will be updated by ViewModel
                )
            }
        }
    }

    suspend fun updateDiscoveryCache(section: String, items: List<TrackedItem>) {
        val entities = items.map { item ->
            DiscoveryItemEntity(
                url = item.url,
                name = item.name,
                currentPrice = item.currentPrice,
                previousPrice = item.previousPrice,
                imageUrl = item.imageUrl,
                source = item.source,
                section = section
            )
        }
        dao.clearDiscoverySection(section)
        dao.insertDiscoveryItems(entities)
    }

    // ==================== TRACK / UNTRACK ====================

    /**
     * Begins tracking a new product.
     * 1. Saves to local Room DB.
     * 2. Syncs product metadata to Firestore.
     * 3. Establishes the initial price point in history.
     */
    suspend fun trackProduct(item: TrackedItem): Result<TrackedProductEntity> {
        return try {
            val now = System.currentTimeMillis()
            val entity = TrackedProductEntity(
                userId = currentUserId,
                url = item.url,
                name = item.name,
                source = item.source,
                currentPrice = item.currentPrice,
                initialPrice = item.currentPrice,
                previousPrice = item.currentPrice,
                imageUrl = item.imageUrl,
                imagesString = item.images.joinToString(","),
                targetPrice = item.targetPrice,
                isAlertEnabled = item.isAlertEnabled,
                category = item.category,
                lastCheckedAt = now,
                alertCondition = item.alertCondition,
                notifyPush = item.notifyPush,
                notifySms = item.notifySms,
                notifyEmail = item.notifyEmail,
                notifyWhatsapp = item.notifyWhatsapp
            )
            dao.insertTrackedProduct(entity)

            // Sync with Firestore
            val productId = HashUtils.md5(item.url)
            firestoreRepository.saveProduct(Product(
                id = productId,
                url = item.url,
                name = item.name,
                currentPrice = item.currentPrice,
                imageUrl = item.imageUrl,
                category = item.category ?: ""
            ))
            
            firestoreRepository.trackProduct(currentUserId, TrackedProduct(
                productId = productId,
                url = item.url,
                name = item.name,
                source = item.source,
                currentPrice = item.currentPrice,
                initialPrice = item.currentPrice,
                previousPrice = item.currentPrice,
                imageUrl = item.imageUrl,
                imagesString = item.images.joinToString(","),
                targetPrice = item.targetPrice,
                isAlertEnabled = item.isAlertEnabled,
                category = item.category,
                addedAt = now,
                lastCheckedAt = now,
                alertCondition = item.alertCondition,
                notifyPush = item.notifyPush,
                notifySms = item.notifySms,
                notifyEmail = item.notifyEmail,
                notifyWhatsapp = item.notifyWhatsapp
            ))

            val lastHistory = dao.getLatestPrice(item.url)
            val priceChanged = lastHistory == null || item.currentPrice != lastHistory.price
            val heartbeatExpired = lastHistory != null && (now - lastHistory.timestamp) >= HEARTBEAT_INTERVAL_MS

            if (priceChanged || heartbeatExpired) {
                val changeAmount = if (lastHistory != null) item.currentPrice - lastHistory.price else 0.0
                val changeType = when {
                    lastHistory == null -> "SAME"
                    changeAmount < 0 -> "DROP"
                    changeAmount > 0 -> "INCREASE"
                    else -> "SAME"
                }

                val priceEntity = PriceEntity(
                    productUrl = item.url,
                    title = item.name,
                    price = item.currentPrice,
                    changeType = changeType,
                    changeAmount = changeAmount,
                    imageUrl = item.imageUrl,
                    source = item.source,
                    timestamp = now
                )
                dao.insertPrice(priceEntity)

                firestoreRepository.addPriceHistoryEntry(
                    productId,
                    PriceHistoryEntry(
                        price = item.currentPrice,
                        source = item.source,
                        url = item.url,
                        timestamp = Timestamp.now()
                    )
                )
                firestoreRepository.updateProductAggregates(productId, item.currentPrice)
            }

            Log.d(TAG, "Tracked: ${item.name} @ TZS ${item.currentPrice}")
            Result.success(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking product: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun untrackProduct(url: String) {
        dao.deleteTrackedProductByUrl(url, currentUserId)
        dao.deletePriceHistory(url)
        firestoreRepository.untrackProduct(currentUserId, HashUtils.md5(url))
    }

    suspend fun updateTrackedProduct(item: TrackedItem) {
        val existing = dao.getTrackedProduct(item.url, currentUserId) ?: return
        val updated = existing.copy(
            targetPrice = item.targetPrice,
            alertCondition = item.alertCondition,
            notifyPush = item.notifyPush,
            notifySms = item.notifySms,
            notifyEmail = item.notifyEmail,
            notifyWhatsapp = item.notifyWhatsapp,
            isAlertEnabled = item.isAlertEnabled,
            imagesString = item.images.joinToString(",")
        )
        dao.updateTrackedProduct(updated)
        
        // Sync tracker state
        firestoreRepository.trackProduct(currentUserId, TrackedProduct(
            productId = HashUtils.md5(item.url),
            url = item.url,
            name = item.name,
            source = item.source,
            currentPrice = item.currentPrice,
            initialPrice = updated.initialPrice,
            previousPrice = updated.previousPrice,
            imageUrl = item.imageUrl,
            imagesString = updated.imagesString,
            targetPrice = item.targetPrice,
            isAlertEnabled = item.isAlertEnabled,
            category = updated.category,
            addedAt = updated.addedAt,
            lastCheckedAt = updated.lastCheckedAt,
            alertCondition = item.alertCondition,
            notifyPush = item.notifyPush,
            notifySms = item.notifySms,
            notifyEmail = item.notifyEmail,
            notifyWhatsapp = item.notifyWhatsapp
        ))
    }

    // ==================== RECENTLY VIEWED ====================

    suspend fun addToRecentlyViewed(item: TrackedItem) {
        try {
            val entity = RecentlyViewedEntity(
                url = item.url,
                name = item.name,
                price = item.currentPrice,
                imageUrl = item.imageUrl,
                source = item.source,
                viewedAt = System.currentTimeMillis()
            )
            dao.insertRecentlyViewed(entity)
            
            firestoreRepository.addToRecentlyViewed(currentUserId, HashUtils.md5(item.url))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to recently viewed: ${e.message}")
        }
    }

    fun getRecentlyViewed(): Flow<List<TrackedItem>> {
        return dao.getRecentlyViewed().map { entities ->
            entities.map { entity ->
                TrackedItem(
                    url = entity.url,
                    name = entity.name,
                    currentPrice = entity.price,
                    imageUrl = entity.imageUrl,
                    source = entity.source,
                    isTracked = false
                )
            }
        }
    }

    suspend fun clearRecentlyViewed() {
        dao.clearRecentlyViewed()
    }

    // ==================== REFRESH / CHECK PRICES ====================

    /**
     * Fetches the latest price from the web for a tracked product.
     * Updates local history, triggers alerts if a price drop occurs, 
     * and recalculates the 'Buy Score' recommendation.
     */
    suspend fun refreshProductPrice(productUrl: String): TrackedItem? {
        val newItem = scraper.refreshProductPrice(productUrl) ?: return null
        val existing = dao.getTrackedProduct(productUrl, currentUserId) ?: return newItem

        val now = System.currentTimeMillis()
        val lastHistory = dao.getLatestPrice(productUrl)
        
        val priceChanged = lastHistory == null || newItem.currentPrice != lastHistory.price
        val heartbeatExpired = lastHistory != null && (now - lastHistory.timestamp) >= HEARTBEAT_INTERVAL_MS

        val productId = HashUtils.md5(productUrl)

        if (priceChanged || heartbeatExpired) {
            val changeAmount = if (lastHistory != null) newItem.currentPrice - lastHistory.price else 0.0
            val changeType = when {
                lastHistory == null -> "SAME"
                changeAmount < 0 -> "DROP"
                changeAmount > 0 -> "INCREASE"
                else -> "SAME"
            }

            val priceEntity = PriceEntity(
                productUrl = productUrl,
                title = newItem.name,
                price = newItem.currentPrice,
                changeType = changeType,
                changeAmount = changeAmount,
                imageUrl = newItem.imageUrl,
                source = newItem.source,
                timestamp = now
            )
            dao.insertPrice(priceEntity)
            
            firestoreRepository.addPriceHistoryEntry(
                productId,
                PriceHistoryEntry(
                    price = newItem.currentPrice,
                    source = newItem.source,
                    url = productUrl,
                    timestamp = Timestamp.now()
                )
            )
            firestoreRepository.updateProductAggregates(productId, newItem.currentPrice)

            if (changeType == "DROP" && existing.isAlertEnabled) {
                val isTriggered = when (existing.alertCondition) {
                    "BELOW" -> newItem.currentPrice <= (existing.targetPrice ?: 0.0)
                    "ANY" -> true
                    else -> false
                }
                
                if (isTriggered) {
                    Log.d(TAG, "📉 Price alert triggered! ${existing.name}")
                    val alert = PriceAlertEntity(
                        userId = currentUserId,
                        productUrl = productUrl,
                        productTitle = existing.name,
                        oldPrice = existing.currentPrice,
                        newPrice = newItem.currentPrice,
                        targetPrice = existing.targetPrice
                    )
                    dao.insertAlert(alert)

                    // Sync Alert to Firestore
                    firestoreRepository.saveAlert(currentUserId, PriceAlert(
                        productUrl = productUrl,
                        productTitle = existing.name,
                        oldPrice = existing.currentPrice,
                        newPrice = newItem.currentPrice,
                        targetPrice = existing.targetPrice,
                        createdAt = now
                    ))
                }
            }
        }

        val updated = existing.copy(
            currentPrice = newItem.currentPrice,
            previousPrice = existing.currentPrice,
            imageUrl = newItem.imageUrl ?: existing.imageUrl,
            imagesString = newItem.images.joinToString(","),
            lastCheckedAt = now
        )
        dao.updateTrackedProduct(updated)

        val insights = getInsights(productUrl)
        val historyEntities = dao.getPriceHistoryList(productUrl)
        val priceHistory = historyEntities.map { it.toPriceData() }
        
        // Construct the item for engine calculation
        val tempItem = newItem.copy(
            previousPrice = existing.currentPrice,
            priceHistory = priceHistory,
            targetPrice = existing.targetPrice,
            minPrice = insights.lowest,
            maxPrice = insights.highest,
            avgPrice = insights.average,
            trend = calculateTrendFromHistory(historyEntities)
        )
        
        // Use real price logic from BuyScoreEngine
        val scoreResult = BuyScoreEngine.calculateScore(tempItem)

        val finalItem = tempItem.copy(
            history = priceHistory.map { it.price },
            imageUrl = newItem.imageUrl ?: existing.imageUrl,
            isTracked = true,
            alertCondition = existing.alertCondition,
            notifyPush = existing.notifyPush,
            notifySms = existing.notifySms,
            notifyEmail = existing.notifyEmail,
            notifyWhatsapp = existing.notifyWhatsapp,
            isAlertEnabled = existing.isAlertEnabled,
            recommendation = scoreResult.recommendation
        )

        firestoreRepository.trackProduct(currentUserId, TrackedProduct(
            productId = productId,
            url = finalItem.url,
            name = finalItem.name,
            source = finalItem.source,
            currentPrice = finalItem.currentPrice,
            initialPrice = existing.initialPrice,
            previousPrice = existing.currentPrice,
            imageUrl = finalItem.imageUrl,
            imagesString = finalItem.images.joinToString(","),
            targetPrice = finalItem.targetPrice,
            isAlertEnabled = finalItem.isAlertEnabled,
            category = finalItem.category,
            addedAt = existing.addedAt,
            lastCheckedAt = now,
            alertCondition = finalItem.alertCondition,
            notifyPush = finalItem.notifyPush,
            notifySms = finalItem.notifySms,
            notifyEmail = finalItem.notifyEmail,
            notifyWhatsapp = finalItem.notifyWhatsapp
        ))

        return finalItem
    }

    suspend fun refreshAllTracked(): Int {
        val products = dao.getAlertEnabledProducts()
        var refreshed = 0
        for (product in products) {
            try {
                refreshProductPrice(product.url)
                refreshed++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh ${product.name}: ${e.message}")
            }
        }
        return refreshed
    }

    // ==================== ALERTS ====================

    fun getAllAlerts(): Flow<List<PriceAlertEntity>> {
        return dao.getAllAlerts(currentUserId)
    }

    suspend fun markAlertsRead(productUrl: String) {
        dao.markAlertsReadForProduct(productUrl, currentUserId)
    }

    suspend fun markAllAlertsRead() {
        dao.markAllAlertsRead(currentUserId)
    }

    suspend fun clearAllAlerts() {
        dao.deleteAllAlerts(currentUserId)
    }

    suspend fun deleteAlert(id: Int) {
        dao.deleteAlert(id, currentUserId)
    }

    // ==================== TRACKED PRODUCTS ====================

    /**
     * Syncs user tracking data from Firestore to local Room DB.
     * Essential for multi-device support.
     */
    suspend fun syncFromFirestore() {
        if (currentUserId == "anonymous") return
        
        try {
            // 1. Sync Tracked Products
            val remoteTracked = firestoreRepository.getTrackedProducts(currentUserId)
            remoteTracked.forEach { remote ->
                val local = dao.getTrackedProduct(remote.url, currentUserId)
                if (local == null) {
                    dao.insertTrackedProduct(TrackedProductEntity(
                        userId = currentUserId,
                        url = remote.url,
                        name = remote.name,
                        source = remote.source,
                        currentPrice = remote.currentPrice,
                        initialPrice = remote.initialPrice,
                        previousPrice = remote.previousPrice,
                        imageUrl = remote.imageUrl,
                        imagesString = remote.imagesString,
                        targetPrice = remote.targetPrice,
                        isAlertEnabled = remote.isAlertEnabled,
                        category = remote.category,
                        addedAt = remote.addedAt,
                        lastCheckedAt = remote.lastCheckedAt,
                        alertCondition = remote.alertCondition,
                        notifyPush = remote.notifyPush,
                        notifySms = remote.notifySms,
                        notifyEmail = remote.notifyEmail,
                        notifyWhatsapp = remote.notifyWhatsapp
                    ))
                }
            }

            // 2. Sync Alerts
            val remoteAlerts = firestoreRepository.getAlerts(currentUserId)
            remoteAlerts.forEach { remote ->
                // Check if alert already exists (basic check by timestamp and product)
                // In a real app, we'd use a UUID for alerts
                val alerts = dao.getAlertsForProduct(remote.productUrl, currentUserId)
                if (alerts.none { it.createdAt == remote.createdAt }) {
                    dao.insertAlert(PriceAlertEntity(
                        userId = currentUserId,
                        productUrl = remote.productUrl,
                        productTitle = remote.productTitle,
                        oldPrice = remote.oldPrice,
                        newPrice = remote.newPrice,
                        targetPrice = remote.targetPrice,
                        isRead = remote.isRead,
                        createdAt = remote.createdAt
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}")
        }
    }

    fun getAllTrackedProducts(): Flow<List<TrackedProductEntity>> {
        return dao.getAllTrackedProducts(currentUserId)
    }

    // ==================== PRICE HISTORY ====================

    fun getPriceHistory(url: String): Flow<List<PriceData>> {
        return dao.getPriceHistory(url).map { entities ->
            entities.map { it.toPriceData() }
        }
    }

    // ==================== DERIVED METRICS ====================

    suspend fun getInsights(url: String, days: Int = 30): Insights {
        val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return Insights(
            lowest = dao.getLowestPriceSince(url, since) ?: 0.0,
            highest = dao.getHighestPriceSince(url, since) ?: 0.0,
            average = dao.getAveragePriceSince(url, since) ?: 0.0,
            volatility = dao.getVolatilitySince(url, since) ?: 0.0
        )
    }

    private fun calculateTrendFromHistory(history: List<PriceEntity>): String {
        if (history.size < 2) return "STABLE"
        
        val latest = history[0].price
        val previous = history[1].price
        
        return when {
            latest < previous -> "DOWN"
            latest > previous -> "UP"
            else -> "STABLE"
        }
    }

    data class Insights(
        val lowest: Double,
        val highest: Double,
        val average: Double,
        val volatility: Double
    )
}
