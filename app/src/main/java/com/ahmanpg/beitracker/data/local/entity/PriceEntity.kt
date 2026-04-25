package com.ahmanpg.beitracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ahmanpg.beitracker.data.repository.PriceData

@Entity(
    tableName = "price_history",
    indices = [Index(value = ["productUrl", "timestamp"], unique = false)],
    foreignKeys = [
        ForeignKey(
            entity = TrackedProductEntity::class,
            parentColumns = ["url"],
            childColumns = ["productUrl"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PriceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productUrl: String,
    val title: String, // Keeping this for UI convenience, though not in spec's history
    val price: Double,
    val changeType: String = "SAME", // "DROP", "INCREASE", "SAME"
    val changeAmount: Double = 0.0,    // price difference (TZS)
    val imageUrl: String? = null,
    val source: String = "jiji",
    val timestamp: Long = System.currentTimeMillis() // spec: recorded_at
) {
    fun toPriceData(): PriceData {
        return PriceData(
            title = title,
            price = price,
            changeType = changeType,
            changeAmount = changeAmount,
            url = productUrl,
            imageUrl = imageUrl,
            source = source,
            timestamp = timestamp
        )
    }
}
