package com.ahmanpg.beitracker.data.remote.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Global canonical product model.
 * Path: /products/{productId}
 */
data class Product(
    val id: String = "",
    val url: String = "",
    val name: String = "",
    val model: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val currentPrice: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val avgPrice: Double = 0.0,
    val priceChangePercent: Double = 0.0,
    @ServerTimestamp val lastUpdated: Timestamp? = null
)

/**
 * Time-series price history entry.
 * Path: /products/{productId}/priceHistory/{entryId}
 */
data class PriceHistoryEntry(
    val price: Double = 0.0,
    val source: String = "",
    val url: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

/**
 * Individual store listing for a product.
 * Path: /products/{productId}/listings/{listingId}
 */
data class ProductListing(
    val price: Double = 0.0,
    val source: String = "",
    val url: String = "",
    @ServerTimestamp val lastSeen: Timestamp? = null
)

/**
 * Product metadata for user tracking.
 * Path: /users/{userId}/trackedItems/{productId}
 */
data class TrackedProduct(
    val productId: String = "",
    val url: String = "",
    val name: String = "",
    val source: String = "",
    val currentPrice: Double = 0.0,
    val initialPrice: Double = 0.0,
    val previousPrice: Double = 0.0,
    val imageUrl: String? = null,
    val imagesString: String? = null,
    val targetPrice: Double? = null,
    val isAlertEnabled: Boolean = true,
    val category: String? = null,
    val addedAt: Long = 0,
    val lastCheckedAt: Long = 0,
    val alertCondition: String = "BELOW",
    val notifyPush: Boolean = true,
    val notifySms: Boolean = false,
    val notifyEmail: Boolean = false,
    val notifyWhatsapp: Boolean = false,
    val lastNotifiedPrice: Double = 0.0
)

/**
 * Price alert record.
 * Path: /users/{userId}/alerts/{alertId}
 */
data class PriceAlert(
    val id: String = "",
    val productUrl: String = "",
    val productTitle: String = "",
    val oldPrice: Double = 0.0,
    val newPrice: Double = 0.0,
    val targetPrice: Double? = null,
    val isRead: Boolean = false,
    val createdAt: Long = 0
)

/**
 * Product watchers for notifications.
 * Path: /product_watchers/{productId}
 */
data class ProductWatchers(
    val userIds: List<String> = emptyList()
)

/**
 * Data model for /users/{userId}/settings
 */
data class UserSettings(
    val userName: String = "",
    val userEmail: String = "",
    val checkIntervalHours: Int = 6,
    val notificationsEnabled: Boolean = true,
    val searchHistory: List<String> = emptyList()
)

/**
 * Data model for /users/{userId}/userChats/{chatId}
 */
data class UserChatSummary(
    val chatRef: DocumentReference? = null,
    val unreadCount: Int = 0,
    val lastMessageContent: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val otherParticipantInfo: ParticipantInfo? = null,
    val groupName: String? = null,
    val groupImageUrl: String? = null,
    val lastAccessed: Timestamp? = null
)

data class ParticipantInfo(
    val userId: String = "",
    val name: String = "",
    val imageUrl: String = ""
)

/**
 * Data model for /chats/{chatId}
 */
data class ChatRoom(
    val participants: List<String> = emptyList(),
    val type: String = "one-on-one", // "one-on-one" or "group"
    val lastMessage: LastMessage? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val groupName: String? = null,
    val groupImageUrl: String? = null
)

data class LastMessage(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null
)

/**
 * Data model for /chats/{chatId}/messages/{messageId}
 */
data class ChatMessage(
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val type: String = "text", // "text", "image", "audio", "system"
    val imageUrl: String? = null,
    val readBy: List<String> = emptyList()
)
