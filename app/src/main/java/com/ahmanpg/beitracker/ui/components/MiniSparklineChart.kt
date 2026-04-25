package com.ahmanpg.beitracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ahmanpg.beitracker.ui.theme.BeiAccentGreen

@Composable
fun MiniSparklineChart(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = BeiAccentGreen
) {
    if (prices.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 1.0
        val priceRange = (maxPrice - minPrice).coerceAtLeast(1.0)

        val points = prices.mapIndexed { index, price ->
            val x = (index.toFloat() / (prices.size - 1)) * width
            val y = height - (((price - minPrice) / priceRange).toFloat() * height)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    p2.x, p2.y
                )
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.2f),
                    color.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            ),
            style = Fill
        )

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
