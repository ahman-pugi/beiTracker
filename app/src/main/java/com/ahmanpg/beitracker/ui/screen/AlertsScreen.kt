package com.ahmanpg.beitracker.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.FiredAlertCard
import com.ahmanpg.beitracker.ui.components.HelpIcon
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(
    viewModel: PriceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAlertClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    
    val activeTriggers = uiState.trackedProducts.filter { it.isAlertEnabled }
    val unreadCount = uiState.alerts.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeiNavyDark)
    ) {
        // Glassy Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Market Activity",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    HelpIcon(
                        title = stringResource(R.string.help_market_activity_title),
                        description = stringResource(R.string.help_market_activity_desc),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        "Real-time price intelligence",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (unreadCount > 0) {
                    Surface(
                        onClick = { viewModel.markAllAlertsRead() },
                        color = BeiAccentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BeiAccentGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Clear All",
                            color = BeiAccentGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (activeTriggers.isEmpty() && uiState.alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(48.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Silent Market", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "We'll ping you as soon as we detect a price drop on your tracked items.",
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary/Status Card
                if (unreadCount > 0) {
                    item {
                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = BeiAccentGreen,
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("New Opportunities", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                    Text("Detected $unreadCount price drops since last check", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 1. ACTIVE TRIGGERS SECTION
                if (activeTriggers.isNotEmpty()) {
                    item {
                        Text(
                            "WATCHING",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }
                    
                    items(activeTriggers) { product ->
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth().clickable { onAlertClick(product.url) }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔔", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        "Target: ${numberFormat.format(product.targetPrice ?: 0.0)}/=",
                                        color = BeiAccentGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }

                // 2. RECENT DROPS SECTION
                if (uiState.alerts.isNotEmpty()) {
                    item {
                        Text(
                            "HISTORY",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 16.dp)
                        )
                    }

                    items(uiState.alerts) { alert ->
                        FiredAlertCard(alert, numberFormat, dateFormat) {
                            onAlertClick(alert.productUrl)
                        }
                    }
                }
            }
        }
    }
}
