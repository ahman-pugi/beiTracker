package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PriceChart(
    prices: List<Double>,
    modifier: Modifier = Modifier
) {
    if (prices.size < 2) {
        Box(
            modifier = modifier.background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Data bado haitoshi kuonyesha grafu")
        }
        return
    }

    Canvas(modifier = modifier.background(Color(0xFFF8F9FA))) {
        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 0.0
        val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0

        val width = size.width
        val height = size.height

        val path = Path()

        prices.forEachIndexed { index, price ->
            val x = (index.toFloat() / (prices.size - 1)) * width
            val normalized = ((price - minPrice) / priceRange).toFloat()
            val y = height * (1 - normalized)   // invert Y axis

            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        // Draw line
        drawPath(
            path = path,
            color = Color(0xFF1976D2),
            style = Stroke(width = 4f)
        )

        // Optional: Draw dots
        prices.forEachIndexed { index, price ->
            val x = (index.toFloat() / (prices.size - 1)) * width
            val normalized = ((price - minPrice) / priceRange).toFloat()
            val y = height * (1 - normalized)

            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFF1976D2),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}