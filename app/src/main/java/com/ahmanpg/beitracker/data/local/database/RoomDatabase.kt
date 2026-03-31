package com.ahmanpg.beitracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ahmanpg.beitracker.data.local.dao.PriceDao
import com.ahmanpg.beitracker.data.local.entity.PriceEntity

@Database(
    entities = [PriceEntity::class],
    version = 5,                    // ← Increased to 5
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun priceDao(): PriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beitracker_database"
                )
                    .fallbackToDestructiveMigration()   // Delete old DB on version change
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}