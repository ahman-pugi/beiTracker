package com.ahmanpg.beitracker.data.di

import android.content.Context
import androidx.room.Room
import com.ahmanpg.beitracker.data.local.AppDatabase
import com.ahmanpg.beitracker.data.local.dao.PriceDao
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
            "beic_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePriceDao(appDatabase: AppDatabase): PriceDao {
        return appDatabase.priceDao()
    }
}
