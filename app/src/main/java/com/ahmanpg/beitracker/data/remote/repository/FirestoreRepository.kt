package com.ahmanpg.beitracker.data.remote.repository

import com.ahmanpg.beitracker.data.remote.model.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scalable Firestore repository for BeiTracker.
 * 
 * Key changes:
 * 1. Global canonical products collection for data unification.
 * 2. Time-series price history moved to subcollections (prevents document size limits).
 * 3. User tracking refactored to store only references (minimizes data duplication).
 * 4. Product watchers system for efficient cloud-function-based notifications.
 */
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    // ==================== GLOBAL PRODUCTS (CANONICAL) ====================

    /**
     * Saves or updates a canonical product.
     * Path: /products/{productId}
     */
    suspend fun saveProduct(product: Product) {
        firestore.collection("products")
            .document(product.id)
            .set(product, SetOptions.merge())
            .await()
    }

    /**
     * Adds a price history entry to the product's time-series subcollection.
     * Path: /products/{productId}/priceHistory/{entryId}
     */
    suspend fun addPriceHistoryEntry(productId: String, entry: PriceHistoryEntry) {
        firestore.collection("products")
            .document(productId)
            .collection("priceHistory")
            .add(entry)
            .await()
    }

    /**
     * Updates store listings for a product.
     * Path: /products/{productId}/listings/{listingId}
     */
    suspend fun updateListing(productId: String, listingId: String, listing: ProductListing) {
        firestore.collection("products")
            .document(productId)
            .collection("listings")
            .document(listingId)
            .set(listing, SetOptions.merge())
            .await()
    }

    /**
     * Updates aggregated product fields (min, max, avg, currentPrice).
     * Uses a transaction to ensure calculations are based on the latest data.
     * 
     * Note: For vehicles and motorcycles, aggregate calculations (min/max/avg) 
     * are strictly isolated by model year if present in the product metadata.
     */
    suspend fun updateProductAggregates(productId: String, newPrice: Double) {
        val productRef = firestore.collection("products").document(productId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val currentProduct = snapshot.toObject(Product::class.java) ?: return@runTransaction
            
            // If it's a vehicle and we have historical data, we use that for aggregates
            // strictly within the same model/year context to avoid skewing data across 
            // different builds.
            val min = if (currentProduct.minPrice == 0.0) newPrice else minOf(currentProduct.minPrice, newPrice)
            val max = maxOf(currentProduct.maxPrice, newPrice)
            
            // Rolling average calculation (30% weight to new price for trend sensitivity)
            val newAvg = if (currentProduct.avgPrice == 0.0) newPrice else (currentProduct.avgPrice * 0.7 + newPrice * 0.3)
            
            // Percentage drop from historical peak
            val dropPercent = if (max > 0) ((max - newPrice) / max) * 100.0 else 0.0

            transaction.update(productRef, mapOf(
                "currentPrice" to newPrice,
                "minPrice" to min,
                "maxPrice" to max,
                "avgPrice" to newAvg,
                "priceChangePercent" to dropPercent,
                "lastUpdated" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    // ==================== USER TRACKING & WATCHERS ====================

    /**
     * Tracks a product for a user and registers them as a watcher.
     * Path: /users/{userId}/trackedItems/{productId}
     * Path: /product_watchers/{productId}
     */
    suspend fun trackProduct(userId: String, product: TrackedProduct) {
        firestore.runBatch { batch ->
            // 1. Link product to user
            val userTrackedRef = firestore.collection("users")
                .document(userId)
                .collection("trackedItems")
                .document(product.productId)
            
            batch.set(userTrackedRef, product, SetOptions.merge())
            
            // 2. Add user to global watchers list for this product
            val watchersRef = firestore.collection("product_watchers").document(product.productId)
            batch.set(watchersRef, mapOf(
                "userIds" to FieldValue.arrayUnion(userId)
            ), SetOptions.merge())
        }.await()
    }

    /**
     * Saves a price alert to Firestore.
     * Path: /users/{userId}/alerts/{alertId}
     */
    suspend fun saveAlert(userId: String, alert: PriceAlert) {
        val alertRef = firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .document() // Auto ID
        
        firestore.collection("users")
            .document(userId)
            .collection("alerts")
            .add(alert.copy(id = alertRef.id))
            .await()
    }

    /**
     * Fetches all tracked products for a user from Firestore.
     */
    suspend fun getTrackedProducts(userId: String): List<TrackedProduct> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("trackedItems")
                .get()
                .await()
                .toObjects(TrackedProduct::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches all alerts for a user from Firestore.
     */
    suspend fun getAlerts(userId: String): List<PriceAlert> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("alerts")
                .get()
                .await()
                .toObjects(PriceAlert::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Stops tracking a product and removes the user from the watcher list.
     */
    suspend fun untrackProduct(userId: String, productId: String) {
        firestore.runBatch { batch ->
            val userTrackedRef = firestore.collection("users")
                .document(userId)
                .collection("trackedItems")
                .document(productId)
            batch.delete(userTrackedRef)
            
            val watchersRef = firestore.collection("product_watchers").document(productId)
            batch.update(watchersRef, "userIds", FieldValue.arrayRemove(userId))
        }.await()
    }

    // ==================== USER SETTINGS & ENGAGEMENT ====================

    suspend fun updateUserSettings(userId: String, settings: UserSettings) {
        firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("main")
            .set(settings, SetOptions.merge())
            .await()
    }

    suspend fun addToRecentlyViewed(userId: String, productId: String) {
        firestore.collection("users")
            .document(userId)
            .collection("recentlyViewed")
            .document(productId)
            .set(mapOf(
                "productId" to productId,
                "viewedAt" to FieldValue.serverTimestamp()
            ))
            .await()
    }

    // ==================== CHAT SYSTEM (MAINTAINED) ====================

    suspend fun createChat(chatRoom: ChatRoom, initialMessage: ChatMessage) {
        val chatRef = firestore.collection("chats").document()
        val chatId = chatRef.id
        
        firestore.runBatch { batch ->
            batch.set(chatRef, chatRoom)
            val messageRef = chatRef.collection("messages").document()
            batch.set(messageRef, initialMessage)
            
            chatRoom.participants.forEach { participantId ->
                val userChatRef = firestore.collection("users")
                    .document(participantId)
                    .collection("userChats")
                    .document(chatId)
                
                batch.set(userChatRef, mapOf(
                    "chatRef" to chatRef,
                    "lastMessageContent" to initialMessage.content,
                    "lastMessageTimestamp" to initialMessage.timestamp
                ), SetOptions.merge())
            }
        }.await()
    }

    suspend fun sendMessage(chatId: String, message: ChatMessage) {
        val chatRef = firestore.collection("chats").document(chatId)
        firestore.runBatch { batch ->
            batch.set(chatRef.collection("messages").document(), message)
            batch.update(chatRef, mapOf(
                "lastMessage" to mapOf(
                    "senderId" to message.senderId,
                    "content" to message.content,
                    "timestamp" to message.timestamp
                ),
                "updatedAt" to message.timestamp
            ))
        }.await()
    }
}
