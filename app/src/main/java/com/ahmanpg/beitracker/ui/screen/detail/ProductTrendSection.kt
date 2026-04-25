package com.ahmanpg.beitracker.ui.screen.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.components.SparklineChart
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen
import com.ahmanpg.beitracker.ui.theme.BeiPriceDropRed
import com.ahmanpg.beitracker.viewmodel.MarketStats
import java.text.NumberFormat

@Composable
fun PriceTrendIndicator(item: TrackedItem, numberFormat: NumberFormat) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val change = item.changePercent ?: 0.0
        val isDrop = change < 0
        
        if (change != 0.0) {
            Icon(
                if (isDrop) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = if (isDrop) BeiAccentGreen else BeiPriceDropRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "${Math.abs(change).toInt()}% ${if (isDrop) "lower" else "higher"} than last check",
                color = if (isDrop) BeiAccentGreen else BeiPriceDropRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text("Price is holding steady", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChartSurface(stats: MarketStats, item: TrackedItem, selectedFilter: String) {
    Surface(
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().height(240.dp)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            val isDropping = (item.flashSaleDropPercent) > 0
            
            // Filter points based on selected filter
            val filteredPoints = when (selectedFilter) {
                "7D" -> stats.historyPoints.takeLast(7)
                "30D" -> stats.historyPoints.takeLast(30)
                "90D" -> stats.historyPoints.takeLast(90)
                else -> stats.historyPoints
            }
            
            SparklineChart(
                dataPoints = filteredPoints,
                color = if (isDropping) BeiAccentGreen else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxSize().padding(bottom = 20.dp),
                minYValue = stats.minPrice,
                maxYValue = stats.maxPrice,
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
