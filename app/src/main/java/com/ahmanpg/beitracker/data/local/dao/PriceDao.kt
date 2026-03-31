package com.ahmanpg.beitracker.data.local.dao

import androidx.room.*
import com.ahmanpg.beitracker.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(price: PriceEntity)

    @Query("SELECT * FROM price_history WHERE productUrl = :url ORDER BY timestamp DESC")
    fun getHistoryForUrl(url: String): Flow<List<PriceEntity>>

    @Query("SELECT * FROM price_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PriceEntity>>

    @Query("DELETE FROM price_history WHERE productUrl = :url")
    suspend fun deleteByUrl(url: String)
}
