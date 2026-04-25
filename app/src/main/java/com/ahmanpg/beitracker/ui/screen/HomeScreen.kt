package com.ahmanpg.beitracker.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.ahmanpg.beitracker.R
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.local.entity.PriceAlertEntity
import com.ahmanpg.beitracker.ui.components.*
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.util.BuyScoreEngine
import com.ahmanpg.beitracker.viewmodel.HomeUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    uiState: HomeUiState,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onDeleteWatchlistItem: (TrackedItem) -> Unit,
    onMarkAllAlertsRead: () -> Unit = {},
    onAlertClick: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val unreadAlertCount = remember(uiState.alerts) {
        uiState.alerts.count { !it.isRead }
    }

    val tabs = listOf(
        stringResource(R.string.your_watchlist),
        stringResource(R.string.triggers),
        stringResource(R.string.explore)
    )
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    val contentAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, delayMillis = 300),
        label = "contentAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "contentScale"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .graphicsLayer {
                alpha = contentAlpha
                scaleX = contentScale
                scaleY = contentScale
            },
        topBar = {
            Column {
                HeroSection(
                    userName = uiState.userName,
                    summary = uiState.watchlistSummary,
                    onSearchClick = { navController.navigate("search") },
                    onJijiClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://jiji.co.tz".toUri())
                        context.startActivity(intent)
                    },
                    onMenuClick = onOpenDrawer
                )

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = BeiAccentGreen,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val selected = selectedTab == index
                            Tab(
                                selected = selected,
                                onClick = { selectedTab = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            title,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                        if (index == 1 && unreadAlertCount > 0) {
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                color = BeiAccentGreen,
                                                shape = CircleShape,
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (unreadAlertCount > 9) "9+" else "$unreadAlertCount",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(BeiAccentGreen.copy(alpha = 0.08f), Color.Transparent),
                            radius = size.width
                        ),
                        center = Offset(size.width * 0.8f, size.height * 0.2f)
                    )
                }

                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "TabContent"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> WatchingTab(uiState.watchlist, uiState.featuredPriceDrops, onDeleteWatchlistItem, navController, numberFormat)
                            1 -> AlertsTab(uiState.alerts, onMarkAllAlertsRead, onAlertClick)
                            2 -> ExploreTab(uiState, navController, numberFormat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    userName: String,
    summary: String?,
    onSearchClick: () -> Unit,
    onJijiClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = stringResource(R.string.navigation_drawer_open),
                            tint = onBackgroundColor
                        )
                    }
                    Column {
                        Text(
                            "Habari, ${userName.split(" ").firstOrNull() ?: "User"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = onBackgroundColor
                        )
                        if (summary != null) {
                            Text(
                                summary,
                                fontSize = 11.sp,
                                color = BeiAccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    color = onBackgroundColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, onBackgroundColor.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onSearchClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = onBackgroundColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = onBackgroundColor.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, onBackgroundColor.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        shadowElevation = 20f
                        alpha = 0.98f
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(onBackgroundColor.copy(alpha = 0.05f), Color.Transparent),
                                        center = Offset(0f, 0f),
                                        radius = size.maxDimension
                                    ),
                                    blendMode = BlendMode.Overlay
                                )
                            }
                        }
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.smart_tracking),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = onBackgroundColor,
                                letterSpacing = (-0.5).sp
                            )
                            HelpIcon(
                                title = stringResource(R.string.help_smart_tracking_title),
                                description = stringResource(R.string.help_smart_tracking_desc),
                                modifier = Modifier.padding(start = 8.dp),
                                tint = onBackgroundColor.copy(alpha = 0.4f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.hero_subtitle),
                                fontSize = 14.sp,
                                color = onBackgroundColor.copy(alpha = 0.6f),
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(24.dp))
                            GlassButton(
                                onClick = onJijiClick,
                                containerColor = BeiAccentGreen,
                                contentColor = Color.White,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(stringResource(R.string.open_jiji), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer {
                                    rotationZ = -10f
                                    translationX = 10f
                                }
                        ) {
                            AppLogo(size = 90.dp, cornerRadius = 24.dp, fontSize = 48)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchingTab(
    watchlist: List<TrackedItem>,
    topDrops: List<TrackedItem>,
    onDelete: (TrackedItem) -> Unit,
    navController: NavController,
    numberFormat: NumberFormat
) {
    if (watchlist.isEmpty() && topDrops.isEmpty()) {
        EmptyWatchlistPlaceholder { navController.navigate("search") }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (topDrops.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = BeiAccentGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.top_price_drops),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            item {
                val bestDrop = topDrops.first()
                DailyDealCard(bestDrop, numberFormat) {
                    navController.navigate("product_detail?url=${Uri.encode(bestDrop.url)}")
                }
            }
        }

        if (watchlist.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.your_watchlist),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    letterSpacing = (-0.5).sp
                )
            }

            items(watchlist, key = { it.url }) { item ->
                WatchlistItemCard(item, { onDelete(item) }) {
                    navController.navigate("product_detail?url=${Uri.encode(item.url)}")
                }
            }
        }
    }
}

@Composable
fun ExploreTab(
    uiState: HomeUiState,
    navController: NavController,
    numberFormat: NumberFormat
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(uiState.categories) { category ->
            CategorySection(category.title, category.items, navController, numberFormat)
        }

        item {
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    items: List<TrackedItem>,
    navController: NavController,
    numberFormat: NumberFormat
) {
    var expanded by remember { mutableStateOf(true) }
    
    val categorySlug = remember(title) {
        title.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase().trim('_')
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (expanded) {
                Text(
                    stringResource(R.string.see_all),
                    color = BeiAccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("category/$categorySlug")
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    CategoryProductCard(item, numberFormat) {
                        navController.navigate("product_detail?url=${Uri.encode(item.url)}")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProductCard(item: TrackedItem, numberFormat: NumberFormat, onClick: () -> Unit) {
    val scoreResult = remember(item) { BuyScoreEngine.calculateScore(item) }
    val statusColor = when (scoreResult.recommendation) {
        "BUY" -> BeiAccentGreen
        "WAIT" -> Color(0xFFF59E0B)
        else -> BeiPriceDropRed
    }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick() }
            .graphicsLayer {
                shadowElevation = 8f
                alpha = 0.98f
            }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    )
                }

                val drop = item.flashSaleDropPercent
                if (drop >= 5) {
                    Surface(
                        color = BeiPriceDropRed,
                        shape = RoundedCornerShape(bottomEnd = 12.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            "${drop.toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(
                    item.name,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(36.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${numberFormat.format(item.currentPrice)}/=",
                        fontWeight = FontWeight.Black,
                        color = BeiAccentGreen,
                        fontSize = 14.sp
                    )
                    
//                    if (item.history.size >= 2) {
//                        MiniSparklineChart(
//                            prices = item.history,
//                            color = statusColor.copy(alpha = 0.6f),
//                            modifier = Modifier.size(width = 40.dp, height = 20.dp)
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistItemCard(item: TrackedItem, onDelete: () -> Unit, onClick: () -> Unit) {
    val scoreResult = remember(item) { BuyScoreEngine.calculateScore(item) }
    val statusColor = when (scoreResult.recommendation) {
        "BUY" -> BeiAccentGreen
        "WAIT" -> Color(0xFFF59E0B)
        else -> BeiPriceDropRed
    }

    val change = item.changePercent ?: 0.0
    val isPriceDrop = change < 0
    val flashDrop = item.flashSaleDropPercent

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    stringResource(R.string.remove_watchlist_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(R.string.remove_watchlist_desc, item.name),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.remove), color = BeiPriceDropRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                shadowElevation = 10f
                alpha = 0.98f
            }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
            ) {
                if (item.imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.formattedCurrentPrice,
                        fontWeight = FontWeight.Black,
                        color = BeiAccentGreen,
                        fontSize = 15.sp
                    )

                    if (flashDrop >= 5) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = BeiPriceDropRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, BeiPriceDropRed.copy(alpha = 0.2f))
                        ) {
                            Text(
                                stringResource(R.string.price_drop_label, flashDrop.toInt()),
                                color = BeiPriceDropRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (change != 0.0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isPriceDrop) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = if (isPriceDrop) BeiAccentGreen else BeiPriceDropRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${abs(change).toInt()}%",
                            color = if (isPriceDrop) BeiAccentGreen else BeiPriceDropRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (item.history.size >= 2) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                    ) {
                        MiniSparklineChart(
                            prices = item.history,
                            color = statusColor.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AlertsTab(
    alerts: List<PriceAlertEntity>,
    onMarkAllRead: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    if (alerts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                    shape = CircleShape,
                    modifier = Modifier.size(80.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(20.dp)
                            .size(40.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.no_alerts_yet),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.no_alerts_desc),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.recent_activity),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                    val hasUnread = alerts.any { !it.isRead }
                    if (hasUnread) {
                        Text(
                            stringResource(R.string.mark_all_read),
                            color = BeiAccentGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onMarkAllRead() }
                        )
                    }
                }
            }

            items(alerts) { alert ->
                FiredAlertCard(
                    alert = alert,
                    numberFormat = numberFormat,
                    dateFormat = dateFormat
                ) {
                    onAlertClick(alert.productUrl)
                }
            }
        }
    }
}

@Composable
fun EmptyWatchlistPlaceholder(onSearch: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo(size = 120.dp, cornerRadius = 32.dp, fontSize = 64)

        Spacer(Modifier.height(32.dp))
        Text(
            stringResource(R.string.track_first_item),
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.track_desc),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(40.dp))
        GlassButton(
            onClick = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            containerColor = BeiAccentGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.search_products), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}
