package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahmanpg.beitracker.data.model.TrackedItem
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailBottomSheet(
    item: TrackedItem,
    onDismiss: () -> Unit,
    numberFormat: NumberFormat
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.url.take(50) + if (item.url.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = item.formattedCurrentPrice,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                val change = item.changePercent ?: 0.0
                Text(
                    text = "${if (change >= 0) "↑" else "↓"} ${String.format("%.1f", change)}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (change >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeframe Selector
            Text("Grafu ya Bei", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val timeframes = listOf("1D", "7D", "30D", "90D", "All")
            var selectedTimeframe by remember { mutableStateOf("30D") }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                timeframes.forEach { tf ->
                    FilterChip(
                        selected = selectedTimeframe == tf,
                        onClick = { selectedTimeframe = tf },
                        label = { Text(tf) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Price Chart (Half Screen)
            PriceChart(
                prices = item.history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* TODO: Set Alert Price */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Weka Bei ya Tahadhari")
                }

                OutlinedButton(
                    onClick = { /* TODO: Toggle Favorite */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Weka kama Favorite")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}