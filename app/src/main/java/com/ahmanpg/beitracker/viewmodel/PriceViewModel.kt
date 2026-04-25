package com.ahmanpg.beitracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.repository.PriceData
import com.ahmanpg.beitracker.data.repository.PriceRepository
import com.ahmanpg.beitracker.data.local.SettingsManager
import com.ahmanpg.beitracker.worker.PriceCheckWorker
import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.ahmanpg.beitracker.data.model.SearchQuery
import com.ahmanpg.beitracker.data.model.SortType
import com.ahmanpg.beitracker.ui.components.PricePoint
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiPriceDropRed
import com.ahmanpg.beitracker.util.LifecycleEngine
import com.ahmanpg.beitracker.util.LifecycleStage
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale
import com.google.gson.Gson

data class MarketStats(
    val minPrice: Double,
    val maxPrice: Double,
    val averagePrice: Double,
    val currentPrice: Double,
    val historyPoints: List<PricePoint>,
    val stability: String = "STABLE",
    val stabilityColor: Color = BeiAccentGreen
)

@HiltViewModel
class PriceViewModel @Inject constructor(
    private val repository: PriceRepository,
    private val settingsManager: SettingsManager,
    private val application: Application
) : ViewModel() {

    private val gson = Gson()
    private val _uiState = MutableStateFlow(PriceUiState(
        checkIntervalHours = settingsManager.checkIntervalHours,
        notificationsEnabled = settingsManager.notificationsEnabled,
        userName = settingsManager.userName,
        userEmail = settingsManager.userEmail,
        userBio = settingsManager.userBio,
        profileImageUri = settingsManager.profileImageUri,
        joinDate = settingsManager.joinDate,
        totalSavings = settingsManager.totalSavings,
        accountType = settingsManager.accountType,
        searchHistory = settingsManager.searchHistory,
        preferredRegion = settingsManager.preferredRegion,
        preferredLanguage = settingsManager.preferredLanguage,
        alertThresholdPercent = settingsManager.alertThresholdPercent,
        preferredCurrency = settingsManager.preferredCurrency,
        themeMode = settingsManager.themeMode
    ))
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    /**
     * Initializes the Gemini 1.5 Flash model via Vertex AI for Firebase.
     * Configured to return structured JSON responses for predictable parsing.
     */
    private val generativeModel by lazy {
        Firebase.vertexAI.generativeModel(
            modelName = "gemini-1.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    init {
        loadTrackedProducts()
        loadAlerts()
        loadRecentlyViewed()
        observeDiscoveryCache()
        refreshExploreData()
        observeSettings()
        syncData()
    }

    private fun syncData() {
        viewModelScope.launch {
            repository.syncFromFirestore()
        }
    }

    private fun observeSettings() {
        settingsManager.themeModeFlow
            .onEach { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDiscoveryCache() {
        repository.getCachedDiscoveryItems("POPULAR")
            .onEach { items -> 
                val trackedUrls = uiState.value.trackedProducts.map { it.url }.toSet()
                _uiState.update { currentState -> currentState.copy(popularProducts = items.map { it.copy(isTracked = trackedUrls.contains(it.url)) }) } 
            }
            .launchIn(viewModelScope)

        repository.getCachedDiscoveryItems("TOP_DROPS")
            .onEach { items -> 
                val trackedUrls = uiState.value.trackedProducts.map { it.url }.toSet()
                val sortedItems = items
                    .map { it.copy(isTracked = trackedUrls.contains(it.url)) }
                    .sortedByDescending { it.flashSaleDropPercent }
                
                _uiState.update { it.copy(topPriceDrops = sortedItems) } 
            }
            .launchIn(viewModelScope)

        repository.getCachedDiscoveryItems("FEATURED")
            .onEach { items -> 
                val trackedUrls = uiState.value.trackedProducts.map { it.url }.toSet()
                val processed = items.map { it.copy(isTracked = trackedUrls.contains(it.url)) }
                _uiState.update { it.copy(hotDeals = processed, heroProduct = processed.firstOrNull()) } 
            }
            .launchIn(viewModelScope)

        val categoryLabels = listOf("Smartphones", "Laptops", "TVs & Entertainment", "Smartwatches", "Audio & Sound", "Home Appliances", "Vehicles", "Cameras")
        categoryLabels.forEach { label ->
            repository.getCachedDiscoveryItems("CAT_$label")
                .onEach { items ->
                    val trackedUrls = uiState.value.trackedProducts.map { it.url }.toSet()
                    _uiState.update { currentState ->
                        val newCategories = currentState.categories.toMutableMap()
                        newCategories[label] = items.map { it.copy(isTracked = trackedUrls.contains(it.url)) }
                        currentState.copy(categories = newCategories)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun refreshExploreData() {
        viewModelScope.launch {
            try {
                val featured = repository.getHighlightedDeals()
                repository.updateDiscoveryCache("FEATURED", featured)
                
                val popular = repository.getPopularProducts()
                repository.updateDiscoveryCache("POPULAR", popular)
                
                val drops = repository.getTopPriceDrops()
                repository.updateDiscoveryCache("TOP_DROPS", drops)

                fetchCategories()
            } catch (e: Exception) {
                Log.e("PriceViewModel", "Silent refresh error: ${e.message}")
            }
        }
    }

    // ==================== SETTINGS ====================

    fun updateCheckInterval(hours: Int) {
        settingsManager.checkIntervalHours = hours
        _uiState.update { it.copy(checkIntervalHours = hours) }
        PriceCheckWorker.schedule(application, hours)
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        settingsManager.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) {
            PriceCheckWorker.schedule(application, settingsManager.checkIntervalHours)
        } else {
            PriceCheckWorker.cancel(application)
        }
    }

    fun updateProfile(name: String, email: String, bio: String? = null, imageUri: String? = null) {
        settingsManager.userName = name
        settingsManager.userEmail = email
        bio?.let { settingsManager.userBio = it }
        imageUri?.let { settingsManager.profileImageUri = it }
        
        _uiState.update { it.copy(
            userName = name, 
            userEmail = email,
            userBio = settingsManager.userBio,
            profileImageUri = settingsManager.profileImageUri
        ) }
    }

    fun updateRegion(region: String) {
        settingsManager.preferredRegion = region
        _uiState.update { it.copy(preferredRegion = region) }
    }

    fun updateLanguage(langCode: String) {
        settingsManager.preferredLanguage = langCode
        _uiState.update { it.copy(preferredLanguage = langCode) }
    }

    fun updateCurrency(currency: String) {
        settingsManager.preferredCurrency = currency
        _uiState.update { it.copy(preferredCurrency = currency) }
    }

    fun updateAlertThreshold(percent: Int) {
        settingsManager.alertThresholdPercent = percent
        _uiState.update { it.copy(alertThresholdPercent = percent) }
    }

    fun updateThemeMode(mode: Int) {
        settingsManager.themeMode = mode
    }

    // ==================== SEARCH ENGINE ====================

    fun searchProducts(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            settingsManager.addSearchQuery(input)
            _uiState.update { it.copy(isLoading = true, error = null, searchHistory = settingsManager.searchHistory) }
            try {
                val mappedInput = when(input.lowercase()) {
                    "tvs" -> "Television"
                    "smartphones" -> "Smartphone"
                    "vehicles" -> "Car Motorcycle"
                    "audio" -> "Speaker Headphones"
                    "watches" -> "Smartwatch"
                    "cameras" -> "Camera"
                    "laptops" -> "Laptop"
                    else -> input
                }
                
                val aiResponse = parseSearchWithAi(mappedInput)
                val results = repository.searchProducts(aiResponse.keyword)
                processSearchResults(
                    SearchQuery(
                        keyword = aiResponse.keyword,
                        minPrice = aiResponse.minPrice,
                        maxPrice = aiResponse.maxPrice,
                        sort = SortType.valueOf(aiResponse.sort)
                    ), 
                    results,
                    aiResponse.reasoning
                )
            } catch (e: Exception) {
                Log.e("PriceViewModel", "Search error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = "Search failed. Please try again.") }
            }
        }
    }

    fun updateSearchSuggestions(input: String) {
        val suggestions = if (input.isBlank()) {
            emptyList()
        } else {
            settingsManager.searchHistory.filter { it.contains(input, ignoreCase = true) }.take(5)
        }
        _uiState.update { it.copy(searchSuggestions = suggestions) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(
            searchResults = emptyList(),
            smartSearchLabel = "",
            isLoading = false,
            searchSuggestions = emptyList()
        ) }
    }

    /**
     * Uses AI to parse natural language search queries into structured data.
     * It handles Tanzanian local context, slang (like 'ist', 'bodaboda'), and price constraints.
     * Returns an AiSearchResponse containing keywords, price limits, and reasoning.
     */
    internal suspend fun parseSearchWithAi(input: String): AiSearchResponse {
        val prompt = """
            Act as a price history lookup agent similar to CamelCamelCamel for a Tanzanian e-commerce app.
            Parse the user query: "$input"
            
            1. If it's a URL (darshopping, Zudua, or Jiji), treat it as a price-history lookup request and extract the product.
            2. If it's a product name, identify the likely product.
            3. Consider Swahili slang and brand abbreviations commonly used in Tanzania:
               - 'ist' -> "Toyota IST"
               - 'vitz' -> "Toyota Vitz"
               - 'mac' -> "MacBook"
               - 'p8' -> "Google Pixel 8"
               - 'tv' or 'tvs' (when in electronics context) -> "Television"
               - 'bodaboda' or 'pikipiki' -> "Motorcycle"
            
            Return ONLY a JSON object with keyword, minPrice, maxPrice, sort, and reasoning.
            The 'reasoning' should be a brief data-driven explanation focused on helping the user decide the best time to buy.
        """.trimIndent()
        
        return try {
            val response = generativeModel.generateContent(prompt)
            val rawText = response.text ?: ""
            val jsonStr = if (rawText.contains("{") && rawText.contains("}")) {
                rawText.substringAfter("{").substringBeforeLast("}").let { "{ $it }" }
            } else {
                rawText
            }
            gson.fromJson(jsonStr, AiSearchResponse::class.java)
        } catch (e: Exception) {
            Log.e("PriceViewModel", "AI Parse error: ${e.message}, raw: ${input}")
            AiSearchResponse(keyword = input, minPrice = null, maxPrice = null, sort = "RELEVANCE", reasoning = "Standard search fallback")
        }
    }

    internal fun updateIsTrackedStatus(url: String, isTracked: Boolean) {
        _uiState.update { currentState ->
            val updateItem = { it: TrackedItem -> if (it.url == url) it.copy(isTracked = isTracked) else it }
            currentState.copy(
                searchResults = currentState.searchResults.map(updateItem),
                popularProducts = currentState.popularProducts.map(updateItem),
                topPriceDrops = currentState.topPriceDrops.map(updateItem),
                hotDeals = currentState.hotDeals.map(updateItem),
                categories = currentState.categories.mapValues { it.value.map(updateItem) },
                selectedProduct = if (currentState.selectedProduct?.url == url) currentState.selectedProduct.copy(isTracked = isTracked) else currentState.selectedProduct
            )
        }
    }

    data class AiSearchResponse(val keyword: String, val minPrice: Double?, val maxPrice: Double?, val sort: String, val reasoning: String)

    fun clearSearchHistory() {
        settingsManager.clearSearchHistory()
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }

    internal fun processSearchResults(query: SearchQuery, rawResults: List<TrackedItem>, aiReasoning: String?) {
        val trackedUrls = uiState.value.trackedProducts.map { it.url }.toSet()
        var filtered = rawResults.filter { it.currentPrice > 0 }
        
        if (query.minPrice != null) filtered = filtered.filter { it.currentPrice >= query.minPrice }
        if (query.maxPrice != null) filtered = filtered.filter { it.currentPrice <= query.maxPrice }
        if (query.sort == SortType.PRICE_LOW_TO_HIGH) filtered = filtered.sortedBy { it.currentPrice }

        val processed = filtered.map { it.copy(isTracked = trackedUrls.contains(it.url)) }
        _uiState.update { it.copy(isLoading = false, searchResults = processed, smartSearchLabel = aiReasoning ?: "") }
    }

    // ==================== TRACKING ====================

    fun trackProduct(item: TrackedItem, explicitTargetPrice: Double? = null) {
        viewModelScope.launch {
            val finalTargetPrice = explicitTargetPrice ?: item.targetPrice
            val itemWithTarget = item.copy(targetPrice = finalTargetPrice, isAlertEnabled = true, isTracked = true)
            repository.trackProduct(itemWithTarget)
            updateIsTrackedStatus(item.url, true)
        }
    }

    fun untrackProduct(url: String) {
        viewModelScope.launch {
            repository.untrackProduct(url)
            updateIsTrackedStatus(url, false)
        }
    }

    fun selectProduct(item: TrackedItem) {
        _uiState.update { it.copy(selectedProduct = item) }
        viewModelScope.launch { repository.addToRecentlyViewed(item) }
    }

    fun showAlertSettings(item: TrackedItem) = _uiState.update { it.copy(itemForAlertSettings = item) }
    fun hideAlertSettings() = _uiState.update { it.copy(itemForAlertSettings = null) }

    fun updateAlertSettings(item: TrackedItem) {
        viewModelScope.launch {
            repository.updateTrackedProduct(item)
            if (_uiState.value.selectedProduct?.url == item.url) {
                _uiState.update { it.copy(selectedProduct = item) }
            }
        }
    }

    // ==================== ALERTS ====================

    private fun loadAlerts() {
        viewModelScope.launch {
            repository.getAllAlerts().collectLatest { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
    }

    fun markAlertsRead(productUrl: String) = viewModelScope.launch { repository.markAlertsRead(productUrl) }
    fun markAllAlertsRead() = viewModelScope.launch { repository.markAllAlertsRead() }
    fun clearAllAlerts() = viewModelScope.launch { repository.clearAllAlerts() }
    fun deleteAlert(id: Int) = viewModelScope.launch { repository.deleteAlert(id) }

    // ==================== REFRESH ====================

    fun refreshAllPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.refreshAllTracked()
            refreshExploreData()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ==================== LOADERS ====================

    fun loadProductDetails(productUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true, 
                error = null, 
                selectedProduct = null, // Clear stale product state
                aiPrediction = null, 
                similarItems = emptyList()
            ) }
            try {
                val item = repository.refreshProductPrice(productUrl) ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "Product not found") }
                    return@launch
                }

                val similarItemsTask = async { 
                    try { 
                        val raw = repository.searchProducts(item.name.take(30)).filter { it.url != productUrl }
                        val targetDims = item.dimensions
                        if (targetDims != null) {
                            // Prioritize items with matching dimensions
                            val matched = raw.filter { it.dimensions == targetDims }
                            if (matched.isNotEmpty()) matched else raw
                        } else {
                            raw
                        }
                    }
                    catch (e: Exception) { emptyList() }
                }

                repository.getPriceHistory(productUrl).collectLatest { history ->
                    val rawSimilarItems = similarItemsTask.await()
                    
                    // Robust Outlier Removal for Market Insights
                    // We filter out items that are suspiciously cheap compared to the current product (likely accessories)
                    val similarItems = if (item.currentPrice > 50000) {
                        rawSimilarItems.filter { it.currentPrice >= item.currentPrice * 0.3 }
                    } else {
                        rawSimilarItems
                    }
                    
                    val historyPoints = if (history.size >= 2) {
                        history.sortedBy { it.timestamp }.map { PricePoint(it.timestamp, it.price) }
                    } else if (similarItems.isNotEmpty()) {
                        val now = System.currentTimeMillis()
                        val day = 24 * 60 * 60 * 1000L
                        similarItems.take(7).mapIndexed { idx, sim -> 
                            PricePoint(now - (8 - idx) * day, sim.currentPrice)
                        } + PricePoint(now, item.currentPrice)
                    } else {
                        val now = System.currentTimeMillis()
                        val day = 24 * 60 * 60 * 1000L
                        listOf(
                            PricePoint(now - 2 * day, item.currentPrice * 1.05),
                            PricePoint(now - day, item.currentPrice * 0.98),
                            PricePoint(now, item.currentPrice)
                        )
                    }

                    val allPrices = historyPoints.map { it.price } + item.currentPrice
                    val minPrice = allPrices.minOrNull() ?: item.currentPrice
                    val maxPrice = allPrices.maxOrNull() ?: item.currentPrice
                    val avgPrice = allPrices.average()
                    
                    val volatility = if (maxPrice > minPrice && avgPrice > 0) (maxPrice - minPrice) / avgPrice else 0.0
                    
                    val stats = MarketStats(
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        averagePrice = avgPrice,
                        currentPrice = item.currentPrice,
                        historyPoints = historyPoints,
                        stability = if (volatility < 0.12) "STABLE" else "VOLATILE",
                        stabilityColor = if (volatility < 0.12) BeiAccentGreen else BeiPriceDropRed
                    )

                    _uiState.update { currentState -> currentState.copy(
                        isLoading = false,
                        selectedProduct = item.copy(
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            avgPrice = avgPrice,
                            priceHistory = history
                        ),
                        priceHistory = currentState.priceHistory + (productUrl to history),
                        marketStats = stats,
                        similarItems = similarItems.take(5)
                    ) }

                    generateAiPrediction(item, historyPoints.map { it.price })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Generates an AI-powered price analysis and prediction for a specific product.
     * Analyzes historical price points to recommend whether to "Buy Now" or "Wait".
     * Produces bilingual reasoning (English/Swahili) and predicts future trends.
     */
    internal fun generateAiPrediction(item: TrackedItem, history: List<Double>) {
        viewModelScope.launch {
            val historyStr = if (history.isEmpty()) "No history available" else history.joinToString(", ")
            val prompt = """
                Follow the behavior and explanation style of CamelCamelCamel price tracking for this analysis.
                
                Product: ${item.name}
                Current Price: TZS ${item.currentPrice}
                Source: ${item.source}
                Price History Data: $historyStr
                
                Analyze the following:
                1. Interpret the product and explain how its price has changed over time.
                2. Explain that the system tracks price history similar to CamelCamelCamel (highs, lows, and fluctuations).
                3. Provide insights:
                   - Current price vs historical average.
                   - Lowest price ever recorded.
                   - Highest price recorded.
                   - Whether the current price is a good buying opportunity.
                4. Alerts: Mention that users can set price drop notifications for target values, all-time lows, or when a threshold is met.
                
                Always end with a simple buying insight like: "Good time to buy", "Wait for a drop", or "Historically expensive right now".
                
                Return ONLY a JSON object with:
                - recommendation: The simple buying insight.
                - confidence: Int (0-100).
                - reasoningEnglish: Data-driven, clear CamelCamelCamel-style explanation in English.
                - reasoningSwahili: The same explanation in Swahili.
                - predictedTrend: "UP", "DOWN", or "STABLE".
            """.trimIndent()

            try {
                val response = generativeModel.generateContent(prompt)
                val rawText = response.text ?: ""
                val jsonStr = if (rawText.contains("{") && rawText.contains("}")) {
                    rawText.substringAfter("{").substringBeforeLast("}").let { "{ $it }" }
                } else {
                    rawText
                }
                val prediction = gson.fromJson(jsonStr, AiPricePrediction::class.java)
                _uiState.update { it.copy(aiPrediction = prediction) }
            } catch (e: Exception) {
                Log.e("PriceViewModel", "AI Prediction error: ${e.message}")
            }
        }
    }

    private fun loadTrackedProducts() {
        viewModelScope.launch {
            repository.getAllTrackedProducts().collectLatest { entities ->
                val items = entities.map { entity ->
                    TrackedItem(
                        url = entity.url,
                        name = entity.name,
                        currentPrice = entity.currentPrice,
                        previousPrice = entity.previousPrice,
                        source = if (entity.url.contains("zudua")) "Zudua" else "Jiji",
                        imageUrl = entity.imageUrl,
                        targetPrice = entity.targetPrice,
                        isAlertEnabled = entity.isAlertEnabled,
                        isTracked = true
                    )
                }
                
                // Calculate Watchlist Summary
                val matureItems = items.count { LifecycleEngine.analyze(it).stage == LifecycleStage.MATURE }
                val summary = if (matureItems > 0) "$matureItems items in your watchlist are in the 'Mature' stage!" else null

                _uiState.update { currentState ->
                    val trackedUrls = items.map { it.url }.toSet()
                    currentState.copy(
                        trackedProducts = items,
                        watchlistSummary = summary,
                        searchResults = currentState.searchResults.map { it.copy(isTracked = trackedUrls.contains(it.url)) },
                        popularProducts = currentState.popularProducts.map { it.copy(isTracked = trackedUrls.contains(it.url)) },
                        topPriceDrops = currentState.topPriceDrops.map { it.copy(isTracked = trackedUrls.contains(it.url)) },
                        hotDeals = currentState.hotDeals.map { it.copy(isTracked = trackedUrls.contains(it.url)) },
                        categories = currentState.categories.mapValues { entry ->
                            entry.value.map { it.copy(isTracked = trackedUrls.contains(it.url)) }
                        },
                        selectedProduct = currentState.selectedProduct?.copy(isTracked = trackedUrls.contains(currentState.selectedProduct.url))
                    )
                }
            }
        }
    }

    private fun loadRecentlyViewed() {
        viewModelScope.launch {
            repository.getRecentlyViewed().collectLatest { items ->
                _uiState.update { it.copy(recentlyViewed = items) }
            }
        }
    }

    fun fetchFeaturedDeals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFeaturedLoading = true) }
            try {
                val deals = repository.getHighlightedDeals()
                repository.updateDiscoveryCache("FEATURED", deals)
            } finally {
                _uiState.update { it.copy(isFeaturedLoading = false) }
            }
        }
    }

    fun fetchPopularProducts() {
        viewModelScope.launch {
            try {
                val popular = repository.getPopularProducts()
                repository.updateDiscoveryCache("POPULAR", popular)
            } catch (e: Exception) {}
        }
    }

    fun fetchTopPriceDrops() {
        viewModelScope.launch {
            try {
                val drops = repository.getTopPriceDrops()
                repository.updateDiscoveryCache("TOP_DROPS", drops)
            } catch (e: Exception) {}
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            val queries = mapOf(
                "Smartphones" to "Smartphone",
                "Laptops" to "Laptop",
                "TVs & Entertainment" to "Television",
                "Smartwatches" to "Smartwatch",
                "Audio & Sound" to "Speaker Headphones",
                "Cameras" to "Camera",
                "Home Appliances" to "Home Appliances",
                "Vehicles" to "Car Motorcycle"
            )
            queries.forEach { (label, query) ->
                launch {
                    try {
                        val results = repository.searchProducts(query).take(8)
                        repository.updateDiscoveryCache("CAT_$label", results)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun getCategoryDisplayName(slug: String): String {
        return when(slug) {
            "smartphones" -> "Smartphones"
            "laptops" -> "Laptops"
            "tvs" -> "TVs & Entertainment"
            "watches" -> "Smartwatches"
            "audio" -> "Audio & Sound"
            "cameras" -> "Cameras"
            "vehicles" -> "Vehicles"
            else -> slug.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}

data class AiPricePrediction(
    val recommendation: String,
    val confidence: Int,
    val reasoningSwahili: String,
    val reasoningEnglish: String,
    val predictedTrend: String
)

data class PriceUiState(
    val isLoading: Boolean = false,
    val isFeaturedLoading: Boolean = false,
    val searchResults: List<TrackedItem> = emptyList(),
    val trackedProducts: List<TrackedItem> = emptyList(),
    val recentlyViewed: List<TrackedItem> = emptyList(),
    val popularProducts: List<TrackedItem> = emptyList(),
    val topPriceDrops: List<TrackedItem> = emptyList(),
    val hotDeals: List<TrackedItem> = emptyList(),
    val categories: Map<String, List<TrackedItem>> = emptyMap(),
    val heroProduct: TrackedItem? = null,
    val priceHistory: Map<String, List<PriceData>> = emptyMap(),
    val alerts: List<com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity> = emptyList(),
    val selectedProduct: TrackedItem? = null,
    val itemForAlertSettings: TrackedItem? = null,
    val smartSearchLabel: String = "",
    val searchHistory: List<String> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),
    val error: String? = null,
    val marketStats: MarketStats? = null,
    val similarItems: List<TrackedItem> = emptyList(),
    val aiPrediction: AiPricePrediction? = null,
    val checkIntervalHours: Int = 6,
    val notificationsEnabled: Boolean = true,
    val userName: String = "Guest User",
    val userEmail: String = "guest@beitracker.tz",
    val userBio: String = "",
    val profileImageUri: String? = null,
    val joinDate: Long = 0,
    val totalSavings: Double = 0.0,
    val accountType: String = "Free",
    val preferredRegion: String = "Tanzania",
    val preferredLanguage: String = "en",
    val alertThresholdPercent: Int = 0,
    val preferredCurrency: String = "TZS",
    val themeMode: Int = 0,
    val watchlistSummary: String? = null
)
