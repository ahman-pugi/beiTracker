package com.ahmanpg.beitracker.data.repository

import com.ahmanpg.beitracker.data.local.dao.PriceDao
import com.ahmanpg.beitracker.data.local.entity.PriceEntity
import com.ahmanpg.beitracker.data.model.TrackedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PriceRepository @Inject constructor(
    private val dao: PriceDao,
    private val scraper: PriceScraper
) {

    suspend fun searchProducts(query: String): List<TrackedItem> {
        return scraper.searchProducts(query)
    }

    suspend fun fetchAndSaveCurrentPrice(query: String): Result<PriceData> {
        return try {
            val results = scraper.searchProducts(query)

            if (results.isEmpty()) {
                return Result.failure(Exception("No products found"))
            }

            val product = results.first()

            val entity = PriceEntity(
                productUrl = product.url,
                title = product.name,
                price = product.currentPrice ?: 0.0,
                timestamp = System.currentTimeMillis(),
                source = product.source
            )

            dao.insert(entity)

            Result.success(
                PriceData(
                    title = product.name,
                    price = product.currentPrice,
                    url = product.url
                )
            )


        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPriceHistory(url: String): Flow<List<PriceData>> {
        return dao.getHistoryForUrl(url).map { list ->
            list.map {
                PriceData(
                    title = it.title,
                    price = it.price,
                    timestamp = it.timestamp,
                    url = it.productUrl
                )
            }
        }
    }

    fun getAllTrackedProducts(): Flow<List<PriceData>> {
        return dao.getAll().map { list ->
            list.map {
                PriceData(
                    title = it.title,
                    price = it.price,
                    timestamp = it.timestamp,
                    url = it.productUrl
                )
            }
        }
    }

    suspend fun deleteProduct(url: String) {
        dao.deleteByUrl(url)
    }
}
