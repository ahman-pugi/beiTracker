package com.ahmanpg.beitracker.ui.screen

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.AppLogo
import com.ahmanpg.beitracker.ui.components.HelpIcon
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.components.SparklineChart
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Category model & Intelligence Engine
// ---------------------------------------------------------------------------
data class ProductCategory(
    val label: String,
    val emoji: String,
    val keywords: List<String>
)

val PRODUCT_CATEGORIES = listOf(
    ProductCategory("Smartphones", "📱", listOf("iphone", "samsung galaxy", "pixel", "tecno spark", "infinix note", "redmi note", "realme", "oppo", "vivo", "nokia", "motorola", "oneplus", "huawei", "smartphone")),
    ProductCategory("Laptops", "💻", listOf("laptop", "notebook", "macbook", "thinkpad", "chromebook", "hp elitebook", "dell latitude", "lenovo ideapad", "asus zenbook", "acer aspire", "surface pro")),
    ProductCategory("Audio", "🎧", listOf("earbuds", "earphones", "headphones", "airpods", "speaker", "soundbar", "woofer", "home theater")),
    ProductCategory("Home", "🏠", listOf("fridge", "microwave", "cooker", "ac", "fan", "kettle", "blender", "iron", "washing machine", "refrigerator")),
    ProductCategory("Gaming", "🎮", listOf("playstation", "ps4", "ps5", "xbox", "nintendo switch", "console")),
)

fun isStrictProductSearch(query: String): Boolean {
    val q = query.lowercase()
    return listOf("iphone", "samsung", "laptop", "ps5", "tv")
        .any { q.contains(it) }
}

fun groupResultsByCategory(items: List<TrackedItem>, query: String): LinkedHashMap<ProductCategory, List<TrackedItem>> {
    val grouped = LinkedHashMap<ProductCategory, MutableList<TrackedItem>>()
    val others = mutableListOf<TrackedItem>()

    // 🚫 Words that indicate accessories (NOT main products)
    val accessoryKeywords = listOf(
        "case", "cover", "charger", "cable", "adapter", "earpiece",
        "screen protector", "battery", "replacement", "strap",
        "keyboard", "mouse", "controller", "pad", "stand"
    )

    val isStrict = isStrictProductSearch(query)
    val queryLower = query.lowercase()

    for (item in items) {
        val nameLower = item.name.lowercase()

        // 🚨 Step 1: Detect accessories
        val detectedAccessory = accessoryKeywords.firstOrNull { nameLower.contains(it) }
        val isAccessory = detectedAccessory != null

        // 🔥 Aggressive Filtering: If searching for product but found accessory not mentioned in query
        if (isStrict && detectedAccessory != null && !queryLower.contains(detectedAccessory)) {
            continue 
        }

        // 🚨 Step 2: Find category match
        val matched = PRODUCT_CATEGORIES.firstOrNull { cat ->
            cat.keywords.any { keyword ->
                // 🔥 stricter match (whole word or strong match)
                Regex("\\b$keyword\\b").containsMatchIn(nameLower)
            }
        }

        when {
            // ❌ If accessory → always "Other"
            isAccessory -> {
                others.add(item)
            }

            // ✅ Valid category match
            matched != null -> {
                grouped.getOrPut(matched) { mutableListOf() }.add(item)
            }

            // 📦 Fallback
            else -> {
                others.add(item)
            }
        }
    }

    if (others.isNotEmpty()) {
        grouped[ProductCategory("Other", "📦", emptyList())] = others
    }

    @Suppress("UNCHECKED_CAST")
    return grouped as LinkedHashMap<ProductCategory, List<TrackedItem>>
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: PriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var query by remember { mutableStateOf("") }
    var isNavigating by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        isNavigating = false
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    val isAiSearch = remember(query) {
        val lq = query.lowercase().trim()
        val conversationalWords = listOf("cheap", "best", "under", "below", "over", "above", "lowest", "premium", "max", "min", "than", "affordable", "expensive", "top", "looking", "find", "show", "me", "want", "i", "need")
        lq.split(" ").size > 2 || conversationalWords.any { lq.contains(it) } || Regex("\\d+\\s*(k|m)").containsMatchIn(lq)
    }

    LaunchedEffect(query) {
        if (query.length > 2) {
            delay(600)
            viewModel.searchProducts(query)
        }
    }

    val groupedResults = remember(uiState.searchResults, query) {
        groupResultsByCategory(uiState.searchResults, query)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BeiAccentGreen.copy(alpha = 0.08f), Color.Transparent),
                    radius = size.width
                ),
                center = Offset(size.width * 0.2f, size.height * 0.1f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .statusBarsPadding()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                navController.navigateUp()
                            },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.ai_market_intelligence),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        HelpIcon(
                            title = stringResource(R.string.help_ai_market_intelligence_title),
                            description = stringResource(R.string.help_ai_market_intelligence_desc)
                        )
                        Spacer(Modifier.weight(1f))
                        AppLogo(size = 36.dp, cornerRadius = 10.dp, fontSize = 20)
                    }

                    Spacer(Modifier.height(24.dp))

                    TextField(
                        value = query,
                        onValueChange = {
                            query = it
                            viewModel.updateSearchSuggestions(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        placeholder = {
                            Text(
                                stringResource(R.string.search_placeholder),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (isAiSearch) Icons.Default.AutoAwesome else Icons.Default.Search,
                                null,
                                tint = if (isAiSearch) BeiAccentGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = if (isAiSearch && uiState.isLoading) Modifier.graphicsLayer {
                                    val scale = 1f + 0.2f * kotlin.math.sin(System.currentTimeMillis() / 200f)
                                    scaleX = scale
                                    scaleY = scale
                                } else Modifier
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {
                                    query = ""
                                    viewModel.clearSearch()
                                    focusRequester.requestFocus()
                                }) {
                                    Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = BeiAccentGreen,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (query.length > 2) {
                                viewModel.searchProducts(query)
                                isFocused = false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        })
                    )

                    AnimatedVisibility(visible = uiState.smartSearchLabel.isNotEmpty() && !isFocused) {
                        Surface(
                            color = BeiAccentGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(top = 16.dp),
                            border = BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.2f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = BeiAccentGreen, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.smartSearchLabel, color = BeiAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AnimatedVisibility(visible = groupedResults.isNotEmpty() && !isFocused) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            CategoryJumpBar(categories = groupedResults.keys.toList())
                        }
                    }
                }
            }
        ) { padding ->
            Surface(
                color = Color.Transparent,
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                Crossfade(
                    targetState = isFocused && (query.isNotEmpty() || uiState.searchHistory.isNotEmpty()),
                    label = "SearchState"
                ) { showSuggestions ->
                    if (showSuggestions) {
                        SearchSuggestionsList(
                            suggestions = uiState.searchSuggestions,
                            history = if (query.isEmpty()) uiState.searchHistory else emptyList(),
                            onSuggestionClick = {
                                query = it
                                isFocused = false
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                viewModel.searchProducts(it)
                            },
                            onClearHistory = { viewModel.clearSearchHistory() }
                        )
                    } else {
                        Box(Modifier.fillMaxSize()) {
                            when {
                                uiState.isLoading -> LoadingState(isAi = isAiSearch)
                                query.isEmpty() -> EmptySearchResults(isInitial = true)
                                uiState.searchResults.isEmpty() -> EmptySearchResults(isInitial = false)
                                else -> GroupedResultsList(
                                    groupedResults = groupedResults,
                                    numberFormat = numberFormat,
                                    onTrackClick = { item ->
                                        if (item.isTracked) viewModel.untrackProduct(item.url)
                                        else viewModel.trackProduct(item)
                                    },
                                    onItemClick = { item ->
                                        if (isNavigating) return@GroupedResultsList
                                        isNavigating = true
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.selectProduct(item)
                                        navController.navigate("product_detail?url=${Uri.encode(item.url)}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryJumpBar(categories: List<ProductCategory>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        items(categories) { cat ->
            Surface(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(cat.emoji, fontSize = 13.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(cat.label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GroupedResultsList(
    groupedResults: LinkedHashMap<ProductCategory, List<TrackedItem>>,
    numberFormat: NumberFormat,
    onTrackClick: (TrackedItem) -> Unit,
    onItemClick: (TrackedItem) -> Unit
) {
    val totalCount = groupedResults.values.sumOf { it.size }

    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$totalCount intelligence signals found",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
                    modifier = Modifier.clickable { }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sort", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        groupedResults.forEach { (category, items) ->
            // Per-category normalization for smarter market visualization
            val categoryMax = (items.map { it.currentPrice }.maxOrNull() ?: 0.0) * 1.1
            
            item(key = "header_${category.label}") {
                CategoryGroupHeader(category = category, count = items.size)
            }
            items(items, key = { it.url }) { item ->
                SearchProductCard(
                    item = item,
                    numberFormat = numberFormat,
                    maxValue = categoryMax,
                    onTrackClick = { onTrackClick(item) },
                    onClick = { onItemClick(item) }
                )
            }
            item(key = "spacer_${category.label}") {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryGroupHeader(category: ProductCategory, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = BeiAccentGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.2f))
        ) {
            Text(category.emoji, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(category.label, fontWeight = FontWeight.Black, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground, letterSpacing = (-0.3).sp)
            Text("$count ${if (count == 1) "item" else "items"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(brush = Brush.horizontalGradient(listOf(BeiAccentGreen.copy(alpha = 0.5f), Color.Transparent)), shape = RoundedCornerShape(1.dp))
        )
    }
}

@Composable
fun LoadingState(isAi: Boolean) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAi) {
            Box(contentAlignment = Alignment.Center) {
                PriceDropChartLoading(size = 80.dp)
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = BeiAccentGreen,
                    modifier = Modifier.size(32.dp).graphicsLayer {
                        val scale = 1f + 0.1f * kotlin.math.sin(System.currentTimeMillis() / 300f)
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.ai_reasoning), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.ai_synthesizing), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        } else {
            PriceDropChartLoading(size = 48.dp)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.scanning_markets), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
        }
    }
}

@Composable
fun EmptySearchResults(isInitial: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(if (isInitial) "🧠" else "📡", fontSize = 42.sp)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            if (isInitial) stringResource(R.string.ai_powered_search) else "End of Signal",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (isInitial) stringResource(R.string.ai_search_desc)
            else stringResource(R.string.no_matches),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        if (isInitial) {
            Spacer(Modifier.height(40.dp))
            Surface(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TipsAndUpdates, null, tint = BeiAccentGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.try_asking), color = BeiAccentGreen, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    val suggestions = listOf(
                        "I want a cheap iPhone 13 under 800k",
                        "Looking for premium Core i7 laptops",
                        "Show me the best deals on 4K TVs",
                        "Find affordable Samsung phones with 5G"
                    )
                    suggestions.forEach { s ->
                        Text(
                            "\"$s\"",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSuggestionsList(
    suggestions: List<String>,
    history: List<String>,
    onSuggestionClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (suggestions.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.suggestions),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
            items(suggestions) { s ->
                SuggestionItem(s, Icons.Default.Search) { onSuggestionClick(s) }
            }
        }

        if (history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.history),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        stringResource(R.string.clear),
                        modifier = Modifier.clickable { onClearHistory() },
                        style = MaterialTheme.typography.labelMedium,
                        color = BeiAccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(history) { h ->
                SuggestionItem(h, Icons.Default.History) { onSuggestionClick(h) }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(text, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.ArrowOutward, null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SearchProductCard(
    item: TrackedItem,
    numberFormat: NumberFormat,
    maxValue: Double? = null,
    onTrackClick: () -> Unit,
    onClick: () -> Unit
) {
    val isTracked = item.isTracked
    val trackBgColor by animateColorAsState(
        targetValue = if (isTracked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(300), label = "TrackBg"
    )
    val trackContentColor by animateColorAsState(
        targetValue = if (isTracked) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
        animationSpec = tween(300), label = "TrackContent"
    )

    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUrl != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(10.dp)
                        )
                    }
                    if (item.flashSaleDropPercent >= 5) {
                        Surface(
                            color = BeiPriceDropRed,
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                "DROP",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    Text(
                        item.source,
                        fontSize = 12.sp,
                        color = if (item.source == "Jiji") BeiJijiText else BeiAccentGreen,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    val drop = item.changePercent ?: 0.0
                    if (drop != 0.0) {
                        val isPriceDrop = drop < 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${numberFormat.format(item.currentPrice)}/=",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = BeiAccentGreen
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = if (isPriceDrop) BeiAccentGreen.copy(alpha = 0.15f) else BeiPriceDropRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "${if (isPriceDrop) "▼" else "▲"} ${String.format("%.0f", abs(drop))}%",
                                    color = if (isPriceDrop) BeiAccentGreen else BeiPriceDropRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            "${numberFormat.format(item.currentPrice)}/=",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = BeiAccentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onTrackClick,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = trackBgColor,
                        contentColor = trackContentColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    AnimatedContent(
                        targetState = isTracked,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "TrackIcon"
                    ) { tracked ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (tracked) Icons.Default.Check else Icons.Default.Notifications,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (tracked) stringResource(R.string.tracking) else stringResource(R.string.track_price), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    onClick = onClick,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(46.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowOutward, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
