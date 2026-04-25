package com.ahmanpg.beitracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ahmanpg.beitracker.data.local.dao.PriceDao
import com.ahmanpg.beitracker.data.local.entity.DiscoveryItemEntity
import com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity
import com.ahmanpg.beitracker.data.local.entity.PriceEntity
import com.ahmanpg.beitracker.data.local.entity.RecentlyViewedEntity
import com.ahmanpg.beitracker.data.local.entity.TrackedProductEntity

@Database(
    entities = [
        PriceEntity::class,
        TrackedProductEntity::class,
        PriceAlertEntity::class,
        RecentlyViewedEntity::class,
        DiscoveryItemEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun priceDao(): PriceDao
}
