package com.ahmanpg.beitracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ahmanpg.beitracker.ui.components.CompactProductCard
import com.ahmanpg.beitracker.ui.components.PriceAlertSettings
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

enum class BestProductsType {
    POPULAR, DROPS, HIGHLIGHTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestProductsScreen(
    type: BestProductsType = BestProductsType.DROPS,
    navController: NavController,
    viewModel: PriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    val items = when (type) {
        BestProductsType.POPULAR -> uiState.popularProducts
        BestProductsType.DROPS -> uiState.topPriceDrops
        BestProductsType.HIGHLIGHTED -> uiState.hotDeals
    }

    val title = when (type) {
        BestProductsType.POPULAR -> "Market Trends"
        BestProductsType.DROPS -> "Biggest Drops"
        BestProductsType.HIGHLIGHTED -> "Flash Deals"
    }

    val description = when (type) {
        BestProductsType.POPULAR -> "Trending products across Jiji and Zudua marketplace right now."
        BestProductsType.DROPS -> "The most significant price reductions detected in the last 24 hours."
        BestProductsType.HIGHLIGHTED -> "Carefully selected offers with exceptional value and limited availability."
    }

    val isLoading = uiState.isLoading || uiState.isFeaturedLoading

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BeiNavyDark,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                "UPDATED REAL-TIME",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                when(type) {
                                    BestProductsType.POPULAR -> viewModel.fetchPopularProducts()
                                    BestProductsType.DROPS -> viewModel.fetchTopPriceDrops()
                                    BestProductsType.HIGHLIGHTED -> viewModel.fetchFeaturedDeals()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (isLoading && items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PriceDropChartLoading(size = 48.dp)
                    }
                } else if (items.isEmpty()) {
                    EmptyBestProductsState(type, viewModel)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Text(
                                    description,
                                    modifier = Modifier.padding(20.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        
                        items(items, key = { it.url }) { item ->
                            CompactProductCard(
                                item = item,
                                numberFormat = numberFormat,
                                onClick = { 
                                    viewModel.selectProduct(item)
                                    navController.navigate("product_detail?url=${URLEncoder.encode(item.url, "UTF-8")}")
                                },
                                onTrackClick = { 
                                    if (item.isTracked) viewModel.untrackProduct(item.url)
                                    else viewModel.showAlertSettings(item)
                                },
                                isTracked = item.isTracked
                            )
                        }
                        
                        item {
                            Spacer(Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        // Bell Icon Dialog Integration
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
fun EmptyBestProductsState(type: BestProductsType, viewModel: PriceViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.03f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("📡", fontSize = 42.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Syncing Market",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "We are currently updating our market data feeds. Please try refreshing in a few moments.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { 
                when(type) {
                    BestProductsType.POPULAR -> viewModel.fetchPopularProducts()
                    BestProductsType.DROPS -> viewModel.fetchTopPriceDrops()
                    BestProductsType.HIGHLIGHTED -> viewModel.fetchFeaturedDeals()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BeiNavyDark),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("RETRY SYNC", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
        }
    }
}
