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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.ui.components.*
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import androidx.compose.ui.res.stringResource
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String,
    navController: NavController,
    viewModel: PriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(categoryName) {
        viewModel.searchProducts(categoryName)
    }

    val items = uiState.searchResults
    
    val maxPriceInResults = remember(items) {
        (items.map { it.currentPrice }.maxOrNull() ?: 1_000_000.0) * 1.1
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                categoryName,
                                color = onBackgroundColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            if (items.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "FOUND ${items.size} DEALS",
                                        color = onBackgroundColor.copy(alpha = 0.4f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    HelpIcon(
                                        title = stringResource(R.string.help_ai_market_intelligence_title),
                                        description = stringResource(R.string.help_ai_market_intelligence_desc),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(onBackgroundColor.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, onBackgroundColor.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = onBackgroundColor, modifier = Modifier.size(18.dp))
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { /* Filter */ },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(onBackgroundColor.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, onBackgroundColor.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = onBackgroundColor, modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (uiState.isLoading && items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PriceDropChartLoading(size = 48.dp)
                    }
                } else if (items.isEmpty()) {
                    EmptyCategoryState(categoryName, viewModel)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items, key = { it.url }) { item ->
                            CompactProductCard(
                                item = item,
                                numberFormat = numberFormat,
                                maxValue = maxPriceInResults,
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
fun EmptyCategoryState(categoryName: String, viewModel: PriceViewModel) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = onBackgroundColor.copy(alpha = 0.03f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, onBackgroundColor.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Search, contentDescription = null, tint = onBackgroundColor.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Market scan complete", 
            fontWeight = FontWeight.Black, 
            fontSize = 22.sp, 
            color = onBackgroundColor,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "We couldn't find any listings for '$categoryName' in the current market cycle. Try adjusting your keywords.", 
            color = onBackgroundColor.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { viewModel.searchProducts(categoryName) },
            colors = ButtonDefaults.buttonColors(containerColor = BeiAccentGreen),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Retry Scan", fontWeight = FontWeight.Black)
        }
    }
}
