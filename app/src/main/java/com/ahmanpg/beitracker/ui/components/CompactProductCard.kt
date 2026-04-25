package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.theme.*
import java.text.NumberFormat

@Composable
fun CompactProductCard(
    item: TrackedItem,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    isTracked: Boolean = false,
    maxValue: Double? = null
) {
    val uriHandler = LocalUriHandler.current
    val change = item.changePercent ?: 0.0
    val isDown = change < 0
    
    val isBestPrice = change <= -15.0
    val isGoodDeal = change < 0 && change > -15.0
    
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Convert to PricePoints for true scale support
    val chartDataPoints = remember(item.priceHistory, item.currentPrice, item.history) {
        if (item.priceHistory.isNotEmpty()) {
            item.priceHistory.map { PricePoint(it.timestamp, it.price) }
        } else if (item.history.size >= 2) {
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L
            item.history.mapIndexed { index, price ->
                PricePoint(now - (item.history.size - 1 - index) * day, price)
            }
        } else {
            val now = System.currentTimeMillis()
            val day = 24 * 60 * 60 * 1000L
            listOf(
                PricePoint(now - 2 * day, item.currentPrice * 1.05),
                PricePoint(now - day, item.currentPrice * 0.95),
                PricePoint(now, item.currentPrice)
            )
        }
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = onSurfaceColor.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(onSurfaceColor.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (isBestPrice || isGoodDeal) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart),
                            color = if (isBestPrice) BeiAccentGreen else Color(0xFFF59E0B),
                            shape = RoundedCornerShape(bottomEnd = 10.dp)
                        ) {
                            Text(
                                if (isBestPrice) "BEST" else "DEAL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = onSurfaceColor,
                            modifier = Modifier.weight(1f)
                        )
                        item.manufactureYear?.let { year ->
                            Surface(
                                color = onSurfaceColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    year.toString(),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Surface(
                            color = if (item.source == "Jiji") Color(0xFFFEF3C7).copy(alpha = 0.1f) else Color(0xFFDCFCE7).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, 
                                if (item.source == "Jiji") Color(0xFFD97706).copy(alpha = 0.3f) else Color(0xFF166534).copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                item.source,
                                color = if (item.source == "Jiji") Color(0xFFD97706) else Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "${numberFormat.format(item.currentPrice)}/=",
                        color = BeiAccentGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )

                    if (change != 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Text(
                                "${if (isDown) "▼" else "▲"} ${Math.abs(change).toInt()}%",
                                color = if (isDown) BeiAccentGreen else BeiPriceDropRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isDown) "drop" else "up",
                                fontSize = 11.sp,
                                color = onSurfaceColor.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onTrackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isTracked) BeiAccentGreen else onSurfaceColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassButton(
                    onClick = { uriHandler.openUri(item.url) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    containerColor = onSurfaceColor,
                    contentColor = surfaceColor
                ) {
                    Text("Visit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                GlassButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    containerColor = onSurfaceColor.copy(alpha = 0.08f),
                    contentColor = onSurfaceColor
                ) {
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun TrendProductCard(
    item: TrackedItem,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTrackClick: () -> Unit = {},
    isTracked: Boolean = false
) {
    val uriHandler = LocalUriHandler.current
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val change = item.changePercent ?: 0.0
    val isDown = change < 0

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = onSurfaceColor.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(onSurfaceColor.copy(alpha = 0.03f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("📦", fontSize = 32.sp)
                    }
                }

                IconButton(
                    onClick = onTrackClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(surfaceColor.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (isTracked) BeiAccentGreen else onSurfaceColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (item.source.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                        color = (if (item.source == "Jiji") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            item.source,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (item.source == "Jiji") Color(0xFFD97706) else Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = onSurfaceColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${numberFormat.format(item.currentPrice)}/=",
                color = BeiAccentGreen,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                maxLines = 1,
                softWrap = false
            )

            if (change != 0.0) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        "${if (isDown) "▼" else "▲"} ${Math.abs(change).toInt()}%",
                        color = if (isDown) BeiAccentGreen else BeiPriceDropRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isDown) "drop" else "up",
                        fontSize = 10.sp,
                        color = onSurfaceColor.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton(
                    onClick = { uriHandler.openUri(item.url) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    containerColor = onSurfaceColor,
                    contentColor = surfaceColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Visit", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }
                
                GlassButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    containerColor = onSurfaceColor.copy(alpha = 0.08f),
                    contentColor = onSurfaceColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}
