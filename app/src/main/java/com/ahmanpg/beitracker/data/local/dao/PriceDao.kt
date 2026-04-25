package com.ahmanpg.beitracker.data.local.dao

import androidx.room.*
import com.ahmanpg.beitracker.data.local.entity.DiscoveryItemEntity
import com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity
import com.ahmanpg.beitracker.data.local.entity.PriceEntity
import com.ahmanpg.beitracker.data.local.entity.RecentlyViewedEntity
import com.ahmanpg.beitracker.data.local.entity.TrackedProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {

    // ==================== PRICE HISTORY ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: PriceEntity)

    @Query("SELECT * FROM price_history WHERE productUrl = :url ORDER BY timestamp ASC")
    fun getPriceHistory(url: String): Flow<List<PriceEntity>>

    @Query("SELECT * FROM price_history WHERE productUrl = :url ORDER BY timestamp ASC")
    suspend fun getPriceHistoryList(url: String): List<PriceEntity>

    @Query("SELECT * FROM price_history WHERE productUrl = :url ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrice(url: String): PriceEntity?

    @Query("SELECT MIN(price) FROM price_history WHERE productUrl = :url AND timestamp >= :since")
    suspend fun getLowestPriceSince(url: String, since: Long): Double?

    @Query("SELECT MAX(price) FROM price_history WHERE productUrl = :url AND timestamp >= :since")
    suspend fun getHighestPriceSince(url: String, since: Long): Double?

    @Query("SELECT AVG(price) FROM price_history WHERE productUrl = :url AND timestamp >= :since")
    suspend fun getAveragePriceSince(url: String, since: Long): Double?

    @Query("SELECT AVG(price) FROM price_history WHERE productUrl = :url AND timestamp >= :start AND timestamp <= :end")
    suspend fun getAveragePriceInRange(url: String, start: Long, end: Long): Double?

    @Query("SELECT MIN(timestamp) FROM price_history WHERE productUrl = :url")
    suspend fun getFirstTrackedDate(url: String): Long?

    @Query("SELECT AVG(ABS(changeAmount)) FROM price_history WHERE productUrl = :url AND timestamp >= :since")
    suspend fun getVolatilitySince(url: String, since: Long): Double?

    @Query("SELECT * FROM price_history WHERE changeType = 'DROP' AND ABS(changeAmount) > :threshold")
    suspend fun getSignificantDrops(threshold: Double): List<PriceEntity>

    @Query("DELETE FROM price_history WHERE productUrl = :url")
    suspend fun deletePriceHistory(url: String)

    @Query("DELETE FROM price_history WHERE timestamp < :threshold")
    suspend fun cleanupOldHistory(threshold: Long)

    @Query("SELECT * FROM price_history WHERE title LIKE :pattern ORDER BY timestamp ASC")
    fun getHistoryByPattern(pattern: String): Flow<List<PriceEntity>>

    // ==================== TRACKED PRODUCTS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedProduct(product: TrackedProductEntity)

    @Query("SELECT * FROM tracked_products WHERE userId = :userId ORDER BY lastCheckedAt DESC")
    fun getAllTrackedProducts(userId: String): Flow<List<TrackedProductEntity>>

    @Query("SELECT * FROM tracked_products WHERE url = :url AND userId = :userId")
    suspend fun getTrackedProduct(url: String, userId: String): TrackedProductEntity?

    @Query("SELECT * FROM tracked_products WHERE isAlertEnabled = 1")
    suspend fun getAlertEnabledProducts(): List<TrackedProductEntity>

    @Update
    suspend fun updateTrackedProduct(product: TrackedProductEntity)

    @Delete
    suspend fun deleteTrackedProduct(product: TrackedProductEntity)

    @Query("DELETE FROM tracked_products WHERE url = :url AND userId = :userId")
    suspend fun deleteTrackedProductByUrl(url: String, userId: String)

    // ==================== PRICE ALERTS ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity)

    @Query("SELECT * FROM price_alerts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllAlerts(userId: String): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE userId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadAlerts(userId: String): Flow<List<PriceAlertEntity>>

    @Query("SELECT COUNT(*) FROM price_alerts WHERE userId = :userId AND isRead = 0")
    fun getUnreadAlertCount(userId: String): Flow<Int>

    @Update
    suspend fun markAlertRead(alert: PriceAlertEntity)

    @Query("UPDATE price_alerts SET isRead = 1 WHERE productUrl = :productUrl AND userId = :userId")
    suspend fun markAlertsReadForProduct(productUrl: String, userId: String)

    @Query("SELECT * FROM price_alerts WHERE productUrl = :url AND userId = :userId")
    suspend fun getAlertsForProduct(url: String, userId: String): List<PriceAlertEntity>

    @Query("UPDATE price_alerts SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAlertsRead(userId: String)

    @Query("DELETE FROM price_alerts WHERE id = :id AND userId = :userId")
    suspend fun deleteAlert(id: Int, userId: String)

    @Query("DELETE FROM price_alerts WHERE userId = :userId")
    suspend fun deleteAllAlerts(userId: String)

    // ==================== RECENTLY VIEWED ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentlyViewed(product: RecentlyViewedEntity)

    @Query("SELECT * FROM recently_viewed ORDER BY viewedAt DESC LIMIT 20")
    fun getRecentlyViewed(): Flow<List<RecentlyViewedEntity>>

    @Query("DELETE FROM recently_viewed WHERE url = :url")
    suspend fun deleteRecentlyViewed(url: String)

    @Query("DELETE FROM recently_viewed")
    suspend fun clearRecentlyViewed()

    // ==================== DISCOVERY CACHE ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscoveryItems(items: List<DiscoveryItemEntity>)

    @Query("SELECT * FROM discovery_items WHERE section = :section ORDER BY updatedAt DESC")
    fun getDiscoveryItems(section: String): Flow<List<DiscoveryItemEntity>>

    @Query("DELETE FROM discovery_items WHERE section = :section")
    suspend fun clearDiscoverySection(section: String)
}
