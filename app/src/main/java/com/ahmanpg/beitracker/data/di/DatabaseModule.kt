package com.ahmanpg.beitracker.data.di

import android.content.Context
import androidx.room.Room
import com.ahmanpg.beitracker.data.local.AppDatabase
import com.ahmanpg.beitracker.data.local.dao.PriceDao
import com.ahmanpg.beitracker.data.repository.PriceScraper
import com.ahmanpg.beitracker.data.repository.PriceRepository
import com.ahmanpg.beitracker.data.remote.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
//import com.ahmanpg.beitracker.worker.AlertChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "beitracker_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePriceDao(db: AppDatabase): PriceDao = db.priceDao()

    @Provides
    @Singleton
    fun providePriceScraper(): PriceScraper = PriceScraper()

    @Provides
    @Singleton
    fun providePriceRepository(
        dao: PriceDao,
        scraper: PriceScraper,
        firestoreRepository: FirestoreRepository,
        auth: FirebaseAuth
    ): PriceRepository {
        return PriceRepository(dao, scraper, firestoreRepository, auth)
    }
}
