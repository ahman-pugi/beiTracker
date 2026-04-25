package com.ahmanpg.beitracker.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.components.FiredAlertCard
import com.ahmanpg.beitracker.ui.components.HelpIcon
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.PriceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggersScreen(
    viewModel: PriceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAlertClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    
    val activeTriggers = uiState.trackedProducts.filter { it.isAlertEnabled }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("MONITORING", "HISTORY")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Decorative background
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BeiAccentGreen.copy(alpha = 0.08f), Color.Transparent),
                    radius = size.width
                ),
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Smart Triggers",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            HelpIcon(
                                title = stringResource(R.string.help_smart_tracking_title),
                                description = stringResource(R.string.help_smart_tracking_desc),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                    },
                    actions = {
                        if (selectedTab == 1 && uiState.alerts.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.clearAllAlerts() },
                                modifier = Modifier.padding(end = 16.dp).size(40.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Apple-style Fluid Segmented Control
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        tabs.forEachIndexed { index, title ->
                            val selected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                                    .clickable { selectedTab = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_content"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> ActiveMonitoringTab(activeTriggers, onAlertClick)
                            1 -> NotificationLogTab(uiState.alerts, numberFormat, dateFormat, onAlertClick, onMarkRead = { viewModel.markAlertsRead(it) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveMonitoringTab(activeTriggers: List<TrackedItem>, onAlertClick: (String) -> Unit) {
    if (activeTriggers.isEmpty()) {
        EmptyTriggersState(
            icon = Icons.Default.Timeline,
            title = "Zero Active Triggers",
            subtitle = "Tracked items with alerts enabled will appear here for real-time monitoring."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(activeTriggers) { product ->
                ActiveTriggerCard(product) { onAlertClick(product.url) }
            }
        }
    }
}

@Composable
fun NotificationLogTab(
    alerts: List<com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity>,
    numberFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onAlertClick: (String) -> Unit,
    onMarkRead: (String) -> Unit
) {
    if (alerts.isEmpty()) {
        EmptyTriggersState(
            icon = Icons.Default.History,
            title = "History is Clean",
            subtitle = "When price drops match your criteria, notifications will be logged here."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(alerts) { alert ->
                FiredAlertCard(
                    alert = alert,
                    numberFormat = numberFormat,
                    dateFormat = dateFormat
                ) {
                    onMarkRead(alert.productUrl)
                    onAlertClick(alert.productUrl)
                }
            }
        }
    }
}

@Composable
fun EmptyTriggersState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
            shape = CircleShape,
            modifier = Modifier.size(100.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), modifier = Modifier.size(44.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            title, 
            fontWeight = FontWeight.Black, 
            fontSize = 22.sp, 
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            subtitle, 
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ActiveTriggerCard(product: TrackedItem, onClick: () -> Unit) {
    val targetPriceText = if (product.targetPrice != null && product.targetPrice > 0) {
        String.format(Locale.US, "%,.0f", product.targetPrice) + "/="
    } else {
        "ANY DROP"
    }

    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    color = BeiAccentGreen.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {}
                Icon(
                    Icons.Default.NotificationsActive, 
                    contentDescription = null, 
                    tint = BeiAccentGreen, 
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    maxLines = 1, 
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "WATCHING: ",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        targetPriceText,
                        color = BeiAccentGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    String.format(Locale.US, "%,.0f", product.currentPrice) + "/=",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    product.source.uppercase(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
