package com.ahmanpg.beitracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.TrendProductCard
import com.ahmanpg.beitracker.ui.components.HelpIcon
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiPriceDropRed
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketTrendsScreen(
    navController: NavController,
    viewModel: PriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    
    // Using cached discovery items for trends
    val trendingUp = uiState.popularProducts.filter { (it.changePercent ?: 0.0) > 0 }.take(10)
    val trendingDown = uiState.topPriceDrops.take(10)
    val volatile = uiState.popularProducts.filter { Math.abs(it.changePercent ?: 0.0) > 5 }.take(10)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Trends", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.popularProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PriceDropChartLoading(size = 48.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    TrendSection(
                        title = "🔥 Trending Down",
                        subtitle = "Best deals right now",
                        items = trendingDown,
                        numberFormat = numberFormat,
                        navController = navController
                    )
                }

                item {
                    TrendSection(
                        title = "📈 Trending Up",
                        subtitle = "Prices are rising",
                        items = trendingUp,
                        numberFormat = numberFormat,
                        navController = navController,
                        accentColor = BeiPriceDropRed
                    )
                }

                item {
                    MarketSummaryCard()
                }

                item {
                    TrendSection(
                        title = "💣 High Volatility",
                        subtitle = "Unstable prices - watch closely",
                        items = volatile,
                        numberFormat = numberFormat,
                        navController = navController,
                        accentColor = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun TrendSection(
    title: String,
    subtitle: String,
    items: List<TrackedItem>,
    numberFormat: NumberFormat,
    navController: NavController,
    accentColor: Color = BeiAccentGreen
) {
    Column {
        Column(Modifier.padding(horizontal = 24.dp)) {
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 24.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Analyzing market data...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    Box(modifier = Modifier.width(180.dp)) {
                        TrendProductCard(
                            item = item,
                            numberFormat = numberFormat,
                            onClick = {
                                val encodedUrl = URLEncoder.encode(item.url, StandardCharsets.UTF_8.toString())
                                navController.navigate("product_detail?url=$encodedUrl")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketSummaryCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = BeiAccentGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("MARKET PULSE", fontWeight = FontWeight.Black, letterSpacing = 1.2.sp, fontSize = 12.sp, color = BeiAccentGreen)
                Spacer(Modifier.weight(1f))
                HelpIcon(
                    title = stringResource(R.string.help_market_pulse_title),
                    description = stringResource(R.string.help_market_pulse_desc)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Electronic prices in Kariakoo are showing signs of stabilization after last week\'s volatility.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}
