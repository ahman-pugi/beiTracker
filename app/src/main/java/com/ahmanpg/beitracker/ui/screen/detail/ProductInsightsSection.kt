package com.ahmanpg.beitracker.ui.screen.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.viewmodel.MarketStats
import java.util.Locale
import kotlin.math.abs

@Composable
fun InsightsGrid(
    stats: MarketStats, 
    statusColor: Color, 
    similarItems: List<TrackedItem>,
    onItemClick: (TrackedItem) -> Unit
) {
    val minItem = similarItems.minByOrNull { it.currentPrice }
    val maxItem = similarItems.maxByOrNull { it.currentPrice }
    val avgItem = similarItems.minByOrNull { abs(it.currentPrice - stats.averagePrice) }

    val tolerance = stats.averagePrice * 0.05 // 5% tolerance for "close enough" matching

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InsightCard(
                label = "LOWEST", 
                value = "${formatPriceCompact(stats.minPrice)}/=", 
                modifier = Modifier.weight(1f), 
                valueColor = statusColor,
                badge = if (minItem != null && abs(minItem.currentPrice - stats.minPrice) < tolerance) "MIN" else null,
                onClick = minItem?.let { item -> { onItemClick(item) } }
            )
            InsightCard(
                label = "HIGHEST", 
                value = "${formatPriceCompact(stats.maxPrice)}/=", 
                modifier = Modifier.weight(1f), 
                valueColor = MaterialTheme.colorScheme.onBackground,
                badge = if (maxItem != null && abs(maxItem.currentPrice - stats.maxPrice) < tolerance) "MAX" else null,
                onClick = maxItem?.let { item -> { onItemClick(item) } }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InsightCard(
                label = "AVERAGE", 
                value = "${formatPriceCompact(stats.averagePrice)}/=",                 modifier = Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                badge = if (avgItem != null) "AVG" else null,
                onClick = avgItem?.let { item -> { onItemClick(item) } }
            )
            InsightCard(
                label = "STABILITY", 
                value = stats.stability.uppercase(), 
                modifier = Modifier.weight(1f), 
                valueColor = stats.stabilityColor
            )
        }
    }
}

@Composable
fun InsightCard(
    label: String, 
    value: String, 
    modifier: Modifier, 
    valueColor: Color,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = modifier
            .height(86.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label, 
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                if (badge != null) {
                    Surface(
                        color = valueColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            badge,
                            color = valueColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(value, color = valueColor, fontWeight = FontWeight.Black, fontSize = 17.sp)
        }
    }
}

@Composable
fun SellerInsightCard(item: TrackedItem) {
    val rating = item.rating ?: 4.5
    
    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "VERIFIED MERCHANT", 
                style = MaterialTheme.typography.labelMedium, 
                color = BeiAccentGreen, 
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
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

fun formatPriceCompact(price: Double): String {
    return when {
        price >= 1_000_000 -> String.format(Locale.US, "%.1fM", price / 1_000_000).replace(".0", "")
        price >= 1_000 -> "${(price / 1_000).toInt()}K"
        else -> price.toInt().toString()
    }
}
