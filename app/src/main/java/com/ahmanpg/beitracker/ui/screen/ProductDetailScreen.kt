package com.ahmanpg.beitracker.ui.screen

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.*
import com.ahmanpg.beitracker.ui.screen.detail.*
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.util.BuyScoreEngine
import com.ahmanpg.beitracker.viewmodel.MarketStats
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productUrl: String,
    navController: NavController,
    viewModel: PriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    
    val scrollState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 100 }
    }
    
    val selectedFilterState = remember { mutableStateOf("7D") }
    val timeFilters = listOf("7D", "30D", "90D", "ALL")

    LaunchedEffect(productUrl) {
        viewModel.loadProductDetails(productUrl)
    }

    val item = uiState.selectedProduct
    
    if (uiState.error == "DEEP_SCRAPE_NEEDED") {
        DeepScraperView(productUrl)
        return
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            PriceDropChartLoading(size = 48.dp)
        }
        return
    }

    if (item == null) {
        ProductNotFoundView(uiState.error, viewModel, productUrl, navController)
        return
    }

    val stats = uiState.marketStats
    val scoreResult = remember(item) { BuyScoreEngine.calculateScore(item) }
    val isInsufficientData = scoreResult.confidenceScore < 40

    val buyStatus = when {
        isInsufficientData -> "COLLECTING DATA..."
        scoreResult.factors.any { it.description == "Unique product found" } -> "UNIQUE FIND"
        scoreResult.recommendation == "BUY" && scoreResult.score >= 90 -> "BEST PRICE"
        scoreResult.recommendation == "BUY" -> "GOOD DEAL"
        scoreResult.recommendation == "WAIT" -> "WAITING"
        else -> "PREMIUM LISTING"
    }
    
    val statusColor = when {
        isInsufficientData -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        buyStatus == "UNIQUE FIND" -> BeiAccentGreen
        scoreResult.recommendation == "BUY" -> BeiAccentGreen
        scoreResult.recommendation == "WAIT" -> Color(0xFFF59E0B)
        else -> Color(0xFFF59E0B)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                DetailBottomBar(item, viewModel, navController)
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(statusColor.copy(alpha = 0.1f), Color.Transparent),
                            radius = size.width
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.2f)
                    )
                }

                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    contentPadding = PaddingValues(bottom = 40.dp, end = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        DetailHeader(item, navController, context, numberFormat)
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "CURRENT MARKET PRICE", 
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), 
                                    fontSize = 11.sp, 
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                                item.manufactureYear?.let { year ->
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "$year MODEL",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${numberFormat.format(item.currentPrice)}/=",
                                    color = BeiAccentGreen,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                )
                                BuyNowBadge(buyStatus, statusColor)
                            }
                            PriceTrendIndicator(item, numberFormat)
                        }
                    }

                    item {
                        ProductImageSlideshow(images = item.images.ifEmpty { item.imageUrl?.let { listOf(it) } ?: emptyList() })
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            BuyScoreDashboard(item)
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            StoreComparisonCard(
                                currentItem = item,
                                similarItems = uiState.similarItems,
                                onNavigateToProduct = { targetUrl ->
                                    val encodedUrl = Uri.encode(targetUrl)
                                    navController.navigate("product_detail?url=$encodedUrl")
                                },
                                onBuyNow = { targetUrl ->
                                    val encodedUrl = Uri.encode(targetUrl)
                                    navController.navigate("jiji?url=$encodedUrl")
                                }
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            LifecycleVisualizer(item)
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "PRICE TREND",
                                    style = MaterialTheme.typography.labelMedium, 
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                                HelpIcon(
                                    title = stringResource(R.string.help_price_trend_title),
                                    description = stringResource(R.string.help_price_trend_desc),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                ) {
                                    timeFilters.forEach { filter ->
                                        TimeFilterTab(
                                            label = filter,
                                            isSelected = selectedFilterState.value == filter,
                                            onClick = { selectedFilterState.value = filter }
                                        )
                                    }
                                }
                            }
                            
                            val now = remember { System.currentTimeMillis() }
                            val (minX, maxX) = remember(selectedFilterState.value) {
                                when (selectedFilterState.value) {
                                    "7D" -> (now - (7 * 24 * 60 * 60 * 1000L)) to now
                                    "30D" -> (now - (30 * 24 * 60 * 60 * 1000L)) to now
                                    "90D" -> (now - (90 * 24 * 60 * 60 * 1000L)) to now
                                    else -> null to null
                                }
                            }

                            val filteredHistoryPoints = remember(minX, stats?.historyPoints) {
                                val points = stats?.historyPoints ?: emptyList()
                                if (points.isEmpty()) return@remember emptyList()
                                
                                val cutoffValue = minX ?: 0L
                                val filtered = points.filter { it.timestamp >= cutoffValue }
                                // Ensure we have at least 2 points for the sparkline, fallback to all if needed
                                if (filtered.size < 2) points else filtered
                            }

                            val currentMin = remember(filteredHistoryPoints) { filteredHistoryPoints.minOfOrNull { it.price } ?: item.currentPrice }
                            val currentMax = remember(filteredHistoryPoints) { filteredHistoryPoints.maxOfOrNull { it.price } ?: item.currentPrice }

                            Spacer(modifier = Modifier.height(16.dp))
                            if (stats != null) {
                                ChartSurface(
                                    stats = stats.copy(
                                        historyPoints = filteredHistoryPoints,
                                        minPrice = currentMin,
                                        maxPrice = currentMax
                                    ),
                                    item = item,
                                    chartColor = statusColor,
                                    minX = minX,
                                    maxX = maxX
                                )
                            } else {
                                PriceDropChartLoading(size = 32.dp)
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "MARKET INSIGHTS", 
                                    style = MaterialTheme.typography.labelMedium, 
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                                    fontWeight = FontWeight.Black, 
                                    letterSpacing = 1.2.sp
                                )
                                HelpIcon(
                                    title = stringResource(R.string.help_market_insights_title),
                                    description = stringResource(R.string.help_market_insights_desc),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            stats?.let { 
                                InsightsGrid(
                                    stats = it, 
                                    statusColor = statusColor, 
                                    similarItems = uiState.similarItems,
                                    onItemClick = { similarItem ->
                                        viewModel.selectProduct(similarItem)
                                        navController.navigate("product_detail?url=${Uri.encode(similarItem.url)}")
                                    }
                                )
                            } ?: Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                PriceDropChartLoading(size = 32.dp)
                            }
                        }
                    }

                    item {
                        SellerInsightCard(item)
                    }

                    val productAlerts = uiState.alerts.filter { it.productUrl == item.url }
                    if (productAlerts.isNotEmpty()) {
                        item {
                            Text(
                                "NOTIFICATION LOG", 
                                style = MaterialTheme.typography.labelMedium, 
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp)
                            )
                        }
                        items(productAlerts) { alert ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                FiredAlertCard(alert, numberFormat, dateFormat)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isScrolled,
                    modifier = Modifier.align(Alignment.CenterEnd),
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(32.dp)
                                .offset(x = 26.dp)
                        ) {
                            drawLine(
                                color = Color.Green.copy(alpha = 0.1f),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = 1.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .offset(x = 26.dp)
                                .graphicsLayer { rotationZ = -90f },
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = CircleShape,
                            border = BorderStroke(
                                width = 0.8.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(statusColor.copy(alpha = 0.2f), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(statusColor, CircleShape)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "${numberFormat.format(item.currentPrice)}/=",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.6.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        uiState.itemForAlertSettings?.let { alertItem ->
            PriceAlertSettings(
                item = alertItem,
                numberFormat = numberFormat,
                onBack = { viewModel.hideAlertSettings() },
                onConfirm = { updatedItem ->
                    if (updatedItem.isTracked) viewModel.updateAlertSettings(updatedItem)
                    else viewModel.trackProduct(updatedItem)
                    viewModel.hideAlertSettings()
                }
            )
        }
    }
}

@Composable
fun ChartSurface(stats: MarketStats, item: TrackedItem, chartColor: Color, minX: Long? = null, maxX: Long? = null) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().height(240.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            SparklineChart(
                dataPoints = stats.historyPoints,
                color = chartColor,
                modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),
                minYValue = stats.minPrice,
                maxYValue = stats.maxPrice,
                minXValue = minX,
                maxXValue = maxX,
                highlightPrice = item.currentPrice
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MARKET ENTRY", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
                Text("CURRENT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SellerInsightCard(item: TrackedItem) {
    val rating = item.rating ?: 4.5
    
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "VERIFIED MERCHANT", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = BeiAccentGreen, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
                HelpIcon(
                    title = stringResource(R.string.help_verified_merchant_title),
                    description = stringResource(R.string.help_verified_merchant_desc),
                    modifier = Modifier.padding(start = 4.dp),
                    tint = BeiAccentGreen.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.sellerName ?: "Jiji Seller", 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 18.sp, 
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$rating Rating", 
                            fontSize = 13.sp, 
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeFilterTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                label,
                color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProductImageSlideshow(images: List<String>) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📦", fontSize = 64.sp)
        }
        return
    }

    // Capture the size as a primitive and wrap the lambda in remember.
    // This is a known workaround for R8/Compose lambda desugaring issues
    // that cause "Unable to find method $r8$lambda" crashes.
    val imageCount = images.size
    val pagerState = rememberPagerState(pageCount = remember(imageCount) { { imageCount } })
    
    Box(modifier = Modifier.fillMaxWidth().height(320.dp).padding(horizontal = 24.dp)) {
        HorizontalPager(
            state = pagerState, 
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)).background(Color.Transparent)
        ) { page ->
            // Safely access the image at the current page index
            val imageUrl = images.getOrNull(page) ?: ""
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl, 
                    contentDescription = "Product Image", 
                    modifier = Modifier.fillMaxSize().padding(24.dp), 
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        if (imageCount > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                color = Color.Black.copy(alpha = 0.3f),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(imageCount) { iteration ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == iteration) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}
